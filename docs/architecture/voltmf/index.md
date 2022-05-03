
<a id="voltmf" />

# vOLT Management Function (vOLTMF)

## Introduction

This section describes the high-level design of the vOLT Management
function (vOLTMF) used to manage ONUs through the vOMCI solution. This
section describes communication between the vOLTMF, vOMCI Proxy and
vOMCI function upon:

-   Creating and deleting ONUs

-   Receiving onu-state-change notifications

-   Sending requests to ONUs

The vOLTMF manages ONUs through a standard ONU adapter that is deployed
in BAA, the association of which is based on the model, type, vendor and
version mentioned while creating the ONU. The standard ONU adapter uses
the standard library of the YANG modules for ONUs that the vOLTMF refers
to for handling ONU requests, responses and notifications from external
management systems. The following figure depicts the vOLTMF and ONU
Adapter components that reside in the BAA microservice.

<p align="center">
 <img width="600px" height="400px" src="{{site.url}}/architecture/voltmf/voltmf_design.png">
</p>

The vOLTMF performs actions upon receiving notifications and requests
either from an OLT device or other components within the BAA core. For
example, the onu-state-change notification that is sent by the OLT
device on its Northbound Interface (NBI) that is received by BAA core.
The BAA core propagates the notification towards vOLTMF and BAA NBI so
that it can be handled by the Access SDN M&C.

Upon reception of the notification, the vOLTMF processes the
notification, checks if a preconfigured ONU device exists and
authenticates the ONU, the vOLTMF transforms the notification to Google
Protobufs (GPB) format and propagates the set-onu-communication Action
towards the vOMCI function and vOMCI-proxy via the Kafka bus.

All the YANG requests are sent towards the vOMCI function and vOMCI
Proxy via the Kafka bus in GPB format. Once the vOMCI function/Proxy
processes the requests, the vOMCI function sends the
notification/request response in GPB format back to the vOLTMF via the
Kafka bus and the response is received through the
KafkaNotificationCallback\#onNotification().

Upon receiving the response, the vOLTMF is responsible for processing
the response and performs actions accordingly.

### vOLTMF Threadpools

There could be multiple interactions between the vOLTMF and the vOMCI
function including parallel configuration requests/commands for either
the same or different ONUs. These interactions are parallel and
asynchronous such that the requests are not idle/blocked while waiting
for responses because the vOLTMF has separate task queues and
threadpools to handle the request/response interactions. The following
table shows the list of vOLTMF threadpools that spawned as new Runnable
tasks:


|Name| Description|
| :--- | :--- |
|processNotificationRequestPool|Used for processing the mediated device event listener callbacks (deviceAdded, deviceRemoved) and device notification requests (onu-state-change notifications).|
|kafkaCommunicationPool|Used to process the individual GET/COPY-CONFIG/EDIT-CONFIG requests inside a MediatedDeviceNetconfSession spawned by processRequestResponsePool..|
|kafkaPollingPool|Used to start up the KafkaConsumer implementation and polling for responses from vOMCI-function/vOMCI Proxy.|
|processNotificationResponsePool|Used for processing notification responses from the vOMCI-function/vOMCI Proxy.|
|processRequestResponsePool|Used for processing GET/COPY-CONFIG/EDIT-CONFIG requests and responses from the vOMCI-function/vOMCI Proxy.|

## vOMCI function and vOMCI Proxy deployment

Prior to communicating with an vOMCI function or vOMCI Proxy, the vOLTMF
has the following preconditions:

-   An external orchestration or administration function (e.g., VNF
    manager) has deployed the vOMCI function and vOMCI Proxy instances

-   An external management function has configured the BAA layer with
    the connectivity parameters necessary for the vOLTMF to connect with
    the vOMCI function or vOMCI Proxy instance.

For this release, the communication channel between the vOMCI function
and the vOLTMF uses a Kafka message transport that is used to send
management requests and receive notifications to/from vOMCI function for
the ONU or the function itself. Likewise a Kafka management channel
exists between the vOMCI Proxy and the BAA core. The BAA core acts as an
intermediary between the vOMCI entities and the SDN M&C, converting
NETCONF requests to YANG messages using the same messages that the
vOLTMF uses to communicate with the vOMCI function.

<p align="center">
 <img width="1000px" height="1000px" src="{{site.url}}/architecture/voltmf/vomci_f_p_deployment.png">
</p>

## Network function management

The figure below depicts an example deployment of vOMCI function and
vOMCI Proxy instances that are connected to the vOLTMF and BAA core
using a Kafka bus. These connections and entities use the messages
associated with TR-451\'s vOLTMF-vOMCI interface. Additionally the vOMCI
function, vOMCI Proxy instances and the OLT are interconnected using
gRPC channels. These entities use the messages associated with TR-451\'s
vOMCI-OLT interface. Finally the OLT connects to the BAA core using
NETCONF for management of the OLT. These entities use the messages
associated with TR-413\'s Minf NBI interface for PNF of type OLT.

<p align="center">
 <img width="800px" height="800px" src="{{site.url}}/architecture/voltmf/network_function_mgmt.png">
</p>

### Use of Endpoints for Connectivity

Network functions and entities like the vOLTMF and BAA core expose
server endpoints (local-endpoints) that can be used by remote entities
in order to establish as connection for communication. Likewise entities
use client endpoints associated with a remote entity (remote-endpoints)
in order to establish communication with the remote-entity. These
endpoints have identifying names that are used to ensure an entity is
communicating with a remote-endpoint over the correct connection.

In the case of the Kafka message transport, the vOLTMF connects as
client to a Kafka broker and publishes and consumes information to a
remote entity though a specified set of topics. In the case of the gRPC
message transport the entity that acts as the server would expose the
local-endpoint and the entity that acts as the client would use a
remote-point of the remote entity.

The following figure depicts an example of the local-endpoints that are
exposed by entities to which the remote entity would connect using the
remote-endpoint information. The vOLTMF has two (2) logical
local-endpoints (vOLTMF\_Kafka\_1, vOLTMF\_Kafka\_2) that are
respectively used by vomci-vendor-1 and proxy-1 to exchange messages
with the vOLTMF. Likewise the vOLTMF uses logical local-endpoints
exposed by the vomci-vendor-1 and proxy-1 as the vOLTMF\'s client
remote-endpoints.

<p align="center">
 <img width="600px" height="600px" src="{{site.url}}/architecture/voltmf/network_function_endpoints.png">
</p>

Note: The underlying connectivity between the entities has to be
established before the vOLTMF can begin to manage an ONU.

The vOLTMF uses the network-function-settings in the
bbf-obbaa-network-manager.yang tree as described below to define its
server-based local endpoints as well as a list of client-based
remote-endpoints. Since the client-based remote-endpoints is simply a
list of remote-endpoints, the remote-endpoint-name within the meta-data
of a network-function is used to correlate the endpoints from the
vOLTMF\'s client list to the network-function from the vOLTMF\'s list of
network functions.

<p align="center">
 <img width="800px" height="800px" src="{{site.url}}/architecture/voltmf/network_manager_nfs.png">
</p>

### Use of Endpoints for the ONU Management Chain

The vOLTMF uses endpoints that link to together network functions in a
topology in order to determine an ONU\'s management chain.

The bbf-obbaa-onu-management.yang tree contains meta-data that maintains
the endpoints and links when discovery of the topology is not possible
as shown in the network-function-links node below:

<p align="center">
 <img width="800px" height="1000px" src="{{site.url}}/architecture/voltmf/endpoint_topology.png">
</p>

## Kafka topics for the vOMCI function and vOMCI Proxy

When a new vOMCI function or vOMCI Proxy is deployed in the cloud, an
external management function has to configure the BAA layer regarding
information about the network to include how the vOLTMF should connect
with the vOMCI function or vOMCI Proxy. When Kafka is used as the
message transport the vOMCI function or vOMCI Proxy\'s endpoint
information needs to be configured as shown below:

