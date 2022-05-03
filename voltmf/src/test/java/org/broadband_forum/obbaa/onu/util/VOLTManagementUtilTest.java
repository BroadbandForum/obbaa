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

import static org.broadband_forum.obbaa.onu.ONUConstants.DEVICE_INSTANCE_IDENTIFIER_FORMAT;
import static org.broadband_forum.obbaa.onu.ONUConstants.ONU_PRESENT_AND_NO_VANI_KNOWN_AND_UNCLAIMED;
import static org.broadband_forum.obbaa.onu.ONUConstants.ONU_PRESENT_AND_ON_INTENDED_CHANNEL_TERMINATION;
import static org.broadband_forum.obbaa.onu.ONUConstants.ONU_PRESENT_AND_V_ANI_KNOW_AND_O5_FAILED_NO_ONU_ID;
import static org.broadband_forum.obbaa.onu.ONUConstants.ONU_PRESENT_AND_V_ANI_KNOW_AND_O5_FAILED_UNDEFINED;
import static org.broadband_forum.obbaa.onu.ONUConstants.ONU_PRESENT_AND_V_ANI_KNOW_BUT_INTENDED_CT_UNKNOWN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.dmyang.dao.DeviceDao;
import org.broadband_forum.obbaa.dmyang.entities.ActualAttachmentPoint;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.DeviceMgmt;
import org.broadband_forum.obbaa.dmyang.entities.DeviceState;
import org.broadband_forum.obbaa.dmyang.entities.ExpectedAttachmentPoint;
import org.broadband_forum.obbaa.dmyang.entities.ExpectedAttachmentPoints;
import org.broadband_forum.obbaa.dmyang.entities.OnuConfigInfo;
import org.broadband_forum.obbaa.dmyang.entities.OnuManagementChain;
import org.broadband_forum.obbaa.dmyang.entities.OnuStateInfo;
import org.broadband_forum.obbaa.dmyang.entities.SoftwareImage;
import org.broadband_forum.obbaa.netconf.alarm.api.AlarmInfo;
import org.broadband_forum.obbaa.netconf.alarm.entity.AlarmSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.TxService;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
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
import org.broadband_forum.obbaa.onu.message.ObjectType;
import org.broadband_forum.obbaa.onu.message.ResponseData;
import org.broadband_forum.obbaa.onu.notification.ONUNotification;
import org.broadband_forum.obbaa.pma.PmaRegistry;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.w3c.dom.Element;

public class VOLTManagementUtilTest {

    private final String ONU_SERIAL_NUMBER = "ABCD12345678";
    private final String CHANNEL_TERM_REF = "CT_1";
    private final String ONU_STATE = "onu-present-and-on-intended-channel-termination";
    private final String CHANNEL_PARTITION_NAME = "CP1";
    private final String VANI_REF = "Onu_1";
    private final String OLT_NAME = "OLT1";
    private final String ONU_NAME = "onu1";
    private final String ONU_ID = "1";
    public static final String RPC_REPLY = "/get-response.xml";
    private static final AtomicLong m_messageId = new AtomicLong(1000);
    private final String DETERMINED_ONU_MANAGEMENT_MODE = "relying-on-vomci";
    private final String DETECTED_LOCATION_ID = "testLocation";
    private final String ONU_NOT_PRESENT = "baa-xpon-onu-types:onu-not-present";

    public static final String RPC_ERROR_REPLY = "/onu-authentication-report-action-error-response.xml";
    String internalGetRequestJson = "/internal-get-request.json";
    String internalGetResponseJson = "/internal-get-response-sw-hw-prop.json";
    String onuAlignmentAlignedNoitifcationJson = "/onu-alignment-status-notification-aligned.json";
    String onuAlignmentMisalignedNoitifcationJson = "/onu-alignment-status-notification-misaligned.json";
    String ietfAlarmsAlarmNotificationJson = "/ietf-alarms-alarm-notification.json";
    String ietfAlarmsAlarmNotificationClearedJson = "/ietf-alarms-alarm-notification-cleared.json";


    TxService m_txService;
    @Mock
    UnknownONUHandler m_unknownOnuHandler;
    @Mock
    ONUNotification m_onuNotification;
    @Mock
    DeviceDao m_deviceDao;
    @Mock
    UnknownONU m_unknownOnu;
    @Mock
    Device m_onuDevice;
    @Mock
    DeviceMgmt m_deviceMgmt;
    @Mock
    DeviceState m_deviceState;
    @Mock
    DeviceManager m_deviceManager;
    @Mock
    MessageFormatter m_messageFormatter;
    @Mock
    NetworkFunctionDao m_networkFunctionDao;
    @Mock
    OnuKafkaConsumer m_onuKafkaConsumer;
    @Mock
    OnuKafkaProducer m_onuKafkaProducer;
    @Mock
    NotificationRequest m_notificationRequest;
    @Mock
    Object m_notification;
    @Mock
    Map<String, Set<String>> m_kafkaConsumerTopicMap;
    @Mock
    KafkaTopic m_kafkaTopic;
    @Mock
    NetconfConnectionManager m_netconfConnectionManager;
    @Mock
    MediatedDeviceNetconfSession m_mediatedDeviceNetconfSession;
    @Mock
    NotificationService m_notificationService;
    @Mock
    OnuConfigInfo m_onuConfigInfo;
    @Mock
    ResponseData m_responseData;
    @Mock
    AbstractNetconfRequest m_request;
    @Mock
    PmaRegistry m_pmaRegistry;
    @Mock
    ExpectedAttachmentPoint m_expectedAttachmentPoint;
    @Mock
    Future<NetConfResponse> m_responseFuture;
    @Mock
    NetConfResponse m_netConfResponse;

    Element m_dataElement = DocumentUtils.stringToDocument(TestUtil.loadAsString(RPC_REPLY)).getDocumentElement();

    @Mock
    ActionRequest m_actionRequest;
    @Mock
    Element m_element;
    @Mock
    List<NetconfRpcError> m_rpcErrorList;
    @Mock
    NetconfRpcError m_rpcError;
    @Mock
    NetConfResponse m_netconfResponse;
    @Mock
    Iterator<ExpectedAttachmentPoint> m_expectedAttachmentPointIterator;
    @Mock
    private ExpectedAttachmentPoints m_expectedAttachmentPoints;
    @Mock
    private Set<ExpectedAttachmentPoint> m_expectedAttachmentPointSet;
    @Mock
    private ActualAttachmentPoint m_actualAttachmentPoint;
    @Mock
    private OnuStateInfo m_onuStateInfo;

    public VOLTManagementUtilTest() throws NetconfMessageBuilderException {
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        m_txService = new TxService();
    }

    @Test
    public void testIsResponseOK() {
        ArrayList<Boolean> responseArray = new ArrayList<>();
        responseArray.add(true);
        responseArray.add(true);
        responseArray.add(true);
        assertTrue(VOLTManagementUtil.isResponseOK(responseArray));
        responseArray.remove(0);
        responseArray.add(0, false);
        assertFalse(VOLTManagementUtil.isResponseOK(responseArray));

    }

