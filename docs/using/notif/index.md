
<a id="notif" />

Notifications in the BAA Layer
===============================
This topic provides information regarding how to use NETCONF event
notifications in the BAA layer as defined in IETF RFC 5277,
specifically:

-   How a SDN M&C can subscribe to an event notification including
    subscriptions to the Alarm stream

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

Create a Subscription for an Alarm Notification
===============================================

NETCONF alarm notifications are subscribed to by subscribing to the
ALARM stream. The SDN M&C can select specific alarms(e.g., resource,
severity) by applying subtree filtering according to rules defined in
IETF RFC 5277.

Below is an example of an alarm subscription that receives all major and
critical alarms, all alarms from interface eth0 of device A and
additionally link-down alarms from eth1 from deviceA.

**Create Alarm Subscription**

```
<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="">
  <create-subscription xmlns="urn:ietf:params:xml:ns:netconf:notification:1.0">
    <stream>ALARM</stream>
    <filter type="subtree">
         <alarm-notification xmlns="urn:ietf:params:xml:ns:yang:ietf-alarms">
              <resource xmlns:baa-network-manager="urn:bbf:yang:obbaa:network-manager" xmlns:if="urn:ietf:params:xml:ns:yang:ietf-interfaces">baa-network-manager:network-manager/baa-network-manager:managed-devices/baa-network-manager:device[baa-network-manager:name='deviceA']/baa-network-manager:root/if:interfaces/if:interface[if:name='eth.0']</resource>
         </alarm-notification>

         <alarm-notification xmlns="urn:ietf:params:xml:ns:yang:ietf-alarms">
              <resource xmlns:baa-network-manager="urn:bbf:yang:obbaa:network-manager" xmlns:if="urn:ietf:params:xml:ns:yang:ietf-interfaces">baa-network-manager:network-manager/baa-network-manager:managed-devices/baa-network-manager:device[baa-network-manager:name='deviceA']/baa-network-manager:root/if:interfaces/if:interface[if:name='eth.1']</resource>
              <alarm-type-id
                 xmlns:vendor-alarms="urn:vendor:vendor-alarms">vendor-alarms:link-down</alarm-type-id>
         </alarm-notification>


         <alarm-notification xmlns="urn:ietf:params:xml:ns:yang:ietf-alarms">
              <perceived-severity>major</perceived-severity>
         </alarm-notification>

         <alarm-notification xmlns="urn:ietf:params:xml:ns:yang:ietf-alarms">
              <perceived-severity>critical</perceived-severity>
         </alarm-notification>


    </filter>
  </create-subscription>
</rpc>
```

In the following table we can see which notifications that would be received accordingly to the filter:

| Interface | Severity | Alarm | Should be | Rule |
| :--- | :--- | :---: | :---: |:--- |
|eth0|minor|los|yes|Receive all alarms from eth0|
|eth0|major|link-down|yes|Receive all alarms from eth0|
|eth0|major|sfp-fail|yes|Receive all alarms from eth0|
|eth1|minor|los|no|Don\'t receive minor alarms by default|
|eth1|minor|link-down|yes|Receive link-down alarm from eth1|
|eth1|major|sfp-fail|yes|Receive all major alarms|
|eth2|minor|los|no|Don\'t receive minor alarms by default|
|eth2|major|link-down|yes|Receive all major alarms by default|
|eth2|minor|sfp-fail|yes|Don\'t receive minor alarms by default|

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

[<--Using OB-BAA](../index.md#using)
