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

package org.broadband_forum.obbaa.onu.util;

import static org.broadband_forum.obbaa.onu.ONUConstants.AUTH_SCCESSFULL;
import static org.broadband_forum.obbaa.onu.ONUConstants.BAA_XPON_ONU_AUTH;
import static org.broadband_forum.obbaa.onu.ONUConstants.BAA_XPON_ONU_TYPES_WITH_XMLNS;
import static org.broadband_forum.obbaa.onu.ONUConstants.CHANNEL_TERMINATION;
import static org.broadband_forum.obbaa.onu.ONUConstants.CHANNEL_TERMINATION_NS;
import static org.broadband_forum.obbaa.onu.ONUConstants.IETF_INTERFACES_NS;
import static org.broadband_forum.obbaa.onu.ONUConstants.IF_PREFIX;
import static org.broadband_forum.obbaa.onu.ONUConstants.INTERFACE;
import static org.broadband_forum.obbaa.onu.ONUConstants.INTERFACES_STATE;
import static org.broadband_forum.obbaa.onu.ONUConstants.NAME;
import static org.broadband_forum.obbaa.onu.ONUConstants.ONU_AUTHENTICATION_NS;
import static org.broadband_forum.obbaa.onu.ONUConstants.ONU_AUTHENTICATION_REPORT;
import static org.broadband_forum.obbaa.onu.ONUConstants.ONU_MGMT_MODE_NS;
import static org.broadband_forum.obbaa.onu.ONUConstants.ONU_NAME;
import static org.broadband_forum.obbaa.onu.ONUConstants.REQUESTED_ONU_MGMT_MODE;
import static org.broadband_forum.obbaa.onu.ONUConstants.ROOT;
import static org.broadband_forum.obbaa.onu.ONUConstants.SERIAL_NUMBER_ONU;
import static org.broadband_forum.obbaa.onu.ONUConstants.V_ANI;

import java.util.HashMap;

