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

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.broadband_forum.obbaa.nm.nwfunctionmgr.NetworkFunctionStateProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class NetworkFunctionManagerImplTest {

    @Mock
    NetworkFunctionStateProvider m_networkFunctionStateProvider;
    NetworkFunctionManagerImpl m_networkFunctionManagerImpl;
    String m_networkFunctionName = "vomci";

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        m_networkFunctionManagerImpl = new NetworkFunctionManagerImpl();
    }

    @Test
    public void testNetworkFunctionAdded() {
        m_networkFunctionManagerImpl.addNetworkFunctionStateProvider(m_networkFunctionStateProvider);
        m_networkFunctionManagerImpl.networkFunctionAdded(m_networkFunctionName);
        verify(m_networkFunctionStateProvider, times(1)).networkFunctionAdded(m_networkFunctionName);
    }

    @Test
    public void testNetworkFunctionAddedWhenStateProviderIsNull() {
        m_networkFunctionManagerImpl.networkFunctionAdded(m_networkFunctionName);
        verify(m_networkFunctionStateProvider, never()).networkFunctionAdded(m_networkFunctionName);
    }

    @Test
    public void testNetworkFunctionRemoved() {
        m_networkFunctionManagerImpl.addNetworkFunctionStateProvider(m_networkFunctionStateProvider);
        m_networkFunctionManagerImpl.networkFunctionRemoved(m_networkFunctionName);
        verify(m_networkFunctionStateProvider, times(1)).networkFunctionRemoved(m_networkFunctionName);
    }

    @Test
    public void testNetworkFunctionRemovedWhenStateProviderIsNull() {
        m_networkFunctionManagerImpl.networkFunctionRemoved(m_networkFunctionName);
        verify(m_networkFunctionStateProvider, never()).networkFunctionRemoved(m_networkFunctionName);
    }
}