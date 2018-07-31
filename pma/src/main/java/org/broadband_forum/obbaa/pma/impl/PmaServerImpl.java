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
import org.broadband_forum.obbaa.store.dm.DeviceInfo;

public class PmaServerImpl implements PmaServer {
    private static final Logger LOGGER = Logger.getLogger(PmaServerImpl.class);
    private final NetconfServer m_netconfServer;
    private final QueuingMessageHandler m_msgHandler;
    private final InMemoryResponseChannel m_responseChannel;
    private final DeviceInfo m_deviceInfo;
    private final ModelNodeDataStoreManager m_dsm;

    public PmaServerImpl(DeviceInfo deviceInfo, NetconfServer netconfServer, ModelNodeDataStoreManager dsm) {
        m_deviceInfo = deviceInfo;
        m_netconfServer = netconfServer;
        m_dsm = dsm;
        m_msgHandler = new QueuingMessageHandler(m_netconfServer);
        m_responseChannel = new InMemoryResponseChannel();
    }

    @Override
    public NetConfResponse executeNetconf(AbstractNetconfRequest request) {
        PmaServer.setCurrentDevice(m_deviceInfo);
        ThreadLocalDSMRegistry.setDsm(m_dsm);
        try {
            m_msgHandler.processRequest(PMA_USER, request, m_responseChannel);
            return m_responseChannel.getLastResponse();
        } finally {
            ThreadLocalDSMRegistry.clearDsm();
            PmaServer.clearCurrentDevice();
        }
    }

    public DeviceInfo getDeviceInfo() {
        return m_deviceInfo;
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
