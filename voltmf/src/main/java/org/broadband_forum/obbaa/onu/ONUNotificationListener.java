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

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.onu.message.MessageFormatter;
import org.broadband_forum.obbaa.onu.notification.ONUNotification;
import org.broadband_forum.obbaa.pma.DeviceNotificationClientListener;
import org.broadband_forum.obbaa.pma.DeviceNotificationListenerService;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * <p>
 * Listens to onu-state-change notifications from OLT
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 22/07/2020.
 */
public class ONUNotificationListener implements DeviceNotificationClientListener {
    private static final Logger LOGGER = Logger.getLogger(ONUNotificationListener.class);
    private final VOLTManagement m_voltMgmt;
    private final DeviceNotificationListenerService m_deviceNotificationService;
    private final MessageFormatter m_messageFormatter;

    public ONUNotificationListener(DeviceNotificationListenerService deviceNotificationService,
                                    VOLTManagement voltMgmt, MessageFormatter messageFormatter) {
        m_deviceNotificationService = deviceNotificationService;
        m_voltMgmt = voltMgmt;
        m_messageFormatter = messageFormatter;

    }

    public void init() {
        m_deviceNotificationService.registerDeviceNotificationClientListener(this);
    }

    public void destroy() {
        m_deviceNotificationService.unregisterDeviceNotificationClientListener(this);
    }

    @Override
    public void deviceNotificationReceived(Device device, Notification notification) {
        QName notificationType = notification.getType();
        if (ONUNotification.isONUStateChangeNotif(notificationType)) {
            try {
                ONUNotification onuNotification = new ONUNotification(notification, device.getDeviceName(),m_messageFormatter);
                m_voltMgmt.onuNotificationProcess(onuNotification, device.getDeviceName());
            } catch (NetconfMessageBuilderException exception) {
                LOGGER.error(String.format("Could not process ONU notification for device %s", device.getDeviceName()), exception);
            }
        }
    }
}
