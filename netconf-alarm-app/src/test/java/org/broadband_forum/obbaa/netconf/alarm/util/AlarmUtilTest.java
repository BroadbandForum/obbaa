package org.broadband_forum.obbaa.netconf.alarm.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.jxpath.JXPathInvalidSyntaxException;
import org.apache.commons.jxpath.ri.compiler.Step;
import org.broadband_forum.obbaa.netconf.alarm.api.AlarmInfo;
import org.broadband_forum.obbaa.netconf.alarm.api.AlarmTestConstants;
import org.broadband_forum.obbaa.netconf.alarm.entity.Alarm;
import org.broadband_forum.obbaa.netconf.alarm.entity.AlarmSeverity;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistryProvider;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKey;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Element;

@RunWith(MockitoJUnitRunner.class)
public class AlarmUtilTest {

    private static final ModelNodeId MODEL_NODE_ID = new ModelNodeId().addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.BAA_NAMESPACE, "device-manager")
            .addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "device-holder").addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "device");

    private static final String DB_ID = "/baa:device-manager/baa-device-holders:device-holder/device";

    private static final ModelNodeId YET_ANOTHER_MODEL_NODE_ID = new ModelNodeId().addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.BAA_NAMESPACE, "device-manager")
            .addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "device-holder").addRdn(ModelNodeRdn.NAME, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "'OLT-2345'")
            .addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "device").addRdn("ID", AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "'R1.S1.LT1.P1.ONT1'");

    private static final ModelNodeId YET_ANOTHER_MODEL_NODE_ID_INVALID = new ModelNodeId().addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.BAA_NAMESPACE, "device-manager")
            .addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "device-holder").addRdn(ModelNodeRdn.NAME, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "OLT-2345")
            .addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "device").addRdn("ID", AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "R1.S1.LT1.P1.ONT1");

    private static final String YET_ANOTHER_DB_ID = "/baa:device-manager/baa-device-holders:device-holder[name='OLT-2345']/device[ID='R1.S1.LT1.P1.ONT1']";
    private static final String DB_ID_FOR_INTF = "/baa:device-manager/baa-device-holders:device-holder[name='OLT-2345']/device[ID='R1.S1.LT1.P1.ONT1']/device-specific-data/ietf-interfaces:interfaces/interface[name='xdsl-line:1/1/1/1']";

    private static final String DB_ID_WITH_INTF = "/baa:device-manager/adh:device-holder[adh:name='OLT-2345']/adh:device[adh:ID='R1.S1.LT1.P1.ONT1']/adh:device-specific-data/"
            + "if:interfaces/if:interface[if:name='xdsl-line:1/1/1/1']";

    private static final String DB_ID_WITH_INTF_INVALID = "/baa:device-manager/adh:device-holder[adh:name=OLT-2345]/adh:device[adh:ID=R1.S1.LT1.P1.ONT1]/adh:device-specific-data/"
            + "if:interfaces/if:interface[if:name=xdsl-line:1/1/1/1]";
    private static final String IF_INTF_NS = "urn:ietf:params:xml:ns:yang:ietf-interfaces";
    private static final ModelNodeId INTF_MODEL_NODE_ID = new ModelNodeId().addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.BAA_NAMESPACE, "device-manager")
            .addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "device-holder").addRdn(ModelNodeRdn.NAME, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "'OLT-2345'")
            .addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "device").addRdn("ID", AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "'R1.S1.LT1.P1.ONT1'")
            .addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "device-specific-data")
            .addRdn(ModelNodeRdn.CONTAINER, IF_INTF_NS, "interfaces").addRdn(ModelNodeRdn.CONTAINER, IF_INTF_NS, "interface").addRdn(ModelNodeRdn.NAME, IF_INTF_NS, "'xdsl-line:1/1/1/1'");
    private static final ModelNodeId SOURCE_MODEL_NODE_ID = new ModelNodeId().addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.BAA_NAMESPACE, "device-manager")
            .addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "device-holder").addRdn(ModelNodeRdn.NAME, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "'OLT-2345'")
            .addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "device").addRdn("ID", AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "'R1.S1.LT1.P1.ONT1'")
            .addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "device-specific-data");
    private static final ModelNodeId MOUNT_MODEL_NODE_ID = new ModelNodeId().addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.BAA_NAMESPACE, "device-manager")
            .addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "device-holder").addRdn(ModelNodeRdn.NAME, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "OLT-2345")
            .addRdn(ModelNodeRdn.CONTAINER, AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "device").addRdn("ID", AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, "R1.S1.LT1.P1.ONT1");
    @Rule
    public ExpectedException m_expectedException = ExpectedException.none();
    private SchemaRegistry m_schemaRegistry;
    private SchemaRegistry m_mountRegistry;
    private ModelNodeDataStoreManager m_dsm;

    @Before
    public void setup() throws Exception {
        RequestScope.setEnableThreadLocalInUT(true);
        QName dmQname = QName.create("(http://www.example.com/network-manager/baa?revision=2018-07-27)device-manager");
        QName dhQname = QName.create("(http://www.example.com/network-manager/managed-devices?revision=2018-07-27)device-holder");
        QName dQname = QName.create("(http://www.example.com/network-manager/managed-devices?revision=2018-07-27)device");
        QName dsdQname = QName.create("(http://www.example.com/network-manager/managed-devices?revision=2018-07-27)device-specific-data");
        SchemaPath mountPath = SchemaPath.create(true, dmQname, dhQname, dQname, dsdQname);
        m_schemaRegistry = mock(SchemaRegistry.class);
        m_mountRegistry = mock(SchemaRegistry.class);
        m_dsm = mock(ModelNodeDataStoreManager.class);
        when(m_schemaRegistry.getNamespaceURI("baa")).thenReturn(AlarmTestConstants.BAA_NAMESPACE);
        when(m_schemaRegistry.getNamespaceURI("adh")).thenReturn(AlarmTestConstants.NETWORK_MANAGER_NAMESPACE);
        when(m_schemaRegistry.getModuleNameByNamespace(AlarmTestConstants.BAA_NAMESPACE)).thenReturn("baa");
        when(m_schemaRegistry.getModuleNameByNamespace(AlarmTestConstants.NETWORK_MANAGER_NAMESPACE)).thenReturn("baa-device-holders");

        when(m_mountRegistry.getNamespaceURI("if")).thenReturn(IF_INTF_NS);
        when(m_mountRegistry.getModuleNameByNamespace(IF_INTF_NS)).thenReturn("ietf-interfaces");
        Module baaModule = mock(Module.class);
        when(baaModule.getPrefix()).thenReturn("baa");
        when(baaModule.getRevision()).thenReturn(Revision.ofNullable("2018-07-27"));
        when(m_schemaRegistry.getModuleByNamespace(AlarmTestConstants.BAA_NAMESPACE)).thenReturn(baaModule);
        Module deviceHolderModule = mock(Module.class);
        when(deviceHolderModule.getPrefix()).thenReturn("adh");
        when(deviceHolderModule.getRevision()).thenReturn(Revision.ofNullable("2018-07-27"));
        when(m_schemaRegistry.getModuleByNamespace(AlarmTestConstants.NETWORK_MANAGER_NAMESPACE)).thenReturn(deviceHolderModule);
        Module intfModule = mock(Module.class);
        when(intfModule.getPrefix()).thenReturn("if");
        when(intfModule.getRevision()).thenReturn(Revision.ofNullable("2018-07-27"));
        when(m_mountRegistry.getModuleByNamespace(IF_INTF_NS)).thenReturn(intfModule);
        when(m_schemaRegistry.getModuleByNamespace(IF_INTF_NS)).thenReturn(intfModule);
        when(m_schemaRegistry.getNamespaceOfModule("ietf-interfaces")).thenReturn(IF_INTF_NS);
        when(m_schemaRegistry.getModuleNameByNamespace(IF_INTF_NS)).thenReturn("ietf-interfaces");
        when(m_schemaRegistry.getNamespaceOfModule("baa")).thenReturn(AlarmTestConstants.BAA_NAMESPACE);
        when(m_schemaRegistry.getNamespaceOfModule("baa-device-holders")).thenReturn(AlarmTestConstants.NETWORK_MANAGER_NAMESPACE);
        when(m_schemaRegistry.getPrefix(AlarmTestConstants.BAA_NAMESPACE)).thenReturn("baa");
        when(m_schemaRegistry.getPrefix(AlarmTestConstants.NETWORK_MANAGER_NAMESPACE)).thenReturn("adh");
        when(m_mountRegistry.getPrefix(IF_INTF_NS)).thenReturn("if");
        when(m_mountRegistry.getNamespaceOfModule("ietf-interfaces")).thenReturn(IF_INTF_NS);

        SchemaMountRegistry smr = mock(SchemaMountRegistry.class);
        SchemaMountRegistryProvider provider = mock(SchemaMountRegistryProvider.class);
        when(smr.getProvider(mountPath)).thenReturn(provider);
        ModelNode modelNode = mock(ModelNode.class);
        when(m_dsm.findNode(mountPath, ModelNodeKey.EMPTY_KEY, MOUNT_MODEL_NODE_ID)).thenReturn(modelNode);
        when(provider.getSchemaRegistry(any(ModelNode.class))).thenReturn(m_mountRegistry);
        when(m_schemaRegistry.getMountRegistry()).thenReturn(smr);
        RequestScope.resetScope();
    }

    @After
    public void after() {
        RequestScope.setEnableThreadLocalInUT(false);
        RequestScope.resetScope();
    }

    @Test
    public void testToDbId() {
        String actualDbId = AlarmUtil.toDbId(m_schemaRegistry, m_dsm, MODEL_NODE_ID, AlarmUtil.getPrefixToNsMap(MODEL_NODE_ID, m_dsm, m_schemaRegistry, null));
        assertEquals(DB_ID, actualDbId);

        actualDbId = AlarmUtil.toDbId(m_schemaRegistry, m_dsm, YET_ANOTHER_MODEL_NODE_ID, AlarmUtil.getPrefixToNsMap(YET_ANOTHER_MODEL_NODE_ID, m_dsm, m_schemaRegistry, null));
        assertEquals(YET_ANOTHER_DB_ID, actualDbId);

        actualDbId = AlarmUtil.toDbId(m_schemaRegistry, m_dsm, INTF_MODEL_NODE_ID, AlarmUtil.getPrefixToNsMap(INTF_MODEL_NODE_ID, m_dsm, m_schemaRegistry, null));
        assertEquals(DB_ID_FOR_INTF, actualDbId);
        Map<String, Pair<String, SchemaRegistry>> prefixToNsMap = new HashMap<>();
        Pair<String, SchemaRegistry> pair = new Pair<String, SchemaRegistry>(AlarmTestConstants.BAA_NAMESPACE, m_schemaRegistry);
        prefixToNsMap.put("baa", pair);
        pair = new Pair<String, SchemaRegistry>(AlarmTestConstants.NETWORK_MANAGER_NAMESPACE, m_schemaRegistry);
        prefixToNsMap.put("adh", pair);
        pair = new Pair<String, SchemaRegistry>(IF_INTF_NS, m_mountRegistry);
        prefixToNsMap.put("if", pair);
        String actualString = AlarmUtil.xPathString(prefixToNsMap, m_schemaRegistry, INTF_MODEL_NODE_ID);
        assertEquals(DB_ID_WITH_INTF, actualString);
    }

    @Test
    public void testToNodeId() {
        ModelNodeId actualNodeId = AlarmUtil.toNodeId(m_schemaRegistry, null, DB_ID);
        assertEquals(MODEL_NODE_ID, actualNodeId);

        actualNodeId = AlarmUtil.toNodeId(m_schemaRegistry, null, YET_ANOTHER_DB_ID);
        assertEquals(YET_ANOTHER_MODEL_NODE_ID, actualNodeId);

        actualNodeId = AlarmUtil.toNodeId(m_schemaRegistry, null, YET_ANOTHER_DB_ID);
        assertNotEquals(YET_ANOTHER_MODEL_NODE_ID_INVALID, actualNodeId);

        actualNodeId = AlarmUtil.toNodeId(m_schemaRegistry, m_dsm, DB_ID_FOR_INTF);
        assertEquals(INTF_MODEL_NODE_ID, actualNodeId);
    }

    @Test(expected = JXPathInvalidSyntaxException.class)
    public void testToNodeId_throw_Exception() {
        ModelNodeId actualNodeId = AlarmUtil.toNodeId(m_schemaRegistry, null, DB_ID_WITH_INTF_INVALID);
        assertEquals(YET_ANOTHER_MODEL_NODE_ID, actualNodeId);
    }

    @Test
    public void testToNodeIdUsingNodeElement() throws NetconfMessageBuilderException {
        String alarmElement = "<alarms:alarm xmlns:alarms=\"urn:ietf:params:xml:ns:yang:ietf-alarms\">"
                + "<alarms:resource xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\" xmlns:baa=\"http://www.example.com/network-manager/baa\" "
                + "xmlns:adh=\"http://www.example.com/network-manager/managed-devices\">/baa:device-manager/adh:device-holder[adh:name='OLT-2345']"
                + "/adh:device[adh:ID='R1.S1.LT1.P1.ONT1']/adh:device-specific-data/if:interfaces/if:interface[if:name='xdsl-line:1/1/1/1']</alarms:resource>"
                + "</alarms:alarm>";

        Element element = DocumentUtils.stringToDocument(alarmElement).getDocumentElement();
        ModelNodeId actualNodeId = AlarmUtil.toNodeIdUsingNodeElement(DB_ID_WITH_INTF, (Element) element.getChildNodes().item(0));
        assertEquals(INTF_MODEL_NODE_ID, actualNodeId);
    }

    @Test
    public void testToNodeIdUsingNodeElementWhenNamespaceMissingException() throws NetconfMessageBuilderException {
        String alarmElement = "<alarms:alarm xmlns:alarms=\"urn:ietf:params:xml:ns:yang:ietf-alarms\">"
                + "<alarms:resource>/baa:device-manager/adh:device-holder[adh:name='OLT-2345']"
                + "/adh:device[adh:ID='R1.S1.LT1.P1.ONT1']/adh:device-specific-data/if:interfaces/if:interface[if:name='xdsl-line:1/1/1/1']</alarms:resource>"
                + "</alarms:alarm>";

        Element element = DocumentUtils.stringToDocument(alarmElement).getDocumentElement();

        m_expectedException.expect(AlarmProcessingException.class);
        m_expectedException.expectMessage("Namespace is missing for prefix 'baa' in resource");

        AlarmUtil.toNodeIdUsingNodeElement(DB_ID_WITH_INTF, (Element) element.getChildNodes().item(0));
    }

    @Test
    public void testToNodeIdUsingRegistry() throws NetconfMessageBuilderException {
        when(m_schemaRegistry.getNamespaceURI("if")).thenReturn(IF_INTF_NS);
        ModelNodeId actualNodeId = AlarmUtil.toNodeIdUsingRegistry(DB_ID_WITH_INTF, m_schemaRegistry);
        assertEquals(INTF_MODEL_NODE_ID, actualNodeId);
    }

    @Test
    public void testToNodeIdUsingRegistryWhenNamespaceMissingException() throws NetconfMessageBuilderException {
        m_expectedException.expect(AlarmProcessingException.class);
        m_expectedException.expectMessage("Namespace is missing for prefix 'if' in resource");

        AlarmUtil.toNodeIdUsingRegistry(DB_ID_WITH_INTF, m_schemaRegistry);
    }

    @Test
    public void testConvertToAlarmEntity() {
        Timestamp raisedTime = new Timestamp(System.currentTimeMillis());
        AlarmInfo info = new AlarmInfo(QName.create("namespace", "hello").toString(), MODEL_NODE_ID, raisedTime, AlarmSeverity.MAJOR, DB_ID, null);
        Alarm alarm = AlarmUtil.convertToAlarmEntity(info, DB_ID);
        assertEquals(info.getTime(), alarm.getRaisedTime());
        assertEquals(info.getAlarmText(), alarm.getAlarmText());
        assertEquals(info.getSeverity(), alarm.getSeverity());
        assertEquals(DB_ID, alarm.getSourceObject());
    }

    @Test
    public void splitTest() {
        String xpath = "/baa:device-manager/adh:device-holder[adh:name='sdolt']/adh:device[adh:name='device1']/adh:device-specific-data/if:interfaces/if:interface[if:name='cp/0']";
        Step[] splits = AlarmUtil.splitXpathByJXPath(xpath);
        assertEquals(6, splits.length);
    }
}
