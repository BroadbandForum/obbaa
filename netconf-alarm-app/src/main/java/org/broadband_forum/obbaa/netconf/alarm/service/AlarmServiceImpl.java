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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.broadband_forum.obbaa.netconf.alarm.api.AlarmChange;
import org.broadband_forum.obbaa.netconf.alarm.api.AlarmChangeType;
import org.broadband_forum.obbaa.netconf.alarm.api.AlarmInfo;
import org.broadband_forum.obbaa.netconf.alarm.api.AlarmService;
import org.broadband_forum.obbaa.netconf.alarm.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.alarm.entity.Alarm;
import org.broadband_forum.obbaa.netconf.alarm.util.AlarmConstants;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.DefaultConcurrentHashMap;
import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.PagingInput;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.opendaylight.yangtools.yang.common.QName;

public class AlarmServiceImpl implements AlarmService {

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(AlarmServiceImpl.class, LogAppNames.NETCONF_ALARM);
    private static final long QUEUE_CLEAR_DELAY = 500L;
    private static final String QUEUE_CLEAR_THREAD = "Clear Alarm Queue";
    private final PersistenceManagerUtil m_persistenceManagerUtil;
    private final AlarmQueue m_alarmQueue;
    private DefaultConcurrentHashMap<QName, HashSet<String>> m_alarmNotificationQNameToTypeVersionsMap =
            new DefaultConcurrentHashMap<>(new HashSet<String>(), true);
    private AlarmQueueThread m_alarmQueueThread;

    public AlarmServiceImpl(PersistenceManagerUtil persistenceManagerUtil, AlarmQueue alarmQueue) {
        m_persistenceManagerUtil = persistenceManagerUtil;
        m_alarmQueue = alarmQueue;
    }

    public void init() {
        m_alarmQueueThread = new AlarmQueueThread(m_alarmQueue);
        m_alarmQueueThread.start();
    }

    public void close() {
        if (m_alarmQueueThread != null) {
            m_alarmQueueThread.stopWorking();
        }
    }

    // for UT purposes
    AlarmQueueThread getAlarmQueueThread() {
        return m_alarmQueueThread;
    }

    // for UT purposes
    AlarmQueue getAlarmQueue() {
        return m_alarmQueue;
    }

    @Override
    public void raiseAlarm(AlarmInfo alarm) {
        AlarmChange alarmChange = AlarmChange.createFromAlarmInfo(AlarmChangeType.RAISE_OR_UPDATE, alarm);
        m_alarmQueue.addAlarmChanges(Collections.singletonList(alarmChange));
    }

    @Override
    public void raiseAlarms(List<AlarmInfo> alarmInfoList) {
        for (AlarmInfo alarmInfo : alarmInfoList) {
            AlarmChange alarmChange = AlarmChange.createFromAlarmInfo(AlarmChangeType.RAISE_OR_UPDATE, alarmInfo);
            m_alarmQueue.addAlarmChanges(Collections.singletonList(alarmChange));
        }
    }

    @Override
    public void clearAlarm(AlarmInfo alarm) {
        AlarmChange alarmChange = AlarmChange.createFromAlarmInfo(AlarmChangeType.CLEAR, alarm);
        m_alarmQueue.addAlarmChanges(Collections.singletonList(alarmChange));
    }

    @Override
    public void clearAlarms(List<AlarmInfo> alarmInfoList) {
        for (AlarmInfo alarmInfo : alarmInfoList) {
            AlarmChange alarmChange = AlarmChange.createFromAlarmInfo(AlarmChangeType.CLEAR, alarmInfo);
            m_alarmQueue.addAlarmChanges(Collections.singletonList(alarmChange));
        }
    }

    @Override
    @Transactional(value = TxType.REQUIRED, rollbackOn = {RuntimeException.class})
    public List<Alarm> getAlarms(PagingInput pagingInput) {
        return getAlarms(pagingInput, null);
    }

