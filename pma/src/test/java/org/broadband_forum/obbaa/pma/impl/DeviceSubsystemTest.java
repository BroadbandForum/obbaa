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

package org.broadband_forum.obbaa.pma.impl;

import static org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams.UNBOUNDED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.broadband_forum.obbaa.device.adapter.AdapterContext;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.device.adapter.DeviceInterface;
import org.broadband_forum.obbaa.device.adapter.impl.NcCompliantAdapterDeviceInterface;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.DeviceMgmt;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ChangeNotification;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemValidationException;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.server.RequestTask;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.pma.DeviceXmlStore;
import org.broadband_forum.obbaa.pma.NetconfDeviceAlignmentService;
import org.broadband_forum.obbaa.pma.PmaServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class DeviceSubsystemTest {

    DeviceSubsystem m_deviceSubsystem;
    private ModelNodeId m_interfacesStateId;
    private QName m_leaf1;
    private QName m_leaf2;
    @Mock
    private Device m_device;
    @Mock
    private DeviceMgmt m_devMgmt;
    @Captor
    private ArgumentCaptor<GetRequest> m_requestCaptor;
    @Mock
    private EditConfigRequest m_request;
    @Mock
    private NetconfDeviceAlignmentService m_das;
    @Mock
    private SchemaRegistry m_schemaReg;
    @Mock
    private Future<NetConfResponse> m_netconfResponse;
    @Mock
    private AdapterManager m_adapterManager;
    private DeviceInterface m_deviceInterface;
    @Mock
    private AdapterContext m_context;
    @Mock
    private DeviceXmlStore m_deviceStore;
    @Mock
    private DeviceXmlStore m_backupStore;
    @Captor
    ArgumentCaptor<Document> m_documentCaptor;

    private List<ChangeNotification> m_changeNotifications = new ArrayList<>();

    @Before
    public void setUp() throws ExecutionException {
        MockitoAnnotations.initMocks(this);
        m_interfacesStateId = new ModelNodeId("/container=interfaces-state", "urn:ietf:ns");
        m_leaf1 = QName.create("urn:ietf:ns", "leaf1");
        m_leaf2 = QName.create("urn:ietf:ns", "leaf2");
        m_deviceSubsystem = new DeviceSubsystem(m_das, m_schemaReg, m_adapterManager);
        String dummyXml = "<some-xml/>";
        PmaServer.setCurrentDevice(m_device);
        PmaServer.setCurrentDeviceXmlStore(m_deviceStore);
        PmaServer.setBackupDeviceXmlStore(m_backupStore);
        when(m_deviceStore.getDeviceXml()).thenReturn(dummyXml);
        when(m_device.getDeviceManagement()).thenReturn(m_devMgmt);
        m_deviceInterface = mock(NcCompliantAdapterDeviceInterface.class);
        when(m_adapterManager.getAdapterContext(any())).thenReturn(m_context);
        when(m_context.getDeviceInterface()).thenReturn(m_deviceInterface);
        when(m_deviceInterface.get(any(), any())).thenReturn(m_netconfResponse);
        when(m_backupStore.getDeviceXml()).thenReturn("<test/>");
    }

    @After
    public void tearDown(){
        PmaServer.clearCurrentDevice();
        RequestScope.resetScope();
    }

    @Test
    public void testSubsystemSchedulesEditWithOriginalRequest(){
        RequestScope.getCurrentScope().putInCache(RequestTask.CURRENT_REQ, m_request);
        m_deviceSubsystem.notifyChanged(Collections.emptyList());
        verify(m_das).queueEdit(m_device.getDeviceName(), (EditConfigRequest) m_request);
    }

    @Test
    public void testNcSessionMgrIsContactedToRetrieveStateValues()
            throws GetAttributeException, ExecutionException, NetconfMessageBuilderException, IOException,
            SAXException {
        Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> attributes = getAttributes();
        m_deviceSubsystem.retrieveStateAttributes(attributes, new NetconfQueryParams(UNBOUNDED, true));
        verify(m_deviceInterface).get(eq(m_device), m_requestCaptor.capture());
        String expectedGetRequest = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "  <get>\n" +
                "    <filter type=\"subtree\">\n" +
                "      <interfaces-state xmlns=\"urn:ietf:ns\">\n" +
                "        <test1>\n" +
                "          <containment-node xmlns=\"urn:containment:ns\">\n" +
                "            <match-node xmlns=\"urn:match:ns\">match-value</match-node>\n" +
                "            <inner-select-node xmlns=\"urn:inner:select:ns\"/>\n" +
                "          </containment-node>\n" +
                "          <select-node xmlns=\"urn:select:ns\"/>\n" +
                "        </test1>\n" +
                "        <test3>\n" +
                "          <leaf-match xmlns=\"urn:leafmatch:ns\">leaf-match-value</leaf-match>\n" +
                "        </test3>\n" +
                "        <leaf1/>\n" +
                "        <leaf2/>\n" +
                "      </interfaces-state>\n" +
                "    </filter>\n" +
                "  </get>\n" +
                "</rpc>";
//        assertEquals(expectedGetRequest, m_requestCaptor.getValue().requestToString().trim());
        TestUtil.assertXMLEquals(DocumentUtils.stringToDocument(expectedGetRequest).getDocumentElement(),
                DocumentUtils.stringToDocument(m_requestCaptor.getValue().requestToString().trim()).getDocumentElement());
    }

    private Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> getAttributes() {
        Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> map = new HashMap<>();
        List<FilterNode> filterNodes = new ArrayList<>();
        FilterNode fn1 = new FilterNode("test1", "urn:ietf:ns");
        FilterNode selectNode = new FilterNode("test2", "urn:ietf:ns");
        selectNode.setNamespace("urn:select:ns");
        selectNode.setNodeName("select-node");
        fn1.addSelectNode(selectNode);
        FilterNode containmentNode = new FilterNode();
        containmentNode.setNamespace("urn:containment:ns");
        containmentNode.setNodeName("containment-node");
        containmentNode.addMatchNode("match-node", "urn:match:ns", "match-value");
        containmentNode.addSelectNode(new FilterNode("inner-select-node", "urn:inner:select:ns"));
        fn1.addContainmentNode(containmentNode);
        filterNodes.add(fn1);
        FilterNode fn2 = new FilterNode("test3", "urn:ietf:ns");
        fn2.addMatchNode("leaf-match", "urn:leafmatch:ns", "leaf-match-value");
        filterNodes.add(fn2);
        Pair<List<QName>, List<FilterNode>> pair = new Pair<>(Arrays.asList(m_leaf1, m_leaf2), filterNodes);
        map.put(m_interfacesStateId, pair);
        return map;
    }

    @Test
    public void testPreCommitChanged() throws SubSystemValidationException, NetconfMessageBuilderException {
        RequestScope.getCurrentScope().putInCache(RequestTask.CURRENT_REQ, m_request);
        m_deviceSubsystem.notifyPreCommitChange(m_changeNotifications);
        verify(m_deviceInterface).veto(eq(m_device), eq(m_request), m_documentCaptor.capture());
        assertEquals("<some-xml/>", DocumentUtils.documentToPrettyString(m_documentCaptor.getValue()).trim());
    }

    @Test
    public void testPreCommitChangedVetoThrowsException() throws SubSystemValidationException {
        RequestScope.getCurrentScope().putInCache(RequestTask.CURRENT_REQ, m_request);
        doThrow(new SubSystemValidationException("Veto threw exception")).when(m_deviceInterface).veto(eq(m_device), eq(m_request), m_documentCaptor.capture());
        try {
            m_deviceSubsystem.notifyPreCommitChange(m_changeNotifications);
            fail("Should have failed");
        } catch (SubSystemValidationException e) {
            assertEquals("Veto threw exception", e.getMessage());
            verify(m_deviceStore).setDeviceXml("<test/>");
        }
    }
}
