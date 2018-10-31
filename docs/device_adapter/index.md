Device Adapter Component
========

<a id="device_adapter" />

The BAA layer provides a \"Device Adapter\" software component that permits devices
that are connected through the Southbound interface to be adapted to a
common representation for that type of device (e.g., DPU, OLT).

In this release, the Device Adapter component provides the following
functions:

-   Life-cycle management of a device Adapter

-   A hot pluggable interface to connect instances of device adapters to
    the BAA layer

-   Receive requests from PMA registry that is translated to device
    specific request and then use the supported protocol specific
    Connection Manager to deploy the instruction into the managed device

-   Device adapters provide the common representation used by the SAI.
    In future releases, this will be enhanced to use the common
    representation for that type of device

Life-cycle Management and Deployment of a Device Adapter
========================================================

The BAA layer provides the capability to deploy (plug) and undeploy
(unplug) device adapters. The device adapters are comprised of the
following artificats:

-   Device adapter description that contains information about the
    Device adapter\'s properties and capabilities. An instance of a
    Device adapter is uniquely identified by the vendor, model and
    interfaceVersion properties.

-   YANG modules that represent the type of device to which the
    connected device is adapted

The instance of a Device adapter is structured with the above artificats
as \".zip\" file that contains a directory named \"yang\" which includes
the YANG modules and a \"model\" directory that contains the Device
adapter description file encoded as an \".xml\" document.

Device Adapter Description
--------------------------

A device adapter is described using the \"adapter.xsd\" xml schema as
defined below:

```
<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema"
            elementFormDefault="qualified"
            targetNamespace="http://www.bbf.org/obbaa/schemas/adapter/1.0"
            xmlns:adp="http://www.bbf.org/obbaa/schemas/adapter/1.0">

    <xsd:element name="Adapter" type="adp:Adapter"/>

    <xsd:complexType name="Adapter">
        <xsd:all>
            <xsd:element name="capabilities" type="adp:capabilities"/>
        </xsd:all>
        <xsd:attribute name="type" type="xsd:string" use="optional">
           <xs:annotation>
               <xs:documentation>The BAA layer SAI device type to which devices that use this adapter are translated.</xs:documentation>
           </xs:annotation>
        </xsd:attribute>
        <xsd:attribute name="interfaceVersion" type="xsd:string" use="optional">
           <xs:annotation>
               <xs:documentation>The interface version of the device that this adapter would utilize.</xs:documentation>
           </xs:annotation>
        </xsd:attribute>
        <xsd:attribute name="model" type="xsd:string" use="optional">
           <xs:annotation>
               <xs:documentation>The model of the device that this adapter would support.</xs:documentation>
           </xs:annotation>
        </xsd:attribute>
        <xsd:attribute name="vendor" type="xsd:string" use="optional">
           <xs:annotation>
               <xs:documentation>The vendor of the device that this adapter would support.</xs:documentation>
           </xs:annotation>
        </xsd:attribute>
    </xsd:complexType>

    <xsd:complexType name="capabilities">
       <xs:annotation>
           <xs:documentation>The YANG modules that comprise this type of device in the BAA layer SAI.</xs:documentation>
        </xs:annotation>
        <xsd:sequence>
            <xsd:element name="value" type="xsd:string" minOccurs="0" maxOccurs="unbounded"/>
        </xsd:sequence>
    </xsd:complexType>
    
</xsd:schema>
```

How to Prepare a Device Adapter
-------------------------------

A device adapter is represented as set of artificats that are contained
within a \".zip\" file that can be deployed within the BAA layer.

The following steps can be used prepare a device adapter for deployment:

-   Create a directory named \"yang\" and place the YANG modules for the
    device type that the device adapter represents into the directory

-   Define the device adapter\'s properties and capabilities

-   Assemble the device adapter for deployment

### Adding YANG Modules that Represent the Device Type

Create a directory named \"yang\' and place the YANG modules that
represent the device type within the directory.

Note : After placing the YANG modules in the directory, it is good
practice to validate the YANG modules for consistency using pyang

Execute \> pyang \*.yang inside the folder and fix the errors if any in
the YANG modules.

<p align="center">
 <img width="800px" height="40px" src="{{site.url}}/device_adapter/da_yang.jpeg">
</p>

### Define the Device Adapter Model

Create a directory named \"model\" and add the adapter.xml that
represents the type of device supported by the device adapter. For
example:

