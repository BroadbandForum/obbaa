
<a id="mda" />

Monitoring Operations for Device Adapters
==================================================

Introduction
------------

When an instance of a device is instantiated in the BAA layer, 
the device uses a Vendor Device Adapter (VDA) and is required to be associated 
with a Standard Device Adapter (SDA) for that type of device (e.g., OLT, DPU).

To assist in maintaining the Device Adapters in the BAA layer, this section
describes commands that can assist in monitoring the library of the
Device Adapters, including finding out what:

-   Device Adapters exist and their corresponding attributes of the Device Adapter

-   YANG modules are associated with a Device Adapter

-   YANG modules used by a device

Looking up Information Associated with Device Adapters
-------------------------------------------

This command provides the capability to lookup the information about
the Device Adapters (i.e., SDA, VDA) that have been deployed into the BAA layer.

**Request to lookup information about Device Adapters**
```
<?xml version="1.0" encoding="UTF-8"?>
<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="1543910289808">
  <get>
    <filter type="subtree">
      <network-manager xmlns="urn:bbf:yang:obbaa:network-manager">
        <device-adapters/>
      </network-manager>
    </filter>
  </get>
</rpc>
```

**Response for Device Adapter lookup**
```
<rpc-reply message-id="1543910289808" xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
  <data>
    <baa-network-manager:network-manager xmlns:baa-network-manager="urn:bbf:yang:obbaa:network-manager">
      <device-adapters xmlns="urn:bbf:yang:obbaa:network-manager">
        <device-adapter-count>3</device-adapter-count>
        <device-adapter>
          <type>DPU</type>
          <interface-version>1.0</interface-version>
          <model>standard</model>
          <vendor>BBF</vendor>
          <description>This is an adapter for standard.DPU provided by BBF</description>
          <developer>BBF</developer>
          <revision>2019-01-01T00:00:00Z</revision>
          <upload-date>2019-01-31T12:20:52.433Z</upload-date>
          <in-use>false</in-use>
          <yang-modules>
            <module>
              <name>bbf-availability</name>
              <revision>2018-07-13</revision>
            </module>
            <module>
              <name>bbf-dot1q-types</name>
              <revision>2018-07-13</revision>
            </module>
            <module>
              <name>bbf-fast</name>
              <revision>2018-10-01</revision>
            </module>
            <module>
              <name>bbf-fastdsl</name>
              <revision>2018-10-01</revision>
            </module>


			...........


 		  </yang-modules>
        </device-adapter>
      </device-adapters>
    </baa-network-manager:network-manager>
  </data>
</rpc-reply>
```

Monitoring  Device Adapter
------------------

When monitoring a Device Adapter, there are several questions that are typically
asked about the Device Adapter:

-   Is the Device Adapter in use and what devices are currently using the Device Adapter?

-   What YANG modules use which Device Adapter and which devices currently use that
    Device Adapter?

-   What devices use which YANG modules?

### Retrieving Information About the Device Adapter\'s Use

For deployed Device Adapters, the BAA layer provides the capability to check:

-   If a Device Adapter currently has devices that use the Device Adapter. If at least one
    device is using the Device Adapter, then the Device Adapter is considered to be in use.

-   What devices (pANs) are currently associated with a Device Adapter

**Retrieve information about the Device Adapter's use**
```
<?xml version="1.0" encoding="UTF-8"?>
<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="1548128755928">
  <get>
    <filter type="subtree">
      <network-manager xmlns="urn:bbf:yang:obbaa:network-manager">
        <device-adapters>
          <device-adapter-count />
          <device-adapter>
            <type />
            <interface-version />
            <model />
            <vendor />
            <description />
            <upload-date />
            <developer />
            <revision/>
            <push-pma-configuration-to-device />
            <in-use />
            <devices-related />
          </device-adapter>
        </device-adapters>
      </network-manager>
    </filter>
  </get>
</rpc>
```

**Response to Device Adapter use**
```
<rpc-reply message-id="1548128755928" xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
  <data>
    <baa-network-manager:network-manager xmlns:baa-network-manager="urn:bbf:yang:obbaa:network-manager">
      <device-adapters xmlns="urn:bbf:yang:obbaa:network-manager">
        <device-adapter>
          <type>OLT</type>
          <interface-version>1.0</interface-version>
          <model>standard</model>
          <vendor>BBF</vendor>
          <description>This is an adapter for standard.OLT provided by BBF</description>
          <upload-date>2019-01-31T12:20:55.193Z</upload-date>
          <developer>BBF</developer>
          <revision>2019-01-01T00:00:00Z</revision>
          <in-use>true</in-use>
          <devices-related>
            <device-count>1</device-count>
            <device>deviceA</device>
          </devices-related>
        </device-adapter>
    </baa-network-manager:network-manager>
  </data>
</rpc-reply>
```