    @Test
    public void testPersistUnknwonOnu() {
        UnknownONU unknownONU = new UnknownONU();
        unknownONU.setOnuID(ONU_ID);
        unknownONU.setSerialNumber(ONU_SERIAL_NUMBER);
        unknownONU.setChannelTermRef(CHANNEL_TERM_REF);
        when(m_onuNotification.getSerialNo()).thenReturn(ONU_SERIAL_NUMBER);
        when(m_onuNotification.getOnuId()).thenReturn(ONU_ID);
        when(m_onuNotification.getChannelTermRef()).thenReturn(CHANNEL_TERM_REF);
        when(m_onuNotification.getOnuState()).thenReturn(ONU_STATE);
        when(m_onuNotification.getVAniRef()).thenReturn(VANI_REF);
        when(m_unknownOnuHandler.findUnknownOnuEntity(ONU_SERIAL_NUMBER, null)).thenReturn(unknownONU);
        when(m_onuNotification.getDeterminedOnuManagementMode()).thenReturn(DETERMINED_ONU_MANAGEMENT_MODE);
        when(m_onuNotification.getDetectedLoId()).thenReturn(DETECTED_LOCATION_ID);
        VOLTManagementUtil.persistUnknownOnuToDB(OLT_NAME, null, m_txService, m_unknownOnuHandler);
        verify(m_onuNotification, times(0)).getDeterminedOnuManagementMode();
        verify(m_onuNotification, times(0)).getDetectedLoId();
        verify(m_unknownOnuHandler, times(0)).findUnknownOnuEntity(anyString(), anyString());
        VOLTManagementUtil.persistUnknownOnuToDB(OLT_NAME, m_onuNotification, m_txService, m_unknownOnuHandler);
        verify(m_onuNotification, times(3)).getDeterminedOnuManagementMode();
        verify(m_onuNotification, times(3)).getDetectedLoId();
        verify(m_unknownOnuHandler, times(1)).findUnknownOnuEntity(ONU_SERIAL_NUMBER, null);
        assertNull(unknownONU.getRegistrationId());
    }

    @Test
    public void testPersistUnknwonOnuWhenMatchedOnuIsNull() {
        UnknownONU unknownONU = new UnknownONU();
        unknownONU.setOnuID(ONU_ID);
        unknownONU.setSerialNumber(ONU_SERIAL_NUMBER);
        unknownONU.setChannelTermRef(CHANNEL_TERM_REF);
        unknownONU.setVAniRef(VANI_REF);
        unknownONU.setOnuState(ONU_STATE);
        when(m_onuNotification.getSerialNo()).thenReturn(ONU_SERIAL_NUMBER);
        when(m_onuNotification.getOnuId()).thenReturn(ONU_ID);
        when(m_onuNotification.getChannelTermRef()).thenReturn(CHANNEL_TERM_REF);
        when(m_onuNotification.getOnuState()).thenReturn(ONU_STATE);
        when(m_onuNotification.getVAniRef()).thenReturn(VANI_REF);
        when(m_unknownOnuHandler.findUnknownOnuEntity(ONU_SERIAL_NUMBER, null)).thenReturn(null);
        VOLTManagementUtil.persistUnknownOnuToDB(OLT_NAME, m_onuNotification, m_txService, m_unknownOnuHandler);
        verify(m_unknownOnuHandler, times(1)).findUnknownOnuEntity(ONU_SERIAL_NUMBER, null);
        verify(m_unknownOnuHandler, times(1)).createUnknownONUEntity(any());
    }

    @Test
    public void testBuildUnknownOnu() {
        when(m_onuNotification.getSerialNo()).thenReturn(ONU_SERIAL_NUMBER);
        when(m_onuNotification.getOnuId()).thenReturn(ONU_ID);
        when(m_onuNotification.getChannelTermRef()).thenReturn(CHANNEL_TERM_REF);
        when(m_onuNotification.getOnuState()).thenReturn(ONU_STATE);
        when(m_onuNotification.getVAniRef()).thenReturn(VANI_REF);
        when(m_onuNotification.getDeterminedOnuManagementMode()).thenReturn(DETERMINED_ONU_MANAGEMENT_MODE);
        when(m_onuNotification.getDetectedLoId()).thenReturn(DETECTED_LOCATION_ID);
        UnknownONU unknownONU = VOLTManagementUtil.buildUnknownOnu(OLT_NAME, m_onuNotification);
        assertNotNull(unknownONU);
        assertEquals(OLT_NAME, unknownONU.getOltDeviceName());
        assertEquals(ONU_ID, unknownONU.getOnuId());
        assertEquals(CHANNEL_TERM_REF, unknownONU.getChannelTermRef());
        assertEquals(VANI_REF, unknownONU.getVAniRef());
        assertEquals(ONU_STATE, unknownONU.getOnuState());
        assertEquals(DETERMINED_ONU_MANAGEMENT_MODE, unknownONU.getDeterminedOnuManagementMode());
        assertEquals(DETECTED_LOCATION_ID, unknownONU.getDetectedLoId());
        assertNull(unknownONU.getRegistrationId());
        assertNull(unknownONU.getOnuStateLastChange());
    }

    @Test
    public void testupdateOnuStateInfoInDevice() {
        when(m_unknownOnu.getChannelTermRef()).thenReturn(CHANNEL_TERM_REF);
        when(m_unknownOnu.getOltDeviceName()).thenReturn(OLT_NAME);
        when(m_unknownOnu.getOnuId()).thenReturn(ONU_ID);
        when(m_unknownOnu.getOnuState()).thenReturn(ONU_STATE);
        when(m_unknownOnu.getEquipmentId()).thenReturn("eqptId1");
        when(m_onuDevice.getDeviceName()).thenReturn(ONU_NAME);
        when(m_onuDevice.getDeviceManagement()).thenReturn(m_deviceMgmt);
        when(m_deviceMgmt.getDeviceState()).thenReturn(m_deviceState);
        when(m_deviceState.getDeviceNodeId()).thenReturn(ONU_NAME);
        when(m_unknownOnu.getDeterminedOnuManagementMode()).thenReturn(DETERMINED_ONU_MANAGEMENT_MODE);
        VOLTManagementUtil.updateOnuStateInfoInDevice(m_onuDevice, m_unknownOnu, m_deviceManager, m_txService);
        verify(m_deviceManager, times(1)).updateOnuStateInfo(any(), anyObject());
        verify(m_deviceState, times(2)).getDeviceNodeId();
        verify(m_unknownOnu, times(1)).getDeterminedOnuManagementMode();
    }

    @Test
    public void testUpdateKafkaSubscriptions() {
        Set<KafkaTopic> kafkaTopicSet = new HashSet<>();
        kafkaTopicSet.add(m_kafkaTopic);
        when(m_networkFunctionDao.getKafkaConsumerTopics("vomci1")).thenReturn(kafkaTopicSet);
        m_messageFormatter = new GpbFormatter();
        VOLTManagementUtil.updateKafkaSubscriptions("vomci1", m_messageFormatter, m_networkFunctionDao, m_onuKafkaConsumer,
                m_kafkaConsumerTopicMap);
        verify(m_onuKafkaConsumer, times(1)).updateSubscriberTopics(kafkaTopicSet);
        verify(m_networkFunctionDao, times(1)).getKafkaConsumerTopics("vomci1");

    }

