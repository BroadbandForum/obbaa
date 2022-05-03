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

package org.broadband_forum.obbaa.onu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.broadband_forum.obbaa.device.adapter.AdapterContext;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.device.adapter.AdapterUtils;
import org.broadband_forum.obbaa.dmyang.dao.DeviceDao;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.DeviceMgmt;
import org.broadband_forum.obbaa.nbiadapter.netconf.NbiNetconfServerMessageListener;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.TxService;
import org.broadband_forum.obbaa.nf.dao.NetworkFunctionDao;
import org.broadband_forum.obbaa.nm.devicemanager.DeviceManager;
import org.broadband_forum.obbaa.onu.message.GpbFormatter;
import org.broadband_forum.obbaa.onu.exception.MessageFormatterException;
import org.broadband_forum.obbaa.onu.kafka.producer.OnuKafkaProducerJson;
import org.broadband_forum.obbaa.onu.message.JsonFormatter;
import org.broadband_forum.obbaa.onu.message.MessageFormatter;
import org.broadband_forum.obbaa.onu.util.JsonUtil;
import org.broadband_forum.obbaa.onu.util.VoltMFTestConstants;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.junit.Before;
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
 * Unit tests that tests session for Mediated devices to handle netconf requests and responses
 * <p>
 * Created by Ranjitha.B.R (Nokia) on 22/07/2020.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({JsonUtil.class, QNameModule.class, Optional.class, AdapterUtils.class, AdapterContext.class})
public class MediatedDeviceNetconfSessionTest {
    @Mock
    public static GetRequest m_getRequestOne;
    @Mock
    public static JSONObject m_jsonObject;
    @Mock
    private Device m_mediatedDevice;
    @Mock
    private OnuKafkaProducerJson m_kafkaProducer;
    @Mock
    private ModelNodeDataStoreManager m_modelNodeDSM;
    @Mock
    private AdapterManager m_adapterManager;
    @Mock
    private EditConfigRequest m_editConfigRequest;
    @Mock
    private NetconfFilter m_filter;
    @Mock
    private SchemaRegistry m_schemaRegistry;
    @Mock
    private DeviceManager m_deviceManager;
    @Mock
    private Device m_onuDevice;
    private MessageFormatter m_messageFormatter;

    private ThreadPoolExecutor m_kafkaCommunicationPool;
    private String m_oltDeviceName;
    private String m_onuId;
    private String m_channelTermRef;
    private String m_onuDeviceName;
    private HashMap<String, String> m_labels;
    private String getRequest = VoltMFTestConstants.GET_REQUEST;

    @Mock
    private NetworkFunctionDao m_networkFunctionDao;
    @Mock
    private DeviceDao m_deviceDao;

    private TxService m_txService;


