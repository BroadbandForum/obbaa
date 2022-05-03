
<a id="onu_auth" />

# ONU Authentication Function

The ability to identify an ONU and subsequently authenticate that the
ONU belongs to a subscriber of one or more services has been
traditionally performed as an function embedded within the physical OLT.
In this regard the OLT acted as the policy enforcement point (PEP) and
policy decision point (PDP). The ONU Authentication virtual function
allows the BAA layer to act as a PDP entity where the BAA layer itself
is able to perform the identification and authentication of an ONU based
on a set of policy rules or the BAA layer can work in concert with
the OLT and/or other SDN management entities to collectively identify
and authenticate the ONU.

## ONU management

The management of an ONU device is the process that is responsible, once
the ONU has been authenticated, to:

-   Push configuration data to this ONU

-   Read state information from this ONU

-   Send actions to this ONUs

-   Receive autonomous notifications/alarms from this ONU

Strictly speaking ONU management and ONU authentication are different
things. By the definition above, an ONU first needs to be authenticated
(i.e. officially identified) before it can be managed. ONU
authentication is the process that binds the ONU that gets connected to
its device instance in the list of expected ONUs.

<p align="center">
 <img width="800px" height="400px" src="{{site.url}}/architecture/onu_auth/onu_auth_mgmt.png">
</p>

## ONU authentication

As mentioned above, ONU authentication is the process by which the xPON
access network determines which ONU device (hence which **subscriber** at
Service Management level) is speaking upstream, in order to:

-   Verify whether the ONU is *allowed* to get service

-   *Retrieve the right configuration* to be pushed to the ONU via OMCI
    (ONU binding)

Explicit ONU authentication is fundamental in an xPON access network
because the xPON access network has no *physical means* to recognize
which ONU is speaking when getting connected. This is different for a
DSLAM/DPU with pt-pt DSL circuits where the *local circuit* at the
DSLAM *does* identify the subscriber.

In all cases, ONU authentication is done by having the xPON access
network compare credentials offered by the ONU at activation time with
pre-configured credentials of the expected ONU devices.

A variety of credentials and procedures can be used to authenticate an
ONU, for instance, matching Serial-Number, Registration-Id or Loid,
running the OMCI Mutual Authentication protocol, or combinations
thereof. Which ONU authentication procedure that need to be considered
depends on operational procedures at the discretion of the operator;
depending on the use-case this could even change in time for a given
ONU, e.g. to ease truck-roll replacement of a failed ONU.

## Authorization entity for ONU authentication

Conceptually, ONU authentication and ONU management are ultimately the
responsibility of the management entities in the management plane, that
have the knowledge of the credentials of the expected ONUs and the
configuration data for each of the ONUs. However several deployments are
possible where management entities gradually delegates more and more of
their authority to the OLT.

-	In a first scenario, the management entity, i.e. the vOLTMF, keeps direct authority for the authentication and management of ONUs:

  <p align="center">
   <img width="800px" height="600px" src="{{site.url}}/architecture/onu_auth/voltmf_mgmt_auth.png">
  </p>

In this deployment, the *TR-385 Separated NE mode* is used for the ONUs. The ONU authentication relies on the PMA, which holds a list of schema-mounted expected ONUs and their authorized credentials, keyed by a device "name" string. The expected credentials are stored in the ONU meta-data appended to the ONU mount-points.

-	In a second scenario, the vOLTMF keeps direct authority for ONU
    management but delegates its ONU authentication authority to the
    OLT:

<p align="center">
 <img width="800px" height="600px" src="{{site.url}}/architecture/onu_auth/voltmf_delegate_auth.png">
</p>

In this deployment, the TR-385 Separated NE mode is also used for these ONU(s). In this case, the OLT is able to perform simple ONU authentication based on expected-serial-number and/or expected-registration-id credentials stored in the vANIs

-	In a third scenario, the vOLTMF keeps the authority to authenticate the ONU but delegates its ONU management authority to the OLT:

<p align="center">
 <img width="1000px" height="600px" src="{{site.url}}/architecture/onu_auth/voltmf_auth_delegate_mgmt.png">
