/*
 * Copyright 2022 Broadband Forum
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

package org.broadband_forum.obbaa.onu;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.netconf.api.client.AbstractNetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcResponse;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.TxService;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.TxTemplate;
import org.broadband_forum.obbaa.nf.dao.NetworkFunctionDao;
import org.broadband_forum.obbaa.nf.dao.impl.KafkaTopicPurpose;
import org.broadband_forum.obbaa.nf.entities.NetworkFunction;
import org.broadband_forum.obbaa.onu.exception.MessageFormatterException;
import org.broadband_forum.obbaa.onu.exception.MessageFormatterSyncException;
import org.broadband_forum.obbaa.onu.impl.VOLTManagementImpl;
import org.broadband_forum.obbaa.onu.kafka.producer.OnuKafkaProducer;
import org.broadband_forum.obbaa.onu.message.JsonFormatter;
import org.broadband_forum.obbaa.onu.message.MessageFormatter;
import org.broadband_forum.obbaa.onu.util.VOLTManagementUtil;
import org.w3c.dom.Document;

/**
 * <p>
 * Session for Mediated devices to handle netconf requests and responses
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 22/07/2020.
 */
public class MediatedNetworkFunctionNetconfSession extends AbstractNetconfClientSession {
    private static final Logger LOGGER = Logger.getLogger(MediatedNetworkFunctionNetconfSession.class);
    private static final InetSocketAddress IP_ADDRESS = InetSocketAddress.createUnresolved("0.0.0.0", 0);
    private final ModelNodeDataStoreManager m_modelNodeDSM;
    private final AdapterManager m_adapterManager;
    private final NetworkFunction m_networkFunction;
    private final Object m_lock = new Object();
    private final long m_creationTime;
    private final MessageFormatter m_messageFormatter;
    private final TxService m_txService;
    private Map<String, TimestampFutureResponse> m_requestMap;
    private boolean m_open = true;
    private String m_networkFunctionName;
    private OnuKafkaProducer m_kafkaProducer;
    private ThreadPoolExecutor m_kafkaCommunicationPool;
    private NetworkFunctionDao m_networkFunctionDao;
    private Map<String, AtomicReference<HashSet<String>>> m_kafkatopicMap = new HashMap<>();
    private VOLTManagementImpl m_voltManagement;

    public MediatedNetworkFunctionNetconfSession(NetworkFunction nf, OnuKafkaProducer kafkaProducer,
                                                 ModelNodeDataStoreManager modelNodeDSM, AdapterManager adapterManager,
                                                 ThreadPoolExecutor kafkaCommunicationPool,
                                                 MessageFormatter messageFormatter,
                                                 TxService txService, NetworkFunctionDao networkFunctionDao,
                                                 VOLTManagementImpl voltManagement) {
        m_creationTime = System.currentTimeMillis();
        m_networkFunction = nf;
        m_networkFunctionName = nf.getNetworkFunctionName();
        m_kafkaProducer = kafkaProducer;
        m_modelNodeDSM = modelNodeDSM;
        m_adapterManager = adapterManager;
        m_requestMap = new LinkedHashMap<>();
        m_kafkaCommunicationPool = kafkaCommunicationPool;
        m_messageFormatter = messageFormatter;
        m_txService = txService;
        m_networkFunctionDao = networkFunctionDao;
        m_voltManagement = voltManagement;
    }

