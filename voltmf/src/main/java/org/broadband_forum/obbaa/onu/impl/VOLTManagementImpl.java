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

import static org.broadband_forum.obbaa.onu.ONUConstants.EOMCI_USED_BY_OLT_CONFLICTING_WITH_PMAA_MGMT_FOR_OLT;
import static org.broadband_forum.obbaa.onu.ONUConstants.IETF_ALARM_NS;
import static org.broadband_forum.obbaa.onu.ONUConstants.ONU_AUTHENTICATED_AND_MGT_MODE_DETERMINED;
import static org.broadband_forum.obbaa.onu.ONUConstants.UNABLE_TO_AUTHENTICATE_ONU;
import static org.broadband_forum.obbaa.onu.ONUConstants.VOMCI_EXPECTED_BY_OLT_BUT_INCONSISTENT_PMAA_MGMT_MODE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.device.alarm.util.AlarmNotificationUtil;
import org.broadband_forum.obbaa.dmyang.dao.DeviceDao;
import org.broadband_forum.obbaa.dmyang.entities.ActualAttachmentPoint;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants;
import org.broadband_forum.obbaa.dmyang.entities.SoftwareImage;
import org.broadband_forum.obbaa.nbiadapter.netconf.NbiNetconfServerMessageListener;
import org.broadband_forum.obbaa.netconf.alarm.api.AlarmInfo;
import org.broadband_forum.obbaa.netconf.alarm.api.AlarmService;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.TxService;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.TxTemplate;
import org.broadband_forum.obbaa.nf.dao.NetworkFunctionDao;
import org.broadband_forum.obbaa.nf.entities.NetworkFunction;
import org.broadband_forum.obbaa.nm.devicemanager.DeviceManager;
import org.broadband_forum.obbaa.onu.MediatedDeviceNetconfSession;
import org.broadband_forum.obbaa.onu.MediatedNetworkFunctionNetconfSession;
import org.broadband_forum.obbaa.onu.NotificationRequest;
import org.broadband_forum.obbaa.onu.ONUConstants;
import org.broadband_forum.obbaa.onu.UnknownONUHandler;
import org.broadband_forum.obbaa.onu.VOLTManagement;
import org.broadband_forum.obbaa.onu.entity.UnknownONU;
import org.broadband_forum.obbaa.onu.exception.MessageFormatterException;
import org.broadband_forum.obbaa.onu.kafka.consumer.OnuKafkaConsumer;
import org.broadband_forum.obbaa.onu.kafka.producer.OnuKafkaProducer;
import org.broadband_forum.obbaa.onu.message.GpbFormatter;
import org.broadband_forum.obbaa.onu.message.HelloResponseData;
import org.broadband_forum.obbaa.onu.message.JsonFormatter;
import org.broadband_forum.obbaa.onu.message.MessageFormatter;
import org.broadband_forum.obbaa.onu.message.NetworkWideTag;
import org.broadband_forum.obbaa.onu.message.ObjectType;
import org.broadband_forum.obbaa.onu.message.ResponseData;
import org.broadband_forum.obbaa.onu.notification.ONUNotification;
import org.broadband_forum.obbaa.onu.util.VOLTManagementUtil;
import org.broadband_forum.obbaa.onu.util.VOLTMgmtRequestCreationUtil;
import org.broadband_forum.obbaa.pma.PmaRegistry;
import org.w3c.dom.Element;

/**
 * <p>
 * Manages vOMCI based ONUs such as CRUD operations on ONUs and notification handling
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 22/06/2020.
 */
public class VOLTManagementImpl implements VOLTManagement {
    private static final Logger LOGGER = Logger.getLogger(VOLTManagementImpl.class);
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
    private final SchemaRegistry m_schemaRegistry;
    private ThreadPoolExecutor m_processNotificationResponsePool;
    private ThreadPoolExecutor m_processRequestResponsePool;
    private ThreadPoolExecutor m_processNotificationRequestPool;
    private ThreadPoolExecutor m_kafkaCommunicationPool;
    private final MessageFormatter m_messageFormatter;
    private final NetworkFunctionDao m_networkFunctionDao;
    private final DeviceDao m_deviceDao;
    private OnuKafkaConsumer m_onuKafkaconsumer;
    private Map<String, Set<String>> m_kafkaConsumerTopicMap;
    private Map<String, ArrayList<Boolean>> m_networkFunctionResponse;
    private Map<String, HelloResponseData> m_helloResponseMessages;
    private AtomicLong m_messageId = new AtomicLong(0);
    private Set<String> m_onuDevicesCreated;
    private NbiNetconfServerMessageListener m_nbiNetconfServerMessageListener;

    public VOLTManagementImpl(TxService txService, DeviceManager deviceManager, AlarmService alarmService,
                              OnuKafkaProducer kafkaProducer, UnknownONUHandler unknownONUHandler,
                              NetconfConnectionManager connectionManager, ModelNodeDataStoreManager modelNodeDSM,
                              NotificationService notificationService, AdapterManager adapterManager,
                              PmaRegistry pmaRegistry, SchemaRegistry schemaRegistry, MessageFormatter messageFormatter,
                              NetworkFunctionDao networkFunctionDao, DeviceDao deviceDao,
                              NbiNetconfServerMessageListener nbiNetconfServerMessageListener) {
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
        m_schemaRegistry = schemaRegistry;
        m_messageFormatter = messageFormatter;
        m_networkFunctionDao = networkFunctionDao;
        m_deviceDao = deviceDao;
        m_kafkaConsumerTopicMap = new HashMap<>();
        m_networkFunctionResponse = new HashMap<>();
        m_onuDevicesCreated = new HashSet<>();
        m_helloResponseMessages = new HashMap<>();
        m_nbiNetconfServerMessageListener = nbiNetconfServerMessageListener;
    }


    @Override
    public void setKafkaConsumer(OnuKafkaConsumer onuKafkaConsumer) {
        m_onuKafkaconsumer = onuKafkaConsumer;
    }

    @Override
    public void unsetKafkaConsumer(OnuKafkaConsumer onuKafkaConsumer) {
        m_onuKafkaconsumer = null;
    }

    @Override
    public void networkFunctionAdded(String networkFunctionName) {
        VOLTManagementUtil.updateKafkaSubscriptions(networkFunctionName, m_messageFormatter, m_networkFunctionDao,
                m_onuKafkaconsumer, m_kafkaConsumerTopicMap);
        LOGGER.info("network function added, updating the subscription");
        Object message = null;
        try {
            NetworkFunction networkFunction = m_networkFunctionDao.getNetworkFunctionByName(networkFunctionName);
            String localEndpointName = m_networkFunctionDao.getLocalEndpointName(networkFunctionName);
            if (networkFunction != null) {
                MediatedNetworkFunctionNetconfSession mediatedSession =
                        VOLTManagementUtil.getMediatedNetworkFunctionNetconfSession(networkFunction.getNetworkFunctionName(),
                                m_connectionManager);
                if (mediatedSession == null) {
                    mediatedSession = new MediatedNetworkFunctionNetconfSession(networkFunction,m_kafkaProducer,
                            m_modelNodeDSM,m_adapterManager,m_kafkaCommunicationPool,m_messageFormatter,m_txService,
                            m_networkFunctionDao, this);
                    m_connectionManager.addMediatedNetworkFunctionNetconfSession(networkFunctionName, mediatedSession);
                }

                ObjectType objectType = ObjectType.getObjectTypeFromYangString(networkFunction.getType());
                if (objectType != null) {
                    message = m_messageFormatter.getFormattedHelloRequest(String.valueOf(m_messageId.addAndGet(1)),
                            networkFunctionName,objectType,localEndpointName);
                    VOLTManagementUtil.sendKafkaMessage(message,networkFunctionName,m_txService, m_networkFunctionDao, m_kafkaProducer);
                } else {
                    LOGGER.info("The Object Type of the given Network Function yang is null.");
                }
            } else {
                LOGGER.info("The network function name provided is not a valid one. Received: " + networkFunctionName);
            }
        } catch (MessageFormatterException e) {
            LOGGER.debug(e);
        }
    }

