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

package org.broadband_forum.obbaa.aggregator.jaxb.aggregatorimpl;

import org.broadband_forum.obbaa.aggregator.api.DispatchException;
import org.broadband_forum.obbaa.aggregator.jaxb.networkmanager.api.NetworkManagerRpc;
import org.broadband_forum.obbaa.netconf.api.messages.PojoToDocumentTransformer;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Utils for Aggregator.
 */
public final class AggregatorUtils {
    private AggregatorUtils() {
        //Hide
    }

    /**
     * Convert String to Document object.
     *
     * @param request Netconf request string
     * @return Document
     * @throws DispatchException Exception
     */
    public static Document stringToDocument(String request) throws DispatchException {
        try {
            return DocumentUtils.stringToDocument(trimXmlFormat(request));
        }
        catch (NetconfMessageBuilderException ex) {
            throw new DispatchException(ex);
        }
    }

    /**
     * Convert Document to String object.
     *
     * @param document Netconf document
     * @return String of Netconf request
     */
    public static String documentToString(Document document) {
        return PojoToDocumentTransformer.requestToString(document);
    }

    /**
     * Trim xml files of Netconf request.
     *
     * @param xml Netconf message file
     * @return String after trim
     */
    public static String trimXmlFormat(String xml) {
        StringBuffer buffer = new StringBuffer();
        for (String line : xml.split("\n")) {
            buffer.append(line.trim().replaceAll("\n|\r", ""));
        }

        return buffer.toString();
    }

    /**
     * Build managed-devices Element.
     *
     * @param document Node document
     * @return Element of managed-devices
     */
    public static Element buildManagedDevicesElement(Document document) {
        Element element = document.createElementNS(NetworkManagerRpc.NAMESPACE, "managed-devices");

        return element;
    }

    /**
     * Build device Element.
     *
     * @param document Owner document
     * @return Element
     */
    public static Element buildDeviceElement(Document document) {
        return document.createElement("device");
    }

    /**
     * Build name Element.
     *
     * @param document Owner document
     * @param deviceName Device name
     * @return Element
     */
    public static Element buildDeviceNameElement(Document document, String deviceName) {
        Element deviceNameNode = document.createElement("name");
        if (deviceName != null) {
            deviceNameNode.setTextContent(deviceName);
        }

        return deviceNameNode;
    }

    /**
     * Build root Element.
     *
     * @param document Owner document
     * @return Element
     */
    public static Element buildRootElement(Document document) {
        return document.createElement("root");
    }

    /**
     * Build framework of root node for inserting message of mounted.
     *
     * @param document Owner document
     * @param parent Parent node
     * @param deviceName Device name
     * @return Element of root
     */
    public static Element buildInsertMountRootFramework(Document document, Node parent, String deviceName) {
        Element managedDevices = buildManagedDevicesElement(document);
        Element device = buildDeviceElement(document);
        Element root = buildRootElement(document);

        device.appendChild(buildDeviceNameElement(document, deviceName));
        device.appendChild(root);
        managedDevices.appendChild(device);

        parent.appendChild(managedDevices);

        return root;
    }

    /**
     * Get node of event-time.
     *
     * @param document Owner document
     * @return Node of event-time
     * @throws DispatchException Exception
     */
    public static Node getEventTimeNode(Document document) throws DispatchException {
        NodeList notificationList = document.getDocumentElement().getElementsByTagName("eventTime");
        DispatchException.assertZero(notificationList.getLength(), "Error notification");

        return notificationList.item(0);
    }

    /**
     * Package notification.
     *
     * @param deviceName Device name
     * @param notification Notification
     * @return Net message
     * @throws DispatchException Exception
     */
    public static String packageNotification(String deviceName, String notification) throws DispatchException {
        Document document = AggregatorUtils.stringToDocument(notification);
        Node eventTime = getEventTimeNode(document);
        Node notificationNode = eventTime.getParentNode();
        Element insertParent = buildInsertMountRootFramework(document, notificationNode, deviceName);

        Node insertNode = notificationNode.getLastChild();
        Node needReplace = eventTime.getNextSibling();
        replaceNotificationPayload(eventTime, insertParent, needReplace, insertNode);

        return AggregatorUtils.documentToString(document);
    }

    /**
     * Replace payload of notification message.
     *
     * @param eventTime event-time node
     * @param insertParent Parent node
     * @param needReplace Node need be replaced
     * @param insertNode Node need insert
     */
    public static void replaceNotificationPayload(Node eventTime, Element insertParent,
                                                  Node needReplace, Node insertNode) {
        while ((needReplace != null) && (!insertNode.equals(needReplace))) {
            needReplace.getParentNode().removeChild(needReplace);
            insertParent.appendChild(needReplace);
            needReplace = eventTime.getNextSibling();
        }
    }
}
