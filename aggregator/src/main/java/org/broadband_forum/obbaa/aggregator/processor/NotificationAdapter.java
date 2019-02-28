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


import java.util.HashSet;
import java.util.Set;

import org.broadband_forum.obbaa.aggregator.api.Aggregator;
import org.broadband_forum.obbaa.aggregator.api.DispatchException;
import org.broadband_forum.obbaa.aggregator.api.GlobalRequestProcessor;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ModuleIdentifier;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetconfServer;
import org.broadband_forum.obbaa.netconf.server.model.notification.utils.NotificationConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class NotificationAdapter implements GlobalRequestProcessor {

    private Aggregator m_aggregator;
    private NetconfServer m_netconfServer;

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationAdapter.class);

    private NotificationAdapter() {
        //Hide
    }

    public NotificationAdapter(Aggregator aggregator, NetconfServer netconfServer) {
        this.m_aggregator = aggregator;
        this.m_netconfServer = netconfServer;
    }

    public void init() {
        try {
            m_aggregator.addProcessor(buildModuleIdentifiers(), this);
        } catch (DispatchException ex) {
            LOGGER.error("NotificationAdapter init failed");
        }
    }

    private Set<ModuleIdentifier> buildModuleIdentifiers() {
        Set<ModuleIdentifier> moduleIdentifiers = new HashSet<>();
        ModuleIdentifier moduleIdentifier = NetconfMessageUtil.buildModuleIdentifier(NotificationConstants.MODULE_NAME,
                NotificationConstants.NAMESPACE, NotificationConstants.YANG_REVISION);
        moduleIdentifiers.add(moduleIdentifier);

        return moduleIdentifiers;
    }

    public void destroy() {
        try {
            m_aggregator.removeProcessor(this);
        } catch (DispatchException ex) {
            LOGGER.error("NotificationAdapter destroy failed");
        }
    }

    @Override
    public String processRequest(NetconfClientInfo clientInfo, String netconfRequest) throws DispatchException {
        Document document = AggregatorMessage.stringToDocument(netconfRequest);
        String messageId = NetconfMessageUtil.getMessageIdFromRpcDocument(document);
        NetConfResponse response = notifManagement(clientInfo, document);
        response.setMessageId(messageId);

        return response.responseToString();
    }

    private NetConfResponse notifManagement(NetconfClientInfo netconfClientInfo, Document document) throws DispatchException {
        String typeOfNetconfRequest = NetconfMessageUtil.getTypeOfNetconfRequest(document);
        NetConfResponse response = new NetConfResponse();

        try {
            switch (typeOfNetconfRequest) {
                case NetconfResources.GET:
                    m_netconfServer.onGet(netconfClientInfo, DocumentToPojoTransformer.getGet(document), response);
                    break;

                case NetconfResources.GET_CONFIG:
                    m_netconfServer.onGetConfig(netconfClientInfo, DocumentToPojoTransformer.getGetConfig(document), response);
                    break;

                default:
                    // Does not support
                    throw new DispatchException("Does not support the operation.");
            }
        } catch (NetconfMessageBuilderException ex) {
            throw new DispatchException(ex);
        }
        return response;
    }
}
