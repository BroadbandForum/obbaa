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

package org.broadband_forum.obbaa.ipfix.collector.entities.header;

import static org.junit.Assert.assertEquals;

import org.broadband_forum.obbaa.ipfix.collector.exception.NotEnoughBytesException;
import org.broadband_forum.obbaa.ipfix.collector.util.IpfixUtilities;
import org.junit.Test;

public class IpfixMessageHeaderTest {

    //                              12345678 12345678 12345678 12345678
    // version         10           00000000 00001010
    // length          4            00000000 00000100
    // export time     1517460001   01011010 01110010 10011010 00100001
    // sequence number 19           00000000 00000000 00000000 00010011
    // obsr domain id  65892        00000000 00000001 00000001 01100100
    @Test
    public void testDecodeMessageHeader() throws NotEnoughBytesException {
        byte[] data = IpfixUtilities.hexStringToByteArray("000A00045A729A210000001300010164");
        IpfixMessageHeader messageHeader = new IpfixMessageHeader(data);
        assertEquals(10, messageHeader.getVersionNumber());
        assertEquals("2018-02-01T04:40:01Z", messageHeader.getExportTime());
        assertEquals(19, messageHeader.getSequenceNumber());
        assertEquals(65892, messageHeader.getObservationDomainId());
        assertEquals(4, messageHeader.getLength());
    }

}
