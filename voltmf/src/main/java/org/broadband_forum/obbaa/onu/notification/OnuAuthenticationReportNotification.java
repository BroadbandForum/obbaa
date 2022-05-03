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

import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.ONU_MANAGEMENT_NS;
import static org.broadband_forum.obbaa.onu.ONUConstants.CHANNEL_TERMINATION_REF_JSON_KEY;
import static org.broadband_forum.obbaa.onu.ONUConstants.DETECTED_REGISTARTION_ID;
import static org.broadband_forum.obbaa.onu.ONUConstants.DETECTED_SERIAL_NUMBER;
import static org.broadband_forum.obbaa.onu.ONUConstants.OLT_LOCAL_ONU_NAME;
import static org.broadband_forum.obbaa.onu.ONUConstants.OLT_NAME_JSON_KEY;
import static org.broadband_forum.obbaa.onu.ONUConstants.ONU_AUTHENTICATION_REPORT_NOTIFICATION;
import static org.broadband_forum.obbaa.onu.ONUConstants.ONU_AUTHENTICATION_STATUS;
import static org.broadband_forum.obbaa.onu.ONUConstants.ONU_DEVICE_TYPE;
import static org.broadband_forum.obbaa.onu.ONUConstants.ONU_ID_JSON_KEY;
import static org.broadband_forum.obbaa.onu.ONUConstants.ONU_NAME_JSON_KEY;
import static org.broadband_forum.obbaa.onu.ONUConstants.V_ANI_NAME;

import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfNotification;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.TxService;
import org.broadband_forum.obbaa.nm.devicemanager.DeviceManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <p>
 * Creates Onu Authentication Report notification
 * </p>
 * Created by Madhukar.Shetty (Nokia) on 18/12/2021.
 */
public class OnuAuthenticationReportNotification extends NetconfNotification {

    String m_onuAuthStatus = null;
    Device m_device;
    ONUNotification m_onuNotification;
    NetconfConnectionManager m_connectionManager;
    DeviceManager m_deviceManager;
    private final TxService m_txService;
    String m_oltOnuName;


    public OnuAuthenticationReportNotification(Device device, ONUNotification onuNotification,
                                               NetconfConnectionManager netconfConnectionManager, String onuAuthStatus,
                                               DeviceManager deviceManager, TxService txService, String oltOnuName) {
        m_device = device;
        m_onuNotification = onuNotification;
        m_connectionManager = netconfConnectionManager;
        m_onuAuthStatus = onuAuthStatus;
        m_deviceManager = deviceManager;
        m_txService = txService;
        m_oltOnuName = oltOnuName;
        init();
    }

    private void init() {
        Element deviceStateChangeElement = null;
        if (m_device != null) {
            deviceStateChangeElement = getOnuAuthenticationReportNotificationElement(m_device);
        } else {
            deviceStateChangeElement = getOnuAuthenticationReportNotificationElement(m_onuNotification);
        }
        setNotificationElement(deviceStateChangeElement);
    }

