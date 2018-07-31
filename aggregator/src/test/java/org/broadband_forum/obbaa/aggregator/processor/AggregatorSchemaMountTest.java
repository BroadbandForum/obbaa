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

import org.broadband_forum.obbaa.aggregator.api.Aggregator;
import org.broadband_forum.obbaa.aggregator.impl.AggregatorImpl;
import org.broadband_forum.obbaa.aggregator.jaxb.networkmanager.api.NetworkManagerRpc;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class AggregatorSchemaMountTest {
    private Aggregator m_aggregator;

    private static String TEST_REQUEST = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1519954457470\">\n" +
                    "  <get>\n" +
                    "    <filter type=\"subtree\">\n" +
                    "      <managed-devices xmlns=\"urn:ietf:params:xml:ns:yang:ietf-yang-schema-mount\">\n" +
                    "      </managed-devices>\n" +
                    "    </filter>\n" +
                    "  </get>\n" +
                    "</rpc>\n";

    private static String TEST_RESPONSE = "<rpc-reply message-id=\"1519954457470\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "  <data>\n" +
            "    <schema-mounts xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
            "      <namespace>\n" +
            "        <prefix>network-manager</prefix>\n" +
            "        <uri>urn:bbf:yang:obbaa:network-manager</uri>\n" +
            "      </namespace>\n" +
            "      <mount-point>\n" +
            "        <module>network-manager</module>\n" +
            "        <label>root</label>\n" +
            "        <config>true</config>\n" +
            "      </mount-point>\n" +
            "    </schema-mounts>\n" +
            "  </data>\n" +
            "</rpc-reply>\n";

    @Test
    public void processRequest() throws Exception {
        m_aggregator = new AggregatorImpl();
        AggregatorSchemaMount aggregatorSchemaMount = new AggregatorSchemaMount(m_aggregator);
        aggregatorSchemaMount.init();

        String response = aggregatorSchemaMount.processRequest(TEST_REQUEST);
        assertTrue(response.contains(NetworkManagerRpc.NAMESPACE));
        assertTrue(response.contains("root"));
    }

}