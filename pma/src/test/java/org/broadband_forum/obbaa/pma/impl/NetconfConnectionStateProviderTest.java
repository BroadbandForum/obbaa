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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;

import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.dm.DeviceManager;
import org.broadband_forum.obbaa.dmyang.entities.ConnectionState;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.DeviceConnection;
import org.broadband_forum.obbaa.dmyang.entities.DeviceMgmt;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class NetconfConnectionStateProviderTest {
    @Mock
    DeviceManager m_dm;
    NetconfConnectionStateProvider m_provider;
    @Mock
    private ConnectionState m_deviceState;
    @Mock
    private NetconfConnectionManager m_cm;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        m_provider = new NetconfConnectionStateProvider(m_dm, m_cm);
        when(m_cm.getConnectionState("device1")).thenReturn(m_deviceState);
        when(m_dm.getDevice("callHomeDevice-1")).thenReturn(createCallHomeDevice("callHomeDevice-1"));
    }

    @Test
    public void testInitAddsProviderDestroyRemovesProvider() {
        verify(m_dm, never()).addDeviceStateProvider(m_provider);
        verify(m_dm, never()).removeDeviceStateProvider(m_provider);

        m_provider.init();
        verify(m_dm).addDeviceStateProvider(m_provider);
        verify(m_dm, never()).removeDeviceStateProvider(m_provider);

        m_provider.destroy();
        verify(m_dm).addDeviceStateProvider(m_provider);
        verify(m_dm).removeDeviceStateProvider(m_provider);
    }

    @Test
    public void testStateIsFetchedFromCM() {
        LinkedHashMap<String, Object> state = m_provider.getState("device1");
        assertEquals(m_deviceState, state.get(NetconfConnectionStateProvider.CONNECTION_STATE));
        assertEquals(1, state.size());
    }

    @Test
    public void testCallHomeNewDeviceConnectionIsClosedWhenDeviceIsAdded() {
        verify(m_cm, never()).dropNewDeviceConnection(anyString());
        m_provider.deviceAdded("callHomeDevice-1");
        verify(m_cm).dropNewDeviceConnection("callHomeDevice-1");
    }

    private Device createCallHomeDevice(String deviceName) {
        Device device = new Device();
        device.setDeviceName(deviceName);
        DeviceMgmt deviceMgmt = new DeviceMgmt();
        DeviceConnection deviceConnection = new DeviceConnection();
        deviceConnection.setConnectionModel("call-home");
        deviceConnection.setDuid("callHomeDevice-1");
        deviceMgmt.setDeviceConnection(deviceConnection);
        device.setDeviceManagement(deviceMgmt);
        return device;
    }
}
