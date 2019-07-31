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

import java.util.concurrent.ExecutionException;

import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.device.adapter.AdapterUtils;
import org.broadband_forum.obbaa.dm.DeviceManager;
import org.broadband_forum.obbaa.dm.DeviceStateProvider;
import org.broadband_forum.obbaa.dmyang.entities.AlignmentOption;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.pma.PmaRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultConfigService implements DeviceStateProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConfigService.class);

    private DeviceManager m_deviceManager;
    private PmaRegistry m_pmaRegistry;
    private AdapterManager m_adapterManager;

    public DefaultConfigService(DeviceManager deviceManager, PmaRegistry pmaRegistry, AdapterManager adapterManager) {
        m_deviceManager = deviceManager;
        m_pmaRegistry = pmaRegistry;
        m_adapterManager = adapterManager;
    }

    public void init() {
        m_deviceManager.addDeviceStateProvider(this);
    }

    public void destroy() {
        m_deviceManager.removeDeviceStateProvider(this);
    }

    @Override
    public void deviceAdded(String deviceName) {
        Device device = m_deviceManager.getDevice(deviceName);
        EditConfigRequest editReq = AdapterUtils.getDefaultEditReq(device, m_adapterManager);
        if (editReq != null) {
            if (AlignmentOption.PUSH.equals(device.getAlignmentOption())) {
                try {
                    m_pmaRegistry.executeNC(deviceName, editReq.requestToString());
                } catch (ExecutionException e) {
                    LOGGER.error(String.format("Error when executing default-config xml request for device %s", deviceName));
                    throw new RuntimeException(e);
                }
            } else {
                LOGGER.info(String.format("For device %s, the alignment mechanism is pull configuration from device to PMA", deviceName));
            }
        } else {
            LOGGER.info(String.format("The adapter for device %s does not contain any default configurations", deviceName));
        }
    }

    @Override
    public void deviceRemoved(String deviceName) {
        //Do nothing
    }
}
