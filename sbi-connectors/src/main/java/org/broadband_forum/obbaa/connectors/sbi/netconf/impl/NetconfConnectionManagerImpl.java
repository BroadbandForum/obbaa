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

package org.broadband_forum.obbaa.connectors.sbi.netconf.impl;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObjectInfo;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.broadband_forum.obbaa.connectors.sbi.netconf.CallHomeListenerComposite;
import org.broadband_forum.obbaa.connectors.sbi.netconf.ConnectionState;
import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfTemplate;
import org.broadband_forum.obbaa.connectors.sbi.netconf.NewDeviceInfo;
import org.broadband_forum.obbaa.netconf.api.NetconfConfigurationBuilderException;
import org.broadband_forum.obbaa.netconf.api.client.CallHomeListener;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientConfiguration;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientDispatcher;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientDispatcherException;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.client.NetconfLoginProvider;
import org.broadband_forum.obbaa.netconf.api.client.util.NetconfClientConfigurationBuilder;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportFactory;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportOrder;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportProtocol;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.client.ssh.auth.PasswordLoginProvider;
import org.broadband_forum.obbaa.store.dm.DeviceAdminStore;
import org.broadband_forum.obbaa.store.dm.DeviceInfo;
import org.broadband_forum.obbaa.store.dm.SshConnectionInfo;
import org.jetbrains.annotations.NotNull;

/**
 * Created by kbhatk on 29/9/17.
 */
public class NetconfConnectionManagerImpl implements NetconfConnectionManager, CallHomeListener {
    private static final Logger LOGGER = Logger.getLogger(NetconfConnectionManagerImpl.class);
    private final NetconfClientDispatcher m_dispatcher;
    private final DeviceAdminStore m_adminStore;
    private final CallHomeListenerComposite m_callHomeListenerComposite;
    private GenericKeyedObjectPool<DeviceInfo, NetconfClientSession> m_connPool;
    private final DeviceMetaNetconfClientSessionKeyedPooledObjectFactory m_factory;
    private static final String DUID_PATTERN = "DUID/";
    private static final int DUID_VALUE_START_INDEX = 5;
    private Map<DeviceInfo, NetconfClientSession> m_callHomeSessions = new ConcurrentHashMap<>();
    private Map<String, NewDeviceInfo> m_newDeviceInfos = new ConcurrentHashMap<>();


    public NetconfConnectionManagerImpl(CallHomeListenerComposite callHomeListenerComposite, DeviceAdminStore
            adminStore,
                                        NetconfClientDispatcher dispatcher) {
        m_callHomeListenerComposite = callHomeListenerComposite;
        m_adminStore = adminStore;
        m_dispatcher = dispatcher;
        m_factory = new DeviceMetaNetconfClientSessionKeyedPooledObjectFactory();
        setupConnPool();
    }

    public static void fillTransportDetails(SshConnectionInfo sshConnectionInfo, NetconfClientConfigurationBuilder
            configurationBuilder) {
        NetconfTransportOrder transportOrder = new NetconfTransportOrder();
        transportOrder.setTransportType(NetconfTransportProtocol.SSH.name());
        transportOrder.setServerSocketAddress(new InetSocketAddress(sshConnectionInfo.getIp(), sshConnectionInfo
                .getPort()));
        configurationBuilder.setNetconfLoginProvider(new PasswordLoginProvider(sshConnectionInfo.getUsername(),
                sshConnectionInfo
                        .getPassword()));
        try {
            configurationBuilder.setTransport(NetconfTransportFactory.makeNetconfTransport(transportOrder));
        } catch (NetconfConfigurationBuilderException e) {
            throw new RuntimeException(e);
        }
    }

    private void setupConnPool() {
        m_connPool = new GenericKeyedObjectPool<>(m_factory);
        m_connPool.setTestOnBorrow(true);
        m_connPool.setTestOnReturn(true);
        m_connPool.setTimeBetweenEvictionRunsMillis(-1); // Disable Eviction Thread
        m_connPool.setMaxIdlePerKey(1);
        m_connPool.setMinIdlePerKey(1);
    }

