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

package org.broadband_forum.obbaa.aggregator.processor;

import org.broadband_forum.obbaa.aggregator.api.DeviceManagementProcessor;
import org.broadband_forum.obbaa.aggregator.api.DispatchException;
import org.broadband_forum.obbaa.aggregator.impl.SingleDeviceRequest;
import org.broadband_forum.obbaa.aggregator.jaxb.networkmanager.api.NetworkManagerRpc;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.PojoToDocumentTransformer;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Aggregator message.
 */
public class AggregatorMessage {
    private String m_originalMessage;
    private String m_netconfMessage;
    private Document m_document;
    private String m_parentXmlns;
    private String m_messageId;

    public static final String NS_OBBAA_NETWORK_MANAGER = "urn:bbf:yang:obbaa:network-manager";
    private static final Logger LOGGER = LoggerFactory.getLogger(AggregatorMessage.class);

    private AggregatorMessage() {
        //Hide
    }

    AggregatorMessage(String netconfMessage) throws DispatchException {
        LOGGER.info("AggregatorMessage: {}", netconfMessage);
        m_originalMessage = netconfMessage;
        m_netconfMessage = trimXmlFormat(netconfMessage);

        try {
            m_document = DocumentUtils.stringToDocument(m_netconfMessage);
            m_parentXmlns = NetconfMessageUtil.getParentXmlns(m_document);
            m_messageId = NetconfMessageUtil.getMessageIdFromRpcDocument(m_document);
        }
        catch (NetconfMessageBuilderException ex) {
            throw new DispatchException(ex);
        }
    }

    static String trimXmlFormat(String xml) {
        StringBuffer buffer = new StringBuffer();
        for (String line : xml.split("\n")) {
            buffer.append(line.trim().replaceAll("\n|\r", ""));
        }
        return  buffer.toString();
    }

    static Node getManagedDevicesNode(Document document) {
        NodeList managedDevicesList = document.getElementsByTagName("managed-devices");
        if ((managedDevicesList == null) || (managedDevicesList.getLength() == 0)) {
            return null;
        }

        return managedDevicesList.item(0);
    }

    private String getDeviceNameFromDeviceNode(Node device) throws DispatchException {
        Node deviceNameNode = device.getFirstChild();
        if (!deviceNameNode.getNodeName().equals("device-name")) {
            throw new DispatchException("The request format is error.");
        }

        return deviceNameNode.getTextContent();
    }

    private SingleDeviceRequest buildOneDeviceRequest(DeviceManagementProcessor processor, Node device)
            throws DispatchException {
        //Build parent node
        Document documentOneDevice = stringToDocument(m_netconfMessage);
        Node managedDevices = getManagedDevicesNode(documentOneDevice);
        if (managedDevices == null) {
            throw new DispatchException("Error request about the schema-mount node");
        }

        //Remove old node
        Node parentNode = managedDevices.getParentNode();
        parentNode.removeChild(managedDevices);

        Node rootNode = device.getLastChild();
        NodeList deviceCfgNodeList = rootNode.getChildNodes();

        //Append new node
        for (int id = 0; id < deviceCfgNodeList.getLength(); id++) {
            Node newNode = documentOneDevice.importNode(deviceCfgNodeList.item(id), true);
            parentNode.appendChild(newNode);
        }

        String deviceName = getDeviceNameFromDeviceNode(device);
        String deviceType = processor.getDeviceTypeByDeviceName(deviceName);
        return new SingleDeviceRequest(deviceName, deviceType, documentOneDevice);
    }

    boolean isNetworkManageMountMessage() {
        return (m_parentXmlns.equalsIgnoreCase(NS_OBBAA_NETWORK_MANAGER));
    }

    static Node getEventTimeNode(Document document) throws DispatchException {
        NodeList notificationList = document.getDocumentElement().getElementsByTagName("eventTime");
        if (notificationList.getLength() == 0) {
            throw new DispatchException("Error notification");
        }

        return notificationList.item(0);
    }

    static String packageNotification(String deviceName, String notification) throws DispatchException {
        Document document = stringToDocument(notification);
        Node eventTime = getEventTimeNode(document);
        Node notificationNode = eventTime.getParentNode();
        Element insertParent = buildInsertMountRootFramework(document, notificationNode, deviceName);

        //Mount responses
        Node insertNode = notificationNode.getLastChild();
        Node needReplace = eventTime.getNextSibling();
        while ((needReplace != null) && (!insertNode.equals(needReplace))) {
            needReplace.getParentNode().removeChild(needReplace);
            insertParent.appendChild(needReplace);
            needReplace = eventTime.getNextSibling();
        }

        return documentToString(document);
    }

    String getParentXmlns() {
        return m_parentXmlns;
    }

