/*
 *   Copyright 2018 Broadband Forum
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

package org.broadband_forum.obbaa.libconsult;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.dm.DeviceManager;
import org.broadband_forum.obbaa.dmyang.entities.Device;

public class BaseLibConsult {

    private AdapterManager m_adapterManager;
    private DeviceManager m_deviceManager;

    public BaseLibConsult(AdapterManager adapterManager, DeviceManager deviceManager) {
        this.m_adapterManager = adapterManager;
        this.m_deviceManager = deviceManager;
    }

    public AdapterManager getAdapterManager() {
        return m_adapterManager;
    }

    public DeviceManager getDeviceManager() {
        return m_deviceManager;
    }

    protected void executeWithAllDevices(Consumer<Device> fun) {
        List<Device> allDevices = new ArrayList<>(getDeviceManager().getAllDevices());
        for (Device device : allDevices) {
            fun.accept(device);
        }
    }
}
