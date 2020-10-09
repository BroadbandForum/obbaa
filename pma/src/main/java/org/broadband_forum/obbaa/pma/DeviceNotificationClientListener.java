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

package org.broadband_forum.obbaa.pma;

import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;

/**
 * Apps interested in device specific notifications (VONU Management) use this interface to register
 * with the DeviceNotificationListener via DeviceNotificationListenerService, and provide specific
 * handling for a device notification.
 */
public interface DeviceNotificationClientListener {
    /**
     * @param device device for which notification is received.
     * @param notification notification received.
     */
    void deviceNotificationReceived(Device device, Notification notification);
}