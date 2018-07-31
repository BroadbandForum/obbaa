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

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import org.broadband_forum.obbaa.aggregator.api.Aggregator;
import org.broadband_forum.obbaa.aggregator.api.DispatchException;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.ActionResponse;
import org.broadband_forum.obbaa.netconf.api.messages.CloseSessionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.CopyConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.DeleteConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.KillSessionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.LockRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcResponse;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.messages.UnLockRequest;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerMessageListener;
import org.broadband_forum.obbaa.netconf.api.server.ResponseChannel;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * OB-BAA NBI SSH Netconf server message listener class
 * Responsible for receiving various Netconf messages, calling Aggregator interface to  process, and returning response messages.
 */
public class NbiNetconfServerMessageListener implements NetconfServerMessageListener {

    private static final Logger LOGGER = Logger.getLogger(NbiNetconfServerMessageListener.class);

    private NetConfServerImpl m_coreNetconfServerImpl;

    private Aggregator m_aggregator;

    public NbiNetconfServerMessageListener(NetConfServerImpl coreNetconfServerImpl, Aggregator aggregator) {
        m_coreNetconfServerImpl = coreNetconfServerImpl;
        m_aggregator = aggregator;
    }


    public String executeNC(String netconfRequest) throws DispatchException {
        return m_aggregator.dispatchRequest(netconfRequest);
    }

    /**
     * Convert request to standard IF and call aggregator to process, and return the response.
     *
     */
    public void executeRequest(AbstractNetconfRequest request, NetConfResponse response) {

        try {
            // Exec Netconf  string message and get response string message
            String responseString = executeNC(request.requestToString());

            // Convert response string message to NetconfResponse object
            Document document = DocumentUtils.stringToDocument(responseString);
            NetConfResponse netconfResponse = DocumentToPojoTransformer.getNetconfResponse(document);

            // Copy reponse object
            response.setMessageId(netconfResponse.getMessageId());
            response.addErrors(netconfResponse.getErrors());
            response.setOk(netconfResponse.isOk());
            if ((response instanceof NetconfRpcResponse) && (netconfResponse instanceof NetconfRpcResponse)) {
                for (Element element : ((NetconfRpcResponse)netconfResponse).getRpcOutputElements()) {
                    ((NetconfRpcResponse)response).addRpcOutputElement(element);
                }
            }
            else {
                response.setData(netconfResponse.getData());
            }

        } catch (NetconfMessageBuilderException | DispatchException e) {
            LOGGER.info("Exec netconf request failed : %s", e);
            response.addError(NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.OPERATION_FAILED,
                    "Error while executing netconf message - " + e.getMessage()));
        }

    }


    @Override
    public void onHello(NetconfClientInfo info, Set<String> clientCaps) {
        LOGGER.info(String.format("received hello rpc from %s, with capabilities %s", info, clientCaps));
        LOGGER.info(clientCaps);

        m_coreNetconfServerImpl.onHello(info, clientCaps);
    }

    @Override
    public void onGet(NetconfClientInfo info, GetRequest req, NetConfResponse resp) {
        logRpc(info, req);

        executeRequest(req, resp);

        logRpcResp(info, resp);
    }


    @Override
    public void onGetConfig(NetconfClientInfo info, GetConfigRequest req, NetConfResponse resp) {
        logRpc(info, req);

        executeRequest(req, resp);

        logRpcResp(info, resp);
    }

    private void logRpc(NetconfClientInfo info, AbstractNetconfRequest req) {
        LOGGER.info(String.format("received netconf rpc %s from %s", req.requestToString(), info));
    }

    private void logRpcResp(NetconfClientInfo info, NetConfResponse resp) {
        LOGGER.info(String.format("send netconf rpc resp %s from %s", resp.responseToString(), info));
    }

    @Override
    public List<Notification> onEditConfig(NetconfClientInfo info, EditConfigRequest req, NetConfResponse resp) {
        logRpc(info, req);

        executeRequest(req, resp);

        logRpcResp(info, resp);

        return null;
    }

    @Override
    public void onCopyConfig(NetconfClientInfo info, CopyConfigRequest req, NetConfResponse resp) {
        logRpc(info, req);

        executeRequest(req, resp);

        logRpcResp(info, resp);
    }

    @Override
    public void onDeleteConfig(NetconfClientInfo info, DeleteConfigRequest req, NetConfResponse resp) {
        logRpc(info, req);

        executeRequest(req, resp);

        logRpcResp(info, resp);
    }

    @Override
    public void onLock(NetconfClientInfo info, LockRequest req, NetConfResponse resp) {
        logRpc(info, req);

        m_coreNetconfServerImpl.onLock(info, req, resp);

        logRpcResp(info, resp);
    }

    @Override
    public void onUnlock(NetconfClientInfo info, UnLockRequest req, NetConfResponse resp) {
        logRpc(info, req);

        m_coreNetconfServerImpl.onUnlock(info, req, resp);

        logRpcResp(info, resp);
    }

    @Override
    public void onCloseSession(NetconfClientInfo info, CloseSessionRequest req, NetConfResponse resp) {
        logRpc(info, req);

        m_coreNetconfServerImpl.onCloseSession(info, req, resp);

        logRpcResp(info, resp);
    }

    @Override
    public void onKillSession(NetconfClientInfo info, KillSessionRequest req, NetConfResponse resp) {
        logRpc(info, req);

        m_coreNetconfServerImpl.onKillSession(info, req, resp);

        logRpcResp(info, resp);
    }

    @Override
    public void onInvalidRequest(NetconfClientInfo info, Document req, NetConfResponse resp) {
        m_coreNetconfServerImpl.onInvalidRequest(info, req, resp);

        logRpcResp(info, resp);
    }

    @Override
    public void sessionClosed(String arg0, int sessionId) {

        m_coreNetconfServerImpl.sessionClosed(arg0, sessionId);
    }

    @Override
    public List<Notification> onRpc(NetconfClientInfo info, NetconfRpcRequest rpcRequest, NetconfRpcResponse response) {
        logRpc(info, rpcRequest);

        executeRequest(rpcRequest, response);

        logRpcResp(info, response);

        return null;
    }

    @Override
    public void onCreateSubscription(NetconfClientInfo info, NetconfRpcRequest req, ResponseChannel responseChannel) {
        m_coreNetconfServerImpl.onCreateSubscription(info, req, responseChannel);
    }

    @Override
    public void onAction(NetconfClientInfo info, ActionRequest req, ActionResponse resp) {
        logRpc(info, req);

        executeRequest(req, resp);

        logRpcResp(info, resp);
    }


}
