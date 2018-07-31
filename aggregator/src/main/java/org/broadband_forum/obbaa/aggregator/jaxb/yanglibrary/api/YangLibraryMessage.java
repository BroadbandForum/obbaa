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

package org.broadband_forum.obbaa.aggregator.jaxb.yanglibrary.api;

import org.broadband_forum.obbaa.aggregator.api.DispatchException;
import org.broadband_forum.obbaa.aggregator.jaxb.netconf.api.NetconfRpcMessage;
import org.broadband_forum.obbaa.aggregator.jaxb.netconf.api.NetconfRpcReplyMessage;
import org.broadband_forum.obbaa.aggregator.jaxb.netconf.api.NetconfVersionManager;
import org.broadband_forum.obbaa.aggregator.jaxb.netconf.schema.rpc.Rpc;
import org.broadband_forum.obbaa.aggregator.jaxb.netconf.schema.rpc.RpcOperationType;
import org.broadband_forum.obbaa.aggregator.jaxb.utils.JaxbUtils;
import org.broadband_forum.obbaa.aggregator.jaxb.yanglibrary.schema.library.YangLibrary;
import org.broadband_forum.obbaa.aggregator.jaxb.yanglibrary.schema.state.ModulesState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.List;

/**
 * Rpc request based on IETF YANG library module.
 */
public class YangLibraryMessage extends NetconfRpcMessage {
    private Rpc rpc;
    private YangLibrary yangLibrary;
    private ModulesState modulesState;
    private RpcOperationType rpcOperationType;
    private static final Logger LOGGER = LoggerFactory.getLogger(YangLibraryMessage.class);

    public YangLibraryMessage(String netconfMessage) throws JAXBException {
        super(netconfMessage);

        rpc = getRpc();
        rpcOperationType = parseRpcOperationType(rpc);
        for (Document payload : getRpcPayloads(rpc)) {
            parseYangLibrary(payload);
            parseModulesState(payload);
        }
    }

    private List<Object> buildPayloads() throws JAXBException {
        List<Object> payloads = new ArrayList<>();

        if (getYangLibrary() != null) {
            payloads.add(JaxbUtils.buildPayloadObject(getYangLibrary()));
        }

        if (getModulesState() != null) {
            payloads.add(JaxbUtils.buildPayloadObject(getModulesState()));
        }

        return payloads;
    }

    public String buildRpcReplyResponse() throws DispatchException {
        try {
            return NetconfRpcReplyMessage.buildRpcReplyData(NetconfVersionManager.getRpcReplyClass(getRpc()),
                    getRpc().getMessageId(), buildPayloads());
        } catch (JAXBException ex) {
            throw new DispatchException(ex);
        }
    }

    private static RpcOperationType parseRpcOperationType(Rpc rpc) throws JAXBException {
        switch (rpc.getRpcOperationType()) {
            case GET:
            case GET_CONFIG:
                return rpc.getRpcOperationType();

            default:
                throw new JAXBException("Error operation of the request");
        }
    }

    private void parseYangLibrary(Document payload) {
        if (payload.getFirstChild().getLocalName().equals("yang-library")) {
            yangLibrary = unmarshalYangLibrary(payload);
        }
    }

    private void parseModulesState(Document payload) {
        if (payload.getFirstChild().getLocalName().equals("modules-state")) {
            modulesState = unmarshalModulesState(payload);
        }
    }

    private static YangLibrary unmarshalYangLibrary(Document payload) {
        try {
            return JaxbUtils.unmarshal(payload, YangLibrary.class);
        } catch (JAXBException ex) {
            LOGGER.error(ex.getErrorCode());
            return null;
        }
    }

    private static ModulesState unmarshalModulesState(Document payload) {
        try {
            return JaxbUtils.unmarshal(payload, ModulesState.class);
        } catch (JAXBException ex) {
            LOGGER.error(ex.getErrorCode());
            return null;
        }
    }

    public YangLibrary getYangLibrary() {
        return yangLibrary;
    }

    public ModulesState getModulesState() {
        return modulesState;
    }

    public RpcOperationType getRpcOperationType() {
        return rpcOperationType;
    }
}