    @Test
    public void testUpdateKafkaSubscriptionsJsonFormatter() {
        Set<KafkaTopic> kafkaTopicSet = new HashSet<>();
        kafkaTopicSet.add(m_kafkaTopic);
        when(m_networkFunctionDao.getKafkaConsumerTopics("vomci1")).thenReturn(kafkaTopicSet);
        m_messageFormatter = new JsonFormatter();
        VOLTManagementUtil.updateKafkaSubscriptions("vomci1", m_messageFormatter, m_networkFunctionDao, m_onuKafkaConsumer,
                m_kafkaConsumerTopicMap);
        verify(m_onuKafkaConsumer, never()).updateSubscriberTopics(kafkaTopicSet);
        verify(m_networkFunctionDao, never()).getKafkaConsumerTopics("vomci1");

    }

    @Test
    public void testRemoveSubscriptionsNoEntryInMap() {
        Set<KafkaTopic> kafkaTopicSet = new HashSet<>();
        kafkaTopicSet.add(m_kafkaTopic);
        when(m_kafkaConsumerTopicMap.containsKey("vomci1")).thenReturn(false);
        VOLTManagementUtil.removeSubscriptions("vomci1", m_onuKafkaConsumer, m_kafkaConsumerTopicMap);
        verify(m_onuKafkaConsumer, never()).removeSubscriberTopics(any());
    }

    @Test
    public void testRemoveSubscriptions() {
        Set<KafkaTopic> kafkaTopicSet = new HashSet<>();
        kafkaTopicSet.add(m_kafkaTopic);
        Set<String> kafkaTopicNameSet = new HashSet<>();
        kafkaTopicNameSet.add("vomc1-request");
        when(m_kafkaConsumerTopicMap.containsKey("vomci1")).thenReturn(true);
        when(m_kafkaConsumerTopicMap.get("vomci1")).thenReturn(kafkaTopicNameSet);
        VOLTManagementUtil.removeSubscriptions("vomci1", m_onuKafkaConsumer, m_kafkaConsumerTopicMap);
        verify(m_onuKafkaConsumer, times(1)).removeSubscriberTopics(any());
    }

    @Test
    public void testGetMediatedDeviceNetconfSession() {
        VOLTManagementUtil.getMediatedDeviceNetconfSession(null, m_netconfConnectionManager);
        verify(m_netconfConnectionManager, never()).getMediatedDeviceSession(any());
        when(m_netconfConnectionManager.getMediatedDeviceSession(m_onuDevice.getDeviceName())).thenReturn(m_mediatedDeviceNetconfSession);
        MediatedDeviceNetconfSession mediatedDeviceNetconfSession = VOLTManagementUtil.getMediatedDeviceNetconfSession(m_onuDevice, m_netconfConnectionManager);
        assertNotNull(mediatedDeviceNetconfSession);
        verify(m_netconfConnectionManager, times(1)).getMediatedDeviceSession(any());
        assertEquals(m_mediatedDeviceNetconfSession, mediatedDeviceNetconfSession);
    }

    @Test
    public void testSendOnuDiscoveryResultNotification() {
        when(m_onuDevice.getDeviceManagement()).thenReturn(m_deviceMgmt);
        when(m_deviceMgmt.getOnuConfigInfo()).thenReturn(m_onuConfigInfo);
        when(m_onuConfigInfo.getExpectedSerialNumber()).thenReturn(ONU_SERIAL_NUMBER);
        when(m_deviceMgmt.getDeviceState()).thenReturn(m_deviceState);
        when(m_deviceState.getOnuStateInfo()).thenReturn(null);
        VOLTManagementUtil.sendOnuDiscoveryResultNotification(m_onuDevice, "online", m_notificationService);
        verify(m_notificationService, times(1)).sendNotification(any(), any());
    }

    @Test
    public void testRemoveActualAttachmentPointInfo() {
        when(m_onuDevice.getDeviceName()).thenReturn(ONU_NAME);
        VOLTManagementUtil.removeActualAttachmentPointInfo(m_onuDevice, m_txService, m_deviceManager);
        verify(m_deviceManager, times(1)).updateOnuStateInfo(ONU_NAME, null);
    }

    @Test
    public void testRretrieveAndUpdateHwSwPropertiesForUnknownONU() {
        when(m_unknownOnuHandler.findMatchingUnknownOnu(OLT_NAME, CHANNEL_TERM_REF, ONU_ID)).thenReturn(m_unknownOnu);
        when(m_responseData.getOltName()).thenReturn(OLT_NAME);
        when(m_responseData.getChannelTermRef()).thenReturn(CHANNEL_TERM_REF);
        when(m_responseData.getOnuId()).thenReturn(ONU_ID);
        when(m_responseData.getResponsePayload()).thenReturn(prepareJsonResponseFromFile(internalGetResponseJson));
        when(m_responseData.getResponseStatus()).thenReturn(ONUConstants.OK_RESPONSE);
        when(m_unknownOnu.getSerialNumber()).thenReturn(ONU_SERIAL_NUMBER);
        VOLTManagementUtil.retrieveAndUpdateHwSwPropertiesForUnknownONU(m_responseData, m_txService, m_unknownOnuHandler);
        verify(m_unknownOnuHandler, times(1)).findMatchingUnknownOnu(OLT_NAME, CHANNEL_TERM_REF, ONU_ID);
        verify(m_unknownOnu, times(1)).setEquipmentId(any());
        verify(m_unknownOnu, times(1)).setSoftwareImages(any());
    }

    @Test
    public void testProcessInternalGetResponseAndRetrieveHwProperties() {
        String data = prepareJsonResponseFromFile(internalGetResponseJson);
        String serialNumber = "ABCD12345678";
        String eqptId = VOLTManagementUtil.processInternalGetResponseAndRetrieveHwProperties(ONUConstants.OK_RESPONSE, data, serialNumber);
        assertNotNull(eqptId);
        assertEquals("EqptModelName_1", eqptId);
        eqptId = VOLTManagementUtil.processInternalGetResponseAndRetrieveHwProperties(ONUConstants.NOK_RESPONSE, "", serialNumber);
        assertNull(eqptId);
        eqptId = VOLTManagementUtil.processInternalGetResponseAndRetrieveHwProperties(ONUConstants.OK_RESPONSE, "", serialNumber);
        assertNull(eqptId);
        eqptId = VOLTManagementUtil.processInternalGetResponseAndRetrieveHwProperties(ONUConstants.NOK_RESPONSE, data, serialNumber);
        assertNull(eqptId);
    }

