/*
 * Copyright 2021 Broadband Forum
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
package org.broadband_forum.obbaa.nm.nwfunctionmgr.impl;

import java.util.LinkedHashSet;
import java.util.Set;

import org.broadband_forum.obbaa.nf.dao.NetworkFunctionDao;
import org.broadband_forum.obbaa.nf.entities.NetworkFunction;
import org.broadband_forum.obbaa.nm.nwfunctionmgr.NetworkFunctionManager;
import org.broadband_forum.obbaa.nm.nwfunctionmgr.NetworkFunctionStateProvider;

public class NetworkFunctionManagerImpl implements NetworkFunctionManager {

    private Set<NetworkFunctionStateProvider> m_networkFunctionStateProviders = new LinkedHashSet<>();
    private NetworkFunctionDao m_dao;

    public NetworkFunctionManagerImpl(NetworkFunctionDao dao) {
        m_dao = dao;
    }

    @Override
    public NetworkFunction getNetworkFunction(String networkFunctionName) {
        NetworkFunction networkFunction = m_dao.getNetworkFunctionByName(networkFunctionName);
        if (networkFunction == null) {
            throw new IllegalArgumentException("Network Function " + networkFunctionName + " does not exist");
        }
        return networkFunction;
    }

    @Override
    public void removeNetworkFunctionStateProvider(NetworkFunctionStateProvider stateProvider) {
        m_networkFunctionStateProviders.remove(stateProvider);
    }

    @Override
    public void addNetworkFunctionStateProvider(NetworkFunctionStateProvider stateProvider) {
        m_networkFunctionStateProviders.add(stateProvider);
    }

    @Override
    public void networkFunctionAdded(String networkFunctionName) {
        for (NetworkFunctionStateProvider provider : m_networkFunctionStateProviders) {
            provider.networkFunctionAdded(networkFunctionName);
        }
    }

    @Override
    public void networkFunctionRemoved(String networkFunctionName) {
        for (NetworkFunctionStateProvider provider : m_networkFunctionStateProviders) {
            provider.networkFunctionRemoved(networkFunctionName);
        }
    }

    @Override
    public void updateConfigAlignmentState(String networkFunctionName, String verdict) {
        m_dao.updateNetworkFunctionAlignmentState(networkFunctionName,verdict);
    }
}
