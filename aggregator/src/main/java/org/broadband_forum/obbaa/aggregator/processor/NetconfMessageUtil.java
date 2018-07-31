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

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import org.broadband_forum.obbaa.aggregator.api.DispatchException;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.util.ModuleIdentifierImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class NetconfMessageUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(NetconfMessageUtil.class);
    private static String XMLNS = "xmlns";

    private NetconfMessageUtil() {
        //Hide
    }

    public static String buildRpcReplyOk(String messageId) throws DispatchException {
        NetConfResponse netConfResponse = new NetConfResponse();
        netConfResponse.setMessageId(messageId);
        netConfResponse.setOk(true);
        return netConfResponse.responseToString();
    }

    public static String getParentXmlns(Document request) {
        NodeList nodeList = request.getDocumentElement().getElementsByTagName("*");
        String xmlnsValue = getFirstXmlnsFromNodeList(nodeList);
        if (xmlnsValue == null) {
            xmlnsValue = getFirstXmlnsFromNodeList(request.getElementsByTagName("*"));
        }

        return xmlnsValue;
    }

    private static String getFirstXmlnsFromNodeList(NodeList nodeList) {
        int nodeCount = nodeList.getLength();

        String xmlnsValue;
        for (int id = 0; id < nodeCount; id++) {
            Node node = nodeList.item(id);
            xmlnsValue = getNodeXmlnsValue(node);
            if ((xmlnsValue == null) || (xmlnsValue.isEmpty())) {
                continue;
            }

            return xmlnsValue;
        }

        return null;
    }

    private static boolean needIgnoreTag(String tagName) {
        String[] netconfProtocolTagsArray = {"rpc", "rpc-reply", "filter", "config", "action",
            "get", "get-config", "edit-config", "copy-config", "delete-config",
            "lock", "unlock", "close-session", "kill-session"};

        for (String netconfProtocolTag : netconfProtocolTagsArray) {
            if (tagName.equalsIgnoreCase(netconfProtocolTag)) {
                return true;
            }
        }

        return false;
    }

    private static String getNodeXmlnsValue(Node node) {
        if (needIgnoreTag(node.getNodeName())) {
            return null;
        }

        NamedNodeMap namedNodeMap = node.getAttributes();
        if (namedNodeMap == null) {
            return null;
        }

        String prefiex = node.getPrefix();
        String xmlnsAttrName;
        if ((prefiex != null) && (!prefiex.isEmpty())) {
            xmlnsAttrName = String.format("xmlns:%s", prefiex);
        }
        else {
            xmlnsAttrName = XMLNS;
        }

        Node nodeNs = namedNodeMap.getNamedItem(xmlnsAttrName);
        if (nodeNs == null) {
            return null;
        }

        return nodeNs.getNodeValue();
    }

    public static Node getFirstNode(Document document, String tagName) {
        NodeList nodeList = document.getElementsByTagName(tagName);
        if ((nodeList == null) || (nodeList.getLength() == 0)) {
            return null;
        }

        return nodeList.item(0);
    }

    public static String getMessageIdFromRpcDocument(Document request) throws DispatchException {
        String messageId = DocumentUtils.getInstance().getMessageIdFromRpcDocument(request);
        if (messageId == null) {
            LOGGER.error("Message ID is null in the request.");
            messageId = "101";
        }

        return messageId;
    }

    public static Document getDocumentFromResponse(NetConfResponse netConfResponse) throws DispatchException {
        try {
            return netConfResponse.getResponseDocument();
        }
        catch (NetconfMessageBuilderException ex) {
            throw new DispatchException(ex);
        }
    }

    public static String getTypeOfNetconfRequest(Document document) throws DispatchException {
        String typeOfNetconfRequest = DocumentToPojoTransformer.getTypeOfNetconfRequest(document);
        if (typeOfNetconfRequest == null) {
            throw new DispatchException("The request does not be supported with error type.");
        }

        return typeOfNetconfRequest;
    }

    public static void appendNewElement(Document document, Element parent, String tagName, String value) {
        if (value == null) {
            return;
        }

        Element deviceType = document.createElement(tagName);
        deviceType.setTextContent(value);
        parent.appendChild(deviceType);
    }

    public static ModuleIdentifier buildModuleIdentifier(String name, String namespace, String revision) {
        try {
            URI namespaceUri = new URI(namespace);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date revisionDate = simpleDateFormat.parse(revision);

            return ModuleIdentifierImpl.create(name, Optional.of(namespaceUri), Optional.of(revisionDate));
        }
        catch (URISyntaxException | ParseException ex) {
            return null;
        }
    }

    public static String buildModuleCapability(ModuleIdentifier moduleIdentifier) {
        return String.format("%s?module=%s&revision=%s", moduleIdentifier.getNamespace(),
                moduleIdentifier.getName(), moduleIdentifier.getQNameModule().getFormattedRevision());
    }
}
