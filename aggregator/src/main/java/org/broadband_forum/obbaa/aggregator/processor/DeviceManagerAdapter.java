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

package org.broadband_forum.obbaa.aggregator.processor;

import java.util.HashMap;
import java.util.Map;

import org.broadband_forum.obbaa.aggregator.api.Aggregator;
import org.broadband_forum.obbaa.aggregator.api.DeviceManagementProcessor;
import org.broadband_forum.obbaa.aggregator.api.DispatchException;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetconfServer;
import org.w3c.dom.Document;

public class DeviceManagerAdapter implements DeviceManagementProcessor {
    Aggregator m_aggregator;
    NetconfServer m_dmNetconfServer;
    Map<String, DeviceAdapterInfo> m_deviceTypeMap;

    public DeviceManagerAdapter(Aggregator aggregator) {
        m_aggregator = aggregator;
        m_deviceTypeMap = new HashMap<>();
    }

    public void init() {
        m_aggregator.registerDeviceManager(this);
    }

    public void destroy() {
        m_aggregator.unregisterDeviceManager();
    }

    public NetconfServer getDmNetconfServer() {
        return m_dmNetconfServer;
    }

    public void setDmNetconfServer(NetconfServer dmNetconfServer) {
        m_dmNetconfServer = dmNetconfServer;
    }

    @Override
    public String processRequest(NetconfClientInfo clientInfo, String netconfRequest) throws DispatchException {
        Document document = AggregatorMessage.stringToDocument(netconfRequest);
        String messageId = NetconfMessageUtil.getMessageIdFromRpcDocument(document);
        NetConfResponse response = deviceManagement(clientInfo, document);
        response.setMessageId(messageId);

        //Device name is invalid in this processor
        return response.responseToString();
    }

    private NetConfResponse deviceManagement(NetconfClientInfo netconfClientInfo, Document document) throws DispatchException {
        String typeOfNetconfRequest = NetconfMessageUtil.getTypeOfNetconfRequest(document);
        NetConfResponse response = new NetConfResponse();

        try {
            switch (typeOfNetconfRequest) {
                case NetconfResources.DELETE_CONFIG:
                    m_dmNetconfServer.onDeleteConfig(netconfClientInfo, DocumentToPojoTransformer.getDeleteConfig(document), response);
                    break;

                case NetconfResources.EDIT_CONFIG:
                    m_dmNetconfServer.onEditConfig(netconfClientInfo, DocumentToPojoTransformer.getEditConfig(document), response);
                    break;

                case NetconfResources.GET:
                    m_dmNetconfServer.onGet(netconfClientInfo, DocumentToPojoTransformer.getGet(document), response);
                    break;

                case NetconfResources.GET_CONFIG:
                    m_dmNetconfServer.onGetConfig(netconfClientInfo, DocumentToPojoTransformer.getGetConfig(document), response);
                    break;

                default:
                    // Does not support
                    throw new DispatchException("Does not support the operation.");
            }
        } catch (NetconfMessageBuilderException ex) {
            throw new DispatchException(ex);
        }
        return response;
    }

    public void removeDeviceAdptInfo(String deviceName) {
        m_deviceTypeMap.remove(deviceName);
    }

    public void updateDeviceAdptInfo(String deviceName, DeviceAdapterInfo deviceAdapterInfo) {
        if (m_deviceTypeMap.get(deviceName) == null) {
            m_deviceTypeMap.put(deviceName, deviceAdapterInfo);
            return;
        }

        m_deviceTypeMap.replace(deviceName, deviceAdapterInfo);
    }

    private DeviceAdapterInfo getDeviceAdptInfo(String deviceName) {
        try {
            return m_deviceTypeMap.get(deviceName);
        } catch (NullPointerException | ClassCastException ex) {
            return new DeviceAdapterInfo();
        }
    }

    @Override
    public String getDeviceTypeByDeviceName(String deviceName) {
        DeviceAdapterInfo deviceAdapterInfo = getDeviceAdptInfo(deviceName);
        if (deviceAdapterInfo == null) {
            return null;
        }

        return deviceAdapterInfo.getType();
    }
}
