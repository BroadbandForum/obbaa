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

import net.sf.ehcache.CacheManager;
import org.broadband_forum.obbaa.ipfix.collector.entities.header.IpfixSetHeader;
import org.broadband_forum.obbaa.ipfix.collector.entities.header.IpfixTemplateRecordHeader;
import org.broadband_forum.obbaa.ipfix.collector.entities.record.AbstractTemplateRecord;
import org.broadband_forum.obbaa.ipfix.collector.entities.record.IpfixTemplateRecord;
import org.broadband_forum.obbaa.ipfix.collector.entities.set.IpfixDataSet;
import org.broadband_forum.obbaa.ipfix.collector.exception.NotEnoughBytesException;
import org.broadband_forum.obbaa.ipfix.collector.util.IpfixUtilities;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class IpfixCachingServiceImplTest {

    private IpfixCachingServiceImpl m_ipfixfeCachingService;

    @Before
    public void setUp() {
        m_ipfixfeCachingService = new IpfixCachingServiceImpl();
        m_ipfixfeCachingService.initialize();
    }

    @After
    public void tearDown() {
        m_ipfixfeCachingService.destroy();
    }

    @Test
    public void testCacheTemplateRecord() throws Exception {
        // Build template record cache
        long obsvDomain = 4335L;
        String hostname = "deviceA";

        IpfixTemplateRecord expected = generateTemplateRecord("01030002000800048008000500000E91");
        IpfixTemplateRecord tr = generateTemplateRecord("01010002000800048008000500000E91");

        HashMap<Integer, AbstractTemplateRecord> templateByIdCache = new HashMap<>();
        templateByIdCache.put(259, expected);
        templateByIdCache.put(257, tr);

        m_ipfixfeCachingService.cacheTemplateRecord(obsvDomain, hostname, templateByIdCache);


        IpfixTemplateRecord tmplRecord11 = generateTemplateRecord("01030002000800048008000500000E91");
        IpfixTemplateRecord tmplRecord12 = generateTemplateRecord("01010002000800048008000500000E91");

        HashMap<Integer, AbstractTemplateRecord> tmplRecordByIdCache1 = new HashMap<>();
        templateByIdCache.put(259, tmplRecord11);
        templateByIdCache.put(257, tmplRecord12);

        m_ipfixfeCachingService.cacheTemplateRecord(obsvDomain, "ANV1.ONT2", tmplRecordByIdCache1);

        IpfixTemplateRecord tmplRecord21 = generateTemplateRecord("01030002000800048008000500000E91");
        IpfixTemplateRecord tmplRecord22 = generateTemplateRecord("01010002000800048008000500000E91");

        HashMap<Integer, AbstractTemplateRecord> tmplRecordByIdCache2 = new HashMap<>();
        templateByIdCache.put(259, tmplRecord21);
        templateByIdCache.put(257, tmplRecord22);

        m_ipfixfeCachingService.cacheTemplateRecord(obsvDomain, "ANV1.ONT3", tmplRecordByIdCache2);


        // Verify cache data
        IpfixTemplateRecord template = (IpfixTemplateRecord) m_ipfixfeCachingService.getTemplateRecord(obsvDomain, hostname, 259);

        assertTemplateRecord(expected, template);

        template = (IpfixTemplateRecord) m_ipfixfeCachingService.getTemplateRecord(obsvDomain, hostname, 257);
        assertTemplateRecord(tr, template);

        Set<String> allCacheKeys = m_ipfixfeCachingService.getAllCacheKeys();
        assertEquals(2, allCacheKeys.size());
        assertTrue(allCacheKeys.contains("4335_deviceA_259"));
        assertTrue(allCacheKeys.contains("4335_deviceA_257"));

        // Test for updateHostname
        String newHostname = "deviceA.New";
        m_ipfixfeCachingService.updateHostName(obsvDomain, hostname, newHostname);
        template = (IpfixTemplateRecord) m_ipfixfeCachingService.getTemplateRecord(obsvDomain, newHostname, 257);
        assertTemplateRecord(tr, template);
        allCacheKeys = m_ipfixfeCachingService.getAllCacheKeys();
        assertEquals(2, allCacheKeys.size());
        assertTrue(allCacheKeys.contains("4335_deviceA.New_259"));
        assertTrue(allCacheKeys.contains("4335_deviceA.New_257"));
    }

    private IpfixTemplateRecord generateTemplateRecord(String hexString) throws NotEnoughBytesException {
        byte[] data = IpfixUtilities.hexStringToByteArray(hexString);
        return new IpfixTemplateRecord(data);
    }

    private void assertTemplateRecord(IpfixTemplateRecord expected, IpfixTemplateRecord template) {
        IpfixTemplateRecordHeader expectedHeader = expected.getHeader();
        IpfixTemplateRecordHeader templateHeader = template.getHeader();

        // Verify header
        assertEquals(expectedHeader.getId(), templateHeader.getId());
        assertEquals(expectedHeader.getFieldCount(), templateHeader.getFieldCount());
        assertTrue("IpfixSetHeader.getPresentation() isn't returned correctly", Arrays.equals(expectedHeader.getPresentation(), templateHeader.getPresentation()));
        // Verify content
        assertTrue("IpfixDataSet.getRawData() isn't returned correctly", Arrays.equals(expected.getPresentation(), template.getPresentation()));
    }

    @Test
    public void testCacheIpfixDataSet() throws Exception {
        Long obsvDomain = 4335L;
        String hostname = "deviceA";
        byte[] data = IpfixUtilities.hexStringToByteArray("0103001800");
        IpfixDataSet expected = new IpfixDataSet(data);

        m_ipfixfeCachingService.cacheIpfixDataSet(obsvDomain, hostname, expected);

        List<IpfixDataSet> result = m_ipfixfeCachingService.getAndInvalidateIpfixDataSet(obsvDomain, hostname);
        assertEquals(1, result.size());

        IpfixSetHeader expectedHeader = expected.getHeader();
        IpfixSetHeader actualHeader = result.get(0).getHeader();

        // Verify header
        assertEquals(expectedHeader.getId(), actualHeader.getId());
        assertEquals(expectedHeader.getLength(), actualHeader.getLength());
        assertTrue("IpfixSetHeader.getPresentation() isn't returned correctly", Arrays.equals(expectedHeader.getPresentation(), actualHeader.getPresentation()));
        // Verify content
        assertTrue("IpfixDataSet.getRawData() isn't returned correctly", Arrays.equals(expected.getRawData(), result.get(0).getRawData()));
        assertEquals(expected.getRecords(), result.get(0).getRecords());

        assertTrue("IpfixDataSet.getPresentation() isn't returned correctly", Arrays.equals(expected.getPresentation(), result.get(0).getPresentation()));


        // After invalidate cache
        result = m_ipfixfeCachingService.getAndInvalidateIpfixDataSet(obsvDomain, hostname);
        assertEquals(0, result.size());
    }
}
