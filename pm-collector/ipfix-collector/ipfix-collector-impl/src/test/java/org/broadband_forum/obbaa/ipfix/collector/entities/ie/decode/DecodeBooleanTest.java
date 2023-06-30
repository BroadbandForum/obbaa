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

package org.broadband_forum.obbaa.ipfix.collector.entities.ie.decode;

import static org.junit.Assert.assertEquals;

import java.net.UnknownHostException;

import org.broadband_forum.obbaa.ipfix.entities.exception.DataTypeLengthMismatchException;
import org.broadband_forum.obbaa.ipfix.entities.exception.MissingAbstractDataTypeException;
import org.broadband_forum.obbaa.ipfix.entities.exception.UtilityException;
import org.broadband_forum.obbaa.ipfix.entities.ie.IpfixDataRecordDecodeMethod;
import org.broadband_forum.obbaa.ipfix.entities.ie.decode.DecodeBoolean;
import org.broadband_forum.obbaa.ipfix.entities.util.IpfixUtilities;
import org.junit.Test;

public class DecodeBooleanTest {

    @Test
    public void testDecodeBoolean() throws UtilityException, DataTypeLengthMismatchException, UnknownHostException, MissingAbstractDataTypeException {
        byte[] data = IpfixUtilities.hexStringToByteArray("01");
        int length = 1;
        IpfixDataRecordDecodeMethod method = new DecodeBoolean();
        String value = method.decodeDataRecord(data, length);
        assertEquals("True", value);
    }

    @Test(expected = DataTypeLengthMismatchException.class)
    public void testDecodeBooleanLengthMismatch() throws UtilityException, DataTypeLengthMismatchException, UnknownHostException, MissingAbstractDataTypeException {
        byte[] data = IpfixUtilities.hexStringToByteArray("01");
        int length = 2;
        IpfixDataRecordDecodeMethod method = new DecodeBoolean();
        String value = method.decodeDataRecord(data, length);
        assertEquals("True", value);
    }

}
