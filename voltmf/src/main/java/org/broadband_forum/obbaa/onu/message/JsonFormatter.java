/*
 * Copyright 2021 Broadband Forum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICEN00SE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadband_forum.obbaa.onu.message;

import static org.broadband_forum.obbaa.onu.ONUConstants.ONU_GET_OPERATION;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.device.adapter.AdapterUtils;
import org.broadband_forum.obbaa.device.adapter.DeviceConfigBackup;
import org.broadband_forum.obbaa.device.adapter.VomciAdapterDeviceInterface;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.CopyConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.onu.NotificationRequest;
import org.broadband_forum.obbaa.onu.ONUConstants;
import org.broadband_forum.obbaa.onu.exception.MessageFormatterException;
import org.broadband_forum.obbaa.onu.util.XmlUtil;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * <p>
 * Formatting messages to JSON format from XML or from JSON Format to XML
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 13/05/2021.
 */
public class JsonFormatter implements MessageFormatter {

    private static final Logger LOGGER = Logger.getLogger(JsonFormatter.class);

    @Override
    public String getFormattedRequest(AbstractNetconfRequest request, String operationType, Device onuDevice,
                                      AdapterManager adapterManager, ModelNodeDataStoreManager modelNodeDSM,
                                      SchemaRegistry schemaRegistry, NetworkWideTag networkWideTag)
            throws NetconfMessageBuilderException, MessageFormatterException {

        String requestJsonString = null;
        switch (operationType) {
            case ONUConstants.DETECT_EVENT:
            case ONUConstants.UNDETECT_EVENT:
                requestJsonString = getFormattedMessageForDetectUndetect(request, networkWideTag);
                break;
            case NetconfResources.COPY_CONFIG:
                requestJsonString = getFormattedMessageForCopyConfig((CopyConfigRequest) request, onuDevice,
                        adapterManager, modelNodeDSM, networkWideTag);
                break;
            case NetconfResources.EDIT_CONFIG:
                requestJsonString = getFormattedMessageForEditconfig((EditConfigRequest) request, onuDevice,
                        adapterManager, modelNodeDSM, networkWideTag);
                break;
            case NetconfResources.GET:
                if (request instanceof NotificationRequest) {
                    requestJsonString = getFormattedMessageForInternalGet((NotificationRequest)request, networkWideTag);
                } else {
                    requestJsonString = getFormattedMessageForGet((GetRequest) request, schemaRegistry, onuDevice,
                            adapterManager, modelNodeDSM, networkWideTag);
                }
                break;
            default:
                break;
        }
        return requestJsonString;
    }

    private String getFormattedMessageForInternalGet(NotificationRequest request, NetworkWideTag networkWideTag) {
        String requestString = getJsonNotification(request, networkWideTag);
        return requestString;
    }

    private String getFormattedMessageForDetectUndetect(AbstractNetconfRequest request, NetworkWideTag networkWideTag) {
        String requestString = null;
        if (request instanceof NotificationRequest) {
            requestString = getJsonNotification((NotificationRequest)request, networkWideTag);
        }
        return requestString;
    }

    @Override
    public ResponseData getResponseData(Object responseObject) throws MessageFormatterException {
        ResponseData responseData = null;
        try {
            JSONObject jsonResponse = new JSONObject(responseObject.toString());
            if (jsonResponse.getString(ONUConstants.EVENT).equals(ONUConstants.RESPONSE_EVENT)) {
                String onuName = jsonResponse.getString(ONUConstants.ONU_NAME_JSON_KEY);
                String oltName = jsonResponse.optString(ONUConstants.OLT_NAME_JSON_KEY);
                String channelTermRef = jsonResponse.optString(ONUConstants.CHANNEL_TERMINATION_REF_JSON_KEY);
                String onuId = jsonResponse.optString(ONUConstants.ONU_ID_JSON_KEY);
                JSONObject payloadJson = new JSONObject(jsonResponse.getString(ONUConstants.PAYLOAD_JSON_KEY));
                String identifier = payloadJson.getString(ONUConstants.IDENTIFIER_JSON_KEY);
                String operationType = payloadJson.getString(ONUConstants.OPERATION_JSON_KEY);
                String responseStatus = payloadJson.getString(ONUConstants.STATUS_JSON_KEY);
                String failureReason = payloadJson.optString(ONUConstants.FAILURE_REASON);
                String data = payloadJson.optString(ONUConstants.DATA_JSON_KEY);
                responseData = new ResponseData(onuName, oltName, channelTermRef, identifier, operationType, onuId,
                        responseStatus, failureReason, data);
            } else {
                LOGGER.warn(String.format("Non response event received on %s kafka topic. Event received is %s",
                        ONUConstants.ONU_RESPONSE_KAFKA_TOPIC, jsonResponse.getString(ONUConstants.EVENT)));
            }
        } catch (JSONException e) {
            throw new MessageFormatterException("Unable to form JSONObject for the response from vomci function " + e);
        }
        return responseData;
    }

