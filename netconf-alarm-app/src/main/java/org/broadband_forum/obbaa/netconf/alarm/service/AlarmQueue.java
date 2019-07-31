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
import java.util.concurrent.atomic.AtomicLong;

import org.broadband_forum.obbaa.netconf.alarm.api.AlarmChange;
import org.broadband_forum.obbaa.netconf.alarm.api.AlarmChangeType;
import org.broadband_forum.obbaa.netconf.alarm.api.AlarmInfo;
import org.broadband_forum.obbaa.netconf.alarm.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;

import com.google.common.collect.Lists;

public class AlarmQueue {

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(AlarmQueue.class, LogAppNames.NETCONF_ALARM);

    private static final String ENV_MAX_ALARM_QUEUE_SIZE = "MAX_ALARM_QUEUE_SIZE";
    private static final int MAX_ALARM_QUEUE_SIZE_DEFAULT = 1000;
    private static final int DB_BATCH_SIZE = 500;

    private List<AlarmChange> m_queue = new ArrayList<>(1000);

    private InternalAlarmService m_alarmService;

    private int m_maxQueueSize = MAX_ALARM_QUEUE_SIZE_DEFAULT;

    private AtomicLong m_droppedAlarms = new AtomicLong(0);
    private int m_queueSize = 0;

    public AlarmQueue(InternalAlarmService service) {
        m_alarmService = service;
        String maxQueueSizeValue = System.getenv(ENV_MAX_ALARM_QUEUE_SIZE);
        configureMaxQueueSize(maxQueueSizeValue);
    }

    // package private for test purposes
    void configureMaxQueueSize(String maxQueueSizeValue) {
        if (maxQueueSizeValue != null && maxQueueSizeValue.trim().length() > 0) {
            try {
                m_maxQueueSize = Integer.parseUnsignedInt(maxQueueSizeValue);
                LOGGER.info(null, "Max Alarm Queue Size Configured - " + m_maxQueueSize);
            } catch (NumberFormatException e) {
                LOGGER.info(null, "Problem parsing Max Alarm Queue Size; Using default of " + MAX_ALARM_QUEUE_SIZE_DEFAULT);
            }
        } else {
            LOGGER.info(null, "Max Alarm Queue Size Not Configured; Using default of " + MAX_ALARM_QUEUE_SIZE_DEFAULT);
        }
    }

    public synchronized void addAlarmChanges(List<AlarmChange> changes) {
        if (!changes.isEmpty()) {
            int currentQueueSize = m_queue.size();
            if (currentQueueSize + changes.size() > m_maxQueueSize) {
                int maxNumberToBeAdded = m_maxQueueSize - currentQueueSize;
                int dropped = changes.size() - maxNumberToBeAdded;
                LOGGER.error(null, "addAlarmChanges() queue overflow: dropping {} events", dropped);
                changes = changes.subList(0, maxNumberToBeAdded);
                m_droppedAlarms.addAndGet(dropped);
            }
            if (!changes.isEmpty()) {
                LOGGER.trace(null, "addAlarmChanges(): adding {} events", changes.size());
                m_queue.addAll(changes);
                m_queueSize = m_queue.size();
            }
        }
    }

    public void clearQueue() {
        List<AlarmChange> changes = Collections.emptyList();
        LOGGER.trace(null, "clearQueue(): trying to acquire lock");
        synchronized (this) {
            if (!m_queue.isEmpty()) {
                changes = m_queue;
                m_queue = new ArrayList<>(1000);
                m_queueSize = 0;
            }
        }
        try {
            if (!changes.isEmpty()) {
                cleanupQueue(changes);
            }
            if (!changes.isEmpty()) {
                LOGGER.debug(null, "clearQueue(): flushing a total of {} events", changes.size());
                for (List<AlarmChange> batchList : Lists.partition(changes, DB_BATCH_SIZE)) {
                    long startTime = System.currentTimeMillis();
                    LOGGER.debug(null, "clearQueue(): flushing {} events", batchList.size());
                    m_alarmService.processChanges(batchList);
                    long delta = System.currentTimeMillis() - startTime;
                    LOGGER.debug(null, "clearQueue(): flushing {} events finished in {} ms", batchList.size(), delta);
                }
            }
        } catch (Exception e) {
            LOGGER.error(null, "clearQueue() exception", e);
        }
    }

    private void cleanupQueue(List<AlarmChange> changes) {
        cleanupQueueForClearSubtree(changes);
        cleanupQueueForTogglingAlarm(changes);
    }

