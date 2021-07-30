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


package org.broadband_forum.obbaa.nf.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;
import org.broadband_forum.obbaa.nf.dao.NetworkFunctionDao;
import org.broadband_forum.obbaa.nf.entities.KafkaAgent;
import org.broadband_forum.obbaa.nf.entities.KafkaAgentParameters;
import org.broadband_forum.obbaa.nf.entities.KafkaConsumptionParameters;
import org.broadband_forum.obbaa.nf.entities.KafkaPublicationParameters;
import org.broadband_forum.obbaa.nf.entities.KafkaTopic;
import org.broadband_forum.obbaa.nf.entities.NetworkFunction;
import org.broadband_forum.obbaa.nf.entities.RemoteEndpoint;
import org.broadband_forum.obbaa.nf.util.AbstractUtilTxTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NetworkFunctionDaoImplTest extends AbstractUtilTxTest {

    @Mock
    RemoteEndpoint m_remoteEndpoint;
    @Mock
    List<RemoteEndpoint> m_remoteEndpointList;
    @Mock
    KafkaAgent m_kafkaAgent;
    @Mock
    NetworkFunction m_networkFunction;
    @Mock
    List<NetworkFunction> m_networkFunctionList;
    @Mock
    KafkaAgentParameters m_kafkaAgentParameter;
    @Mock
    KafkaConsumptionParameters m_kafkaConsumptionParameters;
    @Mock
    KafkaPublicationParameters m_kafkaPublicationParameters;
    @Mock
    Set<KafkaTopic> m_kafkaTopicSet;
    @Mock
    KafkaTopic m_kafkaTopic;
    @Mock
    Iterator<KafkaTopic> m_kafkaTopicIterator ;
    @Mock
    EntityDataStoreManager m_entityDataStoreManager;

    private NetworkFunctionDao m_networkFunctionDao;
    @Mock
    private PersistenceManagerUtil m_persistenceMgrUtil;

    /*@Mock
    ConsoleHandler spyConsoleHandler ;*/

    String m_networkFunctionName = null;
    private String m_localEndpointName = "voltmf_kafka";

    @Before
    public void setup() {
        m_networkFunctionName= "vomci1";
        when(m_remoteEndpoint.getKafkaAgent()).thenReturn(m_kafkaAgent);
        when(m_kafkaAgent.getKafkaAgentParameters()).thenReturn(m_kafkaAgentParameter);
        when(m_kafkaAgentParameter.getKafkaConsumptionParameters()).thenReturn(m_kafkaConsumptionParameters);
        when(m_kafkaAgentParameter.getKafkaPublicationParameters()).thenReturn(m_kafkaPublicationParameters);
        when(m_kafkaConsumptionParameters.getKafkaTopics()).thenReturn(m_kafkaTopicSet);
        when(m_kafkaPublicationParameters.getKafkaTopics()).thenReturn(m_kafkaTopicSet);
        when(m_persistenceMgrUtil.getEntityDataStoreManager()).thenReturn(m_entityDataStoreManager);
        when(m_entityDataStoreManager.findByMatchValue(eq(NetworkFunction.class), anyObject())).thenReturn(m_networkFunctionList);
        when(m_networkFunctionList.get(0)).thenReturn(m_networkFunction);
        when(m_networkFunction.getRemoteEndpointName()).thenReturn("vomci1-endpoint");
        when(m_entityDataStoreManager.findByMatchValue(eq(RemoteEndpoint.class), anyObject())).thenReturn(m_remoteEndpointList);
        when(m_remoteEndpointList.get(0)).thenReturn(m_remoteEndpoint);
        when(m_remoteEndpoint.getKafkaAgent()).thenReturn(m_kafkaAgent);
        when(m_remoteEndpoint.getLocalEndpointName()).thenReturn(m_localEndpointName);
        when(m_kafkaTopicSet.iterator()).thenReturn(m_kafkaTopicIterator);
        when(m_kafkaTopicIterator.hasNext()).thenReturn(true,false);
        when(m_kafkaTopicIterator.next()).thenReturn(m_kafkaTopic);
        m_networkFunctionDao = new NetworkFunctionDaoImpl(m_persistenceMgrUtil);
    }

    @Test
    public void testFetchTopicNameForVOMCIRequestPurpose() {
        when(m_kafkaTopic.getPurpose()).thenReturn(KafkaTopicPurpose.VOMCI_REQUEST.toString());
        when(m_kafkaTopic.getTopicName()).thenReturn("vomci-proxy-request");
        HashSet<String> topicName = m_networkFunctionDao.getKafkaTopicNames(m_networkFunctionName, KafkaTopicPurpose.VOMCI_REQUEST);
        assertEquals(topicName.contains("vomci-proxy-request"), true);
        verify(m_kafkaAgent, times(1)).getKafkaAgentParameters();
        verify(m_kafkaAgentParameter, times(1)).getKafkaPublicationParameters();
        verify(m_kafkaAgentParameter, never()).getKafkaConsumptionParameters();
        verify(m_kafkaPublicationParameters, times(1)).getKafkaTopics();
        verify(m_kafkaConsumptionParameters, never()).getKafkaTopics();
        verify(m_kafkaTopic, times(1)).getPurpose();
        verify(m_kafkaTopic, times(1)).getTopicName();
    }


    @Test
    public void testFetchTopicNameForVOMCIResponsePurpose() {
        when(m_kafkaTopic.getPurpose()).thenReturn(KafkaTopicPurpose.VOMCI_RESPONSE.toString());
        when(m_kafkaTopic.getTopicName()).thenReturn("vomci-proxy-response");
        HashSet<String> topicName = m_networkFunctionDao.getKafkaTopicNames(m_networkFunctionName, KafkaTopicPurpose.VOMCI_RESPONSE);
        assertEquals(topicName.contains("vomci-proxy-response"),true);
        verify(m_kafkaAgent, times(1)).getKafkaAgentParameters();
        verify(m_kafkaAgentParameter, never()).getKafkaPublicationParameters();
        verify(m_kafkaAgentParameter, times(1)).getKafkaConsumptionParameters();
        verify(m_kafkaPublicationParameters, never()).getKafkaTopics();
        verify(m_kafkaConsumptionParameters, times(1)).getKafkaTopics();
        verify(m_kafkaTopic, times(1)).getPurpose();
        verify(m_kafkaTopic, times(1)).getTopicName();
    }

    @Test
    public void testFetchTopicNameForVOMCINotificationPurpose() {
        when(m_kafkaTopic.getPurpose()).thenReturn(KafkaTopicPurpose.VOMCI_NOTIFICATION.toString());
        when(m_kafkaTopic.getTopicName()).thenReturn("vomci-proxy-notification");
        HashSet<String> topicName = m_networkFunctionDao.getKafkaTopicNames(m_networkFunctionName, KafkaTopicPurpose.VOMCI_NOTIFICATION);
        assertEquals(topicName.contains("vomci-proxy-notification"),true);
        verify(m_kafkaAgent, times(1)).getKafkaAgentParameters();
        verify(m_kafkaAgentParameter, never()).getKafkaPublicationParameters();
        verify(m_kafkaAgentParameter, times(1)).getKafkaConsumptionParameters();
        verify(m_kafkaPublicationParameters, never()).getKafkaTopics();
        verify(m_kafkaConsumptionParameters, times(1)).getKafkaTopics();
        verify(m_kafkaTopic, times(1)).getPurpose();
        verify(m_kafkaTopic, times(1)).getTopicName();
    }

    @Test
    public void testFetchTopicDetailWhenRemoteEndpointIsNull() {
        when(m_remoteEndpointList.get(0)).thenReturn(null);
        when(m_networkFunctionList.isEmpty()).thenReturn(true);
        try {
            m_networkFunctionDao.getKafkaTopicNames(m_networkFunctionName, KafkaTopicPurpose.VOMCI_REQUEST);
        } catch (NullPointerException e) {
            assertEquals("Kafka topics is NULL or Empty", e.getMessage());
        }

        try {
            m_networkFunctionDao.getKafkaConsumerTopics(m_networkFunctionName);
        } catch (NullPointerException e) {
            assertEquals("Kafka topics is NULL or Empty", e.getMessage());
        }
    }

    @Test
    public void testFetchTopicNameWhenKafkaTopicSetIsNull() {
        when(m_kafkaTopicSet.isEmpty()).thenReturn(true);
        when(m_kafkaTopic.getPurpose()).thenReturn("VOMCI_NOTIFICATION");
        HashSet<String> kafkaTopicNames = m_networkFunctionDao.getKafkaTopicNames(m_networkFunctionName, KafkaTopicPurpose.VOMCI_REQUEST);
        assertEquals(kafkaTopicNames.isEmpty(), true);
        verify(m_kafkaAgent, times(1)).getKafkaAgentParameters();
        verify(m_kafkaTopic, never()).getPurpose();
        verify(m_kafkaTopic, never()).getTopicName();
    }

    @Test
    public void testGetKafkaConsumerTopics() {
        m_networkFunctionDao.getKafkaConsumerTopics(m_networkFunctionName);
        verify(m_kafkaAgent, times(1)).getKafkaAgentParameters();
        verify(m_kafkaAgentParameter, never()).getKafkaPublicationParameters();
        verify(m_kafkaAgentParameter, times(1)).getKafkaConsumptionParameters();
    }

    @Test
    public void testFetchTopicDetailsWhenPublicationParametersAreNull() {
        when(m_kafkaAgentParameter.getKafkaPublicationParameters()).thenReturn(null);
        try {
            m_networkFunctionDao.getKafkaTopicNames(m_networkFunctionName, KafkaTopicPurpose.VOMCI_REQUEST);
        } catch (NullPointerException e) {
            assertEquals("Kafka Publication parameters are NULL", e.getMessage());
        }
    }

    @Test
    public void testFetchTopicDetailsWhenConsumptionParametersAreNull() {
        when(m_kafkaAgentParameter.getKafkaConsumptionParameters()).thenReturn(null);
        HashSet<String> kafkaTopicNames = m_networkFunctionDao.getKafkaTopicNames(m_networkFunctionName, KafkaTopicPurpose.VOMCI_RESPONSE);
        assertEquals(kafkaTopicNames.isEmpty(), true);

        Set<KafkaTopic> kafkaTopicSet = m_networkFunctionDao.getKafkaConsumerTopics(m_networkFunctionName);
        assertEquals(kafkaTopicSet,null);
    }

    @Test
    public void testFetchMultipleTopicNameforSinglePurpose() {
        when(m_kafkaTopicIterator.hasNext()).thenReturn(true,true, false);
        when(m_kafkaTopic.getPurpose()).thenReturn("VOMCI_REQUEST");
        when(m_kafkaTopic.getTopicName()).thenReturn("vomci-proxy-request-one", "vomci-proxy-request-two");
        HashSet<String> kafkaTopicNameSet = m_networkFunctionDao.getKafkaTopicNames(m_networkFunctionName, KafkaTopicPurpose.VOMCI_REQUEST);
        assertEquals(kafkaTopicNameSet.contains("vomci-proxy-request-one"), true);
        assertEquals(kafkaTopicNameSet.contains("vomci-proxy-request-two"), true);
        assertEquals(kafkaTopicNameSet.contains("vomci-proxy-request-three"), false);

    }

    @Test
    public void testGetLocalEndpointName() {
        String localEndpointName = m_networkFunctionDao.getLocalEndpointName(m_networkFunctionName);
        assertEquals(m_localEndpointName, localEndpointName);
    }

    @Override
    protected PersistenceManagerUtil getPersistenceManagerUtil() {
        return m_persistenceMgrUtil;
    }


}
