/*
 * Copyright 2022 Broadband Forum
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

import org.broadband_forum.obbaa.device.adapter.AdapterContext;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.device.adapter.AdapterUtils;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.messages.CopyConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.TxService;
import org.broadband_forum.obbaa.nf.dao.NetworkFunctionDao;
import org.broadband_forum.obbaa.nf.entities.NetworkFunction;
import org.broadband_forum.obbaa.onu.exception.MessageFormatterException;
import org.broadband_forum.obbaa.onu.impl.VOLTManagementImpl;
import org.broadband_forum.obbaa.onu.kafka.producer.OnuKafkaProducerGpb;
import org.broadband_forum.obbaa.onu.message.GpbFormatter;
import org.broadband_forum.obbaa.onu.message.JsonFormatter;
import org.broadband_forum.obbaa.onu.message.MessageFormatter;
import org.broadband_forum.obbaa.onu.util.VoltMFTestConstants;
import org.jetbrains.annotations.NotNull;
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

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@RunWith(PowerMockRunner.class)
@PrepareForTest({QNameModule.class,AdapterUtils.class, AdapterContext.class})
public class MediatedNetworkFunctionNetconfSessionTest {

    private String m_proxyName = "proxy-1";
    @Mock
    private NetworkFunction m_networkFunction;
    @Mock
    private OnuKafkaProducerGpb m_kafkaProducer;
    @Mock
    private ModelNodeDataStoreManager m_modelNodeDSM;
    @Mock
    private AdapterManager m_adapterManager;
    @Mock
    private NetworkFunctionDao m_networkFunctionDao;
    @Mock
    private VOLTManagementImpl m_VOLTMgmt;
    @Mock
    private SchemaRegistryImpl m_schemaRegistry;
    @Mock
    private EditConfigRequest m_editConfigRequest;

    @Mock
    private CopyConfigRequest m_copyConfigRequest;

    private TxService m_txService = new TxService();
    private ThreadPoolExecutor m_kafkaCommunicationPool;

    private MessageFormatter m_messageFormatter;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(m_networkFunction.getNetworkFunctionName()).thenReturn(m_proxyName);
        when(m_networkFunction.getType()).thenReturn("bbf-nf-types:vomci-function-type");
        when(m_editConfigRequest.getMessageId()).thenReturn(ONUConstants.ONU_EDIT_OPERATION);
        when(m_copyConfigRequest.getMessageId()).thenReturn(ONUConstants.ONU_COPY_OPERATION);
        m_messageFormatter = new GpbFormatter();
        m_kafkaCommunicationPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(VoltMFTestConstants.THREAD_POOL_COUNT);

    }

    @Test
    public void testNoSessionRequests() {
        MediatedNetworkFunctionNetconfSession session = getNewMDNSessionForNewNetworkFunction();
        assertEquals(0,session.getNumberOfPendingRequests());
        verifySessionIntegrity(session,null,null,null);
    }

    public MediatedNetworkFunctionNetconfSession getNewMDNSessionForNewNetworkFunction() {
        return new MediatedNetworkFunctionNetconfSession(m_networkFunction,m_kafkaProducer,m_modelNodeDSM,
                m_adapterManager,m_kafkaCommunicationPool,m_messageFormatter,m_txService,
                m_networkFunctionDao,m_VOLTMgmt);
    }

    private void verifySessionIntegrity(MediatedNetworkFunctionNetconfSession session,
                                        String msgIdOne, String msgIdTwo, String msgIdThree) {
        assertTrue(session instanceof NetconfClientSession);
        assertTrue(session.isOpen());
        int count = 0;
        long timeStamp = 0;
        Map<String,TimestampFutureResponse> requestMap = session.getMapOfRequests();
        for (Map.Entry<String,TimestampFutureResponse> entry : requestMap.entrySet()) {
            switch (count) {
                case 0:
                    assertEquals(entry.getKey(),msgIdOne);
                    break;
                case 1:
                    assertEquals(entry.getKey(),msgIdTwo);
                    break;
                default:
                    assertEquals(entry.getKey(),msgIdThree);
                    break;
            }
            assertTrue(timeStamp <= entry.getValue().getTimeStamp());
            timeStamp = entry.getValue().getTimeStamp();
            count++;
        }
    }

    @Test
    public void testOnGet() {
        //network functions do not have get implemented yet
    }

    @Test
    public void testOnCopyConfig() throws NetconfMessageBuilderException, MessageFormatterException {
        HashSet<String> kafkaTopicNames = new HashSet<>();
        kafkaTopicNames.add("proxy-1");
        kafkaTopicNames.add("vomci-1");
        Module module = Mockito.mock(Module.class);
        QNameModule qNameModule = PowerMockito.mock(QNameModule.class);
        Optional<Revision> revision = PowerMockito.mock(Optional.class);
        AdapterContext adapterContext = Mockito.mock(AdapterContext.class);
        AdapterUtils adapterUtils = PowerMockito.mock(AdapterUtils.class);
        SchemaPathBuilder schemaPathBuilder = Mockito.mock(SchemaPathBuilder.class);
        SchemaPath schemaPath = Mockito.mock(SchemaPath.class);
        JsonFormatter jsonFormatter = Mockito.mock(JsonFormatter.class);
        GpbFormatter gpbFormatter = Mockito.mock(GpbFormatter.class);
        when(adapterContext.getSchemaRegistry()).thenReturn(m_schemaRegistry);
        when(adapterUtils.getAdapterContext(m_networkFunction,m_adapterManager)).thenReturn(adapterContext);
        when(m_networkFunctionDao.getKafkaTopicNames(anyString(),any())).thenReturn(kafkaTopicNames);
        when(m_copyConfigRequest.getSourceConfigElement()).thenReturn(DocumentUtils.
                stringToDocument(VoltMFTestConstants.EDIT_CONFIG_REQUEST).getDocumentElement());
        DataSchemaNode dataSchemaNode = getDataSchemaNode();
        when(m_schemaRegistry.getModuleByNamespace(any())).thenReturn(module);
        when(m_schemaRegistry.getModuleNameByNamespace(any())).thenReturn(VoltMFTestConstants.NETWORK_MANAGER);
        when(m_schemaRegistry.getDataSchemaNode(any(SchemaPath.class))).thenReturn(dataSchemaNode);
        when(module.getQNameModule()).thenReturn(qNameModule);
        when(qNameModule.getRevision()).thenReturn(revision);
        when(schemaPathBuilder.build()).thenReturn(schemaPath);
        m_messageFormatter = gpbFormatter;
        MediatedNetworkFunctionNetconfSession session = getNewMDNSessionForNewNetworkFunction();
        session.onCopyConfig(m_copyConfigRequest);
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        verify(gpbFormatter,times(1)).getFormattedRequestForNF(any(),any(), any(), any(), any());
        verify(jsonFormatter, never()).getFormattedRequestForNF(any(),any(), any(), any(), any());
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
