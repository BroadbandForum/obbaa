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
    String DELETE_STATE_DATA = "delete-state-data";
    String XPON_ONU_TYPE = "xpon-onu-type";
    String BBF_VOMCI_TYPE_XMLNS_PREFIX = "xmlns:bbf-vomcit";
    String BBF_VOMCI_TYPE_PREFIX = "bbf-vomcit";
    String BBF_VOMCI_TYPE_NS = "urn:bbf:yang:bbf-vomci-types";
    String MANAGED_ONUS = "managed-onus";
    String MANAGED_ONU = "managed-onu";
    String SET_ONU_COMMUNICATION = "set-onu-communication";
    String SET_ONU_COMM_AVAILABLE = "onu-communication-available";
    String OLT_REMOTE_ENDPOINT = "olt-remote-endpoint";
    String VOLTMF_REMOTE_ENDPOINT = "voltmf-remote-endpoint";
    String TERMINATION_POINT_A = "termination-point-a";
    String TERMINATION_POINT_B = "termination-point-b";
    String ONU_ATTACHMENT_POINT = "onu-attachment-point";
    String VOLTMF_NAME = "vOLTMF";
    String ENV_VOLTMF_NAME = "VOLTMF_NAME";
    String NOT_APPLICABLE = "not_applicable";
    String ONU_STATE_CHANGE_NS = "urn:bbf:yang:bbf-xpon-onu-states";
    String CHANNEL_TERMINATION_NS = "urn:bbf:yang:bbf-xpon";
    String SERIAL_NUMBER = "serialNumber";
    String REGISTRATION_ID = "registrationId";
    String OLT_DEVICE_NAME = "oltDeviceName";
    String ONU_ID = "onuID";
    String CHANNEL_TERMINATION_REFERENCE = "channelTermRef";
    String ONU_NAME_JSON_KEY = "onu-name";
    String OLT_NAME_JSON_KEY = "olt-name";
    String CHANNEL_TERMINATION_REF_JSON_KEY = "channel-termination-ref";
    String CHANNEL_TERMINATION_NAME = "channel-termination-name";
    String CHANNEL_TERMINATION = "channel-termination";
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
    char DOUBLE_QUOTES = '"';
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
    String IETF_HARDWARE_JSON_KEY = "ietf-hardware:hardware";
    String IETF_HARDWARE_STATE_JSON_KEY = "ietf-hardware-state:hardware";
    String BBF_SW_MGMT_SOFTWARE_JSON_KEY = "bbf-software-management:software";
    String COMPONENT_JSON_KEY = "component";
    String SOFTWARE_JSON_KEY = "software";
    String REVISIONS_JSON_KEY = "revisions";
    String REVISION_JSON_KEY = "revision";
    String SERIAL_NUM_JSON_KEY = "serial-num";
    String MODEL_NAME_JSON_KEY = "model-name";
    String IETF_HARDWARE_NS = "urn:ietf:params:xml:ns:yang:ietf-hardware";
    String HARDWARE = "hardware";
    String HARDWARE_STATE = "hardware-state";
    String COMPONENT = "component";
    String BBF_SW_MGMT_NS = "urn:bbf:yang:bbf-software-management";
    String SOFTWARE = "software";

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
    String SET_ONU_COMMUNICATION_TRUE = "set-onu-communication-true";
    String SET_ONU_COMMUNICATION_FALSE = "set-onu-communication-false";

    // Operation type constants
    String ONU_GET_OPERATION = "get";
    String ONU_COPY_OPERATION = "copy-config";
    String ONU_EDIT_OPERATION = "edit-config";
    String ONU_GET_ALL_ALARMS_OPERATION = "get-all-alarms";

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
    String VOMCI_FUNCTION_REMOTE_ENDPOINT = "vomci-function-remote-endpoint";

    //Gpb notification
    String VOMCI_FUNC_ONU_ALIGNMENT_RESULT_JSON_KEY = "bbf-vomci-function:onu-alignment-result";
    String ALIGNMENT_STATE_JSON_KEY = "alignment-state";
    String EVENT_TIME_JSON_KEY = "event-time";
    String UNALIGNED = "unaligned";
    String ALIGNED = "aligned";

    //onu-descovery-result-notification
    String ONU_DESCOVERY_RESULT_NS = "urn:bbf:yang:bbf-voltmf-entity";
    String ONU_DESCOVERY_RESULT = "onu-discovery-result";
    String ONU_SERIAL_NUMBER = "onu-serial-number";
    String DISCOVERY_RESULT = "discovery-result";
    String SUCCESSFUL = "successful";
    String FAILED_CONNECTIVITY = "failed-connectivity";
    String DEVICE_INFO = "device-info";
    String SOFTWARE_INFO = "software-info";

    //onu-authentication-report-notification
    String ONU_AUTHENTICATION_REPORT_NOTIFICATION = "onu-authentication-report-notification";
    String ONU_AUTHENTICATION_STATUS = "onu-authentication-status";
    String DETECTED_SERIAL_NUMBER = "detected-serial-number";
    String DETECTED_REGISTARTION_ID = "detected-registration-id";
    String V_ANI_NAME = "v-ani-name";
    String OLT_LOCAL_ONU_NAME = "olt-local-onu-name";
    String IETF_INTERFACES_NS = "urn:ietf:params:xml:ns:yang:ietf-interfaces";
    String INTERFACES = "interfaces";
    String INTERFACES_STATE = "interfaces-state";
    String INTERFACE = "interface";
    String TYPE = "type";
    String VANI_TYPE = "bbf-xponift:v-ani";
    String TYPE_NS = "urn:bbf:yang:bbf-xpon-if-type";
    String VANI_NS = "urn:bbf:yang:bbf-xponvani";
    String VANI = "v-ani";
    String ONU_NAME_TAG = "onu-name";
    String XMLNS_BBF_XPONIFT = "xmlns:bbf-xponift";
    String GET_IT_FROM_OLT_DEVICE = "get-from-olt";

    //IETF Alarms for ONU
    String VOMCI_FUNC_ALARM_MISALIGNMENT_JSON_KEY = "bbf-vomci-function:onu-alarm-misalignment";
    String VOMCI_FUNC_ALARM_MISALIGNMENT_ONU_NAME = "onu-name";
    String IETF_ALARMS_ALARM_NOTIFICATION_JSON_KEY = "ietf-alarms:alarm-notification";
    String RESOURCE_JSON_KEY = "resource";
    String ALARM_TYPE_ID_JSON_KEY = "alarm-type-id";
    String ALARM_TYPE_QUALIFIER_JSON_KEY = "alarm-type-qualifier";
    String TIME_JSON_KEY = "time";
    String PERCEIVED_SEVERITY_JSON_KEY = "perceived-severity";
    String ALARM_TEXT_JSON_KEY = "alarm-text";
    String CLEARED = "cleared";
    String DEVICE_INSTANCE_IDENTIFIER_FORMAT = "baa-network-manager:network-manager/"
            + "baa-network-manager:managed-devices/baa-network-manager:device[baa-network-manager:name='%s']/"
            + "baa-network-manager:root";
    String IETF_ALARMS_ALARM = "alarm";
    String IETF_ALARMS_ALARM_LIST = "alarm-list";
    String IETF_ALARMS_ALARMS = "alarms";
    String IETF_ALARMS_MODULE_NAME = "ietf-alarms";
    String IETF_ALARMS_STATUS_CHANGE = "status-change";

    //onu alarm-notification raised from vOMCI
    String IETF_ALARM_NS = "urn:ietf:params:xml:ns:yang:ietf-alarms";
    String ALARM_NOTIFICATION = "alarm-notification";
    String IETF_ALARM_PREFIX = "al";
    String IETF_HW_NS = "urn:ietf:params:xml:ns:yang:ietf-hardware";
    String XML_NS = "http://www.w3.org/2000/xmlns/";
    String ETH_ALARM_TYPE_NS = "urn:bbf:yang:obbaa:ethernet-alarm-types";
    String ETH_ALARM_PREFIX = "bbf-baa-ethalt";
    String XPON_ONU_ALARM_TYPE_NS = "urn:bbf:yang:obbaa:xpon-onu-alarm-types";
    String IETF_INTERFACE_XMLNS_PREFIX = "xmlns:if";
    String NW_MGR_XMLNS_PREFIX = "xmlns:baa-network-manager";
    String HW_XMLNS_PREFIX = "xmlns:hw";
    String ETH_ALARM_XMLNS_PREFIX = "xmlns:bbf-baa-ethalt";
    String XPON_ONU_ALARM_XMLNS_PREFIX = "xmlns:bbf-baa-xpononualt";
    String XPON_ONU_ALARM_PREFIX = "bbf-baa-xpononualt";
    String HW_TX_ALARM_TYPE_XMLNS_PREFIX = "xmlns:bbf-hw-xcvr-alt";
    String BBF_HW_TX_ALARM_TYPE_NS = "urn:bbf:yang:bbf-hardware-transceiver-alarm-types";
    String BBF_ETH_ALARM_TYPE_MODULE_NAME = "bbf-obbaa-ethernet-alarm-types";
    String BBF_XPON_ONU_ALARM_TYPE_MODULE_NAME = "bbf-obbaa-xpon-onu-alarm-types";
    String IETF_INTERFACES = "ietf-interfaces";
    String IETF_HARDWARE = "ietf-hardware";
    String IETF_INTERFACES_RESOURCE_IDENTIFIER_FORMAT = "/if:interfaces/if:interface[if:name=%s]";
    String IETF_HARDWARE_RESOURCE_IDENTIFIER_FORMAT = "/hw:hardware/hw:component[hw:name=%s]";
    String HW_TX_ALARM_TYPE_PREFIX = "bbf-hw-xcvr-alt";


    String ONU_AUTHENTICATION_NS = "urn:bbf:yang:obbaa:xpon-onu-authentication";
    String ONU_MGMT_MODE_NS = "urn:bbf:yang:obbaa:xpon-onu-types";
    String ONU_AUTHENTICATION_REPORT = "onu-authentication-report";
    String BAA_XPON_ONU_AUTH = "baa-xpon-onu-auth";
    String BAA_XPON_ONU_TYPES_WITH_XMLNS = "xmlns:baa-xpon-onu-types";
    String IF_PREFIX = "if";
    String SERIAL_NUMBER_ONU = "serial-number";
    String AUTH_SCCESSFULL = "authentication-successful";
    String V_ANI = "v-ani";
    String ONU_NAME = "onu-name";
    String REQUESTED_ONU_MGMT_MODE = "requested-onu-management-mode";
    //ONU Auth
    String USE_EOMCI = "baa-xpon-onu-types:use-eomci";
    String USE_VOMCI = "baa-xpon-onu-types:use-vomci";
    String ONU_PRESENT_AND_ON_INTENDED_CHANNEL_TERMINATION = "onu-present-and-on-intended-channel-termination";
    String ONU_PRESENT_AND_V_ANI_KNOW_BUT_INTENDED_CT_UNKNOWN = "onu-present-and-v-ani-known-but-intended-ct-unknown";
    String ONU_PRESENT_AND_V_ANI_KNOW_AND_O5_FAILED_NO_ONU_ID = "onu-present-and-v-ani-known-and-o5-failed-no-onu-id";
    String ONU_PRESENT_AND_V_ANI_KNOW_AND_O5_FAILED_UNDEFINED = "onu-present-and-v-ani-known-and-o5-failed-undefined";
    String ONU_PRESENT_AND_NO_VANI_KNOWN_AND_UNCLAIMED = "onu-present-and-no-v-ani-known-and-unclaimed";
    String RELYING_ON_VOMCI = "relying-on-vomci";
    String EOMCI_BEING_USED = "eomci-being-used";
    String UNABLE_TO_AUTHENTICATE_ONU = "unable-to-authenticate-onu";
    String VOMCI_EXPECTED_BY_OLT_BUT_INCONSISTENT_PMAA_MGMT_MODE = "vomci-expected-by-olt-but-inconsistent-pmaa-mgmt-mode";
    String EOMCI_USED_BY_OLT_CONFLICTING_WITH_PMAA_MGMT_FOR_OLT = "eomci-used-by-olt-conflicting-with-pmaa-mgmt-mode-for-this-olt";
    String VOMCI_EXPECTED_BY_OLT_BUT_MISSING_ONU_CONFIG = "vomci-expected-by-olt-but-pmaa-missing-onu-configuration";
    String ONU_GOING_VOMCI = "onu-going-vomci";
    String ONU_GOING_EOMCI = "onu-going-eomci";
    String ONU_NOT_AUTHENTICATED_NOT_PRESENT = "onu-not-authenticated-not-present";
    String ONU_AUTHENTICATED_AND_MGT_MODE_DETERMINED = "onu-authenticated-and-mgt-mode-determined";
    String EOMCI_PER_OLT_VOMCI_PER_PMAA = "eomci-per-olt-vomci-per-pmaa";
    String VOMCI_PER_OLT_EOMCI_PER_PMAA = "vomci-per-olt-eomci-per-pmaa";
    String ONU_NAME_MISMATCH_OLT_PMAA = "onu-name-mismatch-olt-pmaa";
    String ONU_MGT_UNDETERMINED_ERROR = "onu-mgt-undetermined-error";
    //error-app-tag values
    String ONU_NOT_PRESENT = "baa-xpon-onu-types:onu-not-present";
    String ONU_MGMT_MODE_MISMATCH_WITH_VANI = "baa-xpon-onu-types:onu-management-mode-mismatch-with-v-ani";
    String BAA_XPON_ONU_NAME_MISMATCH_WITH_VANI = "baa-xpon-onu-types:onu-name-mismatch-with-v-ani";
    String UNDETERMINED_ERROR = "baa-xpon-onu-types:undetermined-error";

    String VOMCI = "vomci";

}
