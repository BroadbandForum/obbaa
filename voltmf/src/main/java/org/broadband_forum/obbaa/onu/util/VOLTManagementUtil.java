/*
 * Copyright 2021 Broadband Forum
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

package org.broadband_forum.obbaa.onu.util;

import static org.broadband_forum.obbaa.onu.ONUConstants.ONU_NAME_PREFIX;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.dmyang.dao.DeviceDao;
import org.broadband_forum.obbaa.dmyang.entities.ActualAttachmentPoint;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.ExpectedAttachmentPoint;
import org.broadband_forum.obbaa.dmyang.entities.OnuManagementChain;
import org.broadband_forum.obbaa.dmyang.entities.OnuStateInfo;
import org.broadband_forum.obbaa.dmyang.entities.SoftwareImage;
import org.broadband_forum.obbaa.dmyang.entities.SoftwareImages;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.TxService;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.TxTemplate;
import org.broadband_forum.obbaa.nf.dao.NetworkFunctionDao;
import org.broadband_forum.obbaa.nf.dao.impl.KafkaTopicPurpose;
import org.broadband_forum.obbaa.nf.entities.KafkaTopic;
import org.broadband_forum.obbaa.nm.devicemanager.DeviceManager;
import org.broadband_forum.obbaa.onu.MediatedDeviceNetconfSession;
import org.broadband_forum.obbaa.onu.NotificationRequest;
import org.broadband_forum.obbaa.onu.ONUConstants;
import org.broadband_forum.obbaa.onu.UnknownONUHandler;
import org.broadband_forum.obbaa.onu.entity.UnknownONU;
import org.broadband_forum.obbaa.onu.exception.MessageFormatterException;
import org.broadband_forum.obbaa.onu.kafka.consumer.OnuKafkaConsumer;
import org.broadband_forum.obbaa.onu.kafka.producer.OnuKafkaProducer;
import org.broadband_forum.obbaa.onu.message.GpbFormatter;
import org.broadband_forum.obbaa.onu.message.JsonFormatter;
import org.broadband_forum.obbaa.onu.message.MessageFormatter;
import org.broadband_forum.obbaa.onu.message.NetworkWideTag;
import org.broadband_forum.obbaa.onu.message.ObjectType;
import org.broadband_forum.obbaa.onu.message.ResponseData;
import org.broadband_forum.obbaa.onu.notification.ONUNotification;
import org.broadband_forum.obbaa.onu.notification.OnuDiscoveryResultNotification;
import org.broadband_forum.obbaa.pma.PmaRegistry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public final class VOLTManagementUtil {
    private static final Logger LOGGER = Logger.getLogger(VOLTManagementUtil.class);
    private static final String INTERFACE_NS = "urn:ietf:params:xml:ns:yang:ietf-interfaces";
    private static final String PREFIX_INTERFACE = "if";
    private static final String XPON_NS = "urn:bbf:yang:bbf-xpon";
    private static final String PREFIX_XPON = "bbf-xpon";
    private static final Object LOCK = new Object();
    private static Map<String, Pair<String, String>> m_requestMap = new HashMap<>();
    private static Map<String, ArrayList<String>> m_networkFunctionMap = new HashMap<>();

    private VOLTManagementUtil() {
        //Not called
    }

    private static List<String> evaluateXPath(Document document, String xpathExpression) {
        // Create XPathFactory object
        XPathFactory xpathFactory = XPathFactory.newInstance();
        // Create XPath object
        XPath xpath = xpathFactory.newXPath();
        xpath.setNamespaceContext(new NamespaceContext() {
            @Override
            public Iterator getPrefixes(String arg0) {
                return Collections.emptyIterator();
            }

            @Override
            public String getPrefix(String url) {
                if (url != null) {
                    switch (url) {
                        case INTERFACE_NS:
                            return PREFIX_INTERFACE;
                        case XPON_NS:
                            return PREFIX_XPON;
                        default:
                            return null;
                    }
                }
                return null;
            }

            @Override
            public String getNamespaceURI(String prefix) {
                if (prefix != null) {
                    switch (prefix) {
                        case PREFIX_INTERFACE:
                            return INTERFACE_NS;
                        case PREFIX_XPON:
                            return XPON_NS;
                        default:
                            return null;
                    }
                }
                return null;
            }
        });

        List<String> values = new ArrayList<>();
        try {
            // Create XPathExpression object
            XPathExpression expr = xpath.compile(xpathExpression);
            // Evaluate expression result on XML document
            NodeList nodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                values.add(nodes.item(i).getNodeValue());
            }
        } catch (XPathExpressionException e) {
            LOGGER.error("Error during the XPathEvaluation of " + xpathExpression);
        }
        return values;
    }


    public static boolean isInPermittedAttachmentPoint(Device onuDevice, String oltDeviceName, ONUNotification onuNotification,
                                                       PmaRegistry pmaRegistry) {
        ExpectedAttachmentPoint expectedAttachmentPoint = onuDevice.getDeviceManagement().getOnuConfigInfo().getExpectedAttachmentPoint();
        boolean attachmentPointMatched = true;
        if (expectedAttachmentPoint.getOltName() != null && expectedAttachmentPoint.getChannelPartitionName() != null) {
            String actualChannelPartition = getChannelPartition(oltDeviceName, onuNotification.getChannelTermRef(), pmaRegistry);
            if (expectedAttachmentPoint.getOltName().equals(oltDeviceName)
                    && expectedAttachmentPoint.getChannelPartitionName().equals(actualChannelPartition)) {
                attachmentPointMatched = true;
            } else {
                attachmentPointMatched = false;
            }
        }
        return attachmentPointMatched;
    }

    public static boolean isInPermittedAttachmentPoint(Device onuDevice, UnknownONU unknownONU, PmaRegistry pmaRegistry) {
        ExpectedAttachmentPoint expectedAttachmentPoint = onuDevice.getDeviceManagement().getOnuConfigInfo().getExpectedAttachmentPoint();
        boolean attachmentPointMatched = true;
        if (expectedAttachmentPoint.getOltName() != null && expectedAttachmentPoint.getChannelPartitionName() != null) {
            String actualChannelPartition = getChannelPartition(unknownONU.getOltDeviceName(), unknownONU.getChannelTermRef(), pmaRegistry);
            if (expectedAttachmentPoint.getOltName().equals(unknownONU.getOltDeviceName())
                    && expectedAttachmentPoint.getChannelPartitionName().equals(actualChannelPartition)) {
                attachmentPointMatched = true;
            } else {
                attachmentPointMatched = false;
            }
        }
        return attachmentPointMatched;
    }

    private static String getChannelPartition(String oltDeviceName, String channelTermRef, PmaRegistry pmaRegistry) {
        String channelPartition = "";
        GetConfigRequest getConfig = new GetConfigRequest();
        getConfig.setSourceRunning();
        getConfig.setMessageId("internal");
        try {
            Map<NetConfResponse, List<Notification>> response = pmaRegistry.executeNC(oltDeviceName, getConfig.requestToString());
            String xpathExpression;
            List<String> evaluatedXpath = new ArrayList<>();
            Document responseDocument = response.entrySet().iterator().next().getKey().getResponseDocument();
            xpathExpression = "//if:interfaces/if:interface[if:name='" + channelTermRef + "']/bbf-xpon:channel-termination/"
                    + "bbf-xpon:channel-pair-ref/text()";
            evaluatedXpath = evaluateXPath(responseDocument, xpathExpression);
            String channelPair = "";
            if (!evaluatedXpath.isEmpty()) {
                channelPair = evaluatedXpath.get(0);
                xpathExpression = "//if:interfaces/if:interface[if:name='" + channelPair + "']/bbf-xpon:channel-pair/"
                        + "bbf-xpon:channel-partition-ref/text()";
                evaluatedXpath = evaluateXPath(responseDocument, xpathExpression);
                if (!evaluatedXpath.isEmpty()) {
                    channelPartition = evaluatedXpath.get(0);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error while fetching channel partition corresponding to channelTermination: " + channelTermRef, e);
        }
        return channelPartition;
    }

    public static String generateRandomMessageId() {
        int randomNumber = (int) (Math.random() * (ONUConstants.MESSAGE_ID_MAX - ONUConstants.MESSAGE_ID_MIN + 1)
                + ONUConstants.MESSAGE_ID_MIN);
        return String.valueOf(randomNumber);
    }

    public static String processInternalGetResponseAndRetrieveHwProperties(String responseStatus, String getResponseData) {
        String equipmentId = null;
        if (responseStatus.equals(ONUConstants.OK_RESPONSE) && !getResponseData.isEmpty()) {
            try {
                JSONObject getResponseDataJson = new JSONObject(getResponseData);
                equipmentId = getResponseDataJson.getJSONObject(ONUConstants.NETWORK_MANAGER_JSON_KEY)
                        .getJSONObject(ONUConstants.DEVICE_STATE_JSON_KEY).getJSONObject(ONUConstants.ONU_STATE_INFO_JSON_KEY)
                        .optString(ONUConstants.EQPT_ID_JSON_KEY);
            } catch (JSONException e) {
                LOGGER.error("Unable to form JSONObject " + e);
            }
        } else {
            LOGGER.info(String.format("Received %s response for internal get operation", ONUConstants.NOK_RESPONSE));
        }
        return equipmentId;
    }

    public static String processNotificationAndRetrieveOnuAlignmentStatus(String notificationData) {
        String alignmentStatus = null;
        if (!notificationData.isEmpty()) {
            try {
                JSONObject getResponseDataJson = new JSONObject(notificationData);
                alignmentStatus = getResponseDataJson.getJSONObject(ONUConstants.VOMCI_FUNC_ONU_ALIGNMENT_STATUS_JSON_KEY)
                        .optString(ONUConstants.ALIGNMENT_STATUS_JSON_KEY);
            } catch (JSONException e) {
                LOGGER.error("Unable to form JSONObject " + e);
            }
        } else {
            LOGGER.info("Received notification without information");
        }
        return alignmentStatus;
    }

    public static Set<SoftwareImage> processInternalGetResponseAndRetrieveSWProperties(String responseStatus, String getResponseData) {
        Set<SoftwareImage> softwareImageSet = new HashSet<SoftwareImage>();
        if (responseStatus.equals(ONUConstants.OK_RESPONSE) && !getResponseData.isEmpty()) {
            try {
                JSONArray swImageJsonArray = (new JSONObject(getResponseData)).getJSONObject(ONUConstants.NETWORK_MANAGER_JSON_KEY)
                        .getJSONObject(ONUConstants.DEVICE_STATE_JSON_KEY).getJSONObject(ONUConstants.ONU_STATE_INFO_JSON_KEY)
                        .optJSONObject(ONUConstants.SOFTWARE_IMAGES_JSON_KEY).optJSONArray(ONUConstants.SOFTWARE_IMAGE_JSON_KEY);

                for (int j = 0; j < swImageJsonArray.length(); j++) {

                    JSONObject jsonObject = swImageJsonArray.optJSONObject(j);
                    SoftwareImage softwareImage = new SoftwareImage();

                    for (String key : jsonObject.keySet()) {
                        switch (key) {
                            case ONUConstants.SOFTWARE_IMAGE_ID_JSON_KEY:
                                softwareImage.setId(jsonObject.optInt(ONUConstants.SOFTWARE_IMAGE_ID_JSON_KEY));
                                break;
                            case ONUConstants.SOFTWARE_IMAGE_VERSION_JSON_KEY:
                                softwareImage.setVersion(jsonObject.optString(ONUConstants.SOFTWARE_IMAGE_VERSION_JSON_KEY));
                                break;
                            case ONUConstants.SOFTWARE_IMAGE_PRODUCT_CODE_JSON_KEY:
                                softwareImage.setProductCode(jsonObject.optString(ONUConstants.SOFTWARE_IMAGE_PRODUCT_CODE_JSON_KEY));
                                break;
                            case ONUConstants.SOFTWARE_IMAGE_IS_ACTIVE_JSON_KEY:
                                String isActive = jsonObject.optString(ONUConstants.SOFTWARE_IMAGE_IS_ACTIVE_JSON_KEY);
                                softwareImage.setIsActive(Boolean.parseBoolean(isActive));
                                break;
                            case ONUConstants.SOFTWARE_IMAGE_IS_COMMITTED_JSON_KEY:
                                String isCommitted = jsonObject.optString(ONUConstants.SOFTWARE_IMAGE_IS_COMMITTED_JSON_KEY);
                                softwareImage.setIsCommitted(Boolean.parseBoolean(isCommitted));
                                break;
                            case ONUConstants.SOFTWARE_IMAGE_IS_VALID_JSON_KEY:
                                String isValid = jsonObject.optString(ONUConstants.SOFTWARE_IMAGE_IS_VALID_JSON_KEY);
                                softwareImage.setIsValid(Boolean.parseBoolean(isValid));
                                break;

                            case ONUConstants.SOFTWARE_IMAGE_HASH_JSON_KEY:
                                softwareImage.setHash(jsonObject.optString(ONUConstants.SOFTWARE_IMAGE_HASH_JSON_KEY));
                                break;
                            default:
                                LOGGER.warn(String.format("improper response received from vOMCI, failed to to update software details "
                                        + "for the device. response is %s", getResponseData));

                        }
                    }

                    softwareImageSet.add(softwareImage);
                }
            } catch (JSONException e) {
                LOGGER.error("Unable to form JSONObject " + e);
            }
        } else {
            if (responseStatus.equals(ONUConstants.NOK_RESPONSE)) {
                LOGGER.error(String.format("Received %s response for internal get operation", ONUConstants.NOK_RESPONSE));
            }
            if (getResponseData.isEmpty() || getResponseData == null) {
                LOGGER.error(String.format("Response received from vOMCI is either null or empty :%s", getResponseData));

            }
        }
        return softwareImageSet;
    }

    public static UnknownONU buildUnknownOnu(String oltDeviceName, ONUNotification onuNotification) {
        UnknownONU onuEntity = new UnknownONU();
        onuEntity.setOltDeviceName(oltDeviceName);
        if ((onuNotification.getRegId() != null) && !onuNotification.getRegId().isEmpty()) {
            onuEntity.setRegistrationId(onuNotification.getRegId());
        }
        if ((onuNotification.getSerialNo() != null) && !onuNotification.getSerialNo().isEmpty()) {
            onuEntity.setSerialNumber(onuNotification.getSerialNo());
        }
        if ((onuNotification.getVAniRef() != null) && !onuNotification.getVAniRef().isEmpty()) {
            onuEntity.setVAniRef(onuNotification.getVAniRef());
        }
        if ((onuNotification.getOnuState() != null) && !onuNotification.getOnuState().isEmpty()) {
            onuEntity.setOnuState(onuNotification.getOnuState());
        }
        if ((onuNotification.getOnuId() != null) && !onuNotification.getOnuId().isEmpty()) {
            onuEntity.setOnuID(onuNotification.getOnuId());
        }
        if ((onuNotification.getChannelTermRef() != null) && !onuNotification.getChannelTermRef().isEmpty()) {
            onuEntity.setChannelTermRef(onuNotification.getChannelTermRef());
        }
        if ((onuNotification.getOnuStateLastChanged() != null) && !onuNotification.getOnuStateLastChanged().isEmpty()) {
            onuEntity.setOnuStateLastChange(convertStringToTimestamp(onuNotification.getOnuStateLastChanged()));
        }
        return onuEntity;
    }

    private static Timestamp convertStringToTimestamp(String timestampString) {
        if (timestampString != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
            try {
                Date parsedDate = dateFormat.parse(timestampString);
                Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
                return timestamp;
            } catch (ParseException e) {
                LOGGER.error("Field is not in the correct format for timestamp : " + timestampString);
            }
        }
        return null;
    }

    public static boolean isResponseOK(ArrayList<Boolean> respArray) {
        return (respArray == null || respArray.isEmpty() || respArray.contains(false)) ? false : true;
    }

    public static void updateOnuStateInfoInDevice(Device device, UnknownONU unknownONU, DeviceManager deviceManager) {
        ActualAttachmentPoint actualAttachmentPoint = new ActualAttachmentPoint();
        actualAttachmentPoint.setChannelTerminationRef(unknownONU.getChannelTermRef());
        actualAttachmentPoint.setOltName(unknownONU.getOltDeviceName());
        actualAttachmentPoint.setOnuId(unknownONU.getOnuId());
        OnuStateInfo onuStateInfo = new OnuStateInfo();
        onuStateInfo.setOnuState(unknownONU.getOnuState());
        onuStateInfo.setActualAttachmentPoint(actualAttachmentPoint);
        onuStateInfo.setEquipmentId(unknownONU.getEquipmentId());
        onuStateInfo.setOnuStateInfoId(device.getDeviceManagement().getDeviceState().getDeviceNodeId());
        actualAttachmentPoint.setAttachmentPointId(onuStateInfo.getOnuStateInfoId());
        SoftwareImages softwareImages = new SoftwareImages();
        /* below commented lines will be uncommented/enabled after vomci has the get request & response handling support */
        /* Set<SoftwareImage> softwareImageSet = unknownONU.getSoftwareImages().getSoftwareImage();
        for (SoftwareImage softwareImage : softwareImageSet) {
            softwareImage.setParentId(device.getDeviceManagement().getDeviceState().getDeviceNodeId());
        }*/
        Set<SoftwareImage> softwareImageSet = Collections.emptySet(); // this line to be removed after uncommenting above lines
        softwareImages.setSoftwareImagesId(device.getDeviceManagement().getDeviceState().getDeviceNodeId());
        softwareImages.setSoftwareImage(softwareImageSet);
        onuStateInfo.setSoftwareImages(softwareImages);
        deviceManager.updateOnuStateInfo(device.getDeviceName(), onuStateInfo);
    }


    public static void updateKafkaSubscriptions(String vomciFunctionName, MessageFormatter messageFormatter, NetworkFunctionDao
            networkFunctionDao, OnuKafkaConsumer onuKafkaConsumer, Map<String, Set<String>> kafkaConsumerTopicMap) {
        if (messageFormatter instanceof GpbFormatter) {
            Set<KafkaTopic> kafkaTopicSet = networkFunctionDao.getKafkaConsumerTopics(vomciFunctionName);
            Set<String> kafkaTopicNameSet = new HashSet<>();
            if (kafkaTopicSet != null && !kafkaTopicSet.isEmpty()) {
                onuKafkaConsumer.updateSubscriberTopics(kafkaTopicSet);
                for (KafkaTopic kafkaTopic : kafkaTopicSet) {
                    kafkaTopicNameSet.add(kafkaTopic.getTopicName());
                }
                kafkaConsumerTopicMap.put(vomciFunctionName, kafkaTopicNameSet);
            }
        }
    }

    public static void removeSubscriptions(String networkFunctionName, OnuKafkaConsumer onuKafkaConsumer,
                                           Map<String, Set<String>> kafkaConsumerTopicMap) {
        if (kafkaConsumerTopicMap.containsKey(networkFunctionName)) {
            Set<String> kafkaTopicNameSet = kafkaConsumerTopicMap.get(networkFunctionName);
            Set<String> topicsToUnsubscribe = new HashSet<>(kafkaTopicNameSet);
            kafkaConsumerTopicMap.remove(networkFunctionName);
            for (String kafkaTopicName : kafkaTopicNameSet) {
                for (Set<String> kafkaTopicNames : kafkaConsumerTopicMap.values()) {
                    if (kafkaTopicNames.contains(kafkaTopicName)) {
                        topicsToUnsubscribe.remove(kafkaTopicName);
                        break;
                    }
                }
            }

            if (!topicsToUnsubscribe.isEmpty()) {
                LOGGER.info("Unsubscribing topics: " + topicsToUnsubscribe);
                onuKafkaConsumer.removeSubscriberTopics(topicsToUnsubscribe);
            }
        }
    }

    public static MediatedDeviceNetconfSession getMediatedDeviceNetconfSession(Device onuDevice,
                                                                               NetconfConnectionManager netconfConnectionManager) {
        if (onuDevice == null) {
            return null;
        }
        NetconfClientSession devSession = netconfConnectionManager.getMediatedDeviceSession(onuDevice);
        if (devSession != null) {
            return (MediatedDeviceNetconfSession) devSession;
        }
        return null;
    }

    public static void persistUnknownOnuToDB(String oltDeviceName, ONUNotification onuNotification, TxService txService,
                                             UnknownONUHandler unknownONUHandler) {
        if (onuNotification != null) {
            UnknownONU onuEntity = buildUnknownOnu(oltDeviceName, onuNotification);
            LOGGER.info("Persisting unknown onu to DB: " + onuEntity);
            txService.executeWithTxRequiresNew((TxTemplate<Void>) () -> {
                UnknownONU matchedOnu = unknownONUHandler.findUnknownOnuEntity(onuEntity.getSerialNumber(),
                        onuEntity.getRegistrationId());
                if (matchedOnu != null) {
                    LOGGER.info("Found existing unknown ONU, updating it");
                    matchedOnu.setChannelTermRef(onuEntity.getChannelTermRef());
                    matchedOnu.setOnuID(onuEntity.getOnuId());
                    matchedOnu.setVAniRef(onuEntity.getVAniRef());
                    matchedOnu.setOnuState(onuEntity.getOnuState());
                    matchedOnu.setOltDeviceName(onuEntity.getOltDeviceName());
                    matchedOnu.setOnuStateLastChange(onuEntity.getOnuStateLastChange());
                } else {
                    unknownONUHandler.createUnknownONUEntity(onuEntity);
                }
                return null;
            });
        }
    }

    private static void sendInternalGetRequestOnKafka(NotificationRequest request, MessageFormatter messageFormatter, TxService txService,
                                                      DeviceDao deviceDao, NetworkFunctionDao networkFunctionDao,
                                                      OnuKafkaProducer onuKafkaProducer) {
        String kafkaTopicName = null;
        Object notification = null;
        AtomicReference<HashSet<String>> kafkaTopicNameSet = new AtomicReference<>(new HashSet<String>());
        request.setMessageId(ONUConstants.DEFAULT_MESSAGE_ID);
        NetworkWideTag networkWideTag = new NetworkWideTag(request.getOnuDeviceName(), request.getOltDeviceName(),
                request.getOnuId(), request.getChannelTermRef(), request.getLabels());
        if (messageFormatter instanceof JsonFormatter) {
            kafkaTopicName = ONUConstants.ONU_REQUEST_KAFKA_TOPIC;
        } else {
            txService.executeWithTxRequired((TxTemplate<Void>) () -> {
                String networkFunctionName = deviceDao.getVomciFunctionName(request.getOnuDeviceName());
                final HashSet<String> kafkaTopicNameFinal = networkFunctionDao.getKafkaTopicNames(networkFunctionName,
                        KafkaTopicPurpose.VOMCI_REQUEST);
                if (kafkaTopicNameFinal != null && !kafkaTopicNameFinal.isEmpty()) {
                    kafkaTopicNameSet.set(kafkaTopicNameFinal);
                } else {
                    LOGGER.error(String.format("Topic Name for the Network Function %s and Topic Purpose %s was not found",
                            networkFunctionName, KafkaTopicPurpose.VOMCI_REQUEST));
                }
                return null;
            });
        }
        try {
            notification = messageFormatter.getFormattedRequest(request, ONUConstants.ONU_GET_OPERATION, null,
                    null, null, null, networkWideTag);
            if (messageFormatter instanceof JsonFormatter) {
                onuKafkaProducer.sendNotification(kafkaTopicName, notification);
            } else {
                if (!kafkaTopicNameSet.get().isEmpty()) {
                    for (String topicName : kafkaTopicNameSet.get()) {
                        onuKafkaProducer.sendNotification(topicName, notification);
                    }
                }
            }
        } catch (Exception exception) {
            LOGGER.error(String.format("Failed to send %s notification onto Kafka topic %s ", notification, kafkaTopicName), exception);
        }
    }

    public static ResponseData updateOperationTypeInResponseData(ResponseData responseData, MessageFormatter messageFormatter) {
        if (messageFormatter instanceof GpbFormatter) {
            String identifier = responseData.getIdentifier();
            if (responseData.getOperationType().equals(NetconfResources.RPC)
                    || responseData.getOperationType().equals(NetconfResources.ACTION)) {
                if (m_requestMap.get(identifier) != null) {
                    String operationType = m_requestMap.get(identifier).getSecond();
                    String onuName = m_requestMap.get(identifier).getFirst();
                    if (operationType != null) {
                        responseData.setOperationType(operationType);
                    }
                    if (onuName != null) {
                        responseData.setOnuName(onuName);
                    }
                } else {
                    LOGGER.error(String.format("Request correlating the response id %s received for %s was not found in requestMap.",
                            identifier, responseData));
                }
            }
        }
        return responseData;
    }

    public static void retrieveAndUpdateHwSwPropertiesForUnknownONU(ResponseData responseData, TxService txService,
                                                                    UnknownONUHandler unknownONUHandler) {
        String responseStatus = responseData.getResponseStatus();
        String getResponseData = responseData.getResponsePayload();
        String oltName = responseData.getOltName();
        String channelTermRef = responseData.getChannelTermRef();
        String onuId = responseData.getOnuId();

        txService.executeWithTxRequired((TxTemplate<Void>) () -> {
            UnknownONU matchedUnknownONU = unknownONUHandler.findMatchingUnknownOnu(oltName, channelTermRef, onuId);
            if (matchedUnknownONU != null) {
                String eqptId = processInternalGetResponseAndRetrieveHwProperties(responseStatus, getResponseData);
                matchedUnknownONU.setEquipmentId(eqptId);
                Set<SoftwareImage> softwareImageSet = processInternalGetResponseAndRetrieveSWProperties(responseStatus,
                        getResponseData);

                SoftwareImages softwareImages = new SoftwareImages();
                for (SoftwareImage softwareImage : softwareImageSet) {
                    softwareImage.setParentId(ONU_NAME_PREFIX.concat(matchedUnknownONU.getSerialNumber()));
                }
                softwareImages.setSoftwareImagesId(ONU_NAME_PREFIX.concat(matchedUnknownONU.getSerialNumber()));
                softwareImages.setSoftwareImage(softwareImageSet);
                matchedUnknownONU.setSoftwareImages(softwareImages);
            }
            return null;
        });

    }

    public static void sendOnuDiscoveryResultNotification(Device onuDevice, String onuState, NotificationService notificationService) {
        try {
            Notification notification = new OnuDiscoveryResultNotification(onuDevice, onuState);
            notificationService.sendNotification(NetconfResources.STATE_CHANGE, notification);
        } catch (NetconfMessageBuilderException e) {
            LOGGER.error(String.format("Error while sending onu discovery result notification for device %s with onu state %s",
                    onuDevice.getDeviceName(), onuState), e);
        }
    }

    public static List<Pair<ObjectType, String>> getManagementChain(String onuDeviceName, TxService txService, DeviceDao deviceDao) {
        AtomicReference<ArrayList<String>> networkFunctionNamesList = new AtomicReference<ArrayList<String>>();
        txService.executeWithTxRequired((TxTemplate<Void>) () -> {
            OnuManagementChain[] chains = deviceDao.getOnuManagementChains(onuDeviceName);
            ArrayList<String> nwFnNames = new ArrayList<>();
            if (chains.length < 2) {
                LOGGER.warn(String.format("Onu management chain should atleast have two values denoting vOMCI function and OLT."
                        + " ONU device %s has only %d management values", onuDeviceName, chains.length));
            } else {
                for (int i = 0; i < chains.length - 1; i++) {
                    nwFnNames.add(chains[i].getOnuManagementChain());
                }
                networkFunctionNamesList.set(nwFnNames);
            }
            return null;
        });
        ArrayList<String> networkFunctionNames = networkFunctionNamesList.get();
        if (networkFunctionNames == null || networkFunctionNames.isEmpty()) {
            LOGGER.info("Trying to fetch networkFunctionNames from local map");
            networkFunctionNames = m_networkFunctionMap.get(onuDeviceName);
            m_networkFunctionMap.remove(onuDeviceName);
        }
        List<Pair<ObjectType, String>> networkFunctionList = new ArrayList<>();
        if (networkFunctionNames != null && !networkFunctionNames.isEmpty()) {
            m_networkFunctionMap.put(onuDeviceName, networkFunctionNames);
            for (int i = 0; i < networkFunctionNames.size(); i++) {
                ObjectType objectType;
                if (i == 0) {
                    objectType = ObjectType.VOMCI_FUNCTION;
                } else {
                    objectType = ObjectType.VOMCI_PROXY;
                }
                networkFunctionList.add(new Pair<ObjectType, String>(objectType, networkFunctionNames.get(i)));
            }
        } else {
            LOGGER.error("No network functions found for ONU device " + onuDeviceName);
        }
        return networkFunctionList;
    }

    public static void sendKafkaNotification(NotificationRequest request, MessageFormatter messageFormatter,
                                             OnuKafkaProducer onuKafkaProducer) {
        String kafkaTopicName = null;
        String messageId = VOLTManagementUtil.generateRandomMessageId();
        request.setMessageId(messageId);
        NetworkWideTag networkWideTag = new NetworkWideTag(request.getOnuDeviceName(), request.getOltDeviceName(),
                request.getOnuId(), request.getChannelTermRef(), request.getLabels());
        Object notification = null;
        kafkaTopicName = ONUConstants.ONU_NOTIFICATION_KAFKA_TOPIC;
        try {
            notification = messageFormatter.getFormattedRequest(request, request.getEvent(), null,
                    null, null, null, networkWideTag);
            onuKafkaProducer.sendNotification(kafkaTopicName, notification);
        } catch (Exception exception) {
            LOGGER.error(String.format("Failed to send %s notification onto Kafka topic %s ", notification, kafkaTopicName), exception);
        }
    }

    public static void registerInRequestMap(AbstractNetconfRequest request, String deviceName, String operation) {
        synchronized (LOCK) {
            m_requestMap.put(request.getMessageId(), new Pair<String, String>(deviceName, operation));
        }
    }

    public static void removeRequestFromMap(String identifier) {
        synchronized (LOCK) {
            m_requestMap.remove(identifier);
        }
    }

    public static void setMessageId(AbstractNetconfRequest request, AtomicLong messageId) {
        final String currentMessageId = String.valueOf(messageId.addAndGet(1));
        request.setMessageId(currentMessageId);
    }

    public static void removeActualAttachmentPointInfo(Device onuDevice, TxService txService, DeviceManager deviceManager) {
        txService.executeWithTxRequiresNew((TxTemplate<Void>) () -> {
            deviceManager.updateOnuStateInfo(onuDevice.getDeviceName(), null);
            return null;
        });
    }

    public static void sendGetRequest(ONUNotification onuNotification, MessageFormatter messageFormatter, TxService txService,
                                      DeviceDao deviceDao, NetworkFunctionDao networkFunctionDao,
                                      OnuKafkaProducer onuKafkaProducer) {
        String serialNumber = onuNotification.getSerialNo();
        String vendor = serialNumber.substring(0, 4);
        HashMap<String, String> labels = new HashMap<>();
        labels.put("name", "vendor");
        labels.put("value", vendor);
        NotificationRequest request = new NotificationRequest(null, onuNotification.getOltDeviceName(), onuNotification.getChannelTermRef(),
                onuNotification.getOnuId(), ONUConstants.REQUEST_EVENT, labels);
        LOGGER.info(String.format("Sending internal get request to UnknownONU [olt-name:%s, channel-termination-ref:%s, onu-id:%s] ",
                onuNotification.getOltDeviceName(), onuNotification.getChannelTermRef(), onuNotification.getOnuId()));
        sendInternalGetRequestOnKafka(request, messageFormatter, txService, deviceDao, networkFunctionDao,
                onuKafkaProducer);
    }

    public static void sendKafkaMessage(Object kafkaMessage, String networkFunctionName,
                                        TxService txService, NetworkFunctionDao networkFunctionDao,
                                        OnuKafkaProducer onuKafkaProducer) {

        AtomicReference<HashSet<String>> kafkaTopicNames = new AtomicReference<>(new HashSet<String>());
        txService.executeWithTxRequired((TxTemplate<Void>) () -> {
            final HashSet<String> kafkaTopicNamesFinal = networkFunctionDao.getKafkaTopicNames(networkFunctionName,
                    KafkaTopicPurpose.VOMCI_REQUEST);
            if (kafkaTopicNamesFinal != null && !kafkaTopicNamesFinal.isEmpty()) {
                kafkaTopicNames.set(kafkaTopicNamesFinal);
            } else {
                LOGGER.error(String.format("Topic Name for the Network Function %s and Topic Purpose %s was not found",
                        networkFunctionName, KafkaTopicPurpose.VOMCI_REQUEST));
            }
            return null;
        });
        if (!kafkaTopicNames.get().isEmpty()) {
            for (String topicName : kafkaTopicNames.get()) {
                try {
                    onuKafkaProducer.sendNotification(topicName, kafkaMessage);
                } catch (MessageFormatterException e) {
                    LOGGER.error(String.format("Failed to send kafka message: %s to topic :%s", kafkaMessage.toString(), topicName, e));
                }
            }
        }
    }
}