    @Override
    protected CompletableFuture<NetConfResponse> sendRpcMessage(String currentMessageId, Document requestDocument, long timeoutMillis) {
        String requestType = DocumentToPojoTransformer.getTypeOfNetconfRequest(requestDocument);
        AbstractNetconfRequest netconfRequest = null;
        NetConfResponse response = new NetConfResponse();
        CompletableFuture<NetConfResponse> future = null;
        try {
            switch (requestType) {
                case NetconfResources.COPY_CONFIG:
                    netconfRequest = DocumentToPojoTransformer.getCopyConfig(requestDocument);
                    future = onCopyConfig(netconfRequest);
                    break;
                case NetconfResources.EDIT_CONFIG:
                    netconfRequest = DocumentToPojoTransformer.getEditConfig(requestDocument);
                    future = onEditConfig((EditConfigRequest) netconfRequest);
                    break;
                case NetconfResources.GET:
                    netconfRequest = DocumentToPojoTransformer.getGet(requestDocument);
                    future = onGet((GetRequest) netconfRequest);
                    break;
                default:
                    return CompletableFuture.completedFuture(new NetconfRpcResponse().setMessageId(currentMessageId));
            }
        } catch (NetconfMessageBuilderException e) {
            if (requestDocument != null) {
                try {
                    LOGGER.error("Got an invalid netconf request: " + DocumentUtils.documentToString(requestDocument), e);
                } catch (NetconfMessageBuilderException ex) {
                    LOGGER.error("Got an invalid netconf request: ", ex);
                }
            }
            response.setMessageId(currentMessageId);
            response.setOk(false);
            NetconfRpcError rpcError = new NetconfRpcError(NetconfRpcErrorTag.OPERATION_FAILED, NetconfRpcErrorType.RPC,
                    NetconfRpcErrorSeverity.Error, "Got an invalid netconf request to be sent to vOMCI for device: "
                    + m_networkFunction.getNetworkFunctionName());
            response.addError(rpcError);
            future.complete(response);
        }
        return future;
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return IP_ADDRESS;
    }

    @Override
    public void close() throws InterruptedException, IOException {
        // Do other resource house keeping (i.e. closing kafka streams etc) if any
        synchronized (m_lock) {
            m_open = false;
        }
        cleanUpMapOfRequests();
        sessionClosed();
    }

    protected void cleanUpMapOfRequests() {
        synchronized (m_lock) {
            for (Map.Entry<String, TimestampFutureResponse> reqEntry : m_requestMap.entrySet()) {
                reqEntry.getValue().complete(null);
            }
            m_requestMap.clear();
        }
    }

    @Override
    public void closeAsync() {
    }

    private void sendRequest(String operation, Object message, AbstractNetconfRequest request,
                             TimestampFutureResponse future) {
        AtomicReference<HashSet<String>> kafkaTopicNameSet = new AtomicReference<>(new HashSet<String>());
        String kafkaTopicName = null;
        try {
            switch (operation) {
                case ONUConstants.ONU_GET_OPERATION:
                case ONUConstants.ONU_COPY_OPERATION:
                case ONUConstants.ONU_EDIT_OPERATION:
                default:
                    kafkaTopicNameSet = getTopicName(m_networkFunctionName);
                    m_kafkatopicMap.put(m_networkFunctionName, kafkaTopicNameSet);
            }
            synchronized (m_lock) {
                if (m_open) {
                    if (m_messageFormatter instanceof JsonFormatter) {
                        m_kafkaProducer.sendNotification(kafkaTopicName, message);
                    } else {
                        if (!kafkaTopicNameSet.get().isEmpty()) {
                            for (String topicName : kafkaTopicNameSet.get()) {
                                m_kafkaProducer.sendNotification(topicName, message);
                            }
                        } else {
                            LOGGER.error(String.format("Topic name set is NULL"));
                        }
                    }
                    if (future != null) {
                        registerRequestInMap(request, future);
                    }
                } else {
                    LOGGER.debug(String.format("Mediated Device Netconf Session is closed\n"
                            + "Do not send %s request onto Kafka topic and do not register it in the Map", operation));
                    internalNokResponse(request, future, "Mediated Device Netconf Session is closed");
                }
            }
        } catch (Exception e) {
            LOGGER.warn(String.format("Failed to send %s request notification onto Kafka topic ", operation), e);
            internalNokResponse(request, future, e.toString());
        }
    }

