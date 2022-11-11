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
import org.broadband_forum.obbaa.connectors.sbi.netconf.ConnectionListener;
import org.broadband_forum.obbaa.connectors.sbi.netconf.LoggingFuture;
import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfTemplate;
import org.broadband_forum.obbaa.connectors.sbi.netconf.NewDeviceInfo;
import org.broadband_forum.obbaa.dmyang.dao.DeviceDao;
import org.broadband_forum.obbaa.dmyang.entities.Authentication;
import org.broadband_forum.obbaa.dmyang.entities.ConnectionState;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.PmaResource;
import org.broadband_forum.obbaa.dmyang.tx.TXTemplate;
import org.broadband_forum.obbaa.dmyang.tx.TxService;
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
import org.broadband_forum.obbaa.nf.dao.NetworkFunctionDao;
import org.broadband_forum.obbaa.nf.entities.NetworkFunction;

/**
 * Created by kbhatk on 29/9/17.
 */
public class NetconfConnectionManagerImpl implements NetconfConnectionManager, CallHomeListener {
    private static final Logger LOGGER = Logger.getLogger(NetconfConnectionManagerImpl.class);
    private final NetconfClientDispatcher m_dispatcher;
    private DeviceDao m_deviceDao;
    private NetworkFunctionDao m_networkFunctionDao;
    private TxService m_txService;
    private final CallHomeListenerComposite m_callHomeListenerComposite;
    private GenericKeyedObjectPool<String, NetconfClientSession> m_connPool;
    private GenericKeyedObjectPool<String, NetconfClientSession> m_nfConnPool;
    private final DeviceMetaNetconfClientSessionKeyedPooledObjectFactory m_factory;
    private final NetworkFunctionMetaNetconfClientSessionKeyedPooledObjectFactory m_nfFactory;
    private static final String DUID_PATTERN = "DUID/";
    private static final int DUID_VALUE_START_INDEX = 5;
    private Map<String, NetconfClientSession> m_callHomeSessions = new ConcurrentHashMap<>();
    private Map<String, NetconfClientSession> m_mediatedSessions = new ConcurrentHashMap<>();
    private Map<String, NetconfClientSession> m_nfMediatedSessions = new ConcurrentHashMap<>();
    private Map<String, NewDeviceInfo> m_newDeviceInfos = new ConcurrentHashMap<>();
    private List<ConnectionListener> m_connectionListeners = new ArrayList<>();


    public NetconfConnectionManagerImpl(CallHomeListenerComposite callHomeListenerComposite,
                                        NetconfClientDispatcher dispatcher) {
        m_callHomeListenerComposite = callHomeListenerComposite;
        m_dispatcher = dispatcher;
        m_factory = new DeviceMetaNetconfClientSessionKeyedPooledObjectFactory();
        m_nfFactory = new NetworkFunctionMetaNetconfClientSessionKeyedPooledObjectFactory();
        setupConnPool();
    }

    public NetworkFunctionDao getNetworkFunctionDao() {
        return m_networkFunctionDao;
    }

    public void setNetworkFunctionDao(NetworkFunctionDao networkFunctionDao) {
        m_networkFunctionDao = networkFunctionDao;
    }

    public DeviceDao getDeviceDao() {
        return m_deviceDao;
    }

    public void setDeviceDao(DeviceDao deviceDao) {
        m_deviceDao = deviceDao;
    }

    public TxService getTxService() {
        return m_txService;
    }

    public void setTxService(TxService txService) {
        m_txService = txService;
    }

