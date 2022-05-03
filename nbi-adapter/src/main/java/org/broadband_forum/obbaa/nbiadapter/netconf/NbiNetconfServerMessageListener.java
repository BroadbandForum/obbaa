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

import static org.broadband_forum.obbaa.nbiadapter.netconf.NbiConstants.APPLICATION;
import static org.broadband_forum.obbaa.nbiadapter.netconf.NbiConstants.DEVICE;
import static org.broadband_forum.obbaa.nbiadapter.netconf.NbiConstants.ERROR;
import static org.broadband_forum.obbaa.nbiadapter.netconf.NbiConstants.ERROR_MESSAGE;
import static org.broadband_forum.obbaa.nbiadapter.netconf.NbiConstants.ERROR_SEVERITY;
import static org.broadband_forum.obbaa.nbiadapter.netconf.NbiConstants.ERROR_TAG;
import static org.broadband_forum.obbaa.nbiadapter.netconf.NbiConstants.ERROR_TYPE;
import static org.broadband_forum.obbaa.nbiadapter.netconf.NbiConstants.NAME;
import static org.broadband_forum.obbaa.nbiadapter.netconf.NbiConstants.NAMESPACE;
import static org.broadband_forum.obbaa.nbiadapter.netconf.NbiConstants.OPERATION_FAILED;
import static org.broadband_forum.obbaa.nbiadapter.netconf.NbiConstants.ROOT;
import static org.broadband_forum.obbaa.nbiadapter.netconf.NbiConstants.RPC_ERROR;
import static org.broadband_forum.obbaa.nbiadapter.netconf.NbiConstants.TIMEOUT_MESSAGE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.aggregator.api.Aggregator;
import org.broadband_forum.obbaa.aggregator.api.DispatchException;
import org.broadband_forum.obbaa.dmyang.entities.Device;
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
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetconfServer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.broadband_forum.obbaa.nm.devicemanager.DeviceManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * OB-BAA NBI SSH Netconf server message listener class
 * Responsible for receiving various Netconf messages, calling Aggregator interface to  process, and returning response messages.
 */
public class NbiNetconfServerMessageListener implements NetconfServerMessageListener {

    private static final Logger LOGGER = Logger.getLogger(NbiNetconfServerMessageListener.class);

    private NetconfServer m_coreNetconfServer;

    private Aggregator m_aggregator;
    private static final int MAX_ATTEMPTS_TO_GET_RESPONSE_FROM_MAP = 20;
    private static HashMap<String, String> responseMap = new HashMap<>();
    private static HashMap<String, String> externalInternalMessageIdMap = new HashMap<>();
    private static boolean isNetconfSessionIsAlive = false;
    private static List<String> receivedMessageIdList = new ArrayList<>();
    private final DeviceManager m_deviceManager;

    public NbiNetconfServerMessageListener(NetconfServer coreNetconfServer, Aggregator aggregator, DeviceManager deviceManager) {
        m_coreNetconfServer = coreNetconfServer;
        m_aggregator = aggregator;
        m_deviceManager = deviceManager;
    }


    public String executeNC(NetconfClientInfo clientInfo, String netconfRequest) throws DispatchException {
        return m_aggregator.dispatchRequest(clientInfo, netconfRequest);
    }

