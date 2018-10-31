# OB-BAA R1 Release Notes (31 October 2018)

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

