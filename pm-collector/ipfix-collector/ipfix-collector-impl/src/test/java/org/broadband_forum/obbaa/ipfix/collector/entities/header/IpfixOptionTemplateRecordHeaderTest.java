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
import org.broadband_forum.obbaa.ipfix.entities.header.IpfixOptionTemplateRecordHeader;
import org.broadband_forum.obbaa.ipfix.entities.util.IpfixUtilities;
import org.junit.Test;

public class IpfixOptionTemplateRecordHeaderTest {

    @Test
    public void testDecodeOptionTemplateRecordHeader() throws NotEnoughBytesException {
        // Binary:
        // 00000001 00000001
        // 00000000 00000101
        // 00000000 00000001
        byte[] data = IpfixUtilities.hexStringToByteArray("010100050001");
        IpfixOptionTemplateRecordHeader header = new IpfixOptionTemplateRecordHeader(data);
        assertEquals(257, header.getId());
        assertEquals(5, header.getFieldCount());
        assertEquals(1, header.getScopeFieldCount());
    }

}
