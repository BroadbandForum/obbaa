
<a id="pm_collection" />

# Performance Monitoring Collection

## Introduction

The BAA layer provides the capability to collect performance monitoring
(PM) data from managed pANs and then store the collected PM data as the
corresponding standardized data model\'s PM YANG data elements.

The primary responsibility of the PM Collection Framework is to:

-   Collect PM data from managed devices

-   Translate the PM data into the associated data elements defined by
    standardized pAN\'s YANG modules defined in the BBF\'s TR-413
    specification.

Once the data is translated into the data elements of the standardized
pAN\'s YANG modules, the PM Collection Framework hands the translated
data off to a data handler for storage.

Once stored the PM data is accessible to SDN Management and Control (SDN
M&C) elements for their specific functions (e.g., analysis,
troubleshooting). Access to the data store is through the Northbound
Interface of the BAA layer through direct access to the PM datastore.

## PM Collection Framework

The PM Collection Framework is a generalized framework that permits
collection of PM data using various protocols and mechanisms (e.g.,
IPFIX, SFTP).

The determination of which PM Collector is used by the pAN is
accomplished by configuring the pAN with the associated PM collection
template that includes information on what PM data the pAN is to
collect; the interval schedule to collect and report the collected PM
data and finally how the collected PM is transported to the associated
PM Collector in the BAA layer.

The figure below provides a high level overview of the PM Collection
Framework:

<p align="center">
 <img width="800px" height="600px" src="{{site.url}}/architecture//pm_collector/framework.png">
</p>

In this release, the PM data collection framework supports the IPFIX
protocol and mechanisms as IPFIX is the method defined by the BBF\'s
TR-383 specification.

### IPFIX Collector

The IPFIX PM Data Collector (IPFIX Collector) is based standardized
within the BBF\'s TR-383 Common YANG specification for Access Nodes
and is based on the IETF\'s [RFC 7011](http://Specification) IPFIX
protocol specification.

<p align="center">
 <img width="800px" height="600px" src="{{site.url}}/architecture//pm_collector/collection_flow.png">
</p>

