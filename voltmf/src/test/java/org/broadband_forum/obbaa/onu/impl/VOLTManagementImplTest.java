/*
 * Copyright 2020 Broadband Forum
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

package org.broadband_forum.obbaa.onu.impl;

import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.dm.DeviceManager;
import org.broadband_forum.obbaa.dmyang.entities.AttachmentPoint;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.DeviceMgmt;
import org.broadband_forum.obbaa.dmyang.entities.DeviceState;
import org.broadband_forum.obbaa.dmyang.entities.OnuConfigInfo;
import org.broadband_forum.obbaa.netconf.alarm.api.AlarmService;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.TxService;
import org.broadband_forum.obbaa.onu.ONUConstants;
import org.broadband_forum.obbaa.onu.UnknownONUHandler;
import org.broadband_forum.obbaa.onu.VOLTManagement;
import org.broadband_forum.obbaa.onu.entity.UnknownONU;
import org.broadband_forum.obbaa.onu.kafka.OnuKafkaProducer;
import org.broadband_forum.obbaa.pma.PmaRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * <p>
 * Unit tests that tests CRUD of vOMCI based ONU and notification handling
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 22/07/2020.
 */
public class VOLTManagementImplTest {

    @Mock
    private Device m_vonuDevice;
    @Mock
    private AlarmService m_alarmService;
    @Mock
    private UnknownONUHandler m_unknownOnuHandler;
    @Mock
    private OnuKafkaProducer m_kafkaProducer;
    @Mock
    private NetconfConnectionManager m_connectionManager;
    @Mock
    private ModelNodeDataStoreManager m_modelNodeDSM;
    @Mock
    private NotificationService m_notificationService;
    @Mock
    private AdapterManager m_adapterManager;
    @Mock
    private DeviceManager m_deviceManager;
    @Mock
    private DeviceMgmt m_deviceMgmt;
    @Mock
    private DeviceState m_deviceState;
    @Mock
    private PmaRegistry m_pmaRegistry;
    @Mock
    private OnuConfigInfo m_onuConfigInfo;
    @Mock
    private AttachmentPoint m_attachmentPoint;
    private TxService m_txService;
    private VOLTManagementImpl m_voltManagement;
    private String deviceName = "onu";
    private UnknownONU m_unknownOnu;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        m_unknownOnu = new UnknownONU();
        m_txService = new TxService();
        m_voltManagement = new VOLTManagementImpl(m_txService, m_deviceManager, m_alarmService, m_kafkaProducer, m_unknownOnuHandler,
                m_connectionManager, m_modelNodeDSM, m_notificationService, m_adapterManager, m_pmaRegistry);
        when(m_vonuDevice.getDeviceName()).thenReturn(deviceName);
        when(m_deviceManager.getDevice(deviceName)).thenReturn(m_vonuDevice);
        m_voltManagement.init();
    }

    @Test
    public void testOnuDeviceAddedForUnknownOnuNull() {
        m_unknownOnu = null;
        when(m_deviceManager.getDevice(deviceName)).thenReturn(m_vonuDevice);
        when(m_vonuDevice.getDeviceManagement()).thenReturn(m_deviceMgmt);
        when(m_deviceMgmt.getDeviceType()).thenReturn(ONUConstants.ONU_DEVICE_TYPE);
        when(m_unknownOnuHandler.findUnknownOnuEntity(anyString(), anyString())).thenReturn(m_unknownOnu);
        m_voltManagement.deviceAdded(deviceName);
        verify(m_unknownOnuHandler, never()).deleteUnknownOnuEntity(m_unknownOnu);
    }

    @Test
    public void testOnuDeviceAddedForUnknownOnuPresent() {
        when(m_deviceManager.getDevice(deviceName)).thenReturn(m_vonuDevice);
        when(m_vonuDevice.getDeviceManagement()).thenReturn(m_deviceMgmt);
        when(m_deviceMgmt.getDeviceState()).thenReturn(m_deviceState);
        when(m_deviceMgmt.getDeviceType()).thenReturn(ONUConstants.ONU_DEVICE_TYPE);
        when(m_deviceMgmt.getOnuConfigInfo()).thenReturn(m_onuConfigInfo);
        when(m_deviceState.getDeviceNodeId()).thenReturn("");
        when(m_onuConfigInfo.getSerialNumber()).thenReturn("AAAA12345678");
        when(m_onuConfigInfo.getRegistrationId()).thenReturn("12345678");
        when(m_onuConfigInfo.getAttachmentPoint()).thenReturn(m_attachmentPoint);
        when(m_attachmentPoint.getOnuId()).thenReturn("1");
        when(m_attachmentPoint.getChannelPartition()).thenReturn("CT");
        when(m_vonuDevice.isMediatedSession()).thenReturn(true);
        when(m_unknownOnuHandler.findUnknownOnuEntity(anyString(), anyString())).thenReturn(m_unknownOnu);
        m_voltManagement.deviceAdded(deviceName);
        m_voltManagement.waitForNotificationTasks();
        verify(m_kafkaProducer, times(1)).sendNotification(anyString(), anyString());
        verify(m_unknownOnuHandler, times(1)).deleteUnknownOnuEntity(m_unknownOnu);
    }

    @Test
    public void testWhenEonuDeviceAdded() {
        when(m_deviceManager.getDevice(deviceName)).thenReturn(m_vonuDevice);
        when(m_vonuDevice.getDeviceManagement()).thenReturn(m_deviceMgmt);
        when(m_deviceMgmt.getDeviceType()).thenReturn(ONUConstants.ONU_DEVICE_TYPE);
        m_voltManagement.deviceAdded(deviceName);
        when(m_vonuDevice.isMediatedSession()).thenReturn(false);
        verify(m_unknownOnuHandler, never()).findUnknownOnuEntity(anyString(), anyString());
        verify(m_unknownOnuHandler, never()).deleteUnknownOnuEntity(m_unknownOnu);
    }

    @Test
    public void testOnuDeviceAddedNull() {
        when(m_deviceManager.getDevice(deviceName)).thenReturn(null);
        m_voltManagement.deviceAdded(deviceName);
        verify(m_vonuDevice, never()).getDeviceManagement();
        verify(m_unknownOnuHandler, never()).findUnknownOnuEntity(anyString(), anyString());
        verify(m_unknownOnuHandler, never()).deleteUnknownOnuEntity(m_unknownOnu);
    }
}