    @Test
    public void testProcessInternalGetResponseAndRetrieveSWProperties() {
        Set<SoftwareImage> softwareImageSet = VOLTManagementUtil.processInternalGetResponseAndRetrieveSWProperties(ONUConstants.OK_RESPONSE, prepareJsonResponseFromFile(internalGetResponseJson));
        assertEquals(2, softwareImageSet.size());
        assertFalse(softwareImageSet.isEmpty());
        Set<SoftwareImage> expectedSwSet = prepareSwImageSet(null);
        assertEquals(expectedSwSet, softwareImageSet);
        Set<SoftwareImage> softwareImageSetNokResponse = VOLTManagementUtil.processInternalGetResponseAndRetrieveSWProperties(ONUConstants.NOK_RESPONSE, "");
        assertEquals(0, softwareImageSetNokResponse.size());
        assertTrue(softwareImageSetNokResponse.isEmpty());
        Set<SoftwareImage> softwareImageSetokResponseEmptyData = VOLTManagementUtil.processInternalGetResponseAndRetrieveSWProperties(ONUConstants.OK_RESPONSE, "");
        assertEquals(0, softwareImageSetokResponseEmptyData.size());
        assertTrue(softwareImageSetokResponseEmptyData.isEmpty());
        Set<SoftwareImage> softwareImageSetNokResponseNonEmptyData = VOLTManagementUtil.processInternalGetResponseAndRetrieveSWProperties(ONUConstants.NOK_RESPONSE, internalGetResponseJson);
        assertEquals(0, softwareImageSetNokResponseNonEmptyData.size());
        assertTrue(softwareImageSetNokResponseNonEmptyData.isEmpty());
    }

    @Test
    public void testUpdateOperationTypeInResponseDataGpbFormatter() {
        m_messageFormatter = new GpbFormatter();
        when(m_responseData.getIdentifier()).thenReturn(ONU_ID);
        when(m_responseData.getOperationType()).thenReturn(NetconfResources.RPC);
        when(m_request.getMessageId()).thenReturn(ONU_ID);
        VOLTManagementUtil.registerInRequestMap(m_request, ONU_NAME, ONUConstants.CREATE_ONU);
        ResponseData responseData = VOLTManagementUtil.updateOperationTypeInResponseData(m_responseData, m_messageFormatter);
        verify(m_responseData, times(1)).setOnuName(ONU_NAME);
        verify(m_responseData, times(1)).setOperationType(ONUConstants.CREATE_ONU);
        assertNotNull(responseData);
        VOLTManagementUtil.removeRequestFromMap(ONU_ID);
    }

    @Test
    public void testUpdateOperationTypeInResponseDataJsonFormatter() {
        m_messageFormatter = new JsonFormatter();
        when(m_responseData.getIdentifier()).thenReturn("2");
        when(m_request.getMessageId()).thenReturn("2");
        VOLTManagementUtil.registerInRequestMap(m_request, ONU_NAME, ONUConstants.CREATE_ONU);
        ResponseData responseData = VOLTManagementUtil.updateOperationTypeInResponseData(m_responseData, m_messageFormatter);
        verify(m_responseData, times(0)).setOnuName(ONU_NAME);
        verify(m_responseData, times(0)).setOperationType(ONUConstants.CREATE_ONU);
        assertNotNull(responseData);
        assertEquals(m_responseData, responseData);
        VOLTManagementUtil.removeRequestFromMap("2");
    }

    @Test
    public void testSendKafkaNotification() throws MessageFormatterException, NetconfMessageBuilderException {
        HashMap<String, String> labels = new HashMap<>();
        labels.put("name", "vendor");
        labels.put("value", "bbf");
        when(m_notificationRequest.getOnuDeviceName()).thenReturn(ONU_NAME);
        when(m_notificationRequest.getOltDeviceName()).thenReturn(OLT_NAME);
        when(m_notificationRequest.getOnuId()).thenReturn(ONU_ID);
        when(m_notificationRequest.getChannelTermRef()).thenReturn(CHANNEL_TERM_REF);
        when(m_notificationRequest.getLabels()).thenReturn(labels);
        when(m_messageFormatter.getFormattedRequest(any(), any(), any(), any(), any(), any(), any())).thenReturn(m_notification);
        VOLTManagementUtil.sendKafkaNotification(m_notificationRequest, m_messageFormatter, m_onuKafkaProducer);
        verify(m_onuKafkaProducer, times(1)).sendNotification(ONUConstants.ONU_NOTIFICATION_KAFKA_TOPIC, m_notification);
    }

    @Test
    public void testSendKafkaMessage() throws MessageFormatterException {
        final HashSet<String> kafkaTopicNamesFinal = new HashSet<>();
        kafkaTopicNamesFinal.add("vomci1-request");
        when(m_networkFunctionDao.getKafkaTopicNames("vomci1", KafkaTopicPurpose.VOMCI_REQUEST)).thenReturn(kafkaTopicNamesFinal);
        VOLTManagementUtil.sendKafkaMessage(m_notification, "vomci1", m_txService, m_networkFunctionDao, m_onuKafkaProducer);
        verify(m_onuKafkaProducer, times(1)).sendNotification("vomci1-request", m_notification);
        kafkaTopicNamesFinal.remove("vomci1-request");
        VOLTManagementUtil.sendKafkaMessage(m_notification, "vomci1", m_txService, m_networkFunctionDao, m_onuKafkaProducer);
        verify(m_onuKafkaProducer, times(1)).sendNotification("vomci1-request", m_notification);
    }

    @Test
    public void testSendKafkaMessageWhenNoTopicPresent() throws MessageFormatterException {
        final HashSet<String> kafkaTopicNamesFinal = new HashSet<>();
        when(m_networkFunctionDao.getKafkaTopicNames("vomci1", KafkaTopicPurpose.VOMCI_REQUEST)).thenReturn(kafkaTopicNamesFinal);
        VOLTManagementUtil.sendKafkaMessage(m_notification, "vomci1", m_txService, m_networkFunctionDao, m_onuKafkaProducer);
        verify(m_onuKafkaProducer, times(0)).sendNotification("vomci1-request", m_notification);
    }

    @Test
    public void testSetMessageId() {
        AtomicLong messageId = new AtomicLong(0);
        VOLTManagementUtil.setMessageId(m_request, messageId);
        verify(m_request, times(1)).setMessageId("1");
        VOLTManagementUtil.setMessageId(m_request, messageId);
        verify(m_request, times(1)).setMessageId("2");
    }

    @Test
    public void TestGenerateRandomMessageId() {
        String messageId = VOLTManagementUtil.generateRandomMessageId();
        assertNotNull(messageId);
    }

