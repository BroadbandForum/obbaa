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

import static org.broadband_forum.obbaa.onu.ONUConstants.CREATE_ONU;
import static org.broadband_forum.obbaa.onu.ONUConstants.EOMCI_BEING_USED;
import static org.broadband_forum.obbaa.onu.ONUConstants.ONU_MGMT_MODE_MISMATCH_WITH_VANI;
import static org.broadband_forum.obbaa.onu.ONUConstants.ONU_PRESENT_AND_ON_INTENDED_CHANNEL_TERMINATION;
import static org.broadband_forum.obbaa.onu.ONUConstants.RELYING_ON_VOMCI;
import static org.broadband_forum.obbaa.onu.ONUConstants.UNABLE_TO_AUTHENTICATE_ONU;
import static org.broadband_forum.obbaa.onu.ONUConstants.USE_EOMCI;
import static org.broadband_forum.obbaa.onu.ONUConstants.USE_VOMCI;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.dmyang.dao.DeviceDao;
import org.broadband_forum.obbaa.dmyang.entities.ActualAttachmentPoint;
import org.broadband_forum.obbaa.dmyang.entities.ConnectionState;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants;
import org.broadband_forum.obbaa.dmyang.entities.DeviceMgmt;
import org.broadband_forum.obbaa.dmyang.entities.DeviceState;
import org.broadband_forum.obbaa.dmyang.entities.ExpectedAttachmentPoint;
import org.broadband_forum.obbaa.dmyang.entities.ExpectedAttachmentPoints;
import org.broadband_forum.obbaa.dmyang.entities.OnuConfigInfo;
import org.broadband_forum.obbaa.dmyang.entities.OnuManagementChain;
import org.broadband_forum.obbaa.dmyang.entities.OnuStateInfo;
import org.broadband_forum.obbaa.dmyang.entities.SoftwareImage;
import org.broadband_forum.obbaa.dmyang.entities.SoftwareImages;
import org.broadband_forum.obbaa.nbiadapter.netconf.NbiNetconfServerMessageListener;
import org.broadband_forum.obbaa.netconf.alarm.api.AlarmService;
import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfNotification;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.TxService;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.nf.dao.NetworkFunctionDao;
import org.broadband_forum.obbaa.nf.entities.KafkaTopic;
import org.broadband_forum.obbaa.nm.devicemanager.DeviceManager;
import org.broadband_forum.obbaa.onu.MediatedDeviceNetconfSession;
import org.broadband_forum.obbaa.onu.ONUConstants;
import org.broadband_forum.obbaa.onu.UnknownONUHandler;
import org.broadband_forum.obbaa.onu.entity.UnknownONU;
import org.broadband_forum.obbaa.onu.exception.MessageFormatterException;
import org.broadband_forum.obbaa.onu.kafka.consumer.OnuKafkaConsumer;
import org.broadband_forum.obbaa.onu.kafka.consumer.OnuKafkaConsumerGpb;
import org.broadband_forum.obbaa.onu.kafka.consumer.OnuKafkaConsumerJson;
import org.broadband_forum.obbaa.onu.kafka.producer.OnuKafkaProducerGpb;
import org.broadband_forum.obbaa.onu.kafka.producer.OnuKafkaProducerJson;
import org.broadband_forum.obbaa.onu.message.GpbFormatter;
import org.broadband_forum.obbaa.onu.message.HelloResponseData;
import org.broadband_forum.obbaa.onu.message.JsonFormatter;
import org.broadband_forum.obbaa.onu.message.MessageFormatter;
import org.broadband_forum.obbaa.onu.message.NetworkWideTag;
import org.broadband_forum.obbaa.onu.message.ObjectType;
import org.broadband_forum.obbaa.onu.message.ResponseData;
import org.broadband_forum.obbaa.onu.message.gpb.message.Body;
import org.broadband_forum.obbaa.onu.message.gpb.message.GetData;
import org.broadband_forum.obbaa.onu.message.gpb.message.GetDataResp;
import org.broadband_forum.obbaa.onu.message.gpb.message.Header;
import org.broadband_forum.obbaa.onu.message.gpb.message.Header.OBJECT_TYPE;
import org.broadband_forum.obbaa.onu.message.gpb.message.HelloResp;
import org.broadband_forum.obbaa.onu.message.gpb.message.Msg;
import org.broadband_forum.obbaa.onu.message.gpb.message.NFInformation;
import org.broadband_forum.obbaa.onu.message.gpb.message.Request;
import org.broadband_forum.obbaa.onu.message.gpb.message.Response;
import org.broadband_forum.obbaa.onu.notification.ONUNotification;
import org.broadband_forum.obbaa.onu.util.JsonUtil;
import org.broadband_forum.obbaa.onu.util.VOLTManagementUtil;
import org.broadband_forum.obbaa.onu.util.VOLTMgmtRequestCreationUtil;
import org.broadband_forum.obbaa.onu.util.VoltMFTestConstants;
import org.broadband_forum.obbaa.pma.PmaRegistry;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.protobuf.ByteString;

