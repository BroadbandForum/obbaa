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

import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.PREFIX;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.rootPathForDevice;

import org.broadband_forum.obbaa.aggregator.api.DispatchException;
import org.broadband_forum.obbaa.aggregator.jaxb.networkmanager.api.NetworkManagerRpc;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfNotification;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.messages.PojoToDocumentTransformer;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
        } catch (NetconfMessageBuilderException ex) {
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
     * Get node of target.
     *
     * @param document Owner document
     * @return Node of target
     * @throws DispatchException Exception
     */
    public static Element getTargetNode(Document document) throws DispatchException {
        NodeList notificationList = document.getDocumentElement().getElementsByTagName("target");
        DispatchException.assertZero(notificationList.getLength(), "Error notification");

        return (Element) notificationList.item(0);
    }

    /**
     * Package notification.
     *
     * @param deviceName   Device name
     * @param notification Notification
     * @return Net message
     * @throws DispatchException Exception
     */
    public static Notification packageNotification(String deviceName, Notification notification) throws DispatchException,
            NetconfMessageBuilderException {
        Document notificationDocument = notification.getNotificationDocument();
        Document notificationDoc = updateNotificationTargetElement(deviceName, notificationDocument);
        return new NetconfNotification(notificationDoc);
    }

    private static Document updateNotificationTargetElement(String deviceName, Document notificationDocument) throws DispatchException {
        Element oldTargetNode = getTargetNode(notificationDocument);

        StringBuilder newTargetText = new StringBuilder();
        String oldTargetText = oldTargetNode.getTextContent();
        String dmText = rootPathForDevice(deviceName);
        newTargetText.append(dmText);
        newTargetText.append(oldTargetText);
        oldTargetNode.setAttribute(NetworkManagerRpc.XMLNS + ":" + PREFIX, NetworkManagerRpc.NAMESPACE);
        oldTargetNode.setTextContent(newTargetText.toString());
        return notificationDocument;
    }

}
