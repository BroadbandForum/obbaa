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

package org.broadband_forum.obbaa.aggregator.jaxb.pma.api;

import org.broadband_forum.obbaa.aggregator.api.DispatchException;
import org.broadband_forum.obbaa.aggregator.jaxb.netconf.api.NetconfRpcMessage;
import org.broadband_forum.obbaa.aggregator.jaxb.pma.schema.PmaYangLibrary;
import org.broadband_forum.obbaa.aggregator.jaxb.utils.JaxbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.bind.JAXBException;
import java.util.List;

/**
 * PMA Yang library rpc request.
 */
public final class PmaYangLibraryRpc extends NetconfRpcMessage {
    public static final String MODULE_NAME = "pma-yang-library";
    public static final String REVISION = "2018-06-15";
    public static final String NAMESPACE = "urn:bbf:yang:obbaa:pma-yang-library";
    private static final Logger LOGGER = LoggerFactory.getLogger(PmaYangLibraryRpc.class);

    private PmaYangLibrary pmaYangLibrary;

    private PmaYangLibraryRpc(String netconfMessage) throws JAXBException {
        super(netconfMessage);
        parsePmaYangLibrary(getRpcPayloads(getRpc()));
    }

    /**
     * Build a new instance of YANG library config request.
     *
     * @param netconfMessage Request
     * @return Request information
     * @throws DispatchException Exception
     */
    public static PmaYangLibraryRpc getInstance(String netconfMessage) throws DispatchException {
        try {
            PmaYangLibraryRpc instance = new PmaYangLibraryRpc(netconfMessage);
            return instance;
        }
        catch (JAXBException ex) {
            throw new DispatchException(ex);
        }
    }

    /**
     * Parse the payloads of request.
     *
     * @param payloads Payloads
     */
    private void parsePmaYangLibrary(List<Document> payloads) {
        //Just support one device once config
        Document payload = payloads.get(0);
        pmaYangLibrary = unmarshalYangLibrary(payload);
    }

    /**
     * Unmarshal request of YANG library config.
     *
     * @param payload Payload
     * @return Result after unmarshal
     */
    private static PmaYangLibrary unmarshalYangLibrary(Document payload) {
        try {
            return JaxbUtils.unmarshal(payload, PmaYangLibrary.class);
        }
        catch (JAXBException ex) {
            LOGGER.error(ex.getErrorCode());
            return null;
        }
    }

    /**
     * Get information of PMA YANG library config.
     *
     * @return information of PMA YANG library config
     */
    public PmaYangLibrary getPmaYangLibrary() {
        return pmaYangLibrary;
    }
}
