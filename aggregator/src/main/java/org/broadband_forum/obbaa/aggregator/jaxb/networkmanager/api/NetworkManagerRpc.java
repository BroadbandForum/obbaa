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

package org.broadband_forum.obbaa.aggregator.jaxb.networkmanager.api;

import org.broadband_forum.obbaa.aggregator.api.DeviceManagementProcessor;
import org.broadband_forum.obbaa.aggregator.api.DispatchException;
import org.broadband_forum.obbaa.aggregator.impl.SingleDeviceRequest;
import org.broadband_forum.obbaa.aggregator.jaxb.aggregatorimpl.AggregatorUtils;
import org.broadband_forum.obbaa.aggregator.jaxb.netconf.api.NetconfRpcMessage;
import org.broadband_forum.obbaa.aggregator.jaxb.netconf.schema.rpc.RpcOperationType;
import org.broadband_forum.obbaa.aggregator.jaxb.networkmanager.schema.Device;
import org.broadband_forum.obbaa.aggregator.jaxb.networkmanager.schema.NetworkManager;
import org.broadband_forum.obbaa.aggregator.jaxb.networkmanager.schema.ManagedDevices;
import org.broadband_forum.obbaa.aggregator.jaxb.utils.JaxbUtils;
import org.broadband_forum.obbaa.aggregator.processor.AggregatorMessage;
import org.broadband_forum.obbaa.aggregator.processor.NetconfMessageUtil;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ModuleIdentifier;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.bind.JAXBException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Network-manager YANG model message.
 */
public class NetworkManagerRpc {
    public static final String XMLNS = "xmlns";
    public static final String NAMESPACE = "urn:bbf:yang:obbaa:network-manager";

    private String originalMessage;
    private NetworkManager m_networkManager;
    private NetconfRpcMessage netconfRpcMessage;

    private NetworkManagerRpc() {
        //Hide
    }

    /**
     * Network-manager YANG module message.
     *
     * @param originalMessage Original message
     * @param rpcPayload      managed-devices document
     * @throws JAXBException Exception
     */
    public NetworkManagerRpc(String originalMessage, NetconfRpcMessage netconfRpcMessage, Document rpcPayload)
            throws DispatchException {
        this.originalMessage = originalMessage;
        this.netconfRpcMessage = netconfRpcMessage;
        m_networkManager = unmarshal(rpcPayload);
    }

    /**
     * Get module identifiers of YANG network-manager.
     *
     * @return Network-manager module identifiers
     */
    public static Set<ModuleIdentifier> getNetworkManagerModuleIdentifiers() {
        Set<ModuleIdentifier> moduleIdentifiers = new HashSet<>();

        ModuleIdentifier moduleIdentifier = NetconfMessageUtil.buildModuleIdentifier("network-manager",
                AggregatorMessage.NS_OBBAA_NETWORK_MANAGER, "2018-05-07");
        moduleIdentifiers.add(moduleIdentifier);

        return moduleIdentifiers;
    }

    /**
     * Unmarshal message.
     *
     * @param manageDevicesText manage-devices message
     * @return ManagedDevices
     * @throws DispatchException Exception
     */
    public static ManagedDevices unmarshal(String manageDevicesText) throws DispatchException {
        try {
            return JaxbUtils.unmarshal(manageDevicesText, ManagedDevices.class);
        }
        catch (JAXBException ex) {
            throw new DispatchException(ex);
        }
    }

    /**
     * Unmarshal message.
     *
     * @param networkManagerNode network-manager document
     * @return NetworkManager
     * @throws DispatchException Exception
     */
    public static NetworkManager unmarshal(Node networkManagerNode) throws DispatchException {
        try {
            return JaxbUtils.unmarshal(networkManagerNode, NetworkManager.class);
        } catch (JAXBException ex) {
            throw new DispatchException(ex);
        }
    }

    /**
     * Get nodes of devices.
     *
     * @return Devices
     */
    public List<Device> getDevices() {
        return getNetworkManager().getManagedDevices().getDevices();
    }

