
<a id="notif" />

Notifications in the BAA Layer
===============================
This topic provides information regarding how to use NETCONF event notifications in the BAA layer, specifically:

-   How a SDN M&C can subscribe to an event notification

-   What the event notification looks like

Create a Subscription for an Event Notification
-----------------------------------------------

In order to receive an event notification, SDN M&C clients have to first subscribe to a specific event stream to receive the notifications from that stream as shown in the following command:

**Create subscription request**
```
<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="1">
    <create-subscription xmlns="urn:ietf:params:xml:ns:netconf:notification:1.0">
        <stream>NETCONF</stream>
    </create-subscription>
</rpc>
```

If the subscription was successful, the BAA layer responds with an \<ok-response\>.

To see what event streams are supported, the following request can be issued:

**Get event streams request**
```
<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="2">
    <get>
        <filter>
            <manageEvent:netconf xmlns:manageEvent="urn:ietf:params:xml:ns:netmod:notification" />
        </filter>
    </get>
</rpc>
```

and the response will be something similar like below:

**Get event streams response**
```
<?xml version="1.0" encoding="utf-8"?>
<data xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
  <netconf xmlns="urn:ietf:params:xml:ns:netmod:notification">
    <manageEvent:streams xmlns:manageEvent="urn:ietf:params:xml:ns:netmod:notification">
      <manageEvent:stream>
        <manageEvent:name>NETCONF</manageEvent:name>
        <manageEvent:description>default NETCONF event stream</manageEvent:description>
        <manageEvent:replaySupport>true</manageEvent:replaySupport>
        <manageEvent:replayLogCreationTime>2019-02-11T09:34:17.691+00:00</manageEvent:replayLogCreationTime>
      </manageEvent:stream>
      <manageEvent:stream>
        <manageEvent:name>ALARM</manageEvent:name>
        <manageEvent:description>ALARM event stream</manageEvent:description>
        <manageEvent:replaySupport>true</manageEvent:replaySupport>
        <manageEvent:replayLogCreationTime>2019-02-11T09:34:17.741+00:00</manageEvent:replayLogCreationTime>
      </manageEvent:stream>
      <manageEvent:stream>
        <manageEvent:name>CONFIG_CHANGE</manageEvent:name>
        <manageEvent:description>CONFIGURATION CHANGES event stream</manageEvent:description>
        <manageEvent:replaySupport>true</manageEvent:replaySupport>
        <manageEvent:replayLogCreationTime>2019-02-11T09:34:17.741+00:00</manageEvent:replayLogCreationTime>
      </manageEvent:stream>
      <manageEvent:stream>
        <manageEvent:name>STATE_CHANGE</manageEvent:name>
        <manageEvent:description>STATE CHANGES event stream</manageEvent:description>
        <manageEvent:replaySupport>true</manageEvent:replaySupport>
        <manageEvent:replayLogCreationTime>2019-02-11T09:34:17.741+00:00</manageEvent:replayLogCreationTime>
      </manageEvent:stream>
      <manageEvent:stream>
        <manageEvent:name>SYSTEM</manageEvent:name>
        <manageEvent:description>System level notifications of the NETCONF server</manageEvent:description>
        <manageEvent:replaySupport>true</manageEvent:replaySupport>
        <manageEvent:replayLogCreationTime>2019-02-11T09:34:17.741+00:00</manageEvent:replayLogCreationTime>
      </manageEvent:stream>
      <manageEvent:stream>
        <manageEvent:name>HA</manageEvent:name>
        <manageEvent:description>High Availability event stream</manageEvent:description>
        <manageEvent:replaySupport>true</manageEvent:replaySupport>
        <manageEvent:replayLogCreationTime>2019-02-11T09:34:17.741+00:00</manageEvent:replayLogCreationTime>
      </manageEvent:stream>
    </manageEvent:streams>
  </netconf>
</data>
```

## Example Event Notifications

The following are examples of typical event notifications that get emitted through the BAA layer.

**Example of a Configuration change notification**
```
<notification xmlns="urn:ietf:params:xml:ns:netconf:notification:1.0">
  <eventTime>2019-02-11T09:44:47+00:00</eventTime>
  <netconf-config-change xmlns="urn:ietf:params:xml:ns:yang:ietf-netconf-notifications">
    <datastore>running</datastore>
    <changed-by>
      <username>PMA_USER</username>
      <session-id>1</session-id>
      <source-host/>
    </changed-by>
    <edit>
      <target xmlns:baa-network-manager="urn:bbf:yang:obbaa:network-manager"
xmlns:if="urn:ietf:params:xml:ns:yang:ietf-interfaces">/baa-network-manager:network-manager/baa-network-manager:managed-devices/baa-network-manager:device[baa-network-manager:name='deviceB']/baa-network-manager:root/if:interfaces/if:interface[if:name='interfaceB']</target>
      <operation>create</operation>
    </edit>
  </netconf-config-change>
</notification>
```

**Example of a YANG library change notification**
```
<notification xmlns="urn:ietf:params:xml:ns:netconf:notification:1.0">
  <eventTime>2019-02-11T09:34:18+00:00</eventTime>
  <yanglib:yang-library-change xmlns:yanglib="urn:ietf:params:xml:ns:yang:ietf-yang-library">
    <yanglib:module-set-id>70162176d0a783152aaff5c443facfde57985c998877458fc7b7d9392c20d59a</yanglib:module-set-id>
  </yanglib:yang-library-change>
</notification>
```

