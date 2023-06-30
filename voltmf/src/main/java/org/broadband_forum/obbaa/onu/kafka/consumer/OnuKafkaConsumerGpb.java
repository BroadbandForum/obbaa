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

package org.broadband_forum.obbaa.onu.kafka.consumer;


import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.nf.dao.NetworkFunctionDao;
import org.broadband_forum.obbaa.nf.entities.KafkaTopic;
import org.broadband_forum.obbaa.nm.devicemanager.DeviceManager;
import org.broadband_forum.obbaa.onu.ONUConstants;
import org.broadband_forum.obbaa.onu.VOLTManagement;
import org.broadband_forum.obbaa.onu.kafka.KafkaNotificationCallback;
import org.broadband_forum.obbaa.onu.kafka.ThreadSafeOnuKafkaConsumer;
import org.broadband_forum.obbaa.onu.kafka.consumer.convert.MsgDeserializer;
import org.broadband_forum.obbaa.onu.message.gpb.message.Msg;
import org.osgi.framework.Bundle;

/**
 * <p>
 * Consumes Kafka messages of type Gpb from vOMCI
 * </p>
 * Created by Filipe Claudio (Altice Labs) on 09/06/2021.
 */
public class OnuKafkaConsumerGpb extends AbstractOnuKafkaConsumer<Msg> {

    private static final Logger LOGGER = Logger.getLogger(OnuKafkaConsumerGpb.class);
    private static Consumer<String, Msg> m_consumer;
    private final VOLTManagement m_voltMgmt;
    private ThreadPoolExecutor m_kafkaPollingPool;
    private int m_threadcount = ONUConstants.KAFKA_CONSUMER_THREADS;

    public OnuKafkaConsumerGpb(VOLTManagement voltMgmt, Bundle bundle, DeviceManager deviceManager,
                               NetworkFunctionDao networkFunctionDao) {
        super(voltMgmt, bundle, deviceManager, networkFunctionDao);
        this.m_voltMgmt = voltMgmt;
        if (m_voltMgmt != null) {
            m_voltMgmt.setKafkaConsumer(this);
        }
    }

    public void init() {
        final Properties config = loadKafkaConfig();
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, MsgDeserializer.class.getName());
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "vOLTMF");
        config.put(ConsumerConfig.METADATA_MAX_AGE_CONFIG, 5000);
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
        LOGGER.debug(" Subscribing to topics " + m_callbackFunctions.keySet());
        super.subscribeTopics();
        m_consumer.subscribe(m_callbackFunctions.keySet());
        do {
            try {
                final ConsumerRecords<String, Msg> records = m_consumer.poll(100);
                m_callback.onNotification(records, m_callbackFunctions);
            } catch (Exception e) {
                LOGGER.info("Exception occurred while processing kafka notification callback for GPB message", e);
            }
        } while (true);
    }

    @Override
    public void updateSubscriberTopics(Set<KafkaTopic> kafkaTopicSet) {
        super.updateSubscriberTopics(kafkaTopicSet, m_consumer);
    }

    @Override
    public void removeSubscriberTopics(Set<String> kafkaTopicSet) {
        super.removeSubscriberTopics(kafkaTopicSet, m_consumer);
    }

    public void setConsumer(Consumer<String, Msg> consumer) {
        m_consumer = consumer;
    }

    public void setCallback(KafkaNotificationCallback<Msg> callback) {
        this.m_callback = callback;
    }

    public void testInitThreadpool() {
        m_kafkaPollingPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(m_threadcount);
    }
}
