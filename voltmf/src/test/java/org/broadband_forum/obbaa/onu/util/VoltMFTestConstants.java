/*
 * Copyright 2021 Broadband Forum
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

package org.broadband_forum.obbaa.onu.util;

public interface VoltMFTestConstants {

    static final String OLT_NAME = "pOLT";
    static final String CHANNEL_TERMINATION = "CT_1";
    static final String ONU_ID_VALUE = "1";
    static final String ONU_DEVICE_NAME = "test_ont";
    static final String ONU_DEVICE_TYPE = "ONU";
    static final String OLT_DEVICE_TYPE = "OLT";
    static final String DEVICE_MODEL = "standard";
    static final String DEVICE_VENDOR = "BBF";
    static final String INTERFACE_VERSION = "1.0";
    static final int THREAD_POOL_COUNT = 1;
    static final String NETWORK_MANAGER_NAMESPACE = "urn:bbf:yang:obbaa:network-manager";
    static final String OBBAA_NETWORK_MANAGER = "bbf-obbaa-network-manager:network-manager";
    static final String DEFAULT_MESSAGE_ID = "0";
    static final String NETWORK_MANAGER_REVISION = "2021-02-01";
    static final String NETWORK_MANAGER = "network-manager";
    static final String NEVER_ALIGNED = "Never Aligned";
    static final String ONU_DETECTED = "detect";
    static final String ONU_UNDETECTED = "undetect";
    static final String SERIAL_NUMBER = "BRCM00000001";
    static final String REGISTER_NUMBER = "12345678";
    static final String ONU = "onu";
    static final String VOMCI_FUNCTION_NAMESPACE = "urn:bbf:yang:bbf-vomci-function";

            //Status constants
    static final String OK_RESPONSE = "OK";
    static final String NOK_RESPONSE = "NOK";

    // Kafka topics
    static final String ONU_REQUEST_KAFKA_TOPIC = "OBBAA_ONU_REQUEST";

    // Events
    static final String REQUEST_EVENT = "request";

    // Operation type constants
    static final String ONU_GET_OPERATION = "get";
    static final String ONU_EDIT_OPERATION = "edit-config";

    //Request
    static final String FILTER_GET_REQUEST = "<network-manager xmlns=\"urn:bbf:yang:obbaa:network-manager\"><managed-devices><device>"
            + "<name>onu</name><device-management><device-state><onu-state-info xmlns=\"urn:bbf:yang:obbaa:onu-management\"><equipment-id/>"
            + "<software-images><software-image/></software-images></onu-state-info></device-state></device-management>"
            + "</device></managed-devices></network-manager>";

    static final String FILTER_JSON_STRING = "{\"bbf-obbaa-network-manager:network-manager\":{\"managed-devices\":{\"device\":[{\"device-management\""
            + ":{\"device-state\":{\"bbf-obbaa-onu-management:onu-state-info\":{\"equipment-id\":\"\",\"software-images\""
            + ":{\"software-image\":{}}}}},\"name\":\"test_ont\"}]}}}";

    //RPC
    static final String GET_REQUEST = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n"
            + "  <get>\n"
            + "    <filter type=\"subtree\">\n"
            + "      <interfaces-state xmlns=\"urn:ietf:ns\">\n"
            + "        <leaf1/>\n"
            + "        <leaf2/>\n"
            + "      </interfaces-state>\n"
            + "    </filter>\n"
            + "  </get>\n"
            + "</rpc>";

    static final String INTERNAL_GET_REQUEST_WITH_DEFAULT_MESSAGE_ID = "<rpc message-id=\"0\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n"
            + "<get>\n"
            + "<filter type=\"subtree\">\n"
            + "<network-manager xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n"
            + "<managed-devices>\n"
            + "<device>\n"
            + "<name>onu</name>\n"
            + "<device-management>\n"
            + "<device-state>\n"
            + "<onu-state-info xmlns=\"urn:bbf:yang:obbaa:onu-management\">\n"
            + "<equipment-id/>\n"
            + "<software-images>\n"
            + "<software-image/>\n"
            + "</software-images>\n"
            + "</onu-state-info>\n"
            + "</device-state>\n"
            + "</device-management>\n"
            + "</device>\n"
            + "</managed-devices>\n"
            + "</network-manager>\n"
            + "</filter>\n"
            + "</get>\n"
            + "</rpc>\n";



    //response
    static final String RESPONSE_DEFAULT_MSG_ID = "{\n"
            + "    \"olt-name\": \"pOLT\",\n"
            + "    \"channel-termination-ref\": \"CT_1\",\n"
            + "    \"event\": \"request\",\n"
            + "    \"payload\": \"{\\\"operation\\\":\\\"get\\\", \\\"identifier\\\":\\\"0\\\", \\\"filters\\\""
            + ":{\\\"network-manager:device-management\\\":{\\\"device-state\\\":{\\\"bbf-obbaa-onu-management:onu-state-info\\\""
            + ":{\\\"equipment-id\\\":\\\"\\\",\\\"software-images\\\":{\\\"software-image\\\":{}}}}}}}\",\n"
            + "    \"onu-id\": \"1\",\n"
            + "    \"labels\": {}\n"
            + "}";

    static final String RESPONSE_NOT_DEFAULT_MSG_ID = "{\n"
            + "    \"olt-name\": \"pOLT\",\n"
            + "    \"channel-termination-ref\": \"CT_1\",\n"
            + "    \"event\": \"request\",\n"
            + "    \"payload\": \"{\\\"operation\\\":\\\"get\\\", \\\"identifier\\\":\\\"1\\\", \\\"filters\\\""
            + ":{\\\"network-manager:root\\\""
            + ":{\\\"bbf-obbaa-network-manager:network-manager\\\":{\\\"managed-devices\\\":{\\\"device\\\":[{\\\"device-management\\\""
            + ":{\\\"device-state\\\":{\\\"bbf-obbaa-onu-management:onu-state-info\\\""
            + ":{\\\"equipment-id\\\":\\\"\\\",\\\"software-images\\\""
            + ":{\\\"software-image\\\":{}}}}},\\\"name\\\":\\\"test_ont\\\"}]}}}}}\",\n"
            + "    \"onu-id\": \"1\",\n"
            + "    \"labels\": {}\n"
            + "}";

    static final String GET_REQUEST_RESPONSE = "{\n"
            + "    \"olt-name\": \"pOLT\",\n"
            + "    \"payload\": \"{\\\"operation\\\":\\\"get\\\", \\\"identifier\\\":\\\"0\\\", \\\"filters\\\""
            + ":{\\\"network-manager:device-management\\\":{\\\"device-state\\\":{\\\"bbf-obbaa-onu-management:onu-state-info\\\""
            + ":{\\\"equipment-id\\\":\\\"\\\",\\\"software-images\\\":{\\\"software-image\\\":{}}}}}}}\",\n"
            + "    \"onu-name\": \"onu\",\n"
            + "    \"channel-termination-ref\": \"1\",\n"
            + "    \"event\": \"request\",\n"
            + "    \"onu-id\": \"CT_1\",\n"
            + "    \"labels\": {\"name\": \"vendor\"}\n"
            + "}";

    static final String EXPECTED_RESPONSE_DEFAULT_MESSAGE_ID = "{\n"
            + "    \"olt-name\": \"pOLT\",\n"
            + "    \"payload\": \"{\\\"operation\\\":\\\"request\\\",\\\"identifier\\\":\\\"0\\\"}\",\n"
            + "    \"onu-name\": \"test_ont\",\n"
            + "    \"channel-termination-ref\": \"CT_1\",\n"
            + "    \"event\": \"request\",\n"
            + "    \"onu-id\": \"1\",\n"
            + "    \"labels\": \"}\"\n"
            + "}";

    static final String EXPECTED_RESPONSE_NOT_DEFAULT_MESSAGE_ID = "{\n"
            + "    \"olt-name\": \"pOLT\",\n"
            + "    \"payload\": \"{\\\"operation\\\":\\\"request\\\",\\\"identifier\\\":\\\"1\\\"}\",\n"
            + "    \"onu-name\": \"test_ont\",\n"
            + "    \"channel-termination-ref\": \"CT_1\",\n"
            + "    \"event\": \"request\",\n"
            + "    \"onu-id\": \"1\",\n"
            + "    \"labels\": \"}\"\n"
            + "}";

    static final String EXPECTED_RESPONSE_WHEN_DEVICE_IS_NULL = "{\n"
            + "    \"olt-name\": \"pOLT\",\n"
            + "    \"payload\": \"{\\\"operation\\\":\\\"get\\\",\\\"identifier\\\":\\\"0\\\",\\\"filters\\\""
            + ":{\\\"network-manager:device-management\\\":{\\\"device-state\\\":{\\\"bbf-obbaa-onu-management:onu-state-info\\\""
            + ":{\\\"equipment-id\\\":\\\"\\\",\\\"software-images\\\":{\\\"software-image\\\":{}}}}}}\",\n"
            + "    \"onu-name\": \"test_ont\",\n"
            + "    \"channel-termination-ref\": \"CT_1\",\n"
            + "    \"event\": \"request\",\n"
            + "    \"onu-id\": \"1\",\n"
            + "    \"labels\": \"}\"\n"
            + "}";

    static final String EXPECTED_RESPONSE_FOR_DETECT_DEVICE = "{\n"
           + "    \"olt-name\": \"pOLT\",\n"
           + "    \"payload\": \"{\\\"operation\\\":\\\"detect\\\",\\\"identifier\\\":\\\"0\\\"}\",\n"
           + "    \"onu-name\": \"test_ont\",\n"
           + "    \"channel-termination-ref\": \"CT_1\",\n"
           + "    \"event\": \"detect\",\n"
           + "    \"onu-id\": \"1\",\n"
           + "    \"labels\": \"}\"\n"
           + "}";

    static final String EXPECTED_RESPONSE_FOR_UNDETECT_DEVICE = "{\n" +
            "    \"olt-name\": \"pOLT\",\n" +
            "    \"payload\": \"{\\\"operation\\\":\\\"undetect\\\",\\\"identifier\\\":\\\"0\\\"}\",\n" +
            "    \"onu-name\": \"test_ont\",\n" +
            "    \"channel-termination-ref\": \"CT_1\",\n" +
            "    \"event\": \"undetect\",\n" +
            "    \"onu-id\": \"1\",\n" +
            "    \"labels\": \"}\"\n" +
            "}";

    String XML_ACTION_REQUEST = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n"
                                + "  <ietf-action:action xmlns:ietf-action=\"urn:ietf:params:xml:ns:yang:1\">\n"
                                + "    <bbf-vomci-func:managed-onus xmlns:bbf-vomci-func=\"urn:bbf:yang:bbf-vomci-function\">\n"
                                + "      <bbf-vomci-func:managed-onu>\n"
                                + "        <bbf-vomci-func:name>onu1</bbf-vomci-func:name>\n"
                                + "        <bbf-vomci-func:set-onu-communication>\n"
                                + "          <bbf-vomci-func:onu-communication-available>true</bbf-vomci-func:onu-communication-available>\n"
                                + "          <bbf-vomci-func:olt-remote-endpoint-name>endpoint1</bbf-vomci-func:olt-remote-endpoint-name>\n"
                                + "          <bbf-vomci-func:voltmf-remote-endpoint-name>endpoint2</bbf-vomci-func:voltmf-remote-endpoint-name>\n"
                                + "        </bbf-vomci-func:set-onu-communication>\n"
                                + "      </bbf-vomci-func:managed-onu>\n"
                                + "    </bbf-vomci-func:managed-onus>\n"
                                + "  </ietf-action:action>\n"
                                + "</rpc>";

    String JSON_PAYLOAD_ACTION_REQUEST = "\"bbf-vomci-function:managed-onus\":{\n"
                                         + "     \"managed-onu\":{\n"
                                         + "         \"name\":\"onu1\"\n"
                                         + "         \"set-onu-communication\":{\n"
                                         + "            \"onu-communication-available\": \"true\",\n"
                                         + "            \"olt-remote-endpoint-name\":    \"endpoint1\",\n"
                                         + "            \"voltmf-remote-endpoint-name\": \"endpoint2\"\n"
                                         + "         }\n"
                                         + "     }\n"
                                         + "}";

    String EDIT_CONFIG_REQUEST = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"20\">\n"
           + "  <edit-config>\n"
           + "    <target>\n"
           + "      <running/>\n"
           + "    </target>\n"
           + "    <config>\n"
           + "      <network-manager xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n"
           + "      <network-functions>\n"
           + "        <network-function>\n"
           + "          <name>vomci-vendor-1</name>\n"
           + "          <root>\n"
           + "\n"
           + "           <bbf-vomci-func:vomci xmlns:bbf-vomci-func=\"urn:bbf:yang:bbf-vomci-function\">\n"
           + "            <bbf-vomci-func:vomci-msg-timeout>30</bbf-vomci-func:vomci-msg-timeout>\n"
           + "           </bbf-vomci-func:vomci>\n"
           + "\n"
           + "          </root>\n"
           + "        </network-function>\n"
           + "      </network-functions>\n"
           + "      </network-manager>\n"
           + "    </config>\n"
           + "  </edit-config>\n"
           + "\n"
           + "</rpc>";
}
