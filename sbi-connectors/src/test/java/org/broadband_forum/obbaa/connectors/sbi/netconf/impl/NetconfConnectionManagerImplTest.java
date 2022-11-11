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

import static junit.framework.TestCase.fail;
import static org.broadband_forum.obbaa.netconf.api.x509certificates.CertificateUtil.getX509Certificates;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.InetSocketAddress;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.EntityTransaction;

import org.broadband_forum.obbaa.connectors.sbi.netconf.CallHomeListenerComposite;
import org.broadband_forum.obbaa.connectors.sbi.netconf.ConnectionListener;
import org.broadband_forum.obbaa.connectors.sbi.netconf.LoggingFuture;
import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfTemplate;
import org.broadband_forum.obbaa.dmyang.dao.DeviceDao;
import org.broadband_forum.obbaa.dmyang.entities.Authentication;
import org.broadband_forum.obbaa.dmyang.entities.ConnectionState;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.DeviceConnection;
import org.broadband_forum.obbaa.dmyang.entities.DeviceMgmt;
import org.broadband_forum.obbaa.dmyang.entities.PasswordAuth;
import org.broadband_forum.obbaa.dmyang.tx.TxService;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientConfiguration;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientDispatcher;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientDispatcherException;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSessionListener;
import org.broadband_forum.obbaa.netconf.api.client.NetconfLoginProvider;
import org.broadband_forum.obbaa.netconf.api.client.NetconfResponseFuture;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.transport.SshNetconfTransport;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;
import org.broadband_forum.obbaa.nf.dao.NetworkFunctionDao;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Created by kbhatk on 29/9/17.
 */
public class NetconfConnectionManagerImplTest {

    private NetconfConnectionManagerImpl m_cm;
    @Mock
    private DeviceDao m_deviceDao;
    @Mock
    private NetworkFunctionDao m_networkFunctionDao;
    private List<Device> m_devices;
    @Mock
    private NetconfClientDispatcher m_dispatcher;
    private Map<String, NetconfClientSession> m_sessions;
    private java.lang.Long m_time = System.currentTimeMillis();
    @Mock
    private CallHomeListenerComposite m_callHomeListenerComposite;
    @Mock
    private NetconfClientSession m_callhomeSession;
    @Mock
    private NetconfLoginProvider m_netconfLoginProvider;
    private X509Certificate m_deviceCert;
    private InetSocketAddress m_remoteAddress;
    private TxService m_txService;
    @Mock
    private PersistenceManagerUtil m_persistenceMgrUtil;
    @Mock
    private EntityDataStoreManager entityDSM;
    @Mock
    private EntityManager entityMgr;
    @Mock
    private EntityTransaction entityTx;
    @Mock
    private ConnectionListener m_connectionListener;

    @Before
    public void setUp() throws NetconfClientDispatcherException, CertificateException, FileNotFoundException {
        MockitoAnnotations.initMocks(this);
        m_txService = new TxService();
        when(m_persistenceMgrUtil.getEntityDataStoreManager()).thenReturn(entityDSM);
        when(entityDSM.getEntityManager()).thenReturn(entityMgr);
        when(entityMgr.getTransaction()).thenReturn(entityTx);
        when(entityTx.isActive()).thenReturn(true);
        m_devices = new ArrayList<>();
        File certFile = new File(getClass().getResource("/netconfconnectionmanagerimpltest/deviceCert.crt").getFile());
        m_deviceCert = getX509Certificates(new FileInputStream(certFile)).get(0);
        m_remoteAddress = new InetSocketAddress("0.0.0.0", 1234);
        when(m_callhomeSession.getRemoteAddress()).thenReturn(m_remoteAddress);
        when(m_callhomeSession.isOpen()).thenReturn(true);
        when(m_callhomeSession.getCreationTime()).thenReturn(m_time);
        m_devices.add(createDirectDevice("1"));
        m_devices.add(createDirectDevice("2"));
        m_devices.add(createDirectDevice("3"));
        Device callHomeDevice = createCallHomeDevice("CallHomeDevice-1");
        m_devices.add(callHomeDevice);
        when(m_deviceDao.findAllDevices()).thenReturn(m_devices);
        m_sessions = new HashMap<>();
        when(m_dispatcher.createClient(anyObject())).thenAnswer(invocation -> {
            Future future = mock(Future.class);
            NetconfClientSession session = mock(NetconfClientSession.class);
            SshNetconfTransport transport = (SshNetconfTransport) ((NetconfClientConfiguration) invocation
                    .getArguments()[0]).getTransport();
            m_sessions.put(transport.getSocketAddress().getAddress().getHostAddress(), session);
            when(session.isOpen()).thenReturn(true);
            when(session.getCreationTime()).thenReturn(m_time);
            when(future.get()).thenReturn(session);
            return future;
        });
        m_cm = new NetconfConnectionManagerImpl(m_callHomeListenerComposite, m_dispatcher);
        m_cm.registerDeviceConnectionListener(m_connectionListener);
        m_cm.setTxService(m_txService);
        m_cm.setDeviceDao(m_deviceDao);
        m_cm.setNetworkFunctionDao(m_networkFunctionDao);
    }

