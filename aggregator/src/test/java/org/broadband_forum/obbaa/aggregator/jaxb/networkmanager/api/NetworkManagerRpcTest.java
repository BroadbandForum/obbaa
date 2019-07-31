/*
 *  Copyright 2019 Broadband Forum
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.broadband_forum.obbaa.aggregator.jaxb.networkmanager.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.broadband_forum.obbaa.aggregator.api.DispatchException;
import org.broadband_forum.obbaa.aggregator.jaxb.aggregatorimpl.AggregatorRpcMessage;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * 2019/5/8 09:46
 *
 */
public class NetworkManagerRpcTest {

    private static final String GET_REQUEST_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            +"<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"12\">\n"
            +"   <get>\n"
            +"      <filter type=\"subtree\">\n"
            +"         <network-manager xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n"
            +"            <managed-devices>\n"
            +"               <device>\n"
            +"                  <name>deviceA</name>\n"
            +"                  <root />\n"
            +"               </device>\n"
            +"            </managed-devices>\n"
            +"         </network-manager>\n"
            +"      </filter>\n"
            +"   </get>\n"
            +"</rpc>";

    private static final String GET_CONFIG_REQUEST_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            +"<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"12\">\n"
            +"   <get-config>\n"
            +"   <source>\n"
            +"     <running/>\n"
            +"   </source>\n"
            +"      <filter type=\"subtree\">\n"
            +"         <network-manager xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n"
            +"            <managed-devices>\n"
            +"               <device>\n"
            +"                  <name>deviceA</name>\n"
            +"                  <root />\n"
            +"               </device>\n"
            +"            </managed-devices>\n"
            +"         </network-manager>\n"
            +"      </filter>\n"
            +"   </get-config>\n"
            +"</rpc>";


    @Test
    public void packageResponseForGetOperation() throws DispatchException, NetconfMessageBuilderException, IOException, SAXException, JAXBException {

        AggregatorRpcMessage aggregatorRpcMessage = new AggregatorRpcMessage(GET_REQUEST_XML);
        NetworkManagerRpc networkManagerRpc = new NetworkManagerRpc(GET_REQUEST_XML,
                aggregatorRpcMessage.getNetconfRpcMessage(), aggregatorRpcMessage.getOnlyOneTopPayload());
        Document rspDoc = DocumentUtils.stringToDocument("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"12\">\n"
                + "   <data>\n"
                + "      <hw:hardware xmlns:hw=\"urn:ietf:params:xml:ns:yang:ietf-hardware\">\n"
                + "         <hw:component>\n"
                + "            <hw:name>port.0.1.0.0.1</hw:name>\n"
                + "            <hw:alias />\n"
                + "            <hw:asset-id />\n"
                + "            <hw:class xmlns:ianahw=\"urn:ietf:params:xml:ns:yang:iana-hardware\">ianahw:port</hw:class>\n"
                + "            <hw:mfg-name />\n"
                + "            <hw:parent>hw-ont.0.1.0.0</hw:parent>\n"
                + "            <hw:parent-rel-pos>0</hw:parent-rel-pos>\n"
                + "            <hw:uri />\n"
                + "         </hw:component>\n"
                + "      </hw:hardware>\n"
                + "   </data>\n"
                + "</rpc-reply>");

        Map<Document, String> resultMap = new HashMap<Document, String>();
        resultMap.put(rspDoc, "deviceA");
        String result = networkManagerRpc.packageResponse(resultMap);

        String expectXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"12\">\n"
                + "   <data>\n"
                + "      <network-manager xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n"
                + "         <managed-devices>\n"
                + "            <device>\n"
                + "               <name>deviceA</name>\n"
                + "               <root>\n"
                + "                  <hw:hardware xmlns:hw=\"urn:ietf:params:xml:ns:yang:ietf-hardware\">\n"
                + "                     <hw:component>\n"
                + "                        <hw:name>port.0.1.0.0.1</hw:name>\n"
                + "                        <hw:alias />\n"
                + "                        <hw:asset-id />\n"
                + "                        <hw:class xmlns:ianahw=\"urn:ietf:params:xml:ns:yang:iana-hardware\">ianahw:port</hw:class>\n"
                + "                        <hw:mfg-name />\n"
                + "                        <hw:parent>hw-ont.0.1.0.0</hw:parent>\n"
                + "                        <hw:parent-rel-pos>0</hw:parent-rel-pos>\n"
                + "                        <hw:uri />\n"
                + "                     </hw:component>\n"
                + "                  </hw:hardware>\n"
                + "               </root>\n"
                + "            </device>\n"
                + "         </managed-devices>\n"
                + "      </network-manager>\n"
                + "   </data>\n"
                + "</rpc-reply>";

        TestUtil.assertXMLEquals(DocumentUtils.stringToDocument(expectXml).getDocumentElement(),
                DocumentUtils.stringToDocument(result).getDocumentElement());
    }

