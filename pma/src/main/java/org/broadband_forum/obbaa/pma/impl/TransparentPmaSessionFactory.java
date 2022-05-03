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

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.PmaResourceId;
import org.broadband_forum.obbaa.nm.devicemanager.DeviceManager;
import org.broadband_forum.obbaa.pma.PmaSession;
import org.broadband_forum.obbaa.pma.PmaSessionFactory;

public class TransparentPmaSessionFactory extends PmaSessionFactory {
    private final NetconfConnectionManager m_connectionManager;
    private final DeviceManager m_deviceManager;

    public TransparentPmaSessionFactory(NetconfConnectionManager connectionManager, DeviceManager deviceManager) {
        m_connectionManager = connectionManager;
        m_deviceManager = deviceManager;
    }

    @Override
    public PmaSession create(PmaResourceId resourceId) throws Exception {
        Device device = m_deviceManager.getDevice(resourceId.getResourceName());
        return new TransparentPmaSession(device, m_connectionManager);
    }

    @Override
    public PooledObject<PmaSession> wrap(PmaSession value) {
        return new DefaultPooledObject<>(value);
    }

    @Override
    public void deviceDeleted(String deviceName) {
        //nothing to be done here
    }

    @Override
    public void networkFunctionDeleted(String networkFunctionName) {

    }
}
