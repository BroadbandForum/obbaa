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

import java.sql.Timestamp;

import org.broadband_forum.obbaa.netconf.alarm.entity.Alarm;

public class AlarmWithCondition {

    private final Alarm m_alarm;
    private final AlarmCondition m_alarmCondition;
    private final Timestamp m_eventTime;
    private final String m_mountKey;

    public AlarmWithCondition(Alarm alarm, AlarmCondition alarmCondition, Timestamp eventTime, String mountKey) {
        m_alarm = alarm;
        m_alarmCondition = alarmCondition;
        m_eventTime = eventTime;
        m_mountKey = mountKey;
    }

    public AlarmWithCondition(Alarm alarm, AlarmCondition alarmCondition, Timestamp eventTime) {
        this(alarm, alarmCondition, eventTime, null);
    }

    public Alarm getAlarm() {
        return m_alarm;
    }

    public AlarmCondition getAlarmCondition() {
        return m_alarmCondition;
    }

    public Timestamp getEventTime() {
        return m_eventTime;
    }

    public String getMountKey() {
        return m_mountKey;
    }
}
