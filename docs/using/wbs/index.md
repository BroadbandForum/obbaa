
<a id="wbs" />

Integrating Whitebox devices in the OB-BAA Framework
=================

OB-BAA provides the following functions and adaptations that are
necessary to integrate whitebox access node solutions into a
standardized representation of the access node as defined in TR-413:

-   SBI adapters that permit a whitebox solution to be managed using
    standard YANG modules defined in TR-413 for the M<sub>inf</sub> reference point
    and the OB-BAA M<sub>fc-conf</sub> reference point.

-   Functionality to relay user and control plane packets between the
    whitebox and the SDN Management and Control Function using the
    OB-BAA M<sub>fc-relay</sub> reference point. Further information on OB-BAA\'s
    control plane relay functionality can be found
    [here](../../architecture/control_relay/index.md#control_relay).

-   For OLT whiteboxes, functionality to manage ONUs via OB-BAA\'s vOMCI
    and vOLTMF functions. Further information on OB-BAA\'s management of
    ONUs can be found [here](../../architecture/index.md#architecture).

Using the standard interfaces and adaptation functionality of OB-BAA,
whitebox solutions can co-exist with traditional access nodes, providing
a consistent interface between the access nodes and the SDN Management
and Control Function, further reducing the cost of operations by
removing proprietary solutions needed to manage and control access
nodes. Additionally, the risk of introducing new technologies is reduced
as OB-BAA provides a path for coexistence and migration between existing
traditional elements and whitebox solutions.

**Info:** The Broadband Forum has on-going work in Working Text WT-451 to standardize the disaggregated management of ONUs. 
The vOMC support in OB-BAA is based on a functional split and architecture not currently reflected in the current WT-451 draft. 
The OB-BAA Team will contribute the necessary modifications to the Working Text prior to publication within the BBF. 
Future releases of OB-BAA are expected to incorporate the updated, standardized elements of WT-451 once the Working Text is published.

**Info:** The Broadband Forum has started WT-477 to standardize the interfaces between whiteboxes and the SDN Management and Control function including the Control Plane relay. 

The OB-BAA Control Plane relay functionality and interfaces have been contributed to be considered for inclusion of WT-477 and future releases of OB-BAA are expected to incorporate the standardized elements of WT-477 once the Working Text is published.

The following diagram depicts the Whitebox\'s interfaces to the various
services that comprise OB-BAA for the whitebox solution:

<p align="center">
 <img width="600px" height="400px" src="{{site.url}}/using/wbs/wbs_func.png">
</p>

While the reference points shown in the above figure use the standard
adapters from the OB-BAA distribution, the OB-BAA framework is built to
easily plug Vendor Adapters that can be used when the interface to the
whitebox device (and actually any Access Node design) is vendor
specific.

Further information about developing Device Adapters can be found
[here](../../architecture/device_adapter/index.md#device_adapter).

Example OLT Whitebox Solution
=============================

The following is an example of a white box solution that uses a system
on a chip (SOC) reference implementation (e.g., Broadcom) and BBF open
source reference software to provide many of the components to support
the standardized and OB-BAA specific M<sub>inf</sub>, M<sub>fc-conf</sub>, M<sub>fc-relay</sub> and
M<sub>vOMCI-OLT</sub> reference points. The vendor also added support for the
TR-413 IPFIX exporter as well as extensions for any vendor specific
functions that the vendor provides. The SOC reference implementation
provider and vendor supplied extensions (e.g., Altran) are hosted on any
whitebox OLT that supports the SOC reference implementation
(e.g, Broadcom 6862x and 6865x OLT MAC devices and Broadcom 88470
switch).

<p align="center">
 <img width="600px" height="400px" src="{{site.url}}/using/wbs/wbs_impl.png">
</p>

**Info:** Further information about this example\'s open source SOC reference implementation can be found [here](https://github.com/balapi/netconf-polt).

[<--Using OB-BAA](../index.md#using)
