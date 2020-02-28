package org.broadband_forum.obbaa.netconf.alarm.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.broadband_forum.obbaa.netconf.alarm.entity.AlarmSeverity;
import org.broadband_forum.obbaa.netconf.alarm.util.AlarmConstants;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.Module;

public class DefaultAlarmStateChangeNotificationTest {

    private static final String TEST_NAMESPACE = "testNamespace";
    private static final String TEST_ALARM_TYPE_ID = "testAlarmTypeId";
    private static final String TEST_ALARM_TYPE_QUALIFIER = "testAlarmTypeQualifier";
    private static final QName ALARM_STATE_CHANGE = QName.create(AlarmConstants.ALARM_NAMESPACE, "alarm-notification");
    private static final ModelNodeId TEST_RESOURCE = new ModelNodeId().addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.BAA_NAMESPACE, "device-manager")
            .addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "device-holder")
            .addRdn(ModelNodeRdn.NAME, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "OLT-2345")
            .addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "device")
            .addRdn(AlarmTestConstants.DEVICE_NAME, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "R1.S1.S1.ONT1");
    private SchemaRegistry m_schemaRegistry;
    private DefaultAlarmStateChangeNotification m_alarmStateChangeNotification;
    private AlarmNotification m_alarmNotification;
    private ModelNodeDataStoreManager m_dsm;

    @Before
    public void initialize() {
        m_schemaRegistry = mock(SchemaRegistry.class);
        m_dsm = mock(ModelNodeDataStoreManager.class);
        when(m_schemaRegistry.getNamespaceURI("baa")).thenReturn(AlarmTestConstants.BAA_NAMESPACE);
        when(m_schemaRegistry.getNamespaceURI("adh")).thenReturn(AlarmTestConstants.NETWORK_MANAGER_NAMESPACE);
        when(m_schemaRegistry.getModuleNameByNamespace(AlarmTestConstants.BAA_NAMESPACE)).thenReturn("baa");
        when(m_schemaRegistry.getModuleNameByNamespace(AlarmTestConstants.NETWORK_MANAGER_NAMESPACE)).thenReturn("baa-device-holders");
        Module baaModule = mock(Module.class);
        when(baaModule.getPrefix()).thenReturn("baa");
        when(baaModule.getRevision()).thenReturn(Revision.ofNullable("2018-07-27"));
        when(m_schemaRegistry.getModuleByNamespace(AlarmTestConstants.BAA_NAMESPACE)).thenReturn(baaModule);
        Module deviceHolderModule = mock(Module.class);
        when(deviceHolderModule.getPrefix()).thenReturn("adh");
        when(deviceHolderModule.getRevision()).thenReturn(Revision.ofNullable("2018-07-27"));
        when(m_schemaRegistry.getModuleByNamespace(AlarmTestConstants.NETWORK_MANAGER_NAMESPACE)).thenReturn(deviceHolderModule);
        m_alarmStateChangeNotification = new DefaultAlarmStateChangeNotification(m_schemaRegistry, m_dsm);
        m_alarmNotification = mock(AlarmNotification.class);
        m_alarmStateChangeNotification.setAlarmNotification(m_alarmNotification);
    }

    @Test
    public void testGetNotificationElement() throws NetconfMessageBuilderException {
        when(m_alarmNotification.getResource()).thenReturn(TEST_RESOURCE);
        when(m_alarmNotification.getAlarmTypeId()).thenReturn(TEST_ALARM_TYPE_ID);
        when(m_alarmNotification.getAlarmTypeQualifier()).thenReturn(TEST_ALARM_TYPE_QUALIFIER);
        Timestamp testTimestamp = mock(Timestamp.class);
        when(m_alarmNotification.getLastStatusChange()).thenReturn(testTimestamp);
        AlarmSeverity testAlarmSeverity = AlarmSeverity.CLEAR;
        AlarmCondition testAlarmCondition = AlarmCondition.ALARM_OFF;
        when(m_alarmNotification.getLastPerceivedSeverity()).thenReturn(testAlarmSeverity);
        when(m_alarmNotification.getAlarmCondition()).thenReturn(testAlarmCondition);
        when(m_schemaRegistry.getPrefix(AlarmConstants.ALARM_NAMESPACE)).thenReturn(TEST_NAMESPACE);
        assertNotNull(m_alarmStateChangeNotification.getNotificationElement());
    }

    @Test
    public void testToString() {
        assertEquals("DefaultAlarmStateChangeNotification [alarm=" + m_alarmNotification + "]", m_alarmStateChangeNotification.toString());
    }

    @Test
    public void testGetType() {
        assertEquals(ALARM_STATE_CHANGE, m_alarmStateChangeNotification.getType());
    }
}
