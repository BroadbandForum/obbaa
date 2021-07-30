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

import java.util.HashMap;

/**
 * <p>
 * Network wide tag containing information such as sender, recepient etc used for communication
 * between vOLTMF and vOMCI network functions
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 13/05/2021.
 */
public class NetworkWideTag {

    private String onuDeviceName;

    private String oltDeviceName;

    private String onuId;

    private String channelTermRef;

    private HashMap<String, String> labels;

    private String senderName;

    private String recipientName;

    private ObjectType objectType;

    private String objectName;

    public NetworkWideTag(String onuDeviceName, String oltDeviceName,
                          String onuId, String channelTermRef, HashMap<String, String> labels) {
        this.onuDeviceName = onuDeviceName;
        this.oltDeviceName = oltDeviceName;
        this.onuId = onuId;
        this.channelTermRef = channelTermRef;
        this.labels = labels;
    }

    public NetworkWideTag(String onuDeviceName, String oltDeviceName,
                          String onuId, String channelTermRef, HashMap<String, String> labels,
                          String objectName, String recipientName, ObjectType objectType) {
        this.onuDeviceName = onuDeviceName;
        this.oltDeviceName = oltDeviceName;
        this.onuId = onuId;
        this.channelTermRef = channelTermRef;
        this.labels = labels;
        this.objectName = objectName;
        this.recipientName = recipientName;
        this.objectType = objectType;
    }

    public NetworkWideTag(String onuDeviceName, String recipientName, String objectName, ObjectType objectType) {
        this.onuDeviceName = onuDeviceName;
        this.objectName = objectName;
        this.recipientName = recipientName;
        this.objectType = objectType;
    }

    public String getOnuDeviceName() {
        return onuDeviceName;
    }

    public void setOnuDeviceName(String onuDeviceName) {
        this.onuDeviceName = onuDeviceName;
    }

    public String getOltDeviceName() {
        return oltDeviceName;
    }

    public void setOltDeviceName(String oltDeviceName) {
        this.oltDeviceName = oltDeviceName;
    }

    public String getOnuId() {
        return onuId;
    }

    public void setOnuId(String onuId) {
        this.onuId = onuId;
    }

    public String getChannelTermRef() {
        return channelTermRef;
    }

    public void setChannelTermRef(String channelTermRef) {
        this.channelTermRef = channelTermRef;
    }

    public HashMap<String, String> getLabels() {
        return labels;
    }

    public void setLabels(HashMap<String, String> labels) {
        this.labels = labels;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
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
}
