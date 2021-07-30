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

import java.util.HashMap;

import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
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

    public static NetconfRpcRequest prepareCreateOnuRequest(String onuDeviceName, boolean isMsgToProxy) {
        Document document = DocumentUtils.createDocument();
        String elementNS = ONUConstants.BBF_VOMCI_FUNCTION_NS;
        if (isMsgToProxy) {
            elementNS = ONUConstants.BBF_VOMCI_PROXY_NS;
        }
        Element createOnuNode = document.createElementNS(elementNS, ONUConstants.CREATE_ONU);
        Element nameLeaf = document.createElement(ONUConstants.NAME);
        nameLeaf.setTextContent(onuDeviceName);
        createOnuNode.appendChild(nameLeaf);
        document.appendChild(createOnuNode);
        NetconfRpcRequest request = new NetconfRpcRequest();
        request.setRpcInput(document.getDocumentElement());
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

    //VOMCI functions is currently not supporting GET request handling.
    public static GetRequest prepareInternalGetRequest(String deviceName) {
        Document document = DocumentUtils.createDocument();

        Element onuManager = document.createElementNS(ONUConstants.NETWORK_MANAGER_NAMESPACE, ONUConstants.NETWORK_MANAGER);
        Element managedDevices = document.createElementNS(ONUConstants.NETWORK_MANAGER_NAMESPACE, ONUConstants.MANAGED_DEVICES);
        onuManager.appendChild(managedDevices);

        Element device = document.createElementNS(ONUConstants.NETWORK_MANAGER_NAMESPACE, ONUConstants.DEVICE);
        managedDevices.appendChild(device);
        Element name = document.createElementNS(ONUConstants.NETWORK_MANAGER_NAMESPACE, ONUConstants.NAME);
        device.appendChild(name);
        name.setTextContent(deviceName);

        Element deviceManagement = document.createElementNS(ONUConstants.NETWORK_MANAGER_NAMESPACE, ONUConstants.DEVICE_MANAGEMENT);
        Element deviceState = document.createElementNS(ONUConstants.NETWORK_MANAGER_NAMESPACE, ONUConstants.DEVICE_STATE);
        device.appendChild(deviceManagement);
        deviceManagement.appendChild(deviceState);

        Element onuStateInfo = document.createElementNS(ONUConstants.ONU_STATE_INFO_NAMESPACE, ONUConstants.ONU_STATE_INFO);
        Element equipmentId = document.createElementNS(ONUConstants.ONU_STATE_INFO_NAMESPACE, ONUConstants.EQUIPEMENT_ID);
        deviceState.appendChild(onuStateInfo);
        onuStateInfo.appendChild(equipmentId);

        Element softwareImages = document.createElementNS(ONUConstants.ONU_STATE_INFO_NAMESPACE, ONUConstants.SOFTWARE_IMAGES);
        Element softwareImage = document.createElementNS(ONUConstants.ONU_STATE_INFO_NAMESPACE, ONUConstants.SOFTWARE_IMAGE);
        onuStateInfo.appendChild(softwareImages);
        softwareImages.appendChild(softwareImage);
        document.appendChild(onuManager);

        NetconfFilter requestFilter = new NetconfFilter();
        requestFilter.setType(ONUConstants.SUBTREE_FILTER);
        requestFilter.addXmlFilter(document.getDocumentElement());

        GetRequest getRequest = new GetRequest();
        getRequest.setFilter(requestFilter);

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
