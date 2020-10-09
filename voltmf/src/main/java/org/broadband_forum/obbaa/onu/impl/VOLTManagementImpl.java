/*
 * Copyright 2020 Broadband Forum
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

package org.broadband_forum.obbaa.onu.impl;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.dm.DeviceManager;
import org.broadband_forum.obbaa.dmyang.entities.ActualAttachmentPoint;
import org.broadband_forum.obbaa.dmyang.entities.AttachmentPoint;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants;
import org.broadband_forum.obbaa.dmyang.entities.OnuStateInfo;
import org.broadband_forum.obbaa.netconf.alarm.api.AlarmService;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.TxService;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.TxTemplate;
import org.broadband_forum.obbaa.onu.MediatedDeviceNetconfSession;
import org.broadband_forum.obbaa.onu.NotificationRequest;
import org.broadband_forum.obbaa.onu.ONUConstants;
import org.broadband_forum.obbaa.onu.UnknownONUHandler;
import org.broadband_forum.obbaa.onu.VOLTManagement;
import org.broadband_forum.obbaa.onu.entity.UnknownONU;
import org.broadband_forum.obbaa.onu.kafka.OnuKafkaProducer;
import org.broadband_forum.obbaa.onu.notification.ONUNotification;
import org.broadband_forum.obbaa.onu.notification.OnuStateChangeNotification;
import org.broadband_forum.obbaa.pma.PmaRegistry;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * <p>
 * Manages vOMCI based ONUs such as CRUD operations on ONUs and notification handling
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 22/06/2020.
 */
public class VOLTManagementImpl implements VOLTManagement {
    private static final Logger LOGGER = Logger.getLogger(VOLTManagementImpl.class);
    private static final String INTERFACE_NS = "urn:ietf:params:xml:ns:yang:ietf-interfaces";
    private static final String PREFIX_INTERFACE = "if";
    private static final String XPON_NS = "urn:bbf:yang:bbf-xpon";
    private static final String PREFIX_XPON = "bbf-xpon";
    private final TxService m_txService;
    private final DeviceManager m_deviceManager;
    private final AlarmService m_alarmService;
    private final UnknownONUHandler m_unknownOnuHandler;
    private final OnuKafkaProducer m_kafkaProducer;
    private final NetconfConnectionManager m_connectionManager;
    private final ModelNodeDataStoreManager m_modelNodeDSM;
    private final NotificationService m_notificationService;
    private final AdapterManager m_adapterManager;
    private final PmaRegistry m_pmaRegistry;
    private ThreadPoolExecutor m_processNotificationResponsePool;
    private ThreadPoolExecutor m_processRequestResponsePool;
    private ThreadPoolExecutor m_processNotificationRequestPool;
    private ThreadPoolExecutor m_kafkaCommunicationPool;

    public VOLTManagementImpl(TxService txService, DeviceManager deviceManager, AlarmService alarmService,
                              OnuKafkaProducer kafkaProducer, UnknownONUHandler unknownONUHandler,
                              NetconfConnectionManager connectionManager, ModelNodeDataStoreManager modelNodeDSM,
                              NotificationService notificationService, AdapterManager adapterManager,
                              PmaRegistry pmaRegistry) {
        m_txService = txService;
        m_deviceManager = deviceManager;
        m_alarmService = alarmService;
        m_kafkaProducer = kafkaProducer;
        m_unknownOnuHandler = unknownONUHandler;
        this.m_connectionManager = connectionManager;
        m_modelNodeDSM = modelNodeDSM;
        m_notificationService = notificationService;
        m_adapterManager = adapterManager;
        m_pmaRegistry = pmaRegistry;
    }

    public void init() {
        m_processNotificationRequestPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(ONUConstants.PROCESS_NOTIF_REQUEST_THREADS);
        m_processNotificationResponsePool = (ThreadPoolExecutor) Executors.newFixedThreadPool(ONUConstants.PROCESS_NOTIF_RESPONSE_THREADS);
        m_processRequestResponsePool = (ThreadPoolExecutor) Executors.newFixedThreadPool(ONUConstants.PROCESS_REQ_RESPONSE_THREADS);
        m_kafkaCommunicationPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(ONUConstants.KAFKA_COMMUNICATION_THREADS);
    }

