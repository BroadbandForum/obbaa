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

package org.broadband_forum.obbaa.pma.impl;

import static org.broadband_forum.obbaa.netconf.server.RequestTask.CURRENT_REQ;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.device.adapter.AdapterUtils;
import org.broadband_forum.obbaa.device.adapter.DeviceInterface;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.DeviceMgmt;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AbstractSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ChangeNotification;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemValidationException;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.pma.DeviceXmlStore;
import org.broadband_forum.obbaa.pma.NetconfDeviceAlignmentService;
import org.broadband_forum.obbaa.pma.PmaServer;
import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DeviceSubsystem extends AbstractSubSystem {
    private static final Logger LOGGER = Logger.getLogger(DeviceSubsystem.class);
    private SchemaRegistry m_schemaRegistry;
    private final NetconfDeviceAlignmentService m_das;
    private final DeviceSubsystemResponseUtil m_util;
    private AdapterManager m_adapterManager;

    public DeviceSubsystem(NetconfDeviceAlignmentService das, SchemaRegistry schemaRegistry,
                           AdapterManager adapterManager) {
        m_das = das;
        m_schemaRegistry = schemaRegistry;
        m_adapterManager = adapterManager;
        m_util = new DeviceSubsystemResponseUtil(m_schemaRegistry);
    }

    @Override
    protected Map<ModelNodeId, List<Element>> retrieveStateAttributes(Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> attributes) {
        GetRequest getRequest = prepareRequest(attributes);
        try {
            Device device = PmaServer.getCurrentDevice();
            DeviceInterface deviceInterface = AdapterUtils.getAdapterContext(device, m_adapterManager).getDeviceInterface();
            Future<NetConfResponse> future = deviceInterface.get(device, getRequest);
            NetConfResponse response = null;
            String deviceType = null;
            DeviceMgmt deviceMgmt = device.getDeviceManagement();
            if (deviceMgmt != null) {
                deviceType = deviceMgmt.getDeviceType();
            }
            if (deviceType != null && deviceType.equals("ONU")) {
                LOGGER.info("Device type is ONU, waiting for response from vOMCI");
            } else {
                response = future.get();
            }
            if (response != null) {
                return m_util.getStateResponse(attributes, response.getData());
            }
        } catch (ExecutionException | InterruptedException | IllegalStateException e) {
            LOGGER.error("Error while retrieving state attributes", e);
        }
        return Collections.emptyMap();
    }

    private GetRequest prepareRequest(Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> attributes) {
        GetRequest getRequest = new GetRequest();
        NetconfFilter filter = new NetconfFilter().setType("subtree");
        getRequest.setFilter(filter);
        for (Map.Entry<ModelNodeId, Pair<List<QName>, List<FilterNode>>> entry : attributes.entrySet()) {
            FilterNode filterNode = FilterUtil.nodeIdToFilter(entry.getKey());

            addSelectNodes(entry, filterNode);
            addSubFilters(entry, filterNode);

            filter.addXmlFilter(FilterUtil.filterToXml(m_schemaRegistry, filterNode));
        }

        return getRequest;
    }

    private void addSubFilters(Map.Entry<ModelNodeId, Pair<List<QName>, List<FilterNode>>> entry, FilterNode filterNode) {
        List<FilterNode> subFilters = entry.getValue().getSecond();
        if (subFilters != null) {
            for (FilterNode subFilter : subFilters) {
                if (subFilter.isSelectNode()) {
                    filterNode.addSelectNode(subFilter);
                } else {
                    filterNode.addContainmentNode(subFilter);
                }
            }
        }
    }

    private void addSelectNodes(Map.Entry<ModelNodeId, Pair<List<QName>, List<FilterNode>>> entry, FilterNode filterNode) {
        List<QName> selectNodes = entry.getValue().getFirst();
        if (selectNodes != null) {
            for (QName selectNode : selectNodes) {
                filterNode.addSelectNode(new FilterNode(selectNode));
            }
        }
    }

    @Override
    public void notifyChanged(List<ChangeNotification> changeNotificationList) {
        super.notifyChanged(changeNotificationList);
        m_das.queueEdit(PmaServer.getCurrentDevice().getDeviceName(), (EditConfigRequest) RequestScope.getCurrentScope()
                .getFromCache(CURRENT_REQ));
    }

    @Override
    public void notifyPreCommitChange(List<ChangeNotification> changeNotificationList) throws
            SubSystemValidationException {
        Device device = PmaServer.getCurrentDevice();
        DeviceXmlStore updatedDeviceStore = PmaServer.getCurrentDeviceXmlStore();
        DeviceXmlStore oldDataStore = PmaServer.getBackupDeviceXmlStore();
        Document updatedDataStoreDoc = null;
        Document oldDataStoreDoc = null;
        try {
            if (updatedDeviceStore.getDeviceXml() != null && !updatedDeviceStore.getDeviceXml().isEmpty()) {
                updatedDataStoreDoc = DocumentUtils.stringToDocument(updatedDeviceStore.getDeviceXml());
            }
            if (oldDataStore.getDeviceXml() != null && !oldDataStore.getDeviceXml().isEmpty()) {
                oldDataStoreDoc = DocumentUtils.stringToDocument(oldDataStore.getDeviceXml());
            }
        } catch (NetconfMessageBuilderException e) {
            throw new RuntimeException(e.getMessage());
        }
        if (device != null) {
            DeviceInterface deviceInterface = AdapterUtils.getAdapterContext(device, m_adapterManager).getDeviceInterface();
            try {
                deviceInterface.veto(device, (EditConfigRequest) RequestScope.getCurrentScope().getFromCache(CURRENT_REQ),
                        oldDataStoreDoc, updatedDataStoreDoc);
            } catch (SubSystemValidationException e) {
                updatedDeviceStore.setDeviceXml(oldDataStore.getDeviceXml());
                throw e;

            }
        }


    }
}
