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

package org.broadband_forum.obbaa.netconf.alarm.entity;

public enum AlarmSeverity {

    CLEAR("cleared", 1),
    INDETERMINATE("indeterminate", 2),
    WARNING("warning", 3),
    MINOR("minor", 4),
    MAJOR("major", 5),
    CRITICAL("critical", 6);

    private final String m_name;
    private final int m_value;

    AlarmSeverity(String name, int value) {
        this.m_name = name;
        this.m_value = value;
    }

    public static AlarmSeverity getAlarmSeverity(String sev) {
        if (AlarmSeverity.INDETERMINATE.toString().equals(sev)) {
            return AlarmSeverity.INDETERMINATE;
        } else if (AlarmSeverity.CRITICAL.toString().equals(sev)) {
            return AlarmSeverity.CRITICAL;
        } else if (AlarmSeverity.MAJOR.toString().equals(sev)) {
            return AlarmSeverity.MAJOR;
        } else if (AlarmSeverity.MINOR.toString().equals(sev)) {
            return AlarmSeverity.MINOR;
        } else if (AlarmSeverity.WARNING.toString().equals(sev)) {
            return AlarmSeverity.WARNING;
        } else if (AlarmSeverity.CLEAR.toString().equals(sev)) {
            return AlarmSeverity.CLEAR;
        } else {
            return null;
        }
    }

    public String toString() {
        return m_name;
    }

    public int value() {
        return m_value;
    }
}
