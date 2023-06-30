/*
 * Copyright 2023 Broadband Forum
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

package org.broadband_forum.obbaa.ipfix.entities.adapter.impl;

import org.broadband_forum.obbaa.ipfix.entities.adapter.IpfixAdapterInterface;
import org.broadband_forum.obbaa.ipfix.entities.adapter.IpfixAdapterService;
import org.broadband_forum.obbaa.ipfix.entities.util.IpfixDeviceInterfaceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IpfixAdapterServiceImpl implements IpfixAdapterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IpfixAdapterServiceImpl.class);

    private IpfixAdapterInterface m_ipfixAdapterInterface;
    private IpfixDeviceInterfaceUtil m_storeIpfixDeviceInterface;
    private String m_deviceFamily;

    public IpfixAdapterServiceImpl(IpfixAdapterInterface ipfixAdapterInterface, IpfixDeviceInterfaceUtil storeIpfixDeviceInterface,
                                   String deviceFamily) {
        m_ipfixAdapterInterface = ipfixAdapterInterface;
        m_storeIpfixDeviceInterface = storeIpfixDeviceInterface;
        m_deviceFamily = deviceFamily;
    }

    @Override
    public void deployAdapter() {
        LOGGER.info("Deploying adapter" + m_deviceFamily + " in IPFIX ");
        m_storeIpfixDeviceInterface.onDeployed(m_ipfixAdapterInterface, m_deviceFamily);
    }

    @Override
    public void unDeployAdapter() {
        LOGGER.info("Undeploying adapter" + m_deviceFamily + " in IPFIX ");
        m_storeIpfixDeviceInterface.onUndeployed(m_ipfixAdapterInterface, m_deviceFamily);
    }
}