    // remove all alarm changes from the queue for the clear subtree events (before that event)
    private void cleanupQueueForClearSubtree(List<AlarmChange> changes) {
        Set<Integer> cleanupSet = new HashSet<>();
        int index = 0;
        for (AlarmChange change : changes) {
            if (AlarmChangeType.CLEAR_SUBTREE.equals(change.getType())) {
                cleanupSet.addAll(getSubtreeChangesFromQueue(changes, change.getSubtree(), change.getDeviceId(), index));
            }
            index++;
        }
        if (!cleanupSet.isEmpty()) {
            removeByIndices(changes, cleanupSet);
            LOGGER.info(null, "cleanupQueueForClearSubtree(): suppressed {} events from queue", cleanupSet.size());
        }
    }

    private Set<Integer> getSubtreeChangesFromQueue(List<AlarmChange> changes, ModelNodeId subtree, String deviceId,
                                                    Integer untilIndex) {
        Set<Integer> subtreeChanges = new HashSet<>();
        if (subtree != null || deviceId != null) {
            int index = 0;
            for (AlarmChange change : changes) {
                if (untilIndex.equals(index)) {
                    break;
                }
                AlarmInfo alarmInfo = change.getAlarmInfo();
                if (alarmInfo != null && alarmInfo.getSourceObject() != null && alarmInfo.getSourceObject().beginsWith(subtree)) {
                    subtreeChanges.add(index);
                } else if (alarmInfo != null && alarmInfo.getDeviceName() != null && alarmInfo.getDeviceName().equals(deviceId)) {
                    subtreeChanges.add(index);
                } else if (alarmInfo == null && change.getDeviceId() != null && change.getDeviceId().equals(deviceId)) {
                    subtreeChanges.add(index);
                }
                index++;
            }
        }
        return subtreeChanges;
    }

    // only keep the last alarm change for a certain alarm type/source object
    private void cleanupQueueForTogglingAlarm(List<AlarmChange> changes) {
        Map<String, Integer> lastAlarmChangeIndex = new HashMap<>();
        Map<String, Boolean> lastAlarmChangeIsRaised = new HashMap<>();
        Set<Integer> cleanupSet = new HashSet<>();
        int index = 0;
        for (AlarmChange change : changes) {
            AlarmInfo info = change.getAlarmInfo();
            boolean isRaised = isRaised(change);
            if (info != null) {
                String typeId = info.getAlarmTypeId();
                String alarmTypeQualifier = info.getAlarmTypeQualifier();
                if (alarmTypeQualifier != null && !alarmTypeQualifier.isEmpty()) {
                    typeId = typeId + alarmTypeQualifier;
                }
                String sourceObejct = info.getSourceObject() != null ? info.getSourceObject().getModelNodeIdAsString()
                        : info.getSourceObjectString();
                String key = typeId + sourceObejct;
                Integer previousChangeForAlarm = lastAlarmChangeIndex.get(key);
                if (previousChangeForAlarm != null) {
                    cleanupSet.add(previousChangeForAlarm);
                    // set this to be able to handle raise/clear specially
                    // the alarm change should be set to toggling if:
                    //  - either we marked the previous change already as toggling
                    //  - or the previous change was a raise and now it's a clear, or vice versa
                    if (changes.get(previousChangeForAlarm).isToggling() || isRaised != lastAlarmChangeIsRaised.get(key)) {
                        change.setToggling(true);
                    }
                }
                lastAlarmChangeIndex.put(key, index);
                lastAlarmChangeIsRaised.put(key, isRaised(change));
            }
            index++;
        }
        if (!cleanupSet.isEmpty()) {
            removeByIndices(changes, cleanupSet);
            LOGGER.info(null, "cleanupQueueForTogglingAlarm(): suppressed {} events from queue", cleanupSet.size());
        }
    }

    private boolean isRaised(AlarmChange change) {
        AlarmChangeType type = change.getType();
        return type.equals(AlarmChangeType.RAISE) || type.equals(AlarmChangeType.RAISE_OR_UPDATE) || type.equals(AlarmChangeType.UPDATE);
    }

    private void removeByIndices(List<AlarmChange> changes, Set<Integer> cleanupSet) {
        List<Integer> cleanupList = new ArrayList<>(cleanupSet);
        Collections.sort(cleanupList);
        Collections.reverse(cleanupList);
        for (Integer index : cleanupList) {
            changes.remove(index.intValue());
        }
    }

    public long getDroppedAlarms() {
        return m_droppedAlarms.get();
    }

    public long getAlarmQueueSize() {
        return m_queueSize;
    }

    // For UT
    protected int getMaxQueueSize() {
        return m_maxQueueSize;
    }
}
