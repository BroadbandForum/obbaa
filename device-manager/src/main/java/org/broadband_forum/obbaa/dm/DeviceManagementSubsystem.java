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

package org.broadband_forum.obbaa.dm;


import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.AUTH_ID_TEMPLATE;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.CONFIGURATION_ALIGNMENT_STATE;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.CONNECTED;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.CONNECTION_CREATION_TIME;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.CONNECTION_STATE;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.DEVICE_ADAPTER;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.DEVICE_ADAPTERS;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.DEVICE_ADAPTERS_SP;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.DEVICE_CAPABILITY;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.DEVICE_ID_TEMPLATE;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.DEVICE_STATE;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.DUID;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.INTERFACE_VERSION;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.MANAGED_DEVICES_ID_TEMPLATE;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.MANAGED_DEVICES_SP;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.MODEL;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.NETWORK_MANAGER_ID_TEMPLATE;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.NEW_DEVICE;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.NEW_DEVICES;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.NEW_DEVICES_SP;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.NS;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.TYPE;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.VENDOR;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.Transactional;

import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.connectors.sbi.netconf.NewDeviceInfo;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapter;
import org.broadband_forum.obbaa.dmyang.entities.ConnectionState;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.DeviceState;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AbstractSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ChangeNotification;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigChangeNotification;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeChange;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeChangeType;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.SubtreeFilterUtil;
import org.opendaylight.yangtools.yang.common.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DeviceManagementSubsystem extends AbstractSubSystem {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceManagementSubsystem.class);
    private DeviceManager m_deviceManager;
    private SchemaRegistry m_schemaRegistry;
    private SubtreeFilterUtil m_subtreeFilterUtil;
    private NetconfConnectionManager m_connectionManager;
    private AdapterManager m_adapterManager;

    public DeviceManagementSubsystem(SchemaRegistry schemaRegistry, AdapterManager adapterManager) {
        m_schemaRegistry = schemaRegistry;
        m_adapterManager = adapterManager;
        m_subtreeFilterUtil = new SubtreeFilterUtil(m_schemaRegistry);
    }

    public DeviceManager getDeviceManager() {
        return m_deviceManager;
    }

    public void setDeviceManager(DeviceManager deviceManager) {
        m_deviceManager = deviceManager;
    }

    public NetconfConnectionManager getConnectionManager() {
        return m_connectionManager;
    }

    public void setConnectionManager(NetconfConnectionManager connectionManager) {
        m_connectionManager = connectionManager;
    }

    @Override
    public void notifyChanged(List<ChangeNotification> changeNotificationList) {
        LOGGER.debug("notification received : {}", changeNotificationList);
        for (ChangeNotification notification : changeNotificationList) {
            EditConfigChangeNotification editNotif = (EditConfigChangeNotification) notification;
            LOGGER.debug("notification received : {}", editNotif);
            ModelNodeId nodeId = editNotif.getModelNodeId();
            EditContainmentNode changeData = editNotif.getChange().getChangeData();
            if (nodeId.equals(MANAGED_DEVICES_ID_TEMPLATE) && "device".equals(editNotif.getChange().getChangeData().getName())) {
                LOGGER.debug("ModelNodeId[{}] matched device holder template", nodeId);
                handleDeviceCreateOrDelete(nodeId, editNotif);
            } else if (nodeId.matchesTemplate(AUTH_ID_TEMPLATE)) {
                handleAuthChanged(nodeId.getRdnValue("name"), changeData);
            }
        }
    }

    private void handleAuthChanged(String deviceName, EditContainmentNode editNotif) {
        m_deviceManager.devicePropertyChanged(deviceName);
    }

    private void handleDeviceCreateOrDelete(ModelNodeId nodeId, EditConfigChangeNotification editNotif) {
        LOGGER.debug(null, "Handle device create or delete for ModelNodeId[{}] with notification[{}]", nodeId, editNotif);
        ModelNodeChange deviceChange = editNotif.getChange();
        String deviceId = deviceChange.getChangeData().getMatchNodes().get(0).getValue();
        // device added
        if (editNotif.getChange().getChangeType().equals(ModelNodeChangeType.create)) {
            LOGGER.debug(null, "Device create identified for  ModelNodeId[{}] with notification[{}]", nodeId, editNotif);
            m_deviceManager.deviceAdded(deviceId);
        } else if (editNotif.getChange().getChangeType().equals(ModelNodeChangeType.delete)
            || editNotif.getChange().getChangeType().equals(ModelNodeChangeType.remove)) {

            LOGGER.debug(null, "Device delete identified for ModelNodeId[{}] with notification[{}]", nodeId, editNotif);
            m_deviceManager.deviceRemoved(deviceId);

        }
    }

    @Override
    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = {RuntimeException.class,Exception.class})
    protected Map<ModelNodeId, List<Element>> retrieveStateAttributes(Map<ModelNodeId, Pair<List<QName>,
        List<FilterNode>>> mapAttributes) throws GetAttributeException {
        Map<ModelNodeId, List<Element>> stateInfo = new HashMap<>();
        try {
            Document document = DocumentUtils.createDocument();
            for (Map.Entry<ModelNodeId, Pair<List<QName>, List<FilterNode>>> entry : mapAttributes.entrySet()) {
                List<Element> stateElements = new ArrayList<>();
                ModelNodeId nodeId = entry.getKey();
                List<FilterNode> filters = entry.getValue().getSecond();
                Element deviceStateElement = null;
                Element newDevicesElement = null;
                Element deviceAdapterElement = null;
                if (nodeId.matchesTemplate(DEVICE_ID_TEMPLATE)) {
                    Device device = getDevice(nodeId.getRdnValue("name"));
                    if (device != null) {
                        deviceStateElement = buildDeviceStateElement(document, device);
                    }
                } else if (nodeId.matchesTemplate(NETWORK_MANAGER_ID_TEMPLATE)) {
                    List<NewDeviceInfo> newDeviceInfos = m_connectionManager.getNewDevices();
                    List<DeviceAdapter> adapters = new ArrayList<>(m_adapterManager.getAllDeviceAdapters());
                    if (!newDeviceInfos.isEmpty()) {
                        newDevicesElement = buildNewDeviceStateElement(document, newDeviceInfos);
                    }
                    if (!adapters.isEmpty()) {
                        deviceAdapterElement = buildAdaptersStateElement(document, adapters);
                    }
                }
                for (FilterNode filter : filters) {
                    if (DEVICE_STATE.equals(filter.getNodeName()) && NS.equals(filter.getNamespace())) {
                        Element filteredDeviceStateElement = document.createElementNS(NS, DEVICE_STATE);
                        m_subtreeFilterUtil.doFilter(document, filter, m_schemaRegistry.getDataSchemaNode(MANAGED_DEVICES_SP),
                            deviceStateElement, filteredDeviceStateElement);
                        deviceStateElement = filteredDeviceStateElement;
                        if (deviceStateElement != null) {
                            stateElements.add(deviceStateElement);
                        }
                    } else if (NEW_DEVICES.equals(filter.getNodeName()) && NS.equals(filter.getNamespace())) {
                        Element filteredNewDevicesElement = document.createElementNS(NS, NEW_DEVICES);
                        m_subtreeFilterUtil.doFilter(document, filter, m_schemaRegistry.getDataSchemaNode(NEW_DEVICES_SP),
                            newDevicesElement, filteredNewDevicesElement);
                        newDevicesElement = filteredNewDevicesElement;
                        if (newDevicesElement != null) {
                            stateElements.add(newDevicesElement);
                        }
                    } else if (DEVICE_ADAPTERS.equals(filter.getNodeName()) && NS.equals(filter.getNamespace())) {
                        Element filteredDeviceAdaptersElement = document.createElementNS(NS, DEVICE_ADAPTERS);
                        m_subtreeFilterUtil.doFilter(document, filter, m_schemaRegistry.getDataSchemaNode(DEVICE_ADAPTERS_SP),
                            deviceAdapterElement, filteredDeviceAdaptersElement);
                        deviceAdapterElement = filteredDeviceAdaptersElement;
                        if (deviceAdapterElement != null) {
                            stateElements.add(deviceAdapterElement);
                        }
                    }
                    stateInfo.put(nodeId, stateElements);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error in retrieveStateAttributes: ", e);
            throw e;
        }
        return stateInfo;
    }

    private Element buildAdaptersStateElement(Document document, List<DeviceAdapter> adapters) {
        if (!adapters.isEmpty()) {
            Element deviceAdapters = document.createElementNS(NS,DEVICE_ADAPTERS);
            for (DeviceAdapter info : adapters) {
                Element deviceAdapter = document.createElementNS(NS, DEVICE_ADAPTER);
                appendElement(document, deviceAdapter, TYPE, info.getType());
                appendElement(document, deviceAdapter, INTERFACE_VERSION, info.getInterfaceVersion());
                appendElement(document, deviceAdapter, MODEL, info.getModel());
                appendElement(document, deviceAdapter, VENDOR, info.getVendor());
                deviceAdapters.appendChild(deviceAdapter);
            }
            return deviceAdapters;
        }
        return null;
    }

    private Element buildNewDeviceStateElement(Document document, List<NewDeviceInfo> newDeviceInfos) {
        if (!newDeviceInfos.isEmpty()) {
            Element newdevices = document.createElementNS(NS,NEW_DEVICES);
            for (NewDeviceInfo info : newDeviceInfos) {
                Element newDevice = document.createElementNS(NS, NEW_DEVICE);
                appendElement(document, newDevice, DUID, info.getDuid());
                Set<String> deviceCaps = info.getCapabilities();
                appendElement(document, newDevice, DEVICE_CAPABILITY,
                    deviceCaps.toArray(new String[deviceCaps.size()]));


                newdevices.appendChild(newDevice);
            }
            return newdevices;
        }
        return null;
    }

    private Element buildDeviceStateElement(Document document, Device device) {
        DeviceState deviceState = device.getDeviceManagement().getDeviceState();
        if (deviceState != null) {
            Element deviceStateElem = document.createElementNS(NS, DEVICE_STATE);
            appendElement(document, deviceStateElem, CONFIGURATION_ALIGNMENT_STATE,
                String.valueOf(deviceState.getConfigAlignmentState()));
            Element connectionState = document.createElementNS(NS, CONNECTION_STATE);
            ConnectionState state = m_connectionManager.getConnectionState(device.getDeviceName());
            appendElement(document, connectionState, CONNECTED,
                String.valueOf(state.isConnected()));
            appendElement(document, connectionState, CONNECTION_CREATION_TIME,
                state.getConnectionCreationTimeFormat());
            Set<String> deviceCaps = state.getDeviceCapability();
            appendElement(document, connectionState, DEVICE_CAPABILITY,
                deviceCaps.toArray(new String[deviceCaps.size()]));
            deviceStateElem.appendChild(connectionState);
            return deviceStateElem;
        }
        return null;
    }

    private Element appendElement(Document document, Element parentElement, String localName, String... textContents) {
        Element lastElement = null;
        for (String textContent : textContents) {
            Element element = document.createElementNS(NS, localName);
            element.setTextContent(textContent);
            parentElement.appendChild(element);
            lastElement = element;
        }
        return lastElement;
    }

    private Device getDevice(String device) {
        return m_deviceManager.getDevice(device);
    }
}