    @Test
    public void testManagerTriesToSetupDirectConnection() throws NetconfClientDispatcherException {
        //make all devices connected,
        m_cm.auditConnections();
        verifyAttemptMadeToConnectToAllDevices();
    }

    private void verifyAttemptMadeToConnectToAllDevices() throws NetconfClientDispatcherException {
        ArgumentCaptor<NetconfClientConfiguration> captor = ArgumentCaptor.forClass(NetconfClientConfiguration.class);
        verify(m_dispatcher, times(3)).createClient(captor.capture());

        Collection<String> ips = new HashSet<>();
        for (NetconfClientConfiguration config : captor.getAllValues()) {
            ips.add(((SshNetconfTransport) config.getTransport()).getSocketAddress().getAddress().getHostAddress());
        }
        assertEquals(getExpectedIps(), ips);
    }

    @Test
    public void testMgrHandlesExceptions() throws NetconfClientDispatcherException {
        doThrow(new RuntimeException("Something went wrong")).when(m_dispatcher).createClient(anyObject());
        //make all devices connected,
        m_cm.auditConnections();
        verifyAttemptMadeToConnectToAllDevices();
    }

    @Test
    public void testConnMgrClosesConnectionToDeletedDevices() {
        m_cm.auditConnections();
        assertEquals(3, m_cm.getAllSessions().entrySet().size());

        Device removedDevice = m_devices.remove(0);

        verify(m_sessions.get(getIp(removedDevice.getDeviceName())), never()).closeAsync();
        m_cm.auditConnections();
        verify(m_sessions.get(getIp(removedDevice.getDeviceName()))).closeAsync();
    }

    @Test
    public void testConnMgrClosesConnectionToDeletedDevices1() throws Exception {
        List<Device> devicePresent = new ArrayList<>();
        Device nokia = spy(createDirectDevice("5"));
        doThrow(new EntityNotFoundException("Entity Not Found")).when(nokia).toString();
        when(nokia.getDeviceName()).thenReturn(null);
        devicePresent.add(nokia);
        when(m_deviceDao.findAllDevices()).thenReturn(devicePresent);
        try {
            m_cm.auditConnections();
            fail();
        } catch (Exception e) {
            assertEquals("Entity Not Found", e.getMessage());
        }
    }

    @Test
    public void testManagerDoesNotSetupConnectionToAlreadyConnectedDevices() throws Exception {
        //make all devices connected,
        m_cm.auditConnections();
        verify(m_dispatcher, times(3)).createClient(anyObject());
        //trigger again
        m_cm.auditConnections();
        verifyNoMoreInteractions(m_dispatcher);
    }

