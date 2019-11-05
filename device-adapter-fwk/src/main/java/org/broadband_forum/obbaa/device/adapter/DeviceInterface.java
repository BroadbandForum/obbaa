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

package org.broadband_forum.obbaa.device.adapter;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.broadband_forum.obbaa.dmyang.entities.ConnectionState;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemValidationException;
import org.w3c.dom.Document;

public interface DeviceInterface {
    /**
     * Send an edit-config request for a device at the SBI side.
     *
     * @param device  Device for which the request is dedicated
     * @param request the edit-config request
     * @param getConfigResponse the get-config response from PMA data-store for device
     * @return future response
     * @throws ExecutionException throws Execution Exception
     */
    Future<NetConfResponse> align(Device device, EditConfigRequest request, NetConfResponse getConfigResponse)
            throws ExecutionException;

    /**
     * Send request to device on first contact scenario at SBI side.
     *
     * @param device Device for which the request is dedicated
     * @param getConfigResponse get-config response from PMA data-store for device
     * @return pair of request and response
     * @throws NetconfMessageBuilderException throws Netconf Message builder exception
     * @throws ExecutionException             throws Execution Exception
     */
    Pair<AbstractNetconfRequest, Future<NetConfResponse>> forceAlign(Device device, NetConfResponse getConfigResponse)
            throws NetconfMessageBuilderException, ExecutionException;

    /**
     * Send a get request to device at SBI Side.
     *
     * @param device     Device for which the request is dedicated
     * @param getRequest The filtered get request for device
     * @return future response
     * @throws ExecutionException throws Execution Exception
     */
    Future<NetConfResponse> get(Device device, GetRequest getRequest) throws ExecutionException;

    /**
     * Veto changes if needed based on adapter specific rules.
     * @param device    Device for which the request is dedicated
     * @param request   request which needs to validated based on rules
     * @param oldDataStore the PMA data-store for the device as before the edit-config
     * @param updatedDataStore the current PMA data-store for the device
     */
    void veto(Device device, EditConfigRequest request, Document oldDataStore, Document updatedDataStore)
            throws SubSystemValidationException;

    /**
     * Send a get-config request to device at SBI Side.
     *
     * @param device           Device for which the request is dedicated
     * @param getConfigRequest The filtered get-config request for device
     * @return future response
     * @throws ExecutionException throws Execution Exception
     */
    Future<NetConfResponse> getConfig(Device device, GetConfigRequest getConfigRequest) throws ExecutionException;

    /**
     * Get the connection state for the device based on the protocol.
     *
     * @param device Device for which the request is dedicated
     * @return Connection State of the device
     */
    ConnectionState getConnectionState(Device device);

    /**
     * Normalize the vendor specific notification coming from the device to the supported notification format.
     *
     * @param notification the notification from the device to be normalized
     * @return the normalized notification
     */
    Notification normalizeNotification(Notification notification);
}
