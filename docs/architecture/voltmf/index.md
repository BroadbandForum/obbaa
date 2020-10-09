
<a id="voltmf" />

# vOLT Management Function (vOLTMF)

## Introduction

This section describes high-level design of the vOLT Management function
(vOLTMF) used to manage ONUs through the vOMCI solution. This section
describes communication between the vOLTMF, vOMCI Proxy and vOMCI
function upon:

-   Creating and deleting ONUs

-   Receiving DETECT/UNDETECT notifications

-   Sending requests to ONUs

The vOLTMF manages ONUs through a standard ONU adapter that is deployed
for the ONU. The ONU adapter is standard library of the YANG modules for ONUs 
that the VOLTMF refers to for handling ONU requests, responses and 
notifications from external management systems. 
The following figure depicts the vOLTMF and ONU
Adapter components that reside in the BAA microservice.

<p align="center">
 <img width="600px" height="400px" src="{{site.url}}/architecture/voltmf/voltmf_design.png">
</p>

The vOLTMF performs actions upon receiving notifications and requests
either from an OLT device or other components within the BAA CORE. For
example, the onu-detected/undetected notification that is sent by the
OLT device on its Northbound Interface (NBI). The notification is
received by BAA CORE, it propagates the notification towards vOLTMF and
BAA NBI so that it can be handled by the Access SDN M&C.

Upon reception of the notification, vOLTMF processes the notification,
checks if a preconfigured ONU device exist and authenticates the ONU,
the vOLTMF transforms the notification to JSON format and propagates the
notification towards the vOMCI function using the Kafka bus.

All the detect/undetect notifications and YANG requests are sent towards
the vOMCI function via the Kafka bus in a message that contains the
payload. Once the vOMCI function processes the notification/requests, 
the vOMCI function sends the notification/request response in JSON format 
back to the vOLTMF via the Kafka bus and the response is received through 
the KafkaNotificationCallback\#onNotification().

Upon receiving the response, the vOLTMF is responsible for processing the
response and performs actions accordingly.

### vOLTMF Threadpools

There could be multiple interactions between the vOLTMF and the vOMCI
function including parallel configuration requests/commands for either
the same or different ONUs. In order to make these interactions
parallel, asynchronous such that the requests are not idle/blocked
waiting for responses, the vOLTMF has separate task queues and
threadpools. The following table shows the list of vOLTMF threadpools
that spawn new Runnable tasks:

|Name| Description| 
| :--- | :--- |
|processNotificationRequestPool|Used for processing mediated device event listener callbacks (deviceAdded, deviceRemoved) and device notification requests (DETECT/UNDETECT).|
|kafkaCommunicationPool|Used to process the individual GET/COPY-CONFIG/EDIT-CONFIG request inside A MediatedDeviceNetconfSession spawned by processRequestResponsePool.|
|kafkaPollingPool|Used to start up the KafkaConsumer implementation and polling for response notifications from vOMCI Proxy.|
|processNotificationResponsePool|Used for processing DETECT and UNDETECT notification responses from the vOMCI Proxy.|
|processRequestResponsePool|Used for processing GET/COPY-CONFIG/EDIT-CONFIG request responses from the vOMCI Proxy.|

### Notifications/Requests forwarding from the vOLTMF to the vOMCI function

The handling of the notification and request forwarding from the vOLTMF
to the vOMCI function is expected to be performed using the following 2
approaches. In this release, Approach 2 is implemented.

#### Approach 1: vOLTMF forwards notification/requests to vOMCI Proxy and vOMCI proxy takes care of forwarding it to vOMCI function

When the ONU identification in the DETECT/UNDETECT notification matches
with a ONU configured in BAA, the notification is forwarded to the vOMCI
Proxy in JSON formatted messages. When the notification is forwarded to
the vOMCI Proxy the vOLTMF adds tags to the notification. The vOMCI
Proxy then assigns the ONU to a vOMCI function when they have matching
tags. Once an ONU has been allocated to a vOMCI function, the vOMCI
proxy shares a map of the ONU to vOMCI function allocation, so that
subsequent requests are routed to the same vOMCI function.

