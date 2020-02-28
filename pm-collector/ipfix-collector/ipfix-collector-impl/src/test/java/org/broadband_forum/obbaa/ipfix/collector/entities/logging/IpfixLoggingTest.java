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

package org.broadband_forum.obbaa.ipfix.collector.entities.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.broadband_forum.obbaa.ipfix.collector.entities.IpfixFieldSpecifier;
import org.broadband_forum.obbaa.ipfix.collector.entities.header.IpfixMessageHeader;
import org.broadband_forum.obbaa.ipfix.collector.exception.NotEnoughBytesException;
import org.broadband_forum.obbaa.ipfix.collector.util.IpfixUtilities;
import org.junit.Ignore;
import org.junit.Test;

public class IpfixLoggingTest {

    @Test
    public void testIpfixLoggingGenerateToCorrectFormat() throws NotEnoughBytesException {
        IpfixLogging ipfixLogging = new IpfixLogging();
        ipfixLogging.setVersionNumber(10);
        ipfixLogging.setLength(64);
        ipfixLogging.setExportTime("2018-01-27T04:12:52Z");
        ipfixLogging.setSequenceNumber(1);
        ipfixLogging.setObservationDomainId(4335);

        IpfixLoggingTemplateSet ipfixLoggingTemplateSet = new IpfixLoggingTemplateSet(3, 26, 3, 600, null, null);
        IpfixLoggingTemplateRecord ipfixLoggingTemplateRecord = new IpfixLoggingTemplateRecord(1, 1, 260, 96, 3729);
        Set<IpfixLoggingTemplateRecord> templateRecords = new HashSet<>();
        templateRecords.add(ipfixLoggingTemplateRecord);
        ipfixLoggingTemplateSet.setTemplateRecords(templateRecords);
        ipfixLogging.addToSetMessages(ipfixLoggingTemplateSet);

        IpfixLoggingDataSet ipfixLoggingDataSet = new IpfixLoggingDataSet(600, 22, null, null);
        ipfixLoggingDataSet.addTemplateRecord(ipfixLoggingTemplateRecord);
        IpfixLoggingDataRecord ipfixLoggingDataRecord = new IpfixLoggingDataRecord(1, "0561646d696e", "Test");
        Set<IpfixLoggingDataRecord> dataRecords = new HashSet<>();
        dataRecords.add(ipfixLoggingDataRecord);
        ipfixLoggingDataSet.setDataRecords(dataRecords);
        ipfixLogging.addToSetMessages(ipfixLoggingDataSet);

        assertEquals(10, ipfixLogging.getVersionNumber());
        assertEquals(64, ipfixLogging.getLength());
        assertEquals(2, ipfixLogging.getSetMessages().size());

        String NBILogging = ipfixLogging.convertToLog();
        String expectedTemplateSetMessage = "{\"TemplateID\":3,\"FieldCount\":600,\"Field\":[{\"FieldID\":1,\"EnterpriseBit\":1," +
                "\"IEID\":260,\"FieldLength\":96,\"EnterpriseNumber\":3729}],\"SetID\":3,\"Length\":26}";
        assertTrue(NBILogging.contains(expectedTemplateSetMessage));

        String expectedDataSetMessage = "{\"TemplateSet\":[{\"FieldID\":1,\"EnterpriseBit\":1,\"IEID\":260,\"FieldLength\":96," +
                "\"EnterpriseNumber\":3729}],\"Record\":[{\"FieldID\":1,\"FieldValue\":\"0561646d696e (Test)\"}],\"SetID\":600,\"Length\":22}";
        assertTrue(NBILogging.contains(expectedDataSetMessage));
    }

    @Test
    public void testMessageHeader() throws NotEnoughBytesException {
        byte[] data = IpfixUtilities.hexStringToByteArray("000A00045A729A210000001300010164");
        IpfixMessageHeader messageHeader = new IpfixMessageHeader(data);

        IpfixLogging ipfixLogging = new IpfixLogging();
        ipfixLogging.setMessageHeader(messageHeader);

        assertEquals(10, ipfixLogging.getVersionNumber());
        assertEquals("2018-02-01T04:40:01Z", ipfixLogging.getExportTime());
        assertEquals(19, ipfixLogging.getSequenceNumber());
        assertEquals(65892, ipfixLogging.getObservationDomainId());
        assertEquals(4, ipfixLogging.getLength());
    }

    @Test
    public void testSetListTemplateSet() throws NotEnoughBytesException {
        byte[] data = IpfixUtilities.hexStringToByteArray("8008000500000E91");
        IpfixFieldSpecifier fsEnterprise = new IpfixFieldSpecifier(data);
        List<IpfixFieldSpecifier> listIpfixTemplateRecord = new ArrayList<>();
        listIpfixTemplateRecord.add(fsEnterprise);

        IpfixLoggingTemplateSet templateSet = new IpfixLoggingTemplateSet(2, 26, 600, 1, null, null);
        templateSet.setTemplateRecords(new ArrayList<>(listIpfixTemplateRecord));

        IpfixLogging ipfixLogging = new IpfixLogging();
        ipfixLogging.addToSetMessages(templateSet);

        Set<IpfixLoggingWrapper> setMessages = ipfixLogging.getSetMessages();
        IpfixLoggingTemplateSet ipfixTemplateSet = (IpfixLoggingTemplateSet) setMessages.iterator().next();
        Set<IpfixLoggingTemplateRecord> ipfixTemplateRecords = ipfixTemplateSet.getTemplateRecords();
        IpfixLoggingTemplateRecord ipfixFieldSpecifier = ipfixTemplateRecords.iterator().next();

        assertEquals(1, ipfixTemplateRecords.size());
        assertEquals(8, ipfixFieldSpecifier.getIeId());
        assertEquals(5, ipfixFieldSpecifier.getFieldLength());
        assertEquals(3729, ipfixFieldSpecifier.getEnterpriseNumber());
    }

}
