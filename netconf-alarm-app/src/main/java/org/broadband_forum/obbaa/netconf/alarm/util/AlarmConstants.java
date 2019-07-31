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

package org.broadband_forum.obbaa.netconf.alarm.util;

public final class AlarmConstants {

    public static final String ALARM_NAMESPACE = "urn:ietf:params:xml:ns:yang:ietf-alarms";

    public static final String ALARM_NOTIFICATION = "alarm-notification";

    public static final String ALARM_RESOURCE = "resource";

    public static final String ALARM_TYPE_ID = "alarm-type-id";

    public static final String ALARM_TYPE_QUALIFIER = "alarm-type-qualifier";

    public static final String TIME = "time";

    public static final String ALARM_PERCEIVED_SEVERITY = "perceived-severity";

    public static final String ALARM_TEXT = "alarm-text";

    public static final Integer ALARM_END_OF_PAGING = -1;

    public static final String OFFSET = "offset";

    public static final String ALARM_STREAM_NAME = "ALARM";

    public static final String ALARM_ELEMENT = "alarm";

    public static final String ALARM_PREFIX = "alarms";

    public static final String ACTIVE_ALARMS = "active-alarms";

    public static final String DEVICE_ID = "deviceId";


    private AlarmConstants(){}

}
