/*
 *   Copyright 2023 Broadband Forum
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.broadband_forum.obbaa.modelabstracter.impl;

import org.broadband_forum.obbaa.aggregator.api.DispatchException;
import org.broadband_forum.obbaa.aggregator.processor.AggregatorMessage;
import org.broadband_forum.obbaa.aggregator.processor.PmaAdapter;
import org.broadband_forum.obbaa.modelabstracter.ModelAbstracterManager;
import org.broadband_forum.obbaa.modelabstracter.converter.ConverterFactory;
import org.broadband_forum.obbaa.modelabstracter.converter.LineBandwidthProfilesConverter;
import org.broadband_forum.obbaa.modelabstracter.converter.NetworksConverter;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.pma.PmaRegistry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.w3c.dom.Document;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ModelAbstracterManagerImplTest {
    private static final String REQUEST = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" +
            "    <edit-config>" +
            "        <target>" +
            "            <running/>" +
            "        </target>" +
            "        <config>" +
            "            <line-bandwidth-profiles xmlns=\"urn:bbf:yang:obbaa:nt-line-profile\">" +
            "                <line-bandwidth-profile>" +
            "                    <name>bandwidth-profile-1</name>" +
            "                    <fixed-bandwidth>102400</fixed-bandwidth>" +
            "                    <assured-bandwidth>102400</assured-bandwidth>" +
            "                    <maximum-bandwidth>204800</maximum-bandwidth>" +
            "                </line-bandwidth-profile>" +
            "            </line-bandwidth-profiles>" +
            "        </config>" +
            "    </edit-config>" +
            "</rpc>";

    private static final String REQUEST2 = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" " +
            "message-id=\"9\">" +
            "    <edit-config>" +
            "        <target>" +
            "            <running/>" +
            "        </target>" +
            "        <config>" +
            "            <networks xmlns=\"urn:ietf:params:xml:ns:yang:ietf-network\" " +
            "xmlns:nt=\"urn:ietf:params:xml:ns:yang:ietf-network-topology\" " +
            "xmlns:bbf-an-nw-topology=\"urn:bbf:yang:obbaa:an-network-topology\">" +
            "                <network>" +
            "                    <network-id>network-fan1</network-id>" +
            "                    <supporting-network>" +
            "                        <network-ref>network-fan1</network-ref>" +
            "                    </supporting-network>" +
            "                    <node>" +
            "                        <node-id>OLT1</node-id>" +
            "                        <supporting-node>" +
            "                            <network-ref>network-fan1</network-ref>" +
            "                            <node-ref>p-OLT1</node-ref>" +
            "                        </supporting-node>" +
            "                        <nt:termination-point>" +
            "                            <nt:tp-id>uni1</nt:tp-id>" +
            "                            <nt:supporting-termination-point>" +
            "                                <nt:network-ref>network-fan1</nt:network-ref>" +
            "                                <nt:node-ref>OLT1</nt:node-ref>" +
            "                                <nt:tp-ref>pon-tp1</nt:tp-ref>" +
            "                            </nt:supporting-termination-point>" +
            "                            <bbf-an-nw-topology:tp-type>uni</bbf-an-nw-topology:tp-type>" +
            "                        </nt:termination-point>" +
            "                        <nt:termination-point>" +
            "                            <nt:tp-id>nni1</nt:tp-id>" +
            "                            <nt:supporting-termination-point>" +
            "                                <nt:network-ref>network-fan1</nt:network-ref>" +
            "                                <nt:node-ref>OLT1</nt:node-ref>" +
            "                                <nt:tp-ref>eth-tp1</nt:tp-ref>" +
            "                            </nt:supporting-termination-point>" +
            "                            <bbf-an-nw-topology:tp-type>nni</bbf-an-nw-topology:tp-type>" +
            "                        </nt:termination-point>" +
            "                    </node>" +
            "                </network>" +
            "            </networks>" +
            "        </config>" +
            "    </edit-config>" +
            "</rpc>";

    private ModelAbstracterManager modelAbstracterManager;

    @Before
    public void setUp() throws Exception {
        modelAbstracterManager = new ModelAbstracterManagerImpl();

        ConverterFactory mockFactory = Mockito.mock(ConverterFactory.class);
        Mockito.when(mockFactory.getConvertType("networks")).thenReturn(new NetworksConverter());
        Mockito.when(mockFactory.getConvertType("line-bandwidth-profiles")).thenReturn(new LineBandwidthProfilesConverter());
        PmaRegistry mockPmaRegistry = Mockito.mock(PmaRegistry.class);
        Map<NetConfResponse, List<Notification>> responseListMap = new HashMap<>();
        NetConfResponse resp = new NetConfResponse();
        resp.setOk(true);
        responseListMap.put(resp, null);
        Mockito.when(mockPmaRegistry.executeNC(Mockito.any(), Mockito.any())).thenReturn(responseListMap);

        Class<ModelAbstracterManagerImpl> clazz = ModelAbstracterManagerImpl.class;
        Field m_pmaRegistry = clazz.getDeclaredField("m_pmaRegistry");
        m_pmaRegistry.setAccessible(true);
        m_pmaRegistry.set(modelAbstracterManager, mockPmaRegistry);
        Field m_converterFactory = clazz.getDeclaredField("m_converterFactory");
        m_converterFactory.setAccessible(true);
        m_converterFactory.set(modelAbstracterManager, mockFactory);
    }

    @Test
    public void execute() throws DispatchException, NetconfMessageBuilderException, ExecutionException {
        Document document = AggregatorMessage.stringToDocument(REQUEST);
        EditConfigRequest ncReq = DocumentToPojoTransformer.getEditConfig(document);
        NetconfClientInfo clientInfo = new NetconfClientInfo("username", 1);
        NetConfResponse ncResp = modelAbstracterManager.execute(clientInfo, ncReq, REQUEST);
        Assert.assertTrue(ncResp.isOk());
    }

    @Test
    public void execute2() throws DispatchException, NetconfMessageBuilderException, ExecutionException {
        Document document = AggregatorMessage.stringToDocument(REQUEST2);
        EditConfigRequest ncReq = DocumentToPojoTransformer.getEditConfig(document);
        NetconfClientInfo clientInfo = new NetconfClientInfo("username", 1);
        NetConfResponse ncResp = modelAbstracterManager.execute(clientInfo, ncReq, REQUEST2);
        Assert.assertTrue(ncResp.isOk());
    }
}