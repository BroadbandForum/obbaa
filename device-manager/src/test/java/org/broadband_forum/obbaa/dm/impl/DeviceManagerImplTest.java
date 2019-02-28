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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.broadband_forum.obbaa.dm.DeviceStateProvider;
import org.broadband_forum.obbaa.dmyang.dao.DeviceDao;
import org.broadband_forum.obbaa.dmyang.entities.Authentication;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.DeviceConnection;
import org.broadband_forum.obbaa.dmyang.entities.DeviceMgmt;
import org.broadband_forum.obbaa.dmyang.entities.PasswordAuth;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Created by kbhatk on 29/9/17.
 */
public class DeviceManagerImplTest {
    private DeviceManagerImpl m_dm;
    @Mock
    private DeviceDao m_deviceDao;
    @Mock
    private DeviceStateProvider m_deviceConnectionStateProvider;
    @Mock
    private DeviceStateProvider m_deviceAlignmentStateProvider;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        m_dm = new DeviceManagerImpl(m_deviceDao);
        m_dm.setDeviceDao(m_deviceDao);
        m_dm.addDeviceStateProvider(m_deviceConnectionStateProvider);
        m_dm.addDeviceStateProvider(m_deviceAlignmentStateProvider);
    }

    @Test
    public void testCallDeviceAdded() {
        m_dm.deviceAdded("deviceA");
        verify(m_deviceConnectionStateProvider).deviceAdded("deviceA");
        verify(m_deviceAlignmentStateProvider).deviceAdded("deviceA");
    }

    @Test
    public void testRemoveOneStateProvider() {
        m_dm.removeDeviceStateProvider(m_deviceAlignmentStateProvider);
        m_dm.deviceAdded("deviceA");
        verify(m_deviceConnectionStateProvider).deviceAdded("deviceA");
        verify(m_deviceAlignmentStateProvider, never()).deviceAdded("deviceA");
    }

    @Test
    public void testCallDeviceRemoved() {
        m_dm.deviceRemoved("deviceA");
        verify(m_deviceConnectionStateProvider).deviceRemoved("deviceA");
        verify(m_deviceAlignmentStateProvider).deviceRemoved("deviceA");
    }

    @Test
    public void testGetDevice() {
        Device deviceA = createDevice("deviceA");
        when(m_deviceDao.getDeviceByName("deviceA")).thenReturn(deviceA);
        Device device = m_dm.getDevice("deviceA");
        verify(m_deviceDao).getDeviceByName("deviceA");
        assertEquals(deviceA, device);
    }

    @Test
    public void testGetDeviceNull() {
        try {
            m_dm.getDevice("deviceA");
            verify(m_deviceDao).getDeviceByName("deviceA");
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Device deviceA does not exist", e.getMessage());
        }
    }

    @Test
    public void testGetAllDevice() {
        Device deviceA = createDevice("deviceA");
        Device deviceB = createDevice("deviceA");
        List<Device> deviceList = new ArrayList<>();
        deviceList.add(deviceA);
        deviceList.add(deviceB);
        when(m_deviceDao.findAllDevices()).thenReturn(deviceList);
        List<Device> devices = m_dm.getAllDevices();
        verify(m_deviceDao).findAllDevices();
        assertEquals(deviceList, devices);
    }

    @Test
    public void testGetAllDeviceNull() {
        verify(m_deviceDao, never()).findAllDevices();
        List<Device> deviceList = m_dm.getAllDevices();
        verify(m_deviceDao).findAllDevices();
        assertEquals(0, deviceList.size());
    }

    @Test
    public void testCallDevicePropertyChanged() {
        m_dm.devicePropertyChanged("deviceA");
        InOrder inOrder = inOrder(m_deviceAlignmentStateProvider, m_deviceConnectionStateProvider);
        inOrder.verify(m_deviceConnectionStateProvider).deviceRemoved("deviceA");
        inOrder.verify(m_deviceAlignmentStateProvider).deviceRemoved("deviceA");
        inOrder.verify(m_deviceConnectionStateProvider).deviceAdded("deviceA");
        inOrder.verify(m_deviceAlignmentStateProvider).deviceAdded("deviceA");
    }

    @Test
    public void testupdateConfigAlignmentState() {
        m_dm.updateConfigAlignmentState("deviceA", "Aligned");
        verify(m_deviceDao).updateDeviceAlignmentState("deviceA", "Aligned");
    }

    @Test
    public void testDeviceMetaIsFetchedFromDMS() {
        Device device = mock(Device.class);
        when(m_deviceDao.getDeviceByName("Bangalore-south")).thenReturn(device);
        assertEquals(device, m_dm.getDevice("Bangalore-south"));
        verify(m_deviceDao).getDeviceByName("Bangalore-south");
    }

    private Device createDevice(String deviceName) {
        Device device = new Device();
        device.setDeviceName(deviceName);
        DeviceMgmt deviceMgmt = new DeviceMgmt();
        DeviceConnection deviceConnection = new DeviceConnection();
        PasswordAuth password = new PasswordAuth();
        Authentication auth = new Authentication();
        auth.setUsername("user");
        auth.setPassword("pass");
        auth.setAddress(getIp(deviceName));
        auth.setManagementPort(String.valueOf(1234));
        password.setAuthentication(auth);
        deviceConnection.setPasswordAuth(password);
        deviceMgmt.setDeviceConnection(deviceConnection);
        device.setDeviceManagement(deviceMgmt);
        return device;
    }

    private String getIp(String deviceName) {
        return "10.1.1." + deviceName;
    }
}
