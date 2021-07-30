/*
 * Copyright 2020 Broadband Forum
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.device.adapter.AdapterUtils;
import org.broadband_forum.obbaa.dmyang.dao.DeviceDao;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.api.client.AbstractNetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcResponse;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.TxService;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.TxTemplate;
import org.broadband_forum.obbaa.nf.dao.NetworkFunctionDao;
import org.broadband_forum.obbaa.nf.dao.impl.KafkaTopicPurpose;
import org.broadband_forum.obbaa.onu.exception.MessageFormatterException;
import org.broadband_forum.obbaa.onu.exception.MessageFormatterSyncException;
import org.broadband_forum.obbaa.onu.kafka.producer.OnuKafkaProducer;
import org.broadband_forum.obbaa.onu.message.GpbFormatter;
import org.broadband_forum.obbaa.onu.message.JsonFormatter;
import org.broadband_forum.obbaa.onu.message.MessageFormatter;
import org.broadband_forum.obbaa.onu.message.NetworkWideTag;
import org.broadband_forum.obbaa.onu.message.ObjectType;
import org.broadband_forum.obbaa.onu.util.DeviceJsonUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <p>
 * Session for Mediated devices to handle netconf requests and responses
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 22/07/2020.
 */
public class MediatedDeviceNetconfSession extends AbstractNetconfClientSession {
    private static final Logger LOGGER = Logger.getLogger(MediatedDeviceNetconfSession.class);
    private static final InetSocketAddress IP_ADDRESS = InetSocketAddress.createUnresolved("0.0.0.0", 0);
    private final String m_oltDeviceName;
    private final String m_onuId;
    private final String m_channelTermRef;
    private final ModelNodeDataStoreManager m_modelNodeDSM;
    private final AdapterManager m_adapterManager;
    private final Device m_onuDevice;
    private final Object m_lock = new Object();
    private final long m_creationTime;
    private final MessageFormatter m_messageFormatter;
    private final TxService m_txService;
    private Map<String, TimestampFutureResponse> m_requestMap;
    private boolean m_open = true;
    private String m_onuDeviceName;
    private HashMap<String, String> m_labels;
    private OnuKafkaProducer m_kafkaProducer;
    private ThreadPoolExecutor m_kafkaCommunicationPool;
    private SchemaRegistry m_schemaRegistry;
    private NetworkFunctionDao m_networkFunctionDao;
    private DeviceDao m_deviceDao;
    private Map<String, AtomicReference<HashSet<String>>> m_kafkatopicMap = new HashMap<>();

