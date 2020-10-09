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

import java.time.Duration;

import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.broadband_forum.obbaa.onu.VOLTManagement;
import org.broadband_forum.obbaa.onu.kafka.OnuKafkaConsumer;
import org.broadband_forum.obbaa.onu.kafka.OnuKafkaProducer;
import org.junit.Test;
import org.mockito.Mock;
import org.osgi.framework.Bundle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class KafkaConsumerProducerTest {
    @Mock
    private MockProducer m_mockProducer;
    @Mock
    private MockConsumer<String, String> m_mockConsumer;
    @Mock
    private MockConsumer<String, String> m_closeConsumer;
    @Mock
    private Bundle m_bundle;
    @Mock
    private VOLTManagement m_volMgmt;

    private OnuKafkaProducer testProducer;
    private OnuKafkaConsumer testConsumer;

    public KafkaConsumerProducerTest() {
        sendKV_thenVerifyHistory();
        testKafkaTopicChecker();
        add_Callbacks();
        consumeKV_thenVerifyConsumption();
        destroyConsumer();
        destroyProducer();
    }

    @Test
    public void sendKV_thenVerifyHistory() {
        m_mockProducer = new MockProducer<>(true, new StringSerializer(), new StringSerializer());
        testProducer = new OnuKafkaProducer(m_bundle);
        testProducer.setProducer( m_mockProducer);
        testProducer.sendNotification("testkey", "{teststring}");
        assertTrue(m_mockProducer.history().size() == 1);
    }

    @Test
    public void testKafkaTopicChecker() {
        testProducer = new OnuKafkaProducer(m_bundle);
        assertTrue(testProducer.checkTopicName("some.topic").equals("sometopic"));
    }

    @Test
    public void add_Callbacks() {
        m_mockConsumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);
        testConsumer = new OnuKafkaConsumer(m_volMgmt, m_bundle);
        testConsumer.setConsumer(m_mockConsumer);
        testConsumer.setCallback(new KafkaNotificationCallback(m_volMgmt));
        testConsumer.addNotificationCallback();
    }

    @Test
    public void consumeKV_thenVerifyConsumption() {
        Thread subscribeThread = new Thread() {
            public void run() {
                testConsumer.subscribe();
            }
        };
        assertEquals(0, m_mockConsumer.poll(Duration.ZERO).count());
    }

    @Test
    public void destroyProducer() {
        testProducer.destroy();
        assertTrue(m_mockProducer.closed());
    }

    @Test
    public void destroyConsumer() {
        OnuKafkaConsumer closeConsumer = new OnuKafkaConsumer(m_volMgmt, m_bundle);
        m_closeConsumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);
        closeConsumer.setConsumer(m_closeConsumer);
        closeConsumer.testInitThreadpool();
        closeConsumer.destroy();
        assertTrue(m_closeConsumer.closed());
    }
}