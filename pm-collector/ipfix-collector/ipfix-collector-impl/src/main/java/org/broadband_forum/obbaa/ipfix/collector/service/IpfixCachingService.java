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

package org.broadband_forum.obbaa.ipfix.collector.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.ipfix.collector.entities.record.AbstractTemplateRecord;
import org.broadband_forum.obbaa.ipfix.collector.entities.set.IpfixDataSet;

public interface IpfixCachingService {
    void cacheIpfixDataSet(long obsvDomain, String hostName, IpfixDataSet set);

    List<IpfixDataSet> getAndInvalidateIpfixDataSet(long obsvDomain, String hostName);

    void cleanUpCache();

    void cacheTemplateRecord(long obsvDomain, String hostname, Map<Integer, AbstractTemplateRecord> templateByIdCache);

    AbstractTemplateRecord getTemplateRecord(long obsvDomain, String hostname, int templateId);

    Set<String> getAllCacheKeys();

    void updateHostName(long obsvDomain, String oldHostName, String newHostName);
}