Within RFC 7011, IPFIX supports multiple transport protocols (e.g., TCP,
UDP, SCTP), for the BAA layer implementation, the IPFIX collector is
based on the TCP transport protocol described in [section 10.4 of RFC
7011](https://tools.ietf.org/html/rfc7011#section-10.4).

The IPFIX template for PM data collection uses the YANG configuration
data model for IPFIX and Packet Sampling (PSAMP) protocols as defined in
[section 6 of RFC
6728](https://tools.ietf.org/html/rfc6728#section-6) for the IPFIX PM
data collection template.

Note: While the BAA layer uses the IPFIX template to configure the pAN
with the information elements, the BAA layer needs additional
information to manage the PM collection with a pAN. These elements
include items such as enabling/disabling the collection of PM data,
maintenance/persistence method (on-demand, always open) of the IPFIX
session and any session authentication information. This information is
defined in the bbf-obbaa-network-manager.yang.

### IPFIX Collector Translation Function

The IPFIX Collector\'s translation function accepts PM data encoded as
IPFIX information elements in IPFIX messages and decodes these elements
into corresponding standard pAN YANG leaf nodes and instances.

#### Decoding of IPFIX Messages

The IPFIX Collector receives IPFIX messages and based on the IPFIX Set
ID, decodes the information within the message.

| Set ID | Set Type |
| :--- | :--- |
|0-1|Unused|
|2|Template Sets|
|3|Options Template Sets|
|4-255|reserved for future use|
|256 and above|Data sets|

The decoding procedure is specific to the Set Type.

##### Decoding Template and Options Template Sets

We will support IPFIX decoding only when the Enterprise bit is enabled.
In that case vendor specific mappings will be considered for the IE ID
mapping and OBBAA will not be bundled with any default IEid mapping
file. The name of this mapping file should be IPFIX\_IEId.csv and should
be included with the southbound adapter used by the pAN. Whenever an
adapter is deployed, the adapter should be unzipped to find the
IPFIX\_IEid.csv mapping file and be placed in a folder that is named
\<vendor\>-\<model\>-\<type\>-\<interfaceVersion\> within the common
mount point. This mount point is configurable from the docker compose
file using ipfix : IPFIX\_IE\_MAPPING\_DIR. The following is a [Sample
IEid
mapping.csv](https://wiki.broadband-forum.org/download/attachments/68518162/Sample%20IEid%20mapping.csv?api=v2&modificationDate=1575019226038&version=2).
The columns of this csv mapping file is similar to that specified in the
IANA specific mapping file. Details of each column in the
IPFIX\_IEid.csv is as follows:

| Column name | Column description |
| :--- | :--- |
|ElementID|A numeric identifier of the Information Element (counter or metric) from the IPFIX stream.|
|Name|A unique and meaningful name for the mapping of the Information Element. This name should be reference to the YANG leaf node preferably in xpath format.|
|Abstract Data Type|Data type of the Information Element. One of the types listed in <https://tools.ietf.org/html/rfc7012#section-3.1>.|
|Data Type Semantics|The integral types are qualified by additional semantic details. Valid values for the data type semantics are specified in <https://tools.ietf.org/html/rfc7012#section-3.2>.|
|Status|The status of the specification of this Information Element. Allowed values are \'current\' and \'deprecated\'. All newly defined Information Elements have \'current\' status.|
|Description|Description of the Information Element.|
|Units|If the Information Element is a measure of some kind, the units of the measure
Range|Some Information Elements may only be able to take on a restricted set of values that can be expressed as a range (e.g., 0 through 511, inclusive). If this is the case, the valid inclusive range SHOULD be specified; values for this Information Element outside the range are invalid and MUST NOT be exported.|
|References|Identifies additional specifications that more precisely define this item or provide additional context for its use.|
|Revision|The revision number of an Information Element, starting at 0 for Information Elements at time of definition and incremented by one for each revision.|
|Date|The date of the entry of this revision of the Information Element into the registry.|

ElementID, Name and Abstract Data Type are mandatory fields that needs
to be filled for any particular Information element.

Note: We will not support decoding of Template/Option Template Sets if
the Enterprise bit is disabled, because there is no standard IEid
mapping file defined. Standard IEid mapping will be taken care
separately as part of a different story. If IPFIX messages have their
enterprise bit disabled, it will just be logged and not decoded.

All the IE-mapping files are placed in the IPFIX\_IE\_MAPPING\_DIR and
the IPFIX Collector uses the files in this directory to map IE ID to IE
names and values will be decoded based on the data type of the IE
mentioned in this IE mapping file. Hostname will correspond to the
deviceName of the device. IPFIX collector will make a netconf request to
OBBAA to retrieve the device details such as deviceType,
deviceInterfaceVersion and deviceVendor. Based on the device details it
will retrieve the IPFIX\_IEId.csv file
from \<vendor\>-\<model\>-\<type\>-\<interfaceVersion\> folder from
within the common mount point. If the IPFIX\_IEId.csv file is missing in
the IPFIX\_IE\_MAPPING\_DIR, then the collector will log an error
message that the mapping file for the IPFIX messages are missing.

Sample output of the IPFIX collector
```
{"sourceIP":"10.1.1.1","hostName":"lsdpu1","deviceAdapter":"sample-DPU-modeltls-1.0","templateID":267,"observationDomain":4335,"timestamp":"2020-02-14T05:45:03Z","data":[{"metric":"/if:interfaces-state/if:interface/if:statistics/if:in-errors","dataType":"unsigned32","value":"15"},{"metric":"/if:interfaces-state/if:interface/if:statistics/if:out-discards","dataType":"unsigned32","value":"150"},{"metric":"/if:interfaces-state/if:interface/if:statistics/if:in-discards","dataType":"unsigned32","value":"1500"},{"metric":"/if:interfaces-state/if:interface/if:statistics/if:out-errors","dataType":"unsigned32","value":"1"},{"metric":"/if:interfaces-state/if:interface/if:name","dataType":"string","value":"DSL1"},{"metric":"280.3729","dataType":"string","value":"00000000"}]}
```

#### Publishing the decoded IPFIX message to the Data handler

Once an IPFIX messages has been decoded into the requisite IEs and their
associated values, the corresponding output is provided to Data
Handler(s) (IpfixDataHandler) that subscribed to the IPFIX Collector.
Data handlers can subscribe to IPFIX collector using the interface
DataHandlerService. DataHandler should invoke registerIpfixDataHandler
and unregisterIpfixDataHandler of the DataHandlerService from their init
and destroy methods respectively. DataHandlerServiceImpl implements
DataHandlerService which maintains the list of Data handlers.
IpfixCollector will invoke the data handler\'s handleIpfixData() to send
the decoded IPFIX message.

```
public interface DataHandlerService {
	void registerIpfixDataHandler(IpfixDataHandler dataHandler);
  void unregisterIpfixDataHandler(IpfixDataHandler dataHandler);
}
```

The decoded message is provided to the Data Handler by invoking
handleIpfixData() method of IpfixDataHandler. Data handlers should
implement the interface IpfixDataHandler.

```
public interface IpfixDataHandler{
    void handleIpfixData(String ipfixMessageJson);
}
```

## PM Data Collection Sequence Diagram

The following is a typical sequence of events for the collection of PM
data.

<p align="center">
 <img width="800px" height="600px" src="{{site.url}}/architecture//pm_collector/collection_sequence.png">
</p>

In this sequence diagram the BAA layer configures the pAN with the PM
collection template. Once received the pAN establishes a connection to
the Data Collector and sends the PM data encoded based on the
information of the PM collection template. In this case, IPFIX is the
method for PM data collection where the pAN takes on the role of a IPFIX
exporter and sends its PM data encoded as IPFIX messages to the BAA
layer\'s IPFIX collector. Upon reception of the PM data encoded in IPFIX
messages, the IPFIX collector decodes the IPFIX messages into the
requisite IPFIX IEs and then sends the decoded IEs to subscribed Data
Handlers.

## Deployment Architecture

The IPFIX collector is bundled as docker image and deployment is similar
to BAA core service. The IPFIX collector listens for the IPFIX stream on
default port 4494 that is configurable via the docker-compose file. The
docker-compose file will also include the mount point from where the
IPFIX\_IEId.csv file is located.

Sample IPFIX Collector docker-compose file
```
version: '3.5'
networks:
    baadist_default:
        driver: bridge
        name: baadist_default
services:
    ipfix-collector:
        image: ipfix-collector
        container_name: ipfix-collector
        restart: always
        ports:
            - "4494:4494"
        environment:
            - IPFIX_COLLECTOR_PORT=4494
            - IPFIX_IE_MAPPING_DIR=/ipfix/ie-mapping/
        volumes:
            - /baa/stores/ipfix:/ipfix/ie-mapping/
        networks:
            - baadist_default
```

## PM Data Handler Description

The PM Data Handler supports persistency of IPFIX data in form of a time
series DB. InfluxDB version 2.0.0 is used for this. Access to persisted
data is accomplished using the InfluxDB api.

-   Init and shutdown: On the init event the PM Data handler connects to the TSDB and
    registers at the IPFIX Collector. During shutdown the opposite
    operations in opposite order are executed.

-   Handling of IPFIX messages: The IPFIXMessage is parsed to create an array of DB tags from all
    data except for the timestamp and the counter values. Tags should be
    of data type String and Counter should be of type long, double,
    boolean or string depending on the data type of the counter.

## IPFIX Background Material

This section of the document provides background material on the IPFIX
protocol that is useful in understanding how to the IPFIX Collector in
the BAA layer and IPFIX exporter in the pAN establish sessions and
exchange information.

RFC Link : <https://tools.ietf.org/html/rfc7011>

The IPFIX messages from the pAN can be either data set, template set or
options template
set <https://tools.ietf.org/html/rfc7011#section-3> with examples
presented in <https://tools.ietf.org/html/rfc7011#appendix-A>.

<p align="center">
 <img width="600px" height="300px" src="{{site.url}}/architecture//pm_collector/ipfix_message.png">
</p>

### Message Header

<p align="center">
 <img width="500px" height="600px" src="{{site.url}}/architecture//pm_collector/ipfix_message_header.png">
</p>

### Template Set Example

<p align="center">
 <img width="600px" height="800px" src="{{site.url}}/architecture//pm_collector/ipfix_template_set.png">
</p>

### Options Template Set Example

<p align="center">
 <img width="600px" height="800px" src="{{site.url}}/architecture//pm_collector/ipfix_options_set.png">
</p>

### Data Set Example

<p align="center">
 <img width="600px" height="800px" src="{{site.url}}/architecture//pm_collector/ipfix_data_set.png">
</p>

[<--Architecture](../index.md#architecture)
