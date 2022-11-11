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
package org.broadband_forum.obbaa.nm.nwfunctionmgr;

import static org.broadband_forum.obbaa.nf.entities.NetworkFunctionNSConstants.NETWORK_FUNCTION;
import static org.broadband_forum.obbaa.nf.entities.NetworkFunctionNSConstants.NETWORK_FUNCTIONS_ID_TEMPLATE;
import static org.broadband_forum.obbaa.nm.nwfunctionmgr.NetworkFunctionConstants.ADMIN_STATE;
import static org.broadband_forum.obbaa.nm.nwfunctionmgr.NetworkFunctionConstants.ADMIN_STATE_NAME;
import static org.broadband_forum.obbaa.nm.nwfunctionmgr.NetworkFunctionConstants.BAA_MICRO_SERVICE_DISCOVERY_API_ADDRESS;
import static org.broadband_forum.obbaa.nm.nwfunctionmgr.NetworkFunctionConstants.BBF_BAA_NFSTATE;
import static org.broadband_forum.obbaa.nm.nwfunctionmgr.NetworkFunctionConstants.GET_NETWORK_FUNCTION_INSTANCES_URI_PATH;
import static org.broadband_forum.obbaa.nm.nwfunctionmgr.NetworkFunctionConstants.GET_NETWORK_FUNCTION_URI_PATH;
import static org.broadband_forum.obbaa.nm.nwfunctionmgr.NetworkFunctionConstants.HTTP;
import static org.broadband_forum.obbaa.nm.nwfunctionmgr.NetworkFunctionConstants.NAME;
import static org.broadband_forum.obbaa.nm.nwfunctionmgr.NetworkFunctionConstants.NETWORK_FUNCTION_STATE;
import static org.broadband_forum.obbaa.nm.nwfunctionmgr.NetworkFunctionConstants.NETWORK_FUNCTION_STATE_NAMESPACE;
import static org.broadband_forum.obbaa.nm.nwfunctionmgr.NetworkFunctionConstants.OPER_STATE;
import static org.broadband_forum.obbaa.nm.nwfunctionmgr.NetworkFunctionConstants.OPER_STATE_NAME;
import static org.broadband_forum.obbaa.nm.nwfunctionmgr.NetworkFunctionConstants.SOFTWARE_VERSION;
import static org.broadband_forum.obbaa.nm.nwfunctionmgr.NetworkFunctionConstants.SOFTWARE_VERSION_NAME;
import static org.broadband_forum.obbaa.nm.nwfunctionmgr.NetworkFunctionConstants.VIRTUAL_NETWORK_FUNCTION;
import static org.broadband_forum.obbaa.nm.nwfunctionmgr.NetworkFunctionConstants.VIRTUAL_NETWORK_FUNCTION_INSTANCE;
import static org.broadband_forum.obbaa.nm.nwfunctionmgr.NetworkFunctionConstants.VIRTUAL_NETWORK_FUNCTION_INSTANCE_NAME;
import static org.broadband_forum.obbaa.nm.nwfunctionmgr.NetworkFunctionConstants.VIRTUAL_NETWORK_FUNCTION_NAME;
import static org.broadband_forum.obbaa.nm.nwfunctionmgr.NetworkFunctionConstants.VIRTUAL_NETWORK_FUNCTION_TYPE;
import static org.broadband_forum.obbaa.nm.nwfunctionmgr.NetworkFunctionConstants.VIRTUAL_NETWORK_FUNCTION_TYPE_NAME;
import static org.broadband_forum.obbaa.nm.nwfunctionmgr.NetworkFunctionConstants.VIRTUAL_NETWORK_FUNCTION_VENDOR;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.broadband_forum.obbaa.connectors.sbi.netconf.impl.SystemPropertyUtils;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AbstractSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ChangeNotification;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigChangeNotification;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeChangeType;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opendaylight.yangtools.yang.common.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class NetworkFunctionManagementSubsystem extends AbstractSubSystem {


    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkFunctionManagementSubsystem.class);
    private NetworkFunctionManager m_networkFunctionManager;

    public NetworkFunctionManagementSubsystem(NetworkFunctionManager networkFunctionManager) {
        m_networkFunctionManager = networkFunctionManager;
    }

    @Override
    public void notifyChanged(List<ChangeNotification> changeNotificationList) {
        LOGGER.debug("notification received : {}", changeNotificationList);
        for (ChangeNotification notification : changeNotificationList) {
            EditConfigChangeNotification editNotif = (EditConfigChangeNotification) notification;
            LOGGER.debug("notification received : {}", editNotif);
            ModelNodeId nodeId = editNotif.getModelNodeId();
            if (nodeId.equals(NETWORK_FUNCTIONS_ID_TEMPLATE)
                    && NETWORK_FUNCTION.equals(editNotif.getChange().getChangeData().getName())) {
                LOGGER.debug("ModelNodeId[{}] matched network function template", nodeId);
                handleNwFunctionCreateOrDelete(nodeId, editNotif);
            }
        }
    }

    private void handleNwFunctionCreateOrDelete(ModelNodeId nodeId, EditConfigChangeNotification editNotif) {
        LOGGER.debug(null, "Handle Network function create or delete for ModelNodeId[{}] with notification[{}]", nodeId, editNotif);
        String networkFunctionName = editNotif.getChange().getChangeData().getMatchNodes().get(0).getValue();
        if (editNotif.getChange().getChangeType().equals(ModelNodeChangeType.create)) {
            LOGGER.debug(null, "Network Function create identified for  ModelNodeId[{}] with notification[{}]", nodeId, editNotif);
            m_networkFunctionManager.networkFunctionAdded(networkFunctionName);
        } else if (editNotif.getChange().getChangeType().equals(ModelNodeChangeType.delete)
                || editNotif.getChange().getChangeType().equals(ModelNodeChangeType.remove)) {
            LOGGER.debug(null, "Network Function delete identified for ModelNodeId[{}] with notification[{}]", nodeId, editNotif);
            m_networkFunctionManager.networkFunctionRemoved(networkFunctionName);
        }
    }

    @Override
    protected Map<ModelNodeId, List<Element>> retrieveStateAttributes(Map<ModelNodeId, Pair<List<QName>,
            List<FilterNode>>> attributes) throws GetAttributeException {
        Map<ModelNodeId, List<Element>> stateInfo = new HashMap<>();
        for (Map.Entry<ModelNodeId, Pair<List<QName>, List<FilterNode>>> entry : attributes.entrySet()) {
            if (attributes.entrySet().toString().contains(VIRTUAL_NETWORK_FUNCTION_INSTANCE)) {
                String responseString = getNetworkFunctionInstanceDetails();
                stateInfo.put(entry.getKey(), listNetworkFunctionInstancesElement(getJsonObjectListFormString(responseString)));
            } else {
                String responseString = getNetworkFunctionDetails();
                if (responseString != null) {
                    stateInfo.put(entry.getKey(), listNetworkFunctionElement(getJsonObjectListFormString(responseString)));
                }
            }
        }
        return stateInfo;
    }

    private String getNetworkFunctionDetails() {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet(HTTP + SystemPropertyUtils.getInstance()
                .getFromEnvOrSysProperty(BAA_MICRO_SERVICE_DISCOVERY_API_ADDRESS)
                + GET_NETWORK_FUNCTION_URI_PATH);
        CloseableHttpResponse response = null;
        String responseString = null;
        try {
            response = client.execute(request);
            responseString = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            LOGGER.error("failed to get response, Please verify BAA_HOST_IP: ", e);
        }
        return responseString;
    }

    private String getNetworkFunctionInstanceDetails() {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet(HTTP + SystemPropertyUtils.getInstance()
                .getFromEnvOrSysProperty(BAA_MICRO_SERVICE_DISCOVERY_API_ADDRESS)
                + GET_NETWORK_FUNCTION_INSTANCES_URI_PATH);
        CloseableHttpResponse response = null;
        String responseString = null;
        try {
            response = client.execute(request);
            responseString = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            LOGGER.error("failed to get response", e);
        }
        return responseString;
    }

    private List<JSONObject> getJsonObjectListFormString(String jsonString) {
        List<JSONObject> jsonObjectList = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(jsonString);
        for (int jsonArrayIndex = 0; jsonArrayIndex < jsonArray.length(); jsonArrayIndex++) {
            jsonObjectList.add((JSONObject) jsonArray.get(jsonArrayIndex));
        }
        return jsonObjectList;
    }

    private List<Element> listNetworkFunctionElement(List<JSONObject> responseObjectList) {
        Document document = DocumentUtils.createDocument();

        Element networkFunctionState = document.createElementNS(NETWORK_FUNCTION_STATE_NAMESPACE, NETWORK_FUNCTION_STATE);
        networkFunctionState.setPrefix(BBF_BAA_NFSTATE);

        for (JSONObject responseObject : responseObjectList) {
            Element virtualNetworkFunction = document.createElementNS(NETWORK_FUNCTION_STATE_NAMESPACE, VIRTUAL_NETWORK_FUNCTION);
            virtualNetworkFunction.setPrefix(BBF_BAA_NFSTATE);
            networkFunctionState.appendChild(virtualNetworkFunction);

            Element virtualNetworkFunctionName = document.createElementNS(NETWORK_FUNCTION_STATE_NAMESPACE, NAME);
            virtualNetworkFunctionName.setTextContent((String) responseObject.get(NAME));
            virtualNetworkFunctionName.setPrefix(BBF_BAA_NFSTATE);
            virtualNetworkFunction.appendChild(virtualNetworkFunctionName);

            Element virtualNetworkFunctionVendor = document.createElementNS(NETWORK_FUNCTION_STATE_NAMESPACE,
                    VIRTUAL_NETWORK_FUNCTION_VENDOR);
            virtualNetworkFunctionVendor.setTextContent((String) responseObject.get(VIRTUAL_NETWORK_FUNCTION_VENDOR));
            virtualNetworkFunctionVendor.setPrefix(BBF_BAA_NFSTATE);
            virtualNetworkFunction.appendChild(virtualNetworkFunctionVendor);

            Element virtualNetworkFunctionSoftwareVersion = document.createElementNS(NETWORK_FUNCTION_STATE_NAMESPACE, SOFTWARE_VERSION);
            virtualNetworkFunctionSoftwareVersion.setTextContent((String) responseObject.get(SOFTWARE_VERSION_NAME));
            virtualNetworkFunctionSoftwareVersion.setPrefix(BBF_BAA_NFSTATE);
            virtualNetworkFunction.appendChild(virtualNetworkFunctionSoftwareVersion);

            Element virtualNetworkFunctionType = document.createElementNS(NETWORK_FUNCTION_STATE_NAMESPACE, VIRTUAL_NETWORK_FUNCTION_TYPE);
            virtualNetworkFunctionType.setTextContent((String) responseObject.get(VIRTUAL_NETWORK_FUNCTION_TYPE_NAME));
            virtualNetworkFunctionType.setPrefix(BBF_BAA_NFSTATE);
            virtualNetworkFunction.appendChild(virtualNetworkFunctionType);
        }

        List<Element> elementList = new ArrayList<>();
        elementList.add(networkFunctionState);
        return elementList;
    }

    private List<Element> listNetworkFunctionInstancesElement(List<JSONObject> responseObjectList) {
        Document document = DocumentUtils.createDocument();

        Element networkFunctionState = document.createElementNS(NETWORK_FUNCTION_STATE_NAMESPACE, NETWORK_FUNCTION_STATE);
        networkFunctionState.setPrefix(BBF_BAA_NFSTATE);

        for (JSONObject responseObject : responseObjectList) {
            Element virtualNetworkFunctionInstance = document.createElementNS(NETWORK_FUNCTION_STATE_NAMESPACE,
                    VIRTUAL_NETWORK_FUNCTION_INSTANCE);
            virtualNetworkFunctionInstance.setPrefix(BBF_BAA_NFSTATE);
            networkFunctionState.appendChild(virtualNetworkFunctionInstance);

            Element virtualNetworkFunctionName = document.createElementNS(NETWORK_FUNCTION_STATE_NAMESPACE, NAME);
            virtualNetworkFunctionName.setTextContent((String) responseObject.get(VIRTUAL_NETWORK_FUNCTION_INSTANCE_NAME));
            virtualNetworkFunctionName.setPrefix(BBF_BAA_NFSTATE);
            virtualNetworkFunctionInstance.appendChild(virtualNetworkFunctionName);

            Element virtualNetworkFunctionElement = document.createElementNS(NETWORK_FUNCTION_STATE_NAMESPACE,
                    VIRTUAL_NETWORK_FUNCTION);
            virtualNetworkFunctionElement.setTextContent((String) responseObject.get(VIRTUAL_NETWORK_FUNCTION_NAME));
            virtualNetworkFunctionElement.setPrefix(BBF_BAA_NFSTATE);
            virtualNetworkFunctionInstance.appendChild(virtualNetworkFunctionElement);

            Element adminStateElement = document.createElementNS(NETWORK_FUNCTION_STATE_NAMESPACE, ADMIN_STATE);
            adminStateElement.setTextContent((String) responseObject.get(ADMIN_STATE_NAME));
            adminStateElement.setPrefix(BBF_BAA_NFSTATE);
            virtualNetworkFunctionInstance.appendChild(adminStateElement);

            Element operStateElement = document.createElementNS(NETWORK_FUNCTION_STATE_NAMESPACE, OPER_STATE);
            operStateElement.setTextContent((String) responseObject.get(OPER_STATE_NAME));
            operStateElement.setPrefix(BBF_BAA_NFSTATE);
            virtualNetworkFunctionInstance.appendChild(operStateElement);
        }

        List<Element> elementList = new ArrayList<>();
        elementList.add(networkFunctionState);
        return elementList;
    }
}