    public void destroy() {
        m_processNotificationRequestPool.shutdown();
        m_processNotificationResponsePool.shutdown();
        m_processRequestResponsePool.shutdown();
        m_kafkaCommunicationPool.shutdown();
    }

    @Override
    public void onuNotificationProcess(ONUNotification onuNotification, String oltDeviceName) {
        // TO DO:: Perform ONU authentication -
        String onuSerialNum = onuNotification.getSerialNo();
        Device onuDevice = null;
        try {
            onuDevice = m_deviceManager.getDeviceWithSerialNumber(onuSerialNum);
        } catch (Exception e) {
            LOGGER.info("ONU device not found with serial number: " + onuSerialNum);
        }
        if (onuDevice == null) {
            LOGGER.info(String.format("ONU notification request %s is received for ONU device, but unable to find"
                    + " pre-configured device based on the serial number: %s", onuNotification.getOnuState(), onuSerialNum));

            // If ONU authentication fails create unknown ONU and persist in DB
            if (ONUConstants.ONU_DETECTED.equals(onuNotification.getMappedEvent())) {
                persistUnknownOnuToDB(oltDeviceName, onuNotification);
            } else if (ONUConstants.ONU_UNDETECTED.equals(onuNotification.getMappedEvent())) {
                UnknownONU onuEntity = buildUnknownOnu(oltDeviceName, onuNotification);
                m_txService.executeWithTxRequiresNew((TxTemplate<Void>) () -> {
                    UnknownONU matchedOnu = m_unknownOnuHandler.findUnknownOnuEntity(onuEntity.getSerialNumber(),
                            onuEntity.getRegistrationId());
                    if (matchedOnu != null) {
                        m_unknownOnuHandler.deleteUnknownOnuEntity(matchedOnu);
                    }
                    return null;
                });
            }
        } else if (onuDevice.isMediatedSession()) {
            if (isInPermittedAttachmentPoint(onuDevice, oltDeviceName, onuNotification)) {
                if (ONUConstants.ONU_DETECTED.equals(onuNotification.getMappedEvent())) {
                    if (!onuDevice.isAligned()) {
                        Device finalOnuDevice = onuDevice;
                        m_txService.executeWithTxRequiresNew((TxTemplate<Void>) () -> {
                            UnknownONU onuEntity = buildUnknownOnu(oltDeviceName, onuNotification);
                            updateOnuStateInfoInDevice(finalOnuDevice, onuEntity);
                            return null;
                        });

                        HashMap<String, String> labels = getLabels(onuDevice);
                        sendDetectForPreconfiguredDevice(onuDevice.getDeviceName(), onuNotification, labels);
                    }
                } else if (ONUConstants.ONU_UNDETECTED.equals(onuNotification.getMappedEvent())) {
                    removeActualAttachmentPointInfo(onuDevice, onuNotification);
                    handleUndetect(onuDevice, onuNotification);
                }
            } else {
                // Notification received for the ONU from a different attachment point, so persist/remove in DB
                if (ONUConstants.ONU_DETECTED.equals(onuNotification.getMappedEvent())) {
                    persistUnknownOnuToDB(oltDeviceName, onuNotification);
                } else if (ONUConstants.ONU_UNDETECTED.equals(onuNotification.getMappedEvent())) {
                    UnknownONU onuEntity = buildUnknownOnu(oltDeviceName, onuNotification);
                    m_txService.executeWithTxRequiresNew((TxTemplate<Void>) () -> {
                        UnknownONU matchedOnu = m_unknownOnuHandler.findUnknownOnuEntity(onuEntity.getSerialNumber(),
                                onuEntity.getRegistrationId());
                        if (matchedOnu != null) {
                            m_unknownOnuHandler.deleteUnknownOnuEntity(matchedOnu);
                        }
                        return null;
                    });
                }
            }
        }
    }

    private void removeActualAttachmentPointInfo(Device onuDevice, ONUNotification onuNotification) {
        m_txService.executeWithTxRequiresNew((TxTemplate<Void>) () -> {
            m_deviceManager.updateOnuStateInfo(onuDevice.getDeviceName(), null);
            return null;
        });
    }

