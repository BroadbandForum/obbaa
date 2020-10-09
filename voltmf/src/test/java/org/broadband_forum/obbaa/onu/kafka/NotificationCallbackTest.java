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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import java.util.LinkedHashMap;
import org.broadband_forum.obbaa.onu.VOLTManagement;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public final class NotificationCallbackTest {

    @Mock
    private VOLTManagement m_voltManagement;
    @Mock
    private KafkaNotificationCallback m_callback;
    @Mock
    private ConsumerRecord<String, String> m_testRecord;
    @Mock
    private Hashtable<String, java.util.function.Consumer> m_callbackFunctions;

    private JSONObject m_json;
    private int m_result;


    public NotificationCallbackTest() {
        testResponseCallback();
        testOnNotification();
    }

    @Test
    public void testResponseCallback() {
        MockitoAnnotations.initMocks(this);
        m_json = new JSONObject("{test:testvalue}");
        doNothing().when(m_voltManagement).processResponse(m_json);
        doCallRealMethod().when(m_callback).processResponse(any());
        m_callback = new KafkaNotificationCallback(m_voltManagement);
        m_callback.processResponse(m_json);
        verify(m_voltManagement, times(1)).processResponse(any());
    }

    @Test
    public void testOnNotification() {
        MockitoAnnotations.initMocks(this);
        Map<TopicPartition, List<ConsumerRecord<String, String>>> records = new LinkedHashMap<>();
        ConsumerRecord<String, String> testrecord = new ConsumerRecord<>("topic", 1, 0, "key", "value1");
        records.put(new TopicPartition("topic", 1), Arrays.asList(testrecord));
        ConsumerRecords consumerRecords = new ConsumerRecords(records);
        Hashtable<String, java.util.function.Consumer> callbackFunctions = new Hashtable<String, java.util.function.Consumer>();
        callbackFunctions.put("topic", this::changeResult);
        m_result = 0;
        KafkaNotificationCallback.onNotification(consumerRecords, callbackFunctions);
        assertTrue(m_result == 1);
    }

    public void changeResult(Object obj) {
        Object not_relevant = obj;
        m_result = 1;
    }
}