    private AtomicReference<HashSet<String>> getTopicName(String networkFunctionName) {
        AtomicReference<HashSet<String>> kafkaTopicNames = new AtomicReference<>(new HashSet<String>());
        if (networkFunctionName != null) {
            m_txService.executeWithTxRequired((TxTemplate<Void>) () -> {
                final HashSet<String> kafkaTopicNameFinal = m_networkFunctionDao.getKafkaTopicNames(networkFunctionName,
                        KafkaTopicPurpose.VOMCI_REQUEST);
                if (kafkaTopicNameFinal != null && !kafkaTopicNameFinal.isEmpty()) {
                    kafkaTopicNames.set(kafkaTopicNameFinal);
                } else {
                    LOGGER.error(String.format("Kafka topic names for the Network Function: %s is Null or Empty", networkFunctionName));
                }
                return null;
            });
        } else {
            LOGGER.error("Network Function Name is NULL");
        }
        return kafkaTopicNames;
    }

    private void registerRequestInMap(AbstractNetconfRequest request, TimestampFutureResponse future) {
        synchronized (m_lock) {
            m_requestMap.put(request.getMessageId(), future);
        }
    }

    private void internalOkResponse(AbstractNetconfRequest request, TimestampFutureResponse future) {
        NetConfResponse response = new NetConfResponse().setMessageId(request.getMessageId());
        response.setOk(true);
        future.complete(response);
    }

    private void internalNokResponse(AbstractNetconfRequest request, TimestampFutureResponse future, String errorMessage) {
        NetConfResponse response = new NetConfResponse().setMessageId(request.getMessageId());
        NetconfRpcError netconfRpcError = NetconfRpcError.getApplicationError("Internal error detected: " + errorMessage);
        response = response.addError(netconfRpcError);
        if (!(request instanceof GetRequest)) {
            response.setOk(false);
        }
        future.complete(response);
    }

    @Override
    public boolean isOpen() {
        synchronized (m_lock) {
            return m_open;
        }
    }

    @Override
    public long getCreationTime() {
        return m_creationTime;
    }

    @Override
    public void setTcpKeepAlive(boolean keepAlive) {
        // TO DO
    }

    public void open() {
        synchronized (m_lock) {
            m_open = true;
        }
    }

    protected Map<String, TimestampFutureResponse> getMapOfRequests() {
        synchronized (m_lock) {
            return m_requestMap;
        }
    }

    protected void setMapOfRequests(Map<String, TimestampFutureResponse> newRequestMap) {
        synchronized (m_lock) {
            m_requestMap = newRequestMap;
        }
    }

    protected int getNumberOfPendingRequests() {
        synchronized (m_lock) {
            return m_requestMap.size();
        }
    }

    public CompletableFuture<NetConfResponse> onCopyConfig(AbstractNetconfRequest request) {
        LOGGER.info(String.format("Sending copy config request for NF %s: %s", m_networkFunction.getNetworkFunctionName(),
                request.requestToString()));
        synchronized (m_lock) {
            if (!m_open) {
                LOGGER.debug("Mediated Device Netconf Session is closed. "
                        + "Unable to process copy-config request for NF " + m_networkFunction);
                return CompletableFuture.completedFuture(null);
            }
        }
        TimestampFutureResponse future = new TimestampFutureResponse();
        m_kafkaCommunicationPool.execute(() -> {
            try {
                setMessageId(request);
                m_voltManagement.setAndIncrementVoltmfInternalMessageId(request);
                Object formattedMessage = m_messageFormatter.getFormattedRequestForNF(request,
                        NetconfResources.COPY_CONFIG,m_networkFunction,m_modelNodeDSM,m_adapterManager);
                sendRequest(NetconfResources.COPY_CONFIG, formattedMessage, request, future);
                VOLTManagementUtil.registerInRequestMap(request, "", NetconfResources.COPY_CONFIG);
                return;
            } catch (NetconfMessageBuilderException | MessageFormatterException e) {
                LOGGER.error("Error while processing copy-config request for NF " + m_networkFunctionName, e);
            }
            internalNokResponse(request, future, "Error processing copy-config");
        });
        return future;
    }

