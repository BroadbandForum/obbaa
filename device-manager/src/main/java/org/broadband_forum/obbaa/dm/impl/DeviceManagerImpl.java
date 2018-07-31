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

package org.broadband_forum.obbaa.dm.impl;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.connectors.sbi.netconf.NewDeviceInfo;
import org.broadband_forum.obbaa.dm.DeviceManager;
import org.broadband_forum.obbaa.dm.DeviceStateProvider;
import org.broadband_forum.obbaa.store.dm.DeviceAdminStore;
import org.broadband_forum.obbaa.store.dm.DeviceInfo;

/**
 * Created by kbhatk on 29/9/17.
 */
public class DeviceManagerImpl implements DeviceManager {
    private final DeviceAdminStore m_dms;
    private Set<DeviceStateProvider> m_deviceStateProviders = new LinkedHashSet<>();
    private final NetconfConnectionManager m_cm;

    public DeviceManagerImpl(DeviceAdminStore dms, NetconfConnectionManager cm) {
        m_dms = dms;
        m_cm = cm;
    }

    public void createDevice(DeviceInfo deviceInfo) {
        m_dms.create(deviceInfo);
        callDeviceAdded(deviceInfo.getKey());
    }

    private void callDeviceAdded(String deviceName) {
        for (DeviceStateProvider provider : m_deviceStateProviders) {
            provider.deviceAdded(deviceName);
        }
    }

    public DeviceInfo getDevice(String deviceName) {
        DeviceInfo deviceInfo = m_dms.get(deviceName);
        if (deviceInfo == null) {
            throw new IllegalArgumentException("Device " + deviceName + " does not exist");
        }
        populateDeviceStateFromProvider(deviceInfo);
        return deviceInfo;
    }

    private void populateDeviceStateFromProvider(DeviceInfo deviceInfo) {
        for (DeviceStateProvider provider : m_deviceStateProviders) {
            Map<String, Object> stateInfo = provider.getState(deviceInfo.getKey());
            deviceInfo.getDeviceState().putAll(stateInfo);
        }
    }

    @Override
    public Set<DeviceInfo> getAllDevices() {
        //non performant code
        Set<DeviceInfo> allDevices = m_dms.getAllEntries();
        Set<DeviceInfo> returnValue = new HashSet<>();
        for (DeviceInfo info : allDevices) {
            returnValue.add(getDevice(info.getKey()));
        }
        return returnValue;
    }

    public void updateDevice(DeviceInfo deviceInfo) {
        String deviceName = deviceInfo.getKey();
        m_dms.update(deviceInfo);
        callDeviceRemoved(deviceName);
        callDeviceAdded(deviceName);
    }

    public void deleteDevice(String deviceName) {
        m_dms.delete(deviceName);
        callDeviceRemoved(deviceName);
    }

    private void callDeviceRemoved(String deviceName) {
        for (DeviceStateProvider provider : m_deviceStateProviders) {
            provider.deviceRemoved(deviceName);
        }
    }

    @Override
    public void removeDeviceStateProvider(DeviceStateProvider stateProvider) {
        m_deviceStateProviders.remove(stateProvider);
    }

    @Override
    public void addDeviceStateProvider(DeviceStateProvider stateProvider) {
        m_deviceStateProviders.add(stateProvider);
    }

    @Override
    public List<NewDeviceInfo> getNewDevices() {
        return m_cm.getNewDevices();
    }
}
