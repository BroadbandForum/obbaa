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

import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.connectors.sbi.netconf.CallHomeListenerComposite;
import org.broadband_forum.obbaa.netconf.api.client.CallHomeListener;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.client.NetconfLoginProvider;

public class CallHomeListenerCompositeImpl implements CallHomeListenerComposite {

    private static final Logger LOGGER = Logger.getLogger(CallHomeListenerCompositeImpl.class);
    private Set<CallHomeListener> m_listeners = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Override
    public void addListener(CallHomeListener callHomeListener) {
        m_listeners.add(callHomeListener);
    }

    @Override
    public void removeListener(CallHomeListener callHomeListener) {
        m_listeners.remove(callHomeListener);
    }

    @Override
    public void connectionEstablished(NetconfClientSession clientSession, NetconfLoginProvider netconfLoginProvider,
                                      X509Certificate peerX509Certificate, boolean isSelfSigned) {
        for (CallHomeListener listener : m_listeners) {
            try {
                listener.connectionEstablished(clientSession, netconfLoginProvider, peerX509Certificate, isSelfSigned);
            } catch (Exception e) {
                LOGGER.error(String.format("Error while calling listener {}", listener), e);
            }
        }
    }
}