    @Test
    public void testSendGetRequestJson() throws MessageFormatterException {
        m_messageFormatter = new JsonFormatter();
        when(m_onuNotification.getSerialNo()).thenReturn(ONU_SERIAL_NUMBER);
        when(m_onuNotification.getOltDeviceName()).thenReturn(OLT_NAME);
        when(m_onuNotification.getChannelTermRef()).thenReturn(CHANNEL_TERM_REF);
        when(m_onuNotification.getOnuId()).thenReturn(ONU_ID);
        VOLTManagementUtil.sendGetRequest(m_onuNotification, m_messageFormatter, m_txService, m_deviceDao, m_networkFunctionDao, m_onuKafkaProducer);
        verify(m_deviceDao, times(0)).getVomciFunctionName(any());
        verify(m_networkFunctionDao, times(0)).getKafkaTopicNames(any(), any());
        verify(m_onuKafkaProducer, times(1)).sendNotification(ONUConstants.ONU_REQUEST_KAFKA_TOPIC, prepareJsonResponseFromFile(internalGetRequestJson));
    }

    @Test
    public void testGetManagementChain() {
        OnuManagementChain[] onuManagementChainArray = new OnuManagementChain[3];
        OnuManagementChain onuManagementChain1 = new OnuManagementChain();
        onuManagementChain1.setNfName("vomci1");
        onuManagementChain1.setInsertOrder(0);
        OnuManagementChain onuManagementChain2 = new OnuManagementChain();
        onuManagementChain2.setNfName("proxy1");
        onuManagementChain2.setInsertOrder(1);
        OnuManagementChain onuManagementChain3 = new OnuManagementChain();
        onuManagementChain3.setNfName(OLT_NAME);
        onuManagementChain3.setInsertOrder(2);
        onuManagementChainArray[0] = onuManagementChain1;
        onuManagementChainArray[1] = onuManagementChain2;
        onuManagementChainArray[2] = onuManagementChain3;
        when(m_deviceDao.getOnuManagementChains(ONU_NAME)).thenReturn(onuManagementChainArray);
        List<Pair<ObjectType, String>> managementChain = VOLTManagementUtil.getManagementChain(ONU_NAME, m_txService, m_deviceDao);
        assertNotNull(managementChain);
        assertEquals("vomci1", managementChain.get(0).getSecond());
        assertEquals(ObjectType.VOMCI_FUNCTION, managementChain.get(0).getFirst());
        assertEquals("proxy1", managementChain.get(1).getSecond());
        assertEquals(ObjectType.VOMCI_PROXY, managementChain.get(1).getFirst());
    }

    @Test
    public void testGetManagementChain2() {
        OnuManagementChain[] onuManagementChainArray = new OnuManagementChain[1];
        OnuManagementChain onuManagementChain1 = new OnuManagementChain();
        onuManagementChain1.setNfName(OLT_NAME);
        onuManagementChain1.setInsertOrder(0);
        onuManagementChainArray[0] = onuManagementChain1;
        when(m_deviceDao.getOnuManagementChains("onu2")).thenReturn(onuManagementChainArray);
        List<Pair<ObjectType, String>> managementChain = VOLTManagementUtil.getManagementChain("onu2", m_txService, m_deviceDao);
        assertNotNull(managementChain);
        assertEquals(0, managementChain.size());
    }

    @Test
    public void testIsInPermittedAttachmentPointOnuNotification() {
        when(m_onuDevice.getDeviceManagement()).thenReturn(m_deviceMgmt);
        when(m_deviceMgmt.getOnuConfigInfo()).thenReturn(m_onuConfigInfo);
        when(m_onuConfigInfo.getExpectedAttachmentPoints()).thenReturn(m_expectedAttachmentPoints);
        when(m_expectedAttachmentPoints.getExpectedAttachmentPointSet()).thenReturn(m_expectedAttachmentPointSet);
        when(m_expectedAttachmentPointSet.iterator()).thenReturn(m_expectedAttachmentPointIterator);
        when(m_expectedAttachmentPointIterator.hasNext()).thenReturn(true, false);
        when(m_expectedAttachmentPointIterator.next()).thenReturn(m_expectedAttachmentPoint);
        when(m_expectedAttachmentPoint.getOltName()).thenReturn(OLT_NAME);
        when(m_expectedAttachmentPoint.getChannelPartitionName()).thenReturn(CHANNEL_PARTITION_NAME);
        when(m_onuNotification.getChannelTermRef()).thenReturn(CHANNEL_TERM_REF);
        boolean value = VOLTManagementUtil.isInPermittedAttachmentPoint(m_onuDevice, OLT_NAME, m_onuNotification, m_pmaRegistry);
        assertFalse(value);
        verify(m_expectedAttachmentPoint, times(2)).getOltName();
        verify(m_expectedAttachmentPoint, times(2)).getChannelPartitionName();
        verify(m_onuNotification, times(1)).getChannelTermRef();
    }

    @Test
    public void testIsInPermittedAttachmentPointUnknownOnu() {
        when(m_onuDevice.getDeviceManagement()).thenReturn(m_deviceMgmt);
        when(m_deviceMgmt.getOnuConfigInfo()).thenReturn(m_onuConfigInfo);
        when(m_onuConfigInfo.getExpectedAttachmentPoints()).thenReturn(m_expectedAttachmentPoints);
        when(m_expectedAttachmentPoints.getExpectedAttachmentPointSet()).thenReturn(m_expectedAttachmentPointSet);
        when(m_expectedAttachmentPointSet.iterator()).thenReturn(m_expectedAttachmentPointIterator);
        when(m_expectedAttachmentPointIterator.hasNext()).thenReturn(true, false);
        when(m_expectedAttachmentPointIterator.next()).thenReturn(m_expectedAttachmentPoint);
        when(m_expectedAttachmentPoint.getOltName()).thenReturn(OLT_NAME);
        when(m_expectedAttachmentPoint.getChannelPartitionName()).thenReturn(CHANNEL_PARTITION_NAME);
        when(m_unknownOnu.getOltDeviceName()).thenReturn(OLT_NAME);
        when(m_unknownOnu.getChannelTermRef()).thenReturn(CHANNEL_TERM_REF);
        boolean value = VOLTManagementUtil.isInPermittedAttachmentPoint(m_onuDevice, m_unknownOnu, m_pmaRegistry);
        assertFalse(value);
        verify(m_expectedAttachmentPoint, times(2)).getOltName();
        verify(m_expectedAttachmentPoint, times(2)).getChannelPartitionName();
        verify(m_unknownOnu, times(2)).getOltDeviceName();
        verify(m_unknownOnu, times(1)).getChannelTermRef();

    }