    public Element getOnuAuthenticationReportNotificationElement(Device device) {
        Element onuAuthenticationReportElement = null;
        if (device != null && device.getDeviceManagement().getDeviceType().equals(ONU_DEVICE_TYPE)
                && device.isMediatedSession()) {
            Document document = DocumentUtils.createDocument();
            onuAuthenticationReportElement = document.createElementNS(ONU_MANAGEMENT_NS, ONU_AUTHENTICATION_REPORT_NOTIFICATION);
            Element onuAuthenticationStatusElement = document.createElementNS(ONU_MANAGEMENT_NS, ONU_AUTHENTICATION_STATUS);

            appendChildToElement(onuAuthenticationReportElement, onuAuthenticationStatusElement, m_onuAuthStatus);

            Element oltNameElement = document.createElementNS(ONU_MANAGEMENT_NS, OLT_NAME_JSON_KEY);
            String oltName = device.getDeviceManagement().getDeviceState().getOnuStateInfo()
                    .getActualAttachmentPoint().getOltName();
            appendChildToElement(onuAuthenticationReportElement, oltNameElement, oltName);

            Element channelTerminationRefElement = document.createElementNS(ONU_MANAGEMENT_NS,
                    CHANNEL_TERMINATION_REF_JSON_KEY);
            String ctRef = device.getDeviceManagement().getDeviceState().getOnuStateInfo()
                    .getActualAttachmentPoint().getChannelTerminationRef();
            appendChildToElement(onuAuthenticationReportElement, channelTerminationRefElement, ctRef);

            Element detectedSerialNumberElement = document.createElementNS(ONU_MANAGEMENT_NS, DETECTED_SERIAL_NUMBER);
            String serialNumber = device.getDeviceManagement().getOnuConfigInfo().getExpectedSerialNumber();
            appendChildToElement(onuAuthenticationReportElement, detectedSerialNumberElement, serialNumber);

            Element detectedRegistrationIdElement = document.createElementNS(ONU_MANAGEMENT_NS, DETECTED_REGISTARTION_ID);
            String registrationId = device.getDeviceManagement().getOnuConfigInfo().getExpectedRegistrationId();
            appendChildToElement(onuAuthenticationReportElement, detectedRegistrationIdElement, registrationId);

            Element onuIdElement = document.createElementNS(ONU_MANAGEMENT_NS, ONU_ID_JSON_KEY);
            String onuId = device.getDeviceManagement().getDeviceState().getOnuStateInfo().getActualAttachmentPoint().getOnuId();
            appendChildToElement(onuAuthenticationReportElement, onuIdElement, onuId);

            Element vaniNameElement = document.createElementNS(ONU_MANAGEMENT_NS, V_ANI_NAME);
            String vaniName = device.getDeviceManagement().getDeviceState().getOnuStateInfo().getActualAttachmentPoint().getvAniName();
            appendChildToElement(onuAuthenticationReportElement, vaniNameElement, vaniName);

            Element oltLocalOnuNameElement = document.createElementNS(ONU_MANAGEMENT_NS, OLT_LOCAL_ONU_NAME);
            String oltLocalOnuName = null;
            if (device.getDeviceManagement().getDeviceType().equalsIgnoreCase(ONU_DEVICE_TYPE)) {
                oltLocalOnuName = device.getDeviceName();
                appendChildToElement(onuAuthenticationReportElement, oltLocalOnuNameElement, oltLocalOnuName);
            }

            Element onuNameElement = document.createElementNS(ONU_MANAGEMENT_NS, ONU_NAME_JSON_KEY);
            String onuName = null;
            if (device.getDeviceManagement().getDeviceType().equalsIgnoreCase(ONU_DEVICE_TYPE)) {
                onuName = device.getDeviceName();
                appendChildToElement(onuAuthenticationReportElement, onuNameElement, onuName);
            }
        }
        return onuAuthenticationReportElement;
    }

    public Element getOnuAuthenticationReportNotificationElement(ONUNotification notification) {
        Element onuAuthenticationReportElement = null;
        if (notification != null) {

            Document document = DocumentUtils.createDocument();
            onuAuthenticationReportElement = document.createElementNS(ONU_MANAGEMENT_NS, ONU_AUTHENTICATION_REPORT_NOTIFICATION);
            Element onuAuthenticationStatusElement = document.createElementNS(ONU_MANAGEMENT_NS, ONU_AUTHENTICATION_STATUS);

            appendChildToElement(onuAuthenticationReportElement, onuAuthenticationStatusElement, m_onuAuthStatus);

            Element oltNameElement = document.createElementNS(ONU_MANAGEMENT_NS, OLT_NAME_JSON_KEY);
            String oltName = notification.getOltDeviceName();
            appendChildToElement(onuAuthenticationReportElement, oltNameElement, oltName);

            Element channelTerminationRefElement = document.createElementNS(ONU_MANAGEMENT_NS, CHANNEL_TERMINATION_REF_JSON_KEY);
            String ctRef = notification.getChannelTermRef();
            appendChildToElement(onuAuthenticationReportElement, channelTerminationRefElement, ctRef);

            Element onuIdElement = document.createElementNS(ONU_MANAGEMENT_NS, ONU_ID_JSON_KEY);
            String idONU = notification.getOnuId();
            appendChildToElement(onuAuthenticationReportElement, onuIdElement, idONU);

            Element detectedSerialNumberElement = document.createElementNS(ONU_MANAGEMENT_NS, DETECTED_SERIAL_NUMBER);
            String serialNumber = notification.getSerialNo();
            appendChildToElement(onuAuthenticationReportElement, detectedSerialNumberElement, serialNumber);

            Element detectedRegistrationIdElement = document.createElementNS(ONU_MANAGEMENT_NS, DETECTED_REGISTARTION_ID);
            String registrationId = notification.getRegId();
            appendChildToElement(onuAuthenticationReportElement, detectedRegistrationIdElement, registrationId);

            Element vaniNameElement = document.createElementNS(ONU_MANAGEMENT_NS, V_ANI_NAME);
            String vaniName = notification.getVAniRef();
            appendChildToElement(onuAuthenticationReportElement, vaniNameElement, vaniName);

            Element oltLocalOnuNameElement = document.createElementNS(ONU_MANAGEMENT_NS, OLT_LOCAL_ONU_NAME);
            appendChildToElement(onuAuthenticationReportElement, oltLocalOnuNameElement, m_oltOnuName);
        }
        return onuAuthenticationReportElement;
    }

    private void appendChildToElement(Element parentElement, Element childElement, String childElementValue) {
        if (childElementValue != null && !childElementValue.isEmpty()) {
            childElement.setTextContent(childElementValue);
            parentElement.appendChild(childElement);
        }
    }
}