    public static void fillTransportDetails(Authentication authentication, NetconfClientConfigurationBuilder
            configurationBuilder) {
        NetconfTransportOrder transportOrder = new NetconfTransportOrder();
        transportOrder.setTransportType(NetconfTransportProtocol.SSH.name());
        transportOrder.setServerSocketAddress(new InetSocketAddress(authentication.getAddress(), Integer.parseInt(authentication
                .getManagementPort())));
        configurationBuilder.setNetconfLoginProvider(new PasswordLoginProvider(authentication.getUsername(),
                authentication
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

        //FIXME obbaa-366 simply copied from above
        m_nfConnPool = new GenericKeyedObjectPool<>(m_nfFactory);
        m_nfConnPool.setTestOnBorrow(true);
        m_nfConnPool.setTestOnBorrow(true);
        m_nfConnPool.setTimeBetweenEvictionRunsMillis(-1);
        m_nfConnPool.setMaxIdlePerKey(1);
        m_nfConnPool.setMinIdlePerKey(1);
    }

    void auditConnections() {
        m_txService.executeWithTx(new TXTemplate<Void>() {
            @Override
            public Void execute() {
                List<Device> allDevices = m_deviceDao.findAllDevices();
                for (Device meta : allDevices) {
                    NetconfClientSession session = null;
                    try {
                        if (!meta.getDeviceManagement().getDeviceConnection().getConnectionModel().equals("call-home")) {
                            session = m_connPool.borrowObject(meta.getDeviceName());
                        }
                    } catch (Exception e) {
                        LOGGER.debug(String.format("Could not setup connection to device %s", meta), e);
                    } finally {
                        if (session != null) {
                            m_connPool.returnObject(meta.getDeviceName(), session);
                        }
                    }
                }
                Set<Device> deletedDevices = new HashSet<>(m_factory.getAllKeys());
                deletedDevices.removeAll(allDevices);
                for (Device meta : deletedDevices) {
                    LOGGER.debug(String.format("Device %s has been un-managed, closing the connection", meta));
                    m_connPool.clear(meta.getDeviceName()); //this will close any idle connections
                    m_factory.getAllKeys().remove(meta);
                }
                return null;
            }
        });


        m_txService.executeWithTx(new TXTemplate<Void>() {
            @Override
            public Void execute() {
                List<NetworkFunction> networkFunctions = m_networkFunctionDao.findAllNetworkFunctions();
                for (NetworkFunction networkFunction : networkFunctions) {
                    NetconfClientSession session = null;
                    try {
                        session = m_nfConnPool.borrowObject(networkFunction.getNetworkFunctionName());
                    } catch (Exception e) {
                        LOGGER.debug(String.format("Could not setup connection to network function %s",
                                networkFunction), e);
                    } finally {
                        if (session != null) {
                            m_nfConnPool.returnObject(networkFunction.getNetworkFunctionName(), session);
                        }
                    }
                }
                Set<NetworkFunction> deletedNetworkFunctions = new HashSet<>(m_nfFactory.getAllKeys());
                deletedNetworkFunctions.removeAll(deletedNetworkFunctions);
                for (NetworkFunction networkFunction : deletedNetworkFunctions) {
                    LOGGER.debug(String.format("Network Function %s has been un-managed, closing the connection",
                            networkFunction));
                    m_nfConnPool.clear(networkFunction.getNetworkFunctionName());
                    m_nfFactory.getAllKeys().remove(networkFunction);
                }
                return null;
            }
        });

    }

    private NetconfClientSession createSessionToDevice(String deviceName) {
        NetconfClientSession session = null;
        Device device = getDeviceByGivenName(deviceName);
        if (device != null) {
            try {
                if (device.getDeviceManagement().isNetconf()) {
                    session = m_dispatcher.createClient(getClientConfig(device)).get();
                    if (session != null) {
                        LOGGER.info(String.format("Connected to device %s", device));
                        notifyDeviceConnected(deviceName, session);
                        return session;
                    }
                }
            } catch (NetconfClientDispatcherException | InterruptedException | ExecutionException e) {
                LOGGER.debug(String.format("Could not setup connection to device %s", device), e);
            }
            return null;
        }
        return session;
    }

    private void notifyDeviceConnected(String deviceName, NetconfClientSession session) {
        Device device = getDeviceByGivenName(deviceName);
        if (device != null) {
            for (ConnectionListener connectionListener : m_connectionListeners) {
                connectionListener.deviceConnected(device, session);
            }
        }
    }

    private void notifyDeviceDisconnected(String deviceName, NetconfClientSession session) {
        Device device = getDeviceByGivenName(deviceName);
        if (device != null) {
            for (ConnectionListener connectionListener : m_connectionListeners) {
                connectionListener.deviceDisConnected(device, session);
            }
        }
    }

    //TODO obbaa-366 missing some necessary methods (see createSessionToDevice)
    private NetconfClientSession createSessionToNetworkFunction(String networkFunctionName) {
        return null;
    }

    //TODO obbaa-366
    private void notifyNetworkFunctionConnected(String networkFunctionName, NetconfClientSession session) {

    }

    //TODO obbaa-366
    private void notifyNetworkFunctionDisconnected(String networkFunctionName, NetconfClientSession session) {

    }

    private NetworkFunction getNetworkFunctionByName(String networkFunctionName) {
        return m_txService.executeWithTx(() -> m_networkFunctionDao.getNetworkFunctionByName(networkFunctionName));
    }

    private Device getDeviceByGivenName(String deviceName) {
        return m_txService.executeWithTx(() -> m_deviceDao.getDeviceByName(deviceName));
    }

    private NetconfClientConfiguration getClientConfig(Device device) {
        try {
            NetconfClientConfigurationBuilder builder = NetconfClientConfigurationBuilder
                    .createDefaultNcClientBuilder();
            builder.addCapability(NetconfResources.NETCONF_BASE_CAP_1_0);
            Authentication authentication = device.getDeviceManagement().getDeviceConnection()
                    .getPasswordAuth().getAuthentication();
            fillTransportDetails(authentication, builder);
            return builder.build();
        } catch (NetconfConfigurationBuilderException | UnknownHostException e) {
            throw new RuntimeException(String.format("Error while preparing configuration to connect to the device %s",
                    device), e);
        }
    }

    public Map<String, List<DefaultPooledObjectInfo>> getAllSessions() {
        return m_connPool.listAllObjects();
    }

    public NetconfClientSession getMediatedDeviceSession(String deviceName) {
        return m_mediatedSessions.get(deviceName);
    }

    @Override
    public NetconfClientSession getMediatedNetworkFunctionSession(String networkFunctionName) {
        return m_nfMediatedSessions.get(networkFunctionName);
    }

    @Override
    public Future<NetConfResponse> executeNetconf(PmaResource resource, AbstractNetconfRequest request)
            throws IllegalStateException, ExecutionException {
        return executeWithSession(resource, session -> {
            try {
                LOGGER.info(String.format("Sending RPC to device %s RPC %s", resource, request.requestToString()));
                return wrapLogFuture(resource, session.sendRpc(request));
            } catch (Exception e) {
                throw new ExecutionException(e);
            }
        });
    }

    @Override
    public Future<NetConfResponse> executeNetconf(String deviceName, AbstractNetconfRequest request) throws
            IllegalStateException,
            ExecutionException {
        return executeNetconf(m_deviceDao.getDeviceByName(deviceName), request);
    }

    @Override
    public <RT> RT executeWithSession(PmaResource resource, NetconfTemplate<RT> netconfTemplate)
            throws IllegalStateException, ExecutionException {
        if (resource instanceof Device) {
            return executeWithSession((Device) resource,netconfTemplate);
        }
        if (resource instanceof NetworkFunction) {
            return executeWithSession((NetworkFunction) resource,netconfTemplate);
        }
        return null;
    }

    private  <RT> RT executeWithSession(NetworkFunction networkFunction, NetconfTemplate<RT> netconfTemplate)
            throws IllegalStateException, ExecutionException {
        //Since this is a public API from connection manager, we do not want to allow creation of connection to a
        // "non-managed" device here.
        if (!isConnected(networkFunction)) {
            throw new IllegalStateException(String.format("Network Function not connected : %s",
                    networkFunction.getNetworkFunctionName()));
        }
        NetconfClientSession networkFunctionSession = null;
        try {
            networkFunctionSession = m_nfConnPool.borrowObject(networkFunction.getNetworkFunctionName());
            return netconfTemplate.execute(networkFunctionSession);
        } catch (Exception e) {
            throw new ExecutionException(e);
        } finally {
            if (networkFunctionSession != null) {
                m_nfConnPool.returnObject(networkFunction.getNetworkFunctionName(), networkFunctionSession);
            }
        }
    }

    private  <RT> RT executeWithSession(Device device, NetconfTemplate<RT> netconfTemplate)
            throws IllegalStateException, ExecutionException {
        //Since this is a public API from connection manager, we do not want to allow creation of connection to a
        // "non-managed" device here.
        if (!isConnected(device)) {
            throw new IllegalStateException(String.format("Device not connected : %s", device.getDeviceName()));
        }
        NetconfClientSession deviceSession = null;
        try {
            deviceSession = m_connPool.borrowObject(device.getDeviceName());
            return netconfTemplate.execute(deviceSession);
        } catch (Exception e) {
            throw new ExecutionException(e);
        } finally {
            if (deviceSession != null) {
                m_connPool.returnObject(device.getDeviceName(), deviceSession);
            }
        }
    }

    @Override
    public boolean isConnected(PmaResource resource) {
        //active + idle = total
        if (resource instanceof Device) {
            Device device = (Device) resource;
            return ((m_connPool.getNumActive(device.getDeviceName())
                    + m_connPool.getNumIdle(device.getDeviceName())) >= 1);
        }
        if (resource instanceof NetworkFunction) {
            NetworkFunction networkFunction = (NetworkFunction) resource;
            return ((m_nfConnPool.getNumActive(networkFunction.getNetworkFunctionName())
                    + m_nfConnPool.getNumIdle(networkFunction.getNetworkFunctionName())) >= 1);
        }
        return false;
    }

    @Override
    public ConnectionState getConnectionState(PmaResource resource) {
        ConnectionState connectionState = new ConnectionState();
        if (isConnected(resource)) {
            connectionState.setConnected(true);
            try {
                executeWithSession(resource, (NetconfTemplate<Void>) session -> {
                    connectionState.setConnectionCreationTime(new Date(session.getCreationTime()));
                    connectionState.setDeviceCapability(session.getServerCapabilities());
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

    @Override
    public void registerDeviceConnectionListener(ConnectionListener connectionListener) {
        m_connectionListeners.add(connectionListener);
    }

    @Override
    public void unregisterDeviceConnectionListener(ConnectionListener connectionListener) {
        m_connectionListeners.remove(connectionListener);
    }

    private Future<NetConfResponse> wrapLogFuture(PmaResource resource, Future<NetConfResponse> future) {
        return new LoggingFuture(resource, future);
    }

    public void init() {
        m_callHomeListenerComposite.addListener(this);
    }

    @Override
    public void connectionEstablished(NetconfClientSession deviceSession, NetconfLoginProvider netconfLoginProvider,
                                      X509Certificate deviceCert, boolean isSelfSigned) {
        m_txService.executeWithTx(new TXTemplate<Void>() {
            @Override
            public Void execute() {
                InetSocketAddress remoteAddress = (InetSocketAddress) deviceSession.getRemoteAddress();
                String ipAddr = remoteAddress.getAddress().getHostAddress();
                int port = remoteAddress.getPort();
                String duid = extractDuid(deviceCert, ipAddr, port);
                Device callHomeDevice = m_deviceDao.findDeviceWithDuid(duid);
                if (callHomeDevice == null) {
                    //its a new device that is not configured
                    LOGGER.info(String.format("An un-managed device with duid : %s is calling home from ip : %s port : %s",
                            duid, ipAddr, port));
                    m_newDeviceInfos.put(duid, new NewDeviceInfo(duid, deviceSession));
                    deviceSession.addSessionListener((sessionId, closureReason) -> {
                        m_newDeviceInfos.remove(duid);
                    });
                    return null;
                }
                addCallHomeSession(callHomeDevice.getDeviceName(), deviceSession);
                NetconfClientSession borrowedSession = null;
                try {
                    borrowedSession = m_connPool.borrowObject(callHomeDevice.getDeviceName());
                } catch (Exception e) {
                    LOGGER.error("Error while borrowing call-home session", e);
                } finally {
                    if (borrowedSession != null) {
                        m_connPool.returnObject(callHomeDevice.getDeviceName(), borrowedSession);
                    }
                }
                return null;
            }
        });
    }

    private void addCallHomeSession(String callHomeDeviceName, NetconfClientSession deviceSession) {
        m_callHomeSessions.put(callHomeDeviceName, deviceSession);
        notifyDeviceConnected(callHomeDeviceName, deviceSession);
        deviceSession.addSessionListener((sessionId, closureReason) -> {
            m_connPool.clear(callHomeDeviceName);
            notifyDeviceDisconnected(callHomeDeviceName, deviceSession);
        });
    }

    @Override
    public void addMediatedDeviceNetconfSession(String deviceName, NetconfClientSession deviceSession) {
        m_mediatedSessions.put(deviceName, deviceSession);
        notifyDeviceConnected(deviceName, deviceSession);
        deviceSession.addSessionListener((sessionId, closureReason) -> {
            m_connPool.clear(deviceName);
            notifyDeviceDisconnected(deviceName, deviceSession);
        });
        NetconfClientSession borrowedSession = null;
        try {
            borrowedSession = m_connPool.borrowObject(deviceName);
        } catch (Exception e) {
            LOGGER.error("Error while borrowing mediated session", e);
        } finally {
            if (borrowedSession != null) {
                m_connPool.returnObject(deviceName, borrowedSession);
            }
        }
    }

    @Override
    public void addMediatedNetworkFunctionNetconfSession(String networkFunctionName,
                                                         NetconfClientSession networkFunctionSession) {
        m_nfMediatedSessions.put(networkFunctionName,networkFunctionSession);
        notifyNetworkFunctionConnected(networkFunctionName,networkFunctionSession);
        networkFunctionSession.addSessionListener((sessionId, closureReason) -> {
            m_nfConnPool.clear(networkFunctionName);
            notifyNetworkFunctionDisconnected(networkFunctionName,networkFunctionSession);
        });
        NetconfClientSession borrowedSession = null;
        try {
            borrowedSession = m_nfConnPool.borrowObject(networkFunctionName);
        } catch (Exception e) {
            LOGGER.error("Error while borrowing mediated session", e);
        } finally {
            if (borrowedSession != null) {
                m_nfConnPool.returnObject(networkFunctionName, borrowedSession);
            }
        }
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
            extends BaseKeyedPooledObjectFactory<String, NetconfClientSession> {
        Set<Device> m_allKeys = Collections.newSetFromMap(new ConcurrentHashMap<>());

        @Override
        public NetconfClientSession create(String key) {
            Device device = getDeviceByGivenName(key);
            if (device != null) {
                m_allKeys.add(device);
            }
            if (device.isCallhome()) {
                return m_callHomeSessions.get(key);
            } else if (device.isMediatedSession()) {
                return m_mediatedSessions.get(key);
            }
            NetconfClientSession sessionToDevice = createSessionToDevice(key);
            if (sessionToDevice != null) {
                sessionToDevice.addSessionListener((sessionId, closureReason) -> {
                    m_connPool.clear(key);
                    notifyDeviceDisconnected(key, sessionToDevice);
                });
            }
            return sessionToDevice;
        }

        @Override
        public PooledObject<NetconfClientSession> wrap(NetconfClientSession value) {
            return new DefaultPooledObject<>(value);
        }

        @Override
        public void destroyObject(String key, PooledObject<NetconfClientSession> pooledObject) {
            NetconfClientSession session = pooledObject.getObject();
            if (session != null) {
                session.closeAsync();
            }
            m_allKeys.remove(key);
            m_callHomeSessions.remove(key);
            m_mediatedSessions.remove(key);
        }

        @Override
        public boolean validateObject(String key, PooledObject<NetconfClientSession> pooledObject) {
            NetconfClientSession session = pooledObject.getObject();
            if (session == null) {
                return false;
            }
            return session.isOpen();
        }

        public Set<Device> getAllKeys() {
            return m_allKeys;
        }
    }

    private class NetworkFunctionMetaNetconfClientSessionKeyedPooledObjectFactory
            extends BaseKeyedPooledObjectFactory<String, NetconfClientSession> {
        Set<NetworkFunction> m_allKeys = Collections.newSetFromMap(new ConcurrentHashMap<>());

        @Override
        public NetconfClientSession create(String key) throws Exception {
            NetworkFunction networkFunction = getNetworkFunctionByName(key);
            m_allKeys.add(networkFunction);
            //FIXME obbaa-366 checking map instead of checking through network function (see "create" above)
            if (m_nfMediatedSessions.containsKey(key)) {
                return m_nfMediatedSessions.get(key);
            }
            NetconfClientSession sessionToNetworkFunction = createSessionToNetworkFunction(key);
            if (sessionToNetworkFunction != null) {
                sessionToNetworkFunction.addSessionListener((sessionId, closureReason) -> {
                    m_nfConnPool.clear(key);
                    notifyNetworkFunctionConnected(key,sessionToNetworkFunction);
                });
            }
            return sessionToNetworkFunction;
        }

        @Override
        public PooledObject<NetconfClientSession> wrap(NetconfClientSession value) {
            return new DefaultPooledObject<>(value);
        }

        @Override
        public void destroyObject(String key, PooledObject<NetconfClientSession> pooledObject) throws Exception {
            NetconfClientSession session = pooledObject.getObject();
            if (session != null) {
                session.closeAsync();
            }
            m_allKeys.remove(key);
            m_nfMediatedSessions.remove(key);
        }

        @Override
        public boolean validateObject(String key, PooledObject<NetconfClientSession> pooledObject) {
            NetconfClientSession session = pooledObject.getObject();
            if (session == null) {
                return false;
            }
            return session.isOpen();
        }

        public Set<NetworkFunction> getAllKeys() {
            return m_allKeys;
        }
    }
}
