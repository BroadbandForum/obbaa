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

package org.broadband_forum.obbaa.onu;

/**
 * <p>
 * Constants related to ONU
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 22/07/2020.
 */
public interface ONUConstants {

    String ONU_DEVICE_TYPE = "ONU";
    String OLT_DEVICE_TYPE = "OLT";
    String ONU_DETECTED = "detect";
    String ONU_UNDETECTED = "undetect";
    String NOT_APPLICABLE = "not_applicable";
    String ONU_STATE_CHANGE_NS = "urn:bbf:yang:bbf-xpon-onu-states";
    String SERIAL_NUMBER = "serialNumber";
    String REGISTRATION_ID = "registrationId";
    String ONU_NAME_JSON_KEY = "onu-name";
    String OLT_NAME_JSON_KEY = "olt-name";
    String CHANNEL_TERMINATION_REF_JSON_KEY = "channel-termination-ref";
    String ONU_ID_JSON_KEY = "onu-id";
    String PAYLOAD_JSON_KEY = "payload";
    String LABELS_JSON_KEY = "labels";
    String IDENTIFIER_JSON_KEY = "identifier";
    String OPERATION_JSON_KEY = "operation";
    String FILTERS_JSON_KEY = "filters";
    String STATUS_JSON_KEY = "status";
    String DATA_JSON_KEY = "data";
    String FAILURE_REASON = "failure-reason";
    String TRANSACTION_ID = "transaction_id";
    String EVENT = "event";
    String ONU_CURRENT_CONFIG = "current";
    String ONU_TARGET_CONFIG = "target";
    String ONU_DELTA_CONFIG = "delta";
    String COLON = ":";
    String NETWORK_MANAGER = "network-manager";
    String ROOT = "root";
    String DEFAULT_MESSAGE_ID =  "0";
    int JSON_INDENT_FACTOR = 4;
    int MESSAGE_ID_MIN = 0;
    int MESSAGE_ID_MAX = 1000;

    // Kafka topics
    String ONU_REQUEST_KAFKA_TOPIC = "OBBAA_ONU_REQUEST";
    String ONU_RESPONSE_KAFKA_TOPIC = "OBBAA_ONU_RESPONSE";
    String ONU_NOTIFICATION_KAFKA_TOPIC = "OBBAA_ONU_NOTIFICATION";
    String ONU_ALARM_KAFKA_TOPIC = "OBBAA_ONU_ALARM";

    // Events
    String REQUEST_EVENT = "request";
    String RESPONSE_EVENT = "response";
    String DETECT_EVENT = "detect";
    String UNDETECT_EVENT = "undetect";

    // Operation type constants
    String ONU_GET_OPERATION = "get";
    String ONU_COPY_OPERATION = "copy-config";
    String ONU_EDIT_OPERATION = "edit-config";

    //Status constants
    String OK_RESPONSE = "OK";
    String NOK_RESPONSE = "NOK";

    //ONU state
    String OFFLINE = "offline";
    String ONLINE = "online";

    // Threadpools
    int PROCESS_REQ_RESPONSE_THREADS = 5;
    int PROCESS_NOTIF_RESPONSE_THREADS = 5;
    int PROCESS_NOTIF_REQUEST_THREADS = 5;
    int KAFKA_COMMUNICATION_THREADS = 10;
    int KAFKA_CONSUMER_THREADS = 1;
}
