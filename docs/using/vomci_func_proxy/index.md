
<a id="vomci_func_proxy" />

# Examples for configuring the vOMCI function and vOMCI proxy

The configuration of the vOMCI function and vOMCI proxy can be made
through the Northbound API of OB-BAA. The Aggregator component converts
the NETCONF requests into Kafka messages and delivers them to the
respective function.

The examples referred in this section can be found
in *resources/examples/vomci-end-to-end-config*.

**Pre-requisites:**

-   Running instances of BAA core (contains the vOLTMF), vOMCI, vOMCI
    Proxy

    -   The docker-compose file present in baa-dist should be used,
        since it contains initialization parameters that match the
        examples below.

**Steps**

1.  Create the network functions and specify the parameters for the BAA
    core to connect - *1-create-network-functions.xml*.

2.  Configure vOMCI function with connectivity endpoint information
    *2-configure-vomci.xml*

    a.  The example includes a Kafka endpoint for communicating with the
        VOLTMF and a gRPC endpoint for listening to connections from the
        vOMCI Proxy.

3.  Configure vOMCI proxy with connectivity endpoint information
    *3-configure-vproxy.xml*

    a.  The example includes a Kafka endpoint for communicating with the
        VOLTMF, a gRPC endpoint for connecting to the vOMCI function and
        an endpoint for listening from connections from OLTs.

NOTE: The vOMCI function and vOMCI Proxy start with the default
configurations specified in the docker-compose file. When doing these
steps, those configurations will be overridden.


[<--Using OB-BAA](../index.md#using)
