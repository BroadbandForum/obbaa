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

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.onu.exception.MessageFormatterException;
import org.osgi.framework.Bundle;

/**
 * <p>
 * Abstract class for Kafka Producer, contains the generic functions of the kafka producer.
 * </p>
 * Created by Filipe Cláudio (Altice Labs) on 09/06/2021.
 */
public abstract class AbstractOnuKafkaProducer implements OnuKafkaProducer {

    private static final Logger LOGGER = Logger.getLogger(AbstractOnuKafkaProducer.class);
    private final Bundle m_bundle;

    AbstractOnuKafkaProducer(Bundle bundle) {
        m_bundle = bundle;
    }

    @Override
    public abstract void sendNotification(String kafkaTopicName, Object notification) throws MessageFormatterException;

    Properties loadKafkaConfig() {
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
}
