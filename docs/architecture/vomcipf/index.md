
<a id="vomcipf" />

# vOMCI Proxy and vOMCI Function

## Introduction

The vOMCI Function, also known as the vOMC PF (Processing Function), and
vOMCI Proxy provide the capabilities needed to:

-   Translate YANG request/responses and notifications to/from the
    vOLTMF into the OMCI messages

-   Transmit and receive request/responses and notification to/from the
    OLT

## vOMCI Function

The vOMCI function, is deployed as a microservice, is responsible for:

-   Receiving service configurations from the vOLT Management function

-   Translating the received configurations into ITU G.988 OMCI management 
    entities (ME) and formatting them into OMCI messages

-   Encapsulating and sending (receiving and de-encapsulating) formatted OMCI 
    messages (ultimately targeting the ONU attached to the OLT) to (from) the 
    vOMCI Proxy

-   Translating the OMCI messages (representing ONU's operational data) received 
    from the vOMCI Proxy into data (e.g. notifications, acknowledges, alarms, PM
    registers) understandable by the vOLT Management function

-   Sending the above ONU operational data to the vOLT Management function

The vOMCI function communicates with the vOLT Management function using Kafka bus that exchange YANG messages that have been encapsulated in JSON format as defined in WT-451.

**Note:** In this release there is a limitation of a single vOMCI Proxy instance for each OLT.

In this release, the communication between the vOMCI Proxy and the vOMCI function uses a gRPC connection where the remote endpoint is the vOMCI Proxy that serves as an application gateway/proxy function between the OLT and vOMCI function.

## vOMCI Proxy

The vOMCI Proxy works as an aggregation and interception point that
avoids the need for an OLT to directly connect to individual vOMCI
functions and can act as a control/interception point between the OLT
and vOMCI function that is useful for troubleshooting and other
management purposes (e.g., security). As the aggregation/interception
point, the vOMCI Proxy is responsible for maintaining the associations
between OLTs and the corresponding vOMCI functions.

The vOMCI Proxy, deployed as a microservice, communicates with the vOMCI function.

The vOMCI Proxy is a gRPC endpoint that communicates with the OLT.

The vOMCI architecture diagram below depicts the vOMCI function and
vOMCI Proxy microservices:

<p align="center">
 <img width="600px" height="400px" src="{{site.url}}/architecture/voltmf/voltmf_design.png">
</p>

## vOLTMF to vOMCI function Notification and Request Handling


The vOLTMF sends JSON encapsulated requests and notification events as
defined in section 5.4.1 of WT-451. The messages are transmitted to the
vOMCI function using the Kafka bus.

The OB-BAA implementation adds an \"identifier\" field to the payload of
YANG messages that is used to correlate the message request and
response.

### Detect/Undetect Notifications

The notification (event) messages sent between the vOLTMF and vOMCI
function are defined in section 5.4.1 of WT-451. The OB-BAA
implementation adds a \"labels\" field to the message in provide the
vOMCI Proxy the information needed to forward the message to the correct
vOMC PF.

The following code block describes a DETECT notification that is sent
from the vOLTMF toward a vOMCI function with the additional \"labels\"
field inserted in the message:

```
{
	"onu-name":"exampleDevice",
	"olt-name":"olt1",
	"onu-id":"25",
	"channel-termination-ref":"CT-2",
	"event":"DETECT",
	"labels":"{\"name\" : \"vendor\", \"value\": \"BBF\"}",
	"payload":"{\"operation\":\"DETECT\", \"identifier\":\"0\"}"
}
```

### Requests

The request messages sent between the vOLTMF and vOMCI function are
defined in section 5.4.1 of WT-451.

The OB-BAA implementation adds a \"delta\" field to the edit-config
message to provide the information needed by the
vOMCI function to assist in the translation of the YANG message into the
correct OMCI message.

The following code block describes an edit-config request that is sent
from the vOLTMF toward a vOMCI function with the additional \"delta\"
and identifier fields inserted in the message:

```
{
  "onu_name": "onu1",
  "event": "request",
  "payload": {
    "identifier": "3",
    "operation": "edit-config",
    "current": {
      "ietf-hardware:hardware": {
        "component": [
          {
            "name": "chassis_onu1",
            "admin-state": "unlocked",
            "class": "iana-hardware:chassis"
          },
          {
            "parent": "chassis_onu1",
            "name": "ontcage_onu1",
            "parent-rel-pos": 0,
            "class": "bbf-hardware-types:cage"
          },
          {
            "parent": "ontcage_onu1",
            "name": "ontsfp_onu1",
            "parent-rel-pos": 0,
            "class": "bbf-hardware-types:transceiver"
          },
          {
            "parent": "ontsfp_onu1",
            "name": "ontaniport_onu1",
            "parent-rel-pos": 1,
            "class": "bbf-hardware-types:transceiver-link"
          },
          {
            "parent": "chassis_onu1",
            "name": "ontcard1_onu1",
            "admin-state": "unlocked",
            "parent-rel-pos": 1,
            "class": "bbf-hardware-types:board"
          },
          {
            "parent": "ontcard1_onu1",
            "name": "ontuni_uni1_onu1",
            "parent-rel-pos": 1,
            "class": "bbf-hardware-types:transceiver-link"
          }
        ]
      }
    },
	"delta": {
      "ietf-hardware:hardware": {
        "component": [{
            "parent": "ontcard1_onu1",
            "name": "ontuni_uni2_onu1",
            "parent-rel-pos": 2,
            "class": "bbf-hardware-types:transceiver-link"
          },
          {
            "parent": "ontsfp_onu1",
            "name": "ontaniport_onu1",
            "parent-rel-pos": 1,
            "class": "bbf-hardware-types:transceiver-link"
          }
        ]
      }
    },
	"target": {
      "ietf-hardware:hardware": {
        "component": [
          {
            "name": "chassis_onu1",
            "admin-state": "unlocked",
            "class": "iana-hardware:chassis"
          },
          {
            "parent": "chassis_onu1",
            "name": "ontcage_onu1",
            "parent-rel-pos": 0,
            "class": "bbf-hardware-types:cage"
          },
          {
            "parent": "ontcage_onu1",
            "name": "ontsfp_onu1",
            "parent-rel-pos": 0,
            "class": "bbf-hardware-types:transceiver"
          },
          {
            "parent": "chassis_onu1",
            "name": "ontcard1_onu1",
            "admin-state": "unlocked",
            "parent-rel-pos": 1,
            "class": "bbf-hardware-types:board"
          },
          {
            "parent": "ontcard1_onu1",
            "name": "ontuni_uni1_onu1",
            "parent-rel-pos": 1,
            "class": "bbf-hardware-types:transceiver-link"
          },
          {
            "parent": "ontcard1_onu1",
            "name": "ontuni_uni2_onu1",
            "parent-rel-pos": 2,
            "class": "bbf-hardware-types:transceiver-link"
          }
        ]
      }
    }
  }
}
```

### Responses

The response messages sent from the vOMCI function to the vOLTMF are
defined in section 5.4.1 of WT-451 with the addition of the
\"identifier\" field within the payload of the response and the
\"labels\" field that are previously discussed in the request section.

The following code block provides a sample response to the a DETECT
event:

```

{
	"onu-name":"exampleDevice",
	"olt-name":"olt1",
	"onu-id":"25",
	"channel-termination-ref":"CT-2",
	"event":"DETECT",
	"labels":"{\"name\":\"vendor\", \"value\":\"BBF\"}",
	"payload":"{\"operation\":\"DETECT\", \"identifier\":\"0\", \"status\":\"OK\"}"
}
```

## vOMCI function Communication with the OLT

The vOMCI function (vOMC PF) receives requests to translate YANG
messages to corresponding OMCI messages. These OMCI messages are
directed toward an OLT where the targeted ONU is attached. The vOMC PF
interacts with the OLT via the vOMCI Proxy where the vOMCI Proxy is the
gRPC remote endpoint for the ONU.

[<--Architecture](../index.md#architecture)
