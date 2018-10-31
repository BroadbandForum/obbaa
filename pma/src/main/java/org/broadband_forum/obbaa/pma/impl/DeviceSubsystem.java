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

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AbstractSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ChangeNotification;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.pma.NetconfDeviceAlignmentService;
import org.broadband_forum.obbaa.pma.PmaServer;
import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Element;

public class DeviceSubsystem extends AbstractSubSystem {
    private static final Logger LOGGER = Logger.getLogger(DeviceSubsystem.class);
    private final NetconfConnectionManager m_dcm;
    private SchemaRegistry m_schemaRegistry;
    private final NetconfDeviceAlignmentService m_das;
    private final DeviceSubsystemResponseUtil m_util;

    public DeviceSubsystem(NetconfConnectionManager dcm, NetconfDeviceAlignmentService das, SchemaRegistry schemaRegistry) {
        m_dcm = dcm;
        m_das = das;
        m_schemaRegistry = schemaRegistry;
        m_util = new DeviceSubsystemResponseUtil(m_schemaRegistry);
    }

    @Override
    protected Map<ModelNodeId, List<Element>> retrieveStateAttributes(Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> attributes) {
        GetRequest getRequest = prepareRequest(attributes);
        try {
            NetConfResponse response = m_dcm.executeNetconf(PmaServer.getCurrentDevice(), getRequest).get();
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
}
