
<a id="sda" />

Dynamically Create and Deploy a Standard Device Adapter
===============================
SDAs are device adapters that represent the standard data model for a
type of device (e.g., DPU, OLT,ONU).

In this release of OB-BAA, the SDAs for the DPU , OLT and ONU are
available in the obbaa/resources/models/standard-adapters directory.
Additionally support for adding a standard adapter for a new device
type, can done by dynamically by creating a standard adapter within
OB-BAA and then deploying the created SDA like other VDAs.

Description:
============

The creation, deployment and undeployment operations to maintain the the
SDA are exactly same operations used to maintain VDAs.

A sample coded standard adapter is located
at \~obbaa/resources/examples/adapters/coded-standard-adapter-example.


### Sample pom.xml for a Standard Adapters

```
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.broadband-forum.obbaa</groupId>
        <artifactId>adapters</artifactId>
        <version>${revision}</version>
    </parent>
    <groupId>org.broadband-forum.obbaa</groupId>
    <artifactId>coded-standard-adapter-example</artifactId>
    <packaging>pom</packaging>
    <modules>
        <module>coded-standard-adapter-feature</module>
        <module>coded-standard-adapter-sample</module>
    </modules>
    <properties>
        <adapterType>newDeviceType</adapterType>
        <adapterModel>standard</adapterModel>
        <adapterVendor>sample</adapterVendor>
        <adapterVersion>1.0</adapterVersion>
    </properties>
</project>
```

### Example device-adapter.xml for a Standard Adapter

```
<Adapter type="${adapterType}" interfaceVersion="${adapterVersion}" model="${adapterModel}" vendor="${adapterVendor}"
         xmlns="http://www.bbf.org/obbaa/schemas/adapter/1.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://www.bbf.org/obbaa/schemas/adapter/1.0
                    ../../../../../adapter-schema-fwk/src/main/xsd/device-adapter.xsd">

    <capabilities>
        <value>urn:ietf:params:netconf:base:1.0</value>
        <value>urn:ietf:params:netconf:base:1.1</value>
        <value>urn:ietf:params:netconf:capability:writable-running:1.0</value>
        <value>urn:ietf:params:netconf:capability:notification:1.0</value>
        <value>urn:ietf:params:netconf:capability:interleave:1.0</value>
    </capabilities>

    <developer>Sample developer for coded standard adapter</developer>
    <revisions>
        <revision>2020-03-26</revision>
    </revisions>

</Adapter>
```

**Info:** The type, interfaceVersion, model and vendor details are fetched from the pom.xml file of the Standard Adapter.

### Example features.xml for a Standard Adapter

```
<features xmlns="http://karaf.apache.org/xmlns/features/v1.4.0" name="${project.artifactId}-${project.version}">
    <feature name="${project.artifactId}" version="${project.version}" description="coded standard adapter feature">
        <bundle>mvn:org.broadband-forum.obbaa/coded-standard-adapter-sample/${project.version}</bundle>
    </feature>
</features>
```


[<--Using OB-BAA](../index.md#using)
