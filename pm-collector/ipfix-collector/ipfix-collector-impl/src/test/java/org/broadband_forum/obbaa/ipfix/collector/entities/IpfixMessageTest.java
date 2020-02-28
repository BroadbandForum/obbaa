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

package org.broadband_forum.obbaa.ipfix.collector.entities;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.broadband_forum.obbaa.ipfix.collector.entities.header.IpfixMessageHeader;
import org.broadband_forum.obbaa.ipfix.collector.entities.record.IpfixOptionTemplateRecord;
import org.broadband_forum.obbaa.ipfix.collector.entities.set.IpfixOptionTemplateSet;
import org.broadband_forum.obbaa.ipfix.collector.exception.NotEnoughBytesException;
import org.broadband_forum.obbaa.ipfix.collector.util.IpfixUtilities;
import org.junit.Test;

public class IpfixMessageTest {

    //                          12345678 12345678 12345678 12345678
    // version 10               00000000 00001010
    // length 44                00000000 00101100
    // export time 1517460001   01011010 01110010 10011010 00100001
    // sequence number 19       00000000 00000000 00000000 00010011
    // obsr domain id 65892     00000000 00000001 00000001 01100100

    // set id 3                 00000000 00000011
    // set length 28            00000000 00011100

    // id 259                   00000001 00000011
    // field count 3            00000000 00000011
    // s field count 1          00000000 00000001

    // ieID 0|141               00000000 10001101
    // length 4                 00000000 00000100

    // ieID 0|41                00000000 00101001
    // length 2                 00000000 00000010

    // ieID 1|42                10000000 00101010
    // length 4                 00000000 00000100
    // enterprise 3729          00000000 00000000 00001110 10010001

    // padding 00000000 00000000
    @Test
    public void testDecodeMessage() throws NotEnoughBytesException {
        String hex = "000A002C5A729A2100000013000101640003001C010300030001008D000400290002802A000400000E910000";
        byte[] data = IpfixUtilities.hexStringToByteArray(hex);
        IpfixMessage message = new IpfixMessage(data);

        IpfixMessageHeader messageHeader = message.getHeader();
        assertEquals(10, messageHeader.getVersionNumber());
        assertEquals("2018-02-01T04:40:01Z", messageHeader.getExportTime());
        assertEquals(19, messageHeader.getSequenceNumber());
        assertEquals(65892, messageHeader.getObservationDomainId());
        assertEquals(44, messageHeader.getLength());

        IpfixOptionTemplateSet set = message.getOptionTemplateSets().get(0);
        assertEquals(3, set.getHeader().getId());
        assertEquals(28, set.getHeader().getLength());

        IpfixOptionTemplateRecord otr = set.getRecords().get(0);
        assertEquals(259, otr.getHeader().getId());
        assertEquals(3, otr.getHeader().getFieldCount());
        assertEquals(1, otr.getHeader().getScopeFieldCount());

        IpfixFieldSpecifier fsScope = otr.getFieldSpecifiers().get(0);
        assertFalse(fsScope.isEnterprise());
        assertEquals(141, fsScope.getInformationElementId());
        assertEquals(4, fsScope.getFieldLength());

        IpfixFieldSpecifier fsNotEnterprise = otr.getFieldSpecifiers().get(1);
        assertFalse(fsNotEnterprise.isEnterprise());
        assertEquals(41, fsNotEnterprise.getInformationElementId());
        assertEquals(2, fsNotEnterprise.getFieldLength());

        IpfixFieldSpecifier fsEnterprise = otr.getFieldSpecifiers().get(2);
        assertTrue(fsEnterprise.isEnterprise());
        assertEquals(42, fsEnterprise.getInformationElementId());
        assertEquals(4, fsEnterprise.getFieldLength());
        assertEquals(3729, fsEnterprise.getEnterpriseNumber());

        byte[] expectedPadding = IpfixUtilities.hexStringToByteArray("0000");
        assertArrayEquals(expectedPadding, set.getPadding());

        assertArrayEquals(data, message.getPresentation());
    }

    @Test
    public void testSetOptionTemplateSets() throws NotEnoughBytesException {
        String hex = "000A002C5A729A2100000013000101640003001C010300030001008D000400290002802A000400000E910000";
        byte[] data = IpfixUtilities.hexStringToByteArray(hex);
        IpfixMessage message = new IpfixMessage(data);
        message.setOptionTemplateSets(null);
    }

    @Test
    public void testGetDataSets() throws NotEnoughBytesException {
        String hex = "000A002C5A729A2100000013000101640003001C010300030001008D000400290002802A000400000E910000";
        byte[] data = IpfixUtilities.hexStringToByteArray(hex);
        IpfixMessage message = new IpfixMessage(data);
        message.getDataSets();
    }

    @Test
    public void testSetDataSets() throws NotEnoughBytesException {
        String hex = "000A002C5A729A2100000013000101640003001C010300030001008D000400290002802A000400000E910000";
        byte[] data = IpfixUtilities.hexStringToByteArray(hex);
        IpfixMessage message = new IpfixMessage(data);
        message.setDataSets(null);
    }

}
