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

package org.broadband_forum.obbaa.pma;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;

/**
 * <p>
 * The Persisistence Management Agent Interface.
 * Provides primitives to do netconf on a Managed Device.
 * </p>
 */
public interface PmaRegistry {
    /**
     * Executes the given netconfRequest on the device with unique name deviceName.
     *
     * @param deviceName     - unique identifier of the device.
     * @param netconfRequest - Netconf request to be executed on the device.
     * @return - Netconf response for the given netconfRequest.
     * @throws IllegalArgumentException - If the device is not managed.
     * @throws IllegalStateException    - If the device is not connected.
     * @throws ExecutionException       - If there are exceptions during execution.
     */
    Map<NetConfResponse, List<Notification>> executeNC(String deviceName, String netconfRequest) throws IllegalArgumentException,
            IllegalStateException, ExecutionException;

    /**
     * Executes the given sessionTemplate with a PmaSession.
     *
     * @param deviceName       - unique identifier of the device.
     * @param sessionTemplate- the template to be executed.
     * @return - result of execution.
     * @throws IllegalArgumentException - If the device is not managed.
     * @throws IllegalStateException    - If the device is not connected.
     * @throws ExecutionException       - If there are exceptions during execution.
     */
    <RT> RT executeWithPmaSession(String deviceName, PmaSessionTemplate<RT> sessionTemplate)
            throws IllegalArgumentException, IllegalStateException, ExecutionException;

    /**
     * Executes a copy-config RPC from PMA into the connected device to get the device aligned.
     *
     * @param deviceName - Key of the device.
     * @throws ExecutionException - If there were problems during the execution.
     */
    void forceAlign(String deviceName) throws ExecutionException;

    /**
     * API to invoke the edit-config delta alignment on the device.
     *
     * @param deviceName - Key of the device.
     * @throws ExecutionException - If there were problems during the execution.
     */
    void align(String deviceName) throws ExecutionException;

    /**
     * API to get all the persist configuration of the specific device.
     *
     * @param deviceName - Key of the device.
     * @throws ExecutionException - If there were problems during the execution.
     */
    NetConfResponse getAllPersistCfg(String deviceName) throws ExecutionException;
}