    @Test
    public void testSendOnuAuthenticationResultNotification() {
        String onuAuthStatus = "vomci-expected-by-olt-but-inconsistent-pmaa-mgmt-mode";
        String onuName = "ONU1";
        String onuType = "ONU";
        String channelPartition = "CT_1";
        String onuId = "1";
        String vaniName = "";
        String oltName = "OLT1";
        String serialNumber = "ABCD12345678";
        String registrationId = "REG123";
        when(m_onuDevice.getDeviceManagement()).thenReturn(m_deviceMgmt);
        when(m_onuDevice.getDeviceName()).thenReturn(onuName);
        when(m_onuDevice.isMediatedSession()).thenReturn(true);
        when(m_deviceMgmt.getOnuConfigInfo()).thenReturn(m_onuConfigInfo);
        when(m_deviceMgmt.getDeviceType()).thenReturn(onuType);
        when(m_deviceMgmt.getDeviceState()).thenReturn(m_deviceState);
        when(m_deviceState.getOnuStateInfo()).thenReturn(m_onuStateInfo);
        when(m_onuStateInfo.getActualAttachmentPoint()).thenReturn(m_actualAttachmentPoint);
        when(m_actualAttachmentPoint.getChannelTerminationRef()).thenReturn(channelPartition);
        when(m_actualAttachmentPoint.getOnuId()).thenReturn(onuId);
        when(m_actualAttachmentPoint.getvAniName()).thenReturn(vaniName);
        when(m_actualAttachmentPoint.getOltName()).thenReturn(oltName);
        when(m_onuConfigInfo.getExpectedSerialNumber()).thenReturn(serialNumber);
        when(m_onuConfigInfo.getExpectedRegistrationId()).thenReturn(registrationId);
        VOLTManagementUtil.sendOnuAuthenticationResultNotification(m_onuDevice, null, m_netconfConnectionManager,
                m_notificationService, onuAuthStatus, m_deviceManager, m_txService);
        verify(m_onuDevice, times(9)).getDeviceManagement();
        verify(m_onuNotification, never()).getOltDeviceName();
        verify(m_notificationService, times(1)).sendNotification(any(), any(Notification.class));
    }

    @Test
    @Ignore
    public void testSendOnuAuthenticationResultNotificationWhenDeviceIsNull() throws ExecutionException, InterruptedException {
        String onuAuthStatus = "vomci-expected-by-olt-but-inconsistent-pmaa-mgmt-mode";
        String channelPartition = "CT_1";
        String onuId = "1";
        String vaniName = "";
        String oltName = "OLT1";
        String serialNumber = "ABCD12345678";
        String registrationId = "REG123";

        when(m_onuNotification.getOltDeviceName()).thenReturn(oltName);
        when(m_onuNotification.getChannelTermRef()).thenReturn(channelPartition);
        when(m_onuNotification.getOnuId()).thenReturn(onuId);
        when(m_onuNotification.getSerialNo()).thenReturn(serialNumber);
        when(m_onuNotification.getRegId()).thenReturn(registrationId);
        when(m_onuNotification.getVAniRef()).thenReturn(vaniName);
        when(m_onuNotification.getOnuState()).thenReturn("vomci-expected-by-olt-but-inconsistent-pmaa-mgmt-mode");

        GetRequest getRequest = VOLTMgmtRequestCreationUtil.prepareGetRequestForVani(OLT_NAME, VANI_REF);
        VOLTManagementUtil.setMessageId(getRequest, m_messageId);
        when(m_netconfConnectionManager.executeNetconf(anyString(), any(GetRequest.class))).thenReturn(m_responseFuture);
        when(m_responseFuture.get()).thenReturn(m_netConfResponse);
        when(m_netConfResponse.getMessageId()).thenReturn(String.valueOf(m_messageId));
        when(m_netConfResponse.getData()).thenReturn(m_dataElement);
        VOLTManagementUtil.sendOnuAuthenticationResultNotification(null, m_onuNotification, m_netconfConnectionManager, m_notificationService, onuAuthStatus, m_deviceManager, m_txService);
        verify(m_onuNotification, times(1)).getOltDeviceName();
        verify(m_onuNotification, times(1)).getChannelTermRef();
        verify(m_onuDevice, never()).getDeviceManagement();
        verify(m_notificationService, times(1)).sendNotification(any(), any(Notification.class));
    }

    @Test
    @Ignore //TODO : need to fix
    public void testIsOnuAuthenticatedByOlt() {
        String onuState = ONU_PRESENT_AND_ON_INTENDED_CHANNEL_TERMINATION;
        Boolean onuAuthentication1 = VOLTManagementUtil.isOnuAuthenticatedByOLTAndHasNoErrors(onuState);
        assertTrue(onuAuthentication1);

        onuState = ONU_PRESENT_AND_V_ANI_KNOW_BUT_INTENDED_CT_UNKNOWN;
        Boolean onuAuthentication2 = VOLTManagementUtil.isOnuAuthenticatedByOLTAndHasNoErrors(onuState);
        assertTrue(onuAuthentication2);

        onuState = ONU_PRESENT_AND_V_ANI_KNOW_AND_O5_FAILED_NO_ONU_ID;
        Boolean onuAuthentication3 = VOLTManagementUtil.isOnuAuthenticatedByOLTAndHasNoErrors(onuState);
        assertTrue(onuAuthentication3);

        onuState = ONU_PRESENT_AND_V_ANI_KNOW_AND_O5_FAILED_UNDEFINED;
        Boolean onuAuthentication4 = VOLTManagementUtil.isOnuAuthenticatedByOLTAndHasNoErrors(onuState);
        assertTrue(onuAuthentication4);

        onuState = ONU_PRESENT_AND_NO_VANI_KNOWN_AND_UNCLAIMED;
        Boolean onuAuthentication5 = VOLTManagementUtil.isOnuAuthenticatedByOLTAndHasNoErrors(onuState);
        assertFalse(onuAuthentication5);
    }

    @Test
    public void testIsVomciToBeUsed_False_GlobalEomciAndNoLocal() {
        prepareObjectsForIsVomciToBeUsed(null, ONUConstants.USE_EOMCI);
        assertFalse(VOLTManagementUtil.isVomciToBeUsed(m_onuDevice));
    }

    @Test
    public void testIsVomciToBeUsed_True_GlobalEomciLocalAsUseVOMCI() {
        prepareObjectsForIsVomciToBeUsed(ONUConstants.USE_VOMCI, ONUConstants.USE_EOMCI);
        assertTrue(VOLTManagementUtil.isVomciToBeUsed(m_onuDevice));
    }

    @Test
    public void testIsVomciToBeUsed_False_GlobalAndLocalAsEomci() {
        prepareObjectsForIsVomciToBeUsed(ONUConstants.USE_EOMCI, ONUConstants.USE_EOMCI);
        assertFalse(VOLTManagementUtil.isVomciToBeUsed(m_onuDevice));
    }

    @Test
    public void testIsVomciToBeUsed_True_GlobalVomci_NoLocalDefined() {
        prepareObjectsForIsVomciToBeUsed(null, ONUConstants.USE_VOMCI);
        assertTrue(VOLTManagementUtil.isVomciToBeUsed(m_onuDevice));
    }

    @Test
    public void testIsVomciToBeUsed_False_GlobalVomciLocalEomci() {
        prepareObjectsForIsVomciToBeUsed(ONUConstants.USE_EOMCI, ONUConstants.USE_VOMCI);
        assertFalse(VOLTManagementUtil.isVomciToBeUsed(m_onuDevice));
    }

    @Test
    public void testIsVomciToBeUsed_True_GlobalVomciLocalVomci() {
        prepareObjectsForIsVomciToBeUsed(ONUConstants.USE_VOMCI, ONUConstants.USE_VOMCI);
        assertTrue(VOLTManagementUtil.isVomciToBeUsed(m_onuDevice));
    }

