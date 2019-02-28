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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutionException;

import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.client.NotificationListener;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.junit.Before;
import org.junit.Test;

import jdk.nashorn.internal.runtime.ParserException;

public class DeviceReconnectionNotificationActionTest {

    private NotificationService m_notificationService;

    private NetconfConnectionManager m_netconfConnectionManager;
    private DeviceReconnectionNotificationAction m_deviceReconnectNotificationAction;

    @Before
    public void setUp() {
        m_notificationService = mock(NotificationService.class);
        m_netconfConnectionManager = mock(NetconfConnectionManager.class);
        m_deviceReconnectNotificationAction = new DeviceReconnectionNotificationAction(m_notificationService, m_netconfConnectionManager);
    }

    @Test
    public void testInit() {
        m_deviceReconnectNotificationAction.init();
        verify(m_netconfConnectionManager).registerDeviceConnectionListener(m_deviceReconnectNotificationAction);
    }

    @Test
    public void testDestroy() {
        m_deviceReconnectNotificationAction.destroy();
        verify(m_netconfConnectionManager).unregisterDeviceConnectionListener(m_deviceReconnectNotificationAction);
    }


    @Test
    public void testCreateSubscriptionWithReplay() throws NetconfMessageBuilderException, InterruptedException, ExecutionException,
            ParserException {
        NetconfClientSession clientSession = mock(NetconfClientSession.class);
        Device device = mock(Device.class);

        when(m_notificationService.isNotificationSupported(any(NetconfClientSession.class))).thenReturn(true);
        when(m_notificationService.isReplaySupported(any(NetconfClientSession.class))).thenReturn(true);
        NetConfResponse response = new NetConfResponse();
        response.setOk(true);
        when(m_notificationService.createSubscriptionWithCallback(any(NetconfClientSession.class), any(String.class),
                any(NotificationListener.class), any(), eq(true))).thenReturn(response);
        m_deviceReconnectNotificationAction.deviceConnected(device, clientSession);

        verify(m_notificationService, never()).createSubscriptionWithCallback(any(NetconfClientSession.class), any(String.class),
                any(NotificationListener.class), any(), eq(false));
    }

    @Test
    public void testCreateSubscriptionWithoutReplay() throws NetconfMessageBuilderException, InterruptedException, ExecutionException,
            ParserException {
        NetconfClientSession clientSession = mock(NetconfClientSession.class);
        Device device = mock(Device.class);

        when(m_notificationService.isNotificationSupported(any(NetconfClientSession.class))).thenReturn(true);
        when(m_notificationService.isReplaySupported(any(NetconfClientSession.class))).thenReturn(false);
        NetConfResponse response = new NetConfResponse();
        response.setOk(true);
        when(m_notificationService.createSubscriptionWithCallback(any(NetconfClientSession.class), any(String.class),
                any(NotificationListener.class), any(), eq(false))).thenReturn(response);
        m_deviceReconnectNotificationAction.deviceConnected(device, clientSession);

        verify(m_notificationService, never()).createSubscriptionWithCallback(any(NetconfClientSession.class), any(String.class),
                any(NotificationListener.class), any(), eq(true));
    }

}
