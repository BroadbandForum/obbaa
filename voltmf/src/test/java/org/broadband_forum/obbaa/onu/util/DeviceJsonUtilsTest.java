/*
 * Copyright 2020 Broadband Forum
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

package org.broadband_forum.obbaa.onu.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;

import org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * Unit tests that tests utility service that converts JSON to XML
 *
 * Created by Ranjitha.B.R (Nokia) on 22/07/2020.
 */
public class DeviceJsonUtilsTest {
    private final Revision m_27March = Revision.of("1988-03-27");
    private final Revision m_08March = Revision.of("1990-03-08");
    DeviceJsonUtils m_util;
    @Mock
    private SchemaRegistry m_schemaRegistry;
    @Mock
    private ModelNodeDataStoreManager m_modelNodeDSM;
    static final SchemaPath INTERFACES_STATE_SP_WITH_SM = SchemaPathBuilder.fromString("(ns1?revision=1988-03-27)interfaces-state");
    static final SchemaPath INTERFACES_STATE_SP_WITHOUT_SM = SchemaPathBuilder.fromString("(urn:xxxxx-org:yang:netconf-server-dev?revision="+DeviceManagerNSConstants.REVISION+")network-maanger",
            "(urn:ietf:params:xml:ns:yang:ietf-netconf-server?revision="+DeviceManagerNSConstants.REVISION+")device,device-specific-data", "(urn:xxxxx-org:yang:netconf-server-dev/device/Mediated-Device-Type-1.0/ns1?revision=1988-03-27)interfaces-state");
    static final SchemaPath INTERFACES_STATE2_SP_WITH_SM = SchemaPathBuilder.fromString("(ns2?revision=1990-03-08)interfaces-state2");
    static final SchemaPath INTERFACES_STATE2_SP_WITHOUT_SM = SchemaPathBuilder.fromString("(urn:xxxxx-org:yang:netconf-server-dev?revision="+DeviceManagerNSConstants.REVISION+")network-maanger",
            "(urn:ietf:params:xml:ns:yang:ietf-netconf-server?revision="+DeviceManagerNSConstants.REVISION+")device,device-specific-data", "(urn:xxxxx-org:yang:netconf-server-dev/device/Mediated-Device-Type-1.0/ns2?revision=1990-03-08)interfaces-state2");
    static final SchemaPath CREATE_ONU_RPC1_SP_WITH_SM = SchemaPathBuilder.fromString("(ns1?revision=1988-03-27)create-onu");
    static final SchemaPath CREATE_ONU_RPC2_SP_WITH_SM = SchemaPathBuilder.fromString("(ns2?revision=1990-03-08)create-onu");
    @Mock
    private DataSchemaNode m_interfacesStateSN;
    @Mock
    private DataSchemaNode m_interfacesState2SN;
    @Mock
    private RpcDefinition m_rpcSchemaNode;
    @Mock
    private RpcDefinition m_rpcSchemaNode1;
    @Mock
    private Module m_module1;
    private QNameModule m_qnameModule1;
    @Mock
    private Module m_module2;
    private QNameModule m_qnameModule2;
    private QNameModule m_qnameModule1WithoutSm;
    private QNameModule m_qnameModule2WithoutSm;
    @Mock
    private Module m_module1WithoutSm;
    @Mock
    private Module m_module2WithoutSm;


    @Before
    public void setUp() throws URISyntaxException {
        MockitoAnnotations.initMocks(this);
        m_qnameModule1 = QNameModule.create(new URI("ns1"), m_27March);
        m_qnameModule1WithoutSm = QNameModule.create(new URI("urn:xxxxx-org:yang:netconf-server-dev/device/Mediated-Device-Type-1.0/ns1"), m_27March);
        m_qnameModule2 = QNameModule.create(new URI("ns2"), m_08March);
        m_qnameModule2WithoutSm = QNameModule.create(new URI("urn:xxxxx-org:yang:netconf-server-dev/device/Mediated-Device-Type-1.0/ns2"), m_08March);
        m_util = new DeviceJsonUtils(m_schemaRegistry, m_modelNodeDSM);
        when(m_schemaRegistry.getModuleByNamespace("ns1")).thenReturn(m_module1);
        when(m_module1.getQNameModule()).thenReturn(m_qnameModule1);
        when(m_schemaRegistry.getModuleByNamespace("urn:xxxxx-org:yang:netconf-server-dev/device/Mediated-Device-Type-1.0/ns1")).thenReturn(m_module1WithoutSm);
        when(m_module1WithoutSm.getQNameModule()).thenReturn(m_qnameModule1WithoutSm);
        when(m_schemaRegistry.getModuleByNamespace("ns2")).thenReturn(m_module2);
        when(m_module2.getQNameModule()).thenReturn(m_qnameModule2);
        when(m_schemaRegistry.getModuleByNamespace("urn:xxxxx-org:yang:netconf-server-dev/device/Mediated-Device-Type-1.0/ns2")).thenReturn(m_module2WithoutSm);
        when(m_module2WithoutSm.getQNameModule()).thenReturn(m_qnameModule2WithoutSm);
        when(m_schemaRegistry.getDataSchemaNode(INTERFACES_STATE_SP_WITH_SM)).thenReturn(m_interfacesStateSN);
        when(m_schemaRegistry.getDataSchemaNode(INTERFACES_STATE2_SP_WITH_SM)).thenReturn(m_interfacesState2SN);
        when(m_schemaRegistry.getDataSchemaNode(INTERFACES_STATE_SP_WITHOUT_SM)).thenReturn(m_interfacesStateSN);
        when(m_schemaRegistry.getDataSchemaNode(INTERFACES_STATE2_SP_WITHOUT_SM)).thenReturn(m_interfacesState2SN);
        when(m_schemaRegistry.getRpcDefinition(CREATE_ONU_RPC1_SP_WITH_SM)).thenReturn(m_rpcSchemaNode);
        when(m_schemaRegistry.getRpcDefinition(CREATE_ONU_RPC2_SP_WITH_SM)).thenReturn(m_rpcSchemaNode1);
    }


    @Test
    public void testRootSchemaNodeIsRetrievedWithSchemaMount() {
        DataSchemaNode schemaNode = m_util.getDeviceRootSchemaNode("interfaces-state", "ns1");
        assertEquals(m_interfacesStateSN, schemaNode);

        schemaNode = m_util.getDeviceRootSchemaNode("interfaces-state2", "ns2");
        assertEquals(m_interfacesState2SN, schemaNode);
    }

    @Test
    public void testRootSchemaNodeIsRetrievedWithoutSchemaMount() {
        DataSchemaNode schemaNode = m_util.getDeviceRootSchemaNode("interfaces-state", "ns1");
        assertEquals(m_interfacesStateSN, schemaNode);

        schemaNode = m_util.getDeviceRootSchemaNode("interfaces-state2", "ns2");
        assertEquals(m_interfacesState2SN, schemaNode);
    }

    @Test
    public void testRpcSchemaNodeIsRetrievedWithSchemaMount() {
        RpcDefinition schemaNode = m_util.getRpcSchemaNode("create-onu", "ns1");
        assertEquals(m_rpcSchemaNode, schemaNode);

        schemaNode = m_util.getRpcSchemaNode("create-onu", "ns2");
        assertEquals(m_rpcSchemaNode1, schemaNode);
    }
}
