package org.broadband_forum.obbaa.onu.pm.kafka.consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.Duration;

import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.broadband_forum.obbaa.onu.pm.datahandler.OnuPerformanceManagement;
import org.broadband_forum.obbaa.onu.pm.kafka.OnuKafkaNotificationCallback;
import org.broadband_forum.obbaa.onu.pm.message.gpb.message.Msg;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.osgi.framework.Bundle;

public class OnuPmCollectorKafkaCosumerTest {

    @Mock
    private MockConsumer<String, Msg> m_mockConsumer;
    @Mock
    private MockConsumer<String, Msg> m_closeConsumer;
    @Mock
    private OnuPerformanceManagement m_onuPerformanceManagement;

    OnuPmCollectorKafkaConsumerImpl m_onuPmCollectorKafkaConsumer;
    @Mock
    private Bundle m_bundle;

    @Before
    public void setup() {
        m_onuPmCollectorKafkaConsumer = new OnuPmCollectorKafkaConsumerImpl(m_bundle, m_onuPerformanceManagement);
    }

    @Test
    public void testKafkaTopicChecker() {
    }

    @Test
    public void add_Callbacks() {
        m_mockConsumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);
        m_onuPmCollectorKafkaConsumer.setConsumer(m_mockConsumer);
        m_onuPmCollectorKafkaConsumer.setCallback(new OnuKafkaNotificationCallback<>(m_onuPerformanceManagement));
        m_onuPmCollectorKafkaConsumer.addNotificationCallback();
    }

    @Test
    public void consumeKV_thenVerifyConsumption() {
        Thread subscribeThread = new Thread() {
            public void run() {
                m_onuPmCollectorKafkaConsumer.subscribe();
            }
        };
        m_mockConsumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);
        assertEquals(0, m_mockConsumer.poll(Duration.ZERO).count());
    }

    @Test
    public void destroyConsumer() {
        OnuPmCollectorKafkaConsumerImpl closeConsumer = new OnuPmCollectorKafkaConsumerImpl(m_bundle, m_onuPerformanceManagement);
        m_closeConsumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);
        closeConsumer.setConsumer(m_closeConsumer);
        closeConsumer.testInitThreadpool();
        closeConsumer.destroy();
        assertTrue(m_closeConsumer.closed());
    }
}
