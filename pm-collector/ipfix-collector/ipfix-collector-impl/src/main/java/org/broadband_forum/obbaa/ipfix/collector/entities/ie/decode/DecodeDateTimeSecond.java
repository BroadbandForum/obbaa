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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.broadband_forum.obbaa.ipfix.collector.exception.DataTypeLengthMismatchException;
import org.broadband_forum.obbaa.ipfix.collector.exception.UtilityException;
import org.broadband_forum.obbaa.ipfix.collector.util.IpfixUtilities;

public class DecodeDateTimeSecond implements IpfixDataRecordDecodeMethod {

    private static final int LENGTH = 4;
    private static final String PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String TIMEZONE = "UTC";

    @Override
    public String decodeDataRecord(byte[] data, int length) throws UtilityException, DataTypeLengthMismatchException {
        if (length != LENGTH) {
            throw new DataTypeLengthMismatchException("Incorrect length for Date Time Second type: " + length);
        }
        data = IpfixUtilities.copyByteArray(data, 0, LENGTH);
        Long millisecondSinceEpoch = IpfixUtilities.bytesToLong(data, 1, 4) * 1000;
        Date date = new Date(millisecondSinceEpoch);
        DateFormat df = new SimpleDateFormat(PATTERN);
        df.setTimeZone(TimeZone.getTimeZone(TIMEZONE));
        String value = df.format(date);
        return value;
    }

}
