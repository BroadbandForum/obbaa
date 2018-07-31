<h1>Open Broadband: Broadband Access Abstraction (OB-BAA)</h1>
<h4>Version 1.0.0</h4>

**Table of Contents**

1. [Introduction](#introduction)
2. [Overview](./overview/)
3. [Architecture](./architecture/)
4. [Installing OB-BAA](./installing/)
5. [Using OB-BAA](./using/)

# Introduction

<a id="introduction" />

## Legal Notice

  The Broadband Forum is a non-profit corporation organized to create
  guidelines for broadband network system development and deployment.
  This Design Document has been approved by members of the Forum.
  This Design Document is subject to change.  This Design Document
  is copyrighted by the Broadband Forum, and all rights are reserved.
  Portions of this Design Document may be copyrighted by Broadband
  Forum members.

### Intellectual Property

  Recipients of this Design Document are requested to submit, with
  their comments, notification of any relevant patent claims or other
  intellectual property rights of which they may be aware that might
  be infringed by any implementation of this Design Document, or use
  of any software code normatively referenced in this Design Document,
  and to provide supporting documentation.

### Terms of Use

#### License

  Broadband Forum hereby grants you the right, without charge, on a
  perpetual, non-exclusive and worldwide basis, to utilize the Technical
  Report for the purpose of developing, making, having made, using,
  marketing, importing, offering to sell or license, and selling or
  licensing, and to otherwise distribute, products complying with the
  Design Document, in all cases subject to the conditions set forth
  in this notice and any relevant patent and other intellectual
  property rights of third parties (which may include members of
  Broadband Forum).  This license grant does not include the right to
  sublicense, modify or create derivative works based upon the
  Design Document except to the extent this Design Document includes
  text implementable in computer code, in which case your right under
  this License to create and modify derivative works is limited to
  modifying and creating derivative works of such code.  For the
  avoidance of doubt, except as qualified by the preceding sentence,
  products implementing this Design Document are not deemed to be
  derivative works of the Design Document.

#### NO WARRANTIES

  THIS DESIGN DOCUMENT IS BEING OFFERED WITHOUT ANY WARRANTY WHATSOEVER,
  AND IN PARTICULAR, ANY WARRANTY OF NONINFRINGEMENT IS EXPRESSLY
  DISCLAIMED. ANY USE OF THIS DESIGN DOCUMENT SHALL BE MADE ENTIRELY AT
  THE IMPLEMENTER'S OWN RISK, AND NEITHER THE BROADBAND FORUM, NOR ANY
  OF ITS MEMBERS OR SUBMITTERS, SHALL HAVE ANY LIABILITY WHATSOEVER TO
  ANY IMPLEMENTER OR THIRD PARTY FOR ANY DAMAGES OF ANY NATURE WHATSOEVER,
  DIRECTLY OR INDIRECTLY, ARISING FROM THE USE OF THIS DESIGN DOCUMENT.

#### THIRD PARTY RIGHTS

  Without limiting the generality of Section 2 above, BROADBAND FORUM
  ASSUMES NO RESPONSIBILITY TO COMPILE, CONFIRM, UPDATE OR MAKE PUBLIC
  ANY THIRD PARTY ASSERTIONS OF PATENT OR OTHER INTELLECTUAL PROPERTY
  RIGHTS THAT MIGHT NOW OR IN THE FUTURE BE INFRINGED BY AN IMPLEMENTATION
  OF THE DESIGN DOCUMENT IN ITS CURRENT, OR IN ANY FUTURE FORM. IF ANY
  SUCH RIGHTS ARE DESCRIBED ON THE DESIGN DOCUMENT, BROADBAND FORUM
  TAKES NO POSITION AS TO THE VALIDITY OR INVALIDITY OF SUCH ASSERTIONS,
  OR THAT ALL SUCH ASSERTIONS THAT HAVE OR MAY BE MADE ARE SO LISTED.

  The text of this notice must be included in all copies of this
  Design Document.

## Revision History

### Release 1.0

* Release contains specification for the OB-BAA release 1.0.0.


## Participants

| :---: | :---: | :---: | :---: |
|![Broadcom](assets/img/broadcom.png){: width="100px" height="100px"}|![BT](assets/img/bt.png){: width="100px" height="100px"}|![Calix](assets/img/calix.png){: width="100px" height="100px"}|![CenturyLink](assets/img/centurylink.jpg){: width="100px" height="100px"}|
|![ChinaTelecom](assets/img/china-telecom-logo.jpg){: width="100px" height="100px"}|![Huawei](assets/img/huawei.jpg){: width="100px" height="100px"}|![Nokia](assets/img/nokia.png){: width="100px" height="90px"}|![Tibit](assets/img/tibit.png){: width="100px" height="45px"}|
|![TelecomItalia](assets/img/tim.png){: width="100px" height="70px"}|![UNH-IOL](assets/img/iol.png){: width="140px" height="75px"}|![ZTE](assets/img/zte_logo_en.png){: width="100px" height="75px"}|

How to Get Involved
===================

Involvement in OB-BAA requires that you sign the Broadband Forum\'s
[Contribution License Agreement (CLA)/Project Participation
Agreement](https://wiki.broadband-forum.org/download/attachments/37193235/OB-BAA%20CLA%2013Dec2017.pdf?version=1&modificationDate=1516308789992&api=v2)
(PDF), and then send the signed agreement to:

Name: [Robin
Mersh](https://wiki.broadband-forum.org/display/~rmersh@broadband-forum.org)
(CEO The Broadband Forum)

Email: <rmersh@broadband-forum.org>

Phone contact: +1 303 596 7448

Issues and bugs can be submitted using the Issues feature on the OB-BAA
Github repository.

License
=======

The OB-BAA project\'s license is structure is to provide a RAND-Z (Apache 2.0) license for the program\'s
software license and keep the Broadband Forum's license for the project\'s artifacts.

Program (Software) License
-------------------------- 

The OB-BAA project is considered a RAND-Z open source project for
program deliverable (e.g, source code and associated object code). The
program deliverables use the [Apache 2.0
license](http://www.apache.org/licenses/LICENSE-2.0).

Project Artifacts
-----------------

The OB-BAA project\'s artifacts (e.g., documentation, architecture
specifications, functional behavior descriptions, Epics, Stories) use
the [Broadband Forums IPR
policy](https://www.broadband-forum.org/about-the-broadband-forum/the-bbf/intellectual-property).

Third Party Tools and License
-----------------------------
The following tools are used by the OB-BAA project

| Software Name| Purpose | Vendor | Paid/Open Source | License | Details |
| :--- | :--- | :--- | :--- |:--- | :--- |
|Netopeer2|NetConf device simulator|CESNET|Open Source|BSD-3|<a href="https://github.com/CESNET/Netopeer2">Github Link</a>|

[Overview -->](./overview/)
