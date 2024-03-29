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

package org.broadband_forum.obbaa.connectors.sbi.netconf;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.broadband_forum.obbaa.dmyang.entities.ConnectionState;
import org.broadband_forum.obbaa.dmyang.entities.PmaResource;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;

/**
 * <p/>
 * Responsible for maintaining Netconf connections to "Managed Devices".
 * </p>
 * Created by kbhatk on 29/9/17.
 */
public interface NetconfConnectionManager {

    /**
     * Executes the given Netconf request on the specified device and returns a Future object to get the response.
     *
     * @param resource - Target device.
     * @param request    - Request to be executed.
     * @return - Future object to retrieve the result of execution.
     * @throws IllegalStateException - If the device is not already connected.
     * @throws ExecutionException    - If there are exceptions during execution.
     */
    Future<NetConfResponse> executeNetconf(PmaResource resource, AbstractNetconfRequest request)
            throws IllegalStateException, ExecutionException;

    /**
     * Executes the given Netconf request on the specified device and returns a Future object to get the response.
     *
     * @param deviceName - Key of the device.
     * @param request    - Request to be executed.
     * @return - Future object to retrieve the result of execution.
     * @throws IllegalStateException - If the device is not already connected.
     * @throws ExecutionException    - If there are exceptions during execution.
     */
    Future<NetConfResponse> executeNetconf(String deviceName, AbstractNetconfRequest request)
            throws IllegalStateException, ExecutionException;

    /**
     * Executes the given Netconf template on the specified device.
     *
     * @param resource      - MetaData of the device.
     * @param netconfTemplate - Template to be executed with session on the device,
     * @param <RT>            - Type of the result.
     * @return - Result of execution.
     * @throws IllegalStateException - If the device is not already connected.
     * @throws ExecutionException    - If there are exceptions during execution.
     */
    <RT> RT executeWithSession(PmaResource resource, NetconfTemplate<RT> netconfTemplate)
            throws IllegalStateException, ExecutionException;

    /**
     * Gives the current connection state of the device.
     *
     * @param resource - MetaData of the device.
     * @return - true if the device is connected, false otherwise.
     */
    boolean isConnected(PmaResource resource);

    /**
     * Returns the details of the existing connection to the device.
     *
     * @param resource - Key of the device.
     * @return - Connection state details.
     */
    ConnectionState getConnectionState(PmaResource resource);

    /**
     * Get list of "new devices" that have called home, but have not been managed in the data store.
     *
     * @return Returns the list of new devices.
     */
    List<NewDeviceInfo> getNewDevices();

    /**
     * Drops the connection to a "new device".
     *
     * @param duid - Unique identifier of the new device.
     */
    void dropNewDeviceConnection(String duid);

    void registerDeviceConnectionListener(ConnectionListener connectionListener);

    void unregisterDeviceConnectionListener(ConnectionListener connectionListener);

    void addMediatedDeviceNetconfSession(String deviceName, NetconfClientSession deviceSession);

    void addMediatedNetworkFunctionNetconfSession(String networkFunctionName,
                                                  NetconfClientSession networkFunctionSession);

    NetconfClientSession getMediatedDeviceSession(String deviceName);

    NetconfClientSession getMediatedNetworkFunctionSession(String networkFunctionName);

}