    void auditConnections() {
        Set<DeviceInfo> allDevices = m_adminStore.getAllEntries();
        for (DeviceInfo meta : allDevices) {
            NetconfClientSession session = null;
            try {
                if (!meta.isCallHome()) {
                    session = m_connPool.borrowObject(meta);
                }
            } catch (Exception e) {
                LOGGER.error(String.format("Could not setup connection to device %s", meta), e);
            } finally {
                if (session != null) {
                    m_connPool.returnObject(meta, session);
                }
            }
        }
        Set<DeviceInfo> deletedDevices = new HashSet<>(m_factory.getAllKeys());
        deletedDevices.removeAll(allDevices);
        for (DeviceInfo meta : deletedDevices) {
            LOGGER.debug(String.format("Device %s has been un-managed, closing the connection", meta));
            m_connPool.clear(meta);
        }
    }

    private NetconfClientSession createSessionToDevice(DeviceInfo deviceInfo) {
        try {
            NetconfClientSession session = m_dispatcher.createClient(getClientConfig(deviceInfo)).get();
            if (session != null) {
                LOGGER.info(String.format("Connected to device %s", deviceInfo));
                return session;
            }
        } catch (NetconfClientDispatcherException | InterruptedException | ExecutionException e) {
            LOGGER.debug(String.format("Could not setup connection to device %s", deviceInfo), e);
        }
        return null;
    }

    private NetconfClientConfiguration getClientConfig(DeviceInfo meta) {
        try {
            NetconfClientConfigurationBuilder builder = NetconfClientConfigurationBuilder
                    .createDefaultNcClientBuilder();
            builder.addCapability(NetconfResources.NETCONF_BASE_CAP_1_0);
            fillTransportDetails(meta.getDeviceConnectionInfo(), builder);
            return builder.build();
        } catch (NetconfConfigurationBuilderException | UnknownHostException e) {
            throw new RuntimeException(String.format("Error while preparing configuration to connect to the device %s",
                    meta), e);
        }
    }

    public Map<String, List<DefaultPooledObjectInfo>> getAllSessions() {
        return m_connPool.listAllObjects();
    }

    @Override
    public Future<NetConfResponse> executeNetconf(DeviceInfo deviceInfo, AbstractNetconfRequest request)
            throws IllegalStateException, ExecutionException {
        return executeWithSession(deviceInfo, session -> {
            try {
                LOGGER.info(String.format("Sending RPC to device %s RPC %s", deviceInfo, request.requestToString()));
                return wrapLogFuture(deviceInfo, session.sendRpc(request));
            } catch (Exception e) {
                throw new ExecutionException(e);
            }
        });
    }

    @Override
    public Future<NetConfResponse> executeNetconf(String deviceName, AbstractNetconfRequest request) throws
            IllegalStateException,
            ExecutionException {
        return executeNetconf(m_adminStore.get(deviceName), request);
    }

    @Override
    public <RT> RT executeWithSession(DeviceInfo deviceInfo, NetconfTemplate<RT> netconfTemplate)
            throws IllegalStateException, ExecutionException {
        //Since this is a public API from connection manager, we do not want to allow creation of connection to a
        // "non-managed" device here.
        if (!isConnected(deviceInfo)) {
            throw new IllegalStateException(String.format("Device not connected : %s", deviceInfo.getKey()));
        }
        NetconfClientSession deviceSession = null;
        try {
            deviceSession = m_connPool.borrowObject(deviceInfo);
            return netconfTemplate.execute(deviceSession);
        } catch (Exception e) {
            throw new ExecutionException(e);
        } finally {
            if (deviceSession != null) {
                m_connPool.returnObject(deviceInfo, deviceSession);
            }
        }
    }

