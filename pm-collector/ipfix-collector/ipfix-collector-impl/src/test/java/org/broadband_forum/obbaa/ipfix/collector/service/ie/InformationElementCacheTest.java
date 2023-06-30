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

package org.broadband_forum.obbaa.ipfix.collector.service.ie;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.broadband_forum.obbaa.ipfix.entities.ie.InformationElement;
import org.broadband_forum.obbaa.ipfix.entities.service.ie.impl.InformationElementCacheImpl;
import org.junit.Before;
import org.junit.Test;

public class InformationElementCacheTest {

    private InformationElementCacheImpl m_informationElementCache = new InformationElementCacheImpl();

    @Before
    public void testCacheIsAvailable() {
        assertFalse(m_informationElementCache.isAvailable());
    }

    @Test
    public void testGetPutInformationElement() {
        String family = "family";
        assertFalse(m_informationElementCache.isAvailable(family));

        int ieId = 5002;
        assertNull(m_informationElementCache.getInformationElement(family, ieId));

        InformationElement ie = putInformationElement(family, ieId);
        assertEquals(ie, m_informationElementCache.getInformationElement(family, ieId));
        assertTrue(m_informationElementCache.isAvailable(family));
        assertTrue(m_informationElementCache.isAvailable());
    }

    private InformationElement putInformationElement(String family, int ieId) {
        InformationElement ie = new InformationElement();
        ie.setElementId(ieId);
        m_informationElementCache.putInformationElementId(family, ie);
        return ie;
    }

    @Test
    public void testGetPutInformationElementByEnterpriseNum() {
        String ieName = "/sys:system/sys:hostname";
        assertNull(m_informationElementCache.getInformationElement(183L, ieName));

        InformationElement ie = putInformationElement(ieName);
        assertEquals(ie, m_informationElementCache.getInformationElement(183L, ieName));
    }

    private InformationElement putInformationElement(String ieName) {
        InformationElement ie = new InformationElement();
        ie.setName(ieName);
        m_informationElementCache.putInformationElementName(183L, ie);
        return ie;
    }

    @Test
    public void testRemoveInformationElement() {
        assertFalse(m_informationElementCache.removeInformationElement("family"));

        putInformationElement("family", 5002);
        assertTrue(m_informationElementCache.isAvailable());
        assertTrue(m_informationElementCache.removeInformationElement("family"));
    }

}