    private boolean isInPermittedAttachmentPoint(Device onuDevice, String oltDeviceName, ONUNotification onuNotification) {
        AttachmentPoint expectedAttachmentPoint = onuDevice.getDeviceManagement().getOnuConfigInfo().getAttachmentPoint();
        boolean attachmentPointMatched = true;
        if (expectedAttachmentPoint.getOltName() != null && expectedAttachmentPoint.getChannelPartition() != null
                && expectedAttachmentPoint.getOnuId() != null) {
            String actualChannelPartition = getChannelPartition(oltDeviceName, onuNotification.getChannelTermRef());
            if (expectedAttachmentPoint.getOltName().equals(oltDeviceName)
                    && expectedAttachmentPoint.getOnuId().equals(onuNotification.getOnuId())
                    && expectedAttachmentPoint.getChannelPartition().equals(actualChannelPartition)) {
                attachmentPointMatched = true;
            } else {
                attachmentPointMatched = false;
            }
        }
        return attachmentPointMatched;
    }

    private boolean isInPermittedAttachmentPoint(Device onuDevice, UnknownONU unknownONU) {
        AttachmentPoint expectedAttachmentPoint = onuDevice.getDeviceManagement().getOnuConfigInfo().getAttachmentPoint();
        boolean attachmentPointMatched = true;
        if (expectedAttachmentPoint.getOltName() != null && expectedAttachmentPoint.getChannelPartition() != null
                && expectedAttachmentPoint.getOnuId() != null) {
            String actualChannelPartition = getChannelPartition(unknownONU.getOltDeviceName(), unknownONU.getChannelTermRef());
            if (expectedAttachmentPoint.getOltName().equals(unknownONU.getOltDeviceName())
                    && expectedAttachmentPoint.getOnuId().equals(unknownONU.getOnuId())
                    && expectedAttachmentPoint.getChannelPartition().equals(actualChannelPartition)) {
                attachmentPointMatched = true;
            } else {
                attachmentPointMatched = false;
            }
        }
        return attachmentPointMatched;
    }

