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

package org.broadband_forum.obbaa.device.adapter.impl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.device.adapter.DeviceInterface;
import org.broadband_forum.obbaa.dmyang.entities.ConnectionState;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.CopyConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigElement;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class NcCompliantAdapterDeviceInterface implements DeviceInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(NcCompliantAdapterDeviceInterface.class);
    private final NetconfConnectionManager m_ncm;

    public NcCompliantAdapterDeviceInterface(NetconfConnectionManager ncm) {
        m_ncm = ncm;
    }

    @Override
    public Future<NetConfResponse> align(Device device, EditConfigRequest request) throws ExecutionException {
        if (m_ncm.isConnected(device)) {
            return m_ncm.executeNetconf(device.getDeviceName(), request);
        } else {
            throw new IllegalStateException(String.format("Device not connected %s", device.getDeviceName()));
        }
    }

    @Override
    public Pair<AbstractNetconfRequest, Future<NetConfResponse>> forceAlign(Device device, NetConfResponse getConfigResponse)
            throws NetconfMessageBuilderException, ExecutionException {
        if (m_ncm.isConnected(device)) {
            CopyConfigRequest ccRequest = new CopyConfigRequest();
            EditConfigElement config = new EditConfigElement();
            config.setConfigElementContents(getConfigResponse.getDataContent());
            ccRequest.setSourceConfigElement(config.getXmlElement());
            ccRequest.setTargetRunning();
            Future<NetConfResponse> responseFuture = m_ncm.executeNetconf(device.getDeviceName(), ccRequest);
            return new Pair<>(ccRequest, responseFuture);
        } else {
            throw new IllegalStateException(String.format("Device not connected %s", device.getDeviceName()));
        }
    }

    @Override
    public Future<NetConfResponse> get(Device device, GetRequest getRequest) throws ExecutionException {
        if (m_ncm.isConnected(device)) {
            return m_ncm.executeNetconf(device, getRequest);
        } else {
            throw new IllegalStateException(String.format("Device not connected %s", device.getDeviceName()));
        }
    }

    @Override
    public void veto(Device device, EditConfigRequest request, Document dataStore) throws SubSystemValidationException {

    }

    @Override
    public Future<NetConfResponse> getConfig(Device device, GetConfigRequest getConfigRequest) throws ExecutionException {
        if (m_ncm.isConnected(device)) {
            return m_ncm.executeNetconf(device, getConfigRequest);
        } else {
            throw new IllegalStateException(String.format("Device not connected %s", device.getDeviceName()));
        }
    }

    @Override
    public ConnectionState getConnectionState(Device device) {
        return m_ncm.getConnectionState(device);
    }
}