    @Test
    public void packageResponseForGetConfigOperation() throws DispatchException, NetconfMessageBuilderException, IOException, SAXException, JAXBException {

        AggregatorRpcMessage aggregatorRpcMessage = new AggregatorRpcMessage(GET_CONFIG_REQUEST_XML);
        NetworkManagerRpc networkManagerRpc = new NetworkManagerRpc(GET_CONFIG_REQUEST_XML,
                aggregatorRpcMessage.getNetconfRpcMessage(), aggregatorRpcMessage.getOnlyOneTopPayload());
        Document rspDoc = DocumentUtils.stringToDocument("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"12\">\n"
                + "   <data>\n"
                + "      <hw:hardware xmlns:hw=\"urn:ietf:params:xml:ns:yang:ietf-hardware\">\n"
                + "         <hw:component>\n"
                + "            <hw:name>port.0.1.0.0.1</hw:name>\n"
                + "            <hw:alias />\n"
                + "            <hw:asset-id />\n"
                + "            <hw:class xmlns:ianahw=\"urn:ietf:params:xml:ns:yang:iana-hardware\">ianahw:port</hw:class>\n"
                + "            <hw:mfg-name />\n"
                + "            <hw:parent>hw-ont.0.1.0.0</hw:parent>\n"
                + "            <hw:parent-rel-pos>0</hw:parent-rel-pos>\n"
                + "            <hw:uri />\n"
                + "         </hw:component>\n"
                + "      </hw:hardware>\n"
                + "   </data>\n"
                + "</rpc-reply>");

        Map<Document, String> resultMap = new HashMap<Document, String>();
        resultMap.put(rspDoc, "deviceA");
        String result = networkManagerRpc.packageResponse(resultMap);

        String expectXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"12\">\n"
                + "   <data>\n"
                + "      <network-manager xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n"
                + "         <managed-devices>\n"
                + "            <device>\n"
                + "               <name>deviceA</name>\n"
                + "               <root>\n"
                + "                  <hw:hardware xmlns:hw=\"urn:ietf:params:xml:ns:yang:ietf-hardware\">\n"
                + "                     <hw:component>\n"
                + "                        <hw:name>port.0.1.0.0.1</hw:name>\n"
                + "                        <hw:alias />\n"
                + "                        <hw:asset-id />\n"
                + "                        <hw:class xmlns:ianahw=\"urn:ietf:params:xml:ns:yang:iana-hardware\">ianahw:port</hw:class>\n"
                + "                        <hw:mfg-name />\n"
                + "                        <hw:parent>hw-ont.0.1.0.0</hw:parent>\n"
                + "                        <hw:parent-rel-pos>0</hw:parent-rel-pos>\n"
                + "                        <hw:uri />\n"
                + "                     </hw:component>\n"
                + "                  </hw:hardware>\n"
                + "               </root>\n"
                + "            </device>\n"
                + "         </managed-devices>\n"
                + "      </network-manager>\n"
                + "   </data>\n"
                + "</rpc-reply>";

        TestUtil.assertXMLEquals(DocumentUtils.stringToDocument(expectXml).getDocumentElement(),
                DocumentUtils.stringToDocument(result).getDocumentElement());
    }
}