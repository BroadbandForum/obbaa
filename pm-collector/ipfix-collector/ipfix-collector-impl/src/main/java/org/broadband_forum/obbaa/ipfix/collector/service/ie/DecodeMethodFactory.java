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

package org.broadband_forum.obbaa.ipfix.collector.service.ie;

import org.broadband_forum.obbaa.ipfix.collector.entities.ie.InformationElementType;
import org.broadband_forum.obbaa.ipfix.collector.entities.ie.decode.DecodeBoolean;
import org.broadband_forum.obbaa.ipfix.collector.entities.ie.decode.DecodeDateTimeSecond;
import org.broadband_forum.obbaa.ipfix.collector.entities.ie.decode.DecodeEnumeration;
import org.broadband_forum.obbaa.ipfix.collector.entities.ie.decode.DecodeFloat64;
import org.broadband_forum.obbaa.ipfix.collector.entities.ie.decode.DecodeIPv4Address;
import org.broadband_forum.obbaa.ipfix.collector.entities.ie.decode.DecodeIPv6ddress;
import org.broadband_forum.obbaa.ipfix.collector.entities.ie.decode.DecodeMacAddress;
import org.broadband_forum.obbaa.ipfix.collector.entities.ie.decode.DecodeSigned16;
import org.broadband_forum.obbaa.ipfix.collector.entities.ie.decode.DecodeSigned32;
import org.broadband_forum.obbaa.ipfix.collector.entities.ie.decode.DecodeSigned64;
import org.broadband_forum.obbaa.ipfix.collector.entities.ie.decode.DecodeSigned8;
import org.broadband_forum.obbaa.ipfix.collector.entities.ie.decode.DecodeString;
import org.broadband_forum.obbaa.ipfix.collector.entities.ie.decode.DecodeUnion;
import org.broadband_forum.obbaa.ipfix.collector.entities.ie.decode.DecodeUnsigned16;
import org.broadband_forum.obbaa.ipfix.collector.entities.ie.decode.DecodeUnsigned32;
import org.broadband_forum.obbaa.ipfix.collector.entities.ie.decode.DecodeUnsigned64;
import org.broadband_forum.obbaa.ipfix.collector.entities.ie.decode.DecodeUnsigned8;
import org.broadband_forum.obbaa.ipfix.collector.entities.ie.decode.IpfixDataRecordDecodeMethod;
import org.broadband_forum.obbaa.ipfix.collector.exception.MissingAbstractDataTypeException;

public class DecodeMethodFactory {

    public IpfixDataRecordDecodeMethod getDecodeMethodByIEType(InformationElementType type) throws MissingAbstractDataTypeException {
        switch (type) {
            case UNSIGNED8:
                return new DecodeUnsigned8();
            case UNSIGNED16:
                return new DecodeUnsigned16();
            case UNSIGNED32:
                return new DecodeUnsigned32();
            case UNSIGNED64:
                return new DecodeUnsigned64();
            case SIGNED8:
                return new DecodeSigned8();
            case SIGNED16:
                return new DecodeSigned16();
            case SIGNED32:
                return new DecodeSigned32();
            case SIGNED64:
                return new DecodeSigned64();
            case BOOLEAN:
                return new DecodeBoolean();
            case DATATIMESECONDS:
                return new DecodeDateTimeSecond();
            case STRING:
                return new DecodeString();
            case FLOAT64:
                return new DecodeFloat64();
            case IPV4ADDRESS:
                return new DecodeIPv4Address();
            case IPV6ADDRESS:
                return new DecodeIPv6ddress();
            case MACADDRESS:
                return new DecodeMacAddress();
            case ENUMERATION:
                return new DecodeEnumeration();
            case UNION:
                return new DecodeUnion();
            default:
                throw new MissingAbstractDataTypeException("Cannot found abstract data type");
        }
    }

    public IpfixDataRecordDecodeMethod getDecodeMethodByUnionKey(int unionKey) throws MissingAbstractDataTypeException {
        switch (unionKey) {
            case 1:
                return new DecodeUnsigned8();
            case 2:
                return new DecodeUnsigned16();
            case 3:
                return new DecodeUnsigned32();
            case 4:
                return new DecodeUnsigned64();
            case 5:
                return new DecodeSigned8();
            case 6:
                return new DecodeSigned16();
            case 7:
                return new DecodeSigned32();
            case 8:
                return new DecodeSigned64();
            case 11:
                return new DecodeBoolean();
            case 12:
                return new DecodeMacAddress();
            case 13:
                return new DecodeString();
            case 14:
                return new DecodeDateTimeSecond();
            case 18:
                return new DecodeIPv4Address();
            case 19:
                return new DecodeIPv6ddress();
            case 254:
                return new DecodeEnumeration();
            case 255:
                return new DecodeUnion();
            default:
                throw new MissingAbstractDataTypeException("Cannot found abstract data type");
        }
    }
}
