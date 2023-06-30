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

import org.broadband_forum.obbaa.aggregator.api.Aggregator;
import org.broadband_forum.obbaa.aggregator.api.DispatchException;
import org.broadband_forum.obbaa.aggregator.api.GlobalRequestProcessor;
import org.broadband_forum.obbaa.aggregator.impl.AggregatorImpl;
import org.broadband_forum.obbaa.modelabstracter.ModelAbstracterManager;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.concurrent.ExecutionException;

public class ModelAbstracterAdapterTest {
    private ModelAbstracterAdapter modelAbstracterAdapter;

    @Mock
    private Aggregator aggregator;

    @Mock
    private ModelAbstracterManager modelAbstracterManager;

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

    private static final String VALID_RESP = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" " +
            "message-id=\"1\">\n" +
            "   <ok/>\n" +
            "</rpc-reply>\n";

    @Before
    public void setUp() throws Exception {
        aggregator = Mockito.mock(AggregatorImpl.class);
        modelAbstracterManager = Mockito.mock(ModelAbstracterManagerImpl.class);
        modelAbstracterAdapter = new ModelAbstracterAdapter(aggregator, modelAbstracterManager);
    }

    @Test
    public void init() throws DispatchException {
        Mockito.doNothing().when(aggregator).addProcessor(Mockito.any(), Mockito.any());
        try {
            modelAbstracterAdapter.init();
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void destroy() throws DispatchException {
        Mockito.doNothing().when(aggregator).removeProcessor(Mockito.any(GlobalRequestProcessor.class));
        try {
            modelAbstracterAdapter.destroy();
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void processRequest() throws DispatchException, NetconfMessageBuilderException, ExecutionException {
        NetConfResponse ncResp = new NetConfResponse();
        ncResp.setOk(true);
        ncResp.setMessageId("1");
        Mockito.when(modelAbstracterManager.execute(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(ncResp);

        NetconfClientInfo clientInfo = new NetconfClientInfo("username", 1);
        String response = modelAbstracterAdapter.processRequest(clientInfo, REQUEST);
        Assert.assertEquals(VALID_RESP, response);
    }
}