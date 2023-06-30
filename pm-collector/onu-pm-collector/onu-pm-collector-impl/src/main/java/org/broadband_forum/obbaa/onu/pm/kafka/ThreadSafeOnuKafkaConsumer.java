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

import java.time.Duration;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;


public class ThreadSafeOnuKafkaConsumer<K, V> extends KafkaConsumer {

    public ThreadSafeOnuKafkaConsumer(Properties properties) {
        super(properties);
    }

    @Override
    public synchronized void subscribe(Collection collection) {
        super.subscribe(collection);
    }

    @Override
    public synchronized ConsumerRecords poll(long timeout) {
        return super.poll(Duration.ofMillis(timeout));
    }

    @Override
    public synchronized void close() {
        super.close();
    }

    @Override
    public synchronized Set<String> subscription() {
        return super.subscription();
    }
}
