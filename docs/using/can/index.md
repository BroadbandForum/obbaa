
<a id="can" />

Examples for Configuring Access Nodes
=================

The OB-BAA distribution includes standard device adapters (SDA) for OLTs
and DPUs along with a set of examples on how to configure those devices
using a NETCONF client.

The examples described in this section of the document can be found in
the *resources/examples/dpu-config* and *resources/examples/olt-config* directories
of the OB-BAA source code distribution.

The BAA layers NAI uses standard\'s based YANG models for configuring
the Access Nodes.

For configuration of OLTs and DPUs the following YANG modules are
important:

-   TR-355 \- YANG Modules for FTTdp Management
-   TR-383 \- Common YANG Modules for Access Network
-   TR-385 \- ITU-T PON YANG Modules

DPU Configuration
=================

The DPU configuration examples are located
in *resources/examples/dpu-config* and consist on the following steps:

1.  Create a DPU using the DPU SDA

2.  Add interfaces

3.  Configure N:1 services

4.  Configure 1:1 services

5.  Configure and apply G.fast profiles

The following figure summarizes the VLAN operations configured in step 3
and 4:

<p align="center">
 <img width="900px" height="600px" src="{{site.url}}/using/can/dpu.png">
</p>

Legend:

-   sub itf \- TR-383 sub-interface

-   fwd \- TR-383 forwarder

	**Info:** the forwarder ports were omitted since they have a 1:1 relationship to the sub-interface

-   ptm \- Packet Transfer Mode Interface

-   dsl \- DSL interface

OLT Configuration
=================

The OLT configuration examples are located
in *resources/examples/olt-config*.

This section of the documents provides a examples of how to configure
the OLT using either:

-   1:1 Services: Each ONU LAN sub-interface has a corresponding OLT WAN
    sub-interface

-   N:1 Services: Multiple ONU LAN sub-interfaces use the same OLT WAN
    sub-interface

Configure OLT for 1:1 Services
------------------------------

The first OLT configuration example consists on the following steps:

1.  Create an OLT using the OLT SDA

2.  Add the OLT and ONU interfaces. This includes the vANI and OLT-vENET
    interfaces. Please refer to TR-385 for additional information.

3.  Populate the TR-385 link table

4.  Create TR-383 QoS Classifiers

5.  Create TR-383 sub-interfaces on the ONT ports

6.  Create GEM ports and T-CONTs

7.  Configure a 1:1 services

The following figure summarizes the operations configured in step 5,6
and 7:

<p align="center">
 <img width="900px" height="600px" src="{{site.url}}/using/can/olt_1_1.png">
</p>

Legend:

-   sub itf \- TR-383 sub-interface

-   fwd \- TR-383 Forwarder. Note: the forwarder ports were omitted since
    they have a 1:1 relationship to the sub-interface

-   eth \- Ethernet Interface

Configure OLT for N:1 services
------------------------------

The second OLT configuration example consists on the following steps:

1.  Create an OLT using the standard OLT adapter

2.  Add the OLT and ONU interfaces. This includes the vANI and OLT-vENET
    interfaces. Please refer to TR-385 for additional information.

3.  Populate the TR-385 link table.

4.  Create TR-383 QoS Classifiers

5.  Create TR-383 sub-interfaces on the ONT ports

6.  Create GEM ports and T-CONTs

7.  Configure a N:1 service

The following figure summarizes the operations configured in step 5,6
and 7:

<p align="center">
 <img width="900px" height="600px" src="{{site.url}}/using/can/olt_n_1.png">
</p>

Legend:

-   sub itf \- TR-383 sub-interface

-   fwd \- TR-383 Forwarder

	**Info:** the forwarder ports were omitted since they have a 1:1 relationship to the sub-interface

-   eth \- Ethernet interface

[<--Using OB-BAA](../index.md#using)
