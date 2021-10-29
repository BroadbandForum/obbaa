/*
 * Copyright 2021 Broadband Forum
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

package org.broadband_forum.obbaa.onu.message;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.device.adapter.AdapterUtils;
import org.broadband_forum.obbaa.device.adapter.DeviceConfigBackup;
import org.broadband_forum.obbaa.device.adapter.VomciAdapterDeviceInterface;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.CopyConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.onu.ONUConstants;
import org.broadband_forum.obbaa.onu.exception.MessageFormatterException;
import org.broadband_forum.obbaa.onu.message.gpb.message.Action;
import org.broadband_forum.obbaa.onu.message.gpb.message.Body;
import org.broadband_forum.obbaa.onu.message.gpb.message.GetData;
import org.broadband_forum.obbaa.onu.message.gpb.message.Header;
import org.broadband_forum.obbaa.onu.message.gpb.message.Header.OBJECT_TYPE;
import org.broadband_forum.obbaa.onu.message.gpb.message.Msg;
import org.broadband_forum.obbaa.onu.message.gpb.message.Notification;
import org.broadband_forum.obbaa.onu.message.gpb.message.RPC;
import org.broadband_forum.obbaa.onu.message.gpb.message.ReplaceConfig;
import org.broadband_forum.obbaa.onu.message.gpb.message.Request;
import org.broadband_forum.obbaa.onu.message.gpb.message.Response;
import org.broadband_forum.obbaa.onu.message.gpb.message.Status;
import org.broadband_forum.obbaa.onu.message.gpb.message.Status.StatusCode;
import org.broadband_forum.obbaa.onu.message.gpb.message.UpdateConfig;
import org.broadband_forum.obbaa.onu.message.gpb.message.UpdateConfigInstance;
import org.broadband_forum.obbaa.onu.util.XmlUtil;

import com.google.protobuf.ByteString;

/**
 * <p>
 * Formatting messages to GPB format from XML or from GPB Format to XML
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 13/05/2021. Updated by Filipe Cláudio (Altice Labs) on 19/05/2021
 */
public class GpbFormatter implements MessageFormatter<Msg> {

    private static final Logger LOGGER = Logger.getLogger(GpbFormatter.class);
    private static final String SENDER_NAME = ONUConstants.VOLTMF_NAME;

    @Override
    public Msg getFormattedRequest(AbstractNetconfRequest request,
                                   String operationType,
                                   Device onuDevice,
                                   AdapterManager adapterManager,
                                   ModelNodeDataStoreManager modelNodeDsm,
                                   SchemaRegistry schemaRegistry,
                                   NetworkWideTag networkWideTag) throws NetconfMessageBuilderException, MessageFormatterException {

        Msg msg = null;
        // These operations can be sent to the ONU, vOMCI-Function or vOMCI-Proxy.
        switch (operationType) {
            case NetconfResources.RPC:
                msg = getFormattedMessageForRpc((NetconfRpcRequest) request, onuDevice,
                        adapterManager, modelNodeDsm, networkWideTag);
                break;
            case ONUConstants.CREATE_ONU:
                msg = getFormattedMessageForCreateOnu((NetconfRpcRequest) request, schemaRegistry,
                        modelNodeDsm, networkWideTag);
                break;
            case ONUConstants.DELETE_ONU:
                msg = getFormattedMessageForOnuAction((ActionRequest) request, schemaRegistry,
                        modelNodeDsm, networkWideTag);
                break;
            case ONUConstants.SET_ONU_COMMUNICATION_TRUE:
            case ONUConstants.SET_ONU_COMMUNICATION_FALSE:
                msg = getFormattedMessageForOnuAction((ActionRequest) request, schemaRegistry,
                        modelNodeDsm, networkWideTag);
                break;

            case NetconfResources.ACTION:
                msg = getFormattedMessageForAction((ActionRequest) request, onuDevice,
                        adapterManager, modelNodeDsm, networkWideTag);
                break;
            case NetconfResources.COPY_CONFIG:
                if (networkWideTag.getObjectType().equals(ObjectType.ONU)) {
                    msg = getFormattedMessageForCopyConfig((CopyConfigRequest) request, onuDevice,
                            adapterManager, modelNodeDsm, networkWideTag);
                } else {
                    throw new MessageFormatterException("Object type not yet supported for copy-config:"
                                                        + networkWideTag.getObjectType().name());
                }
                break;
            case NetconfResources.EDIT_CONFIG:
                if (networkWideTag.getObjectType().equals(ObjectType.ONU)) {
                    msg = getFormattedMessageForEditConfig((EditConfigRequest) request, onuDevice,
                            adapterManager, modelNodeDsm, networkWideTag);
                } else {
                    throw new MessageFormatterException("Object type not yet supported for edit-config:"
                                                        + networkWideTag.getObjectType().name());
                }
                break;
            case NetconfResources.GET:
                msg = getFormattedMessageForGet((GetRequest) request, schemaRegistry, onuDevice,
                        adapterManager, modelNodeDsm, networkWideTag);
                break;
            default:
                LOGGER.warn("GpbFormatter didn't recognize the operation: " + operationType);
                break;
        }
        return msg;
    }

