
<a id="vomcipf" />

# vOMCI Proxy and vOMCI Function

## Introduction

The vOMCI Function, also known as the vOMC PF (Processing Function), and
vOMCI Proxy provide the capabilities needed to:

-   Translate YANG request/responses and notifications to/from the
    vOLTMF into the OMCI messages

-   Transmit and receive request/responses and notification to/from the
    OLT

### vOMCI Function

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

The vOMCI function communicates with the vOLT Management function using Kafka bus that exchange GPB formatted messages as defined in WT-451.

**Note:** In this release there is a limitation of a single vOMCI Proxy instance for each OLT.

In this release, the communication between the vOMCI Proxy and the vOMCI function uses a gRPC connection where the remote endpoint is the vOMCI Proxy that serves as an application gateway/proxy function between the OLT and vOMCI function.

### vOMCI Proxy

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


## Deployment

The vOMCI function and vOMCI Proxy are deployed using a shared yaml file and the same docker network. An important part of deploying them correctly and ensuring the connectivity between network functions is assigning the correct GRPC endpoint names and Kafka topics.

Both the vOMCI function and the vOMCI Proxy have the following environmental variables in the yaml file:
- GRPC_SERVER_NAME: is used for setting the GRPC server local endpoint name
- GRPC_CLIENT_NAME: is used for setting the GRPC client local endpoint name
- KAFKA_CONSUMER_TOPICS: is used for setting the kafka consumer topics
- KAFKA_RESPONSE_TOPICS: for setting the response (kafka producer) topics
- KAFKA_NOTIFICATION_TOPICS: for setting the notification (kafka producer) topics

Note: Before any communication between the vOMCI function and vOMCI Proxy, for example when set_onu_communication request is received, it is important the remote endpoint name of the other entity to match the local endpoint name of that entity.

The following is an example yaml file that would define the deployment of a vOMCI Function:

```
omci:
  image: obbaa-vomci
  hostname: obbaa-vomci
  container_name: obbaa-vomci
  ports:
    - 8801:8801
    - 58433:58433
  environment:
    GRPC_SERVER_NAME: obbaa-grpc-1
    LOCAL_GRPC_SERVER_PORT: 58433
    KAFKA_BOOTSTRAP_SERVER: "kafka:9092 localhost:9092"
    KAFKA_CONSUMER_TOPICS: "OBBAA_ONU_REQUEST"
    KAFKA_RESPONSE_TOPICS: 'OBBAA_ONU_RESPONSE'
    KAFKA_NOTIFICATION_TOPICS: "OBBAA_PROXY_ONU_NOTIFICATION" networks:
    - baadist_default
  depends_on:
    - zookeeper
    - kafka
```

The following is an example yaml file that would define the deployment of a vOMCI Proxy:

```
vproxy:
      image: obbaa-vproxy
      hostname: obbaa-vproxy
      container_name: obbaa-vproxy
      ports:
        - 8433:8433
      environment:
        GRPC_CLIENT_NAME: obbaa-vproxy-grpc-client-1
        GRPC_SERVER_NAME: obbaa-vproxy-grpc-server-1
        LOCAL_GRPC_SERVER_PORT: 8433
        REMOTE_GRPC_SERVER_PORT: 58433
        REMOTE_GRPC_SERVER_ADDR: obbaa-vomci
        KAFKA_BOOTSTRAP_SERVER: "kafka:9092 localhost:9092"
        # List of Consumer topics, seperated by spaces
        KAFKA_CONSUMER_TOPICS: "OBBAA_PROXY_ONU_REQUEST"
        KAFKA_RESPONSE_TOPICS: "OBBAA_PROXY_ONU_RESPONSE"
        KAFKA_NOTIFICATION_TOPICS: "OBBAA_PROXY_ONU_NOTIFICATION"
      networks:
        - baadist_default
```

## vOLTMF to vOMCI/vOMCI Proxy Request Handling
The vOLTMF sends GPB formatted requests as defined in WT-451 for the vOLTMF-vOMCI interface. The messages are transmitted to the vOMCI function and the vOMCI Proxy function using the Kafka bus.

### Creating an ONU in the vOMCI function
The following is an example of a create-onu RPC that is directed toward a vOMCI function:

