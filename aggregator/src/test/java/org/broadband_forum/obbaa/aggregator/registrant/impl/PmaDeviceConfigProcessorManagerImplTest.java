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

import org.broadband_forum.obbaa.aggregator.api.DispatchException;
import org.broadband_forum.obbaa.aggregator.api.DeviceConfigProcessor;
import org.broadband_forum.obbaa.aggregator.processor.NetconfMessageUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ModuleIdentifier;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class PmaDeviceConfigProcessorManagerImplTest {
    private RequestProcessorManagerImpl m_requestProcessorManager;

    @Mock
    private DeviceConfigProcessor m_deviceConfigProcessor;

    private static String TEST_DEVICE_TYPE = "DPU";

    @Before
    public void setUp() throws Exception {
        m_requestProcessorManager = new RequestProcessorManagerImpl();

        m_deviceConfigProcessor = mock(DeviceConfigProcessor.class);

        //Register capability or do Schema-mount
        Set<ModuleIdentifier> moduleIdentifiers = new HashSet<>();

        ModuleIdentifier moduleIdentifier = NetconfMessageUtil.buildModuleIdentifier("yang:ietf-interfaces",
                "urn:ietf:params:xml:ns:yang:ietf-interfaces","2017-05-07");
        moduleIdentifiers.add(moduleIdentifier);

        m_requestProcessorManager.addProcessor(TEST_DEVICE_TYPE, moduleIdentifiers, m_deviceConfigProcessor);
    }

    @Test
    public void addProcessor() throws Exception {
        try {
            m_requestProcessorManager.addProcessor(null, null, null);
        }
        catch (DispatchException ex) {
            assertTrue(ex.getMessage().contains("Aggregator"));
        }
    }

    @Test
    public void removeProcessor() throws Exception {
        try {
            m_requestProcessorManager.addProcessor(null, null, null);
        }
        catch (DispatchException ex) {
            assertTrue(ex.getMessage().contains("Aggregator"));
        }
    }

    @Test
    public void getProcessor() throws Exception {
        DeviceConfigProcessor processor = m_requestProcessorManager.getProcessor(null, null);
        assertTrue(processor == null);

        Set<DeviceConfigProcessor> processors = m_requestProcessorManager.getAllDeviceConfigProcessors();
        assertFalse(processors.isEmpty());

        processor = m_requestProcessorManager.getProcessor(TEST_DEVICE_TYPE,
                "urn:ietf:params:xml:ns:yang:ietf-interfaces");
        assertEquals(m_deviceConfigProcessor, processor);

    }
}