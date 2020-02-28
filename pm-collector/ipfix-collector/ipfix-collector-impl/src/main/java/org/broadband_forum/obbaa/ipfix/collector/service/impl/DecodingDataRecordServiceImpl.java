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

package org.broadband_forum.obbaa.ipfix.collector.service.impl;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.broadband_forum.obbaa.ipfix.collector.entities.IpfixFieldSpecifier;
import org.broadband_forum.obbaa.ipfix.collector.entities.IpfixMessage;
import org.broadband_forum.obbaa.ipfix.collector.entities.ie.InformationElement;
import org.broadband_forum.obbaa.ipfix.collector.entities.ie.InformationElementType;
import org.broadband_forum.obbaa.ipfix.collector.entities.ie.decode.IpfixDataRecordDecodeMethod;
import org.broadband_forum.obbaa.ipfix.collector.entities.logging.IpfixDecodedData;
import org.broadband_forum.obbaa.ipfix.collector.entities.record.AbstractTemplateRecord;
import org.broadband_forum.obbaa.ipfix.collector.entities.record.IpfixDataRecord;
import org.broadband_forum.obbaa.ipfix.collector.entities.set.IpfixDataSet;
import org.broadband_forum.obbaa.ipfix.collector.exception.DataTypeLengthMismatchException;
import org.broadband_forum.obbaa.ipfix.collector.exception.DecodingException;
import org.broadband_forum.obbaa.ipfix.collector.exception.MissingAbstractDataTypeException;
import org.broadband_forum.obbaa.ipfix.collector.exception.UtilityException;
import org.broadband_forum.obbaa.ipfix.collector.service.DecodingDataRecordService;
import org.broadband_forum.obbaa.ipfix.collector.service.DeviceCacheService;
import org.broadband_forum.obbaa.ipfix.collector.service.InformationElementService;
import org.broadband_forum.obbaa.ipfix.collector.service.ie.DecodeMethodFactory;
import org.broadband_forum.obbaa.ipfix.collector.util.IpfixUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DecodingDataRecordServiceImpl implements DecodingDataRecordService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DecodingDataRecordServiceImpl.class);

    private static final int VARIABLE_LENGTH_INDICATOR = 65535;
    private static final String EMPTY = "";

    private InformationElementService m_informationElementService;
    private DecodeMethodFactory m_decodeMethodFactory;
    private DeviceCacheService m_deviceFamilyCacheService;

    public DecodingDataRecordServiceImpl(InformationElementService informationElementServiceCache,
                                         DecodeMethodFactory decodeMethodFactory, DeviceCacheService deviceCacheService) {
        m_informationElementService = informationElementServiceCache;
        m_decodeMethodFactory = decodeMethodFactory;
        m_deviceFamilyCacheService = deviceCacheService;
    }

    @Override
    public Set<IpfixDecodedData> decodeDataSet(String deviceName, long obsvDomain, IpfixDataSet set, IpfixMessage ipfixMessage,
                                                CollectingServiceImpl.TemplateProvider templateProvider) throws DecodingException {
        if (Objects.isNull(set)) {
            throw new DecodingException("Data set is invalid for decoding process.");
        }

        int templateId = set.getHeader().getId();
        LOGGER.debug(String.format("Start decoding data set (Observation domain: %s, Set id: %s)", obsvDomain, templateId));
        AbstractTemplateRecord blueprint = templateProvider.get(templateId);
        if (Objects.isNull(blueprint)) {
            LOGGER.debug(String.format("Template %s on device %s not available in cache, start buffering", templateId, deviceName));
            return Collections.emptySet();
        } else {
            LOGGER.debug("Template available in cache, start decoding");
            return decodeDataSetWithTemplate(deviceName, blueprint, obsvDomain, set, ipfixMessage);
        }
    }

    private Set<IpfixDecodedData> decodeDataSetWithTemplate(String deviceName, AbstractTemplateRecord blueprint,
                                                                long obsvDomain, IpfixDataSet set, IpfixMessage ipfixMessage)
            throws DecodingException {
        Set<IpfixDecodedData> decodedDataPerField = new HashSet<>();
        String deviceFamily = StringUtils.isEmpty(deviceName) ? null : m_deviceFamilyCacheService.getDeviceFamily(deviceName);
        while (set.getRawData().length > 0) {
            if (StringUtils.isEmpty(deviceName) && Objects.nonNull(ipfixMessage) && CollectionUtils.isNotEmpty(
                    ipfixMessage.getOptionTemplateSets())) {
                deviceName = decodeDeviceName(set);
                deviceFamily = m_deviceFamilyCacheService.getDeviceFamily(deviceName);
                LOGGER.debug(String.format("Found device family: %s based on the deviceName: %s", deviceFamily, deviceName));
            }

            if (Objects.isNull(deviceFamily)) {
                throw new DecodingException("Could not find device family based on the deviceName: " + deviceName);
            }

            for (IpfixFieldSpecifier field : blueprint.getFieldSpecifiers()) {
                LOGGER.debug(String.format("Using IEId from %s adapter to decode data set with IpfixFieldSpecifier %s",
                        deviceFamily, field));
                int ieID = field.getInformationElementId();
                long enterpriseNumber = field.getEnterpriseNumber();
                int length = field.getFieldLength();
                String key = generateKey(enterpriseNumber, ieID);
                InformationElement ie = m_informationElementService.getInformationElement(deviceFamily, ieID);
                String value = decodeData(deviceName, blueprint, obsvDomain, set, ipfixMessage, length, ie);
                String dataType = "string";
                if (ie != null) {
                    key = ie.getName();
                    dataType = ie.getDataType().getKey();
                }
                IpfixDecodedData decodedCounter = new IpfixDecodedData(key, dataType, value);
                decodedDataPerField.add(decodedCounter);
            }
        }
        return decodedDataPerField;
    }

    /**
     * Decode the deviceName with the first record in ipfix data dataSet.
     *
     * @param dataSet data set of option template
     * @return deviceName in string value
     * @throws DecodingException when could not decode devicename with {@param dataSet}
     */
    private String decodeDeviceName(IpfixDataSet dataSet) throws DecodingException {
        byte[] data = dataSet.getRawData();
        try {
            int fieldLength = getVariableLength(data);
            data = getData(data, fieldLength);

            IpfixDataRecordDecodeMethod decodeMethod = m_decodeMethodFactory.getDecodeMethodByIEType(InformationElementType.STRING);
            return decodeMethod.decodeDataRecord(data, fieldLength);
        } catch (Exception e) {
            LOGGER.error(String.format("Failed to decode deviceName from option template data record. Data: %s , type: %s.",
                    IpfixUtilities.bytesToHex(data), InformationElementType.STRING, e));
            throw new DecodingException("Could not decode the device name", e);
        }
    }

    private byte[] getData(byte[] data, int fieldLength) {
        return (fieldLength < 255) ? IpfixUtilities.copyByteArray(data, 1, data.length - 1)
                : IpfixUtilities.copyByteArray(data, 3, data.length - 3);
    }

    private String decodeData(String deviceName, AbstractTemplateRecord blueprint, long obsvDomain, IpfixDataSet set,
                              IpfixMessage ipfixMessage, int length, InformationElement ie) throws DecodingException {
        try {
            if (ie == null || ie.getDataType() == InformationElementType.OCTETARRAY) {
                LOGGER.debug(String.format("Keeping the raw data with the IE: {}", ie));
                return keepRawDataAndUpdateDataRecord(set, length);
            }
            return decodeAndUpdateDataRecord(set, length, ie, ipfixMessage);
        } catch (UtilityException | UnknownHostException e) {
            String errorMsg = "Error decoding data set (Observation domain: " + obsvDomain + ", Set id: " + set.getHeader().getId()
                    + ") with template set: " + blueprint + " from original data: " + ipfixMessage;
            throw new DecodingException(errorMsg, e);
        } catch (Exception ex) {
            throw new DecodingException("Error decoding data set: " + set + ", with template set: " + blueprint
                    + " on the device: " + deviceName, ex);
        }
    }

    private String generateKey(long enterpriseNumber, int ieID) {
        String key = Integer.toString(ieID);
        if (enterpriseNumber != 0) {
            key = ieID + "." + enterpriseNumber;
        }
        return key;
    }

    private String decodeAndUpdateDataRecord(IpfixDataSet set, int fieldLength, InformationElement ie, IpfixMessage ipfixMessage)
            throws UtilityException, UnknownHostException {
        LOGGER.debug(String.format("Decode data record of ieID: %s with fieldLength is %s", ie.getElementId(), fieldLength));
        byte[] data = set.getRawData();
        if (fieldLength == VARIABLE_LENGTH_INDICATOR) {
            fieldLength = getVariableLength(data);
            if (ie.getDataType() == InformationElementType.UNION) {
                if (fieldLength == 0) {
                    // No data has been sent from device
                    LOGGER.warn(String.format("Received the union data with length is zero for ieId: %s", ie.getElementId()));
                    data = IpfixUtilities.copyByteArray(data, 2, data.length - 2);
                    set.setRawData(data);
                    return EMPTY;
                }
                // Plus one byte for full message length: Byte of size + Byte of data
                fieldLength ++;
            } else {
                data = getData(data, fieldLength);
            }
        }

        String value;
        byte[] rawData = IpfixUtilities.copyByteArray(data, 0, fieldLength);
        String hexValue = IpfixUtilities.bytesToHex(rawData);
        try {
            IpfixDataRecordDecodeMethod decodeMethod = m_decodeMethodFactory.getDecodeMethodByIEType(ie.getDataType());
            value = decodeMethod.decodeDataRecord(data, fieldLength);
        } catch (DataTypeLengthMismatchException | MissingAbstractDataTypeException e) {
            LOGGER.error(String.format("Failed to decode value from raw value in data record. Data: %s , type: %s. From original data: %s."
                            + " Using raw value",
                    IpfixUtilities.bytesToHex(data), ie.getDataType(), ipfixMessage, e));
            value = hexValue;
        }
        IpfixDataRecord dataRecord = new IpfixDataRecord(value, hexValue);
        set.addRecord(dataRecord);
        data = IpfixUtilities.copyByteArray(data, fieldLength, data.length - fieldLength);
        set.setRawData(data);

        return value;
    }

    private String keepRawDataAndUpdateDataRecord(IpfixDataSet set, int fieldLength) throws UtilityException {
        byte[] data = set.getRawData();
        if (fieldLength == VARIABLE_LENGTH_INDICATOR) {
            fieldLength = getVariableLength(data);
            data = getData(data, fieldLength);
        }
        byte[] rawData = IpfixUtilities.copyByteArray(data, 0, fieldLength);
        byte[] remainingData = IpfixUtilities.copyByteArray(data, fieldLength, data.length - fieldLength);

        String value = IpfixUtilities.bytesToHex(rawData);
        IpfixDataRecord dataRecord = new IpfixDataRecord(value);
        set.addRecord(dataRecord);
        set.setRawData(remainingData);
        return value;
    }

    // Variable Length: in case of variable length then the length is added to
    // the Information element context.
    // When the length is < 255:
    // +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    // | <255 (1 octet) | IE |
    // +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    // When the length is > 255:
    // +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    // | 255 (1 octet) | 0 .. 65525 (2 octets) | IE |
    // +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    private int getVariableLength(byte[] data) throws UtilityException {
        byte firstOctet = data[0];
        int length = IpfixUtilities.oneByteToInteger(firstOctet, 1);
        if (length < 255) {
            return length;
        }
        byte[] lengthCarriedOctets = Arrays.copyOfRange(data, 1, 3);
        return IpfixUtilities.byteToInteger(lengthCarriedOctets, 1, 2);
    }
}
