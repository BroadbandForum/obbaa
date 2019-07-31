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

package org.broadband_forum.obbaa.netconf.alarm.api;

import java.util.List;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.alarm.entity.Alarm;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.persistence.PagingInput;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * Api to manager alarms.
 */
public interface AlarmService {

    void raiseAlarm(AlarmInfo alarm);

    void clearAlarm(AlarmInfo alarm);

    /**
     * Get the list of Alarms by Device Holder name and device id.
     *
     * @param pagingInput : page input.
     * @param deviceId    : device name.
     * @return {@link List} of Alarms
     */

    List<Alarm> getAlarms(PagingInput pagingInput, String deviceId);

    /**
     * Get the list of Alarms by device holder name.
     *
     * @param pagingInput : page input.
     * @return {@link List} of Alarms
     */
    List<Alarm> getAlarms(PagingInput pagingInput);

    void clearAlarmsOnNodeAndSubtree(ModelNodeId identifier, String mountKey);

    void clearAlarmsOnDevice(String deviceId);

    void updateAlarmsForResync(List<AlarmInfo> newRaiseAlarms, List<AlarmInfo> updateAlarms, List<AlarmInfo> clearAlarms);

    List<Alarm> getAlarmsUnderResource(String resourceDbIdLike);

    void updateAlarmNotificationQNameToAdapterTypeVersion(QName alarmNotificationQName, String typeVersion);

    void removeAlarmNotificationQNameToAdapterTypeVersion(QName alarmNotificationQName, String typeVersion);

    Set<String> retrieveAdapterTypeVersionsFromAlarmNotificationQName(QName alarmNotificationQName);

    void clearAlarms(List<AlarmInfo> alarmInfoList);

    void raiseAlarms(List<AlarmInfo> alarmInfoList);
}
