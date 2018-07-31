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

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.pma.PmaServer;
import org.broadband_forum.obbaa.store.dm.DeviceInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Created by kbhatk on 10/30/17.
 */
public class TransparentPmaSessionTest {
    private static final String OK_RESPONSE = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "  <ok/>\n" +
            "</rpc-reply>\n";
    public static final String REQ_STR = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "  <get>\n" +
            "    <filter>\n" +
            "      <device-manager xmlns=\"http://www.test.com/management-solutions\" " +
            "xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "      </device-manager>\n" +
            "    </filter>\n" +
            "  </get>\n" +
            "</rpc>\n";
    private TransparentPmaSession m_pmaSession;
    @Mock
    private NetconfConnectionManager m_cm;
    @Mock
    private DeviceInfo m_device1Meta;
    @Mock
    private PmaServer m_pmaServer;

    private NetConfResponse m_response = new NetConfResponse().setOk(true).setMessageId("1");

    @Before
    public void setUp() throws ExecutionException, InterruptedException {
        MockitoAnnotations.initMocks(this);
        when(m_cm.isConnected(m_device1Meta)).thenReturn(true);
        Future<NetConfResponse> futureObject = mock(Future.class);
        when(futureObject.get()).thenReturn(m_response);
        when(m_cm.executeNetconf((DeviceInfo) anyObject(), anyObject())).thenReturn(futureObject);
        m_pmaSession = new TransparentPmaSession(m_device1Meta, m_cm);

    }

    @Test
    public void testExceptionIsThrownWhenInvalidReqIsSent(){
        try{
            m_pmaSession.executeNC("<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" > <blah xmlns=\"\" /> </rpc>");
            fail("Expected an exception here");
        }catch (IllegalArgumentException e){
            assertEquals("Invalid netconf request received : <rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" > <blah xmlns=\"\" /> </rpc>", e.getMessage());
        }
    }

    @Test
    public void testPmaContactsDMAndCMToExecuteNC() throws ExecutionException {
        String response = m_pmaSession.executeNC(REQ_STR);
        ArgumentCaptor<AbstractNetconfRequest> reqCaptor = ArgumentCaptor.forClass(AbstractNetconfRequest.class);

        verify(m_cm).executeNetconf(eq(m_device1Meta), reqCaptor.capture());
        assertEquals(REQ_STR, reqCaptor.getValue().requestToString());
        assertEquals(OK_RESPONSE, response);
    }

}
