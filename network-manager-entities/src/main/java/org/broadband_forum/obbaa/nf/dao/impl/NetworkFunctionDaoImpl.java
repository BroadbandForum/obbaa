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

package org.broadband_forum.obbaa.nf.dao.impl;


import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.transaction.Transactional;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;
import org.broadband_forum.obbaa.netconf.persistence.jpa.JPAEntityDataStoreManager;
import org.broadband_forum.obbaa.nf.dao.NetworkFunctionDao;
import org.broadband_forum.obbaa.nf.entities.KafkaAgent;
import org.broadband_forum.obbaa.nf.entities.KafkaAgentParameters;
import org.broadband_forum.obbaa.nf.entities.KafkaConsumptionParameters;
import org.broadband_forum.obbaa.nf.entities.KafkaPublicationParameters;
import org.broadband_forum.obbaa.nf.entities.KafkaTopic;
import org.broadband_forum.obbaa.nf.entities.NetworkFunction;
import org.broadband_forum.obbaa.nf.entities.NetworkFunctionNSConstants;
import org.broadband_forum.obbaa.nf.entities.RemoteEndpoint;

/**
 * <p>
 * Dao class implementation for Network Functions and Network Function settings
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 10/05/2021.
 */
public class NetworkFunctionDaoImpl implements NetworkFunctionDao {

    private static final Logger LOGGER = Logger.getLogger(NetworkFunctionDaoImpl.class);
    private final PersistenceManagerUtil m_persistenceManagerUtil;

    public NetworkFunctionDaoImpl(PersistenceManagerUtil persistenceManagerUtil) {
        m_persistenceManagerUtil = persistenceManagerUtil;
    }


    @Override
    public HashSet<String> getKafkaTopicNames(String networkFunctionName, KafkaTopicPurpose kafkaTopicPurpose) {
        HashSet<String> topicNames = new HashSet<>();
        if (networkFunctionName != null && kafkaTopicPurpose != null) {
            Set<KafkaTopic> kafkaTopics = getKafkaTopics(networkFunctionName, kafkaTopicPurpose);
            if (kafkaTopics != null && !kafkaTopics.isEmpty()) {
                for (KafkaTopic topic : kafkaTopics) {
                    if (topic.getPurpose().equals(kafkaTopicPurpose.toString())) {
                        topicNames.add(topic.getTopicName());
                    }
                }
            } else {
                LOGGER.error(String.format("Kafka Topics for the network function %s and kafka topic purpose %s was not found",
                        networkFunctionName, kafkaTopicPurpose));
            }
            if (topicNames.isEmpty()) {
                LOGGER.error(String.format("Kafka topic name for the network function %s and kafka topic purpose %s was not found",
                        networkFunctionName, kafkaTopicPurpose));
            }
        } else {
            LOGGER.error(String.format("Given network function name is NULL"));
        }
        return topicNames;
    }

    private String getRemoteEndPointName(String networkFunctionName) {
        Map<String, Object> matchedValues = new HashMap<String, Object>();
        String endpointName = null;
        matchedValues.put(JPAEntityDataStoreManager.buildQueryPath(NetworkFunctionNSConstants.NETWORK_FUNCTION_DB_NAME),
                networkFunctionName);
        List<NetworkFunction> networkFunctions = m_persistenceManagerUtil.getEntityDataStoreManager()
                .findByMatchValue(NetworkFunction.class, matchedValues);
        if (networkFunctions != null && !networkFunctions.isEmpty()) {
            endpointName = networkFunctions.get(0).getRemoteEndpointName();
        } else {
            LOGGER.error(String.format("Remote endpoint name for the network function %s was not found", networkFunctionName));
        }
        return endpointName;
    }

