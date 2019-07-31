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

package org.broadband_forum.obbaa.protocol.tls;

import static org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer.getNetconfResponse;
import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.stringToDocument;
import static org.broadband_forum.obbaa.protocol.tls.SftpFileTransferUtil.createSftpSession;
import static org.broadband_forum.obbaa.protocol.tls.SftpFileTransferUtil.transfer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.broadband_forum.obbaa.device.adapter.DeviceInterface;
import org.broadband_forum.obbaa.device.adapter.util.FutureNetconfReponse;
import org.broadband_forum.obbaa.dmyang.entities.Authentication;
import org.broadband_forum.obbaa.dmyang.entities.ConnectionState;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.CopyConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigElement;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemValidationException;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class ProtocolTranslDevInterface implements DeviceInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolTranslDevInterface.class);
    private static final String INTERFACES = "interfaces";
    private static final String INTERFACE = "interface";
    private static final String NAME = "name";
    private Bundle m_bundle;
    private static final String SFTP_PROPERTIES = "sftp.properties";
    private static final String FILE_SUFFIX = "_running_ds.xml";
    public static final String OK_RESPONSE = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n"
            + "  <ok/>\n"
            + "</rpc-reply>";
    private Session m_session = null;

    public ProtocolTranslDevInterface(Bundle bundle) {
        m_bundle = bundle;
    }

    @Override
    public Future<NetConfResponse> align(Device device, EditConfigRequest request, NetConfResponse getConfigResponse)
            throws ExecutionException {
        if (isConnected(device)) {
            return executeRequest(device);
        } else {
            throw new IllegalStateException(String.format("Device not connected %s", device.getDeviceName()));
        }
    }

    @Override
    public Pair<AbstractNetconfRequest, Future<NetConfResponse>> forceAlign(Device device, NetConfResponse getConfigResponse)
            throws NetconfMessageBuilderException, ExecutionException {
        if (isConnected(device)) {
            CopyConfigRequest ccRequest = new CopyConfigRequest();
            EditConfigElement config = new EditConfigElement();
            config.setConfigElementContents(getConfigResponse.getDataContent());
            ccRequest.setSourceConfigElement(config.getXmlElement());
            ccRequest.setTargetRunning();
            Document responseDocument = stringToDocument(OK_RESPONSE);
            final NetConfResponse testResponse = getNetconfResponse(responseDocument);
            Future<NetConfResponse> responseFuture = executeRequest(device);
            return new Pair<>(ccRequest, responseFuture);
        } else {
            throw new IllegalStateException(String.format("Device not connected %s", device.getDeviceName()));
        }
    }

    @Override
    public Future<NetConfResponse> get(Device device, GetRequest getRequest) throws ExecutionException {
        return null;
    }

    @Override
    public void veto(Device device, EditConfigRequest request, Document dataStore) throws SubSystemValidationException {
        try {
            String dataStoreString = DocumentUtils.documentToPrettyString(dataStore.getDocumentElement());
            if ((StringUtils.countMatches(dataStoreString, "<interface>") > 3)
                   || (StringUtils.countMatches(dataStoreString, "<if:interface>") > 3)) {
                throw new SubSystemValidationException(new NetconfRpcError(
                        NetconfRpcErrorTag.OPERATION_FAILED, NetconfRpcErrorType.Application,
                        NetconfRpcErrorSeverity.Error, "The maximum instances which can be created "
                        + "for " + INTERFACES + " 3"));
            }
            NodeList listOfNodes = request.getConfigElement().getXmlElement().getChildNodes();
            for (int i = 0; i < listOfNodes.getLength(); i++) {
                if (listOfNodes.item(i).getLocalName().equalsIgnoreCase(INTERFACES)) {
                    for (Element element : DocumentUtils.getChildElements(listOfNodes.item(i))) {
                        if (element.getLocalName().equalsIgnoreCase(INTERFACE)) {
                            for (Element children : DocumentUtils.getChildElements(element)) {
                                if (children.getLocalName().equalsIgnoreCase(NAME)) {
                                    if (children.getTextContent().length() > 10) {
                                        throw new SubSystemValidationException(new NetconfRpcError(
                                                NetconfRpcErrorTag.OPERATION_FAILED, NetconfRpcErrorType.Application,
                                                NetconfRpcErrorSeverity.Error, INTERFACE + " name should not exceed 10 characters"));
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
        return null;
    }

    private boolean isConnected(Device device) {
        if (!device.isCallhome()) {
            SftpTargetInfo sftpTargetInfo = createSftpInfo(device);
            try {
                m_session = createSftpSession(sftpTargetInfo.getServerAddress(), sftpTargetInfo.getUserName(),
                        sftpTargetInfo.getPassword(), sftpTargetInfo.getPortNumber());
                return m_session != null;
            } catch (JSchException e) {
                LOGGER.error("error while creating sftp session", e);
            }
            return false;
        } else {
            LOGGER.warn(String.format("Sftp does not support callhome devices, hence returning connection "
                    + "status as false for device %s", device.getDeviceName()));
            return false;
        }
    }

    @NotNull
    private SftpTargetInfo createSftpInfo(Device device) {
        try {
            Authentication auth = device.getDeviceManagement().getDeviceConnection().getPasswordAuth().getAuthentication();
            Properties prop = new Properties();
            try (InputStream stream = m_bundle.getResource(SFTP_PROPERTIES).openStream()) {
                prop.load(stream);
            } catch (IOException e) {
                LOGGER.error("Error during IO operation for file " + SFTP_PROPERTIES);
            }
            return new SftpTargetInfo(auth.getAddress(), auth.getUsername(), auth.getPassword(),
                    prop.getProperty("remoteDir"), new File(prop.getProperty("localDir")
                    + device.getDeviceName() + FILE_SUFFIX), Integer.parseInt(auth.getManagementPort()));
        } catch (Exception e) {
            LOGGER.error("Eror when creating sftp info", e);
            throw new RuntimeException("Error when creating sftp info", e);
        }
    }

    @Override
    public ConnectionState getConnectionState(Device device) {
        ConnectionState connectionState = new ConnectionState();
        connectionState.setConnected(isConnected(device));
        if (connectionState.isConnected()) {
            connectionState.setConnectionCreationTime(new Date());
        }
        return connectionState;
    }

    @Override
    public Notification normalizeNotification(Notification notification) {
        return notification;
    }

    private Future<NetConfResponse> executeRequest(Device device) throws IllegalStateException, ExecutionException {
        try {
            transfer(createSftpInfo(device), m_session);
        } catch (IOException | SftpException | JSchException e) {
            LOGGER.error("error while performing ftp transfer", e);
            throw new RuntimeException(e);
        }
        try {
            Document responseDocument = stringToDocument(OK_RESPONSE);
            final NetConfResponse testResponse = getNetconfResponse(responseDocument);
            return new FutureNetconfReponse(testResponse);
        } catch (NetconfMessageBuilderException e) {
            LOGGER.error("Error while aligning with device");
            throw new RuntimeException("Error while aligning with device");
        }
    }
}