    @Override
    public boolean isConnected(DeviceInfo deviceInfo) {
        //active + idle = total
        return ((m_connPool.getNumActive(deviceInfo) + m_connPool.getNumIdle(deviceInfo)) >= 1);
    }

    @Override
    public ConnectionState getConnectionState(String deviceName) {
        DeviceInfo deviceInfo = m_adminStore.get(deviceName);
        ConnectionState connectionState = new ConnectionState();
        if (isConnected(deviceInfo)) {
            connectionState.setConnected(true);
            try {
                executeWithSession(deviceInfo, (NetconfTemplate<Void>) session -> {
                    connectionState.setCreationTime(new Date(session.getCreationTime()));
                    connectionState.setCapabilities(session.getServerCapabilities());
                    return null;
                });
            } catch (ExecutionException e) {
                LOGGER.error("Error while fetching creation time of session", e);
            }
        }
        return connectionState;
    }

    @Override
    public List<NewDeviceInfo> getNewDevices() {
        return new ArrayList(m_newDeviceInfos.values());
    }

    @Override
    public void dropNewDeviceConnection(String duid) {
        NewDeviceInfo info = m_newDeviceInfos.get(duid);
        if (info != null) {
            info.closeSession();
        }
    }

    private Future<NetConfResponse> wrapLogFuture(DeviceInfo deviceInfo, Future<NetConfResponse> future) {
        return new LoggingFuture(deviceInfo, future);
    }

    public void init() {
        m_callHomeListenerComposite.addListener(this);
    }

    @Override
    public void connectionEstablished(NetconfClientSession deviceSession, NetconfLoginProvider netconfLoginProvider,
                                      X509Certificate deviceCert, boolean isSelfSigned) {
        InetSocketAddress remoteAddress = (InetSocketAddress) deviceSession.getRemoteAddress();
        String ipAddr = remoteAddress.getAddress().getHostAddress();
        int port = remoteAddress.getPort();
        String duid = extractDuid(deviceCert, ipAddr, port);
        DeviceInfo callHomeDeviceInfo = m_adminStore.getCallHomeDeviceWithDuid(duid);
        if (callHomeDeviceInfo == null) {
            //its a new device that is not configured
            LOGGER.info(String.format("An un-managed device with duid : %s is calling home from ip : %s port : %s",
                    duid, ipAddr, port));
            m_newDeviceInfos.put(duid, new NewDeviceInfo(duid, deviceSession));
            deviceSession.addSessionListener(sessionId -> m_newDeviceInfos.remove(duid));
            return;
        }
        addCallHomeSession(callHomeDeviceInfo, deviceSession);
        NetconfClientSession borrowedSession = null;
        try {
            borrowedSession = m_connPool.borrowObject(callHomeDeviceInfo);
        } catch (Exception e) {
            LOGGER.error("Error while borrowing call-home session", e);
        } finally {
            if (borrowedSession != null) {
                m_connPool.returnObject(callHomeDeviceInfo, borrowedSession);
            }
        }
    }

    private void addCallHomeSession(DeviceInfo callHomeDeviceInfo, NetconfClientSession deviceSession) {
        m_callHomeSessions.put(callHomeDeviceInfo, deviceSession);
        deviceSession.addSessionListener(i -> m_connPool.clear(callHomeDeviceInfo));
    }

    public String extractDuid(X509Certificate x509Certificate, String ipAddress, Integer port) {
        LOGGER.debug(String.format("Retrieving DUID of call home device from Certificate : %s", x509Certificate
                .getSubjectDN()));
        X500Name x500Name = getX500Name(x509Certificate);
        String commonName = getSubjectCN(x500Name);


        LOGGER.debug(String.format("Retrieved SubjectCN : %s of call home device from Certificate", commonName));
        if (containsIDevId(x500Name, ipAddress, port)) {
            return commonName;
        } else {
            if (commonName != null && commonName.startsWith(DUID_PATTERN)) {
                String duid = commonName.substring(DUID_VALUE_START_INDEX);
                return duid;
            }
        }
        return null;
    }