    /**
     * Get managed-devices of network-manager YANG model.
     *
     * @return Managed devices information
     */
    public NetworkManager getNetworkManager() {
        return m_networkManager;
    }

    /**
     * Get the node of managed-devices.
     *
     * @param document Request
     * @return Node of managed-devices
     */
    private Node getDeviceManagerNode(Document document) {
        NodeList deviceManagerList = document.getElementsByTagName("network-manager");

        //Must be one managed-devices node
        if ((deviceManagerList == null) || (deviceManagerList.getLength() != 1)) {
            return null;
        }

        return deviceManagerList.item(0);
    }

    /**
     * Build rpc framework for mounted message.
     *
     * @param rpcMessage Original message
     * @return Node for adding the mounted document
     * @throws DispatchException Exception
     */
    private Node buildRpcFramework(String rpcMessage) throws DispatchException {
        Document document = AggregatorUtils.stringToDocument(rpcMessage);
        Node deviceManagerNode = getDeviceManagerNode(document);
        Node parentNode = deviceManagerNode.getParentNode();
        parentNode.removeChild(deviceManagerNode);

        return parentNode;
    }

    /**
     * Get device type for mounted message management.
     *
     * @param deviceManagementProcessor Device manager
     * @param deviceName            Device name
     * @return Device type
     */
    private String getDeviceType(DeviceManagementProcessor deviceManagementProcessor, String deviceName) {
        try {
            return deviceManagementProcessor.getDeviceTypeByDeviceName(deviceName);
        } catch (DispatchException ex) {
            return null;
        }
    }

    /**
     * Build request only for one device service config, which is mounted to the root.
     *
     * @param rpcMessage            Original message
     * @param device                Device node
     * @param deviceManagementProcessor Device manager
     * @return Request of one device
     * @throws DispatchException Exception
     */
    private SingleDeviceRequest buildSingleDeviceRequest(String rpcMessage, Device device,
                                                         DeviceManagementProcessor deviceManagementProcessor) throws DispatchException {
        Node frameworkParent = buildRpcFramework(rpcMessage);
        Document parentDocument = frameworkParent.getOwnerDocument();
        for (Document childDocument : device.getRoot().getDocuments()) {
            appendChilds(parentDocument, frameworkParent, childDocument);
        }

        if ((device.getRoot().getDocuments().size() == 0) && (frameworkParent.getLocalName().equals("filter"))) {
            frameworkParent.getParentNode().removeChild(frameworkParent);
        }

        String deviceName = device.getDeviceName();
        String deviceType = getDeviceType(deviceManagementProcessor, deviceName);

        return new SingleDeviceRequest(deviceName, deviceType, parentDocument);
    }

    /**
     * Append child nodes of another document.
     *
     * @param parentDocument Parent document
     * @param parentNode     Parent Node
     * @param childDocument  Child document
     */
    private void appendChilds(Document parentDocument, Node parentNode, Document childDocument) {
        NodeList nodeList = childDocument.getChildNodes();

        for (int id = 0; id < nodeList.getLength(); id++) {
            Node childNode = parentDocument.importNode(nodeList.item(id), true);

            parentNode.appendChild(childNode);
        }
    }

    /**
     * Build requests for every device. It will delete the information of network-manager YANG model.
     *
     * @param deviceManagementProcessor Device manager processor for get device type
     * @param rpcMessage            Original message
     * @return Set of these device requests
     * @throws DispatchException Exception
     */
    public Set<SingleDeviceRequest> buildSingleDeviceRequests(DeviceManagementProcessor deviceManagementProcessor,
                                                              String rpcMessage) throws DispatchException {
        List<Device> devices = getNetworkManager().getManagedDevices().getDevices();
        Set<SingleDeviceRequest> singleDeviceRequests = new HashSet<>();

        for (Device device : devices) {
            if ((device.getRoot() != null) && (device.getRoot().getDocuments() != null)) {
                SingleDeviceRequest request = buildSingleDeviceRequest(rpcMessage, device, deviceManagementProcessor);
                singleDeviceRequests.add(request);
            }
        }

        return singleDeviceRequests;
    }

