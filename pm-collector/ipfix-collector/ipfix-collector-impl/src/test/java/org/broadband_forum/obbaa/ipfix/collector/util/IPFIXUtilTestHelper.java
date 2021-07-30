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

import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.digest.Crypt;
import org.broadband_forum.obbaa.ipfix.collector.entities.header.IpfixMessageHeader;
import org.broadband_forum.obbaa.ipfix.collector.entities.header.IpfixOptionTemplateRecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IPFIXUtilTestHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(IPFIXUtilTestHelper.class);
    private static final String SHA512_PREFIX = "$6$";
    private static final String CLEAR_PASS_PREFIX = "$0$";
    private static final int OPTION_TEMPLATE_SET_ID = 3;
    private static final int TEMPLATE_SET_ID = 2;
    private static final int HEADER_LENGTH = 4;
    private static final int ENTERPRISE_LENGTH = 8;
    private static final int OBSERVATION_DOMAIN_ID_DEFAULT = 4335;
    private static final int TEMPLATE_ID_MIN = 256;
    private static final int TEMPLATE_ID_MAX = 65535;

    // Hard-coded for ipfix message when there is not enough time
    // The list of parameters can be hard-coded for the moment as the template is also hardcoded.
    public static byte[] collectMessage(String hostName, String password, String userName) {
        // OptionsTL/ TL: admin/pass
        // Data fix

        int templateId = 256;
        int templateFieldCount = 46;
        int optionTemplateId = 600;
        int optionTemplateFieldCount = 3;
        int optionTemplateScopeFieldCount = 1; //The Scope Field Count MUST NOT be zero.

        // Option TL first
        byte[] optionTemplateRecordHeader = buildOptionTemplateRecordHeader(optionTemplateId, optionTemplateFieldCount, optionTemplateScopeFieldCount);
        byte[] optionTemplateRecords = buildOptionTemplateRecords();
        int optionTLSetLength = HEADER_LENGTH + IpfixOptionTemplateRecordHeader.TOTAL_LENGTH + optionTemplateRecords.length;
        byte[] optionTemplateSetHeader = buildIPFixSetId(OPTION_TEMPLATE_SET_ID, optionTLSetLength);
        byte[] optionTemplateSet = new byte[optionTLSetLength];
        combineArrays(optionTemplateSet, Arrays.asList(optionTemplateSetHeader, optionTemplateRecordHeader, optionTemplateRecords));

        // Data option template next
        byte[] dataRecordsOptionTL = buildDataRecordsOptionTL(hostName, userName, password);
        int dataSetOptionTLLength = HEADER_LENGTH + dataRecordsOptionTL.length;
        byte[] dataSetOptionTLHeader = buildIPFixSetId(optionTemplateId, dataSetOptionTLLength); // dataSetID > 255
        byte[] dataSetOptionTL = new byte[dataSetOptionTLLength];
        combineArrays(dataSetOptionTL, Arrays.asList(dataSetOptionTLHeader, dataRecordsOptionTL));

        // Template next
        byte[] templateRecordHeader = buildTemplateRecordHeader(templateId, templateFieldCount);
        byte[] templateSetRecords = buildTemplateSetRecords();
        int templateSetLength = HEADER_LENGTH + templateRecordHeader.length + templateSetRecords.length;
        byte[] templateSetHeader = buildIPFixSetId(TEMPLATE_SET_ID, templateSetLength);
        byte[] templateSet = new byte[templateSetLength];
        combineArrays(templateSet, Arrays.asList(templateSetHeader, templateRecordHeader, templateSetRecords));

        // Data Template
        byte[] dataRecordsTL = buildDataRecordsTL();
        int dataSetTLLength = IpfixMessageHeader.TOTAL_LENGTH + dataRecordsTL.length;
        byte[] dataSetTLHeader = buildIPFixSetId(templateId, dataSetTLLength); // dataSetID > 255
        byte[] dataSetTL = new byte[dataSetTLLength];
        combineArrays(dataSetTL, Arrays.asList(dataSetTLHeader, dataRecordsTL));

        // msg header
        int ipfixVersion = 10;
        long exportTime = Instant.now().getEpochSecond();
        long sequenceNumber = 0; //dummy sequence
        long observationDomainId = OBSERVATION_DOMAIN_ID_DEFAULT; //fixed to 4355 (in case of SD-DPU).

        int totalIPFixMessageSetLength = IpfixMessageHeader.TOTAL_LENGTH + optionTLSetLength + dataSetOptionTLLength + templateSetLength + dataSetTLLength;
        byte[] ipfixMessageHeader = encodeIPFIXHeader(ipfixVersion, totalIPFixMessageSetLength, exportTime, sequenceNumber, observationDomainId);
        byte[] ipfixMessageSet = new byte[totalIPFixMessageSetLength];

        return combineArrays(ipfixMessageSet, Arrays.asList(ipfixMessageHeader, optionTemplateSet, dataSetOptionTL, templateSet, dataSetTL));
    }

    private static byte[] buildOptionTemplateRecordHeader(int templateId, int fieldCount, int scopeFieldCount) {
        if (scopeFieldCount == 0) {
            LOGGER.error("The Scope Field Count MUST NOT be zero.");
            return null;
        }

        byte[] data = new byte[IpfixOptionTemplateRecordHeader.TOTAL_LENGTH];
        byte[] scopeFieldCountInBytes = parseTwoBytes(scopeFieldCount);
        return combineArrays(data, Arrays.asList(buildTemplateRecordHeader(templateId, fieldCount), scopeFieldCountInBytes));
    }

    private static byte[] parseValueToBytes(long num, int length) {
        byte[] data = new byte[length];
        int offset = 0;
        for (int i = length - 1; i >= 0; i--) {
            data[i] = (byte) ((num >> offset) & 0xFF);
            offset += Byte.SIZE;
        }
        return data;
    }

    private static byte[] buildTemplateRecordHeader(int templateId, int fieldCount) {
        // Template ID in the range 256 to 65535
        if (templateId < TEMPLATE_ID_MIN || templateId > TEMPLATE_ID_MAX) {
            LOGGER.error("Template ID " + templateId + " must be in the range 256 to 65535.");
            return null;
        }
        return buildTwoFieldsFourBytes(templateId, fieldCount);
    }

    private static byte[] combineArrays(byte[] dest, List<byte[]> arrays, int destPos) {
        int start = destPos;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, dest, start, array.length);
            start += array.length;
        }

        return dest;
    }

    private static byte[] combineArrays(byte[] dest, List<byte[]> arrays) {
        return combineArrays(dest, arrays, 0);
    }

    private static byte parseOneByte(int num) {
        return (byte) (num & 0xFF);
    }

    private static byte[] parseTwoBytes(int num) {
        return parseValueToBytes(num, 2);
    }

    private static byte[] parseFourBytes(long num) {
        return parseValueToBytes(num, 4);
    }

    private static byte[] buildDataRecordsOptionTL(String hostname, String username, String password) {
        // hostname
        String dataRecord1Val = hostname;
        byte[] dataRecord1InBytes = dataRecord1Val.getBytes();
        byte[] dataRecord1 = buildDataRecordWithLength(dataRecord1InBytes);

        // username
        String dataRecord2Val = username;
        byte[] dataRecord2InBytes = dataRecord2Val.getBytes();
        byte[] dataRecord2 = buildDataRecordWithLength(dataRecord2InBytes);

        // password
        String dataRecord3Val = password;
        if (dataRecord3Val.startsWith(CLEAR_PASS_PREFIX)) {
            dataRecord3Val = Crypt.crypt(dataRecord3Val.substring(CLEAR_PASS_PREFIX.length()));
        } else if (!dataRecord3Val.startsWith(SHA512_PREFIX)) {
            LOGGER.error("Not support encode for pass with prefix: " + dataRecord3Val.substring(0, 3));
            return null;
        }

        byte[] dataRecord3InBytes = dataRecord3Val.getBytes();
        byte[] dataRecord3 = buildDataRecordWithLength(dataRecord3InBytes);

        int totalLength = dataRecord1.length + dataRecord2.length + dataRecord3.length;
        byte[] data = new byte[totalLength];
        return combineArrays(data, Arrays.asList(dataRecord1, dataRecord2, dataRecord3));
    }

    private static byte[] buildDataRecordWithLength(byte[] dataRecord) {
        int dataLength = dataRecord.length;
        if (dataLength < 255) {
            byte[] destRecord = new byte[dataLength + 1];
            destRecord[0] = parseOneByte(dataLength);
            System.arraycopy(dataRecord, 0, destRecord, 1, dataLength);
            return destRecord;
        } else {
            byte[] destRecord = new byte[dataLength + 3];
            destRecord[0] = parseOneByte(255);
            return combineArrays(destRecord, Arrays.asList(parseTwoBytes(dataLength), dataRecord), 1);
        }
    }

    private static byte[] buildOptionTemplateRecords() {
        byte[] ipfixScopeField = buildIpfixFieldSpecifier(5002, 65535, 3729);   // hostname
        byte[] ipfixFieldSpecifier1 = buildIpfixFieldSpecifier(8636, 65535, 3729);  // username
        byte[] ipfixFieldSpecifier2 = buildIpfixFieldSpecifier(8637, 65535, 3729);  // password

        int totalLength = ipfixScopeField.length + ipfixFieldSpecifier1.length + ipfixFieldSpecifier2.length;
        byte[] data = new byte[totalLength];
        return combineArrays(data, Arrays.asList(ipfixScopeField, ipfixFieldSpecifier1, ipfixFieldSpecifier2));
    }

    private static byte[] buildIpfixFieldSpecifier(int ieId, int fieldLength, long enterpriseNum) {
        byte[] record1 = buildIpfixFieldSpecifier(ieId, fieldLength);
        if (enterpriseNum > 0) {
            byte[] data = new byte[ENTERPRISE_LENGTH];
            encodeEnterpriseBit(record1);
            byte[] record2 = parseFourBytes(enterpriseNum);
            return combineArrays(data, Arrays.asList(record1, record2));
        }
        return record1;
    }

    private static byte[] buildIpfixFieldSpecifier(int ieId, int fieldLength) {
        return buildTwoFieldsFourBytes(ieId, fieldLength);
    }

    private static void encodeEnterpriseBit(byte[] data) {
        data[0] = (byte) (data[0] | 0x80);
    }

    private static byte[] buildTwoFieldsFourBytes(int firstFieldTwoBytes, int secondFieldTwoBytes) {
        byte[] data = new byte[4];
        byte[] firstFieldInBytes = parseTwoBytes(firstFieldTwoBytes);
        byte[] secondFieldInBytes = parseTwoBytes(secondFieldTwoBytes);
        combineArrays(data, Arrays.asList(firstFieldInBytes, secondFieldInBytes));
        return data;
    }

    private static byte[] buildIPFixSetId(int setId, int length) {
        return buildTwoFieldsFourBytes(setId, length);
    }

    private static byte[] buildDataRecordsTL() {
        String dataRecord0Val = "DSL1";
        byte[] dataRecord0InBytes = dataRecord0Val.getBytes();
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        String timestamp = String.valueOf(currentTime.toInstant());
        byte[] dataRecordInBytes = timestamp.getBytes();

        byte[] dataRecord0 = buildDataRecordWithLength(dataRecord0InBytes); // string
        byte[] dataRecord1 = parseFourBytes(1); // enum
        byte[] dataRecord2 = parseFourBytes(1); // enum

        byte[] dataRecord3 = parseFourBytes(1); // unsigned32
        byte[] dataRecord4 = parseBooleanToByteArray(false);
        byte[] dataRecord5 = buildDataRecordWithLength(dataRecordInBytes); // string
        byte[] dataRecord6 = parseFourBytes(1); // unsigned32
        byte[] dataRecord7 = parseFourBytes(1); // unsigned32
        byte[] dataRecord8 = parseFourBytes(1); // unsigned32
        byte[] dataRecord9 = parseFourBytes(1); // unsigned32
        byte[] dataRecord10 = parseFourBytes(1); // unsigned32

        byte[] dataRecord11 = parseFourBytes(1); // unsigned32
        byte[] dataRecord12 = parseFourBytes(1); // unsigned32
        byte[] dataRecord13 = parseFourBytes(1); // unsigned32
        byte[] dataRecord14 = parseFourBytes(1); // unsigned32
        byte[] dataRecord15 = parseFourBytes(1); // unsigned32
        byte[] dataRecord16 = parseFourBytes(1); // unsigned32
        byte[] dataRecord17 = parseFourBytes(1); // unsigned32
        byte[] dataRecord18 = parseFourBytes(1); // unsigned32
        byte[] dataRecord19 = parseFourBytes(1); // unsigned32
        byte[] dataRecord20 = parseFourBytes(1); // unsigned32

        byte[] dataRecord21 = parseFourBytes(1); // unsigned32
        byte[] dataRecord22 = parseFourBytes(1); // unsigned32
        byte[] dataRecord23 = parseFourBytes(1); // unsigned32
        byte[] dataRecord24 = parseFourBytes(1); // unsigned32
        byte[] dataRecord25 = parseFourBytes(1); // unsigned32
        byte[] dataRecord26 = parseFourBytes(1); // unsigned32
        byte[] dataRecord27 = parseFourBytes(1); // unsigned32
        byte[] dataRecord28 = parseFourBytes(1); // unsigned32
        byte[] dataRecord29 = parseFourBytes(1); // unsigned32
        byte[] dataRecord30 = parseFourBytes(1); // unsigned32

        byte[] dataRecord31 = parseBooleanToByteArray(false); // boolean
        byte[] dataRecord32 = buildDataRecordWithLength(dataRecordInBytes); // string
        byte[] dataRecord33 = parseFourBytes(1); // unsigned32
        byte[] dataRecord34 = parseFourBytes(1); // unsigned32
        byte[] dataRecord35 = parseFourBytes(1); // unsigned32
        byte[] dataRecord36 = parseFourBytes(1); // unsigned32
        byte[] dataRecord37 = parseFourBytes(1); // unsigned32
        byte[] dataRecord38 = parseFourBytes(1); // unsigned32
        byte[] dataRecord39 = parseFourBytes(1); // unsigned32
        byte[] dataRecord40 = parseFourBytes(1); // unsigned32

        byte[] dataRecord41 = parseFourBytes(1); // unsigned32
        byte[] dataRecord42 = parseFourBytes(1); // unsigned32
        byte[] dataRecord43 = parseFourBytes(1); // unsigned32
        byte[] dataRecord44 = parseFourBytes(1); // unsigned32
        byte[] dataRecord45 = parseFourBytes(1); // unsigned32

        int totalLength = dataRecord0.length
                + dataRecord1.length + dataRecord2.length + dataRecord3.length + dataRecord4.length + dataRecord5.length
                + dataRecord6.length + dataRecord7.length + dataRecord8.length + dataRecord9.length + dataRecord10.length
                + dataRecord11.length + dataRecord12.length + dataRecord13.length + dataRecord14.length + dataRecord15.length
                + dataRecord16.length + dataRecord17.length + dataRecord18.length + dataRecord19.length + dataRecord20.length
                + dataRecord21.length + dataRecord22.length + dataRecord23.length + dataRecord24.length + dataRecord25.length
                + dataRecord26.length + dataRecord27.length + dataRecord28.length + dataRecord29.length + dataRecord30.length
                + dataRecord31.length + dataRecord32.length + dataRecord33.length + dataRecord34.length + dataRecord35.length
                + dataRecord36.length + dataRecord37.length + dataRecord38.length + dataRecord39.length + dataRecord40.length
                + dataRecord41.length + dataRecord42.length + dataRecord43.length + dataRecord44.length + dataRecord45.length;

        byte[] data = new byte[totalLength];

        return combineArrays(data, Arrays.asList(dataRecord0,
                dataRecord1, dataRecord2, dataRecord3, dataRecord4, dataRecord5,
                dataRecord6, dataRecord7, dataRecord8, dataRecord9, dataRecord10,
                dataRecord11, dataRecord12, dataRecord13, dataRecord14, dataRecord15,
                dataRecord16, dataRecord17, dataRecord18, dataRecord19, dataRecord20,
                dataRecord21, dataRecord22, dataRecord23, dataRecord24, dataRecord25,
                dataRecord26, dataRecord27, dataRecord28, dataRecord29, dataRecord30,
                dataRecord31, dataRecord32, dataRecord33, dataRecord34, dataRecord35,
                dataRecord36, dataRecord37, dataRecord38, dataRecord39, dataRecord40,
                dataRecord41, dataRecord42, dataRecord43, dataRecord44, dataRecord45));
    }

    private static byte[] parseBooleanToByteArray(boolean value) {
        byte[] data = new byte[1];
        data[0] = (value) ? (byte) 1 : (byte) 0;
        return data;
    }

    private static byte[] floatToBytes(float value, int length) {
        byte[] bytes = new byte[length];
        ByteBuffer.wrap(bytes).putFloat(value);
        return bytes;
    }

    private static byte[] encodeIPFIXHeader(int ipfixVersion, int length, long exportTime, long sequenceNumber, long observationDomainId) {
        byte[] data = new byte[IpfixMessageHeader.TOTAL_LENGTH];
        byte[] ipfixVersionInBytes = parseTwoBytes(ipfixVersion);
        byte[] lengthInBytes = parseTwoBytes(length);
        byte[] exportTimeInBytes = parseFourBytes(exportTime);
        byte[] sequenceNumberInBytes = parseFourBytes(sequenceNumber);
        byte[] observationDomainIdInBytes = parseFourBytes(observationDomainId);

        return combineArrays(data, Arrays.asList(ipfixVersionInBytes, lengthInBytes, exportTimeInBytes,
                sequenceNumberInBytes, observationDomainIdInBytes));
    }

    private static byte[] buildTemplateSetRecords() {
        // 46 counters

        byte[] ipfixFieldSpecifier0 = buildIpfixFieldSpecifier(263, 65535, 3729);   // string
        byte[] ipfixFieldSpecifier1 = buildIpfixFieldSpecifier(265, 4, 3729);   // enum
        byte[] ipfixFieldSpecifier2 = buildIpfixFieldSpecifier(266, 4, 3729);   //enum

        byte[] ipfixFieldSpecifier3 = buildIpfixFieldSpecifier(462, 4, 3729);   // unsigned32
        byte[] ipfixFieldSpecifier4 = buildIpfixFieldSpecifier(463, 1, 3729);   // boolean
        byte[] ipfixFieldSpecifier5 = buildIpfixFieldSpecifier(464, 65535, 3729);   // string
        byte[] ipfixFieldSpecifier6 = buildIpfixFieldSpecifier(465, 4, 3729);   // unsigned32
        byte[] ipfixFieldSpecifier7 = buildIpfixFieldSpecifier(466, 4, 3729);   // unsigned32
        byte[] ipfixFieldSpecifier8 = buildIpfixFieldSpecifier(467, 4, 3729);   // unsigned32
        byte[] ipfixFieldSpecifier9 = buildIpfixFieldSpecifier(468, 4, 3729);   // unsigned32
        byte[] ipfixFieldSpecifier10 = buildIpfixFieldSpecifier(469, 4, 3729);   // unsigned32

        byte[] ipfixFieldSpecifier11 = buildIpfixFieldSpecifier(470, 4, 3729);   // unsigned32
        byte[] ipfixFieldSpecifier12 = buildIpfixFieldSpecifier(471, 4, 3729);   // unsigned32
        byte[] ipfixFieldSpecifier13 = buildIpfixFieldSpecifier(472, 4, 3729);   // unsigned32
        byte[] ipfixFieldSpecifier14 = buildIpfixFieldSpecifier(473, 4, 3729);   // unsigned32
        byte[] ipfixFieldSpecifier15 = buildIpfixFieldSpecifier(474, 4, 3729);   // unsigned32
        byte[] ipfixFieldSpecifier16 = buildIpfixFieldSpecifier(475, 4, 3729);   // unsigned32
        byte[] ipfixFieldSpecifier17 = buildIpfixFieldSpecifier(476, 4, 3729);   // unsigned32
        byte[] ipfixFieldSpecifier18 = buildIpfixFieldSpecifier(477, 4, 3729);   // unsigned32
        byte[] ipfixFieldSpecifier19 = buildIpfixFieldSpecifier(478, 4, 3729);   // unsigned32
        byte[] ipfixFieldSpecifier20 = buildIpfixFieldSpecifier(479, 4, 3729);   // unsigned32

        byte[] ipfixFieldSpecifier21 = buildIpfixFieldSpecifier(480, 4, 3729);   // unsigned32
        byte[] ipfixFieldSpecifier22 = buildIpfixFieldSpecifier(481, 4, 3729);   // unsigned32
        byte[] ipfixFieldSpecifier23 = buildIpfixFieldSpecifier(482, 4, 3729);   // unsigned32
        byte[] ipfixFieldSpecifier24 = buildIpfixFieldSpecifier(483, 4, 3729);   // unsigned32
        byte[] ipfixFieldSpecifier25 = buildIpfixFieldSpecifier(484, 4, 3729);   // unsigned32
        byte[] ipfixFieldSpecifier26 = buildIpfixFieldSpecifier(485, 4, 3729);   // unsigned32
        byte[] ipfixFieldSpecifier27 = buildIpfixFieldSpecifier(486, 4, 3729);   // unsigned32
        byte[] ipfixFieldSpecifier28 = buildIpfixFieldSpecifier(487, 4, 3729);   // unsigned32
        byte[] ipfixFieldSpecifier29 = buildIpfixFieldSpecifier(488, 4, 3729);   // unsigned32
        byte[] ipfixFieldSpecifier30 = buildIpfixFieldSpecifier(489, 4, 3729);   // unsigned32

        byte[] ipfixFieldSpecifier31 = buildIpfixFieldSpecifier(490, 1, 3729);   // boolean
        byte[] ipfixFieldSpecifier32 = buildIpfixFieldSpecifier(491, 65535, 3729);   //string
        byte[] ipfixFieldSpecifier33 = buildIpfixFieldSpecifier(492, 4, 3729);   // unsigned32
        byte[] ipfixFieldSpecifier34 = buildIpfixFieldSpecifier(493, 4, 3729);   // unsigned32
        byte[] ipfixFieldSpecifier35 = buildIpfixFieldSpecifier(494, 4, 3729);   // unsigned32
        byte[] ipfixFieldSpecifier36 = buildIpfixFieldSpecifier(495, 4, 3729);   // unsigned32
        byte[] ipfixFieldSpecifier37 = buildIpfixFieldSpecifier(496, 4, 3729);   // unsigned32
        byte[] ipfixFieldSpecifier38 = buildIpfixFieldSpecifier(497, 4, 3729);   // unsigned32
        byte[] ipfixFieldSpecifier39 = buildIpfixFieldSpecifier(498, 4, 3729);   // unsigned32
        byte[] ipfixFieldSpecifier40 = buildIpfixFieldSpecifier(499, 4, 3729);   // unsigned32

        byte[] ipfixFieldSpecifier41 = buildIpfixFieldSpecifier(500, 4, 3729);   // unsigned32
        byte[] ipfixFieldSpecifier42 = buildIpfixFieldSpecifier(501, 4, 3729);   // unsigned32
        byte[] ipfixFieldSpecifier43 = buildIpfixFieldSpecifier(502, 4, 3729);   // unsigned32
        byte[] ipfixFieldSpecifier44 = buildIpfixFieldSpecifier(503, 4, 3729);   // unsigned32
        byte[] ipfixFieldSpecifier45 = buildIpfixFieldSpecifier(504, 4, 3729);   // unsigned32

        int totalLength = ipfixFieldSpecifier0.length
                + ipfixFieldSpecifier1.length + ipfixFieldSpecifier2.length + ipfixFieldSpecifier3.length + ipfixFieldSpecifier4.length + ipfixFieldSpecifier5.length
                + ipfixFieldSpecifier6.length + ipfixFieldSpecifier7.length + ipfixFieldSpecifier8.length + ipfixFieldSpecifier9.length + ipfixFieldSpecifier10.length
                + ipfixFieldSpecifier11.length + ipfixFieldSpecifier12.length + ipfixFieldSpecifier13.length + ipfixFieldSpecifier14.length + ipfixFieldSpecifier15.length
                + ipfixFieldSpecifier16.length + ipfixFieldSpecifier17.length + ipfixFieldSpecifier18.length + ipfixFieldSpecifier19.length + ipfixFieldSpecifier20.length
                + ipfixFieldSpecifier21.length + ipfixFieldSpecifier22.length + ipfixFieldSpecifier23.length + ipfixFieldSpecifier24.length + ipfixFieldSpecifier25.length
                + ipfixFieldSpecifier26.length + ipfixFieldSpecifier27.length + ipfixFieldSpecifier28.length + ipfixFieldSpecifier29.length + ipfixFieldSpecifier30.length
                + ipfixFieldSpecifier31.length + ipfixFieldSpecifier32.length + ipfixFieldSpecifier33.length + ipfixFieldSpecifier34.length + ipfixFieldSpecifier35.length
                + ipfixFieldSpecifier36.length + ipfixFieldSpecifier37.length + ipfixFieldSpecifier38.length + ipfixFieldSpecifier39.length + ipfixFieldSpecifier40.length
                + ipfixFieldSpecifier41.length + ipfixFieldSpecifier42.length + ipfixFieldSpecifier43.length + ipfixFieldSpecifier44.length + ipfixFieldSpecifier45.length;

        byte[] data = new byte[totalLength];

        return combineArrays(data, Arrays.asList(ipfixFieldSpecifier0,
                ipfixFieldSpecifier1, ipfixFieldSpecifier2, ipfixFieldSpecifier3, ipfixFieldSpecifier4, ipfixFieldSpecifier5,
                ipfixFieldSpecifier6, ipfixFieldSpecifier7, ipfixFieldSpecifier8, ipfixFieldSpecifier9, ipfixFieldSpecifier10,
                ipfixFieldSpecifier11, ipfixFieldSpecifier12, ipfixFieldSpecifier13, ipfixFieldSpecifier14, ipfixFieldSpecifier15,
                ipfixFieldSpecifier16, ipfixFieldSpecifier17, ipfixFieldSpecifier18, ipfixFieldSpecifier19, ipfixFieldSpecifier20,
                ipfixFieldSpecifier21, ipfixFieldSpecifier22, ipfixFieldSpecifier23, ipfixFieldSpecifier24, ipfixFieldSpecifier25,
                ipfixFieldSpecifier26, ipfixFieldSpecifier27, ipfixFieldSpecifier28, ipfixFieldSpecifier29, ipfixFieldSpecifier30,
                ipfixFieldSpecifier31, ipfixFieldSpecifier32, ipfixFieldSpecifier33, ipfixFieldSpecifier34, ipfixFieldSpecifier35,
                ipfixFieldSpecifier36, ipfixFieldSpecifier37, ipfixFieldSpecifier38, ipfixFieldSpecifier39, ipfixFieldSpecifier40,
                ipfixFieldSpecifier41, ipfixFieldSpecifier42, ipfixFieldSpecifier43, ipfixFieldSpecifier44, ipfixFieldSpecifier45));
    }
}