    @Test
    public void verifyClosedSessionAreNotKept() throws Exception {
        //make all devices connected,
        m_cm.auditConnections();
        //make the second device connection go bad
        when(m_sessions.get(getIp("2")).isOpen()).thenReturn(false);
        m_cm.auditConnections();
        verify(m_connectionListener).deviceConnected(m_devices.get(0), m_sessions.get(getIp("1")));
        verify(m_connectionListener).deviceConnected(m_devices.get(1), m_sessions.get(getIp("2")));
        verify(m_connectionListener).deviceConnected(m_devices.get(2), m_sessions.get(getIp("3")));
        verify(m_dispatcher, times(4)).createClient(anyObject());
    }

    @Test
    public void makeSureSessionMgrDetectsClosedConnections() {
        //make all devices connected,
        m_cm.auditConnections();
        assertEquals(3, m_cm.getAllSessions().entrySet().size());
        Device device = createDirectDevice("4");
        when(m_deviceDao.findAllDevices()).thenReturn(Arrays.asList(device));

        //make them connected again
        m_cm.auditConnections();
        assertEquals(1, m_cm.getAllSessions().entrySet().size());
    }

    @Test
    public void makeSureUnmangedDeviceConnectionsAreClosed() {
        //make all devices connected,
        m_cm.auditConnections();
        assertEquals(3, m_cm.getAllSessions().entrySet().size());
        //make the second device connection go bad
        when(m_sessions.get(getIp("2")).isOpen()).thenReturn(false);
        ArgumentCaptor<NetconfClientSessionListener> captor = ArgumentCaptor.forClass(NetconfClientSessionListener
                .class);
        verify(m_sessions.get(getIp("2"))).addSessionListener(captor.capture());
        //fire session closed
        captor.getValue().sessionClosed(2, null);
        assertEquals(2, m_cm.getAllSessions().entrySet().size());
        verify(m_connectionListener).deviceDisConnected(getDevice("2"), m_sessions.get(getIp("2")));

        //make them connected again
        m_cm.auditConnections();
        assertEquals(3, m_cm.getAllSessions().entrySet().size());
        verify(m_connectionListener).deviceConnected(getDevice("2"), m_sessions.get(getIp("2")));
    }

    @Test
    public void testExecuteNetconfPrimitiveIsInvokedOnSession() throws NetconfMessageBuilderException,
            ExecutionException {
        //make all devices connected,
        m_cm.auditConnections();
        AbstractNetconfRequest request = mock(AbstractNetconfRequest.class);
        NetconfResponseFuture future = mock(NetconfResponseFuture.class);
        NetconfClientSession session1 = m_sessions.get(getIp("1"));
        when(session1.sendRpc(request)).thenReturn(future);
        Device device = getDevice("1");
        Future<NetConfResponse> responseFuture = m_cm.executeNetconf(device, request);
        verify(session1).sendRpc(request);
        assertTrue(responseFuture instanceof LoggingFuture);
    }

    private Device getDevice(String deviceName) {
        return m_deviceDao.getDeviceByName(deviceName);
    }

    @Test
    public void testExecuteNetconfTemplate() throws Exception {
        //make all devices connected,
        m_cm.auditConnections();
        AbstractNetconfRequest request = mock(AbstractNetconfRequest.class);
        NetconfResponseFuture future = mock(NetconfResponseFuture.class);
        NetconfClientSession session1 = m_sessions.get(getIp("1"));
        when(session1.sendRpc(request)).thenReturn((NetconfResponseFuture) future);
        Future<NetConfResponse> responseFuture = m_cm.executeWithSession(getDevice("1"), new
                NetconfTemplate<Future<NetConfResponse>>() {
                    @Override
                    public Future<NetConfResponse> execute(NetconfClientSession session) {
                        try {
                            return session.sendRpc(request);
                        } catch (NetconfMessageBuilderException e) {
                            fail("un expected exception" + e.getMessage());
                        }
                        return null;
                    }
                });
        verify(session1).sendRpc(request);
        assertEquals(future, responseFuture);
    }

