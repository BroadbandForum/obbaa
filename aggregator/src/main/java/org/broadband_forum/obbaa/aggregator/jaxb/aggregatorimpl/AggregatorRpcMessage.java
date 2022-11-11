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

package org.broadband_forum.obbaa.aggregator.jaxb.aggregatorimpl;

import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.NETWORK_FUNCTION_STATE_NAMESPACE;

import org.broadband_forum.obbaa.aggregator.api.DispatchException;
import org.broadband_forum.obbaa.aggregator.jaxb.netconf.api.NetconfRpcMessage;
import org.broadband_forum.obbaa.aggregator.jaxb.networkmanager.api.NetworkManagerRpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.bind.JAXBException;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Aggregator rpc message. Just support only 1 YANG model in the version specification.
 */
public class AggregatorRpcMessage extends NetconfRpcMessage {
    private List<Document> topPayloads;
    private static final Logger LOGGER = LoggerFactory.getLogger(AggregatorRpcMessage.class);

    /** The rpc message need Aggregator processing. Just support 1 operation in current version.
     *
     * @param request Netconf message
     * @throws JAXBException Exception
     */
    public AggregatorRpcMessage(String request) throws JAXBException {
        super(request);
        topPayloads = NetconfRpcMessage.getRpcPayloads(getRpc());

        if (getTopPayloads().size() != 1) {
            throw new JAXBException("Don't support multiple configurations in one rpc message.");
        }
    }

    /**
     * Get rpc-message object.
     *
     * @return Information of rpc
     */
    public NetconfRpcMessage getNetconfRpcMessage() {
        return this;
    }

    /**
     * Get payloads of rpc message. which include config information in edit-config request
     * and include filter subtree in get or get-config request.
     *
     * @return Payloads
     */
    private List<Document> getTopPayloads() {
        return topPayloads;
    }

    public String getOnlyOneTopXmlns() throws DispatchException {
        try {
            return getTopPayloads().get(0).getFirstChild().getNamespaceURI();
        }
        catch (NoSuchElementException | IndexOutOfBoundsException ex) {
            LOGGER.error("getOnlyOneTopXmlns : {}", ex);
            throw new DispatchException("Error namespace of the message.");
        }
    }

    public String getSecondTopXmlns() throws DispatchException {
        try {
            if (getTopPayloads().get(0).getFirstChild().getFirstChild() != null) {
                return getTopPayloads().get(0).getFirstChild().getFirstChild().getNamespaceURI();
            }
        }
        catch (NoSuchElementException | IndexOutOfBoundsException ex) {
            LOGGER.error("getSecondTopXmlns : {}", ex);
            throw new DispatchException("Error namespace of the message.");
        }
        return null;
    }

    /**
     * Just get one top payload.
     *
     * @return Payload
     * @throws DispatchException Exception
     */
    public Document getOnlyOneTopPayload() throws DispatchException {
        try {
            return getTopPayloads().get(0);
        }
        catch (NoSuchElementException | IndexOutOfBoundsException ex) {
            LOGGER.error("getOnlyOneTopPayload : {}", ex);
            throw new DispatchException("Error message has no valid payload.");
        }
    }

    /**
     * Judge if the request based on network-manager YANG model.
     *
     * @return true or false
     */
    public boolean isNetworkManagerMessage() {
        try {
            return getOnlyOneTopXmlns().equals(NetworkManagerRpc.NAMESPACE);
        }
        catch (DispatchException ex) {
            //Not the network-manager message
            return false;
        }
    }

    public boolean isNetworkFunctionStateMessage() {
        try {
            if (getOnlyOneTopXmlns() != null && getSecondTopXmlns() != null) {
                return (getOnlyOneTopXmlns().equals(NETWORK_FUNCTION_STATE_NAMESPACE)
                        || getSecondTopXmlns().equals(NETWORK_FUNCTION_STATE_NAMESPACE));
            } else {
                return false;
            }
        }
        catch (DispatchException ex) {
            //Not the network-manager message
            return false;
        }
    }
}
