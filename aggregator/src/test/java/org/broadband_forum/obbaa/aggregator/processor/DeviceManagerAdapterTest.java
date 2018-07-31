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

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer.getNetconfResponse;
import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.stringToDocument;
import static org.broadband_forum.obbaa.pma.impl.NetconfConnectionStateProvider.CONNECTION_STATE;
import static org.broadband_forum.obbaa.pma.impl.NetconfDeviceAlignmentServiceImpl.ALIGNMENT_STATE;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.broadband_forum.obbaa.aggregator.api.Aggregator;
import org.broadband_forum.obbaa.aggregator.impl.AggregatorImpl;
import org.broadband_forum.obbaa.connectors.sbi.netconf.ConnectionState;
import org.broadband_forum.obbaa.connectors.sbi.netconf.NewDeviceInfo;
import org.broadband_forum.obbaa.dm.DeviceManager;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.store.alignment.DeviceAlignmentInfo;
import org.broadband_forum.obbaa.store.dm.CallHomeInfo;
import org.broadband_forum.obbaa.store.dm.DeviceInfo;
import org.broadband_forum.obbaa.store.dm.SshConnectionInfo;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DeviceManagerAdapterTest {
    private static final String DIRECT_DEVICE = "directDevice";
    private static Aggregator m_aggregator;
    private static DeviceManagerAdapter m_deviceManagerAdapter;
    private static String DEVICE_NAME_A = "deviceA";
    private static String DEVICE_TYPE = "DPU";
    private static final String EXPECTED_DM_GET_REPLY_DIRECT = "<rpc-reply message-id=\"101\" " +
            "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "    <data>\n" +
            "        <managed-devices xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
            "            <device>\n" +
            "                <device-management>\n" +
            "                    <device-connection>\n" +
            "                        <connection-model>direct</connection-model>\n" +
            "                        <password-auth>\n" +
            "                            <address>192.168.100.101</address>\n" +
            "                            <management-port>803</management-port>\n" +
            "                            <password>testPassword</password>\n" +
            "                            <user-name>testUser</user-name>\n" +
            "                        </password-auth>\n" +
            "                    </device-connection>\n" +
            "                    <device-software-version>1.0.0</device-software-version>\n" +
            "                    <device-type>DPU</device-type>\n" +
            "                    <device-vendor>huawei</device-vendor>\n" +
            "                </device-management>\n" +
            "                <device-name>directDevice</device-name>\n" +
            "                <device-state>\n" +
            "                    <configuration-alignment-state>Never Aligned</configuration-alignment-state>\n" +
            "                    <connection-state>\n" +
            "                        <connected>false</connected>\n" +
            "                        <connection-creation-time>1970-01-01T00:00:00</connection-creation-time>\n" +
            "                        <device-capability>cap1</device-capability>\n" +
            "                        <device-capability>cap2</device-capability>\n" +
            "                    </connection-state>\n" +
            "                </device-state>\n" +
            "            </device>\n" +
            "        </managed-devices>\n" +
            "        <new-devices xmlns=\"urn:bbf:yang:obbaa:network-manager\"/>\n" +
            "    </data>\n" +
            "</rpc-reply>\n";

    private static String REQUEST_DEVICE_ADD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1527307907664\">\n" +
            "  <edit-config>\n" +
            "    <target>\n" +
            "      <running />\n" +
            "    </target>\n" +
            "    <config>\n" +
            "      <managed-devices xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
            "        <device xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xc:operation=\"create\">\n" +
            "          <device-name>" + DIRECT_DEVICE + "</device-name>\n" +
            "          <device-management>\n" +
            "            <device-type>DPU</device-type>\n" +
            "            <device-software-version>1.0.0</device-software-version>\n" +
            "            <device-vendor>huawei</device-vendor>\n" +
            "            <device-connection>\n" +
            "              <connection-model>direct</connection-model>\n" +
            "              <password-auth>\n" +
            "                <authentication>\n" +
            "                  <address>192.168.100.101</address>\n" +
            "                  <management-port>803</management-port>\n" +
            "                  <user-name>testUser</user-name>\n" +
            "                  <password>testPassword</password>\n" +
            "                </authentication>\n" +
            "              </password-auth>\n" +
            "            </device-connection>\n" +
            "          </device-management>\n" +
            "        </device>\n" +
            "      </managed-devices>\n" +
            "    </config>\n" +
            "  </edit-config>\n" +
            "</rpc>";
    private static final String CALLHOME_DEVICE = "callhomeDevice";
    private static String REQUEST_DEVICE_ADD_CALLHOME = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1527307907664\">\n" +
            "  <edit-config>\n" +
            "    <target>\n" +
            "      <running />\n" +
            "    </target>\n" +
            "    <config>\n" +
            "      <managed-devices xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
            "        <device xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xc:operation=\"create\">\n" +
            "          <device-name>" + CALLHOME_DEVICE + "</device-name>\n" +
            "          <device-management>\n" +
            "            <device-type>DPU</device-type>\n" +
            "            <device-software-version>1.0.0</device-software-version>\n" +
            "            <device-vendor>huawei</device-vendor>\n" +
            "            <device-connection>\n" +
            "              <connection-model>call-home</connection-model>\n" +
            "              <duid>" + CALLHOME_DEVICE + "-duid" + "</duid>\n" +
            "            </device-connection>\n" +
            "          </device-management>\n" +
            "        </device>\n" +
            "      </managed-devices>\n" +
            "    </config>\n" +
            "  </edit-config>\n" +
            "</rpc>";

    private static String REQUEST_DEVICE_DELETE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1527307907664\">\n" +
            "  <delete-config>\n" +
            "    <target>\n" +
            "      <running />\n" +
            "    </target>\n" +
            "    <config>\n" +
            "      <managed-devices xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
            "        <device>\n" +
            "          <device-name>" + DIRECT_DEVICE + "</device-name>\n" +
            "        </device>\n" +
            "      </managed-devices>\n" +
            "    </config>\n" +
            "  </delete-config>\n" +
            "</rpc>";
    private static String REQUEST_DEVICE_DELETE_CONFIG_CALLHOME = REQUEST_DEVICE_DELETE.replaceAll(DIRECT_DEVICE,
            CALLHOME_DEVICE);
    private static String REQUEST_DEVICE_DELETE_CALLHOME = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1527307907664\">\n" +
            "  <edit-config>\n" +
            "    <target>\n" +
            "      <running />\n" +
            "    </target>\n" +
            "    <config>\n" +
            "      <managed-devices xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
            "        <device xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xc:operation=\"delete\">\n" +
            "          <device-name>" + CALLHOME_DEVICE + "</device-name>\n" +
            "        </device>\n" +
            "      </managed-devices>\n" +
            "    </config>\n" +
            "  </edit-config>\n" +
            "</rpc>";

    private static String REQUEST_DEVICE_DELETE_DIRECT = REQUEST_DEVICE_DELETE_CALLHOME.replaceAll(CALLHOME_DEVICE, DIRECT_DEVICE);
    private static String REQUEST_DEVICE_GET =
            "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                    "<get xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                    "  <source>\n" +
                    "    <running/>\n" +
                    "  </source>\n" +
                    "<managed-devices xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
                    "  <device>\n" +
                    "    <device-name>" + DIRECT_DEVICE + "</device-name>\n" +
                    "  </device>\n" +
                    "</managed-devices>\n" +
                    "</get>\n" +
                    "</rpc>";

    private static String REQUEST_DEVICE_GET_CONFIG =
            "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                    "<get-config xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                    "  <source>\n" +
                    "    <running/>\n" +
                    "  </source>\n" +
                    "<managed-devices xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
                    "  <device>\n" +
                    "    <device-name>" + DIRECT_DEVICE + "</device-name>\n" +
                    "  </device>\n" +
                    "</managed-devices>\n" +
                    "</get-config>\n" +
                    "</rpc>";
    private static String REQUEST_DEVICE_GET_CONFIG_CALLHOME = REQUEST_DEVICE_GET_CONFIG.replaceAll(DIRECT_DEVICE,
            CALLHOME_DEVICE);
    private static final String EXPECTED_DM_GET_CONFIG_REPLY_DIRECT = " <rpc-reply message-id=\"101\" " +
            "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "  <data>\n" +
            "    <managed-devices xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
            "      <device>\n" +
            "        <device-management>\n" +
            "          <device-connection>\n" +
            "            <connection-model>direct</connection-model>\n" +
            "              <password-auth>\n" +
            "                <address>192.168.100.101</address>\n" +
            "                <management-port>803</management-port>\n" +
            "                <password>testPassword</password>\n" +
            "                <user-name>testUser</user-name>\n" +
            "              </password-auth>\n" +
            "          </device-connection>\n" +
            "          <device-software-version>1.0.0</device-software-version>\n" +
            "          <device-type>DPU</device-type>\n" +
            "          <device-vendor>huawei</device-vendor>\n" +
            "        </device-management>\n" +
            "        <device-name>directDevice</device-name>\n" +
            "      </device>\n" +
            "    </managed-devices>\n" +
            "  </data>\n" +
            "</rpc-reply>";
    private static final String EXPECTED_DM_GET_REPLY_CALLHOME = " <rpc-reply message-id=\"101\" " +
            "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "    <data>\n" +
            "        <managed-devices xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
            "            <device>\n" +
            "                <device-management>\n" +
            "                    <device-connection>\n" +
            "                        <connection-model>call-home</connection-model>\n" +
            "                        <duid>callhomeDevice-duid</duid>\n" +
            "                    </device-connection>\n" +
            "                    <device-software-version>1.0.0</device-software-version>\n" +
            "                    <device-type>DPU</device-type>\n" +
            "                    <device-vendor>huawei</device-vendor>\n" +
            "                </device-management>\n" +
            "                <device-name>callhomeDevice</device-name>\n" +
            "                <device-state>\n" +
            "                    <configuration-alignment-state>Never Aligned</configuration-alignment-state>\n" +
            "                    <connection-state>\n" +
            "                        <connected>false</connected>\n" +
            "                        <connection-creation-time>1970-01-01T00:00:00</connection-creation-time>\n" +
            "                        <device-capability>cap1</device-capability>\n" +
            "                        <device-capability>cap2</device-capability>\n" +
            "                    </connection-state>\n" +
            "                </device-state>\n" +
            "            </device>\n" +
            "        </managed-devices>\n" +
            "        <new-devices xmlns=\"urn:bbf:yang:obbaa:network-manager\"/>\n" +
            "    </data>\n" +
            "</rpc-reply>\n";

    private static final String EXPECTED_DM_GET_REPLY_WITH_NEWDEVICES = "<rpc-reply message-id=\"101\" " +
            "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "    <data>\n" +
            "        <new-devices xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
            "            <new-device>\n" +
            "                <device-capability>caps1</device-capability>\n" +
            "                <device-capability>caps2</device-capability>\n" +
            "                <duid>duid-1234</duid>\n" +
            "            </new-device>\n" +
            "        </new-devices>\n" +
            "    </data>\n" +
            "</rpc-reply>";

    private static final String EXPECTED_DM_GET_CONFIG_REPLY_CALLHOME = " <rpc-reply message-id=\"101\" " +
            "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "  <data>\n" +
            "    <managed-devices xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
            "      <device>\n" +
            "        <device-name>" + CALLHOME_DEVICE + "</device-name>\n" +
            "        <device-management>\n" +
            "          <device-connection>\n" +
            "            <connection-model>call-home</connection-model>\n" +
            "            <duid>" + CALLHOME_DEVICE + "-duid" + "</duid>\n" +
            "          </device-connection>\n" +
            "          <device-software-version>1.0.0</device-software-version>\n" +
            "          <device-type>DPU</device-type>\n" +
            "          <device-vendor>huawei</device-vendor>\n" +
            "        </device-management>\n" +
            "      </device>\n" +
            "    </managed-devices>\n" +
            "  </data>\n" +
            "</rpc-reply>";
    private static String REQUEST_DEVICE_GET_CALLHOME = REQUEST_DEVICE_GET.replaceAll(DIRECT_DEVICE, CALLHOME_DEVICE);
    @Mock
    private DeviceManager m_deviceManager;
    @Mock
    private NetconfClientSession m_newDeviceSession;
    private Set<String> m_newDeviceCaps;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        m_aggregator = new AggregatorImpl();
        m_deviceManagerAdapter = new DeviceManagerAdapter(m_aggregator, m_deviceManager);

        DeviceInfo deviceInfo = getDirectDeviceInfo();
        when(m_deviceManager.getDevice(DIRECT_DEVICE)).thenReturn(deviceInfo);
        DeviceInfo callhomeInfo = getCallHomeDeviceInfo();
        when(m_deviceManager.getDevice(CALLHOME_DEVICE)).thenReturn(callhomeInfo);
        doNothing().when(m_deviceManager).updateDevice(any(DeviceInfo.class));
        m_newDeviceCaps = new LinkedHashSet<>();
        m_newDeviceCaps.add("caps1");
        m_newDeviceCaps.add("caps2");
        when(m_newDeviceSession.getServerCapabilities()).thenReturn(m_newDeviceCaps);
    }

    @NotNull
    private static DeviceInfo getDirectDeviceInfo() {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setKey(DIRECT_DEVICE);

        SshConnectionInfo sshConnectionInfo = new SshConnectionInfo();
        sshConnectionInfo.setIp("192.168.100.101");
        sshConnectionInfo.setPassword("testPassword");
        sshConnectionInfo.setPort(803);
        sshConnectionInfo.setUsername("testUser");
        deviceInfo.setDeviceConnectionInfo(sshConnectionInfo);
        DeviceAlignmentInfo alignmentInfo = new DeviceAlignmentInfo();
        deviceInfo.getDeviceState().put(ALIGNMENT_STATE, alignmentInfo);
        ConnectionState connectionState = new ConnectionState();
        Set<String> caps = new HashSet<>();
        caps.add("cap1");
        caps.add("cap2");
        connectionState.setCapabilities(caps);
        deviceInfo.getDeviceState().put(CONNECTION_STATE, connectionState);
        return deviceInfo;
    }

    private static DeviceInfo getCallHomeDeviceInfo() {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setKey(CALLHOME_DEVICE);

        CallHomeInfo callHomeInfo = new CallHomeInfo();
        callHomeInfo.setDuid(CALLHOME_DEVICE + "-duid");
        deviceInfo.setDeviceCallHomeInfo(callHomeInfo);
        DeviceAlignmentInfo alignmentInfo = new DeviceAlignmentInfo();
        deviceInfo.getDeviceState().put(ALIGNMENT_STATE, alignmentInfo);
        ConnectionState connectionState = new ConnectionState();
        Set<String> caps = new HashSet<>();
        caps.add("cap1");
        caps.add("cap2");
        connectionState.setCapabilities(caps);
        deviceInfo.getDeviceState().put(CONNECTION_STATE, connectionState);
        return deviceInfo;
    }

    @Test
    public void processRequestDmTest() throws Exception {
        String response = m_deviceManagerAdapter.processRequest(REQUEST_DEVICE_ADD);
        NetConfResponse netconfResponse = getNetconfResponse(stringToDocument(response));
        assertTrue(netconfResponse.isOk());
        ArgumentCaptor<DeviceInfo> deviceInfoCapture = ArgumentCaptor.forClass(DeviceInfo.class);
        verify(m_deviceManager).createDevice(deviceInfoCapture.capture());
        assertEquals(getDirectDeviceInfo(), deviceInfoCapture.getValue());

        response = m_deviceManagerAdapter.processRequest(REQUEST_DEVICE_GET);
        netconfResponse = getNetconfResponse(stringToDocument(response));
        assertFalse(response.isEmpty());
        TestUtil.assertXMLEquals(stringToDocument(EXPECTED_DM_GET_REPLY_DIRECT).getDocumentElement(),
                netconfResponse.getResponseDocument().getDocumentElement());

        response = m_deviceManagerAdapter.processRequest(REQUEST_DEVICE_GET_CONFIG);
        assertFalse(response.isEmpty());
        netconfResponse = getNetconfResponse(stringToDocument(response));
        TestUtil.assertXMLEquals(stringToDocument(EXPECTED_DM_GET_CONFIG_REPLY_DIRECT).getDocumentElement(),
                netconfResponse.getResponseDocument().getDocumentElement());

        response = m_deviceManagerAdapter.processRequest(REQUEST_DEVICE_DELETE);
        assertFalse(response.isEmpty());

        response = m_deviceManagerAdapter.processRequest(REQUEST_DEVICE_GET);
        assertFalse(response.isEmpty());
    }

    @Test
    public void processRequestDmTest_WithCallHomeDevices() throws Exception {

        String response = m_deviceManagerAdapter.processRequest(REQUEST_DEVICE_ADD_CALLHOME);
        NetConfResponse netconfResponse = getNetconfResponse(stringToDocument(response));
        Assert.assertTrue(netconfResponse.isOk());
        ArgumentCaptor<DeviceInfo> deviceInfoCapture = ArgumentCaptor.forClass(DeviceInfo.class);
        verify(m_deviceManager).createDevice(deviceInfoCapture.capture());
        Assert.assertEquals(getCallHomeDeviceInfo(), deviceInfoCapture.getValue());

        response = m_deviceManagerAdapter.processRequest(REQUEST_DEVICE_GET_CALLHOME);
        netconfResponse = getNetconfResponse(stringToDocument(response));
        assertFalse(response.isEmpty());
        TestUtil.assertXMLEquals(stringToDocument(EXPECTED_DM_GET_REPLY_CALLHOME).getDocumentElement(),
                netconfResponse.getResponseDocument().getDocumentElement());

        response = m_deviceManagerAdapter.processRequest(REQUEST_DEVICE_GET_CONFIG_CALLHOME);
        assertFalse(response.isEmpty());
        netconfResponse = getNetconfResponse(stringToDocument(response));
        TestUtil.assertXMLEquals(stringToDocument(EXPECTED_DM_GET_CONFIG_REPLY_CALLHOME).getDocumentElement(),
                netconfResponse.getResponseDocument().getDocumentElement());

        response = m_deviceManagerAdapter.processRequest(REQUEST_DEVICE_DELETE_CONFIG_CALLHOME);
        assertFalse(response.isEmpty());

        response = m_deviceManagerAdapter.processRequest(REQUEST_DEVICE_DELETE_CALLHOME);
        assertFalse(response.isEmpty());

        response = m_deviceManagerAdapter.processRequest(REQUEST_DEVICE_DELETE_DIRECT);
        assertFalse(response.isEmpty());

        response = m_deviceManagerAdapter.processRequest(REQUEST_DEVICE_GET_CALLHOME);
        assertFalse(response.isEmpty());
    }

    @Test
    public void processRequestDmGetNewDevices() throws Exception {
        when(m_deviceManager.getNewDevices()).thenReturn(newDevices());
        String response = m_deviceManagerAdapter.processRequest(REQUEST_DEVICE_GET);
        NetConfResponse netconfResponse = getNetconfResponse(stringToDocument(response));
        TestUtil.assertXMLEquals(stringToDocument(EXPECTED_DM_GET_REPLY_WITH_NEWDEVICES).getDocumentElement(),
                netconfResponse.getResponseDocument().getDocumentElement());


    }

    private List<NewDeviceInfo> newDevices() {
        List<NewDeviceInfo> newDevices = new ArrayList<>();
        NewDeviceInfo newDevice = new NewDeviceInfo("duid-1234", m_newDeviceSession);
        newDevices.add(newDevice);
        return newDevices;
    }

    @Test
    public void getDeviceTypeByDeviceName() throws Exception {
        DeviceAdapterInfo deviceAdapterInfo = new DeviceAdapterInfo(DEVICE_TYPE, null, null, null);
        m_deviceManagerAdapter.updateDeviceAdptInfo(DEVICE_NAME_A, deviceAdapterInfo);
        String response = m_deviceManagerAdapter.getDeviceTypeByDeviceName(DEVICE_NAME_A);
        assertEquals(DEVICE_TYPE, response);

        m_deviceManagerAdapter.removeDeviceAdptInfo(DEVICE_NAME_A);
    }


    @Test
    public void getDeviceFilterMsgTest() throws Exception {
        String request =
                "<rpc message-id=\"5\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "<get-config>\n" +
                        "<source>\n" +
                        "<running/>\n" +
                        "</source>\n" +
                        "<filter>\n" +
                        "<managed-devices xmlns=\"urn:bbf:yang:obbaa:network-manager\"/>\n" +
                        "</filter>\n" +
                        "</get-config>\n" +
                        "</rpc>";

        String response = m_deviceManagerAdapter.processRequest(REQUEST_DEVICE_GET);
        assertFalse(response.isEmpty());
    }

}