```
<?xml version="1.0" encoding="utf-8"?>
<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="18">
  <edit-config>
    <target>
      <running/>
    </target>
    <config>
      <baa-network-manager:network-manager xmlns:baa-network-manager="urn:bbf:yang:obbaa:network-manager">
        <baa-network-manager:network-functions-settings>
          <baa-network-manager:nf-client>
            <baa-network-manager:enabled>true</baa-network-manager:enabled>
              <baa-network-manager:nf-initiate>
                <baa-network-manager:remote-endpoints>
                     <!-- vomci -->
                    <baa-network-manager:remote-endpoint>
                    <baa-network-manager:name>vOMCi-kfk-1</baa-network-manager:name>
                    <baa-network-manager:type xmlns:bbf-nf-types="urn:bbf:yang:bbf-network-function-types">bbf-nf-types:vomci-function-type</baa-network-manager:type>
				    <baa-network-manager:local-endpoint-name>vOLTMF_Kafka_1</baa-network-manager:local-endpoint-name>
                    <baa-network-manager:kafka-agent>
                      <baa-network-manager:kafka-agent-parameters>
                        <baa-network-manager:client-id>client-id1</baa-network-manager:client-id>
                        <baa-network-manager:publication-parameters>
                          <baa-network-manager:topic>
                            <baa-network-manager:name>vomci1-request</baa-network-manager:name>
                            <baa-network-manager:purpose>VOMCI_REQUEST</baa-network-manager:purpose>
                          </baa-network-manager:topic>
                        </baa-network-manager:publication-parameters>
                        <baa-network-manager:consumption-parameters>
                          <baa-network-manager:group-id>group-id</baa-network-manager:group-id>
                          <baa-network-manager:topic>
                            <baa-network-manager:name>vomci1-response</baa-network-manager:name>
                            <baa-network-manager:purpose>VOMCI_RESPONSE</baa-network-manager:purpose>
                          </baa-network-manager:topic>
                          <baa-network-manager:topic>
                            <baa-network-manager:name>vomci1-notification</baa-network-manager:name>
                            <baa-network-manager:purpose>VOMCI_NOTIFICATION</baa-network-manager:purpose>
                          </baa-network-manager:topic>
                        </baa-network-manager:consumption-parameters>
                      </baa-network-manager:kafka-agent-parameters>
                    </baa-network-manager:kafka-agent>
                    <baa-network-manager:access-point>
                      <baa-network-manager:name>vomci1</baa-network-manager:name>
                      <baa-network-manager:kafka-agent>
                        <baa-network-manager:kafka-agent-transport-parameters>
                          <baa-network-manager:remote-address>kafka-host</baa-network-manager:remote-address>
                        </baa-network-manager:kafka-agent-transport-parameters>
                      </baa-network-manager:kafka-agent>
                    </baa-network-manager:access-point>
                  </baa-network-manager:remote-endpoint>

                  <!-- proxy -->
                  <baa-network-manager:remote-endpoint>
                    <baa-network-manager:name>proxy-kfk-1</baa-network-manager:name>
                    <baa-network-manager:type xmlns:bbf-nf-types="urn:bbf:yang:bbf-network-function-types">bbf-nf-types:vomci-proxy-type</baa-network-manager:type>
    				<baa-network-manager:local-endpoint-name>vOLTMF_Kafka_2</baa-network-manager:local-endpoint-name>
                    <baa-network-manager:kafka-agent>
                      <baa-network-manager:kafka-agent-parameters>
                        <baa-network-manager:client-id>client-id2</baa-network-manager:client-id>
                        <baa-network-manager:publication-parameters>
                          <baa-network-manager:topic>
                            <baa-network-manager:name>vomci-proxy-request</baa-network-manager:name>
                            <baa-network-manager:purpose>VOMCI_REQUEST</baa-network-manager:purpose>
                          </baa-network-manager:topic>
                        </baa-network-manager:publication-parameters>
                        <baa-network-manager:consumption-parameters>
                          <baa-network-manager:group-id>group-id</baa-network-manager:group-id>
                          <baa-network-manager:topic>
                            <baa-network-manager:name>vomci-proxy-response</baa-network-manager:name>
                            <baa-network-manager:purpose>VOMCI_RESPONSE</baa-network-manager:purpose>
                          </baa-network-manager:topic>
                          <baa-network-manager:topic>
                            <baa-network-manager:name>vomci-proxy-notification</baa-network-manager:name>
                            <baa-network-manager:purpose>VOMCI_NOTIFICATION</baa-network-manager:purpose>
                          </baa-network-manager:topic>
                        </baa-network-manager:consumption-parameters>
                      </baa-network-manager:kafka-agent-parameters>
                    </baa-network-manager:kafka-agent>
                    <baa-network-manager:access-point>
                      <baa-network-manager:name>vomci-proxy</baa-network-manager:name>
                      <baa-network-manager:kafka-agent>
                        <baa-network-manager:kafka-agent-transport-parameters>
                          <baa-network-manager:remote-address>kafka-host</baa-network-manager:remote-address>
                        </baa-network-manager:kafka-agent-transport-parameters>
                      </baa-network-manager:kafka-agent>
                    </baa-network-manager:access-point>
                  </baa-network-manager:remote-endpoint>
                </baa-network-manager:remote-endpoints>
              </baa-network-manager:nf-initiate>
          </baa-network-manager:nf-client>
        </baa-network-manager:network-functions-settings>

        <!-- network functions -->
        <baa-network-manager:network-functions>
          <baa-network-manager:network-function>
            <baa-network-manager:name>vomci-vendor-1</baa-network-manager:name>
            <baa-network-manager:type xmlns:bbf-nf-types="urn:bbf:yang:bbf-network-function-types">bbf-nf-types:vomci-function-type</baa-network-manager:type>
             <!-- must match the local endpoint name configured locally in the vOMCI function -->
            <baa-network-manager:remote-endpoint-name>vOMCi-kfk-1</baa-network-manager:remote-endpoint-name>
          </baa-network-manager:network-function>
          <baa-network-manager:network-function>
            <baa-network-manager:name>proxy-1</baa-network-manager:name>
            <baa-network-manager:type xmlns:bbf-nf-types="urn:bbf:yang:bbf-network-function-types">bbf-nf-types:vomci-proxy-type</baa-network-manager:type>
             <!-- must match the local endpoint name configured locally in the Proxy -->
            <baa-network-manager:remote-endpoint-name>proxy-kfk-1</baa-network-manager:remote-endpoint-name>
          </baa-network-manager:network-function>
        </baa-network-manager:network-functions>

      </baa-network-manager:network-manager>
    </config>
  </edit-config>
</rpc>
```

### Obtaining Kafka Topics when Communicating with an ONU

Each ONU that is managed via vOMCI has to be associated with a vOMCI
function that is represented as part of the ONU\'s metadata maintained
by the BAA core. Before publishing messages for an ONU, the vOLTMF needs to determine the Kafka topics on which the messages must be published. The vOLTMF does so by invoking the following API in the
NetworkFunctionDao.java:

**String getKafkaTopicName(String networkFunctionName, KafkaTopicPurpose
kafkaTopicPurpose);**

-   kafkaTopicPurpose is an enum with the following definition:

```
public enum KafkaTopicPurpose{
    VOMCI_NOTIFICATION, VOMCI_REQUEST, VOMCI_RESPONSE
}
```

-   networkFunctionName corresponding to the vOMCI function can be
    retrieved using the API DeviceDao\#getVomciFunctionName(deviceName).

### Lifecycle Management of Network Functions and Topic Maintenance

The NetworkFunctionSubsystem is used to create, modify and delete network
functions. When a new network function is deployed, the vOLTMF must
subscribe to the Kafka topics configured with purpose as
VOMCI\_NOTIFICATION and VOMCI\_RESPONSE in order to listen to messages
coming from that network function. When the network function is
undeployed the subscribed topics must be unsubscribed. All the requests
from vOLTMF to the networkfunction are forwarded on the topic with
purpose VOMCI\_REQUEST.

## gRPC connection establishment between pOLT and vOMCI Proxy or vOMCI function

The communication channel between the pOLT and the vOMCI Proxy or vOMCI
function uses gRPC as the message transport. Prior to any communication
with the ONU the communication session between the the vOMCI function
and vOMCI Proxy has to be established. For establishing the connection
between the pOLT and the vOMCI Proxy or the vOMCI function, the
association between the pOLT and vOMCI Proxy or vOMCI function has to be
configured in the BAA layer by the Access SDN M&C. The following code
block provides an example configuration for associating vOMCI Proxy
within the pOLT.

```
<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="34566755">
  <edit-config>
    <target>
      <running/>
    </target>
    <config>
      <network-manager xmlns="urn:bbf:yang:obbaa:network-manager">
        <managed-devices>
          <device>
            <name>OLT1</name>
            <root>
              <bbf-olt-vomci:remote-network-function xmlns:bbf-olt-vomci="urn:bbf:yang:bbf-olt-vomci">
                <bbf-olt-vomci:nf-client>
                  <bbf-olt-vomci:enabled>true</bbf-olt-vomci:enabled>
                  <bbf-olt-vomci:nf-initiate>
                    <bbf-olt-vomci:remote-endpoints>
                      <bbf-olt-vomci:remote-endpoint>
                        <bbf-olt-vomci:name>olt-grpc-2</bbf-olt-vomci:name>
                        <bbf-olt-vomci:local-endpoint-name>olt-grpc-2</bbf-olt-vomci:local-endpoint-name>
                        <bbf-olt-vomci:type xmlns:bbf-nf-types="urn:bbf:yang:bbf-network-function-types">bbf-nf-types:vomci-proxy-type</bbf-olt-vomci:type>
                        <bbf-olt-vomci:grpc/>
                        <bbf-olt-vomci:access-point>
                          <bbf-olt-vomci:name>vOMCIProxy</bbf-olt-vomci:name>
                          <bbf-olt-vomci:grpc>
                            <bbf-olt-vomci:grpc-transport-parameters>
                              <bbf-olt-vomci:remote-address>192.168.240.1</bbf-olt-vomci:remote-address>
                              <bbf-olt-vomci:remote-port>8433</bbf-olt-vomci:remote-port>
                            </bbf-olt-vomci:grpc-transport-parameters>
                          </bbf-olt-vomci:grpc>
                        </bbf-olt-vomci:access-point>
                      </bbf-olt-vomci:remote-endpoint>
                    </bbf-olt-vomci:remote-endpoints>
                  </bbf-olt-vomci:nf-initiate>
                </bbf-olt-vomci:nf-client>
                <bbf-olt-vomci:nf-endpoint-filter>
                  <bbf-olt-vomci:rule>
                    <bbf-olt-vomci:name>client_rule1</bbf-olt-vomci:name>
                    <bbf-olt-vomci:priority>1</bbf-olt-vomci:priority>
                    <bbf-olt-vomci:flexible-match>
                      <bbf-olt-vomci:onu-vendor>ABCD</bbf-olt-vomci:onu-vendor>
                    </bbf-olt-vomci:flexible-match>
                    <bbf-olt-vomci:resulting-endpoint>olt-grpc-2</bbf-olt-vomci:resulting-endpoint>
                  </bbf-olt-vomci:rule>
                </bbf-olt-vomci:nf-endpoint-filter>
                <bbf-olt-vomci:nf-server>
                  <bbf-olt-vomci:enabled>true</bbf-olt-vomci:enabled>
                </bbf-olt-vomci:nf-server>
              </bbf-olt-vomci:remote-network-function>
            </root>
          </device>
        </managed-devices>
      </network-manager>
    </config>
  </edit-config>
</rpc>  
```