</p>

This deployment relies on the use for these ONUs of the *TR-385 Separated NE mode* in the vOLTMF and the *TR-385-Combined mode* in the OLT.

-	Finally in a fourth scenario, the vOLTMF delegates to the OLT its authority for both authentication and management of ONUs:

<p align="center">
 <img width="1000px" height="600px" src="{{site.url}}/architecture/onu_auth/voltmf_delegate_mgmt_auth.png">
</p>

In this deployment, the *TR-385 Combined NE mode* is used in the OLT for these ONUs. Very similar to how the PMA deals with ONUs, the OLT contains, embedded in its YANG configuration, a list of schema-mounted expected ONUs keyed by a device "name" string. The credentials of the expected ONUs are stored in the vANI and/or the ONU meta-data appended to the ONU mount-points.

In SDN systems, xPON access networks in general and OLTs in particular
allow a mix of ONUs on the same xPON such that:

-   some of the ONUs are authenticated and managed by the BAA layer

-   some of the ONUs are authenticated by the OLT and managed by the BAA
    layer

-   some of the ONUs are authenticated by the BAA layer and managed by
    the OLT

-   and the ONUs are authenticated and managed by the OLT

The four deployments possibilities are summarized in the following
table:

<p align="center">
 <img width="800px" height="600px" src="{{site.url}}/architecture/onu_auth/OLT_BAA_Combinations.png">
</p>

### Authenticating and managing an ONU: Deciding which entity is in charge - General Principle

The behavior of the OLT and BAA layer will ultimately be driven by the
YANG model of the OLT and BAA layer. Each scenario will require specific
configuration at the OLT and BAA layer and make use of appropriate YANG
notifications and actions between the OLT and the BAA layer.

When an ONU shows-up on the OLT, it is the OLT - based on its YANG
configuration from the BAA layer - that will first determine if it is
expected and/or able to authenticate it by itself or not.

In a simplified way, the coarse decision tree in the OLT to support the
four Use Cases above is illustrated as follows:

<p align="center">
 <img width="800px" height="600px" src="{{site.url}}/architecture/onu_auth/olt_mgmt_auth_flow.png">
</p>

If the OLT is expected and able to authenticate the ONU, it will do so.
The OLT will then further determine whether it is expected and able to
manage it itself using eOMCI. Otherwise the OLT will rely on the BAA
Layer\'s vOLTMF and the PMA for further initiative where for instance
the vOLTMF could apply vOMCI to the ONU (nb: finer grained flow charts
further below will reveal more alternatives).

If the OLT is not in a position to authenticate the ONU by itself, it
will rely on the vOLTMF and PMA for authentication of the unknown ONU.
The OLT will then wait for a confirmation from vOLTMF that the ONU is
legitimate and can go into service. The OLT will also expect to be told
by the vOLTMF that it is expected to manage the ONU with its local eOMCI
or instead that the ONU will be managed by vOMCI.

And finally, if both the OLT and the BAA layer are unable to
authenticate the ONU - and thus even less to manage it - then the ONU
will not go into service and the BAA layer would raise an alarm.

The following pictures illustrate each branch of the decision tree
process:

<p align="center">
 <img width="800px" height="600px" src="{{site.url}}/architecture/onu_auth/scen_4.png">
</p>

<p align="center">
 <img width="800px" height="600px" src="{{site.url}}/architecture/onu_auth/scen_2.png">
</p>

<p align="center">
 <img width="800px" height="600px" src="{{site.url}}/architecture/onu_auth/scen_3.png">
</p>

<p align="center">
 <img width="800px" height="600px" src="{{site.url}}/architecture/onu_auth/scen_1.png">
</p>

### Mapping between notifications and sequences

<p align="center">
 <img width="1200px" height="600px" src="{{site.url}}/architecture/onu_auth/pma_olt_events.png">
</p>

The notifications above don\'t exist as standalone identities. They are
derived from the TR-385 "onu-presence-state-change" which was extended
with the **determined-onu-management-mode** leaf.


[<--Architecture](../index.md#architecture)
