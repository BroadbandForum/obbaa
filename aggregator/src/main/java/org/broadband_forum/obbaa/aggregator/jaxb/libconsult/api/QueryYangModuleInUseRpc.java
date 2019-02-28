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

package org.broadband_forum.obbaa.aggregator.jaxb.libconsult.api;

import java.util.List;

import javax.xml.bind.JAXBException;

import org.broadband_forum.obbaa.aggregator.api.DispatchException;
import org.broadband_forum.obbaa.aggregator.jaxb.libconsult.schema.UsedYangModules;
import org.broadband_forum.obbaa.aggregator.jaxb.netconf.api.NetconfRpcMessage;
import org.broadband_forum.obbaa.aggregator.jaxb.utils.JaxbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class QueryYangModuleInUseRpc extends NetconfRpcMessage {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryYangModuleInUseRpc.class);
    private UsedYangModules usedYangModules;

    /**
     * Parse a rpc message.
     *
     * @param netconfMessage Message
     * @throws JAXBException Exception
     */
    public QueryYangModuleInUseRpc(String netconfMessage) throws JAXBException {
        super(netconfMessage);
        parseYangModuleFilterData(getRpcPayloads(getRpc()));
    }

    public static QueryYangModuleInUseRpc getInstance(String netconfMessage) throws DispatchException {
        try {
            QueryYangModuleInUseRpc instance = new QueryYangModuleInUseRpc(netconfMessage);
            return instance;
        }
        catch (JAXBException ex) {
            throw new DispatchException(ex);
        }
    }

    private void parseYangModuleFilterData(List<Document> payloads) {
        Document payload = payloads.get(0);
        usedYangModules = unmarshalFilterYangModule(payload);
    }

    private static UsedYangModules unmarshalFilterYangModule(Document payload) {
        try {
            return JaxbUtils.unmarshal(payload, UsedYangModules.class);
        }
        catch (JAXBException ex) {
            LOGGER.error(ex.getErrorCode());
            return null;
        }
    }

    public UsedYangModules getUsedYangModules() {
        return usedYangModules;
    }
}
