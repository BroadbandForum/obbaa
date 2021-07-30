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
    String CREATE_ONU = "create-onu";
    String DELETE_ONU = "delete-onu";
    String MANAGED_ONUS = "managed-onus";
    String MANAGED_ONU = "managed-onu";
    String SET_ONU_COMMUNICATION = "set-onu-communication";
    String SET_ONU_COMM_AVAILABLE = "onu-communication-available";
    String OLT_REMOTE_NAME = "olt-remote-endpoint-name";
    String VOLTMF_REMOTE_NAME = "voltmf-remote-endpoint-name";
    String TERMINATION_POINT_A = "termination-point-a";
    String TERMINATION_POINT_B = "termination-point-b";
    String ONU_ATTACHMENT_POINT = "onu-attachment-point";
    String VOLTMF_NAME = "vOLTMF";
    String NOT_APPLICABLE = "not_applicable";
    String ONU_STATE_CHANGE_NS = "urn:bbf:yang:bbf-xpon-onu-states";
    String SERIAL_NUMBER = "serialNumber";
    String REGISTRATION_ID = "registrationId";
    String OLT_DEVICE_NAME = "oltDeviceName";
    String ONU_ID = "onuID";
    String CHANNEL_TERMINATION_REFERENCE = "channelTermRef";
    String ONU_NAME_JSON_KEY = "onu-name";
    String OLT_NAME_JSON_KEY = "olt-name";
    String CHANNEL_TERMINATION_REF_JSON_KEY = "channel-termination-ref";
    String CHANNEL_TERMINATION_NAME = "channel-termination-name";
    String ONU_ID_JSON_KEY = "onu-id";
    String PAYLOAD_JSON_KEY = "payload";
    String LABELS_JSON_KEY = "labels";
    String IDENTIFIER_JSON_KEY = "identifier";
    String EQPT_ID_JSON_KEY = "equipment-id";
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
    String DEFAULT_MESSAGE_ID = "0";
    String NETWORK_MANAGER_JSON_KEY = "network-manager:device-management";
    String DEVICE_STATE_JSON_KEY = "device-state";
    String ONU_STATE_INFO_JSON_KEY = "bbf-obbaa-onu-management:onu-state-info";
    int JSON_INDENT_FACTOR = 4;
    int MESSAGE_ID_MIN = 0;
    int MESSAGE_ID_MAX = 1000;

    // SOFTWARE images JSON KEYS
    String SOFTWARE_IMAGES_JSON_KEY = "software-images";
    String SOFTWARE_IMAGE_JSON_KEY = "software-image";
    String SOFTWARE_IMAGE_ID_JSON_KEY = "id";
    String SOFTWARE_IMAGE_VERSION_JSON_KEY = "version";
    String SOFTWARE_IMAGE_IS_COMMITTED_JSON_KEY = "is-committed";
    String SOFTWARE_IMAGE_IS_ACTIVE_JSON_KEY = "is-active";
    String SOFTWARE_IMAGE_IS_VALID_JSON_KEY = "is-valid";
    String SOFTWARE_IMAGE_PRODUCT_CODE_JSON_KEY = "product-code";
    String SOFTWARE_IMAGE_HASH_JSON_KEY = "hash";
    String ONU_NAME_PREFIX = "onu_";
    String DOT = ".";
    String INVALID_EDIT_CONFIG = "Invalid edit-config";
    String EDIT_CONFIG_SYNCED = "Edit-config already synced";


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
    String ERROR_RESPONSE = "ERROR_GENERAL";

    //ONU state
    String OFFLINE = "offline";
    String ONLINE = "online";

    // Threadpools
    int PROCESS_REQ_RESPONSE_THREADS = 5;
    int PROCESS_NOTIF_RESPONSE_THREADS = 5;
    int PROCESS_NOTIF_REQUEST_THREADS = 5;
    int KAFKA_COMMUNICATION_THREADS = 10;
    int KAFKA_CONSUMER_THREADS = 1;


    String NETWORK_MANAGER_NAMESPACE = "urn:bbf:yang:obbaa:network-manager";
    String OBBAA_NETWORK_MANAGER = "bbf-obbaa-network-manager:network-manager";
    String BBF_VOMCI_FUNCTION_NS = "urn:bbf:yang:bbf-vomci-function";
    String BBF_VOMCI_PROXY_NS = "urn:bbf:yang:bbf-vomci-proxy";
    String SUBTREE_FILTER = "subtree";
    String MANAGED_DEVICES = "managed-devices";
    String DEVICE = "device";
    String NAME = "name";
    String DEVICE_MANAGEMENT = "device-management";
    String DEVICE_STATE = "device-state";
    String ONU_STATE_INFO = "onu-state-info";
    String ONU_STATE_INFO_NAMESPACE = "urn:bbf:yang:obbaa:onu-management";
    String EQUIPEMENT_ID = "equipment-id";
    String SOFTWARE_IMAGES = "software-images";
    String SOFTWARE_IMAGE = "software-image";
    String GET_FILTER = "{\"network-manager:device-management\":{\"device-state\":{\"bbf-obbaa-onu-management:onu-state-info\""
            + ":{\"equipment-id\":\"\",\"software-images\":{\"software-image\":{}}}}}}";
    String VOLTMF_LOCAL_ENDPOINT_NAME = "KAFKA_LOCAL_ENDPOINT_NAME";
    String VOMCI_FUNCTION_REMOTE_ENDPOINT = "vomci-func-remote-endpoint-name";

    //Gpb notification
    String VOMCI_FUNC_ONU_ALIGNMENT_STATUS_JSON_KEY = "bbf-vomci-function:onu-alignment-status";
    String ALIGNMENT_STATUS_JSON_KEY = "alignment-status";
    String EVENT_TIME_JSON_KEY = "event-time";
    String UNALIGNED = "unaligned";
    String ALIGNED = "aligned";

}
