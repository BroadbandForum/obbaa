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

package org.broadband_forum.obbaa.modelabstracter;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;

import java.util.concurrent.ExecutionException;

/**
 * Provide API for Aggregator to process Model Adapter message.
 */
public interface ModelAbstracterManager {
    /**
     * Provide a unified API for Model Adapter RPC of Aggregator.
     *
     * @param clientInfo Client info
     * @param netconfRequest Message of Model Adapter RPC
     * @param originRequest Original message of Model Adapter RPC
     * @return Response
     * @throws NetconfMessageBuilderException build NETCONF message exception
     * @throws ExecutionException process NETCONF message error
     */
    NetConfResponse execute(NetconfClientInfo clientInfo, EditConfigRequest netconfRequest, String originRequest)
        throws NetconfMessageBuilderException, ExecutionException;
}
