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
    String NS = "urn:bbf:yang:obbaa:network-manager";
    String REVISION = "2018-05-07";
    String NETWORK_MANAGER = "network-manager";
    String MANAGED_DEVICES = "managed-devices";
    String NEW_DEVICES = "new-devices";
    String DEVICE_ADAPTERS = "device-adapters";
    String DEVICE_ADAPTER = "device-adapter";
    SchemaPath NETWORK_MANAGER_SP = SchemaPath.create(true , QName.create(NS , REVISION , NETWORK_MANAGER));
    SchemaPath MANAGED_DEVICES_SP = SchemaPath.create(true, QName.create(NS, REVISION, NETWORK_MANAGER),
        QName.create(NS, REVISION, MANAGED_DEVICES));
    SchemaPath NEW_DEVICES_SP = SchemaPath.create(true, QName.create(NS , REVISION , NETWORK_MANAGER),
        QName.create(NS, REVISION, NEW_DEVICES));
    SchemaPath DEVICE_ADAPTERS_SP = SchemaPath.create(true, QName.create(NS , REVISION , NETWORK_MANAGER),
        QName.create(NS, REVISION, DEVICE_ADAPTERS));

    ModelNodeId MANAGED_DEVICES_ID_TEMPLATE = new ModelNodeId("/container=network-manager/container=managed-devices", NS);
    ModelNodeId AUTH_ID_TEMPLATE = new ModelNodeId("/container=network-manager/container=managed-devices"
        + "/container=device/name=direct-device"
        + "/container=device-management/container=device-connection"
        + "/container=password-auth/container=authentication", NS);
    String CONTAINER_DEVICE_TEMPLATE = "/container=device/name=";
    String DEVICE_MANAGEMENT = "device-management";
    ModelNodeId DEVICE_ID_TEMPLATE = new ModelNodeId("/container=network-manager/container=" + MANAGED_DEVICES
        + CONTAINER_DEVICE_TEMPLATE + "directDevice/container=" + DEVICE_MANAGEMENT, NS);
    String NEW_DEVICE = "new-device";
    ModelNodeId NETWORK_MANAGER_ID_TEMPLATE = new ModelNodeId("/container=network-manager", NS);
    String NAME = "name";
    String TYPE = "type";
    String INTERFACE_VERSION = "interface-version";
    String MODEL = "model";
    String VENDOR = "vendor";
    String PUSH_PMA_CONFIGURATION_TO_DEVICE = "push-pma-configuration-to-device";
    String DEVICE_CONNECTION = "device-connection";
    String NS_REVISION = "2018-05-07";
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
}