    /**
     * Convert request to standard IF and call aggregator to process, and return the response.
     *
     */
    public void executeRequest(NetconfClientInfo clientInfo, AbstractNetconfRequest request, NetConfResponse response) {

        try {
            // Exec Netconf  string message and get response string message
            String responseString = executeNC(clientInfo, request.requestToString());

            // Convert response string message to NetconfResponse object
            Document document = DocumentUtils.stringToDocument(responseString);
            NetConfResponse netconfResponse = DocumentToPojoTransformer.getNetconfResponse(document);

            // Copy reponse object
            response.setMessageId(request.getMessageId());
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

    private void executeGetRequest(NetconfClientInfo clientInfo, AbstractNetconfRequest request, NetConfResponse response) {
        try {
            // Exec onu external get request
            String responseFromMap = null;
            int attemptsMadeToFetchResponseFromMap = 0;
            setReceivedMessageId(request.getMessageId());
            executeNC(clientInfo, request.requestToString());
            String internalMessageId = getInternalMessageIdFromExternalMessageId(request.getMessageId());
            LOGGER.info("Waiting for the response from vOLTMF");
            String responseString = null;
            while (responseString == null) {
                LOGGER.debug("Fetching response from VOLT-MF, Attempts made: " + attemptsMadeToFetchResponseFromMap);
                responseFromMap = getResponseMap(internalMessageId);
                if (responseFromMap != null) {
                    isNetconfSessionIsAlive = false;
                    responseString = responseFromMap;
                    removeResponseFromMap(internalMessageId);
                } else if (attemptsMadeToFetchResponseFromMap >= MAX_ATTEMPTS_TO_GET_RESPONSE_FROM_MAP) {
                    isNetconfSessionIsAlive = false;
                    LOGGER.info("Timed out while getting response from VOLT-MF");
                    responseString = getErrorResponse(request.getMessageId());
                }
                attemptsMadeToFetchResponseFromMap++;
                Thread.sleep(1000);
            }
            // Convert response string message to NetconfResponse object
            Document document = DocumentUtils.stringToDocument(responseString);
            NetConfResponse netconfResponse = DocumentToPojoTransformer.getNetconfResponse(document);

            // Copy reponse object
            response.setMessageId(request.getMessageId());
            response.addErrors(netconfResponse.getErrors());
            response.setOk(netconfResponse.isOk());
            if ((response instanceof NetconfRpcResponse) && (netconfResponse instanceof NetconfRpcResponse)) {
                for (Element element : ((NetconfRpcResponse) netconfResponse).getRpcOutputElements()) {
                    ((NetconfRpcResponse) response).addRpcOutputElement(element);
                }
            } else {
                response.setData(netconfResponse.getData());
            }
        } catch (NetconfMessageBuilderException | DispatchException | InterruptedException e) {
            LOGGER.info("Exec netconf request failed : %s", e);
            response.addError(NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.OPERATION_FAILED,
                    "Error while executing netconf message - " + e.getMessage()));
        }
    }

    private String getDeviceNameFromGetRequest(AbstractNetconfRequest request) throws NetconfMessageBuilderException {
        String deviceName = null;
        if (request != null) {
            Document requestDocument = request.getRequestDocument();
            if (requestDocument != null) {
                NodeList deviceNodeList = requestDocument.getElementsByTagName(DEVICE);
                if (deviceNodeList != null) {
                    Node deviceNode = deviceNodeList.item(0);
                    if (deviceNode != null) {
                        Element deviceElement = (Element) deviceNode;
                        NodeList deviceNameNodeList = deviceElement.getElementsByTagName(NAME);
                        if (deviceNameNodeList != null) {
                            Node deviceNameNode = deviceNameNodeList.item(0);
                            if (deviceNameNode != null) {
                                NodeList deviceChildNodeList = deviceNameNode.getChildNodes();
                                if (deviceChildNodeList != null) {
                                    Node deviceChildNode = deviceChildNodeList.item(0);
                                    if (deviceChildNode != null) {
                                        deviceName = deviceChildNode.getNodeValue();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return deviceName;
    }

    private boolean isExternalgetRequest(GetRequest request) throws NetconfMessageBuilderException {
        boolean isRootTagPresent = false;
        if (request != null) {
            Document requestDocument = request.getRequestDocument();
            if (requestDocument != null) {
                NodeList deviceNodeList = requestDocument.getElementsByTagName(DEVICE);
                if (deviceNodeList != null) {
                    Node deviceNode = deviceNodeList.item(0);
                    if (deviceNode != null) {
                        Element deviceElement = (Element) deviceNode;
                        NodeList deviceNameNodeList = deviceElement.getElementsByTagName(ROOT);
                        if (deviceNameNodeList != null) {
                            Node deviceNameNode = deviceNameNodeList.item(0);
                            if (deviceNameNode != null) {
                                isRootTagPresent = true;
                            }
                        }
                    }
                }
            }
        }
        return isRootTagPresent;
    }

    private String getdeviceType(AbstractNetconfRequest request) {
        String deviceName = null;
        String deviceType = null;
        try {
            deviceName = getDeviceNameFromGetRequest(request);
        } catch (NetconfMessageBuilderException e) {
            LOGGER.info("Error while fetching device-name from Get request");
        }
        if (deviceName != null) {
            Device device = m_deviceManager.getDevice(deviceName);
            deviceType = device.getDeviceManagement().getDeviceType();
        }
        return deviceType;
    }

    public void setOnuCurrentMessageId(String currentMessageId) {
        if (isNetconfSessionIsAlive && currentMessageId != null && receivedMessageIdList.get(0) != null) {
            externalInternalMessageIdMap.put(receivedMessageIdList.get(0), currentMessageId);
            receivedMessageIdList.remove(0);
        }
    }

    private void setReceivedMessageId(String messageId) {
        if (messageId != null) {
            receivedMessageIdList.add(messageId);
        }
    }

    private String getErrorResponse(String messageId) {
        Document document = DocumentUtils.createDocument();
        Element rpcError = document.createElementNS(NAMESPACE, RPC_ERROR);
        Element errorType = document.createElementNS(NAMESPACE, ERROR_TYPE);
        rpcError.appendChild(errorType);
        errorType.setTextContent(APPLICATION);

        Element errorTag = document.createElementNS(NAMESPACE, ERROR_TAG);
        rpcError.appendChild(errorTag);
        errorTag.setTextContent(OPERATION_FAILED);

        Element errorSeverity = document.createElementNS(NAMESPACE, ERROR_SEVERITY);
        rpcError.appendChild(errorSeverity);
        errorSeverity.setTextContent(ERROR);

        Element errorMessage = document.createElementNS(NAMESPACE, ERROR_MESSAGE);
        rpcError.appendChild(errorMessage);
        errorMessage.setTextContent(TIMEOUT_MESSAGE);

        document.appendChild(rpcError);

        NetConfResponse netConfResponse = new NetConfResponse();
        netConfResponse.setMessageId(messageId);
        netConfResponse.setData(document.getDocumentElement());

        return netConfResponse.responseToString();
    }

    private String getInternalMessageIdFromExternalMessageId(String externalMessageId) {
        String internalMessageId = null;
        if (externalMessageId != null) {
            internalMessageId = externalInternalMessageIdMap.get(externalMessageId);
            externalInternalMessageIdMap.remove(externalMessageId);
        }
        return internalMessageId;
    }

    @Override
    public void onHello(NetconfClientInfo info, Set<String> clientCaps) {
        LOGGER.info(String.format("received hello rpc from %s, with capabilities %s", info, clientCaps));
        LOGGER.info(clientCaps);

        m_coreNetconfServer.onHello(info, clientCaps);
    }

    @Override
    public void onGet(NetconfClientInfo info, GetRequest req, NetConfResponse resp) {
        logRpc(info, req);
        boolean isexternalGetRequest = false;
        String deviceType = null;
        try {
            isexternalGetRequest = isExternalgetRequest(req);
        } catch (NetconfMessageBuilderException e) {
            LOGGER.info("error while parsing get request");
        }
        deviceType = getdeviceType(req);
        if (isexternalGetRequest && deviceType != null && deviceType.equals("ONU")) {
            isNetconfSessionIsAlive = true;
            executeGetRequest(info, req, resp);
        } else {
            executeRequest(info, req, resp);
        }
        logRpcResp(info, resp);
    }

    public void addResponseIntoMap(String messageId, String response) {
        if (isNetconfSessionIsAlive) {
            responseMap.put(messageId, response);
        }
    }

    public String getResponseMap(String messageId) {
        return responseMap.get(messageId);
    }

    public void removeResponseFromMap(String messageId) {
        responseMap.remove(messageId);
    }

    @Override
    public void onGetConfig(NetconfClientInfo info, GetConfigRequest req, NetConfResponse resp) {
        logRpc(info, req);

        executeRequest(info, req, resp);

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

        executeRequest(info, req, resp);

        logRpcResp(info, resp);

        return null;
    }

    @Override
    public void onCopyConfig(NetconfClientInfo info, CopyConfigRequest req, NetConfResponse resp) {
        logRpc(info, req);

        executeRequest(info, req, resp);

        logRpcResp(info, resp);
    }

    @Override
    public void onDeleteConfig(NetconfClientInfo info, DeleteConfigRequest req, NetConfResponse resp) {
        logRpc(info, req);

        executeRequest(info, req, resp);

        logRpcResp(info, resp);
    }

    @Override
    public void onLock(NetconfClientInfo info, LockRequest req, NetConfResponse resp) {
        logRpc(info, req);

        m_coreNetconfServer.onLock(info, req, resp);

        logRpcResp(info, resp);
    }

    @Override
    public void onUnlock(NetconfClientInfo info, UnLockRequest req, NetConfResponse resp) {
        logRpc(info, req);

        m_coreNetconfServer.onUnlock(info, req, resp);

        logRpcResp(info, resp);
    }

    @Override
    public void onCloseSession(NetconfClientInfo info, CloseSessionRequest req, NetConfResponse resp) {
        logRpc(info, req);

        m_coreNetconfServer.onCloseSession(info, req, resp);

        logRpcResp(info, resp);
    }

    @Override
    public void onKillSession(NetconfClientInfo info, KillSessionRequest req, NetConfResponse resp) {
        logRpc(info, req);

        m_coreNetconfServer.onKillSession(info, req, resp);

        logRpcResp(info, resp);
    }

    @Override
    public void onInvalidRequest(NetconfClientInfo info, Document req, NetConfResponse resp) {
        m_coreNetconfServer.onInvalidRequest(info, req, resp);

        logRpcResp(info, resp);
    }

    @Override
    public void sessionClosed(String arg0, int sessionId) {

        m_coreNetconfServer.sessionClosed(arg0, sessionId);
    }

    @Override
    public List<Notification> onRpc(NetconfClientInfo info, NetconfRpcRequest rpcRequest, NetconfRpcResponse response) {
        logRpc(info, rpcRequest);

        executeRequest(info, rpcRequest, response);

        logRpcResp(info, response);

        return null;
    }

    @Override
    public void onCreateSubscription(NetconfClientInfo info, NetconfRpcRequest req, ResponseChannel responseChannel) {
        logRpc(info, req);

        m_coreNetconfServer.onCreateSubscription(info, req, responseChannel);
    }

    @Override
    public void onAction(NetconfClientInfo info, ActionRequest req, ActionResponse resp) {
        logRpc(info, req);

        executeRequest(info, req, resp);

        logRpcResp(info, resp);
    }


}
