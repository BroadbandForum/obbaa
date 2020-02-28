
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

While most northbound interfaces used by OB-BAA are standardized, several interfaces (e.g., Data Lake) are used that haven't been standardized or are in the process of standardization.

<p align="center">
 <img width="600px" height="400px" src="{{site.url}}/architecture/system_interfaces.png">
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
 <img width="1000px" height="700px" src="{{site.url}}/architecture/system_architecture.png">
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

OB-BAA implements a NETCONF Connection Manager (NCM) in this release. If
required additional connection managers for specific protocols will be
implemented (e.g., SNMP connection manager). The NETCONF Connection
Manager is responsible for establishing, maintaining and tearing down
NETCONF sessions with the managed devices. It also maintains the
connection state of the device.

#### NETCONF Stack

The NETCONF Stack component Implements the NETCONF Client and provides
an API to the NETCONF Connection Manager for interactions with managed
device.

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

## PM Service
OB-BAA supports IPFIX (RFC 7011) based PM Collection from AN. PM data collection function in BAA is realized as a dedicated micro-service.

It has two architectural components:
-	PM Collector module
-	DataHandler module

### PM Collector
The PM Collector is responsible for collecting performance monitoring data (IPFIX messages) from managed pANs, The PM Collector will decode and provide formatted data to data handlers for further processing and storage of data. The IPFIX PM Data Collector (IPFIX Collector) is based on IETF\'s RFC 7011 IPFIX protocol specification and mechanisms as IPFIX is the method defined by the BBF\'s TR-383 specification.

IPFIX collector decodes IPFIX messages into the requisite Information Elements (IE) and their associated values. This is then forwarded to Data Handler(s) (IpfixDataHandler) that subscribes to IPFIX Collector.

[For more information on the PM Collector](pm_collector/index.md#pm_collection)

### PM DataHandler(s)
PM DataHandlers are karaf modules plugged into the PM micro-service and subscribes to the PM Collector for the counter stream. Several PM DataHandlers can be implemented depending on the post processing needs. The OB-BAA reference implementation comes with two reference Data Handlers.

#### Persistence Manager Data Handler
Persistence Manager is a reference PM Datahandler which subscribes to IPFIX collection stream and updates them into a common data lake. OB-BAA uses InfluxDB as its common data lake. The Persistence Manager does reformatting of the incoming counter stream into tags & counters before updating them into the persistence store. Once in the persistence store, counter information can be queried from the data store by management and control elements using the interface provided by InfluxDB.

#### Logger Data Handler
The Logger is another reference PM DataHandler which subscribes to IPFIX collection stream and logs it into the BAA application log.

## Data Lake
OB-BAA bundles InfluxDB as its common data lake for storing PM Counters. InfluxDB is a time series database, hence ideal for maintaining PM counters which is tracked based on time. InfluxDB provides a HTTP based API interface for querying data which can be used by SDN M&C and other management entities. It also provides a graphical user interface for data visualization purpose.

[<--Overview](../overview/index.md#overview)

[Installing OB-BAA -->](../installing/index.md#installing)
