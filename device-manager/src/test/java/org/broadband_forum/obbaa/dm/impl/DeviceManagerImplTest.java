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

package org.broadband_forum.obbaa.dm.impl;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.List;

import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.connectors.sbi.netconf.NewDeviceInfo;
import org.broadband_forum.obbaa.dm.DeviceStateProvider;
import org.broadband_forum.obbaa.store.dm.CallHomeInfo;
import org.broadband_forum.obbaa.store.dm.DeviceAdminStore;
import org.broadband_forum.obbaa.store.dm.DeviceInfo;
import org.broadband_forum.obbaa.store.dm.SshConnectionInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Created by kbhatk on 29/9/17.
 */
public class DeviceManagerImplTest {
    private DeviceManagerImpl m_dm;
    @Mock
    private DeviceAdminStore m_dms;
    @Mock
    private DeviceStateProvider m_das;
    @Mock
    private NetconfConnectionManager m_cm;
    @Mock
    private List<NewDeviceInfo> m_newDevices;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        m_dm = new DeviceManagerImpl(m_dms, m_cm);
        m_dm.addDeviceStateProvider(m_das);
        when(m_cm.getNewDevices()).thenReturn(m_newDevices);
    }

    @Test
    public void testDMSIsUpdatedOnDeviceCreate() {
        DeviceInfo deviceInfo = createDevice();
        verify(m_dms).create(deviceInfo);
    }

    @Test
    public void testDMSIsUpdatedOnDeviceCreateWithCallhomeInfo() {
        DeviceInfo deviceInfo = createDeviceWithCallhomeInfo();
        verify(m_dms).create(deviceInfo);
    }

    @Test
    public void testDASIsUpdatedOnDeviceCreate() {
        DeviceInfo deviceInfo = createDevice();
        verify(m_das).deviceAdded(deviceInfo.getKey());
    }

    @Test
    public void testDASIsUpdatedOnDeviceDelete() {
        DeviceInfo deviceInfo = createDevice();
        when(m_dms.get("Bangalore-south")).thenReturn(deviceInfo);
        m_dm.deleteDevice(deviceInfo.getKey());
        verify(m_das).deviceRemoved(deviceInfo.getKey());
    }

    @Test
    public void testDASIsUpdatedOnDeviceUpdate() {
        DeviceInfo deviceInfo = createDevice();
        when(m_dms.get("Bangalore-south")).thenReturn(deviceInfo);
        m_dm.updateDevice(deviceInfo);
        verify(m_das).deviceRemoved(deviceInfo.getKey());
        verify(m_das, times(2)).deviceAdded(deviceInfo.getKey());
    }

    private DeviceInfo createDevice() {
        DeviceInfo deviceInfo = new DeviceInfo("Bangalore-south", new SshConnectionInfo("10.1.1.1", 1234, "user",
                "pass"));
        m_dm.createDevice(deviceInfo);
        when(m_dms.get(deviceInfo.getKey())).thenReturn(deviceInfo);
        return deviceInfo;
    }

    private DeviceInfo createDeviceWithCallhomeInfo() {
        DeviceInfo deviceInfo = new DeviceInfo("Bangalore-south", new CallHomeInfo("duid"));
        m_dm.createDevice(deviceInfo);
        when(m_dms.get(deviceInfo.getKey())).thenReturn(deviceInfo);
        return deviceInfo;
    }

    @Test
    public void testDeviceMetaIsFetchedFromDMS() {
        DeviceInfo deviceInfo = mock(DeviceInfo.class);
        when(m_dms.get("Bangalore-south")).thenReturn(deviceInfo);
        assertEquals(deviceInfo, m_dm.getDevice("Bangalore-south"));
        verify(m_dms).get("Bangalore-south");
    }

    @Test
    public void testDeviceMetaUpdateContactsDMS() {
        DeviceInfo firstMeta = createDevice();
        when(m_dms.get("Bangalore-south")).thenReturn(firstMeta);
        DeviceInfo deviceInfo = new DeviceInfo("Bangalore-south", new SshConnectionInfo("10.1.1.3", 1234, "user",
                "pass"));
        m_dm.updateDevice(deviceInfo);
        verify(m_dms).update(deviceInfo);
    }

    @Test
    public void testDeleteDeviceContactsDMS() {
        m_dm.deleteDevice("Bangalore-south");
        verify(m_dms).delete("Bangalore-south");
    }

    @Test
    public void testGetAllDevices() {
        verify(m_dms, never()).getAllEntries();
        m_dm.getAllDevices();
        verify(m_dms).getAllEntries();
    }

    @Test
    public void testDataFromStateProvidersIsFetched() {
        createDevice();
        DeviceStateProvider stateProvider = mock(DeviceStateProvider.class);
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("state1", "statevalue1");
        when(stateProvider.getState("Bangalore-south")).thenReturn(map);
        m_dm.addDeviceStateProvider(stateProvider);
        DeviceInfo info = m_dm.getDevice("Bangalore-south");
        assertEquals(map, info.getDeviceState());

        createDevice();
        m_dm.removeDeviceStateProvider(stateProvider);
        assertTrue(m_dm.getDevice("Bangalore-south").getDeviceState().isEmpty());
    }

    @Test
    public void testGetNewDevices() {
        assertEquals(m_newDevices, m_dm.getNewDevices());
    }

    private DeviceInfo createDevice(String deviceName) {
        DeviceInfo deviceInfo = new DeviceInfo(deviceName, new SshConnectionInfo(getIp(deviceName), 1234, "user",
                "pass"));
        return deviceInfo;
    }

    private String getIp(String deviceName) {
        return "10.1.1." + deviceName;
    }
}
