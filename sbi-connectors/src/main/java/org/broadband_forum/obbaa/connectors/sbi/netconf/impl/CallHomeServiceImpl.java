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

import java.util.concurrent.ExecutionException;

import org.broadband_forum.obbaa.connectors.sbi.netconf.CallHomeService;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientConfiguration;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientDispatcher;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientDispatcherException;
import org.broadband_forum.obbaa.netconf.api.client.TcpServerSession;

public class CallHomeServiceImpl implements CallHomeService {
    private final NetconfClientConfiguration m_tlsConfiguration;
    private final NetconfClientDispatcher m_netconfClientDispatcher;
    private TcpServerSession m_tcpSession;

    public CallHomeServiceImpl(NetconfClientConfiguration tlsConfiguration, NetconfClientDispatcher
            netconfClientDispatcher) {
        m_tlsConfiguration = tlsConfiguration;
        m_netconfClientDispatcher = netconfClientDispatcher;
    }

    public void init() throws NetconfClientDispatcherException, ExecutionException, InterruptedException {
        m_tcpSession = m_netconfClientDispatcher.createReverseClient(m_tlsConfiguration).get();
    }

    public void destroy() {
        m_tcpSession.closeTcpChannel();
    }

}
