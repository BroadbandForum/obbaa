/*
 * Copyright 2018 Broadband Forum
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

package org.broadband_forum.obbaa.dmyang.entities;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public interface DeviceManagerNSConstants {
    String PREFIX = "baa-network-manager";
    String NS = "urn:bbf:yang:obbaa:network-manager";
    String ONU_MANAGEMENT_NS = "urn:bbf:yang:obbaa:onu-management";
    String ONU_MANAGEMENT_REVISION = "2020-07-15";
    String REVISION = "2020-07-23";
    String NETWORK_MANAGER = "network-manager";
    String MANAGED_DEVICES = "managed-devices";
    String NEW_DEVICES = "new-devices";
    String DEVICE_ADAPTERS = "device-adapters";
    String DEVICE_ADAPTER = "device-adapter";
    SchemaPath NETWORK_MANAGER_SP = SchemaPath.create(true, QName.create(NS, REVISION, NETWORK_MANAGER));
    SchemaPath MANAGED_DEVICES_SP = SchemaPath.create(true, QName.create(NS, REVISION, NETWORK_MANAGER),
            QName.create(NS, REVISION, MANAGED_DEVICES));
    SchemaPath NEW_DEVICES_SP = SchemaPath.create(true, QName.create(NS, REVISION, NETWORK_MANAGER),
            QName.create(NS, REVISION, NEW_DEVICES));
    SchemaPath DEVICE_ADAPTERS_SP = SchemaPath.create(true, QName.create(NS, REVISION, NETWORK_MANAGER),
            QName.create(NS, REVISION, DEVICE_ADAPTERS));

    ModelNodeId MANAGED_DEVICES_ID_TEMPLATE = new ModelNodeId("/container=network-manager/container=managed-devices", NS);
    ModelNodeId AUTH_ID_TEMPLATE = new ModelNodeId("/container=network-manager/container=managed-devices"
            + "/container=device/name=direct-device"
            + "/container=device-management/container=device-connection"
            + "/container=password-auth/container=authentication", NS);
    ModelNodeId SNMP_AUTH_ID_TEMPLATE = new ModelNodeId("/container=network-manager/container=managed-devices"
            + "/container=device/name=snmp-device"
            + "/container=device-management/container=device-connection"
            + "/container=snmp-auth/container=snmp-authentication", NS);
    String CONTAINER_DEVICE_TEMPLATE = "/container=device/name=";
    String DEVICE_MANAGEMENT = "device-management";
    ModelNodeId DEVICE_ID_TEMPLATE = new ModelNodeId("/container=network-manager/container=" + MANAGED_DEVICES
            + CONTAINER_DEVICE_TEMPLATE + "directDevice/container=" + DEVICE_MANAGEMENT, NS);
    String NEW_DEVICE = "new-device";
    ModelNodeId NETWORK_MANAGER_ID_TEMPLATE = new ModelNodeId("/container=network-manager", NS);

    static String rootPathForDevice(String deviceName) {
        return "/baa-network-manager:network-manager/baa-network-manager:managed-devices/baa-network-manager:device"
                + "[baa-network-manager:name='" + deviceName + "']/baa-network-manager:root";
    }

    String NAME = "name";
    String TYPE = "type";
    String INTERFACE_VERSION = "interface-version";
    String MODEL = "model";
    String VENDOR = "vendor";
    String DESCRIPTION = "description";
    String IS_NETCONF = "is-netconf";
    String DEVELOPER = "developer";
    String ADAPTER_REVISION = "revision";
    String UPLOAD_DATE = "upload-date";
    String PUSH_PMA_CONFIGURATION_TO_DEVICE = "push-pma-configuration-to-device";
    String DEVICE_CONNECTION = "device-connection";
    String DEVICE_TYPE_ONU = "ONU";
    String ONU_CONFIG_INFO = "onu-config-info";
    String ONU_STATE_INFO = "onu-state-info";
    String EXPECTED_ATTACHMENT_POINT = "expected-attachment-point";
    String OLT_NAME = "olt-name";
    String CHANNEL_PARTITION = "channel-partition";
    String ONU_ID = "onu-id";
    String DEVICE_STATE = "device-state";
    String DEVICE = "device";
    String CONNECTION_MODEL = "connection-model";
    String CONNECTION_STATE = "connection-state";
    String PASSWORD_AUTH = "password-auth";
    String AUTHENTICATION = "authentication";
    String ADDRESS = "address";
    String USER_NAME = "user-name";
    String PASSWORD = "password";
    String MANAGEMENT_PORT = "management-port";
    String CONFIGURATION_ALIGNMENT_STATE = "configuration-alignment-state";
    String CONNECTED = "connected";
    String CONNECTION_CREATION_TIME = "connection-creation-time";
    String DEVICE_NOTIFICATION = "device-notification";
    String EVENT = "event";
    String DUID = "duid";
    String DEVICE_CAPABILITY = "device-capability";
    String ALIGNED = "Aligned";
    String IN_ERROR = "In Error";
    String NOT_ALIGNED = "Not Aligned";
    String NEVER_ALIGNED = "Never Aligned";
    String CALL_HOME = "call-home";
    String SNMP = "snmp";
    String MEDIATED_SESSION = "mediated-session";
    String MEDIATED_PROTOCOL = "mediated-protocol";
    String SERIAL_NUMBER = "serial-number";
    String REGISTRATION_ID = "registration-id";
    String VENDOR_NAME = "vendor-name";
    String AUTHENTICATION_METHOD = "authentication-method";
    String XPON_TECHNOLOGY = "xpon-technology";
    String ID = "id";
    String DEVICES_RELATED = "devices-related";
    String DEVICE_COUNT = "device-count";
    String IN_USE = "in-use";
    String DEVICE_STATE_CHANGE = "device-state-change";
    String YANG_MODULES = "yang-modules";
    String MODULE = "module";
    String DEVICE_ADAPTER_COUNT = "device-adapter-count";
    String SNMP_AUTH = "snmp-auth";
    String SNMP_AUTHENTICATION = "snmp-authentication";
    String AGENT_PORT = "agent-port";
    String TRAP_PORT = "trap-port";
    String SNMP_VERSION = "snmp-version";
    String COMMUNITY_STRING = "community-string";
    String SNMPV3_AUTH = "snmpv3-auth";
    String SECURITY_LEVEL = "security-level";
    String AUTH_PROTOCOL = "auth-protocol";
    String AUTH_PASSWORD = "auth-password";
    String PRIV_PROTOCOL = "priv-protocol";
    String PRIV_PASSWORD = "priv-password";
    String FACTORY_GARMENT_TAG = "factory-garment-tag";
    String ADHERENCE_TO_STD_MODULES = "percentage-adherence-to-standard-module";
    String DEVIATED_STD_MODULE = "deviated-standard-module";
    String PERCENTAGE_STD_MODULES_HAVING_DEVIATION = "percentage-of-standard-modules-having-deviation";
    String AUGMENTED_STD_MODULES = "augmented-standard-module";
    String PERCENTAGE_STD_MODULES_HAVING_AUGMENTS = "percentage-of-standard-modules-having-augments";
    String NUMBER_OF_MODULES_PRESENT_IN_STD_ADAPTERS = "number-of-modules-present-in-standard-adapter";
    String NUMBER_OF_MODULES_PRESENT_IN_VENDOR_ADAPTERS = "total-number-of-modules-present";
}
