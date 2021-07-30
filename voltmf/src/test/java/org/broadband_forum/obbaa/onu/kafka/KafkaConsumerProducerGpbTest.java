package org.broadband_forum.obbaa.onu.kafka;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.broadband_forum.obbaa.nf.dao.NetworkFunctionDao;
import org.broadband_forum.obbaa.nm.devicemanager.DeviceManager;
import org.broadband_forum.obbaa.onu.VOLTManagement;
import org.broadband_forum.obbaa.onu.exception.MessageFormatterException;
import org.broadband_forum.obbaa.onu.kafka.consumer.OnuKafkaConsumerGpb;
import org.broadband_forum.obbaa.onu.kafka.producer.OnuKafkaProducerGpb;
import org.broadband_forum.obbaa.onu.kafka.producer.convert.MsgSerializer;
import org.broadband_forum.obbaa.onu.message.gpb.message.Msg;
import org.junit.Test;
import org.mockito.Mock;
import org.osgi.framework.Bundle;

public class KafkaConsumerProducerGpbTest {
    @Mock
    private MockProducer<String, Msg> m_mockProducer;
    @Mock
    private MockConsumer<String, Msg> m_mockConsumer;
    @Mock
    private MockConsumer<String, Msg> m_closeConsumer;
    @Mock
    private Bundle m_bundle;
    @Mock
    private VOLTManagement m_volMgmt;
    @Mock
    private DeviceManager m_deviceManager;
    @Mock
    private NetworkFunctionDao m_networkFunctionDao;

    private OnuKafkaProducerGpb testProducer;
    private OnuKafkaConsumerGpb testConsumer;

    public KafkaConsumerProducerGpbTest() throws MessageFormatterException {
        sendKV_thenVerifyHistory();
        testKafkaTopicChecker();
        add_Callbacks();
        consumeKV_thenVerifyConsumption();
        destroyConsumer();
        destroyProducer();
    }

    @Test
    public void sendKV_thenVerifyHistory() throws MessageFormatterException {
        m_mockProducer = new MockProducer<>(true, new StringSerializer(), new MsgSerializer());
        testProducer = new OnuKafkaProducerGpb(m_bundle);
        testProducer.setProducer(m_mockProducer);
        testProducer.sendNotification("testkey", Msg.newBuilder().build());
        assertEquals(1, m_mockProducer.history().size());
    }

    @Test
    public void testKafkaTopicChecker() {
        testProducer = new OnuKafkaProducerGpb(m_bundle);
        assertEquals("sometopic", testProducer.checkTopicName("some.topic"));
    }

    @Test
    public void add_Callbacks() {
        m_mockConsumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);
        testConsumer = new OnuKafkaConsumerGpb(m_volMgmt, m_bundle, m_deviceManager, m_networkFunctionDao);
        testConsumer.setConsumer(m_mockConsumer);
        testConsumer.setCallback(new KafkaNotificationCallback<>(m_volMgmt));
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
        OnuKafkaConsumerGpb closeConsumer = new OnuKafkaConsumerGpb(m_volMgmt, m_bundle, m_deviceManager, m_networkFunctionDao);
        m_closeConsumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);
        closeConsumer.setConsumer(m_closeConsumer);
        closeConsumer.testInitThreadpool();
        closeConsumer.destroy();
        assertTrue(m_closeConsumer.closed());
    }
}
