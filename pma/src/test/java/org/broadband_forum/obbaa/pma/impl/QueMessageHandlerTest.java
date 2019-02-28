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

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.CloseSessionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerMessageListener;
import org.broadband_forum.obbaa.netconf.api.server.ResponseChannel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


public class QueMessageHandlerTest {
    @Mock
    NetconfServerMessageListener m_netconfServerMessageListener;
    @Mock
    NetconfClientInfo m_clientInfo;
    @Mock
    CloseSessionRequest m_closeSessionRequest;
    @Mock
    GetConfigRequest m_getConfigRequest;
    @Mock
    ResponseChannel m_responseChannel;

    QueMessageHandler m_queMessageHandler;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        m_queMessageHandler = new QueMessageHandler(m_netconfServerMessageListener);
    }

    @Test
    public void testProcessRequest() {
        m_queMessageHandler.processRequest(m_clientInfo, m_closeSessionRequest, m_responseChannel);
        verify(m_netconfServerMessageListener).onCloseSession(eq(m_clientInfo), eq(m_closeSessionRequest), anyObject());
        m_queMessageHandler.processRequest(m_clientInfo, m_getConfigRequest, m_responseChannel);
        verify(m_netconfServerMessageListener).onGetConfig(eq(m_clientInfo), eq(m_getConfigRequest), anyObject());
    }

}