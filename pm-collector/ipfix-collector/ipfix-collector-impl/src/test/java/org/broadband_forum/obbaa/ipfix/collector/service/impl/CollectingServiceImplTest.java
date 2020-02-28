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

import com.google.gson.Gson;
import org.broadband_forum.obbaa.ipfix.collector.entities.IpfixMessage;
import org.broadband_forum.obbaa.ipfix.collector.entities.IpfixMessageNotification;
import org.broadband_forum.obbaa.ipfix.collector.entities.logging.IpfixDecodedData;
import org.broadband_forum.obbaa.ipfix.collector.entities.record.AbstractTemplateRecord;
import org.broadband_forum.obbaa.ipfix.collector.entities.record.IpfixOptionTemplateRecord;
import org.broadband_forum.obbaa.ipfix.collector.entities.record.IpfixTemplateRecord;
import org.broadband_forum.obbaa.ipfix.collector.entities.set.IpfixDataSet;
import org.broadband_forum.obbaa.ipfix.collector.exception.NotEnoughBytesException;
import org.broadband_forum.obbaa.ipfix.collector.service.DecodingDataRecordService;
import org.broadband_forum.obbaa.ipfix.collector.service.DeviceCacheService;
import org.broadband_forum.obbaa.ipfix.collector.service.IpfixCachingService;
import org.broadband_forum.obbaa.ipfix.collector.util.IpfixUtilities;
import org.broadband_forum.obbaa.pm.service.DataHandlerService;
import org.broadband_forum.obbaa.pm.service.IpfixDataHandler;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

public class CollectingServiceImplTest {

    @Mock
    private DecodingDataRecordService m_decodingDataRecordService;

    private IpfixCachingService m_cachingService;

    @Mock
    private DeviceCacheService m_deviceFamilyCacheService;

    @Mock
    private DataHandlerService m_dataHandlerService;

    private byte[] m_data;

