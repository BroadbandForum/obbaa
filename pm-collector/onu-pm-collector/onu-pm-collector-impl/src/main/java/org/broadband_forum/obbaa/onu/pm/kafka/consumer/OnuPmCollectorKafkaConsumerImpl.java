/*
 * Copyright 2023 Broadband Forum
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

package org.broadband_forum.obbaa.onu.pm.kafka.consumer;

import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.onu.pm.datahandler.OnuPerformanceManagement;
import org.broadband_forum.obbaa.onu.pm.kafka.OnuKafkaNotificationCallback;
import org.broadband_forum.obbaa.onu.pm.kafka.ThreadSafeOnuKafkaConsumer;
import org.broadband_forum.obbaa.onu.pm.kafka.consumer.convert.MsgDeserializer;
import org.broadband_forum.obbaa.onu.pm.message.gpb.message.Msg;
import org.osgi.framework.Bundle;

/**
 * <p>
 * Consumes Kafka messages of type Gpb from vOMCI
 * </p>
 * Created by Madhukar Shetty (Nokia) on 30/05/2023.
 */
public class OnuPmCollectorKafkaConsumerImpl extends AbstractOnuPmCollectorKafkaConsumer<Msg> {

    private static final Logger LOGGER = Logger.getLogger(OnuPmCollectorKafkaConsumerImpl.class);
    private static Consumer<String, Msg> m_consumer;
    private ThreadPoolExecutor m_kafkaPollingPool;
    private int m_threadcount = 1;
    private OnuPerformanceManagement m_onuPerformanceManagement;

    public OnuPmCollectorKafkaConsumerImpl(Bundle bundle, OnuPerformanceManagement onuPerformanceManagement) {
        super(bundle, onuPerformanceManagement);
        m_onuPerformanceManagement = onuPerformanceManagement;
    }

    public void init() {
        final Properties config = loadKafkaConfig();
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        try {
            config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, Class.forName(MsgDeserializer.class.getName()));
        } catch (ClassNotFoundException e) {
            LOGGER.error("Failed in deserializer config");
        }
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "vONUPM");
        config.put(ConsumerConfig.METADATA_MAX_AGE_CONFIG, 5000);

        Thread.currentThread().setContextClassLoader(null);

        m_consumer = new ThreadSafeOnuKafkaConsumer<>(config);
        m_kafkaPollingPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(m_threadcount);
        addNotificationCallback();
        m_kafkaPollingPool.execute(this::subscribe);
    }

    public void destroy() {
        m_kafkaPollingPool.shutdown();
        m_consumer.close();
    }

    @Override
    public void subscribe() {
        LOGGER.debug("Subscribing to topic " + m_callbackFunctions.keySet());
        m_consumer.subscribe(m_callbackFunctions.keySet());
        do {
            final ConsumerRecords<String, org.broadband_forum.obbaa.onu.pm.message.gpb.message.Msg> records = m_consumer.poll(100);
            m_callback.onNotification(records, m_callbackFunctions);
        } while (true);
    }

    public void setConsumer(Consumer<String, Msg> consumer) {
        m_consumer = consumer;
    }

    public void setCallback(OnuKafkaNotificationCallback<Msg> callback) {
        this.m_callback = callback;
    }

    public void testInitThreadpool() {
        m_kafkaPollingPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(m_threadcount);
    }

}
