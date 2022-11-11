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

import static org.broadband_forum.obbaa.onu.ONUConstants.ONU_GET_OPERATION;

import java.util.HashMap;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.json.JSONObject;
import org.w3c.dom.Document;

/**
 * <p>
 * Messages to be forwarded to vOMCI
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 22/07/2020.
 */
public class NotificationRequest extends AbstractNetconfRequest {
    private final String m_onuDeviceName;
    private final String m_oltDeviceName;
    private final String m_chnlTermRef;
    private final String m_onuId;
    private final String m_event;
    private final HashMap<String, String> m_labels;

    public NotificationRequest(String onuDeviceName, String oltDeviceName, String chnlTermRef, String onuId,
                               String notificationEvent, HashMap<String, String> labels) {
        m_onuDeviceName = onuDeviceName;
        m_oltDeviceName = oltDeviceName;
        m_chnlTermRef = chnlTermRef;
        m_onuId = onuId;
        m_event = notificationEvent;
        m_labels = labels;
    }

    @Override
    public Document getRequestDocumentInternal() {
        return null;
    }

    @Override
    public String getRpcType() {
        return null;
    }

    public String getOnuDeviceName() {
        return m_onuDeviceName;
    }

    public String getOltDeviceName() {
        return m_oltDeviceName;
    }

    public String getChannelTermRef() {
        return m_chnlTermRef;
    }

    public String getOnuId() {
        return m_onuId;
    }

    public HashMap<String, String> getLabels() {
        return m_labels;
    }

    public String getEvent() {
        return m_event;
    }

    public String getJsonNotification() {
        StringBuffer payloadString = new StringBuffer("{");
        if (getOnuDeviceName() == null && getMessageId() == ONUConstants.DEFAULT_MESSAGE_ID) {
            payloadString.append("\"" + "operation" + "\"" + ONUConstants.COLON + "\"" + ONU_GET_OPERATION + "\",");
        } else {
            payloadString.append("\"" + "operation" + "\"" + ONUConstants.COLON + "\"" + getEvent() + "\",");
        }
        payloadString.append("\"" + "identifier" + "\"" + ONUConstants.COLON + "\"" + getMessageId() + "\",");
        if (getMessageId() == ONUConstants.DEFAULT_MESSAGE_ID && getOnuDeviceName() == null) {
            payloadString.append("\"" + "filters" + "\"" + ONUConstants.COLON + ONUConstants.GET_FILTER);
        }
        payloadString.replace(payloadString.length() - 1, payloadString.length(), "}");

        StringBuffer labelsJsonString = new StringBuffer("{");
        m_labels.forEach((name, value) -> {
            labelsJsonString.append("\"" + name + "\"" + ONUConstants.COLON + "\"" + value + "\",");
        });
        labelsJsonString.replace(labelsJsonString.length() - 1, labelsJsonString.length(), "}");
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put(ONUConstants.PAYLOAD_JSON_KEY, payloadString);
        requestMap.put(ONUConstants.ONU_NAME_JSON_KEY, m_onuDeviceName);
        if (m_oltDeviceName != null) {
            requestMap.put(ONUConstants.OLT_NAME_JSON_KEY, m_oltDeviceName);
        }
        if (m_chnlTermRef != null) {
            requestMap.put(ONUConstants.CHANNEL_TERMINATION_REF_JSON_KEY, m_chnlTermRef);
        }
        if (m_onuId != null) {
            requestMap.put(ONUConstants.ONU_ID_JSON_KEY, m_onuId);
        }
        requestMap.put(ONUConstants.EVENT, getEvent());
        requestMap.put(ONUConstants.LABELS_JSON_KEY, labelsJsonString);
        JSONObject requestJSON = new JSONObject(requestMap);
        return requestJSON.toString(ONUConstants.JSON_INDENT_FACTOR);
    }

}

