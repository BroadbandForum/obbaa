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

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.broadband_forum.obbaa.ipfix.collector.entities.IpfixMessageNotification;
import org.broadband_forum.obbaa.ipfix.collector.service.CollectingService;
import org.broadband_forum.obbaa.ipfix.collector.service.DecodingDataRecordService;
import org.broadband_forum.obbaa.ipfix.collector.service.DeviceCacheService;
import org.broadband_forum.obbaa.ipfix.entities.adapter.IpfixAdapterInterface;
import org.broadband_forum.obbaa.ipfix.entities.exception.CollectingProcessException;
import org.broadband_forum.obbaa.ipfix.entities.exception.DecodingException;
import org.broadband_forum.obbaa.ipfix.entities.exception.NotEnoughBytesException;
import org.broadband_forum.obbaa.ipfix.entities.header.IpfixSetHeader;
import org.broadband_forum.obbaa.ipfix.entities.message.IpfixMessage;
import org.broadband_forum.obbaa.ipfix.entities.message.logging.IpfixDecodedData;
import org.broadband_forum.obbaa.ipfix.entities.message.logging.IpfixLogging;
import org.broadband_forum.obbaa.ipfix.entities.message.logging.IpfixLoggingDataSet;
import org.broadband_forum.obbaa.ipfix.entities.message.set.IpfixOptionTemplateSet;
import org.broadband_forum.obbaa.ipfix.entities.message.set.IpfixTemplateSet;
import org.broadband_forum.obbaa.ipfix.entities.record.AbstractTemplateRecord;
import org.broadband_forum.obbaa.ipfix.entities.service.IpfixCachingService;
import org.broadband_forum.obbaa.ipfix.entities.set.IpfixDataSet;
import org.broadband_forum.obbaa.ipfix.entities.util.IpfixConstants;
import org.broadband_forum.obbaa.ipfix.entities.util.IpfixDeviceInterfaceUtil;
import org.broadband_forum.obbaa.ipfix.entities.util.IpfixUtilities;
import org.broadband_forum.obbaa.pm.service.DataHandlerService;
import org.broadband_forum.obbaa.pm.service.IpfixDataHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class CollectingServiceImpl implements CollectingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CollectingServiceImpl.class);
    private static final long DEFAULT_PAST_TIME_GAP = TimeUnit.HOURS.toMinutes(1L);
    private static final long DEFAULT_FUTURE_TIME_GAP = TimeUnit.MINUTES.toMinutes(1L);
    private static final String IPFIX_FE_ACCEPTANCE_PAST_TIME_GAP = "IPFIX_FE_ACCEPTANCE_PAST_TIME_GAP";
    private static final String IPFIX_FE_ACCEPTANCE_FUTURE_TIME_GAP = "IPFIX_FE_ACCEPTANCE_FUTURE_TIME_GAP";
    private static long m_acceptantPastTimeGap;
    private static long m_acceptantFutureTimeGap;
    private DeviceCacheService m_deviceCacheService;
    private DecodingDataRecordService m_decodingDataRecordService;
    private IpfixCachingService m_cachingService;
    private DataHandlerService m_dataHandlerService;
    private IpfixDeviceInterfaceUtil m_ipfixDeviceInterfaceUtil;

    private AtomicInteger m_decodeFailureCount = new AtomicInteger(0);
    private AtomicInteger m_processedMessageCount = new AtomicInteger(0);
    private AtomicInteger m_authenFailureCount = new AtomicInteger(0);

    public CollectingServiceImpl(DecodingDataRecordService decodingDataRecordService, IpfixCachingService cachingService,
                                 DataHandlerService dataHandlerService, DeviceCacheService deviceCacheService,
                                 IpfixDeviceInterfaceUtil ipfixDeviceInterfaceUtil) {
        m_decodingDataRecordService = decodingDataRecordService;
        m_cachingService = cachingService;
        m_dataHandlerService = dataHandlerService;
        m_deviceCacheService = deviceCacheService;
        m_acceptantPastTimeGap = loadTimeRangeConfig(IPFIX_FE_ACCEPTANCE_PAST_TIME_GAP, DEFAULT_PAST_TIME_GAP);
        m_acceptantFutureTimeGap = loadTimeRangeConfig(IPFIX_FE_ACCEPTANCE_FUTURE_TIME_GAP, DEFAULT_FUTURE_TIME_GAP);
        m_ipfixDeviceInterfaceUtil = ipfixDeviceInterfaceUtil;
    }

    private static boolean isMessageInThePast(Instant now, Instant exportTime) {
        return exportTime.compareTo(now) < 0;
    }

    private static boolean isMessageInTheFuture(Instant now, Instant exportTime) {
        return exportTime.compareTo(now) > 0;
    }

    private static long loadTimeRangeConfig(String envName, long defaultValue) {
        String value = System.getenv(envName);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return defaultValue;
    }

    private static boolean isAcceptGap(Duration between, boolean isPast) {
        if (isPast) {
            return m_acceptantPastTimeGap > Math.abs(between.toMinutes());
        }
        return m_acceptantFutureTimeGap > Math.abs(between.toMinutes());
    }

    private IpfixAdapterInterface getIpifixAdapterInterface(String deviceFamily) {
        return m_ipfixDeviceInterfaceUtil.getDeviceInterface(deviceFamily);
    }

    @Override
    public Map.Entry<Optional<Long>, Optional<String>> collect(byte[] data, String remoteAddress, Optional<String> optionalHostName)
            throws NotEnoughBytesException, CollectingProcessException {
        final Optional<String>[] arrOptHostNames = new Optional[]{optionalHostName};
        String deviceName = null;
        if (optionalHostName.isPresent()) {
            deviceName = optionalHostName.get();
        }

        IpfixLogging ipfixLogging = new IpfixLogging();
        IpfixMessage ipfixMessage = createIpfixMessage(data);
        ipfixLogging.setMessageHeader(ipfixMessage.getHeader());

        LOGGER.debug(String.format("IPFIX message decoded successfully. Packet decoded: %s", ipfixMessage));
        String messageExportTime = ipfixMessage.getHeader().getExportTime(); // 1970-01-04T03:12:07Z
        validateIpfixMessageExportTime(messageExportTime, remoteAddress, deviceName);

        long obsvDomain = ipfixMessage.getHeader().getObservationDomainId();

        IpfixMessageNotification notificationMessage = new IpfixMessageNotification();
        notificationMessage.setSourceIP(remoteAddress);
        notificationMessage.setObservationDomain(obsvDomain);
        notificationMessage.setTimestamp(messageExportTime);

        // Extract template record from IPFIXMessage
        Map<Integer, AbstractTemplateRecord> templateByIdMap = buildTemplateToIdMap(ipfixMessage);

        // Device update template cache when hostname exist
        if (arrOptHostNames[0].isPresent() && !templateByIdMap.isEmpty()) {
            m_cachingService.cacheTemplateRecord(obsvDomain, arrOptHostNames[0].get(), templateByIdMap);
        }

        // Need to cache template record
        final boolean[] needToCache = {!arrOptHostNames[0].isPresent()};

        // In the first message, we will not have hostname info because it is stored in dataset. In this case we will get
        // template cache from above map not from the cache
        LOGGER.debug(String.format("Hostname %s and templateByIdMap %s", arrOptHostNames[0], templateByIdMap));
        TemplateProvider templateProvider = templateId -> {
            if (arrOptHostNames[0].isPresent()) {
                try {
                    AbstractTemplateRecord templateCache = m_cachingService.getTemplateRecord(obsvDomain, arrOptHostNames[0].get(),
                            templateId);
                    if (Objects.isNull(templateCache)) {
                        needToCache[0] = true;
                        LOGGER.debug(String.format("Cannot get template cache %d for device %s.", templateId, arrOptHostNames[0].get()));
                    }
                    return templateCache;
                } catch (Exception e) {
                    LOGGER.debug(String.format("Error while retrieving template cache %d for device %s.", templateId,
                            arrOptHostNames[0].get()));
                }
            }
            LOGGER.debug(String.format("Hostname isn't present. Need to cache the templates %s", templateByIdMap));
            return templateByIdMap.get(templateId);
        };

        List<IpfixDataSet> cacheDataSetFromPreviousMessage = new ArrayList<>();
        if (arrOptHostNames[0].isPresent()) {
            cacheDataSetFromPreviousMessage = m_cachingService.getAndInvalidateIpfixDataSet(obsvDomain, arrOptHostNames[0].get());
        }

        // Merge data set from current message with cache dataset from previous message
        List<IpfixDataSet> rawDataSets = new LinkedList<>(cacheDataSetFromPreviousMessage);
        if (ipfixMessage.getDataSets() != null) {
            List<AbstractTemplateRecord> templateRecords = extractTemplateRecordFromMessage(ipfixMessage);
            if (!templateRecords.isEmpty()) {
                ipfixLogging.addTemplateSet(ipfixMessage.getTemplateSets());
            }

            List<AbstractTemplateRecord> optionTemplateRecords = extractOptionTemplateRecordFromMessage(ipfixMessage);
            if (!optionTemplateRecords.isEmpty()) {
                ipfixLogging.addOptionTemplateSet(ipfixMessage.getOptionTemplateSets());
            }

            LOGGER.debug(String.format("Decoded more packages from buffer: %s", rawDataSets));
            rawDataSets.addAll(ipfixMessage.getDataSets());
        }

        for (IpfixDataSet set : rawDataSets) {
            try {
                deviceName = m_decodingDataRecordService.decodeDeviceName(set);
                String deviceFamily = StringUtils.isEmpty(deviceName) ? null : m_deviceCacheService.getDeviceFamily(deviceName);
                final Optional<String>[] arrOptHostNamesFromIpfix = new Optional[]{Optional.of(deviceName)};
                if (!arrOptHostNames[0].isPresent()) {
                    arrOptHostNames[0] = arrOptHostNamesFromIpfix[0];
                }
                if (deviceFamily == null || (deviceFamily != null && deviceFamily.contains(IpfixConstants.BBF)
                        && deviceFamily.contains(IpfixConstants.STANDARD))) {
                    LOGGER.info("Decoding IPFIX message in IPFIX collector for standard adapter: " + deviceFamily);
                    Set<IpfixDecodedData> decodeDataSet;
                    try {
                        decodeDataSet = m_decodingDataRecordService.decodeDataSet(arrOptHostNamesFromIpfix[0].orElse(""), obsvDomain,
                                set, ipfixMessage, templateProvider);
                    } catch (DecodingException e) {
                        throw new CollectingProcessException("Error while decoding data set.", e);
                    }

                    // Logging
                    boolean isOptionTemplateSet = false;
                    if (decodeDataSet != null) {
                        isOptionTemplateSet = true;
                    }
                    IpfixSetHeader setHeader = set.getHeader();
                    IpfixLoggingDataSet ipfixLoggingDataSet = new IpfixLoggingDataSet(setHeader);
                    ipfixLoggingDataSet.setDataRecords(set.getRecords(), isOptionTemplateSet);

                    AbstractTemplateRecord abstractTemplateRecord = templateProvider.get(setHeader.getId());
                    if (abstractTemplateRecord != null) {
                        ipfixLoggingDataSet.setTemplateRecords(abstractTemplateRecord.getFieldSpecifiers());
                    }
                    ipfixLogging.addToSetMessages(ipfixLoggingDataSet);

                    if (decodeDataSet != null && !decodeDataSet.isEmpty()) {
                        int templateID = set.getHeader().getId();
                        LOGGER.debug(String.format("Data set decoded successfully (Observation domain: %s, Set id: %s)",
                                obsvDomain, templateID));
                        LOGGER.info("Decoded ipfix message for " + deviceName + " is: " + ipfixLogging.convertToLog());
                        if (isAuthenticateDataSet(decodeDataSet)) {
                            // DataSet for OptionTemplateSet
                            String hostName = "";
                            for (IpfixDecodedData decodedData : decodeDataSet) {
                                if (decodedData.getCounterName().equalsIgnoreCase(IpfixConstants.HOSTNAME_IE_KEY)) {
                                    hostName = decodedData.getDecodedValue();
                                }
                            }

                            // In case user manually update hostname, we need to check & update the cache accordingly
                            if (arrOptHostNamesFromIpfix[0].isPresent() && !arrOptHostNamesFromIpfix[0].get().equals(hostName)) {
                                m_cachingService.updateHostName(obsvDomain, arrOptHostNamesFromIpfix[0].get(), hostName);
                            }
                            if (hostName.isEmpty()) {
                                throw new CollectingProcessException("Missing hostname");
                            }
                            arrOptHostNamesFromIpfix[0] = Optional.of(hostName);
                            notificationMessage.setHostName(hostName);
                        } else {
                            // DataSet for TemplateSet
                            if (arrOptHostNamesFromIpfix[0].isPresent() && !arrOptHostNamesFromIpfix[0].get().isEmpty()) {
                                handleDataSetForTemplateSet(arrOptHostNamesFromIpfix[0].get(), decodeDataSet,
                                        templateID, notificationMessage);
                            } else {
                                LOGGER.error(String.format("Couldn't find hostname so data set %s will be ignored", set.toString()));
                                break;
                            }
                        }
                        if (arrOptHostNamesFromIpfix[0].isPresent()) {
                            deviceName = arrOptHostNamesFromIpfix[0].get();
                        }
                    } else {
                        LOGGER.debug(String.format("Buffering data set (Observation domain: %s, Set id: %s)",
                                obsvDomain, set.getHeader().getId()));
                        if (arrOptHostNamesFromIpfix[0].isPresent()) {
                            m_cachingService.cacheIpfixDataSet(obsvDomain, arrOptHostNamesFromIpfix[0].get(), set);
                        } else {
                            LOGGER.info(String.format("Couldn't find hostname so data set %s will be ignored", set.toString()));
                        }
                    }
                } else {
                    LOGGER.info("Sending IPFIX message to adapter " + deviceFamily + " to decode");
                    IpfixAdapterInterface ipfixAdapterInterface = getIpifixAdapterInterface(deviceFamily);
                    List<Set<IpfixDecodedData>> decodeDataSetList = new ArrayList<>();
                    if (ipfixAdapterInterface != null) {

                        decodeDataSetList = ipfixAdapterInterface.decodeIpfixMessage(set, arrOptHostNamesFromIpfix, ipfixMessage,
                                ipfixLogging, deviceFamily);
                    } else {
                        throw new CollectingProcessException("IPFIX device interface is missing in the map");
                    }
                    for (int arrayIndex = 0; arrayIndex < decodeDataSetList.size(); arrayIndex++) {
                        // Logging
                        boolean isOptionTemplateSet = false;
                        if (decodeDataSetList.get(arrayIndex) != null) {
                            isOptionTemplateSet = true;
                        }
                        IpfixSetHeader setHeader = rawDataSets.get(arrayIndex).getHeader();
                        IpfixLoggingDataSet ipfixLoggingDataSet = new IpfixLoggingDataSet(setHeader);
                        ipfixLoggingDataSet.setDataRecords(rawDataSets.get(arrayIndex).getRecords(), isOptionTemplateSet);

                        AbstractTemplateRecord abstractTemplateRecord = templateProvider.get(setHeader.getId());
                        if (abstractTemplateRecord != null) {
                            ipfixLoggingDataSet.setTemplateRecords(abstractTemplateRecord.getFieldSpecifiers());
                        }
                        ipfixLogging.addToSetMessages(ipfixLoggingDataSet);

                        if (decodeDataSetList.get(arrayIndex) != null && !decodeDataSetList.get(arrayIndex).isEmpty()) {
                            int templateID = rawDataSets.get(arrayIndex).getHeader().getId();
                            LOGGER.debug(String.format("Data set decoded successfully (Observation domain: %s, Set id: %s)",
                                    obsvDomain, templateID));
                            LOGGER.info("Decoded ipfix message for " + deviceName + " is: " + ipfixLogging.convertToLog());
                            if (isAuthenticateDataSet(decodeDataSetList.get(arrayIndex))) {
                                // DataSet for OptionTemplateSet
                                String hostName = "";
                                for (IpfixDecodedData decodedData : decodeDataSetList.get(arrayIndex)) {
                                    if (decodedData.getCounterName().equalsIgnoreCase(IpfixConstants.HOSTNAME_IE_KEY)) {
                                        hostName = decodedData.getDecodedValue();
                                    }
                                }

                                // In case user manually update hostname, we need to check & update the cache accordingly
                                if (arrOptHostNamesFromIpfix[0].isPresent() && !arrOptHostNamesFromIpfix[0].get().equals(hostName)) {
                                    m_cachingService.updateHostName(obsvDomain, arrOptHostNamesFromIpfix[0].get(), hostName);
                                }
                                if (hostName.isEmpty()) {
                                    throw new CollectingProcessException("Missing hostname");
                                }
                                arrOptHostNamesFromIpfix[0] = Optional.of(hostName);
                                notificationMessage.setHostName(hostName);
                            } else {
                                // DataSet for TemplateSet
                                if (arrOptHostNamesFromIpfix[0].isPresent() && !arrOptHostNamesFromIpfix[0].get().isEmpty()) {
                                    handleDataSetForTemplateSet(arrOptHostNamesFromIpfix[0].get(),
                                            decodeDataSetList.get(arrayIndex), templateID, notificationMessage);
                                } else {
                                    LOGGER.error(String.format("Couldn't find hostname so data set %s will be ignored",
                                            rawDataSets.get(arrayIndex).toString()));
                                    break;
                                }
                            }
                            if (arrOptHostNamesFromIpfix[0].isPresent()) {
                                deviceName = arrOptHostNamesFromIpfix[0].get();
                            }
                        } else {
                            LOGGER.debug(String.format("Buffering data set (Observation domain: %s, Set id: %s)",
                                    obsvDomain, rawDataSets.get(arrayIndex).getHeader().getId()));
                            if (arrOptHostNamesFromIpfix[0].isPresent()) {
                                m_cachingService.cacheIpfixDataSet(obsvDomain, arrOptHostNamesFromIpfix[0].get(),
                                        rawDataSets.get(arrayIndex));
                            } else {
                                LOGGER.info(String.format("Couldn't find hostname so data set %s will be ignored",
                                        rawDataSets.get(arrayIndex).toString()));
                            }
                        }
                    }
                }
            } catch (DecodingException e) {
                LOGGER.error("Error while fetching device name from ipfix set");
            }
        }

        if (arrOptHostNames[0].isPresent() && needToCache[0]) {
            LOGGER.debug(String.format("Need to cache the templates %s for device %s", templateByIdMap, arrOptHostNames[0].get()));
            m_cachingService.cacheTemplateRecord(obsvDomain, arrOptHostNames[0].get(), templateByIdMap);
        }

        return new AbstractMap.SimpleEntry(Optional.of(obsvDomain), arrOptHostNames[0]);
    }

    private void handleDataSetForTemplateSet(String hostName, Set<IpfixDecodedData> decodedDataSet, int templateID,
                                             IpfixMessageNotification notificationMessage) {
        final Gson gsonBuilder = new GsonBuilder().create();

        //Send data to Data handler
        notificationMessage.setHostName(hostName);
        notificationMessage.setDeviceAdapter(m_deviceCacheService.getDeviceFamily(hostName));
        notificationMessage.setTemplateID(templateID);
        notificationMessage.setData(decodedDataSet);
        String ipfixMessage = gsonBuilder.toJson(notificationMessage, notificationMessage.getClass());

        List<IpfixDataHandler> dataHandlers = m_dataHandlerService.getIpfixDataHandlers();
        for (IpfixDataHandler dataHandler : dataHandlers) {
            dataHandler.handleIpfixData(ipfixMessage);
        }
    }

    private boolean isAuthenticateDataSet(Set<IpfixDecodedData> decodedDataList) {
        for (IpfixDecodedData decodedData : decodedDataList) {
            if (decodedData.getCounterName().equalsIgnoreCase(IpfixConstants.HOSTNAME_IE_KEY)) {
                return true;
            }
        }
        return false;
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

    private void validateIpfixMessageExportTime(String messageExportTime, String remoteAddress, String deviceName)
            throws CollectingProcessException {
        try {
            Instant exportTime = Instant.parse(messageExportTime);
            Instant now = getCurrentTime();
            Duration between = Duration.between(exportTime, now);
            /* FE will drop message if:
                1./ Message is too old when compares current timestamp at FE to exportTimestamp from device and the gap
                is greater than acceptant range
                    - Default acceptant gap for old message is 1 hour
                    - It's configurable via IPFIX_FE_ACCEPTANCE_PAST_TIME_GAP
                2./ Message is in future timestamp (if a message was exported from device earlier than the timestamp in
                the collector) and the gap is greater than acceptant range
                    - Default acceptant gap for future message is 1 min
                    - It's configurable via IPFIX_FE_ACCEPTANCE_FUTURE_TIME_GAP
            */
            if (isMessageInThePast(now, exportTime) && !isAcceptGap(between, true)) {
                handleLogsForOldMessage(now, exportTime, between, remoteAddress, deviceName);
            } else if (isMessageInTheFuture(now, exportTime) && !isAcceptGap(between, false)) {
                handleLogsForEarlyMessage(now, exportTime, between, remoteAddress, deviceName);
            }
        } catch (DateTimeParseException e) {
            LOGGER.error(String.format("Error while validating the IPFIX message messageExportTime: {}, deviceRefId: {}, remoteAddress: {}",
                    messageExportTime, deviceName, remoteAddress, e));
            throw new CollectingProcessException("Error while validating the IPFIX message time", e);
        }
    }

    private void handleLogsForOldMessage(Instant now, Instant exportTime, Duration between, String remoteAddress,
                                         String deviceName) throws CollectingProcessException {
        //Ipfix collector will receive many packages, the hostName is only decoded if the first message is valid ->
        // to avoid users confusing about deviceName is null, split into 2 cases
        LOGGER.error(String.format("Too big gap between the timestamp received the IPFIX message %s from deviceId "
                        + "%s hostIP %s and the current timestamp at the collector %s. The duration is %s", exportTime,
                deviceName, remoteAddress, now, between));
        throw new CollectingProcessException("Too big gap detected in IPFIX message: " + between.toMinutes() + "M");
    }

    private void handleLogsForEarlyMessage(Instant now, Instant exportTime, Duration between, String remoteAddress,
                                           String deviceName) throws CollectingProcessException {
        LOGGER.error(String.format("Received message in future %s from deviceId: %s, hostIP: %s, the current timestamp "
                + "at the collector %s. The duration is %s", exportTime, deviceName, remoteAddress, now, between));
        throw new CollectingProcessException("IPFIX message comes early: " + Math.abs(between.toMinutes()) + "M");
    }

    //For UT
    protected Instant getCurrentTime() {
        return Instant.now();
    }

    protected IpfixMessage createIpfixMessage(byte[] data) throws NotEnoughBytesException {
        IpfixMessage ipfixMessage;
        try {
            ipfixMessage = new IpfixMessage(data);
        } catch (NotEnoughBytesException e) {
            LOGGER.error("The IPFIX message is not correct formatted: " + IpfixUtilities.bytesToHex(data));
            throw new NotEnoughBytesException("The IPFIX message is not correct formatted", e);
        }
        return ipfixMessage;
    }

    @Override
    public void processAuthenFailure() {
        m_authenFailureCount.incrementAndGet();
        m_processedMessageCount.incrementAndGet(); // Processed Msg includes bad Msg
    }

    @Override
    public void processDecodeFailure() {
        m_decodeFailureCount.incrementAndGet();
        m_processedMessageCount.incrementAndGet(); // Processed Msg includes bad Msg
    }

    @Override
    public void processMessageCount() {
        m_processedMessageCount.incrementAndGet();
    }

    public interface TemplateProvider {
        AbstractTemplateRecord get(int templateId);
    }
}
