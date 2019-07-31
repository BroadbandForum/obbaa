package org.broadband_forum.obbaa.netconf.alarm.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.sql.Timestamp;
import java.util.Date;

import org.broadband_forum.obbaa.netconf.alarm.entity.AlarmSeverity;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.junit.Before;
import org.junit.Test;


public class AlarmInfoTest {

    private static final String TEST_ADDITIONAL_INFO = "testAdditionalInfo";
    private static final String TEST_ADDITIONAL_INFO2 = "testAdditionalInfo2";
    private static final String TEST_ALARM_TYPE_ID = "testAlarmTypeId";
    private static final String TEST_ALARM_TYPE_ID2 = "testAlarmTypeId2";
    private static final String TEST_DEVICE_ID = "testDeviceId";
    private static final String TEST_DEVICE_ID2 = "testDeviceId2";
    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final String RESOURCETYPE_STRING = "ams:mobject-manager/prefix=XDSL Port/ne=myNE/agent=IACM/relative=R1.S1.LT1.P1";


    private ModelNodeId m_sourceObject = mock(ModelNodeId.class);
    private ModelNodeId m_sourceObject2 = mock(ModelNodeId.class);
    private Timestamp m_raisedTime = new Timestamp(new Date().getTime());
    private Timestamp m_raisedTime2 = mock(Timestamp.class);
    private AlarmSeverity m_severity = AlarmSeverity.CLEAR;
    private AlarmSeverity m_severity2 = AlarmSeverity.CRITICAL;


    private AlarmInfo m_alarmInfo, m_alarmInfo2, m_alarmInfoWithDifferentRaisedTime, m_alarmInfoWithSourceObjectString;

    @Before
    public void initialize() {
        m_alarmInfo = new AlarmInfo(TEST_ALARM_TYPE_ID, "", m_sourceObject, m_raisedTime, m_severity,
                TEST_ADDITIONAL_INFO, TEST_DEVICE_ID);
        m_alarmInfo2 = m_alarmInfo;
        m_alarmInfoWithDifferentRaisedTime = new AlarmInfo(TEST_ALARM_TYPE_ID, "", m_sourceObject, m_raisedTime2,
                m_severity, TEST_ADDITIONAL_INFO, TEST_DEVICE_ID);
        m_alarmInfoWithSourceObjectString = new AlarmInfo(TEST_ALARM_TYPE_ID, "", null, m_raisedTime,
                m_severity, TEST_ADDITIONAL_INFO, TEST_DEVICE_ID, RESOURCETYPE_STRING);

    }

    @Test
    public void testHashCode() {
        assertEquals(m_alarmInfo2.hashCode(), m_alarmInfo.hashCode());
        assertNotEquals(m_alarmInfoWithDifferentRaisedTime.hashCode(), m_alarmInfo2.hashCode());
    }

    private String randomAlphaNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

    @Test
    public void testAdditionalInfoLength() {
        String BigValue = randomAlphaNumeric(1001);
        m_alarmInfo = new AlarmInfo(TEST_ALARM_TYPE_ID, "", m_sourceObject, m_raisedTime, m_severity, BigValue,
                TEST_DEVICE_ID);
        assertEquals(1000, m_alarmInfo.getAlarmText().length());

    }

    @Test
    public void testSetSourceObject() {
        ModelNodeId modelNodeId = new ModelNodeId("/container=platform", "http://www.example.com/network-manager/baa");
        m_alarmInfo = new AlarmInfo(TEST_ALARM_TYPE_ID, "", m_sourceObject, m_raisedTime, m_severity,
                TEST_ADDITIONAL_INFO, TEST_DEVICE_ID);
        m_alarmInfo.setSourceObject(modelNodeId);
        assertEquals(modelNodeId, m_alarmInfo.getSourceObject());
    }