    @Rule
    public final EnvironmentVariables m_environmentVariables = new EnvironmentVariables();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        m_data = IpfixUtilities.hexStringToByteArray("010300200000006F6161000000DE00000000014D6262000001BC0503000318A3");
    }

    @Test
    public void testCreateIpfixMessage() {
        try {
            byte[] data = IpfixUtilities.hexStringToByteArray("0103002000");
            CollectingServiceImpl collectingService = new CollectingServiceImpl(m_decodingDataRecordService, m_cachingService,
                    m_dataHandlerService, m_deviceFamilyCacheService);
            collectingService.createIpfixMessage(data);
            fail();
        } catch (NotEnoughBytesException e) {
            assertEquals("The IPFIX message is not correct formatted", e.getMessage());
        }
    }

    @Test
    public void testCollectOptionTemplateSet() throws Exception {
        byte[] data = IpfixUtilities.hexStringToByteArray("000A00AC5DAEECB700000000000010EF00030022025800030001938AFFFF00000E91A1BCFFFF" +
                "00000E91A1BDFFFF00000E910258007A08686F73746E616D650961646D696E757365726224362471334739564D6D5424736C484C59547A6A6553495A3" +
                "86E525A465A5A487255384356385352387352456B6447544A656D6B705063616D4476414979372E504247494F6771767543757037716557382E6B6246" +
                "38416D44596378586B6373792E");
        IpfixMessage ipfixMessage = new IpfixMessage(data);
        ipfixMessage.getHeader().setExportTime(Instant.now().toString());
        String hostName = "hostname";
        String remoteAddress = "192.168.1.1";
        String username = "adminuser";
        String hashedPass = "$6$129267dbd52b96f3$BcacdzmrCHVqpQcEklp1ZjsBkW2n78xsTfl.nVKOFa.G1QXjC.MXtamcnAKCWRAMMg/rd/GCOSTq20T72CRGk.";
        Set<IpfixDecodedData> decodedDataSet = new HashSet<>();
        decodedDataSet.add(new IpfixDecodedData("8636.3729", "String", username));
        decodedDataSet.add(new IpfixDecodedData("8637.3729", "String", hashedPass));
        decodedDataSet.add(new IpfixDecodedData("5002.3729", "hostName", hashedPass));
        m_cachingService = new IpfixCachingServiceImpl(){
            @Override
            public void cacheTemplateRecord(long obsvDomain, String hostname, Map<Integer, AbstractTemplateRecord> templateByIdCache) {
            }
            @Override
            public List<IpfixDataSet> getAndInvalidateIpfixDataSet(long obsvDomain, String hostName) {
                return ipfixMessage.getDataSets();
            }
            @Override
            public AbstractTemplateRecord getTemplateRecord(long obsvDomain, String hostname, int templateId) {
                return null;
            }
        };
        CollectingServiceImpl collectingService = new CollectingServiceImpl(m_decodingDataRecordService, m_cachingService,
                m_dataHandlerService, m_deviceFamilyCacheService) {
            @Override
            protected IpfixMessage createIpfixMessage(byte[] data) {
                return ipfixMessage;
            }
        };

        when(m_decodingDataRecordService.decodeDataSet(eq(""), eq(4335L), any(), eq(ipfixMessage), any()))
                .thenReturn(decodedDataSet);

        verifyCollectOptionTLSuccess(collectingService, data, remoteAddress, "");
    }

    private void verifyCollectOptionTLSuccess(CollectingServiceImpl collectingService, byte[] data, String remoteAddress,
                                              String hostName) throws Exception {

        Map.Entry<Optional<Long>, Optional<String>> result = collectingService.collect(data, remoteAddress, Optional.of(hostName));

        assertEquals(Optional.of(4335L), result.getKey());
        assertEquals(Optional.of(""), result.getValue());
    }

    @Test
    public void testCacheTemplateWithTemplateRecordExisted() throws Exception {
        AbstractTemplateRecord templateRecord = new IpfixOptionTemplateRecord(new byte[16]);
        m_cachingService = mock(IpfixCachingServiceImpl.class);
        when(m_cachingService.getTemplateRecord(4335L, "hostname", 600)).thenReturn(templateRecord);
        when(m_deviceFamilyCacheService.getDeviceFamily("hostname")).thenReturn("family");
        verifyCacheTemplate();
        verify(m_cachingService).cacheTemplateRecord(eq(4335L), eq("hostname"), anyMap());
    }

    private void verifyCacheTemplate() throws Exception {
        byte[] data = IpfixUtilities.hexStringToByteArray("000A00AC5DAEECB700000000000010EF00030022025800030001938AFFFF00000E91A1BCFFFF" +
                "00000E91A1BDFFFF00000E910258007A08686F73746E616D650961646D696E757365726224362471334739564D6D5424736C484C59547A6A6553495A3" +
                "86E525A465A5A487255384356385352387352456B6447544A656D6B705063616D4476414979372E504247494F6771767543757037716557382E6B6246" +
                "38416D44596378586B6373792E");
        IpfixMessage ipfixMessage = new IpfixMessage(data);
        ipfixMessage.getHeader().setExportTime(Instant.now().toString());
        CollectingServiceImpl collectingService = new CollectingServiceImpl(m_decodingDataRecordService, m_cachingService,
                m_dataHandlerService, m_deviceFamilyCacheService) {
            @Override
            protected IpfixMessage createIpfixMessage(byte[] data) {
                return ipfixMessage;
            }
        };

        Map.Entry<Optional<Long>, Optional<String>> result = collectingService.collect(data, "192.168.1.1", Optional.of("hostname"));
        assertEquals(Optional.of(4335L), result.getKey());
        assertEquals(Optional.of("hostname"), result.getValue());
    }

    @Test
    public void testCacheTemplateWithTemplateRecordIsNotExist() throws Exception {
        byte[] data = IpfixUtilities.hexStringToByteArray("000A00AC5DAEECB700000000000010EF00030022025800030001938AFFFF00000E91A1BCFFFF" +
                "00000E91A1BDFFFF00000E910258007A08686F73746E616D650961646D696E757365726224362471334739564D6D5424736C484C59547A6A6553495A3" +
                "86E525A465A5A487255384356385352387352456B6447544A656D6B705063616D4476414979372E504247494F6771767543757037716557382E6B6246" +
                "38416D44596378586B6373792E");
        IpfixMessage ipfixMessage = new IpfixMessage(data);
        m_cachingService = new IpfixCachingServiceImpl() {
            @Override
            public void cacheTemplateRecord(long obsvDomain, String hostname, Map<Integer, AbstractTemplateRecord> templateByIdCache) {
            }
            @Override
            public List<IpfixDataSet> getAndInvalidateIpfixDataSet(long obsvDomain, String hostName) {
                return ipfixMessage.getDataSets();
            }
            @Override
            public AbstractTemplateRecord getTemplateRecord(long obsvDomain, String hostname, int templateId) {
                return null;
            }
            @Override
            public void cacheIpfixDataSet(long obsvDomain, String hostName, IpfixDataSet set) {
            }
        };
        verifyCacheTemplate();
    }

    @Test
    public void testCollectTemplateSet() throws Exception {
        byte[] data = IpfixUtilities.hexStringToByteArray("000A01DB5DB0B80D00000000000010EF00020020011400038107FFFF" +
                "00000E918109000400000E91810A000400000E91011401AB0444534C3100000001000000020544534C3130000000010000000" +
                "10544534C313100000001000000010544534C313200000001000000010544534C313300000001000000010544534C31340000" +
                "0001000000020544534C313500000001000000010544534C313600000001000000010444534C3200000001000000010844534" +
                "C322E50544D00000001000000010444534C3300000001000000010844534C332E50544D00000001000000010444534C340000" +
                "0001000000010444534C3500000001000000010444534C3600000001000000010444534C3700000001000000010444534C380" +
                "0000001000000010444534C39000000010000000109534650312E45544831000000010000000109534650322E455448310000" +
                "00010000000106696E62616E6400000001000000010C6E66616365312D75736572310000000100000001076F757462616E640" +
                "0000001000000010773696E62616E64000000010000000108736F757462616E6400000001000000010C7566616365312D7573" +
                "65723100000001000000010C7566616365312D757365723300000001000000010258310000000100000001");
        IpfixMessage ipfixMessage = new IpfixMessage(data);
        ipfixMessage.getHeader().setExportTime(Instant.now().toString());
        AbstractTemplateRecord templateRecord = new IpfixTemplateRecord(new byte[16]);
        m_cachingService = mock(IpfixCachingServiceImpl.class);
        when(m_cachingService.getTemplateRecord(4335L, "hostname", 600)).thenReturn(templateRecord);
        String hostName = "hostname";
        String remoteAddress = "192.168.1.1";
        String username = "adminuser";
        String hashedPass = "$6$129267dbd52b96f3$BcacdzmrCHVqpQcEklp1ZjsBkW2n78xsTfl.nVKOFa.G1QXjC.MXtamcnAKCWRAMMg/rd/GCOSTq20T72CRGk.";
        Set<IpfixDecodedData> decodedDataSet = new HashSet<>();
        decodedDataSet.add(new IpfixDecodedData("8636.3729", "String", username));
        decodedDataSet.add(new IpfixDecodedData("8637.3729", "String", hashedPass));
        decodedDataSet.add(new IpfixDecodedData("5002.3729", "String", hostName));
        CollectingServiceImpl collectingService = new CollectingServiceImpl(m_decodingDataRecordService, m_cachingService,
                m_dataHandlerService, m_deviceFamilyCacheService) {
            @Override
            protected IpfixMessage createIpfixMessage(byte[] data) {
                return ipfixMessage;
            }
        };
        String family = "sample-DPU-model-tls-1.0";
        Optional<String> optionalHostName = Optional.of(hostName);
        when(m_deviceFamilyCacheService.getDeviceFamily(hostName)).thenReturn(family);
        when(m_decodingDataRecordService.decodeDataSet(eq(hostName), eq(4335L), any(), any(), any())).thenReturn(decodedDataSet);
        List<IpfixDataHandler> dataHandlers = new ArrayList<>();
        IpfixDataHandler dataHandler = mock(IpfixDataHandler.class);
        dataHandlers.add(dataHandler);
        when(m_dataHandlerService.getDataHandlers()).thenReturn(dataHandlers);
        collectingService.collect(m_data, remoteAddress, optionalHostName);
        ArgumentMatcher<String> messageMatcher = new ArgumentMatcher<String>() {
            @Override
            public boolean matches(Object o) {
                String msgAsStr = (String) o;
                Gson gson = new Gson();
                IpfixMessageNotification message = gson.fromJson(msgAsStr, IpfixMessageNotification.class);
                assertNotNull(message);
                assertNotNull(message.getTimestamp());
                assertEquals("hostname", message.getHostName());
                assertEquals("192.168.1.1", message.getSourceIP());
                assertEquals("sample-DPU-model-tls-1.0", message.getDeviceAdapter());
                assertEquals(276, message.getTemplateID());
                assertEquals(4335L, message.getObservationDomain());
                return true;
            }
        };

        verify(dataHandler).handleIpfixData(argThat(messageMatcher));
    }

}
