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
package org.broadband_forum.obbaa.onu.pm.kafka;

import java.util.Hashtable;
import java.util.function.Consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.onu.pm.datahandler.OnuPerformanceManagement;

/**
 * <p>
 * Implements decision making for notifications consumed via Kafka
 * </p>
 * Created by Madhukar Shetty (Nokia) on 30/05/2023.
 */

public class OnuKafkaNotificationCallback<T> {

    private static final Logger LOGGER = Logger.getLogger(OnuKafkaNotificationCallback.class);

    OnuPerformanceManagement m_onuPerformanceManagement;

    public OnuKafkaNotificationCallback(OnuPerformanceManagement onuPerformanceManagement) {
        m_onuPerformanceManagement = onuPerformanceManagement;
    }

    public void onNotification(final ConsumerRecords<String, T> records,
                               Hashtable<String, Consumer> callbackFunctions) {
        for (final ConsumerRecord<String, T> record : records) {
            final T value = record.value();
            final String topic = record.topic();
            callbackFunctions.get(topic).accept(value);
        }
    }


    public void processOnuPmData(Object obj) {
        m_onuPerformanceManagement.processOnuPmData(obj);
    }
}