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

package org.broadband_forum.obbaa.nbiadapter.netconf;

import org.broadband_forum.obbaa.aggregator.api.Aggregator;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.*;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;


import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NbiNetconfServerMessageListenerTest {

    private NetConfServerImpl netconfServer;
    private Aggregator aggregator;
    private NbiNetconfServerMessageListener listener;
    private NetconfClientInfo client;
    private NetConfResponse  resp;

    @Before
    public void setUp() throws Exception {
        netconfServer = mock(NetConfServerImpl.class);
        aggregator = mock(Aggregator.class);
        listener = new NbiNetconfServerMessageListener(netconfServer, aggregator);
        client = new NetconfClientInfo("test", 1);
        resp = new NetConfResponse();

        when(aggregator.dispatchRequest(anyObject())).thenReturn("<ok/>");
    }

    @Test
    public void executeRequest() {
        GetRequest req = new GetRequest();

        listener.executeRequest(req, resp);

        assertTrue(resp.isOk());
    }

    @Test
    public void onHello() {
        Set<String> caps = new HashSet<>();
        caps.add(NetconfResources.NETCONF_BASE_CAP_1_0);
        caps.add(NetconfResources.NETCONF_BASE_CAP_1_1);

        listener.onHello(client, caps);
    }

    @Test
    public void onGet() {
        GetRequest req = new GetRequest();

        listener.onGet(client, req, resp);

        assertTrue(resp.isOk());
    }

    @Test
    public void onGetConfig() {
        GetConfigRequest req = new GetConfigRequest();

        listener.onGetConfig(client, req, resp);

        assertTrue(resp.isOk());
    }

    @Test
    public void onEditConfig() {
        EditConfigRequest req = new EditConfigRequest();
        req.setConfigElement(new EditConfigElement());

        listener.onEditConfig(client, req, resp);

        assertTrue(resp.isOk());
    }

    @Test
    public void onCopyConfig() {
        CopyConfigRequest req = new CopyConfigRequest();
        req.setSource("running", false);
        req.setTarget("running", false);

        listener.onCopyConfig(client, req, resp);

        assertTrue(resp.isOk());
    }

    @Test
    public void onDeleteConfig() {
    }

    @Test
    public void onLock() {
        LockRequest req = new LockRequest();
        req.setTarget("running");

        listener.onLock(client, req, resp);
    }

    @Test
    public void onUnlock() {
        UnLockRequest req = new UnLockRequest();
        req.setTarget("running");

        listener.onUnlock(client, req, resp);
    }

    @Test
    public void onCloseSession() {
        CloseSessionRequest req = new CloseSessionRequest();

        listener.onCloseSession(client, req, resp);
    }

    @Test
    public void onKillSession() {
        KillSessionRequest req = new KillSessionRequest();

        listener.onKillSession(client, req, resp);
    }

    @Test
    public void onInvalidRequest() throws Exception{
        Document req = DocumentUtils.stringToDocument("<rpc/>");

        listener.onInvalidRequest(client, req, resp);
    }

    @Test
    public void sessionClosed() {
        listener.sessionClosed("test", 2);
    }

    @Test
    public void onRpc() throws Exception {
        Document reqDoc = DocumentUtils.stringToDocument("<rpc/>");
        NetconfRpcRequest req = new NetconfRpcRequest();
        NetconfRpcResponse resp = new NetconfRpcResponse();
        req.setRpcInput(reqDoc.getDocumentElement());

        listener.onRpc(client, req, resp);

        assertTrue(resp.isOk());
    }

    @Test
    public void onAction() throws Exception {
        Document reqDoc = DocumentUtils.stringToDocument("<rpc/>");
        ActionRequest req = new ActionRequest();
        ActionResponse resp = new ActionResponse();
        req.setActionTreeElement(reqDoc.getDocumentElement());

        listener.onAction(client, req, resp);

        assertTrue(resp.isOk());
    }
}