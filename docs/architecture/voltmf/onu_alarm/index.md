
<a id="onu_alarm" />

# ONU Alarm Handling

## Alarms from ONU to vOMCI (defined in G.988)

The ONU alarms are associated with a specific OMCI ME instance that may
have 0, 1 or multiple alarms (up to 224).

When an alarm changes state, the ONU sends a notification with the
current state of all the alarms that exist in the ME instance related to
the alarm that changed.

This means that in a notification sent by the ONU can be used to
identify one or more alarms that changed its state simultaneously with
several other alarms that kept the previous state. The OMCI notification
doesn\'t explicitly state which ONU alarm(s) have changed.

## vOMCI function handling of ONU alarms

The vOMCI function receives the ONU alarm\'s status then to send
notifications that reflect alarm changes. Because of this, the vOMCI
function is required to store the status of the ONU\'s alarms.

Everytime an alarm message is received from the ONU, the vOMCI function
checks all the ONU\'s alarms status received and verifies which alarms
have changed its state. Then, the notification with the alarms that
changed are sent to vOLTMF.

## Report ONU alarms to the vOLTMF

Alarms received from the ONU are forwarded to the vOLTMF using the topic
dedicated to NOTIFICATIONS. The alarm will be encoded using ietf-alarm
([RFC8632](https://datatracker.ietf.org/doc/html/rfc8632)) style
notifications.

Generic Sample Alarm Notification
```
Msg {
 header {
  msg_id: "1"
  sender_name: "vomci-vendor-1"
  recipient_name: "vOLTMF"
  object_type: ONU
  object_name: "ont1"
}
body {
  notification {    
   event_timestamp: "2022-01-09T13:53:36+00:00"
   data: <data>
  }
}
}
```

Where \<data\>

```
{
  "ietf-alarms:alarm-notification": {
    "resource": "/ietf-interfaces:interfaces/interface[name='eth0']",
    "alarm-type-id": "bbf-obbaa-ethernet-alarm-types:loss-of-signal",
    "alarm-type-qualifier": "",
    "time": "2022-01-09T13:53:36+00:00",
    "perceived-severity": "major",
    "alarm-text": "example alarm"
  }
}
```

## Implemented alarms types

[RFC8632](https://datatracker.ietf.org/doc/html/rfc8632) uses YANG
identities to identify the possible types of alarms that an entity can
generate. All alarm types must be derived from the ***alarm-type-id***
entity defined in *ietf-alarms.yang*.

**List of implemented alarms:**

|**Entity**|**Alarm**|**Alarm type ID entity**|
|PPTP Ethernet (ITU-T G.988 clause 9.5.1)|UNI LOS|bbf-obbaa-ethernet-alarm-types:loss-of-signal|
|ANI (ITU-T G.988 clause 9.2.1)|Low received optical power|bbf-hardware-transceiver-alarm-types:rx-power-low|
||High received optical power|bbf-hardware-transceiver-alarm-types:rx-power-high|
||SF (Signal Fail)|bbf-obbaa-xpon-onu-alarm-types:signal-fail|
||SD (Signal Degraded)|bbf-obbaa-xpon-onu-alarm-types:signal-degraded|
||Low transmit optical power|bbf-hardware-transceiver-alarm-types:tx-power-low|
||High transmit optical power|bbf-hardware-transceiver-alarm-types:tx-power-high|
||Laser bias current|bbf-hardware-transceiver-alarm-types:tx-bias-high|

At the time of implementation, there were no identities derived from
ietf-alarm:alarm-type-id which could be applicable to model some of
these alarms. As such the alarm identities used by OB-BAA are defined in
the OB-BAA specific modules (bbf-obbaa-ethernet-alarm-types and
bbf-obbaa-xpon-onu-alarm-types).

```
module bbf-obbaa-ethernet-alarm-types {
  yang-version 1.1;
  namespace "urn:bbf:yang:obbaa:ethernet-alarm-types";
  prefix bbf-baa-ethalt;

  import bbf-alarm-types {
    prefix bbf-alt;
  }

 organization
   "Broadband Forum <https://www.broadband-forum.org>";

 contact
   "Comments or questions about this Broadband Forum YANG module
    should be directed to <mailto:obbaa-leaders@broadband-forum.org>.
   ";

 description
   "This module contains a set of Ethernet alarm definitions that are
    applicable to OB-BAA.

    Copyright 2022 Broadband Forum

    Licensed under the Apache License, Version 2.0 (the \"License\");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an \"AS IS\" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.";


  revision 2022-01-31 {
    description
      "Initial revision.";
    reference
       "https://obbaa.broadband-forum.org/";
  }

  identity ethernet-alarm {
    base bbf-alt:bbf-alarm-type-id;
    description
      "Base identity for all Ethernet alarms.

       This identity is abstract and SHOULD NOT generally be used for
       alarms. If used to define an alarm that was not known at
       design time, it MUST be qualified with an alarm type qualifier
       string. This practice, however, should be generally avoided to
       ensure all possible alarms are known at design time.";
  }

  identity ethernet-interface-alarm {
    base ethernet-alarm;
    description
      "Base identity for all Ethernet interface alarms.

       This identity is abstract and SHOULD NOT generally be used for
       alarms. If used to define an alarm that was not known at
       design time, it MUST be qualified with an alarm type qualifier
       string. This practice, however, should be generally avoided to
       ensure all possible alarms are known at design time.";
  }

  /* Ethernet Interface Alarms */

  identity loss-of-signal {
    base ethernet-interface-alarm;
    description
      "A 'loss-of-signal' alarm is declared when a carrier
       is not detected at the interface.";
  }
```

```
module bbf-obbaa-xpon-onu-alarm-types {
  yang-version 1.1;
  namespace "urn:bbf:yang:obbaa:xpon-onu-alarm-types";
  prefix bbf-baa-xpononualt;

  import bbf-alarm-types {
    prefix bbf-alt;
  }

 organization
   "Broadband Forum <https://www.broadband-forum.org>";

 contact
   "Comments or questions about this Broadband Forum YANG module
    should be directed to <mailto:obbaa-leaders@broadband-forum.org>.
   ";

 description
   "This module contains a set of xPON alarm definitions that are
    applicable to OB-BAA.

    Copyright 2022 Broadband Forum

    Licensed under the Apache License, Version 2.0 (the \"License\");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an \"AS IS\" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.";


  revision 2022-01-31 {
    description
      "Initial revision.";
    reference
       "https://obbaa.broadband-forum.org/";
  }

  identity xpon-onu-alarms {
    base bbf-alt:bbf-alarm-type-id;
    description
      "Base identity for autonomous alarms reported by the Optical
       Network Unit (ONU) and that are specific to the xPON
       technology.

       This identity is abstract and SHOULD NOT generally be used for
       alarms. If used to define an alarm that was not known at
       design time, it MUST be qualified with an alarm type qualifier
       string. This practice, however, should be generally avoided to
       ensure all possible alarms are known at design time.";
    reference
      "ITU-T G.988 (11/2017) 7.2";
  }

  identity signal-fail {
    base xpon-onu-alarms;
    base bbf-alt:bbf-threshold-crossing-alarm-type-id;
    description
      "A 'signal-fail' alarm is declared when the downstream Bit
       Error Rate (BER) becomes >= a configurable value.";
    reference
      "ITU-T G.988 (11/2017) 9.2.1";
  }

  identity signal-degraded {
    base xpon-onu-alarms;
    base bbf-alt:bbf-threshold-crossing-alarm-type-id;
    description
      "A 'signal-degraded' alarm is declared when the downstream Bit
       Error Rate (BER) becomes >= a configurable value which must
       be lower than the 'signal-fail' threshold.";
    reference
      "ITU-T G.988 (11/2017) 9.2.1";
  }

}
```

## Sample notifications in the MvOLTMF-vOMCI interface 

**Sample UNI LOS Alarm**

```
{
  "ietf-alarms:alarm-notification": {
    "resource": "/ietf-interfaces:interfaces/interface[name='enet_uni_ont1_1_1']",
    "alarm-type-id": "bbf-obbaa-ethernet-alarm-types:loss-of-signal",
    "alarm-type-qualifier": "",
    "time": "2022-01-09T13:53:36+00:00",
    "perceived-severity": "major",
    "alarm-text": "Loss of signal detected."
  }
}
```

**Sample UNI LOS Alarm Clear**

```
{
  "ietf-alarms:alarm-notification": {
    "resource": "/ietf-interfaces:interfaces/interface[name='enet_uni_ont1_1_1']",
    "alarm-type-id": "bbf-obbaa-ethernet-alarm-types:loss-of-signal",
    "alarm-type-qualifier": "",
    "time": "2022-01-09T13:53:36+00:00",
    "perceived-severity": "cleared",
    "alarm-text": "Loss of signal cleared."
  }
}
```

**Sample ANI SF Alarm**

```
{
  "ietf-alarms:alarm-notification": {
    "resource": "/ietf-interfaces:interfaces/interface[name='ontAni_ont1']",
    "alarm-type-id": "bbf-obbaa-xpon-onu-alarm-types:signal-fail",
    "alarm-type-qualifier": "",
    "time": "2022-01-09T13:53:36+00:00",
    "perceived-severity": "major",
    "alarm-text": "Signal Fail detected."
  }
}
```

**Sample ANI SF Alarm Clear**

```
{
  "ietf-alarms:alarm-notification": {
    "resource": "/ietf-interfaces:interfaces/interface[name='ontAni_ont1']",
    "alarm-type-id": "bbf-obbaa-xpon-onu-alarm-types:signal-fail",
    "alarm-type-qualifier": "",
    "time": "2022-01-09T13:53:36+00:00",
    "perceived-severity": "cleared",
    "alarm-text": "Signal Fail cleared."
  }
}
```

**Sample ANI SD Alarm**

```
{
  "ietf-alarms:alarm-notification": {
    "resource": "/ietf-interfaces:interfaces/interface[name='ontAni_ont1']",
    "alarm-type-id": "bbf-obbaa-xpon-onu-alarm-types:signal-degraded",
    "alarm-type-qualifier": "",
    "time": "2022-01-09T13:53:36+00:00",
    "perceived-severity": "major",
    "alarm-text": "Signal Degraded detected."
  }
}
```

**Sample ANI SD Alarm Clear**

```
{
  "ietf-alarms:alarm-notification": {
    "resource": "/ietf-interfaces:interfaces/interface[name='ontAni_ont1']",
    "alarm-type-id": "bbf-obbaa-xpon-onu-alarm-types:signal-degraded",
    "alarm-type-qualifier": "",
    "time": "2022-01-09T13:53:36+00:00",
    "perceived-severity": "cleared",
    "alarm-text": "Signal Degraded cleared."
  }
}
```

**Sample ANI RX Power Low Alarm**

```
{
  "ietf-alarms:alarm-notification": {
    "resource": "/ietf-hardware:hardware/component[name='ontAniPort_ont1']",
    "alarm-type-id": "bbf-hardware-transceiver-alarm-types:rx-power-low",
    "alarm-type-qualifier": "",
    "time": "2022-01-09T13:53:36+00:00",
    "perceived-severity": "major",
    "alarm-text": "Low receive (RX) input power detected."
  }
}
```

**Sample ANI RX Power Low Clear**

```
{
  "ietf-alarms:alarm-notification": {
    "resource": "/ietf-hardware:hardware/component[name='ontAniPort_ont1']",
    "alarm-type-id": "bbf-hardware-transceiver-alarm-types:rx-power-low",
    "alarm-type-qualifier": "",
    "time": "2022-01-09T13:53:36+00:00",
    "perceived-severity": "cleared",
    "alarm-text": "Low receive (RX) input power cleared."
  }
}
```

**Sample ANI RX Power High Alarm**

```
{
  "ietf-alarms:alarm-notification": {
    "resource": "/ietf-hardware:hardware/component[name='ontAniPort_ont1']",
    "alarm-type-id": "bbf-hardware-transceiver-alarm-types:rx-power-high",
    "alarm-type-qualifier": "",
    "time": "2022-01-09T13:53:36+00:00",
    "perceived-severity": "major",
    "alarm-text": "High receive (RX) input power detected."
  }
}
```

**Sample ANI RX Power High Clear**

```
{
  "ietf-alarms:alarm-notification": {
    "resource": "/ietf-hardware:hardware/component[name='ontAniPort_ont1']",
    "alarm-type-id": "bbf-hardware-transceiver-alarm-types:rx-power-high",
    "alarm-type-qualifier": "",
    "time": "2022-01-09T13:53:36+00:00",
    "perceived-severity": "cleared",
    "alarm-text": "High receive (RX) input power cleared."
  }
}
```

**Sample ANI TX Power Low Alarm**

```
{
  "ietf-alarms:alarm-notification": {
    "resource": "/ietf-hardware:hardware/component[name='ontAniPort_ont1']",
    "alarm-type-id": "bbf-hardware-transceiver-alarm-types:tx-power-low",
    "alarm-type-qualifier": "",
    "time": "2022-01-09T13:53:36+00:00",
    "perceived-severity": "major",
    "alarm-text": "Low transmit (TX) input power detected."
  }
}
```

**Sample ANI TX Power Low Clear**

```
{
  "ietf-alarms:alarm-notification": {
    "resource": "/ietf-hardware:hardware/component[name='ontAniPort_ont1']",
    "alarm-type-id": "bbf-hardware-transceiver-alarm-types:tx-power-low",
    "alarm-type-qualifier": "",
    "time": "2022-01-09T13:53:36+00:00",
    "perceived-severity": "cleared",
    "alarm-text": "Low transmit (TX) input power cleared."
  }
}
```

**Sample ANI TX Power High Alarm**

```
{
  "ietf-alarms:alarm-notification": {
    "resource": "/ietf-hardware:hardware/component[name='ontAniPort_ont1']",
    "alarm-type-id": "bbf-hardware-transceiver-alarm-types:tx-power-high",
    "alarm-type-qualifier": "",
    "time": "2022-01-09T13:53:36+00:00",
    "perceived-severity": "major",
    "alarm-text": "High transmit (TX) input power detected."
  }
}
```

**Sample ANI TX Power High Clear**

```
{
  "ietf-alarms:alarm-notification": {
    "resource": "/ietf-hardware:hardware/component[name='ontAniPort_ont1']",
    "alarm-type-id": "bbf-hardware-transceiver-alarm-types:tx-power-high",
    "alarm-type-qualifier": "",
    "time": "2022-01-09T13:53:36+00:00",
    "perceived-severity": "cleared",
    "alarm-text": "High transmit (TX) input power cleared."
  }
}
```

**Sample ANI TX Bias High Alarm**

```
{
  "ietf-alarms:alarm-notification": {
    "resource": "/ietf-hardware:hardware/component[name='ontAniPort_ont1']",
    "alarm-type-id": "bbf-hardware-transceiver-alarm-types:tx-bias-high",
    "alarm-type-qualifier": "",
    "time": "2022-01-09T13:53:36+00:00",
    "perceived-severity": "major",
    "alarm-text": "High transmit (TX) bias current detected."
  }
}
```

**Sample ANI TX Bias High Clear**

```
{
  "ietf-alarms:alarm-notification": {
    "resource": "/ietf-hardware:hardware/component[name='ontAniPort_ont1']",
    "alarm-type-id": "bbf-hardware-transceiver-alarm-types:tx-bias-high",
    "alarm-type-qualifier": "",
    "time": "2022-01-09T13:53:36+00:00",
    "perceived-severity": "cleared",
    "alarm-text": "High transmit (TX) bias current cleared."
  }
}
```

[<--vOLTMF](../index.md#voltmf)
