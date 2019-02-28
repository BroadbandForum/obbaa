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

import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.getNewDocument;

import javax.xml.parsers.ParserConfigurationException;

import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfNotification;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class DeviceStateChangeNotification extends NetconfNotification {

    public static final QName TYPE = QName.create(NetconfResources.NC_STACK_NS, NetconfResources.NC_STATE_CHANGE_NOTIFICATION);
    private static final String ERROR_WHILE_BUILDING_DOCUMENT = "Error while building document ";

    private Document m_doc;
    private Device m_device;
    private String m_event;

    public DeviceStateChangeNotification(Device device, String event) throws NetconfMessageBuilderException {
        m_device = device;
        m_event = event;
        init();
    }

    private void init() throws NetconfMessageBuilderException {
        Element deviceStateChangeElement = getDeviceStateChangeNotificationElement(m_device, m_event);
        setNotificationElement(deviceStateChangeElement);
    }


    public Element getDeviceStateChangeNotificationElement(Device device, String newValue) throws NetconfMessageBuilderException {
        try {
            m_doc = getNewDocument();
            Element eventElement = m_doc.createElementNS(DeviceManagerNSConstants.NS,
                    DeviceManagerNSConstants.EVENT);
            eventElement.setTextContent(newValue);
            Element deviceStateChangeElement = m_doc.createElementNS(DeviceManagerNSConstants.NS,
                    DeviceManagerNSConstants.DEVICE_STATE_CHANGE);
            deviceStateChangeElement.appendChild(eventElement);
            Element deviceNotifElement = m_doc.createElementNS(DeviceManagerNSConstants.NS,
                    DeviceManagerNSConstants.DEVICE_NOTIFICATION);
            deviceNotifElement.appendChild(deviceStateChangeElement);
            Element nameElement = m_doc.createElementNS(DeviceManagerNSConstants.NS,
                    DeviceManagerNSConstants.NAME);
            nameElement.setTextContent(device.getDeviceName());
            Element devicesElement = m_doc.createElementNS(DeviceManagerNSConstants.NS,
                    DeviceManagerNSConstants.DEVICE);
            devicesElement.appendChild(nameElement);
            devicesElement.appendChild(deviceNotifElement);
            Element managedDevicesElement = m_doc.createElementNS(DeviceManagerNSConstants.NS,
                    DeviceManagerNSConstants.MANAGED_DEVICES);
            managedDevicesElement.appendChild(devicesElement);
            Element networkManagerElement = m_doc.createElementNS(DeviceManagerNSConstants.NS,
                    DeviceManagerNSConstants.NETWORK_MANAGER);
            networkManagerElement.appendChild(managedDevicesElement);
            return networkManagerElement;
        } catch (ParserConfigurationException e) {
            throw new NetconfMessageBuilderException(ERROR_WHILE_BUILDING_DOCUMENT, e);
        }
    }

    @Override
    public QName getType() {
        return TYPE;
    }
}
