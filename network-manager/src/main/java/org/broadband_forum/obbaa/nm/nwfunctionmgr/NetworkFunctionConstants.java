/*
 * Copyright 2022 Broadband Forum
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

package org.broadband_forum.obbaa.nm.nwfunctionmgr;

public interface NetworkFunctionConstants {

    String SOFTWARE_VERSION = "software-version";
    String USAGE = "usage";
    String VIRTUAL_NETWORK_FUNCTION_TYPE = "network-function-type";
    String VIRTUAL_NETWORK_FUNCTION = "virtual-network-function";
    String VIRTUAL_NETWORK_FUNCTION_INSTANCE = "virtual-network-function-instance";
    String NETWORK_FUNCTION_STATE = "network-function-state";
    String VIRTUAL_NETWORK_FUNCTION_VENDOR = "vendor";
    String NETWORK_FUNCTION_STATE_NAMESPACE = "urn:bbf:yang:obbaa:network-function-state";
    String BBF_BAA_NFSTATE = "bbf-baa-nfstate";
    String NAME = "name";
    String VIRTUAL_NETWORK_FUNCTION_INSTANCE_NAME = "virtualNetworkFunctionInstanceName";
    String VIRTUAL_NETWORK_FUNCTION_NAME = "virtualNetworkFunction";
    String ADMIN_STATE = "admin-state";
    String OPER_STATE = "oper-state";
    String SOFTWARE_VERSION_NAME = "softwareVersion";
    String VIRTUAL_NETWORK_FUNCTION_TYPE_NAME = "networkFunctionType";
    String ADMIN_STATE_NAME = "adminState";
    String OPER_STATE_NAME = "operState";
    String BAA_HOST_IP = "BAA_HOST_IP";
    String HTTP = "http://";
    String PORT = ":8093";
    String GET_NETWORK_FUNCTION_URI_PATH = "/nbi/api/getNetworkFunction";
    String GET_NETWORK_FUNCTION_INSTANCES_URI_PATH = "/nbi/api/getNetworkFunctionInstances";
    String BAA_MICRO_SERVICE_DISCOVERY_API_ADDRESS = "BAA_MICRO_SERVICE_DISCOVERY_API_ADDRESS";
}
