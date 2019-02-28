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

package org.broadband_forum.obbaa.device.adapter.impl;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.device.adapter.DeviceInterface;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.CopyConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class NcCompliantAdapterDeviceInterfaceTest {

    public static final String DEVICE_NOT_CONNECTED_TEST_DEVICE = "Device not connected testDevice";
    @Mock
    private NetconfConnectionManager m_ncm;
    private DeviceInterface m_deviceInterface;
    @Mock
    private EditConfigRequest m_editRequest;
    @Mock
    private Device m_device;
    @Mock
    private Future<NetConfResponse> m_response;
    @Mock
    private GetRequest m_getRequest;
    @Mock
    private GetConfigRequest m_getConfigRequest;


    @Before
    public void setup() throws ExecutionException {
        MockitoAnnotations.initMocks(this);
        m_deviceInterface = new NcCompliantAdapterDeviceInterface(m_ncm);
        when(m_device.getDeviceName()).thenReturn("testDevice");
        when(m_ncm.executeNetconf(anyString(), any(CopyConfigRequest.class))).thenReturn(m_response);
        when(m_ncm.isConnected(m_device)).thenReturn(true);
    }

    @Test
    public void testEditConfig() throws ExecutionException {
        m_deviceInterface.align(m_device, m_editRequest);
        verify(m_ncm).executeNetconf("testDevice", m_editRequest);
    }

    @Test
    public void testCopyConfig() throws NetconfMessageBuilderException, ExecutionException {
        NetConfResponse getResponse = new NetConfResponse();
        getResponse.addDataContent(DocumentUtils.stringToDocument("<if:interfaces\n" +
                "                            xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">\n" +
                "    <if:interface>\n" +
                "        <if:name>xdsl-line:1/1/1/new</if:name>\n" +
                "        <if:type xmlns:bbfift=\"urn:broadband-forum-org:yang:bbf-if-type\">bbfift:xdsl</if" +
                ":type>\n" +
                "    </if:interface>\n" +
                "</if:interfaces>").getDocumentElement());
        String expected = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "  <copy-config>\n" +
                "    <target>\n" +
                "      <running/>\n" +
                "    </target>\n" +
                "    <source>\n" +
                "      <config>\n" +
                "        <if:interfaces xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">\n" +
                "    <if:interface>\n" +
                "        <if:name>xdsl-line:1/1/1/new</if:name>\n" +
                "        <if:type xmlns:bbfift=\"urn:broadband-forum-org:yang:bbf-if-type\">bbfift:xdsl</if:type>\n" +
                "    </if:interface>\n" +
                "</if:interfaces>\n" +
                "      </config>\n" +
                "    </source>\n" +
                "  </copy-config>\n" +
                "</rpc>";
        Pair<AbstractNetconfRequest, Future<NetConfResponse>> copyConfigResponse = m_deviceInterface.forceAlign(m_device, getResponse);
        verify(m_ncm).executeNetconf(anyString(), any(CopyConfigRequest.class));
        assertEquals(expected, copyConfigResponse.getFirst().requestToString().trim());
    }

    @Test
    public void testGet() throws ExecutionException {
        m_deviceInterface.get(m_device, m_getRequest);
        verify(m_ncm).executeNetconf(m_device, m_getRequest);
    }

    @Test
    public void testGetConfig() throws ExecutionException {
        m_deviceInterface.getConfig(m_device, m_getConfigRequest);
        verify(m_ncm).executeNetconf(m_device, m_getConfigRequest);
    }

    @Test
    public void testGetWhenDeviceNotConnected() throws ExecutionException {
        when(m_ncm.isConnected(m_device)).thenReturn(false);
        try {
            m_deviceInterface.get(m_device, m_getRequest);
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains(DEVICE_NOT_CONNECTED_TEST_DEVICE));
        }
    }

    @Test
    public void testAlignWhenDeviceNotConnected() throws ExecutionException {
        when(m_ncm.isConnected(m_device)).thenReturn(false);
        try {
            m_deviceInterface.align(m_device, m_editRequest);
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains(DEVICE_NOT_CONNECTED_TEST_DEVICE));
        }
    }

    @Test
    public void testGetConfigWhenDeviceNotConnected() throws ExecutionException {
        try {
            m_deviceInterface.getConfig(m_device, m_getConfigRequest);
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains(DEVICE_NOT_CONNECTED_TEST_DEVICE));
        }
    }

    @Test
    public void testForceALignWhenDeviceNotConnected() throws ExecutionException, NetconfMessageBuilderException {
        NetConfResponse getResponse = new NetConfResponse();
        when(m_ncm.isConnected(m_device)).thenReturn(false);
        getResponse.addDataContent(DocumentUtils.stringToDocument("<if:interfaces\n" +
                "                            xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">\n" +
                "    <if:interface>\n" +
                "        <if:name>xdsl-line:1/1/1/new</if:name>\n" +
                "        <if:type xmlns:bbfift=\"urn:broadband-forum-org:yang:bbf-if-type\">bbfift:xdsl</if" +
                ":type>\n" +
                "    </if:interface>\n" +
                "</if:interfaces>").getDocumentElement());
        String expected = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "  <copy-config>\n" +
                "    <target>\n" +
                "      <running/>\n" +
                "    </target>\n" +
                "    <source>\n" +
                "      <config>\n" +
                "        <if:interfaces xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">\n" +
                "    <if:interface>\n" +
                "        <if:name>xdsl-line:1/1/1/new</if:name>\n" +
                "        <if:type xmlns:bbfift=\"urn:broadband-forum-org:yang:bbf-if-type\">bbfift:xdsl</if:type>\n" +
                "    </if:interface>\n" +
                "</if:interfaces>\n" +
                "      </config>\n" +
                "    </source>\n" +
                "  </copy-config>\n" +
                "</rpc>";
        try {
            Pair<AbstractNetconfRequest, Future<NetConfResponse>> copyConfigResponse = m_deviceInterface.forceAlign(m_device, getResponse);
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains(DEVICE_NOT_CONNECTED_TEST_DEVICE));
        }
    }

    @Test
    public void testGetConnectionStateOfDevice() {
        m_deviceInterface.getConnectionState(m_device);
        verify(m_ncm).getConnectionState(m_device);
    }

}