    @Override
    @Transactional(value = TxType.REQUIRED, rollbackOn = {RuntimeException.class})
    public List<Alarm> getAlarms(PagingInput pagingInput, String deviceId) {
        List<Alarm> alarms = new ArrayList<>();
        try {
            if (pagingInput.getFirstResult() != AlarmConstants.ALARM_END_OF_PAGING) {
                EntityDataStoreManager manager = m_persistenceManagerUtil.getEntityDataStoreManager();

                // increase max results by 1, to see if there are more results
                int originalMaxResult = pagingInput.getMaxResult();
                pagingInput.setMaxResult(originalMaxResult + 1);

                Map<String, Object> matchValues = new HashMap<>();
                if (deviceId != null && !deviceId.isEmpty()) {
                    matchValues.put(AlarmConstants.DEVICE_ID, deviceId);
                }
                alarms = manager.findWithPaging(Alarm.class, pagingInput, matchValues);
                pagingInput.setMaxResult(originalMaxResult);

                boolean hasMoreResults = false;
                if (alarms.size() == originalMaxResult + 1) {
                    hasMoreResults = true;
                    alarms.remove(alarms.size() - 1);
                }

                /**
                 * If there are no more rows to be fetched,
                 * -1 has to be set as the next paging start index, indicating no more paging possible
                 **/
                if (hasMoreResults) {
                    pagingInput.setFirstResult(originalMaxResult + pagingInput.getFirstResult());
                } else {
                    pagingInput.setFirstResult(AlarmConstants.ALARM_END_OF_PAGING);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to get alarms", e);
        }
        return alarms;
    }

    @Override
    public void clearAlarmsOnNodeAndSubtree(ModelNodeId identifier, String mountKey) {
        AlarmChange alarmChange = AlarmChange.createForClearSubtree(identifier, mountKey);
        m_alarmQueue.addAlarmChanges(Collections.singletonList(alarmChange));
    }

    @Override
    public void clearAlarmsOnDevice(String deviceId) {
        AlarmChange alarmChange = AlarmChange.createForClearSubtree(deviceId);
        m_alarmQueue.addAlarmChanges(Collections.singletonList(alarmChange));
    }

    @Override
    @Transactional(value = TxType.REQUIRED, rollbackOn = {RuntimeException.class})
    public List<Alarm> getAlarmsUnderResource(String resourceDbIdLike) {
        try {
            EntityDataStoreManager manager = m_persistenceManagerUtil.getEntityDataStoreManager();
            List<Alarm> existingBaaAlarms = manager.findByLike(Alarm.class, Alarm.SOURCE_OBJECT, resourceDbIdLike);
            return existingBaaAlarms;
        } catch (Exception e) {
            throw new RuntimeException("Unable to get existing baa alarms", e);
        }
    }

    @Override
    public void updateAlarmsForResync(List<AlarmInfo> newRaiseAlarms, List<AlarmInfo> updateAlarms, List<AlarmInfo> clearAlarms) {
        List<AlarmChange> alarmChanges = new ArrayList<>();
        for (AlarmInfo newRaiseAlarm : newRaiseAlarms) {
            alarmChanges.add(AlarmChange.createFromAlarmInfo(AlarmChangeType.RAISE, newRaiseAlarm));
        }
        for (AlarmInfo updateAlarm : updateAlarms) {
            alarmChanges.add(AlarmChange.createFromAlarmInfo(AlarmChangeType.UPDATE, updateAlarm));
        }
        for (AlarmInfo clearAlarm : clearAlarms) {
            alarmChanges.add(AlarmChange.createFromAlarmInfo(AlarmChangeType.CLEAR, clearAlarm));
        }
        m_alarmQueue.addAlarmChanges(alarmChanges);
    }

    @Override
    public void updateAlarmNotificationQNameToAdapterTypeVersion(QName alarmNotificationQName, String typeVersion) {
        Set<String> typeVersions = m_alarmNotificationQNameToTypeVersionsMap.get(alarmNotificationQName);
        typeVersions.add(typeVersion);
    }

    @Override
    public void removeAlarmNotificationQNameToAdapterTypeVersion(QName alarmNotificationQName, String typeVersion) {
        Set<String> typeVersions = m_alarmNotificationQNameToTypeVersionsMap.get(alarmNotificationQName);
        typeVersions.remove(typeVersion);
        if (typeVersions.isEmpty()) {
            m_alarmNotificationQNameToTypeVersionsMap.remove(alarmNotificationQName);
        }
    }

    @Override
    public Set<String> retrieveAdapterTypeVersionsFromAlarmNotificationQName(QName alarmNotificationQName) {
        return m_alarmNotificationQNameToTypeVersionsMap.get(alarmNotificationQName);
    }

    // package private for UT purposes
    static class AlarmQueueThread extends Thread {  // NOSONAR
        private boolean m_stop = false;
        private AlarmQueue m_alarmQueue;

        AlarmQueueThread(AlarmQueue alarmQueue) {
            super(QUEUE_CLEAR_THREAD);
            m_alarmQueue = alarmQueue;
        }

        public void stopWorking() {
            m_stop = true;
        }

        @Override
        public void run() {
            while (!m_stop) {
                doOneRun();
            }
        }

        protected void doOneRun() {
            RequestScope.withScope(new RequestScope.RsTemplate<Void>() {
                @Override
                protected Void execute() {
                    try {
                        m_alarmQueue.clearQueue();
                        Thread.sleep(QUEUE_CLEAR_DELAY);
                    } catch (Exception e) {
                        LOGGER.error(null, "Error in Alarm Queue Thread", e);
                        try {
                            Thread.sleep(QUEUE_CLEAR_DELAY);
                        } catch (InterruptedException ignored) {
                            LOGGER.error(null, "Error in Alarm Queue Thread", e);
                        }
                    }
                    return null;
                }
            });
        }
    }

}
