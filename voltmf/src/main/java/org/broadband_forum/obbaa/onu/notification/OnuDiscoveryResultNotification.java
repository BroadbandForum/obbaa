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

import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.SOFTWARE_IMAGE_HASH;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.SOFTWARE_IMAGE_IS_ACTIVE;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.SOFTWARE_IMAGE_IS_COMMITTED;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.SOFTWARE_IMAGE_IS_VALID;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.SOFTWARE_IMAGE_PRODUCT_CODE;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.SOFWARE_IMAGE_VERSION;

import java.util.Set;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.OnuStateInfo;
import org.broadband_forum.obbaa.dmyang.entities.SoftwareImage;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfNotification;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.onu.ONUConstants;
import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <p>
 * Creates ONU discovery result notification
 * </p>
 * Created by Madhukar.Shetty (Nokia) on 18/06/2021.
 */
public class OnuDiscoveryResultNotification extends NetconfNotification {

    private static final Logger LOGGER = Logger.getLogger(OnuDiscoveryResultNotification.class);
    public static final String ONU_DESCOVERY_RESULT_NS = "urn:bbf:yang:bbf-voltmf-entity";
    public static final String ONU_DESCOVERY_RESULT = "onu-discovery-result";
    public static final String SERIAL_NUMBER = "onu-serial-number";
    public static final String DISCOVERY_RESULT = "discovery-result";
    public static final String SUCCESSFUL = "successful";
    public static final String FAILED_CONNECTIVITY = "failed-connectivity";
    public static final String DEVICE_INFO = "device-info";
    public static final String SOFTWARE_INFO = "software-info";
    public static final String ONU_MANAGEMENT_NS = "urn:bbf:yang:obbaa:onu-management";
    public static final String SOFTWARE_IMAGES = "software-images";
    public static final String SOFTWARE_IMAGE = "software-image";
    public static final String SOFTWARE_IMAGE_ID = "id";

    public static final QName TYPE = QName.create(ONU_DESCOVERY_RESULT_NS, ONU_DESCOVERY_RESULT);

    private final Device m_device;
    private final String m_onuState;

    public OnuDiscoveryResultNotification(Device device, String onuState) throws NetconfMessageBuilderException {
        m_device = device;
        m_onuState = onuState;
        init();
    }

    private void init() {
        Element deviceStateChangeElement = getOnuDiscoveryResultNotificationElement(m_device, m_onuState);
        setNotificationElement(deviceStateChangeElement);
    }

    public Element getOnuDiscoveryResultNotificationElement(Device device, String onuStateInfo) {
        Element onuDiscoveryResultElement = null;
        if (device != null) {
            Document document = DocumentUtils.createDocument();

            onuDiscoveryResultElement = document.createElementNS(ONU_DESCOVERY_RESULT_NS, ONU_DESCOVERY_RESULT);

            Element serialNumber = document.createElementNS(ONU_DESCOVERY_RESULT_NS, SERIAL_NUMBER);
            String serialNum = device.getDeviceManagement().getOnuConfigInfo().getExpectedSerialNumber();
            serialNumber.setTextContent(serialNum);
            onuDiscoveryResultElement.appendChild(serialNumber);

            Element discoveryResult = document.createElementNS(ONU_DESCOVERY_RESULT_NS, DISCOVERY_RESULT);
            String result = FAILED_CONNECTIVITY;
            if (onuStateInfo.equals(ONUConstants.ONLINE)) {
                result = SUCCESSFUL;
            }
            discoveryResult.setTextContent(result);
            onuDiscoveryResultElement.appendChild(discoveryResult);

            OnuStateInfo onuStateInfoDB = device.getDeviceManagement().getDeviceState().getOnuStateInfo();
            if (onuStateInfoDB != null) {
                Element deviceInfo = document.createElementNS(ONU_DESCOVERY_RESULT_NS, DEVICE_INFO);
                deviceInfo.setTextContent(onuStateInfoDB.getEquipmentId());
                onuDiscoveryResultElement.appendChild(deviceInfo);
                Element softwareInfo = document.createElementNS(ONU_DESCOVERY_RESULT_NS, SOFTWARE_INFO);
                softwareInfo.appendChild(buildSoftwareImagesElement(device, document));
                onuDiscoveryResultElement.appendChild(softwareInfo);
            }
        } else {
            LOGGER.error("Failed to send onu discovery result, Device is null");
        }
        return onuDiscoveryResultElement;
    }

    private Element buildSoftwareImagesElement(Device device, Document document) {
        Element softwareImagesElementFinal = null;
        OnuStateInfo onuStateInfo = device.getDeviceManagement().getDeviceState().getOnuStateInfo();
        Set<SoftwareImage> softwareImageSet = onuStateInfo.getSoftwareImages().getSoftwareImage();
        if (softwareImageSet != null) {
            Element softwareImagesElement = document.createElementNS(ONU_MANAGEMENT_NS, SOFTWARE_IMAGES);
            softwareImageSet.forEach(softwareImage -> {
                Element softwareImageElement = document.createElementNS(ONU_MANAGEMENT_NS, SOFTWARE_IMAGE);
                softwareImagesElement.appendChild(softwareImageElement);
                appendElementWithoutNS(document, softwareImageElement, SOFTWARE_IMAGE_ID, String.valueOf(softwareImage.getId()));
                appendElementWithoutNS(document, softwareImageElement, SOFWARE_IMAGE_VERSION,
                        String.valueOf(softwareImage.getVersion()));
                appendElementWithoutNS(document, softwareImageElement, SOFTWARE_IMAGE_IS_COMMITTED,
                        String.valueOf(softwareImage.getIsCommitted()));
                appendElementWithoutNS(document, softwareImageElement, SOFTWARE_IMAGE_IS_ACTIVE,
                        String.valueOf(softwareImage.getIsActive()));
                appendElementWithoutNS(document, softwareImageElement, SOFTWARE_IMAGE_IS_VALID,
                        String.valueOf(softwareImage.getIsValid()));
                appendElementWithoutNS(document, softwareImageElement, SOFTWARE_IMAGE_PRODUCT_CODE,
                        String.valueOf(softwareImage.getProductCode()));
                appendElementWithoutNS(document, softwareImageElement, SOFTWARE_IMAGE_HASH, String.valueOf(softwareImage.getHash()));
            });
            softwareImagesElementFinal = softwareImagesElement;
        }
        return softwareImagesElementFinal;
    }

    private Element appendElementWithoutNS(Document document, Element parentElement, String localName, String... textContents) {
        Element lastElement = null;
        for (String textContent : textContents) {
            Element element = document.createElement(localName);
            element.setTextContent(textContent);
            parentElement.appendChild(element);
            lastElement = element;
        }
        return lastElement;
    }

    @Override
    public QName getType() {
        return TYPE;
    }
}
