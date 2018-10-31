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

import org.broadband_forum.obbaa.aggregator.api.DeviceManagementProcessor;
import org.broadband_forum.obbaa.aggregator.impl.SingleDeviceRequest;
import org.broadband_forum.obbaa.aggregator.jaxb.networkmanager.api.NetworkManagerRpc;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AggregatorMessageTest {
    private AggregatorMessage m_aggregatorMessage;

    @Mock
    private DeviceManagementProcessor m_deviceManagementProcessor;

    private static String TEST_DEVICE_NAME_A = "deviceA";
    private static String TEST_DEVICE_NAME_B = "deviceB";

    private static String TEST_MOUNTED_REQUEST =
            "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                    "<get xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
                    "<managed-devices xmlns=\"urn:bbf:yang:obbaa:network-manager\">" +
                        "<device>" +
                        "<name>deviceA</name>" +
                        "<type>DPU</type>" +
                        "<root>" +
                            "<system xmlns=\"urn:ietf:params:xml:ns:yang:ietf-system\">" +
                            "<name>A</name>" +
                            "<type>AA</type>" +
                            "<restart/>" +
                            "</system>" +
                        "</root>" +
                        "</device>" +
                        "<device>" +
                        "<name>deviceB</name>" +
                        "<type>DPU</type>" +
                        "<root>" +
                            "<system xmlns=\"urn:ietf:params:xml:ns:yang:ietf-vlan\">" +
                            "<name>B</name>" +
                            "<type>BB</type>" +
                            "<restart/>" +
                            "</system>" +
                        "</root>" +
                        "</device>" +
                    "</managed-devices>" +
                    "</get>" +
                    "</rpc>";

    private static String TEST_RPC_REPLY_MSG_A = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "  <fiber-traffic-descriptor-profiles xmlns=\"urn:broadband_forum:yang:broadband_forum-fiber\">\n" +
            "    <traffic-descriptor-profile>\n" +
            "      <name>test1</name>\n" +
            "      <fixed-bandwidth>10240000</fixed-bandwidth>\n" +
            "    </traffic-descriptor-profile>\n" +
            "  </fiber-traffic-descriptor-profiles>\n" +
            "</data>\n";

    private static String TEST_RPC_REPLY_MSG_B = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1520491011755\">\n" +
            "  <data>\n" +
            "    <system-state xmlns=\"urn:ietf:params:xml:ns:yang:ietf-system\">\n" +
            "      <platform>\n" +
            "        <os-name>Linux</os-name>\n" +
            "        <os-release>Linux SSH-OLT 3.10.53-HULK2 #1 SMP Tue Feb 13 07:40:21 UTC 2018 armv7l GNU/Linux</os-release>\n" +
            "        <os-version>232510</os-version>\n" +
            "        <machine>MA5800-X7</machine>\n" +
            "      </platform>\n" +
            "      <clock>\n" +
            "        <current-datetime>2000-05-30T05:02:39Z</current-datetime>\n" +
            "        <boot-datetime>2000-05-24T06:07:52Z</boot-datetime>\n" +
            "      </clock>\n" +
            "    </system-state>\n" +
            "  </data>\n" +
            "</rpc-reply>\n";

    @Before
    public void setUp() throws Exception {
        m_aggregatorMessage = new AggregatorMessage(TEST_MOUNTED_REQUEST);
        assertTrue(m_aggregatorMessage != null);

        m_deviceManagementProcessor = mock(DeviceManagementProcessor.class);
    }

    @Test
    public void isNetworkManageMountMessageTest() throws Exception {
        assertTrue(m_aggregatorMessage.isNetworkManageMountMessage());
    }

    @Test
    public void getParentXmlnsTest() throws Exception {
        assertEquals(NetworkManagerRpc.NAMESPACE, m_aggregatorMessage.getParentXmlns());
    }

    @Test
    public void getDocumentEveryDeviceTest() throws Exception {
        when(m_deviceManagementProcessor.getDeviceTypeByDeviceName("deviceA")).thenReturn("");
        when(m_deviceManagementProcessor.getDeviceTypeByDeviceName("deviceB")).thenReturn("");
        Set<SingleDeviceRequest> requests = m_aggregatorMessage.getDocumentEveryDevice(m_deviceManagementProcessor);
        assertTrue(requests.size() == 2);
    }

    @Test
    public void containMountedMessageTest() throws Exception {
        assertTrue(m_aggregatorMessage.containMountedMessage());
    }

    @Test
    public void packageResponseTest() throws Exception {
        Map<Document, String> inputMap = new HashMap<>();
        inputMap.put(AggregatorMessage.stringToDocument(TEST_RPC_REPLY_MSG_A), TEST_DEVICE_NAME_A);
        inputMap.put(AggregatorMessage.stringToDocument(TEST_RPC_REPLY_MSG_B), TEST_DEVICE_NAME_B);

        String responses = m_aggregatorMessage.packageResponse(inputMap);
        assertFalse(responses.isEmpty());
        assertTrue(responses.contains(TEST_DEVICE_NAME_B));
    }

    @Test
    public void toStringTest() throws Exception {
        assertEquals(TEST_MOUNTED_REQUEST, m_aggregatorMessage.toString());
    }
}