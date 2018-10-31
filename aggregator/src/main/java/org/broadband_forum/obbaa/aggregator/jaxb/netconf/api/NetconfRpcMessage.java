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

package org.broadband_forum.obbaa.aggregator.jaxb.netconf.api;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.broadband_forum.obbaa.aggregator.api.DispatchException;
import org.broadband_forum.obbaa.aggregator.jaxb.netconf.schema.rpc.Rpc;
import org.broadband_forum.obbaa.aggregator.jaxb.netconf.schema.rpc.RpcV10;
import org.broadband_forum.obbaa.aggregator.jaxb.netconf.schema.rpc.RpcV11;
import org.broadband_forum.obbaa.aggregator.jaxb.netconf.schema.rpcreply.RpcError;
import org.broadband_forum.obbaa.aggregator.jaxb.utils.JaxbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * Netconf rpc message.
 */
public class NetconfRpcMessage extends NetconfPayload {
    private static final Logger LOGGER = LoggerFactory.getLogger(NetconfRpcMessage.class);

    private Rpc rpc;
    private String originalMessage;

    private NetconfRpcMessage() {
        //Hide
    }

    /**
     * Parse a rpc message.
     *
     * @param netconfMessage Message
     * @throws JAXBException Exception
     */
    public NetconfRpcMessage(String netconfMessage) throws JAXBException {
        rpc = getRpc(netconfMessage);
        originalMessage = netconfMessage;
        setPayloadDocuments(getRpcPayloads(rpc));
    }

    /**
     * Get a instance of Netconf rpc message.
     *
     * @param netconfRequest Request
     * @return NetconfRpcMessage instance
     * @throws DispatchException Exception
     */
    public static NetconfRpcMessage getInstance(String netconfRequest) throws DispatchException {
        try {
            return new NetconfRpcMessage(netconfRequest);
        }
        catch (JAXBException ex) {
            LOGGER.error("Parse request error : {}", ex);
            throw new DispatchException(ex);
        }
    }

    /**
     * Get payloads of rpc message.
     *
     * @param rpc Rpc message
     * @return Payloads
     */
    public static List<Document> getRpcPayloads(Rpc rpc) throws JAXBException {
        List<Object> objects = getRpcPayloadObjects(rpc);

        return JaxbUtils.objectsToDocument(objects);
    }

    /**
     * Get payload objects.
     *
     * @param rpc Rpc
     * @return Payloads
     */
    public static List<Object> getRpcPayloadObjects(Rpc rpc) throws JAXBException {
        try {
            return rpc.getPayloadObjects();
        }
        catch (JAXBException ex) {
            LOGGER.info("Payload is null");
            return new ArrayList<>();
        }
    }

    /**
     * Get rpc common information. Just support 1.0 and 1.1 version.
     *
     * @param netconfMessage Rpc message
     * @return Rpc
     * @throws JAXBException Exception
     */
    public static Rpc getRpc(String netconfMessage) throws JAXBException {
        if (netconfMessage.contains(NetconfProtocol.VERSION_1_1)) {
            return JaxbUtils.unmarshal(netconfMessage, RpcV11.class);
        }

        return JaxbUtils.unmarshal(netconfMessage, RpcV10.class);
    }

    /**
     * Get Rpc object.
     *
     * @return object
     */
    public Rpc getRpc() {
        return rpc;
    }

    /**
     * Get original message.
     *
     * @return Original message
     */
    public String getOriginalMessage() {
        return originalMessage;
    }

    /**
     * Build a rpc-reply response of OK based on the rpc request.
     *
     * @return Response OK of this rpc.
     */
    public String buildRpcReplyOk() {
        return NetconfRpcReplyMessage.buildRpcReplyOk(NetconfVersionManager.getRpcReplyClass(getRpc()),
                getRpc().getMessageId());
    }

    /**
     * Build a common error rpc-reply.
     *
     * @param error Error message
     * @return rpc-reply message
     */
    public String buildRpcReplyError(String error) {
        RpcError rpcError = new RpcError();
        rpcError.setErrorMessage(error);
        return NetconfRpcReplyMessage.buildRpcReplyError(NetconfVersionManager.getRpcReplyClass(getRpc()),
                getRpc().getMessageId(), rpcError);
    }
}
