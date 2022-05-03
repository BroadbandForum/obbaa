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

package org.broadband_forum.obbaa.aggregator.registrant.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.aggregator.api.DeviceConfigProcessor;
import org.broadband_forum.obbaa.aggregator.api.DispatchException;
import org.broadband_forum.obbaa.aggregator.api.GlobalRequestProcessor;
import org.broadband_forum.obbaa.aggregator.api.NetworkFunctionConfigProcessor;
import org.broadband_forum.obbaa.aggregator.api.ProcessorCapability;
import org.broadband_forum.obbaa.aggregator.impl.ProcessorCapabilityImpl;
import org.broadband_forum.obbaa.aggregator.processor.NetconfMessageUtil;
import org.broadband_forum.obbaa.aggregator.registrant.api.RequestProcessorManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ModuleIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Request processor manager.
 */
public class RequestProcessorManagerImpl implements RequestProcessorManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestProcessorManagerImpl.class);
    private static final String ERROR_FORMAT = "{} Error: ";
    private static final String REMOVE_FUNCTION = "removeProcessor";

    private Map<GlobalRequestProcessor, Set<ModuleIdentifier>> m_commonRequestProcessors = new HashMap<>();
    private Map<DeviceConfigProcessor, Set<ProcessorCapability>> m_deviceRequestProcessors = new HashMap<>();
    private Map<NetworkFunctionConfigProcessor, Set<ModuleIdentifier>> m_networkFunctionRequestProcessors = new HashMap<>();

    @Override
    public void addProcessor(Set<ModuleIdentifier> moduleIdentifiers, GlobalRequestProcessor globalRequestProcessor)
            throws DispatchException {
        try {
            Set<ModuleIdentifier> moduleIdentifiersOld = getCommonRequestProcessors().get(globalRequestProcessor);
            if (moduleIdentifiersOld == null) {
                addNewProcessor(moduleIdentifiers, globalRequestProcessor);
                return;
            }

            moduleIdentifiersOld.addAll(moduleIdentifiers);
        }
        catch (NullPointerException ex) {
            LOGGER.error(ERROR_FORMAT, "addCommonProcessor", ex);
            throw new DispatchException(ex);
        }
    }

    @Override
    public void addProcessor(String deviceType, Set<ModuleIdentifier> moduleIdentifiers,
                             DeviceConfigProcessor deviceConfigProcessor) throws DispatchException {
        try {
            Set<ProcessorCapability> capabilities = getDeviceRequestProcessors().get(deviceConfigProcessor);
            if (capabilities == null) {
                addNewProcessor(deviceType, moduleIdentifiers, deviceConfigProcessor);
                return;
            }

            mergeProcessor(deviceType, moduleIdentifiers, capabilities);
        }
        catch (NullPointerException ex) {
            LOGGER.error(ERROR_FORMAT, "addDeviceRequestProcessor", ex);
            throw new DispatchException(ex);
        }
    }

    @Override
    public void addNFCProcessor(Set<ModuleIdentifier> moduleIdentifiers, NetworkFunctionConfigProcessor networkFunctionConfigProcessor)
            throws DispatchException {
        try {
            Set<ModuleIdentifier> moduleIdentifiersOld = m_networkFunctionRequestProcessors.get(networkFunctionConfigProcessor);
            if (moduleIdentifiersOld == null) {
                m_networkFunctionRequestProcessors.put(networkFunctionConfigProcessor,moduleIdentifiers);
            }
            else {
                moduleIdentifiersOld.addAll(moduleIdentifiers);
            }
        }
        catch (NullPointerException ex) {
            LOGGER.error(ERROR_FORMAT, "addNFConfigProcessor", ex);
            throw new DispatchException(ex);
        }
    }

    @Override
    public void removeProcessor(GlobalRequestProcessor globalRequestProcessor) throws DispatchException {
        try {
            getCommonRequestProcessors().remove(globalRequestProcessor);
        }
        catch (NullPointerException ex) {
            LOGGER.error(ERROR_FORMAT, REMOVE_FUNCTION, ex);
            throw new DispatchException(ex);
        }
    }

    @Override
    public void removeProcessor(DeviceConfigProcessor deviceConfigProcessor) throws DispatchException {
        try {
            getDeviceRequestProcessors().remove(deviceConfigProcessor);
        }
        catch (NullPointerException ex) {
            LOGGER.error(ERROR_FORMAT, REMOVE_FUNCTION, ex);
            throw new DispatchException(ex);
        }
    }

    @Override
    public void removeProcessor(String deviceType, Set<ModuleIdentifier> moduleIdentifiers,
                                DeviceConfigProcessor deviceConfigProcessor) throws DispatchException {
        try {
            Set<ProcessorCapability> capabilities = getDeviceRequestProcessors().get(deviceConfigProcessor);
            DispatchException.assertNull(capabilities, "Can not find the processor");

            for (ProcessorCapability capability : capabilities) {
                if (capability.getDeviceType().equalsIgnoreCase(deviceType)) {
                    capability.getModuleIdentifiers().removeAll(moduleIdentifiers);
                    correctionDeviceRequestProcessors(deviceConfigProcessor, capability);
                    return;
                }
            }
        }
        catch (NullPointerException ex) {
            LOGGER.error(ERROR_FORMAT, REMOVE_FUNCTION, ex);
            throw new DispatchException(ex);
        }
    }

    @Override
    public NetworkFunctionConfigProcessor getNFCProcessor(String xmlns) {
        for (Map.Entry<NetworkFunctionConfigProcessor, Set<ModuleIdentifier>> entry: m_networkFunctionRequestProcessors.entrySet()) {
            ModuleIdentifier moduleIdentifier = getModuleByXmlns(xmlns, entry.getValue());
            if (moduleIdentifier != null) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public GlobalRequestProcessor getProcessor(String xmlns) {
        for (Map.Entry<GlobalRequestProcessor, Set<ModuleIdentifier>> entry : getCommonRequestProcessors().entrySet()) {
            ModuleIdentifier moduleIdentifier = getModuleByXmlns(xmlns, entry.getValue());
            if (moduleIdentifier != null) {
                return entry.getKey();
            }
        }

        return null;
    }

    @Override
    public DeviceConfigProcessor getProcessor(String deviceType, String xmlns) {
        try {
            for (Map.Entry<DeviceConfigProcessor, Set<ProcessorCapability>> entry :
                    getDeviceRequestProcessors().entrySet()) {
                if (isProcessorSupportTheXmlns(deviceType, xmlns, entry.getValue())) {
                    return entry.getKey();
                }
            }
        }
        catch (NullPointerException ex) {
            LOGGER.error(ERROR_FORMAT, REMOVE_FUNCTION, ex);
        }

        return null;
    }

    @Override
    public Set<NetworkFunctionConfigProcessor> getAllNFConfigProcessors() {
        return m_networkFunctionRequestProcessors.keySet();
    }

    @Override
    public Set<DeviceConfigProcessor> getAllDeviceConfigProcessors() {
        Set<DeviceConfigProcessor> deviceConfigProcessors = new HashSet<>();

        try {
            for (Map.Entry<DeviceConfigProcessor, Set<ProcessorCapability>> entry :
                    getDeviceRequestProcessors().entrySet()) {
                deviceConfigProcessors.add(entry.getKey());
            }
        }
        catch (NullPointerException ex) {
            LOGGER.error(ERROR_FORMAT, "setDefaultRequestProcessor", ex);
        }

        return deviceConfigProcessors;
    }

    @Override
    public Map<DeviceConfigProcessor, Set<ProcessorCapability>> getDeviceProcessorsCapabilities() {
        return getDeviceRequestProcessors();
    }

    @Override
    public Set<ProcessorCapability> getDeviceProcessorCapabilities() {
        Set<ProcessorCapability> processorCapabilities = new HashSet<>();

        try {
            for (Map.Entry<DeviceConfigProcessor, Set<ProcessorCapability>> entry :
                    getDeviceRequestProcessors().entrySet()) {
                mergeCapabilities(processorCapabilities, entry.getValue());
            }
        }
        catch (NullPointerException ex) {
            LOGGER.error(ERROR_FORMAT, "getDeviceProcessorCapabilities", ex);
        }

        return processorCapabilities;
    }

    @Override
    public Set<ModuleIdentifier> getAllModuleIdentifiers() {
        Set<ModuleIdentifier> moduleIdentifiers = new HashSet<>();

        try {
            moduleIdentifiers.addAll(getCommonModuleIdentifiers());
            moduleIdentifiers.addAll(getDeviceModuleIdentifiers());
        }
        catch (NullPointerException ex) {
            LOGGER.error(ERROR_FORMAT, "getDeviceProcessorCapabilities", ex);
        }

        return moduleIdentifiers;
    }

    @Override
    public Set<ModuleIdentifier> getCommonModuleIdentifiers() {
        Set<ModuleIdentifier> moduleIdentifiers = new HashSet<>();

        for (Map.Entry<GlobalRequestProcessor, Set<ModuleIdentifier>> entryCommon :
                getCommonRequestProcessors().entrySet()) {
            mergeModuleList(moduleIdentifiers, entryCommon.getValue());
        }

        return moduleIdentifiers;
    }

    @Override
    public Set<String> getAllProcessorsCapability() {
        Set<ModuleIdentifier> moduleIdentifiers = getAllModuleIdentifiers();
        Set<String> capabilities = new HashSet<>();

        for (ModuleIdentifier moduleIdentifier : moduleIdentifiers) {
            capabilities.add(NetconfMessageUtil.buildModuleCapability(moduleIdentifier));
        }

        return capabilities;
    }

    @Override
    public Map<GlobalRequestProcessor, Set<ModuleIdentifier>> getCommonProcessorsCapabilities() {
        return getCommonRequestProcessors();
    }

    /**
     * Merge module list.
     *
     * @param target Target list
     * @param source Source list
     */
    private void mergeModuleList(Set<ModuleIdentifier> target, Set<ModuleIdentifier> source) {
        target.addAll(source);
    }

    /**
     * Merge module list set.
     *
     * @param target Target
     * @param source Source
     */
    private void mergeModuleListFromOneProcessor(Set<ModuleIdentifier> target, Set<ProcessorCapability> source) {
        for (ProcessorCapability processorCapability : source) {
            mergeModuleList(target, processorCapability.getModuleIdentifiers());
        }
    }

    /**
     * Merge capabilities set.
     *
     * @param target Target
     * @param source Source
     */
    private void mergeCapabilities(Set<ProcessorCapability> target, Set<ProcessorCapability> source) {
        for (ProcessorCapability targetType : target) {
            for (ProcessorCapability sourceType : source) {
                if (targetType.getDeviceType().equals(sourceType.getDeviceType())) {
                    mergeModuleList(targetType.getModuleIdentifiers(), sourceType.getModuleIdentifiers());
                    return;
                }
            }
        }

        target.addAll(source);
    }

    /**
     * Get module identifier by namespace.
     *
     * @param xmlns YANG namespace
     * @param modules Modules
     * @return Module identifier
     */
    private ModuleIdentifier getModuleByXmlns(String xmlns, Set<ModuleIdentifier> modules) {
        for (ModuleIdentifier moduleIdentifier : modules) {
            if (moduleIdentifier.getNamespace().toString().equalsIgnoreCase(xmlns)) {
                return moduleIdentifier;
            }
        }

        return null;
    }

    /**
     * Get module identifiers of device process modules.
     *
     * @return Module identifiers
     */
    private Set<ModuleIdentifier> getDeviceModuleIdentifiers() {
        Set<ModuleIdentifier> moduleIdentifiers = new HashSet<>();

        for (Map.Entry<DeviceConfigProcessor, Set<ProcessorCapability>> entry :
                getDeviceRequestProcessors().entrySet()) {
            mergeModuleListFromOneProcessor(moduleIdentifiers, entry.getValue());
        }

        return moduleIdentifiers;
    }

    /**
     * Is the processor support the namespace of the device type.
     *
     * @param deviceType Device type
     * @param xmlns Namespace
     * @param deviceProcessorCapabilities Capabilities
     * @return true or false
     */
    private boolean isProcessorSupportTheXmlns(String deviceType, String xmlns,
                                               Set<ProcessorCapability> deviceProcessorCapabilities) {
        for (ProcessorCapability capability : deviceProcessorCapabilities) {
//            if (!capability.getDeviceType().equalsIgnoreCase(deviceType)) {
//                continue;
//            }

            ModuleIdentifier moduleIdentifier = getModuleByXmlns(xmlns, capability.getModuleIdentifiers());
            if (moduleIdentifier != null) {
                return true;
            }
        }

        return false;
    }

    /**
     * Correction if the capability is empty.
     *
     * @param deviceConfigProcessor Processor
     * @param capability Capability
     */
    private void correctionDeviceRequestProcessors(DeviceConfigProcessor deviceConfigProcessor,
                                                   ProcessorCapability capability) {
        Set<ProcessorCapability> capabilities = getDeviceRequestProcessors().get(deviceConfigProcessor);

        if (capability.getModuleIdentifiers().isEmpty()) {
            capabilities.remove(capability);
        }

        if (capabilities.isEmpty()) {
            getDeviceRequestProcessors().remove(deviceConfigProcessor);
        }
    }

    /**
     * Get Common request processors.
     * @return processors
     */
    private Map<GlobalRequestProcessor, Set<ModuleIdentifier>> getCommonRequestProcessors() {
        return m_commonRequestProcessors;
    }

    /**
     * Get Device request processors.
     *
     * @return processors
     */
    private Map<DeviceConfigProcessor, Set<ProcessorCapability>> getDeviceRequestProcessors() {
        return m_deviceRequestProcessors;
    }

    /**
     * Add a new gloabl processor.
     *
     * @param moduleIdentifiers Module identifiers
     * @param globalRequestProcessor Processor
     */
    private void addNewProcessor(Set<ModuleIdentifier> moduleIdentifiers,
                                GlobalRequestProcessor globalRequestProcessor) {
        Set<ModuleIdentifier> moduleIdentifiersNew = new HashSet<>();
        moduleIdentifiersNew.addAll(moduleIdentifiers);
        getCommonRequestProcessors().put(globalRequestProcessor, moduleIdentifiersNew);
    }

    /**
     * Add a new device config processor.
     *
     * @param deviceType Device type
     * @param moduleIdentifiers Module identifiers
     * @param deviceConfigProcessor Processor
     */
    private void addNewProcessor(String deviceType, Set<ModuleIdentifier> moduleIdentifiers,
                             DeviceConfigProcessor deviceConfigProcessor) {
        Set<ProcessorCapability> capabilities = new HashSet<>();
        capabilities.add(new ProcessorCapabilityImpl(deviceType, moduleIdentifiers));
        getDeviceRequestProcessors().put(deviceConfigProcessor, capabilities);
    }

    /**
     * Merge processor.
     *
     * @param deviceType Device type
     * @param moduleIdentifiers Module identifiers
     * @param oldCapabilities Old capabilities
     */
    public void mergeProcessor(String deviceType, Set<ModuleIdentifier> moduleIdentifiers,
                               Set<ProcessorCapability> oldCapabilities) {
        for (ProcessorCapability capability : oldCapabilities) {
            if (capability.getDeviceType().equalsIgnoreCase(deviceType)) {
                capability.getModuleIdentifiers().addAll(moduleIdentifiers);
                return;
            }
        }

        oldCapabilities.add(new ProcessorCapabilityImpl(deviceType, moduleIdentifiers));
    }
}