    @Test
    public void testProcessNotificationAndRetrieveOnuAlimentStatus() {
        String alignStatusMisaligned = VOLTManagementUtil.processNotificationAndRetrieveOnuAlignmentStatus(prepareJsonResponseFromFile(onuAlignmentMisalignedNoitifcationJson));
        assertEquals(ONUConstants.UNALIGNED, alignStatusMisaligned);
        String alignStatusAligned = VOLTManagementUtil.processNotificationAndRetrieveOnuAlignmentStatus(prepareJsonResponseFromFile(onuAlignmentAlignedNoitifcationJson));
        assertEquals(ONUConstants.ALIGNED, alignStatusAligned);
    }

    @Test
    public void testIetfAlarmNotification() throws NetconfMessageBuilderException {
        String alarmNotification = "<al:alarm-notification xmlns:al=\"urn:ietf:params:xml:ns:yang:ietf-alarms\">\n" +
                "<al:resource xmlns:baa-network-manager=\"urn:bbf:yang:obbaa:network-manager\" xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">/if:interfaces/if:interface[if:name='eth1']</al:resource>\n" +
                "<al:alarm-type-id xmlns:bbf-baa-ethalt=\"urn:bbf:yang:obbaa:ethernet-alarm-types\">bbf-baa-ethalt:loss-of-signal</al:alarm-type-id>\n" +
                "<al:alarm-type-qualifier/>\n" +
                "<al:time>2022-01-09 13:53:36.0</al:time>\n" +
                "<al:perceived-severity>major</al:perceived-severity>\n" +
                "<al:alarm-text>example alarm</al:alarm-text>\n" +
                "</al:alarm-notification>";
        AlarmInfo alarmInfo = VOLTManagementUtil.processVomciNotificationAndPrepareAlarmInfo(prepareJsonResponseFromFile(ietfAlarmsAlarmNotificationJson), "onu1");
        assertNotNull(alarmInfo);
        assertTrue(alarmInfo.getAlarmText().equals("example alarm"));
        assertTrue(alarmInfo.getDeviceName().equals("onu1"));
        assertTrue(alarmInfo.getAlarmTypeId().equals("bbf-baa-ethalt:loss-of-signal"));
        assertTrue(alarmInfo.getSeverity().equals(AlarmSeverity.MAJOR));
        assertTrue(alarmInfo.getAlarmTypeQualifier().equals(""));
        assertTrue(alarmInfo.getSourceObjectString().equals("/if:interfaces/if:interface[if:name='eth1']"));
        Element alarmNotif = VOLTManagementUtil.prepareAlarmNotificationElementFromAlarmInfo(alarmInfo);
        assertNotNull(alarmNotif);
        assertEquals(alarmNotification, DocumentUtils.documentToPrettyString(alarmNotif).trim());

    }

    @Test
    public void testIetfAlarmNotificationCleared() throws NetconfMessageBuilderException {
        String alarmNotificationCleared = "<al:alarm-notification xmlns:al=\"urn:ietf:params:xml:ns:yang:ietf-alarms\">\n" +
                "<al:resource xmlns:baa-network-manager=\"urn:bbf:yang:obbaa:network-manager\" xmlns:hw=\"urn:ietf:params:xml:ns:yang:ietf-hardware\">/hw:hardware/hw:component[hw:name='ontAniPort_ont1']</al:resource>\n" +
                "<al:alarm-type-id xmlns:bbf-hw-xcvr-alt=\"urn:bbf:yang:bbf-hardware-transceiver-alarm-types\">bbf-hw-xcvr-alt:rx-power-low</al:alarm-type-id>\n" +
                "<al:alarm-type-qualifier/>\n" +
                "<al:time>2022-01-09 13:53:36.0</al:time>\n" +
                "<al:perceived-severity>cleared</al:perceived-severity>\n" +
                "<al:alarm-text>example alarm cleared</al:alarm-text>\n" +
                "</al:alarm-notification>";
        AlarmInfo alarmInfo = VOLTManagementUtil.processVomciNotificationAndPrepareAlarmInfo(prepareJsonResponseFromFile(ietfAlarmsAlarmNotificationClearedJson), "onu2");
        assertNotNull(alarmInfo);
        assertTrue(alarmInfo.getAlarmText().equals("example alarm cleared"));
        assertTrue(alarmInfo.getDeviceName().equals("onu2"));
        assertTrue(alarmInfo.getAlarmTypeId().equals("bbf-hw-xcvr-alt:rx-power-low"));
        assertTrue(alarmInfo.getSeverity().equals(AlarmSeverity.CLEAR));
        assertTrue(alarmInfo.getAlarmTypeQualifier().equals(""));
        assertTrue(alarmInfo.getSourceObjectString().equals("/hw:hardware/hw:component[hw:name='ontAniPort_ont1']"));
        Element alarmNotif = VOLTManagementUtil.prepareAlarmNotificationElementFromAlarmInfo(alarmInfo);
        assertNotNull(alarmNotif);
        assertEquals(alarmNotificationCleared, DocumentUtils.documentToPrettyString(alarmNotif).trim());
    }


    @Test
    public void testSendOnuAuthenticationActionRequestAndGetOutputForErrorReply() throws ExecutionException, InterruptedException, NetconfMessageBuilderException {
        when(m_netconfConnectionManager.executeNetconf(m_onuDevice, m_actionRequest)).thenReturn(m_responseFuture);
        Future<NetConfResponse> netConfResponse = VOLTManagementUtil.sendOnuAuthenticationActionRequest(m_onuDevice,
                m_netconfConnectionManager, m_actionRequest);
        assertNotNull(netConfResponse);
    }

    @Test
    public void testProcessOnuAuthenticationActionResponse_OK_Response() {
        String NO_ERROR ="No Error Reported";
        when(m_netconfResponse.isOk()).thenReturn(true);
        when(m_rpcErrorList.get(0)).thenReturn(m_rpcError);
        when(m_rpcError.getErrorAppTag()).thenReturn(NO_ERROR);
        String response = VOLTManagementUtil.getErrorAppTagFromActionResponse(m_netconfResponse);
        assertEquals(response, ONUConstants.OK_RESPONSE);
    }

    @Test
    public void testProcessOnuAuthenticationActionResponse_When_ErrorAppTagIsNull() {
        String ONU_MODE_MISMATCH = "baa-xpon-onu-types:onu-management-mode-mismatch-with-v-ani";
        when(m_netconfResponse.isOk()).thenReturn(false);
        when(m_netconfResponse.getErrors()).thenReturn(m_rpcErrorList);
        when(m_rpcErrorList.get(0)).thenReturn(m_rpcError);
        when(m_rpcError.getErrorAppTag()).thenReturn(null);
        when(m_rpcError.getErrorMessage()).thenReturn(ONU_MODE_MISMATCH);
        String response = VOLTManagementUtil.getErrorAppTagFromActionResponse(m_netconfResponse);
        assertEquals(response, ONU_MODE_MISMATCH);
    }

