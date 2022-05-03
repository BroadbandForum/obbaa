/*
 * Copyright 2022 Broadband Forum
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

import org.broadband_forum.obbaa.device.adapter.AdapterContext;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
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
import org.broadband_forum.obbaa.nf.entities.NetworkFunction;
import org.broadband_forum.obbaa.pma.DeviceXmlStore;
import org.broadband_forum.obbaa.pma.NetconfNetworkFunctionAlignmentService;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NetworkFunctionSubsystemTest {
    NetworkFunctionSubsystem m_networkFunctionSubsystem;
    private ModelNodeId m_interfacesStateId;
    private QName m_leaf1;
    private QName m_leaf2;
    @Mock
    private NetworkFunction m_networkFunction;
    @Captor
    private ArgumentCaptor<GetRequest> m_requestCaptor;
    @Mock
    private EditConfigRequest m_request;
    @Mock
    private NetconfNetworkFunctionAlignmentService m_nas;
    @Mock
    private SchemaRegistry m_schemaReg;
    @Mock
    private Future<NetConfResponse> m_netconfResponse;
    @Mock
    private AdapterManager m_adapterManager;
    @Mock
    private AdapterContext m_context;
    @Mock
    private DeviceXmlStore m_deviceStore;
    @Mock
    private DeviceXmlStore m_backupStore;
    @Captor
    ArgumentCaptor<Document> m_updatedDsCaptor;
    @Captor
    ArgumentCaptor<Document> m_oldDsCaptor;
    private List<ChangeNotification> m_changeNotifications = new ArrayList<>();
    private static final String UPDATED_DS = "<updated-ds/>";
    private static final String OLD_DS = "<old-ds/>";

    @Before
    public void setUp() throws ExecutionException {
        MockitoAnnotations.initMocks(this);
        m_interfacesStateId = new ModelNodeId("/container=interfaces-state", "urn:ietf:ns");
        m_leaf1 = QName.create("urn:ietf:ns", "leaf1");
        m_leaf2 = QName.create("urn:ietf:ns", "leaf2");
        m_networkFunctionSubsystem = new NetworkFunctionSubsystem(m_nas);
        PmaServer.setCurrentNetworkFunction(m_networkFunction);
        PmaServer.setCurrentDeviceXmlStore(m_deviceStore);
        PmaServer.setBackupDeviceXmlStore(m_backupStore);
        when(m_deviceStore.getDeviceXml()).thenReturn(UPDATED_DS);
        when(m_backupStore.getDeviceXml()).thenReturn(OLD_DS);
        when(m_adapterManager.getAdapterContext(any())).thenReturn(m_context);
    }

    @After
    public void tearDown(){
        PmaServer.clearCurrentDevice();
        RequestScope.resetScope();
    }

    @Test
    public void testSubsystemSchedulesEditWithOriginalRequest(){
        RequestScope.getCurrentScope().putInCache(RequestTask.CURRENT_REQ, m_request);
        m_networkFunctionSubsystem.notifyChanged(Collections.emptyList());
        verify(m_nas).queueEdit(m_networkFunction.getNetworkFunctionName(), m_request);
    }

    @Test
    public void testNcSessionMgrIsContactedToRetrieveStateValues()
            throws GetAttributeException, ExecutionException, NetconfMessageBuilderException, IOException,
            SAXException {
        //network functions do not have get or state values implemented yet
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
        //network function subsystem notifyPreCommitChanged currently does nothing
    }

    @Test
    public void testPreCommitChangedVetoThrowsException() throws SubSystemValidationException, NetconfMessageBuilderException {
        /* veto is currently not called by notifyPreCommitChanged in network function subsystem,
        because network function adapters do not exist yet*/
    }
}
