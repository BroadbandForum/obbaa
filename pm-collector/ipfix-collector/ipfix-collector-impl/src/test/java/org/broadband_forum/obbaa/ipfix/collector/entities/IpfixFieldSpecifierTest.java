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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.broadband_forum.obbaa.ipfix.entities.IpfixFieldSpecifier;
import org.broadband_forum.obbaa.ipfix.entities.exception.NotEnoughBytesException;
import org.broadband_forum.obbaa.ipfix.entities.util.IpfixUtilities;
import org.junit.Test;

public class IpfixFieldSpecifierTest {

    @Test
    public void testDecodeFieldSpecifier() throws NotEnoughBytesException {
        // Binary:
        // 00000000000010000000000000000100
        byte[] data = IpfixUtilities.hexStringToByteArray("00080004");
        IpfixFieldSpecifier fsNotEnterprise = new IpfixFieldSpecifier(data);
        assertFalse(fsNotEnterprise.isEnterprise());
        assertEquals(8, fsNotEnterprise.getInformationElementId());
        assertEquals(4, fsNotEnterprise.getFieldLength());
        assertEquals(0, fsNotEnterprise.getEnterpriseNumber());
    }

    @Test
    public void testDecodeEnterpriseFieldSpecifier() throws NotEnoughBytesException {
        // Binary:
        // 10000000000010000000000000000101
        // 00000000000000000000111010010001
        byte[] data = IpfixUtilities.hexStringToByteArray("8008000500000E91");
        IpfixFieldSpecifier fsEnterprise = new IpfixFieldSpecifier(data);
        assertTrue(fsEnterprise.isEnterprise());
        assertEquals(8, fsEnterprise.getInformationElementId());
        assertEquals(5, fsEnterprise.getFieldLength());
        assertEquals(3729, fsEnterprise.getEnterpriseNumber());
    }

}
