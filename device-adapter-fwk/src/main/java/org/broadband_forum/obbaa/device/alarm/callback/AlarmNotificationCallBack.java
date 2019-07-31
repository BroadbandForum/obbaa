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

package org.broadband_forum.obbaa.device.alarm.callback;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.device.alarm.util.AlarmNotificationUtil;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.alarm.api.AlarmInfo;
import org.broadband_forum.obbaa.netconf.alarm.api.AlarmService;
import org.broadband_forum.obbaa.netconf.alarm.entity.AlarmSeverity;
import org.broadband_forum.obbaa.netconf.alarm.util.AlarmUtil;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationCallBack;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.joda.time.DateTime;
import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Element;

public class AlarmNotificationCallBack implements NotificationCallBack {

    private static final String DEVICE_INSTANCE_IDENTIFIER_FORMAT = "baa-network-manager:network-manager/"
            + "baa-network-manager:managed-devices/baa-network-manager:device[baa-network-manager:name='%s']/"
            + "baa-network-manager:root";
    private static final String DEVICE_MANAGER_NAMESPACE = "xmlns:baa-network-manager=\"urn:bbf:yang:obbaa:network-manager\"";
    private static final Logger LOGGER = Logger.getLogger(AlarmNotificationCallBack.class);
    private SchemaRegistry m_adapterSchemaRegistry;
    private AlarmService m_alarmService;
    private String m_alarmModuleNs;
    private QName m_alarmNotificationType;
    private SchemaRegistry m_dmSchemaRegistry;

    public AlarmNotificationCallBack(SchemaRegistry adapterSchemaRegistry, AlarmService alarmService,
                                     String moduleNs, SchemaRegistry dmSchemaRegistry) {
        m_adapterSchemaRegistry = adapterSchemaRegistry;
        m_alarmService = alarmService;
        m_alarmModuleNs = moduleNs;
        m_alarmNotificationType = QName.create(moduleNs, "alarm-notification");
        m_dmSchemaRegistry = dmSchemaRegistry;
    }

    @Override
    public void onNotificationReceived(Notification notification, NotificationContext context, DateTime receivedTime) {
        if (notification.getType().equals(m_alarmNotificationType)) {
            Device device = (Device) context.get(Device.class.getSimpleName());
            LOGGER.info("Received alarmNotificationCallback from device: " + device.getDeviceName()
                    + " for " + notification.notificationToString());
            Element notificationElement = notification.getNotificationElement();
            List<AlarmInfo> alarmInfoList = getAlarmListFromNotification(device, notificationElement);
            AlarmInfo alarmInfo = alarmInfoList.get(0);
            updateAlarmInfoSourceObject(device, Arrays.asList(alarmInfo));
            updateAlarmInfoSourceObjectString(device, Arrays.asList(alarmInfo));
            if (alarmInfo.getSeverity().equals(AlarmSeverity.CLEAR)) {
                LOGGER.debug(String.format("ClearAlarm invoked for the alarm: %s on device %s", alarmInfo, device.getDeviceName()));
                m_alarmService.clearAlarm(alarmInfo);
            } else {
                LOGGER.debug(String.format("RaiseAlarm invoked for the alarm: %s on device %s", alarmInfo, device.getDeviceName()));
                m_alarmService.raiseAlarm(alarmInfo);
            }
        }
    }

    @Override
    public void resynchronize(Object deviceId) {
        //TO-DO
    }

    private void updateAlarmInfoSourceObjectString(Device device, List<AlarmInfo> activeDPUAlarms) {
        String deviceInstanceIdentifier = String.format(DEVICE_INSTANCE_IDENTIFIER_FORMAT, device.getDeviceName());
        for (AlarmInfo alarmInfo : activeDPUAlarms) {
            alarmInfo.setSourceObjectString(deviceInstanceIdentifier + alarmInfo.getSourceObjectString());
            alarmInfo.setSourceObjectNamespaces(alarmInfo.getSourceObjectNamespaces() + " " + DEVICE_MANAGER_NAMESPACE);
        }

    }

    private void updateAlarmInfoSourceObject(Device device, List<AlarmInfo> activeDPUAlarms) {
        String deviceInstanceIdentifier = String.format(DEVICE_INSTANCE_IDENTIFIER_FORMAT, device.getDeviceName());
        for (AlarmInfo alarmInfo : activeDPUAlarms) {
            ModelNodeId mountNodeId = AlarmUtil.toNodeIdUsingRegistry(deviceInstanceIdentifier, m_dmSchemaRegistry);
            if (mountNodeId != null) {
                for (ModelNodeRdn rdn : alarmInfo.getSourceObject().getRdns()) {
                    mountNodeId.addRdn(rdn);
                }
                alarmInfo.setSourceObject(mountNodeId);
            }
        }
    }

    private List<AlarmInfo> getAlarmListFromNotification(Device device, Element notificationElement) {
        if (notificationElement == null) {
            throw new RuntimeException("Could not process alarm since notification is incorrect for device: " + device.getDeviceName());
        }
        return AlarmNotificationUtil.extractAndCreateAlarmEntryFromNotification(
                notificationElement, m_adapterSchemaRegistry, device, m_alarmModuleNs);
    }

}