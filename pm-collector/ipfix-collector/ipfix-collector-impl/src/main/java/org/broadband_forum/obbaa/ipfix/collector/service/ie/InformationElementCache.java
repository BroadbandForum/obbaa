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

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections4.MapUtils;
import org.broadband_forum.obbaa.ipfix.collector.entities.ie.InformationElement;

public class InformationElementCache {

    private Map<Long, InformationElementStorage> m_simInformationElementStorage;

    private Map<String, InformationElementStorage> m_informationElementStorage;

    public InformationElementCache() {
        m_informationElementStorage = new ConcurrentHashMap<>();
        m_simInformationElementStorage = new ConcurrentHashMap<>();
    }

    public InformationElement getInformationElement(String family, int informationElementId) {
        InformationElementStorage informationElementStorage = m_informationElementStorage.get(family);
        if (Objects.isNull(informationElementStorage)) {
            return null;
        }

        Map<Integer, InformationElement> ieIdMap = informationElementStorage.getIEIdMap();
        return ieIdMap.get(informationElementId);
    }

    public InformationElement getInformationElement(long enterpriseNumber, String informationElementName) {
        InformationElementStorage informationElementStorage = m_simInformationElementStorage.get(enterpriseNumber);
        if (Objects.isNull(informationElementStorage)) {
            return null;
        }

        Map<String, InformationElement> ieNameMap = informationElementStorage.getIENameMap();
        return ieNameMap.get(informationElementName);
    }

    public void putInformationElementId(String family, InformationElement ie) {
        InformationElementStorage informationElementStorage = getInformationElementStorage(family);
        Map<Integer, InformationElement> ieIdMap = informationElementStorage.getIEIdMap();
        ieIdMap.put(ie.getElementId(), ie);

        m_informationElementStorage.put(family, informationElementStorage);
    }

    public void putInformationElementName(long enterpriseNumber, InformationElement ie) {
        InformationElementStorage informationElementStorage = getInformationElementStorage(enterpriseNumber);
        Map<String, InformationElement> ieNameMap = informationElementStorage.getIENameMap();
        ieNameMap.put(ie.getName(), ie);

        m_simInformationElementStorage.put(enterpriseNumber, informationElementStorage);
    }

    private InformationElementStorage getInformationElementStorage(String family) {
        InformationElementStorage informationElementStorage = m_informationElementStorage.get(family);
        if (Objects.isNull(informationElementStorage)) {
            informationElementStorage = new InformationElementStorage();
        }
        return informationElementStorage;
    }

    private InformationElementStorage getInformationElementStorage(long enterpriseNumber) {
        InformationElementStorage informationElementStorage = m_simInformationElementStorage.get(enterpriseNumber);
        if (Objects.isNull(informationElementStorage)) {
            informationElementStorage = new InformationElementStorage();
        }
        return informationElementStorage;
    }

    public boolean removeInformationElement(String deviceFamily) {
        return Objects.nonNull(m_informationElementStorage.remove(deviceFamily));
    }

    public boolean isAvailable() {
        return MapUtils.isNotEmpty(m_informationElementStorage);
    }

    public boolean isAvailable(String family) {
        return isAvailable() && Objects.nonNull(m_informationElementStorage.get(family));
    }
}