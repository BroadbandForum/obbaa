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

package org.broadband_forum.obbaa.onu.kafka.consumer;


import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants;
import org.broadband_forum.obbaa.nf.dao.NetworkFunctionDao;
import org.broadband_forum.obbaa.nf.dao.impl.KafkaTopicPurpose;
import org.broadband_forum.obbaa.nf.entities.KafkaTopic;
import org.broadband_forum.obbaa.nm.devicemanager.DeviceManager;
import org.broadband_forum.obbaa.onu.ONUConstants;
import org.broadband_forum.obbaa.onu.VOLTManagement;
import org.broadband_forum.obbaa.onu.kafka.KafkaNotificationCallback;
import org.osgi.framework.Bundle;

/**
 * <p>
 * Abstract class for Kafka Consumer, contains the generic functions of the kafka consumer.
 * </p>
 * Created by Filipe Claudio (Altice Labs) on 09/06/2021.
 */
public abstract class AbstractOnuKafkaConsumer<T> implements OnuKafkaConsumer<T> {

    private static final Logger LOGGER = Logger.getLogger(AbstractOnuKafkaConsumer.class);
    private final Bundle m_bundle;
    volatile Hashtable<String, java.util.function.Consumer> m_callbackFunctions;
    KafkaNotificationCallback<T> m_callback;
    private DeviceManager m_deviceManager;
    private NetworkFunctionDao m_networkFunctionDao;

    AbstractOnuKafkaConsumer(VOLTManagement voltMgmt, Bundle bundle, DeviceManager deviceManager, NetworkFunctionDao networkFunctionDao) {
        m_bundle = bundle;
        m_callback = new KafkaNotificationCallback<>(voltMgmt);
        m_deviceManager = deviceManager;
        m_networkFunctionDao = networkFunctionDao;
    }

    @Override
    public abstract void subscribe();

    @Override
    public abstract void updateSubscriberTopics(Set<KafkaTopic> kafkaTopicSet);

    void updateSubscriberTopics(Set<KafkaTopic> kafkaTopicSet, Consumer<String, T> consumer) {
        boolean isChanged = false;
        for (KafkaTopic kafkaTopic : kafkaTopicSet) {
            if (kafkaTopic.getPurpose().equals(KafkaTopicPurpose.VOMCI_NOTIFICATION.toString())
                    && !m_callbackFunctions.contains(kafkaTopic.getTopicName())) {
                m_callbackFunctions.put(kafkaTopic.getTopicName(), m_callback::processNotification);
                isChanged = true;
            } else if (kafkaTopic.getPurpose().equals(KafkaTopicPurpose.VOMCI_RESPONSE.toString())
                    && !m_callbackFunctions.contains(kafkaTopic.getTopicName())) {
                m_callbackFunctions.put(kafkaTopic.getTopicName(), m_callback::processResponse);
                isChanged = true;
            }
        }

        if (isChanged) {
            LOGGER.debug("Subscribing to topics " + m_callbackFunctions.keySet());
            consumer.subscribe(m_callbackFunctions.keySet());
        }
    }

    @Override
    public abstract void removeSubscriberTopics(Set<String> kafkaTopicSet);

    public void removeSubscriberTopics(Set<String> kafkaTopicSet, Consumer<String, T> consumer) {
        boolean isChanged = false;
        List<String> deletedKafkaTopicList = new ArrayList<>();
        if (kafkaTopicSet != null && !kafkaTopicSet.isEmpty()) {
            for (String kafkaTopicName : kafkaTopicSet) {
                if (m_callbackFunctions.remove(kafkaTopicName) != null) {
                    isChanged = true;
                    deletedKafkaTopicList.add(kafkaTopicName);
                }
            }
        }

        if (isChanged) {
            LOGGER.info("Unsubscribing unused topics. New KafkaTopic set: " + m_callbackFunctions.keySet());
            consumer.subscribe(m_callbackFunctions.keySet());
            deleteTopics(deletedKafkaTopicList);
        }
    }

    private void deleteTopics(List<String> kafkaTopicList) {
        Properties properties = loadKafkaConfig();
        try (AdminClient kafkaAdminClient = KafkaAdminClient.create(properties)) {
            kafkaAdminClient.deleteTopics(kafkaTopicList);
            //Waiting to finish kafkaAdminClient.deleteTopics(kafkaTopicList) execution.
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            LOGGER.error("Exception while deleting unsubscribed topics", ex);
            Thread.currentThread().interrupt();
        }
    }

    public void addNotificationCallback() {
        m_callbackFunctions = new Hashtable<>();
        m_callbackFunctions.put(ONUConstants.ONU_ALARM_KAFKA_TOPIC, m_callback::processNotification);
        m_callbackFunctions.put(ONUConstants.ONU_RESPONSE_KAFKA_TOPIC, m_callback::processResponse);
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

    void subscribeTopics() {
        if (!m_deviceManager.getAllDevices().isEmpty()) {
            Set<String> nwFunctionNames = getAllNetworkFunctionName();
            if (nwFunctionNames != null && !nwFunctionNames.isEmpty()) {
                registerAllNetWorkFunctionTopics(nwFunctionNames);
            }
        }
    }

    private Set<String> getAllNetworkFunctionName() {
        List<Device> devicesList = m_deviceManager.getAllDevices();
        Set<String> nfFunctionNamesSet = new HashSet<>();
        for (Device device : devicesList) {
            if (device.getDeviceManagement().getDeviceType().equals(DeviceManagerNSConstants.DEVICE_TYPE_ONU)
                    && device.isMediatedSession()) {
                String nwFunctionName = device.getDeviceManagement().getOnuConfigInfo().getVomciOnuManagement().getVomciFunction();
                if (nwFunctionName != null) {
                    nfFunctionNamesSet.add(nwFunctionName);
                }
            }
        }
        return nfFunctionNamesSet;
    }

    private void registerAllNetWorkFunctionTopics(Set<String> nwFunctionNames) {
        Set<KafkaTopic> kafkaConsumerTopics = new HashSet<>();
        for (String name : nwFunctionNames) {
            Set<KafkaTopic> consumerTopics = m_networkFunctionDao.getKafkaConsumerTopics(name);
            if (consumerTopics != null && !consumerTopics.isEmpty()) {
                kafkaConsumerTopics.addAll(consumerTopics);
            } else {
                LOGGER.error(String.format("Kafka Consumer topics for the network function name %s is empty or null", name));
            }
        }
        updateSubscriberTopics(kafkaConsumerTopics);
    }

    //test only method
    public Hashtable<String, java.util.function.Consumer> getCallBackFunctionList() {
        return m_callbackFunctions;
    }
}