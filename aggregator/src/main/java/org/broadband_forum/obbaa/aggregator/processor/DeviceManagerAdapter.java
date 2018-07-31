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

import static org.broadband_forum.obbaa.pma.impl.NetconfConnectionStateProvider.CONNECTION_STATE;
import static org.broadband_forum.obbaa.pma.impl.NetconfDeviceAlignmentServiceImpl.ALIGNMENT_STATE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.aggregator.api.Aggregator;
import org.broadband_forum.obbaa.aggregator.api.DeviceManagementProcessor;
import org.broadband_forum.obbaa.aggregator.api.DispatchException;
import org.broadband_forum.obbaa.connectors.sbi.netconf.ConnectionState;
import org.broadband_forum.obbaa.connectors.sbi.netconf.NewDeviceInfo;
import org.broadband_forum.obbaa.dm.DeviceManager;
import org.broadband_forum.obbaa.netconf.api.messages.DeleteConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.store.alignment.DeviceAlignmentInfo;
import org.broadband_forum.obbaa.store.dm.CallHomeInfo;
import org.broadband_forum.obbaa.store.dm.DeviceInfo;
import org.broadband_forum.obbaa.store.dm.SshConnectionInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DeviceManagerAdapter implements DeviceManagementProcessor {
    private static final String NM_NS = "urn:bbf:yang:obbaa:network-manager";
    private static final String NC_BASE_NS = "urn:ietf:params:xml:ns:netconf:base:1.0";
    Aggregator m_aggregator;
    DeviceManager m_deviceManager;
    Map<String, DeviceAdapterInfo> m_deviceTypeMap;

    public DeviceManagerAdapter(Aggregator aggregator, DeviceManager deviceManager) {
        m_aggregator = aggregator;
        m_deviceManager = deviceManager;
        m_deviceTypeMap = new HashMap<>();
    }

    private static String getFirstNodeStringValue(Document document, String tagName) {
        NodeList nodeList = document.getElementsByTagNameNS(NM_NS, tagName);
        if ((nodeList == null) || (nodeList.getLength() == 0)) {
            return null;
        }

        return nodeList.item(0).getTextContent();
    }

    public void init() {
        m_aggregator.registerDeviceManager(this);
    }

    public void destroy() {
        m_aggregator.unregisterDeviceManager();
    }

    @Override
    public String processRequest(String netconfRequest) throws DispatchException {
        Document document = AggregatorMessage.stringToDocument(netconfRequest);

        //Device name is invalid in this processor
        return deviceManagement(document);
    }

    private List<String> getDeviceName(Document document) {
        NodeList nodeList = document.getElementsByTagName("device-name");
        List<String> deviceNames = new ArrayList<>();
        for (int id = 0; id < nodeList.getLength(); id++) {
            deviceNames.add(nodeList.item(id).getTextContent());
        }

        return deviceNames;
    }

    private DeviceAdapterInfo parseDeviceAdptInfo(Document oneDeviceDocument) {
        DeviceAdapterInfo deviceAdapterInfo = new DeviceAdapterInfo();

        deviceAdapterInfo.setType(getFirstNodeStringValue(oneDeviceDocument, "device-type"));
        deviceAdapterInfo.setModel(getFirstNodeStringValue(oneDeviceDocument, "device-model"));
        deviceAdapterInfo.setSoftwareVersion(getFirstNodeStringValue(oneDeviceDocument,
                "device-software-version"));
        deviceAdapterInfo.setVendor(getFirstNodeStringValue(oneDeviceDocument, "device-vendor"));

        return deviceAdapterInfo;
    }

    private void populateConnectionInfo(DeviceInfo deviceInfo, Document oneDeviceDocument, boolean
            ignoreMissingFields) {
        if (isDirectDevice(oneDeviceDocument)) {
            SshConnectionInfo sshConnectionInfo = new SshConnectionInfo();

            sshConnectionInfo.setIp(getFirstNodeStringValue(oneDeviceDocument, "address"));
            sshConnectionInfo.setUsername(getFirstNodeStringValue(oneDeviceDocument, "user-name"));
            sshConnectionInfo.setPassword(getFirstNodeStringValue(oneDeviceDocument, "password"));
            String portString = getFirstNodeStringValue(oneDeviceDocument, "management-port");

            if ((portString != null) && (!portString.isEmpty())) {
                sshConnectionInfo.setPort(Integer.parseInt(portString));
            } else if (!ignoreMissingFields) {
                throw new RuntimeException("Device direct connection details not correct");
            }
            deviceInfo.setDeviceConnectionInfo(sshConnectionInfo);
        } else {
            CallHomeInfo callHomeInfo = new CallHomeInfo();
            String duid = getFirstNodeStringValue(oneDeviceDocument, "duid");
            if (duid == null && !ignoreMissingFields) {
                throw new RuntimeException("Device is neither direct nor call-home");
            }
            callHomeInfo.setDuid(duid);
            deviceInfo.setDeviceCallHomeInfo(callHomeInfo);
        }
    }

    private boolean isDirectDevice(Document oneDeviceDocument) {
        return getFirstNodeStringValue(oneDeviceDocument, "address") != null;
    }

    private DeviceInfo parseDeviceInfo(Document oneDeviceDocument, boolean ignoreMissingFields) {
        DeviceInfo deviceInfo = new DeviceInfo();

        deviceInfo.setKey(getFirstNodeStringValue(oneDeviceDocument, "device-name"));
        populateConnectionInfo(deviceInfo, oneDeviceDocument, ignoreMissingFields);

        return deviceInfo;
    }

    private DeviceInfo parseDeviceInfo(Document oneDeviceDocument) {
        return parseDeviceInfo(oneDeviceDocument, false);
    }

    private void createDevice(Document oneDeviceDocument) throws DispatchException {
        DeviceInfo deviceInfo = parseDeviceInfo(oneDeviceDocument);
        DeviceAdapterInfo deviceAdapterInfo = getDeviceAdptInfo(deviceInfo.getKey());
        if (deviceAdapterInfo != null) {
            throw new DispatchException("Device has exist.");
        }

        m_deviceManager.createDevice(deviceInfo);
        deviceAdapterInfo = parseDeviceAdptInfo(oneDeviceDocument);
        updateDeviceAdptInfo(deviceInfo.getKey(), deviceAdapterInfo);
    }

    private void updateDevice(Document oneDeviceDocument) {
        DeviceInfo deviceInfo = parseDeviceInfo(oneDeviceDocument, true);
        m_deviceManager.updateDevice(deviceInfo);

        DeviceAdapterInfo deviceAdapterInfo = parseDeviceAdptInfo(oneDeviceDocument);
        updateDeviceAdptInfo(deviceInfo.getKey(), deviceAdapterInfo);
    }

    private void deleteDevice(Document oneDeviceDocument) {
        DeviceInfo deviceInfo = parseDeviceInfo(oneDeviceDocument, true);
        m_deviceManager.deleteDevice(deviceInfo.getKey());
        removeDeviceAdptInfo(deviceInfo.getKey());
    }

    String getDeviceOperation(Node deviceNode) {
        return deviceNode.getAttributes().getNamedItemNS(NC_BASE_NS, "operation").getNodeValue();
    }

    private void deviceEditConfig(Node deviceNode) throws DispatchException {
        Document document = deviceNode.getOwnerDocument();
        String operation = getDeviceOperation(deviceNode);

        switch (operation) {
            case EditConfigOperations.CREATE:
                createDevice(document);
                break;

            case EditConfigOperations.MERGE:
                updateDevice(document);
                break;

            case EditConfigOperations.DELETE:
            case EditConfigOperations.REMOVE:
                deleteDevice(document);
                break;

            default:
                throw new DispatchException("Error operation.");
        }
    }

    private String devicesEditConfig(Document document, EditConfigRequest editConfigRequest) throws DispatchException {
        NodeList devices = AggregatorMessage.getDeviceNodes(document);
        int nodeNum = devices.getLength();
        for (int id = 0; id < nodeNum; id++) {
            Node device = devices.item(id);

            //Build request for every device
            deviceEditConfig(device);
        }

        return NetconfMessageUtil.buildRpcReplyOk(editConfigRequest.getMessageId());
    }

    private String deviceDeleteConfig(Document document, DeleteConfigRequest deleteConfigRequest) throws
            DispatchException {
        List<String> deviceNames = getDeviceName(document);

        for (String deviceName : deviceNames) {
            m_deviceManager.deleteDevice(deviceName);
            removeDeviceAdptInfo(deviceName);
        }

        return NetconfMessageUtil.buildRpcReplyOk(deleteConfigRequest.getMessageId());
    }

    private Element buildDeviceElementAfterFilter(Element device, NetconfFilter netconfFilter) {
        //TODO : If filtered (not support)
        //TODO : If need response
        return device;
    }

    private void appendNewElement(Document document, Element parent, String tagName, String value) {
        appendNewElement(document, parent, tagName, value, NM_NS);
    }

    private void appendNewElement(Document document, Element parent, String tagName, String value, String namespace) {
        if (value == null) {
            return;
        }

        Element deviceType = document.createElementNS(namespace, tagName);
        deviceType.setTextContent(value);
        parent.appendChild(deviceType);
    }

    private void buildDirectConnectionInfo(Document response, Element passwordAuth, SshConnectionInfo
            sshConnectionInfo) {
        if (sshConnectionInfo == null) {
            return;
        }

        appendNewElement(response, passwordAuth, "address", sshConnectionInfo.getIp());
        appendNewElement(response, passwordAuth, "management-port", String.valueOf(sshConnectionInfo.getPort()));
        appendNewElement(response, passwordAuth, "user-name", sshConnectionInfo.getUsername());
        appendNewElement(response, passwordAuth, "password", sshConnectionInfo.getPassword());
    }

    private void buildDeviceConnectionElement(Document response, Element deviceManagement, String deviceName) {
        Element deviceConnection = response.createElementNS(NM_NS, "device-connection");

        DeviceInfo deviceInfo = m_deviceManager.getDevice(deviceName);
        if (!deviceInfo.isCallHome()) {
            appendNewElement(response, deviceConnection, "connection-model", "direct");
            SshConnectionInfo sshConnectionInfo = deviceInfo.getDeviceConnectionInfo();
            Element passwordAuth = response.createElementNS(NM_NS, "password-auth");
            buildDirectConnectionInfo(response, passwordAuth, sshConnectionInfo);
            deviceConnection.appendChild(passwordAuth);
        } else {
            appendNewElement(response, deviceConnection, "connection-model", "call-home");
            buildCallHomeConnectionInfo(response, deviceConnection, deviceInfo.getDeviceCallHomeInfo());
        }

        deviceManagement.appendChild(deviceConnection);
    }

    private void buildCallHomeConnectionInfo(Document response, Element deviceConnection, CallHomeInfo
            deviceCallHomeInfo) {
        if (deviceCallHomeInfo == null) {
            return;
        }
        appendNewElement(response, deviceConnection, "duid", deviceCallHomeInfo.getDuid());
    }

    private void buildDeviceManagementConfigElement(Document response, Element device, String deviceName) {
        DeviceAdapterInfo deviceAdapterInfo = getDeviceAdptInfo(deviceName);

        Element deviceManagement = response.createElementNS(NM_NS, "device-management");
        appendNewElement(response, deviceManagement, "device-type", deviceAdapterInfo.getType());
        appendNewElement(response, deviceManagement, "device-software-version",
                deviceAdapterInfo.getSoftwareVersion());
        appendNewElement(response, deviceManagement, "device-model", deviceAdapterInfo.getModel());
        appendNewElement(response, deviceManagement, "device-vendor", deviceAdapterInfo.getVendor());

        buildDeviceConnectionElement(response, deviceManagement, deviceName);

        device.appendChild(deviceManagement);
    }

    private void buildDeviceManagementAllElement(Document response, Element device, String deviceName) {
        DeviceAdapterInfo deviceAdapterInfo = getDeviceAdptInfo(deviceName);

        Element deviceManagement = response.createElement("device-management");
        appendNewElement(response, deviceManagement, "device-type", deviceAdapterInfo.getType());
        appendNewElement(response, deviceManagement, "device-software-version",
                deviceAdapterInfo.getSoftwareVersion());
        appendNewElement(response, deviceManagement, "device-model", deviceAdapterInfo.getModel());
        appendNewElement(response, deviceManagement, "device-vendor", deviceAdapterInfo.getVendor());

        buildDeviceConnectionElement(response, deviceManagement, deviceName);

        //TODO: device state need to append
        device.appendChild(buildDeviceState(response, deviceName));

        device.appendChild(deviceManagement);
    }

    private Node buildDeviceState(Document response, String deviceName) {
        DeviceInfo deviceInfo = m_deviceManager.getDevice(deviceName);
        Element deviceState = response.createElementNS(NM_NS, "device-state");
        Element configurationAlignmentState = response.createElementNS(NM_NS, "configuration-alignment-state");
        DeviceAlignmentInfo alignmentInfo = (DeviceAlignmentInfo) deviceInfo.getDeviceState().get(ALIGNMENT_STATE);
        configurationAlignmentState.setTextContent(alignmentInfo.getVerdict());
        deviceState.appendChild(configurationAlignmentState);

        Element connectionStateElement = response.createElementNS(NM_NS, "connection-state");
        deviceState.appendChild(connectionStateElement);

        Element connected = response.createElementNS(NM_NS, "connected");
        connectionStateElement.appendChild(connected);
        ConnectionState connectionState = (ConnectionState) deviceInfo.getDeviceState().get(CONNECTION_STATE);
        connected.setTextContent(String.valueOf(connectionState.isConnected()));

        Element connectionCreationTime = response.createElementNS(NM_NS, "connection-creation-time");
        connectionStateElement.appendChild(connectionCreationTime);
        connectionCreationTime.setTextContent(connectionState.getFormattedCreationTime());

        for (String cap : connectionState.getCapabilities()) {
            Element capElement = response.createElementNS(NM_NS, "device-capability");
            connectionStateElement.appendChild(capElement);
            capElement.setTextContent(cap);
        }

        return deviceState;
    }

    private Element buildDeviceElement(Document request, Document response, GetRequest getRequest, String deviceName) {
        Element device = response.createElement("device");
        Element deviceNameNode = response.createElement("device-name");
        deviceNameNode.setTextContent(deviceName);
        device.appendChild(deviceNameNode);

        buildDeviceManagementAllElement(response, device, deviceName);

        return buildDeviceElementAfterFilter(device, getRequest.getFilter());
    }

    private Element buildDeviceElement(Document response,
                                       GetConfigRequest getConfigRequest, String deviceName) {
        Element device = response.createElementNS(NM_NS, "device");
        Element deviceNameNode = response.createElementNS(NM_NS, "device-name");
        deviceNameNode.setTextContent(deviceName);
        device.appendChild(deviceNameNode);

        buildDeviceManagementConfigElement(response, device, deviceName);

        return buildDeviceElementAfterFilter(device, getConfigRequest.getFilter());
    }

    private Element buildManagedDevices(Document request, Document response, GetRequest getRequest) {
        Element managedDevice = null;

        Iterator<Map.Entry<String, DeviceAdapterInfo>> iterator;
        iterator = m_deviceTypeMap.entrySet().iterator();
        Map.Entry<String, DeviceAdapterInfo> entry;

        try {
            while (iterator.hasNext()) {
                if (managedDevice == null) {
                    managedDevice = response.createElementNS(NM_NS, "managed-devices");
                }

                entry = iterator.next();
                Element device = buildDeviceElement(request, response, getRequest, entry.getKey());
                if (device != null) {
                    managedDevice.appendChild(device);
                }
            }
        } catch (NullPointerException ex) {
            //Null
        }

        return managedDevice;
    }

    private Element buildManagedDevices(Document response, GetConfigRequest getConfigRequest) {
        Element managedDevice = null;

        Iterator<Map.Entry<String, DeviceAdapterInfo>> iterator;
        iterator = m_deviceTypeMap.entrySet().iterator();
        Map.Entry<String, DeviceAdapterInfo> entry;

        try {
            while (iterator.hasNext()) {
                if (managedDevice == null) {
                    managedDevice = response.createElementNS(NM_NS, "managed-devices");
                }

                entry = iterator.next();
                Element device = buildDeviceElement(response, getConfigRequest, entry.getKey());
                if (device != null) {
                    managedDevice.appendChild(device);
                }
            }
        } catch (NullPointerException ex) {
            //Null
        }

        return managedDevice;
    }

    private String deviceGet(Document document, GetRequest getRequest) throws DispatchException {
        NetConfResponse netConfResponse = new NetConfResponse().setMessageId(getRequest.getMessageId());

        Document responseDocument = NetconfMessageUtil.getDocumentFromResponse(netConfResponse);
        netConfResponse.addDataContent(buildManagedDevices(document, responseDocument, getRequest));
        netConfResponse.addDataContent(buildNewDevices(document, responseDocument, getRequest));

        return netConfResponse.responseToString();
    }

    private Element buildNewDevices(Document document, Document responseDocument, GetRequest getRequest) {
        List<NewDeviceInfo> newDevices = m_deviceManager.getNewDevices();
        Element newDevicesElement = responseDocument.createElementNS(NM_NS, "new-devices");
        for (NewDeviceInfo newDevice : newDevices) {
            Element newDeviceElement = responseDocument.createElementNS(NM_NS, "new-device");
            newDevicesElement.appendChild(newDeviceElement);
            newDeviceElement.appendChild(buildDuidElement(newDevice, responseDocument));
            for (String capability : newDevice.getCapabilities()) {
                Element capElement = responseDocument.createElementNS(NM_NS, "device-capability");
                capElement.setTextContent(capability);
                newDeviceElement.appendChild(capElement);
            }
        }
        return newDevicesElement;
    }

    private Element buildDuidElement(NewDeviceInfo newDevice, Document responseDocument) {
        Element duid = responseDocument.createElementNS(NM_NS, "duid");
        duid.setTextContent(newDevice.getDuid());
        return duid;
    }

    private String deviceGetConfig(Document document, GetConfigRequest getConfigRequest) throws DispatchException {
        NetConfResponse netConfResponse = new NetConfResponse().setMessageId(getConfigRequest.getMessageId());

        Document responseDocument = NetconfMessageUtil.getDocumentFromResponse(netConfResponse);
        netConfResponse.addDataContent(buildManagedDevices(responseDocument, getConfigRequest));
        return netConfResponse.responseToString();
    }

    private String deviceManagement(Document document) throws DispatchException {
        String typeOfNetconfRequest = NetconfMessageUtil.getTypeOfNetconfRequest(document);

        try {
            switch (typeOfNetconfRequest) {
                case NetconfResources.DELETE_CONFIG:
                    return deviceDeleteConfig(document, DocumentToPojoTransformer.getDeleteConfig(document));

                case NetconfResources.EDIT_CONFIG:
                    return devicesEditConfig(document, DocumentToPojoTransformer.getEditConfig(document));

                case NetconfResources.GET:
                    return deviceGet(document, DocumentToPojoTransformer.getGet(document));

                case NetconfResources.GET_CONFIG:
                    return deviceGetConfig(document, DocumentToPojoTransformer.getGetConfig(document));

                default:
                    // Does not support
                    throw new DispatchException("Does not support the operation.");
            }
        } catch (NetconfMessageBuilderException ex) {
            throw new DispatchException(ex);
        }
    }

    public void removeDeviceAdptInfo(String deviceName) {
        m_deviceTypeMap.remove(deviceName);
    }

    public void updateDeviceAdptInfo(String deviceName, DeviceAdapterInfo deviceAdapterInfo) {
        if (m_deviceTypeMap.get(deviceName) == null) {
            m_deviceTypeMap.put(deviceName, deviceAdapterInfo);
            return;
        }

        m_deviceTypeMap.replace(deviceName, deviceAdapterInfo);
    }

    private DeviceAdapterInfo getDeviceAdptInfo(String deviceName) {
        try {
            return m_deviceTypeMap.get(deviceName);
        } catch (NullPointerException | ClassCastException ex) {
            return new DeviceAdapterInfo();
        }
    }

    @Override
    public String getDeviceTypeByDeviceName(String deviceName) {
        DeviceAdapterInfo deviceAdapterInfo = getDeviceAdptInfo(deviceName);
        if (deviceAdapterInfo == null) {
            return null;
        }

        return deviceAdapterInfo.getType();
    }
}
