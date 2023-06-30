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

package org.broadband_forum.obbaa.ipfix.entities.ie;

import java.net.UnknownHostException;

import org.broadband_forum.obbaa.ipfix.entities.exception.DataTypeLengthMismatchException;
import org.broadband_forum.obbaa.ipfix.entities.exception.MissingAbstractDataTypeException;
import org.broadband_forum.obbaa.ipfix.entities.exception.UtilityException;

public interface IpfixDataRecordDecodeMethod {

    // DataRecord data is byte[] which represent a list of all data record in a
    // Set
    // Therefore, we have to update data for the next decoding process.
    // Return value of a data corresponding to a field specifier in a template
    String decodeDataRecord(byte[] data, int length) throws UtilityException, DataTypeLengthMismatchException,
            UnknownHostException, MissingAbstractDataTypeException;
}
