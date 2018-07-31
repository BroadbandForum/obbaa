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

package org.broadband_forum.obbaa.connectors.sbi.netconf.impl;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientConfiguration;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientDispatcher;
import org.broadband_forum.obbaa.netconf.api.client.TcpServerSession;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CallHomeServiceImplTest {
    CallHomeServiceImpl m_callHomeService;
    @Mock
    private NetconfClientConfiguration m_tlsConfiguration;
    @Mock
    private NetconfClientDispatcher m_netconfClientDispatcher;
    @Mock
    private TcpServerSession m_tcpSession;
    private Future<TcpServerSession> m_tcpSessionFuture;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        m_tcpSessionFuture = CompletableFuture.completedFuture(m_tcpSession);
        m_callHomeService = new CallHomeServiceImpl(m_tlsConfiguration, m_netconfClientDispatcher);
        when(m_netconfClientDispatcher.createReverseClient(m_tlsConfiguration)).thenReturn(m_tcpSessionFuture);
    }

    @Test
    public void verifyInitInvokesDispatcher() throws Exception {
        verifyZeroInteractions(m_netconfClientDispatcher);

        m_callHomeService.init();

        verify(m_netconfClientDispatcher).createReverseClient(m_tlsConfiguration);
    }

    @Test
    public void testDestroyClosesTCPSession() throws Exception {
        m_callHomeService.init();
        verifyZeroInteractions(m_tcpSession);

        m_callHomeService.destroy();

        verify(m_tcpSession).closeTcpChannel();
    }


}
