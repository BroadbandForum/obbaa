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

import org.broadband_forum.obbaa.nm.nwfunctionmgr.NetworkFunctionManager;
import org.broadband_forum.obbaa.nm.nwfunctionmgr.NetworkFunctionStateProvider;

public class NetworkFunctionCRUDListener implements NetworkFunctionStateProvider {
    private final NetworkFunctionManager m_networkFunctionManager;
    private final PmaRegistryImpl m_pmaRegistry;

    public void init() {
        m_networkFunctionManager.addNetworkFunctionStateProvider(this);
    }

    public void destroy() {
        m_networkFunctionManager.removeNetworkFunctionStateProvider(this);
    }

    public NetworkFunctionCRUDListener(NetworkFunctionManager deviceManager, PmaRegistryImpl pmaRegistry) {
        m_networkFunctionManager = deviceManager;
        m_pmaRegistry = pmaRegistry;
    }

    @Override
    public void networkFunctionAdded(String networkFunctionName) {

    }

    @Override
    public void networkFunctionRemoved(String networkFunctionName) {
        m_pmaRegistry.networkFunctionRemoved(networkFunctionName);
    }
}