    @Test
    public void testExecuteNetconfTemplateThrowsIllegalStateException() throws Exception {
        AbstractNetconfRequest request = mock(AbstractNetconfRequest.class);
        try {
            m_cm.executeWithSession(createDirectDevice("1"), session -> {
                try {
                    return session.sendRpc(request);
                } catch (NetconfMessageBuilderException e) {
                    fail("un expected exception" + e.getMessage());
                }
                return null;
            });
            fail("Expected an IllegalStateException here");
        } catch (IllegalStateException e) {
            assertEquals("Device not connected : 1", e.getMessage());
        }
    }

    @Test
    public void testConnectionStateGetsUpdated() {

        assertEquals(getDefaultConnectionState(), m_cm.getConnectionState(getDevice("1")));
        assertEquals(getDefaultConnectionState(), m_cm.getConnectionState(getDevice("2")));
        assertEquals(getDefaultConnectionState(), m_cm.getConnectionState(getDevice("3")));

        m_cm.auditConnections();

        assertEquals(getConnectedState(), m_cm.getConnectionState(getDevice("1")));
        assertEquals(getConnectedState(), m_cm.getConnectionState(getDevice("2")));
        assertEquals(getConnectedState(), m_cm.getConnectionState(getDevice("3")));

    }

    @Test
    public void testCMRegistersAndUnregistersCallHomeListener() {
        verify(m_callHomeListenerComposite, never()).addListener(m_cm);
        verify(m_callHomeListenerComposite, never()).removeListener(m_cm);

        m_cm.init();

        verify(m_callHomeListenerComposite).addListener(m_cm);
        verify(m_callHomeListenerComposite, never()).removeListener(m_cm);

        m_cm.destroy();

        verify(m_callHomeListenerComposite).addListener(m_cm);
        verify(m_callHomeListenerComposite).removeListener(m_cm);
    }

    @Test
    public void testCallHomeDeviceConnectionIsNotSetup() {
        assertEquals(getDefaultConnectionState(), m_cm.getConnectionState(createDirectDevice("CallHomeDevice-1")));
        m_cm.auditConnections();
        assertEquals(getDefaultConnectionState(), m_cm.getConnectionState(createDirectDevice("CallHomeDevice-1")));
    }

    @Test
    public void testCallHomeDeviceConnectionIsAvailableWhenDeviceCallsHome() {
        assertEquals(getDefaultConnectionState(), m_cm.getConnectionState(createCallHomeDevice("CallHomeDevice-1")));

        m_cm.connectionEstablished(m_callhomeSession, m_netconfLoginProvider, m_deviceCert, false);
        Device callHomeDevice1 = getDevice("CallHomeDevice-1");
        verify(m_connectionListener).deviceConnected(callHomeDevice1, m_callhomeSession);
        verify(m_connectionListener, never()).deviceDisConnected(callHomeDevice1, m_callhomeSession);
        assertEquals(getConnectedState(), m_cm.getConnectionState(callHomeDevice1));
    }

    @Test
    public void testCallHomeDeviceDisConnection() {
        assertEquals(getDefaultConnectionState(), m_cm.getConnectionState(createCallHomeDevice("CallHomeDevice-1")));

        ArgumentCaptor<NetconfClientSessionListener> captor = ArgumentCaptor.forClass(NetconfClientSessionListener.class);

        m_cm.connectionEstablished(m_callhomeSession, m_netconfLoginProvider, m_deviceCert, false);
        Device callHomeDevice1 = getDevice("CallHomeDevice-1");
        verify(m_connectionListener).deviceConnected(callHomeDevice1, m_callhomeSession);
        verify(m_connectionListener, never()).deviceDisConnected(callHomeDevice1, m_callhomeSession);
        assertEquals(getConnectedState(), m_cm.getConnectionState(callHomeDevice1));
        verify(m_callhomeSession).addSessionListener(captor.capture());

        captor.getValue().sessionClosed(m_callhomeSession.getSessionId(), null);
        assertEquals(getDefaultConnectionState(), m_cm.getConnectionState(callHomeDevice1));
        verify(m_connectionListener).deviceDisConnected(callHomeDevice1, m_callhomeSession);
    }

