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

package org.broadband_forum.obbaa.ipfix.entities.ie.decode;

import org.broadband_forum.obbaa.ipfix.entities.exception.DataTypeLengthMismatchException;
import org.broadband_forum.obbaa.ipfix.entities.exception.UtilityException;
import org.broadband_forum.obbaa.ipfix.entities.ie.IpfixDataRecordDecodeMethod;
import org.broadband_forum.obbaa.ipfix.entities.util.IpfixUtilities;

public class DecodeSigned32 implements IpfixDataRecordDecodeMethod {

    private static final int LENGTH = 4;

    @Override
    public String decodeDataRecord(byte[] data, int length) throws UtilityException, DataTypeLengthMismatchException {
        if (length != LENGTH) {
            throw new DataTypeLengthMismatchException("Incorrect length for Signed32 type: " + length);
        }
        data = IpfixUtilities.copyByteArray(data, 0, LENGTH);
        return Long.toString((IpfixUtilities.bytesToLong(data, 0, LENGTH)));
    }

}
