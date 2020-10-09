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

package org.broadband_forum.obbaa.protocol.snmp;

import static org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer.getNetconfResponse;
import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.stringToDocument;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.broadband_forum.obbaa.device.adapter.DeviceInterface;
import org.broadband_forum.obbaa.device.adapter.util.FutureNetconfReponse;
import org.broadband_forum.obbaa.device.listener.RegisterTrapCallback;
import org.broadband_forum.obbaa.dmyang.entities.ConnectionState;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.CopyConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigElement;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemValidationException;
import org.broadband_forum.obbaa.pma.NonNCNotificationHandler;

import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;


public class ProtocolTranslDevInterface implements DeviceInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolTranslDevInterface.class);
    private static final String NAME = "name";
    private Bundle m_bundle;
    public static final String OK_RESPONSE = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n"
            + "  <ok/>\n"
            + "</rpc-reply>";
    private SnmpTransport m_snmp = null;
    private final NonNCNotificationHandler m_nonNCNotificationHandler;
    private final RegisterTrapCallback m_trapHandle;

    public ProtocolTranslDevInterface(Bundle bundle, NonNCNotificationHandler nonNCNotificationHandler, RegisterTrapCallback trapListener) {
        m_bundle = bundle;
        m_nonNCNotificationHandler = nonNCNotificationHandler;
        m_trapHandle = trapListener;
    }

    @Override
    public Future<NetConfResponse> align(Device device, EditConfigRequest request, NetConfResponse getConfigResponse)
            throws ExecutionException {
        if (isConnected(device)) {
            Mapper mapper = new Mapper(m_snmp);
            mapper.mapAndSendSnmp(request);
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
    public void veto(Device device, EditConfigRequest request, Document oldDataStore, Document updatedDataStore)
            throws SubSystemValidationException {
    }

    @Override
    public Future<NetConfResponse> getConfig(Device device, GetConfigRequest getConfigRequest) throws ExecutionException {
        return null;
    }

    private boolean isConnected(Device device) {
        boolean ret = true;
        try {
            if (m_snmp == null) {
                m_snmp = new SnmpTransport(device,m_nonNCNotificationHandler,m_trapHandle);
            }
        } catch (Exception e) {
            ret = false;
            LOGGER.error("Eror when creating snmp info", e);
            throw new RuntimeException("Error when creating snmp info", e);
        }
        return ret;
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
        // TODO as of now we are using this function to only return success.
        // Eventually this function should be removed and return should be done
        // ar align directly.
        try {
            Document responseDocument = stringToDocument(OK_RESPONSE);
            final NetConfResponse testResponse = getNetconfResponse(responseDocument);
            return new FutureNetconfReponse(testResponse);
        } catch (NetconfMessageBuilderException e) {
            LOGGER.error("Error while aligning with device");
            throw new RuntimeException("Error while aligning with device", e);
        }
    }

    /**
     * This is a dummy implementation when a notification is received. When a notification is received, the notification must be
     * converted to a netconf notification. This notification along with ip and string of the device should call
     * m_nonNCNotificationHandler.handleNotification().
     * @param ip : ip of the device from which notification is received
     * @param port : port of the device from which notification is received
     * @param notification : Notification received from the device
     */
    private void notificationReceived(String ip, String port, Notification notification) {
        m_nonNCNotificationHandler.handleNotification(ip, port, notification);
    }
}
