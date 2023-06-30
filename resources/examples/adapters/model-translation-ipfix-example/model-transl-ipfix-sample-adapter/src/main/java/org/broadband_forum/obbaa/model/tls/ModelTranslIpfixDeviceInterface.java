/*
 * Copyright 2023 Broadband Forum
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

package org.broadband_forum.obbaa.model.tls;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.broadband_forum.obbaa.ipfix.entities.IpfixFieldSpecifier;
import org.broadband_forum.obbaa.ipfix.entities.adapter.IpfixAdapterInterface;
import org.broadband_forum.obbaa.ipfix.entities.exception.CollectingProcessException;
import org.broadband_forum.obbaa.ipfix.entities.exception.DataTypeLengthMismatchException;
import org.broadband_forum.obbaa.ipfix.entities.exception.DecodingException;
import org.broadband_forum.obbaa.ipfix.entities.exception.MissingAbstractDataTypeException;
import org.broadband_forum.obbaa.ipfix.entities.exception.UtilityException;
import org.broadband_forum.obbaa.ipfix.entities.ie.InformationElement;
import org.broadband_forum.obbaa.ipfix.entities.ie.InformationElementType;
import org.broadband_forum.obbaa.ipfix.entities.ie.IpfixDataRecordDecodeMethod;
import org.broadband_forum.obbaa.ipfix.entities.ie.decode.DecodeBoolean;
import org.broadband_forum.obbaa.ipfix.entities.ie.decode.DecodeDateTimeSecond;
import org.broadband_forum.obbaa.ipfix.entities.ie.decode.DecodeEnumeration;
import org.broadband_forum.obbaa.ipfix.entities.ie.decode.DecodeFloat64;
import org.broadband_forum.obbaa.ipfix.entities.ie.decode.DecodeIPv4Address;
import org.broadband_forum.obbaa.ipfix.entities.ie.decode.DecodeIPv6ddress;
import org.broadband_forum.obbaa.ipfix.entities.ie.decode.DecodeMacAddress;
import org.broadband_forum.obbaa.ipfix.entities.ie.decode.DecodeSigned16;
import org.broadband_forum.obbaa.ipfix.entities.ie.decode.DecodeSigned32;
import org.broadband_forum.obbaa.ipfix.entities.ie.decode.DecodeSigned64;
import org.broadband_forum.obbaa.ipfix.entities.ie.decode.DecodeSigned8;
import org.broadband_forum.obbaa.ipfix.entities.ie.decode.DecodeString;
import org.broadband_forum.obbaa.ipfix.entities.ie.decode.DecodeUnion;
import org.broadband_forum.obbaa.ipfix.entities.ie.decode.DecodeUnsigned16;
import org.broadband_forum.obbaa.ipfix.entities.ie.decode.DecodeUnsigned32;
import org.broadband_forum.obbaa.ipfix.entities.ie.decode.DecodeUnsigned64;
import org.broadband_forum.obbaa.ipfix.entities.ie.decode.DecodeUnsigned8;
import org.broadband_forum.obbaa.ipfix.entities.message.IpfixMessage;
import org.broadband_forum.obbaa.ipfix.entities.message.logging.IpfixDecodedData;
import org.broadband_forum.obbaa.ipfix.entities.message.logging.IpfixLogging;
import org.broadband_forum.obbaa.ipfix.entities.message.set.IpfixOptionTemplateSet;
import org.broadband_forum.obbaa.ipfix.entities.message.set.IpfixTemplateSet;
import org.broadband_forum.obbaa.ipfix.entities.record.AbstractTemplateRecord;
import org.broadband_forum.obbaa.ipfix.entities.record.IpfixDataRecord;
import org.broadband_forum.obbaa.ipfix.entities.service.InformationElementIpfixService;
import org.broadband_forum.obbaa.ipfix.entities.service.IpfixCachingService;
import org.broadband_forum.obbaa.ipfix.entities.set.IpfixDataSet;
import org.broadband_forum.obbaa.ipfix.entities.util.IpfixUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelTranslIpfixDeviceInterface implements IpfixAdapterInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelTranslIpfixDeviceInterface.class);

    private static final int VARIABLE_LENGTH_INDICATOR = 65535;
    private static final String EMPTY = "";

    private final IpfixCachingService m_cachingService;
    private final InformationElementIpfixService m_informationElementIpfixService;

    public ModelTranslIpfixDeviceInterface(IpfixCachingService cachingService, InformationElementIpfixService
            informationElementIpfixService) {
        m_cachingService = cachingService;
        m_informationElementIpfixService = informationElementIpfixService;
    }

    @Override
    public List<Set<IpfixDecodedData>> decodeIpfixMessage(List<IpfixDataSet> rawDataSets, Optional<String>[] arrOptHostNames,
                            IpfixMessage ipfixMessage, IpfixLogging ipfixLogging, String deviceFamily) throws CollectingProcessException {
        LOGGER.info("IPFIX decoding started in " + deviceFamily + " adapter");
        long obsvDomain = ipfixMessage.getHeader().getObservationDomainId();

        // Need to cache template record
        final boolean[] needToCache = {!arrOptHostNames[0].isPresent()};

        // Extract template record from IPFIXMessage
        Map<Integer, AbstractTemplateRecord> templateByIdMap = buildTemplateToIdMap(ipfixMessage);

        TemplateProvider templateProvider = templateId -> {
            if (arrOptHostNames[0].isPresent()) {
                AbstractTemplateRecord templateCache = m_cachingService.getTemplateRecord(obsvDomain, arrOptHostNames[0].get(),
                        templateId);
                if (Objects.isNull(templateCache)) {
                    needToCache[0] = true;
                    LOGGER.debug(String.format("Cannot get template cache %d for device %s.", templateId, arrOptHostNames[0].get()));
                }
                return templateCache;
            }
            LOGGER.debug(String.format("Hostname isn't present. Need to cache the templates %s", templateByIdMap));
            return templateByIdMap.get(templateId);
        };

        List<Set<IpfixDecodedData>> decodeDataSetList = new ArrayList<>();
        for (IpfixDataSet set : rawDataSets) {
            Set<IpfixDecodedData> decodeDataSet;
            try {
                decodeDataSet = decodeDataSet(arrOptHostNames[0].orElse(""), deviceFamily, obsvDomain,
                        set, ipfixMessage, templateProvider);
            } catch (DecodingException e) {
                throw new CollectingProcessException("Error while decoding data set.", e);
            }
            decodeDataSetList.add(decodeDataSet);
        }
        return decodeDataSetList;
    }

    private Map<Integer, AbstractTemplateRecord> buildTemplateToIdMap(IpfixMessage ipfixMessage) {
        Map<Integer, AbstractTemplateRecord> rs = new HashMap<>();

        extractTemplateRecordFromMessage(ipfixMessage).forEach(tmpl -> rs.put(IpfixUtilities.getTemplateId(tmpl), tmpl));
        extractOptionTemplateRecordFromMessage(ipfixMessage).forEach(tmpl -> rs.put(IpfixUtilities.getTemplateId(tmpl), tmpl));
        return rs;
    }

    private List<AbstractTemplateRecord> extractTemplateRecordFromMessage(IpfixMessage message) {
        List<AbstractTemplateRecord> templateRecords = new LinkedList<>();
        if (message.getTemplateSets() != null) {
            for (IpfixTemplateSet set : message.getTemplateSets()) {
                templateRecords.addAll(set.getRecords());
            }
        }
        return templateRecords;
    }

    private List<AbstractTemplateRecord> extractOptionTemplateRecordFromMessage(IpfixMessage message) {
        List<AbstractTemplateRecord> optionTemplateRecords = new LinkedList<>();
        if (message.getOptionTemplateSets() != null) {
            for (IpfixOptionTemplateSet set : message.getOptionTemplateSets()) {
                optionTemplateRecords.addAll(set.getRecords());
            }
        }
        return optionTemplateRecords;
    }

    public Set<IpfixDecodedData> decodeDataSet(String deviceName, String deviceFamily, long obsvDomain, IpfixDataSet set,
                                               IpfixMessage ipfixMessage, TemplateProvider templateProvider) throws DecodingException {
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
            return decodeDataSetWithTemplate(deviceName, blueprint, deviceFamily, obsvDomain, set, ipfixMessage);
        }
    }

    private Set<IpfixDecodedData> decodeDataSetWithTemplate(String deviceName, AbstractTemplateRecord blueprint, String deviceFamily,
                                                            long obsvDomain, IpfixDataSet set, IpfixMessage ipfixMessage)
            throws DecodingException {
        Set<IpfixDecodedData> decodedDataPerField = new HashSet<>();
        while (set.getRawData().length > 0) {
            if (StringUtils.isEmpty(deviceName) && Objects.nonNull(ipfixMessage) && CollectionUtils.isNotEmpty(
                    ipfixMessage.getOptionTemplateSets())) {
                deviceName = decodeDeviceName(set);
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
                InformationElement ie = m_informationElementIpfixService.getInformationElement(deviceFamily, ieID);
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

    private String generateKey(long enterpriseNumber, int ieID) {
        String key = Integer.toString(ieID);
        if (enterpriseNumber != 0) {
            key = ieID + "." + enterpriseNumber;
        }
        return key;
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

            IpfixDataRecordDecodeMethod decodeMethod = getDecodeMethodByIEType(InformationElementType.STRING);
            return decodeMethod.decodeDataRecord(data, fieldLength);
        } catch (UtilityException | DataTypeLengthMismatchException | UnknownHostException | MissingAbstractDataTypeException e) {
            LOGGER.error(String.format("Failed to decode deviceName from option template data record. Data: %s , type: %s.",
                    IpfixUtilities.bytesToHex(data), InformationElementType.STRING, e));
            throw new DecodingException("Could not decode the device name", e);
        }
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
        }
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
                fieldLength++;
            } else {
                data = getData(data, fieldLength);
            }
        }

        String value;
        byte[] rawData = IpfixUtilities.copyByteArray(data, 0, fieldLength);
        String hexValue = IpfixUtilities.bytesToHex(rawData);
        try {
            IpfixDataRecordDecodeMethod decodeMethod = getDecodeMethodByIEType(ie.getDataType());
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

    public interface TemplateProvider {
        AbstractTemplateRecord get(int templateId);
    }

}
