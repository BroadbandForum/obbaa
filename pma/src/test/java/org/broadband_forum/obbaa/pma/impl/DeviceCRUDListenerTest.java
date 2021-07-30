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

package org.broadband_forum.obbaa.pma.impl;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.broadband_forum.obbaa.nm.devicemanager.DeviceManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DeviceCRUDListenerTest {
    DeviceCRUDListener m_listener;
    @Mock
    private DeviceManager m_deviceManager;
    @Mock
    private PmaRegistryImpl m_pmaRegistry;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        m_listener = new DeviceCRUDListener(m_deviceManager, m_pmaRegistry);
    }

    @Test
    public void testSessionFactoryIsCalledOnDeviceDelete() {
        verifyZeroInteractions(m_pmaRegistry);

        m_listener.deviceRemoved("device A");

        verify(m_pmaRegistry).deviceRemoved("device A");
    }

    @Test
    public void testInitRegisters() {
        verifyZeroInteractions(m_deviceManager);

        m_listener.init();

        verify(m_deviceManager).addDeviceStateProvider(m_listener);
    }

    @Test
    public void testDestroyUnregisters() {
        verifyZeroInteractions(m_deviceManager);

        m_listener.destroy();

        verify(m_deviceManager).removeDeviceStateProvider(m_listener);
    }
}
