/*
 * Copyright 2021 Broadband Forum
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
package org.broadband_forum.obbaa.onu.notification;

import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.DeviceMgmt;
import org.broadband_forum.obbaa.dmyang.entities.DeviceState;
import org.broadband_forum.obbaa.dmyang.entities.OnuConfigInfo;
import org.broadband_forum.obbaa.dmyang.entities.OnuStateInfo;
import org.broadband_forum.obbaa.dmyang.entities.SoftwareImage;
import org.broadband_forum.obbaa.dmyang.entities.SoftwareImages;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.onu.ONUConstants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.w3c.dom.Element;

@RunWith(PowerMockRunner.class)
@PrepareForTest({OnuDiscoveryResultNotification.class})
public class OnuDiscoveryResultNotificationTest {

    @Mock
    Device m_device;
    @Mock
    DeviceMgmt m_deviceMgmt;
    @Mock
    OnuConfigInfo m_onuConfigInfo;
    @Mock
    DeviceState m_deviceState;
    @Mock
    OnuStateInfo m_onuStateInfo;
    @Mock
    SoftwareImages m_softwareImages;

    OnuDiscoveryResultNotification m_onuDiscoveryResultNotification;
    Set<SoftwareImage> m_softwareImageSet = new HashSet<>();

    String m_serialNumber = "BRCM00000001";
    String m_equipmentId = "eqpt-1";
    SoftwareImage softwareImage1 = new SoftwareImage();

    String resultOnusStateOnline = "/onu-state-online-result.xml";
    String resultWithEquipIdNull = "/equipment-id-null-result.xml";
    String resultSoftwareInfoNull = "/software-info-null-result.xml";
    String resultForMultipleSftwrImg = "/multiple-software-image-result.xml";

    @Before
    public void setup() {
        softwareImage1.setId(0);
        softwareImage1.setVersion("1.0");
        softwareImage1.setIsCommitted(true);
        softwareImage1.setIsValid(true);
        softwareImage1.setIsActive(true);
        softwareImage1.setProductCode("product-1.0");
        softwareImage1.setHash("1234");
        when(m_device.getDeviceManagement()).thenReturn(m_deviceMgmt);
        when(m_deviceMgmt.getOnuConfigInfo()).thenReturn(m_onuConfigInfo);
        when(m_onuConfigInfo.getExpectedSerialNumber()).thenReturn(m_serialNumber);
        when(m_deviceMgmt.getDeviceState()).thenReturn(m_deviceState);
        when(m_deviceState.getOnuStateInfo()).thenReturn(m_onuStateInfo);
        when(m_onuStateInfo.getSoftwareImages()).thenReturn(m_softwareImages);
        when(m_softwareImages.getSoftwareImage()).thenReturn(m_softwareImageSet);
    }

    @Test
    public void testGetOnuDiscoveryResult() throws Exception {
        m_softwareImageSet.add(softwareImage1);
        when(m_onuStateInfo.getEquipmentId()).thenReturn(m_equipmentId);
        m_onuDiscoveryResultNotification = new OnuDiscoveryResultNotification(m_device, ONUConstants.ONLINE);
        Element result = m_onuDiscoveryResultNotification.getOnuDiscoveryResultNotificationElement(m_device, ONUConstants.ONLINE);
        assertEquals(DocumentUtils.documentToPrettyString(result),
                DocumentUtils.documentToPrettyString(loadAsXml(resultOnusStateOnline)));
    }

    @Test
    public void testGetOnuDiscoveryResultWhenEquipmentIdIsNULL() throws NetconfMessageBuilderException {
        m_softwareImageSet.add(softwareImage1);
        when(m_onuStateInfo.getEquipmentId()).thenReturn(null);
        m_onuDiscoveryResultNotification = new OnuDiscoveryResultNotification(m_device, ONUConstants.OFFLINE);
        Element result = m_onuDiscoveryResultNotification.getOnuDiscoveryResultNotificationElement(m_device, ONUConstants.OFFLINE);
        assertEquals(DocumentUtils.documentToPrettyString(result),
                DocumentUtils.documentToPrettyString(loadAsXml(resultWithEquipIdNull)));
    }

    @Test
    public void testGetOnuDiscoveryResultWhenSoftInfoIsNULL() throws NetconfMessageBuilderException {
        when(m_onuStateInfo.getEquipmentId()).thenReturn(null);
        m_onuDiscoveryResultNotification = new OnuDiscoveryResultNotification(m_device, ONUConstants.OFFLINE);
        Element result = m_onuDiscoveryResultNotification.getOnuDiscoveryResultNotificationElement(m_device, ONUConstants.OFFLINE);
        assertEquals(DocumentUtils.documentToPrettyString(result),
                DocumentUtils.documentToPrettyString(loadAsXml(resultSoftwareInfoNull)));
    }

    @Test
    public void testGetOnuDiscoveryResultWithMultipleSoftwareImage() throws NetconfMessageBuilderException {
        SoftwareImage softwareImage2 = new SoftwareImage();
        softwareImage2.setId(1);
        softwareImage2.setVersion("2.0");
        softwareImage2.setIsCommitted(false);
        softwareImage2.setIsValid(false);
        softwareImage2.setIsActive(false);
        softwareImage2.setProductCode("product2");
        softwareImage2.setHash("4321");

        m_softwareImageSet.add(softwareImage1);
        m_softwareImageSet.add(softwareImage2);

        when(m_onuStateInfo.getEquipmentId()).thenReturn(m_equipmentId);
        m_onuDiscoveryResultNotification = new OnuDiscoveryResultNotification(m_device, ONUConstants.ONLINE);
        Element result = m_onuDiscoveryResultNotification.getOnuDiscoveryResultNotificationElement(m_device, ONUConstants.ONLINE);
        assertEquals(DocumentUtils.documentToPrettyString(result),
                DocumentUtils.documentToPrettyString(loadAsXml(resultForMultipleSftwrImg)));
    }

}
