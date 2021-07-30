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

package org.broadband_forum.obbaa.nbiadapter.netconf;

import static org.mockito.Mockito.mock;

import org.broadband_forum.obbaa.netconf.api.server.NetconfServerDispatcher;
import org.broadband_forum.obbaa.netconf.api.util.ExecutorServiceProvider;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.DynamicCapabilityProviderImpl;
import org.broadband_forum.obbaa.netconf.server.dispatcher.NetconfServerDispatcherImpl;
import org.junit.Before;
import org.junit.Test;

public class NbiSshNetconfServerTest {

    private NbiSshNetconfServer netconfServer;
    private NbiNetconfServerMessageListener netconfListener;
    private NbiSshNetconfAuth netconfAuth;
    private NetconfServerDispatcher dispatcher;
    private DynamicCapabilityProviderImpl provider;

    @Before
    public void setUp() throws Exception {
        netconfAuth = mock(NbiSshNetconfAuth.class);
        netconfListener = mock(NbiNetconfServerMessageListener.class);
        dispatcher = new NetconfServerDispatcherImpl(ExecutorServiceProvider.getInstance()
                .getExecutorService());
        provider = mock(DynamicCapabilityProviderImpl.class);
        netconfServer = new NbiSshNetconfServer(netconfAuth, netconfListener, dispatcher, provider);
        netconfServer.setPort(9293);
        netconfServer.setConnectionTimeout(100000);
    }

    @Test
    public void start() throws Exception{
        netconfServer.start();
        netconfServer.stop();
    }
}