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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.broadband_forum.obbaa.aggregator.api.DeviceConfigProcessor;
import org.broadband_forum.obbaa.aggregator.api.DeviceManagementProcessor;
import org.broadband_forum.obbaa.aggregator.api.GlobalRequestProcessor;
import org.broadband_forum.obbaa.aggregator.api.NotificationProcessor;
import org.broadband_forum.obbaa.aggregator.api.ProcessorCapability;
import org.broadband_forum.obbaa.aggregator.processor.NetconfMessageUtil;
import org.broadband_forum.obbaa.aggregator.processor.PmaAdapter;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfNotification;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ModuleIdentifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class AggregatorImplTest {
    private AggregatorImpl m_aggregator;

    private NotificationProcessor m_notificationProcessor;

    @Mock
    private PmaAdapter m_pmaAdapterProcessor;

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

    private static String REQUEST_DEVICE_CONFIG = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1527307907169\">\n" +
            "    <edit-config>\n" +
            "        <target>\n" +
            "            <running/>\n" +
            "        </target>\n" +
            "        <config>\n" +
            "            <network-manager xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
            "                <managed-devices>\n" +
            "                    <device>\n" +
            "                        <name>deviceA</name>\n" +
            "                        <root>\n" +
            "                            <if:interfaces xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">\n" +
            "                                <if:interface xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xc:operation=\"create\">\n" +
            "                                    <if:name>interfaceB</if:name>\n" +
            "                                    <if:type xmlns:ianaift=\"urn:ietf:params:xml:ns:yang:iana-if-type\">ianaift:ethernetCsmacd</if:type>\n" +
            "                                </if:interface>\n" +
            "                            </if:interfaces>\n" +
            "                        </root>\n" +
            "                    </device>\n" +
            "                    <device>\n" +
            "                        <name>deviceB</name>\n" +
            "                    </device>\n" +
            "                </managed-devices>\n" +
            "            </network-manager>\n" +
            "        </config>\n" +
            "    </edit-config>\n" +
            "</rpc>";

    private static String REQUEST_DEVICE_ADD = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1527307907656\">\n" +
            "    <edit-config>\n" +
            "        <target>\n" +
            "            <running />\n" +
            "        </target>\n" +
            "        <config>\n" +
            "            <network-manager xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
            "                <managed-devices>\n" +
            "                    <device xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xc:operation=\"create\">\n" +
            "                        <name>deviceA</name>\n" +
            "                        <device-management>\n" +
            "                            <type>DPU</type>\n" +
            "                            <interface-version>1.0.0</interface-version>\n" +
            "                            <vendor>Nokia</vendor>\n" +
            "                            <device-connection>\n" +
            "                                <connection-model>direct</connection-model>\n" +
            "                                <password-auth>\n" +
            "                                    <authentication>\n" +
            "                                        <address>192.168.169.1</address>\n" +
            "                                        <management-port>92994</management-port>\n" +
            "                                        <user-name>DPU</user-name>\n" +
            "                                        <password>DPU</password>\n" +
            "                                    </authentication>\n" +
            "                                </password-auth>\n" +
            "                            </device-connection>\n" +
            "                        </device-management>\n" +
            "                    </device>\n" +
            "                </managed-devices>\n" +
            "            </network-manager>\n" +
            "        </config>\n" +
            "    </edit-config>\n" +
            "</rpc>";

    private static String REQUEST_SCHEMA_MOUNT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1520261367256\">\n" +
            "  <get>\n" +
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
            "    <filter type=\"subtree\">\n" +
            "      <yang-library xmlns=\"urn:ietf:params:xml:ns:yang:ietf-yang-library\">\n" +
            "     </yang-library>\n" +
            "    </filter>\n" +
            "  </get>\n" +
            "</rpc>\n";

    private static String REQUEST_DEVICE_SERVICE_GET = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1527307907656\">\n" +
            "    <get-config xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "        <source>\n" +
            "            <running/>\n" +
            "        </source>\n" +
            "        <filter type=\"subtree\">\n" +
            "            <network-manager xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
            "                <managed-devices>\n" +
            "                    <device>\n" +
            "                        <name>deviceA</name>\n" +
            "                        <root>\n" +
            "                            <if:interfaces xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\"/>\n" +
            "                        </root>\n" +
            "                    </device>\n" +
            "                </managed-devices>\n" +
            "            </network-manager>\n" +
            "        </filter>\n" +
            "    </get-config>\n" +
            "</rpc>";

    private static String REQUEST_DEVICE_GET = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1527307907656\">\n" +
            "    <get xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "        <filter type=\"subtree\">\n" +
            "            <network-manager xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
            "                <managed-devices>\n" +
            "                    <device/>\n" +
            "                </managed-devices>\n" +
            "            </network-manager>\n" +
            "        </filter>\n" +
            "    </get>\n" +
            "</rpc>";

    private static String REQUEST_DEVICE_GET_CONFIG = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1527307907656\">\n" +
            "    <get-config xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "        <source>\n" +
            "            <running/>\n" +
            "        </source>\n" +
            "        <filter type=\"subtree\">\n" +
            "            <network-manager xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
            "                <managed-devices>\n" +
            "                    <device/>\n" +
            "                </managed-devices>\n" +
            "            </network-manager>\n" +
            "        </filter>\n" +
            "    </get-config>\n" +
            "</rpc>";

    public static String NOTIFICATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<notification xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">\n" +
            "<eventTime>2019-01-24T09:46:40+00:00</eventTime>\n" +
            "<netconf-config-change xmlns=\"urn:ietf:params:xml:ns:yang:ietf-netconf-notifications\">\n" +
            "<datastore>running</datastore>\n" +
            "<changed-by>\n" +
            "<username>PMA_USER</username>\n" +
            "<session-id>1</session-id>\n" +
            "<source-host/>\n" +
            "</changed-by>\n" +
            "<edit>\n" +
            "<target xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">/if:interfaces/if:interface[if:name='interfaceB']</target>\n" +
            "<operation>delete</operation>\n" +
            "</edit>\n" +
            "</netconf-config-change>\n" +
            "</notification>";

    private static String DEVICE_NAME_TEST = "deviceName-ABC";
    private NetconfClientInfo m_clientInfo = new NetconfClientInfo("UT", 1);

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

        m_pmaAdapterProcessor = mock(PmaAdapter.class);
        m_deviceConfigProcessorDPU = mock(DeviceConfigProcessor.class);
        m_deviceConfigProcessorOLT = mock(DeviceConfigProcessor.class);
        m_globalRequestProcessor = mock(GlobalRequestProcessor.class);
        m_notificationProcessor = mock(NotificationProcessor.class);
        m_deviceManagementProcessor = mock(DeviceManagementProcessor.class);

        m_moduleIdentifiers = new HashSet<>();
        m_moduleIdentifiers.add(m_moduleIdentifierTestA);
        m_moduleIdentifiers.add(m_moduleIdentifierTestB);

        when(m_pmaAdapterProcessor.processRequest(anyString(), anyString())).thenReturn(RESPONSE_OK);
        when(m_deviceConfigProcessorDPU.processRequest(anyString(), anyString())).thenReturn(RESPONSE_OK);
        when(m_deviceConfigProcessorOLT.processRequest(anyString(), anyString())).thenReturn(RESPONSE_OK);
        m_aggregator.addProcessor(DPU, m_moduleIdentifiers, m_deviceConfigProcessorDPU);
        m_aggregator.addProcessor(OLT, m_moduleIdentifiers, m_deviceConfigProcessorOLT);
        m_aggregator.addProcessor(DPU, m_moduleIdentifiers, m_pmaAdapterProcessor);
        m_aggregator.addProcessor(m_notificationProcessor);

        m_aggregator.registerDeviceManager(m_deviceManagementProcessor);
        when(m_deviceManagementProcessor.processRequest(m_clientInfo, REQUEST_DEVICE_ADD)).thenReturn(RESPONSE_OK);
        when(m_deviceManagementProcessor.getDeviceTypeByDeviceName("deviceA")).thenReturn(DPU);
        when(m_deviceManagementProcessor.getDeviceTypeByDeviceName("deviceB")).thenReturn(DPU);
    }

    @Test
    public void dispatchRequestDeviceAddTest() throws Exception {
        String response = m_aggregator.dispatchRequest(m_clientInfo, REQUEST_DEVICE_ADD);
        verify(m_deviceManagementProcessor).processRequest(eq(m_clientInfo), anyString());
        assertFalse(response.isEmpty());
    }

    @Test
    public void dispatchRequestDeviceGetTest() throws Exception {
        when(m_deviceManagementProcessor.processRequest(eq(m_clientInfo), anyString())).thenReturn(REQUEST_DEVICE_GET);
        String response = m_aggregator.dispatchRequest(m_clientInfo, REQUEST_DEVICE_GET);
        verify(m_deviceManagementProcessor).processRequest(eq(m_clientInfo), anyString());
        assertEquals(REQUEST_DEVICE_GET, response);
    }

    @Test
    public void dispatchRequestDeviceGetConfigTest() throws Exception {
        when(m_deviceManagementProcessor.processRequest(eq(m_clientInfo), anyString())).thenReturn(REQUEST_DEVICE_GET_CONFIG);
        String response = m_aggregator.dispatchRequest(m_clientInfo, REQUEST_DEVICE_GET_CONFIG);
        verify(m_deviceManagementProcessor).processRequest(eq(m_clientInfo), anyString());
        assertEquals(REQUEST_DEVICE_GET_CONFIG, response);
    }

    @Test
    public void dispatchRequestDeviceConfigTest() throws Exception {
        //Service config
        when(m_deviceManagementProcessor.getDeviceTypeByDeviceName(anyString())).thenReturn("DPU");
        String response = m_aggregator.dispatchRequest(m_clientInfo, REQUEST_DEVICE_CONFIG);
        assertFalse(response.isEmpty());
    }

    @Test
    public void dispatchRequestDeviceConfigGetTest() throws Exception {
        //Service get
        String response = m_aggregator.dispatchRequest(m_clientInfo, REQUEST_DEVICE_SERVICE_GET);
        assertFalse(response.isEmpty());
    }

    @Test
    public void publishNotificationTest() throws Exception {
        Notification notification = new NetconfNotification(DocumentUtils.stringToDocument(NOTIFICATION));
        m_aggregator.publishNotification(DEVICE_NAME_TEST, notification);
        verify(m_notificationProcessor).publishNotification(any(Notification.class));
    }

    @Test
    public void removeNotificationProcessorTest() throws Exception {
        m_aggregator.removeProcessor(m_notificationProcessor);
        Notification notification = new NetconfNotification(DocumentUtils.stringToDocument(NOTIFICATION));
        m_aggregator.publishNotification(DEVICE_NAME_TEST, notification);
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
        when(m_globalRequestProcessor.processRequest(m_clientInfo, REQUEST_SCHEMA_MOUNT))
                .thenReturn(RESPONSE_OK);

        String response = m_aggregator.dispatchRequest(m_clientInfo, REQUEST_SCHEMA_MOUNT);
        assertEquals(RESPONSE_OK, response);
    }

    @Test
    public void registerYangLibraryManagerTest() throws Exception {
        Set<ModuleIdentifier> moduleIdentifiers = new HashSet<>();
        moduleIdentifiers.add(m_moduleIdentifierYangLibrary);
        m_aggregator.addProcessor(moduleIdentifiers, m_globalRequestProcessor);
        when(m_globalRequestProcessor.processRequest(m_clientInfo, REQUEST_YANG_LIBRAARY))
                .thenReturn(RESPONSE_OK);

        String response = m_aggregator.dispatchRequest(m_clientInfo, REQUEST_YANG_LIBRAARY);
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