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

import org.broadband_forum.obbaa.aggregator.api.DispatchException;
import org.broadband_forum.obbaa.netconf.api.messages.PojoToDocumentTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Netconf base message like payload etc.
 */
public abstract class NetconfPayload {
    private List<Document> payloadDocuments;
    private static final Logger LOGGER = LoggerFactory.getLogger(NetconfPayload.class);

    /**
     * Get payloads.
     *
     * @return Payload of rpc or rpc-reply
     */
    public List<String> getPayloads() {
        List<String> payloadTexts = new ArrayList<>();

        for (Document document : getPayloadDocuments()) {
            String data = PojoToDocumentTransformer.requestToString(document);
            payloadTexts.add(data);
        }

        return payloadTexts;
    }

    /**
     * Get payload xmlns(YANG namespace) only support one model configuration once.
     *
     * @return Namespace
     * @throws DispatchException Exception
     */
    public String getOnlyOneTopXmlns() throws DispatchException {
        try {
            return getPayloadDocuments().get(0).getFirstChild().getNamespaceURI();
        }
        catch (NoSuchElementException | IndexOutOfBoundsException ex) {
            LOGGER.error("getOnlyOneTopXmlns : {}", ex);
            throw new DispatchException("Error message about namespace.");
        }
    }

    /**
     * Get payload documents.
     *
     * @return Payload documents
     */
    public List<Document> getPayloadDocuments() {
        return payloadDocuments;
    }

    /**
     * Set payload documents.
     *
     * @param payloadDocuments Payload documents
     */
    protected void setPayloadDocuments(List<Document> payloadDocuments) {
        this.payloadDocuments = payloadDocuments;
    }
}