```
<?xml version="1.0" encoding="UTF-8"?>
<Adapter type="ADAPTER1" interfaceVersion="1.0" model="4LT" vendor="VENDOR1"
         xmlns="http://www.bbf.org/obbaa/schemas/adapter/1.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://www.bbf.org/obbaa/schemas/adapter/1.0
                    ../../../../../adapter-schema-fwk/src/main/xsd/adapter.xsd">

    <capabilities>
        <value>urn:ietf:params:netconf:base:1.0</value>
        <value>urn:ietf:params:netconf:base:1.1</value>
        <value>urn:ietf:params:netconf:capability:writable-running:1.0</value>
        <value>urn:ietf:params:netconf:capability:notification:1.0</value>
        <value>urn:ietf:params:netconf:capability:interleave:1.0</value>
    </capabilities>

</Adapter>
```

### Assemble the Device Adapter

Zip the created directories (yang/ and model/) and their contents to
create an adaptor-plug.zip device adapter.

**Tip:** The device adapter archive should follow a naming convention of "vendor_type_model_version.zip" 
to easily identify what devices the adapter supports.

<p align="center">
 <img width="400px" height="200px" src="{{site.url}}/device_adapter/da_plugin.png">
</p>

The created zip file has the following structure:

<p align="center">
 <img width="200px" height="300px" src="{{site.url}}/device_adapter/da_structure.png">
</p>

Managing a Device Adapter
-------------------------

Device adapters are managed within the BAA layer by deploying and
undeploying the assembled device adapter\'s zip file.

The candidate list of device adapters are located in the BAA layer\'s
device adapter data store located at: \"/baa/stores/deviceAdapter\".

### Deploying a Device Adapter

-   Copy the assembled device adapter (e.g., adapter-plug.zip) to the
    /baa/stores/deviceAdapter

<p align="center">
 <img width="400px" height="60px" src="{{site.url}}/device_adapter/da_deploy.jpeg">
</p>


-   Send a command to the BAA layer to deploy the device adapter using
    the BAA layer\'s \"deploy\" command

```
<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="10101">
    <action xmlns="urn:ietf:params:xml:ns:yang:1">
        <deploy-adapter xmlns="urn:bbf:yang:obbaa:device-adapters">
            <deploy>
                <adapter-archive>adapter-plug.zip</adapter-archive>
            </deploy>
        </deploy-adapter>
    </action>
</rpc>
```

### Verifying Deployed Device Adapters

Device Adapter\'s that have been deployed in the BAA layer by requesting
the BAA layer to retrieve Device Adapter information using the following
command:

```
<rpc message-id="10101" xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
    <get>
        <filter type="subtree">
            <network-manager xmlns="urn:bbf:yang:obbaa:network-manager">
                <device-adapters xmlns="urn:bbf:yang:obbaa:network-manager"/>
            </network-manager>
        </filter>
    </get>
</rpc>
```

An example response to the retrieval is as follows:

```
<rpc-reply xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="10101">
  <data>
    <network-manager:network-manager xmlns:network-manager="urn:bbf:yang:obbaa:network-manager">
      <device-adapters xmlns="urn:bbf:yang:obbaa:network-manager">
        <device-adapter>
          <type>ADAPTER1</type>
          <interface-version>1.0</interface-version>
          <model>4LT</model>
          <vendor>VENDOR1</vendor>
        </device-adapter>
      </device-adapters>
    </network-manager:network-manager>
  </data>
</rpc-reply>
```

### Undeploying a Device Adapter

Deployed device adapters that do not have instances of devices
associated with the Device adapter can be undeployed from the BAA layer
by sending the following request where the name of the Device adapter\'s
zip file is used to identify the device adapter.

**Warning:** Device adapters can be removed even if corresponding devices are connected or pre-provisioned in the BAA layer.
This would not have a service impact on physical device. However once the device adapter is undeployed, no further interactions are possible on impacted managed devices until the device adapter is redeployed.


```
<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="10101">
   <action xmlns="urn:ietf:params:xml:ns:yang:1">
     <undeploy-adapter xmlns="urn:bbf:yang:obbaa:device-adapters">
        <undeploy>
            <adapter-archive>adapter-plug.zip</adapter-archive>
        </undeploy>
      </undeploy-adapter>
    </action>
</rpc>
```

[<--Architecture](../architecture)
