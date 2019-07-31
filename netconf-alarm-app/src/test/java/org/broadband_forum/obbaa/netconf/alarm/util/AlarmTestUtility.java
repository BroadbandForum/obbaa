package org.broadband_forum.obbaa.netconf.alarm.util;

import static org.junit.Assert.assertEquals;

import org.broadband_forum.obbaa.netconf.alarm.api.AlarmInfo;

public class AlarmTestUtility {

    /**
     * Assert that expected alarm info and actual alarm info are equal except raised time.
     *
     * @param expectedAlarmInfo
     * @param actualAlarmInfo
     */
    public static void assertAlarmInfoEquals(AlarmInfo expectedAlarmInfo, AlarmInfo actualAlarmInfo) {
        assertEquals(expectedAlarmInfo.getAlarmTypeId(), actualAlarmInfo.getAlarmTypeId());
        assertEquals(expectedAlarmInfo.getSourceObject(), actualAlarmInfo.getSourceObject());
        assertEquals(expectedAlarmInfo.getSeverity(), actualAlarmInfo.getSeverity());
        assertEquals(expectedAlarmInfo.getAlarmText(), actualAlarmInfo.getAlarmText());
        assertEquals(expectedAlarmInfo.getDeviceName(), actualAlarmInfo.getDeviceName());
    }
}