In order to assign labels to an ONU, the vOLTMF uses the vendor name of the ONU.

**Example to illustrate the computation of \'labels\':**

-   When ONU is pre-configured, the vendor field in device-management
    node of the device is used as the vendor
-   DETECT/UNDETECT notifications are forwarded to the vOMCI proxy in
    JSON formatted messages that contain the label for the vendor.
    Notifications are forwarded to the vOMCI Proxy on topic
    **OBBAA\_ONU\_NOTIFICATION**

The following is a sample DETECT notification:
```
{
    "onu-name":"exampleDevice",
    "olt-name":"olt1",
    "onu-id":"25",
    "channel-termination-ref":"CT-2",
    "event":"detect",
    "labels":"{\"name\" : \"vendor\", \"value\": \"BBF\"}",
    "payload":"{\"operation\":\"detect\", \"identifier\":\"0\"}"
}
```

In the above scenario where vendor of the device is used as the
matching criteria and the ONU is not pre-configured, the vOLTMF
forwards the DETECT notification without tags to the vOMCI proxy and
the vOMCI proxy assigns the ONU to default vOMCI function. The
default vOMCI function retrieves basic data of the ONU and notifies
the vOLTMF. The vOLTMF updates this basic data for future use. When
the ONU is pre-configured, the vendor field in device-management
node of the device is used in the vendor label, the vOLTMF assigns
tags and sends new DETECT notification with tags to the vOMCI proxy.
The vOMCI proxy then assigns the ONU to the appropriate vOMCI
function based on the matching tags.

#### Approach 2: vOLTMF forwards notification/requests to vOMCI function directly

Notifications/requests are forwarded from the vOLTMF to the vOMCI
function using JSON formatted messages. The vOLTMF retrieves the
vomci-name from the device-details in order to forward the
notifications/requests to the correct vOMCI function. For a
pre-configured ONU, the vendor field in device-management of the device
is used as the vomci-name. By default vomci-name is empty(null)
corresponding to the standard vOMCI function. When notifications from
the vOLTMF are forwarded to the vOMCI function, the messages are published
on topics with a suffix that includes the vomci-name. This ensures that
messages are directed only to the intended vOMCI function. 
If the vomci-name is not present, the Kafka messages will not have the suffix and 
the messages will be forwarded to the standard vOMCI function.

**Note:** In a future release, the determination of the vomci-name is expected to be rule based that uses onu-criterion.

### gRPC connection establishment between pOLT and vOMCI proxy or vOMCI function

For establishing the connection between pOLT and the vOMCI proxy or the
vOMCI function, the association between the pOLT and vOMCI proxy or
vOMCI-function has to be configured in the BAA layer by the Access SDN
M&C. The following code block provides an example configuration for
associating vOMCIProxy within the pOLT.

The following is a Sample vOMCI function configuration on the OLT:
```
<remote-nf-settings xmlns="urn:bbf:yang:bbf-polt-vomci">
    <nf-client>
        <enabled>true</enabled>
        <client-parameters>
            <nf-initiate>
                <remote-endpoints>
                    <name>vOMCI-proxy</name>
                    <type>vOMCI-nf</type>
                    <remote-endpoint>
                        <access-points>
                            <name>vOMCIProxy</name>
                            <grpc>
                                <grpc-client-parameters>
                                        <remote-address>www.example.com</remote-address>
                                        <remote-port>443</remote-port>
                                        <local-address>0.0.0.0</local-address>
                                        <local-port>0</local-port>
                                        <keepalives>
                                            <idle-time>15</idle-time>
                                            <max-probes>3</max-probes>
                                            <probe-interval>30</probe-interval>
                                        </keepalives>
                                </grpc-client-parameters>
                            </grpc>
                        </access-points>
                    </remote-endpoint>
                </remote-endpoints>
            </nf-initiate>
        </client-parameters>
        <nf-endpoint-filter>
            <rule>
                <name>rule1</name>
                <priority>1</priority>
                <flexible-match>
                    <onu-vendor>PTIN</onu-vendor>
                </flexible-match>
            </rule>
        </nf-endpoint-filter>
    </nf-client>
    <nf-server>
        <!--  if at least one rule does not exist an error will be thrown -->
        <nf-endpoint-filter>
          <rule>
              <name>rule1</name>
              <priority>1</priority>
              <flexible-match>
                  <onu-vendor>none</onu-vendor>
              </flexible-match>
          </rule>
        </nf-endpoint-filter>
    </nf-server>
</remote-nf-settings>
```

