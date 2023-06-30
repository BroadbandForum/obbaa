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
import org.broadband_forum.obbaa.onu.kafka.producer.convert.MsgSerializer;
import org.broadband_forum.obbaa.onu.message.gpb.message.Msg;
import org.osgi.framework.Bundle;

/**
 * <p>
 * Produces Kafka messages of type Gpb for vOMCI
 * </p>
 * Created by Filipe Claudio (Altice Labs) on 09/06/2021.
 */
public class OnuKafkaProducerGpb extends AbstractOnuKafkaProducer {

    public OnuKafkaProducerGpb(Bundle bundle) {
        super(bundle);
    }

    private static final Logger LOGGER = Logger.getLogger(OnuKafkaProducerGpb.class);
    private static Producer<String, Msg> m_producer;

    public void init() {
        LOGGER.debug(" Kafka Producer is starting ");
        Properties config = loadKafkaConfig();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, MsgSerializer.class.getName());
        m_producer = new KafkaProducer<String, Msg>(config);
        LOGGER.debug(" Kafka Producer initialized successfully ");
    }

    public void destroy() {
        m_producer.close();
    }

    public void sendNotification(final String kafkaTopicName, final Object notification) throws MessageFormatterException {
        LOGGER.info(String.format("Sending kafka message: %s on topic: %s ", notification, kafkaTopicName));
        validateMessageFormat(notification);
        final String messageKey = java.time.LocalDateTime.now().toString();
        final String checkedTopic = checkTopicName(kafkaTopicName);
        // Runs Asynchronous to not block anything else
        m_producer.send(new ProducerRecord<>(checkedTopic, messageKey, (Msg) notification), new Callback() {
            public void onCompletion(RecordMetadata metadata, Exception exc) {
                if (exc != null) {
                    LOGGER.error("Unable to send record via Kafkaproducer:" + exc);
                }
                LOGGER.debug("Sent message: (" + messageKey + ", " + notification + ") at offset: " + metadata.offset());
            }
        });
        m_producer.flush();
    }

    private void validateMessageFormat(final Object message) throws MessageFormatterException {
        if (message == null) {
            throw new MessageFormatterException("Message must be different of 'null'.");
        }
        if (!(message instanceof Msg)) {
            throw new MessageFormatterException(String.format("OnuKafkaProducerGpb can only send messages of type '%s'. Given: '%s'.",
                    Msg.class.toString(), message.getClass().toString()));
        }
    }

    public void setProducer(Producer<String, Msg> producer) {
        m_producer = producer;
    }
}
