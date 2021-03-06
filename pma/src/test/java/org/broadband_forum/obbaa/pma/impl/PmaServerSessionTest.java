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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.pma.NetconfDeviceAlignmentService;
import org.broadband_forum.obbaa.pma.PmaServer;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class PmaServerSessionTest {
    public static final String EDIT_REQ = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "  <edit-config>\n" +
            "    <target>\n" +
            "      <running/>\n" +
            "    </target>\n" +
            "    <default-operation>merge</default-operation>\n" +
            "    <test-option>set</test-option>\n" +
            "    <error-option>stop-on-error</error-option>\n" +
            "    <config>\n" +
            "      <if:interfaces xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">\n" +
            "                <if:interface>\n" +
            "                    <if:name>xdsl-line:1/1/1/new</if:name>\n" +
            "                    <if:type xmlns:bbfift=\"urn:bbf:yang:bbf-if-type\">bbfift:xdsl</if:type>\n" +
            "                </if:interface>\n" +
            "            </if:interfaces>\n" +
            "    </config>\n" +
            "  </edit-config>\n" +
            "</rpc>\n";
    PmaServerSession m_session;
    @Mock
    private PmaServer m_server;
    @Captor
    private ArgumentCaptor<AbstractNetconfRequest> m_reqCaptor;
    @Captor
    private ArgumentCaptor<NetConfResponse> m_resCaptor;
    @Mock
    private Device m_device;
    @Mock
    private NetConfResponse m_response;
    @Mock
    private NetconfDeviceAlignmentService m_das;
    private List<Element> m_dataContent;
    private static Element m_element1;
    private static Element m_element2;

    @BeforeClass
    public static void beforeClass() throws NetconfMessageBuilderException {
        m_element1 = DocumentUtils.stringToDocument("<some-config1 xmlns=\"some:ns\"/>").getDocumentElement();
        m_element2 = DocumentUtils.stringToDocument("<some-config2 xmlns=\"some:ns\"/>").getDocumentElement();
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        m_dataContent = new ArrayList<>();
        m_dataContent.add(m_element1);
        m_dataContent.add(m_element2);
        when(m_response.getDataContent()).thenReturn(m_dataContent);
        Map<NetConfResponse, List<Notification>> map = new HashMap<>();
        map.put(m_response, Collections.emptyList());
        when(m_server.executeNetconf(anyObject())).thenReturn(map);
        m_session = new PmaServerSession(m_device, m_server, m_das);
    }

    @Test
    public void testPmaSessionContactsPmaServer() throws NetconfMessageBuilderException, IOException, SAXException {
        m_session.executeNC(EDIT_REQ);
        verify(m_server).executeNetconf(m_reqCaptor.capture());
        //assertEquals(EDIT_REQ, m_reqCaptor.getValue().requestToString());
        TestUtil.assertXMLEquals(DocumentUtils.stringToDocument(EDIT_REQ).getDocumentElement(),
                DocumentUtils.stringToDocument(m_reqCaptor.getValue().requestToString()).getDocumentElement());
    }


    @Test
    public void testFullResyncSendsCC() throws NetconfMessageBuilderException, IOException, SAXException {
        m_session.forceAlign();

        verify(m_server).executeNetconf(m_reqCaptor.capture());
//        assertEquals("<rpc message-id=\"internal\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
//                "  <get-config>\n" +
//                "    <source>\n" +
//                "      <running/>\n" +
//                "    </source>\n" +
//                "  </get-config>\n" +
//                "</rpc>\n", m_reqCaptor.getAllValues().get(0).requestToString());

        String expect = "<rpc message-id=\"internal\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "  <get-config>\n" +
                "    <source>\n" +
                "      <running/>\n" +
                "    </source>\n" +
                "  </get-config>\n" +
                "</rpc>\n";

        TestUtil.assertXMLEquals(DocumentUtils.stringToDocument(expect).getDocumentElement(),
                DocumentUtils.stringToDocument(m_reqCaptor.getAllValues().get(0).requestToString()).getDocumentElement());
        verify(m_das).forceAlign(any(Device.class), any(NetConfResponse.class));
    }
}
