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

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ModuleIdentifier;

import java.util.Set;

/**
 * Capability data of the processor.
 */
public interface ProcessorCapability {

    /**
     * Get device type. some words is reserved for global processor like System-global.
     *
     * @return Device type
     */
    String getDeviceType();

    /**
     * Get module identifier.
     *
     * @return Module identifier
     */
    Set<ModuleIdentifier> getModuleIdentifiers();
}
