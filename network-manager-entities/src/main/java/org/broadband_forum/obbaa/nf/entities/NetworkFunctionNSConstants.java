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


package org.broadband_forum.obbaa.nf.entities;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * <p>
 * Namespace constants for network function and network function settings
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 10/05/2021.
 */
public interface NetworkFunctionNSConstants {
    String NS = "urn:bbf:yang:obbaa:network-manager";
    String REVISION = "2021-02-01";
    String NETWORK_MANAGER = "network-manager";
    String NETWORK_FUNCTION = "network-function";
    String NETWORK_FUNCTION_NAME = "name";
    String NETWORK_FUNCTION_TYPE = "type";
    String NETWORK_FUNCTIONS = "network-functions";
    String ONU_FUNCTION = "onu-management-proxy-type";
    String VOMCI_FUNCTION = "vomci-function-type";
    String PROXY_FUNCTION = "vomci-proxy-type";
    String VOLTMF_FUNCTION = "voltmf-type";
    String LISTEN = "listen";
    String IDLE_TIMEOUT = "idle-timeout";
    String LISTEN_ENDPOINT = "listen-endpoint";
    String LISTEN_ENDPOINT_NAME = "name";
    String ACCESS_POINT_NAME = "name";
    String REMOTE_ENDPOINT = "remote-endpoint";
    String REMOTE_ENDPOINTS = "remote-endpoints";
    String REMOTE_ENDPOINT_NAME = "remote-endpoint-name";
    String NF_INITIATE = "nf-initiate";
    String KAFKA_AGENT = "kafka-agent";
    String KAFKA_TRANSPORT_AGENT_PARAMETERS = "kafka-agent-transport-parameters";
    String KAFKA_AGENT_PARAMETERS = "kafka-agent-parameters";
    String KAFKA_PUBLICATION_PARAMETERS = "publication-parameters";
    String KAFKA_CONSUMPTION_PARAMTERS = "consumption-parameters";
    String KAFKA_TOPIC = "topic";
    String TOPIC_NAME = "name";
    String PURPOSE = "purpose";
    String PARTITION = "partition";
    String CLIENT_ID = "client-id";
    String GROUP_ID = "group-id";
    String GRPC_TRANSPORT = "grpc";
    String GRPC_SERVER_PARAMETERS = "grpc-server-parameters";
    String GRPC_CLIENT_PARAMETERS = "grpc-client-parameters";
    String LOCAL_ENDPOINT_NAME = "local-endpoint-name";
    String LOCAL_ADDRESS = "local-address";
    String LOCAL_PORT = "local-port";
    String ACCESS_POINT = "access-point";
    String GRPC_CONNECTION_BACKOFF = "connection-backoff";
    String INITIAL_BACKOFF = "initial-backoff";
    String MIN_CONNECT_TIMEOUT = "min-connect-timeout";
    String MULTIPLIER = "multiplier";
    String JITTER = "jitter";
    String MAX_BACKOFF = "max-backoff";
    String GRPC_CHANNEL = "channel";
    String PING_INTERVAL = "ping-interval";
    String REMOTE_ADDRESS = "remote-address";
    String REMOTE_PORT = "remote-port";
    String GRPC_TRANSPORT_PARAMETERS = "grpc-transport-parameters";
    String REMOTE_ENDPOINT_FUNCTION_NAME = "name";
    String REMOTE_ENDPOINT_TYPE = "type";
    String NF_CLIENT = "nf-client";
    String NETWORK_FUNCTIONS_SETTINGS = "network-functions-settings";
    String NF_SERVER = "nf-server";
    String ENABLED = "enabled";
    String ON_DEMAND = "on-demand";
    SchemaPath NETWORK_MANAGER_SP = SchemaPath.create(true, QName.create(NS, REVISION, NETWORK_MANAGER));

    //Database Constants
    String NETWORK_FUNCTION_DB_NAME = "networkFunctionName";
    String REMOTE_ENDPOINT_DB_NAME = "remoteEndpointName";

    //networkFunction
    ModelNodeId NETWORK_FUNCTIONS_ID_TEMPLATE = new ModelNodeId("/container=network-manager/container=network-functions", NS);

}