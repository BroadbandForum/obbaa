package org.broadband_forum.obbaa.netconf.alarm.util;

import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.Arrays;

import org.broadband_forum.obbaa.netconf.alarm.api.AlarmCondition;
import org.broadband_forum.obbaa.netconf.alarm.api.AlarmNotification;
import org.broadband_forum.obbaa.netconf.alarm.api.AlarmParameters;
import org.broadband_forum.obbaa.netconf.alarm.api.AlarmTestConstants;
import org.broadband_forum.obbaa.netconf.alarm.entity.AlarmSeverity;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistryProvider;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKey;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class AlarmsDocumentTransformerTest extends AbstractAlarmSetup {

    private static final String EXPECTED_ALARM_NOTIFICATION = "/alarm-notification.xml";
    private static final String EXPECTED_ALARM_NOTIFICATION_NON_YANG_RESOURCE = "/alarm-notification-non-yang-resource.xml";
    private static final String EXPECTED_IETF_ALARM_NOTIFICATION = "/ietf-alarm-notification.xml";
    private static final String EXPECTED_IETF_ALARM_NOTIFICATION_RESOURCE_IDENTIFICATION = "/ietf-alarm-notification-resource-identification.xml";
    private static final String EXPECTED_ALARM_RPC_OUTPUT = "/retrieve-alarm-rpc-output.xml";
    private static final String EXPECTED_ALARM_RPC_OUTPUT_DEVICE_HOLDER = "/retrieve-alarm-in-device-holder-rpc-output.xml";
    private static final String IF_INTF_NS = "urn:ietf:params:xml:ns:yang:ietf-interfaces";
    private static final String NON_YANG_RESOURCE_STRING = "ams:mobject-manager/prefix=XDSL port/ne=myNE/agent=ICAM/relative=R1.S1.LT1.P1";
    private static final String EXPECTED_GET_ALARM_RPC_OUTPUT_WITH_NON_YANG_RESOURCE = "/retrieve-alarm-in-device-rpc-with-non-yang-resource-output.xml";
    private static final ModelNodeId TEST_RESOURCE = new ModelNodeId().addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.BAA_NAMESPACE, "network-manager")
            .addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "managed-devices")
            .addRdn(ModelNodeRdn.NAME, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "OLT-2345")
            .addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "device")
            .addRdn(AlarmTestConstants.DEVICE_NAME, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "R1.S1.S1.ONT1");
    private static final ModelNodeId INTF_MODEL_NODE_ID = new ModelNodeId().addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.BAA_NAMESPACE, "network-manager")
            .addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "managed-devices").addRdn(ModelNodeRdn.NAME, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "'OLT-2345'")
            .addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "device").addRdn("ID", AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "'R1.S1.LT1.P1.ONT1'")
            .addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "root")
            .addRdn(ModelNodeRdn.CONTAINER, IF_INTF_NS, "interfaces").addRdn(ModelNodeRdn.CONTAINER, IF_INTF_NS, "interface").addRdn(ModelNodeRdn.NAME, IF_INTF_NS, "'xdsl-line:1/1/1/1'");
    private static final ModelNodeId INTF_MODEL_NODE_ID_IDENTIFICATION = new ModelNodeId().addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.BAA_NAMESPACE, "network-manager")
            .addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "new-devices")
            .addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "new-device")
            .addRdn("identification-method", AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "duid")
            .addRdn("identification-value", AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "R1.S1.LT1.P1.ONT1");
    private static final ModelNodeId MOUNT_MODEL_NODE_ID = new ModelNodeId().addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.BAA_NAMESPACE, "network-manager")
            .addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "managed-devices").addRdn(ModelNodeRdn.NAME, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "OLT-2345")
            .addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "device").addRdn("ID", AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "R1.S1.LT1.P1.ONT1");
    private SchemaRegistry m_mountRegistry;

    @Before
    public void setup() {
        super.setup();
        QName dmQname = QName.create("(http://www.example.com/network-manager/baa?revision=2018-07-27)network-manager");
        QName dhQname = QName.create("(http://www.example.com/network-manager/managed-devices?revision=2018-07-27)managed-devices");
        QName dQname = QName.create("(http://www.example.com/network-manager/managed-devices?revision=2018-07-27)device");
        QName dsdQname = QName.create("(http://www.example.com/network-manager/managed-devices?revision=2018-07-27)root");
        SchemaPath mountPath = SchemaPath.create(true, dmQname, dhQname, dQname, dsdQname);
        QName ndsQname = QName.create("(http://www.example.com/network-manager/managed-devices?revision=2018-07-27)new-devices");
        QName ndQname = QName.create("(http://www.example.com/network-manager/managed-devices?revision=2018-07-27)new-device");
        SchemaPath mountPath1 = SchemaPath.create(true, dmQname, ndsQname, ndQname);
        when(m_schemaRegistry.getModuleNameByNamespace("urn:broadband-forum-org:yang:dpu-alarm-types")).thenReturn("xyz-al");
        m_mountRegistry = mock(SchemaRegistry.class);
        when(m_mountRegistry.getNamespaceURI("if")).thenReturn(IF_INTF_NS);
        when(m_mountRegistry.getModuleNameByNamespace(IF_INTF_NS)).thenReturn("ietf-interfaces");
        when(m_schemaRegistry.getPrefix(AlarmConstants.ALARM_NAMESPACE)).thenReturn("alarms");
        when(m_schemaRegistry.getNamespaceURI("baa")).thenReturn(AlarmTestConstants.BAA_NAMESPACE);
        when(m_schemaRegistry.getNamespaceURI("baa-network-manager")).thenReturn(AlarmTestConstants.NETWORK_MANAGER_NAMESPACE);
        when(m_schemaRegistry.getModuleNameByNamespace(AlarmTestConstants.BAA_NAMESPACE)).thenReturn("baa");
        when(m_schemaRegistry.getModuleNameByNamespace(AlarmTestConstants.NETWORK_MANAGER_NAMESPACE)).thenReturn("managed-devices");
        Module baaModule = mock(Module.class);
        when(baaModule.getPrefix()).thenReturn("baa");
        when(baaModule.getRevision()).thenReturn(Revision.ofNullable("2018-07-27"));
        when(m_schemaRegistry.getModuleByNamespace(AlarmTestConstants.BAA_NAMESPACE)).thenReturn(baaModule);
        Module deviceHolderModule = mock(Module.class);
        when(deviceHolderModule.getPrefix()).thenReturn("baa-network-manager");
        when(deviceHolderModule.getRevision()).thenReturn(Revision.ofNullable("2018-07-27"));
        when(m_schemaRegistry.getModuleByNamespace(AlarmTestConstants.NETWORK_MANAGER_NAMESPACE)).thenReturn(deviceHolderModule);
        Module intfModule = mock(Module.class);
        when(intfModule.getPrefix()).thenReturn("if");
        when(intfModule.getRevision()).thenReturn(Revision.ofNullable("2018-07-27"));
        when(m_mountRegistry.getModuleByNamespace(IF_INTF_NS)).thenReturn(intfModule);
        when(m_mountRegistry.getPrefix(IF_INTF_NS)).thenReturn("if");
        when(m_mountRegistry.getNamespaceOfModule("ietf-interfaces")).thenReturn(IF_INTF_NS);
        when(m_schemaRegistry.getModuleByNamespace(IF_INTF_NS)).thenReturn(intfModule);
        when(m_schemaRegistry.getModuleNameByNamespace(IF_INTF_NS)).thenReturn("ietf-interfaces");
        when(m_mountRegistry.getModuleNameByNamespace("urn:broadband-forum-org:yang:dpu-alarm-types")).thenReturn("xyz-al");
        SchemaMountRegistry smr = mock(SchemaMountRegistry.class);
        SchemaMountRegistryProvider provider = mock(SchemaMountRegistryProvider.class);
        when(smr.getProvider(mountPath)).thenReturn(provider);
        when(smr.getProvider(mountPath1)).thenReturn(provider);
        ModelNode modelNode = mock(ModelNode.class);
        when(m_dsm.findNode(mountPath, ModelNodeKey.EMPTY_KEY, MOUNT_MODEL_NODE_ID,m_schemaRegistry)).thenReturn(modelNode);
        when(provider.getSchemaRegistry(modelNode.getModelNodeId())).thenReturn(m_mountRegistry);
        when(provider.getSchemaRegistry(MOUNT_KEY)).thenReturn(m_mountRegistry);
        when(m_schemaRegistry.getMountRegistry()).thenReturn(smr);
    }

    @Test
    public void testAlarmNotification() throws Exception {
        Element expectedAlarmNotificationElement = loadAsXml(EXPECTED_ALARM_NOTIFICATION);
        long timeInMillis = 1465967097648L;
        Timestamp now = new Timestamp(timeInMillis);
        AlarmNotification notification = new AlarmNotification(
                "{baa-network-manager}(http://www.example.com/network-manager/managed-devices?revision=2015-07-14)testId", "",
                TEST_RESOURCE, now, AlarmSeverity.CRITICAL,
                "Test", AlarmCondition.ALARM_ON, MOUNT_KEY, null);
        Element alarmNotificationElement = new AlarmsDocumentTransformer(m_schemaRegistry, null)
                .getAlarmNotificationElement(notification);
        assertXMLEquals(expectedAlarmNotificationElement, alarmNotificationElement, m_ignoreElements);
    }

    @Test
    public void testAlarmNotificationWithNonYangResource() throws Exception {
        Element expectedAlarmNotificationElement = loadAsXml(EXPECTED_ALARM_NOTIFICATION_NON_YANG_RESOURCE);
        long timeInMillis = 1465967097648L;
        Timestamp now = new Timestamp(timeInMillis);
        AlarmNotification notification = new AlarmNotification(
                "{baa-network-manager}(http://www.example.com/network-manager/managed-devices?revision=2015-07-14)testId", "",
                null, now, AlarmSeverity.CRITICAL,
                "Test", AlarmCondition.ALARM_ON, MOUNT_KEY, NON_YANG_RESOURCE_STRING);
        Element alarmNotificationElement = new AlarmsDocumentTransformer(m_schemaRegistry, null)
                .getAlarmNotificationElement(notification);
        assertXMLEquals(expectedAlarmNotificationElement, alarmNotificationElement, m_ignoreElements);
    }

    @Test
    public void testIETFAlarmNotificationIdentification() throws Exception {
        Element expectedAlarmNotificationElement = loadAsXml(EXPECTED_IETF_ALARM_NOTIFICATION_RESOURCE_IDENTIFICATION);
        long timeInMillis = 1465967097648L;
        Timestamp now = new Timestamp(timeInMillis);
        AlarmNotification notification = new AlarmNotification(
                "{baa-network-manager}" + AlarmTestConstants.ALARM_TYPE_ID_FOR_DEVICE,
                "{xyz-al}(urn:broadband-forum-org:yang:dpu-alarm-types)xdsl-fe-los",
                INTF_MODEL_NODE_ID_IDENTIFICATION, now, AlarmSeverity.MAJOR,
                "dummyText", AlarmCondition.ALARM_ON, MOUNT_KEY, null);
        Element alarmNotificationElement = new AlarmsDocumentTransformer(m_schemaRegistry, m_dsm).buildAlarmNotification(notification, "urn:ietf:params:xml:ns:yang:ietf-alarms");
        assertXMLEquals(expectedAlarmNotificationElement, alarmNotificationElement, m_ignoreElements);
    }

    @Test
    public void testBBFAlarmNotification() throws Exception {
        Element expectedAlarmNotificationElement = loadAsXml(EXPECTED_IETF_ALARM_NOTIFICATION);
        long timeInMillis = 1465967097648L;
        Timestamp now = new Timestamp(timeInMillis);
        AlarmNotification notification = new AlarmNotification("{baa-network-manager}" + AlarmTestConstants.ALARM_TYPE_ID_FOR_DEVICE,
                "{xyz-al}(urn:broadband-forum-org:yang:dpu-alarm-types)xdsl-fe-los", INTF_MODEL_NODE_ID, now, AlarmSeverity.MAJOR,
                "dummyText", AlarmCondition.ALARM_ON, MOUNT_KEY, null);
        Element alarmNotificationElement = new AlarmsDocumentTransformer(m_schemaRegistry, m_dsm).buildAlarmNotification(notification, "urn:ietf:params:xml:ns:yang:ietf-alarms");
        assertXMLEquals(expectedAlarmNotificationElement, alarmNotificationElement, m_ignoreElements);
    }

    @Test
    public void testAlarmRpcOutputElement() throws Exception {
        long timeInMillis = 1465967097648L;
        Timestamp now = new Timestamp(timeInMillis);
        AlarmParameters parameter = new AlarmParameters(
                "{baa-network-manager}(http://www.example.com/network-manager/managed-devices?revision=2015-07-14)testId", "",
                TEST_RESOURCE, now, AlarmSeverity.CRITICAL,
                "Test");
        Element expectedAlarmRpcElement = loadAsXml(EXPECTED_ALARM_RPC_OUTPUT_DEVICE_HOLDER);
        Element alarmRpcOutputElement = new AlarmsDocumentTransformer(m_schemaRegistry, null)
                .getAlarmRpcOutputElement(AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "1", Arrays.asList(parameter));
        assertXMLEquals(expectedAlarmRpcElement, alarmRpcOutputElement, m_ignoreElements);
        expectedAlarmRpcElement = loadAsXml(EXPECTED_ALARM_RPC_OUTPUT);
        alarmRpcOutputElement = new AlarmsDocumentTransformer(m_schemaRegistry, null).getAlarmRpcOutputElement(AlarmConstants.ALARM_NAMESPACE,
                "1", Arrays.asList(parameter));
        assertXMLEquals(expectedAlarmRpcElement, alarmRpcOutputElement, m_ignoreElements);
    }

    @Test
    public void testGetActiveAlarmRpcOutputElementWithNonYangResourceString() throws Exception {
        long timeInMillis = 1465967097648L;
        Timestamp now = new Timestamp(timeInMillis);
        AlarmParameters parameter = new AlarmParameters(
                "{baa-network-manager}(http://www.example.com/network-manager/managed-devices?revision=2015-07-14)testId",
                "",
                null, now, AlarmSeverity.CRITICAL,
                "Test", null, NON_YANG_RESOURCE_STRING);
        Element expectedAlarmRpcElement = loadAsXml(EXPECTED_GET_ALARM_RPC_OUTPUT_WITH_NON_YANG_RESOURCE);
        Element alarmRpcOutputElement = new AlarmsDocumentTransformer(m_schemaRegistry, null)
                .getAlarmRpcOutputElement(AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "1", Arrays.asList(parameter));
        assertXMLEquals(expectedAlarmRpcElement, alarmRpcOutputElement, m_ignoreElements);
    }

    @Test
    public void testAlarmRpcOutputElementWithNoSchemaRegistry() throws Exception {
        long timeInMillis = 1465967097648L;
        Timestamp now = new Timestamp(timeInMillis);
        AlarmParameters parameter = new AlarmParameters(
                "(http://www.example.com/network-manager/managed-devices?revision=2015-07-14)testId", "",
                TEST_RESOURCE, now, AlarmSeverity.CRITICAL,
                "Test");
        Element alarmRpcOutputElement = new AlarmsDocumentTransformer(null, null)
                .getAlarmRpcOutputElement(AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "1", Arrays.asList(parameter));
        NodeList nodes = alarmRpcOutputElement.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            assertFalse(nodes.item(i).getNodeName().contains("baa-network-manager:"));
        }

        alarmRpcOutputElement = new AlarmsDocumentTransformer(null, null).getAlarmRpcOutputElement(AlarmConstants.ALARM_NAMESPACE, "1",
                Arrays.asList(parameter));
        nodes = alarmRpcOutputElement.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            assertFalse(nodes.item(i).getNodeName().contains("alarms:"));
        }
    }
}
