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

package org.broadband_forum.obbaa.ipfix.ncclient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.broadband_forum.obbaa.ipfix.ncclient.app.NcClientServiceImpl;
import org.broadband_forum.obbaa.netconf.api.NetconfConfigurationBuilderException;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientConfiguration;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientDispatcher;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.transport.api.NetconfTransport;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NcClientServiceImplTest {

    private static final String getRpc = "<baa-network-manager:network-manager xmlns:baa-network-manager=\"urn:bbf:yang:obbaa:network-manager\">\n" +
            "      <baa-network-manager:managed-devices>\n" +
            "        <baa-network-manager:device>\n" +
            "        </baa-network-manager:device>\n" +
            "      </baa-network-manager:managed-devices>\n" +
            "    </baa-network-manager:network-manager>";

    private static final String getRpcInput = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"11\">\n" +
            "<get xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "  <filter type=\"subtree\">\n" +
            "    <network-manager xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
            "      <managed-devices>\n" +
            "      </managed-devices>\n" +
            "    </network-manager>\n" +
            "  </filter>\n" +
            "</get>\n" +
            "</rpc>";

    private static final String getRpcOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "<baa-network-manager:network-manager xmlns:baa-network-manager=\"urn:bbf:yang:obbaa:network-manager\">\n" +
            "      <baa-network-manager:managed-devices>\n" +
            "        <baa-network-manager:device>\n" +
            "        </baa-network-manager:device>\n" +
            "      </baa-network-manager:managed-devices>\n" +
            "    </baa-network-manager:network-manager>\n" +
            "</rpc-reply>\n";

    private static final String getConfigRpcInput = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"12\">\n" +
            "    <get-config xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "        <source>\n" +
            "            <running/>\n" +
            "        </source>\n" +
            "        <filter type=\"subtree\">\n" +
            "            <network-manager xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
            "                <managed-devices>\n" +
            "                </managed-devices>\n" +
            "            </network-manager>\n" +
            "        </filter>\n" +
            "    </get-config>\n" +
            "</rpc>";

    private static final String editConfigInput = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
            "    <edit-config>\n" +
            "        <target>\n" +
            "            <running />\n" +
            "        </target>\n" +
            "        <config>\n" +
            "            <network-manager xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
            "                <managed-devices>\n" +
            "                    <device xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xc:operation=\"create\">\n" +
            "                        <name>deviceA</name>\n" +
            "                    </device>\n" +
            "                </managed-devices>\n" +
            "            </network-manager>\n" +
            "        </config>\n" +
            "    </edit-config>\n" +
            "</rpc>";

    NcClientServiceImpl m_ncClientServiceImpl;

    NetConfResponse m_response = new NetConfResponse();
    @Mock
    NetconfClientDispatcher m_dispatcher;
    @Mock
    Future<NetconfClientSession> m_futureSession;
    @Mock
    NetconfClientSession m_session;
    @Mock
    NetconfClientConfiguration m_clientConfig;
    @Mock
    NetconfTransport m_transport;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(m_dispatcher.createClient(any())).thenReturn(m_futureSession);
        when(m_clientConfig.getTransport()).thenReturn(m_transport);
        when(m_transport.getTranportProtocol()).thenReturn("SSH");
        when(m_futureSession.get()).thenReturn(m_session);
        when(m_session.isOpen()).thenReturn(true);
        m_ncClientServiceImpl = new NcClientServiceImpl(m_dispatcher) {
            @Override
            protected NetconfClientConfiguration getNetconfClientConfiguration() throws NetconfConfigurationBuilderException {
                return m_clientConfig;
            }

            @Override
            protected void setClientTransportOrder(String hostname, int port) throws UnknownHostException {
            }
        };
    }

    @Test
    public void testPerformNcRequest_Get() throws Exception{
        m_response.setData(DocumentUtils.stringToDocument(getRpc).getDocumentElement());
        CompletableFuture<NetConfResponse> futureResponse = mock(CompletableFuture.class);
        when(futureResponse.get()).thenReturn(m_response);
        when(m_session.sendRpc(any())).thenReturn(futureResponse);
        NetconfFilter requestFilter = new NetconfFilter();
        GetRequest getRequest = new GetRequest();
        getRequest.setFilter(requestFilter);
        requestFilter.addXmlFilter(DocumentUtils.stringToDocument(getRpcInput).getDocumentElement());
        assertEquals(getRpcOutput, DocumentUtils.documentToPrettyString(m_ncClientServiceImpl.performNcRequest(getRequest).getResponseDocument()));
    }

    @Test
    public void testPerformNcRequest_NetconfMessageBuilderException() throws Exception {
        try {
            NetconfFilter requestFilter = new NetconfFilter();
            GetConfigRequest getConfigRequest = new GetConfigRequest();
            getConfigRequest.setFilter(requestFilter);
            requestFilter.addXmlFilter(DocumentUtils.stringToDocument(getConfigRpcInput).getDocumentElement());
            m_ncClientServiceImpl.performNcRequest(getConfigRequest);
        } catch (Exception e) {
            assertTrue(e instanceof NetconfMessageBuilderException);
        }
    }

    @Test
    public void testPerformNcRequest_ExecutionException() throws Exception {
        try {
            m_response = null;
            CompletableFuture<NetConfResponse> futureResponse = mock(CompletableFuture.class);
            when(futureResponse.get()).thenReturn(m_response);
            when(m_session.sendRpc(any())).thenReturn(futureResponse);
            NetconfFilter requestFilter = new NetconfFilter();
            EditConfigRequest editConfigRequest = new EditConfigRequest();
            requestFilter.addXmlFilter(DocumentUtils.stringToDocument(editConfigInput).getDocumentElement());
            m_ncClientServiceImpl.performNcRequest(editConfigRequest);
        } catch (Exception e) {
            assertTrue(e instanceof ExecutionException);
        }
    }

    @After
    public void destroy() {
        m_ncClientServiceImpl = null;
    }

    }
