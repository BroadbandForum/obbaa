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

package org.broadband_forum.obbaa.aggregator.registrant.api;

import org.broadband_forum.obbaa.aggregator.api.DeviceConfigProcessor;
import org.broadband_forum.obbaa.aggregator.api.DispatchException;
import org.broadband_forum.obbaa.aggregator.api.GlobalRequestProcessor;
import org.broadband_forum.obbaa.aggregator.api.ProcessorCapability;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ModuleIdentifier;

import java.util.Map;
import java.util.Set;

public interface RequestProcessorManager {
    /**
     * Provide a unified API to message processor(YANG library...) register themselves.
     *
     * @param moduleIdentifiers YANG model paths
     * @param globalRequestProcessor The instance of request processor
     * @throws DispatchException Dispatch exception
     */
    void addProcessor(Set<ModuleIdentifier> moduleIdentifiers,
                      GlobalRequestProcessor globalRequestProcessor) throws DispatchException;

    /**
     * Provide a unified API to message processor(PMA/DM) register themselves.
     *
     * @param deviceType Device type
     * @param moduleIdentifiers YANG model paths
     * @param deviceConfigProcessor The instance of message processor
     * @throws DispatchException Dispatch exception
     */
    void addProcessor(String deviceType, Set<ModuleIdentifier> moduleIdentifiers,
                      DeviceConfigProcessor deviceConfigProcessor) throws DispatchException;

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
     * @param deviceType Device type
     * @param moduleIdentifiers YANG model namespaces
     * @param deviceConfigProcessor The instance of message processor
     * @throws DispatchException Dispatch exception
     */
    void removeProcessor(String deviceType, Set<ModuleIdentifier> moduleIdentifiers,
                         DeviceConfigProcessor deviceConfigProcessor) throws DispatchException;

    /**
     * Get common request processor.
     *
     * @param xmlns Request namespace
     * @return Request processor
     */
    GlobalRequestProcessor getProcessor(String xmlns);

    /**
     * Get request processor.
     *
     * @param deviceType Device type
     * @param xmlns Request namespace
     * @return Request processor
     */
    DeviceConfigProcessor getProcessor(String deviceType, String xmlns);

    /**
     * Get all of the request processor.
     *
     * @return All request processor
     */
    Set<DeviceConfigProcessor> getAllDeviceConfigProcessors();

    /**
     * Get all capabilities of the modules.
     * @return All capabilities
     */
    Set<String> getAllProcessorsCapability();

    /**
     * Get all capabilities of common processors.
     *
     * @return Capabilities of common processors
     */
    Map<GlobalRequestProcessor, Set<ModuleIdentifier>> getCommonProcessorsCapabilities();

    /**
     * Get all capabilities of device processors.
     *
     * @return Capabilities of device processors
     */
    Map<DeviceConfigProcessor, Set<ProcessorCapability>> getDeviceProcessorsCapabilities();

    /**
     * Get all capabilities of all processors.
     *
     * @return Capabilities
     */
    Set<ProcessorCapability> getDeviceProcessorCapabilities();

    /**
     * Get all modules of all processors.
     *
     * @return Module identifiers
     */
    Set<ModuleIdentifier> getAllModuleIdentifiers();

    /**
     * Get all modules of common processors.
     *
     * @return Module identifiers
     */
    Set<ModuleIdentifier> getCommonModuleIdentifiers();
}
