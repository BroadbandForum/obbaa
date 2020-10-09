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

import java.util.Date;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.connectors.sbi.netconf.ConnectionListener;
import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.device.adapter.AdapterContext;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.device.adapter.AdapterUtils;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapterId;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.pma.DeviceNotificationListenerService;
import org.broadband_forum.obbaa.pma.PmaRegistry;
import org.joda.time.DateTime;

public class DeviceReconnectionNotificationAction implements ConnectionListener {

    private static final Logger LOGGER = Logger.getLogger(DeviceReconnectionNotificationAction.class);
    private final PmaRegistry m_pmaRegistry;
    private final DeviceNotificationListenerService m_deviceNotificationListenerRegistry;

    private NotificationService m_notificationService;

    private NetconfConnectionManager m_netconfConnectionManager;
    private AdapterManager m_adapterManager;

    public DeviceReconnectionNotificationAction(NotificationService notificationService,
                                                NetconfConnectionManager netconfConnectionManager, AdapterManager adapterManager,
                                                PmaRegistry pmaRegistry,
                                                DeviceNotificationListenerService deviceNotificationClientListenerRegistry) {
        m_notificationService = notificationService;
        m_netconfConnectionManager = netconfConnectionManager;
        m_adapterManager = adapterManager;
        m_pmaRegistry = pmaRegistry;
        m_deviceNotificationListenerRegistry = deviceNotificationClientListenerRegistry;
    }

    public void init() {
        m_netconfConnectionManager.registerDeviceConnectionListener(this);
    }

    public void destroy() {
        m_netconfConnectionManager.unregisterDeviceConnectionListener(this);
    }

    @Override
    public void deviceConnected(Device device, NetconfClientSession clientSession) {
        if (m_notificationService.isNotificationSupported(clientSession)) {
            try {
                if (m_notificationService.isReplaySupported(clientSession)) {
                    createSubscriptionToDevice(clientSession, device, true);
                } else {
                    LOGGER.debug("Create subscription to device doesn't support replay");
                    createSubscriptionToDevice(clientSession, device, false);
                }
            } catch (NetconfMessageBuilderException | InterruptedException | ExecutionException e) {
                LOGGER.error("Device can't get replay supported attribute in Netconf Stream", e);
            }
        } else {
            LOGGER.info(String.format("Device %s doesn't support for notification function", device));
        }
    }

    @Override
    public void deviceDisConnected(Device device, NetconfClientSession session) {
        LOGGER.info(String.format("Device %s disconnected closing subscription %s", device, session));
    }

    private void createSubscriptionToDevice(NetconfClientSession clientSession, final Device device, boolean isReplaySupported) throws
            NetconfMessageBuilderException, InterruptedException, ExecutionException {
        NetConfResponse response = null;
        DeviceAdapterId deviceAdapterId = new DeviceAdapterId(device.getDeviceManagement().getDeviceType(),
                device.getDeviceManagement().getDeviceInterfaceVersion(), device.getDeviceManagement().getDeviceModel(),
                device.getDeviceManagement().getDeviceVendor());
        AdapterContext adapterContext = AdapterUtils.getAdapterContext(device, m_adapterManager);

        DeviceNotificationListener subscriber = new DeviceNotificationListener(device, deviceAdapterId, m_notificationService,
                adapterContext, m_pmaRegistry, m_deviceNotificationListenerRegistry.getDeviceNotificationClientListeners());

        Date timeOfLastSentEvent = NetconfResources.parseDateTime("1970-01-01T00:00:00+00:00").toDate();
        String timeFormat = NetconfResources.DATE_TIME_WITH_TZ_WITHOUT_MS.print(new DateTime(timeOfLastSentEvent));
        response = m_notificationService
                .createSubscriptionWithCallback(clientSession, timeFormat,
                        subscriber, device, isReplaySupported);
        if (response.isOk()) {
            LOGGER.info("Create Subscription Successful");
        }
    }
}
