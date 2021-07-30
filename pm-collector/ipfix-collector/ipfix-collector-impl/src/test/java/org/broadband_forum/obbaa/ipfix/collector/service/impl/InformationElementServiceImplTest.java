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

package org.broadband_forum.obbaa.ipfix.collector.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.File;

import org.broadband_forum.obbaa.ipfix.collector.entities.ie.InformationElement;
import org.broadband_forum.obbaa.ipfix.collector.entities.ie.InformationElementType;
import org.broadband_forum.obbaa.ipfix.collector.service.IEMappingCacheService;
import org.broadband_forum.obbaa.ipfix.collector.service.ie.InformationElementCache;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class InformationElementServiceImplTest {

    private InformationElementServiceImpl m_informationElementService;

    @Mock
    private InformationElementCache m_cache;

    @Rule
    public final EnvironmentVariables envVar = new EnvironmentVariables();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        m_informationElementService = new InformationElementServiceImpl(m_cache);
    }

    @Test
    public void testInitWithoutCSVInIEMappingDir() {
        InformationElementServiceImpl informationElementService = new InformationElementServiceImpl(m_cache);
        assertFalse(informationElementService.isIECacheAvailable());
    }

    @Test
    public void testGetWithSyncInformationElement() {
        String family = "family";
        int informationElementId = 471;
        IEMappingCacheService ieMappingCacheService = mock(IEMappingCacheService.class);

        InformationElement expected = new InformationElement();
        when(m_cache.isAvailable(family)).thenReturn(false);
        when(m_cache.getInformationElement(family, informationElementId)).thenReturn(expected);
        m_informationElementService = new InformationElementServiceImpl(m_cache);
        m_informationElementService.setIeMappingCacheService(ieMappingCacheService);

        InformationElement actual = m_informationElementService.getInformationElement(family, informationElementId);
        verify(ieMappingCacheService).syncIEMappingCache(family);
        verify(m_cache).getInformationElement(family, informationElementId);
        assertEquals(expected, actual);
    }

    @Test
    public void testGetInformationElement() {
        String informationElementName = "/sys:system/sys:hostname";

        InformationElement expected = new InformationElement();
        when(m_cache.getInformationElement(3729L, informationElementName)).thenReturn(expected);

        m_informationElementService = new InformationElementServiceImpl(m_cache);
        InformationElement actual = m_informationElementService.getInformationElement(3729L, informationElementName);
        verify(m_cache).getInformationElement(3729L, informationElementName);
        assertEquals(expected, actual);
    }

    @Test
    public void testRemoveInformationElementMapping() {
        String family = "family";
        when(m_cache.removeInformationElement(family)).thenReturn(true);
        m_informationElementService = new InformationElementServiceImpl(m_cache);

        assertTrue(m_informationElementService.removeInformationElementMapping(family));
        verify(m_cache).removeInformationElement(family);
    }

    @Test
    public void testLoadInformationElementMapping() {
        String family = "family";
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("ie-mapping/IPFIX_IEId.csv").getFile());
        m_informationElementService.loadInformationElementMapping(family, file);

        InformationElement ie = new InformationElement();
        ie.setElementId(1);
        ie.setDataType(InformationElementType.STRING);
        ie.setName("/if:interfaces/if:interface/if:name");
        ie.setDataTypeSemantics("default");
        ie.setStatus("current");
        ie.setDescription("A short name uniquely describing an interface, eg \"Eth1/0\".");
        ie.setUnits("");
        ie.setRange("");
        ie.setReferences("See [RFC2863] for the definition of the ifName object.");
        ie.setRequester("");
        ie.setRevision("0");
        ie.setDate("20-11-2019");
        verify(m_cache).putInformationElementId(family, ie);
    }

    @Test
    public void testLoadInformationElementMappingWithCSVMissMatchHeader() {
        String family = "family";
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("ie-mapping/IPFIX_IEId_Header_Miss_Match.csv").getFile());
        m_informationElementService.loadInformationElementMapping(family, file);
        verifyZeroInteractions(m_cache);
    }
}
