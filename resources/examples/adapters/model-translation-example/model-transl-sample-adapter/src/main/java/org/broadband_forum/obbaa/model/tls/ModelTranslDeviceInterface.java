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
import org.broadband_forum.obbaa.netconf.alarm.util.AlarmConstants;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.CopyConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigElement;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigErrorOptions;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigTestOptions;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfNotification;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemValidationException;
import org.opendaylight.yangtools.yang.common.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ModelTranslDeviceInterface implements DeviceInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelTranslDeviceInterface.class);
    private NetconfConnectionManager m_ncm;
    public static final String IETF_ALARM_NS = "urn:ietf:params:xml:ns:yang:ietf-alarms";

    public ModelTranslDeviceInterface(NetconfConnectionManager ncm) {
        this.m_ncm = ncm;
    }

    @Override
    public Future<NetConfResponse> align(Device device, EditConfigRequest request, NetConfResponse getConfigResponse)
            throws ExecutionException {
        if (m_ncm.isConnected(device)) {
            try {
                String dataStoreString = DocumentUtils.documentToPrettyString(getConfigResponse.getData());
                /* String check is done in this class because it is just a sample VDA. Essentially DOM/Xpath
                 evaluation should be used */
                int interfaceCount = StringUtils.countMatches(dataStoreString, "<interface>");
                interfaceCount = interfaceCount + StringUtils.countMatches(dataStoreString, "<if:interface>");
                LOGGER.info(String.format("The current PMA datastore interface count for device %s is : %s", device.getDeviceName(),
                        interfaceCount));
                NodeList listOfNodes = request.getConfigElement().getXmlElement().getChildNodes();
                EditConfigRequest newRequest = new EditConfigRequest()
                        .setTargetRunning()
                        .setTestOption(EditConfigTestOptions.SET)
                        .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                        .setConfigElement(new EditConfigElement()
                        .setConfigElementContents(translate(listOfNodes)));
                request.setMessageId(request.getMessageId());
                return m_ncm.executeNetconf(device.getDeviceName(), newRequest);
            } catch (NetconfMessageBuilderException e) {
                LOGGER.error("Error while aligning device", e);
                throw new RuntimeException("Error while aligning device", e);
            }
        } else {
            throw new IllegalStateException(String.format("Device %s is not connected", device.getDeviceName()));
        }
    }

    @Override
    public Pair<AbstractNetconfRequest, Future<NetConfResponse>> forceAlign(Device device, NetConfResponse getConfigResponse)
            throws NetconfMessageBuilderException, ExecutionException {
        if (m_ncm.isConnected(device)) {
            CopyConfigRequest ccRequest = new CopyConfigRequest();
            EditConfigElement config = new EditConfigElement();
            config.setConfigElementContents(translate(getConfigResponse.getData().getChildNodes()));
            ccRequest.setSourceConfigElement(config.getXmlElement());
            ccRequest.setTargetRunning();
            Future<NetConfResponse> responseFuture = m_ncm.executeNetconf(device.getDeviceName(), ccRequest);
            return new Pair<>(ccRequest, responseFuture);
        } else {
            throw new IllegalStateException(String.format("Device %s is not connected", device.getDeviceName()));
        }
    }

    private List<Element> translate(NodeList listOfNodes) {
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
        return elementList;
    }

    @Override
    public Future<NetConfResponse> get(Device device, GetRequest getRequest) throws ExecutionException {
        if (m_ncm.isConnected(device)) {
            return m_ncm.executeNetconf(device, getRequest);
        } else {
            throw new IllegalStateException(String.format("Device %s is not connected", device.getDeviceName()));
        }
    }

    @Override
    public void veto(Device device, EditConfigRequest request, Document oldDataStore, Document updatedDataStore)
            throws SubSystemValidationException {
        try {
            String dataStoreString = DocumentUtils.documentToPrettyString(updatedDataStore.getDocumentElement());
            /* String check is done in this class because it is just a sample VDA. Essentially a DOM/Xpath
                 evaluation should be used */
            if ((StringUtils.countMatches(dataStoreString, "<interface>") > 3)
                    || (StringUtils.countMatches(dataStoreString, "<if:interface>") > 3)) {
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
            throw new IllegalStateException(String.format("Device %s is not connected", device.getDeviceName()));
        }
    }

    @Override
    public ConnectionState getConnectionState(Device device) {
        return m_ncm.getConnectionState(device);
    }

    @Override
    public Notification normalizeNotification(Notification notification) {
        //check the notification type is alarm-notification
        QName alarmNotificationQname = QName.create(IETF_ALARM_NS, "alarm-notification");
        if (alarmNotificationQname.equals(notification.getType())) {
            return convertAlarm(notification);
        }
        return null;
    }

    private Notification convertAlarm(Notification notification) {
        NodeList list = notification.getNotificationElement().getChildNodes();
        QName severityQName = QName.create(IETF_ALARM_NS, AlarmConstants.ALARM_PERCEIVED_SEVERITY);
        QName alarmTextQName = QName.create(IETF_ALARM_NS, AlarmConstants.ALARM_TEXT);
        //change the severity and alarm-text -> to test that normalization works
        for (int index = 0; index < list.getLength(); index++) {
            Node innerNode = list.item(index);
            if (innerNode.getNodeType() == Node.ELEMENT_NODE) {
                Element innerNodeEle = (Element) innerNode;
                String innerNodeName = innerNodeEle.getLocalName();
                QName attributeQName = QName.create(innerNodeEle.getNamespaceURI(), innerNodeName);
                if (attributeQName.equals(severityQName)) {
                    if ("major".equals(innerNodeEle.getTextContent().trim())) {
                        innerNodeEle.setTextContent("warning");
                    }
                } else if (attributeQName.equals(alarmTextQName)) {
                    if ("raisealarm".equalsIgnoreCase(innerNodeEle.getTextContent().trim())) {
                        innerNodeEle.setTextContent("raiseAlarm :: Normalized Vendor alarm");
                    }
                }
            }
        }
        NetconfNotification normalizedNotification = null;
        try {
            normalizedNotification = new NetconfNotification(notification.getNotificationDocument());
        } catch (NetconfMessageBuilderException e) {
            LOGGER.error("error while forming netconf notification", e);
        }
        LOGGER.info(String.format("the Normalized alarm is %s: ", notification.notificationToPrettyString()));
        return normalizedNotification;
    }
}
