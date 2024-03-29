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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.pma.PmaServer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.xml.sax.SAXException;

/**
 * Created by kbhatk on 10/30/17.
 */
public class TransparentPmaSessionTest {
    private static final String OK_RESPONSE = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "  <ok/>\n" +
            "</rpc-reply>\n";
    public static final String REQ_STR = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "  <get>\n" +
            "    <filter type=\"subtree\">\n" +
            "      <network-manager xmlns=\"http://www.test.com/management-solutions\" " +
            "xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "      </network-manager>\n" +
            "    </filter>\n" +
            "  </get>\n" +
            "</rpc>\n";
    private TransparentPmaSession m_pmaSession;
    @Mock
    private NetconfConnectionManager m_cm;
    @Mock
    private Device m_device1Meta;
    @Mock
    private PmaServer m_pmaServer;

    private NetConfResponse m_response = new NetConfResponse().setOk(true).setMessageId("1");

    @Before
    public void setUp() throws ExecutionException, InterruptedException {
        MockitoAnnotations.initMocks(this);
        when(m_cm.isConnected(m_device1Meta)).thenReturn(true);
        Future<NetConfResponse> futureObject = mock(Future.class);
        when(futureObject.get()).thenReturn(m_response);
        when(m_cm.executeNetconf((Device) anyObject(), anyObject())).thenReturn(futureObject);
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
    public void testPmaContactsDMAndCMToExecuteNC()
            throws ExecutionException, NetconfMessageBuilderException, IOException, SAXException {
        Map<NetConfResponse , List<Notification>> netConfResponseListMap = m_pmaSession.executeNC(REQ_STR);
        Map.Entry<NetConfResponse , List<Notification>> entry = netConfResponseListMap.entrySet().iterator().next();
        String response = entry.getKey().responseToString();
        ArgumentCaptor<AbstractNetconfRequest> reqCaptor = ArgumentCaptor.forClass(AbstractNetconfRequest.class);

        verify(m_cm).executeNetconf(eq(m_device1Meta), reqCaptor.capture());

        TestUtil.assertXMLEquals(DocumentUtils.stringToDocument(REQ_STR).getDocumentElement(),
                DocumentUtils.stringToDocument(reqCaptor.getValue().requestToString()).getDocumentElement());

        TestUtil.assertXMLEquals(DocumentUtils.stringToDocument(OK_RESPONSE).getDocumentElement(),
                DocumentUtils.stringToDocument(response).getDocumentElement());
    }

}
