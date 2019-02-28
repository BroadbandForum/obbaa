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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.connectors.sbi.netconf.ConnectionListener;
import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;


public class DeviceReconnectionNotificationListener implements ConnectionListener {

    private static final Logger LOGGER = Logger.getLogger(DeviceReconnectionNotificationAction.class);

    private NotificationService m_notificationService;
    private NetconfConnectionManager m_netconfConnectionManager;

    public DeviceReconnectionNotificationListener(NotificationService notificationService,
                                                  NetconfConnectionManager netconfConnectionManager) {
        m_notificationService = notificationService;
        m_netconfConnectionManager = netconfConnectionManager;
    }

    public void init() {
        m_netconfConnectionManager.registerDeviceConnectionListener(this);
    }

    public void destroy() {
        m_netconfConnectionManager.unregisterDeviceConnectionListener(this);
    }

    @Override
    public void deviceConnected(Device device, NetconfClientSession clientSession) {
        sendDeviceNotificationsToNbi(device);
    }

    @Override
    public void deviceDisConnected(Device device, NetconfClientSession session) {
        try {
            m_notificationService.sendNotification(NetconfResources.STATE_CHANGE, buildDeviceStateChangeNotification(device, "offline"));
        } catch (NetconfMessageBuilderException e) {
            LOGGER.error("Exception while sending device-state-change notification ", e);
        }
    }

    private void sendDeviceNotificationsToNbi(Device device) {
        Map<String, List<Notification>> notificationsPerStream = new HashMap<>();
        try {
            notificationsPerStream.put(NetconfResources.STATE_CHANGE, Arrays.asList(buildDeviceStateChangeNotification(device, "online")));
        } catch (NetconfMessageBuilderException e) {
            LOGGER.error("Exception while sending device-state-change notification ", e);
        }
        LOGGER.info(String.format("Device %s has notification capability supported, forwards notifications to NBI", device));
        sendNotificationPerStream(notificationsPerStream);
    }

    private void sendNotificationPerStream(Map<String, List<Notification>> notifications) {
        for (Map.Entry<String, List<Notification>> entry : notifications.entrySet()) {
            for (Notification notification : entry.getValue()) {
                if (null != notification) {
                    m_notificationService.sendNotification(entry.getKey(), notification);
                } else {
                    LOGGER.error("There was a null entry in notifications");
                }
            }
        }
    }

    private Notification buildDeviceStateChangeNotification(Device device, String newValue) throws NetconfMessageBuilderException {
        return new DeviceStateChangeNotification(device, newValue);
    }
}
