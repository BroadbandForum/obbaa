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

import org.broadband_forum.obbaa.dm.DeviceManager;
import org.broadband_forum.obbaa.dm.DeviceStateProvider;

public class DeviceCRUDListener implements DeviceStateProvider {
    private final DeviceManager m_deviceManager;
    private final PmaRegistryImpl m_pmaRegistry;

    public void init() {
        m_deviceManager.addDeviceStateProvider(this);
    }

    public void destroy() {
        m_deviceManager.removeDeviceStateProvider(this);
    }

    public DeviceCRUDListener(DeviceManager deviceManager, PmaRegistryImpl pmaRegistry) {
        m_deviceManager = deviceManager;
        m_pmaRegistry = pmaRegistry;
    }

    @Override
    public void deviceAdded(String deviceName) {

    }

    @Override
    public void deviceRemoved(String deviceName) {
        m_pmaRegistry.deviceRemoved(deviceName);
    }
}
