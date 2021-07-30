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

package org.broadband_forum.obbaa.onu.kafka.producer;

import java.util.Properties;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.onu.exception.MessageFormatterException;
import org.osgi.framework.Bundle;

/**
 * <p>
 * Produces Kafka messages towards vOMCI
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 22/07/2020.
 */
public class OnuKafkaProducerJson extends AbstractOnuKafkaProducer {

    public OnuKafkaProducerJson(Bundle bundle) {
        super(bundle);
    }

    private static final Logger LOGGER = Logger.getLogger(OnuKafkaProducerJson.class);
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

    public void sendNotification(final String kafkaTopicName, final Object notificationString) throws MessageFormatterException {
        LOGGER.info(String.format("Sending kafka message: %s on topic: %s ", notificationString, kafkaTopicName));
        validateMessageFormat(notificationString);
        final String messageKey = java.time.LocalDateTime.now().toString();
        final String checkedTopic = checkTopicName(kafkaTopicName);
        // Runs Asynchronous to not block anything else
        m_producer.send(new ProducerRecord<>(checkedTopic, messageKey, (String) notificationString), new Callback() {
            public void onCompletion(RecordMetadata metadata, Exception exc) {
                if (exc != null) {
                    LOGGER.error("Unable to send record via Kafkaproducer:" + exc);
                }
                LOGGER.debug("Sent message: (" + messageKey + ", " + notificationString + ") at offset: " + metadata.offset());
            }
        });
        m_producer.flush();
    }

    private void validateMessageFormat(final Object message) throws MessageFormatterException {
        if (message == null || message == "") {
            throw new MessageFormatterException("Message must be different of 'null'.");
        }
        if (!(message instanceof String)) {
            throw new MessageFormatterException(String.format("OnuKafkaProducerGpb can only send messages of type '%s'. Given: '%s'.",
                    String.class.toString(), message.getClass().toString()));
        }
    }

    public void setProducer(Producer<String, String> producer) {
        m_producer = producer;
    }
}