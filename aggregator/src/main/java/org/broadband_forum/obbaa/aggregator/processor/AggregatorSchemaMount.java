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
import org.broadband_forum.obbaa.aggregator.jaxb.networkmanager.api.NetworkManagerRpc;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ModuleIdentifier;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AggregatorSchemaMount implements GlobalRequestProcessor {
    Aggregator m_aggregator;
    private static final String SCHEMA_MOUNT_NS = "urn:ietf:params:xml:ns:yang:ietf-yang-schema-mount";

    private AggregatorSchemaMount() {
        //Hide
    }

    public AggregatorSchemaMount(Aggregator aggregator) {
        m_aggregator = aggregator;
    }

    public void init() {
        ModuleIdentifier moduleIdentifier = NetconfMessageUtil.buildModuleIdentifier("ietf-yang-schema-mount",
                SCHEMA_MOUNT_NS,"2018-04-05");

        Set<ModuleIdentifier> moduleIdentifiers = new HashSet<>();
        moduleIdentifiers.add(moduleIdentifier);
        try {
            m_aggregator.addProcessor(moduleIdentifiers, this);
        }
        catch (DispatchException ex) {
            //Ignore
        }
    }

    public void destroy() {
        try {
            m_aggregator.removeProcessor(this);
        }
        catch (DispatchException ex) {
            //Ignore
        }
    }

    @Override
    public String processRequest(NetconfClientInfo clientInfo, String netconfRequest) throws DispatchException {
        Document document = AggregatorMessage.stringToDocument(netconfRequest);

        //Device name is invalid in this processor
        return processSchemaMountRequest(document);
    }

    private Element buildNetworkManageNamespaces(Document request, Document response) {
        Element namespace = response.createElement("namespace");
        NetconfMessageUtil.appendNewElement(response, namespace, "prefix", "network-manager");
        NetconfMessageUtil.appendNewElement(response, namespace,
                "uri", NetworkManagerRpc.NAMESPACE);

        return namespace;
    }

    private Element buildNetworkManageMountPoint(Document request, Document response) {
        Element mountPoint = response.createElement("mount-point");
        NetconfMessageUtil.appendNewElement(response, mountPoint, "module", "network-manager");
        NetconfMessageUtil.appendNewElement(response, mountPoint, "label", "root");
        NetconfMessageUtil.appendNewElement(response, mountPoint, "config", "true");

        return mountPoint;
    }

    private void buildNamespaces(Document request, Document response, Element schemaMounts) {
        schemaMounts.appendChild(buildNetworkManageNamespaces(request, response));
    }

    private void buildMountPoints(Document request, Document response, Element schemaMounts) {
        schemaMounts.appendChild(buildNetworkManageMountPoint(request, response));
    }

    private Element buildSchemaMounts(Document request, Document response) {
        //TODO: Filter not supported, just support network-management.yang
        Element schemaMounts = response.createElement("schema-mounts");
        schemaMounts.setAttribute("xmlns", "urn:ietf:params:xml:ns:yang:ietf-yang-schema-mount");

        buildNamespaces(request, response, schemaMounts);
        buildMountPoints(request, response, schemaMounts);

        return schemaMounts;
    }

    private String processSchemaMountRequest(Document request) throws DispatchException {
        String typeOfNetconfRequest = NetconfMessageUtil.getTypeOfNetconfRequest(request);
        if (!typeOfNetconfRequest.equalsIgnoreCase(NetconfResources.GET)) {
            throw new DispatchException("Does not support the type of operation.");
        }

        String messageId = NetconfMessageUtil.getMessageIdFromRpcDocument(request);
        NetConfResponse netConfResponse = new NetConfResponse().setMessageId(messageId);
        Document responseDocument = NetconfMessageUtil.getDocumentFromResponse(netConfResponse);

        netConfResponse.addDataContent(buildSchemaMounts(request, responseDocument));
        return netConfResponse.responseToString();
    }
}