    @Test
    public void testEquals() {
        assertTrue(m_alarmInfo.equals(m_alarmInfo2));
        assertFalse(m_alarmInfoWithDifferentRaisedTime.equals(m_alarmInfo2));
        assertFalse(m_alarmInfo.equals(null));
        assertFalse(m_alarmInfo.equals("String literal"));
        m_alarmInfo2 = new AlarmInfo(TEST_ALARM_TYPE_ID, "", m_sourceObject, m_raisedTime, m_severity,
                TEST_ADDITIONAL_INFO2, TEST_DEVICE_ID);
        assertFalse(m_alarmInfo.equals(m_alarmInfo2));
        m_alarmInfo2 = new AlarmInfo(TEST_ALARM_TYPE_ID, "", m_sourceObject, m_raisedTime, m_severity, null, TEST_DEVICE_ID);
        assertFalse(m_alarmInfo2.equals(m_alarmInfo));
        m_alarmInfo2 = new AlarmInfo(TEST_ALARM_TYPE_ID, null, m_sourceObject, m_raisedTime, m_severity, TEST_ADDITIONAL_INFO, TEST_DEVICE_ID);
        assertFalse(m_alarmInfo2.equals(m_alarmInfo));
        m_alarmInfo = new AlarmInfo(null, "", m_sourceObject, m_raisedTime, m_severity,
                TEST_ADDITIONAL_INFO, TEST_DEVICE_ID);
        m_alarmInfo2 = new AlarmInfo(TEST_ALARM_TYPE_ID, "", m_sourceObject, m_raisedTime, m_severity,
                TEST_ADDITIONAL_INFO, TEST_DEVICE_ID);
        assertFalse(m_alarmInfo.equals(m_alarmInfo2));
        m_alarmInfo = new AlarmInfo(TEST_ALARM_TYPE_ID, "", m_sourceObject, m_raisedTime, m_severity,
                TEST_ADDITIONAL_INFO, TEST_DEVICE_ID);
        m_alarmInfo2 = new AlarmInfo(TEST_ALARM_TYPE_ID2, "", m_sourceObject, m_raisedTime, m_severity,
                TEST_ADDITIONAL_INFO, TEST_DEVICE_ID);
        assertFalse(m_alarmInfo.equals(m_alarmInfo2));
        m_alarmInfo = new AlarmInfo(TEST_ALARM_TYPE_ID, "", m_sourceObject, null, m_severity,
                TEST_ADDITIONAL_INFO, TEST_DEVICE_ID);
        m_alarmInfo2 = new AlarmInfo(TEST_ALARM_TYPE_ID, "", m_sourceObject, m_raisedTime, m_severity,
                TEST_ADDITIONAL_INFO, TEST_DEVICE_ID);
        assertFalse(m_alarmInfo.equals(m_alarmInfo2));
        m_alarmInfo = new AlarmInfo(TEST_ALARM_TYPE_ID, "", m_sourceObject, m_raisedTime, m_severity,
                TEST_ADDITIONAL_INFO, TEST_DEVICE_ID);
        m_alarmInfo2 = new AlarmInfo(TEST_ALARM_TYPE_ID, "", m_sourceObject, m_raisedTime, m_severity2,
                TEST_ADDITIONAL_INFO, TEST_DEVICE_ID);
        assertFalse(m_alarmInfo.equals(m_alarmInfo2));
        m_alarmInfo = new AlarmInfo(TEST_ALARM_TYPE_ID, "", null, m_raisedTime, m_severity,
                TEST_ADDITIONAL_INFO, TEST_DEVICE_ID);
        m_alarmInfo2 = new AlarmInfo(TEST_ALARM_TYPE_ID, "", m_sourceObject, m_raisedTime, m_severity,
                TEST_ADDITIONAL_INFO, TEST_DEVICE_ID);
        assertFalse(m_alarmInfo.equals(m_alarmInfo2));
        m_alarmInfo = new AlarmInfo(TEST_ALARM_TYPE_ID, "", m_sourceObject, m_raisedTime, m_severity,
                TEST_ADDITIONAL_INFO, TEST_DEVICE_ID);
        m_alarmInfo2 = new AlarmInfo(TEST_ALARM_TYPE_ID, "", m_sourceObject2, m_raisedTime, m_severity,
                TEST_ADDITIONAL_INFO, TEST_DEVICE_ID);
        assertFalse(m_alarmInfo.equals(m_alarmInfo2));
        m_alarmInfo = new AlarmInfo(TEST_ALARM_TYPE_ID, "", m_sourceObject, m_raisedTime, m_severity,
                TEST_ADDITIONAL_INFO, TEST_DEVICE_ID);
        m_alarmInfo2 = new AlarmInfo(TEST_ALARM_TYPE_ID, "", m_sourceObject, m_raisedTime, m_severity,
                TEST_ADDITIONAL_INFO, TEST_DEVICE_ID);
        assertTrue(m_raisedTime == m_raisedTime);
        assertTrue(m_alarmInfo.equals(m_alarmInfo2));

        m_alarmInfo = new AlarmInfo(TEST_ALARM_TYPE_ID, "", m_sourceObject, m_raisedTime, m_severity,
                TEST_ADDITIONAL_INFO, null);
        m_alarmInfo2 = new AlarmInfo(TEST_ALARM_TYPE_ID, "", m_sourceObject, m_raisedTime, m_severity,
                TEST_ADDITIONAL_INFO, TEST_DEVICE_ID);
        assertFalse(m_alarmInfo.equals(m_alarmInfo2));
        m_alarmInfo = new AlarmInfo(TEST_ALARM_TYPE_ID, "", m_sourceObject, m_raisedTime, m_severity,
                TEST_ADDITIONAL_INFO, TEST_DEVICE_ID);
        m_alarmInfo2 = new AlarmInfo(TEST_ALARM_TYPE_ID, "", m_sourceObject, m_raisedTime, m_severity,
                TEST_ADDITIONAL_INFO, TEST_DEVICE_ID2);
        assertFalse(m_alarmInfo.equals(m_alarmInfo2));

        m_alarmInfo = new AlarmInfo(TEST_ALARM_TYPE_ID, "", m_sourceObject, m_raisedTime, m_severity,
                TEST_ADDITIONAL_INFO, TEST_DEVICE_ID);
        m_alarmInfo2 = new AlarmInfo(TEST_ALARM_TYPE_ID, "some value", m_sourceObject, m_raisedTime, m_severity,
                TEST_ADDITIONAL_INFO, TEST_DEVICE_ID);
        assertFalse(m_alarmInfo.equals(m_alarmInfo2));

        assertFalse(m_alarmInfo.equals(m_alarmInfoWithSourceObjectString));
        m_alarmInfo.setSourceObject(null);
        m_alarmInfo.setSourceObjectString(RESOURCETYPE_STRING);
        assertTrue(m_alarmInfo.equals(m_alarmInfoWithSourceObjectString));
    }
}