    /**
     * Get original message.
     *
     * @return Original message
     */
    public String getOriginalMessage() {
        return originalMessage;
    }

    /**
     * Get Netconf rpc message.
     *
     * @return Netconf rpc message
     */
    public NetconfRpcMessage getNetconfRpcMessage() {
        return netconfRpcMessage;
    }

    /**
     * Judge if the request is used for device-config or device-management.
     *
     * @return If the request is used for device-config
     */
    public boolean isDeviceConfigRequest() {
        try {
            for (Device device : getDevices()) {
                if (device.getRoot() != null) {
                    return true;
                }
            }
        }
        catch (NullPointerException ex) {
            //No information of device
        }

        return false;
    }

    /**
     * Just need return the last response.
     *
     * @param responses All of the responses from every processor.
     * @return Last response
     */
    private String getLastResult(Map<Document, String> responses) {
        return AggregatorUtils.documentToString(responses.keySet().iterator().next());
    }

    /**
     * Judge if these responses need to be packaged.
     *
     * @return If need to be packaged
     */
    private boolean ifResponseNeedPackage() {
        RpcOperationType rpcOperationType = getNetconfRpcMessage().getRpc().getRpcOperationType();
        switch (rpcOperationType) {
            case GET:
            case GET_CONFIG:
                return true;
            default:
                return false;
        }
    }

    /**
     * Create a element of managed-devices.
     *
     * @param managedDeviceDocument Current document
     * @return Element
     */
    private static Element buildManagedDevicesElement(Document managedDeviceDocument) {
        Element managedDevices = managedDeviceDocument.createElement("managed-devices");
        managedDevices.setAttribute("xmlns", NetworkManagerRpc.NAMESPACE);

        return managedDevices;
    }

    /**
     * Build a element of name.
     *
     * @param document   Current document
     * @param deviceName Device name
     * @return Element
     */
    private static Element buildDeviceNameElement(Document document, String deviceName) {
        Element deviceNameNode = document.createElement("name");
        if (deviceName != null) {
            deviceNameNode.setTextContent(deviceName);
        }

        return deviceNameNode;
    }

    /**
     * Build a element named root for schema-mount.
     *
     * @param document Current document
     * @param elements Elements need to be append to root
     * @return Element of root
     */
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

    /**
     * Build device element with device information.
     *
     * @param document       Current document
     * @param managedDevices managed-devices
     * @param deviceName     Device name
     * @param response       The result from processors.
     */
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

    /**
     * Judge if the responses need to be packaged.
     *
     * @param responses Responses from some processors
     * @return Global response
     * @throws DispatchException Exception
     */
    private String buildNeedPackageResponse(Map<Document, String> responses) throws DispatchException {
        NetConfResponse netConfResponse = new NetConfResponse().setMessageId(
                getNetconfRpcMessage().getRpc().getMessageId());

        Document managedDeviceDocument = DocumentUtils.createDocument();
        Element managedDevicesElement = buildManagedDevicesElement(managedDeviceDocument);
        for (Map.Entry<Document, String> entry : responses.entrySet()) {
            Document oneResponseDocument = entry.getKey();
            buildDeviceElementWithInfo(managedDeviceDocument, managedDevicesElement, entry.getValue(), oneResponseDocument);
        }

        netConfResponse.addDataContent(managedDevicesElement);
        return netConfResponse.responseToString();
    }

    /**
     * Package the response from processor.
     *
     * @param responses Responses
     * @return Whole response
     * @throws DispatchException Exception
     */
    public String packageResponse(Map<Document, String> responses) throws DispatchException {
        if (responses.isEmpty()) {
            throw new DispatchException("Processing has an exception of response.");
        }

        if (ifResponseNeedPackage()) {
            return buildNeedPackageResponse(responses);
        }

        //Last result is the whole result
        return getLastResult(responses);
    }
}
