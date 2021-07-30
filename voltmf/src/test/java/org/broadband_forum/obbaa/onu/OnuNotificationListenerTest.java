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

package org.broadband_forum.obbaa.onu;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfNotification;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.onu.message.MessageFormatter;
import org.broadband_forum.obbaa.onu.notification.ONUNotification;
import org.broadband_forum.obbaa.pma.DeviceNotificationListenerService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * <p>
 * Unit tests that tests onu-state-change notification handling
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 22/07/2020.
 */
public class OnuNotificationListenerTest {
    @Mock
    private DeviceNotificationListenerService m_deviceNotifService;
    @Mock
    private Notification m_someOtherNotif;
    @Mock
    private VOLTManagement voltManagement;
    @Mock
    private Device device;
    @Mock
    private MessageFormatter messageFormatter;

    private ONUNotificationListener m_listener;
    String deviceName = "onu";
    private QName m_someOtherQname = QName.create("http://example.com", "example");
    private NetconfNotification m_onuDetectedNotif;

    @Before
    public void setUp() throws NetconfMessageBuilderException {
        MockitoAnnotations.initMocks(this);
        when(device.getDeviceName()).thenReturn(deviceName);
        m_onuDetectedNotif = new NetconfNotification(DocumentUtils.stringToDocument("<notification xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">\n" +
                "    <eventTime>2019-07-25T05:53:36+00:00</eventTime>\n" +
                "    <bbf-xpon-onu-states:onu-state-change xmlns:bbf-xpon-onu-states=\"urn:bbf:yang:bbf-xpon-onu-states\">                               \n" +
                "        <bbf-xpon-onu-states:detected-serial-number>ABCD00000002</bbf-xpon-onu-states:detected-serial-number>\n" +
                "        <bbf-xpon-onu-states:onu-id>25</bbf-xpon-onu-states:onu-id>\n" +
                "        <bbf-xpon-onu-states:channel-termination-ref>CT-1</bbf-xpon-onu-states:channel-termination-ref>\n" +
                "        <bbf-xpon-onu-states:onu-state>onu-not-present</bbf-xpon-onu-states:onu-state>\n" +
                "        <bbf-xpon-onu-states:detected-registration-id>ABCD1234</bbf-xpon-onu-states:detected-registration-id>\n" +
                "        <bbf-xpon-onu-states:onu-state-last-change>2019-07-25T05:53:36+00:00</bbf-xpon-onu-states:onu-state-last-change>\n" +
                "        <bbf-xpon-onu-states:v-ani-ref>onu_25</bbf-xpon-onu-states:v-ani-ref>\n" +
                "    </bbf-xpon-onu-states:onu-state-change>\n" +
                "</notification>"));
        when(m_someOtherNotif.getType()).thenReturn(m_someOtherQname);
        when(m_someOtherNotif.getNotificationElement()).thenReturn(DocumentUtils.stringToDocument("    <bbf-xpon-onu-states:onu-state-change xmlns:bbf-xpon-onu-states=\"urn:bbf:yang:bbf-xpon-onu-states\">                               \n" +
                        "        <bbf-xpon-onu-states:detected-serial-number>ALCL00000002</bbf-xpon-onu-states:detected-serial-number>\n" +
                        "        <bbf-xpon-onu-states:onu-id>25</bbf-xpon-onu-states:onu-id>\n" +
                        "        <bbf-xpon-onu-states:channel-termination-ref>CT-1</bbf-xpon-onu-states:channel-termination-ref>\n" +
                        "        <bbf-xpon-onu-states:onu-state>onu-not-present</bbf-xpon-onu-states:onu-state>\n" +
                        "        <bbf-xpon-onu-states:detected-registration-id>ABCD1234</bbf-xpon-onu-states:detected-registration-id>\n" +
                        "        <bbf-xpon-onu-states:onu-state-last-change>2019-07-25T05:53:36+00:00</bbf-xpon-onu-states:onu-state-last-change>\n" +
                        "        <bbf-xpon-onu-states:v-ani-ref>onu_25</bbf-xpon-onu-states:v-ani-ref>\n" +
                        "    </bbf-xpon-onu-states:onu-state-change>").getDocumentElement());
        m_listener = new ONUNotificationListener(m_deviceNotifService, voltManagement,messageFormatter);
    }

    @Test
    public void testListenerRegistersAndUnregisters() {
        verifyZeroInteractions(m_deviceNotifService);

        m_listener.init();
        verify(m_deviceNotifService).registerDeviceNotificationClientListener(m_listener);
        verify(m_deviceNotifService, never()).unregisterDeviceNotificationClientListener(m_listener);

        m_listener.destroy();
        verify(m_deviceNotifService).registerDeviceNotificationClientListener(m_listener);
        verify(m_deviceNotifService).unregisterDeviceNotificationClientListener(m_listener);

    }

    @Test
    public void testListenerIgnoresOtherNotifications() {
        m_listener.deviceNotificationReceived(device, m_someOtherNotif);
        verify(voltManagement, never()).onuNotificationProcess(any(), any());
    }

    @Test
    public void testListenerFwdsDetectedNotifications() {
        m_listener.deviceNotificationReceived(device, m_onuDetectedNotif);
        try {
            ArgumentCaptor<ONUNotification> notification = ArgumentCaptor.forClass(ONUNotification.class);
            verify(voltManagement).onuNotificationProcess(notification.capture(), deviceName);
            verifyNoMoreInteractions(voltManagement);
        } catch (Exception e) {}
    }
}