    public CompletableFuture<NetConfResponse> onGet(GetRequest request) {
        LOGGER.info(String.format("Sending get request for NF %s: %s", m_networkFunctionName, request.requestToString()));
        synchronized (m_lock) {
            if (!m_open) {
                LOGGER.debug("Mediated Device Netconf Session is closed. Unable to process GET request");
                return CompletableFuture.completedFuture(null);
            }
        }
        LOGGER.error("GET not implemented for NF");
        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<NetConfResponse> onEditConfig(EditConfigRequest request) {
        LOGGER.info(String.format("Sending edit config request for NF %s: >%s<", m_networkFunctionName,
                request.requestToString()));
        synchronized (m_lock) {
            if (!m_open) {
                LOGGER.warn("Mediated Device Netconf Session is closed\n"
                        + "Unable to process edit-config request for " + m_networkFunctionName);
                return CompletableFuture.completedFuture(null);
            }
        }
        TimestampFutureResponse future = new TimestampFutureResponse();
        m_kafkaCommunicationPool.execute(() -> {
            setMessageId(request);
            if (request.getMessageId() == null || request.getMessageId().isEmpty()) {
                LOGGER.warn("Unable to set edit-config request messageId\n"
                        + "Terminate processing edit-config request for " + m_networkFunctionName);
                internalNokResponse(request, future, "Unable to set edit-config request messageId");
            }
            try {
                m_voltManagement.setAndIncrementVoltmfInternalMessageId(request);
                Object formattedMessage = m_messageFormatter.getFormattedRequestForNF(request,
                        NetconfResources.EDIT_CONFIG,m_networkFunction,m_modelNodeDSM,m_adapterManager);
                sendRequest(NetconfResources.EDIT_CONFIG, formattedMessage, request, future);
                VOLTManagementUtil.registerInRequestMap(request, "", NetconfResources.EDIT_CONFIG);
            } catch (MessageFormatterSyncException e) {
                internalOkResponse(request, future);
            } catch (MessageFormatterException e) {
                internalNokResponse(request, future, e.getMessage());
            } catch (Exception e) {
                LOGGER.error("Error while processing edit-config request for device" + m_networkFunctionName, e);
                internalNokResponse(request, future, e.toString());
            }
        });
        return future;
    }

    public NetConfResponse processGetResponse(String identifier, String responseStatus, String jsonResponse, String failureReason) {
        LOGGER.error("GET not implemented for NF");
        return null;
    }

    public NetConfResponse processResponse(String identifier, String operationType, String responseStatus, String failureReason) {
        synchronized (m_lock) {
            if (!m_requestMap.containsKey(identifier)) {
                LOGGER.debug(String.format("%s request with identifier %s has expired\n"
                        + "Terminate processing response", operationType, identifier));
                return null;
            }
        }
        //TODO check if ok
        NetConfResponse response = new NetConfResponse().setMessageId(identifier);
        if (responseStatus.equals(ONUConstants.OK_RESPONSE)) {
            LOGGER.debug(String.format("Start processing %s response with identifier: %s and status: %s",
                    operationType, identifier, responseStatus));
            response.setOk(true);
        } else {
            LOGGER.debug(String.format("Adding the Application Error to the Netconf response for %s "
                            + "request upon receiving NOK Response Status received from VONUMgmt is due to the following Failure: {}",
                    operationType, failureReason));
            NetconfRpcError netconfRpcError = NetconfRpcError.getApplicationError(operationType
                    + " Request Error upon receiving NOK Response Status received from VONUMgmt is due to the following Failure: "
                    + failureReason);
            response = response.addError(netconfRpcError);
            response.setOk(false);
            if (operationType.equals(ONUConstants.ONU_COPY_OPERATION) || operationType.equals(ONUConstants.ONU_EDIT_OPERATION)) {
                LOGGER.debug(String.format("Resetting configBackupId due to NOK Response Status for %s response",
                        operationType));
            }
        }
        removeRequestFromMap(identifier, response);
        LOGGER.debug(String.format("Processed %s response based on vOMCI response as follows:\n %s",
                operationType, response.responseToString()));
        return response;
    }

    private void removeRequestFromMap(String identifier, NetConfResponse response) {
        synchronized (m_lock) {
            m_requestMap.get(identifier).complete(response);
            m_requestMap.remove(identifier);
        }
    }

}