    private KafkaAgent getKafkaAgent(String remoteEndpointName) {
        Map<String, Object> matchedValues = new HashMap<String, Object>();
        KafkaAgent kafkaAgent = null;
        matchedValues.put(JPAEntityDataStoreManager.buildQueryPath(NetworkFunctionNSConstants.REMOTE_ENDPOINT_DB_NAME), remoteEndpointName);
        List<RemoteEndpoint> remoteEndpoints = m_persistenceManagerUtil.getEntityDataStoreManager()
                .findByMatchValue(RemoteEndpoint.class, matchedValues);
        if (remoteEndpoints != null && !remoteEndpoints.isEmpty()) {
            kafkaAgent = remoteEndpoints.get(0).getKafkaAgent();
        } else {
            LOGGER.error(String.format("Kafka Agent for the remote endpoint %s was not found",remoteEndpointName));
        }
        return kafkaAgent;
    }

    private Set<KafkaTopic> getKafkaTopics(String networkFunctionName, KafkaTopicPurpose kafkaTopicPurpose) {
        Set<KafkaTopic> kafkaTopics = null;
        String remoteEndpointName = getRemoteEndPointName(networkFunctionName);
        if (remoteEndpointName != null) {
            KafkaAgent kafkaAgent = getKafkaAgent(remoteEndpointName);
            if (kafkaAgent != null) {
                KafkaAgentParameters kafkaAgentParameters = kafkaAgent.getKafkaAgentParameters();
                if (kafkaAgentParameters != null) {
                    if (kafkaTopicPurpose.equals(KafkaTopicPurpose.VOMCI_REQUEST)) {
                        KafkaPublicationParameters kafkaPublicationParameters = kafkaAgentParameters.getKafkaPublicationParameters();
                        if (kafkaPublicationParameters != null) {
                            kafkaTopics = kafkaPublicationParameters.getKafkaTopics();
                        } else {
                            LOGGER.error(String.format("kafka Publication Parameter for the network function %s and Kafka "
                                            + "Topic purpose %s was not found", networkFunctionName, kafkaTopicPurpose));
                        }
                    } else {
                        KafkaConsumptionParameters kafkaConsumptionParameters = kafkaAgentParameters.getKafkaConsumptionParameters();
                        if (kafkaConsumptionParameters != null) {
                            kafkaTopics = kafkaConsumptionParameters.getKafkaTopics();
                        } else {
                            LOGGER.error(String.format("kafka Consumption Parameter for the network function %s and Kafka"
                                        + " Topic purpose %s was not found", networkFunctionName, kafkaTopicPurpose));
                        }
                    }
                }
            }
        }
        return kafkaTopics;
    }

    @Override
    public Set<KafkaTopic> getKafkaConsumerTopics(String networkFunctionName) {
        Set<KafkaTopic> kafkaTopicSet = new HashSet<>();
        if (networkFunctionName != null) {
            kafkaTopicSet = getKafkaTopics(networkFunctionName, KafkaTopicPurpose.VOMCI_RESPONSE);
            if (kafkaTopicSet == null || kafkaTopicSet.isEmpty()) {
                LOGGER.error(String.format("kafkaConsumerTopics for the network function %s was not found", networkFunctionName));
            }
        }
        return kafkaTopicSet;
    }

    @Override
    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = {RuntimeException.class})
    public String getLocalEndpointName(String networkFunctionName) {
        String localEndpointName = null;
        String remoteEndpointName  = getRemoteEndPointName(networkFunctionName);
        if (remoteEndpointName != null) {
            localEndpointName = getLocalEndPointName(remoteEndpointName);
        }
        return localEndpointName;
    }

    private String getLocalEndPointName(String remoteEndpointName) {
        Map<String, Object> matchedValues = new HashMap<String, Object>();
        String endpointName = null;
        matchedValues.put(JPAEntityDataStoreManager.buildQueryPath(NetworkFunctionNSConstants.REMOTE_ENDPOINT_DB_NAME),
                remoteEndpointName);
        List<RemoteEndpoint> remoteEndpoints = m_persistenceManagerUtil.getEntityDataStoreManager()
                .findByMatchValue(RemoteEndpoint.class, matchedValues);
        if (remoteEndpoints != null && !remoteEndpoints.isEmpty()) {
            endpointName = remoteEndpoints.get(0).getLocalEndpointName();
        } else {
            LOGGER.error(String.format("Remote endpoint corresponding to %s was not found", remoteEndpointName));
        }
        return endpointName;
    }

}
