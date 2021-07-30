/*
 * Copyright 2021 Broadband Forum
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
package org.broadband_forum.obbaa.nm.nwfunctionmgr;

import static org.broadband_forum.obbaa.nf.entities.NetworkFunctionNSConstants.NETWORK_FUNCTION;
import static org.broadband_forum.obbaa.nf.entities.NetworkFunctionNSConstants.NETWORK_FUNCTIONS_ID_TEMPLATE;

import java.util.List;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AbstractSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ChangeNotification;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigChangeNotification;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeChangeType;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkFunctionSubsystem extends AbstractSubSystem {


    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkFunctionSubsystem.class);
    private NetworkFunctionManager m_networkFunctionManager;

    public NetworkFunctionSubsystem(NetworkFunctionManager networkFunctionManager) {
        m_networkFunctionManager = networkFunctionManager;
    }

    @Override
    public void notifyChanged(List<ChangeNotification> changeNotificationList) {
        LOGGER.debug("notification received : {}", changeNotificationList);
        for (ChangeNotification notification : changeNotificationList) {
            EditConfigChangeNotification editNotif = (EditConfigChangeNotification) notification;
            LOGGER.debug("notification received : {}", editNotif);
            ModelNodeId nodeId = editNotif.getModelNodeId();
            if (nodeId.equals(NETWORK_FUNCTIONS_ID_TEMPLATE)
                    && NETWORK_FUNCTION.equals(editNotif.getChange().getChangeData().getName())) {
                LOGGER.debug("ModelNodeId[{}] matched network function template", nodeId);
                handleNwFunctionCreateOrDelete(nodeId, editNotif);
            }
        }
    }

    private void handleNwFunctionCreateOrDelete(ModelNodeId nodeId, EditConfigChangeNotification editNotif) {
        LOGGER.debug(null, "Handle Network function create or delete for ModelNodeId[{}] with notification[{}]", nodeId, editNotif);
        String networkFunctionName = editNotif.getChange().getChangeData().getMatchNodes().get(0).getValue();
        if (editNotif.getChange().getChangeType().equals(ModelNodeChangeType.create)) {
            LOGGER.debug(null, "Network Function create identified for  ModelNodeId[{}] with notification[{}]", nodeId, editNotif);
            m_networkFunctionManager.networkFunctionAdded(networkFunctionName);
        } else if (editNotif.getChange().getChangeType().equals(ModelNodeChangeType.delete)
                || editNotif.getChange().getChangeType().equals(ModelNodeChangeType.remove)) {
            LOGGER.debug(null, "Network Function delete identified for ModelNodeId[{}] with notification[{}]", nodeId, editNotif);
            m_networkFunctionManager.networkFunctionRemoved(networkFunctionName);
        }
    }
}