/**
 * <p>
 * Unit tests that tests CRUD of vOMCI based ONU and notification handling
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 22/07/2020.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({VOLTManagementImpl.class, MediatedDeviceNetconfSession.class, JsonUtil.class, QNameModule.class, Optional.class, OnuKafkaConsumer.class})
public class VOLTManagementImplTest {

    @Mock
    public static GetRequest m_getRequestOne;
    String getResponse = "{\"olt-name\":\"OLT1\",\"payload\":\"{\\\"identifier\\\":\\\"0\\\",\\\"operation\\\":\\\"get\\\",\\\"data\\\":{\\\"network-manager:device-management\\\":{\\\"device-state\\\":{\\\"bbf-obbaa-onu-management:onu-state-info\\\":{\\\"equipment-id\\\":\\\"test1\\\",\\\"software-images\\\":{\\\"software-image\\\":[{\\\"id\\\":\\\"1\\\",\\\"version\\\":\\\"1111\\\",\\\"is-committed\\\":\\\"false\\\",\\\"is-active\\\":\\\"false\\\",\\\"is-valid\\\":\\\"true\\\",\\\"product-code\\\":\\\"test1111\\\",\\\"hash\\\":\\\"11\\\"},{\\\"id\\\":\\\"0\\\",\\\"version\\\":\\\"0000\\\",\\\"is-committed\\\":\\\"true\\\",\\\"is-active\\\":\\\"true\\\",\\\"is-valid\\\":\\\"true\\\",\\\"product-code\\\":\\\"test\\\",\\\"hash\\\":\\\"00\\\"}]}}}}},\\\"status\\\":\\\"OK\\\"}\",\"onu-name\":\"onu\",\"channel-termination-ref\":\"channeltermination.1\",\"event\": \"response\",\"onu-id\": \"1\"}";
    String getResponseNOK = "{\"olt-name\":\"OLT1\",\"payload\":\"{\\\"identifier\\\":\\\"0\\\",\\\"operation\\\":\\\"get\\\",\\\"data\\\":{\\\"network-manager:device-management\\\":{\\\"device-state\\\":{\\\"bbf-obbaa-onu-management:onu-state-info\\\":{\\\"equipment-id\\\":\\\"test1\\\",\\\"software-images\\\":{\\\"software-image\\\":[{\\\"id\\\":\\\"1\\\",\\\"version\\\":\\\"1111\\\",\\\"is-committed\\\":\\\"false\\\",\\\"is-active\\\":\\\"false\\\",\\\"is-valid\\\":\\\"true\\\",\\\"product-code\\\":\\\"test1111\\\",\\\"hash\\\":\\\"11\\\"},{\\\"id\\\":\\\"0\\\",\\\"version\\\":\\\"0000\\\",\\\"is-committed\\\":\\\"true\\\",\\\"is-active\\\":\\\"true\\\",\\\"is-valid\\\":\\\"true\\\",\\\"product-code\\\":\\\"test\\\",\\\"hash\\\":\\\"00\\\"}]}}}}},\\\"status\\\":\\\"NOK\\\"}\",\"onu-name\":\"onu\",\"channel-termination-ref\":\"channeltermination.1\",\"event\": \"response\",\"onu-id\": \"1\"}";
    String emptyGetResponse = "{\"olt-name\":\"OLT1\",\"payload\":\"{\\\"identifier\\\":\\\"0\\\",\\\"operation\\\":\\\"get\\\",\\\"data\\\":{\\\"network-manager:device-management\\\":{\\\"device-state\\\":{\\\"bbf-obbaa-onu-management:onu-state-info\\\":{\\\"software-images\\\":{\\\"software-image\\\":[]}}}}},\\\"status\\\":\\\"OK\\\"}\",\"onu-name\":\"onu\",\"channel-termination-ref\":\"channeltermination.1\",\"event\": \"response\",\"onu-id\": \"1\"}";
    @Mock
    KafkaTopic m_kafkaTopic;
    @Mock
    private Device m_vonuDevice;
    @Mock
    private Device m_oltDevice;
    @Mock
    private AlarmService m_alarmService;
    @Mock
    private UnknownONUHandler m_unknownOnuHandler;
    @Mock
    private OnuKafkaProducerJson m_kafkaProducerJson;
    @Mock
    private OnuKafkaProducerGpb m_kafkaProducerGpb;
    @Mock
    private NetconfConnectionManager m_connectionManager;
    @Mock
    private ConnectionState m_connectionState;
    @Mock
    private ModelNodeDataStoreManager m_modelNodeDSM;
    @Mock
    private NotificationService m_notificationService;
    @Mock
    private AdapterManager m_adapterManager;
    @Mock
    private DeviceManager m_deviceManager;
    @Mock
    private DeviceMgmt m_deviceMgmt;
    @Mock
    private DeviceState m_deviceState;
    @Mock
    private PmaRegistry m_pmaRegistry;
    @Mock
    private OnuConfigInfo m_onuConfigInfo;
    @Mock
    private ExpectedAttachmentPoint m_attachmentPoint;
    @Mock
    private SchemaRegistry m_schemaRegistry;
    @Mock
    private OnuStateInfo m_onuStateInfo;
    @Mock
    private NetconfFilter m_filter;
    @Mock
    private ActualAttachmentPoint m_actualAttachmentPoint;
    @Mock
    Iterator<ExpectedAttachmentPoint> m_expectedAttachmentPointIterator;
    @Mock
    private ExpectedAttachmentPoint m_expectedAttachmentPoint;
    @Mock
    private ExpectedAttachmentPoints m_expectedAttachmentPoints;
    @Mock
    private Set<ExpectedAttachmentPoint> m_expectedAttachmentPointSet;
    private ThreadPoolExecutor m_kafkaCommunicationPool;
    private TxService m_txService;
    private VOLTManagementImpl m_voltManagement;
    private String deviceName = VoltMFTestConstants.ONU;
    private UnknownONU m_unknownOnu;
    private SoftwareImages m_softwareImages;
    private MessageFormatter m_messageFormatter;
    @Mock
    private NetworkFunctionDao m_networkFunctionDao;
    @Mock
    private DeviceDao m_deviceDao;
    @Mock
    private MediatedDeviceNetconfSession m_mediatedDeviceNCsession;
    @Mock
    private Set<KafkaTopic> m_kafkaTopicSet;
    @Mock
    private ResponseData m_responseData;
    @Mock
    private Object m_notificationResponse;
    @Mock
    private GpbFormatter m_gpbFormatter;
    public static final String GET_RESPONSE = "/get-response.xml";
    public static final String GET_REQUEST = "/internal-get-request-filter.xml";
    private static final AtomicLong m_messageId = new AtomicLong(1000);
    @Mock
    Future<NetConfResponse> m_responseFuture;
    @Mock
    NetConfResponse m_netConfResponse;
    @Mock
    private ONUNotification m_onuNotification;
    @Mock
    List<NetconfRpcError> m_netconfRpcErrorList;
    @Mock
    NetconfRpcError m_netconfRpcError;
    @Mock
    private NbiNetconfServerMessageListener m_nbiNetconfServerMessageListener;

    Element m_dataElement = DocumentUtils.stringToDocument(TestUtil.loadAsString(GET_RESPONSE)).getDocumentElement();

    private OnuKafkaConsumer<String> m_onuKafkaConsumerJson = PowerMockito.mock(OnuKafkaConsumerJson.class);
    private OnuKafkaConsumer<Msg> m_onuKafkaConsumerGpb = PowerMockito.mock(OnuKafkaConsumerGpb.class);

    String onuAlignmentAlignedNoitifcationJson = "/onu-alignment-status-notification-aligned.json";
    String onuAlignmentMisalignedNoitifcationJson = "/onu-alignment-status-notification-misaligned.json";
    String ietfAlarmsAlarmNotificationJson = "/ietf-alarms-alarm-notification.json";
    String ietfAlarmsAlarmNotificationClearedJson = "/ietf-alarms-alarm-notification-cleared.json";
    String internalGetResponseJson = "/internal-get-response-sw-hw-prop.json";

    private final String ONU_SERIAL_NUMBER = "ABCD12345678";
    private final String ONU_NAME = "onu1";
    private final String SENDER_VOMCI = "vomci1";
    private final String ONU_ID = "1";

    public VOLTManagementImplTest() throws NetconfMessageBuilderException {
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        m_unknownOnu = new UnknownONU();
        m_softwareImages = new SoftwareImages();
        m_txService = new TxService();
        m_messageFormatter = new JsonFormatter();
        m_voltManagement = new VOLTManagementImpl(m_txService, m_deviceManager, m_alarmService, m_kafkaProducerJson, m_unknownOnuHandler,
                m_connectionManager, m_modelNodeDSM, m_notificationService, m_adapterManager, m_pmaRegistry, m_schemaRegistry, m_messageFormatter,
                m_networkFunctionDao, m_deviceDao, m_nbiNetconfServerMessageListener);
        m_voltManagement.setKafkaConsumer(m_onuKafkaConsumerJson);
        when(m_vonuDevice.getDeviceName()).thenReturn(deviceName);
        when(m_deviceManager.getDevice(deviceName)).thenReturn(m_vonuDevice);
        PowerMockito.mockStatic(JsonUtil.class);

        m_kafkaCommunicationPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
        m_voltManagement.init();
    }

    @Test
    public void testOnuDeviceAddedForUnknownOnuNull() {
        m_unknownOnu = null;
        when(m_deviceManager.getDevice(deviceName)).thenReturn(m_vonuDevice);
        when(m_vonuDevice.getDeviceManagement()).thenReturn(m_deviceMgmt);
        when(m_deviceMgmt.getDeviceType()).thenReturn(VoltMFTestConstants.ONU_DEVICE_TYPE);
        when(m_unknownOnuHandler.findUnknownOnuEntity(anyString(), anyString())).thenReturn(m_unknownOnu);
        m_voltManagement.deviceAdded(deviceName);
        verify(m_unknownOnuHandler, never()).deleteUnknownOnuEntity(m_unknownOnu);
    }

    @Test
    public void testOnuDeviceAddedForUnknownOnuPresent() throws MessageFormatterException {
        when(m_deviceManager.getDevice(deviceName)).thenReturn(m_vonuDevice);
        when(m_vonuDevice.getDeviceManagement()).thenReturn(m_deviceMgmt);
        when(m_deviceMgmt.getDeviceState()).thenReturn(m_deviceState);
        when(m_deviceMgmt.getDeviceType()).thenReturn(VoltMFTestConstants.ONU_DEVICE_TYPE);
        when(m_deviceMgmt.getOnuConfigInfo()).thenReturn(m_onuConfigInfo);
        when(m_deviceState.getDeviceNodeId()).thenReturn("");
        when(m_onuConfigInfo.getExpectedSerialNumber()).thenReturn(VoltMFTestConstants.SERIAL_NUMBER);
        when(m_onuConfigInfo.getExpectedRegistrationId()).thenReturn(VoltMFTestConstants.REGISTER_NUMBER);
        when(m_onuConfigInfo.getExpectedAttachmentPoints()).thenReturn(m_expectedAttachmentPoints);
        when(m_expectedAttachmentPoints.getExpectedAttachmentPointSet()).thenReturn(m_expectedAttachmentPointSet);
        when(m_expectedAttachmentPointSet.iterator()).thenReturn(m_expectedAttachmentPointIterator);
        when(m_expectedAttachmentPointIterator.hasNext()).thenReturn(true, false);
        when(m_expectedAttachmentPointIterator.next()).thenReturn(m_expectedAttachmentPoint);
        when(m_attachmentPoint.getChannelPartitionName()).thenReturn("CT");
        when(m_vonuDevice.isMediatedSession()).thenReturn(true);
        when(m_unknownOnuHandler.findUnknownOnuEntity(anyString(), anyString())).thenReturn(m_unknownOnu);
        Set<SoftwareImage> emptySwSet = Collections.emptySet();
        m_softwareImages.setSoftwareImagesId("swImages_id1");
        m_softwareImages.setSoftwareImage(emptySwSet);
        m_unknownOnu.setSoftwareImages(m_softwareImages);
        m_voltManagement.deviceAdded(deviceName);
        m_voltManagement.waitForNotificationTasks();
        verify(m_kafkaProducerJson, times(1)).sendNotification(anyString(), anyString());
        verify(m_unknownOnuHandler, times(1)).deleteUnknownOnuEntity(m_unknownOnu);

    }

    @Test
    public void testOnuDeviceAddedForUnknownOnuPresentWithJsonFormatter() throws MessageFormatterException {
        when(m_deviceManager.getDevice(deviceName)).thenReturn(m_vonuDevice);
        when(m_vonuDevice.getDeviceManagement()).thenReturn(m_deviceMgmt);
        when(m_deviceMgmt.getDeviceState()).thenReturn(m_deviceState);
        when(m_deviceMgmt.getDeviceType()).thenReturn(VoltMFTestConstants.ONU_DEVICE_TYPE);
        when(m_deviceMgmt.getOnuConfigInfo()).thenReturn(m_onuConfigInfo);
        when(m_deviceState.getDeviceNodeId()).thenReturn("");
        when(m_onuConfigInfo.getExpectedSerialNumber()).thenReturn(VoltMFTestConstants.SERIAL_NUMBER);
        when(m_onuConfigInfo.getExpectedRegistrationId()).thenReturn(VoltMFTestConstants.REGISTER_NUMBER);
        when(m_onuConfigInfo.getExpectedAttachmentPoints()).thenReturn(m_expectedAttachmentPoints);
        when(m_expectedAttachmentPoints.getExpectedAttachmentPointSet()).thenReturn(m_expectedAttachmentPointSet);
        when(m_expectedAttachmentPointSet.iterator()).thenReturn(m_expectedAttachmentPointIterator);
        when(m_expectedAttachmentPointIterator.hasNext()).thenReturn(true, false);
        when(m_expectedAttachmentPointIterator.next()).thenReturn(m_expectedAttachmentPoint);
        when(m_attachmentPoint.getChannelPartitionName()).thenReturn("CT");
        when(m_vonuDevice.isMediatedSession()).thenReturn(true);
        when(m_unknownOnuHandler.findUnknownOnuEntity(anyString(), anyString())).thenReturn(m_unknownOnu);
        Set<SoftwareImage> emptySwSet = Collections.emptySet();
        m_softwareImages.setSoftwareImagesId("swImages_id1");
        m_softwareImages.setSoftwareImage(emptySwSet);
        m_unknownOnu.setSoftwareImages(m_softwareImages);
        m_voltManagement.deviceAdded(deviceName);
        m_voltManagement.waitForNotificationTasks();
        verify(m_kafkaProducerJson, times(1)).sendNotification(anyString(), anyString());
        verify(m_unknownOnuHandler, times(1)).deleteUnknownOnuEntity(m_unknownOnu);
        verify(m_deviceDao, never()).getVomciFunctionName(deviceName);
        verify(m_networkFunctionDao, never()).getKafkaConsumerTopics(anyString());
    }

    @Test
    public void testVonuDeviceAddedForUnknownOnuPresentWithGpbFormatter() throws MessageFormatterException, NetconfMessageBuilderException {
        HashSet<String> kafkaTopicNameSet = new HashSet<>();
        kafkaTopicNameSet.add("vomci-proxy-request1");
        kafkaTopicNameSet.add("vomci-proxy-request2");
        m_messageFormatter = PowerMockito.mock(GpbFormatter.class);
        m_voltManagement = new VOLTManagementImpl(m_txService, m_deviceManager, m_alarmService, m_kafkaProducerGpb, m_unknownOnuHandler,
                m_connectionManager, m_modelNodeDSM, m_notificationService, m_adapterManager, m_pmaRegistry, m_schemaRegistry, m_messageFormatter, m_networkFunctionDao,
                m_deviceDao, m_nbiNetconfServerMessageListener);
        m_voltManagement.init();
        m_voltManagement.setKafkaConsumer(m_onuKafkaConsumerGpb);
        when(m_deviceManager.getDevice(deviceName)).thenReturn(m_vonuDevice);
        when(m_vonuDevice.getDeviceManagement()).thenReturn(m_deviceMgmt);
        when(m_deviceMgmt.getDeviceState()).thenReturn(m_deviceState);
        when(m_deviceMgmt.getDeviceType()).thenReturn(VoltMFTestConstants.ONU_DEVICE_TYPE);
        when(m_deviceMgmt.getOnuConfigInfo()).thenReturn(m_onuConfigInfo);
        when(m_deviceState.getDeviceNodeId()).thenReturn("");
        when(m_onuConfigInfo.getExpectedSerialNumber()).thenReturn(VoltMFTestConstants.SERIAL_NUMBER);
        when(m_onuConfigInfo.getExpectedAttachmentPoints()).thenReturn(m_expectedAttachmentPoints);
        when(m_expectedAttachmentPoints.getExpectedAttachmentPointSet()).thenReturn(m_expectedAttachmentPointSet);
        when(m_expectedAttachmentPointSet.iterator()).thenReturn(m_expectedAttachmentPointIterator);
        when(m_expectedAttachmentPointIterator.hasNext()).thenReturn(true, false);
        when(m_expectedAttachmentPointIterator.next()).thenReturn(m_expectedAttachmentPoint);
        when(m_onuConfigInfo.getExpectedRegistrationId()).thenReturn(VoltMFTestConstants.REGISTER_NUMBER);
        when(m_expectedAttachmentPoint.getChannelPartitionName()).thenReturn("CT");
        when(m_vonuDevice.isMediatedSession()).thenReturn(true);
        when(m_unknownOnuHandler.findUnknownOnuEntity(anyString(), anyString())).thenReturn(m_unknownOnu);
        when(m_networkFunctionDao.getKafkaConsumerTopics(any())).thenReturn(m_kafkaTopicSet);
        when(m_vonuDevice.getDeviceManagement().getOnuConfigInfo().getPlannedOnuManagementMode()).thenReturn(USE_VOMCI);
        OnuManagementChain chain1 = new OnuManagementChain();
        chain1.setNfName("vomci1");
        OnuManagementChain chain2 = new OnuManagementChain();
        chain2.setNfName("olt1");
        OnuManagementChain[] managementChains = {chain1, chain2};
        when(m_deviceDao.getOnuManagementChains(deviceName)).thenReturn(managementChains);
        when(m_networkFunctionDao.getKafkaTopicNames(any(), any())).thenReturn(kafkaTopicNameSet);
        Object formattedMessage = Msg.newBuilder().build();
        when(m_messageFormatter.getFormattedRequest(any(), anyString(), any(), any(), any(), any(), any())).thenReturn(formattedMessage);

        when(m_vonuDevice.getDeviceManagement()).thenReturn(m_deviceMgmt);
        when(m_deviceMgmt.getOnuConfigInfo()).thenReturn(m_onuConfigInfo);
        when(m_deviceMgmt.getDeviceState()).thenReturn(m_deviceState);
        when(m_deviceState.getOnuStateInfo()).thenReturn(m_onuStateInfo);
        when(m_onuStateInfo.getActualAttachmentPoint()).thenReturn(m_actualAttachmentPoint);
        when(m_onuConfigInfo.getExpectedAttachmentPoints()).thenReturn(m_expectedAttachmentPoints);
        when(m_expectedAttachmentPoints.getExpectedAttachmentPointSet()).thenReturn(m_expectedAttachmentPointSet);
        when(m_vonuDevice.getDeviceManagement().getDeviceState().getOnuStateInfo().getActualAttachmentPoint()).thenReturn(m_actualAttachmentPoint);
        when(m_actualAttachmentPoint.getOltName()).thenReturn("OLT1");
        when(m_actualAttachmentPoint.getChannelTerminationRef()).thenReturn("CT");

        m_voltManagement.deviceAdded(deviceName);
        m_voltManagement.waitForNotificationTasks();


        verify(m_kafkaProducerGpb, times(4)).sendNotification(anyString(), any());
        verify(m_unknownOnuHandler, times(1)).deleteUnknownOnuEntity(m_unknownOnu);
    }

    @Test
    public void testEonuDeviceAddedForUnknownOnuPresentWithGpbFormatter() throws MessageFormatterException, NetconfMessageBuilderException {
        HashSet<String> kafkaTopicNameSet = new HashSet<>();
        kafkaTopicNameSet.add("vomci-proxy-request1");
        kafkaTopicNameSet.add("vomci-proxy-request2");
        m_messageFormatter = PowerMockito.mock(GpbFormatter.class);
        m_voltManagement = new VOLTManagementImpl(m_txService, m_deviceManager, m_alarmService, m_kafkaProducerGpb, m_unknownOnuHandler,
                m_connectionManager, m_modelNodeDSM, m_notificationService, m_adapterManager, m_pmaRegistry, m_schemaRegistry, m_messageFormatter, m_networkFunctionDao,
                m_deviceDao, m_nbiNetconfServerMessageListener);
        m_voltManagement.init();
        m_voltManagement.setKafkaConsumer(m_onuKafkaConsumerGpb);
        when(m_deviceManager.getDevice(deviceName)).thenReturn(m_vonuDevice);
        when(m_vonuDevice.getDeviceManagement()).thenReturn(m_deviceMgmt);
        when(m_deviceMgmt.getDeviceState()).thenReturn(m_deviceState);
        when(m_deviceMgmt.getDeviceType()).thenReturn(VoltMFTestConstants.ONU_DEVICE_TYPE);
        when(m_deviceMgmt.getOnuConfigInfo()).thenReturn(m_onuConfigInfo);
        when(m_deviceState.getDeviceNodeId()).thenReturn("");
        when(m_onuConfigInfo.getExpectedSerialNumber()).thenReturn(VoltMFTestConstants.SERIAL_NUMBER);
        when(m_onuConfigInfo.getExpectedAttachmentPoints()).thenReturn(m_expectedAttachmentPoints);
        when(m_expectedAttachmentPoints.getExpectedAttachmentPointSet()).thenReturn(m_expectedAttachmentPointSet);
        when(m_expectedAttachmentPointSet.iterator()).thenReturn(m_expectedAttachmentPointIterator);
        when(m_expectedAttachmentPointIterator.hasNext()).thenReturn(true, false);
        when(m_expectedAttachmentPointIterator.next()).thenReturn(m_expectedAttachmentPoint);
        when(m_onuConfigInfo.getExpectedRegistrationId()).thenReturn(VoltMFTestConstants.REGISTER_NUMBER);
        when(m_expectedAttachmentPoint.getChannelPartitionName()).thenReturn("CT");
        when(m_vonuDevice.isMediatedSession()).thenReturn(true);
        when(m_unknownOnuHandler.findUnknownOnuEntity(anyString(), anyString())).thenReturn(m_unknownOnu);
        when(m_networkFunctionDao.getKafkaConsumerTopics(any())).thenReturn(m_kafkaTopicSet);
        when(m_vonuDevice.getDeviceManagement().getOnuConfigInfo().getPlannedOnuManagementMode()).thenReturn(ONUConstants.USE_EOMCI);
        OnuManagementChain chain1 = new OnuManagementChain();
        chain1.setNfName("vomci1");
        OnuManagementChain chain2 = new OnuManagementChain();
        chain2.setNfName("olt1");
        OnuManagementChain[] managementChains = {chain1, chain2};
        when(m_deviceDao.getOnuManagementChains(deviceName)).thenReturn(managementChains);
        when(m_networkFunctionDao.getKafkaTopicNames(any(), any())).thenReturn(kafkaTopicNameSet);
        Object formattedMessage = Msg.newBuilder().build();
        when(m_messageFormatter.getFormattedRequest(any(), anyString(), any(), any(), any(), any(), any())).thenReturn(formattedMessage);

        when(m_vonuDevice.getDeviceManagement()).thenReturn(m_deviceMgmt);
        when(m_deviceMgmt.getOnuConfigInfo()).thenReturn(m_onuConfigInfo);
        when(m_deviceMgmt.getDeviceState()).thenReturn(m_deviceState);
        when(m_deviceState.getOnuStateInfo()).thenReturn(m_onuStateInfo);
        when(m_onuStateInfo.getActualAttachmentPoint()).thenReturn(m_actualAttachmentPoint);
        when(m_onuConfigInfo.getExpectedAttachmentPoints()).thenReturn(m_expectedAttachmentPoints);
        when(m_expectedAttachmentPoints.getExpectedAttachmentPointSet()).thenReturn(m_expectedAttachmentPointSet);
        when(m_vonuDevice.getDeviceManagement().getDeviceState().getOnuStateInfo().getActualAttachmentPoint()).thenReturn(m_actualAttachmentPoint);
        when(m_actualAttachmentPoint.getOltName()).thenReturn("OLT1");

        m_voltManagement.deviceAdded(deviceName);
        m_voltManagement.waitForNotificationTasks();

        verify(m_kafkaProducerGpb, times(2)).sendNotification(anyString(), any());
        verify(m_unknownOnuHandler, times(1)).deleteUnknownOnuEntity(m_unknownOnu);
    }


    @Test
    public void testProcessResponseWhenDeviceNull() {
        Device device = null;
        when(m_deviceManager.getDevice(deviceName)).thenReturn(device);
        m_voltManagement.processNotificationResponse(deviceName, VoltMFTestConstants.ONU_GET_OPERATION, VoltMFTestConstants.DEFAULT_MESSAGE_ID,
                VoltMFTestConstants.OK_RESPONSE, null, null);
        verify(m_connectionManager, never()).addMediatedDeviceNetconfSession(deviceName, null);
    }

    //VOMCI functions is currently not supporting GET request handling.
    @Test
    public void testProcessDetectResponseWhenDevicePresentSendsGetRequest() throws NetconfMessageBuilderException, MessageFormatterException {
        Document filter = DocumentUtils.stringToDocument(TestUtil.loadAsString(GET_REQUEST));
        ArrayList filterList = new ArrayList();
        setupGpbFormatter();
        when(m_deviceManager.getDevice(deviceName)).thenReturn(m_vonuDevice);
        when(m_vonuDevice.getDeviceManagement()).thenReturn(m_deviceMgmt);
        when(m_deviceMgmt.getOnuConfigInfo()).thenReturn(m_onuConfigInfo);
        when(m_onuConfigInfo.getExpectedSerialNumber()).thenReturn(VoltMFTestConstants.SERIAL_NUMBER);
        when(m_onuConfigInfo.getExpectedAttachmentPoints()).thenReturn(m_expectedAttachmentPoints);
        when(m_expectedAttachmentPoints.getExpectedAttachmentPointSet()).thenReturn(Collections.singleton(m_attachmentPoint));
        when(m_deviceMgmt.getDeviceState()).thenReturn(m_deviceState);
        when(m_deviceState.getOnuStateInfo()).thenReturn(m_onuStateInfo);
        when(m_onuStateInfo.getActualAttachmentPoint()).thenReturn(m_actualAttachmentPoint);
        when(m_onuStateInfo.getSoftwareImages()).thenReturn(m_softwareImages);
        m_softwareImages.setSoftwareImage(prepareSwImageSet(deviceName));
        when(m_actualAttachmentPoint.getOltName()).thenReturn(VoltMFTestConstants.OLT_NAME);
        when(m_actualAttachmentPoint.getOnuId()).thenReturn(VoltMFTestConstants.ONU_ID_VALUE);
        when(m_actualAttachmentPoint.getChannelTerminationRef()).thenReturn(VoltMFTestConstants.CHANNEL_TERMINATION);
        when(m_connectionManager.getMediatedDeviceSession(deviceName)).thenReturn(m_mediatedDeviceNCsession);

        Module module = Mockito.mock(Module.class);
        QNameModule qNameModule = PowerMockito.mock(QNameModule.class);
        Optional<Revision> revision = PowerMockito.mock(Optional.class);
        SchemaPathBuilder schemaPathBuilder = Mockito.mock(SchemaPathBuilder.class);
        SchemaPath schemaPath = Mockito.mock(SchemaPath.class);
        filterList.add(filter.getDocumentElement());
        when(m_getRequestOne.getFilter()).thenReturn(m_filter);
        when(m_getRequestOne.getFilter().getXmlFilterElements()).thenReturn(filterList);
        when(m_getRequestOne.getMessageId()).thenReturn(VoltMFTestConstants.DEFAULT_MESSAGE_ID);

        when(m_schemaRegistry.getModuleByNamespace(VoltMFTestConstants.NETWORK_MANAGER_NAMESPACE)).thenReturn(module);
        when(m_schemaRegistry.getModuleNameByNamespace(VoltMFTestConstants.NETWORK_MANAGER_NAMESPACE)).thenReturn(ONUConstants.NETWORK_MANAGER);

        when(module.getQNameModule()).thenReturn(qNameModule);
        when(qNameModule.getRevision()).thenReturn(revision);
        when(schemaPathBuilder.build()).thenReturn(schemaPath);

        DataSchemaNode dataSchemaNode = getDataSchemaNode();

        when(m_schemaRegistry.getDataSchemaNode(any(SchemaPath.class))).thenReturn(dataSchemaNode);

        PowerMockito.when(JsonUtil.convertFromXmlToJsonIgnoreEmptyLeaves(any(), any(), any(), any())).thenReturn(VoltMFTestConstants.FILTER_JSON_STRING);

        m_voltManagement.processNotificationResponse(deviceName, VoltMFTestConstants.ONU_GET_OPERATION, VoltMFTestConstants.DEFAULT_MESSAGE_ID,
                VoltMFTestConstants.OK_RESPONSE, null, null);
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        verify(m_connectionManager, times(1)).getMediatedDeviceSession(deviceName);
        verify(m_getRequestOne, times(1)).getFilter();
        verify(m_mediatedDeviceNCsession, times(1)).onGet(any());
    }

    @Test
    public void testProcessOnuUndetectWithOkResponse() {
        when(m_deviceManager.getDevice(deviceName)).thenReturn(m_vonuDevice);
        m_voltManagement.processNotificationResponse(deviceName, VoltMFTestConstants.ONU_UNDETECTED, VoltMFTestConstants.DEFAULT_MESSAGE_ID,
                VoltMFTestConstants.OK_RESPONSE, null, null);
        verify(m_deviceManager, times(1)).updateConfigAlignmentState(deviceName, VoltMFTestConstants.NEVER_ALIGNED);
        verify(m_connectionManager, never()).addMediatedDeviceNetconfSession(deviceName, null);
    }

    @Test
    public void testProcessOnuUndetectWithNotOkResponse() {
        when(m_deviceManager.getDevice(deviceName)).thenReturn(m_vonuDevice);
        m_voltManagement.processNotificationResponse(deviceName, VoltMFTestConstants.ONU_UNDETECTED, VoltMFTestConstants.DEFAULT_MESSAGE_ID,
                VoltMFTestConstants.NOK_RESPONSE, null, null);

        verify(m_deviceManager, never()).updateConfigAlignmentState(deviceName, VoltMFTestConstants.NEVER_ALIGNED);
        verify(m_connectionManager, never()).addMediatedDeviceNetconfSession(deviceName, null);
        verify(m_vonuDevice, never()).getDeviceManagement();
    }

    @Test
    public void testWhenEonuDeviceAdded() {
        when(m_deviceManager.getDevice(deviceName)).thenReturn(m_vonuDevice);
        when(m_vonuDevice.getDeviceManagement()).thenReturn(m_deviceMgmt);
        when(m_deviceMgmt.getDeviceType()).thenReturn(VoltMFTestConstants.ONU_DEVICE_TYPE);
        m_voltManagement.deviceAdded(deviceName);
        when(m_vonuDevice.isMediatedSession()).thenReturn(false);
        verify(m_unknownOnuHandler, never()).findUnknownOnuEntity(anyString(), anyString());
        verify(m_unknownOnuHandler, never()).deleteUnknownOnuEntity(m_unknownOnu);
    }

    @Test
    public void testOnuDeviceAddedNull() {
        when(m_deviceManager.getDevice(deviceName)).thenReturn(null);
        m_voltManagement.deviceAdded(deviceName);
        verify(m_vonuDevice, never()).getDeviceManagement();
        verify(m_unknownOnuHandler, never()).findUnknownOnuEntity(anyString(), anyString());
        verify(m_unknownOnuHandler, never()).deleteUnknownOnuEntity(m_unknownOnu);
    }

    private DataSchemaNode getDataSchemaNode() {
        return new DataSchemaNode() {
            @Override
            public boolean isConfiguration() {
                return false;
            }

            @Override
            public boolean isAugmenting() {
                return false;
            }

            @Override
            public boolean isAddedByUses() {
                return false;
            }

            @NotNull
            @Override
            public QName getQName() {
                return null;
            }

            @NotNull
            @Override
            public SchemaPath getPath() {
                return SchemaPath.create(true, QName.create(VoltMFTestConstants.NETWORK_MANAGER_NAMESPACE, VoltMFTestConstants.NETWORK_MANAGER_REVISION, VoltMFTestConstants.NETWORK_MANAGER));
            }

            @NotNull
            @Override
            public Status getStatus() {
                return null;
            }

            @Override
            public Optional<String> getDescription() {
                return Optional.empty();
            }

            @Override
            public Optional<String> getReference() {
                return Optional.empty();
            }

            @Override
            public Optional<RevisionAwareXPath> getWhenCondition() {
                return Optional.empty();
            }
        };
    }


    @Test
    @Ignore
    public void testInternalOKGetResponseCreatedONU() {
        JSONObject jsonResponse = new JSONObject(getResponse);
        when(m_deviceManager.getDevice(deviceName)).thenReturn(m_vonuDevice);
        when(m_deviceMgmt.getDeviceState()).thenReturn(m_deviceState);
        when(m_deviceDao.getDeviceState(deviceName)).thenReturn(m_deviceState);
        when(m_deviceState.getOnuStateInfo()).thenReturn(m_onuStateInfo);
        when(m_connectionManager.getMediatedDeviceSession(deviceName)).thenReturn(m_mediatedDeviceNCsession);
        when(m_deviceState.getOnuStateInfo().getEquipmentId()).thenReturn("");
        when(m_deviceDao.getDeviceState(deviceName)).thenReturn(m_deviceState);
        String eqptId = getEqptId(jsonResponse);
        Set<SoftwareImage> swSet = prepareSwImageSet(null);
        m_voltManagement.processResponse(jsonResponse);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        verify(m_deviceManager, times(1)).updateEquipmentIdInOnuStateInfo(eq(deviceName), eq(eqptId));
        verify(m_deviceManager, times(1)).updateSoftwareImageInOnuStateInfo(eq(deviceName), any());
    }

    @Test
    public void testInternalOKGetResponseCreatedONUGpb() {
        setupGpbFormatter();
        String onuDeviceName = "ont1";
        when(m_deviceManager.getDevice(onuDeviceName)).thenReturn(m_vonuDevice);
        when(m_deviceMgmt.getDeviceState()).thenReturn(m_deviceState);
        when(m_vonuDevice.getDeviceManagement()).thenReturn(m_deviceMgmt);
        when(m_deviceMgmt.getOnuConfigInfo()).thenReturn(m_onuConfigInfo);
        when(m_onuConfigInfo.getExpectedSerialNumber()).thenReturn("ABCD12345678");
        when(m_deviceDao.getDeviceState(onuDeviceName)).thenReturn(m_deviceState);
        when(m_deviceState.getOnuStateInfo()).thenReturn(m_onuStateInfo);
        when(m_connectionManager.getMediatedDeviceSession(onuDeviceName)).thenReturn(m_mediatedDeviceNCsession);
        when(m_deviceState.getOnuStateInfo().getEquipmentId()).thenReturn("");
        when(m_deviceDao.getDeviceState(onuDeviceName)).thenReturn(m_deviceState);
        String getResponseString = prepareJsonResponseFromFile(internalGetResponseJson);
        Msg msg = prepareGetResponseGpb(getResponseString);
        m_voltManagement.processResponse(msg);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        verify(m_deviceManager, times(1)).updateEquipmentIdInOnuStateInfo(eq(onuDeviceName), eq("EqptModelName_1"));
        verify(m_deviceManager, times(1)).updateSoftwareImageInOnuStateInfo(eq(onuDeviceName), any());
    }

    @Test
    public void testInternalEmptyGetResponseCreatedONUGpb() {
        setupGpbFormatter();
        String onuDeviceName = "ont1";
        when(m_deviceManager.getDevice(onuDeviceName)).thenReturn(m_vonuDevice);
        when(m_deviceMgmt.getDeviceState()).thenReturn(m_deviceState);
        when(m_vonuDevice.getDeviceManagement()).thenReturn(m_deviceMgmt);
        when(m_deviceMgmt.getOnuConfigInfo()).thenReturn(m_onuConfigInfo);
        when(m_onuConfigInfo.getExpectedSerialNumber()).thenReturn("ABCD12345678");
        when(m_deviceDao.getDeviceState(onuDeviceName)).thenReturn(m_deviceState);
        when(m_deviceState.getOnuStateInfo()).thenReturn(m_onuStateInfo);
        when(m_connectionManager.getMediatedDeviceSession(onuDeviceName)).thenReturn(m_mediatedDeviceNCsession);
        when(m_deviceState.getOnuStateInfo().getEquipmentId()).thenReturn("");
        when(m_deviceDao.getDeviceState(onuDeviceName)).thenReturn(m_deviceState);
        Msg msg = prepareGetResponseGpb("");
        m_voltManagement.processResponse(msg);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        verify(m_deviceManager, never()).updateEquipmentIdInOnuStateInfo(eq(onuDeviceName), eq("EqptModelName_1"));
        verify(m_deviceManager, never()).updateSoftwareImageInOnuStateInfo(eq(onuDeviceName), any());
    }

    @Test
    public void testGpbHelloResponseProcess() throws MessageFormatterException {
        m_messageFormatter = new GpbFormatter();

        Msg msg = Msg.newBuilder()
                .setHeader(Header.newBuilder()
                        .setMsgId("1")
                        .setSenderName("vomci-vendor-1")
                        .setRecipientName("vOLTMF")
                                        .setObjectName("vOLTMF")
                                        .setObjectType(OBJECT_TYPE.VOMCI_FUNCTION)
                                        .build())
                    .setBody(Body.newBuilder()
                                .setResponse(Response.newBuilder()
                                                    .setHelloResp(HelloResp.newBuilder()
                                                                            .setServiceEndpointName("KAFKA_LOCAL_ENDPOINT_NAME")
                                                                            .addNetworkFunctionInfo(NFInformation.newBuilder()
                                                                                    .putNfTypes("software-version","5.0.0")
                                                                                    .putNfTypes("vendor-name","Broadband Forum")
                                                                                    .addCapabilities(NFInformation.NFCapability.ONU_STATE_ONLY_SUPPORT))
                                                                            .build())
                                                    .build()))
                    .build();

        Map<String,String> nf_types = new HashMap<>();
        nf_types.put("software-version","5.0.0");
        nf_types.put("vendor-name","Broadband Forum");

        HelloResponseData responseData = new HelloResponseData("vomci-vendor-1",ObjectType.VOMCI_FUNCTION,
                "vOLTMF","1","KAFKA_LOCAL_ENDPOINT_NAME",nf_types, HelloResponseData.NfCapabilities.ONU_STATE_ONLY_SUPPORT);

        assertTrue(m_messageFormatter.isHelloResponse(msg));

        HelloResponseData actualResponseData =  m_messageFormatter.getHelloResponseData(msg);
        assertEquals(responseData,actualResponseData);

        /* This not work because m_messageFormatter is not a PowerMockito
        * m_voltManagement.processResponse(msg);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        verify(m_voltManagement, times(1)).processHelloRequestResponse(eq(responseData));
        *
        * It will be left here for possible futures
        * */

    }

    @Test
    public void testInternalGetResponseNOKCreatedONU() {
        setupGpbFormatter();
        Set<SoftwareImage> softwareImageSet = new HashSet<SoftwareImage>();
        when(m_deviceManager.getDevice(deviceName)).thenReturn(m_vonuDevice);
        when(m_deviceMgmt.getDeviceState()).thenReturn(m_deviceState);
        when(m_deviceState.getOnuStateInfo()).thenReturn(m_onuStateInfo);
        when(m_connectionManager.getMediatedDeviceSession(deviceName)).thenReturn(m_mediatedDeviceNCsession);
        String getResponseString = prepareJsonResponseFromFile(internalGetResponseJson);
        Msg msg = prepareGetResponseGpb(getResponseString);
        m_voltManagement.processResponse(msg);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        verify(m_deviceManager, never()).updateEquipmentIdInOnuStateInfo(eq(deviceName), eq(""));
        verify(m_deviceManager, never()).updateSoftwareImageInOnuStateInfo(eq(deviceName), eq(softwareImageSet));
    }

    @Test
    public void testInternalEmptyGetResponseCreatedONU() {
        setupGpbFormatter();
        verify(m_deviceManager, never()).updateEquipmentIdInOnuStateInfo(eq(deviceName), any());
        verify(m_deviceManager, never()).updateSoftwareImageInOnuStateInfo(eq(deviceName), any());
        Set<SoftwareImage> softwareImageSet = new HashSet<SoftwareImage>();
        when(m_deviceManager.getDevice(deviceName)).thenReturn(m_vonuDevice);
        when(m_deviceMgmt.getDeviceState()).thenReturn(m_deviceState);
        when(m_deviceState.getOnuStateInfo()).thenReturn(m_onuStateInfo);
        when(m_connectionManager.getMediatedDeviceSession(deviceName)).thenReturn(m_mediatedDeviceNCsession);
        String getResponseString = prepareJsonResponseFromFile(internalGetResponseJson);
        Msg msg = prepareGetResponseGpb("");
        m_voltManagement.processResponse(msg);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        verify(m_deviceManager, never()).updateEquipmentIdInOnuStateInfo(eq(deviceName), eq(""));
        verify(m_deviceManager, never()).updateSoftwareImageInOnuStateInfo(eq(deviceName), eq(softwareImageSet));
    }


    @Test
    @Ignore
    public void testInternalGetResponseUnknownONU() {
        JSONObject jsonResponse = new JSONObject(getResponse);
        String eqptId = getEqptId(jsonResponse);
        when(m_deviceMgmt.getDeviceState()).thenReturn(m_deviceState);
        when(m_deviceState.getOnuStateInfo()).thenReturn(m_onuStateInfo);
        when(m_deviceManager.getDevice(deviceName)).thenReturn(null);
        UnknownONU matchedUnknownONU = new UnknownONU();
        matchedUnknownONU.setChannelTermRef("channeltermination.1");
        matchedUnknownONU.setOnuID("1");
        matchedUnknownONU.setOltDeviceName("OLT1");
        matchedUnknownONU.setSerialNumber("ABCD12345678");
        matchedUnknownONU.setRegistrationId("ABCD1234");
        when(m_unknownOnuHandler.findMatchingUnknownOnu("OLT1", "channeltermination.1", "1")).thenReturn(matchedUnknownONU);
        String parentId = "onu_ABCD12345678";
        m_voltManagement.processResponse(jsonResponse);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(eqptId, matchedUnknownONU.getEquipmentId());
        assertEquals(parentId, matchedUnknownONU.getSoftwareImages().getSoftwareImagesId());
        assertTrue(prepareSwImageSet(parentId).equals(matchedUnknownONU.getSoftwareImages().getSoftwareImage()));

    }

    // Not finished yet
    @Test
    @Ignore
    public void testConvertActionMessage() throws NetconfMessageBuilderException, MessageFormatterException {
        NetworkWideTag networkWideTag = new NetworkWideTag("onu", "olt", "1", "voltmf", null, "ONU", "Recipient", ObjectType.ONU);

        when(m_vonuDevice.getDeviceName()).thenReturn(deviceName);
        when(m_deviceManager.getDevice(deviceName)).thenReturn(m_vonuDevice);
        when(m_adapterManager.getAdapterContext(any())).thenReturn(any());

        ActionRequest actionRequest = generateActionRequest();
        actionRequest.setMessageId("12");
        GpbFormatter gpbFormatter = new GpbFormatter();
        Msg msg = gpbFormatter.getFormattedRequest(eq(actionRequest), eq(NetconfResources.ACTION), eq(m_vonuDevice), eq(m_adapterManager), eq(m_modelNodeDSM), eq(m_schemaRegistry), eq(networkWideTag));
        String payload = msg.getBody().getRequest().getAction().getInputData().toStringUtf8();
        assertEquals(payload, VoltMFTestConstants.JSON_PAYLOAD_ACTION_REQUEST);
    }

    private ActionRequest generateActionRequest() throws NetconfMessageBuilderException {
        String actionRequest = VoltMFTestConstants.XML_ACTION_REQUEST;
        return DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest));
    }

    /* test will be enabled after vomci has the get request & response handling feature */
    @Test
    @Ignore
    public void testSendGetRequestToUnknowONU() throws MessageFormatterException {
        Notification notification;
        ONUNotification onuNotification = null;
        String serialNum = "ABCD12345678";
        String regId = "ABCD1234";
        String chanTermRef = "channeltermination.1";
        String vaniRef = "onu_1";
        String onuId = "1";
        String onuState = "onu-present-and-on-intended-channel-termination";
        try {
            notification = getDetectNotification(serialNum, regId, chanTermRef, vaniRef, onuId, onuState);
            onuNotification = new ONUNotification(notification, "OLT1", m_messageFormatter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        when(m_deviceManager.getDeviceWithSerialNumber(serialNum)).thenReturn(null);
        when(m_unknownOnuHandler.findUnknownOnuEntity(serialNum, regId)).thenReturn(m_unknownOnu);
        m_voltManagement.onuNotificationProcess(onuNotification, "OLT1");
        verify(m_kafkaProducerJson, times(1)).sendNotification(eq(ONUConstants.ONU_REQUEST_KAFKA_TOPIC), anyString());  //TO-DO : add expectedGetRequest post merging (internal get request changes)with develop branch.

    }

    @Test
    public void testNetworkFunctionCreationAndDeletion() {
        String networkFunctionName = "vomci";
        m_messageFormatter = new GpbFormatter();
        m_voltManagement = new VOLTManagementImpl(m_txService, m_deviceManager, m_alarmService, m_kafkaProducerJson, m_unknownOnuHandler,
                m_connectionManager, m_modelNodeDSM, m_notificationService, m_adapterManager, m_pmaRegistry, m_schemaRegistry, m_messageFormatter,
                m_networkFunctionDao, m_deviceDao, m_nbiNetconfServerMessageListener);
        Set<KafkaTopic> kafkaTopicSet = new HashSet<>();
        kafkaTopicSet.add(m_kafkaTopic);
        when(m_networkFunctionDao.getKafkaConsumerTopics(networkFunctionName)).thenReturn(kafkaTopicSet);
        //m_voltManagement.setKafkaConsumer(m_onuKafkaConsumerJson);
        m_voltManagement.setKafkaConsumer(m_onuKafkaConsumerGpb);
        m_voltManagement.networkFunctionAdded(networkFunctionName);
        verify(m_networkFunctionDao, times(1)).getKafkaConsumerTopics(networkFunctionName);
        verify(m_onuKafkaConsumerGpb, times(1)).updateSubscriberTopics(any());
        //verify(m_onuKafkaConsumerJson, times(1)).updateSubscriberTopics(any());

        m_voltManagement.networkFunctionRemoved(networkFunctionName);
        //verify(m_onuKafkaConsumerJson, times(1)).removeSubscriberTopics(any());
        verify(m_onuKafkaConsumerGpb, times(1)).removeSubscriberTopics(any());
    }

    @Test
    public void testProcessNotificationWhenMisaligned() throws MessageFormatterException {
        VOLTManagementImpl voltManagement = new VOLTManagementImpl(m_txService, m_deviceManager, m_alarmService, m_kafkaProducerJson, m_unknownOnuHandler,
                m_connectionManager, m_modelNodeDSM, m_notificationService, m_adapterManager, m_pmaRegistry, m_schemaRegistry, m_gpbFormatter, m_networkFunctionDao,
                m_deviceDao, m_nbiNetconfServerMessageListener);
        when(m_responseData.getIdentifier()).thenReturn(ONU_ID);
        when(m_responseData.getSenderName()).thenReturn(SENDER_VOMCI);
        when(m_responseData.getObjectType()).thenReturn(ObjectType.ONU);
        when(m_responseData.getOnuName()).thenReturn(ONU_NAME);
        when(m_responseData.getOperationType()).thenReturn(NetconfResources.NOTIFICATION);
        when(m_responseData.getResponsePayload()).thenReturn(prepareJsonResponseFromFile(onuAlignmentMisalignedNoitifcationJson));
        when(m_gpbFormatter.getResponseData(any())).thenReturn(m_responseData);
        when(m_gpbFormatter.getResponseData(m_notificationResponse)).thenReturn(m_responseData);
        voltManagement.processNotification(m_notificationResponse);
        verify(m_deviceManager, times(1)).updateConfigAlignmentState(ONU_NAME, DeviceManagerNSConstants.NEVER_ALIGNED);
        verify(m_alarmService, never()).raiseAlarm(any());
        verify(m_alarmService, never()).clearAlarm(any());
    }

    @Test
    public void testProcessNotificationWhenAligned() throws MessageFormatterException {
        VOLTManagementImpl voltManagement = new VOLTManagementImpl(m_txService, m_deviceManager, m_alarmService, m_kafkaProducerJson, m_unknownOnuHandler,
                m_connectionManager, m_modelNodeDSM, m_notificationService, m_adapterManager, m_pmaRegistry, m_schemaRegistry, m_gpbFormatter, m_networkFunctionDao,
                m_deviceDao, m_nbiNetconfServerMessageListener);
        when(m_deviceManager.getDevice(ONU_NAME)).thenReturn(m_vonuDevice);
        when(m_responseData.getIdentifier()).thenReturn(ONU_ID);
        when(m_responseData.getSenderName()).thenReturn(SENDER_VOMCI);
        when(m_responseData.getObjectType()).thenReturn(ObjectType.ONU);
        when(m_responseData.getOnuName()).thenReturn(ONU_NAME);
        when(m_responseData.getOperationType()).thenReturn(NetconfResources.NOTIFICATION);
        when(m_responseData.getResponsePayload()).thenReturn(prepareJsonResponseFromFile(onuAlignmentAlignedNoitifcationJson));
        when(m_gpbFormatter.getResponseData(any())).thenReturn(m_responseData);
        when(m_gpbFormatter.getResponseData(m_notificationResponse)).thenReturn(m_responseData);
        when(m_connectionManager.getConnectionState(m_vonuDevice)).thenReturn(m_connectionState);
        when(m_vonuDevice.getDeviceManagement()).thenReturn(m_deviceMgmt);
        when(m_deviceMgmt.getOnuConfigInfo()).thenReturn(m_onuConfigInfo);
        when(m_onuConfigInfo.getExpectedSerialNumber()).thenReturn(ONU_SERIAL_NUMBER);
        when(m_deviceMgmt.getDeviceState()).thenReturn(m_deviceState);
        when(m_deviceState.getOnuStateInfo()).thenReturn(null);
        when(m_connectionState.isConnected()).thenReturn(true);
        voltManagement.processNotification(m_notificationResponse);
        verify(m_deviceManager, times(1)).updateConfigAlignmentState(ONU_NAME , DeviceManagerNSConstants.ALIGNED);
        verify(m_alarmService, never()).raiseAlarm(any());
        verify(m_alarmService, never()).clearAlarm(any());
    }

    @Test
    @Ignore
    public void testProcessNotificationIetfAlarmsAlarmNotification() throws MessageFormatterException {
        VOLTManagementImpl voltManagement = new VOLTManagementImpl(m_txService, m_deviceManager, m_alarmService, m_kafkaProducerJson, m_unknownOnuHandler,
                m_connectionManager, m_modelNodeDSM, m_notificationService, m_adapterManager, m_pmaRegistry, m_schemaRegistry, m_gpbFormatter, m_networkFunctionDao,
                m_deviceDao, m_nbiNetconfServerMessageListener);

        when(m_deviceManager.getDevice(ONU_NAME)).thenReturn(m_vonuDevice);
        when(m_responseData.getIdentifier()).thenReturn(ONU_ID);
        when(m_responseData.getSenderName()).thenReturn(SENDER_VOMCI);
        when(m_responseData.getObjectType()).thenReturn(ObjectType.ONU);
        when(m_responseData.getOnuName()).thenReturn(ONU_NAME);
        when(m_responseData.getOperationType()).thenReturn(NetconfResources.NOTIFICATION);
        when(m_responseData.getResponsePayload()).thenReturn(prepareJsonResponseFromFile(ietfAlarmsAlarmNotificationJson));
        when(m_gpbFormatter.getResponseData(any())).thenReturn(m_responseData);
        when(m_gpbFormatter.getResponseData(m_notificationResponse)).thenReturn(m_responseData);
        when(m_connectionManager.getConnectionState(m_vonuDevice)).thenReturn(m_connectionState);
        when(m_vonuDevice.getDeviceManagement()).thenReturn(m_deviceMgmt);
        when(m_deviceMgmt.getOnuConfigInfo()).thenReturn(m_onuConfigInfo);
        when(m_onuConfigInfo.getExpectedSerialNumber()).thenReturn(ONU_SERIAL_NUMBER);
        when(m_deviceMgmt.getDeviceState()).thenReturn(m_deviceState);
        when(m_deviceState.getOnuStateInfo()).thenReturn(null);
        when(m_connectionState.isConnected()).thenReturn(true);
        voltManagement.processNotification(m_notificationResponse);
        verify(m_deviceManager, never()).updateConfigAlignmentState(ONU_NAME , DeviceManagerNSConstants.ALIGNED);
        verify(m_connectionManager, never()).getConnectionState(m_vonuDevice);
        verify(m_alarmService, times(1)).raiseAlarm(any());
        verify(m_alarmService, never()).clearAlarm(any());
    }

    @Test
    @Ignore
    public void testProcessNotificationIetfAlarmsAlarmNotificationCleared() throws MessageFormatterException {
        VOLTManagementImpl voltManagement = new VOLTManagementImpl(m_txService, m_deviceManager, m_alarmService, m_kafkaProducerJson, m_unknownOnuHandler,
                m_connectionManager, m_modelNodeDSM, m_notificationService, m_adapterManager, m_pmaRegistry, m_schemaRegistry, m_gpbFormatter, m_networkFunctionDao,
                m_deviceDao, m_nbiNetconfServerMessageListener);
        when(m_deviceManager.getDevice(ONU_NAME)).thenReturn(m_vonuDevice);
        when(m_responseData.getIdentifier()).thenReturn(ONU_ID);
        when(m_responseData.getSenderName()).thenReturn(SENDER_VOMCI);
        when(m_responseData.getObjectType()).thenReturn(ObjectType.ONU);
        when(m_responseData.getOnuName()).thenReturn(ONU_NAME);
        when(m_responseData.getOperationType()).thenReturn(NetconfResources.NOTIFICATION);
        when(m_responseData.getResponsePayload()).thenReturn(prepareJsonResponseFromFile(ietfAlarmsAlarmNotificationClearedJson));
        when(m_gpbFormatter.getResponseData(any())).thenReturn(m_responseData);
        when(m_gpbFormatter.getResponseData(m_notificationResponse)).thenReturn(m_responseData);
        when(m_connectionManager.getConnectionState(m_vonuDevice)).thenReturn(m_connectionState);
        when(m_vonuDevice.getDeviceManagement()).thenReturn(m_deviceMgmt);
        when(m_deviceMgmt.getOnuConfigInfo()).thenReturn(m_onuConfigInfo);
        when(m_onuConfigInfo.getExpectedSerialNumber()).thenReturn(ONU_SERIAL_NUMBER);
        when(m_deviceMgmt.getDeviceState()).thenReturn(m_deviceState);
        when(m_deviceState.getOnuStateInfo()).thenReturn(null);
        when(m_connectionState.isConnected()).thenReturn(true);
        voltManagement.processNotification(m_notificationResponse);
        verify(m_deviceManager, never()).updateConfigAlignmentState(ONU_NAME, DeviceManagerNSConstants.ALIGNED);
        verify(m_connectionManager, never()).getConnectionState(m_vonuDevice);
        verify(m_alarmService, times(1)).clearAlarm(any());
        verify(m_alarmService, never()).raiseAlarm(any());
    }

    @Test
    @Ignore
    public void testOnuNotificationProcessForDeviceNull() throws Exception {
        String serialNumber = "ABCD12345678";
        String registrationId = "REG123";
        String oltName = "OLT1";
        String channelPartition = "CT_1";
        String vaniName = "vomci-use";
        String onuId = "1";
        String onuAuthStatus = ONU_PRESENT_AND_ON_INTENDED_CHANNEL_TERMINATION;
        when(m_onuNotification.getOltDeviceName()).thenReturn(oltName);
        when(m_onuNotification.getChannelTermRef()).thenReturn(channelPartition);
        when(m_onuNotification.getOnuId()).thenReturn(onuId);
        when(m_onuNotification.getSerialNo()).thenReturn(serialNumber);
        when(m_onuNotification.getRegId()).thenReturn(registrationId);
        when(m_onuNotification.getVAniRef()).thenReturn(vaniName);
        when(m_onuNotification.getOnuState()).thenReturn(onuAuthStatus);
        when(m_onuNotification.getDeterminedOnuManagementMode()).thenReturn(RELYING_ON_VOMCI);
        when(m_deviceManager.getDeviceWithSerialNumber(ONU_SERIAL_NUMBER)).thenReturn(null);
        GetRequest getRequest = VOLTMgmtRequestCreationUtil.prepareGetRequestForVani(oltName, vaniName);
        VOLTManagementUtil.setMessageId(getRequest, m_messageId);
        when(m_connectionManager.executeNetconf(m_oltDevice, getRequest)).thenReturn(m_responseFuture);
        when(m_responseFuture.get()).thenReturn(m_netConfResponse);
        when(m_netConfResponse.getMessageId()).thenReturn(String.valueOf(m_messageId));
        when(m_netConfResponse.getData()).thenReturn(m_dataElement);
        when(m_onuNotification.getMappedEvent()).thenReturn(ONUConstants.CREATE_ONU);
        when(m_onuNotification.getDeterminedOnuManagementMode()).thenReturn(RELYING_ON_VOMCI);
        when(m_oltDevice.getDeviceName()).thenReturn("OLT1");
        when(m_deviceManager.getDevice(anyString())).thenReturn(m_oltDevice);
        m_voltManagement.onuNotificationProcess(m_onuNotification, oltName);
        verify(m_onuNotification, times(5)).getSerialNo();
        verify(m_onuNotification, times(7)).getDeterminedOnuManagementMode();
        verify(m_notificationService, times(1)).sendNotification(any(), any());
        verify(m_vonuDevice, never()).getDeviceManagement();
    }

    @Test
    public void testOnuNotificationProcessForEOMCI() throws Exception {
        String serialNumber = "ABCD12345678";
        String oltName = "OLT1";
        String vaniName = "vomci-use";
        String registrationId = "REG123";
        String channelPartition = "CT_1";
        String onuId = "1";
        String onuName = "ONU1";
        String onuType = "ONU";
        Set<ExpectedAttachmentPoint> expectedAttachmentPointSet = new HashSet<>();
        expectedAttachmentPointSet.add(m_expectedAttachmentPoint);
        String onuAuthStatus = ONU_PRESENT_AND_ON_INTENDED_CHANNEL_TERMINATION;
        when(m_onuNotification.getSerialNo()).thenReturn(serialNumber);
        when(m_onuNotification.getOnuState()).thenReturn(onuAuthStatus);
        when(m_onuNotification.getDeterminedOnuManagementMode()).thenReturn(RELYING_ON_VOMCI);
        when(m_vonuDevice.getDeviceManagement()).thenReturn(m_deviceMgmt);
        when(m_vonuDevice.getDeviceName()).thenReturn(onuName);
        when(m_vonuDevice.isMediatedSession()).thenReturn(true);
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
        when(m_deviceManager.getDeviceWithSerialNumber(ONU_SERIAL_NUMBER)).thenReturn(m_vonuDevice);
        when(m_vonuDevice.isMediatedSession()).thenReturn(true);
        when(m_vonuDevice.getDeviceManagement()).thenReturn(m_deviceMgmt);
        when(m_deviceMgmt.getOnuConfigInfo()).thenReturn(m_onuConfigInfo);
        when(m_onuConfigInfo.getExpectedAttachmentPoints()).thenReturn(m_expectedAttachmentPoints);
        when(m_onuConfigInfo.getPlannedOnuManagementMode()).thenReturn(USE_EOMCI);
        when(m_expectedAttachmentPoints.getExpectedAttachmentPointSet()).thenReturn(expectedAttachmentPointSet);
        when(m_onuNotification.getMappedEvent()).thenReturn(ONUConstants.CREATE_ONU);
        when(m_deviceManager.getDevice(anyString())).thenReturn(m_vonuDevice);
        GetRequest getRequest = VOLTMgmtRequestCreationUtil.prepareGetRequestForVani(oltName, vaniName);
        VOLTManagementUtil.setMessageId(getRequest, m_messageId);
        when(m_connectionManager.executeNetconf(anyString(), any(GetRequest.class))).thenReturn(m_responseFuture);
        when(m_responseFuture.get()).thenReturn(m_netConfResponse);
        when(m_netConfResponse.getMessageId()).thenReturn(String.valueOf(m_messageId));
        when(m_netConfResponse.getData()).thenReturn(m_dataElement);
        m_voltManagement.onuNotificationProcess(m_onuNotification, oltName);
        verify(m_notificationService, times(1)).sendNotification(any(), any());
        verify(m_vonuDevice, times(15)).getDeviceManagement();
        verify(m_onuNotification, never()).getOltDeviceName();
    }

    @Test
    public void testOnuNotificationProcessForLegacyOLT() {
        String oltName = "OLT1";
        String serialNumber = "ABCD12345678";
        String vaniName = "vomci-use";
        String registrationId = "REG123";
        String channelPartition = "CT_1";
        String onuId = "1";
        String onuName = "ONU1";
        String onuType = "ONU";
        when(m_onuNotification.getSerialNo()).thenReturn(serialNumber);
        when(m_onuNotification.getDeterminedOnuManagementMode()).thenReturn("");
        when(m_deviceManager.getDeviceWithSerialNumber(ONU_SERIAL_NUMBER)).thenReturn(m_vonuDevice);
        when(m_vonuDevice.isMediatedSession()).thenReturn(true);
        when(m_onuNotification.getMappedEvent()).thenReturn(CREATE_ONU);
        when(m_vonuDevice.getDeviceManagement()).thenReturn(m_deviceMgmt);
        when(m_deviceMgmt.getOnuConfigInfo()).thenReturn(m_onuConfigInfo);
        when(m_vonuDevice.isAligned()).thenReturn(false);
        when(m_onuConfigInfo.getExpectedAttachmentPoints()).thenReturn(m_expectedAttachmentPoints);
        when(m_vonuDevice.getDeviceManagement()).thenReturn(m_deviceMgmt);
        when(m_vonuDevice.getDeviceName()).thenReturn(onuName);
        when(m_vonuDevice.isMediatedSession()).thenReturn(true);
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

        OnuManagementChain[] onuManagementChainArray = new OnuManagementChain[3];
        OnuManagementChain onuManagementChain1 = new OnuManagementChain();
        onuManagementChain1.setNfName("vomci1");
        onuManagementChain1.setInsertOrder(0);
        OnuManagementChain onuManagementChain2 = new OnuManagementChain();
        onuManagementChain2.setNfName("proxy1");
        onuManagementChain2.setInsertOrder(1);
        OnuManagementChain onuManagementChain3 = new OnuManagementChain();
        onuManagementChain3.setNfName(oltName);
        onuManagementChain3.setInsertOrder(2);
        onuManagementChainArray[0] = onuManagementChain1;
        onuManagementChainArray[1] = onuManagementChain2;
        onuManagementChainArray[2] = onuManagementChain3;
        when(m_deviceDao.getOnuManagementChains(onuName)).thenReturn(onuManagementChainArray);

        m_voltManagement.onuNotificationProcess(m_onuNotification, oltName);
        verify(m_notificationService, never()).sendNotification(any(), any());
        verify(m_onuNotification, times(6)).getDeterminedOnuManagementMode();
        verify(m_vonuDevice, times(1)).isMediatedSession();
        verify(m_onuNotification, times(2)).getChannelTermRef();
    }

    @Test
    public void testOnuNotificationProcessForLegacyOLTWhenOnuIsNull() {
        String oltName = "OLT1";
        when(m_onuNotification.getDeterminedOnuManagementMode()).thenReturn("");
        when(m_onuNotification.getMappedEvent()).thenReturn(CREATE_ONU);
        m_voltManagement.onuNotificationProcess(m_onuNotification, oltName);
        verify(m_notificationService, never()).sendNotification(any(), any());
        verify(m_onuNotification, times(6)).getDeterminedOnuManagementMode();
    }

    @Test
    @Ignore
    public void testOnuNotificationProcessForUnableToAuthOnu() throws Exception {
        String serialNumber = "ABCD12345678";
        String oltName = "OLT1";
        String vaniName = "vomci-use";
        String registrationId = "REG123";
        String channelPartition = "CT_1";
        String onuId = "1";
        String onuName = "ONU1";
        String onuType = "ONU";
        Set<ExpectedAttachmentPoint> expectedAttachmentPointSet = new HashSet<>();
        expectedAttachmentPointSet.add(m_expectedAttachmentPoint);
        String onuAuthStatus = UNABLE_TO_AUTHENTICATE_ONU;
        when(m_onuNotification.getSerialNo()).thenReturn(serialNumber);
        when(m_onuNotification.getOnuState()).thenReturn(onuAuthStatus);
        when(m_vonuDevice.getDeviceManagement()).thenReturn(m_deviceMgmt);
        when(m_vonuDevice.getDeviceName()).thenReturn(onuName);
        when(m_vonuDevice.isMediatedSession()).thenReturn(true);
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
        when(m_deviceManager.getDeviceWithSerialNumber(ONU_SERIAL_NUMBER)).thenReturn(null);
        when(m_vonuDevice.isMediatedSession()).thenReturn(true);
        when(m_vonuDevice.getDeviceManagement()).thenReturn(m_deviceMgmt);
        when(m_deviceMgmt.getOnuConfigInfo()).thenReturn(m_onuConfigInfo);
        when(m_onuConfigInfo.getExpectedAttachmentPoints()).thenReturn(m_expectedAttachmentPoints);
        when(m_onuConfigInfo.getPlannedOnuManagementMode()).thenReturn(USE_VOMCI);
        when(m_expectedAttachmentPoints.getExpectedAttachmentPointSet()).thenReturn(expectedAttachmentPointSet);
        when(m_onuNotification.getMappedEvent()).thenReturn(ONUConstants.CREATE_ONU);
        when(m_onuNotification.getDeterminedOnuManagementMode()).thenReturn(RELYING_ON_VOMCI);
        when(m_deviceManager.getDevice(anyString())).thenReturn(m_vonuDevice);
        GetRequest getRequest = VOLTMgmtRequestCreationUtil.prepareGetRequestForVani(oltName, vaniName);
        VOLTManagementUtil.setMessageId(getRequest, m_messageId);
        when(m_connectionManager.executeNetconf(any(Device.class), any(GetRequest.class))).thenReturn(m_responseFuture);
        when(m_responseFuture.get()).thenReturn(m_netConfResponse);
        when(m_netConfResponse.getMessageId()).thenReturn(String.valueOf(m_messageId));
        when(m_netConfResponse.getData()).thenReturn(m_dataElement);
        when(m_netConfResponse.getErrors()).thenReturn(m_netconfRpcErrorList);
        when(m_netconfRpcErrorList.get(0)).thenReturn(m_netconfRpcError);
        when(m_netconfRpcError.getErrorAppTag()).thenReturn(ONU_MGMT_MODE_MISMATCH_WITH_VANI);
        m_voltManagement.onuNotificationProcess(m_onuNotification, oltName);
        verify(m_notificationService, times(1)).sendNotification(any(), any());
        verify(m_vonuDevice, never()).getDeviceManagement();
        verify(m_onuNotification, times(1)).getOltDeviceName();
    }

    @Test
    public void testOnuNotificationProcessForVOMCI() throws Exception {
        String serialNumber = "ABCD12345678";
        String oltName = "OLT1";
        String vaniName = "vomci-use";
        String registrationId = "REG123";
        String channelPartition = "CT_1";
        String onuId = "1";
        String onuName = "ONU1";
        String onuType = "ONU";
        Set<ExpectedAttachmentPoint> expectedAttachmentPointSet = new HashSet<>();
        expectedAttachmentPointSet.add(m_expectedAttachmentPoint);
        String onuAuthStatus = ONU_PRESENT_AND_ON_INTENDED_CHANNEL_TERMINATION;
        when(m_onuNotification.getSerialNo()).thenReturn(serialNumber);
        when(m_onuNotification.getOnuState()).thenReturn(onuAuthStatus);
        when(m_vonuDevice.getDeviceManagement()).thenReturn(m_deviceMgmt);
        when(m_vonuDevice.getDeviceName()).thenReturn(onuName);
        when(m_vonuDevice.isMediatedSession()).thenReturn(true);
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
        when(m_deviceManager.getDeviceWithSerialNumber(ONU_SERIAL_NUMBER)).thenReturn(m_vonuDevice);
        when(m_vonuDevice.isMediatedSession()).thenReturn(true);
        when(m_vonuDevice.getDeviceManagement()).thenReturn(m_deviceMgmt);
        when(m_deviceMgmt.getOnuConfigInfo()).thenReturn(m_onuConfigInfo);
        when(m_onuConfigInfo.getExpectedAttachmentPoints()).thenReturn(m_expectedAttachmentPoints);
        when(m_onuConfigInfo.getPlannedOnuManagementMode()).thenReturn(USE_VOMCI);
        when(m_expectedAttachmentPoints.getExpectedAttachmentPointSet()).thenReturn(expectedAttachmentPointSet);
        when(m_onuNotification.getMappedEvent()).thenReturn(ONUConstants.CREATE_ONU);
        when(m_onuNotification.getDeterminedOnuManagementMode()).thenReturn(EOMCI_BEING_USED);
        when(m_deviceManager.getDevice(anyString())).thenReturn(m_vonuDevice);
        GetRequest getRequest = VOLTMgmtRequestCreationUtil.prepareGetRequestForVani(oltName, vaniName);
        VOLTManagementUtil.setMessageId(getRequest, m_messageId);
        when(m_connectionManager.executeNetconf(anyString(), any(GetRequest.class))).thenReturn(m_responseFuture);
        when(m_responseFuture.get()).thenReturn(m_netConfResponse);
        when(m_netConfResponse.getMessageId()).thenReturn(String.valueOf(m_messageId));
        when(m_netConfResponse.getData()).thenReturn(m_dataElement);
        m_voltManagement.onuNotificationProcess(m_onuNotification, oltName);
        verify(m_notificationService, times(1)).sendNotification(any(), any());
        verify(m_vonuDevice, times(15)).getDeviceManagement();
        verify(m_onuNotification, never()).getOltDeviceName();
    }

    private Notification getDetectNotification(String serialNum, String regId, String chanTermref, String vaniRef,
                                               String onuId, String onuState) throws NetconfMessageBuilderException {
        String onuNotification = "<notification xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">\n" +
                "    <eventTime>2019-07-25T05:53:36+00:00</eventTime>\n" +
                "    <bbf-xpon-onu-states:onu-state-change xmlns:bbf-xpon-onu-states=\"urn:bbf:yang:bbf-xpon-onu-states\">                               \n" +
                "        <bbf-xpon-onu-states:detected-serial-number>" + serialNum + "</bbf-xpon-onu-states:detected-serial-number>\n" +
                "        <bbf-xpon-onu-states:onu-id>" + onuId + "</bbf-xpon-onu-states:onu-id>\n" +
                "        <bbf-xpon-onu-states:channel-termination-ref>" + chanTermref + "</bbf-xpon-onu-states:channel-termination-ref>\n" +
                "        <bbf-xpon-onu-states:onu-state>" + onuState + "</bbf-xpon-onu-states:onu-state>\n" +
                "        <bbf-xpon-onu-states:detected-registration-id>" + regId + "</bbf-xpon-onu-states:detected-registration-id>\n" +
                "        <bbf-xpon-onu-states:onu-state-last-change>2019-07-25T05:53:36+00:00</bbf-xpon-onu-states:onu-state-last-change>\n" +
                "        <bbf-xpon-onu-states:v-ani-ref>" + vaniRef + "</bbf-xpon-onu-states:v-ani-ref>\n" +
                "    </bbf-xpon-onu-states:onu-state-change>\n" +
                "</notification>";
        Notification notification = new NetconfNotification(DocumentUtils.stringToDocument(onuNotification));
        return notification;
    }

    private Set<SoftwareImage> prepareSwImageSet(String parentId) {
        Set<SoftwareImage> softwareImageSet = new HashSet<>();
        SoftwareImage softwareImage0 = new SoftwareImage();
        softwareImage0.setId(0);
        softwareImage0.setParentId(parentId);
        softwareImage0.setHash("00");
        softwareImage0.setProductCode("test");
        softwareImage0.setVersion("0000");
        softwareImage0.setIsValid(true);
        softwareImage0.setIsCommitted(true);
        softwareImage0.setIsActive(true);
        SoftwareImage softwareImage1 = new SoftwareImage();
        softwareImage1.setId(1);
        softwareImage1.setParentId(parentId);
        softwareImage1.setHash("11");
        softwareImage1.setProductCode("test1111");
        softwareImage1.setVersion("1111");
        softwareImage1.setIsValid(true);
        softwareImage1.setIsCommitted(false);
        softwareImage1.setIsActive(false);
        softwareImageSet.add(softwareImage0);
        softwareImageSet.add(softwareImage1);
        return softwareImageSet;
    }

    private String getEqptId(JSONObject jsonResponse) {
        JSONObject payloadJson = new JSONObject(jsonResponse.getString(ONUConstants.PAYLOAD_JSON_KEY));
        String data = payloadJson.optString(ONUConstants.DATA_JSON_KEY);
        JSONObject dataJson = new JSONObject(data);
        return dataJson.getJSONObject(ONUConstants.NETWORK_MANAGER_JSON_KEY)
                .getJSONObject(ONUConstants.DEVICE_STATE_JSON_KEY).getJSONObject(ONUConstants.ONU_STATE_INFO_JSON_KEY)
                .optString(ONUConstants.EQPT_ID_JSON_KEY);
    }

    private String prepareJsonResponseFromFile(String name) {
        return TestUtil.loadAsString(name).trim();
    }

    private void setupGpbFormatter() {
        m_messageFormatter = new GpbFormatter();
        m_voltManagement = new VOLTManagementImpl(m_txService, m_deviceManager, m_alarmService, m_kafkaProducerGpb, m_unknownOnuHandler,
                m_connectionManager, m_modelNodeDSM, m_notificationService, m_adapterManager, m_pmaRegistry, m_schemaRegistry, m_messageFormatter, m_networkFunctionDao,
                m_deviceDao, m_nbiNetconfServerMessageListener);
        m_voltManagement.setKafkaConsumer(m_onuKafkaConsumerGpb);
        m_voltManagement.init();
    }

    private Msg prepareGetResponseGpb(String getResponse) {
        Msg msg = Msg.newBuilder()
                .setHeader(Header.newBuilder()
                        .setMsgId("0")
                        .setSenderName("vomci-vendor-1")
                        .setRecipientName("vOLTMF")
                        .setObjectName("ont1")
                        .setObjectType(OBJECT_TYPE.ONU)
                        .build())
                .setBody(Body.newBuilder()
                        .setResponse(Response.newBuilder()
                                .setGetResp(GetDataResp.newBuilder()
                                        .setData(ByteString.copyFromUtf8(getResponse))
                                )
                                .build()))
                .build();
        return msg;
    }

    private Msg prepareGetRequestGpb(String getRequest) {
        Msg msg = Msg.newBuilder()
                .setHeader(Header.newBuilder()
                        .setMsgId("0")
                        .setSenderName("vOLTMF")
                        .setRecipientName("vomci-vendor-1")
                        .setObjectName("ont1")
                        .setObjectType(OBJECT_TYPE.ONU)
                        .build())
                .setBody(Body.newBuilder()
                        .setRequest(Request.newBuilder()
                                .setGetData(GetData.newBuilder()
                                        .addFilter(ByteString.copyFromUtf8(getRequest))
                                )
                                .build()))
                .build();

        return msg;
    }

}
