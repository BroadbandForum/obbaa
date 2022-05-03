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

package org.broadband_forum.obbaa.nm.devicemanager.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.broadband_forum.obbaa.dmyang.dao.DeviceDao;
import org.broadband_forum.obbaa.dmyang.dao.SoftwareImageDao;
import org.broadband_forum.obbaa.dmyang.entities.Authentication;
import org.broadband_forum.obbaa.dmyang.entities.ConnectionState;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.DeviceConnection;
import org.broadband_forum.obbaa.dmyang.entities.DeviceMgmt;
import org.broadband_forum.obbaa.dmyang.entities.DeviceState;
import org.broadband_forum.obbaa.dmyang.entities.OnuStateInfo;
import org.broadband_forum.obbaa.dmyang.entities.PasswordAuth;
import org.broadband_forum.obbaa.dmyang.entities.SoftwareImage;
import org.broadband_forum.obbaa.nm.devicemanager.DeviceStateProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Created by kbhatk on 29/9/17.
 */
public class DeviceManagerImplTest {
    Date m_now = new Date();
    private DeviceManagerImpl m_dm;
    @Mock
    private DeviceDao m_deviceDao;
    @Mock
    private OnuStateInfo m_onuStateInfo;
    @Mock
    private SoftwareImageDao m_softwareImageDao;
    @Mock
    private DeviceStateProvider m_deviceConnectionStateProvider;
    @Mock
    private DeviceStateProvider m_deviceAlignmentStateProvider;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        m_dm = new DeviceManagerImpl(m_deviceDao, m_softwareImageDao);
        m_dm.setDeviceDao(m_deviceDao);
        m_dm.setSoftwareImageDao(m_softwareImageDao);
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

    @Test
    public void testUpdateEquipmentIdInOnuStateInfo() {
        Device onuDevice = new Device();
        onuDevice.setDeviceName("onu1");
        DeviceState deviceState = new DeviceState();
        deviceState.setConfigAlignmentState("Aligned");
        ConnectionState connectionState = new ConnectionState();
        connectionState.setConnectionCreationTime(m_now);
        connectionState.setConnected(true);
        DeviceMgmt devicemgmt = new DeviceMgmt();
        devicemgmt.setDeviceState(deviceState);
        devicemgmt.setDeviceType("ONU");
        onuDevice.setDeviceManagement(devicemgmt);
        OnuStateInfo onuStateInfo = new OnuStateInfo();
        deviceState.setOnuStateInfo(onuStateInfo);
        onuStateInfo.setEquipmentId("test");
        when(m_deviceDao.getDeviceByName("onu1")).thenReturn(onuDevice);
        when(m_dm.getDevice("onu1")).thenReturn(onuDevice);
        when(m_deviceDao.getDeviceState("onu1")).thenReturn(deviceState);
        assertEquals("test", m_deviceDao.getDeviceState("onu1").getOnuStateInfo().getEquipmentId());
        m_dm.updateEquipmentIdInOnuStateInfo("onu1", "eqptIdUpdated");
        assertEquals("eqptIdUpdated", m_deviceDao.getDeviceState("onu1").getOnuStateInfo().getEquipmentId());
    }

    @Test
    public void testDeterminedMgmtModeInOnuStateInfo() {
        Device onuDevice = new Device();
        onuDevice.setDeviceName("onu1");
        DeviceState deviceState = new DeviceState();
        deviceState.setConfigAlignmentState("Aligned");
        ConnectionState connectionState = new ConnectionState();
        connectionState.setConnectionCreationTime(m_now);
        connectionState.setConnected(true);
        DeviceMgmt devicemgmt = new DeviceMgmt();
        devicemgmt.setDeviceState(deviceState);
        devicemgmt.setDeviceType("ONU");
        onuDevice.setDeviceManagement(devicemgmt);
        OnuStateInfo onuStateInfo = new OnuStateInfo();
        deviceState.setOnuStateInfo(onuStateInfo);
        onuStateInfo.setDetermineOnuManagementMode("use-vomci");
        when(m_deviceDao.getDeviceByName("onu1")).thenReturn(onuDevice);
        when(m_dm.getDevice("onu1")).thenReturn(onuDevice);
        when(m_deviceDao.getDeviceState("onu1")).thenReturn(deviceState);
        m_dm.updateOnuStateInfo("onu1", onuStateInfo);
        assertEquals("use-vomci", m_deviceDao.getDeviceState("onu1").getOnuStateInfo().getDetermineOnuManagementMode());
    }

    @Test
    public void testUpdateSoftwareImageInOnuStateInfo() {
        Device onuDevice = new Device();
        onuDevice.setDeviceName("onu2");
        DeviceState deviceState = new DeviceState();
        deviceState.setConfigAlignmentState("Aligned");
        ConnectionState connectionState = new ConnectionState();
        connectionState.setConnectionCreationTime(m_now);
        connectionState.setConnected(true);
        DeviceMgmt devicemgmt = new DeviceMgmt();
        devicemgmt.setDeviceState(deviceState);
        devicemgmt.setDeviceType("ONU");
        onuDevice.setDeviceManagement(devicemgmt);
        OnuStateInfo onuStateInfo = new OnuStateInfo();
        deviceState.setOnuStateInfo(onuStateInfo);
        when(m_deviceDao.getDeviceByName("onu2")).thenReturn(onuDevice);
        when(m_dm.getDevice("onu2")).thenReturn(onuDevice);
        when(m_deviceDao.getDeviceState("onu2")).thenReturn(deviceState);
        Set<SoftwareImage> swImageSet = prepareVomciSwImage0Set("onu2");
        m_dm.updateSoftwareImageInOnuStateInfo("onu2", swImageSet);
        verify(m_softwareImageDao, times(1)).updateSWImageInOnuStateInfo(m_deviceDao.getDeviceState("onu2"),swImageSet );
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

    private Set<SoftwareImage> prepareVomciSwImage0Set(String parentId) {
        // create vomci sw Image set
        Set<SoftwareImage> swImage0SetVomci = new HashSet<>();
        SoftwareImage softwareImage0 = new SoftwareImage();
        softwareImage0.setId(0);
        softwareImage0.setParentId(parentId);
        softwareImage0.setHash("1001");
        softwareImage0.setProductCode("test");
        softwareImage0.setVersion("1.01");
        softwareImage0.setIsValid(false);
        softwareImage0.setIsCommitted(false);
        softwareImage0.setIsActive(false);
        swImage0SetVomci.add(softwareImage0);
        return swImage0SetVomci;
    }

    private String getIp(String deviceName) {
        return "10.1.1." + deviceName;
    }
}
