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

import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.nm.devicemanager.DeviceManager;
import org.broadband_forum.obbaa.nm.devicemanager.DeviceStateProvider;

public class NetconfConnectionStateProvider implements DeviceStateProvider {
    public static final String CONNECTION_STATE = "connectionState";
    public static final String CALL_HOME = "call-home";
    private final DeviceManager m_deviceManager;
    private final NetconfConnectionManager m_cm;

    public NetconfConnectionStateProvider(DeviceManager deviceManager, NetconfConnectionManager cm) {
        m_deviceManager = deviceManager;
        m_cm = cm;
    }

    public void init() {
        m_deviceManager.addDeviceStateProvider(this);
    }

    @Override
    public void deviceAdded(String deviceName) {
        Device device = m_deviceManager.getDevice(deviceName);
        if (device.getDeviceManagement().getDeviceConnection().getConnectionModel().equals(CALL_HOME)) {
            m_cm.dropNewDeviceConnection(device.getDeviceManagement().getDeviceConnection().getDuid());
        }
    }

    @Override
    public void deviceRemoved(String deviceName) {

    }

    public void destroy() {
        m_deviceManager.removeDeviceStateProvider(this);
    }
}
