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

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.adapter.threadlocals.ThreadLocalModelNodeHelperRegistry;
import org.broadband_forum.obbaa.adapter.threadlocals.ThreadLocalRootModelNodeAggregator;
import org.broadband_forum.obbaa.adapter.threadlocals.ThreadLocalSchemaRegistry;
import org.broadband_forum.obbaa.adapter.threadlocals.ThreadLocalSubsystemRegistry;
import org.broadband_forum.obbaa.device.adapter.AdapterContext;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.server.ResponseChannel;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetconfServer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.ThreadLocalDSMRegistry;
import org.broadband_forum.obbaa.netconf.server.QueuingMessageHandler;
import org.broadband_forum.obbaa.pma.PmaServer;

public class PmaServerImpl implements PmaServer {
    private static final Logger LOGGER = Logger.getLogger(PmaServerImpl.class);
    private final NetconfServer m_netconfServer;
    private final QueuingMessageHandler m_msgHandler;
    private final InMemoryResponseChannel m_responseChannel;
    private final Device m_device;
    private final ModelNodeDataStoreManager m_dsm;
    private AdapterContext m_adapterContext;
    private boolean m_active = true;

    public PmaServerImpl(Device device, NetconfServer netconfServer, ModelNodeDataStoreManager dsm, AdapterContext adapterContext) {
        m_device = device;
        m_netconfServer = netconfServer;
        m_dsm = dsm;
        m_adapterContext = adapterContext;
        m_msgHandler = new QueuingMessageHandler(m_netconfServer);
        m_responseChannel = new InMemoryResponseChannel();
        m_adapterContext.addListener(() -> m_active = false);
    }

    @Override
    public NetConfResponse executeNetconf(AbstractNetconfRequest request) {
        PmaServer.setCurrentDevice(m_device);
        ThreadLocalDSMRegistry.setDsm(m_dsm);
        ThreadLocalSubsystemRegistry.setSubsystemRegistry(m_adapterContext.getSubSystemRegistry());
        ThreadLocalSchemaRegistry.setSchemaRegistry(m_adapterContext.getSchemaRegistry());
        ThreadLocalModelNodeHelperRegistry.setModelNodeHelperRegistry(m_adapterContext.getModelNodeHelperRegistry());
        ThreadLocalRootModelNodeAggregator.setRootAggregator(m_adapterContext.getRootModelNodeAggregator());
        try {
            m_msgHandler.processRequest(PMA_USER, request, m_responseChannel);
            return m_responseChannel.getLastResponse();
        } finally {
            ThreadLocalDSMRegistry.clearDsm();
            ThreadLocalSchemaRegistry.clearRegistry();
            ThreadLocalSubsystemRegistry.clearRegistry();
            ThreadLocalModelNodeHelperRegistry.clearRegistry();
            ThreadLocalRootModelNodeAggregator.clearRootAggregator();
            PmaServer.clearCurrentDevice();
        }
    }

    public boolean isActive() {
        return m_active;
    }

    public Device getDevice() {
        return m_device;
    }

    private class InMemoryResponseChannel implements ResponseChannel {
        NetConfResponse m_lastResponse;

        @Override
        public void sendResponse(NetConfResponse response, AbstractNetconfRequest request) throws
                NetconfMessageBuilderException {
            m_lastResponse = response;
        }

        @Override
        public void sendNotification(Notification notification) {
            LOGGER.info(String.format("not sending notification %s", notification.notificationToPrettyString()));
        }

        @Override
        public boolean isSessionClosed() {
            return false;
        }

        @Override
        public void markSessionClosed() {
            LOGGER.info("not marking session as closed");
        }

        public NetConfResponse getLastResponse() {
            return m_lastResponse;
        }
    }
}