    @Override
    public ResponseData getResponseData(Object responseObject) throws MessageFormatterException {
        if (responseObject == null) {
            throw new MessageFormatterException("Response received without information.");
        }

        String recipientName = null;
        String senderName = null;
        ObjectType objectType = null;
        String objectName = null;
        String identifier = null;
        String operationType = null;
        String responseStatus = null;
        String failureReason = null;
        String data = null;

        ResponseData responseData = null;

        if (responseObject instanceof Msg) {
            Msg msg = (Msg) responseObject;
            validateResponse(msg);

            recipientName = msg.getHeader().getRecipientName();
            senderName = msg.getHeader().getSenderName();
            objectType = ObjectType.getObjectTypeFromCode(msg.getHeader().getObjectType().getNumber());
            objectName = msg.getHeader().getObjectName();
            identifier = msg.getHeader().getMsgId();
            if (msg.getBody() != null && msg.getBody().getResponse() != null && !msg.getBody().getResponse().toString().isEmpty()) {
                Response response = msg.getBody().getResponse();
                if (response.hasGetResp()) {
                    operationType = NetconfResources.GET_CONFIG;
                    data = response.getGetResp().getData().toStringUtf8();
                } else if (response.hasReplaceConfigResp()) {
                    operationType = NetconfResources.COPY_CONFIG;
                    responseStatus = getStatusFromResponse(response.getReplaceConfigResp().getStatusResp());
                    failureReason = getErrorCauseFromResponse(response.getReplaceConfigResp().getStatusResp());
                } else if (response.hasUpdateConfigResp()) {
                    operationType = NetconfResources.EDIT_CONFIG;
                    responseStatus = getStatusFromResponse(response.getUpdateConfigResp().getStatusResp());
                    failureReason = getErrorCauseFromResponse(response.getUpdateConfigResp().getStatusResp());
                } else if (response.hasRpcResp()) {
                    operationType = NetconfResources.RPC;
                    data = response.getRpcResp().getOutputData().toStringUtf8();
                    responseStatus = getStatusFromResponse(response.getRpcResp().getStatusResp());
                    failureReason = getErrorCauseFromResponse(response.getRpcResp().getStatusResp());
                } else if (response.hasActionResp()) {
                    operationType = NetconfResources.ACTION;
                    data = response.getActionResp().getOutputData().toStringUtf8();
                    responseStatus = getStatusFromResponse(response.getActionResp().getStatusResp());
                    failureReason = getErrorCauseFromResponse(response.getActionResp().getStatusResp());
                } else {
                    throw new MessageFormatterException("The response body does not match with any of the expected.");
                }
            } else if (msg.getBody() != null && msg.getBody().getNotification() != null && !msg.getBody()
                    .getNotification().toString().isEmpty()) {
                Notification notification = msg.getBody().getNotification();
                data = notification.getData().toStringUtf8();
                operationType = NetconfResources.NOTIFICATION;
            } else {
                throw new MessageFormatterException("Response received without information.");
            }
            responseData = new ResponseData(recipientName, senderName, objectType, objectName,
                    identifier, operationType, responseStatus, failureReason, data);
            if (objectType != null && objectType.toString().equals(ObjectType.ONU.name())) {
                responseData.setOnuName(objectName);
            }
        } else {
            throw new MessageFormatterException(String.format("GpbFormatter can only process responses of type '%s'. Given: '%s'.",
                    Msg.class.toString(), responseObject.getClass().toString()));
        }
        return responseData;
    }

