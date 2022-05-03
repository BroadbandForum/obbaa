/*
 * Copyright 2022 Broadband Forum
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

import java.io.File;
//import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.adapter.threadlocals.ThreadLocalModelNodeHelperRegistry;
import org.broadband_forum.obbaa.adapter.threadlocals.ThreadLocalRootModelNodeAggregator;
import org.broadband_forum.obbaa.adapter.threadlocals.ThreadLocalSchemaRegistry;
import org.broadband_forum.obbaa.adapter.threadlocals.ThreadLocalSubsystemRegistry;
import org.broadband_forum.obbaa.device.adapter.AdapterContext;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
//import org.broadband_forum.obbaa.netconf.api.messages.NetconfNotification;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.server.ResponseChannel;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetconfServer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.ThreadLocalDSMRegistry;
import org.broadband_forum.obbaa.nf.entities.NetworkFunction;
import org.broadband_forum.obbaa.pma.DeviceXmlStore;
import org.broadband_forum.obbaa.pma.PmaServer;
import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * Implementation of Pma Server for network functions
 * </p>
 * Created by J.V.Correia (Altice Labs) on 11/02/2022.
 */
public class PmaServerImplNf implements PmaServer {
    private static final Logger LOGGER = Logger.getLogger(PmaServerImplNf.class);
    private final NetworkFunction m_networkFunction;
    private final DeviceXmlStore m_deviceStore;
    private final ModelNodeDataStoreManager m_dsm;
    private final NetconfServer m_netconfServer;
    private final QueMessageHandler m_msgHandler;
    private final String m_networkFunctionFilePathDir;
    private final InMemoryResponseChannel m_responseChannel;
    private AdapterContext m_stdAdapterContext;
    private boolean m_active = true;

    public PmaServerImplNf(NetworkFunction nf, NetconfServer netconfServer, Pair<ModelNodeDataStoreManager, DeviceXmlStore>
            dsmStorePair, AdapterContext stdAdapterContext, String networkFunctionFilePathDir) {
        m_networkFunction = nf;
        m_netconfServer = netconfServer;
        m_dsm = dsmStorePair.getFirst();
        m_deviceStore = dsmStorePair.getSecond();
        m_msgHandler = new QueMessageHandler(m_netconfServer);
        m_responseChannel = new InMemoryResponseChannel();
        m_stdAdapterContext = stdAdapterContext;
        m_networkFunctionFilePathDir = networkFunctionFilePathDir;
    }

    @Override
    public Map<NetConfResponse, List<Notification>> executeNetconf(AbstractNetconfRequest request) {

//        LOGGER.info("Network function - executeNetconf");
//        Map<NetConfResponse, List<Notification>> replyMap = new HashMap<>();
//        List<Notification> notifications = new ArrayList<>();
//        Notification notification = new NetconfNotification();
//        notifications.add(notification);
//        NetConfResponse response = new NetConfResponse();
//        replyMap.put(response,notifications);
//
//        //should send the request using the GPB consumer, but keep the schema registry operations
//        return replyMap;

        PmaServer.setCurrentDevice(m_networkFunction);
        PmaServer.setCurrentDeviceXmlStore(m_deviceStore);
        ThreadLocalDSMRegistry.setDsm(m_dsm);
        ThreadLocalSubsystemRegistry.setSubsystemRegistry(m_stdAdapterContext.getSubSystemRegistry());
        ThreadLocalSchemaRegistry.setSchemaRegistry(m_stdAdapterContext.getSchemaRegistry());
        m_netconfServer.getDataStore(StandardDataStores.RUNNING).setNamespaceContext(ThreadLocalSchemaRegistry.getRegistry());
        ThreadLocalModelNodeHelperRegistry.setModelNodeHelperRegistry(m_stdAdapterContext.getModelNodeHelperRegistry());
        ThreadLocalRootModelNodeAggregator.setRootAggregator(m_stdAdapterContext.getRootModelNodeAggregator());
        if (request instanceof EditConfigRequest) {
            PmaServer.setBackupDeviceXmlStore(createBackUpStore(getDeviceStoreBackupFilePath(m_networkFunction.getNetworkFunctionName())));
        }
        try {
            List<Notification> notifications = m_msgHandler.processRequest(PMA_USER, request, m_responseChannel);
            Map<NetConfResponse, List<Notification>> replyMap = new HashMap<>();
            replyMap.put(m_responseChannel.getLastResponse(), notifications);
            return replyMap;
        } finally {
            ThreadLocalDSMRegistry.clearDsm();
            ThreadLocalSchemaRegistry.clearRegistry();
            ThreadLocalSubsystemRegistry.clearRegistry();
            ThreadLocalModelNodeHelperRegistry.clearRegistry();
            ThreadLocalRootModelNodeAggregator.clearRootAggregator();
            PmaServer.clearCurrentDevice();
            PmaServer.clearCurrentDeviceXmlStore();
            if (request instanceof EditConfigRequest) {
                PmaServer.clearBackupDeviceXmlStore();
                deleteBackupFile(m_networkFunction.getNetworkFunctionName());
            }
        }
    }

    private DeviceXmlStore createBackUpStore(String filePath) {
        final DeviceXmlStore backupStore = new DeviceXmlStore(filePath);
        backupStore.setDeviceXml(m_deviceStore.getDeviceXml());
        return backupStore;
    }

    private void deleteBackupFile(String networkFunctionName) {
        String filePath = getDeviceStoreBackupFilePath(networkFunctionName);
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }

    @NotNull
    private String getDeviceStoreBackupFilePath(String networkFunctionName) {
        return m_networkFunctionFilePathDir + File.separator + networkFunctionName + "_running_ds_backup.xml";
    }

    @Override
    public boolean isActive() {
        return m_active;
    }

    public NetworkFunction getNetworkFunction() {
        return m_networkFunction;
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

        @Override
        public CompletableFuture<Boolean> getCloseFuture() {
            return null;
        }

        public NetConfResponse getLastResponse() {
            return m_lastResponse;
        }

    }
}