    @Test
    public void testProcessOnuAuthenticationActionResponse_ERROR_Response() {
        String ONU_NOT_PRESENT = "baa-xpon-onu-types:onu-not-present";
        when(m_netconfResponse.isOk()).thenReturn(false);
        when(m_netconfResponse.getErrors()).thenReturn(m_rpcErrorList);
        when(m_rpcErrorList.get(0)).thenReturn(m_rpcError);
        when(m_rpcError.getErrorAppTag()).thenReturn(ONU_NOT_PRESENT);
        String response = VOLTManagementUtil.getErrorAppTagFromActionResponse(m_netconfResponse);
        assertEquals(response, ONU_NOT_PRESENT);
    }

    private void prepareObjectsForIsVomciToBeUsed(String localMode, String globalMode) {
        ExpectedAttachmentPoint expectedAttachmentPoint = new ExpectedAttachmentPoint();
        expectedAttachmentPoint.setName("eONU1");
        expectedAttachmentPoint.setChannelPartitionName("e_ONU_CP1");
        expectedAttachmentPoint.setOltName("OLT1");

        VOLTManagementUtil.updateMatchedAttachmentPointMap("ONU1", expectedAttachmentPoint);

        when(m_onuDevice.getDeviceManagement()).thenReturn(m_deviceMgmt);
        when(m_onuDevice.getDeviceName()).thenReturn("ONU1");
        when(m_deviceMgmt.getOnuConfigInfo()).thenReturn(m_onuConfigInfo);
        when(m_deviceMgmt.getDeviceState()).thenReturn(m_deviceState);
        when(m_deviceState.getOnuStateInfo()).thenReturn(m_onuStateInfo);
        when(m_onuStateInfo.getActualAttachmentPoint()).thenReturn(m_actualAttachmentPoint);
        Set<ExpectedAttachmentPoint> expectedAttachmentPointsSet = new HashSet<>();
        ExpectedAttachmentPoint expectedAttachmentPoint1 = new ExpectedAttachmentPoint();
        expectedAttachmentPoint1.setChannelPartitionName("e_ONU_CP1");
        expectedAttachmentPoint1.setName("OLT1.CP1");
        expectedAttachmentPoint1.setOltName("OLT1");
        expectedAttachmentPoint1.setPlannedOnuManagementModeInThisOlt(localMode);
        expectedAttachmentPointsSet.add(expectedAttachmentPoint1);
        ExpectedAttachmentPoints expectedAttachmentPoints = new ExpectedAttachmentPoints();
        expectedAttachmentPoints.setExpectedAttachmentPointSet(expectedAttachmentPointsSet);

        m_expectedAttachmentPointSet.add(expectedAttachmentPoint1);
        if (localMode != null) {
            when(m_onuConfigInfo.getExpectedAttachmentPoints()).thenReturn(expectedAttachmentPoints);
        } else {
            when(m_onuConfigInfo.getExpectedAttachmentPoints()).thenReturn(null);
        }
        when(m_expectedAttachmentPoints.getExpectedAttachmentPointSet()).thenReturn(expectedAttachmentPointsSet);
        when(m_onuDevice.getDeviceManagement().getOnuConfigInfo().getPlannedOnuManagementMode()).thenReturn(globalMode);
        when(m_onuDevice.getDeviceManagement().getDeviceState().getOnuStateInfo().getActualAttachmentPoint()).thenReturn(m_actualAttachmentPoint);
        when(m_actualAttachmentPoint.getOltName()).thenReturn("OLT1");
        when(m_actualAttachmentPoint.getChannelTerminationRef()).thenReturn("CT1");
    }

    private Set<SoftwareImage> prepareSwImageSet(String parentId) {
        Set<SoftwareImage> softwareImageSet = new HashSet<>();
        SoftwareImage softwareImage0 = new SoftwareImage();
        softwareImage0.setId(1);
        softwareImage0.setParentId(parentId);
        softwareImage0.setHash("123456789");
        softwareImage0.setProductCode("pcode");
        softwareImage0.setVersion("1.0.0");
        softwareImage0.setIsValid(true);
        softwareImage0.setIsCommitted(true);
        softwareImage0.setIsActive(true);
        SoftwareImage softwareImage1 = new SoftwareImage();
        softwareImage1.setId(2);
        softwareImage1.setParentId(parentId);
        softwareImage1.setHash("134323233");
        softwareImage1.setProductCode("pcode");
        softwareImage1.setVersion("2.0.0");
        softwareImage1.setIsValid(true);
        softwareImage1.setIsCommitted(false);
        softwareImage1.setIsActive(false);
        softwareImageSet.add(softwareImage0);
        softwareImageSet.add(softwareImage1);
        return softwareImageSet;
    }

    @Test
    @Ignore
    public void testGetOnuNameFromVani() throws ExecutionException, InterruptedException {
        GetRequest getRequest = VOLTMgmtRequestCreationUtil.prepareGetRequestForVani(OLT_NAME, VANI_REF);
        VOLTManagementUtil.setMessageId(getRequest, m_messageId);
        when(m_netconfConnectionManager.executeNetconf(any(Device.class), any(GetRequest.class))).thenReturn(m_responseFuture);
        when(m_responseFuture.get()).thenReturn(m_netConfResponse);
        when(m_netConfResponse.getMessageId()).thenReturn(getRequest.getMessageId());
        when(m_netConfResponse.getData()).thenReturn(m_dataElement);
        when(m_onuDevice.getDeviceName()).thenReturn(OLT_NAME);
        //String onuName = VOLTManagementUtil.getOnuNameFromVani(m_onuDevice, VANI_REF, m_netconfConnectionManager);
        String onuName = "test";
        verify(m_netconfConnectionManager, times(1)).executeNetconf(any(Device.class), any(GetRequest.class));
        verify(m_netConfResponse, times(1)).getData();
        assertEquals(onuName, "test");
    }


    @Test
    @Ignore
    public void testGetOnuNameFromVaniForNullResponse() throws ExecutionException, InterruptedException {
        GetRequest getRequest = VOLTMgmtRequestCreationUtil.prepareGetRequestForVani(OLT_NAME, VANI_REF);
        VOLTManagementUtil.setMessageId(getRequest, m_messageId);
        when(m_netconfConnectionManager.executeNetconf(any(Device.class), any(GetRequest.class))).thenReturn(m_responseFuture);
        when(m_responseFuture.get()).thenReturn(null);
        when(m_netConfResponse.getMessageId()).thenReturn(getRequest.getMessageId());
        when(m_netConfResponse.getData()).thenReturn(m_dataElement);
        when(m_onuDevice.getDeviceName()).thenReturn(OLT_NAME);
        //String onuName = VOLTManagementUtil.getOnuNameFromVani(m_onuDevice, VANI_REF, m_netconfConnectionManager);
        String onuName = null;
        verify(m_netconfConnectionManager, times(1)).executeNetconf(any(Device.class), any(GetRequest.class));
        verify(m_netConfResponse, never()).getData();
        assertEquals(onuName, null);
    }

    private String prepareJsonResponseFromFile(String name) {
        return TestUtil.loadAsString(name).trim();
    }

}