    private String getFormattedMessageForCopyConfig(CopyConfigRequest request, Device onuDevice,
                                                    AdapterManager adapterManager, ModelNodeDataStoreManager modelNodeDsm,
                                                    NetworkWideTag networkWideTag) throws NetconfMessageBuilderException {
        String targetConfig = XmlUtil.convertXmlToJson(onuDevice, adapterManager, modelNodeDsm,
                DocumentUtils.documentToPrettyString(request.getSourceConfigElement()));
        String payloadString = getPayloadPrefix(ONUConstants.ONU_COPY_OPERATION, request.getMessageId())
                + ONUConstants.ONU_TARGET_CONFIG + "\"" + ONUConstants.COLON + targetConfig + "}";
        String requestJsonString = getJsonRequest(ONUConstants.PAYLOAD_JSON_KEY, payloadString, networkWideTag);
        return requestJsonString;
    }

    private String getFormattedMessageForEditconfig(EditConfigRequest request, Device onuDevice,
                                                    AdapterManager adapterManager, ModelNodeDataStoreManager modelNodeDsm,
                                                    NetworkWideTag networkWideTag) throws MessageFormatterException {
        String requestJsonString = null;
        DeviceConfigBackup backupDatastore = ((VomciAdapterDeviceInterface) AdapterUtils.getAdapterContext(onuDevice,
                adapterManager).getDeviceInterface()).getDatastoreBackup();
        String onuDeviceName = networkWideTag.getOnuDeviceName();
        MessageFormatterHelper.validateBackupDatastore(onuDeviceName, request.getMessageId(), backupDatastore);

        String preConfig = XmlUtil.convertXmlToJson(onuDevice, adapterManager, modelNodeDsm, backupDatastore.getOldDataStore());
        String postConfig = XmlUtil.convertXmlToJson(onuDevice, adapterManager, modelNodeDsm, backupDatastore.getUpdatedDatastore());
        MessageFormatterHelper.validateConfig(preConfig, postConfig, onuDeviceName);

        String deltaConfig = MessageFormatterHelper.getJsonForDeltaConfig(request, onuDevice, adapterManager, modelNodeDsm);
        String payloadJsonString = getPayloadPrefix(ONUConstants.ONU_EDIT_OPERATION, request.getMessageId())
                        + ONUConstants.ONU_CURRENT_CONFIG + "\"" + ONUConstants.COLON + preConfig + ", \""
                        + ONUConstants.ONU_TARGET_CONFIG + "\"" + ONUConstants.COLON + postConfig + ", \""
                        + ONUConstants.ONU_DELTA_CONFIG + "\"" + ONUConstants.COLON + deltaConfig + "}";
        requestJsonString = getJsonRequest(ONUConstants.PAYLOAD_JSON_KEY, payloadJsonString, networkWideTag);

        return requestJsonString;
    }

    private String getFormattedMessageForGet(GetRequest request, SchemaRegistry schemaRegistry, Device onuDevice,
                                             AdapterManager adapterManager, ModelNodeDataStoreManager modelNodeDsm,
                                             NetworkWideTag networkWideTag) {
        String filterElementsJsonList = MessageFormatterHelper.getFilterElementsJsonListForGetRequest(request, schemaRegistry,
                onuDevice, adapterManager, modelNodeDsm);

        String payloadJsonString = getPayloadPrefix(ONUConstants.ONU_GET_OPERATION, request.getMessageId())
                + ONUConstants.FILTERS_JSON_KEY + "\"" + ONUConstants.COLON + filterElementsJsonList + "}";

        return getJsonRequest("payload", payloadJsonString, networkWideTag);
    }

