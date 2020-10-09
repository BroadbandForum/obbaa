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

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Hashtable;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.onu.ONUConstants;
import org.broadband_forum.obbaa.onu.VOLTManagement;
import org.osgi.framework.Bundle;

/**
 * <p>
 * Consumes Kafka messages from vOMCI related to ONU notifications and responses
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 22/07/2020.
 */
public final class OnuKafkaConsumer {

    public OnuKafkaConsumer(VOLTManagement voltMgmt, Bundle bundle) {
        m_voltMgmt = voltMgmt;
        m_bundle = bundle;
    }

    private static final Logger LOGGER = Logger.getLogger(OnuKafkaConsumer.class);
    private final VOLTManagement m_voltMgmt;
    private final Bundle m_bundle;
    private static Consumer<String, String> m_consumer;
    private Hashtable<String, java.util.function.Consumer> m_callbackFunctions;
    private KafkaNotificationCallback m_callback;
    private ThreadPoolExecutor m_kafkaPollingPool;
    private int m_threadcount = ONUConstants.KAFKA_CONSUMER_THREADS;

    public void init() {
        final Properties config = loadKafkaConfig();
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class.getName());
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "vOLTMF");
        m_consumer = new KafkaConsumer<String, String>(config);
        m_callback = new KafkaNotificationCallback(m_voltMgmt);
        addNotificationCallback();
        m_kafkaPollingPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(m_threadcount);
        m_kafkaPollingPool.execute(() -> {
            subscribe();
        });
    }

    public void destroy() {
        m_kafkaPollingPool.shutdown();
        m_consumer.close();
    }

    public void subscribe() {
        LOGGER.debug(" Subscribing to topics " + m_callbackFunctions.keySet());
        m_consumer.subscribe(m_callbackFunctions.keySet());
        try {
            do {
                final ConsumerRecords<String, String> records = m_consumer.poll(100);
                m_callback.onNotification(records, m_callbackFunctions);
            } while (true);
        } finally {
            m_consumer.close();
        }
    }

    public void addNotificationCallback() {
        m_callbackFunctions = new Hashtable<String, java.util.function.Consumer>();
        m_callbackFunctions.put(ONUConstants.ONU_ALARM_KAFKA_TOPIC, m_callback::processAlarm);
        m_callbackFunctions.put(ONUConstants.ONU_RESPONSE_KAFKA_TOPIC, m_callback::processResponse);
    }

    public Properties loadKafkaConfig() {
        Properties config = new Properties();
        try {
            InputStream stream = m_bundle.getResource("kafka_config.properties").openStream();
            config.load(stream);
            config.put("client.id", InetAddress.getLocalHost().getHostName());
        } catch (IOException e) {
            LOGGER.error("KafkaConfigReadException", e);
        }
        LOGGER.info("Kafka: configuration loaded");
        return config;
    }

    protected void setConsumer(Consumer<String, String> consumer) {
        this.m_consumer = consumer;
    }

    protected void setCallback(KafkaNotificationCallback callback) {
        this.m_callback = callback;
    }

    protected void testInitThreadpool() {
        m_kafkaPollingPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(m_threadcount);
    }
}
