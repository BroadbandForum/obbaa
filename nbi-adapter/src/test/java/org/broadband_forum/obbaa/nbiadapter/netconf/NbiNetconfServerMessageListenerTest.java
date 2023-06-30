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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.broadband_forum.obbaa.aggregator.api.Aggregator;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.DeviceMgmt;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.ActionResponse;
import org.broadband_forum.obbaa.netconf.api.messages.CloseSessionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.CopyConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigElement;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.KillSessionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.LockRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcResponse;
import org.broadband_forum.obbaa.netconf.api.messages.UnLockRequest;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl;
import org.broadband_forum.obbaa.nm.devicemanager.DeviceManager;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

public class NbiNetconfServerMessageListenerTest {

    private NetConfServerImpl netconfServer;
    private Aggregator aggregator;
    private NbiNetconfServerMessageListener listener;
    private NetconfClientInfo client;
    private NetConfResponse resp;
    private DeviceManager m_deviceManager;
    private Device m_device;
    private DeviceMgmt m_deviceMgmt;

    @Before
    public void setUp() throws Exception {
        netconfServer = mock(NetConfServerImpl.class);
        aggregator = mock(Aggregator.class);
        m_deviceManager = mock(DeviceManager.class);
        m_device = mock(Device.class);
        m_deviceMgmt = mock(DeviceMgmt.class);
        listener = new NbiNetconfServerMessageListener(netconfServer, aggregator, m_deviceManager);
        client = new NetconfClientInfo("test", 1);
        resp = new NetConfResponse();

        when(aggregator.dispatchRequest(eq(client), anyObject())).thenReturn("<ok/>");
    }

    @Test
    public void executeRequest() {
        GetRequest req = new GetRequest();

        listener.executeRequest(client, req, resp);

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
        Document reqDoc = DocumentUtils.stringToDocument("<network-manager xmlns='urn:bbf:yang:obbaa:network-manager'/>");
        ActionRequest req = new ActionRequest();
        ActionResponse resp = new ActionResponse();
        req.setActionTreeElement(reqDoc.getDocumentElement());

        listener.onAction(client, req, resp);

        assertTrue(resp.isOk());
    }

    @Test
    public void onActionWithOutputElements() throws Exception {
        final String respData =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\"  message-id=\"1\" >" +
                  "<element1 xmlns=\"dummy\" />" +
                  "<element2 xmlns=\"dummy\" />" +
                "</rpc-reply>";

        Document reqDoc = DocumentUtils.stringToDocument("<network-manager xmlns='urn:bbf:yang:obbaa:network-manager' message-id=\"1\"/>");
        Document expectedResp = DocumentUtils.stringToDocument(respData);

        ActionRequest req = new ActionRequest();
        ActionResponse resp = new ActionResponse();
        req.setActionTreeElement(reqDoc.getDocumentElement());

        when(aggregator.dispatchRequest(anyObject(), anyObject())).thenReturn(respData);

        listener.onAction(client, req, resp);

        assertTrue(expectedResp.toString().equals(resp.getResponseDocument().toString()));
    }

    @Test
    public void onExternalGetNotOkayResponse() throws NetconfMessageBuilderException {
        String getRpcInput = "<filter type=\"subtree\">\n" +
                "      <network-manager xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
                "        <managed-devices>\n" +
                "          <device>\n" +
                "            <name>ont1</name>\n" +
                "            <root/>\n" +
                "          </device>\n" +
                "        </managed-devices>\n" +
                "      </network-manager>\n" +
                "    </filter>";
        NetconfFilter requestFilter = new NetconfFilter();
        GetRequest getRequest = new GetRequest();
        ActionResponse resp = new ActionResponse();
        getRequest.setFilter(requestFilter);
        requestFilter.addXmlFilter(DocumentUtils.stringToDocument(getRpcInput).getDocumentElement());
        when(m_deviceManager.getDevice(anyString())).thenReturn(m_device);
        when(m_device.getDeviceManagement()).thenReturn(m_deviceMgmt);
        when(m_deviceMgmt.getDeviceType()).thenReturn("ONU");
        listener.onGet(client, getRequest, resp);
        assertFalse(resp.isOk());
    }

    @Test
    public void onExternalGetOkayResponse() throws NetconfMessageBuilderException {
        String getRpcInput = "<filter type=\"subtree\">\n" +
                "      <network-manager xmlns=\"urn:bbf:yang:obbaa:network-manager\">\n" +
                "        <managed-devices>\n" +
                "          <device>\n" +
                "            <name>ont1</name>\n" +
                "            <root/>\n" +
                "          </device>\n" +
                "        </managed-devices>\n" +
                "      </network-manager>\n" +
                "    </filter>";
        String okResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"110\">\n"
                + "  <data>\n"
                + "  </data>\n"
                + "</rpc-reply>";
        final String[] mapResponseValue = {null};
        HashMap<String, String> responseMap = new HashMap<>();
        responseMap.put("110", okResponse);
        NetconfFilter requestFilter = new NetconfFilter();
        GetRequest getRequest = new GetRequest();
        ActionResponse resp = new ActionResponse();
        getRequest.setMessageId("110");
        getRequest.setFilter(requestFilter);
        requestFilter.addXmlFilter(DocumentUtils.stringToDocument(getRpcInput).getDocumentElement());
        when(m_deviceManager.getDevice(anyString())).thenReturn(m_device);
        when(m_device.getDeviceManagement()).thenReturn(m_deviceMgmt);
        when(m_deviceMgmt.getDeviceType()).thenReturn("ONU");
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    Thread.sleep(2000);
                    listener.addResponseIntoMap(null, okResponse);
                    mapResponseValue[0] = listener.getResponseMap(null);
                    return;
                } catch (InterruptedException e) {

                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
        listener.onGet(client, getRequest, resp);
        assertNotNull(mapResponseValue);
        assertEquals(mapResponseValue[0], okResponse);
        assertNull(listener.getResponseMap(null));
    }

}