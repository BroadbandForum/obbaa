
<a id="architecture" />

Architecture
============

The OB-BAA software architecture is based on the logical system
architecture where standardized northbound interfaces are defined for management
(M<sub>inf\_xxx</sub>) and control (M<sub>fc\_xxx</sub>). The standardized management interface is further
broken down in the BAA layer administration (M<sub>inf\_net-map</sub>,
M<sub>inf\_eq-inv</sub>) and the device management interfaces (M<sub>inf\_Lxxx</sub>). The
representation of the AN is exposed through each of the interfaces using
access control to restrict access to parts of the AN.

While most northbound interfaces used by OB-BAA are standardized, several interfaces (e.g., M<sub>fc_L2-3</sub>) are used that haven't been standardized or are in the process of standardization.

<p align="center">
 <img width="800px" height="500px" src="{{site.url}}/architecture/system_interfaces.png">
</p>

ANs interact with the BAA layer through the adapters of the SBI. These
adapters can be specific a vendor\'s implementation or can be generic in
the sense that vendors that have the same SBI can share a default
adapter for the type of device.

High-level Software Architecture Components
-------------------------------------------

A high-level software architecture is depicted below that implements the
logical system architecture described in the previous section.

<p align="center">
 <img width="1100px" height="1000px" src="{{site.url}}/architecture/system_architecture.png">
</p>

The roles and responsibilities of each of these components are as
follows.

### Southbound Adaptation Interface (SAI) & Southbound Interface (SBI)

This layer addresses the functional requirements documented under
Section 7.2 & 7.3 in OB-BAA system description by providing a java based
interface to the BAA Core.

The SAI and SBI hosts the interfacing/adaptation modules that allow for
communication between the BAA layer and managed devices connected
through the SBI including the protocol specifics required for device
communication between the BAA layer and the managed device.

Broadly the SAI and SBI host two components:

1.  SBI Connectors

2.  Supporting Protocol Stacks

#### SBI Connectors

SBI Connectors act as the boundary object for interactions of other
layers with SAI & SBI layer. SBI Connectors implements a java interface
that exposed to the BAA Core. The BAA Core uses this interface to
communicate with the device.

This interface allows the BAA Core to:

-   Execute requests on the device (e.g., NETCONF edit-config)

-   Receive notifications via callbacks from the device (e.g., NETCONF
    notification)

SBI connectors uses the Device Admin store for retrieving device
connectivity details and also regularly updates the connectivity status.
An instance of SBI Connector is required for each supported protocol for
communication with Device.

The NETCONF Connection Manager is responsible for establishing, maintaining and tearing down NETCONF sessions with the managed devices. It also maintains the
connection state of the device.

#### NETCONF Stack

The NETCONF Stack component implements the NETCONF Client and provides
APIs to the NETCONF Connection Manager for interactions with managed
devices.

### SNMP Stack

The SNMP stack component implements the SNMP client and provides API to
device adapters for interactions with managed device.

### vOLTMF - vOLT Management Function

The vOLT Management Function (vOLTMF) is responsible for:

-   Receiving notifications from the OLT regarding the detection state
    of the ONU.

-   Notifying the vOMCI function whenever an ONU is detected or
    undetected.

-   Receiving management and control requests from the Access SDN M&C.

-   After detecting on-line ONUs, generating and sending requests for
    OLT configuration via the interface with OLT, and ONU requests that
    use the OLT's OMCI channel via the interface with the vOMCI proxy.

-   Receiving connectivity information between the OLT and vOMCI
    function and proxy from the Access SDN M&C and sending the
    connectivity information to the vOMCI function and/or vOMCI proxy.

-   Receiving notifications including events and alarms from the OLT,
    vOMCI proxy and vOMCI function and sending notifications to the
    Access SDN M&C.

