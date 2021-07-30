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

/**
 * <p>
 * Response information received from vomci function
 * </p>
 * Created by Ranjitha B R (Nokia) on 13/05/2021.
 */
public class ResponseData {

    private String onuName;

    private String oltName;

    private String channelTermRef;

    private String operationType;

    private String identifier;

    private String responseStatus;

    private String failureReason;

    private String responsePayload;

    private String onuId;

    // These are GPB specific
    private String recipientName;

    private String senderName;

    private ObjectType objectType;

    private String objectName;

    // For now used by the JsonFormatter
    public ResponseData(String onuName, String oltName, String channelTermRef, String identifier, String operationType,
                        String onuId, String responseStatus, String failureReason, String data) {
        this.onuName = onuName;
        this.oltName = oltName;
        this.channelTermRef = channelTermRef;
        this.identifier = identifier;
        this.operationType = operationType;
        this.onuId = onuId;
        this.responseStatus = responseStatus;
        this.failureReason = failureReason;
        this.responsePayload = data;
    }

    // For now used by the GpbFormatter
    public ResponseData(String recipientName, String senderName, ObjectType objectType, String objectName,
                        String identifier, String operationType, String responseStatus, String failureReason, String data) {
        this.recipientName = recipientName;
        this.senderName = senderName;
        this.objectType = objectType;
        this.objectName = objectName;
        this.identifier = identifier;
        this.operationType = operationType;
        this.responseStatus = responseStatus;
        this.failureReason = failureReason;
        this.responsePayload = data;
    }


    public String getOnuName() {
        return onuName;
    }

    public void setOnuName(String onuName) {
        this.onuName = onuName;
    }

    public String getOltName() {
        return oltName;
    }

    public void setOltName(String oltName) {
        this.oltName = oltName;
    }

    public String getChannelTermRef() {
        return channelTermRef;
    }

    public void setChannelTermRef(String channelTermRef) {
        this.channelTermRef = channelTermRef;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    public void setObjectType(ObjectType objectType) {
        this.objectType = objectType;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(String responseStatus) {
        this.responseStatus = responseStatus;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getResponsePayload() {
        return responsePayload;
    }

    public void setResponsePayload(String responsePayload) {
        this.responsePayload = responsePayload;
    }

    public String getOnuId() {
        return onuId;
    }

    public void setOnuId(String onuId) {
        this.onuId = onuId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ResponseData{");
        sb.append("identifier='").append(identifier).append('\'');
        sb.append(", recipientName='").append(recipientName).append('\'');
        sb.append(", senderName='").append(senderName).append('\'');
        sb.append(", objectType='").append(objectType).append('\'');
        sb.append(", objectName='").append(objectName).append('\'');
        sb.append(", onuName='").append(onuName).append('\'');
        sb.append(", oltName='").append(oltName).append('\'');
        sb.append(", channelTermRef='").append(channelTermRef).append('\'');
        sb.append(", operationType='").append(operationType).append('\'');
        sb.append(", responsePayload='").append(responsePayload).append('\'');
        sb.append(", failureReason='").append(failureReason).append('\'');
        sb.append(", responseStatus='").append(responseStatus).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
