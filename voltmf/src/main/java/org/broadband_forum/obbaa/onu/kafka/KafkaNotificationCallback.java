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

import java.util.Hashtable;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.onu.VOLTManagement;
import org.json.JSONException;
import org.json.JSONObject;

/**
* <p>
* Implements decision making for notifications consumed via Kafka
* </p>
* Created by Marc Michalke on <15/07/2020>.
*/

public class KafkaNotificationCallback {

    public KafkaNotificationCallback(final VOLTManagement voltMgmt) {
        m_voltMgmt = voltMgmt;
    }

    private static final Logger LOGGER = Logger.getLogger(KafkaNotificationCallback.class);
    private VOLTManagement m_voltMgmt;

    public static void onNotification(final ConsumerRecords<String, String> records,
            Hashtable<String, java.util.function.Consumer> callbackFunctions) {
        for (final ConsumerRecord<String, String> record : records) {
            final String value = record.value();
            final String topic = record.topic();
            callbackFunctions.get(topic).accept(value);
        }
    }

    public void processAlarm(Object obj) {
        String msg = obj.toString();
        //m_voltMgmt.processAlarm(msg);
        LOGGER.debug("Sent message " + obj.toString());
    }

    public void processResponse(Object obj) {
        try {
            JSONObject jsonResponse = new JSONObject(obj.toString());
            m_voltMgmt.processResponse(jsonResponse);
            LOGGER.debug("Sent message " + obj.toString());
        } catch (JSONException e) {
            LOGGER.error("Unable to form JSONObject" + e);
        }
    }
    //If additional callback functions have to be added, don't forget to add them to
    //OnuKafkaConsumer#addNotificationCallback() as well
}