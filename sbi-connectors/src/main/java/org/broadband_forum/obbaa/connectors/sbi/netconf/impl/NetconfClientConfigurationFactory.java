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

import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.CALL_HOME_IANA_PORT_TLS;

import java.util.Set;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.netconf.api.NetconfConfigurationBuilderException;
import org.broadband_forum.obbaa.netconf.api.authentication.AuthenticationListener;
import org.broadband_forum.obbaa.netconf.api.client.CallHomeListener;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientConfiguration;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportFactory;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportOrder;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportProtocol;

import io.netty.channel.EventLoopGroup;
import io.netty.handler.ssl.SslProvider;

public final class NetconfClientConfigurationFactory {
    private NetconfClientConfigurationFactory(){}

    static final String CALLHOME_TLS_IP = "NC_NBI_IP";
    static final String CALLHOME_TLS_PORT = "CALLHOME_TLS_PORT";
    static final String CALLHOME_TLS_HANDSHAKE_TIMEOUT_MS = "CALLHOME_TLS_HANDSHAKE_TIMEOUT_MS";

    private static final Logger LOGGER = Logger.getLogger(NetconfClientConfigurationFactory.class);

    public static NetconfClientConfiguration createCallHomeTLSClientConfiguration(Long connectionTimeoutMillis,
                                                                                  Set<String> capabilities,
                                                                                  EventLoopGroup nioEventLoopGroup,
                                                                                  TrustManager trustManager,
                                                                                  KeyManager keyManager,
                                                                                  AuthenticationListener
                                                                                          authenticationListener,
                                                                                  CallHomeListener callHomeListener)
            throws NetconfConfigurationBuilderException {
        NetconfTransportOrder reverseTLSTransportOrder = new NetconfTransportOrder();
        reverseTLSTransportOrder.setTransportType(NetconfTransportProtocol.REVERSE_TLS.name());
        SystemPropertyUtils propertyUtils = SystemPropertyUtils.getInstance();
        reverseTLSTransportOrder.setCallHomeIp(propertyUtils.getFromEnvOrSysProperty(CALLHOME_TLS_IP, "0.0.0.0"));
        reverseTLSTransportOrder.setCallHomePort(Integer.parseInt(propertyUtils.getFromEnvOrSysProperty(
                CALLHOME_TLS_PORT, String.valueOf(CALL_HOME_IANA_PORT_TLS))));
        reverseTLSTransportOrder.setCallHomeListener(callHomeListener);
        reverseTLSTransportOrder.setAllowSelfSigned(false);
        reverseTLSTransportOrder.setTlsKeepalive(true);
        reverseTLSTransportOrder.setTlsHandshaketimeoutMillis(Long.parseLong(propertyUtils.getFromEnvOrSysProperty(
                CALLHOME_TLS_HANDSHAKE_TIMEOUT_MS, String.valueOf(reverseTLSTransportOrder
                        .getTlsHandshaketimeoutMillis()))));
        setSSLProvider(reverseTLSTransportOrder, trustManager, keyManager);
        return new NetconfClientConfiguration(connectionTimeoutMillis, null, capabilities,
                NetconfTransportFactory.makeNetconfTransport(reverseTLSTransportOrder), nioEventLoopGroup, null,
                authenticationListener);
    }

    private static void setSSLProvider(NetconfTransportOrder tlsTransportOrder, TrustManager trustManager, KeyManager
            keyManager) {
        tlsTransportOrder.setSslProvider(SslProvider.JDK);
        tlsTransportOrder.setKeyManager(keyManager);
        tlsTransportOrder.setTrustManager(trustManager);

    }
}
