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

package org.broadband_forum.obbaa.aggregator.jaxb.pma.api;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class PmaDeviceConfigRpcTest {
    private static String REQUEST_ALIGN = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"10101\">\n" +
            "  <action xmlns=\"urn:ietf:params:xml:ns:yang:1\">\n" +
            "      <pma-device-config xmlns=\"urn:bbf:yang:obbaa:pma-device-config\">\n" +
            "        <align/>\n" +
            "      </pma-device-config>\n" +
            "  </action>\n" +
            "</rpc>";

    private static String REQUEST_ALIGN_FORCE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"10101\">\n" +
            "  <action xmlns=\"urn:ietf:params:xml:ns:yang:1\">\n" +
            "      <pma-device-config xmlns=\"urn:bbf:yang:obbaa:pma-device-config\">\n" +
            "        <align>\n" +
            "          <force>\n" +
            "          </force>\n" +
            "        </align>\n" +
            "      </pma-device-config>\n" +
            "  </action>\n" +
            "</rpc>\n";

    @Test
    public void getPmaDeviceConfig() throws Exception {
        PmaDeviceConfigRpc pmaDeviceConfigRpc = PmaDeviceConfigRpc.getInstance(REQUEST_ALIGN);
        assertTrue(pmaDeviceConfigRpc.getPmaDeviceConfig().getPmaDeviceConfigAlign() != null);
    }

    @Test
    public void buildRpcReplyDataResponse() throws Exception {
        PmaDeviceConfigRpc pmaDeviceConfigRpc = PmaDeviceConfigRpc.getInstance(REQUEST_ALIGN_FORCE);
        assertTrue(pmaDeviceConfigRpc.buildRpcReplyDataResponse().contains("10101"));
    }
}