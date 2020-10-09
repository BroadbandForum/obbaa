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
import java.util.Properties;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;

/**
 * <p>
 * Produces Kafka messages towards vOMCI
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 22/07/2020.
 */
public class OnuKafkaProducer {

    public OnuKafkaProducer(Bundle bundle) {
        m_bundle = bundle;
    }

    private static final Logger LOGGER = Logger.getLogger(OnuKafkaProducer.class);
    private final Bundle m_bundle;
    private static Producer<String, String> m_producer;

    public void init() {
        LOGGER.debug(" Kafka Producer is starting ");
        Thread.currentThread().setContextClassLoader(null);
        final Properties config = loadKafkaConfig();
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        m_producer = new KafkaProducer<>(config);
        LOGGER.debug(" Kafka Producer initialized successfully ");
    }

    public void destroy() {
        m_producer.close();
    }

    public void sendNotification(final String kafkaTopicName, final String notificationString) {
        LOGGER.info(String.format("Sending kafka message: %s on topic: %s ", notificationString, kafkaTopicName));
        final String messageKey = java.time.LocalDateTime.now().toString();
        final String checkedTopic = OnuKafkaProducer.checkTopicName(kafkaTopicName);
        // Runs Asynchronous to not block anything else
        m_producer.send(new ProducerRecord<>(checkedTopic, messageKey, notificationString), new Callback() {
            public void onCompletion(RecordMetadata metadata, Exception exc) {
                if (exc != null) {
                    LOGGER.error("Unable to send record via Kafkaproducer:" + exc);
                }
                LOGGER.debug("Sent message: (" + messageKey + ", " + notificationString + ") at offset: " + metadata.offset());
            }
        });
        m_producer.flush();
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
        return config;
    }

    public static String checkTopicName(final String topic) {
        String returnString = topic;
        if (returnString.indexOf(".") != -1) {
            returnString = returnString.replaceAll("\\.", "");
            LOGGER.error("Found illegal characters in topic '" + topic + "' and deleted them");
        }
        return returnString;
    }

    protected void setProducer(Producer<String, String> producer) {
        this.m_producer = producer;
    }
}