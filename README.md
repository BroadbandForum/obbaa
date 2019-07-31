# OB-BAA R2 Release Notes (31 July 2019)

<a name="rel_2_1_0"></a>

Release 2.1.0 Highlights:
=========================

This release provides additional enhancements for:

-   Vendor Device Adapters (VDA): Provides the capability for VDAs to include a default configuration to be used for initial device configurations.

-	Included the capability to have multiple versions of Vendor and Standard device adapters for a type of Access Node and/or vendor model/version.

-   Examples related to the usage of the [NETCONF stack](https://github.com/BroadbandForum/obbaa-netconf-stack) used in OB-BAA

## Restrictions:
Same as release 2.0.0 but a workaround for restriction 1 that provided with the OLT Standard Device Adapter.

<a name="rel_2_0_0"></a>

Release 2.0.0 Highlights:
=========================

This release enhances the existing OB-BAA software by added now
functionality that:

-   provides the capability for vendors of access nodes that use
    protocols other than NETCONF/YANG to be used in OB-BAA

-   provides the ability for service providers to define the YANG
    modules that comprise a type of Access Node (e.g., OLT, DPU). The
    project includes examples of the YANG modules for an OLT or DPU
    based on the work of the Broadband Forum\'s TR-413 specification.
    Included with this feature is additional capabilities to audit the
    adapters and the instances of access nodes that are associated with
    the type of access node.

-   includes the basic framework for support NETCONF notifications
    needed in future releases of OB-BAA

In addition to the new functionality, the OB-BAA distribution can be
download as a docker directly from the [Broadband Forum\'s public docker
repository](https://hub.docker.com/r/broadbandforum/baa).

Finally this release includes **additional** examples of NETCONF
commands that can be used to configure access nodes (OLTs, DPUs, ONTs)
including the ability to configure:

-   access node network interfaces along with their associated VLAN
    tagging (C-VLAN, S-VLAN) and p-bit policies

-   forwarding rules for traffic between the ONU UNI and the OLT NNI

-   traffic descriptors and associated traffic filters for the access
    node

-   Layer 1 profiles (e.g., G.Fast) and association of the profile to
    the interfaces

## Restrictions:
1. Restriction for TR-385 PON YANG modules: 
	When trying to create a channel partition, the BAA layer returns an error that "An unexpected element channel-group-ref is present".
	This message is due to the Open Daylight YANG tools component used by the BAA layer. There has been an issue submitted for this problem
	with the OpenDaylight team.

<a name="rel_1_1_0"></a>

Release 1.1.0 Highlights:
=========================

-   BAA Core Framework Enhancements:

    -   Consistency checking within the BAA layer for configuration
        commands

    -   Incorporate the ability to include vendor specific modules and
        deviations from the common modules per AN type using inline
        schema mounts.

-   Plugins and Profiles

    -   NETCONF Device Adapter Plugins for OLTs and DPUs

-   BAA Layer Administration

    -   Device Adapter Management

-   Relevant documentation

    -   Examples of requests to the BAA layer and YANG modules for an
        OLT

<a name="rel_1_0_0"></a>

Release 1.0.0 Highlights:
=========================

-   Framework for the BAA Layer to include:

    -   NETCONF/YANG based Southbound Interface

    -   NETCONF/YANG based Northbound Interface

    -   Persistent management enabled BAA core that allows an Access
        Node to be configured when they are offline and to be
        synchronized when connectivity is reestablished.

-   Capabilities to discover and manage an Access Node that includes the
    ability to:

    -   YANG module sets for a type of Access Node

    -   Discover an Access Node based on Direct SSH, TR-301 CallHome

    -   Create, retrieve, update and delete (CRUD) an Access Node within
        the BAA layer as well the AN\'s data

-   Relevant documentation

    -   Deployment & Usage instructions

    -   Simulator recommendations

    -   Examples of requests to the BAA layer and YANG modules for a DPU

