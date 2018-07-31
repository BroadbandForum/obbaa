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

import static org.broadband_forum.obbaa.pma.PmaServer.PMA_USER;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetconfServer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.store.dm.DeviceInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PmaServerImplTest {
    PmaServerImpl m_pmaServer;
    private GetRequest m_request;
    @Mock
    private NetconfServer m_netconfServer;
    @Mock
    private DeviceInfo m_deviceInfo;
    @Mock
    private ModelNodeDataStoreManager m_dsm;
    @Captor
    private ArgumentCaptor<NetConfResponse> m_responseCaptor;

    @Before
    public void setUp() {
        m_request = new GetRequest();
        m_request.setMessageId("1");
        MockitoAnnotations.initMocks(this);
        m_pmaServer = new PmaServerImpl(m_deviceInfo, m_netconfServer, m_dsm);
        doAnswer(invocation -> {
            NetConfResponse response = (NetConfResponse) invocation.getArguments()[2];
            response.addDataContent(DocumentUtils.stringToDocument("<if:interfaces\n" +
                    "                            xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">\n" +
                    "    <if:interface>\n" +
                    "        <if:name>xdsl-line:1/1/1/new</if:name>\n" +
                    "        <if:type xmlns:bbfift=\"urn:broadband-forum-org:yang:bbf-if-type\">bbfift:xdsl</if" +
                    ":type>\n" +
                    "    </if:interface>\n" +
                    "</if:interfaces>").getDocumentElement());
            return null;
        }).when(m_netconfServer).onGet(eq(PMA_USER), eq(m_request), anyObject());
    }

    @Test
    public void testDeviceInfoIsSet(){
        assertEquals(m_deviceInfo, m_pmaServer.getDeviceInfo());
    }

    @Test
    public void testPmaServerContactsServerMessageListener() {
        NetConfResponse response = m_pmaServer.executeNetconf(m_request);
        verify(m_netconfServer).onGet(eq(PMA_USER), eq(m_request), anyObject());
        assertEquals("<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "  <data>\n" +
                "    <if:interfaces xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">\n" +
                "    <if:interface>\n" +
                "        <if:name>xdsl-line:1/1/1/new</if:name>\n" +
                "        <if:type xmlns:bbfift=\"urn:broadband-forum-org:yang:bbf-if-type\">bbfift:xdsl</if:type>\n" +
                "    </if:interface>\n" +
                "</if:interfaces>\n" +
                "  </data>\n" +
                "</rpc-reply>\n", response.responseToString());
    }
}