    @Override
    public void networkFunctionRemoved(String networkFunctionName) {
        LOGGER.debug("network function removed, removing the subscription");
        VOLTManagementUtil.removeSubscriptions(networkFunctionName, m_onuKafkaconsumer, m_kafkaConsumerTopicMap);
        m_helloResponseMessages.remove(networkFunctionName);
        m_pmaRegistry.networkFunctionRemoved(networkFunctionName);
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
        // legacy OLT == true When OLT doesn't support WT.489 yang modules. (i.e OLT interface version != 2.1)
        Boolean legacyOLT = true;
        if (onuNotification.getDeterminedOnuManagementMode() != null && !onuNotification.getDeterminedOnuManagementMode().equals("")) {
            legacyOLT = false;
        }
        Boolean onuAuthenticatedByOLT = false;
        if (onuNotification != null) {
            onuAuthenticatedByOLT = VOLTManagementUtil.isOnuAuthenticatedByOLTAndHasNoErrors(onuNotification.getOnuState());
        }
        Boolean oltNotifiesVomciToBeUsed = false;
        if (onuNotification.getDeterminedOnuManagementMode() != null
                && onuNotification.getDeterminedOnuManagementMode().contains(ONUConstants.RELYING_ON_VOMCI)) {
            oltNotifiesVomciToBeUsed = true;
        }
        Device onuDevice = null;
        try {
            onuDevice = m_txService.executeWithTxRequired(() -> m_deviceManager.getDeviceWithSerialNumber(onuSerialNum));
        } catch (Exception e) {
            LOGGER.info("ONU device not found with serial number: " + onuSerialNum);
        }
        if (onuDevice == null) {
            if (onuNotification.getMappedEvent().equals(ONUConstants.ONU_DETECTED)
                    || onuNotification.getMappedEvent().equals(ONUConstants.CREATE_ONU)) {
                // If ONU authentication fails create unknown ONU and persist in DB
                VOLTManagementUtil.persistUnknownOnuToDB(oltDeviceName, onuNotification, m_txService, m_unknownOnuHandler);
                /* below commented line will be uncommented/enabled after vomci has the get request & response handling support */
                //VOLTManagementUtil.sendGetRequest(onuNotification, m_messageFormatter, m_txService, m_deviceDao, m_networkFunctionDao,
                //        m_kafkaProducer);
                if (!legacyOLT) {
                    if (onuAuthenticatedByOLT) {
                        if (oltNotifiesVomciToBeUsed) {
                            LOGGER.info(String.format("vomci expected by olt but device not found in pmaa. Send  Onu Authentication Report "
                                            + "Notification with Authentication status: %s",
                                    ONUConstants.VOMCI_EXPECTED_BY_OLT_BUT_MISSING_ONU_CONFIG));
                            VOLTManagementUtil.sendOnuAuthenticationResultNotification(onuDevice, onuNotification,
                                    m_connectionManager, m_notificationService, ONUConstants.VOMCI_EXPECTED_BY_OLT_BUT_MISSING_ONU_CONFIG,
                                    m_deviceManager, m_txService);
                        } else {
                            LOGGER.info("OLT want the ONU to be managed by eOMCI and PMAA does not have the ONU configrued,"
                                    + " ONU to be manage by OLT in eOMCI mode");
                        }
                    } else { // OBBAA-459
                        LOGGER.info(String.format("OLT: unable to authenticate ONU. send  Onu Authentication Report Notification "
                                + " with Authentication status: %s", ONUConstants.UNABLE_TO_AUTHENTICATE_ONU));
                        VOLTManagementUtil.sendOnuAuthenticationResultNotification(onuDevice, onuNotification,
                                m_connectionManager, m_notificationService, ONUConstants.UNABLE_TO_AUTHENTICATE_ONU, m_deviceManager,
                                m_txService);
                        LOGGER.info(String.format("ONU notification request %s is received for ONU device, but unable to find"
                                + " pre-configured device based on the serial number: %s", onuNotification.getOnuState(), onuSerialNum));
                    }
                }
            } else {
                UnknownONU onuEntity = VOLTManagementUtil.buildUnknownOnu(oltDeviceName, onuNotification);
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
            if (VOLTManagementUtil.isInPermittedAttachmentPoint(onuDevice, oltDeviceName, onuNotification, m_pmaRegistry)) {
                if (ONUConstants.ONU_DETECTED.equals(onuNotification.getMappedEvent())
                        || ONUConstants.CREATE_ONU.equals(onuNotification.getMappedEvent())) {
                    if (!onuDevice.isAligned()) {
                        Device finalOnuDevice = onuDevice;
                        m_txService.executeWithTxRequiresNew((TxTemplate<Void>) () -> {
                            UnknownONU unknownOnuEntity = VOLTManagementUtil.buildUnknownOnu(oltDeviceName, onuNotification);
                            LOGGER.info(String.format("update onu state info in device %s", finalOnuDevice.getDeviceName()));
                            VOLTManagementUtil.updateOnuStateInfoInDevice(finalOnuDevice, unknownOnuEntity, m_deviceManager,
                                    m_txService);
                            return null;
                        });
                        if (legacyOLT) {
                            sendSetOnuCommunication(onuDevice.getDeviceName(), true, onuNotification.getOltDeviceName(),
                                    onuNotification.getChannelTermRef(), onuNotification.getOnuId());
                        } else {
                            Boolean finalOltNotifiesVomciToBeUsed = oltNotifiesVomciToBeUsed;
                            Boolean finalOnuAuthenticatedByOLT = onuAuthenticatedByOLT;
                            Runnable runnable = new Runnable() {
                                public void run() {
                                    evaluateOnuStatusAndSendOnuAuthReportNotification(onuNotification, finalOnuAuthenticatedByOLT,
                                            finalOltNotifiesVomciToBeUsed, finalOnuDevice.getDeviceName());
                                    return;

                                }
                            };
                            Thread thread = new Thread(runnable);
                            thread.start();
                            try {
                                thread.join();
                            } catch (InterruptedException e) {
                                LOGGER.error("Error while joining thread");
                            }
                        }
                    }
                } else if (ONUConstants.ONU_UNDETECTED.equals(onuNotification.getMappedEvent())
                        || ONUConstants.DELETE_ONU.equals(onuNotification.getMappedEvent())) {
                    VOLTManagementUtil.removeActualAttachmentPointInfo(onuDevice, m_txService, m_deviceManager);
                    handleUndetect(onuDevice, onuNotification);
                }
            } else {
                // Notification received for the ONU from a different attachment point, so persist/remove in DB
                if (ONUConstants.ONU_DETECTED.equals(onuNotification.getMappedEvent())
                        || ONUConstants.CREATE_ONU.equals(onuNotification.getMappedEvent())) {
                    VOLTManagementUtil.persistUnknownOnuToDB(oltDeviceName, onuNotification, m_txService, m_unknownOnuHandler);
                    /* below commented line will be uncommented/enabled after vomci has the get request & response handling support */
                    //VOLTManagementUtil.sendGetRequest(onuNotification, m_messageFormatter, m_txService, m_deviceDao, m_networkFunctionDao,
                    //        m_kafkaProducer);
                } else if (ONUConstants.ONU_UNDETECTED.equals(onuNotification.getMappedEvent())
                        || ONUConstants.DELETE_ONU.equals(onuNotification.getMappedEvent())) {
                    UnknownONU onuEntity = VOLTManagementUtil.buildUnknownOnu(oltDeviceName, onuNotification);
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

    private void evaluateOnuStatusAndSendOnuAuthReportNotification(ONUNotification onuNotification, Boolean onuAuthenticatedByOLT,
                                                                   Boolean oltNotifiesVomciToBeUsed, String deviceName) {
        Device onuDevice = m_txService.executeWithTxRequired(() -> m_deviceManager.getDevice(deviceName));
        if (onuDevice != null) {
            if (!onuAuthenticatedByOLT) {
                String requestedMgmtMode = null;
                if (VOLTManagementUtil.isVomciToBeUsed(onuDevice)) {
                    requestedMgmtMode = ONUConstants.USE_VOMCI;
                } else {
                    requestedMgmtMode = ONUConstants.USE_EOMCI;
                }
                String oltDeviceName = onuDevice.getDeviceManagement().getDeviceState().getOnuStateInfo()
                        .getActualAttachmentPoint().getOltName();
                String vaniName = onuDevice.getDeviceManagement().getDeviceState().getOnuStateInfo()
                        .getActualAttachmentPoint().getvAniName();
                if (vaniName == null) {
                    vaniName = onuNotification.getVAniRef();
                }
                processActionRequestResponse(oltDeviceName, onuDevice, requestedMgmtMode, vaniName, onuNotification);
            }
            if (onuAuthenticatedByOLT && oltNotifiesVomciToBeUsed && !VOLTManagementUtil.isVomciToBeUsed(onuDevice)) {
                LOGGER.info(String.format("Expected and Actual ONU Management mode does not match, Send Onu Authentication Report "
                                + "Notification with Authentication status: %s",
                        ONUConstants.VOMCI_EXPECTED_BY_OLT_BUT_INCONSISTENT_PMAA_MGMT_MODE));
                VOLTManagementUtil.sendOnuAuthenticationResultNotification(onuDevice, null, m_connectionManager,
                        m_notificationService, ONUConstants.VOMCI_EXPECTED_BY_OLT_BUT_INCONSISTENT_PMAA_MGMT_MODE, m_deviceManager,
                        m_txService);
            } else if (onuAuthenticatedByOLT && oltNotifiesVomciToBeUsed && VOLTManagementUtil.isVomciToBeUsed(onuDevice)) {
                HashMap<String, String> labels = VOLTMgmtRequestCreationUtil.getLabels(onuDevice);
                if (m_messageFormatter instanceof JsonFormatter) {
                    NotificationRequest request = VOLTMgmtRequestCreationUtil.prepareDetectForPreconfiguredDevice(
                            onuDevice.getDeviceName(), onuNotification, labels);
                    VOLTManagementUtil.sendKafkaNotification(request, m_messageFormatter, m_kafkaProducer);
                } else {
                    LOGGER.info("Set ONU comunication triggered");
                    sendSetOnuCommunication(onuDevice.getDeviceName(), true, onuNotification.getOltDeviceName(),
                            onuNotification.getChannelTermRef(), onuNotification.getOnuId());

                }
            } else if (onuAuthenticatedByOLT && !oltNotifiesVomciToBeUsed && VOLTManagementUtil.isVomciToBeUsed(onuDevice)) {
                LOGGER.info(String.format("Expected and Actual ONU Management mode does not match, Send Onu Authentication Report "
                                + "Notification with Authentication status: %s",
                        ONUConstants.EOMCI_USED_BY_OLT_CONFLICTING_WITH_PMAA_MGMT_FOR_OLT));
                VOLTManagementUtil.sendOnuAuthenticationResultNotification(onuDevice, null, m_connectionManager,
                        m_notificationService, ONUConstants.EOMCI_USED_BY_OLT_CONFLICTING_WITH_PMAA_MGMT_FOR_OLT, m_deviceManager,
                        m_txService);
            } else if (onuAuthenticatedByOLT && !oltNotifiesVomciToBeUsed && !VOLTManagementUtil.isVomciToBeUsed(onuDevice)) {
                LOGGER.info(String.format("OLT & PMAA wants the ONU %s to be managed by eOMCI", onuDevice.getDeviceName()));
            }
        }
    }

    private void validateResponseAndSendOnuAuthReportNotification(String response, String requestedMgmtMode, Device onuDevice,
                                                                  ONUNotification onuNotification) {
        if (response != null) {
            String onuAuthStatus = null;
            if (response.equals(ONUConstants.OK_RESPONSE)) {
                if (requestedMgmtMode.contains(ONUConstants.USE_VOMCI)) {
                    onuAuthStatus = ONU_AUTHENTICATED_AND_MGT_MODE_DETERMINED;
                    sendSetOnuCommunication(onuDevice.getDeviceName(), true, onuNotification.getOltDeviceName(),
                            onuNotification.getChannelTermRef(), onuNotification.getOnuId());
                } else if (requestedMgmtMode.contains(ONUConstants.USE_EOMCI)) {
                    onuAuthStatus = ONU_AUTHENTICATED_AND_MGT_MODE_DETERMINED;
                    LOGGER.info(String.format("OLT & PMAA wants the ONU %s to be managed by eOMCI", onuDevice.getDeviceName()));
                }
            } else if (response.contains(ONUConstants.ONU_NOT_PRESENT)) {
                onuAuthStatus = UNABLE_TO_AUTHENTICATE_ONU;
            } else if (response.contains(ONUConstants.ONU_MGMT_MODE_MISMATCH_WITH_VANI)) {
                if (requestedMgmtMode.equals(ONUConstants.USE_VOMCI)) {
                    onuAuthStatus = EOMCI_USED_BY_OLT_CONFLICTING_WITH_PMAA_MGMT_FOR_OLT;
                } else if (requestedMgmtMode.equals(ONUConstants.USE_EOMCI)) {
                    onuAuthStatus = VOMCI_EXPECTED_BY_OLT_BUT_INCONSISTENT_PMAA_MGMT_MODE;
                }
            } else if (response.contains(ONUConstants.BAA_XPON_ONU_NAME_MISMATCH_WITH_VANI)) {
                onuAuthStatus = UNABLE_TO_AUTHENTICATE_ONU;
            } else if (response.contains(ONUConstants.UNDETERMINED_ERROR)) {
                onuAuthStatus = UNABLE_TO_AUTHENTICATE_ONU;
            }

            if (onuAuthStatus != null && !onuAuthStatus.equals(ONU_AUTHENTICATED_AND_MGT_MODE_DETERMINED)) {
                VOLTManagementUtil.sendOnuAuthenticationResultNotification(onuDevice, onuNotification, m_connectionManager,
                        m_notificationService, onuAuthStatus, m_deviceManager, m_txService);
            }
        }
    }

    private void processActionRequestResponse(String oltNAme, Device onuDevice, String requestedMgmtMode,
                                                String vaniName, ONUNotification onuNotification) {
        String channelTerminationName = onuDevice.getDeviceManagement().getDeviceState().getOnuStateInfo()
                .getActualAttachmentPoint().getChannelTerminationRef();
        ActionRequest actionRequest = VOLTMgmtRequestCreationUtil.prepareOnuAuthenicationReportActionRequest(onuDevice.getDeviceName(),
                false, vaniName, requestedMgmtMode, onuDevice.getDeviceManagement().getOnuConfigInfo().getExpectedSerialNumber(),
                channelTerminationName);
        VOLTManagementUtil.setMessageId(actionRequest, m_messageId);
        final NetConfResponse[] response = {null};
        final String[] actionResponse = {null};
        Device oltDevice = m_txService.executeWithTxRequired(() -> m_deviceManager.getDevice(oltNAme));
        try {
            LOGGER.info(String.format("Sending action request to OLT: %s", actionRequest.requestToString()));
            if (oltDevice != null) {
                Future<NetConfResponse> netConfResponseFuture = VOLTManagementUtil.sendOnuAuthenticationActionRequest(oltDevice,
                        m_connectionManager, actionRequest);
                LOGGER.info(String.format("Netconf response future: %s", netConfResponseFuture.toString()));
                Runnable runnable = new Runnable() {
                    public void run() {
                        try {
                            response[0] = netConfResponseFuture.get();
                            LOGGER.info(String.format("response is: %s", response[0].responseToString()));
                            if (response[0] != null) {
                                if (!response[0].isOk()) {
                                    actionResponse[0] = VOLTManagementUtil.getErrorAppTagFromActionResponse(response[0]);
                                } else {
                                    actionResponse[0] = ONUConstants.OK_RESPONSE;
                                }
                            }
                            validateResponseAndSendOnuAuthReportNotification(actionResponse[0], requestedMgmtMode, onuDevice,
                                    onuNotification);
                        } catch (InterruptedException | ExecutionException e) {
                            LOGGER.error("failed to get response from netconf future: ", e);
                        }
                        return;
                    }
                };
                Thread thread = new Thread(runnable);
                thread.start();
            }
        } catch (InterruptedException e) {
            LOGGER.error("Error while executing action request");
        }
    }

    @Override
    public void deviceAdded(String deviceName) {
        m_processNotificationRequestPool.execute(() -> {
            Device device = m_txService.executeWithTxRequired(() -> m_deviceManager.getDevice(deviceName));
            if (device != null && device.getDeviceManagement().getDeviceType().equals(ONUConstants.ONU_DEVICE_TYPE)
                    && device.isMediatedSession()) {
                // TO DO :: ONU Authentication
                String serialNumber = device.getDeviceManagement().getOnuConfigInfo().getExpectedSerialNumber();
                if (m_messageFormatter instanceof GpbFormatter) {
                    sendCreateOnu(deviceName);
                    m_onuDevicesCreated.add(deviceName);
                }
                m_txService.executeWithTxRequiresNew((TxTemplate<Void>) () -> {
                    UnknownONU unknownONU = m_unknownOnuHandler.findUnknownOnuEntity(serialNumber, null);
                    if (unknownONU != null) {
                        if (VOLTManagementUtil.isInPermittedAttachmentPoint(device, unknownONU, m_pmaRegistry)) {
                            LOGGER.info("Found unknown onu with matching identification: " + unknownONU);
                            HashMap<String, String> labels = VOLTMgmtRequestCreationUtil.getLabels(device);
                            if (m_messageFormatter instanceof JsonFormatter) {
                                NotificationRequest request = VOLTMgmtRequestCreationUtil.prepareDetectForPreconfiguredDevice(deviceName,
                                        unknownONU, labels);
                                VOLTManagementUtil.sendKafkaNotification(request, m_messageFormatter, m_kafkaProducer);
                            } else {
                                if (VOLTManagementUtil.isVomciToBeUsed(device)) {
                                    sendSetOnuCommunication(deviceName, true, unknownONU.getOltDeviceName(),
                                            unknownONU.getChannelTermRef(), unknownONU.getOnuId());
                                }
                            }

                            VOLTManagementUtil.updateOnuStateInfoInDevice(device, unknownONU, m_deviceManager, m_txService);
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

    private void sendCreateOnu(String onuDeviceName) {
        List<Pair<ObjectType, String>> managementChain = VOLTManagementUtil.getManagementChain(onuDeviceName, m_txService, m_deviceDao);
        if (!managementChain.isEmpty()) {
            for (Pair<ObjectType, String> networkFunction : managementChain) {
                ActionRequest actionRequest = VOLTMgmtRequestCreationUtil.prepareCreateOnuRequest(onuDeviceName,
                        networkFunction.getFirst().toString().equals(String.valueOf(ObjectType.VOMCI_PROXY)));
                LOGGER.debug("Prepared Create ONU Action request " + actionRequest.requestToString());
                Object kafkaMessage = getFormattedKafkaMessage(actionRequest, onuDeviceName, networkFunction.getSecond(),
                        networkFunction.getSecond(), networkFunction.getFirst(), ONUConstants.CREATE_ONU);
                if (kafkaMessage != null) {
                    VOLTManagementUtil.sendKafkaMessage(kafkaMessage, networkFunction.getSecond(),
                            m_txService, m_networkFunctionDao, m_kafkaProducer);
                }
            }
        } else {
            LOGGER.warn("No ONU management chain details found for onu device " + onuDeviceName);
        }
    }

    private void sendDeleteOnu(String onuDeviceName) {
        List<Pair<ObjectType, String>> managementChain = VOLTManagementUtil.getManagementChain(onuDeviceName, m_txService, m_deviceDao);
        if (!managementChain.isEmpty()) {
            for (Pair<ObjectType, String> networkFunction : managementChain) {
                ActionRequest request = VOLTMgmtRequestCreationUtil.prepareDeleteOnuRequest(onuDeviceName,
                        networkFunction.getFirst().toString().equals(String.valueOf(ObjectType.VOMCI_PROXY)));
                LOGGER.debug("Prepared Delete ONU Action request " + request.requestToString());
                Object kafkaMessage = getFormattedKafkaMessage(request, onuDeviceName, networkFunction.getSecond(),
                        networkFunction.getSecond(), networkFunction.getFirst(), ONUConstants.DELETE_ONU);
                if (kafkaMessage != null) {
                    VOLTManagementUtil.sendKafkaMessage(kafkaMessage, networkFunction.getSecond(),
                            m_txService, m_networkFunctionDao, m_kafkaProducer);
                }
            }
        } else {
            LOGGER.warn("No ONU management chain details found for onu device " + onuDeviceName);
        }
    }

    private void sendSetOnuCommunication(String onuDeviceName, boolean isCommAvailable, String oltName,
                                         String channelTermName, String onuId) {
        List<Pair<ObjectType, String>> managementChain = VOLTManagementUtil.getManagementChain(onuDeviceName, m_txService, m_deviceDao);
        if (!managementChain.isEmpty()) {
            for (int i = 0; i < managementChain.size(); i++) {
                String voltmfRemoteEpName;
                String oltRemoteEpName;
                if (i == 0) {
                    voltmfRemoteEpName = m_txService.executeWithTxRequired(() -> m_networkFunctionDao.getLocalEndpointName(
                            managementChain.get(0).getSecond()));
                    if (managementChain.size() == 1) {
                        oltRemoteEpName = m_txService.executeWithTxRequired(() -> m_deviceManager.getEndpointName(onuDeviceName,
                                ONUConstants.TERMINATION_POINT_B, oltName));
                    } else {
                        int index = i + 1;
                        oltRemoteEpName = m_txService.executeWithTxRequired(() -> m_deviceManager.getEndpointName(onuDeviceName,
                                ONUConstants.TERMINATION_POINT_B, managementChain.get(index).getSecond()));
                    }
                } else if (i == (managementChain.size() - 1)) {
                    int index = i - 1;
                    voltmfRemoteEpName = m_txService.executeWithTxRequired(() -> m_deviceManager.getEndpointName(onuDeviceName,
                            ONUConstants.TERMINATION_POINT_A, managementChain.get(index).getSecond()));
                    oltRemoteEpName = m_txService.executeWithTxRequired(() -> m_deviceManager.getEndpointName(onuDeviceName,
                            ONUConstants.TERMINATION_POINT_B, oltName));
                } else {
                    int prevIndex = i - 1;
                    voltmfRemoteEpName = m_txService.executeWithTxRequired(() -> m_deviceManager.getEndpointName(onuDeviceName,
                            ONUConstants.TERMINATION_POINT_A, managementChain.get(prevIndex).getSecond()));
                    int nextIndex = i + 1;
                    oltRemoteEpName = m_txService.executeWithTxRequired(() -> m_deviceManager.getEndpointName(onuDeviceName,
                            ONUConstants.TERMINATION_POINT_B, managementChain.get(nextIndex).getSecond()));
                }
                ActionRequest request = VOLTMgmtRequestCreationUtil.prepareSetOnuCommunicationRequest(onuDeviceName,
                        isCommAvailable, oltName, channelTermName, onuId, voltmfRemoteEpName, oltRemoteEpName, (i != 0) ? true : false);
                LOGGER.debug("Prepared Set ONU Communincation Action request " + request.requestToString());
                Object kafkaMessage = getFormattedKafkaMessage(request, onuDeviceName, managementChain.get(i).getSecond(),
                        managementChain.get(i).getSecond(), managementChain.get(i).getFirst(),
                        isCommAvailable ? ONUConstants.SET_ONU_COMMUNICATION_TRUE : ONUConstants.SET_ONU_COMMUNICATION_FALSE);
                if (kafkaMessage != null) {
                    VOLTManagementUtil.sendKafkaMessage(kafkaMessage, managementChain.get(i).getSecond(),
                            m_txService, m_networkFunctionDao, m_kafkaProducer);
                }
            }
        } else {
            LOGGER.warn("No ONU management chain details found for onu device " + onuDeviceName);
        }
    }

    private Object getFormattedKafkaMessage(AbstractNetconfRequest request, String onuDeviceName, String recepientName, String objectName,
                                            ObjectType objectType, String operationType) {
        NetworkWideTag networkWideTag = new NetworkWideTag(onuDeviceName, recepientName, objectName, objectType);
        Device onuDevice = null;
        Object kafkaMessage = null;
        VOLTManagementUtil.setMessageId(request, m_messageId);
        try {
            if (!operationType.equals(ONUConstants.DELETE_ONU)) {
                LOGGER.info(String.format("Trying to fetch device-details from DB for %s before sending %s request",
                        onuDeviceName, operationType));
                onuDevice = m_txService.executeWithTxRequired(() -> m_deviceManager.getDevice(onuDeviceName));
            }
            kafkaMessage = m_messageFormatter.getFormattedRequest(request, operationType, onuDevice,
                    m_adapterManager, m_modelNodeDSM, m_schemaRegistry, networkWideTag);
            VOLTManagementUtil.registerInRequestMap(request, onuDeviceName, operationType);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Trying to send Kafka message to onu device that is already deleted from OBBAA: " + onuDeviceName, e);
        } catch (NetconfMessageBuilderException e) {
            LOGGER.error(String.format("Failed to convert netconf request to json: %s", request.requestToString(), e));
        } catch (MessageFormatterException e) {
            LOGGER.error(String.format("Failed to build GPB message from netconf request: %s", request.requestToString(), e));
        }
        return kafkaMessage;
    }

    protected void waitForNotificationTasks() {
        try {
            m_processNotificationRequestPool.shutdown();
            m_processNotificationRequestPool.awaitTermination(2, java.util.concurrent.TimeUnit.MINUTES);
        } catch (java.lang.InterruptedException e) {
            LOGGER.debug("Waiting process got interrupted");
        }
    }

    @Override
    public void deviceRemoved(String deviceName) {
        m_processNotificationRequestPool.execute(() -> {
            try {
                Device device = m_txService.executeWithTxRequired(() -> m_deviceManager.getDevice(deviceName));
                if (device != null) {
                    if (device.getDeviceManagement().getDeviceType().equals(ONUConstants.ONU_DEVICE_TYPE)) {
                        onuDeviceRemoved(device);
                    } else if (device.getDeviceManagement().getDeviceType().equals(ONUConstants.OLT_DEVICE_TYPE)) {
                        oltDeviceRemoved(device);
                    }
                }
            } catch (IllegalArgumentException e) {
                LOGGER.info("Device " + deviceName + " is already removed from DB");
                if (m_onuDevicesCreated.contains(deviceName)) {
                    if (m_messageFormatter instanceof GpbFormatter) {
                        sendDeleteOnu(deviceName);
                    }
                }
                m_onuDevicesCreated.remove(deviceName);
                if (m_networkFunctionResponse.containsKey(deviceName)) {
                    m_networkFunctionResponse.remove(deviceName);
                }
                VOLTManagementUtil.removeMatchedAttachmentPointFromMap(deviceName);
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
        MediatedDeviceNetconfSession deviceSession = VOLTManagementUtil.getMediatedDeviceNetconfSession(onuDevice, m_connectionManager);
        if (deviceSession != null) {
            if (m_messageFormatter instanceof JsonFormatter) {
                deviceSession.closeAsync();
            } else {
                if (VOLTManagementUtil.isVomciToBeUsed(onuDevice)) {
                    sendSetOnuCommunication(onuDevice.getDeviceName(), false, notification.getOltDeviceName(),
                            notification.getChannelTermRef(), notification.getOnuId());
                }
            }

        } else if (notification != null) {
            if (m_messageFormatter instanceof JsonFormatter) {
                NotificationRequest request = VOLTMgmtRequestCreationUtil.prepareUndetectKafkaMessage(onuDevice, notification,
                        VOLTMgmtRequestCreationUtil.getLabels(onuDevice));
                VOLTManagementUtil.sendKafkaNotification(request, m_messageFormatter, m_kafkaProducer);
            }
        }
    }

    @Override
    public void processResponse(Object responseObject) {
        try {
            if (m_messageFormatter.isHelloResponse(responseObject)) {
                LOGGER.info("Received an Hello Response Message.");
                HelloResponseData helloResponseData = m_messageFormatter.getHelloResponseData(responseObject);
                if (helloResponseData != null) {
                    m_processNotificationResponsePool.execute(() -> {
                        processHelloRequestResponse(helloResponseData);
                    });
                }
            } else {
                ResponseData initialResponseData = m_messageFormatter.getResponseData(responseObject);
                ResponseData responseData = VOLTManagementUtil.updateOperationTypeInResponseData(initialResponseData, m_messageFormatter);
                if (responseData != null) {
                    if (responseData.getOperationType().equals(ONUConstants.ONU_DETECTED)
                            || responseData.getOperationType().equals(ONUConstants.ONU_UNDETECTED)) {
                        /** Since this is notification response, spawn new thread out of processNotificationResponseTask pool.
                         * Therefore, do not block this main Kafka thread nor any other process request response threads.
                         * All DETECTs/UNDETECTs responses must be processed under processNotificationResponseTask pool.
                         * Since the main Kafka thread is freed immediately, and all other process request response threads are
                         * initiated under processNotificationResponse pool, it eliminates any possible cases of the deadlock and it
                         * improves efficiency and response time of processing other responses.
                         */
                        m_processNotificationResponsePool.execute(() -> {
                            processNotificationResponse(responseData.getOnuName(), responseData.getOperationType(),
                                    responseData.getIdentifier(), responseData.getResponseStatus(), responseData.getFailureReason(),
                                    responseData.getResponsePayload());
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
                            processRequestResponse(responseData);
                        });
                    }
                } else {
                    LOGGER.error("Error while processing response. Response Data could not be formed ");
                }
            }
        } catch (MessageFormatterException e) {
            LOGGER.error(e.getMessage());
        }
    }

    protected void processNotificationResponse(String onuDeviceName, String operationType, String identifier,
                                               String responseStatus, String failureReason, String data) {
        LOGGER.info(String.format("%s notification response is received for ONU device %s:\nIdentifier (message-id): %s\n"
                        + "Response Status: %s\n" + (responseStatus.equals(ONUConstants.NOK_RESPONSE) ? "Failure Reason: "
                        + failureReason + "\n" : "") + "Retrieved Data in JSON Format: None",
                operationType, onuDeviceName, identifier, responseStatus));
        // Find ONU Device
        String state = ONUConstants.OFFLINE;
        Device onuDevice = null;
        try {
            onuDevice = m_txService.executeWithTxRequired(() -> m_deviceManager.getDevice(onuDeviceName));
        } catch (IllegalArgumentException e) {
            LOGGER.debug(String.format("ONU device %s not found for the notification response received", onuDeviceName));
        }
        if (onuDevice != null) {
            MediatedDeviceNetconfSession mediatedSession = VOLTManagementUtil.getMediatedDeviceNetconfSession(onuDevice,
                    m_connectionManager);
            if (responseStatus.equals(ONUConstants.OK_RESPONSE) && (operationType.equals(ONUConstants.ONU_UNDETECTED)
                    || operationType.equals(ONUConstants.DELETE_ONU))) {
                if (mediatedSession == null) {
                    LOGGER.debug(String.format("Mediated Device Netconf Session has been already closed "
                            + "upon UNDETECT notification request with id %s for device %s", identifier, onuDeviceName));
                } else {
                    LOGGER.error(String.format("Mediated Device Netconf Session must have been already "
                            + "closed upon UNDETECT notification request for device:%s and should not be open upon receiving and "
                            + "processing UNDETECT notification response", onuDeviceName));
                    LOGGER.debug(String.format("Attempt to close Mediated Device Netconf Session of device %s due to "
                            + "UNDETECT notification response with id %s", onuDeviceName, identifier));
                    try {
                        mediatedSession.close();
                    } catch (Exception e) {
                        LOGGER.error("Error while closing Mediated device netconf session for device " + onuDeviceName);
                    }
                }
                m_txService.executeWithTxRequired((TxTemplate<Void>) () -> {
                    m_deviceManager.updateConfigAlignmentState(onuDeviceName, DeviceManagerNSConstants.NEVER_ALIGNED);
                    return null;
                });
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
                        HashMap<String, String> labels = VOLTMgmtRequestCreationUtil.getLabels(onuDevice);
                        MediatedDeviceNetconfSession newMediatedSession = new MediatedDeviceNetconfSession(onuDevice,
                                oltDeviceName, onuId, channelTermRef, labels, m_kafkaProducer,
                                m_modelNodeDSM, m_adapterManager, m_kafkaCommunicationPool, m_schemaRegistry, m_messageFormatter,
                                m_txService, m_networkFunctionDao, m_deviceDao, m_nbiNetconfServerMessageListener);
                        m_connectionManager.addMediatedDeviceNetconfSession(onuDeviceName, newMediatedSession);

                        //VOMCI functions is currently not supporting GET request handling, commenting this code for now.
                        //Also Ignoring "testProcessDetectResponseWhenDevicePresentSendsGetRequest()" UT
                        sendInternalGetOnMediatedDeviceSession(onuDeviceName, newMediatedSession);
                    } else {
                        LOGGER.debug(String.format("Mediated Device Netconf Session already exists with the device %s "
                                + "while processing DETECT OK response notification with id %s", onuDeviceName, identifier));
                        mediatedSession.open();
                        sendInternalGetOnMediatedDeviceSession(onuDeviceName, mediatedSession);
                    }

                    //Send notification to BAA NBI
                    state = ONUConstants.ONLINE;
                    VOLTManagementUtil.sendOnuDiscoveryResultNotification(onuDevice, state, m_notificationService);
                }
            }
        } else {
            LOGGER.warn(String.format("ONU Device is not found based on its name: %s "
                    + "Processing %s response notification with id %s has failed", onuDeviceName, operationType, identifier));
        }
    }

    private void sendInternalGetOnMediatedDeviceSession(String onuDeviceName, MediatedDeviceNetconfSession mediatedDeviceNetconfSession) {
        GetRequest getRequest = VOLTMgmtRequestCreationUtil.prepareInternalGetRequest(onuDeviceName);
        getRequest.setMessageId(ONUConstants.DEFAULT_MESSAGE_ID);
        mediatedDeviceNetconfSession.onGet(getRequest);
    }

    protected void processRequestResponse(ResponseData responseData) {
        String onuDeviceName = responseData.getOnuName();
        String operationType = responseData.getOperationType();
        String identifier = responseData.getIdentifier();
        String responseStatus = responseData.getResponseStatus();
        String failureReason = responseData.getFailureReason();
        String data = responseData.getResponsePayload();
        String senderName = responseData.getSenderName();
        String equipmentId = null;
        Set<SoftwareImage> softwareImageSet = null;

        LOGGER.info(String.format("%s response is received for ONU device %s from %s\nIdentifier (message-id): %s\nResponse Status: %s\n"
                        + (responseStatus.equals(ONUConstants.NOK_RESPONSE) || (responseStatus.equals(ONUConstants.ERROR_RESPONSE))
                        ? "Failure Reason: " + failureReason + "\n" : "") + " ",
                operationType, onuDeviceName, senderName, identifier, responseStatus));
        Device onuDevice = null;
        try {
            onuDevice = m_txService.executeWithTxRequired(() -> m_deviceManager.getDevice(onuDeviceName));
        } catch (IllegalArgumentException e) {
            LOGGER.debug(String.format("ONU device %s not found for the notification response received", onuDeviceName));
        }
        if (onuDevice != null) {
            MediatedDeviceNetconfSession mediatedSession = VOLTManagementUtil.getMediatedDeviceNetconfSession(onuDevice,
                    m_connectionManager);
            if (m_messageFormatter instanceof JsonFormatter) {
                if (mediatedSession == null) {
                    LOGGER.error(String.format("Unable to find Mediated Device Netconf Session while processing %s "
                                    + "response for %s device:\nIdentifier (message-id): %s\nResponse Status: %s\n"
                                    + (responseStatus.equals(ONUConstants.NOK_RESPONSE) ? "Failure Reason: " + failureReason
                                    + "\n" : "") + "Retrieved Data in JSON Format: %s",
                            operationType, onuDeviceName, identifier, responseStatus, data));
                    return;
                }
            }
            NetConfResponse response;
            switch (operationType) {
                case ONUConstants.ONU_GET_OPERATION:
                    if (!identifier.equals(ONUConstants.DEFAULT_MESSAGE_ID)) {
                        //Internal GET response has indentifier = 0
                        data = "{" + ONUConstants.DOUBLE_QUOTES + ONUConstants.NETWORK_MANAGER + ONUConstants.COLON
                                + ONUConstants.ROOT + ONUConstants.DOUBLE_QUOTES + ONUConstants.COLON + data + "}";
                        LOGGER.info(String.format("Processing Get response  :%s", data));
                        response = mediatedSession.processGetResponse(identifier, responseStatus, data, failureReason);
                        if (response != null) {
                            m_nbiNetconfServerMessageListener.addResponseIntoMap(identifier, response.responseToString());
                        } else {
                            LOGGER.error("Failed to process Get response, Response is NULL");
                        }
                        LOGGER.debug("Processing " + operationType + " response with id "
                                + identifier + " is " + (response == null ? "failed" : "successful"));
                    } else {
                        String onuSerialNumber = onuDevice.getDeviceManagement().getOnuConfigInfo().getExpectedSerialNumber();
                        equipmentId = VOLTManagementUtil.processInternalGetResponseAndRetrieveHwProperties(responseStatus, data,
                                onuSerialNumber);
                        softwareImageSet = VOLTManagementUtil.processInternalGetResponseAndRetrieveSWProperties(responseStatus, data);
                        String finalEquipmentId = equipmentId;
                        Set<SoftwareImage> finalSoftwareImageSet = softwareImageSet;
                        m_txService.executeWithTxRequired((TxTemplate<Void>) () -> {
                            if (finalEquipmentId != null) {
                                m_deviceManager.updateEquipmentIdInOnuStateInfo(onuDeviceName, finalEquipmentId);
                            }
                            if (!finalSoftwareImageSet.isEmpty()) {
                                m_deviceManager.updateSoftwareImageInOnuStateInfo(onuDeviceName, finalSoftwareImageSet);
                            }
                            return null;
                        });
                    }
                    VOLTManagementUtil.removeRequestFromMap(identifier);
                    break;
                case ONUConstants.ONU_COPY_OPERATION:
                    response = mediatedSession.processResponse(identifier, operationType, responseStatus, failureReason);
                    LOGGER.debug("Processing " + operationType + " response with id "
                            + identifier + " is " + (response == null ? "failed" : "successful"));
                    if (responseStatus.equals(ONUConstants.OK_RESPONSE)) {
                        try {
                            onuDevice = m_txService.executeWithTxRequired(() -> m_deviceManager.getDevice(onuDeviceName));

                        } catch (IllegalArgumentException e) {
                            LOGGER.debug(String.format("ONU device %s not found", onuDeviceName));
                        }
                        if (m_connectionManager.getConnectionState(onuDevice).isConnected()) {
                            m_txService.executeWithTxRequired((TxTemplate<Void>) () -> {
                                m_deviceManager.updateConfigAlignmentState(onuDeviceName, DeviceManagerNSConstants.ALIGNED);
                                return null;
                            });
                            VOLTManagementUtil.sendOnuDiscoveryResultNotification(onuDevice, ONUConstants.ONLINE,
                                    m_notificationService);
                        }
                    } else {
                        LOGGER.debug("Received response " + ONUConstants.NOK_RESPONSE + "for the operation" + operationType
                                + " with id " + identifier + " failed with error " + failureReason);
                    }
                    VOLTManagementUtil.removeRequestFromMap(identifier);
                    break;
                case ONUConstants.ONU_EDIT_OPERATION:
                    response = mediatedSession.processResponse(identifier, operationType, responseStatus, failureReason);
                    LOGGER.debug("Processing " + operationType + " response with id "
                            + identifier + " is " + (response == null ? "failed" : "successful"));
                    VOLTManagementUtil.removeRequestFromMap(identifier);
                    break;
                case ONUConstants.CREATE_ONU:
                    if (responseStatus.equals(ONUConstants.OK_RESPONSE)) {
                        LOGGER.info("Processing " + operationType + " response from " + senderName + " with id "
                                + identifier + " is successful");
                    } else if (responseStatus.equals(ONUConstants.ERROR_RESPONSE)) {
                        LOGGER.info("Processing " + operationType + " response from " + senderName + " with id "
                                + identifier + " failed with error " + failureReason);
                    } else {
                        LOGGER.info(String.format("Invalid response code %s received from %s ", responseStatus, senderName));
                    }
                    m_txService.executeWithTxRequired((TxTemplate<Void>) () -> {
                        m_deviceManager.updateConfigAlignmentState(onuDeviceName, DeviceManagerNSConstants.ALIGNMENT_UNKNOWN);
                        return null;
                    });
                    VOLTManagementUtil.removeRequestFromMap(identifier);
                    break;
                case ONUConstants.SET_ONU_COMMUNICATION_FALSE:
                case ONUConstants.SET_ONU_COMMUNICATION_TRUE:
                    boolean isSuccessful = false;
                    if (responseStatus.equals(ONUConstants.OK_RESPONSE)) {
                        isSuccessful = true;
                    } else if (responseStatus.equals(ONUConstants.ERROR_RESPONSE)) {
                        isSuccessful = false;
                    } else {
                        LOGGER.warn(String.format("Invalid response code %s received from %s ", responseStatus, senderName));
                    }
                    if (m_networkFunctionResponse.containsKey(onuDeviceName)) {
                        m_networkFunctionResponse.get(onuDeviceName).add(isSuccessful);
                    } else {
                        ArrayList<Boolean> responseArray = new ArrayList<>();
                        responseArray.add(isSuccessful);
                        m_networkFunctionResponse.put(onuDeviceName, responseArray);
                    }
                    int mgmtChainSize = VOLTManagementUtil.getManagementChain(onuDeviceName, m_txService, m_deviceDao).size();
                    if (VOLTManagementUtil.isVomciToBeUsed(onuDevice)
                            && mgmtChainSize <= m_networkFunctionResponse.get(onuDeviceName).size()) {
                        MediatedDeviceNetconfSession devSession = VOLTManagementUtil.getMediatedDeviceNetconfSession(onuDevice,
                                m_connectionManager);
                        ArrayList<Boolean> respArray = m_networkFunctionResponse.get(onuDeviceName);
                        m_networkFunctionResponse.remove(onuDeviceName);
                        if (VOLTManagementUtil.isResponseOK(respArray)) {
                            if (ONUConstants.SET_ONU_COMMUNICATION_TRUE.equals(operationType)) {
                                if (devSession == null) {
                                    LOGGER.info("Received all responses from network functions. " + onuDeviceName
                                            + " is connected so creating new MediatedDeviceSession");
                                    ActualAttachmentPoint actualAttachmentPoint = onuDevice.getDeviceManagement().getDeviceState()
                                            .getOnuStateInfo().getActualAttachmentPoint();
                                    String oltDeviceName = actualAttachmentPoint.getOltName();
                                    String onuId1 = actualAttachmentPoint.getOnuId();
                                    String channelTermRef1 = actualAttachmentPoint.getChannelTerminationRef();
                                    MediatedDeviceNetconfSession newDeviceMediatedSession = new MediatedDeviceNetconfSession(onuDevice,
                                            oltDeviceName, onuId1, channelTermRef1, null, m_kafkaProducer,
                                            m_modelNodeDSM, m_adapterManager, m_kafkaCommunicationPool, m_schemaRegistry,
                                            m_messageFormatter, m_txService, m_networkFunctionDao, m_deviceDao,
                                            m_nbiNetconfServerMessageListener);
                                    m_connectionManager.addMediatedDeviceNetconfSession(onuDeviceName, newDeviceMediatedSession);
                                    m_connectionManager.getConnectionState(onuDevice).setConnected(true);
                                    if (onuDevice.isAligned()) {
                                        VOLTManagementUtil.sendOnuDiscoveryResultNotification(onuDevice, ONUConstants.ONLINE,
                                                m_notificationService);
                                    }
                                } else {
                                    LOGGER.info("Received all responses from network functions. " + onuDeviceName
                                            + "MediatedDeviceSession already created.");
                                }
                            } else {
                                if (devSession != null) {
                                    LOGGER.info("Received all responses from network functions. " + onuDeviceName
                                            + " is disconnected so closing MediatedDeviceSession");
                                    try {
                                        devSession.close();
                                    } catch (IOException e) {
                                        LOGGER.error("Error while closing session for " + onuDeviceName, e);
                                    } catch (InterruptedException e) {
                                        LOGGER.error("Error while closing session for " + onuDeviceName, e);
                                    }

                                    m_connectionManager.getConnectionState(onuDevice).setConnected(false);
                                    m_txService.executeWithTxRequired((TxTemplate<Void>) () -> {
                                        m_deviceManager.updateConfigAlignmentState(onuDeviceName,
                                                DeviceManagerNSConstants.ALIGNMENT_UNKNOWN);
                                        return null;
                                    });
                                    VOLTManagementUtil.sendOnuDiscoveryResultNotification(onuDevice, ONUConstants.OFFLINE,
                                            m_notificationService);
                                } else {
                                    LOGGER.info("Received all responses from network functions. " + onuDeviceName
                                            + " is disconnected. MediatedDeviceSession was already closed.");
                                }
                            }
                        } else {
                            LOGGER.info("Processing " + operationType + " response from " + senderName + " with id "
                                    + identifier + " failed with error " + failureReason);
                        }
                        respArray = null;
                    } else {
                        LOGGER.info("Responses from all network functions still not received. Waiting for "
                                + (mgmtChainSize - m_networkFunctionResponse.get(onuDeviceName).size()) + " responses.");
                    }
                    VOLTManagementUtil.removeRequestFromMap(identifier);
                    break;
                default:
                    LOGGER.warn(String.format("Unknown response for device %s of type %s from vOMCI. Unable to process it ",
                            onuDeviceName, operationType));
            }
        } else {
            if (operationType.equals(ONUConstants.DELETE_ONU)) {
                if (responseStatus.equals(ONUConstants.OK_RESPONSE)) {
                    MediatedDeviceNetconfSession deviceSession = VOLTManagementUtil.getMediatedDeviceNetconfSession(onuDevice,
                            m_connectionManager);
                    try {
                        if (deviceSession != null) {
                            deviceSession.close();
                        } else {
                            LOGGER.info("Device Session for " + onuDeviceName + " is " + deviceSession);
                        }
                        LOGGER.info("Processing " + operationType + " response with id " + identifier + " is successful");
                    } catch (Exception e) {
                        LOGGER.error(String.format("Error while closing Mediated device netconf session for device:%s ",
                                onuDeviceName));
                    }
                } else if (responseStatus.equals(ONUConstants.ERROR_RESPONSE)) {
                    LOGGER.info(String.format("Processing %s response from %s with id %s failed with error: %s ",
                            operationType, senderName, identifier, failureReason));
                } else {
                    LOGGER.info(String.format("Invalid response code %s received from %s ", responseStatus, senderName));
                }
                VOLTManagementUtil.removeRequestFromMap(identifier);
                if (m_networkFunctionResponse.containsKey(onuDeviceName)) {
                    m_networkFunctionResponse.remove(onuDeviceName);
                }
            } else if (operationType.equals(NetconfResources.EDIT_CONFIG)
                    || operationType.equals(NetconfResources.COPY_CONFIG)) {
                MediatedNetworkFunctionNetconfSession networkFunctionNetconfSession =
                        VOLTManagementUtil.getMediatedNetworkFunctionNetconfSession(senderName, m_connectionManager);
                if (networkFunctionNetconfSession != null) {
                    NetConfResponse response = networkFunctionNetconfSession.processResponse(identifier,
                            operationType,
                            responseStatus,
                            failureReason);
                    LOGGER.info("Processing " + operationType + " response with id "
                            + identifier + " is " + (response == null ? "failed" : "successful"));
                } else {
                    LOGGER.info("Session for sender'" + senderName + "' not found.");
                }
                VOLTManagementUtil.removeRequestFromMap(identifier);
            } else {
                LOGGER.warn(String.format("Unknown operation type %s ", operationType));
            }
        }
    }

    /*
    This method will need further implementations to process the received information.
    For now it only saves the received response in a Map (m_helloResponseMessages).
    */
    protected void processHelloRequestResponse(HelloResponseData responseData) {
        // the sender name is the network function name
        m_helloResponseMessages.put(responseData.getSenderName(),responseData);
        VOLTManagementUtil.removeRequestFromMap(responseData.getIdentifier());
        LOGGER.info("Processing hello message response from " + responseData.getSenderName() + " with id "
                + responseData.getIdentifier() + " was successfully processed");
        LOGGER.info("\nReceived hello message response: " + responseData.toString());
    }

    @Override
    public void processNotification(Object notificationResponse) {
        LOGGER.info(String.format("Received notification:  %s", notificationResponse.toString()));
        try {
            if (m_messageFormatter instanceof GpbFormatter) {
                ResponseData responseData = m_messageFormatter.getResponseData(notificationResponse);
                String notificationData = responseData.getResponsePayload();
                String onuName = responseData.getOnuName();
                String onuAlignmentStatus = null;
                Device onuDevice = null;
                if (responseData.getOperationType().equals(NetconfResources.NOTIFICATION) && responseData.getObjectType()
                        .equals(ObjectType.ONU)) {
                    if (notificationData.contains(ONUConstants.VOMCI_FUNC_ONU_ALIGNMENT_STATUS_JSON_KEY)) {
                        onuAlignmentStatus = VOLTManagementUtil.processNotificationAndRetrieveOnuAlignmentStatus(notificationData);
                        if (onuAlignmentStatus != null) {
                            if (onuAlignmentStatus.equals(ONUConstants.ALIGNED)) {
                                try {
                                    onuDevice = m_txService.executeWithTxRequired(() -> m_deviceManager.getDevice(onuName));
                                } catch (IllegalArgumentException e) {
                                    LOGGER.error(String.format("Device with name %s does not exist: %s", onuName, e));
                                }
                                if (onuDevice != null) {
                                    m_txService.executeWithTxRequired((TxTemplate<Void>) () -> {
                                        m_deviceManager.updateConfigAlignmentState(onuName, DeviceManagerNSConstants.ALIGNED);
                                        return null;
                                    });
                                    if (m_connectionManager.getConnectionState(onuDevice).isConnected()) {
                                        VOLTManagementUtil.sendOnuDiscoveryResultNotification(onuDevice, ONUConstants.ONLINE,
                                                m_notificationService);
                                    }
                                }
                            } else {
                                m_txService.executeWithTxRequired((TxTemplate<Void>) () -> {
                                    m_deviceManager.updateConfigAlignmentState(onuName, DeviceManagerNSConstants.NEVER_ALIGNED);
                                    return null;
                                });
                            }
                        } else {
                            LOGGER.error("ONU Alignment Status received from notification is NULL");
                        }
                    } else {
                        onuDevice = m_txService.executeWithTxRequired(() -> m_deviceManager.getDevice(onuName));

                        AlarmInfo internalAlarmInfo = VOLTManagementUtil.processVomciNotificationAndPrepareAlarmInfo(
                                notificationData, onuName);

                        SchemaRegistry onuDeviceSchemaRegistry = VOLTManagementUtil.getDeviceSchemaRegistry(m_adapterManager, onuDevice);

                        Element alarmNotificationElement = VOLTManagementUtil.prepareAlarmNotificationElementFromAlarmInfo(
                                internalAlarmInfo);
                        LOGGER.info(String.format("Converted alarm notification: %s", DocumentUtils
                                .documentToPrettyString(alarmNotificationElement)));

                        List<AlarmInfo> alarmInfoList = AlarmNotificationUtil.extractAndCreateAlarmEntryFromNotification(
                                alarmNotificationElement, onuDeviceSchemaRegistry, onuDevice, IETF_ALARM_NS);

                        if (alarmInfoList != null && !alarmInfoList.isEmpty()) {
                            for (AlarmInfo alarmInfo : alarmInfoList) {
                                AlarmInfo updatedAlarmInfo = VOLTManagementUtil.updateDeviceInstanceIdentifierInAlarmInfo(alarmInfo,
                                        onuName);
                                if (alarmInfo != null) {
                                    if (alarmInfo.getSeverity().toString().equalsIgnoreCase(ONUConstants.CLEARED)) {
                                        LOGGER.info(String.format("Clearing ONU Alarm : %s", updatedAlarmInfo));
                                        m_alarmService.clearAlarm(updatedAlarmInfo);
                                    } else {
                                        LOGGER.info(String.format("Raising ONU Alarm : %s", updatedAlarmInfo));
                                        m_alarmService.raiseAlarm(updatedAlarmInfo);
                                    }
                                } else {
                                    LOGGER.info(String.format("Error while converting the received alarm-notification to AlarmInfo. "
                                            + "Notification received from vOMCI is: %s ", responseData));
                                }
                            }
                        } else {
                            LOGGER.info(String.format("AlarmInfo List is either empty or null"));
                        }
                    }
                }
            }
        } catch (MessageFormatterException | NetconfMessageBuilderException e) {
            LOGGER.error(e.getMessage());
        }

    }

    public void setAndIncrementVoltmfInternalMessageId(AbstractNetconfRequest request) {
        VOLTManagementUtil.setMessageId(request, m_messageId);
    }
}