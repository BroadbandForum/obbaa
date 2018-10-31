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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.server.RequestTask;
import org.broadband_forum.obbaa.pma.NetconfDeviceAlignmentService;
import org.broadband_forum.obbaa.pma.PmaServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yangtools.yang.common.QName;

public class DeviceSubsystemTest {

    DeviceSubsystem m_deviceSubsystem;
    private ModelNodeId m_interfacesStateId;
    private QName m_leaf1;
    private QName m_leaf2;
    @Mock
    private NetconfConnectionManager m_dcm;
    @Mock
    private Device m_device;
    @Captor
    private ArgumentCaptor<AbstractNetconfRequest> m_requestCaptor;
    @Mock
    private EditConfigRequest m_request;
    @Mock
    private NetconfDeviceAlignmentService m_das;
    @Mock
    private SchemaRegistry m_schemaReg;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        m_interfacesStateId = new ModelNodeId("/container=interfaces-state", "urn:ietf:ns");
        m_leaf1 = QName.create("urn:ietf:ns", "leaf1");
        m_leaf2 = QName.create("urn:ietf:ns", "leaf3");
        m_deviceSubsystem = new DeviceSubsystem(m_dcm, m_das, m_schemaReg);
        PmaServer.setCurrentDevice(m_device);
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

    @Ignore
    @Test
    public void testNcSessionMgrIsContactedToRetrieveStateValues() throws GetAttributeException, ExecutionException {
        Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> attributes = getAttributes();
        m_deviceSubsystem.retrieveStateAttributes(attributes, new NetconfQueryParams(UNBOUNDED, true));
        verify(m_dcm).executeNetconf(eq(m_device.getDeviceName()), m_requestCaptor.capture());
        assertEquals("", m_requestCaptor.getValue().requestToString());
    }

    private Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> getAttributes() {
        Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> map = new HashMap<>();
        List<FilterNode> filterNodes = new ArrayList<>();
        FilterNode fn1 = new FilterNode();
        FilterNode selectNode = new FilterNode();
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
        FilterNode fn2 = new FilterNode();
        fn2.addMatchNode("leaf-match", "urn:leafmatch:ns", "leaf-match-value");
        filterNodes.add(fn2);
        Pair<List<QName>, List<FilterNode>> pair = new Pair<>(Arrays.asList(m_leaf1, m_leaf2), filterNodes);
        map.put(m_interfacesStateId, pair);
        return map;
    }
}
