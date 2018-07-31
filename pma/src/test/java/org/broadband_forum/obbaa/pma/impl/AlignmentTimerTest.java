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

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.pma.PmaRegistry;
import org.broadband_forum.obbaa.pma.PmaSession;
import org.broadband_forum.obbaa.pma.PmaSessionTemplate;
import org.broadband_forum.obbaa.store.alignment.DeviceAlignmentInfo;
import org.broadband_forum.obbaa.store.alignment.DeviceAlignmentStore;
import org.broadband_forum.obbaa.store.dm.DeviceAdminStore;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AlignmentTimerTest {

    private static final String SAMPLE_EMPTY_NC_RES = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" " +
            "message-id=\"101012\">\n" +
            "  <data>\n" +
            "  </data>\n" +
            "</rpc-reply>";
    private static final String SAMPLE_NON_EMPTY_NC_RES = "<rpc-reply " +
            "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" " +
            "message-id=\"101012\">\n" +
            "  <data>\n" +
            "    <root-config xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
            "      <config1>\n" +
            "      </config1>\n" +
            "    </root-config>\n" +
            "  </data>\n" +
            "</rpc-reply>";
    private AlignmentTimer m_timer;
    private Set<DeviceAlignmentInfo> m_devices;
    private DeviceAlignmentInfo m_newDevice;
    private DeviceAlignmentInfo m_alignedDevice;
    private DeviceAlignmentInfo m_misalignedDevice;
    private DeviceAlignmentInfo m_inErrorDevice;
    @Mock
    private PmaRegistry m_pmaRegistry;
    @Mock
    private DeviceAlignmentStore m_alignmentStore;
    @Mock
    private PmaSession m_pmaSession;
    @Mock
    private NetconfConnectionManager m_connMgr;
    @Mock
    private DeviceAdminStore m_adminStore;

    @Before
    public void setUp() throws ExecutionException {
        MockitoAnnotations.initMocks(this);
        m_devices = new HashSet<>();
        m_newDevice = createDevice("new-device");
        m_newDevice.setVerdict(DeviceAlignmentInfo.NEVER_ALIGNED);
        m_devices.add(m_newDevice);
        m_alignedDevice = createDevice("aligned-device");
        m_alignedDevice.setVerdict(DeviceAlignmentInfo.ALIGNED);
        m_devices.add(m_alignedDevice);
        m_misalignedDevice = createDevice("misaligned-device");
        m_misalignedDevice.setVerdict("2 Edits pending");
        m_devices.add(m_misalignedDevice);
        m_inErrorDevice = createDevice("in-error");
        m_inErrorDevice.setVerdict(DeviceAlignmentInfo.IN_ERROR + ", blah blah");
        m_devices.add(m_inErrorDevice);
        m_timer = new AlignmentTimer(m_alignmentStore, m_pmaRegistry, m_adminStore, m_connMgr);
        when(m_alignmentStore.getAllEntries()).thenReturn(m_devices);
        when(m_pmaSession.executeNC(anyString())).thenReturn(SAMPLE_EMPTY_NC_RES);
        when(m_connMgr.isConnected(anyObject())).thenReturn(true);
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            return ((PmaSessionTemplate) args[1]).execute(m_pmaSession);
        }).when(m_pmaRegistry).executeWithPmaSession(anyString(), anyObject());
    }

    private DeviceAlignmentInfo createDevice(String deviceName) {
        DeviceAlignmentInfo device = new DeviceAlignmentInfo(deviceName);
        return device;
    }

    @Test
    public void testNewDevicesAreAligned() throws ExecutionException {
        m_timer.runAlignment();
        verify(m_pmaRegistry, never()).forceAlign("new-device");
        verify(m_pmaRegistry, never()).forceAlign("aligned-device");
        verify(m_pmaRegistry, never()).forceAlign("misaligned-device");
        verify(m_pmaRegistry).align(m_misalignedDevice.getName());
        verify(m_pmaRegistry, never()).align(m_alignedDevice.getName());
        verify(m_pmaRegistry, never()).align(m_newDevice.getName());
    }

    @Test
    public void testNewDevicesWithConfigurationsAreAligned() throws ExecutionException {
        when(m_pmaSession.executeNC(anyString())).thenReturn(SAMPLE_NON_EMPTY_NC_RES);
        m_timer.runAlignment();
        verify(m_pmaRegistry).forceAlign("new-device");
    }

    @Test
    public void testNewDevicesWithConfigurationsAreAlignedOnlyWhenDeviceIsConnected() throws ExecutionException {
        when(m_connMgr.isConnected(anyObject())).thenReturn(false);
        m_timer.runAlignment();
        verify(m_pmaRegistry, never()).forceAlign("new-device");
        verify(m_pmaRegistry, never()).forceAlign("aligned-device");
        verify(m_pmaRegistry, never()).forceAlign("misaligned-device");
        verify(m_pmaRegistry, never()).align(m_misalignedDevice.getName());
        verify(m_pmaRegistry, never()).align(m_alignedDevice.getName());
        verify(m_pmaRegistry, never()).align(m_newDevice.getName());
    }
}
