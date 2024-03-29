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

import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.nf.entities.NetworkFunction;

import org.broadband_forum.obbaa.pma.NetconfDeviceAlignmentService;
import org.broadband_forum.obbaa.pma.NetconfNetworkFunctionAlignmentService;
import org.broadband_forum.obbaa.pma.PmaServer;
import org.broadband_forum.obbaa.pma.PmaSession;
import org.w3c.dom.Document;

public class PmaServerSession implements PmaSession {
    private final PmaServer m_pmaServer;
    private final Device m_device;
    private final NetworkFunction m_networkFunction;
    private final NetconfDeviceAlignmentService m_das;
    private final NetconfNetworkFunctionAlignmentService m_nas;

    public PmaServerSession(Device device, PmaServer pmaServer, NetconfDeviceAlignmentService das) {
        m_device = device;
        m_pmaServer = pmaServer;
        m_das = das;
        m_networkFunction = null;
        m_nas = null;
    }

    public PmaServerSession(NetworkFunction networkFunction, PmaServer pmaServer, NetconfNetworkFunctionAlignmentService nas) {
        m_networkFunction = networkFunction;
        m_pmaServer = pmaServer;
        m_nas = nas;
        m_device = null;
        m_das = null;
    }

    @Override
    public Map<NetConfResponse, List<Notification>> executeNC(String netconfRequest) {
        try {
            PmaServer.setCurrentDevice(m_device);
            Document document = null;
            document = DocumentUtils.stringToDocument(netconfRequest);
            AbstractNetconfRequest request = DocumentToPojoTransformer.getRequest(document);
            return m_pmaServer.executeNetconf(request);
        } catch (NetconfMessageBuilderException e) {
            throw new IllegalArgumentException(String.format("Invalid netconf request received : %s for device %s",
                    netconfRequest, m_device), e);
        } finally {
            PmaServer.clearCurrentDevice();
        }
    }

    @Override
    public void forceAlign() {
        try {
            if (m_das != null) {
                PmaServer.setCurrentDevice(m_device);
                GetConfigRequest getConfig = new GetConfigRequest();
                getConfig.setSourceRunning();
                getConfig.setMessageId("internal");
                Map.Entry<NetConfResponse, List<Notification>> entry = getNetConfResponseListEntry(getConfig);
                m_das.forceAlign(m_device, entry.getKey());
            }
            else {
                if (m_nas != null) {
                    GetConfigRequest getConfig = new GetConfigRequest();
                    getConfig.setSourceRunning();
                    getConfig.setMessageId("internal");
                    Map.Entry<NetConfResponse, List<Notification>> entry = getNetConfResponseListEntry(getConfig);
                    m_nas.forceAlign(m_networkFunction, entry.getKey());
                }
            }

        } finally {
            PmaServer.clearCurrentDevice();
        }
    }

    @Override
    public void align() {
        try {
            PmaServer.setCurrentDevice(m_device);
            GetConfigRequest getConfig = new GetConfigRequest();
            getConfig.setSourceRunning();
            getConfig.setMessageId("internal");
            Map.Entry<NetConfResponse, List<Notification>> entry = getNetConfResponseListEntry(getConfig);
            //TODO obbaa-366 if m_das is null, call m_nas which in turn will call network function specific objects
            if (m_das != null) {
                m_das.align(m_device, entry.getKey());
            }
            else {
                if (m_nas != null) {
                    m_nas.align(m_networkFunction,entry.getKey());
                }
            }
        } finally {
            PmaServer.clearCurrentDevice();
        }
    }

    public boolean isActive() {
        return m_pmaServer.isActive();
    }

    @Override
    public NetConfResponse getAllPersistCfg() {
        PmaServer.setCurrentDevice(m_device);
        GetConfigRequest getConfig = new GetConfigRequest();
        getConfig.setSourceRunning();
        getConfig.setMessageId("internal");
        Map.Entry<NetConfResponse, List<Notification>> entry = getNetConfResponseListEntry(getConfig);
        return entry.getKey();
    }

    private Map.Entry<NetConfResponse, List<Notification>> getNetConfResponseListEntry(GetConfigRequest getConfig) {
        return m_pmaServer.executeNetconf(getConfig).entrySet().iterator().next();
    }
}
