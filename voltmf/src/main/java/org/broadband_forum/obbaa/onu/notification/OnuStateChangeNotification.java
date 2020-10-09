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

package org.broadband_forum.obbaa.onu.notification;

import javax.xml.parsers.ParserConfigurationException;

import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfNotification;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <p>
 * Creates ONU state change notification
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 22/07/2020.
 */
public class OnuStateChangeNotification extends NetconfNotification {

    public static final String ONU_STATE_CHANGE_NS = "urn:bbf:yang:bbf-xpon-onu-states";
    public static final String ONU_STATE_CHANGE_NOTIFICATION = "onu-state-change";
    public static final String ONU_STATE_CHANGE_PREFIX = "bbf-xpon-onu-states";
    public static final String SERIAL_NUMBER = "detected-serial-number";
    public static final String ONU_NAME = "onu-name";
    public static final String SOFTWARE_VERSION = "software-version";
    public static final String ONU_STATE = "onu-state";
    public static final String ONU_ID = "onu-id";
    public static final String CHANNEL_TERMINATION_REF = "channel-termination-ref";
    public static final String REGISTRATION_ID = "detected-registration-id";
    public static final String VANI_REF = "v-ani-ref";

    public static final QName TYPE = QName.create(ONU_STATE_CHANGE_NS, ONU_STATE_CHANGE_NOTIFICATION);
    private static final String ERROR_WHILE_BUILDING_DOCUMENT = "Error while building document ";

    private Document m_doc;
    private Device m_device;
    private String m_onuState;

    public OnuStateChangeNotification(Device device, String onuState) throws NetconfMessageBuilderException {
        m_device = device;
        m_onuState = onuState;
        init();
    }

    private void init() throws NetconfMessageBuilderException {
        Element deviceStateChangeElement = getOnuStateChangeNotificationElement(m_device, m_onuState);
        setNotificationElement(deviceStateChangeElement);
    }


    public Element getOnuStateChangeNotificationElement(Device device, String onuStateInfo) throws NetconfMessageBuilderException {
        try {
            m_doc = DocumentUtils.getNewDocument();
            Element onuStateChangeElement = m_doc.createElementNS(ONU_STATE_CHANGE_NS,
                    buildLocalName(ONU_STATE_CHANGE_PREFIX, ONU_STATE_CHANGE_NOTIFICATION));

            Element onuName = m_doc.createElementNS(ONU_STATE_CHANGE_NS, ONU_NAME);
            onuName.setTextContent(device.getDeviceName());
            onuStateChangeElement.appendChild(onuName);

            Element onuState = m_doc.createElementNS(ONU_STATE_CHANGE_NS, ONU_STATE);
            onuState.setTextContent(onuStateInfo);
            onuStateChangeElement.appendChild(onuState);

            Element serialNumber = m_doc.createElementNS(ONU_STATE_CHANGE_NS, SERIAL_NUMBER);
            String serialNum = device.getDeviceManagement().getOnuConfigInfo().getSerialNumber();
            serialNumber.setTextContent(serialNum);
            onuStateChangeElement.appendChild(serialNumber);

            Element onuId = m_doc.createElementNS(ONU_STATE_CHANGE_NS, ONU_ID);
            String onuID = device.getDeviceManagement().getOnuConfigInfo().getAttachmentPoint().getOnuId();
            onuId.setTextContent(onuID);
            onuStateChangeElement.appendChild(onuId);

            Element channelTermRef = m_doc.createElementNS(ONU_STATE_CHANGE_NS, CHANNEL_TERMINATION_REF);
            String chnTermRef = device.getDeviceManagement().getOnuConfigInfo().getAttachmentPoint().getChannelPartition();
            channelTermRef.setTextContent(chnTermRef);
            onuStateChangeElement.appendChild(channelTermRef);

            Element regId = m_doc.createElementNS(ONU_STATE_CHANGE_NS, REGISTRATION_ID);
            String expectedRegId = device.getDeviceManagement().getOnuConfigInfo().getRegistrationId();
            regId.setTextContent(expectedRegId);
            onuStateChangeElement.appendChild(regId);

            Element vaniRef = m_doc.createElementNS(ONU_STATE_CHANGE_NS, VANI_REF);
            String vani = "onu_25"; //Hardcoding for testing. Retrieve from device
            vaniRef.setTextContent(vani);
            onuStateChangeElement.appendChild(serialNumber);

            Element softwareVer = m_doc.createElementNS(ONU_STATE_CHANGE_NS, SOFTWARE_VERSION);
            String swVer = "1.0"; //Hardcoding for testing. Retrieve from device
            softwareVer.setTextContent(swVer);
            onuStateChangeElement.appendChild(softwareVer);

            return onuStateChangeElement;
        } catch (ParserConfigurationException e) {
            throw new NetconfMessageBuilderException(ERROR_WHILE_BUILDING_DOCUMENT, e);
        }
    }

    protected String buildLocalName(String prefix, String localName) {
        if (prefix == null) {
            return localName;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append(":").append(localName);
        return sb.toString();
    }

    @Override
    public QName getType() {
        return TYPE;
    }
}