import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.onu.NotificationRequest;
import org.broadband_forum.obbaa.onu.ONUConstants;
import org.broadband_forum.obbaa.onu.entity.UnknownONU;
import org.broadband_forum.obbaa.onu.notification.ONUNotification;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public final class VOLTMgmtRequestCreationUtil {

    private VOLTMgmtRequestCreationUtil() {
        //Not Called
    }

    public static ActionRequest prepareCreateOnuRequest(String onuDeviceName, boolean isMsgToProxy) {
        Document document = DocumentUtils.createDocument();
        String elementNS = ONUConstants.BBF_VOMCI_FUNCTION_NS;
        if (isMsgToProxy) {
            elementNS = ONUConstants.BBF_VOMCI_PROXY_NS;
        }
        Element managedOnus = document.createElementNS(elementNS, ONUConstants.MANAGED_ONUS);
        Element nameLeaf = document.createElementNS(elementNS, ONUConstants.NAME);
        nameLeaf.setTextContent(onuDeviceName);
        Element createOnuNode = document.createElementNS(elementNS, ONUConstants.CREATE_ONU);
        createOnuNode.appendChild(nameLeaf);
        managedOnus.appendChild(createOnuNode);
        document.appendChild(managedOnus);
        ActionRequest request = new ActionRequest();
        request.setActionTreeElement(document.getDocumentElement());
        return request;
    }

    public static ActionRequest prepareDeleteOnuRequest(String onuDeviceName, boolean isMsgToProxy) {
        Document document = DocumentUtils.createDocument();
        String elementNS = ONUConstants.BBF_VOMCI_FUNCTION_NS;
        if (isMsgToProxy) {
            elementNS = ONUConstants.BBF_VOMCI_PROXY_NS;
        }
        Element managedOnus = document.createElementNS(elementNS, ONUConstants.MANAGED_ONUS);
        Element managedOnu = document.createElementNS(elementNS, ONUConstants.MANAGED_ONU);
        Element nameLeaf = document.createElementNS(elementNS, ONUConstants.NAME);
        nameLeaf.setTextContent(onuDeviceName);
        managedOnu.appendChild(nameLeaf);
        managedOnus.appendChild(managedOnu);
        Element deleteOnuNode = document.createElementNS(elementNS, ONUConstants.DELETE_ONU);
        managedOnu.appendChild(deleteOnuNode);
        document.appendChild(managedOnus);
        ActionRequest request = new ActionRequest();
        request.setActionTreeElement(document.getDocumentElement());
        return request;
    }

    public static ActionRequest prepareSetOnuCommunicationRequest(String onuDeviceName, boolean isCommunicationAvailable, String oltName,
                                                                  String channelTermName, String onuId, String voltmfRemoteEpName,
                                                                  String oltRemoteEpName, boolean isMsgToProxy) {
        Document document = DocumentUtils.createDocument();
        String elementNS = ONUConstants.BBF_VOMCI_FUNCTION_NS;
        if (isMsgToProxy) {
            elementNS = ONUConstants.BBF_VOMCI_PROXY_NS;
        }
        Element managedOnus = document.createElementNS(elementNS, ONUConstants.MANAGED_ONUS);
        Element managedOnu = document.createElementNS(elementNS, ONUConstants.MANAGED_ONU);
        Element nameLeaf = document.createElementNS(elementNS, ONUConstants.NAME);
        nameLeaf.setTextContent(onuDeviceName);
        managedOnu.appendChild(nameLeaf);
        managedOnus.appendChild(managedOnu);
        Element setOnuCommunication = document.createElementNS(elementNS, ONUConstants.SET_ONU_COMMUNICATION);
        managedOnu.appendChild(setOnuCommunication);
        appendElement(elementNS, document, setOnuCommunication, ONUConstants.SET_ONU_COMM_AVAILABLE,
                String.valueOf(isCommunicationAvailable));
        appendElement(elementNS, document, setOnuCommunication, ONUConstants.OLT_REMOTE_NAME, oltRemoteEpName);
        String nbRemoteEndpoint;
        if (isMsgToProxy) {
            nbRemoteEndpoint = ONUConstants.VOMCI_FUNCTION_REMOTE_ENDPOINT;
        } else {
            nbRemoteEndpoint = ONUConstants.VOLTMF_REMOTE_NAME;
        }
        appendElement(elementNS, document, setOnuCommunication, nbRemoteEndpoint, voltmfRemoteEpName);
        Element onuAttachmentPoint = document.createElementNS(elementNS, ONUConstants.ONU_ATTACHMENT_POINT);
        appendElement(elementNS, document, onuAttachmentPoint, ONUConstants.OLT_NAME_JSON_KEY, oltName);
        appendElement(elementNS, document, onuAttachmentPoint, ONUConstants.CHANNEL_TERMINATION_NAME, channelTermName);
        appendElement(elementNS, document, onuAttachmentPoint, ONUConstants.ONU_ID_JSON_KEY, onuId);
        setOnuCommunication.appendChild(onuAttachmentPoint);
        document.appendChild(managedOnus);
        ActionRequest request = new ActionRequest();
        request.setActionTreeElement(document.getDocumentElement());
        return request;
    }

    public static ActionRequest prepareOnuAuthenicationReportActionRequest(String onuDeviceName, boolean isAuthSuccessful, String vaniName,
                                                 String requestedOnuMgmtMode, String onuSerialNumber, String channelTerminationName) {
        Document document = DocumentUtils.createDocument();

        Element interfacesState = document.createElementNS(IETF_INTERFACES_NS, INTERFACES_STATE);
        interfacesState.setPrefix(IF_PREFIX);
        Element intf = document.createElementNS(IETF_INTERFACES_NS, INTERFACE);
        intf.setPrefix(IF_PREFIX);
        appendElementWithPrefix(IETF_INTERFACES_NS, document, intf, NAME, IF_PREFIX, channelTerminationName);
        Element channelTermination = document.createElementNS(CHANNEL_TERMINATION_NS, CHANNEL_TERMINATION);
        document.appendChild(interfacesState);
        interfacesState.appendChild(intf);
        intf.appendChild(channelTermination);

        Element onuAuthReport = document.createElementNS(ONU_AUTHENTICATION_NS, ONU_AUTHENTICATION_REPORT);
        onuAuthReport.setPrefix(BAA_XPON_ONU_AUTH);
        channelTermination.appendChild(onuAuthReport);
        appendElementWithPrefix(ONU_AUTHENTICATION_NS, document, onuAuthReport, SERIAL_NUMBER_ONU, BAA_XPON_ONU_AUTH,
                onuSerialNumber);
        appendElementWithPrefix(ONU_AUTHENTICATION_NS, document, onuAuthReport, AUTH_SCCESSFULL, BAA_XPON_ONU_AUTH,
                String.valueOf(isAuthSuccessful));
        appendElementWithPrefix(ONU_AUTHENTICATION_NS, document, onuAuthReport, V_ANI, BAA_XPON_ONU_AUTH, vaniName);
        appendElementWithPrefix(ONU_AUTHENTICATION_NS, document, onuAuthReport, ONU_NAME, BAA_XPON_ONU_AUTH, onuDeviceName);
        appendElementWithxmlnsPrefix(ONU_MGMT_MODE_NS, document, onuAuthReport, REQUESTED_ONU_MGMT_MODE, BAA_XPON_ONU_AUTH,
                BAA_XPON_ONU_TYPES_WITH_XMLNS, requestedOnuMgmtMode);

        ActionRequest request = new ActionRequest();
        request.setActionTreeElement(document.getDocumentElement());
        return request;
    }

    public static Element appendElement(String elementNS, Document document, Element parentElement, String localName,
                                        String... textContents) {
        Element lastElement = null;
        for (String textContent : textContents) {
            Element element = document.createElementNS(elementNS, localName);
            element.setTextContent(textContent);
            parentElement.appendChild(element);
            lastElement = element;
        }
        return lastElement;
    }

    public static Element appendElementWithPrefix(String elementNS, Document document, Element parentElement, String localName,
                                                  String prefix, String... textContents) {
        Element lastElement = null;
        for (String textContent : textContents) {
            Element element = document.createElementNS(elementNS, localName);
            element.setTextContent(textContent);
            element.setPrefix(prefix);
            parentElement.appendChild(element);
            lastElement = element;
        }
        return lastElement;
    }

    public static Element appendElementWithxmlnsPrefix(String elementNS, Document document, Element parentElement, String localName,
                                                       String prefix, String xmlnsPrefix, String... textContents) {
        Element lastElement = null;
        for (String textContent : textContents) {
            Element element = document.createElement(prefix + ":" + localName);
            element.setAttribute(xmlnsPrefix, elementNS);
            element.setTextContent(textContent);
            parentElement.appendChild(element);
            lastElement = element;
        }
        return lastElement;
    }

    //VOMCI functions is currently not supporting GET request handling.
    public static GetRequest prepareInternalGetRequest(String onuDeviceName) {
        Document document = DocumentUtils.createDocument();

        Element onuManager = document.createElementNS(ONUConstants.NETWORK_MANAGER_NAMESPACE, ONUConstants.NETWORK_MANAGER);
        Element managedDevices = document.createElementNS(ONUConstants.NETWORK_MANAGER_NAMESPACE, ONUConstants.MANAGED_DEVICES);
        onuManager.appendChild(managedDevices);

        Element device = document.createElementNS(ONUConstants.NETWORK_MANAGER_NAMESPACE, ONUConstants.DEVICE);
        managedDevices.appendChild(device);
        Element name = document.createElementNS(ONUConstants.NETWORK_MANAGER_NAMESPACE, ONUConstants.NAME);
        device.appendChild(name);
        name.setTextContent(onuDeviceName);

        Element root = document.createElementNS(ONUConstants.NETWORK_MANAGER_NAMESPACE, ROOT);
        device.appendChild(root);


        Element hardwareState = document.createElementNS(ONUConstants.IETF_HARDWARE_NS, ONUConstants.HARDWARE_STATE);
        Element component = document.createElementNS(ONUConstants.IETF_HARDWARE_NS, ONUConstants.COMPONENT);
        hardwareState.appendChild(component);
        root.appendChild(hardwareState);

        Element hardware = document.createElementNS(ONUConstants.IETF_HARDWARE_NS, ONUConstants.HARDWARE);
        Element hardwareComponent = document.createElementNS(ONUConstants.IETF_HARDWARE_NS, ONUConstants.COMPONENT);
        root.appendChild(hardware);
        hardware.appendChild(hardwareComponent);
        Element componentName = document.createElementNS(ONUConstants.IETF_HARDWARE_NS, ONUConstants.NAME);
        componentName.setTextContent(onuDeviceName);
        Element software = document.createElementNS(ONUConstants.BBF_SW_MGMT_NS, ONUConstants.SOFTWARE);
        hardwareComponent.appendChild(componentName);
        hardwareComponent.appendChild(software);

        document.appendChild(onuManager);

        NetconfFilter requestFilter = new NetconfFilter();
        requestFilter.setType(ONUConstants.SUBTREE_FILTER);
        requestFilter.addXmlFilter(document.getDocumentElement());

        GetRequest getRequest = new GetRequest();
        getRequest.setFilter(requestFilter);

        return getRequest;
    }

    public static GetRequest prepareGetRequestForVani(String oltName, String vaniNAme) {

        Document document = DocumentUtils.createDocument();

        Element interfaces = document.createElementNS(ONUConstants.IETF_INTERFACES_NS, ONUConstants.INTERFACES);
        interfaces.setPrefix(IF_PREFIX);

        Element interfaceElement = document.createElementNS(ONUConstants.IETF_INTERFACES_NS, ONUConstants.INTERFACE);
        interfaceElement.setPrefix(IF_PREFIX);
        interfaces.appendChild(interfaceElement);

        Element nameElement = document.createElementNS(ONUConstants.IETF_INTERFACES_NS, ONUConstants.NAME);
        nameElement.setTextContent(vaniNAme);
        nameElement.setPrefix(IF_PREFIX);
        interfaceElement.appendChild(nameElement);

        document.appendChild(interfaces);

        GetRequest getRequest = new GetRequest();
        NetconfFilter netconfFilter = new NetconfFilter();
        netconfFilter.setType(ONUConstants.SUBTREE_FILTER);
        netconfFilter.addXmlFilter(document.getDocumentElement());
        getRequest.setFilter(netconfFilter);

        return getRequest;
    }

    public static NotificationRequest prepareUndetectKafkaMessage(Device onuDevice, ONUNotification notification,
                                                                  HashMap<String, String> labels) {
        String notificationEvent = ONUConstants.UNDETECT_EVENT;
        NotificationRequest request = new NotificationRequest(onuDevice.getDeviceName(), notification.getOltDeviceName(),
                notification.getChannelTermRef(), notification.getOnuId(), notificationEvent, labels);
        return request;
    }

    public static NotificationRequest prepareDetectForPreconfiguredDevice(String deviceName, UnknownONU unknownONU,
                                                                          HashMap<String, String> labels) {
        String notificationEvent = ONUConstants.DETECT_EVENT;
        NotificationRequest request = new NotificationRequest(deviceName, unknownONU.getOltDeviceName(), unknownONU.getChannelTermRef(),
                unknownONU.getOnuId(), notificationEvent, labels);
        return request;
    }

    public static NotificationRequest prepareDetectForPreconfiguredDevice(String deviceName, ONUNotification onuNotification,
                                                                          HashMap<String, String> labels) {
        String notificationEvent = ONUConstants.DETECT_EVENT;
        NotificationRequest request = new NotificationRequest(deviceName, onuNotification.getOltDeviceName(),
                onuNotification.getChannelTermRef(), onuNotification.getOnuId(), notificationEvent, labels);
        return request;
    }

    public static HashMap<String, String> getLabels(Device device) {
        HashMap<String, String> labels = new HashMap<>();
        labels.put("name", "vendor");
        labels.put("value", device.getDeviceManagement().getDeviceVendor());
        return labels;
    }
}
