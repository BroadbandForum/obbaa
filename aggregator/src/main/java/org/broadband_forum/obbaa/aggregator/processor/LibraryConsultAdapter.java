/*
 *   Copyright 2018 Broadband Forum
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.broadband_forum.obbaa.aggregator.processor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

import org.broadband_forum.obbaa.aggregator.api.Aggregator;
import org.broadband_forum.obbaa.aggregator.api.DeviceConfigProcessor;
import org.broadband_forum.obbaa.aggregator.api.DispatchException;
import org.broadband_forum.obbaa.aggregator.api.GlobalRequestProcessor;
import org.broadband_forum.obbaa.aggregator.jaxb.libconsult.api.QueryYangModuleInUseRpc;
import org.broadband_forum.obbaa.aggregator.jaxb.libconsult.schema.UsedYangModules;
import org.broadband_forum.obbaa.aggregator.jaxb.libconsult.schema.YangModule;
import org.broadband_forum.obbaa.libconsult.LibConsultMgr;
import org.broadband_forum.obbaa.libconsult.utils.LibraryConsultUtils;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ModuleIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.broadband_forum.obbaa.libconsult.utils.LibraryConsultUtils.MODULE_NAME;
import static org.broadband_forum.obbaa.libconsult.utils.LibraryConsultUtils.REVISION;

public class LibraryConsultAdapter implements GlobalRequestProcessor, DeviceConfigProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LibraryConsultAdapter.class);
    private static final String NAME_SPACE = LibraryConsultUtils.NAMESPACE;
    private static final String DEVICE_DPU = "DPU";
    private Aggregator m_aggregator;
    private LibConsultMgr m_libConsultMgr;

    public LibraryConsultAdapter(Aggregator aggregator, LibConsultMgr libConsultMgr) {
        this.m_aggregator = aggregator;
        this.m_libConsultMgr = libConsultMgr;
    }

    public void init() {

        LOGGER.info("LibraryConsultAdapter processor register begin");
        ModuleIdentifier moduleIdentifier = NetconfMessageUtil
                .buildModuleIdentifier(MODULE_NAME, NAME_SPACE, REVISION);

        Set<ModuleIdentifier> moduleIdentifiers = new HashSet<>();
        moduleIdentifiers.add(moduleIdentifier);
        try {
            m_aggregator.addProcessor(moduleIdentifiers, this);
            m_aggregator.addProcessor(DEVICE_DPU, moduleIdentifiers, this);
        }
        catch (DispatchException ex) {
            LOGGER.error("exception caught when processor register :" + ex.getMessage());
        }
        LOGGER.info("LibraryConsultAdapter processor register end");
    }

    public void destroy() {
        try {
            m_aggregator.removeProcessor((GlobalRequestProcessor) this);
            m_aggregator.removeProcessor((DeviceConfigProcessor) this);
        }
        catch (DispatchException ex) {
            //Ignore
        }
    }

    @Override
    public String processRequest(NetconfClientInfo clientInfo, String netconfRequest) throws DispatchException {
        QueryYangModuleInUseRpc queryYangModuleInUseRpc = QueryYangModuleInUseRpc.getInstance(netconfRequest);

        return processRequestInternal(netconfRequest, null, (Document document, String devName) -> {
            UsedYangModules usedYangModules = queryYangModuleInUseRpc.getUsedYangModules();
            if (usedYangModules == null) {
                return null;
            }
            List<YangModule> yangModules = usedYangModules.getYangModules();
            if ((yangModules != null) && (!yangModules.isEmpty())) {
                YangModule filterModule = yangModules.get(0);
                return m_libConsultMgr.getSpecificUsedYangModules(filterModule.getName(), filterModule.getRevision(), document);
            }
            return m_libConsultMgr.getAllUsedYangModules(document);
        });
    }

    @Override
    public String processRequest(String deviceName, String netconfRequest) throws DispatchException {

        return processRequestInternal(netconfRequest, deviceName,
            (document, devName) -> m_libConsultMgr.getUsedYangModulesPerDevice(document, devName));
    }

    private String processRequestInternal(String netconfRequest, String devName,
            BiFunction<Document, String, Element> function) throws DispatchException {
        Document document = AggregatorMessage.stringToDocument(netconfRequest);
        try {
            GetRequest getRequest = DocumentToPojoTransformer.getGet(document);
            NetConfResponse netConfResponse = new NetConfResponse().setMessageId(getRequest.getMessageId());
            Document responseDocument = NetconfMessageUtil.getDocumentFromResponse(netConfResponse);

            Element resultEl = function.apply(responseDocument, devName);
            netConfResponse.addDataContent(resultEl);
            return netConfResponse.responseToString();
        }
        catch (NetconfMessageBuilderException e) {
            throw new DispatchException(e);
        }
    }
}
