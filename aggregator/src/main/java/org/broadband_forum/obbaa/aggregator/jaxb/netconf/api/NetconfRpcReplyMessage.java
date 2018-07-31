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

import org.broadband_forum.obbaa.aggregator.jaxb.netconf.schema.common.Data;
import org.broadband_forum.obbaa.aggregator.jaxb.netconf.schema.rpcreply.Ok;
import org.broadband_forum.obbaa.aggregator.jaxb.netconf.schema.rpcreply.RpcError;
import org.broadband_forum.obbaa.aggregator.jaxb.netconf.schema.rpcreply.RpcReply;
import org.broadband_forum.obbaa.aggregator.jaxb.netconf.schema.rpcreply.RpcReplyV10;
import org.broadband_forum.obbaa.aggregator.jaxb.netconf.schema.rpcreply.RpcReplyV11;
import org.broadband_forum.obbaa.aggregator.jaxb.utils.JaxbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.List;

/**
 * Netconf rpc-reply message.
 */
public class NetconfRpcReplyMessage extends NetconfPayload {
    private RpcReply rpcReply;
    private static final Logger LOGGER = LoggerFactory.getLogger(NetconfRpcReplyMessage.class);

    private NetconfRpcReplyMessage() {
        //Hide
    }

    /**
     * Parse a rpc-reply message.
     *
     * @param netconfMessage Message
     * @throws JAXBException Exception
     */
    public NetconfRpcReplyMessage(String netconfMessage) throws JAXBException {
        rpcReply = getRpcReply(netconfMessage);
        setPayloadDocuments(getRpcReplyPayloads(rpcReply));
    }

    /**
     * Get rpc-reply payload objects.
     *
     * @param rpcReply Rpc-reply
     * @return Payloads
     */
    public static List<Object> getRpcReplyPayloadObjects(RpcReply rpcReply) throws JAXBException {
        return rpcReply.getPayloadObjects();
    }

    /**
     * Get rpc-reply payloads.
     *
     * @param rpcReply Rpc-reply
     * @return Payloads
     */
    public static List<Document> getRpcReplyPayloads(RpcReply rpcReply) throws JAXBException {
        List<Object> objects = getRpcReplyPayloadObjects(rpcReply);
        return JaxbUtils.objectsToDocument(objects);
    }

    /**
     * Get rpc-reply information.
     *
     * @param netconfMessage rpc-reply message
     * @return Rpc reply
     * @throws JAXBException Exception
     */
    public static RpcReply getRpcReply(String netconfMessage) throws JAXBException {
        try {
            return (RpcReply) JaxbUtils.unmarshal(netconfMessage, RpcReplyV10.class);
        }
        catch (JAXBException ex) {
            return (RpcReply) JaxbUtils.unmarshal(netconfMessage, RpcReplyV11.class);
        }
    }

    /**
     * Get rpc-reply object.
     *
     * @return RpcReply
     */
    public RpcReply getRpcReply() {
        return rpcReply;
    }

    /**
     * Build rpc-reply OK of Netconf v1.0.
     *
     * @param messageId Message ID
     * @return reponse
     */
    public static String buildRpcReplyOkV10(String messageId) {
        return buildRpcReplyOk(RpcReplyV10.class, messageId);
    }

    /**
     * Build rpc-reply OK of Netconf v1.1.
     *
     * @param messageId Message ID
     * @return reponse
     */
    public static String buildRpcReplyOkV11(String messageId) {
        return buildRpcReplyOk(RpcReplyV11.class, messageId);
    }

    /**
     * Build rpc-reply data of Netconf v1.0.
     *
     * @param messageId Message ID
     * @param nodes     Data
     * @return Response
     */
    public static String buildRpcReplyDataV10(String messageId, List<Node> nodes) {
        List<Object> objects = new ArrayList<>(nodes);
        return buildRpcReplyData(RpcReplyV10.class, messageId, objects);
    }

    /**
     * Build rpc-reply data of Netconf v1.1.
     *
     * @param messageId Message ID
     * @param nodes     Data
     * @return Response
     */
    public static String buildRpcReplyDataV11(String messageId, List<Node> nodes) {
        List<Object> objects = new ArrayList<>(nodes);
        return buildRpcReplyData(RpcReplyV11.class, messageId, objects);
    }

    /**
     * Build rpc-reply error of Netconf v1.0.
     *
     * @param messageId Message ID
     * @param rpcError  RpcError
     * @return Response
     */
    public static String buildRpcReplyErrorV10(String messageId, RpcError rpcError) {
        return buildRpcReplyError(RpcReplyV10.class, messageId, rpcError);
    }

    /**
     * Build rpc-reply error of Netconf v1.1.
     *
     * @param messageId Message ID
     * @param rpcError  RpcError
     * @return Response
     */
    public static String buildRpcReplyErrorV11(String messageId, RpcError rpcError) {
        return buildRpcReplyError(RpcReplyV11.class, messageId, rpcError);
    }

    /**
     * Build rpc-reply of OK.
     *
     * @param replyClass Class of the version
     * @param messageId  Message ID
     * @param <T>        Class of rpc-reply
     * @return Response
     */
    public static <T extends RpcReply> String buildRpcReplyOk(Class<T> replyClass, String messageId) {
        return buildRpcReplyContext(replyClass, messageId, reply -> {
            reply.setOk(new Ok());
        });
    }

    /**
     * Build rpc-reply message with data.
     *
     * @param replyClass Rpc-reply class
     * @param messageId  Message ID
     * @param objects    Data list, the object must extend from Node.
     * @param <T>        Rpc-reply schema class
     * @return Response string
     */
    public static <T extends RpcReply> String buildRpcReplyData(Class<T> replyClass,
                                                                String messageId, List<Object> objects) {
        return buildRpcReplyContext(replyClass, messageId, reply -> {
            Data data = new Data();
            data.setObjects(objects);
            reply.setData(data);
        });
    }

    /**
     * Build rpc-reply message with context.
     *
     * @param replyClass Rpc-reply class
     * @param messageId  Message ID
     * @param <T>        Rpc-reply schema class
     * @return Response string
     */
    public static <T extends RpcReply> String buildRpcReplyError(Class<T> replyClass,
                                                                 String messageId, RpcError rpcError) {
        return buildRpcReplyContext(replyClass, messageId, reply -> {
            reply.setRpcError(rpcError);
        });
    }

    /**
     * Build rpc-reply message with errors.
     *
     * @param replyClass Rpc-reply class
     * @param messageId  Message ID
     * @param <T>        Rpc-reply schema class
     * @return Response string
     */
    private static <T extends RpcReply> String buildRpcReplyContext(Class<T> replyClass, String messageId,
                                                                    NetconfRpcReplyTemplate netconfRpcReplyTemplate) {
        try {
            T rpcReply = replyClass.newInstance();
            rpcReply.setMessageId(messageId);
            netconfRpcReplyTemplate.execute(rpcReply);
            return JaxbUtils.marshalToString(rpcReply);
        }
        catch (JAXBException | IllegalAccessException | InstantiationException ex) {
            LOGGER.error(ex.getMessage());
            return "";
        }
    }
}
