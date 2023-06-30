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


import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.onu.pm.datahandler.OnuPerformanceManagement;
import org.broadband_forum.obbaa.onu.pm.kafka.OnuKafkaNotificationCallback;
import org.osgi.framework.Bundle;

/**
 * <p>
 * Abstract class for Kafka Consumer, contains the generic functions of the kafka consumer.
 * </p>
 * Created by Madhukar Shetty (Nokia) on 30/05/2023.
 */
public abstract class AbstractOnuPmCollectorKafkaConsumer<T> implements OnuPmCollectorKafkaConsumer<T> {

    private static final Logger LOGGER = Logger.getLogger(AbstractOnuPmCollectorKafkaConsumer.class);
    private final Bundle m_bundle;
    volatile Hashtable<String, java.util.function.Consumer> m_callbackFunctions;
    OnuKafkaNotificationCallback<T> m_callback;

    AbstractOnuPmCollectorKafkaConsumer(Bundle bundle, OnuPerformanceManagement onuPerformanceManagement) {
        m_bundle = bundle;
        m_callback = new OnuKafkaNotificationCallback<>(onuPerformanceManagement);
    }

    @Override
    public abstract void subscribe();

    public void addNotificationCallback() {
        m_callbackFunctions = new Hashtable<>();
        String telemetryTopic = getFromEnvOrSysProperty("KAFKA_TELEMETRY_TOPICS", "vomci1-telemetry");
        m_callbackFunctions.put(telemetryTopic, m_callback::processOnuPmData);
        LOGGER.info("Call back added for telemetry topic");
    }

    public String getFromEnvOrSysProperty(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null) {
            value = System.getProperty(name, null);
        }
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    Properties loadKafkaConfig() {
        Properties config = new Properties();
        try (InputStream stream = m_bundle.getResource("kafka_config.properties").openStream()) {
            config.load(stream);
            config.put("client.id", InetAddress.getLocalHost().getHostName());
        } catch (IOException e) {
            LOGGER.error("KafkaConfigReadException", e);
        }
        LOGGER.info("Kafka: configuration loaded");
        return config;
    }


}