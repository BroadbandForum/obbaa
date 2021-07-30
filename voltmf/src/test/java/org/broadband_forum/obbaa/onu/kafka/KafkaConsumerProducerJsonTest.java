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
package org.broadband_forum.obbaa.onu.kafka;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.broadband_forum.obbaa.nf.dao.NetworkFunctionDao;
import org.broadband_forum.obbaa.nf.dao.impl.KafkaTopicPurpose;
import org.broadband_forum.obbaa.nf.entities.KafkaTopic;
import org.broadband_forum.obbaa.nm.devicemanager.DeviceManager;
import org.broadband_forum.obbaa.onu.VOLTManagement;
import org.junit.Before;
import org.broadband_forum.obbaa.onu.exception.MessageFormatterException;
import org.broadband_forum.obbaa.onu.kafka.consumer.OnuKafkaConsumerJson;
import org.broadband_forum.obbaa.onu.kafka.producer.OnuKafkaProducerJson;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@PowerMockIgnore("javax.management.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest({OnuKafkaConsumerJson.class})
public final class KafkaConsumerProducerJsonTest {
    @Mock
    KafkaTopic m_kafkaTopic;
    @Mock
    ConsumerRecords m_consumerRecords;
    @Mock
    private MockProducer<String, String> m_mockProducer;
    @Mock
    private MockConsumer<String, String> m_mockConsumer;
    @Mock
    private Bundle m_bundle;
    @Mock
    private VOLTManagement m_volMgmt;
    @Mock
    private DeviceManager m_deviceManager;
    @Mock
    private NetworkFunctionDao m_networkFunctionDao;
    @Mock
    private KafkaNotificationCallback m_callback;
    @Mock
    private Consumer m_consumer;
    @Mock
    private org.apache.kafka.clients.consumer.Consumer m_consumerClient;
    @Mock
    private URL m_url;

    private OnuKafkaProducerJson m_testProducer;
    private OnuKafkaConsumerJson m_testConsumer;
    private InputStream m_inputStream;

    @Before
    public void setUp() {
        m_testConsumer = Mockito.spy(new OnuKafkaConsumerJson(m_volMgmt, m_bundle, m_deviceManager, m_networkFunctionDao));
        m_testProducer = Mockito.spy(new OnuKafkaProducerJson(m_bundle));
    }

    @Test
    public void sendKV_thenVerifyHistory() throws MessageFormatterException {
        m_mockProducer = new MockProducer<>(true, new StringSerializer(), new StringSerializer());
        m_testProducer.setProducer(m_mockProducer);
        m_testProducer.sendNotification("testkey", "{teststring}");
        assertTrue(m_mockProducer.history().size() == 1);
    }

    @Test
    public void testKafkaTopicChecker() {
        assertTrue(m_testProducer.checkTopicName("some.topic").equals("sometopic"));
    }

    @Test
    public void add_Callbacks() {
        m_mockConsumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);
        m_testConsumer.setConsumer(m_mockConsumer);
        m_testConsumer.setCallback(new KafkaNotificationCallback(m_volMgmt));
        m_testConsumer.addNotificationCallback();
    }

    @Test
    public void consumeKV_thenVerifyConsumption() {
        when(m_mockConsumer.poll(Duration.ZERO)).thenReturn(m_consumerRecords);
        Thread subscribeThread = new Thread() {
            public void run() {
                m_testConsumer.subscribe();
            }
        };
        assertEquals(0, m_mockConsumer.poll(Duration.ZERO).count());
    }

    @Test
    public void testUpdateKafkaSubscriptionWithNotification() {
        Hashtable<String, java.util.function.Consumer> m_callbackFunctions = new Hashtable<>();
        m_callbackFunctions.put("topic1", m_consumer);
        Set<KafkaTopic> kafkaTopicSet = new HashSet<>();
        kafkaTopicSet.add(m_kafkaTopic);
        when(m_kafkaTopic.getPurpose()).thenReturn(KafkaTopicPurpose.VOMCI_NOTIFICATION.toString());
        when(m_kafkaTopic.getTopicName()).thenReturn("topic1");
        m_testConsumer.setCallback(m_callback);
        m_testConsumer.setConsumer(m_consumerClient);
        m_testConsumer.addNotificationCallback();
        m_testConsumer.updateSubscriberTopics(kafkaTopicSet);
        verify(m_kafkaTopic, times(1)).getPurpose();
        verify(m_kafkaTopic, times(2)).getTopicName();
        verify(m_consumerClient, times(1)).subscribe(m_testConsumer.getCallBackFunctionList().keySet());
        assertTrue(m_testConsumer.getCallBackFunctionList().containsKey("topic1"));
    }

    @Test
    public void testUpdateKafkaSubscriptionWithResponse() {
        Hashtable<String, java.util.function.Consumer> m_callbackFunctions = new Hashtable<>();
        m_callbackFunctions.put("topic1", m_consumer);
        Set<KafkaTopic> kafkaTopicSet = new HashSet<>();
        kafkaTopicSet.add(m_kafkaTopic);
        when(m_kafkaTopic.getPurpose()).thenReturn(KafkaTopicPurpose.VOMCI_RESPONSE.toString());
        when(m_kafkaTopic.getTopicName()).thenReturn("topic1");
        m_testConsumer.setCallback(m_callback);
        m_testConsumer.setConsumer(m_consumerClient);
        m_testConsumer.addNotificationCallback();
        m_testConsumer.updateSubscriberTopics(kafkaTopicSet);
        verify(m_kafkaTopic, times(2)).getPurpose();
        verify(m_kafkaTopic, times(2)).getTopicName();
        verify(m_consumerClient, times(1)).subscribe(m_testConsumer.getCallBackFunctionList().keySet());
        assertTrue(m_testConsumer.getCallBackFunctionList().containsKey("topic1"));
    }

    @Test
    public void testUpdateKafkaSubscriptionWithRequest() {
        Hashtable<String, java.util.function.Consumer> m_callbackFunctions = new Hashtable<>();
        m_callbackFunctions.put("topic1", m_consumer);
        Set<KafkaTopic> kafkaTopicSet = new HashSet<>();
        kafkaTopicSet.add(m_kafkaTopic);
        when(m_kafkaTopic.getPurpose()).thenReturn(KafkaTopicPurpose.VOMCI_REQUEST.toString());
        m_testConsumer.setCallback(m_callback);
        m_testConsumer.setConsumer(m_consumerClient);
        m_testConsumer.addNotificationCallback();
        m_testConsumer.updateSubscriberTopics(kafkaTopicSet);
        verify(m_kafkaTopic, times(2)).getPurpose();
        verify(m_kafkaTopic, never()).getTopicName();
        verify(m_consumerClient, never()).subscribe(anyCollection());
        assertFalse(m_testConsumer.getCallBackFunctionList().containsKey("topic1"));
    }

    @Test
    public void testRemoveKafkaSubscription() throws IOException {
        String topicName = "OBBAA_ONU_RESPONSE";
        Set<String> kafkaTopicNameSet = new HashSet<>();
        kafkaTopicNameSet.add(topicName);
        when(m_bundle.getResource("kafka_config.properties")).thenReturn(m_url);
        String initialString = "bootstrap.servers=127.0.0.1:9092";
        m_inputStream = new ByteArrayInputStream(initialString.getBytes());
        when(m_url.openStream()).thenReturn(m_inputStream);
        m_testConsumer.setCallback(m_callback);
        m_testConsumer.setConsumer(m_consumerClient);
        m_testConsumer.addNotificationCallback();
        m_testConsumer.removeSubscriberTopics(kafkaTopicNameSet);
        assertFalse(m_testConsumer.getCallBackFunctionList().containsKey("OBBAA_ONU_RESPONSE"));
        verify(m_consumerClient, times(1)).subscribe(m_testConsumer.getCallBackFunctionList().keySet());
    }
}