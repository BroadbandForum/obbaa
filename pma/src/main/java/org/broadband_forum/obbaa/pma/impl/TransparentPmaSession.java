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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.pma.PmaSession;
import org.broadband_forum.obbaa.store.dm.DeviceInfo;
import org.w3c.dom.Document;

/**
 * Created by kbhatk on 10/30/17.
 */
public class TransparentPmaSession implements PmaSession {
    private final NetconfConnectionManager m_connectionManager;
    private final DeviceInfo m_deviceInfo;

    public TransparentPmaSession(DeviceInfo deviceInfo, NetconfConnectionManager connectionManager) {
        m_deviceInfo = deviceInfo;
        m_connectionManager = connectionManager;
    }

    @Override
    public String executeNC(String netconfRequest) {
        try {
            Document document = null;
            document = DocumentUtils.stringToDocument(netconfRequest);
            AbstractNetconfRequest request = DocumentToPojoTransformer.getRequest(document);
            Future<NetConfResponse> responseFuture = m_connectionManager.executeNetconf(m_deviceInfo, request);
            return responseFuture.get().responseToString();
        } catch (NetconfMessageBuilderException e) {
            throw new IllegalArgumentException(String.format("Invalid netconf request received : %s", netconfRequest), e);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(String.format("Could not execute request %s on device %s", netconfRequest,
                    m_deviceInfo), e);
        }
    }

    @Override
    public void forceAlign() {
        throw new UnsupportedOperationException("Full resync is not supported in transparent mode");
    }

    @Override
    public void align() {
        throw new UnsupportedOperationException("Full resync is not supported in transparent mode");
    }
}