**Example of a device state change notification**
```
<notification xmlns="urn:ietf:params:xml:ns:netconf:notification:1.0">
  <eventTime>2019-02-11T09:47:59+00:00</eventTime>
  <network-manager xmlns="urn:bbf:yang:obbaa:network-manager">
    <managed-devices>
      <device>
        <name>deviceB</name>
        <device-notification>
          <device-state-change>
            <event>online</event>
          </device-state-change>
        </device-notification>
      </device>
    </managed-devices>
  </network-manager>
</notification>
```

## Example Alarm Notification

If a user/operator wants to receive alarm notifications at NBI,
operator/administrator should be subscribed to ALARM stream to receive
only alarm notification. Note: If the subscription was successful, the
BAA layer responds with an \<ok-response\>.

**Subscribe to the alarm stream**

```
<create-subscription xmlns="urn:ietf:params:xml:ns:netconf:notification:1.0">
  <stream>ALARM</stream>
</create-subscription>
```

Example of notification received at NBI:

**Device\'s alarm-notification**

```
<notification xmlns="urn:ietf:params:xml:ns:netconf:notification:1.0">
  <eventTime>2019-07-19T06:09:40+00:00</eventTime>
  <alarms:alarm-notification xmlns:alarms="urn:ietf:params:xml:ns:yang:ietf-alarms">
    <alarms:alarm>
      <alarms:resource xmlns:baa-network-manager="urn:bbf:yang:obbaa:network-manager"
xmlns:if="urn:ietf:params:xml:ns:yang:ietf-interfaces">baa-network-manager:network-manager/baa-network-manager:managed-devices/baa-network-manager:device[baa-network-manager:name='DPU1-callhome']/baa-network-manager:root/if:interfaces/if:interface[if:name='xdsl-line:1/1/1/1']</alarms:resource>
      <alarms:alarm-type-id xmlns:sample-al="urn:broadband-forum-org:yang:sample-dpu-alarm-types">sample-al:alarm-type1</alarms:alarm-type-id>
      <alarms:alarm-type-qualifier/>
      <alarms:time>2019-07-19T06:09:40.000Z</alarms:time>
      <alarms:perceived-severity>minor</alarms:perceived-severity>
      <alarms:alarm-text>raisealarm</alarms:alarm-text>
    </alarms:alarm>
  </alarms:alarm-notification>
</notification>
```

## Example ONU state change notification

For an ONU state change onu-state-change) notification, the stream used is STATE_CHANGE.

**Subscribe to the state change stream**
```
<create-subscription xmlns="urn:ietf:params:xml:ns:netconf:notification:1.0">
  <stream>STATE_CHANGE</stream>
</create-subscription>
```

Example of notification received at NBI:

**ONU state change notification received at the NBI**

```
<notification xmlns="urn:ietf:params:xml:ns:netconf:notification:1.0">
  <eventTime>2019-07-25T05:53:36+00:00</eventTime>
  <bbf-xpon-onu-states:onu-state-change xmlns:bbf-xpon-onu-states="urn:bbf:yang:bbf-xpon-onu-states">
    <bbf-xpon-onu-states:detected-serial-number>ABCD0P5S6T7Y</bbf-xpon-onu-states:detected-serial-number>
    <bbf-xpon-onu-states:channel-termination-ref>Int1</bbf-xpon-onu-states:channel-termination-ref>
    <bbf-xpon-onu-states:onu-state-last-change>2019-07-25T05:51:36+00:00</bbf-xpon-onu-states:onu-state-last-change>
    <bbf-xpon-onu-states:onu-state xmlns:bbf-xpon-onu-types="urn:bbf:yang:bbf-xpon-more-types">bbf-xpon-onu-types:onu-present</bbf-xpon-onu-states:onu-state>
    <bbf-xpon-onu-states:onu-id>25</bbf-xpon-onu-states:onu-id>
    <bbf-xpon-onu-states:detected-registration-id>device1-slot1-port1</bbf-xpon-onu-states:detected-registration-id>
  </bbf-xpon-onu-states:onu-state-change>
</notification>

```

**Note: The detected-registration-id is in the format: OltName-OltSlot-OltPort. The value is auto-generated by OB-BAA framework using the device's datastore configuration to the slot and port corresponding to the channel-termination-ref received in the notification.**

## Notification support in OB-BAA 
-	OB-BAA supports only the ietf-alarm structure, if the device supports any other alarm structure, 
the vendor has to convert/normalize their specific alarm notifications to IETF alarm structure which is defined in ietf-alarms@2018-11-22.yang.
-	OB-BAA currently supports notifications of type alarm-notification (from ietf-alarms) and onu-state-change (from bbf-xpon-onu-states) notification callback. Other notification types received by OB-BAA are not processed..

<p align="center">
 <img width="400px" height="280px" src="{{site.url}}/using/notif/notif_interface.png">
</p>
<p align="center">
 <img width="400px" height="600px" src="{{site.url}}/using/notif/onu_state_change_notification.png">
</p>

[<--Using OB-BAA](../index.md#using)