### Retrieving Information About a YANG module\'s Use

In the operation of the BAA layer, YANG modules are used by many devices
in the context of an Device Adapter.

This command retrieves the following information about YANG modules;

-   If the YANG module is currently in use by at least 1 device instance

-   What Device Adapter is associated with the YANG module along with the devices
    that use the Device Adapter

**Retrieving information about the YANG module's use**
```
<?xml version="1.0" encoding="UTF-8"?>
<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="1542612595090">
  <get>
    <filter type="subtree">
      <in-use-library-modules xmlns="urn:bbf:yang:obbaa:module-library-check" />
    </filter>
  </get>
</rpc>
```

**Response to retrieve YANG module information**
```
<rpc-reply message-id="1542612595090" xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
  <data>
    <in-use-library-modules xmlns="urn:bbf:yang:obbaa:module-library-check">
      <module>
        <name>iana-hardware</name>
        <revision>2018-03-13</revision>
        <associated-adapters>
          <device-adapter>
            <vendor>BBF</vendor>
            <model>standard</model>
            <type>OLT</type>
            <interface-version>1.0</interface-version>
            <device-count>1</device-count>
            <devices>
              <device>deviceA</device>
            </devices>
          </device-adapter>
        </associated-adapters>
      </module>
      <module>
        <name>ietf-hardware</name>
        <revision>2017-03-07</revision>
        <associated-adapters>
          <device-adapter>
            <vendor>BBF</vendor>
            <model>standard</model>
            <type>OLT</type>
            <interface-version>1.0</interface-version>
            <device-count>1</device-count>
            <devices>
              <device>deviceA</device>
            </devices>
          </device-adapter>
        </associated-adapters>
      </module>
    </in-use-library-modules>
  </data>
</rpc-reply>
```

**Retrieve YANG module information for a specific module**
```
<?xml version="1.0" encoding="UTF-8"?>
<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="1543477791934">
  <get>
    <filter type="subtree">
      <in-use-library-modules xmlns="urn:bbf:yang:obbaa:module-library-check">
        <module>
           <name>iana-hardware</name>
           <revision>2018-03-13</revision>
        </module>
      </in-use-library-modules>
    </filter>
  </get>
</rpc>
```

**Response for retrieving YANG module information for a specific module**
```
<rpc-reply message-id="1543477791934" xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
  <data>
    <in-use-library-modules xmlns="urn:bbf:yang:obbaa:module-library-check">
      <module>
        <name>iana-hardware</name>
        <revision>2018-03-13</revision>
        <associated-adapters>
          <device-adapter>
            <vendor>BBF</vendor>
            <model>standard</model>
            <type>OLT</type>
            <interface-version>1.0</interface-version>
            <device-count>1</device-count>
            <devices>
              <device>deviceA</device>
            </devices>
          </device-adapter>
        </associated-adapters>
      </module>
    </in-use-library-modules>
  </data>
</rpc-reply>
```

### Retrieving Information About the Device\'s Use

In the BAA layer, a device instance uses many different YANG modules in
the context of a Device Adapter.

This command retrieves information about the device\'s use with the BAA
layer of:

-   The Device Adapter associated with the device instance

-   The YANG modules used by the device instance

**Retrieving information about the Device\'s use**
```

<?xml version="1.0" encoding="UTF-8"?>
<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="1527307907169">
<get xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
  <filter type="subtree">
    <network-manager xmlns="urn:bbf:yang:obbaa:network-manager">
      <managed-devices>
        <device>
          <name>deviceA</name>
          <root>
            <device-used-yang-modules xmlns="urn:bbf:yang:obbaa:module-library-check" />
          </root>
        </device>
      </managed-devices>
    </network-manager>
  </filter>
</get>
</rpc>
```

**Response to retrieve of device information**
```
<rpc-reply message-id="1527307907169" xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
  <data>
    <managed-devices xmlns="urn:bbf:yang:obbaa:network-manager">
      <device>
        <name>deviceA</name>
        <root>
          <device-library-modules xmlns="urn:bbf:yang:obbaa:module-library-check">
            <related-adapter>
              <vendor>BBF</vendor>
              <model>standard</model>
              <type>OLT</type>
              <interface-version>1.0</interface-version>
            </related-adapter>
            <in-use-library-modules>
              <module>
                <name>iana-hardware</name>
                <revision>2018-03-13</revision>
              </module>
              <module>
                <name>ietf-hardware</name>
                <revision>2017-03-07</revision>
              </module>
            </in-use-library-modules>
          </device-library-modules>
        </root>
      </device>
    </managed-devices>
  </data>
</rpc-reply>
```
  
[<--Using OB-BAA](../index.md#using)
  