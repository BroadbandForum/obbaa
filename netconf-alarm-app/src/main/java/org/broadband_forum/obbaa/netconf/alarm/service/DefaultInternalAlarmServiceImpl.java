/*
 * Copyright 2018 Broadband Forum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadband_forum.obbaa.netconf.alarm.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.persistence.LockModeType;

import org.broadband_forum.obbaa.netconf.alarm.api.AlarmChange;
import org.broadband_forum.obbaa.netconf.alarm.api.AlarmChangeType;
import org.broadband_forum.obbaa.netconf.alarm.api.AlarmCondition;
import org.broadband_forum.obbaa.netconf.alarm.api.AlarmInfo;
import org.broadband_forum.obbaa.netconf.alarm.api.AlarmNotification;
import org.broadband_forum.obbaa.netconf.alarm.api.AlarmWithCondition;
import org.broadband_forum.obbaa.netconf.alarm.api.DefaultAlarmStateChangeNotification;
import org.broadband_forum.obbaa.netconf.alarm.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.alarm.entity.Alarm;
import org.broadband_forum.obbaa.netconf.alarm.util.AlarmConstants;
import org.broadband_forum.obbaa.netconf.alarm.util.AlarmUtil;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.TxService;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.TxTemplate;
import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;

public class DefaultInternalAlarmServiceImpl implements InternalAlarmService {
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(DefaultInternalAlarmServiceImpl.class,
            LogAppNames.NETCONF_ALARM);

    private final SchemaRegistry m_schemaRegistry;
    private final PersistenceManagerUtil m_persistenceManagerUtil;
    private final NotificationService m_notificationService;
    private final TxService m_txService;
    private DefaultAlarmStateChangeNotification m_notification;
    private AtomicLong m_raisedAlarms = new AtomicLong(0);
    private AtomicLong m_clearedAlarms = new AtomicLong(0);
    private AtomicLong m_updatedAlarms = new AtomicLong(0);
    private AtomicLong m_togglingAlarms = new AtomicLong(0);
    private ModelNodeDataStoreManager m_dsm;

    public DefaultInternalAlarmServiceImpl(SchemaRegistry schemaRegistry, PersistenceManagerUtil persistenceManagerUtil,
                                           ModelNodeDataStoreManager dsm, NotificationService notificationService, TxService txService) {
        m_schemaRegistry = schemaRegistry;
        m_persistenceManagerUtil = persistenceManagerUtil;
        m_notificationService = notificationService;
        m_txService = txService;
        m_dsm = dsm;
    }

    // Used only for UT
    DefaultAlarmStateChangeNotification getNotification() {
        return m_notification;
    }

    @Override
    public void processChanges(List<AlarmChange> alarmChanges) {
        m_txService.executeWithTxRequiresNew((TxTemplate<Void>) () -> {
            List<AlarmWithCondition> notificationList = new ArrayList<>();
            EntityDataStoreManager manager = m_persistenceManagerUtil.getEntityDataStoreManager();
            try {
                for (AlarmChange alarmChange : alarmChanges) {
                    AlarmChangeType alarmChangeType = alarmChange.getType();
                    switch (alarmChangeType) {
                        case RAISE:
                        case RAISE_OR_UPDATE:
                        case UPDATE:
                            handleRaiseOrUpdate(manager, alarmChange, notificationList);
                            break;
                        case CLEAR:
                            handleClear(manager, alarmChange, notificationList);
                            break;
                        case CLEAR_SUBTREE:
                            handleClearSubtree(manager, alarmChange, notificationList);
                            break;
                        default:
                            throw new RuntimeException("Unhandled alarm change type: " + alarmChangeType);
                    }
                }
                sendNotification(notificationList);
            } catch (Exception e) {
                LOGGER.error(null, "Problem in handling alarm changes: {}", alarmChanges);
                throw new RuntimeException("Unable to raise/update/clear alarms", e);
            }
            return null;
        });
    }

    private void handleRaiseOrUpdate(EntityDataStoreManager manager, AlarmChange alarmChange, List<AlarmWithCondition> notificationList) {
        AlarmInfo alarmInfo = alarmChange.getAlarmInfo();
        Pair<Alarm, Boolean> alarmFromInfo = getAlarmFromInfo(alarmInfo, manager, alarmChange.getType());
        if (alarmFromInfo != null) {
            Alarm alarm = alarmFromInfo.getFirst();
            boolean isNewAlarm = alarmFromInfo.getSecond();
            if (isNewAlarm) {
                manager.create(alarm);
                notificationList.add(new AlarmWithCondition(alarm, AlarmCondition.ALARM_ON, alarmInfo.getTime()));
                LOGGER.debug(null, "Raising alarm: {}", alarm);
                m_raisedAlarms.incrementAndGet();
            } else {
                if (doUpdateOfAlarm(alarm, alarmInfo)) {
                    notificationList.add(new AlarmWithCondition(alarm, AlarmCondition.ALARM_ON, alarmInfo.getTime()));
                    LOGGER.debug(null, "The alarm {} is already currently raised, so just updating severity/additionalInfo",
                            alarmInfo.getAlarmTypeId());
                    m_updatedAlarms.incrementAndGet();
                } else {
                    LOGGER.debug(null, "The alarm {} is already currently raised", alarmInfo.getAlarmTypeId());
                }
            }
        } else {
            LOGGER.warn(null, "Could not raise/update alarm for alarm change {}", alarmChange);
        }
    }

    private boolean doUpdateOfAlarm(Alarm matchedAlarm, AlarmInfo alarmInfo) {
        boolean isUpdateAlarm = false;
        if (!matchedAlarm.getAlarmText().equals(alarmInfo.getAlarmText())) {
            matchedAlarm.setAlarmText(alarmInfo.getAlarmText());
            isUpdateAlarm = true;
        }
        if (!matchedAlarm.getSeverity().equals(alarmInfo.getSeverity())) {
            matchedAlarm.setSeverity(alarmInfo.getSeverity());
            isUpdateAlarm = true;
        }
        return isUpdateAlarm;
    }

    private void handleClear(EntityDataStoreManager manager, AlarmChange alarmChange, List<AlarmWithCondition> notificationList) {
        AlarmInfo alarmInfo = alarmChange.getAlarmInfo();
        Pair<Alarm, Boolean> alarmFromInfo = getAlarmFromInfo(alarmInfo, manager, alarmChange.getType());
        if (alarmFromInfo != null) {
            Alarm matchedAlarm = alarmFromInfo.getFirst();
            manager.delete(matchedAlarm);
            String resourceObject;
            if (alarmInfo.getSourceObject() != null) {
                resourceObject = alarmInfo.getSourceObject().xPathString();
            } else {
                resourceObject = alarmInfo.getSourceObjectString();
            }
            Alarm clearedAlarm = AlarmUtil.convertToAlarmEntity(alarmInfo, resourceObject);
            notificationList.add(new AlarmWithCondition(clearedAlarm, AlarmCondition.ALARM_OFF, alarmInfo.getTime(),
                    alarmChange.getMountKey()));
            LOGGER.debug(null, "Clearing alarm: {}", matchedAlarm);
            m_clearedAlarms.incrementAndGet();
        } else if (alarmChange.isToggling()) {
            // special case: raise/clear -> we need to send those events, but not store in DB
            Pair<Alarm, Boolean> toggledAlarm = getAlarmFromInfo(alarmInfo, manager, AlarmChangeType.RAISE);
            if (toggledAlarm != null) {
                notificationList.add(new AlarmWithCondition(toggledAlarm.getFirst(), AlarmCondition.ALARM_ON,
                        alarmInfo.getTime(), alarmChange.getMountKey()));
                notificationList.add(new AlarmWithCondition(toggledAlarm.getFirst(), AlarmCondition.ALARM_OFF,
                        alarmInfo.getTime(), alarmChange.getMountKey()));
                LOGGER.debug(null, "Sending raise and clear notification for alarm: {}", toggledAlarm);
                m_togglingAlarms.incrementAndGet();
            } else {
                LOGGER.warn(null, "Could not send raise/clear notification for alarm {}", alarmInfo);
            }
        } else {
            LOGGER.debug(null, "The alarm is currently not raised {}", alarmInfo.getAlarmTypeId());
        }
    }

    private void handleClearSubtree(EntityDataStoreManager manager, AlarmChange alarmChange,
                                    List<AlarmWithCondition> notificationList) {
        Map<String, String> matchValue = new HashMap<String, String>();
        if (alarmChange.getSubtree() != null) {
            ModelNodeId subtree = alarmChange.getSubtree();
            String dbId = AlarmUtil.toDbId(m_schemaRegistry, m_dsm, subtree, AlarmUtil.getPrefixToNsMap(subtree, m_dsm,
                    m_schemaRegistry, null));
            matchValue.put(Alarm.SOURCE_OBJECT, dbId);
        } else {
            matchValue.put(Alarm.DEVICE_ID, alarmChange.getDeviceId());
        }
        List<Alarm> alarms = manager.findLike(Alarm.class, matchValue);
        Timestamp clearTime = new Timestamp(System.currentTimeMillis());
        for (Alarm alarm : alarms) {
            manager.delete(alarm);
            notificationList.add(new AlarmWithCondition(alarm, AlarmCondition.ALARM_OFF, clearTime, alarmChange.getMountKey()));
            m_clearedAlarms.incrementAndGet();
        }
        LOGGER.debug(null, "Clearing alarm on node and subtree {}", alarms);
    }

    // the returned boolean is true if it's a new alarm, false if it is an existing alarm
    private Pair<Alarm, Boolean> getAlarmFromInfo(AlarmInfo alarmInfo, EntityDataStoreManager manager, AlarmChangeType changeType) {
        String resourceObject = null;
        if (alarmInfo.getSourceObject() != null) {
            resourceObject = alarmInfo.getSourceObject().xPathString();
        } else {
            resourceObject = alarmInfo.getSourceObjectString();
        }
        if (AlarmChangeType.RAISE.equals(changeType)) {
            return new Pair<>(AlarmUtil.convertToAlarmEntity(alarmInfo, resourceObject), true);
        } else {
            Alarm matchedAlarm = getMatchedAlarm(manager, alarmInfo, resourceObject, LockModeType.PESSIMISTIC_WRITE);
            if (matchedAlarm != null) {
                return new Pair<>(matchedAlarm, false);
            } else if (AlarmChangeType.RAISE_OR_UPDATE.equals(changeType)) {
                return new Pair<>(AlarmUtil.convertToAlarmEntity(alarmInfo, resourceObject), true);
            } else {
                LOGGER.debug(null, "No matched alarm found for {}, changeType {}", alarmInfo, changeType);
            }
        }
        return null;
    }

    private Alarm getMatchedAlarm(EntityDataStoreManager manager, AlarmInfo alarmInfo, String dbId, LockModeType
            lockModeType) {

        Map<String, Object> matchValue = new HashMap<String, Object>();
        matchValue.put(Alarm.SOURCE_OBJECT, dbId);
        matchValue.put(Alarm.ALARM_TYPE_ID, alarmInfo.getAlarmTypeId());
        matchValue.put(Alarm.ALARM_TYPE_QUALIFIER, alarmInfo.getAlarmTypeQualifier());
        List<Alarm> alarmList = manager.findByMatchValue(Alarm.class, matchValue, lockModeType);
        if (alarmList.size() > 0) {
            return alarmList.get(0);
        }
        return null;
    }

    private void sendNotification(List<AlarmWithCondition> alarms) {
        if (!alarms.isEmpty()) {
            for (AlarmWithCondition alarmWithCondition : alarms) {
                AlarmNotification alarmNotification = getAlarmNotificationFor(alarmWithCondition);
                notify(alarmNotification);
            }
        }
    }

    private AlarmNotification getAlarmNotificationFor(AlarmWithCondition alarmWithCondition) {
        Alarm alarm = alarmWithCondition.getAlarm();
        AlarmCondition alarmCondition = alarmWithCondition.getAlarmCondition();
        ModelNodeId modelNodeId = null;
        String alarmSourceObject = alarm.getSourceObject();
        AlarmNotification alarmNotification = new AlarmNotification(alarm.getAlarmTypeId(),
                alarm.getAlarmTypeQualifier(), modelNodeId, alarmWithCondition.getEventTime(),
                alarm.getSeverity(), alarm.getAlarmText(), alarmCondition, alarmWithCondition.getMountKey(), alarmSourceObject);
        alarmNotification.setResourceNamespaces(alarm.getResourceNamespaces());
        alarmNotification.setResourceObjectString(alarm.getResourceName());
        return alarmNotification;
    }

    private void notify(AlarmNotification alarmNotification) {
        m_notification = new DefaultAlarmStateChangeNotification(m_schemaRegistry, m_dsm);
        m_notification.setAlarmNotification(alarmNotification);
        m_notificationService.sendNotification(AlarmConstants.ALARM_STREAM_NAME, m_notification);
    }

    public long getRaisedAlarms() {
        return m_raisedAlarms.get();
    }

    public long getClearedAlarms() {
        return m_clearedAlarms.get();
    }

    public long getUpdatedAlarms() {
        return m_updatedAlarms.get();
    }

    public long getTogglingAlarms() {
        return m_togglingAlarms.get();
    }

    public long getActiveAlarms() {
        EntityDataStoreManager entityDataStoreManager = m_persistenceManagerUtil.getEntityDataStoreManager();
        return entityDataStoreManager.countByMatchAndNotMatchValue(Alarm.class, null, null, LockModeType.NONE);
    }

}
