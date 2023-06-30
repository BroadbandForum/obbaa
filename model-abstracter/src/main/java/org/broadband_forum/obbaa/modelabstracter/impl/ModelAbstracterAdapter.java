/*
 * Copyright 2022 Broadband Forum
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadband_forum.obbaa.modelabstracter.impl;

import org.apache.commons.lang3.StringUtils;
import org.broadband_forum.obbaa.aggregator.api.Aggregator;
import org.broadband_forum.obbaa.aggregator.api.DispatchException;
import org.broadband_forum.obbaa.aggregator.api.GlobalRequestProcessor;
import org.broadband_forum.obbaa.aggregator.processor.AggregatorMessage;
import org.broadband_forum.obbaa.aggregator.processor.NetconfMessageUtil;
import org.broadband_forum.obbaa.modelabstracter.ModelAbstracterManager;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ModuleIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * New NBI YANG model abstracter adapter.
 */
public class ModelAbstracterAdapter implements GlobalRequestProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelAbstracterAdapter.class);

    private static final String SEPARATOR = ":";

    private final Aggregator m_aggregator;

    private final ModelAbstracterManager m_modelAbstracterManager;

    private static final Map<String, String> MODEL_ADAPTER_NAMESPACES = new HashMap<>();

    static {
        MODEL_ADAPTER_NAMESPACES.put("urn:ietf:params:xml:ns:yang:ietf-network", "2021-09-14");
        MODEL_ADAPTER_NAMESPACES.put("urn:ietf:params:xml:ns:yang:ietf-network-topology", "2021-09-14");
        MODEL_ADAPTER_NAMESPACES.put("urn:bbf:yang:bbf-equipment-inventory", "2021-09-14");
        MODEL_ADAPTER_NAMESPACES.put("urn:bbf:yang:obbaa:an-network-topology", "2021-09-14");
        MODEL_ADAPTER_NAMESPACES.put("urn:bbf:yang:obbaa:l2-access-common", "2021-09-30");
        MODEL_ADAPTER_NAMESPACES.put("urn:bbf:yang:obbaa:l2-topology", "2021-09-14");
        MODEL_ADAPTER_NAMESPACES.put("urn:bbf:yang:obbaa:nt-line-profile", "2021-09-14");
        MODEL_ADAPTER_NAMESPACES.put("urn:bbf:yang:obbaa:nt-service-profile", "2021-09-14");
    }

    public ModelAbstracterAdapter(Aggregator aggregator, ModelAbstracterManager modelAbstracterManager) {
        this.m_aggregator = aggregator;
        this.m_modelAbstracterManager = modelAbstracterManager;
    }

    public void init() {
        try {
            Set<ModuleIdentifier> identifiers = MODEL_ADAPTER_NAMESPACES.entrySet()
                .stream()
                .map(entry -> NetconfMessageUtil.buildModuleIdentifier(
                    StringUtils.substringAfterLast(entry.getKey(), SEPARATOR), entry.getKey(), entry.getValue()))
                .collect(Collectors.toSet());
            m_aggregator.addProcessor(identifiers, this);
        } catch (DispatchException ex) {
            LOGGER.error("NewNbiYangModelAdapter init failed");
        }
    }

    public void destroy() {
        try {
            m_aggregator.removeProcessor(this);
        } catch (DispatchException ex) {
            LOGGER.error("NewNbiYangModelAdapter destroy failed");
        }
    }

    @Override
    public String processRequest(NetconfClientInfo clientInfo, String request) throws DispatchException {
        LOGGER.info("Request msg is {}.", request);
        Document document = AggregatorMessage.stringToDocument(request);
        String typeOfNetconfRequest = NetconfMessageUtil.getTypeOfNetconfRequest(document);
        // Process edit-config request only
        if (!NetconfResources.EDIT_CONFIG.equals(typeOfNetconfRequest)) {
            throw new DispatchException("Does not support the operation.");
        }
        try {
            EditConfigRequest editConfigRequest = DocumentToPojoTransformer.getEditConfig(document);
            NetConfResponse response = m_modelAbstracterManager.execute(clientInfo, editConfigRequest, request);
            String messageId = NetconfMessageUtil.getMessageIdFromRpcDocument(document);
            response.setMessageId(messageId);
            return response.responseToString();
        } catch (NetconfMessageBuilderException | ExecutionException e) {
            LOGGER.error("error while sending edit-config request", e);
            throw new DispatchException(e);
        }
    }
}
