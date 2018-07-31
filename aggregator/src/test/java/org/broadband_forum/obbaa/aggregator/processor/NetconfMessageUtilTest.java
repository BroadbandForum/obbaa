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

import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.w3c.dom.Document;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class NetconfMessageUtilTest {
    private static String TEST_REQUEST_AFTER_UNMOUNT =
            "<edit-config xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                    "  <target>\n" +
                    "    <running/>\n" +
                    "  </target>\n" +
                    "  <config>\n" +
                    "    <if:interfaces xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">\n" +
                    "      <if:interface xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xc:operation=\"create\">\n" +
                    "        <if:name>testxxxxxxadaf</if:name>\n" +
                    "        <if:type xmlns:ianaift=\"urn:ietf:params:xml:ns:yang:iana-if-type\">ianaift:ethernetCsmacd</if:type>\n" +
                    "      </if:interface>\n" +
                    "    </if:interfaces>\n" +
                    "  </config>\n" +
                    "</edit-config>\n";

    @Test
    public void buildRpcReplyOkTest() throws Exception {
        String result = NetconfMessageUtil.buildRpcReplyOk("202");
        assertTrue(result.contains("202"));
        assertTrue(result.contains("ok"));
    }

    @Test
    public void getParentXmlnsTest() throws Exception {
        Document document = AggregatorMessage.stringToDocument(TEST_REQUEST_AFTER_UNMOUNT);
        String xmlns = NetconfMessageUtil.getParentXmlns(document);
        assertFalse(xmlns.isEmpty());
    }

    @Test
    public void buildModuleCapabilityTest() throws Exception {
        ModuleIdentifier moduleIdentifier = NetconfMessageUtil.buildModuleIdentifier("network-manager",
                AggregatorMessage.NS_OBBAA_NETWORK_MANAGER,"2018-05-07");

        String result = NetconfMessageUtil.buildModuleCapability(moduleIdentifier);
        assertFalse(result.isEmpty());
        assertEquals("urn:bbf:yang:obbaa:network-manager?module=network-manager&revision=2018-05-07", result);
    }

}