```
Msg {
	header {                      
	  msg_id: "1"                                                                                                                                                       
	  sender_name: "vOLTMF"                                                                                                                                             
	  recipient_name: "vomci-vendor-1"                                                                                                                                  
	  object_type: VOMCI_FUNCTION                                                                                                                                       
	  object_name: "vomci-vendor-1"                                                                                                                                     
	}                                                                                                                                                                   
	body {                                                                                                                                                              
	  request {                                                                                                                                                         
	    rpc {                                                                                                                                                           
	      input_data: "{\"bbf-vomci-function:create-onu\":{\"name\":\"ont1\"}}"                                                                                         
	    }                                                                                                                                                               
	  }                                                                                                                                                                 
	}
}
```

Upon receipt the receiving function ensures that the ONU is created within the function:
- ONU is added to the list of managed ONUs and internal structures are initialized
- Response is sent on the kafka bus

### Setting an ONU Management Chain
The following is an example of a set-onu-communication that is directed toward a vOMCI Proxy:

```
header.msg_id=2
header.sender_name="VOLTMF"
header.recipient_name="vproxy-vendor-1"
header.object_type = VOMCI_PROXY
header.object_name = "vproxy-vendor-1"

body.action.input_data =

"bbf-vomci-function:managed-onus":{
     "managed-onu":{
         "name":"onu1"
         "set-onu-communication":{
            "onu-communication-available": "true",
            "vomci-remote-endpoint-name":    "obbaa-grpc-1",
            "olt-remote-endpoint-name": "OLT1",
            "onu-attachment-point":{
               "olt-name":"OLT1",
               "channel-termination-name":"CT1",
               "onu-id": 1         
      }
   }
}
```

Upon receipt the receiving function ensures that the ONU communication is established and activation has started within the function:
- if set onu configuration has not been received before for this ONU:
	- save the new information regarding the ONU\'s management chain
	- lookup the communication channels with the given endpoint names and add to them the managed ONU
    - if the endpoint corresponds to a remote grpc server that is not known, the set-onu-communication request will be rejected by sending a unsuccessful kafka response otherwise send a successful kafka response
    - initiate ONU activation sequence (vOMCI function only)
    - send responses through kafka notifications (vOMCI function only)

- if ONU already exists and is configured (set ONU configuration has been received before):
	- if only communication available changed, only the "communication available" field is handled

### Deleting a Managed ONU
The following is an example of a delete-onu action that is directed toward a vOMCI Proxy:

```
Msg {
header {
  msg_id: "6"
  sender_name: "vOLTMF"
  recipient_name: "proxy-1"
  object_type: VOMCI_PROXY
  object_name: "proxy-1"
}
body {
  request {
    action {
      input_data: "{\"bbf-vomci-proxy:managed-onus\":{\"managed-onu\":[{\"name\":\"ont1\",\"delete-onu\":{}}]}}"
    }
  }
}
}
```
Upon receipt the receiving function ensures that the ONU is deleted within the function:
  - ONU is deleted from the list of managed ONUs and any internal structures
  - Response is sent on the kafka bus

### ONU requests
ONU is added to the list of managed ONUs and internal structures are initialized.
The OB-BAA implementation adds a "delta" field to the  edit-config message to provide the information needed by the vOMCI function to assist in the translation of the YANG message into the correct OMCI message.

The following example describes an edit-config request that is sent from the vOLTMF toward a vOMCI function with the additional "delta" and identifier fields inserted in the message:

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
### Notifications
Notifications can be sent from the vOMCI function or vOMCI Proxy for various purposes, For example, once the alignment of an ONU has been completed (after getting the MIB Data Sync from the ONU) the vOMCI function sends a notification to the vOLTMF.
The following is an example of a onu-alignment-status notification that sent from a vOMCI function:

```
header {
    msg_id: "1"
    sender_name: "vomci1"
    recipient_name: "vOLTMF"
    object_name: "onu1"
}
body {
    notification {
        data: "{\"bbf-vomci-function:onu-alignment-status\":{\"event-time\": \"2021-06-01T15:53:36+00:00\",\"onu-name\": \"ont1\",\"alignment-status\": \"aligned\"}}"
    }
}
```

## vOMCI function Communication with the OLT

The vOMCI function (vOMC PF) receives requests to translate YANG
messages to corresponding OMCI messages. These OMCI messages are
directed toward an OLT where the targeted ONU is attached. The vOMC PF
interacts with the OLT via the vOMCI Proxy where the vOMCI Proxy is the gRPC remote endpoint for the ONU.

[<--Architecture](../index.md#architecture)