    private Msg getFormattedMessageForRpc(NetconfRpcRequest request, Device onuDevice, AdapterManager adapterManager,
                                          ModelNodeDataStoreManager modelNodeDsm,
                                          NetworkWideTag networkWideTag) throws NetconfMessageBuilderException {
        String payload = XmlUtil.convertXmlToJson(onuDevice, adapterManager, modelNodeDsm,
                DocumentUtils.documentToPrettyString(request.getRpcInput()));
        return Msg.newBuilder()
                    .setHeader(buildHeader(request, networkWideTag))
                    .setBody(Body.newBuilder().setRequest(buildRpcRequest(payload)).build())
                    .build();
    }

    private Msg getFormattedMessageForCreateOnu(NetconfRpcRequest request, SchemaRegistry schemaRegistry,
                                          ModelNodeDataStoreManager modelNodeDsm,
                                          NetworkWideTag networkWideTag) throws NetconfMessageBuilderException {
        String payload = XmlUtil.convertXmlToJson(schemaRegistry, modelNodeDsm,
                DocumentUtils.documentToPrettyString(request.getRpcInput()));
        return Msg.newBuilder()
                .setHeader(buildHeader(request, networkWideTag))
                .setBody(Body.newBuilder().setRequest(buildRpcRequest(payload)).build())
                .build();
    }

    private Msg getFormattedMessageForOnuAction(ActionRequest request, SchemaRegistry schemaRegistry,
                                                ModelNodeDataStoreManager modelNodeDsm,
                                                NetworkWideTag networkWideTag) throws NetconfMessageBuilderException {
        String payload = XmlUtil.convertXmlToJson(schemaRegistry, modelNodeDsm,
                DocumentUtils.documentToPrettyString(request.getActionTreeElement()));
        return Msg.newBuilder()
                .setHeader(buildHeader(request, networkWideTag))
                .setBody(Body.newBuilder().setRequest(buildActionRequest(payload)).build())
                .build();
    }


    private Msg getFormattedMessageForAction(ActionRequest request, Device onuDevice, AdapterManager adapterManager,
                                             ModelNodeDataStoreManager modelNodeDsm,
                                             NetworkWideTag networkWideTag) throws NetconfMessageBuilderException {
        String payload = XmlUtil.convertXmlToJson(onuDevice, adapterManager, modelNodeDsm,
                DocumentUtils.documentToPrettyString(request.getActionTreeElement()));
        return Msg.newBuilder()
                .setHeader(buildHeader(request, networkWideTag))
                .setBody(Body.newBuilder().setRequest(buildActionRequest(payload)).build())
                .build();
    }

    private Msg getFormattedMessageForCopyConfig(CopyConfigRequest request, Device onuDevice, AdapterManager adapterManager,
                                                 ModelNodeDataStoreManager modelNodeDsm,
                                                 NetworkWideTag networkWideTag) throws NetconfMessageBuilderException {
        String targetConfig = XmlUtil.convertXmlToJson(onuDevice, adapterManager, modelNodeDsm,
                DocumentUtils.documentToPrettyString(request.getSourceConfigElement()));
        return Msg.newBuilder()
                .setHeader(buildHeader(request, networkWideTag))
                .setBody(Body.newBuilder().setRequest(buildReplaceConfigRequest(targetConfig)).build())
                .build();
    }

    private Msg getFormattedMessageForEditConfig(EditConfigRequest request, Device onuDevice,
                                                 AdapterManager adapterManager, ModelNodeDataStoreManager modelNodeDsm,
                                                 NetworkWideTag networkWideTag) throws MessageFormatterException {

        DeviceConfigBackup backupDatastore = ((VomciAdapterDeviceInterface) AdapterUtils.getAdapterContext(onuDevice,
                adapterManager).getDeviceInterface()).getDatastoreBackup();
        String onuDeviceName = networkWideTag.getOnuDeviceName();
        MessageFormatterHelper.validateBackupDatastore(onuDeviceName, request.getMessageId(), backupDatastore);

        String currentConfig = XmlUtil.convertXmlToJson(onuDevice, adapterManager, modelNodeDsm, backupDatastore.getOldDataStore());
        String postConfig = XmlUtil.convertXmlToJson(onuDevice, adapterManager, modelNodeDsm, backupDatastore.getUpdatedDatastore());
        MessageFormatterHelper.validateConfig(currentConfig, postConfig, onuDeviceName);

        String deltaConfig = MessageFormatterHelper.getJsonForDeltaConfig(request, onuDevice, adapterManager, modelNodeDsm);
        return Msg.newBuilder()
                .setHeader(buildHeader(request, networkWideTag))
                .setBody(Body.newBuilder().setRequest(buildUpdateConfigRequest(currentConfig, deltaConfig)).build())
                .build();
    }