    private String getPayloadPrefix(String operation, String messageId) {
        return "{\"operation\"" + ONUConstants.COLON + "\"" + operation
                + "\", \"identifier\"" + ONUConstants.COLON + "\"" + messageId + "\", \"";
    }

    private static String getJsonRequest(String key, String value, NetworkWideTag networkWideTag) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put(ONUConstants.ONU_NAME_JSON_KEY, networkWideTag.getOnuDeviceName());
        requestMap.put(ONUConstants.OLT_NAME_JSON_KEY, networkWideTag.getOltDeviceName());
        requestMap.put(ONUConstants.ONU_ID_JSON_KEY, networkWideTag.getOnuId());
        requestMap.put(ONUConstants.CHANNEL_TERMINATION_REF_JSON_KEY, networkWideTag.getChannelTermRef());
        requestMap.put(ONUConstants.EVENT, ONUConstants.REQUEST_EVENT);
        requestMap.put(ONUConstants.LABELS_JSON_KEY, networkWideTag.getLabels());
        requestMap.put(key, value);
        JSONObject requestJSON = new JSONObject(requestMap);
        return requestJSON.toString(ONUConstants.JSON_INDENT_FACTOR);
    }

    private static String getJsonNotification(NotificationRequest request, NetworkWideTag networkWideTag) {
        StringBuffer payloadString = new StringBuffer("{");
        if (networkWideTag.getOnuDeviceName() == null && request.getMessageId() == ONUConstants.DEFAULT_MESSAGE_ID) {
            payloadString.append("\"" + "operation" + "\"" + ONUConstants.COLON + "\"" + ONU_GET_OPERATION + "\",");
        } else {
            payloadString.append("\"" + "operation" + "\"" + ONUConstants.COLON + "\"" + request.getEvent() + "\",");
        }
        payloadString.append("\"" + "identifier" + "\"" + ONUConstants.COLON + "\"" + request.getMessageId() + "\",");
        if (request.getMessageId() == ONUConstants.DEFAULT_MESSAGE_ID && request.getOnuDeviceName() == null) {
            payloadString.append("\"" + "filters" + "\"" + ONUConstants.COLON + ONUConstants.GET_FILTER);
        }
        payloadString.replace(payloadString.length() - 1, payloadString.length(), "}");

        StringBuffer labelsJsonString = new StringBuffer("{");
        request.getLabels().forEach((name, value) -> {
            labelsJsonString.append("\"" + name + "\"" + ONUConstants.COLON + "\"" + value + "\",");
        });
        labelsJsonString.replace(labelsJsonString.length() - 1, labelsJsonString.length(), "}");
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put(ONUConstants.PAYLOAD_JSON_KEY, payloadString);
        requestMap.put(ONUConstants.ONU_NAME_JSON_KEY, networkWideTag.getOnuDeviceName());
        if (networkWideTag.getOltDeviceName() != null) {
            requestMap.put(ONUConstants.OLT_NAME_JSON_KEY, networkWideTag.getOltDeviceName());
        }
        if (networkWideTag.getChannelTermRef() != null) {
            requestMap.put(ONUConstants.CHANNEL_TERMINATION_REF_JSON_KEY, networkWideTag.getChannelTermRef());
        }
        if (networkWideTag.getOnuId() != null) {
            requestMap.put(ONUConstants.ONU_ID_JSON_KEY, networkWideTag.getOnuId());
        }
        requestMap.put(ONUConstants.EVENT, request.getEvent());
        requestMap.put(ONUConstants.LABELS_JSON_KEY, labelsJsonString);
        JSONObject requestJSON = new JSONObject(requestMap);
        return requestJSON.toString(ONUConstants.JSON_INDENT_FACTOR);
    }
}
