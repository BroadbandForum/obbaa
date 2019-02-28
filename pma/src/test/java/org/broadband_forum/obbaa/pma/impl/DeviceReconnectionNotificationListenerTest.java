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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class DeviceReconnectionNotificationListenerTest {


    private DeviceReconnectionNotificationListener m_listener;

    private NotificationService m_notificationService;

    private NetconfConnectionManager m_connMgr;

    @Before
    public void setUp() {

        m_notificationService = mock(NotificationService.class);
        m_connMgr = mock(NetconfConnectionManager.class);
        m_listener = new DeviceReconnectionNotificationListener(m_notificationService, m_connMgr);
    }

    @Test
    public void testInit() {
        m_listener.init();
        verify(m_connMgr).registerDeviceConnectionListener(m_listener);
    }

    @Test
    public void testDestroy() {
        m_listener.destroy();
        verify(m_connMgr).unregisterDeviceConnectionListener(m_listener);
    }

    @Test
    public void testDeviceConnected() throws NetconfMessageBuilderException {
        Device device = new Device();
        device.setDeviceName("testDeviceOn");
        NetconfClientSession clientSession = mock(NetconfClientSession.class);
        m_listener.deviceConnected(device, clientSession);
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(m_notificationService, times(1)).sendNotification(eq("STATE_CHANGE"), captor.capture());
        Notification notification = captor.getValue();
        String expectedNotifElem = "<network-manager xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
                "<managed-devices>\n" +
                "<device>\n" +
                "<name>testDeviceOn</name>\n" +
                "<device-notification>\n" +
                "<device-state-change>\n" +
                "<event>online</event>\n" +
                "</device-state-change>\n" +
                "</device-notification>\n" +
                "</device>\n" +
                "</managed-devices>\n" +
                "</network-manager>\n";
        assertEquals(expectedNotifElem, DocumentUtils.documentToPrettyString(notification.getNotificationElement()));
    }

    @Test
    public void testDeviceDisConnected() throws NetconfMessageBuilderException {
        Device device = new Device();
        device.setDeviceName("testDeviceOFF");
        NetconfClientSession clientSession = mock(NetconfClientSession.class);
        m_listener.deviceDisConnected(device, clientSession);
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(m_notificationService, times(1)).sendNotification(eq("STATE_CHANGE"), captor.capture());
        Notification notification = captor.getValue();
        String expectedNotifElem = "<network-manager xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
                "<managed-devices>\n" +
                "<device>\n" +
                "<name>testDeviceOFF</name>\n" +
                "<device-notification>\n" +
                "<device-state-change>\n" +
                "<event>offline</event>\n" +
                "</device-state-change>\n" +
                "</device-notification>\n" +
                "</device>\n" +
                "</managed-devices>\n" +
                "</network-manager>\n";
        assertEquals(expectedNotifElem, DocumentUtils.documentToPrettyString(notification.getNotificationElement()));
    }


}