    private String getChannelPartition(String oltDeviceName, String channelTermRef) {
        String channelPartition = "";
        GetConfigRequest getConfig = new GetConfigRequest();
        getConfig.setSourceRunning();
        getConfig.setMessageId("internal");
        try {
            Map<NetConfResponse, List<Notification>> response = m_pmaRegistry.executeNC(oltDeviceName, getConfig.requestToString());
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

    private void persistUnknownOnuToDB(String oltDeviceName, ONUNotification onuNotification) {
        if (onuNotification != null) {
            UnknownONU onuEntity = buildUnknownOnu(oltDeviceName, onuNotification);
            LOGGER.info("Persisting unknown onu to DB: " + onuEntity);
            m_txService.executeWithTxRequiresNew((TxTemplate<Void>) () -> {
                UnknownONU matchedOnu = m_unknownOnuHandler.findUnknownOnuEntity(onuEntity.getSerialNumber(),
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
                    m_unknownOnuHandler.createUnknownONUEntity(onuEntity);
                }
                return null;
            });
        }
    }

    private UnknownONU buildUnknownOnu(String oltDeviceName, ONUNotification onuNotification) {
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

    private Timestamp convertStringToTimestamp(String timestampString) {
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

    @Override
    public void deviceAdded(String deviceName) {
        m_processNotificationRequestPool.execute(() -> {
            Device device = m_deviceManager.getDevice(deviceName);
            if (device != null && device.getDeviceManagement().getDeviceType().equals(ONUConstants.ONU_DEVICE_TYPE)
                    && device.isMediatedSession()) {
                // TO DO :: ONU Authentication
                String serialNumber = device.getDeviceManagement().getOnuConfigInfo().getSerialNumber();
                m_txService.executeWithTxRequiresNew((TxTemplate<Void>) () -> {
                    UnknownONU unknownONU = m_unknownOnuHandler.findUnknownOnuEntity(serialNumber, null);
                    if (unknownONU != null) {
                        if (isInPermittedAttachmentPoint(device, unknownONU)) {
                            LOGGER.info("Found unknown onu with matching identification: " + unknownONU);
                            HashMap<String, String> labels = getLabels(device);
                            sendDetectForPreconfiguredDevice(deviceName, unknownONU, labels);
                            updateOnuStateInfoInDevice(device, unknownONU);
                            m_unknownOnuHandler.deleteUnknownOnuEntity(unknownONU);
                        }
                    }
                    return null;
                });
            } else {
                LOGGER.debug("Device added is not of type ONU managed by vOMCI");
            }
        });
    }

    protected void waitForNotificationTasks() {
        try {
            m_processNotificationRequestPool.shutdown();
            m_processNotificationRequestPool.awaitTermination(2, java.util.concurrent.TimeUnit.MINUTES);
        } catch (java.lang.InterruptedException e) {
            LOGGER.debug("Waiting process got interrupted");
        }
    }

    private void updateOnuStateInfoInDevice(Device device, UnknownONU unknownONU) {
        ActualAttachmentPoint actualAttachmentPoint = new ActualAttachmentPoint();
        actualAttachmentPoint.setChannelTerminationRef(unknownONU.getChannelTermRef());
        actualAttachmentPoint.setOltName(unknownONU.getOltDeviceName());
        actualAttachmentPoint.setOnuId(unknownONU.getOnuId());
        OnuStateInfo onuStateInfo = new OnuStateInfo();
        onuStateInfo.setOnuState(unknownONU.getOnuState());
        onuStateInfo.setActualAttachmentPoint(actualAttachmentPoint);
        onuStateInfo.setOnuStateInfoId(device.getDeviceManagement().getDeviceState().getDeviceNodeId());
        actualAttachmentPoint.setAttachmentPointId(onuStateInfo.getOnuStateInfoId());
        m_deviceManager.updateOnuStateInfo(device.getDeviceName(), onuStateInfo);
    }

    private HashMap<String, String> getLabels(Device device) {
        HashMap<String, String> labels = new HashMap<>();
        labels.put("name", "vendor");
        labels.put("value", device.getDeviceManagement().getDeviceVendor());
        return labels;
    }

    private void sendDetectForPreconfiguredDevice(String deviceName, UnknownONU unknownONU, HashMap<String, String> labels) {
        NotificationRequest request = new NotificationRequest(deviceName, unknownONU.getOltDeviceName(), unknownONU.getChannelTermRef(),
                unknownONU.getOnuId(), ONUConstants.DETECT_EVENT, labels);
        sendKafkaNotification(request);
    }

    private void sendDetectForPreconfiguredDevice(String deviceName, ONUNotification onuNotification, HashMap<String, String> labels) {
        NotificationRequest request = new NotificationRequest(deviceName, onuNotification.getOltDeviceName(),
                onuNotification.getChannelTermRef(), onuNotification.getOnuId(), ONUConstants.DETECT_EVENT, labels);
        sendKafkaNotification(request);
    }

    private void sendKafkaNotification(NotificationRequest request) {
        String messageId = generateRandomMessageId();
        request.setMessageId(messageId);
        String jsonNotificationString = request.getJsonNotification();
        String kafkaTopicName = ONUConstants.ONU_NOTIFICATION_KAFKA_TOPIC;
        try {
            m_kafkaProducer.sendNotification(kafkaTopicName, jsonNotificationString);
        } catch (Exception exception) {
            LOGGER.error(String.format("Failed to send %s notification onto Kafka topic %s ",
                    jsonNotificationString, kafkaTopicName), exception);
        }
    }

    private String generateRandomMessageId() {
        int randomNumber = (int)(Math.random() * (ONUConstants.MESSAGE_ID_MAX - ONUConstants.MESSAGE_ID_MIN + 1)
                + ONUConstants.MESSAGE_ID_MIN);
        return String.valueOf(randomNumber);
    }

    @Override
    public void deviceRemoved(String deviceName) {
        m_processNotificationRequestPool.execute(() -> {
            try {
                Device device = m_deviceManager.getDevice(deviceName);
                if (device != null) {
                    if (device.getDeviceManagement().getDeviceType().equals(ONUConstants.ONU_DEVICE_TYPE)) {
                        onuDeviceRemoved(device);
                    } else if (device.getDeviceManagement().getDeviceType().equals(ONUConstants.OLT_DEVICE_TYPE)) {
                        oltDeviceRemoved(device);
                    }
                }
            } catch (IllegalArgumentException e) {
                LOGGER.info("Device " + deviceName + "is already removed ");
            }
        });
    }

    private void oltDeviceRemoved(Device oltDevice) {
        //TO DO
    }

    private void onuDeviceRemoved(Device onuDevice) {
        if (onuDevice != null) {
            handleUndetect(onuDevice, null);
        } else {
            LOGGER.warn(String.format("Device %s to be deleted, does not exist", onuDevice.getDeviceName()));
        }
    }

    private void handleUndetect(Device onuDevice, ONUNotification notification) {
        MediatedDeviceNetconfSession deviceSession = getMediatedDeviceNetconfSession(onuDevice);
        if (deviceSession != null) {
            deviceSession.closeAsync();
        } else if (notification != null) {
            sendKafkaUndetectMessage(onuDevice, notification);
        }
    }

    private void sendKafkaUndetectMessage(Device onuDevice, ONUNotification notification) {
        HashMap<String, String> labels = getLabels(onuDevice);
        NotificationRequest request = new NotificationRequest(onuDevice.getDeviceName(), notification.getOltDeviceName(),
                notification.getChannelTermRef(), notification.getOnuId(), ONUConstants.UNDETECT_EVENT, labels);
        sendKafkaNotification(request);
    }

    @Override
    public void processResponse(JSONObject jsonResponse) {
        if (jsonResponse.getString(ONUConstants.EVENT).equals(ONUConstants.RESPONSE_EVENT)) {
            String onuName = jsonResponse.getString(ONUConstants.ONU_NAME_JSON_KEY);
            JSONObject payloadJson = new JSONObject(jsonResponse.getString(ONUConstants.PAYLOAD_JSON_KEY));
            String identifier = payloadJson.getString(ONUConstants.IDENTIFIER_JSON_KEY);
            String operationType = payloadJson.getString(ONUConstants.OPERATION_JSON_KEY);
            String responseStatus = payloadJson.getString(ONUConstants.STATUS_JSON_KEY);
            String failureReason = payloadJson.optString(ONUConstants.FAILURE_REASON);
            String data = payloadJson.optString(ONUConstants.DATA_JSON_KEY);
            long transactionId = payloadJson.optLong(ONUConstants.TRANSACTION_ID, 0);
            if (operationType.equals(ONUConstants.ONU_DETECTED) || operationType.equals(ONUConstants.ONU_UNDETECTED)) {
                /** Since this is notification response, spawn new thread out of processNotificationResponseTask pool.
                 * Therefore, do not block this main Kafka thread nor any other process request response threads.
                 * All DETECTs/UNDETECTs responses must be processed under processNotificationResponseTask pool.
                 * Since the main Kafka thread is freed immediately, and all other process request response threads are
                 * initiated under processNotificationResponse pool, it eliminates any possible cases of the deadlock and it
                 * improves efficiency and response time of processing other responses.
                 */
                m_processNotificationResponsePool.execute(() -> {
                    processNotificationResponse(onuName, operationType,
                        identifier, responseStatus, failureReason, data, transactionId);
                });
            } else {
                /**
                 * Since this is request response, spawn new thread out of processRequestResponseTask pool.
                 * Also, do not block this main Kafka thread nor any other process notification response threads.
                 * All request responses GETs/COPY-CONFIGs/EDIT-CONFIGs must be processes under processRequestResponseTask pool.
                 * Since it immediately frees the main Kafka thread, it would not get blocked by bootstrap mechanism in case
                 * of DETECT response with OK status.
                 */
                m_processRequestResponsePool.execute(() -> {
                    processRequestResponse(onuName, operationType, identifier,
                        responseStatus, failureReason, data);
                });
            }
        } else {
            LOGGER.warn(String.format("Non response event received on %s kafka topic. Event received is %s",
                    ONUConstants.ONU_RESPONSE_KAFKA_TOPIC, jsonResponse.getString(ONUConstants.EVENT)));
        }

    }

    protected void processNotificationResponse(String onuDeviceName, String operationType, String identifier,
                                               String responseStatus, String failureReason, String data, long transactionId) {
        LOGGER.info(String.format("%s notification response is received for ONU device %s:\nIdentifier (message-id): %s\n"
                        + "Response Status: %s\n" + (responseStatus.equals(ONUConstants.NOK_RESPONSE) ? "Failure Reason: "
                        + failureReason + "\n" : "") + "Retrieved Data in JSON Format: None\nDB Version: %s",
                operationType, onuDeviceName, identifier, responseStatus, transactionId));
        // Find ONU Device
        String state = ONUConstants.OFFLINE;
        Device onuDevice = null;
        try {
            onuDevice = m_txService.executeWithTxRequired(() -> m_deviceManager.getDevice(onuDeviceName));
        } catch (IllegalArgumentException e) {
            LOGGER.debug(String.format("ONU device %s not found for the notification response received", onuDeviceName));
        }
        if (onuDevice != null) {
            MediatedDeviceNetconfSession mediatedSession = getMediatedDeviceNetconfSession(onuDevice);
            if (responseStatus.equals(ONUConstants.OK_RESPONSE) && operationType.equals(ONUConstants.ONU_UNDETECTED)) {
                if (mediatedSession == null) {
                    LOGGER.debug(String.format("Mediated Device Netconf Session has been already closed "
                            + "upon UNDETECT notification request with id %s for device %s", identifier, onuDeviceName));
                } else {
                    LOGGER.error(String.format("Mediated Device Netconf Session must have been already "
                            + "closed upon UNDETECT notification request for device:%s and should not be open upon receiving and "
                            + "processing UNDETECT notification response", onuDeviceName));
                    LOGGER.debug(String.format("Attempt to close Mediated Device Netconf Session of device %s due to "
                            + "UNDETECT notification response with id %s",onuDeviceName, identifier));
                    try {
                        mediatedSession.close();
                    } catch (Exception e) {
                        LOGGER.error("Error while closing Mediated device netconf session for device " + onuDeviceName);
                    }
                }
                m_deviceManager.updateConfigAlignmentState(onuDeviceName, DeviceManagerNSConstants.NEVER_ALIGNED);
            } else {
                // This is DETECT response
                if (responseStatus.equals(ONUConstants.NOK_RESPONSE)) {
                    if (mediatedSession != null) {
                        LOGGER.debug(String.format("Close Mediated Device Netconf Session due to NOK DETECT "
                                        + "response notification for device %s with id %s and failure reason: %s",
                                onuDeviceName, identifier, failureReason));
                        try {
                            mediatedSession.close();
                        } catch (Exception e) {
                            LOGGER.error("Error while closing Mediated device netconf session for device " + onuDeviceName);
                        }
                    }
                } else {
                    // Create new Mediated Device Netconf session if session does not exist and (or if session exists) trigger COPY-CONFIG
                    if (mediatedSession == null) {
                        LOGGER.debug(String.format("Create new Mediated Device Netconf Session for device %s "
                                + "upon processing DETECT OK response notification with id %s", onuDeviceName, identifier));
                        ActualAttachmentPoint actualAttachmentPoint = onuDevice.getDeviceManagement().getDeviceState()
                                .getOnuStateInfo().getActualAttachmentPoint();
                        String oltDeviceName = actualAttachmentPoint.getOltName();
                        String onuId = actualAttachmentPoint.getOnuId();
                        String channelTermRef = actualAttachmentPoint.getChannelTerminationRef();
                        HashMap<String, String> labels = getLabels(onuDevice);
                        MediatedDeviceNetconfSession newMediatedSession = new MediatedDeviceNetconfSession(onuDevice,
                            oltDeviceName, channelTermRef, onuId, labels, transactionId, m_kafkaProducer,
                            m_modelNodeDSM, m_adapterManager, m_kafkaCommunicationPool);
                        m_connectionManager.addMediatedDeviceNetconfSession(onuDevice, newMediatedSession);
                    } else {
                        LOGGER.debug(String.format("Mediated Device Netconf Session already exists with the device %s "
                                + "while processing DETECT OK response notification with id %s", onuDeviceName, identifier));
                        mediatedSession.open();
                    }
                    //TO DO:: Send Get ME operation for software and hardware properties of the ONU

                    //Send notification to BAA NBI
                    state = ONUConstants.ONLINE;
                    sendOnuStateChangeNotification(onuDevice, state);
                }
            }
        } else {
            LOGGER.warn(String.format("ONU Device is not found based on its name: %s "
                    + "Processing %s response notification with id %s has failed", onuDeviceName, operationType, identifier));
        }
    }

    private void sendOnuStateChangeNotification(Device onuDevice, String onuState) {
        try {
            Notification notification = new OnuStateChangeNotification(onuDevice, onuState);
            m_notificationService.sendNotification(NetconfResources.STATE_CHANGE, notification);
        } catch (NetconfMessageBuilderException e) {
            LOGGER.error(String.format("Error while sending onu state change notification for device %s with onu state %s",
                    onuDevice.getDeviceName(), onuState), e);
        }
    }

    private MediatedDeviceNetconfSession getMediatedDeviceNetconfSession(Device onuDevice) {
        if (onuDevice == null) {
            return null;
        }
        NetconfClientSession devSession = m_connectionManager.getMediatedDeviceSession(onuDevice);
        if (devSession != null) {
            return (MediatedDeviceNetconfSession) devSession;
        }
        return null;
    }

    protected void processRequestResponse(String onuDeviceName, String operationType, String identifier,
                                          String responseStatus, String failureReason, String data) {
        LOGGER.info(String.format("%s response is received for ONU device %s:\nIdentifier (message-id): %s\nResponse Status: %s\n"
                        + (responseStatus.equals(ONUConstants.NOK_RESPONSE) ? "Failure Reason: " + failureReason + "\n" : "") + " ",
                operationType, onuDeviceName, identifier, responseStatus));
        Device onuDevice = null;
        try {
            onuDevice = m_txService.executeWithTxRequired(() -> m_deviceManager.getDevice(onuDeviceName));
        } catch (IllegalArgumentException e) {
            LOGGER.debug(String.format("ONU device %s not found for the notification response received", onuDeviceName));
        }
        if (onuDevice != null) {
            MediatedDeviceNetconfSession mediatedSession = getMediatedDeviceNetconfSession(onuDevice);
            if (mediatedSession == null) {
                LOGGER.error(String.format("Unable to find Mediated Device Netconf Session while processing %s "
                                + "response for %s device:\nIdentifier (message-id): %s\nResponse Status: %s\n"
                                + (responseStatus.equals(ONUConstants.NOK_RESPONSE) ? "Failure Reason: " + failureReason
                                + "\n" : "") + "Retrieved Data in JSON Format: %s",
                        operationType, onuDeviceName, identifier, responseStatus, data));
                return;
            }
            NetConfResponse response;
            switch (operationType) {
                case ONUConstants.ONU_GET_OPERATION:
                    response = mediatedSession.processGetResponse(identifier, responseStatus, data, failureReason);
                    LOGGER.debug("Processing " + operationType + " response with id "
                            + identifier + " is " + (response == null ? "failed" : "successful"));
                    break;
                case ONUConstants.ONU_COPY_OPERATION:
                case ONUConstants.ONU_EDIT_OPERATION:
                    response = mediatedSession.processResponse(identifier, operationType, responseStatus, failureReason);
                    LOGGER.debug("Processing " + operationType + " response with id "
                            + identifier + " is " + (response == null ? "failed" : "successful"));
                    break;
                default:
                    LOGGER.warn(String.format("Unknown response for device %s of type %s from vOMCI. Unable to process it", operationType));
            }
        } else {
            LOGGER.warn(String.format("ONU Device is not found based on its name: %s "
                    + "Processing %s response with id %s has failed", onuDeviceName, operationType, identifier));
        }
    }
}