    Set<SingleDeviceRequest> getDocumentEveryDevice(DeviceManagementProcessor processor) throws DispatchException {
        if (!containMountedMessage()) {
            return null;
        }

        //Get managed device container
        Node managedDevices = getManagedDevicesNode(m_document);
        if (managedDevices == null) {
            return null;
        }

        //Parse every device
        NodeList devices = managedDevices.getChildNodes();
        int nodeNum = devices.getLength();
        Set<SingleDeviceRequest> singleDeviceRequests = new HashSet<>();
        for (int id = 0; id < nodeNum; id++) {
            Node device = devices.item(id);

            //Build request for every device
            SingleDeviceRequest singleDeviceRequest = buildOneDeviceRequest(processor, device);
            singleDeviceRequests.add(singleDeviceRequest);
        }

        return singleDeviceRequests;
    }

    boolean containMountedMessage() {
        if (!isNetworkManageMountMessage()) {
            return false;
        }

        NodeList nodeList = m_document.getElementsByTagName("root");
        if ((nodeList == null) || (nodeList.getLength() == 0)) {
            return false;
        }

        return true;
    }

    public static NodeList getDeviceNodes(Document document) {
        //Get managed device container
        Node managedDevices = NetconfMessageUtil.getFirstNode(document, "managed-devices");
        if (managedDevices == null) {
            return null;
        }

        return managedDevices.getChildNodes();
    }

    public static Document stringToDocument(String request) throws DispatchException {
        try {
            String message = trimXmlFormat(request);
            return DocumentUtils.stringToDocument(message);
        }
        catch (NetconfMessageBuilderException ex) {
            throw new DispatchException(ex);
        }
    }

    static String documentToString(Document document) {
        return PojoToDocumentTransformer.requestToString(document);
    }

    private static Element buildInsertMountRootFramework(Document document, Node parent, String deviceName) {

        Element device = document.createElement("device");
        device.appendChild(buildDeviceNameElement(document, deviceName));

        Element root = document.createElement("root");
        device.appendChild(root);

        Element managedDevices = document.createElement("managed-devices");
        managedDevices.setAttribute("xmlns", NetworkManagerRpc.NAMESPACE);
        managedDevices.appendChild(device);
        parent.appendChild(managedDevices);

        return root;
    }

    private static Element createManagedDevicesElement(Document managedDeviceDocument) {
        Element managedDevices = managedDeviceDocument.createElement("managed-devices");
        managedDevices.setAttribute("xmlns", NetworkManagerRpc.NAMESPACE);

        return managedDevices;
    }

    private static Element buildDeviceNameElement(Document document, String deviceName) {
        Element deviceNameNode = document.createElement("device-name");
        if (deviceName != null) {
            deviceNameNode.setTextContent(deviceName);
        }

        return deviceNameNode;
    }

    private static Element buildRootElement(Document document, List<Element> elements) {
        Element root = document.createElement("root");
        for (Element element : elements) {
            NodeList nodeList = element.getChildNodes();
            for (int id = 0; id < nodeList.getLength(); id++) {
                root.appendChild(document.importNode(nodeList.item(id), true));
            }
        }

        return root;
    }

    private static void buildDeviceElementWithInfo(Document document, Element managedDevices,
                                                   String deviceName, Document response) {
        Element device = document.createElement("device");
        Element deviceNameElement = buildDeviceNameElement(document, deviceName);
        device.appendChild(deviceNameElement);

        List<Element> dataElementsFromRpcReply = DocumentUtils.getInstance().getDataElementsFromRpcReply(response);
        if (dataElementsFromRpcReply != null) {
            device.appendChild(buildRootElement(document, dataElementsFromRpcReply));
        }

        managedDevices.appendChild(device);
    }

    private String buildNeedPackageResponse(Map<Document, String> responses) throws DispatchException {
        NetConfResponse netConfResponse = new NetConfResponse().setMessageId(m_messageId);

        Document managedDeviceDocument = DocumentUtils.createDocument();
        Element managedDevicesElement = createManagedDevicesElement(managedDeviceDocument);
        for (Map.Entry<Document, String> entry : responses.entrySet()) {
            Document oneResponseDocument = entry.getKey();
            buildDeviceElementWithInfo(managedDeviceDocument, managedDevicesElement, entry.getValue(), oneResponseDocument);
        }

        netConfResponse.addDataContent(managedDevicesElement);
        return netConfResponse.responseToString();
    }

    String getLastResult(Map<Document, String> responses) {
        return documentToString(responses.keySet().iterator().next());
    }

    String packageResponse(Map<Document, String> responses) throws DispatchException {
        if (responses.isEmpty()) {
            throw new DispatchException("Processing has an exception of response.");
        }

        String response;
        String typeOfNetconfRequest = NetconfMessageUtil.getTypeOfNetconfRequest(m_document);
        switch (typeOfNetconfRequest) {
            case NetconfResources.GET:
            case NetconfResources.GET_CONFIG:
            case NetconfResources.GET_CONFIG_CONFIG:
                response = buildNeedPackageResponse(responses);
                break;
            default:
                response = getLastResult(responses);
                break;
        }

        return response;
    }

    String getMessageString() {
        return m_originalMessage;
    }

    Document getDocument() {
        return m_document;
    }

    @Override
    public String toString() {
        return m_originalMessage;
    }
}