Note:

1. Once this is configured on pOLT, the pOLT sends the HelloVomciRequest to the vOMCI proxy. After receiving the HelloVomciRequest, the vOMCI proxy registers this pOLT and uses this information when the vOMCI proxy has to forward OMCI messages towards this pOLT.
2. The remote address in the vOMCIProxy access-point is the docker network's gatewayip address. In our setup, baadist_default -- is the docker network created via the docker-compose file that has vOMCIProxy and so baadist_default's gateway IP address is used as the remote address in the vOMCIProxy access-point..

## ONU Management
### ONU Device Data Model

In OB-BAA, the ONU is managed as a separate network element, like the
OLT and uses a new a connection type \"mediated-session\" as defined by
the [Aggregrators](../aggregator/index.md#aggregator) component\'s
bbf-network-manager.yang YANG module in order to communicate with the
ONU. Additionally, a YANG module that represents the ONU\'s management
metadata is defined in bbf-obbaa-onu-management.yang. The metadata
information in this YANG module includes data needed to discover and
authenticate the ONU among other items. For example:

-   The *onu-state-info* container holds the actual information read
    from the ONU when it becomes online.

-   The *onu-config* container contains the information for
    authenticating the ONU and that must be provided when creating the
    pONU instance in OB-BAA> It includes:

    -   planned-onu-management-mode (please refer to ONU Authentication Function for more details).
    -   serial-number
    -   registration-id
    -	  expected-attachment-point

The bbf-obbaa-onu-management.yang and bbf-network-manager.yang YANG
modules can be found in the /resources/models/yang/aggregator-yang
directory.

```
module: bbf-obbaa-network-manager
  +--rw network-manager
     +--rw managed-devices
     |  +--rw device* [name]
     |     +--rw name                   string
     |     +--rw device-management
     |     |  +--rw type?                               string
     |     |  +--rw interface-version?                  string
     |     |  +--rw model?                              string
     |     |  +--rw vendor?                             string
     |     |  +--rw push-pma-configuration-to-device?   boolean
     |     |  +--ro is-netconf?                         boolean
     |     |  +--rw device-connection
     |     |  |  +--rw connection-model?          enumeration
     |     |  |  +--rw (protocol)
     |     |  |     +--:(password-auth)
     |     |  |     |  +--rw password-auth
     |     |  |     |     +--rw authentication
     |     |  |     |        +--rw address            inet:ip-address
     |     |  |     |        +--rw management-port?   uint32
     |     |  |     |        +--rw user-name?         string
     |     |  |     |        +--rw password?          string
     |     |  |     +--:(snmp-auth)
     |     |  |     |  +--rw snmp-auth
     |     |  |     |     +--rw snmp-authentication
     |     |  |     |        +--rw address                   inet:ip-address
     |     |  |     |        +--rw agent-port?               inet:port-number
     |     |  |     |        +--rw trap-port?                inet:port-number
     |     |  |     |        +--rw snmp-version?             enumeration
     |     |  |     |        +--rw (auth-info)?
     |     |  |     |           +--:(community-string)
     |     |  |     |           |  +--rw community-string?   string
     |     |  |     |           +--:(snmpv3-auth)
     |     |  |     |              +--rw snmpv3-auth
     |     |  |     |                 +--rw user-name?        string
     |     |  |     |                 +--rw security-level?   enumeration
     |     |  |     |                 +--rw auth-protocol?    enumeration
     |     |  |     |                 +--rw auth-password?    string
     |     |  |     |                 +--rw priv-protocol?    enumeration
     |     |  |     |                 +--rw priv-password?    string
     |     |  |     +--:(duid)
     |     |  |     |  +--rw duid?                string
     |     |  |     +--:(mediated-protocol)
     |     |  |        +--rw mediated-protocol?   enumeration
     |     |  +--ro device-state
     |     |     +--ro configuration-alignment-state?   string
     |     |     +--ro connection-state
     |     |        +--ro connected?                  boolean
     |     |        +--ro connection-creation-time?   yang:date-and-time
     |     |        +--ro device-capability*          string
     |     +--rw device-notification
     |     |  +---n device-state-change
     |     |     +-- event?   enumeration
     |     +--rw root
     +--rw network-functions-settings
     |  +--rw nf-client {nf-client-supported}?
     |  |  +--rw enabled?       boolean
     |  |  +--rw nf-initiate!
     |  |     +--rw remote-endpoints
     |  |        +--rw remote-endpoint* [name]
     |  |           +--rw name                             bbf-yang:string-ascii64
     |  |           +--rw nf-type?                         identityref
     |  |           +--rw on-demand?                       boolean
     |  |           +--rw local-endpoint-name?             bbf-yang:string-ascii64
     |  |           +--rw (client-transport)
     |  |           |  +--:(grpc) {bbf-nfc:grpc-client-supported}?
     |  |           |  |  +--rw grpc
     |  |           |  |     +--rw grpc-client-parameters
     |  |           |  |        +--rw channel
     |  |           |  |        |  +--rw ping-interval?   uint32
     |  |           |  |        +--rw connection-backoff {bbf-grpc:connection-backoff-supported}?
     |  |           |  |           +--rw initial-backoff?       uint16
     |  |           |  |           +--rw min-connect-timeout?   uint16
     |  |           |  |           +--rw multiplier?            decimal64
     |  |           |  |           +--rw jitter?                decimal64
     |  |           |  |           +--rw max-backoff?           uint16
     |  |           |  +--:(kafka-agent) {bbf-nfc:kafka-agent-supported}?
     |  |           |     +--rw kafka-agent
     |  |           |        +--rw kafka-agent-parameters
     |  |           |           +--rw client-id?                string
     |  |           |           +--rw publication-parameters {bbf-kafkaa:publication-supported}?
     |  |           |           |  +--rw topic* [name]
     |  |           |           |     +--rw name         string
     |  |           |           |     +--rw purpose?     string
     |  |           |           |     +--rw partition?   string
     |  |           |           +--rw consumption-parameters {bbf-kafkaa:consumption-supported}?
     |  |           |              +--rw group-id?   string
     |  |           |              +--rw topic* [name]
     |  |           |                 +--rw name         string
     |  |           |                 +--rw purpose?     string
     |  |           |                 +--rw partition?   string
     |  |           +--rw access-point* [name]
     |  |           |  +--rw name                 bbf-yang:string-ascii64
     |  |           |  +--rw (message-transport)
     |  |           |     +--:(grpc) {bbf-nfc:grpc-client-supported}?
     |  |           |     |  +--rw grpc
     |  |           |     |     +--rw grpc-transport-parameters
     |  |           |     |        +--rw remote-address    inet:host
     |  |           |     |        +--rw remote-port?      inet:port-number
     |  |           |     |        +--rw local-address?    inet:ip-address {local-binding-supported}?
     |  |           |     |        +--rw local-port?       inet:port-number {local-binding-supported}?
     |  |           |     |        +--rw proxy-server! {proxy-connect}?
     |  |           |     |        |  +--rw (proxy-type)
     |  |           |     |        |     +--:(socks4)
     |  |           |     |        |     |  +--rw socks4-parameters
     |  |           |     |        |     |     +--rw remote-address    inet:ip-address
     |  |           |     |        |     |     +--rw remote-port?      inet:port-number
     |  |           |     |        |     +--:(socks4a)
     |  |           |     |        |     |  +--rw socks4a-parameters
     |  |           |     |        |     |     +--rw remote-address    inet:host
     |  |           |     |        |     |     +--rw remote-port?      inet:port-number
     |  |           |     |        |     +--:(socks5)
     |  |           |     |        |        +--rw socks5-parameters
     |  |           |     |        |           +--rw remote-address               inet:host
     |  |           |     |        |           +--rw remote-port?                 inet:port-number
     |  |           |     |        |           +--rw authentication-parameters!
     |  |           |     |        |              +--rw (auth-type)
     |  |           |     |        |                 +--:(gss-api) {socks5-gss-api}?
     |  |           |     |        |                 |  +--rw gss-api
     |  |           |     |        |                 +--:(username-password) {socks5-username-password}?
     |  |           |     |        |                    +--rw username-password
     |  |           |     |        |                       +--rw username                    string
     |  |           |     |        |                       +--rw (password-type)
     |  |           |     |        |                          +--:(cleartext-password)
     |  |           |     |        |                          |  +--rw cleartext-password?   string
     |  |           |     |        |                          +--:(encrypted-password) {password-encryption}?
     |  |           |     |        |                             +--rw encrypted-password
     |  |           |     |        |                                +--rw encrypted-by
     |  |           |     |        |                                +--rw encrypted-value    binary
     |  |           |     |        +--rw keepalives! {keepalives-supported}?
     |  |           |     |           +--rw idle-time         uint16
     |  |           |     |           +--rw max-probes        uint16
     |  |           |     |           +--rw probe-interval    uint16
     |  |           |     +--:(kafka-agent) {bbf-nfc:kafka-agent-supported}?
     |  |           |        +--rw kafka-agent
     |  |           |           +--rw kafka-agent-transport-parameters
     |  |           |              +--rw remote-address    inet:host
     |  |           |              +--rw remote-port?      inet:port-number
     |  |           |              +--rw local-address?    inet:ip-address {local-binding-supported}?
     |  |           |              +--rw local-port?       inet:port-number {local-binding-supported}?
     |  |           |              +--rw proxy-server! {proxy-connect}?
     |  |           |              |  +--rw (proxy-type)
     |  |           |              |     +--:(socks4)
     |  |           |              |     |  +--rw socks4-parameters
     |  |           |              |     |     +--rw remote-address    inet:ip-address
     |  |           |              |     |     +--rw remote-port?      inet:port-number
     |  |           |              |     +--:(socks4a)
     |  |           |              |     |  +--rw socks4a-parameters
     |  |           |              |     |     +--rw remote-address    inet:host
     |  |           |              |     |     +--rw remote-port?      inet:port-number
     |  |           |              |     +--:(socks5)
     |  |           |              |        +--rw socks5-parameters
     |  |           |              |           +--rw remote-address               inet:host
     |  |           |              |           +--rw remote-port?                 inet:port-number
     |  |           |              |           +--rw authentication-parameters!
     |  |           |              |              +--rw (auth-type)
     |  |           |              |                 +--:(gss-api) {socks5-gss-api}?
     |  |           |              |                 |  +--rw gss-api
     |  |           |              |                 +--:(username-password) {socks5-username-password}?
     |  |           |              |                    +--rw username-password
     |  |           |              |                       +--rw username                    string
     |  |           |              |                       +--rw (password-type)
     |  |           |              |                          +--:(cleartext-password)
     |  |           |              |                          |  +--rw cleartext-password?   string
     |  |           |              |                          +--:(encrypted-password) {password-encryption}?
     |  |           |              |                             +--rw encrypted-password
     |  |           |              |                                +--rw encrypted-by
     |  |           |              |                                +--rw encrypted-value    binary
     |  |           |              +--rw keepalives! {keepalives-supported}?
     |  |           |                 +--rw idle-time         uint16
     |  |           |                 +--rw max-probes        uint16
     |  |           |                 +--rw probe-interval    uint16
     |  |           +---n remote-endpoint-status-change
     |  |              +-- access-point                         -> ../../access-point/name
     |  |              +-- connected                            boolean
     |  |              +-- remote-endpoint-state-last-change    yang:date-and-time
     |  +--rw nf-server {nf-server-supported}?
     |     +--rw enabled?   boolean
     |     +--rw listen!
     |        +--rw idle-timeout?      uint16
     |        +--rw listen-endpoint* [name]
     |           +--rw name                bbf-yang:string-ascii64
     |           +--rw (transport)
     |           |  +--:(grpc)
     |           |     +--rw grpc
     |           |        +--rw grpc-server-parameters
     |           |           +--rw local-endpoint-name?   bbf-yang:string-ascii64
     |           |           +--rw local-address          inet:ip-address
     |           |           +--rw local-port?            inet:port-number
     |           |           +--rw keepalives! {keepalives-supported}?
     |           |              +--rw idle-time         uint16
     |           |              +--rw max-probes        uint16
     |           |              +--rw probe-interval    uint16
     |           +--rw remote-endpoints
     |              +--ro remote-endpoint* [name]
     |              |  +--ro name    bbf-yang:string-ascii64
     |              +---n remote-endpoint-status-change
     |                 +-- remote-endpoint                      -> ../../../remote-endpoints/remote-endpoint/name
     |                 +-- connected                            boolean
     |                 +-- remote-endpoint-state-last-change    yang:date-and-time
     +--rw network-functions
     |  +--rw network-function* [name]
     |     +--rw name                    string
     |     +--rw type?                   identityref
     |     +--rw remote-endpoint-name?   string
     |     +--rw root
     +--ro new-devices
     |  +--ro new-device* [duid]
     |     +--ro duid                 string
     |     +--ro device-capability*   string
     +--ro device-adapters
        +--ro device-adapter-count?   string
        +--ro device-adapter* [type interface-version model vendor]
           +--ro type                                string
           +--ro interface-version                   string
           +--ro model                               string
           +--ro vendor                              string
           +--ro push-pma-configuration-to-device?   boolean
           +--ro is-netconf?                         boolean
           +--ro description?                        string
           +--ro developer?                          string
           +--ro revision?                           string
           +--ro upload-date?                        yang:date-and-time
           +--ro in-use?                             boolean
           +--ro devices-related
           |  +--ro device-count?   uint32
           |  +--ro device* [name]
           |     +--ro name    -> /network-manager/managed-devices/device/name
           +--ro yang-modules
           |  +--ro module* [name revision]
           |     +--ro name        -> /yanglib:modules-state/module/name
           |     +--ro revision    -> /yanglib:modules-state/module/revision
           +--ro factory-garment-tag
              +--ro total-number-of-modules-present?                   string
              +--ro number-of-modules-present-in-standard-adapter?     string
              +--ro percentage-adherence-to-standard-module?           string
              +--ro deviated-standard-module*                          string
              +--ro percentage-of-standard-modules-having-deviation?   string
              +--ro augmented-standard-module*                         string
              +--ro percentage-of-standard-modules-having-augments?    string
```

```
module: bbf-obbaa-onu-management

  augment /baa-network-manager:network-manager/baa-network-manager:managed-devices/baa-network-manager:device/baa-network-manager:device-management:
    +--rw onu-config-info
       +--rw vendor-name?                   string
       +--rw expected-serial-number?        bbf-xpon-types:onu-serial-number
       +--rw expected-registration-id?      bbf-xpon-types:onu-registration-id
       +--rw xpon-technology?               identityref
       +--rw planned-onu-management-mode?   identityref
       +--rw expected-attachment-points
       |  +--rw list-type?                   enumeration
       |  +--rw expected-attachment-point* [name]
       |     +--rw name                                       string
       |     +--rw olt-name                                   -> /baa-network-manager:network-manager/managed-devices/device/name
       |     +--rw channel-partition-name?                    string
       |     +--rw planned-onu-management-mode-in-this-olt?   identityref
       +--rw vomci-onu-management
          +--rw use-vomci-management?             boolean
          +--rw onu-management-chain-selection?   bbf-vomcit:onu-management-chain-selection
          +--rw vomci-function?                   bbf-yang:string-ascii64
          +--rw onu-management-chain* [nf-type nf-name]
          |  +--rw nf-type    bbf-vomcit:vomci-entity-type
          |  +--rw nf-name    bbf-yang:string-ascii64
          +--rw network-function-links
             +--rw network-function-link* [name]
                +--rw name                   string
                +--rw termination-point-a
                |  +--rw function-name          string
                |  +--rw local-endpoint-name    string
                +--rw termination-point-b
                   +--rw function-name          string
                   +--rw local-endpoint-name    string
  augment /baa-network-manager:network-manager/baa-network-manager:managed-devices/baa-network-manager:device/baa-network-manager:device-management/baa-network-manager:device-state:
    +--ro onu-state-info
       +--ro onu-state                         identityref
       +--ro determined-onu-management-mode?   identityref
       +--ro detected-serial-number?           bbf-xpon-types:onu-serial-number
       +--ro detected-registration-id?         bbf-xpon-types:onu-registration-id
       +--ro vendor-id?                        string
       +--ro equipment-id?                     string
       +--ro attachment-point
       |  +--ro olt-name                    string
       |  +--ro channel-termination-name    string
       |  +--ro onu-id?                     bbf-xpon-types:onu-id
       |  +--ro v-ani-name?                 string
       |  +--ro olt-local-onu-name?         string
       +--ro software-images
       |  +--ro software-image* [id]
       |     +--ro id              uint8
       |     +--ro version?        string
       |     +--ro is-committed    boolean
       |     +--ro is-active       boolean
       |     +--ro is-valid        boolean
       |     +--ro product-code?   string
       |     +--ro hash?           string
       +--ro voltmf-msg-data
          +--ro in-messages?       bbf-yang:performance-counter64
          +--ro out-messages?      bbf-yang:performance-counter64
          +--ro messages-errors?   bbf-yang:performance-counter64

  notifications:
    +---n onu-authentication-report-notification
       +--ro onu-authentication-status    identityref
       +--ro olt-name                     string
       +--ro channel-termination-name     string
       +--ro onu-id?                      bbf-xpon-types:onu-id
       +--ro detected-serial-number?      bbf-xpon-types:onu-serial-number
       +--ro detected-registration-id?    bbf-xpon-types:onu-registration-id
       +--ro v-ani-name?                  string
       +--ro olt-local-onu-name?          string
       +--ro onu-name?                    string
```

### Standard ONU Adapter

The management of an ONU utilizes the Device Adapter concept within
OB-BAA and the OB-BAA distribution provides an ONU standard adapter
called bbf-onu-standard that can be found in the distribution\'s
/resources/models/standard-adapters directory. The standard ONU adapter
has below properties:

-   adapterType: ONU

-   adapterModel: standard

-   vendor: BBF

-   version: 1.0

### ONU Creation

ONU\'s can be created within the BAA layer either before an ONU is
detected (pre-provisioned) by an OLT or once an ONU has been detected by
an OLT. When the ONU has been pre-provisioned within the BAA layer, the
ONU device is created and persisted in the BAA layer\'s datastore.
Additionally, the vOMCI function and vOMCI Proxy that are part of the
ONU\'s management chain also notified with Create-ONU RPC requests about
the ONU that is created in the BAA layer.

Whenever the OLT notifies the BAA layer that the OLT has detected an
ONU, the OLT sends an ONU state change notification that the BAA layer
uses to determine if the change of ONU state is because the OLT has
detected an ONU attachment. If the BAA layer has determined that the ONU
attachment has been detected but the BAA layer doesn\'t have the ONU
device configured in the BAA layer\'s datastore, the ONU is treated as
an orphaned or unknown ONU. Once the ONU has been created within the BAA
layer with match of the ONU identification properties and the
authentication of the ONU succeeds, a set-onu-communication Action
Request is forwarded to vOMCI Proxy and vOMCI function that is part of
the ONU management chain.

**Info:** Release 5.0 introduces new options for ONU authentication. Pleaser refer to the ONU Authentication page for mored details. In this section, it is assumed that ONU authentication is performed by the OLT and that the ONU created in the BAA layer is managed trough vOMCI. When an ONU name is not included in the notifications sent by the OLT, the matching of the ONU identification properties is made through parameters such as the attachment point, the Serial Number and the Registration ID.

#### ONU creation with the management chain specified by SDN M&C

In this release, the ONU management chain configuration is expected to be configured by an external management entity. The external management entity will send the ONU management chain details with the create-onu netconf request. In a future release, the BAA layer will be able to support the determination of the ONU management chain.

The following is an example of the Create ONU request that contains the ONU\'s management chain:

```
<!--
   ~ Copyright 2018 Broadband Forum
   ~
   ~ Licensed under the Apache License, Version 2.0 (the "License");
   ~ you may not use this file except in compliance with the License.
   ~ You may obtain a copy of the License at
   ~
   ~ http://www.apache.org/licenses/LICENSE-2.0
   ~
   ~ Unless required by applicable law or agreed to in writing, software
   ~ distributed under the License is distributed on an "AS IS" BASIS,
   ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   ~ See the License for the specific language governing permissions and
   ~ limitations under the License.
-->
<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="1527307907657">
   <edit-config>
      <target>
         <running />
      </target>
      <config>
         <network-manager xmlns="urn:bbf:yang:obbaa:network-manager">
            <managed-devices>
               <device xmlns:xc="urn:ietf:params:xml:ns:netconf:base:1.0" xc:operation="create">
                  <name>onuA</name>
                  <device-management>
                     <type>ONU</type>
                     <interface-version>1.1</interface-version>
                     <vendor>BBF</vendor>
                     <model>standard</model>
                     <device-connection>
                        <connection-model>mediated-session</connection-model>
                        <mediated-protocol>vomci</mediated-protocol>
                     </device-connection>
                     <onu-config-info xmlns="urn:bbf:yang:obbaa:onu-management" xmlns:onu="urn:bbf:yang:obbaa:onu-management">
                        <expected-serial-number>ABCD12345678</expected-serial-number>
                        <planned-onu-management-mode xmlns:baa-onu-types="urn:bbf:yang:obbaa:xpon-onu-types">baa-onu-types:use-vomci</planned-onu-management-mode>
                        <expected-attachment-points>
                           <list-type>allow-any</list-type>
                           <expected-attachment-point>
                              <name>OLT1-CPart_1</name>
                              <olt-name>OLT1</olt-name>
                              <channel-partition-name>CG_1.CPart_1</channel-partition-name>
                           </expected-attachment-point>
                        </expected-attachment-points>
                        <xpon-technology xmlns:bbf-xpon-types="urn:bbf:yang:bbf-xpon-types">bbf-xpon-types:gpon</xpon-technology>
                        <vomci-onu-management>
                           <vomci-function>vomci-vendor-1</vomci-function>
                           <onu-management-chain>
                             <nf-type>vomci-function</nf-type>
                             <nf-name>vomci-vendor-1</nf-name>
                           </onu-management-chain>
                           <onu-management-chain>
                             <nf-type>onu-management-proxy</nf-type>
                             <nf-name>proxy-1</nf-name>
                           </onu-management-chain>
                           <onu-management-chain>
                             <nf-type>olt</nf-type>
                             <nf-name>OLT1</nf-name>
                           </onu-management-chain>
                            <network-function-links>
                             <network-function-link>
                               <name>vOMCI-proxy</name>
                               <termination-point-a>
                                 <function-name>vomci-vendor-1</function-name>
                                 <local-endpoint-name>vOMCi-grpc-1</local-endpoint-name>
                               </termination-point-a>
                              <termination-point-b>
                                 <function-name>proxy-1</function-name>
                                  <local-endpoint-name>proxy-grpc-1</local-endpoint-name>
                               </termination-point-b>
                             </network-function-link>
                              <network-function-link>
                               <name>proxy-OLT</name>
                               <termination-point-a>
                                 <function-name>proxy-1</function-name>
                                 <local-endpoint-name>proxy-grpc-2</local-endpoint-name>
                               </termination-point-a>
                               <termination-point-b>
                                 <function-name>OLT1</function-name>
                                 <local-endpoint-name>olt-grpc-2</local-endpoint-name>
                               </termination-point-b>
                             </network-function-link>
                           </network-function-links>
                      </vomci-onu-management>
                     </onu-config-info>
                  </device-management>
               </device>
            </managed-devices>
         </network-manager>
      </config>
   </edit-config>
</rpc>

```

#### Registration for OLT notifications (onu-state-change notifications)

When an ONU\'s attachment state changes toward an OLT, the OLT transmits
onu-state-change notifications through its Northbound management system
to subscribed management entities including the BAA layer where
the vOLTMF uses these state change notifications to determine if the ONU
has been considered \"detected\" or \"undetected\".

The following is an example of an onu-state-change notification:

```
<notification xmlns="urn:ietf:params:xml:ns:netconf:notification:1.0">
   <eventTime>2019-07-25T05:53:36+00:00</eventTime>
   <bbf-xpon-onu-states:onu-state-change xmlns:bbf-xpon-onu-states="urn:bbf:yang:bbf-xpon-onu-states">
       <bbf-xpon-onu-states:detected-serial-number>ABCD12345678</bbf-xpon-onu-states:detected-serial-number>
       <bbf-xpon-onu-states:onu-id>1</bbf-xpon-onu-states:onu-id>
       <bbf-xpon-onu-states:channel-termination-ref>CT_1</bbf-xpon-onu-states:channel-termination-ref>
       <bbf-xpon-onu-states:onu-state>onu-present-and-unexpected</bbf-xpon-onu-states:onu-state>
       <bbf-xpon-onu-states:onu-state-last-change>2019-07-25T05:53:36+00:00</bbf-xpon-onu-states:onu-state-last-change>
   </bbf-xpon-onu-states:onu-state-change>
</notification>
```

In order to receive the onu-state-change notification, the BAA layer\'s
ONUNotificationListener registers with DeviceNotificationListenerService
for deviceNotifications using the ONUNotificationListener\'s
deviceNotificationReceived() callback method. Upon reception of the
notification, the ONUNotificationListener performs some validation on
the notification and forwards the notification onto the vOLTMF using the
VOLTManagementImpl\'s ONUNotificationProcess() method for further
processing by the vOLTMF.

**Info:** The ONUNotificationListener is one of the entities that registers to receive ONU state change notifications, other listeners of device notifications also receive these ONU state change notifications for their purposes such as forwarding the notification to the Access SDN M&C.

##### ONU state change notification mapping to detect and undetect events

The ONU detect and undetect events are mapped to one or more of the ONU
state change notifications received by the ONUNotificationListener.

The table below maps the ONU state change notifications to the ONU
DETECT or UNDETECT event:

|onu-state|Event|
| :--- | :--- |
|onu-present|detect|
|onu-present-and-unexpected|detect|
|onu-present-and-on-intended-channel-termination|detect|
|onu-present-and-in-wavelength-discovery|detect|
|onu-present-and-discovery-tune-failed|detect|
|onu-present-and-no-v-ani-known-and-o5-failed|detect|
|onu-present-and-no-v-ani-known-and-o5-failed-no-onu-id|detect|
|onu-present-and-no-v-ani-known-and-o5-failed-undefined|detect|
|onu-present-and-v-ani-known-and-o5-failed|detect|
|onu-present-and-v-ani-known-and-o5-failed-no-onu-id|detect|
|onu-present-and-v-ani-known-and-o5-failed-undefined|detect|
|onu-present-and-no-v-ani-known-and-in-o5|detect|
|onu-present-and-no-v-ani-known-and-unclaimed|detect|
|onu-present-and-v-ani-known-but-intended-ct-unknown|detect|
|onu-present-and-emergency-stopped|undetect|
|onu-not-present|undetect|
|onu-not-present-with-v-ani|undetect|
|onu-not-present-without-v-ani|undetect|

#### ONU detected online

Upon receiving an onu-state-change notification from the OLT, the
vOLTManagementFunction determines if the onu-state-change
notification translates into a DETECT event as described above. If the
onu-state-change notification fields such as serial-number and
channel-termination matches with a preconfigured ONU in the BAA layer,
the vOLTMF sends a set-onu-communication action request to the vOMCI
function and vOMCI Proxy. The action includes details of the onu-name,
attachment-point and north and south bound remote-endpoints.

Once the vOMCI function and vOMCI Proxy processes set-onu-communication
action, the vOMCI Proxy and vOMCI function sends the response to the
action. When the OK response is received from both vOMCI Function and
vOMCI Proxy that comprises the ONU\'s managment chain, vOLTMF updates
the connection status of the ONU to \"CONNECTED\". Addiitonally, vOLTMF
creates a new MediatedDeviceNectonfSession and begins the process of
obtaining necessary information about the ONU and then aligning the ONU
by sending GET and COPY-CONFIG requests to the targeted ONU via the
vOMCI function. The results of the alignment (i.e., COPY-CONFIG
response) is updated in the BAA layer\'s datastore.

Once the ONU detect sequence is successfully completed, the
onu-discovery-result notification is forwarded to the Access SDN M\_C
that includes information about ONU serial number, discovery status,
device-info and software information.

```
<notification xmlns="urn:ietf:params:xml:ns:netconf:notification:1.0">
  <eventTime>2021-07-02T14:20:08+00:00</eventTime>
  <onu-discovery-result xmlns="urn:bbf:yang:bbf-voltmf-entity">
    <onu-serial-number>ABCD12345678</onu-serial-number>
    <discovery-result>successful</discovery-result>
    <device-info/>
    <software-info>
      <software-images xmlns="urn:bbf:yang:obbaa:onu-management"/>
    </software-info>
  </onu-discovery-result>
</notification>
```

<p align="center">
 <img width="1000px" height="1000px" src="{{site.url}}/architecture/voltmf/onu_detect_notif.png">
</p>

#### ONU detected offline

Upon receiving of an onu-state-change notification from the OLT, the
vOLTManagementFunction determines if the onu-state-change notification
translates into an UNDETECT event as described above. When an ONU
UNDETECT event occurs, the vOLTMF retrieves the necessary ONU
identification information from the BAA layer\'s DeviceManager using the
onu-state-change\'s serial-number and sends a set-onu-communication
request to the vOMCI Proxy and vOMCI function instances that comprise
the ONU\'s management chain. The set-onu-communication action includes
information such as attachment-point, north and south bound
remote-endpoints and communication availability. Upon a successful
response to the action, an onu-discovery-result notification is
forwarded to Access SDN M\_C along with information about the ONU
including the ONU\'s current state.

The following diagram depicts the flow when ONU goes offline:

<p align="center">
 <img width="800px" height="800px" src="{{site.url}}/architecture/voltmf/onu_undetect_notif.png">
</p>


### ONU Deletion

ONUs in the BAA layer can be deleted regardless whether the ONU has been
pre-provisioned or was discovered by the OLT. In either case, the
deletion of the ONU device in the BAA layer, triggers the
MediatedDeviceEventListener\'s onuDeviceRemoved() method that results in
Delete-ONU Action request towards vOMCI Proxy and vOMCI function. If a
MediatedDeviceNetconfSession has been established then the Delete-ONU
request is transmitted and the session is closed after the response to the request is received.

<p align="center">
 <img width="800px" height="800px" src="{{site.url}}/architecture/voltmf/onu_delete.png">
</p>

### ONU Alarm handling via vOMCI

The following picture depicts the general workflow for processing ONU alarms received through the vOMCI function. The alarms are forwarded to the SDN M&C using ietf-alarm (RFC8632) style notifications.

<p align="center">
 <img width="800px" height="600px" src="{{site.url}}/architecture/voltmf/onu_alarm_handling_sequence.png">
</p>

Details about the implemented alarms and the processing made in the vOMCI function can be found in [vOMCI - ONU alarm Handling](onu_alarm/index.md#onu_alarm).

## vOLTMF to vOMCI Messages over MvOLTMF-vOMCI

In WT-451 Revision:27 messages conveyed on the
MvOLTMF-vOMCI interface are serialized as Google Protobufs (GPB) and
YANG modules are encoded in JSON. In order to support old and new
message format in this release, the following strategy pattern is
introduced in the vOLTMF design.

### MessageFormatter

<p align="center">
 <img width="700px" height="500px" src="{{site.url}}/architecture/voltmf/message_formatter.png">
</p>

VOLTManagementImpl decides the Messageformatter to be used and creates a
MediatedDeviceNetconfSession with the constructor argument
\"MessageFormatter\"(JSON/GPB). In the release, the JSONFormatter and
GPBFormatter is added, both of which implements the MessageFormatter
interface. The MediatedDeviceNetconfSession calles
MessageFormmater.getFormattedRequest() to get the formatted message
request. The JSONFormatter formats the message in JSON that was
supported in previous releases. The GPBFormatter formats the messages in
the GPB format. Both these classes implement the method
getFormattedRequest() and getResponseData() which returns the
corresponding formatted message and MediatedDeviceNetconfSession use
this message to communicate with the vOMCI function or vOMCI Proxy.

```
public interface MessageFormatter<T> {

    T getFormattedRequest(AbstractNetconfRequest request, String operationType, Device onuDevice,
                               AdapterManager adapterManager, ModelNodeDataStoreManager modelNodeDsm,
                               SchemaRegistry schemaRegistry, NetworkWideTag networkWideTag)
            throws NetconfMessageBuilderException, MessageFormatterException;

    ResponseData getResponseData(Object responseObject) throws MessageFormatterException;
}
```
### JSONFormatter

In previous releases, messages were encoded in JSON format along with
NetworkWideTags. However, the formatting of the messages were taken care
by MediatedDeviceNetconfSession itself. As part of this release, the
functionalities are decoupled when forming the JSON message and
abstracted it to the JSONFormatter. JSONFormatter implements the method
getFormattedRequest() and getResponseData() to return the JSON formatted
message that was supported in previous releases.

### GPBFormatter

The tr451\_vomci\_nbi\_message.proto is used to generate the corresponding Java classes. The GPBFormatter implements the method getFormattedRequest and getResponseData. Using the Java classes generated for the .proto file, it returns the formatted
message.

Note: For more information about converting protobufs to Java see: [Java
Generated Code  \|  Protocol Buffers  \|  Google
Developers](https://developers.google.com/protocol-buffers/docs/reference/java-generated)

In the new format the header of the message requires details such as
msg\_id, sender\_name, recepient\_name, object\_type and object\_name
that should be populated in the NetworkWideTag before sending to the
MessageFormatter. 

A new enum is introduced for identifying the Object type that shows the type of target for the message.

```
Public enum ObjectType {
    ONU, VOMCI_FUNCTION, VOMCI_PROXY, VOLTMF
  }
```
Based on the operationType argument passed to GPBFormatter in
getFormattedRequest(), the GPBFormatter populates the req\_type field in the message body.

|operationType|req_type|
| :--- | :--- |
|ONUConstants.ONU_GET_OPERATION|get_data|
|ONUConstants.ONU_COPY_OPERATION|replace_config|
|ONUConstants.ONU_EDIT_OPERATION|update_config|
|ONUConstants.CREATE_ONU|action|
|ONUConstants.DELETE_ONU/ ONUConstants.SET_ONU_COMMUNICATION|action|

On receiving the responses, the GPBFormatter validates following fields:

|Field name|Check|
| :--- | :--- |
|recipient_name|If it is not null, verify that it is "vOLTMF". If not log error and discard message|
|version||
|object_type|If it is not null, verify that the source is either "VOMCI_FUNCTION" or "VOMCI_PROXY" or "ONU". If not log error and discard message|
|object_name|If not null, verify that if the object_type is "ONU", the object name is present in the onu devices managed by BAA. Use this field value to populate onuName in ResponseData|

Based on the resp\_type GPBFormatter populates the operationType field
in ResponseData object it returns.

|resp_type|operationType|
| :--- | :--- |
|get_resp|ONUConstants.ONU_GET_OPERATION|
|replace_config_resp|ONUConstants.ONU_COPY_OPERATION|
|update_config_resp|ONUConstants.ONU_EDIT_OPERATION|
|action_resp|ONUConstants.CREATE_ONU|
|action_resp|ONUConstants.DELETE_ONU/ ONUConstants.SET_ONU_COMMUNICATION|

## Sample messages on the MvOLTMF-vOMCI (processed locally by the vOMCI instance)

Below are sample GPB formatted messages sent from vOLTMF to vOMCI
function and vOMCI Proxy:

Create-ONU request towards the vOMCI Function

```
Msg {
header {
  msg_id: "1"
  sender_name: "vOLTMF"
  recipient_name: "vomci-vendor-1"
  object_type: VOMCI_FUNCTION
  object_name: "vomci-vendor-1"
}
body {
  request {
    action {
      input_data: "{\"bbf-vomci-function:managed-onus\":{\"create-onu\":{\"name\":\"ont1\"}}}"
    }
  }
}
 }            
```

Create-ONU request towards the vOMCI Proxy

```
Msg {
header {
  msg_id: "2"
  sender_name: "vOLTMF"
  recipient_name: "proxy-1"
  object_type: VOMCI_PROXY
  object_name: "proxy-1"
}
body {
  request {
    action {
      input_data: "{\"bbf-vomci-proxy:managed-onus\":{\"create-onu\":{\"name\":\"ont1\"}}}"
    }
  }
}
 }  
```

Create-ONU response from the vOMCI Function

```
Msg{
header {
  msg_id: "1"
  sender_name: "vomci-vendor-1"
  recipient_name: "vOLTMF"
  object_type: VOMCI_FUNCTION
  object_name: "vomci-vendor-1"
}
body {
  response {
    action_resp {
      status_resp {
		 status_code=0
      }
    }
  }
}
}   
```

Create-ONU response from the vOMCI Proxy

```
Msg{ header {
  msg_id: "2"
  sender_name: "proxy-1"
  recipient_name: "vOLTMF"
  object_type: VOMCI_PROXY
  object_name: "proxy-1"
}
body {
  response {
    action_resp {
      status_resp {
		 status_code=0
      }
    }
  }
}
}
```

Set-ONU-Communication action towards the vOMCI Function

```
Msg{
header {
  msg_id: "3"
  sender_name: "vOLTMF"
  recipient_name: "vomci-vendor-1"
  object_type: VOMCI_FUNCTION
  object_name: "vomci-vendor-1"
}
body {
  request {
    action {
      input_data: "{\"bbf-vomci-function:managed-onus\":{\"managed-onu\":[{\"name\":\"ont1\",\"set-onu-communication\":{\"onu-attachment-point\":{\"olt-name\":\"OLT1,\"channel-termination-name\":\"CT_1\",\"onu-id\":1},\"voltmf-remote-endpoint-name\":\"vOLTMF_Kafka\",\"onu-communication-available\":true,\"olt-remote-endpoint-name\":\"proxy-grpc-1\"}}]}}"}
  }
}
```

Set-ONU-Communication action towards the vOMCI Proxy

```
Msg{
header {                       
  msg_id: "4"
  sender_name: "vOLTMF"
  recipient_name: "proxy-1"
  object_type: VOMCI_PROXY
  object_name: "proxy-1"
}
body {
  request {
    action {
      input_data: "{\"bbf-vomci-proxy:managed-onus\":{\"managed-onu\":[{\"name\":\"ont1\",\"set-onu-communication\":{\"onu-attachment-point\":{\"olt-name\":\"OLT1\", \"channel-termination-name\":\"CT_1\",\"onu-id\":1},\"onu-communication-available\":true,\"vomci-func-remote-endpoint-name\":\"vOMCi-grpc-1\",\"olt-remote-endpoint-name\":\"OLT1\"}}]}}"}
  }
}
}
```

Set-ONU-Communication response from the vOMCI Function

```
Msg {
header {
  msg_id: "3"
  sender_name: "vomci-vendor-1"
  recipient_name: "vOLTMF"
  object_type: VOMCI_FUNCTION
  object_name: "vomci-vendor-1"
}
body {
  response {
    action_resp {
      status_resp {
		 status_code=0
      }
    }
  }
}
}
```

Set-ONU-Communication response from the vOMCI Proxy

```
Msg {
header {
  msg_id: "4"
  sender_name: "proxy-1"
  recipient_name: "vOLTMF"
  object_type: VOMCI_PROXY
  object_name: "proxy-1"
}
body {
  response {
    action_resp {
      status_resp {
		 status_code=0
      }
    }
  }
}
}
```

Delete-ONU action towards the vOMCI Function

```
Msg {
header {
  msg_id: "5"
  sender_name: "vOLTMF"
  recipient_name: "vomci-vendor-1"
  object_type: VOMCI_FUNCTION
  object_name: "vomci-vendor-1"
}
body {
  request {
    action {
      input_data: "{\"bbf-vomci-function:managed-onus\":{\"managed-onu\":[{\"name\":\"ont1\",\"delete-onu\":{}}]}}"
    }
  }
}
}
```

Delete-ONU action towards the vOMCI Proxy

```
Msg {
header {
  msg_id: "6"
  sender_name: "vOLTMF"
  recipient_name: "proxy-1"
  object_type: VOMCI_PROXY
  object_name: "proxy-1"
}
body {
  request {
    action {
      input_data: "{\"bbf-vomci-proxy:managed-onus\":{\"managed-onu\":[{\"name\":\"ont1\",\"delete-onu\":{}}]}}"
    }
  }
}
}
```

Delete-ONU response from the vOMCI Function

```
Msg {
header {
  msg_id: "5"
  sender_name: "vomci-vendor-1"
  recipient_name: "vOLTMF"
  object_type: VOMCI_FUNCTION
  object_name: "vomci-vendor-1"
}
body {
  response {
    action_resp {
      status_resp {
		 status_code=0
      }
    }
  }
}
}
```

Delete-ONU response from the vOMCI Proxy

```
Msg {
header {
  msg_id: "6"
  sender_name: "proxy-1"
  recipient_name: "vOLTMF"
  object_type: VOMCI_PROXY
  object_name: "proxy-1"
}
body {
  response {
    action_resp {
      status_resp {
		 status_code=0
      }
    }
  }
}
}
```

Configure endpoints towards the vOMCI function

```
Msg {
 header {
  msg_id: \"2\"
  sender_name: \"vOLTMF\"
  recipient_name: \"vomci-vendor-1\"
  object_name: \"vomci-vendor-1\"
  object_type: VOMCI_FUNCTION
}
body {
  request {
    update_config {
      update_config_replica: {
         delta_config: {
"{
   \"bbf-vomci-function:vomci\": {
      \"remote-network-function\": {
         \"nf-client\": {
            \"enabled\": true,
            \"nf-initiate\": {
               \"remote-endpoints\": {
                  \"remote-endpoint\": [
                     {
                        \"name\": \"obbaa-vomci\",
                        \"nf-type\": \"bbf-network-function-types:voltmf-type\",
                        \"local-endpoint-name\": \"vOMCI-kfk-1\",
                        \"kafka-agent\": {
                           \"kafka-agent-parameters\": {
                              \"client-id\": \"client-id2\",
                              \"publication-parameters\": {
                                 \"topic\": [
                                    {
                                       \"name\": \"vomci1-response\",
                                       \"purpose\": \"VOMCI_RESPONSE\"
                                    },
                                    {
                                       \"name\": \"vomci1-notification\",
                                       \"purpose\": \"VOMCI_NOTIFICATION\"
                                    }
                                 ]
                              },
                              \"consumption-parameters\": {
                                 \"topic\": [
                                    {
                                       \"name\": \"vomci1-request\",
                                       \"purpose\": \"VOMCI_REQUEST\"
                                    }
                                 ]
                              }
                           }
                        },
                        \"access-point\": [
                           {
                              \"name\": \"obbaa-vomci\",
                              \"kafka-agent\": {
                                 \"kafka-agent-transport-parameters\": {
                                    \"remote-address\": \"kafka-host\"
                                 }
                              }
                           }
                        ]
                     }
                  ]
               }
            }
         },
         \"nf-server\": {
            \"enabled\": true,
            \"listen\": {
               \"listen-endpoint\": [
                  {
                     \"name\": \"vOMCI-grpc-1\",
                     \"grpc\": {
                        \"grpc-server-parameters\": {
                           \"local-endpoint-name\": \"vOMCI-grpc-1\",
                           \"local-address\": \"::\",
                           \"local-port\": 8100
                        }
                     }
                  }
               ]
            }
         }
      }
   }
}"
       }
     }
    }
  }
}

```

Configure endpoints towards the vOMCI Proxy

```
Msg {
 header {
  msg_id: \"2\"
  sender_name: \"vOLTMF\"
  recipient_name: \"proxy1\"
  object_name: \"proxy1\"
  object_type: VOMCI_PROXY
}
body {
  request {
    update_config {
      update_config_replica: {
         delta_config:  {
"{
  \"bbf-vomci-proxy:vomci\": {
    \"remote-network-function\": {
      \"nf-client\": {
        \"enabled\": true,
        \"nf-initiate\": {
          \"remote-endpoints\": {
            \"remote-endpoint\": [
              {
                \"name\": \"vOLTMF_Kafka_2\",
                \"nf-type\": \"bbf-network-function-types:voltmf-type\",
                \"local-endpoint-name\": \"proxy-kfk-1\",
                \"kafka-agent\": {
                  \"kafka-agent-parameters\": {
                    \"client-id\": \"client-id3\",
                    \"publication-parameters\": {
                      \"topic\": [
                        {
                          \"name\": \"vomci-proxy-request\",
                          \"purpose\": \"VOMCI_RESPONSE\"
                        },
                        {
                          \"name\": \"vomci-proxy-notification\",
                          \"purpose\": \"VOMCI_NOTIFICATION\"
                        }
                      ]
                    },
                    \"consumption-parameters\": {
                      \"topic\": [
                        {
                          \"name\": \"vomci-proxy-request\",
                          \"purpose\": \"VOMCI_REQUEST\"
                        }
                      ]
                    }
                  }
                },
                \"access-point\": [
                  {
                    \"name\": \"vOLTMF_Kafka_2\",
                    \"kafka-agent\": {
                      \"kafka-agent-transport-parameters\": {
                        \"remote-address\": \"kafka-host\",
                        \"remote-port\": 9092
                      }
                    }
                  }
                ]
              },
              {
                \"name\": \"vOMCI-grpc-1\",
                \"nf-type\": \"bbf-network-function-types:vomci-function-type\",
                \"local-endpoint-name\": \"proxy-grpc-2\",
                \"access-point\": [
                  {
                    \"name\": \"vOMCI-grpc-1\",
                    \"grpc\": {
                      \"grpc-transport-parameters\": {
                        \"remote-address\": \"vomci-host\",
                        \"remote-port\": 8100
                      }
                    }
                  }
                ]
              }
            ]
          }
        }
      },
      \"nf-server\": {
        \"enabled\": true,
        \"listen\": {
          \"listen-endpoint\": [
            {
              \"name\": \"proxy-grpc-2\",
              \"grpc\": {
                \"grpc-server-parameters\": {
                  \"local-endpoint-name\": \"proxy-grpc-2\",
                  \"local-address\": \"::\",
                  \"local-port\": 8433
                }
              }
            }
          ]
        }
      }
    }
  }
}"
        }
      }
    }
  }
}

```

## Sample notifications on the MvOLTMF-vOMCI (sent by the vOMCI function)

ONU Alignment Result (aligned) Notification

```
Msg {
 header {
  msg_id: "1"
  sender_name: "vomci-vendor-1"
  recipient_name: "vOLTMF"
  object_type: ONU
  object_name: "ont1"
}
body {
  notification {
    data: "{\"bbf-vomci-function:onu-alignment-status\":{\"event-time\": \"2021-06-01T15:53:36+00:00\",\"onu-name\": \"ont1\",\"alignment-status\": \"aligned\"}}"
  }
}
}
```

ONU Alignment Result (mis-aligned) Notification

```
Msg {
 header {
  msg_id: "1"
  sender_name: "vomci-vendor-1"
  recipient_name: "vOLTMF"
  object_type: ONU
  object_name: "ont1"
}
body {
  notification {     
    data: "{\"bbf-vomci-function:onu-alignment-status\":{\"event-time\": \"2021-06-01T15:53:36+00:00\",\"onu-name\": \"ont1\",\"alignment-status\": \"unaligned"}}"
  }
}
}
```

ONU Reported Alarms via the vOMCI function

```
Msg {
 header {
  msg_id: "1"
  sender_name: "vomci-vendor-1"
  recipient_name: "vOLTMF"
  object_type: ONU
  object_name: "ont1"
}
body {
  notification {   
    event_timestamp: "2022-01-09T13:53:36+00:00"
    data: "{ "ietf-alarms:alarm-notification": {
    "resource": "/ietf-interfaces:interfaces/interface[name='eth0']",
    "alarm-type-id": "bbf-alarm-types:bbf-alarm-type-id",
    "alarm-type-qualifier": "",
    "time": "2022-01-09T13:53:36+00:00",
    "perceived-severity": "major",
    "alarm-text": "example alarm"
     }
	}"
  }
}
}
```

ONU Cleared Alarms via the vOMCI function

```
Msg {
 header {
  msg_id: "1"
  sender_name: "vomci-vendor-1"
  recipient_name: "vOLTMF"
  object_type: ONU
  object_name: "ont1"
}
body {
  notification {   
    event_timestamp: "2022-01-09T13:53:36+00:00"
    data: "{ "ietf-alarms:alarm-notification": {
    "resource": "/ietf-interfaces:interfaces/interface[name='eth0']",
    "alarm-type-id": "bbf-alarm-types:bbf-alarm-type-id",
    "alarm-type-qualifier": "",
    "time": "2022-01-09T13:54:36+00:00",
    "perceived-severity": "cleared",
    "alarm-text": "example alarm"
         }
	}"
  }
}
}
```

## Sample messages on the MvOLTMF-vOMCI (translated and forwarded to the ONU)

### Replace-Config
When vOLTMF receives onu-alignment result notification from the
vOMCI function with alignment-status as \"misaligned\", the vOLTMF tries to align the ONU by sending replace-config request towards the  vOMCI function. For an ONU created with standard onu adapter 1.0 below is the sample replace-config request and response:

replace-config request towards the vOMCI function:

```
Msg {
 header {
  msg_id: "2"
  sender_name: "vOLTMF"
  recipient_name: "vomci-vendor-1"
  object_type: ONU
  object_name: "ont1"
}
body {
  request {
    replace_config {
      config_inst: "{\"bbf-qos-filters:filters\":{},\"ietf-ipfix-psamp:ipfix\":{},\"ietf-hardware:hardware\":{},\"bbf-qos-classifiers:classifiers\":{},\"ietf-pseudowires:pseudowires\":{},\"bbf-ghn:ghn\":{},\"bbf-qos-policies:qos-policy-profiles\":{},\"bbf-vdsl:vdsl\":{},\"bbf-xpongemtcont:xpongemtcont\":{},\"ietf-system:system\":{},\"bbf-qos-policer-envelope-profiles:envelope-profiles\":{},\"bbf-ghs:ghs\":{},\"ietf-interfaces:interfaces\":{},\"bbf-qos-policies:policies\":{},\"bbf-qos-traffic-mngt:tm-profiles\":{},\"bbf-qos-policing:policing-profiles\":{},\"bbf-selt:selt\":{},\"bbf-l2-dhcpv4-relay:l2-dhcpv4-relay-profiles\":{},\"ieee802-dot1x:nid-group\":{},\"ietf-alarms:alarms\":{},\"ietf-netconf-acm:nacm\":{},\"bbf-hardware-rpf-dpu:rpf\":{},\"bbf-pppoe-intermediate-agent:pppoe-profiles\":{},\"bbf-fast:fast\":{},\"bbf-mgmd:multicast\":{},\"bbf-melt:melt\":{},\"bbf-subscriber-profiles:subscriber-profiles\":{},\"bbf-ldra:dhcpv6-ldra-profiles\":{},\"bbf-l2-forwarding:forwarding\":{}}"
    }
  }
}
}
```

replace-config response from a vOMCI function:

```
Msg {
 header {
  msg_id: "2"
  sender_name: "vomci-vendor-1"
  recipient_name: "vOLTMF"
  object_type: ONU
  object_name: "ont1"
}
body {
  response {
    replace_config_resp {
      status_resp{
		status_code=0
	  }
    }
  }
}
}
```

### Get-Data

Information about an ONU can be retrieved by sending a get-data request towards the vOMCI function. This request contains a list of filters which specify which parts of the ONU data model need to be retrieved. For an ONU created with the standard onu adapter 1.0 below is the sample get-data request and response:

get-data request towards the vOMCI function:

```
Msg {
  header {
    msg_id: "3"
    sender_name: "vOLTMF"
    recipient_name: "vomci-vendor-1"
    object_name: "ont1"
  }
  body {
    request {
      get_data {
        filter: "{
          \"ietf-hardware:hardware\": {
            \"component\": [
              {
                \"name\": \"ont1\",
                \"bbf-software-management:software\": {}
              }
            ]
          }
        }"
      }
    }
  }
}
```

get-data response from the vOMCI function:

```
Msg {
  header {
    msg_id: "3"
    sender_name: "vomci-vendor-1"
    recipient_name: "vOLTMF"
    object_type: ONU
    object_name: "ont1"
  }
  body {
    response {
      get_resp {
        data: "{
          \"ietf-hardware:hardware\": {
            \"component\": [
              {
                 \"name\": \"ont1\",
                  \"bbf-software-management:software\": {
                  \"software\": [
                    {
                      \"name\": \"model1-software\",
                      \"revisions\": {
                        \"revision\": [
                          {
                            \"id\": 1,
                            \"alias": \"model1-software-rev1\",
                            \"state": \"in-use\",
                            \"version\": \"1.0.0\",
                            \"product-code\": \"pcode\",
                            \"hash\": \"123456789\",
                            \"is-valid": true,
                            \"is-active": true,
                            \"is-committed": true
                          },
                          {
                            \"id\": 2,
                            \"alias\": \"model1-software-rev2\",
                            \"state": \"available\",
                            \"version\": \"2.0.0\",
                            \"product-code\": \"pcode\",
                            \"hash\": \"134323233\",
                            \"is-valid\": true,
                            \"is-active\": false,
                            \"is-committed\": false
                          }
                        ]
                      }
                    }
                  ]
                }
              }
            ]
          }
        }"
      }
    }
  }
}
```


[<--Architecture](../index.md#architecture)
