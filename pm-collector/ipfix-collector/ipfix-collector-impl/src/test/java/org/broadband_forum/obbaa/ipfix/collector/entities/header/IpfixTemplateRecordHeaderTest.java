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

import org.broadband_forum.obbaa.ipfix.entities.exception.NotEnoughBytesException;
import org.broadband_forum.obbaa.ipfix.entities.exception.UtilityException;
import org.broadband_forum.obbaa.ipfix.entities.header.IpfixTemplateRecordHeader;
import org.broadband_forum.obbaa.ipfix.entities.util.IpfixUtilities;
import org.junit.Test;

public class IpfixTemplateRecordHeaderTest {

    @Test
    public void testDecodeTemplateRecordHeader() throws UtilityException, NotEnoughBytesException {
        // Binary:
        //                  12345678 12345678
        // id       257     00000001 00000001
        // length   5       00000000 00000101
        byte[] data = IpfixUtilities.hexStringToByteArray("01010005");
        IpfixTemplateRecordHeader header = new IpfixTemplateRecordHeader(data);
        assertEquals(257, header.getId());
        assertEquals(5, header.getFieldCount());
    }

}
