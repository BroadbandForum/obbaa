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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.device.adapter.AdapterUtils;
import org.broadband_forum.obbaa.device.adapter.DeviceConfigBackup;
import org.broadband_forum.obbaa.device.adapter.VomciAdapterDeviceInterface;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.api.client.AbstractNetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.CopyConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigElement;
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
import org.broadband_forum.obbaa.onu.kafka.OnuKafkaProducer;
import org.broadband_forum.obbaa.onu.util.DeviceJsonUtils;
import org.broadband_forum.obbaa.onu.util.JsonUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
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
    private Map<String, TimestampFutureResponse> m_requestMap;
    private boolean m_open = true;
    private final Object m_lock = new Object();
    private String m_onuDeviceName;
    private HashMap<String, String> m_labels;
    private OnuKafkaProducer m_kafkaProducer;
    private final long m_creationTime;
    private long m_transactionId;
    private ThreadPoolExecutor m_kafkaCommunicationPool;

    public MediatedDeviceNetconfSession(Device device, String oltDeviceName, String onuId, String channelTermRef,
                                        HashMap<String, String> labels, long transactionId, OnuKafkaProducer kafkaProducer,
                                        ModelNodeDataStoreManager modelNodeDSM, AdapterManager adapterManager,
                                        ThreadPoolExecutor kafkaCommunicationPool) {
        m_creationTime = System.currentTimeMillis();
        m_onuDevice = device;
        m_onuDeviceName = device.getDeviceName();
        m_kafkaProducer = kafkaProducer;
        m_labels = labels;
        m_transactionId = transactionId;
        m_oltDeviceName = oltDeviceName;
        m_onuId = onuId;
        m_channelTermRef = channelTermRef;
        m_modelNodeDSM = modelNodeDSM;
        m_adapterManager = adapterManager;
        m_requestMap = new LinkedHashMap<>();
        m_kafkaCommunicationPool = kafkaCommunicationPool;
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
                    future = onEditConfig((EditConfigRequest)netconfRequest);
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
        NotificationRequest request = new NotificationRequest(m_onuDeviceName, m_oltDeviceName, m_channelTermRef,
                m_onuId, ONUConstants.ONU_UNDETECTED, m_labels);
        String jsonUndetectNotification = getJsonNotification(request);
        if (m_open) {
            sendRequest(ONUConstants.ONU_UNDETECTED, jsonUndetectNotification, request, null);
        }
        m_kafkaCommunicationPool.shutdown();
        try {
            close();
        } catch (InterruptedException e) {
            LOGGER.error("Error while closing session for " + m_onuDeviceName, e);
        } catch (IOException e) {
            LOGGER.error("Error while closing session for " + m_onuDeviceName, e);
        }
    }

    private void sendRequest(String operation, String requestStringInJSON, AbstractNetconfRequest request,
                             TimestampFutureResponse future) {
        String topicName = null;
        try {
            switch (operation) {
                case ONUConstants.ONU_DETECTED:
                case ONUConstants.ONU_UNDETECTED:
                    topicName = ONUConstants.ONU_NOTIFICATION_KAFKA_TOPIC;
                    break;
                case ONUConstants.ONU_GET_OPERATION:
                case ONUConstants.ONU_COPY_OPERATION:
                case ONUConstants.ONU_EDIT_OPERATION:
                    topicName = ONUConstants.ONU_REQUEST_KAFKA_TOPIC;
                    break;
                default:
                    topicName = ONUConstants.ONU_REQUEST_KAFKA_TOPIC;
            }
            synchronized (m_lock) {
                if (m_open) {
                    m_kafkaProducer.sendNotification(topicName, requestStringInJSON);
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

    private void registerRequestInMap(AbstractNetconfRequest request, TimestampFutureResponse future) {
        synchronized (m_lock) {
            m_requestMap.put(request.getMessageId(), future);
        }
    }

    public String getJsonNotification(NotificationRequest request) {
        LOGGER.debug(String.format("Processing %s request", request.getEvent()));
        setMessageId(request);
        if (request.getMessageId().isEmpty() || request.getMessageId() == null) {
            LOGGER.error("MessegeId is not set for the DETECT/UNDETECT request notification");
        }
        return request.getJsonNotification();
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
        TimestampFutureResponse future = new TimestampFutureResponse();
        m_kafkaCommunicationPool.execute(() -> {
            try {
                setMessageId(request);
                String targetConfig = DocumentUtils.documentToPrettyString(request.getRequestDocument());
                if (request instanceof CopyConfigRequest) {
                    CopyConfigRequest copyConfig = (CopyConfigRequest)request;
                    targetConfig = convertXmlToJson(DocumentUtils.documentToPrettyString(copyConfig.getSourceConfigElement()));
                }
                String payloadString = getPayloadPrefix(ONUConstants.ONU_COPY_OPERATION, request.getMessageId())
                        + ONUConstants.ONU_TARGET_CONFIG + "\"" + ONUConstants.COLON + targetConfig + "}";
                String requestJsonString = getJsonRequest(ONUConstants.PAYLOAD_JSON_KEY, payloadString);

                sendRequest(ONUConstants.ONU_COPY_OPERATION, requestJsonString, request, future);
                internalOkResponse(request, future);
                return;
            } catch (NetconfMessageBuilderException e) {
                LOGGER.error("Error while processing copy-config request for device " + m_onuDeviceName, e);
            }
            internalNokResponse(request, future, "Error processing copy-config");
        });
        return future;
    }

    private String getJsonRequest(String key, String value) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put(ONUConstants.ONU_NAME_JSON_KEY, m_onuDeviceName);
        requestMap.put(ONUConstants.OLT_NAME_JSON_KEY, m_oltDeviceName);
        requestMap.put(ONUConstants.ONU_ID_JSON_KEY, m_onuId);
        requestMap.put(ONUConstants.CHANNEL_TERMINATION_REF_JSON_KEY, m_channelTermRef);
        requestMap.put(ONUConstants.EVENT, ONUConstants.REQUEST_EVENT);
        requestMap.put(ONUConstants.LABELS_JSON_KEY, m_labels);
        requestMap.put(key, value);
        JSONObject requestJSON = new JSONObject(requestMap);
        return requestJSON.toString(ONUConstants.JSON_INDENT_FACTOR);
    }

    private CompletableFuture<NetConfResponse> onGet(GetRequest request) {
        LOGGER.info(String.format("Sending get request for device %s: %s", m_onuDeviceName, request.requestToString()));
        synchronized (m_lock) {
            if (!m_open) {
                LOGGER.debug("Mediated Device Netconf Session is closed. Unable to process GET request");
                return CompletableFuture.completedFuture(null);
            }
        }
        TimestampFutureResponse future = new TimestampFutureResponse();
        m_kafkaCommunicationPool.execute(() -> {
            setMessageId(request);
            if (request.getMessageId() == null || request.getMessageId().isEmpty()) {
                LOGGER.debug("Unable to set GET request messageId. Terminate processing GET request");
                internalNokResponse(request, future, "Unable to set GET request messageId");
                return;
            }
            NetconfFilter filter = request.getFilter();
            if (filter != null) {
                SchemaRegistry deviceSchemaRegistry = AdapterUtils.getAdapterContext(m_onuDevice, m_adapterManager).getSchemaRegistry();
                DeviceJsonUtils deviceJsonUtils = new DeviceJsonUtils(deviceSchemaRegistry, m_modelNodeDSM);
                List<String> filterElementsJsonList = new ArrayList<>();
                for (Element element : filter.getXmlFilterElements()) {
                    DataSchemaNode deviceRootSchemaNode = deviceJsonUtils.getDeviceRootSchemaNode(element.getLocalName(),
                            element.getNamespaceURI());
                    String filterElementJsonString = JsonUtil.convertFromXmlToJsonIgnoreEmptyLeaves(Arrays.asList(element),
                            deviceRootSchemaNode, deviceSchemaRegistry, m_modelNodeDSM);
                    filterElementsJsonList.add(filterElementJsonString.substring(1, filterElementJsonString.length() - 1));
                    LOGGER.debug("JSON converted string for the GET request of the filter element is " + filterElementJsonString);
                }
                String payloadJsonString = getPayloadPrefix(ONUConstants.ONU_GET_OPERATION, request.getMessageId())
                        + ONUConstants.FILTERS_JSON_KEY + "\"" + ONUConstants.COLON + "{\"" + ONUConstants.NETWORK_MANAGER
                        + ONUConstants.COLON + ONUConstants.ROOT + "\"" + ONUConstants.COLON
                        + "{" + String.join(",", filterElementsJsonList) + "}}}";
                String requestJsonString = getJsonRequest("payload", payloadJsonString);
                sendRequest(ONUConstants.ONU_GET_OPERATION, requestJsonString, request, future);
            } else {
                LOGGER.error("No filter for GET request");
                internalNokResponse(request, future, "Unable to get filters of the GET request");
            }
        });
        return future;
    }

    private CompletableFuture<NetConfResponse> onEditConfig(EditConfigRequest request) {
        LOGGER.info(String.format("DZS: Sending edit config request for device %s: >%s<", m_onuDeviceName,
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
                DeviceConfigBackup backupDatastore = ((VomciAdapterDeviceInterface)AdapterUtils.getAdapterContext(m_onuDevice,
                    m_adapterManager).getDeviceInterface()).getDatastoreBackup();
                if (backupDatastore != null) {
                    String preConfig = convertXmlToJson(backupDatastore.getOldDataStore());
                    String postConfig = convertXmlToJson(backupDatastore.getUpdatedDatastore());

                    if (isInvalidConfig(request, future, ONUConstants.ONU_EDIT_OPERATION, preConfig)
                            || isInvalidConfig(request, future, ONUConstants.ONU_EDIT_OPERATION, postConfig)
                            || preConfigEqualsPostConfig(request, future, ONUConstants.ONU_EDIT_OPERATION, preConfig, postConfig)) {
                        //Do nothing
                    } else {
                        EditConfigElement configElement = request.getConfigElement();
                        List<String> configElementsJsonList = new ArrayList<>();
                        if (configElement != null) {
                            SchemaRegistry deviceSchemaRegistry = AdapterUtils.getAdapterContext(m_onuDevice, m_adapterManager)
                                    .getSchemaRegistry();
                            DeviceJsonUtils deviceJsonUtils = new DeviceJsonUtils(deviceSchemaRegistry, m_modelNodeDSM);
                            for (Element element : configElement.getConfigElementContents()) {
                                DataSchemaNode deviceRootSchemaNode = deviceJsonUtils.getDeviceRootSchemaNode(element.getLocalName(),
                                        element.getNamespaceURI());
                                String configElementJsonString = JsonUtil.convertFromXmlToJsonIgnoreEmptyLeaves(Arrays.asList(element),
                                        deviceRootSchemaNode, deviceSchemaRegistry, m_modelNodeDSM);
                                configElementsJsonList.add(configElementJsonString.substring(1, configElementJsonString.length() - 1));
                                LOGGER.debug("JSON converted string for the edit-config request of the filter element is "
                                        + configElementJsonString);
                            }
                        }
                        String payloadJsonString = getPayloadPrefix(ONUConstants.ONU_EDIT_OPERATION, request.getMessageId())
                                + ONUConstants.ONU_CURRENT_CONFIG + "\"" + ONUConstants.COLON + preConfig + ", \""
                                + ONUConstants.ONU_TARGET_CONFIG + "\"" + ONUConstants.COLON + postConfig + ", \""
                                + ONUConstants.ONU_DELTA_CONFIG + "\"" + ONUConstants.COLON
                                + "{" + String.join(",", configElementsJsonList)  + "}}";
                        String requestJsonString = getJsonRequest(ONUConstants.PAYLOAD_JSON_KEY, payloadJsonString);
                        sendRequest(ONUConstants.ONU_EDIT_OPERATION, requestJsonString, request, future);
                    }
                } else {
                    LOGGER.error(String.format("No backup datastore found for edit-config with message-id %s found for device %s",
                            request.getMessageId(), m_onuDeviceName));
                    internalNokResponse(request, future, "Error while processing edit-config. No backup datastore found for message-id "
                            + request.getMessageId());
                }
            } catch (Exception e) {
                LOGGER.error("Error while processing edit-config request for device" + m_onuDeviceName, e);
                internalNokResponse(request, future, e.toString());
            }
        });
        return future;
    }

    private boolean isInvalidConfig(AbstractNetconfRequest request, TimestampFutureResponse future, String operation,
                                    String config) {
        if (config == null || config.isEmpty()) {
            LOGGER.error(String.format("Unable to fetch pre/post backup configuration for %s request for device %s",
                    operation, m_onuDeviceName));
            internalNokResponse(request, future,
                    "Unable to fetch pre/post backup configuration for " + operation + " request for device " + m_onuDeviceName);
            return true;
        }
        return false;
    }

    private boolean preConfigEqualsPostConfig(AbstractNetconfRequest request, TimestampFutureResponse future, String operation,
                                              String preConfig, String postConfig) {
        if (preConfig.equals(postConfig)) {
            LOGGER.debug(String.format("Config already is in sync (same config in cache) - skipping %s request for device %s",
                    operation, m_onuDeviceName));
            internalOkResponse(request, future);
            return true;
        }
        return false;
    }

    private String getPayloadPrefix(String operation, String messageId) {
        return "{\"operation\"" + ONUConstants.COLON + "\"" + operation
                + "\", \"identifier\"" + ONUConstants.COLON + "\"" + messageId + "\", \"";
    }

    private String convertXmlToJson(String xml) {
        try {
            Element element = DocumentUtils.stringToDocument(xml).getDocumentElement();
            List<Element> deviceRootNodes = DocumentUtils.getChildElements(element);
            return convertElementsToJson(deviceRootNodes);
        } catch (Exception exception) {
            LOGGER.error("Error in convertXmlToJson ", exception);
            return "";
        }
    }

    private String convertElementsToJson(List<Element> deviceRootNodes) {
        SchemaRegistry deviceSchemaRegistry = AdapterUtils.getAdapterContext(m_onuDevice, m_adapterManager).getSchemaRegistry();
        DeviceJsonUtils deviceJsonUtils = new DeviceJsonUtils(deviceSchemaRegistry, m_modelNodeDSM);
        List<String> jsonNodes = new ArrayList<>();
        for (Element deviceRootNode : deviceRootNodes) {
            DataSchemaNode schemaNode = deviceJsonUtils.getDeviceRootSchemaNode(deviceRootNode.getLocalName(),
                    deviceRootNode.getNamespaceURI());
            String jsonNode = JsonUtil.convertFromXmlToJson(Arrays.asList(deviceRootNode), schemaNode,
                    deviceSchemaRegistry, m_modelNodeDSM);
            jsonNodes.add(jsonNode.substring(1, jsonNode.length() - 1));
        }
        return "{" + String.join(",", jsonNodes) + "}";
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
}