    @Test
    public void testCallHomeFromUnknownDeviceIsHandledCorrectly() {
        //make it as if the device was unknown
        when(m_deviceDao.findDeviceWithDuid(anyString())).thenReturn(null);

        m_cm.connectionEstablished(m_callhomeSession, m_netconfLoginProvider, m_deviceCert, false);
        Device callHomeDevice1 = createCallHomeDevice("CallHomeDevice-1");
        verify(m_connectionListener, never()).deviceConnected(callHomeDevice1, m_callhomeSession);
        verify(m_connectionListener, never()).deviceDisConnected(callHomeDevice1, m_callhomeSession);

        assertEquals(getDefaultConnectionState(), m_cm.getConnectionState(createCallHomeDevice("CallHomeDevice-1")));

        assertEquals(getDefaultConnectionState(), m_cm.getConnectionState(callHomeDevice1));
        assertEquals(1, m_cm.getNewDevices().size());
    }

    @Test
    public void testDirectConnectionForNotNetconfDevice() throws NetconfClientDispatcherException {
        List<Device> devices = new ArrayList<>();
        Device device = createDirectDevice("4");
        device.getDeviceManagement().setNetconf(false);
        devices.add(device);
        when(m_deviceDao.findAllDevices()).thenReturn(devices);
        m_cm.auditConnections();
        ArgumentCaptor<NetconfClientConfiguration> captor = ArgumentCaptor.forClass(NetconfClientConfiguration.class);
        verify(m_dispatcher, never()).createClient(captor.capture());
    }

    private ConnectionState getConnectedState() {
        ConnectionState connectionState = new ConnectionState();
        connectionState.setConnected(true);
        connectionState.setConnectionCreationTime(new Date(m_time));
        return connectionState;
    }

    private ConnectionState getDefaultConnectionState() {
        ConnectionState connectionState = new ConnectionState();
        return connectionState;
    }

    private Collection<String> getExpectedIps() {
        Collection<String> ips = new HashSet<>();
        ips.add(getIp("1"));
        ips.add(getIp("3"));
        ips.add(getIp("2"));
        return ips;
    }


    private Device createDirectDevice(String deviceName) {
        Device device = new Device();
        device.setDeviceName(deviceName);
        DeviceMgmt deviceMgmt = new DeviceMgmt();
        DeviceConnection deviceConn = new DeviceConnection();
        deviceConn.setConnectionModel("direct");
        PasswordAuth passAuth = new PasswordAuth();
        Authentication auth = new Authentication();
        auth.setAddress(getIp(deviceName));
        auth.setManagementPort("1234");
        auth.setUsername("user");
        auth.setPassword("pass");
        passAuth.setAuthentication(auth);
        deviceConn.setPasswordAuth(passAuth);
        deviceMgmt.setDeviceConnection(deviceConn);
        device.setDeviceManagement(deviceMgmt);
        return setUpDaoToAnswer(deviceName, device);
    }

    private Device createCallHomeDevice(String deviceName) {
        Device device = new Device();
        device.setDeviceName(deviceName);
        DeviceMgmt deviceMgmt = new DeviceMgmt();
        DeviceConnection devConn = new DeviceConnection();
        devConn.setConnectionModel("call-home");
        devConn.setDuid("OLT1.ONT2");
        deviceMgmt.setDeviceConnection(devConn);
        device.setDeviceManagement(deviceMgmt);
        when(m_deviceDao.findDeviceWithDuid(devConn.getDuid())).thenReturn(device);
        when(m_deviceDao.getDeviceByName(deviceName)).thenReturn(device);
        return device;
    }

    private Device setUpDaoToAnswer(String deviceName, Device device) {
        when(getDevice(deviceName)).thenReturn(device);
        return device;
    }

    private String getIp(String deviceName) {
        return "10.1.1." + deviceName;
    }

}
