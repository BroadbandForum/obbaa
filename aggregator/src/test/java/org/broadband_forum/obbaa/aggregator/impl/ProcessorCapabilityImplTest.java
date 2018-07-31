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
import org.broadband_forum.obbaa.aggregator.processor.NetconfMessageUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class ProcessorCapabilityImplTest {
    private ProcessorCapability processorCapability;
    private final String DEVICE_TYPE = "DPU";

    @Before
    public void setUp() throws Exception {
        Set<ModuleIdentifier> moduleIdentifiers = new HashSet<>();
        ModuleIdentifier moduleIdentifier = NetconfMessageUtil.buildModuleIdentifier("ietf-yang-library",
                "urn:ietf:params:xml:ns:yang:ietf-yang-library",
                "2017-10-30");
        moduleIdentifiers.add(moduleIdentifier);

        processorCapability = new ProcessorCapabilityImpl(DEVICE_TYPE, moduleIdentifiers);
    }

    @Test
    public void getDeviceType() throws Exception {
        String deviceType = processorCapability.getDeviceType();
        assertEquals(DEVICE_TYPE, deviceType);
    }

    @Test
    public void getModuleIdentifiers() throws Exception {
        Set<ModuleIdentifier> moduleIdentifiers = processorCapability.getModuleIdentifiers();
        assertEquals(1, moduleIdentifiers.size());
    }

}
