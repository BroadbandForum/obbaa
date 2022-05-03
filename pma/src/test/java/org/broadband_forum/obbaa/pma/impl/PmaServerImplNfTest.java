/*
 * Copyright 2022 Broadband Forum
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

import com.google.common.io.Files;
import org.broadband_forum.obbaa.device.adapter.AdapterContext;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigElement;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.DataStore;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetconfServer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.nf.entities.NetworkFunction;
import org.broadband_forum.obbaa.pma.DeviceXmlStore;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.broadband_forum.obbaa.pma.PmaServer.PMA_USER;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class PmaServerImplNfTest {
    PmaServerImplNf m_pmaServer;
    private GetRequest m_getRequest;
    private EditConfigRequest m_editRequest;
    @Mock
    private NetconfServer m_netconfServer;
    @Mock
    private NetworkFunction m_networkFunction;
    @Mock
    private Pair<ModelNodeDataStoreManager, DeviceXmlStore> m_dsmXmlPair;
    @Mock
    private AdapterContext m_stdAdapter;
    @Mock
    private DataStore m_dataStore;
    private File m_tempDir;
    @Mock
    private DeviceXmlStore m_deviceDataStore;


    @Before
    public void setUp() {
        m_getRequest = new GetRequest();
        m_getRequest.setMessageId("1");
        m_editRequest = new EditConfigRequest();
        m_editRequest.setMessageId("2");
        EditConfigElement element = new EditConfigElement();
        m_editRequest.setConfigElement(element);
        MockitoAnnotations.initMocks(this);
        m_tempDir = Files.createTempDir();
        String deviceFileBaseDir = m_tempDir.getAbsolutePath();
        when(m_netconfServer.getDataStore("running")).thenReturn(m_dataStore);
        when(m_dsmXmlPair.getSecond()).thenReturn(m_deviceDataStore);
        when(m_deviceDataStore.getDeviceXml()).thenReturn("<test-xml/>");
        m_pmaServer = new PmaServerImplNf(m_networkFunction, m_netconfServer, m_dsmXmlPair, m_stdAdapter, deviceFileBaseDir);
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
        }).when(m_netconfServer).onGet(eq(PMA_USER), eq(m_getRequest), anyObject());
        doAnswer(invocation -> {
            NetConfResponse response = (NetConfResponse) invocation.getArguments()[2];
            response.addDataContent(DocumentUtils.stringToDocument("<ok xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\"/>"
            ).getDocumentElement());
            return null;
        }).when(m_netconfServer).onEditConfig(eq(PMA_USER), eq(m_editRequest), anyObject());
    }

    @Test
    public void testDeviceInfoIsSet() {
        assertEquals(m_networkFunction, m_pmaServer.getNetworkFunction());
    }

    @Test
    public void testPmaServerContactsServerMessageListener()
            throws NetconfMessageBuilderException, IOException, SAXException {
        Map<NetConfResponse, List<Notification>> netConfResponseListMap = m_pmaServer.executeNetconf(m_getRequest);
        Map.Entry<NetConfResponse, List<Notification>> entry = netConfResponseListMap.entrySet().iterator().next();
        NetConfResponse response = entry.getKey();
        verify(m_netconfServer).onGet(eq(PMA_USER), eq(m_getRequest), anyObject());
//        assertEquals("<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
//                "  <data>\n" +
//                "    <if:interfaces xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">\n" +
//                "    <if:interface>\n" +
//                "        <if:name>xdsl-line:1/1/1/new</if:name>\n" +
//                "        <if:type xmlns:bbfift=\"urn:broadband-forum-org:yang:bbf-if-type\">bbfift:xdsl</if:type>\n" +
//                "    </if:interface>\n" +
//                "</if:interfaces>\n" +
//                "  </data>\n" +
//                "</rpc-reply>\n", response.responseToString());


        String expect = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "  <data>\n" +
                "    <if:interfaces xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">\n" +
                "    <if:interface>\n" +
                "        <if:name>xdsl-line:1/1/1/new</if:name>\n" +
                "        <if:type xmlns:bbfift=\"urn:broadband-forum-org:yang:bbf-if-type\">bbfift:xdsl</if:type>\n" +
                "    </if:interface>\n" +
                "</if:interfaces>\n" +
                "  </data>\n" +
                "</rpc-reply>\n";

        TestUtil.assertXMLEquals(DocumentUtils.stringToDocument(expect).getDocumentElement(),
                DocumentUtils.stringToDocument(response.responseToString()).getDocumentElement());

        verifyZeroInteractions(m_deviceDataStore);
    }

    @Test
    public void testPmaServerContactsServerMessageListenerOnEdit()
            throws NetconfMessageBuilderException, IOException, SAXException {
        Map<NetConfResponse, List<Notification>> netConfResponseListMap = m_pmaServer.executeNetconf(m_editRequest);
        Map.Entry<NetConfResponse, List<Notification>> entry = netConfResponseListMap.entrySet().iterator().next();
        NetConfResponse response = entry.getKey();
        verify(m_netconfServer).onEditConfig(eq(PMA_USER), eq(m_editRequest), anyObject());
//        assertEquals("<rpc-reply message-id=\"2\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
//                "  <data>\n" +
//                "    <ok/>\n" +
//                "  </data>\n" +
//                "</rpc-reply>", response.responseToString().trim());


        String expect = "<rpc-reply message-id=\"2\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "  <data>\n" +
                "    <ok/>\n" +
                "  </data>\n" +
                "</rpc-reply>";

        TestUtil.assertXMLEquals(DocumentUtils.stringToDocument(expect).getDocumentElement(),
                DocumentUtils.stringToDocument(response.responseToString().trim()).getDocumentElement());

        verify(m_deviceDataStore).getDeviceXml();
    }
}
