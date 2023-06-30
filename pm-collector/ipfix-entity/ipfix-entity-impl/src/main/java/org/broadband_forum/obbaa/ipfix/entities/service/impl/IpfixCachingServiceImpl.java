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

package org.broadband_forum.obbaa.ipfix.entities.service.impl;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.broadband_forum.obbaa.ipfix.entities.record.AbstractTemplateRecord;
import org.broadband_forum.obbaa.ipfix.entities.service.IpfixCachingService;
import org.broadband_forum.obbaa.ipfix.entities.set.IpfixDataSet;
import org.broadband_forum.obbaa.ipfix.entities.util.IpfixConstants;
import org.broadband_forum.obbaa.ipfix.entities.util.IpfixUtilities;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

public class IpfixCachingServiceImpl implements IpfixCachingService {

    private static final String IPFIX_DATASET_CACHE_NAME = "ipfix.collector.dataset.buffer.cache";

    private static final String TEMPLATE_RECORD_CACHE_NAME = "ipfix.collector.template.record.cache";
    // Key: HostName_TemplateID -> DataSet
    public Cache m_ipfixDataSetCache;
    // Key: OBSV_HostName_TemplateID -> AbstractTemplateRecord
    public Cache m_ipfixTemplateRecordCache;
    private CacheManager m_centralCacheManager;

    public IpfixCachingServiceImpl() {
        m_ipfixDataSetCache = new Cache(new CacheConfiguration()
                .name(IPFIX_DATASET_CACHE_NAME).maxElementsInMemory(IpfixConstants.getDataSetCacheSize()).eternal(false)
                .timeToLiveSeconds(TimeUnit.SECONDS.toSeconds(0)).timeToIdleSeconds(
                        TimeUnit.SECONDS.toSeconds(IpfixConstants.FE_DATA_SET_BUFFER_TIME))
                .persistence(new PersistenceConfiguration().strategy(PersistenceConfiguration.Strategy.NONE))
                .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU));

        m_ipfixTemplateRecordCache = new Cache(new CacheConfiguration()
                .name(TEMPLATE_RECORD_CACHE_NAME).maxElementsInMemory(IpfixConstants.getTemplateRecordCacheSize()).eternal(false)
                .timeToLiveSeconds(TimeUnit.HOURS.toSeconds(0)).timeToIdleSeconds(IpfixConstants.getTemplateRecordCacheExpiration())
                .persistence(new PersistenceConfiguration().strategy(PersistenceConfiguration.Strategy.NONE))
                .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU));
    }

    @Override
    public void cleanUpCache() {
    }

    public void destroy() {
        m_ipfixDataSetCache.removeAll();
        m_ipfixTemplateRecordCache.removeAll();
        m_centralCacheManager.removeCache(m_ipfixDataSetCache.getName());
        m_centralCacheManager.removeCache(m_ipfixTemplateRecordCache.getName());
    }

    public void setCentralCacheManager(CacheManager centralCacheManager) {
        m_centralCacheManager = centralCacheManager;
    }

    public void initialize() {
        m_centralCacheManager = CacheManager.getInstance();
        m_centralCacheManager.addCache(m_ipfixDataSetCache);
        m_centralCacheManager.addCache(m_ipfixTemplateRecordCache);
    }

    @Override
    public void cacheTemplateRecord(long obsvDomain, String hostname, Map<Integer, AbstractTemplateRecord> templateByIdCache) {
        if (templateByIdCache == null || templateByIdCache.isEmpty()) {
            return;
        }
        String prefix = IpfixConstants.buildCacheKey(obsvDomain, hostname);
        templateByIdCache.forEach((templateId, templateRecord) -> {
            m_ipfixTemplateRecordCache.put(new Element(IpfixConstants.buildCacheKey(prefix, templateId), templateRecord));
        });
    }

    @Override
    public AbstractTemplateRecord getTemplateRecord(long obsvDomain, String hostname, int templateId) {
        return (AbstractTemplateRecord) m_ipfixTemplateRecordCache.get(
                IpfixConstants.buildCacheKey(obsvDomain, hostname, templateId)).getObjectValue();
    }

    @Override
    public Set<String> getAllCacheKeys() {
        return new HashSet<>(m_ipfixTemplateRecordCache.getKeys());
    }

    @Override
    public void updateHostName(long obsvDomain, String oldHostName, String newHostName) {
        String oldPrefix = IpfixConstants.buildCacheKey(obsvDomain, oldHostName);
        String newPrefix = IpfixConstants.buildCacheKey(obsvDomain, newHostName);

        // Update hostname of IPFIXDataSet
        Set<String> keys = new HashSet<>(m_ipfixDataSetCache.getKeys());
        keys.stream().filter(key -> key.startsWith(oldPrefix))
                .forEach(key -> {
                    IpfixDataSet set = (IpfixDataSet) m_ipfixDataSetCache.get(key).getObjectValue();
                    m_ipfixDataSetCache.remove(key);
                    m_ipfixDataSetCache.put(new Element(IpfixConstants.buildCacheKey(newPrefix, set.getHeader().getId()), set));
                });

        // Update hostname of m_ipfixTemplateRecordCache
        // NOTE: there are maybe 250k entry on m_ipfixTemplateRecordCache but we will not change hostname all the time so
        //   there will be no issue to iterate over key of m_ipfixTemplateRecordCache
        keys = new HashSet<>(m_ipfixTemplateRecordCache.getKeys());
        keys.stream().filter(key -> key.startsWith(oldPrefix))
                .forEach(key -> {
                    AbstractTemplateRecord template = (AbstractTemplateRecord) m_ipfixTemplateRecordCache.get(key).getObjectValue();
                    m_ipfixTemplateRecordCache.remove(key);
                    m_ipfixTemplateRecordCache.put(new Element(IpfixConstants.buildCacheKey(newPrefix,
                            IpfixUtilities.getTemplateId(template)), template));
                });
    }

    @Override
    public void cacheIpfixDataSet(long obsvDomain, String hostName, IpfixDataSet set) {
        m_ipfixDataSetCache.put(new Element(IpfixConstants.buildCacheKey(obsvDomain, hostName, set.getHeader().getId()), set));
    }

    @Override
    public List<IpfixDataSet> getAndInvalidateIpfixDataSet(long obsvDomain, String hostName) {
        // There are not much buffered ipfixDataSetCache so keys's size isn't big
        String prefix = IpfixConstants.buildCacheKey(obsvDomain, hostName);

        Set<String> keys = new HashSet<>(m_ipfixDataSetCache.getKeys());

        return keys.stream().filter(key -> key.startsWith(prefix))
                .map(key -> {
                    IpfixDataSet dataSet = (IpfixDataSet) m_ipfixDataSetCache.get(key).getObjectValue();
                    m_ipfixDataSetCache.remove(key);
                    return dataSet;
                }).filter(set -> set != null)
                .collect(Collectors.toCollection(LinkedList::new));
    }
}
