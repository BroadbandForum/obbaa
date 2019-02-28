
<a id="architecture" />

Architecture
============

The OB-BAA software architecture is based on the logical system
architecture where northbound interfaces are defined for management
(M<sub>inf\_xxx</sub>), control (M<sub>fc\_xxx</sub>). The management interface is further
broken down in the BAA layer administration (M<sub>inf\_net-map</sub>,
M<sub>inf\_eq-inv</sub>) and the device management interfaces (M<sub>inf\_Lxxx</sub>). The
representation of the AN is exposed through each of the interfaces using
access control to restrict access to parts of the AN.

<p align="center">
 <img width="600px" height="400px" src="{{site.url}}/architecture/system_interfaces.png">
</p>

ANs interact with the BAA layer through the adapters of the SBI. These
adapters can be specific a vendor\'s implementation or can be generic in
the sense that vendors that have the same SBI can share a default
adapter for the type of device.

**Warning:** In this release all devices will share the same DPU adapter. In the
next release, vendor specific adapters are implemented.

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
implemented (e.g. SNMP connection manager). The NETCONF Connection
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
deployment modes (i.e, BAA Actuator, UAM)

Primary blocks in this component include the following:

1.  Aggregator

2.  Network Topology and Equipment Inventory (NTEI)

3.  L1 Engine

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
    more SDN Managers and Controllers connected via the NBI Adapter
    
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

##### vAN Registry

The BAA layer represents the managed device as a pAN where the pAN
provides a common data model for each type of device. Included within
the pAN data model is the data model that represents the pAN as a vAN.
When deployed as a BAA Actuator, the BAA Layer simply uses the same vAN
data model across all types of devices, requiring the Device Adapters to
provide the adaptation of the device specific data modules into the pAN
and its vAN.

When deployed as a UAM, the vAN Registry component is used to represent
the vAN in more complex implementations where the vAN data model is
different than the data model that is represented in the NAI. In this
scenario the vAN instance that is exposed via the NAI is different but
correlated with the pAN instance exposed via the SAI. As such vAN
Registry component is responsible for the management of vAN instances.

When deployed as a UAM, the Aggregator component directs vAN requests
from the NBI towards the vAN Registry component. Likewise, vAN
notifications and events are directed by the Device Adapters to this
component.

On receiving requests from Aggregator component, the vAN Registry
component does the following:

1.  Validates the semantics of the request

2.  Persists information in the AN Config Store

3.  Redirects the request to \"Engine\" for further processing

On receiving abstracted notifications from lower layers, vAN Registry
does the following:

1.  Updates the AN Config Store (if required)

2.  Forwards to the Aggregator Component

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
to the Aggregator component.When deployed as a UAM the notification and
events are directed to the L1 Engine component.

###### Interaction with SBI Connectors

For each managed device, the PMA Registry component maintains a SBI
Connector reference for the Device Adapter that is used by the managed
device and subscribes to Connection Manager\'s call back interface for
receiving notifications and events from devices.

#### Device Adapter Framework

The Device Adapter Framework performs translation of device specific
data models into the data models identified to the BAA layer\'s SAI
component.

-   The Device Adapter Framework receives requests from PMA registry
    which it translates to device specific request and uses a
    protocol specific Connection Manager to deploy the request into
    the managed device.

-   The Device Adapter Framework also normalizes the device specific
    notifications and events to the data model supported by the BAA
    layer\'s SAI component for that type of device and then forwards the
    notification and event to the PAM Registry component

[For more information on the Device Adapter Framework](device_adapter/index.md#device_adapter)

#### L1 Engine

When deployed as an UAM, the L1 Engine component performs algorithms and
profiling to maintain an translate between pAN and vAN instances.

On receiving requests from the vAN Registry component, the L1 Engine
component:

-   Generates the equivalent pAN request by applying the L1 profile
    corresponding to the provisioning use-case

-   Passes the pAN request to PMA Registry component

On receiving notification and events from the PMA Registry component,
the L1 Engine component:

-   Translates the pAN instance into the vAN instance using the
    respective data models and translation logic.

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
    Aggregator component; forwarding them to the respective SND Manager
    and Controllers.

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

Provides debugging utilities for application trouble shooting.

[<--Overview](../overview/index.md#overview)

[Installing OB-BAA -->](../installing/index.md#installing)