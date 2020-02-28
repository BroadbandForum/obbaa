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
import org.broadband_forum.obbaa.ipfix.collector.exception.UtilityException;
import org.broadband_forum.obbaa.ipfix.collector.util.IpfixUtilities;
import org.junit.Test;

public class IpfixSetHeaderTest {

    @Test
    public void testDecodeSetHeaderTest() throws UtilityException, NotEnoughBytesException {
        // Binary:
        //                         12345678 12345678
        // id              3       00000000 00000011
        // length          24      00000000 00011000
        byte[] data = IpfixUtilities.hexStringToByteArray("00030018");
        IpfixSetHeader header = new IpfixSetHeader(data);
        assertEquals(3, header.getId());
        assertEquals(24, header.getLength());
    }

}
