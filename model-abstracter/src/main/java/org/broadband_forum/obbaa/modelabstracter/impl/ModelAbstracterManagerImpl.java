/*
 *   Copyright 2022 Broadband Forum
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

package org.broadband_forum.obbaa.modelabstracter.impl;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.broadband_forum.obbaa.dmyang.entities.PmaResourceId;
import org.broadband_forum.obbaa.modelabstracter.ConvertRet;
import org.broadband_forum.obbaa.modelabstracter.ModelAbstracterManager;
import org.broadband_forum.obbaa.modelabstracter.converter.ConverterFactory;
import org.broadband_forum.obbaa.modelabstracter.converter.ModelConverter;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FlagForRestPutOperations;
import org.broadband_forum.obbaa.pma.PmaRegistry;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Process YANG Models, including data transformation and message mapping, and so on.
 */
@Slf4j
@Setter
public class ModelAbstracterManagerImpl implements ModelAbstracterManager {
    private PmaRegistry m_pmaRegistry;

    private ConverterFactory m_converterFactory;

    @Override
    public NetConfResponse execute(NetconfClientInfo clientInfo, EditConfigRequest netconfRequest, String originRequest)
        throws NetconfMessageBuilderException, ExecutionException {
        Element content = netconfRequest.getConfigElement().getConfigElementContents().get(0);
        String nodeName = content.getNodeName();
        ModelConverter modelConverter = m_converterFactory.getConvertType(nodeName);
        if (Objects.isNull(modelConverter)) {
            log.error("unsupported model, node name is {}, message is {}", nodeName, originRequest);
            throw new NetconfMessageBuilderException("unsupported YANG model");
        }
        String originMsgId = netconfRequest.getMessageId();
        NetConfResponse response = new NetConfResponse();
        response.setMessageId(originMsgId);
        ConvertRet convertRet = modelConverter.convert(DocumentUtils.documentToPrettyString(content));
        if (convertRet.isConfigEmpty()) {
            log.info("There is no need to send to device, content: {}", content);
            response.setOk(true);
            response.setInstanceReplaced(FlagForRestPutOperations.m_instanceReplace.get());
            return response;
        }
        // update the config elements and msg id, then send to device
        for (List<Element> configElement : convertRet.getConfigElements()) {
            netconfRequest.getConfigElement().setConfigElementContents(configElement);
            netconfRequest.setMessageId(UUID.randomUUID().toString());
            Map<NetConfResponse, List<Notification>> pmaResp = m_pmaRegistry.executeNC(
                new PmaResourceId(PmaResourceId.Type.DEVICE, convertRet.getDeviceName()),
                netconfRequest.requestToString());
            response = pmaResp.keySet().stream().findFirst().orElse(response);
            if (!response.isOk()) {
                log.error("failed to send edit config message, error info: {}", response.getErrors());
                response.setMessageId(originMsgId);
                return response;
            }
        }
        return response;
    }
}
