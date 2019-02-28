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

package org.broadband_forum.obbaa.aggregator.processor;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.verify;

import org.broadband_forum.obbaa.aggregator.api.Aggregator;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetconfServer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


public class NotificationAdapterTest {

    private NotificationAdapter m_notificationAdapter;
    @Mock
    private Aggregator m_aggregator;
    @Mock
    private NetconfServer m_netconfServer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        m_notificationAdapter = new NotificationAdapter(m_aggregator, m_netconfServer);
    }

    @Test
    public void testProcessRequest() throws Exception {
        String netconfRequest = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"4\">\n" +
                "    <get>\n" +
                "        <filter>\n" +
                "            <manageEvent:netconf xmlns:manageEvent=\"urn:ietf:params:xml:ns:netmod:notification\" />\n" +
                "        </filter>\n" +
                "    </get>\n" +
                "</rpc>";

        NetconfClientInfo clientInfo = new NetconfClientInfo("test", 1);
        m_notificationAdapter.processRequest(clientInfo, netconfRequest);
        verify(m_netconfServer).onGet(anyObject(), anyObject(), anyObject());
    }
}