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
import org.broadband_forum.obbaa.dm.DeviceManager;
import org.broadband_forum.obbaa.dm.impl.DeviceManagerImpl;
import org.broadband_forum.obbaa.pma.DeviceModelDeployer;
import org.broadband_forum.obbaa.pma.PmaRegistry;
import org.broadband_forum.obbaa.pma.impl.DeviceModelDeployerImpl;
import org.broadband_forum.obbaa.pma.impl.PmaRegistryImpl;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PmaAdapterTest {
    private static PmaAdapter m_pmaAdapter;

    @Mock
    private static Aggregator m_aggregator;

    @Mock
    private static DeviceManager m_deviceManager;

    @Mock
    private static PmaRegistry m_pmaRegistry;

    @Mock
    private static DeviceModelDeployer m_deviceModelDeployer;

    private static String RESPONSE_OK = "ok";
    private static String DEVICE_NAME_A = "deviceA";
    private static String REQUEST_DEVICE_CONFIG_GET =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1520261367256\">\n" +
                    "  <get-config>\n" +
                    "    <source>\n" +
                    "      <running />\n" +
                    "    </source>\n" +
                    "    <filter type=\"subtree\">\n" +
                    "      <interfaces xmlns=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\" xmlns:ip=\"urn:ietf:params:xml:ns:yang:ietf-ip\" xmlns:v6ur=\"urn:ietf:params:xml:ns:yang:ietf-ipv6-unicast-routing\" xmlns:fiber=\"urn:broadband_forum:yang:broadband_forum-fiber\" xmlns:broadband_forum-subif=\"urn:broadband_forum:yang:broadband_forum-sub-interfaces\" xmlns:huawei-interfaces=\"urn:huawei:params:xml:ns:yang:huawei-interfaces\">\n" +
                    "        <interface>\n" +
                    "          <name>channelpartition.0.1.0.1</name>\n" +
                    "          <fiber:channelpartition />\n" +
                    "        </interface>\n" +
                    "      </interfaces>\n" +
                    "    </filter>\n" +
                    "  </get-config>\n" +
                    "</rpc>";

    private static String REQUEST_RELOAD_YANG =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"10101\">\n" +
                    "  <action xmlns=\"urn:ietf:params:xml:ns:yang:1\">\n" +
                    "    <pma-yang-library xmlns=\"urn:bbf:yang:obbaa:pma-yang-library\">\n" +
                    "        <reload/>\n" +
                    "    </pma-yang-library>\n" +
                    "  </action>\n" +
                    "</rpc>\n";

    private static String REQUEST_ALIGN = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1527307907464\">\n" +
            "  <action>\n" +
            "      <pma-device-config xmlns=\"urn:bbf:yang:obbaa:pma-device-config\">\n" +
            "        <align>\n" +
            "        </align>\n" +
            "      </pma-device-config>\n" +
            "  </action>\n" +
            "</rpc>";

    private static String REQUEST_ALIGN_FORCE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1527307907464\">\n" +
            "  <action>\n" +
            "      <pma-device-config xmlns=\"urn:bbf:yang:obbaa:pma-device-config\">\n" +
            "        <align>\n" +
            "          <force>\n" +
            "          </force>\n" +
            "        </align>\n" +
            "      </pma-device-config>\n" +
            "  </action>\n" +
            "</rpc>\n";

    private static String REQUEST_ALIGN_STATE_GET =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"10101\">\n" +
                    "<get xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                    "  <source>\n" +
                    "    <running/>\n" +
                    "  </source>\n" +
                    "  <filter type=\"subtree\">\n" +
                    "    <pma-device-config xmlns=\"urn:bbf:yang:obbaa:pma-device-config\">\n" +
                    "    </pma-device-config>\n" +
                    "  </filter>\n" +
                    "</get>\n" +
                    "</rpc>\n";

    @BeforeClass
    public static void setUp() throws Exception {
        m_aggregator = mock(AggregatorImpl.class);
        m_deviceManager = mock(DeviceManagerImpl.class);
        m_pmaRegistry = mock(PmaRegistryImpl.class);
        m_deviceModelDeployer = mock(DeviceModelDeployerImpl.class);

        m_pmaAdapter = new PmaAdapter(m_aggregator, m_deviceManager, m_pmaRegistry, m_deviceModelDeployer);
        m_pmaAdapter.init();
    }

    @Test
    public void processRequest() throws Exception {
        when(m_pmaRegistry.executeNC(DEVICE_NAME_A, REQUEST_DEVICE_CONFIG_GET)).thenReturn(RESPONSE_OK);
        String response = m_pmaAdapter.processRequest(DEVICE_NAME_A, REQUEST_DEVICE_CONFIG_GET);
        assertEquals(RESPONSE_OK, response);
    }

    @Test
    public void processRequestReloadYang() throws Exception {
        String response = m_pmaAdapter.processRequest(REQUEST_RELOAD_YANG);
        assertTrue(response.contains(RESPONSE_OK));
    }

    @Test
    public void processRequestAlign() throws Exception {
        String response = m_pmaAdapter.processRequest(DEVICE_NAME_A, REQUEST_ALIGN);
        verify(m_pmaRegistry).align(DEVICE_NAME_A);
        assertTrue(response.contains(RESPONSE_OK));
    }

    @Test
    public void processRequestAlignForce() throws Exception {
        String response = m_pmaAdapter.processRequest(DEVICE_NAME_A, REQUEST_ALIGN_FORCE);
        verify(m_pmaRegistry).forceAlign(DEVICE_NAME_A);
        assertTrue(response.contains(RESPONSE_OK));
    }

    @Test
    public void processRequestAlignGet() throws Exception {
        String response = m_pmaAdapter.processRequest(DEVICE_NAME_A, REQUEST_ALIGN_STATE_GET);
        assertTrue(response.contains("alignment-state"));
    }
}