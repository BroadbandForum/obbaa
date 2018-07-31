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

package org.broadband_forum.obbaa.aggregator.impl;

import org.broadband_forum.obbaa.aggregator.api.GlobalRequestProcessor;
import org.broadband_forum.obbaa.aggregator.api.DeviceConfigProcessor;
import org.broadband_forum.obbaa.aggregator.api.DeviceManagementProcessor;
import org.broadband_forum.obbaa.aggregator.api.NotificationProcessor;
import org.broadband_forum.obbaa.aggregator.api.ProcessorCapability;
import org.broadband_forum.obbaa.aggregator.processor.NetconfMessageUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class AggregatorImplTest {
    private AggregatorImpl m_aggregator;

    private NotificationProcessor m_notificationProcessor;

    @Mock
    private DeviceConfigProcessor m_deviceConfigProcessorDPU;

    @Mock
    private DeviceConfigProcessor m_deviceConfigProcessorOLT;

    @Mock
    private DeviceManagementProcessor m_deviceManagementProcessor;

    @Mock
    private GlobalRequestProcessor m_globalRequestProcessor;

    private static String OLT = "OLT";
    private static String DPU = "DPU";

    private ModuleIdentifier m_moduleIdentifierYangLibrary;
    private ModuleIdentifier m_moduleIdentifierSchemaMount;
    private ModuleIdentifier m_moduleIdentifierTestA;
    private ModuleIdentifier m_moduleIdentifierTestB;
    private Set<ModuleIdentifier> m_moduleIdentifiers;

    private static String RESPONSE_OK =
            "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" " +
                    "message-id=\"101\">\n" +
                    "  <ok />\n" +
                    "</rpc-reply>\n";

    private static String REQUEST_DEVICE_CONFIG = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1527307907664\">\n" +
            "<edit-config xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "  <target>\n" +
            "    <running/>\n" +
            "  </target>\n" +
            "  <config>\n" +
            "    <managed-devices xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
            "      <device>\n" +
            "        <device-name>deviceA</device-name>\n" +
            "        <root>\n" +
            "          <if:interfaces xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">\n" +
            "            <if:interface xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xc:operation=\"create\">\n" +
            "              <if:name>interfaceA</if:name>\n" +
            "              <if:type xmlns:ianaift=\"urn:ietf:params:xml:ns:yang:iana-if-type\">ianaift:ethernetCsmacd</if:type>\n" +
            "            </if:interface>\n" +
            "          </if:interfaces>\n" +
            "        </root>\n" +
            "      </device>\n" +
            "      <device>\n" +
            "        <device-name>deviceB</device-name>\n" +
            "      </device>\n" +
            "    </managed-devices>\n" +
            "  </config>\n" +
            "</edit-config>\n" +
            "</rpc>\n";

    private static String REQUEST_DEVICE_ADD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1527307907664\">\n" +
            "  <edit-config>\n" +
            "    <target>\n" +
            "      <running />\n" +
            "    </target>\n" +
            "    <config>\n" +
            "      <managed-devices xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
            "        <device operation=\"create\">\n" +
            "          <device-name>deviceA</device-name>\n" +
            "          <device-management>\n" +
            "            <device-type>DPU</device-type>\n" +
            "            <device-software-version>1.0.0</device-software-version>\n" +
            "            <device-vendor>huawei</device-vendor>\n" +
            "            <device-connection>\n" +
            "              <connection-model>direct</connection-model>\n" +
            "              <password-auth>\n" +
            "                <authentication>\n" +
            "                 <address>10.93.101.67</address>\n" +
            "                <management-port>830</management-port>\n" +
            "                <user-name>netconf</user-name>\n" +
            "                <password>netconf</password>\n" +
            "                </authentication>\n" +
            "              </password-auth>\n" +
            "            </device-connection>\n" +
            "          </device-management>\n" +
            "        </device>\n" +
            "        <device>\n" +
            "          <device-name>deviceB</device-name>\n" +
            "        </device>\n" +
            "      </managed-devices>\n" +
            "    </config>\n" +
            "  </edit-config>\n" +
            "</rpc>\n";

    private static String REQUEST_SCHEMA_MOUNT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1520261367256\">\n" +
            "  <get>\n" +
            "    <source>\n" +
            "      <running />\n" +
            "    </source>\n" +
            "    <filter type=\"subtree\">\n" +
            "      <schema-mounts xmlns=\"urn:ietf:params:xml:ns:yang:ietf-yang-schema-mount\">\n" +
            "      <mount-point>\n" +
            "      </mount-point>\n" +
            "     </schema-mounts>\n" +
            "    </filter>\n" +
            "  </get>\n" +
            "</rpc>\n";

    private static String REQUEST_YANG_LIBRAARY = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1520261367256\">\n" +
            "  <get>\n" +
            "    <source>\n" +
            "      <running />\n" +
            "    </source>\n" +
            "    <filter type=\"subtree\">\n" +
            "      <yang-library xmlns=\"urn:ietf:params:xml:ns:yang:ietf-yang-library\">\n" +
            "     </yang-library>\n" +
            "    </filter>\n" +
            "  </get>\n" +
            "</rpc>\n";

    private static String REQUEST_DEVICE_SERVICE_GET = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1527307907664\">\n" +
            "<get-config xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "  <source>\n" +
            "    <running/>\n" +
            "  </source>\n" +
            "  <filter type=\"subtree\">\n" +
            "    <managed-devices xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
            "      <device>\n" +
            "        <device-name>deviceA</device-name>\n" +
            "        <root>\n" +
            "          <interfaces xmlns=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\"/>\n" +
            "        </root>\n" +
            "      </device>\n" +
            "    </managed-devices>\n" +
            "  </filter>\n" +
            "</get-config>\n" +
            "</rpc>\n";

    private static String REQUEST_DEVICE_GET = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<rpc template-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "<get xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "  <source>\n" +
            "    <running/>\n" +
            "  </source>\n" +
            "  <filter type=\"subtree\">\n" +
            "    <managed-devices xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
            "      <device/>\n" +
            "    </managed-devices>\n" +
            "  </filter>\n" +
            "</get>\n" +
            "</rpc>\n";

    private static String REQUEST_DEVICE_GET_CONFIG = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<rpc template-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "<get-config xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "  <source>\n" +
            "    <running/>\n" +
            "  </source>\n" +
            "  <filter type=\"subtree\">\n" +
            "    <managed-devices xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
            "      <device/>\n" +
            "    </managed-devices>\n" +
            "  </filter>\n" +
            "</get-config>\n" +
            "</rpc>\n";

    public static String NOTIFICATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<notification xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">\n" +
            "  <eventTime>2000-06-03T06:38:06Z</eventTime>\n" +
            "  <board-remove xmlns=\"urn:huawei:params:xml:ns:yang:huawei-board\">\n" +
            "    <type>H901GPHD</type>\n" +
            "    <name>0.2</name>\n" +
            "  </board-remove>\n" +
            "  <board-remove xmlns=\"urn:huawei:params:xml:ns:yang:huawei-board\">\n" +
            "    <type>TestBoard</type>\n" +
            "    <name>0.5</name>\n" +
            "  </board-remove>\n" +
            "</notification>";

    private static String DEVICE_NAME_TEST = "deviceName-ABC";

    private void buildModuleIdentifierYangLibrary() {
        m_moduleIdentifierYangLibrary = NetconfMessageUtil.buildModuleIdentifier("ietf-yang-library",
                "urn:ietf:params:xml:ns:yang:ietf-yang-library",
                "2017-10-30");
    }

    private void buildModuleIdentifierSchemaMount() {
        m_moduleIdentifierSchemaMount = NetconfMessageUtil.buildModuleIdentifier("ietf-yang-schema-mount",
                "urn:ietf:params:xml:ns:yang:ietf-yang-schema-mount",
                "2018-04-05");
    }

    private void buildModuleIdentifierTest() {
        m_moduleIdentifierTestA = NetconfMessageUtil.buildModuleIdentifier("module-test-a",
                "urn:broadband_forum:yang:obbaa:module-test-a",
                "2017-11-10");

        m_moduleIdentifierTestB = NetconfMessageUtil.buildModuleIdentifier("module-test-b",
                "urn:broadband_forum:yang:obbaa:module-test-b",
                "1986-10-09");
    }

    @After
    public void destroy() throws Exception {
        m_aggregator.removeProcessor(m_notificationProcessor);
        m_aggregator.removeProcessor(m_globalRequestProcessor);
        m_aggregator.removeProcessor(m_deviceConfigProcessorOLT);
        m_aggregator.removeProcessor(DPU, m_moduleIdentifiers, m_deviceConfigProcessorDPU);
        m_aggregator.unregisterDeviceManager();
    }

    @Before
    public void setUp() throws Exception {
        buildModuleIdentifierYangLibrary();
        buildModuleIdentifierSchemaMount();
        buildModuleIdentifierTest();

        m_aggregator = new AggregatorImpl();

        m_deviceConfigProcessorDPU = mock(DeviceConfigProcessor.class);
        m_deviceConfigProcessorOLT = mock(DeviceConfigProcessor.class);
        m_globalRequestProcessor = mock(GlobalRequestProcessor.class);
        m_notificationProcessor = mock(NotificationProcessor.class);
        m_deviceManagementProcessor = mock(DeviceManagementProcessor.class);

        m_moduleIdentifiers = new HashSet<>();
        m_moduleIdentifiers.add(m_moduleIdentifierTestA);
        m_moduleIdentifiers.add(m_moduleIdentifierTestB);

        when(m_deviceConfigProcessorDPU.processRequest(anyString(), anyString())).thenReturn(RESPONSE_OK);
        when(m_deviceConfigProcessorOLT.processRequest(anyString(), anyString())).thenReturn(RESPONSE_OK);
        m_aggregator.addProcessor(DPU, m_moduleIdentifiers, m_deviceConfigProcessorDPU);
        m_aggregator.addProcessor(OLT, m_moduleIdentifiers, m_deviceConfigProcessorOLT);

        m_aggregator.addProcessor(m_notificationProcessor);

        m_aggregator.registerDeviceManager(m_deviceManagementProcessor);
        when(m_deviceManagementProcessor.processRequest(REQUEST_DEVICE_ADD)).thenReturn(RESPONSE_OK);
        when(m_deviceManagementProcessor.getDeviceTypeByDeviceName("deviceA")).thenReturn(DPU);
        when(m_deviceManagementProcessor.getDeviceTypeByDeviceName("deviceB")).thenReturn(DPU);
    }

    @Test
    public void dispatchRequestDeviceAddTest() throws Exception {
        String response = m_aggregator.dispatchRequest(REQUEST_DEVICE_ADD);
        verify(m_deviceManagementProcessor).processRequest(anyString());
        assertFalse(response.isEmpty());
    }

    @Test
    public void dispatchRequestDeviceGetTest() throws Exception {
        when(m_deviceManagementProcessor.processRequest(anyString())).thenReturn(REQUEST_DEVICE_GET);
        String response = m_aggregator.dispatchRequest(REQUEST_DEVICE_GET);
        verify(m_deviceManagementProcessor).processRequest(anyString());
        assertEquals(REQUEST_DEVICE_GET, response);
    }

    @Test
    public void dispatchRequestDeviceGetConfigTest() throws Exception {
        when(m_deviceManagementProcessor.processRequest(anyString())).thenReturn(REQUEST_DEVICE_GET_CONFIG);
        String response = m_aggregator.dispatchRequest(REQUEST_DEVICE_GET_CONFIG);
        verify(m_deviceManagementProcessor).processRequest(anyString());
        assertEquals(REQUEST_DEVICE_GET_CONFIG, response);
    }

    @Test
    public void dispatchRequestDeviceConfigTest() throws Exception {
        //Service config
        when(m_deviceManagementProcessor.getDeviceTypeByDeviceName(anyString())).thenReturn("DPU");
        String response = m_aggregator.dispatchRequest(REQUEST_DEVICE_CONFIG);
        assertFalse(response.isEmpty());
    }

    @Test
    public void dispatchRequestDeviceConfigGetTest() throws Exception {
        //Service get
        String response = m_aggregator.dispatchRequest(REQUEST_DEVICE_SERVICE_GET);
        assertFalse(response.isEmpty());
    }

    @Test
    public void publishNotificationTest() throws Exception {
        m_aggregator.publishNotification(DEVICE_NAME_TEST, NOTIFICATION);
        verify(m_notificationProcessor).publishNotification(anyString());
    }

    @Test
    public void removeNotificationProcessorTest() throws Exception {
        m_aggregator.removeProcessor(m_notificationProcessor);
        m_aggregator.publishNotification(DEVICE_NAME_TEST, NOTIFICATION);
        verifyZeroInteractions(m_notificationProcessor);
    }

    @Test
    public void getModuleCapabilityTest() throws Exception {
        Set<String> capabilitiesSet = m_aggregator.getSystemCapabilities();
        assertFalse(capabilitiesSet.isEmpty());
    }

    @Test
    public void registerSchemaMountManagerTest() throws Exception {
        Set<ModuleIdentifier> moduleIdentifiers = new HashSet<>();
        moduleIdentifiers.add(m_moduleIdentifierSchemaMount);
        m_aggregator.addProcessor(moduleIdentifiers, m_globalRequestProcessor);
        when(m_globalRequestProcessor.processRequest(REQUEST_SCHEMA_MOUNT))
                .thenReturn(RESPONSE_OK);

        String response = m_aggregator.dispatchRequest(REQUEST_SCHEMA_MOUNT);
        assertEquals(RESPONSE_OK, response);
    }

    @Test
    public void registerYangLibraryManagerTest() throws Exception {
        Set<ModuleIdentifier> moduleIdentifiers = new HashSet<>();
        moduleIdentifiers.add(m_moduleIdentifierYangLibrary);
        m_aggregator.addProcessor(moduleIdentifiers, m_globalRequestProcessor);
        when(m_globalRequestProcessor.processRequest(REQUEST_YANG_LIBRAARY))
                .thenReturn(RESPONSE_OK);

        String response = m_aggregator.dispatchRequest(REQUEST_YANG_LIBRAARY);
        assertEquals(RESPONSE_OK, response);
    }

    @Test
    public void getAllCapabilitiesTest() throws Exception {
        Set<ProcessorCapability> processorCapabilities = m_aggregator.getProcessorCapabilities();
        assertFalse(processorCapabilities.isEmpty());
    }

    @Test
    public void getAllModuleIdentifiersTest() throws Exception {
        Set<ModuleIdentifier> moduleIdentifiers = m_aggregator.getModuleIdentifiers();
        assertTrue(moduleIdentifiers.contains(m_moduleIdentifierTestA));
    }

}