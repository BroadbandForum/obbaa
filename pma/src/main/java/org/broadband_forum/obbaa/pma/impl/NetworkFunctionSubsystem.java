/*
 * Copyright 2022 Broadband Forum
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

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AbstractSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ActionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ChangeNotification;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemValidationException;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.pma.NetconfNetworkFunctionAlignmentService;
import org.broadband_forum.obbaa.pma.PmaServer;
import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Element;


public class NetworkFunctionSubsystem extends AbstractSubSystem {
    private static final Logger LOGGER = Logger.getLogger(NetworkFunctionSubsystem.class);
    private final NetconfNetworkFunctionAlignmentService m_nas;

    public NetworkFunctionSubsystem(NetconfNetworkFunctionAlignmentService nas) {
        m_nas = nas;
    }

    @Override
    protected Map<ModelNodeId, List<Element>> retrieveStateAttributes(Map<ModelNodeId, Pair<List<QName>,
            List<FilterNode>>> attributes) throws GetAttributeException {
        LOGGER.warn("Not implemented for network functions. Returning empty map.");
        return super.retrieveStateAttributes(attributes);
    }

    @Override
    public void notifyChanged(List<ChangeNotification> changeNotificationList) {
        super.notifyChanged(changeNotificationList);
        m_nas.queueEdit(PmaServer.getCurrentNetworkFunction().getNetworkFunctionName(),
                 (EditConfigRequest) RequestScope.getCurrentScope().getFromCache(CURRENT_REQ));
    }

    @Override
    public void notifyPreCommitChange(List<ChangeNotification> changeNotificationList) throws SubSystemValidationException {

    }

    @Override
    public List<Element> executeAction(ActionRequest actionRequest) throws ActionException {
        LOGGER.info("Executing action on Network function subsystem");
        return m_nas.executeAction(PmaServer.getCurrentNetworkFunction(), actionRequest);
    }
}
