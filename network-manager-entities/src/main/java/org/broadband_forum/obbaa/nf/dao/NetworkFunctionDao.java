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

package org.broadband_forum.obbaa.nf.dao;

import java.util.HashSet;
import java.util.Set;

import org.broadband_forum.obbaa.nf.dao.impl.KafkaTopicPurpose;
import org.broadband_forum.obbaa.nf.entities.KafkaTopic;

/**
 * <p>
 * Dao class for Network Functions and Network Function settings
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 10/05/2021.
 */
public interface NetworkFunctionDao {

    HashSet<String> getKafkaTopicNames(String networkFunctionName, KafkaTopicPurpose kafkaTopicPurpose);

    Set<KafkaTopic> getKafkaConsumerTopics(String networkFunctionName);

    String getLocalEndpointName(String networkFunctionName);
}