[For more information about the vOLT Management Function](voltmf/index.md#voltmf)

### ONU Authenticator - ONU Authentication Function

The ability for the operator to identify an ONU and authenticate that the ONU belongs to a subscriber of one or more services from the operator has been traditionally performed as an function embedded within the physical OLT. In this regard the OLT acted as the policy enforcement point (PEP) and policy decision point (PDP). The ONU Authentication function allows the BAA layer to act as a PDP entity where the BAA layer itself is able to perform the identification and authentication of an ONU based on a set of policy rules or it can work in concert with the OLT and/or other SDN management entities to collectively identify and authenticate the ONU.

Several scenarios are supported by the BAA layer:
-	vOMCI (Separated model) and ONU authentication in the BAA layer
-	vOMCI (Separated model) and ONU authentication in the OLT (for PLOAM credentials) and the BAA layer (for LOID & OMCI Mutual Authentication protocol)
-	eOMCI (Combined model) and ONU authentication in the OLT (BAA layer not to be engaged ! )

[For more information about the ONU Authentication Function](onu_auth/index.md#onu_auth)

### NAI & BAA Core

BAA-Core addresses the functional requirements identified under Section
7.1 & 7.4 in the OB-BAA system description. These components are
responsible for implementing the functionality needed by the various BAA
deployment modes (i.e., BAA Actuator, UAM)

Primary blocks in this component include the following:

1.  Aggregator

2.  Network Topology and Equipment Inventory (NTEI)

#### Aggregator

The Aggregator component is the interface point between the BAA Core and
NBI Adapters. The interface is implemented using a java interface that
is exposed over NAI layer.

The Aggregator implements the requirements defined in Section 7.4 of
OB-BAA system description.

The functionality provided by the Aggregator component includes the
ability to:

-   Redirect requests coming from NBI to appropriate management
    component with-in the BAA Core

-   Forward notifications and events coming from lower layers to one or
    more SDN Managers and Controllers connected via the NBI Adapter. In particular, more information on Alarm filtering is available [here](alarm/index.md#alarm_filtering)

[For more information on the Aggregator Component](aggregator/index.md#aggregator)

#### Network Topology and Equipment Inventory (NTEI)

The NTEI component is responsible for maintaining the Access Network Map
(ANM) for managed devices and implementing the device administration &
management functions necessary to operate the BAA layer.

In addition the NTEI components provides the persistent management agent
(PMA) functionality that acts as the system\'s source of truth for the
managed devices even when the devices are not currently connected to the
network.

##### Device Manager

The Device Manager component controls the lifecycle and administration
of managed devices. The component is responsible for the instantiation
and maintenance of managed device within the BAA layer including any
information necessary to establish and maintain communication with the
managed device. OB-BAA has defined a YANG module that includes necessary
information required for the discovery of the managed device and to
maintain connectivity with the device.

The Aggregator component directs device administration requests from the
NBI towards the Device Manager component where the Device Manager
persists the information in the Device Admin Store.

##### PMA Registry

PMA Registry component represents managed devices as physical Access
Node (pAN) instances using the BAA layer\'s data models defined for that
type of device.

Following are the major functions of the PMA Registry:

-   Maintain Device Config Store for incoming requests

-   Provide offline and online persistency for managed devices. If a
    device goes offline, when the device connects back the pending
    instructions will be played back into the device.

-   Maintains a mapping table with Device Adapter used by the managed
    device

-   Interacts with SBI Connectors

Upon receiving a request for a managed device, the PMA Registry
component retrieves details about the managed device from the Device
Admin store and looks up the corresponding Device Adapter. The PMA
Registry component then sends the request to the Device Adapter for
processing.

On receiving notifications and events from the SBI Connector component,
the PMA Registry component passes them to the right Device Adapter for
further normalization to pAN YANG data model. The PMA Registry component then
propagates the normalized notification or event to layers above it. When
deployed as a BAA Actuator, the notification and events will be passed
to the Aggregator component.

Further information on how Alarm notifications are reported is available [here](alarm/index.md#alarm_forwarding).

###### Interaction with SBI Connectors

For each managed device, the PMA Registry component maintains a SBI
Connector reference for the Device Adapter that is used by the managed
device and subscribes to Connection Manager\'s call back interface for
receiving notifications and events from devices.

#### Device Adapter

The Device Adapter component performs translation of device specific data models into the data models identified to the BAA layer\'s SAI component.

-	The Device Adapter component receives requests from PMA registry which it translates to device specific request and uses supported protocol specific Connection Manager to deploy the instruction into the managed device.

- The Device Adapter component also normalizes the device specific notifications and events to the data model supported by the BAA layer\'s SAI component for that type of device and then forwards the notification and event to the PAM Registry component.

[For more information on the Device Adapter](device_adapter/index.md#device_adapter)

### NBI Adapter

The NBI Adapter component addresses the functional requirements
documented under Section 7.5 in OB-BAA System description which adapts
the YANG data model exposed by the NAI into a protocol specific
interface (e.g. NETCONF) which interacts with SDN Manager and
Controllers as well as the BAA Core.

NBI Adapter component uses the NAI java interface exposed by Aggregator
component to:

-   Relay requests originating from the North

-   Registers to callback for notification and events from the
    Aggregator component; forwarding them to the respective SDN Manager
    and Controllers

### Infrastructure

Infrastructure component contains the supporting utilities needed by the
BAA layer. These include:

#### Logging

Utility used by components to create & maintain logs.

#### User Management

Functions for create & administer users.

#### NETCONF Server Framework

Provides tools for NETCONF Client & Server implementation. This includes
utilities like YANG module handling, security etc.

#### Debugging

Provides debugging utilities for application troubleshooting.

## BAA Services
The BAA layer has several services that provide needed functionality to
manage and control Access Nodes (AN). In this release the following
services are provided:

-   PM Service

-   Data Lake

-   vOMCI Function

-   vOMCI Proxy

-   Control Relay Service

### PM Service
OB-BAA supports IPFIX (RFC 7011) based PM Collection from AN. PM data collection function in BAA is realized as a dedicated micro-service.

It has two architectural components:

-	PM Collector module

-	DataHandler module

#### PM Collector
The PM Collector is responsible for collecting performance monitoring data (IPFIX messages) from managed pANs, The PM Collector will decode and provide formatted data to data handlers for further processing and storage of data. The IPFIX PM Data Collector (IPFIX Collector) is based on IETF\'s RFC 7011 IPFIX protocol specification and mechanisms as IPFIX is the method defined by the BBF\'s TR-383 specification.

IPFIX collector decodes IPFIX messages into the requisite Information Elements (IE) and their associated values. This is then forwarded to Data Handler(s) (IpfixDataHandler) that subscribes to IPFIX Collector.

[For more information on the PM Collector](pm_collector/index.md#pm_collection)

#### PM DataHandler(s)
PM DataHandlers are karaf modules plugged into the PM micro-service and subscribes to the PM Collector for the counter stream. Several PM DataHandlers can be implemented depending on the post processing needs. The OB-BAA reference implementation comes with two reference Data Handlers.

##### Persistence Manager Data Handler
Persistence Manager is a reference PM Datahandler which subscribes to IPFIX collection stream and updates them into a common data lake. OB-BAA uses InfluxDB as its common data lake. The Persistence Manager does reformatting of the incoming counter stream into tags & counters before updating them into the persistence store. Once in the persistence store, counter information can be queried from the data store by management and control elements using the interface provided by InfluxDB.

##### Logger Data Handler
The Logger is another reference PM DataHandler which subscribes to IPFIX collection stream and logs it into the BAA application log.

### Data Lake
OB-BAA bundles InfluxDB as its common data lake for storing PM Counters. InfluxDB is a time series database, hence ideal for maintaining PM counters which is tracked based on time. InfluxDB provides a HTTP based API interface for querying data which can be used by SDN M&C and other management entities. It also provides a graphical user interface for data visualization purpose.

### vOMCI Function

The vOMCI function is responsible for:

-   Receiving service configurations from the vOLT Management function

-   Translating the received configurations into ITU G.988 OMCI
    management entities (ME) and formatting them into OMCI messages

-   Encapsulating and sending above formatted OMCI messages through the
    OLT to the attached ONU

-   Translating the received ONU\'s operational data from the OLT and
    sending it to the vOLT Management function

[For more information on the vOMCI Function](vomcipf/index.md#vomcipf)

### vOMCI Proxy

The vOMCI Proxy works as an aggregation and interception point that
avoids the need for an OLT to directly connect to individual vOMCI
Functions and can act as a control/interception point between the OLT
and vOMCI function that is useful for troubleshooting and other
management purposes (e.g., security). As the aggregration/interception
point, the vOMCI Proxy is responsible for maintaining the associations
between OLTs and the corresponding vOMCI functions.

[For more information on the vOMCI Proxy](vomcipf/index.md#vomcipf)

### Control Relay Service

The Control Relay Service along with the AN Control and Control Protocol
Adapters provides the capability to relay packets from an AN to an
application endpoint in the  SDN Management and Control functional
layer.

The determination of which packets are relayed between the access node
and SDN M&C application endpoint is configurable through the M<sub>fc-conf</sub> reference point.

[For more information on the Control Relay Service](control_relay/index.md#control_relay)

### Service Discovery

The Service discovery is used to retrieve VNF details from Container management tool. In this release Kubernetes is the supported container orchestrator. This service allows OBBAA user to query the list of VNFs, their status & details. The Module uses the orchestrator API (for Kubernetes its REST API) to retrieve the requested details.

[For more information on Discovering micro-services](/using/micro_discovery/index.md#micro_discovery)


[<--Overview](../overview/index.md#overview)

[Installing OB-BAA -->](../installing/index.md#installing)