    private Msg getFormattedMessageForGet(GetRequest request, SchemaRegistry schemaRegistry, Device onuDevice,
                                          AdapterManager adapterManager, ModelNodeDataStoreManager modelNodeDsm,
                                          NetworkWideTag networkWideTag) {
        String filterElementsJsonList = MessageFormatterHelper.getFilterElementsJsonListForGetRequest(request, schemaRegistry,
                onuDevice, adapterManager, modelNodeDsm);
        return Msg.newBuilder()
                .setHeader(buildHeader(request, networkWideTag))
                .setBody(Body.newBuilder().setRequest(buildGetDataRequest(filterElementsJsonList)).build())
                .build();
    }

    private Header buildHeader(AbstractNetconfRequest request, NetworkWideTag networkWideTag) {
        return Header.newBuilder()
                .setMsgId(request.getMessageId())
                .setSenderName(SENDER_NAME)
                .setRecipientName(networkWideTag.getRecipientName())
                .setObjectType(OBJECT_TYPE.forNumber(networkWideTag.getObjectType().getCode()))
                .setObjectName(networkWideTag.getObjectName())
                .build();
    }

    private Request buildGetDataRequest(String filters) {
        return Request.newBuilder()
                .setGetData(GetData.newBuilder()
                        .addFilter(ByteString.copyFromUtf8(filters))
                        .build())
                .build();
    }

    private Request buildReplaceConfigRequest(String targetConfig) {
        return Request.newBuilder()
                .setReplaceConfig(ReplaceConfig.newBuilder()
                        .setConfigInst(ByteString.copyFromUtf8(targetConfig))
                        .build())
                .build();
    }

    private Request buildUpdateConfigRequest(String currentConfig, String deltaConfig) {
        return Request.newBuilder()
                .setUpdateConfig(UpdateConfig.newBuilder()
                        .setUpdateConfigInst(UpdateConfigInstance.newBuilder()
                                .setCurrentConfigInst(ByteString.copyFromUtf8(currentConfig))
                                .setDeltaConfig(ByteString.copyFromUtf8(deltaConfig))
                                .build())
                        .build())
                .build();
    }

    private Request buildActionRequest(String payload) {
        return Request.newBuilder()
                .setAction(Action.newBuilder()
                        .setInputData(ByteString.copyFromUtf8(payload))
                        .build())
                    .build();
    }

    private Request buildRpcRequest(String payload) {
        return Request.newBuilder()
                .setRpc(RPC.newBuilder()
                        .setInputData(ByteString.copyFromUtf8(payload))
                        .build())
                .build();
    }

    private String getStatusFromResponse(Status status) {
        if (status != null && status.getStatusCode() != null) {
            return status.getStatusCode().name();
        }
        return StatusCode.UNRECOGNIZED.name();
    }

    private String getErrorCauseFromResponse(Status status) {
        if (status != null && status.getErrorList() != null && !status.getErrorList().isEmpty()) {
            return status.getErrorList().stream()
                    .map(e -> e.getErrorType() + ":" + e.getErrorMessage())
                    .reduce("Error Messages: ", (acc, element) -> acc + "\n-" + element);
        }
        return null;
    }

    private void validateResponse(Msg msg) throws MessageFormatterException {
        if (msg.getHeader() == null) {
            throw new MessageFormatterException("Message received without information..");
        }
        Header header = msg.getHeader();
        if (!header.getRecipientName().equals(SENDER_NAME)) {
            throw new MessageFormatterException(String.format("The recipient name from response is different than expected. "
                                      + "Expected = %s, Received = %s", SENDER_NAME, header.getRecipientName()));
        }
        String objectType = header.getObjectType().toString();
        if (objectType.equals(String.valueOf(OBJECT_TYPE.VOLTMF))) {
            throw new MessageFormatterException(String.format("The object type from response is different than expected. "
                                      + "Expected = %s, Received = %s", OBJECT_TYPE.VOMCI_FUNCTION.name(), header.getObjectType().name()));
        }
    }
}