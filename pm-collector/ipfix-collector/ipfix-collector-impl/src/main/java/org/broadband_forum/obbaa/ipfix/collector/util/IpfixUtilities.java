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

package org.broadband_forum.obbaa.ipfix.collector.util;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.broadband_forum.obbaa.ipfix.collector.entities.ie.decode.IpfixDataRecordDecodeMethod;
import org.broadband_forum.obbaa.ipfix.collector.entities.record.AbstractTemplateRecord;
import org.broadband_forum.obbaa.ipfix.collector.entities.record.IpfixOptionTemplateRecord;
import org.broadband_forum.obbaa.ipfix.collector.entities.record.IpfixTemplateRecord;
import org.broadband_forum.obbaa.ipfix.collector.exception.DataTypeLengthMismatchException;
import org.broadband_forum.obbaa.ipfix.collector.exception.MissingAbstractDataTypeException;
import org.broadband_forum.obbaa.ipfix.collector.exception.UtilityException;
import org.broadband_forum.obbaa.ipfix.collector.service.ie.DecodeMethodFactory;

public final class IpfixUtilities {

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private static final String BYTE_ARRAY_TOO_SHORT = "Byte array too short!";

    private IpfixUtilities() {
    }

    public static int oneByteToInteger(byte value, int signum) {
        return (signum == 1) ? (int) value & 0xFF : (int) value;
    }

    public static int byteToInteger(byte[] value, int signum, int numberOctets) throws UtilityException {
        validate(value, numberOctets);

        return (signum == 1) ? new BigInteger(1, value).intValue() : new BigInteger(value).intValue();
    }

    public static long bytesToLong(byte[] value, int signum, int numberOctets) throws UtilityException {
        validate(value, numberOctets);

        return (signum == 1) ? new BigInteger(1, value).longValue() : new BigInteger(value).longValue();
    }

    public static float bytesToFloat(byte[] value, int numberOctets) throws UtilityException {
        validate(value, numberOctets);
        return ByteBuffer.wrap(value).getFloat();
    }

    public static String bytesToIPv4OrIPv6Address(byte[] value, int numberOctets) throws UtilityException, UnknownHostException {
        validate(value, numberOctets);
        return InetAddress.getByAddress(value).getHostAddress();
    }

    public static String bytesToMacAddress(byte[] value, int numberOctets) throws UtilityException {
        validate(value, numberOctets);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length; i++) {
            sb.append(String.format("%02X%s", value[i], (i < value.length - 1) ? ":" : ""));
        }
        return sb.toString();
    }

    public static String bytesToEnum(byte[] value, int numberOctets) throws UtilityException {
        validate(value, numberOctets);

        String enumStr = "enum";
        enumStr += bytesToLong(value, 0, numberOctets);
        return enumStr;
    }

    private static void validate(byte[] value, int numberOctets) throws UtilityException {
        if (value.length < numberOctets) {
            throw new UtilityException(BYTE_ARRAY_TOO_SHORT);
        }
    }

    public static String bytesToUnion(byte[] value) throws UtilityException, DataTypeLengthMismatchException,
            UnknownHostException, MissingAbstractDataTypeException {
        int dataLength = oneByteToInteger(value[0], 1);
        int dataType = oneByteToInteger(value[1], 1);
        byte[] data = copyByteArray(value, 2, dataLength - 1);

        if (dataType == 0) {
            return bytesToHex(data);
        }

        DecodeMethodFactory decodeMethodFactory = new DecodeMethodFactory();
        IpfixDataRecordDecodeMethod decodeMethod = decodeMethodFactory.getDecodeMethodByUnionKey(dataType);
        return decodeMethod.decodeDataRecord(data, data.length);
    }

    public static byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }

    public static int getBit(byte[] data, int pos) {
        int posByte = pos / 8;
        int posBit = pos % 8;
        byte valByte = data[posByte];
        return valByte >> (8 - (posBit + 1)) & 0x0001;
    }

    public static byte[] copyByteArray(byte[] data, int start, int fieldLength) {
        byte[] result = new byte[0];
        int end = start + fieldLength;
        return (end > data.length) ? result : Arrays.copyOfRange(data, start, end);
    }

    public static String byteArrayToString(byte[] data) {
        return new String(data);
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int hexValue = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[hexValue >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[hexValue & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] concatByteArray(byte[] firstByteArray, byte[] secondByteArray) {
        byte[] concatenatedByteArray = new byte[firstByteArray.length + secondByteArray.length];
        System.arraycopy(firstByteArray, 0, concatenatedByteArray, 0, firstByteArray.length);
        System.arraycopy(secondByteArray, 0, concatenatedByteArray, firstByteArray.length, secondByteArray.length);
        return concatenatedByteArray;
    }

    public static int getTemplateId(AbstractTemplateRecord record) {
        if (record instanceof IpfixTemplateRecord) {
            return ((IpfixTemplateRecord) record).getHeader().getId();
        }
        return ((IpfixOptionTemplateRecord) record).getHeader().getId();
    }
}
