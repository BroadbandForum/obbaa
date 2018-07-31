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
import org.broadband_forum.obbaa.aggregator.jaxb.netconf.api.NetconfRpcReplyMessage;
import org.broadband_forum.obbaa.aggregator.jaxb.netconf.api.NetconfVersionManager;
import org.broadband_forum.obbaa.aggregator.jaxb.pma.schema.PmaDeviceConfig;
import org.broadband_forum.obbaa.aggregator.jaxb.utils.JaxbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.List;

/**
 * PMA device config rpc request.
 */
public final class PmaDeviceConfigRpc extends NetconfRpcMessage {
    public static final String MODULE_NAME = "bbf-pma-device-configuration";
    public static final String REVISION = "2018-06-15";
    public static final String NAMESPACE = "urn:bbf:yang:obbaa:pma-device-config";
    private static final Logger LOGGER = LoggerFactory.getLogger(PmaDeviceConfigRpc.class);

    private PmaDeviceConfig pmaDeviceConfig;

    private PmaDeviceConfigRpc(String netconfMessage) throws JAXBException {
        super(netconfMessage);
        parsePmaDeviceConfig(getRpcPayloads(getRpc()));
    }

    /**
     * Create a instance of PMA device config rpc request.
     *
     * @param netconfMessage Request
     * @return Result after parsing
     * @throws DispatchException Exception
     */
    public static PmaDeviceConfigRpc getInstance(String netconfMessage) throws DispatchException {
        try {
            PmaDeviceConfigRpc instance = new PmaDeviceConfigRpc(netconfMessage);
            checkMessage(instance);
            return instance;
        }
        catch (JAXBException ex) {
            throw new DispatchException(ex);
        }
    }

    /**
     * Check if the message if correct.
     *
     * @param instance Request
     * @throws DispatchException Exception
     */
    private static void checkMessage(PmaDeviceConfigRpc instance) throws DispatchException {
        switch (instance.getRpc().getRpcOperationType()) {
            case ACTION:
                if (instance.getPmaDeviceConfig().getAlignmentState() != null) {
                    throw DispatchException.buildNotSupport();
                }
                break;

            case GET:
                if (instance.getPmaDeviceConfig().getPmaDeviceConfigAlign() != null) {
                    throw DispatchException.buildNotSupport();
                }
                break;

            default:
                throw DispatchException.buildNotSupport();
        }
    }

    /**
     * Parse the PMA device config payload.
     *
     * @param payloads Payloads in the request
     */
    private void parsePmaDeviceConfig(List<Document> payloads) {
        //Just support one device once config
        Document payload = payloads.get(0);
        pmaDeviceConfig = unmarshalYangLibrary(payload);
    }

    /**
     * Unmarshal YANG module of PMA YANG library.
     *
     * @param payload Payload
     * @return Container information of device-config
     */
    private static PmaDeviceConfig unmarshalYangLibrary(Document payload) {
        try {
            return JaxbUtils.unmarshal(payload, PmaDeviceConfig.class);
        }
        catch (JAXBException ex) {
            LOGGER.error(ex.getErrorCode());
            return null;
        }
    }

    /**
     * Get PMA device config container.
     *
     * @return Config information
     */
    public PmaDeviceConfig getPmaDeviceConfig() {
        return pmaDeviceConfig;
    }

    /**
     * Build rpc-reply response of PMA device config request.
     *
     * @return Response
     * @throws DispatchException Exception
     */
    public String buildRpcReplyDataResponse() throws DispatchException {
        try {
            List<Object> payloads = new ArrayList<>();
            payloads.add(JaxbUtils.buildPayloadObject(getPmaDeviceConfig()));
            return NetconfRpcReplyMessage.buildRpcReplyData(NetconfVersionManager.getRpcReplyClass(getRpc()),
                    getRpc().getMessageId(), payloads);
        }
        catch (JAXBException ex) {
            throw new DispatchException(ex);
        }
    }
}