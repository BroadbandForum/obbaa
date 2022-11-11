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

package org.broadband_forum.obbaa.ipfix.ncclient.app;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.broadband_forum.obbaa.ipfix.ncclient.api.NcClientService;
import org.broadband_forum.obbaa.ipfix.ncclient.api.NetConfClientException;
import org.broadband_forum.obbaa.netconf.api.ClosureReason;
import org.broadband_forum.obbaa.netconf.api.NetconfConfigurationBuilderException;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientConfiguration;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientDispatcher;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientDispatcherException;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSessionListener;
import org.broadband_forum.obbaa.netconf.api.client.NetconfLoginProvider;
import org.broadband_forum.obbaa.netconf.api.client.authentication.Login;
import org.broadband_forum.obbaa.netconf.api.client.util.NetconfClientConfigurationBuilder;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportFactory;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportOrder;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportProtocol;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.client.dispatcher.NetconfClientDispatcherImpl;
import org.broadband_forum.obbaa.netconf.client.ssh.auth.PasswordLoginProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

public class NcClientServiceImpl implements NcClientService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NcClientServiceImpl.class);

    public static final String PMA_HOST_STRING = "BAA_HOST";
    private static final String PMA_DEFAULT_HOST = "localhost";
    private static final String PMA_HOST = System.getenv(PMA_HOST_STRING);
    private static final String PMA_SSH_PORT = "BAA_SSH_PORT";
    private static final int PMA_DEFAULT_SSH_PORT = 9292;
    private static final String PMA_SSH_PORT_STRING = System.getenv(PMA_SSH_PORT);
    private static final String PMA_DEFAULT_USERNAME = "adminuser";
    private static final String PMA_USERNAME = "BAA_USERNAME";
    private static final String PMA_USERNAME_STRING = System.getenv(PMA_USERNAME);
    private static final String PMA_DEFAULT_PASSWORD = "pass";
    private static final String PMA_PASSWORD = "BAA_PASSWORD";
    private static final String PMA_PASSWORD_STRING = System.getenv(PMA_PASSWORD);

    public static final String HOSTKEY_SER = "hostkey.ser";
    private static Set<String> CLIENTCAPS = new HashSet<>(Arrays.asList(NetconfResources.NETCONF_BASE_CAP_1_0,
            NetconfResources.NETCONF_BASE_CAP_1_1));

    private NetconfClientDispatcher m_dispatcher = new NetconfClientDispatcherImpl(Executors.newCachedThreadPool());

    NetconfLoginProvider m_authorizationProvider = null;

    private NetconfTransportOrder m_clientTransportOder;

    private NetconfClientConfiguration m_clientConfig;

    private Future<NetconfClientSession> m_futureSession;

    private NetconfClientSession m_session;

    private boolean m_isSessionAvailable;

    public NcClientServiceImpl() throws NetConfClientException {
        String username = getPmaUserName();
        String password = getPmaPassword();
        String hostname = getPmaHost();
        int port = getPmaSshPort();
        m_authorizationProvider = new PasswordLoginProvider(new Login(username, password));
        try {
            setClientTransportOrder(hostname, port);
            m_clientConfig = getNetconfClientConfiguration();
            createNetconfSession(hostname, port);
        } catch (ExecutionException | NetconfConfigurationBuilderException | UnknownHostException
                | NetconfClientDispatcherException | InterruptedException e) {
            NetConfClientException ne = new NetConfClientException(e.getMessage(), e);
            LOGGER.error(String.format("Error Occurred while trying to connect to BAA host: %s port: %s username: %s "
                    + "password: %s", hostname, port, username, password), e);
            throw ne;
        }
    }

    public NcClientServiceImpl(NetconfClientDispatcher dispatcher) throws NetConfClientException {
        m_dispatcher = dispatcher;
        String username = getPmaUserName();
        String password = getPmaPassword();
        String hostname = getPmaHost();
        int port = getPmaSshPort();
        m_authorizationProvider = new PasswordLoginProvider(new Login(username, password));
        try {
            setClientTransportOrder(hostname, port);
            m_clientConfig = getNetconfClientConfiguration();
            createNetconfSession(hostname, port);
        } catch (ExecutionException | NetconfConfigurationBuilderException | UnknownHostException
                | NetconfClientDispatcherException | InterruptedException e) {
            NetConfClientException ne = new NetConfClientException(e.getMessage(), e);
            LOGGER.error(String.format("Error Occurred while trying to connect to BAA host: %s port: %s username: %s "
                    + "password: %s", hostname, port, username, password), e);
            throw ne;
        }
    }

    private void createNetconfSession(String hostname, int port) throws ExecutionException,
            NetconfClientDispatcherException, InterruptedException {
        m_futureSession = m_dispatcher.createClient(m_clientConfig);
        m_session = m_futureSession.get();
        m_isSessionAvailable = true;
        m_session.addSessionListener(new NetconfClientSessionListener() {
            @Override
            public void sessionClosed(int sessionId, ClosureReason closureReason) {
                LOGGER.info("Got Session close for " + m_session.getSessionId() + " for host : " + hostname + " port : " + port);
                m_isSessionAvailable = false;
            }
        });
        LOGGER.info("Got Session " + m_session.getSessionId() + " for host : " + hostname + " port : " + port);
    }

    private String getPmaUserName() {
        return StringUtils.isEmpty(PMA_USERNAME_STRING) ? PMA_DEFAULT_USERNAME : PMA_USERNAME_STRING;
    }

    private String getPmaPassword() {
        return StringUtils.isEmpty(PMA_PASSWORD_STRING) ? PMA_DEFAULT_PASSWORD : PMA_PASSWORD_STRING;
    }

    private String getPmaHost() {
        return StringUtils.isEmpty(PMA_HOST) ? PMA_DEFAULT_HOST : PMA_HOST;
    }

    private static int getPmaSshPort() {
        if (StringUtils.isNotEmpty(PMA_SSH_PORT_STRING)) {
            try {
                return Integer.parseInt(PMA_SSH_PORT_STRING);
            } catch (Exception e) {
                return PMA_DEFAULT_SSH_PORT;
            }
        }
        return PMA_DEFAULT_SSH_PORT;
    }

    @VisibleForTesting
    protected NetconfClientConfiguration getNetconfClientConfiguration() throws NetconfConfigurationBuilderException {
        return new NetconfClientConfigurationBuilder().setNetconfLoginProvider(m_authorizationProvider)
        .setTransport(NetconfTransportFactory.makeNetconfTransport(m_clientTransportOder)).setConnectionTimeout(Long.MAX_VALUE)
        .setCapabilities(CLIENTCAPS).build();
    }

    @VisibleForTesting
    protected void setClientTransportOrder(String hostname, int port) throws UnknownHostException {
        m_clientTransportOder = new NetconfTransportOrder();
        m_clientTransportOder.setServerSocketAddress(new InetSocketAddress(InetAddress.getByName(hostname), port));
        m_clientTransportOder.setServerSshHostKeyPath(HOSTKEY_SER);
        m_clientTransportOder.setTransportType(NetconfTransportProtocol.SSH.name().toUpperCase());
    }

    private NetConfResponse getResponse(CompletableFuture<NetConfResponse> futureResponse)
            throws InterruptedException, ExecutionException, NetconfMessageBuilderException {
        if (futureResponse != null) {
            NetConfResponse response = futureResponse.get();
            if (response != null) {
                return response;
            } else {
                throw new ExecutionException(new RuntimeException("NetConf Response is NULL"));
            }
        } else {
            throw new NetconfMessageBuilderException("Failed to dispatch request");
        }
    }

    @Override
    public NetConfResponse performNcRequest(AbstractNetconfRequest request)
            throws NetConfClientException, NetconfMessageBuilderException, InterruptedException, ExecutionException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Sending netconf request:  " + request.requestToString());
        }
        if (!isSessionAvailable()) {
            String hostname = getPmaHost();
            int port = getPmaSshPort();
            try {
                createNetconfSession(hostname, port);
            } catch (ExecutionException | NetconfClientDispatcherException | InterruptedException e) {
                NetConfClientException ne = new NetConfClientException(e.getMessage(), e);
                LOGGER.error("Error Occurred while creating netconf session: ", e);
                throw ne;
            }
        }
        CompletableFuture<NetConfResponse> futureResponse = m_session.sendRpc(request);
        NetConfResponse response = getResponse(futureResponse);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("netconf response: " + response.responseToString());
        }
        return response;
    }

    @Override
    public void closeSession() {
        try {
            m_session.close();
        } catch (InterruptedException | IOException e) {
            LOGGER.error("Error Occurred while closing session: ", e);
        }
    }

    @Override
    public boolean isSessionAvailable() {
        boolean sessionAvailability = m_session != null && m_session.isOpen() && m_isSessionAvailable;
        LOGGER.debug("Session availablilty for session:" + m_session + " is " + sessionAvailability);
        return sessionAvailability;
    }
}
