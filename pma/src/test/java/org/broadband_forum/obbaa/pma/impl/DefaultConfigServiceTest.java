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

package org.broadband_forum.obbaa.pma.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapter;
import org.broadband_forum.obbaa.dm.DeviceManager;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.DeviceMgmt;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.pma.PmaRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DefaultConfigServiceTest {
    @Mock
    private DeviceManager m_deviceManager;
    private DefaultConfigService m_defaultConfigService;
    @Mock
    private PmaRegistry m_pmaRegistry;
    @Mock
    private AdapterManager m_adapterManager;
    private Device m_device;
    @Mock
    private DeviceAdapter m_deviceAdapter;
    private byte[] m_defaultXmlBytes;
    private DeviceMgmt m_deviceMgmt;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        m_defaultConfigService = new DefaultConfigService(m_deviceManager, m_pmaRegistry, m_adapterManager);
        m_deviceMgmt = new DeviceMgmt();
        m_deviceMgmt.setDeviceType("DPU");
        m_deviceMgmt.setDeviceInterfaceVersion("1.0");
        m_deviceMgmt.setDeviceModel("example");
        m_deviceMgmt.setDeviceVendor("UT");
        m_device = new Device();
        m_device.setDeviceName("UT-device");
        m_device.setDeviceManagement(m_deviceMgmt);
        when(m_deviceManager.getDevice(m_device.getDeviceName())).thenReturn(m_device);
        when(m_adapterManager.getDeviceAdapter(any())).thenReturn(m_deviceAdapter);
    }

    @Test
    public void testInitAddsProviderProvider() {
        verify(m_deviceManager, never()).addDeviceStateProvider(m_defaultConfigService);
        verify(m_deviceManager, never()).removeDeviceStateProvider(m_defaultConfigService);

        m_defaultConfigService.init();
        verify(m_deviceManager).addDeviceStateProvider(m_defaultConfigService);
        verify(m_deviceManager, never()).removeDeviceStateProvider(m_defaultConfigService);
    }

    @Test
    public void testDestroyRemovesProvider() {
        verify(m_deviceManager, never()).addDeviceStateProvider(m_defaultConfigService);
        verify(m_deviceManager, never()).removeDeviceStateProvider(m_defaultConfigService);

        m_defaultConfigService.destroy();
        verify(m_deviceManager, never()).addDeviceStateProvider(m_defaultConfigService);
        verify(m_deviceManager).removeDeviceStateProvider(m_defaultConfigService);
    }

    @Test
    public void testDeviceAddedWithCorrectDefaultXml() throws ExecutionException, IOException, NetconfMessageBuilderException {
        String editConfigRequestString = "<rpc message-id=\"e1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" + System.lineSeparator() +
                "  <edit-config>" + System.lineSeparator() +
                "    <target>" + System.lineSeparator() +
                "      <running/>" + System.lineSeparator() +
                "    </target>" + System.lineSeparator() +
                "    <default-operation>merge</default-operation>" + System.lineSeparator() +
                "    <test-option>set</test-option>" + System.lineSeparator() +
                "    <error-option>stop-on-error</error-option>" + System.lineSeparator() +
                "    <config>" + System.lineSeparator() +
                "      <if:interfaces xmlns=\"urn:bbf:yang:obbaa:network-manager\" xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">" + System.lineSeparator() +
                "                <if:interface xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xc:operation=\"create\">" + System.lineSeparator() +
                "                    <if:name>Interface</if:name>" + System.lineSeparator() +
                "                    <if:type xmlns:ianaift=\"urn:ietf:params:xml:ns:yang:iana-if-type\">ianaift:fastdsl</if:type>" + System.lineSeparator() +
                "                    <if:description>InterfaceDescription</if:description>" + System.lineSeparator() +
                "                </if:interface>" + System.lineSeparator() +
                "            </if:interfaces>" + System.lineSeparator() +
                "    </config>" + System.lineSeparator() +
                "  </edit-config>" + System.lineSeparator() +
                "</rpc>" + System.lineSeparator();
        EditConfigRequest request = DocumentToPojoTransformer.getEditConfig(DocumentUtils.stringToDocument(editConfigRequestString));
        when(m_adapterManager.getEditRequestForAdapter(any())).thenReturn(request);

        //when the mechanism is push, there should be interaction with pmaregistry with the edit-config of default config retuned by the adapter-manager
        m_defaultConfigService.deviceAdded(m_device.getDeviceName());
        verify(m_pmaRegistry).executeNC(m_device.getDeviceName(), editConfigRequestString);

        // when the mechanism is pull, there should be no interaction with the pmaregistry
        m_deviceMgmt.setPushPmaConfigurationToDevice("false");
        m_device.setDeviceManagement(m_deviceMgmt);
        m_defaultConfigService.deviceAdded(m_device.getDeviceName());
        //only one interaction from push mechanism
        verify(m_pmaRegistry).executeNC(m_device.getDeviceName(), editConfigRequestString);
    }

    @Test
    public void testDeviceAddedWithEmptyDefaultXml() throws IOException {
        when(m_adapterManager.getEditRequestForAdapter(any())).thenReturn(null);
        m_defaultConfigService.deviceAdded(m_device.getDeviceName());
        verifyZeroInteractions(m_pmaRegistry);
    }
    
    @Test
    public void testErrorScenarioDuringExecuteNc() {
        
    }

}