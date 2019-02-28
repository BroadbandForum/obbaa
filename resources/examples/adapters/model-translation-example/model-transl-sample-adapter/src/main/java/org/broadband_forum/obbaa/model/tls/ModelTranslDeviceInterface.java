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

package org.broadband_forum.obbaa.model.tls;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.device.adapter.DeviceInterface;
import org.broadband_forum.obbaa.dmyang.entities.ConnectionState;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.CopyConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigElement;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigErrorOptions;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigTestOptions;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ModelTranslDeviceInterface implements DeviceInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelTranslDeviceInterface.class);
    private NetconfConnectionManager m_ncm;

    public ModelTranslDeviceInterface(NetconfConnectionManager ncm) {
        this.m_ncm = ncm;
    }

    @Override
    public Future<NetConfResponse> align(Device device, EditConfigRequest request) throws ExecutionException {
        if (m_ncm.isConnected(device)) {
            try {
                NodeList listOfNodes = request.getConfigElement().getXmlElement().getChildNodes();
                List<Element> elementList = new ArrayList<>();
                for (int i = 0; i < listOfNodes.getLength(); i++) {
                    Element dataNode = (Element) listOfNodes.item(i);
                    if (dataNode.getLocalName().equalsIgnoreCase("interfaces")) {
                        for (Element element : DocumentUtils.getChildElements(listOfNodes.item(i))) {
                            if (element.getLocalName().equalsIgnoreCase("interface")) {
                                for (Element child : DocumentUtils.getChildElements(element)) {
                                    if (child.getLocalName().equalsIgnoreCase("description")) {
                                        element.removeChild(child);
                                    }
                                }
                            }
                        }
                    }
                    elementList.add(dataNode);
                }
                EditConfigRequest newRequest = new EditConfigRequest()
                        .setTargetRunning()
                        .setTestOption(EditConfigTestOptions.SET)
                        .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                        .setConfigElement(new EditConfigElement()
                                .setConfigElementContents(elementList));
                request.setMessageId(request.getMessageId());
                return m_ncm.executeNetconf(device.getDeviceName(), newRequest);
            } catch (NetconfMessageBuilderException e) {
                LOGGER.error("Error while aligning with device");
                throw new RuntimeException("Error while aligning with device");
            }
        } else {
            throw new IllegalStateException(String.format("Device not connected %s", device));
        }
    }

    @Override
    public Pair<AbstractNetconfRequest, Future<NetConfResponse>> forceAlign(Device device, NetConfResponse getConfigResponse)
            throws NetconfMessageBuilderException, ExecutionException {
        if (m_ncm.isConnected(device)) {
            CopyConfigRequest ccRequest = new CopyConfigRequest();
            EditConfigElement config = new EditConfigElement();
            config.setConfigElementContents(getConfigResponse.getDataContent());
            ccRequest.setSourceConfigElement(config.getXmlElement());
            ccRequest.setTargetRunning();
            Future<NetConfResponse> responseFuture = m_ncm.executeNetconf(device.getDeviceName(), ccRequest);
            return new Pair<>(ccRequest, responseFuture);
        } else {
            throw new IllegalStateException(String.format("Device not connected %s", device));
        }
    }

    @Override
    public Future<NetConfResponse> get(Device device, GetRequest getRequest) throws ExecutionException {
        if (m_ncm.isConnected(device)) {
            return m_ncm.executeNetconf(device, getRequest);
        } else {
            throw new IllegalStateException(String.format("Device not connected %s", device));
        }
    }

    @Override
    public void veto(Device device, EditConfigRequest request, Document dataStore) throws SubSystemValidationException {
        LOGGER.info("Inside veto of the dummy device adapter - just testing");
        try {
            String dataStoreString = DocumentUtils.documentToPrettyString(dataStore);
            if ((StringUtils.countMatches(dataStoreString, "<interface>") > 3)) {
                throw new SubSystemValidationException(new NetconfRpcError(
                        NetconfRpcErrorTag.OPERATION_FAILED, NetconfRpcErrorType.Application,
                        NetconfRpcErrorSeverity.Error, "The maximum instances which can be created "
                        + "for interfaces is 3"));
            }
            NodeList listOfNodes = request.getConfigElement().getXmlElement().getChildNodes();
            for (int i = 0; i < listOfNodes.getLength(); i++) {
                if (listOfNodes.item(i).getLocalName().equalsIgnoreCase("interfaces")) {
                    for (Element element : DocumentUtils.getChildElements(listOfNodes.item(i))) {
                        if (element.getLocalName().equalsIgnoreCase("interface")) {
                            for (Element children : DocumentUtils.getChildElements(element)) {
                                if (children.getLocalName().equalsIgnoreCase("name")) {
                                    if (children.getTextContent().length() > 10) {
                                        throw new SubSystemValidationException(new NetconfRpcError(
                                                NetconfRpcErrorTag.OPERATION_FAILED, NetconfRpcErrorType.Application,
                                                NetconfRpcErrorSeverity.Error, "The interface name should not exceed 10 characters"));
                                    }
                                }
                            }

                        }
                    }
                }
            }
        } catch (NetconfMessageBuilderException e) {
            LOGGER.error("error occurred duting veto", e);
            throw new SubSystemValidationException(new NetconfRpcError(
                    NetconfRpcErrorTag.OPERATION_FAILED, NetconfRpcErrorType.Application,
                    NetconfRpcErrorSeverity.Error, "Error occurred during veto"));
        }
    }

    @Override
    public Future<NetConfResponse> getConfig(Device device, GetConfigRequest getConfigRequest) throws ExecutionException {
        if (m_ncm.isConnected(device)) {
            return m_ncm.executeNetconf(device, getConfigRequest);
        } else {
            throw new IllegalStateException(String.format("Device not connected %s", device));
        }
    }

    @Override
    public ConnectionState getConnectionState(Device device) {
        return m_ncm.getConnectionState(device);
    }
}
