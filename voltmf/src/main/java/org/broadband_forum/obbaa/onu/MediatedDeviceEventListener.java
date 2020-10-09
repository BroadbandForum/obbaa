/*
 * Copyright 2020 Broadband Forum
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

package org.broadband_forum.obbaa.onu;

import org.broadband_forum.obbaa.dm.DeviceManager;
import org.broadband_forum.obbaa.dm.DeviceStateProvider;

/**
 * <p>
 * Listens to events such as device addition and deletion from mediated devices
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 22/07/2020.
 */
public class MediatedDeviceEventListener implements DeviceStateProvider {

    private final DeviceManager m_deviceManager;
    private final VOLTManagement m_voltManagement;

    public MediatedDeviceEventListener(DeviceManager deviceManager, VOLTManagement voltManagement) {
        m_deviceManager = deviceManager;
        m_voltManagement = voltManagement;
    }

    public void init() {
        m_deviceManager.addDeviceStateProvider(this);
    }

    public void destroy() {
        m_deviceManager.removeDeviceStateProvider(this);
    }

    @Override
    public void deviceAdded(String deviceName) {
        m_voltManagement.deviceAdded(deviceName);
    }

    @Override
    public void deviceRemoved(String deviceName) {
        m_voltManagement.deviceRemoved(deviceName);
    }
}
