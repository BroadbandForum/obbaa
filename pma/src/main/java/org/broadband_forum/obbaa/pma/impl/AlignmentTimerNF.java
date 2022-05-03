/*
 * Copyright 2022 Broadband Forum
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

package org.broadband_forum.obbaa.pma.impl;

import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.NEVER_ALIGNED;
import static org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer.getNetconfResponse;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.dmyang.entities.AlignmentOption;
import org.broadband_forum.obbaa.dmyang.entities.PmaResourceId;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.nf.dao.NetworkFunctionDao;
import org.broadband_forum.obbaa.nf.entities.NetworkFunction;
import org.broadband_forum.obbaa.pma.PmaRegistry;

/**
 * <p>
 * Alignment Timer for Network Functions
 * </p>
 * Created by Andre Brizido (Altice Labs) on 28/04/2022.
 */
public class AlignmentTimerNF {
    private static final Logger LOGGER = Logger.getLogger(AlignmentTimerNF.class);
    private final PmaRegistry m_pmaRegistry;
    private final Timer m_timer;
    private NetworkFunctionDao m_networkFunctionDao;

    public AlignmentTimerNF(PmaRegistry pmaRegistry, NetworkFunctionDao networkFunctionDao) {
        m_pmaRegistry = pmaRegistry;
        m_timer = new Timer();
        m_networkFunctionDao = networkFunctionDao;
    }

    public void init() {
        m_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (m_networkFunctionDao != null) {
                    try {
                        runAlignment();
                    } catch (Exception e) {
                        LOGGER.error("something wrong during alignment:" + e.getMessage());
                    }
                }
            }
        }, 10000L, 10000L);
    }

    public void destroy() {
        m_timer.cancel();
    }

    public void runAlignment() {
        /* At this level we only want to know the list of functions and if they need to be aligned or not.
        For each function there will be a call to m_txService.executeWithTx while aligning. If we put the block below
        inside a m_txService.executeWithTx this would block forever in the second network function while calling
        getKafkaTopicNames */
        List<NetworkFunction> networkFunctions = m_networkFunctionDao.findAllNetworkFunctions();
        for (NetworkFunction networkFunction : networkFunctions) {
            PmaResourceId resourceId = new PmaResourceId(PmaResourceId.Type.NETWORK_FUNCTION,
                    networkFunction.getNetworkFunctionName());
            try {
                m_pmaRegistry.executeWithPmaSession(resourceId, session -> {
                    if (networkFunction.isNeverAligned()) {
                        LOGGER.debug((String.format("Network Function %s is " + NEVER_ALIGNED, networkFunction)));
                        AlignmentOption alignmentOption = networkFunction.getAlignmentOption();
                        if (alignmentOption.equals(AlignmentOption.PUSH)) {
                            if (resourceHasConfigurations(resourceId)) {
                                session.forceAlign();
                            }
                        } else if (alignmentOption.equals(AlignmentOption.PULL)) {
                            LOGGER.error("Upload config option not supported for Network Functions. "
                                    + "Function name = " + networkFunction.getNetworkFunctionName());
                        }
                    } else if (!networkFunction.isAligned() && !networkFunction.isAlignmentUnknown()) {
                        session.align();
                    }
                    return null;
                });
            } catch (ExecutionException e) {
                LOGGER.error(String.format("Could not align network function %s", networkFunction), e);
            }
        }
    }

    //TODO obbaa-366 maybe this can substitute the non resource generic deviceHasconfigurations method?
    private boolean resourceHasConfigurations(PmaResourceId resourceId) throws ExecutionException {
        return m_pmaRegistry.executeWithPmaSession(resourceId, session -> {
            Map<NetConfResponse, List<Notification>> netConfResponseListTreeMap = session.executeNC(new GetConfigRequest()
                    .setSourceRunning().setMessageId("xx")
                    .requestToString());
            Map.Entry<NetConfResponse, List<Notification>> entry = netConfResponseListTreeMap.entrySet().iterator().next();
            String responseStr = entry.getKey().responseToString();
            try {
                NetConfResponse response = getNetconfResponse(DocumentUtils.stringToDocument(responseStr));
                return !response.getDataContent().isEmpty();
            } catch (NetconfMessageBuilderException e) {
                throw new ExecutionException(e);
            }
        });
    }
}
