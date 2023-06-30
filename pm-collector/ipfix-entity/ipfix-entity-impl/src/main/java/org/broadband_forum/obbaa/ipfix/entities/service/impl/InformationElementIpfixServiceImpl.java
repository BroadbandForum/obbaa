/*
 * Copyright 2023 Broadband Forum
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

package org.broadband_forum.obbaa.ipfix.entities.service.impl;

import org.broadband_forum.obbaa.ipfix.entities.ie.InformationElement;
import org.broadband_forum.obbaa.ipfix.entities.service.InformationElementIpfixService;
import org.broadband_forum.obbaa.ipfix.entities.service.ie.impl.InformationElementCacheImpl;

public class InformationElementIpfixServiceImpl implements InformationElementIpfixService {

    private InformationElementCacheImpl m_ieCache;

    public InformationElementIpfixServiceImpl(InformationElementCacheImpl ieCache) {
        this.m_ieCache = ieCache;
    }

    public InformationElement getInformationElement(String family, int informationElementId) {
        //TODO: check for sync
        return m_ieCache.getInformationElement(family, informationElementId);
    }
}
