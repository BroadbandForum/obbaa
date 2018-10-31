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

package org.broadband_forum.obbaa.aggregator.api;

import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;

/**
 * BAA Core API for NBI. It's used for message forwarding and notification listening.
 * The maximum number of request processors and notification processors is 100
 */
public interface Aggregator {

    /**
     * Provide a unified API to NBI for request message forwarding.
     *
     * @param clientInfo Client Info
     * @param netconfRequest Message defined with YANG from NBI.
     * @return Result
     * @throws DispatchException Dispatch exception
     */
    String dispatchRequest(NetconfClientInfo clientInfo, String netconfRequest) throws DispatchException;

    /**
     * Provide a unified API for notification publication.
     *
     * @param notificationMessage Notification message
     * @throws DispatchException Dispatch exception
     */
    void publishNotification(String notificationMessage) throws DispatchException;

    /**
     * Provide a unified API to PMA/DM for notification publish.
     *
     * @param deviceName          Device name
     * @param notificationMessage Notification message
     * @throws DispatchException Dispatch exception
     */
    void publishNotification(String deviceName, String notificationMessage) throws DispatchException;

    /**
     * Provide a unified API to message processor(YANG library...) register themselves.
     *
     * @param moduleIdentifiers      YANG model paths
     * @param globalRequestProcessor The instance of request processor
     * @throws DispatchException Dispatch exception
     */
    void addProcessor(Set<ModuleIdentifier> moduleIdentifiers,
                      GlobalRequestProcessor globalRequestProcessor) throws DispatchException;

    /**
     * Provide a unified API to message processor(PMA/DM) register themselves.
     *
     * @param deviceType            Device type
     * @param moduleIdentifiers     YANG model paths
     * @param deviceConfigProcessor The instance of message processor
     * @throws DispatchException Dispatch exception
     */
    void addProcessor(String deviceType, Set<ModuleIdentifier> moduleIdentifiers, DeviceConfigProcessor deviceConfigProcessor)
        throws DispatchException;

    /**
     * Provide a unified API to NAI for notification provider registry.
     *
     * @param notificationProcessor NAI notification processor
     * @throws DispatchException Dispatch exception
     */
    void addProcessor(NotificationProcessor notificationProcessor) throws DispatchException;

    /**
     * Provide a unified API to message processor(YANG library...) unregister themselves.
     *
     * @param globalRequestProcessor The instance of message processor like Schema-mount etc.
     * @throws DispatchException Dispatch exception
     */
    void removeProcessor(GlobalRequestProcessor globalRequestProcessor) throws DispatchException;

    /**
     * Provide a unified API to message processor(PMA/DM) unregister themselves.
     *
     * @param deviceConfigProcessor The instance of message processor like Device Manager etc.
     * @throws DispatchException Dispatch exception
     */
    void removeProcessor(DeviceConfigProcessor deviceConfigProcessor) throws DispatchException;

    /**
     * Provide a unified API to message processor(PMA/DM) unregister themselves.
     *
     * @param notificationProcessor The instance of message processor like Device Manager etc.
     * @throws DispatchException Dispatch exception
     */
    void removeProcessor(NotificationProcessor notificationProcessor) throws DispatchException;

    /**
     * Provide a unified API to message processor(PMA/DM) unregister themselves.
     *
     * @param deviceType            Device type
     * @param moduleIdentifiers     YANG model namespaces
     * @param deviceConfigProcessor The instance of message processor
     * @throws DispatchException Dispatch exception
     */
    void removeProcessor(String deviceType, Set<ModuleIdentifier> moduleIdentifiers, DeviceConfigProcessor deviceConfigProcessor)
        throws DispatchException;

    /**
     * Register device management processor.
     *
     * @param deviceManagementProcessor Device manager processor
     */
    void registerDeviceManager(DeviceManagementProcessor deviceManagementProcessor);

    /**
     * Unregister device management processor.
     */
    void unregisterDeviceManager();

    /**
     * Get all capabilities of all processors.
     *
     * @return Capabilities
     */
    Set<ProcessorCapability> getProcessorCapabilities();

    /**
     * Get all modules of all processors.
     *
     * @return All of the modules
     */
    Set<ModuleIdentifier> getModuleIdentifiers();

    /**
     * Get the YANG module capabilities of OB-BAA system.
     *
     * @return YANG modules
     */
    Set<String> getSystemCapabilities();
}