    @Mock
    private DeviceMgmt m_deviceMgmt;
    @Mock
    private NbiNetconfServerMessageListener m_nbiNetconfServerMessageListener;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        m_oltDeviceName = VoltMFTestConstants.OLT_NAME;
        m_onuId = VoltMFTestConstants.ONU_ID_VALUE;
        m_channelTermRef = VoltMFTestConstants.CHANNEL_TERMINATION;
        m_labels = new HashMap<>();
        m_txService = new TxService();
        when(m_onuDevice.getDeviceName()).thenReturn(VoltMFTestConstants.ONU_DEVICE_NAME);
        when(m_deviceManager.getDevice(VoltMFTestConstants.ONU_DEVICE_NAME)).thenReturn(m_onuDevice);
        when(m_editConfigRequest.getMessageId()).thenReturn(VoltMFTestConstants.ONU_EDIT_OPERATION);
        when(m_onuDevice.getDeviceManagement()).thenReturn(m_deviceMgmt);
        when(m_mediatedDevice.getDeviceManagement()).thenReturn(m_deviceMgmt);
        when(m_deviceMgmt.getDeviceType()).thenReturn(VoltMFTestConstants.OLT_DEVICE_TYPE);
        when(m_deviceMgmt.getDeviceModel()).thenReturn(VoltMFTestConstants.DEVICE_MODEL);
        when(m_deviceMgmt.getDeviceVendor()).thenReturn(VoltMFTestConstants.DEVICE_VENDOR);
        when(m_deviceMgmt.getDeviceInterfaceVersion()).thenReturn(VoltMFTestConstants.INTERFACE_VERSION);
        PowerMockito.mockStatic(JsonUtil.class);
        m_messageFormatter = new JsonFormatter();
        m_kafkaCommunicationPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(VoltMFTestConstants.THREAD_POOL_COUNT);
    }

    @Test
    public void testNoSessionRequests() {
        MediatedDeviceNetconfSession session = getNewMDNSessionForNewDevice();

        // Ensure kafka communication pool is never invoked
        //verify(m_kafkaCommunicationPool, never()).offer(anyString(), (Runnable) anyObject());
        assertEquals(0, session.getNumberOfPendingRequests());
        // Verify integrity of the session
        verifySessionIntegrity(session, null, null, null);
    }

    private MediatedDeviceNetconfSession getNewMDNSessionForNewDevice() {
        return new MediatedDeviceNetconfSession(m_mediatedDevice, m_oltDeviceName, m_onuId, m_channelTermRef, m_labels,
                m_kafkaProducer, m_modelNodeDSM, m_adapterManager, m_kafkaCommunicationPool,
                m_schemaRegistry, m_messageFormatter, m_txService, m_networkFunctionDao, m_deviceDao, m_nbiNetconfServerMessageListener);
    }

    private void verifySessionIntegrity(MediatedDeviceNetconfSession session, String messageIdOne, String messageIdTwo,
                                        String messageIdThree) {
        assertTrue(session instanceof NetconfClientSession);
        assertTrue(session.isOpen());
        int count = 0;
        long timeStamp = 0;
        Map<String, TimestampFutureResponse> requestMap = session.getMapOfRequests();
        for (Map.Entry<String, TimestampFutureResponse> entry : requestMap.entrySet()) {
            switch (count) {
                case 0:
                    assertEquals(entry.getKey(), messageIdOne);
                    break;
                case 1:
                    assertEquals(entry.getKey(), messageIdTwo);
                    break;
                case 2:
                    assertEquals(entry.getKey(), messageIdThree);
                    break;
            }
            assertTrue(timeStamp <= entry.getValue().getTimeStamp());
            timeStamp = entry.getValue().getTimeStamp();
            count++;
        }
    }

    @Test
    public void testOnGetDefaultMsgId() throws NetconfMessageBuilderException, MessageFormatterException {
        Document filter = DocumentUtils.stringToDocument(VoltMFTestConstants.FILTER_GET_REQUEST);
        ArrayList filterList = new ArrayList();
        Module module = Mockito.mock(Module.class);
        QNameModule qNameModule = PowerMockito.mock(QNameModule.class);
        Optional<Revision> revision = PowerMockito.mock(Optional.class);
        SchemaPathBuilder schemaPathBuilder = Mockito.mock(SchemaPathBuilder.class);
        SchemaPath schemaPath = Mockito.mock(SchemaPath.class);
        filterList.add(filter.getDocumentElement());
        when(m_getRequestOne.getFilter()).thenReturn(m_filter);
        when(m_getRequestOne.getFilter().getXmlFilterElements()).thenReturn(filterList);
        when(m_getRequestOne.getMessageId()).thenReturn(VoltMFTestConstants.DEFAULT_MESSAGE_ID);
        when(m_getRequestOne.getRequestDocument()).thenReturn(DocumentUtils.stringToDocument(VoltMFTestConstants.INTERNAL_GET_REQUEST_WITH_DEFAULT_MESSAGE_ID));
        when(m_schemaRegistry.getModuleByNamespace(VoltMFTestConstants.NETWORK_MANAGER_NAMESPACE)).thenReturn(module);
        when(m_schemaRegistry.getModuleNameByNamespace(VoltMFTestConstants.NETWORK_MANAGER_NAMESPACE)).thenReturn(VoltMFTestConstants.NETWORK_MANAGER);
        when(module.getQNameModule()).thenReturn(qNameModule);
        when(qNameModule.getRevision()).thenReturn(revision);
        when(schemaPathBuilder.build()).thenReturn(schemaPath);
        DataSchemaNode dataSchemaNode = getDataSchemaNode();
        when(m_schemaRegistry.getDataSchemaNode(any(SchemaPath.class))).thenReturn(dataSchemaNode);
        PowerMockito.when(JsonUtil.convertFromXmlToJsonIgnoreEmptyLeaves(any(), any(), any(), any())).thenReturn(VoltMFTestConstants.FILTER_JSON_STRING);
        MediatedDeviceNetconfSession session = getNewMDNSessionForNewDevice();
        session.onGet(m_getRequestOne);
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        verify(m_getRequestOne, times(3)).getFilter();
        verify(m_jsonObject, never()).getJSONObject(VoltMFTestConstants.OBBAA_NETWORK_MANAGER);
        verify(m_kafkaProducer, times(1)).sendNotification(VoltMFTestConstants.ONU_REQUEST_KAFKA_TOPIC, VoltMFTestConstants.RESPONSE_DEFAULT_MSG_ID);
    }

    @Test
    public void testOnGetNotDefaultMsgId() throws NetconfMessageBuilderException, MessageFormatterException {
        Document filter = DocumentUtils.stringToDocument(VoltMFTestConstants.FILTER_GET_REQUEST);
        ArrayList filterList = new ArrayList();
        AdapterUtils adapterUtils = PowerMockito.mock(AdapterUtils.class);
        AdapterContext adapterContext = Mockito.mock(AdapterContext.class);
        SchemaRegistryImpl schemaRegistryImpl = Mockito.mock(SchemaRegistryImpl.class);
        Module module = Mockito.mock(Module.class);
        QNameModule qNameModule = PowerMockito.mock(QNameModule.class);
        Optional<Revision> revision = PowerMockito.mock(Optional.class);
        SchemaPathBuilder schemaPathBuilder = Mockito.mock(SchemaPathBuilder.class);
        SchemaPath schemaPath = Mockito.mock(SchemaPath.class);
        filterList.add(filter.getDocumentElement());
        when(m_getRequestOne.getFilter()).thenReturn(m_filter);
        when(m_getRequestOne.getFilter().getXmlFilterElements()).thenReturn(filterList);
        when(m_getRequestOne.getMessageId()).thenReturn("1");
        when(m_getRequestOne.getRequestDocument()).thenReturn(DocumentUtils.stringToDocument(VoltMFTestConstants.INTERNAL_GET_REQUEST_WITH_DEFAULT_MESSAGE_ID));
        when(adapterUtils.getAdapterContext(m_onuDevice, m_adapterManager)).thenReturn(adapterContext);
        when(adapterContext.getSchemaRegistry()).thenReturn(schemaRegistryImpl);
        when(schemaRegistryImpl.getModuleByNamespace(VoltMFTestConstants.NETWORK_MANAGER_NAMESPACE)).thenReturn(module);
        when(schemaRegistryImpl.getModuleNameByNamespace(VoltMFTestConstants.NETWORK_MANAGER_NAMESPACE)).thenReturn(VoltMFTestConstants.NETWORK_MANAGER);
        when(module.getQNameModule()).thenReturn(qNameModule);
        when(qNameModule.getRevision()).thenReturn(revision);
        when(schemaPathBuilder.build()).thenReturn(schemaPath);
        DataSchemaNode dataSchemaNode = getDataSchemaNode();
        when(schemaRegistryImpl.getDataSchemaNode(any(SchemaPath.class))).thenReturn(dataSchemaNode);
        PowerMockito.when(JsonUtil.convertFromXmlToJsonIgnoreEmptyLeaves(any(), any(), any(), any())).thenReturn(VoltMFTestConstants.FILTER_JSON_STRING);
        MediatedDeviceNetconfSession session = getNewMDNSessionForNewDevice();
        session.onGet(m_getRequestOne);
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        verify(m_getRequestOne, times(3)).getFilter();
        verify(m_jsonObject, never()).getJSONObject(VoltMFTestConstants.OBBAA_NETWORK_MANAGER);
        verify(m_kafkaProducer, times(1)).sendNotification(VoltMFTestConstants.ONU_REQUEST_KAFKA_TOPIC, VoltMFTestConstants.RESPONSE_NOT_DEFAULT_MSG_ID);
    }

    @Test
    public void testOnGetMsgIdNull() throws NetconfMessageBuilderException, MessageFormatterException {
        String messageId = null;
        when(m_getRequestOne.getFilter()).thenReturn(m_filter);
        when(m_getRequestOne.getMessageId()).thenReturn(messageId);
        when(m_getRequestOne.getRequestDocument()).thenReturn(DocumentUtils.stringToDocument(VoltMFTestConstants.INTERNAL_GET_REQUEST_WITH_DEFAULT_MESSAGE_ID));
        MediatedDeviceNetconfSession session = getNewMDNSessionForNewDevice();
        session.onGet(m_getRequestOne);
        verify(m_getRequestOne, never()).getFilter();
        verify(m_kafkaProducer, never()).sendNotification(VoltMFTestConstants.ONU_REQUEST_KAFKA_TOPIC, VoltMFTestConstants.RESPONSE_NOT_DEFAULT_MSG_ID);
    }

    @Test
    public void testOnGetWithNullFilter() throws NetconfMessageBuilderException, MessageFormatterException {
        NetconfFilter filter = null;
        when(m_getRequestOne.getFilter()).thenReturn(filter);
        when(m_getRequestOne.getMessageId()).thenReturn(VoltMFTestConstants.DEFAULT_MESSAGE_ID);
        when(m_getRequestOne.getRequestDocument()).thenReturn(DocumentUtils.stringToDocument(VoltMFTestConstants.INTERNAL_GET_REQUEST_WITH_DEFAULT_MESSAGE_ID));
        MediatedDeviceNetconfSession session = getNewMDNSessionForNewDevice();
        session.onGet(m_getRequestOne);
        verify(m_kafkaProducer, never()).sendNotification(VoltMFTestConstants.ONU_REQUEST_KAFKA_TOPIC, VoltMFTestConstants.RESPONSE_DEFAULT_MSG_ID);
    }

    @Test
    public void testSendRequestForGetOrEditConfigWithJsonFormatter() throws NetconfMessageBuilderException, MessageFormatterException {
        Document filter = DocumentUtils.stringToDocument(VoltMFTestConstants.FILTER_GET_REQUEST);
        ArrayList filterList = new ArrayList();
        Module module = Mockito.mock(Module.class);
        QNameModule qNameModule = PowerMockito.mock(QNameModule.class);
        Optional<Revision> revision = PowerMockito.mock(Optional.class);
        SchemaPathBuilder schemaPathBuilder = Mockito.mock(SchemaPathBuilder.class);
        SchemaPath schemaPath = Mockito.mock(SchemaPath.class);
        JsonFormatter jsonFormatter = Mockito.mock(JsonFormatter.class);
        GpbFormatter gpbFormatter = Mockito.mock(GpbFormatter.class);
        filterList.add(filter.getDocumentElement());
        when(m_getRequestOne.getFilter()).thenReturn(m_filter);
        when(m_getRequestOne.getFilter().getXmlFilterElements()).thenReturn(filterList);
        when(m_getRequestOne.getMessageId()).thenReturn(VoltMFTestConstants.DEFAULT_MESSAGE_ID);
        when(m_getRequestOne.getRequestDocument()).thenReturn(DocumentUtils.stringToDocument(VoltMFTestConstants.INTERNAL_GET_REQUEST_WITH_DEFAULT_MESSAGE_ID));
        when(m_schemaRegistry.getModuleByNamespace(VoltMFTestConstants.NETWORK_MANAGER_NAMESPACE)).thenReturn(module);
        when(m_schemaRegistry.getModuleNameByNamespace(VoltMFTestConstants.NETWORK_MANAGER_NAMESPACE)).thenReturn(VoltMFTestConstants.NETWORK_MANAGER);
        when(module.getQNameModule()).thenReturn(qNameModule);
        when(qNameModule.getRevision()).thenReturn(revision);
        when(schemaPathBuilder.build()).thenReturn(schemaPath);

        DataSchemaNode dataSchemaNode = getDataSchemaNode();
        when(m_schemaRegistry.getDataSchemaNode(any(SchemaPath.class))).thenReturn(dataSchemaNode);
        PowerMockito.when(JsonUtil.convertFromXmlToJsonIgnoreEmptyLeaves(any(), any(), any(), any())).thenReturn(VoltMFTestConstants.FILTER_JSON_STRING);
        m_messageFormatter = jsonFormatter;
        MediatedDeviceNetconfSession session = getNewMDNSessionForNewDevice();
        session.onGet(m_getRequestOne);
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        verify(jsonFormatter, times(1)).getFormattedRequest(any(),any(), any(), any(), any() ,any(), any());
        verify(gpbFormatter, never()).getFormattedRequest(any(),any(), any(), any(), any() ,any(), any());
        verify(m_jsonObject, never()).getJSONObject(VoltMFTestConstants.OBBAA_NETWORK_MANAGER);
        verify(m_networkFunctionDao, never()).getKafkaConsumerTopics(anyString());
        verify(m_networkFunctionDao, never()).getKafkaTopicNames(any(), any());
    }


    @Test
    public void testSendRequestForGetOrEditConfigWithGPBFormatter() throws NetconfMessageBuilderException, MessageFormatterException {
        HashSet<String> kafkaTopicNameSet = new HashSet<>();
        kafkaTopicNameSet.add("vomci1");
        kafkaTopicNameSet.add("vomci2");
        Document filter = DocumentUtils.stringToDocument(VoltMFTestConstants.FILTER_GET_REQUEST);
        ArrayList filterList = new ArrayList();
        Module module = Mockito.mock(Module.class);
        QNameModule qNameModule = PowerMockito.mock(QNameModule.class);
        Optional<Revision> revision = PowerMockito.mock(Optional.class);
        SchemaPathBuilder schemaPathBuilder = Mockito.mock(SchemaPathBuilder.class);
        SchemaPath schemaPath = Mockito.mock(SchemaPath.class);
        GpbFormatter gpbFormatter = Mockito.mock(GpbFormatter.class);
        JsonFormatter jsonFormatter = Mockito.mock(JsonFormatter.class);
        filterList.add(filter.getDocumentElement());
        when(m_getRequestOne.getFilter()).thenReturn(m_filter);
        when(m_getRequestOne.getFilter().getXmlFilterElements()).thenReturn(filterList);
        when(m_getRequestOne.getMessageId()).thenReturn(VoltMFTestConstants.DEFAULT_MESSAGE_ID);
        when(m_getRequestOne.getRequestDocument()).thenReturn(DocumentUtils.stringToDocument(VoltMFTestConstants.INTERNAL_GET_REQUEST_WITH_DEFAULT_MESSAGE_ID));
        when(m_schemaRegistry.getModuleByNamespace(VoltMFTestConstants.NETWORK_MANAGER_NAMESPACE)).thenReturn(module);
        when(m_schemaRegistry.getModuleNameByNamespace(VoltMFTestConstants.NETWORK_MANAGER_NAMESPACE)).thenReturn(VoltMFTestConstants.NETWORK_MANAGER);
        when(module.getQNameModule()).thenReturn(qNameModule);
        when(qNameModule.getRevision()).thenReturn(revision);
        when(schemaPathBuilder.build()).thenReturn(schemaPath);
        DataSchemaNode dataSchemaNode = getDataSchemaNode();
        when(m_schemaRegistry.getDataSchemaNode(any(SchemaPath.class))).thenReturn(dataSchemaNode);
        PowerMockito.when(JsonUtil.convertFromXmlToJsonIgnoreEmptyLeaves(any(), any(), any(), any())).thenReturn(VoltMFTestConstants.FILTER_JSON_STRING);
        m_messageFormatter = gpbFormatter;
        MediatedDeviceNetconfSession session = getNewMDNSessionForNewDevice();
        when(m_deviceDao.getVomciFunctionName(anyString())).thenReturn("vomci1");
        when(m_networkFunctionDao.getKafkaTopicNames(anyString(), any())).thenReturn(kafkaTopicNameSet);
        session.onGet(m_getRequestOne);
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        verify(gpbFormatter,times(1)).getFormattedRequest(any(),any(), any(), any(), any() ,any(), any());
        verify(jsonFormatter, never()).getFormattedRequest(any(),any(), any(), any(), any() ,any(), any());
        verify(m_deviceDao, times(2)).getVomciFunctionName(anyString());
        verify(m_networkFunctionDao, times(1)).getKafkaTopicNames(anyString(), any());
        verify(m_kafkaProducer, times(2)).sendNotification(any(),anyString());
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
}