    private boolean containsIDevId(X500Name x500Name, String ipAddress, Integer port) {
        RDN[] rdns = x500Name.getRDNs(BCStyle.SERIALNUMBER);
        if (rdns.length > 0) {
            LOGGER.debug(String.format(" The certificate of the device IP: %s , port: %s contains serialnumber",
                    ipAddress, port));
            return true;
        }
        LOGGER.debug(String.format(" The certificate of the device IP: %s , port: %s does not contain serialnumber",
                ipAddress, port));
        return false;
    }

    private String getSubjectCN(X500Name x500Name) {
        RDN[] rdns = x500Name.getRDNs(BCStyle.CN);
        if (rdns.length > 0) {
            RDN commonName = rdns[0];
            return IETFUtils.valueToString(commonName.getFirst().getValue());
        }
        return null;
    }

    private X500Name getX500Name(X509Certificate x509Certificate) {
        Principal principal = x509Certificate.getSubjectDN();
        return new X500Name(principal.getName());
    }

    public void destroy() {
        m_callHomeListenerComposite.removeListener(this);
    }

    private class DeviceMetaNetconfClientSessionKeyedPooledObjectFactory
            extends BaseKeyedPooledObjectFactory<DeviceInfo, NetconfClientSession> {
        Set<DeviceInfo> m_allKeys = Collections.newSetFromMap(new ConcurrentHashMap<>());

        @Override
        public NetconfClientSession create(DeviceInfo key) {
            m_allKeys.add(key);
            if (key.isCallHome()) {
                return m_callHomeSessions.get(key);
            }
            NetconfClientSession sessionToDevice = createSessionToDevice(key);
            if (sessionToDevice != null) {
                sessionToDevice.addSessionListener(i -> m_connPool.clear(key));
            }
            return sessionToDevice;
        }

        @Override
        public PooledObject<NetconfClientSession> wrap(NetconfClientSession value) {
            return new DefaultPooledObject<>(value);
        }

        @Override
        public void destroyObject(DeviceInfo key, PooledObject<NetconfClientSession> pooledObject) {
            NetconfClientSession session = pooledObject.getObject();
            if (session != null) {
                session.closeAsync();
            }
            m_allKeys.remove(key);
            m_callHomeSessions.remove(key);
        }

        @Override
        public boolean validateObject(DeviceInfo key, PooledObject<NetconfClientSession> pooledObject) {
            NetconfClientSession session = pooledObject.getObject();
            if (session == null) {
                return false;
            }
            return session.isOpen();
        }

        public Set<DeviceInfo> getAllKeys() {
            return m_allKeys;
        }
    }

    public class LoggingFuture implements Future<NetConfResponse> {
        private final Future<NetConfResponse> m_innerFuture;
        private final DeviceInfo m_deviceInfo;

        public LoggingFuture(DeviceInfo deviceInfo, Future<NetConfResponse> future) {
            m_deviceInfo = deviceInfo;
            m_innerFuture = future;
        }

        @Override
        public boolean cancel(boolean bool) {
            return m_innerFuture.cancel(bool);
        }

        @Override
        public boolean isCancelled() {
            return m_innerFuture.isCancelled();
        }

        @Override
        public boolean isDone() {
            return m_innerFuture.isDone();
        }

        @Override
        public NetConfResponse get() throws InterruptedException, ExecutionException {
            NetConfResponse netConfResponse = m_innerFuture.get();
            LOGGER.info(String.format("Got response for device %s, response %s", m_deviceInfo, netConfResponse
                    .responseToString()));
            return netConfResponse;
        }

        @Override
        public NetConfResponse get(long value, @NotNull TimeUnit timeUnit) throws InterruptedException,
                ExecutionException,
                TimeoutException {
            NetConfResponse netConfResponse = m_innerFuture.get(value, timeUnit);
            LOGGER.info(String.format("Got response for device %s, response %s", m_deviceInfo, netConfResponse
                    .responseToString()));
            return netConfResponse;
        }
    }
}