Once this is configured on pOLT, the pOLT sends the HelloVomciRequest to
the vOMCI proxy. After receiving the HelloVomciRequest, the vOMCI proxy
registers this pOLT and uses this information when the vOMCI proxy has
to forward OMCI messages towards this pOLT.

### Kafka Topics

In this release, the vOLTMF uses the Kafka bus to communicate with the
vOMCI proxy and/or vOMCI function. The following topics are used on the
Kafka bus.

|Topic Name| Description| 
| :--- | :--- |
|OBBAA_ONU_NOTIFICATION|ONU lifecycle event forwarded from the the BAA Layer towards the vOMCI Proxy/function.|
|OBBAA_ONU_RESPONSE|Response received from the vOMCI function.|
|OBBAA_ONU_REQUEST|ONU request forwarded from the BAA layer towards the vOMCI Proxy/function.|

## ONU Creation and Deletion

### ONU Device Data Model

In OB-BAA, the ONU is managed as a separate network elements, like the
OLT and uses a new a connection type \"mediated-session\" is defined by
the
[Aggregrators](../aggregator/index.md#aggregator)
component\'s bbf-network-manager.yang YANG module in order to
communicate with the ONU. Additionally, a YANG module that represents
the ONU\'s management metadata is defined in
bbf-obbaa-onu-management.yang. The metadata information in this YANG
module includes data needed to discover and authenticate the ONU among
other items. For example:

-   The *onu-state-info* container holds the actual information read
    from the ONU when it becomes online.

-   The *onu-config* container contains the information for
    authenticating the ONU and that must be provided when creating the
    pONU instance in OB-BAA:

    -   Authentication method (see the ONU Authentication section below).
        Defines a combination of serial-number and/or registration id
    -   serial-number
    -   registration-id

The bbf-obbaa-onu-management.yang and bbf-network-manager.yang YANG
modules can be found in the /resources/models/yang/aggregator-yang
directory.

### Standard ONU Adapter

The management of an ONU utilizes the Device Adapter concept within
OB-BAA and the OB-BAA distribution provides an ONU standard adapter
called bbf-onu-standard that can be found in the distribution\'s
/resources/models/standard-adapters directory. The standard ONU adapter
has below properties:

-   adapterType: ONU
-   adapterModel: standard
-   vendor: BBF
-   version: 1.0

**Info:** ONU standard adapter - Model contents
The modules in the ONU standard adapter use the same modules as the OLT standard adapter with the exception of the modules that are OLT specific as per TR-413 and TR-385, sections 6.2.2. and 6.2.4:

-   bbf-link-table (Used in combined NE-mode)
-   bbf-xpon (Models the OLT side xPON configurations)
-   bbf-xponvani (vANI is the virtual entity on the OLT side)
-   bbf-xpon-onu-states (State data on the OLT side)
All the other modules in the ONU standard adapter are applicable for ONUs that are "TR-167 composite PON-fed access node" or have DSL ports.

### ONU Device Creation

ONU\'s can be created within the BAA layer either before an ONU is
detected (pre-provisioned) by an OLT or once an ONU has been detected by
an OLT. When the ONU has
been pre-provisioned within the BAA layer, the ONU device is created and
persisted in the BAA layer\'s datastore but no further processing with
respect to management of the ONU is performed (e.g., the vOMCI proxy is
not notified that an ONU has been detected). However when the OLT
notifies the BAA layer that the OLT has detected an ONU the OLT sends an
ONU state change notification that the BAA layer uses to determine if
the change of ONU state is because the OLT has detected an ONU
attachment.

If the BAA layer has determined that the ONU attachment has been
detected but the BAA layer doesn\'t have the ONU device configured in
the BAA layer\'s datastore, the ONU is treated as an orphaned or unknown
ONU. When this occurs, a GET request is sent to a default vOMCI function
in order to retrieve the software and hardware properties and the
unknown ONU is created and persisted along with the identification and
software and hardware properties. Once the ONU has been created within
the BAA layer with match ONU identification proprieties and the
authentication of the ONU succeeds, a DETECT notification is forwarded
to vOMCI proxy/function. More information regarding the ONU detection
procedure can be found in section 
[ONU DETECT event process when ONU Authentication has failed](#authfailed).

The following diagram depicts the pre-provisioning of an ONU within the
BAA layer. The creation of the ONU in the BAA layer when the OLT has
first detected the ONU attachment is explained in the [ONU Detect
Notification
Handling](#onudetectnotif)

<p align="center">
 <img width="700px" height="500px" src="{{site.url}}/architecture/voltmf/onu_create.png">
</p>

### ONU Device Deletion

ONUs in the BAA layer can be deleted regardless whether the ONU has
been pre-provisioned or was discovered by the OLT. In either case, the
deletion of the ONU device in the BAA layer, triggers the
MediatedDeviceEventListener\'s onuDeviceRemoved() method that results in
an UNDETECT notification being sent to the vOMCI proxy/function. If a
MediatedDeviceNetconfSession to the vOMCI Proxy/function has been
established then the UNDETECT notification is transmitted and the
session is closed after the response to the UNDETECT notification is
received. If the ONU device was never detected, the UNDETECT
notification is ignored and the notification is not forwarded to vOMCI
function.

The following diagram depicts the deletion of an ONU within the BAA
layer. The deletion of the ONU in the BAA layer when the OLT has
detected that the ONU has unattached from the OLT PON is explained in
the [ONU Undetect Notification
Handling](#onuundetectnotif)
section.

<p align="center">
 <img width="700px" height="500px" src="{{site.url}}/architecture/voltmf/onu_delete.png">
</p>

OLT Device Deletion

The OLT device in the BAA layer can be removed at anytime whether the
OLT has ONU device(s) associated with OLT. When the OLT device is
removed, it is vOLTMF\'s responsibility to perform clean up operations
in order to remove all the associated ONU devices and send the ONU
Undetect notification towards vOMCI proxy/function.

When any device gets removed including the OLT, the
MediatedDeviceEventListener\'s deviceRemoved() API is invoked. When the
deviceRemoved() API is invoked, the vOLTMF is notificed via the
oltDeviceRemoved function and the vOLTMF then retrieves all the ONUs
currently associated with the OLT and forwards the UNDETECT notification
for each ONU toward the vOMCI Proxy/function.

The following diagram depicts the deletion of an OLT within the BAA
layer:

<p align="center">
 <img width="700px" height="500px" src="{{site.url}}/architecture/voltmf/olt_delete.png">
</p>

## ONU-DETECT/UNDETECT Notification Handling

### Registration for OLT notifications (onu-detected/onu-undetected)

When an ONU\'s attachment state changes toward an OLT, the OLT transmits
onu-state-change notifications through its Northbound management system
to subscribed management entities including the BAA layer where the
vOLTMF uses these state change notifications to determine if the ONU has
been considered \"detected\" or \"undetected\".

The following is an example of an onu-state-change notification:

```
<notification xmlns="urn:ietf:params:xml:ns:netconf:notification:1.0">
	<eventTime>2019-07-25T05:53:36+00:00</eventTime>
	<bbf-xpon-onu-states:onu-state-change xmlns:bbf-xpon-onu-states="urn:bbf:yang:bbf-xpon-onu-states">                                
        <bbf-xpon-onu-states:detected-serial-number>ALCL00000002</bbf-xpon-onu-states:detected-serial-number>
		<bbf-xpon-onu-states:onu-id>25</bbf-xpon-onu-states:onu-id>
		<bbf-xpon-onu-states:channel-termination-ref>CT-2</bbf-xpon-onu-states:channel-termination-ref
		<bbf-xpon-onu-states:onu-state>onu-present-and-on-intended-channel-termination</bbf-xpon-onu-states:onu-state>
		<bbf-xpon-onu-states:detected-registration-id></bbf-xpon-onu-states:detected-registration-id>
		<bbf-xpon-onu-states:onu-state-last-change>2019-07-25T05:53:36+00:00</bbf-xpon-onu-states:onu-state-last-change>
		<bbf-xpon-onu-states:v-ani-ref>onu_25</bbf-xpon-onu-states:v-ani-ref>
	</bbf-xpon-onu-states:onu-state-change>
</notification>
```

In order to receive the onu-state-change notification, the BAA layer\'s
ONUNotificationListener registers with DeviceNotificationListenerService
for deviceNotifications using the ONUNotificationListener\'s
deviceNotificationReceived() callback method. Upon reception of the
notification, the ONUNotificationListener performs some validation on
the notification and forward the notification onto the vOLTMF using the
VOLTManagementImpl\'s ONUNotificationProcess() method for further
processing by the vOLTMF.

**Info:** The ONUNotificationListener is one if the entities that registers to receive ONU state change notifications, other listeners of device notifications also receive these ONU state change notifications for their purposes such as forwarding the notification to the Access SDN M&C.

### ONU state change notification mapping to detect and undetect events

The ONU detect and undetect events are mapped to one or more of the ONU
state change notifications received by the ONUNotificationListener.

The table below maps the ONU state change notifications to the ONU
DETECT or UNDETECT event:

|ONU State|Event| 
| :--- | :--- |
|onu-present-and-unexpected|detect|
|onu-present-and-on-intended-channel-termination|detect|
|onu-present-and-in-wavelength-discovery|detect|
|onu-present-and-discovery-tune-failed|detect|
|onu-present-and-no-v-ani-known-and-o5-failed|detect|
|onu-present-and-no-v-ani-known-and-o5-failed-no-id|detect|
|onu-present-and-no-v-ani-known-and-o5-failed-undefined|detect|
|onu-present-and-v-ani-known-and-o5-failed|detect|
|onu-present-and-v-ani-known-and-o5-failed-no-id|detect|
|onu-present-and-v-ani-known-and-o5-failed-undefined|detect|
|onu-present-and-no-v-ani-known-and-o5-passed|detect|
|onu-present-and-no-v-ani-known-and-unclaimed|detect|
|onu-present-and-v-ani-known-but-intended-ct-unknown|detect|
|onu-present-and-in-discovery|undetect|
|onu-present-and-emergency-stopped|undetect|
|onu-not-present|undetect|
|onu-not-present-with-v-ani|undetect|
|onu-not-present-without-v-ani|undetect|

### ONU Authentication

Upon receiving an ONU state change notification, the vOLTMF goes through
a process of identifying and authenticating the ONU using the
information in the ONU state change notification and information that
was assigned to the ONU in the BAA layer for the purpose of
authenticating the ONU.

In this release, the procedure to authenticate an ONU in accomplished by
interrogation a set of rule criteria that is configured when the ONU is
instantiated. The vOLTMF evaluates the information from the ONU state
change information with the rules provided in the table below:

|Parameters supplied at ONU instance creation time (config-info)|Result| 
| :--- | :--- |
|expected-registration id<br />expected-attachment-point|The ONU with a matching registration id will only be allowed in the specified attachment point|
|expected-registration-id|The ONU with a matching registration id will be allowed in any attachment point|
|expected-serial-number|The ONU with a matching serial number will be allowed in any attachment point|
|expected-serial-number<br />expected-attachment-point|The ONU with a matching serial number will only be allowed in the specified attachment point|
|expected-serial-number<br />expected-registration id<br />expected-attachment-point|The ONU with a matching serial number and registration id will be allowed in the specified attachment point|
|expected-serial-number<br />expected-registration-id|The ONU with a matching serial number and registration id will be allowed in any attachment point|

### ONU Detection<a name="onudetectnotif"></a>

Upon receiving of an onu-state-change notification from the OLT, the
vOLTManagementFunction determines if the if the onu-state-change
notification translates into a DETECT event as described above. If the
ONU passes the authentication procedure described above, the vOLTMF
sends a ONU DETECT event to the vOMCI Proxy/function using the
**OBBAA\_ONU\_NOTIFICATION** topic on the Kafka bus. An example of the
ONU DETECT event is provided below:

```
{
    "onu-name":"onu1",
    "olt-name":"olt1",
    "channel-termination-ref":"CT-2",
    "onu-id":"25",    
    "event":"detect",
    "labels":"{\"name\" : \"vendor\", \"value\": \"BBF\"}",
    "payload":"{\"operation\":\"detect\", \"identifier\":\"0\"}"
}
```

Once the vOMCI function processes ONU DETECT event, the vOMCI
Proxy/function sends response to the event using the
**OBBAA\_ONU\_RESPONSE** topic on the Kafka bus.

**Info:** BAA Kafka bus subscription
The BAA microservice which contains the vOLTMF subscribes to the Kafka topic **OBBAA_ONU_RESPONSE** and forwards messages received on this topic to the  vOLTMF's onNotification() callback and processResponse() methods.

The BAA microservice which contains the vOLTMF subscribes to the Kafka topic **OBBAA_ONU_RESPONSE** and forwards messages received on this topic to the  vOLTMF's onNotification() callback and processResponse() methods.
An example response to the ONU DETECT event:

```
{
    "onu-name":"onu1",
    "event":"response",
    "olt-name":"olt1",
    "channel-termination-ref":"CT-2",
    "onu-id":"25",
	"labels":"{\"name\" : \"vendor\", \"value\": \"BBF\"}",
    "payload":"{\"identifier\":\"0\",\"operation\":\"detect\",\"data\":\"\",\"status\":\"OK\"}"
}
```

**Warning:** If the vOLTMF receives a NOK response for an ONU, the vOLTMF closes the MediatedDeviceNetconfSession for the ONU if it already exists.

If the ONU DETECT response\'s status is OK, the vOLTMF creates a new
MediatedDeviceNectonfSession and begins the process of obtaining
necessary information about the ONU and then aligning the ONU by sending
GET and COPY-CONFIG requests to the targeted ONU via the vOMCI
Proxy/function. The results of the alignment (i.e., COPY-CONFIG
response) is updated in the BAA layer\'s datastore.

Once the ONU detect sequence is successfully completed, a notification
is forwarded to the BAA NBI.

The diagram below depicts a successful ONU detection event:

<p align="center">
 <img width="800px" height="900px" src="{{site.url}}/architecture/voltmf/onu_detect_notif.png">
</p>

### ONU DETECT event process when ONU Authentication has failed<a name="authfailed"></a>

If the ONU authentication procedure fails, the vOLTMF retrieves
additional information from the ONU in order to help management systems
determine if the ONU is permitted to be attached to the network. The
vOLTMF does this by sending a GET request for the targeted ONU to the
vOMCI Proxy/function using the **OBBAA\_ONU\_REQUEST** and
**OBBAA\_ONU\_RESPONSE** topics on the Kafka bus. Once the successful
response to the GET request is received, the information about the ONU
is updated in the BAA datastore and a notification is sent through the
BAA layer NBI to interested management systems.

**Warning:** If the vOLTMF receives a NOK response for an ONU, the vOLTMF closes the MediatedDeviceNetconfSession for the ONU if it already exists.

The following diagram depicts the processing when an ONU fails
authentication:

<p align="center">
 <img width="800px" height="900px" src="{{site.url}}/architecture/voltmf/onu_detect_failed.png">
</p>

### ONU Undetection<a name="onuundetectnotif"></a>

Upon receiving of an onu-state-change notification from the OLT, the
vOLTManagementFunction determines if the if the onu-state-change
notification translates into an UNDETECT event as described above. When
an ONU UNDETECT event occurs, the vOLTMF retrieves the necessary ONU
identification information from the BAA layer\'s DeviceManager using the
onu-state-change\'s serialnumber and sends a ONU UNDETECT event to the
vOMCI Proxy/function using the OBBAA\_ONU\_NOTIFICATION topic on the
Kafka bus.

**Info:** If ONU device doesn\'t exist, corresponding error is logged.

An example of the ONU UNDETECT event is provided below:

```
{
    "onu-name": "onu1",
    "olt-name":"olt1",
    "channel-termination-ref":"CT-2",
    "onu-id":"25",
    "event": "undetect",
    "labels":"{\"name\" : \"vendor\", \"value\": \"BBF\"}",
    "payload": "{\"operation\":\"undetect\", \"identifier\":\"7\"}"   
}
```

Once the vOMCI function processes ONU UNDETECT event, the vOMCI
Proxy/function sends response to the event using
the **OBBAA\_ONU\_RESPONSE** topic on the Kafka bus. Upon a successful
response to the ONU UNDETECT event, a notification is forwarded to BAA
NBI along with information about the ONU including the ONU\'s current
state.

**Info:** BAA Kafka bus subscription
The BAA microservice which contains the vOLTMF subscribes to the Kafka topic **OBBAA_ONU_RESPONSE** and forwards messages received on this topic to the  vOLTMF's onNotification() callback and processResponse() methods.

An example response to the ONU UNDETECT event:

```
{
    "onu-name": "onu1",
    "event":"response",
    "olt-name":"olt1",
    "channel-termination-ref":"CT-2",
    "onu-id":"25",
    "labels":"{\"name\" : \"vendor\", \"value\": \"BBF\"}",    
    "payload": "{\"identifier\":\"7\",\"operation\":\"undetect\",\"data\":\"\",\"status\":\"OK\"
}
```

**Warning:** If the vOLTMF receives a NOK response for an ONU, the vOLTMF closes the MediatedDeviceNetconfSession for the ONU if it already exists.

The following diagram depicts the flow for the ONU UNDETECT event:

<p align="center">
 <img width="800px" height="900px" src="{{site.url}}/architecture/voltmf/onu_undetect_notif.png">
</p>

## ONU Request and Response Handling

Requests (e.g., COPY-CONFIG, EDIT-CONFIG, GET) that are to be processed by the ONU are handled by vOLTMF.
The vOLTMF refers to the YANG modules in the standard ONU adapter for the processing of the requests 
and the MediatedDeviceNetconfSession to process the request and response from the vOMCI Proxy/function 
using the **OBBAA\_ONU\_REQUEST** and **OBBAA\_ONU\_RESPONSE** topics on the Kafka bus.

The requests that the vOLTMF directs toward the ONU are encapsulated
within the \"data\" element of the request that is sent to vOMCI
Proxy/function as shown below:

```
{
  "onu-name": "onu1",
  "event": "request",
  "olt-name":"olt1",
  "onu-id":"25",
  "channel-termination-ref":"CT-2",
  "labels":"{\"name\" : \"vendor\", \"value\": \"BBF\"}",
  "payload": {
    "identifier": "2",
    "operation": "get",
    "filters": {
      "network-manager:root": {
        "nc-notifications:netconf": {}
      }
    },
    "data": ""
  }
}
```

The following diagram depicts how a request that is targeted to
an ONU is handled:

<p align="center">
 <img width="800px" height="700px" src="{{site.url}}/architecture/voltmf/onu_request.png">
</p>

[<--Architecture](../index.md#architecture)
