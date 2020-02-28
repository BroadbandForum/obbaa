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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.broadband_forum.obbaa.ipfix.collector.entities.IpfixMessage;
import org.broadband_forum.obbaa.ipfix.collector.entities.ie.InformationElement;
import org.broadband_forum.obbaa.ipfix.collector.entities.ie.InformationElementType;
import org.broadband_forum.obbaa.ipfix.collector.entities.logging.IpfixDecodedData;
import org.broadband_forum.obbaa.ipfix.collector.entities.record.AbstractTemplateRecord;
import org.broadband_forum.obbaa.ipfix.collector.entities.record.IpfixTemplateRecord;
import org.broadband_forum.obbaa.ipfix.collector.entities.set.IpfixDataSet;
import org.broadband_forum.obbaa.ipfix.collector.exception.DecodingException;
import org.broadband_forum.obbaa.ipfix.collector.service.DecodingDataRecordService;
import org.broadband_forum.obbaa.ipfix.collector.service.InformationElementService;
import org.broadband_forum.obbaa.ipfix.collector.service.ie.DecodeMethodFactory;
import org.broadband_forum.obbaa.ipfix.collector.util.IpfixUtilities;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DecodingDataRecordServiceImplTest {

    private DecodingDataRecordService m_decodingDataRecordService;

    @Mock
    private InformationElementService m_informationElementService;

    @Mock
    private CollectingServiceImpl.TemplateProvider m_templateProvider;

    @Mock
    private DeviceCacheServiceImpl m_deviceFamilyCacheService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        DecodeMethodFactory decodeMethodFactory = new DecodeMethodFactory();
        m_decodingDataRecordService = new DecodingDataRecordServiceImpl(m_informationElementService, decodeMethodFactory, m_deviceFamilyCacheService);
    }

    @Test
    public void testDecodeWhenDataSetIsInvalid() {
        try {
            m_decodingDataRecordService.decodeDataSet("", 1L, null, null, m_templateProvider);
            fail();
        } catch (DecodingException e) {
            assertEquals("Data set is invalid for decoding process.", e.getMessage());
        }
    }

    @Test
    public void testDecodeWhenBlueprintIsNotAvailable() throws Exception {
        byte[] dataSet = IpfixUtilities.hexStringToByteArray("010300200000006F6161000000DE00000000014D6262000001BC0503000318A3");
        IpfixDataSet set = new IpfixDataSet(dataSet);

        Set<IpfixDecodedData> decodedDataSet = m_decodingDataRecordService.decodeDataSet("", 1L, set, null, m_templateProvider);
        assertTrue(decodedDataSet.isEmpty());
        verify(m_templateProvider).get(259);
    }

    @Test
    public void testDecodeDataWhenFamilyIsNotAvailable() throws Exception {
        String hostName = "hostname";
        byte[] dataSet = IpfixUtilities.hexStringToByteArray("010300200000006F6161000000DE00000000014D6262000001BC0503000318A3");
        byte[] dataTemplateRecord = IpfixUtilities.hexStringToByteArray("01030004008D000400290002802A000400000E918286FFFF00000E91");
        IpfixDataSet set = new IpfixDataSet(dataSet);
        AbstractTemplateRecord blueprint = new IpfixTemplateRecord(dataTemplateRecord);
        when(m_templateProvider.get(259)).thenReturn(blueprint);

        try {
            m_decodingDataRecordService.decodeDataSet(hostName, 1L, set, null, m_templateProvider);
            fail();
        } catch (DecodingException e) {
            verify(m_deviceFamilyCacheService).getDeviceFamily(hostName);
            assertEquals("Could not find device family based on the deviceName: " + hostName, e.getMessage());
        }
    }

    @Test
    public void testDecodeHostName() throws Exception {
        byte[] data = IpfixUtilities.hexStringToByteArray("000A00AC5DAEECB700000000000010EF00030022025800030001938AFFFF00000E91A1BCFFFF" +
                "00000E91A1BDFFFF00000E910258007A08686F73746E616D650961646D696E757365726224362471334739564D6D5424736C484C59547A6A6553495A3" +
                "86E525A465A5A487255384356385352387352456B6447544A656D6B705063616D4476414979372E504247494F6771767543757037716557382E6B6246" +
                "38416D44596378586B6373792E");
        IpfixMessage ipfixMessage = new IpfixMessage(data);

        IpfixDataSet dataSet = ipfixMessage.getDataSets().get(0);
        AbstractTemplateRecord blueprint = ipfixMessage.getOptionTemplateSets().get(0).getRecords().get(0);
        String family = "sample-model-tls-DPU-1.0";
        when(m_templateProvider.get(600)).thenReturn(blueprint);
        when(m_deviceFamilyCacheService.getDeviceFamily("hostname")).thenReturn(family);

        Set<IpfixDecodedData> decodedDataSet = m_decodingDataRecordService.decodeDataSet("", 1L, dataSet, ipfixMessage, m_templateProvider);
        verify(m_deviceFamilyCacheService).getDeviceFamily("hostname");
        Set<IpfixDecodedData> decodedDataRecords = decodedDataSet;
        assertEquals(3, decodedDataRecords.size());
        Set<String> decodedCounters = new HashSet<>();
        for (IpfixDecodedData decodedData : decodedDataRecords) {
            decodedCounters.add(decodedData.getCounterName());
        }
        assertTrue(decodedCounters.contains("5002.3729"));
        assertTrue(decodedCounters.contains("8636.3729"));
        assertTrue(decodedCounters.contains("8637.3729"));
    }

    @Test
    public void testDecodeRawSetsException() throws Exception {
        byte[] dataSet = IpfixUtilities.hexStringToByteArray("0103012000000061BC0503000F6161000000DE00000000014D6262000001BC0503000318A3286FFFF00000E910" +
                "0103012000000061BC0503000F6161000000DE00000000014D6262000001BC0503000318A3286FFFF00000E91000014D6262000001BC0503000318A3286FFFF00000E91001" +
                "0103012000000061BC0503000F6161000000DE00000000014D6262000001BC0503000318A3286FFFF00000E91000014D6262000001BC0503000318A3286FFFF00000E91002" +
                "0103012000000061BC0503000F6161000000DE00000000014D6262000001BC0503000318A3286FFFF00000E91000014D6262000001BC0503000318A3286FFFF00000E91002" +
                "0103012000000061BC0503000F6161000000DE00000000014D6262000001BC0503000318A3286FFFF00000E91000014D6262000001BC0503000318A3286FFFF00000E91003");
        byte[] dataTemplateRecord = IpfixUtilities.hexStringToByteArray("01030004008D000400290002802A000400000E918286FFFF00000E91");
        IpfixDataSet set = new IpfixDataSet(dataSet);
        AbstractTemplateRecord blueprint = new IpfixTemplateRecord(dataTemplateRecord);
        when(m_templateProvider.get(259)).thenReturn(blueprint);
        InformationElement ie32 = mock(InformationElement.class);
        String hostName = "hostname";
        String family = "sample-model-tls-DPU-1.0";

        when(m_deviceFamilyCacheService.getDeviceFamily(hostName)).thenReturn(family);
        when(m_informationElementService.getInformationElement(family, 646)).thenReturn(ie32);
        when(ie32.getDataType()).thenReturn(InformationElementType.UNSIGNED32);

        try {
            m_decodingDataRecordService.decodeDataSet(hostName, 1L, set, null, m_templateProvider);
            fail();
        } catch (DecodingException e) {
            assertEquals("Error decoding data set: " + set + ", with template set: " + blueprint + " on the device: hostname", e.getMessage());
        }
    }

    @SuppressWarnings("serial")
    @Test
    public void testDecodeRawSets() throws Exception {
        byte[] dataSet = IpfixUtilities.hexStringToByteArray("010300200000006F6161000000DE00000000014D6262000001BC0503000318A3");
        byte[] dataTemplateRecord = IpfixUtilities.hexStringToByteArray("01030004008D000400290002802A000400000E918286FFFF00000E91");
        IpfixDataSet set = new IpfixDataSet(dataSet);
        AbstractTemplateRecord blueprint = new IpfixTemplateRecord(dataTemplateRecord);

        when(m_templateProvider.get(259)).thenReturn(blueprint);
        String hostName = "hostname";
        String family = "standard-dpu";

        when(m_deviceFamilyCacheService.getDeviceFamily(hostName)).thenReturn(family);

        InformationElement ie32 = mock(InformationElement.class);
        InformationElement ieString = mock(InformationElement.class);
        InformationElement ieUnion = mock(InformationElement.class);
        when(m_informationElementService.getInformationElement(family, 141)).thenReturn(null);
        when(m_informationElementService.getInformationElement(family, 41)).thenReturn(ieString);
        when(m_informationElementService.getInformationElement(family, 42)).thenReturn(ie32);
        when(m_informationElementService.getInformationElement(family, 646)).thenReturn(ieUnion);
        when(ieString.getName()).thenReturn("keyForString");
        when(ie32.getName()).thenReturn("keyForUnsigned32");
        when(ieUnion.getName()).thenReturn("keyForUnion");
        when(ie32.getDataType()).thenReturn(InformationElementType.UNSIGNED32);
        when(ieString.getDataType()).thenReturn(InformationElementType.STRING);
        when(ieUnion.getDataType()).thenReturn(InformationElementType.UNION);

        Set<IpfixDecodedData> decodedDataSet = m_decodingDataRecordService.decodeDataSet(hostName, 1L, set, null, m_templateProvider);
        Set<String> decodedCounterValues = new HashSet<>();
        for (IpfixDecodedData decodedData : decodedDataSet) {
            decodedCounterValues.add(decodedData.getDecodedValue());
        }
        Set<String> expectedRecord = new HashSet<>();
        expectedRecord.add("aa");
        expectedRecord.add("bb");
        expectedRecord.add("0000014D");
        expectedRecord.add("444");
        assertTrue(decodedCounterValues.containsAll(expectedRecord));
    }

}
