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

package org.broadband_forum.obbaa.pma;

import java.util.List;


import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.nf.entities.NetworkFunction;

/**
 * <p>
 * Netconf Network Function Alignment Service
 * </p>
 * Created by J.V.Correia (Altice Labs) on 26/01/2022.
 */
public interface NetconfNetworkFunctionAlignmentService extends NetworkFunctionAlignmentService {
    void queueEdit(String networkFunctionName, EditConfigRequest request);

    List<EditConfigRequest> getEditQueue(String networkFunctionName);

    void align(NetworkFunction networkFunction, NetConfResponse getConfigResponse);

    void forceAlign(NetworkFunction networkFunction, NetConfResponse getConfigResponse);

}
