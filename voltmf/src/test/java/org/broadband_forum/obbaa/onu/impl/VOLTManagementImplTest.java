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
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

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
import org.broadband_forum.obbaa.dmyang.entities.OnuConfigInfo;
import org.broadband_forum.obbaa.dmyang.entities.OnuManagementChain;
import org.broadband_forum.obbaa.dmyang.entities.OnuStateInfo;
import org.broadband_forum.obbaa.dmyang.entities.SoftwareImage;
import org.broadband_forum.obbaa.dmyang.entities.SoftwareImages;
import org.broadband_forum.obbaa.netconf.alarm.api.AlarmService;
import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfNotification;
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
import org.broadband_forum.obbaa.onu.message.JsonFormatter;
import org.broadband_forum.obbaa.onu.message.MessageFormatter;
import org.broadband_forum.obbaa.onu.message.NetworkWideTag;
import org.broadband_forum.obbaa.onu.message.ObjectType;
import org.broadband_forum.obbaa.onu.message.ResponseData;
import org.broadband_forum.obbaa.onu.message.gpb.message.Msg;
import org.broadband_forum.obbaa.onu.notification.ONUNotification;
import org.broadband_forum.obbaa.onu.util.JsonUtil;
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
    String ExpectedGetRequest = "{\n" +
            "    \"olt-name\": \"OLT1\",\n" +
            "    \"channel-termination-ref\": \"channeltermination.1\",\n" +
            "    \"event\": \"request\",\n" +
            "    \"payload\": \"{\\\"operation\\\":\\\"get\\\",\\\"identifier\\\":\\\"0\\\",\\\"filters\\\":{\\\"network-manager:device-management\\\":{\\\"device-state\\\":{\\\"bbf-obbaa-onu-management:onu-state-info\\\":{\\\"equipment-id\\\":\\\"\\\",\\\"software-images\\\":{\\\"software-image\\\":{}}}}}}\",\n" +
            "    \"onu-id\": \"1\",\n" +
            "    \"labels\": \"{\\\"name\\\":\\\"vendor\\\",\\\"value\\\":\\\"ABCD\\\"}\"\n" +
            "}";
    String getResponseNOK = "{\"olt-name\":\"OLT1\",\"payload\":\"{\\\"identifier\\\":\\\"0\\\",\\\"operation\\\":\\\"get\\\",\\\"data\\\":{\\\"network-manager:device-management\\\":{\\\"device-state\\\":{\\\"bbf-obbaa-onu-management:onu-state-info\\\":{\\\"equipment-id\\\":\\\"test1\\\",\\\"software-images\\\":{\\\"software-image\\\":[{\\\"id\\\":\\\"1\\\",\\\"version\\\":\\\"1111\\\",\\\"is-committed\\\":\\\"false\\\",\\\"is-active\\\":\\\"false\\\",\\\"is-valid\\\":\\\"true\\\",\\\"product-code\\\":\\\"test1111\\\",\\\"hash\\\":\\\"11\\\"},{\\\"id\\\":\\\"0\\\",\\\"version\\\":\\\"0000\\\",\\\"is-committed\\\":\\\"true\\\",\\\"is-active\\\":\\\"true\\\",\\\"is-valid\\\":\\\"true\\\",\\\"product-code\\\":\\\"test\\\",\\\"hash\\\":\\\"00\\\"}]}}}}},\\\"status\\\":\\\"NOK\\\"}\",\"onu-name\":\"onu\",\"channel-termination-ref\":\"channeltermination.1\",\"event\": \"response\",\"onu-id\": \"1\"}";
    String emptyGetResponse = "{\"olt-name\":\"OLT1\",\"payload\":\"{\\\"identifier\\\":\\\"0\\\",\\\"operation\\\":\\\"get\\\",\\\"data\\\":{\\\"network-manager:device-management\\\":{\\\"device-state\\\":{\\\"bbf-obbaa-onu-management:onu-state-info\\\":{\\\"software-images\\\":{\\\"software-image\\\":[]}}}}},\\\"status\\\":\\\"OK\\\"}\",\"onu-name\":\"onu\",\"channel-termination-ref\":\"channeltermination.1\",\"event\": \"response\",\"onu-id\": \"1\"}";
    @Mock
    KafkaTopic m_kafkaTopic;
    @Mock
    private Device m_vonuDevice;
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

    private OnuKafkaConsumer<String> m_onuKafkaConsumerJson = PowerMockito.mock(OnuKafkaConsumerJson.class);
    private OnuKafkaConsumer<Msg> m_onuKafkaConsumerGpb = PowerMockito.mock(OnuKafkaConsumerGpb.class);

    String onuAlignmentAlignedNoitifcationJson = "/onu-alignment-status-notification-aligned.json";
    String onuAlignmentMisalignedNoitifcationJson = "/onu-alignment-status-notification-misaligned.json";

    private final String ONU_SERIAL_NUMBER = "ABCD12345678";
    private final String ONU_NAME = "onu1";
    private final String SENDER_VOMCI = "vomci1";
    private final String ONU_ID = "1";

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        m_unknownOnu = new UnknownONU();
        m_softwareImages = new SoftwareImages();
        m_txService = new TxService();
        m_messageFormatter = new JsonFormatter();
        m_voltManagement = new VOLTManagementImpl(m_txService, m_deviceManager, m_alarmService, m_kafkaProducerJson, m_unknownOnuHandler,
                m_connectionManager, m_modelNodeDSM, m_notificationService, m_adapterManager, m_pmaRegistry, m_schemaRegistry, m_messageFormatter, m_networkFunctionDao, m_deviceDao);
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
        when(m_onuConfigInfo.getExpectedAttachmentPoint()).thenReturn(m_attachmentPoint);
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
        when(m_onuConfigInfo.getExpectedAttachmentPoint()).thenReturn(m_attachmentPoint);
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
    public void testOnuDeviceAddedForUnknownOnuPresentWithGpbFormatter() throws MessageFormatterException, NetconfMessageBuilderException {
        HashSet<String> kafkaTopicNameSet = new HashSet<>();
        kafkaTopicNameSet.add("vomci-proxy-request1");
        kafkaTopicNameSet.add("vomci-proxy-request2");
        m_messageFormatter = PowerMockito.mock(GpbFormatter.class);
        m_voltManagement = new VOLTManagementImpl(m_txService, m_deviceManager, m_alarmService, m_kafkaProducerGpb, m_unknownOnuHandler,
                m_connectionManager, m_modelNodeDSM, m_notificationService, m_adapterManager, m_pmaRegistry, m_schemaRegistry, m_messageFormatter, m_networkFunctionDao, m_deviceDao);
        m_voltManagement.init();
        m_voltManagement.setKafkaConsumer(m_onuKafkaConsumerGpb);
        when(m_deviceManager.getDevice(deviceName)).thenReturn(m_vonuDevice);
        when(m_vonuDevice.getDeviceManagement()).thenReturn(m_deviceMgmt);
        when(m_deviceMgmt.getDeviceState()).thenReturn(m_deviceState);
        when(m_deviceMgmt.getDeviceType()).thenReturn(VoltMFTestConstants.ONU_DEVICE_TYPE);
        when(m_deviceMgmt.getOnuConfigInfo()).thenReturn(m_onuConfigInfo);
        when(m_deviceState.getDeviceNodeId()).thenReturn("");
        when(m_onuConfigInfo.getExpectedSerialNumber()).thenReturn(VoltMFTestConstants.SERIAL_NUMBER);
        when(m_onuConfigInfo.getExpectedRegistrationId()).thenReturn(VoltMFTestConstants.REGISTER_NUMBER);
        when(m_onuConfigInfo.getExpectedAttachmentPoint()).thenReturn(m_attachmentPoint);
        when(m_attachmentPoint.getChannelPartitionName()).thenReturn("CT");
        when(m_vonuDevice.isMediatedSession()).thenReturn(true);
        when(m_unknownOnuHandler.findUnknownOnuEntity(anyString(), anyString())).thenReturn(m_unknownOnu);
        when(m_networkFunctionDao.getKafkaConsumerTopics(any())).thenReturn(m_kafkaTopicSet);
        OnuManagementChain chain1 = new OnuManagementChain();
        chain1.setOnuManagementChain("vomci1");
        OnuManagementChain chain2 = new OnuManagementChain();
        chain2.setOnuManagementChain("olt1");
        OnuManagementChain[] managementChains = {chain1, chain2};
        when(m_deviceDao.getOnuManagementChains(deviceName)).thenReturn(managementChains);
        when(m_networkFunctionDao.getKafkaTopicNames(any(), any())).thenReturn(kafkaTopicNameSet);
        Object formattedMessage = Msg.newBuilder().build();
        when(m_messageFormatter.getFormattedRequest(any(), anyString(), any(), any(), any(), any(), any())).thenReturn(formattedMessage);

        m_voltManagement.deviceAdded(deviceName);
        m_voltManagement.waitForNotificationTasks();

        verify(m_kafkaProducerGpb, times(4)).sendNotification(anyString(), any());
        verify(m_unknownOnuHandler, times(1)).deleteUnknownOnuEntity(m_unknownOnu);
    }


    @Test
    public void testProcessResponseWhenDeviceNull() {
        Device device = null;
        when(m_deviceManager.getDevice(deviceName)).thenReturn(device);
        m_voltManagement.processNotificationResponse(deviceName, VoltMFTestConstants.ONU_GET_OPERATION, VoltMFTestConstants.DEFAULT_MESSAGE_ID,
                VoltMFTestConstants.OK_RESPONSE, null, null);
        verify(m_connectionManager, never()).addMediatedDeviceNetconfSession(m_vonuDevice, null);
    }

    //VOMCI functions is currently not supporting GET request handling.
    @Ignore
    @Test
    public void testProcessDetectResponseWhenDevicePresentSendsGetRequest() throws NetconfMessageBuilderException, MessageFormatterException {
        Document filter = DocumentUtils.stringToDocument(VoltMFTestConstants.FILTER_GET_REQUEST);
        ArrayList filterList = new ArrayList();

        when(m_deviceManager.getDevice(deviceName)).thenReturn(m_vonuDevice);
        when(m_vonuDevice.getDeviceManagement()).thenReturn(m_deviceMgmt);
        when(m_deviceMgmt.getOnuConfigInfo()).thenReturn(m_onuConfigInfo);
        when(m_onuConfigInfo.getExpectedSerialNumber()).thenReturn(VoltMFTestConstants.SERIAL_NUMBER);
        when(m_onuConfigInfo.getExpectedAttachmentPoint()).thenReturn(m_attachmentPoint);
        when(m_deviceMgmt.getDeviceState()).thenReturn(m_deviceState);
        when(m_deviceState.getOnuStateInfo()).thenReturn(m_onuStateInfo);
        when(m_onuStateInfo.getActualAttachmentPoint()).thenReturn(m_actualAttachmentPoint);
        when(m_actualAttachmentPoint.getOltName()).thenReturn(VoltMFTestConstants.OLT_NAME);
        when(m_actualAttachmentPoint.getOnuId()).thenReturn(VoltMFTestConstants.ONU_ID_VALUE);
        when(m_actualAttachmentPoint.getChannelTerminationRef()).thenReturn(VoltMFTestConstants.CHANNEL_TERMINATION);

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
        verify(m_connectionManager, times(1)).addMediatedDeviceNetconfSession(any(), any());
        verify(m_getRequestOne, times(1)).getFilter();
        verify(m_kafkaProducerJson, times(1)).sendNotification(VoltMFTestConstants.ONU_REQUEST_KAFKA_TOPIC, VoltMFTestConstants.GET_REQUEST_RESPONSE);
    }

    @Test
    public void testProcessOnuUndetectWithOkResponse() {
        when(m_deviceManager.getDevice(deviceName)).thenReturn(m_vonuDevice);
        m_voltManagement.processNotificationResponse(deviceName, VoltMFTestConstants.ONU_UNDETECTED, VoltMFTestConstants.DEFAULT_MESSAGE_ID,
                VoltMFTestConstants.OK_RESPONSE, null, null);
        verify(m_deviceManager, times(1)).updateConfigAlignmentState(deviceName, VoltMFTestConstants.NEVER_ALIGNED);
        verify(m_connectionManager, never()).addMediatedDeviceNetconfSession(m_vonuDevice, null);
    }

    @Test
    public void testProcessOnuUndetectWithNotOkResponse() {
        when(m_deviceManager.getDevice(deviceName)).thenReturn(m_vonuDevice);
        m_voltManagement.processNotificationResponse(deviceName, VoltMFTestConstants.ONU_UNDETECTED, VoltMFTestConstants.DEFAULT_MESSAGE_ID,
                VoltMFTestConstants.NOK_RESPONSE, null, null);

        verify(m_deviceManager, never()).updateConfigAlignmentState(deviceName, VoltMFTestConstants.NEVER_ALIGNED);
        verify(m_connectionManager, never()).addMediatedDeviceNetconfSession(m_vonuDevice, null);
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
    public void testInternalOKGetResponseCreatedONU() {
        JSONObject jsonResponse = new JSONObject(getResponse);
        when(m_deviceManager.getDevice(deviceName)).thenReturn(m_vonuDevice);
        when(m_deviceMgmt.getDeviceState()).thenReturn(m_deviceState);
        when(m_deviceDao.getDeviceState(deviceName)).thenReturn(m_deviceState);
        when(m_deviceState.getOnuStateInfo()).thenReturn(m_onuStateInfo);
        when(m_connectionManager.getMediatedDeviceSession(m_vonuDevice)).thenReturn(m_mediatedDeviceNCsession);
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
    public void testInternalGetResponseNOKCreatedONU() {
        JSONObject jsonResponse = new JSONObject(getResponseNOK);
        Set<SoftwareImage> softwareImageSet = new HashSet<SoftwareImage>();
        when(m_deviceManager.getDevice(deviceName)).thenReturn(m_vonuDevice);
        when(m_deviceMgmt.getDeviceState()).thenReturn(m_deviceState);
        when(m_deviceState.getOnuStateInfo()).thenReturn(m_onuStateInfo);
        when(m_connectionManager.getMediatedDeviceSession(m_vonuDevice)).thenReturn(m_mediatedDeviceNCsession);
        m_voltManagement.processResponse(jsonResponse);
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
        verify(m_deviceManager, never()).updateEquipmentIdInOnuStateInfo(eq(deviceName), any());
        verify(m_deviceManager, never()).updateSoftwareImageInOnuStateInfo(eq(deviceName), any());
        JSONObject jsonResponse = new JSONObject(emptyGetResponse);
        Set<SoftwareImage> softwareImageSet = new HashSet<SoftwareImage>();
        when(m_deviceManager.getDevice(deviceName)).thenReturn(m_vonuDevice);
        when(m_deviceMgmt.getDeviceState()).thenReturn(m_deviceState);
        when(m_deviceState.getOnuStateInfo()).thenReturn(m_onuStateInfo);
        when(m_connectionManager.getMediatedDeviceSession(m_vonuDevice)).thenReturn(m_mediatedDeviceNCsession);
        m_voltManagement.processResponse(jsonResponse);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        verify(m_deviceManager, times(1)).updateEquipmentIdInOnuStateInfo(eq(deviceName), eq(getEqptId(jsonResponse)));
        verify(m_deviceManager, never()).updateSoftwareImageInOnuStateInfo(eq(deviceName), eq(softwareImageSet));
    }


    @Test
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
                m_connectionManager, m_modelNodeDSM, m_notificationService, m_adapterManager, m_pmaRegistry, m_schemaRegistry, m_messageFormatter, m_networkFunctionDao, m_deviceDao);
        Set<KafkaTopic> kafkaTopicSet = new HashSet<>();
        kafkaTopicSet.add(m_kafkaTopic);
        when(m_networkFunctionDao.getKafkaConsumerTopics(networkFunctionName)).thenReturn(kafkaTopicSet);
        m_voltManagement.setKafkaConsumer(m_onuKafkaConsumerJson);
        m_voltManagement.networkFunctionAdded(networkFunctionName);
        verify(m_networkFunctionDao, times(1)).getKafkaConsumerTopics(networkFunctionName);
        verify(m_onuKafkaConsumerJson, times(1)).updateSubscriberTopics(any());

        m_voltManagement.networkFunctionRemoved(networkFunctionName);
        verify(m_onuKafkaConsumerJson, times(1)).removeSubscriberTopics(any());
    }

    @Test
    public void testProcessNotificationWhenMisaligned() throws MessageFormatterException {
        VOLTManagementImpl voltManagement = new VOLTManagementImpl(m_txService, m_deviceManager, m_alarmService, m_kafkaProducerJson, m_unknownOnuHandler,
                m_connectionManager, m_modelNodeDSM, m_notificationService, m_adapterManager, m_pmaRegistry, m_schemaRegistry, m_gpbFormatter, m_networkFunctionDao, m_deviceDao);
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
    }

    @Test
    public void testProcessNotificationWhenAligned() throws MessageFormatterException {
        VOLTManagementImpl voltManagement = new VOLTManagementImpl(m_txService, m_deviceManager, m_alarmService, m_kafkaProducerJson, m_unknownOnuHandler,
                m_connectionManager, m_modelNodeDSM, m_notificationService, m_adapterManager, m_pmaRegistry, m_schemaRegistry, m_gpbFormatter, m_networkFunctionDao, m_deviceDao);
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

}
