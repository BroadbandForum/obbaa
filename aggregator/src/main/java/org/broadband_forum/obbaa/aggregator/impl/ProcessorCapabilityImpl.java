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

package org.broadband_forum.obbaa.aggregator.impl;

import org.broadband_forum.obbaa.aggregator.api.ProcessorCapability;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;

import java.util.HashSet;
import java.util.Set;

/**
 * Implement data description of processor capability.
 */
public class ProcessorCapabilityImpl implements ProcessorCapability {
    private String m_deviceType;
    private Set<ModuleIdentifier> m_moduleIdentifiers;

    public ProcessorCapabilityImpl(String deviceType, Set<ModuleIdentifier> moduleIdentifiers) {
        m_deviceType = deviceType;
        m_moduleIdentifiers = new HashSet<>(moduleIdentifiers);
    }

    @Override
    public String getDeviceType() {
        return m_deviceType;
    }

    @Override
    public Set<ModuleIdentifier> getModuleIdentifiers() {
        return m_moduleIdentifiers;
    }
}
