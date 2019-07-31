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
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.junit.Before;
import org.junit.Test;

public class AlarmParametersTest {

    protected static final String MOUNT_KEY = "G.FAST|1.0";
    protected static final String MOUNT_KEY1 = "G.FAST|2.0";
    private static final String TEST_LAST_ALARM_TEXT = "testLastAlarmText";
    private static final String TEST_LAST_ALARM_TEXT2 = "testLastAlarmText2";
    private static final String TEST_ALARM_TYPE_ID = "testAlarmTypeId";
    private static final String TEST_ALARM_TYPE_ID2 = "testAlarmTypeId2";
    private static final String TEST_ALARM_TYPE_QUALIFIER = "testAlarmTypeQualifier";
    private static final ModelNodeId TEST_RESOURCE = new ModelNodeId().addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.BAA_NAMESPACE, "device-manager")
            .addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "device-holder")
            .addRdn(ModelNodeRdn.NAME, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "TestBAA")
            .addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "device")
            .addRdn(AlarmTestConstants.DEVICE_NAME, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "R1.S1.LT1.P1.ONT1");
    private static final ModelNodeId TEST_RESOURCE2 = new ModelNodeId().addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.BAA_NAMESPACE, "device-manager")
            .addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "device-holder")
            .addRdn(ModelNodeRdn.NAME, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "TestBAA")
            .addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "device")
            .addRdn(AlarmTestConstants.DEVICE_NAME, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "R1.S1.LT1.P1.ONT2");
    private Timestamp m_lastStatusChange = new Timestamp(new Date().getTime());
    private Timestamp m_lastStatusChange2 = mock(Timestamp.class);
    private AlarmSeverity m_lastPerceivedSeverity = AlarmSeverity.CLEAR;
    private AlarmSeverity m_lastPerceivedSeverity2 = AlarmSeverity.MINOR;
    private AlarmParameters m_alarmParameters, m_alarmParameters2, m_alarmParameters3, m_AlarmParametersDifferentSeverity;

    @Before
    public void initialize() {
        m_alarmParameters = new AlarmParameters(TEST_ALARM_TYPE_ID, TEST_ALARM_TYPE_QUALIFIER, TEST_RESOURCE, m_lastStatusChange, m_lastPerceivedSeverity,
                TEST_LAST_ALARM_TEXT, MOUNT_KEY, null);
        m_alarmParameters2 = m_alarmParameters;
        m_AlarmParametersDifferentSeverity = new AlarmParameters(TEST_ALARM_TYPE_ID, TEST_ALARM_TYPE_QUALIFIER, TEST_RESOURCE, m_lastStatusChange,
                m_lastPerceivedSeverity2, TEST_LAST_ALARM_TEXT, MOUNT_KEY, null);
    }

    @Test
    public void testHashCode() {
        assertEquals(m_alarmParameters.hashCode(), m_alarmParameters2.hashCode());
        assertNotEquals(m_AlarmParametersDifferentSeverity.hashCode(), m_alarmParameters2.hashCode());
    }

    @Test
    public void testToString() {
        assertEquals("AlarmParameters [alarmTypeId=testAlarmTypeId, resource=ModelNodeId[/container=device-manager/container=device-holder/name=TestBAA/container=device/name=R1.S1.LT1.P1.ONT1], lastStatusChange="
                        + m_alarmParameters.getLastStatusChange() + ", lastPerceivedSeverity=cleared, lastAlarmText=testLastAlarmText, resourceObjectString=null, resourceNamespaces=null]",
                m_alarmParameters.toString());
    }

    @Test
    public void testSetMountKey() {
        m_alarmParameters = new AlarmParameters(TEST_ALARM_TYPE_ID, TEST_ALARM_TYPE_QUALIFIER, TEST_RESOURCE,
                m_lastStatusChange, m_lastPerceivedSeverity, TEST_LAST_ALARM_TEXT, MOUNT_KEY, null);
        m_alarmParameters.setMountKey(MOUNT_KEY1);
        assertEquals(MOUNT_KEY1, m_alarmParameters.getMountKey());
    }

    @Test
    public void testEquals() {
        assertTrue(m_alarmParameters.equals(m_alarmParameters2));
        assertFalse(m_AlarmParametersDifferentSeverity.equals(m_alarmParameters2));
        assertFalse(m_alarmParameters.equals(null));
        assertFalse(m_alarmParameters.equals("String literal"));
        m_alarmParameters = new AlarmParameters(null, TEST_ALARM_TYPE_QUALIFIER, TEST_RESOURCE, m_lastStatusChange, m_lastPerceivedSeverity,
                TEST_LAST_ALARM_TEXT);
        m_alarmParameters2 = new AlarmParameters(TEST_ALARM_TYPE_ID, TEST_ALARM_TYPE_QUALIFIER, TEST_RESOURCE, m_lastStatusChange, m_lastPerceivedSeverity,
                TEST_LAST_ALARM_TEXT);
        m_alarmParameters3 = new AlarmParameters(TEST_ALARM_TYPE_ID, null, TEST_RESOURCE, m_lastStatusChange, m_lastPerceivedSeverity,
                TEST_LAST_ALARM_TEXT);
        assertFalse(m_alarmParameters3.equals(m_alarmParameters2));
        assertFalse(m_alarmParameters.equals(m_alarmParameters2));
        m_alarmParameters = new AlarmParameters(TEST_ALARM_TYPE_ID, TEST_ALARM_TYPE_QUALIFIER, TEST_RESOURCE, m_lastStatusChange, m_lastPerceivedSeverity,
                TEST_LAST_ALARM_TEXT);
        m_alarmParameters2 = new AlarmParameters(TEST_ALARM_TYPE_ID2, TEST_ALARM_TYPE_QUALIFIER, TEST_RESOURCE, m_lastStatusChange, m_lastPerceivedSeverity,
                TEST_LAST_ALARM_TEXT);
        assertFalse(m_alarmParameters.equals(m_alarmParameters2));
        m_alarmParameters = new AlarmParameters(TEST_ALARM_TYPE_ID, TEST_ALARM_TYPE_QUALIFIER, TEST_RESOURCE, m_lastStatusChange, m_lastPerceivedSeverity,
                null);
        m_alarmParameters2 = new AlarmParameters(TEST_ALARM_TYPE_ID, TEST_ALARM_TYPE_QUALIFIER, TEST_RESOURCE, m_lastStatusChange, m_lastPerceivedSeverity,
                TEST_LAST_ALARM_TEXT);
        assertFalse(m_alarmParameters.equals(m_alarmParameters2));
        m_alarmParameters = new AlarmParameters(TEST_ALARM_TYPE_ID, TEST_ALARM_TYPE_QUALIFIER, TEST_RESOURCE, m_lastStatusChange, m_lastPerceivedSeverity,
                TEST_LAST_ALARM_TEXT);
        m_alarmParameters2 = new AlarmParameters(TEST_ALARM_TYPE_ID, TEST_ALARM_TYPE_QUALIFIER, TEST_RESOURCE, m_lastStatusChange, m_lastPerceivedSeverity,
                TEST_LAST_ALARM_TEXT2);
        assertFalse(m_alarmParameters.equals(m_alarmParameters2));
        m_alarmParameters = new AlarmParameters(TEST_ALARM_TYPE_ID, TEST_ALARM_TYPE_QUALIFIER, TEST_RESOURCE, null, m_lastPerceivedSeverity,
                TEST_LAST_ALARM_TEXT);
        m_alarmParameters2 = new AlarmParameters(TEST_ALARM_TYPE_ID, TEST_ALARM_TYPE_QUALIFIER, TEST_RESOURCE, m_lastStatusChange, m_lastPerceivedSeverity,
                TEST_LAST_ALARM_TEXT);
        assertFalse(m_alarmParameters.equals(m_alarmParameters2));
        m_alarmParameters = new AlarmParameters(TEST_ALARM_TYPE_ID, TEST_ALARM_TYPE_QUALIFIER, TEST_RESOURCE, m_lastStatusChange, m_lastPerceivedSeverity,
                TEST_LAST_ALARM_TEXT);
        m_alarmParameters2 = new AlarmParameters(TEST_ALARM_TYPE_ID, TEST_ALARM_TYPE_QUALIFIER, TEST_RESOURCE, m_lastStatusChange2, m_lastPerceivedSeverity,
                TEST_LAST_ALARM_TEXT);
        assertFalse(m_alarmParameters.equals(m_alarmParameters2));
        m_alarmParameters = new AlarmParameters(TEST_ALARM_TYPE_ID, TEST_ALARM_TYPE_QUALIFIER, null, m_lastStatusChange, m_lastPerceivedSeverity,
                TEST_LAST_ALARM_TEXT);
        m_alarmParameters2 = new AlarmParameters(TEST_ALARM_TYPE_ID, TEST_ALARM_TYPE_QUALIFIER, TEST_RESOURCE, m_lastStatusChange, m_lastPerceivedSeverity,
                TEST_LAST_ALARM_TEXT);
        assertFalse(m_alarmParameters.equals(m_alarmParameters2));
        m_alarmParameters = new AlarmParameters(TEST_ALARM_TYPE_ID, TEST_ALARM_TYPE_QUALIFIER, TEST_RESOURCE, m_lastStatusChange, m_lastPerceivedSeverity,
                TEST_LAST_ALARM_TEXT);
        m_alarmParameters2 = new AlarmParameters(TEST_ALARM_TYPE_ID, TEST_ALARM_TYPE_QUALIFIER, TEST_RESOURCE2, m_lastStatusChange, m_lastPerceivedSeverity,
                TEST_LAST_ALARM_TEXT);
        assertFalse(m_alarmParameters.equals(m_alarmParameters2));
        m_alarmParameters = new AlarmParameters(TEST_ALARM_TYPE_ID, TEST_ALARM_TYPE_QUALIFIER, TEST_RESOURCE, m_lastStatusChange, m_lastPerceivedSeverity,
                TEST_LAST_ALARM_TEXT);
        m_alarmParameters2 = new AlarmParameters(TEST_ALARM_TYPE_ID, TEST_ALARM_TYPE_QUALIFIER, TEST_RESOURCE, m_lastStatusChange, m_lastPerceivedSeverity,
                TEST_LAST_ALARM_TEXT);
        assertTrue(m_alarmParameters.equals(m_alarmParameters2));
        assertEquals(m_alarmParameters.getMountKey(), m_alarmParameters2.getMountKey());

    }

}