    public MediatedDeviceNetconfSession(Device device, String oltDeviceName, String onuId, String channelTermRef,
                                        HashMap<String, String> labels, OnuKafkaProducer kafkaProducer,
                                        ModelNodeDataStoreManager modelNodeDSM, AdapterManager adapterManager,
                                        ThreadPoolExecutor kafkaCommunicationPool, SchemaRegistry schemaRegistry,
                                        MessageFormatter messageFormatter, TxService txService,
                                        NetworkFunctionDao networkFunctionDao, DeviceDao deviceDao) {
        m_creationTime = System.currentTimeMillis();
        m_onuDevice = device;
        m_onuDeviceName = device.getDeviceName();
        m_kafkaProducer = kafkaProducer;
        m_labels = labels;
        m_oltDeviceName = oltDeviceName;
        m_onuId = onuId;
        m_channelTermRef = channelTermRef;
        m_modelNodeDSM = modelNodeDSM;
        m_adapterManager = adapterManager;
        m_requestMap = new LinkedHashMap<>();
        m_kafkaCommunicationPool = kafkaCommunicationPool;
        m_schemaRegistry = schemaRegistry;
        m_messageFormatter = messageFormatter;
        m_txService = txService;
        m_networkFunctionDao = networkFunctionDao;
        m_deviceDao = deviceDao;
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
                    + m_onuDeviceName);
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
        if (m_messageFormatter instanceof JsonFormatter) {
            String notificationEvent = ONUConstants.UNDETECT_EVENT;
            NotificationRequest request = new NotificationRequest(m_onuDeviceName, m_oltDeviceName, m_channelTermRef,
                    m_onuId, notificationEvent, m_labels);
            setMessageId(request);
            NetworkWideTag networkWideTag = new NetworkWideTag(m_onuDeviceName, m_oltDeviceName, m_onuId, m_channelTermRef, m_labels);
            try {
                Object message = m_messageFormatter.getFormattedRequest(request, notificationEvent, m_onuDevice,
                        null, null, null, networkWideTag);
                if (m_open) {
                    sendRequest(notificationEvent, message, request, null);
                }
                close();
            } catch (InterruptedException e) {
                LOGGER.error("Error while closing session for " + m_onuDeviceName, e);
            } catch (IOException e) {
                LOGGER.error("Error while closing session for " + m_onuDeviceName, e);
            } catch (NetconfMessageBuilderException | MessageFormatterException e) {
                LOGGER.error("Error while closing session for " + m_onuDeviceName, e);
            }
        }
    }

    private void sendRequest(String operation, Object message, AbstractNetconfRequest request,
                             TimestampFutureResponse future) {
        AtomicReference<HashSet<String>> kafkaTopicNameSet = new AtomicReference<>(new HashSet<String>());
        String kafkaTopicName = null;
        try {
            switch (operation) {
                case ONUConstants.ONU_DETECTED:
                case ONUConstants.ONU_UNDETECTED:
                    if (m_messageFormatter instanceof JsonFormatter) {
                        kafkaTopicName = ONUConstants.ONU_NOTIFICATION_KAFKA_TOPIC;
                    }
                    break;
                case ONUConstants.CREATE_ONU:
                case ONUConstants.DELETE_ONU:
                    if (m_messageFormatter instanceof GpbFormatter) {
                        if (m_kafkatopicMap.containsKey(m_onuDeviceName)) {
                            kafkaTopicNameSet = m_kafkatopicMap.get(m_onuDeviceName);
                            m_kafkatopicMap.remove(m_onuDeviceName);
                        }
                    }
                    break;
                case ONUConstants.ONU_GET_OPERATION:
                case ONUConstants.ONU_COPY_OPERATION:
                case ONUConstants.ONU_EDIT_OPERATION:
                    if (m_messageFormatter instanceof JsonFormatter) {
                        kafkaTopicName = ONUConstants.ONU_REQUEST_KAFKA_TOPIC;
                    } else {
                        String networkFunctionName = getNwFunctionName(m_onuDeviceName);
                        kafkaTopicNameSet = getTopicName(networkFunctionName);
                        m_kafkatopicMap.put(m_onuDeviceName, kafkaTopicNameSet);
                    }
                    break;
                default:
                    if (m_messageFormatter instanceof JsonFormatter) {
                        kafkaTopicName = ONUConstants.ONU_REQUEST_KAFKA_TOPIC;
                    } else {
                        String networkFunctionName = getNwFunctionName(m_onuDeviceName);
                        kafkaTopicNameSet = getTopicName(networkFunctionName);
                        m_kafkatopicMap.put(m_onuDeviceName, kafkaTopicNameSet);
                    }
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
        LOGGER.info(String.format("Sending copy config request for device %s: %s", m_onuDeviceName, request.requestToString()));
        synchronized (m_lock) {
            if (!m_open) {
                LOGGER.debug("Mediated Device Netconf Session is closed. Unable to process copy-config request for " + m_onuDeviceName);
                return CompletableFuture.completedFuture(null);
            }
        }
        TimestampFutureResponse future = new TimestampFutureResponse();
        m_kafkaCommunicationPool.execute(() -> {
            try {
                setMessageId(request);
                NetworkWideTag networkWideTag ;
                if (m_messageFormatter instanceof JsonFormatter) {
                    networkWideTag = new NetworkWideTag(m_onuDeviceName, m_oltDeviceName, m_onuId, m_channelTermRef, m_labels);
                } else {
                    String vomciFunctionName = getNwFunctionName(m_onuDeviceName);
                    networkWideTag = new NetworkWideTag(m_onuDeviceName, vomciFunctionName, m_onuDeviceName, ObjectType.ONU);
                }
                Object formattedMessage = m_messageFormatter.getFormattedRequest(request, ONUConstants.ONU_COPY_OPERATION, m_onuDevice,
                        m_adapterManager, m_modelNodeDSM, m_schemaRegistry, networkWideTag);
                sendRequest(ONUConstants.ONU_COPY_OPERATION, formattedMessage, request, future);
                return;
            } catch (NetconfMessageBuilderException | MessageFormatterException e) {
                LOGGER.error("Error while processing copy-config request for device " + m_onuDeviceName, e);
            }
            internalNokResponse(request, future, "Error processing copy-config");
        });
        return future;
    }

    public CompletableFuture<NetConfResponse> onGet(GetRequest request) {
        LOGGER.info(String.format("Sending get request for device %s: %s", m_onuDeviceName, request.requestToString()));
        synchronized (m_lock) {
            if (!m_open) {
                LOGGER.debug("Mediated Device Netconf Session is closed. Unable to process GET request");
                return CompletableFuture.completedFuture(null);
            }
        }
        TimestampFutureResponse future = new TimestampFutureResponse();
        m_kafkaCommunicationPool.execute(() -> {
            try {
                if (request.getMessageId() == null || request.getMessageId().isEmpty()) {
                    LOGGER.debug("Unable to set GET request messageId. Terminate processing GET request");
                    internalNokResponse(request, future, "Unable to set GET request messageId");
                    return;
                }
                NetconfFilter filter = request.getFilter();
                if (filter != null) {
                    NetworkWideTag networkWideTag;
                    if (m_messageFormatter instanceof JsonFormatter) {
                        networkWideTag = new NetworkWideTag(m_onuDeviceName, m_oltDeviceName, m_onuId, m_channelTermRef, m_labels);
                    } else {
                        String vomciFunctionName = getNwFunctionName(m_onuDeviceName);
                        networkWideTag = new NetworkWideTag(m_onuDeviceName, vomciFunctionName, m_onuDeviceName, ObjectType.ONU);
                    }
                    Object formattedMessage = m_messageFormatter.getFormattedRequest(request, ONUConstants.ONU_GET_OPERATION, m_onuDevice,
                            m_adapterManager, m_modelNodeDSM, m_schemaRegistry, networkWideTag);
                    sendRequest(ONUConstants.ONU_GET_OPERATION, formattedMessage, request, future);
                } else {
                    LOGGER.error("No filter for GET request");
                    internalNokResponse(request, future, "Unable to get filters of the GET request");
                }
            } catch (Exception e) {
                LOGGER.error("Error while processing onGet request for device " + m_onuDeviceName, e);
            }
        });
        return future;
    }

    private CompletableFuture<NetConfResponse> onEditConfig(EditConfigRequest request) {
        LOGGER.info(String.format("Sending edit config request for device %s: >%s<", m_onuDeviceName,
                request.requestToString()));
        synchronized (m_lock) {
            if (!m_open) {
                LOGGER.warn("Mediated Device Netconf Session is closed\n"
                        + "Unable to process edit-config request for " + m_onuDeviceName);
                return CompletableFuture.completedFuture(null);
            }
        }
        TimestampFutureResponse future = new TimestampFutureResponse();
        m_kafkaCommunicationPool.execute(() -> {
            setMessageId(request);
            if (request.getMessageId() == null || request.getMessageId().isEmpty()) {
                LOGGER.warn("Unable to set edit-config request messageId\n"
                        + "Terminate processing edit-config request for " + m_onuDeviceName);
                internalNokResponse(request, future, "Unable to set edit-config request messageId");
            }
            try {
                NetworkWideTag networkWideTag;
                if (m_messageFormatter instanceof JsonFormatter) {
                    networkWideTag = new NetworkWideTag(m_onuDeviceName, m_oltDeviceName, m_onuId, m_channelTermRef, m_labels);
                } else {
                    String vomciFunctionName = getNwFunctionName(m_onuDeviceName);
                    networkWideTag = new NetworkWideTag(m_onuDeviceName, vomciFunctionName, m_onuDeviceName, ObjectType.ONU);
                }
                Object formattedMessage = m_messageFormatter.getFormattedRequest(request, ONUConstants.ONU_EDIT_OPERATION, m_onuDevice,
                        m_adapterManager, m_modelNodeDSM, m_schemaRegistry, networkWideTag);
                sendRequest(ONUConstants.ONU_EDIT_OPERATION, formattedMessage, request, future);
            } catch (MessageFormatterSyncException e) {
                internalOkResponse(request, future);
            } catch (MessageFormatterException e) {
                internalNokResponse(request, future, e.getMessage());
            } catch (Exception e) {
                LOGGER.error("Error while processing edit-config request for device" + m_onuDeviceName, e);
                internalNokResponse(request, future, e.toString());
            }
        });
        return future;
    }

    public NetConfResponse processGetResponse(String identifier, String responseStatus, String jsonResponse, String failureReason) {
        synchronized (m_lock) {
            if (!m_requestMap.containsKey(identifier)) {
                LOGGER.warn(String.format("The GET Request with identifier %s has been expired\n"
                        + "Terminate processing GET response", identifier));
                return null;
            }
        }
        NetConfResponse response = new NetConfResponse().setMessageId(identifier);
        if (responseStatus.equals(ONUConstants.OK_RESPONSE)) {
            LOGGER.debug(String.format("Start processing GET response with identifier: %s and status: %s",
                    identifier, responseStatus));
            if (jsonResponse.isEmpty()) {
                LOGGER.warn(String.format("Unable to process GET response with identifier: %s since data"
                                + " in the JSON Response object is empty\nCompleting the future with empty Netconf Response",
                        identifier));
                removeRequestFromMap(identifier, response);
                return response;
            }
            try {
                JSONObject jsonObject = new JSONObject(jsonResponse);
                JSONObject deviceSpecificJsonObj = jsonObject.getJSONObject(ONUConstants.NETWORK_MANAGER + ONUConstants.COLON
                        + ONUConstants.ROOT);
                List<Element> elementsList = new ArrayList<>();
                for (Object key : deviceSpecificJsonObj.keySet()) {
                    String childKey = (String) key;
                    Object childValue = deviceSpecificJsonObj.get(childKey);
                    SchemaRegistry deviceSchemaRegistry = AdapterUtils.getAdapterContext(m_onuDevice, m_adapterManager).getSchemaRegistry();
                    try {
                        DeviceJsonUtils deviceJsonUtils = new DeviceJsonUtils(deviceSchemaRegistry, m_modelNodeDSM);
                        Element element = deviceJsonUtils.convertFromJsonToXmlSBI("{\"" + childKey + "\""
                                + ONUConstants.COLON + childValue + "}");
                        elementsList.add(element);
                    } catch (DOMException e) {
                        LOGGER.error("Error while converting child of JSON object into the XML element ", e);
                        String moduleName = childKey.split(ONUConstants.COLON)[0];
                        String namespace = deviceSchemaRegistry.getNamespaceOfModule(moduleName);
                        String prefix = deviceSchemaRegistry.getPrefix(namespace);
                        String localName = childKey.split(ONUConstants.COLON)[1];
                        String elementName = prefix + ONUConstants.COLON + childKey.split(ONUConstants.COLON)[1];
                        NetconfRpcErrorType netconfRpcErrorType = NetconfRpcErrorType.getType("rpc");
                        NetconfRpcError netconfRpcError = NetconfRpcError.getUnknownNamespaceError(namespace,
                                "One or more of the following: key=" + childKey + ", moduleName=" + moduleName
                                        + ", namespace=" + namespace + ", prefix=" + prefix + ", localName=" + localName
                                        + ", elementName=" + elementName, netconfRpcErrorType);
                        response = response.addError(netconfRpcError);
                        removeRequestFromMap(identifier, response);
                        return response;
                    } catch (RuntimeException e) {
                        LOGGER.error("Error while converting child of JSON object into the XML element ", e);
                        NetconfRpcErrorType netconfRpcErrorType = NetconfRpcErrorType.getType("rpc");
                        NetconfRpcError netconfRpcError = NetconfRpcError.getBadElementError(childKey, netconfRpcErrorType);
                        response = response.addError(netconfRpcError);
                        removeRequestFromMap(identifier, response);
                        return response;
                    } catch (Exception e) {
                        LOGGER.error("Error while converting child of JSON object into the XML element ", e);
                        NetconfRpcError netconfRpcError = NetconfRpcError.getApplicationError(e.getMessage());
                        response = response.addError(netconfRpcError);
                        removeRequestFromMap(identifier, response);
                        return response;
                    }
                }
                response = response.setDataContent(elementsList);
            } catch (JSONException e) {
                LOGGER.error(String.format("Error while generating JSONObject as part of building NetconfResponse"
                                + " based on the received GET JSON Response with identifier %s and object data: %s\n",
                        identifier, jsonResponse, e));
                NetconfRpcErrorType netconfRpcErrorType = NetconfRpcErrorType.getType("rpc");
                NetconfRpcError netconfRpcError = NetconfRpcError.getBadAttributeError(ONUConstants.DATA_JSON_KEY,
                        netconfRpcErrorType, e.getMessage());
                response = response.addError(netconfRpcError);
                removeRequestFromMap(identifier, response);
                return response;
            } catch (Exception e) {
                LOGGER.error("Error while processing GET response with identifier:" + identifier, e);
                NetconfRpcError netconfRpcError = NetconfRpcError.getApplicationError(e.getMessage());
                response = response.addError(netconfRpcError);
                removeRequestFromMap(identifier, response);
                return response;
            }
        } else {
            LOGGER.debug("Adding the Application Error to the Netconf response upon receiving  "
                    + "NOK Response Status from VONUMgmt is due to the following Failure: " + failureReason);
            NetconfRpcError netconfRpcError = NetconfRpcError.getApplicationError(
                    "GET Request Error upon receiving NOK Response Status from VONUMgmt is due to the following Failure: "
                            + failureReason);
            response = response.addError(netconfRpcError);
        }
        removeRequestFromMap(identifier, response);
        LOGGER.debug("Processed GET response based on VONU Mgmt response as follows: " + response.responseToString());
        return response;
    }

    public NetConfResponse processResponse(String identifier, String operationType, String responseStatus, String failureReason) {
        synchronized (m_lock) {
            if (!m_requestMap.containsKey(identifier)) {
                LOGGER.debug(String.format("%s request with identifier %s has expired\n"
                        + "Terminate processing response", operationType, identifier));
                return null;
            }
        }
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

    protected String getNwFunctionName(String onuDeviceName) {
        AtomicReference<String> networkFunctionName = new AtomicReference<String>();
        m_txService.executeWithTxRequired((TxTemplate<Void>) () -> {
            String nwFunctionName = m_deviceDao.getVomciFunctionName(onuDeviceName);
            if (nwFunctionName != null) {
                networkFunctionName.set(nwFunctionName);
            } else {
                LOGGER.error(String.format("No Network Function found for the given device %s:", onuDeviceName));
            }
            return null;
        });
        return networkFunctionName.get();
    }
}