
<a id="man" />

Maintaining Access Nodes
========================

Using OB-BAA to allow SDN M&C elements to interact with AN requires that
the BAA layer is configured to understand the model for the type of
device that the BAA layer will interact. Once the type of Access Node is
modelled, an instance of the Access Node is created for the type of AN
model. Likewise the SDN M&C element will need to be identified within
the BAA layer with its permissions that define what the element is
authorized to perform within the BAA layer.

Modelling a type of Access Node (AN)
------------------------------------

In the BAA layer, each type of AN (e.g., DPU, OLT) supported in the BAA
layer is defined and maintained through the administration interface of
OB-BAA. In OB-BAA each type of AN that is defined is associated with a
set of YANG modules defined for that type of AN. In addition, compatible
SBI Adapters are associated with AN type. The YANG modules that are
supported by an AN type is determined by administrator of the BAA layer.

Modules for a type of AN are exposed to SDN M&C elements using the
"inline schema mount" mechanism defined in the
"draft-ietf-netmod-schema-mount" where each mount point defines the
modules that comprise the type of AN.

```
<schema-mounts xmlns="urn:ietf:params:xml:ns:yang:ietf-yang-schema-mount">
  <mount-point>
    <module>bbf-obbaa-network-manager</module>
    <name>root</name>
      <module-set>
        <name>olt-device</name>
        <module>
        ...
        </module>
      </module-set>
      <module-set>
        <name>dpu-device</name>
        <module>
        ...
        </module>
      </module-set>
    </use-schema>
  </mount-point>
</schema-mounts>

```

**Info:** Example requests for a SDN M&C element to retrieve the YANG modules
provided by an AN type is provided in the source code \"examples/yang\"
directory.

Creating and Maintaining an Access Node Instance
------------------------------------------------

In order for the BAA layer to interact with an Access Node, the BAA
layer needs to have enough information to:

1.  Establish a communication session between the BAA layer and the AN
    instance (e.g., type of device, information to identify the device,
    connection information)

2.  Provide information to SDN M&C elements they would need to build and
    maintain network maps and equipment inventory (e.g., GPS location,
    device connectivity state)

**Info:** The information that BAA layer maintains is defined in the
\"bbf-obbaa-network-manager\" YANG module located in the source code\'s
"modules" directory.

**Info:** Example requests for a SDN M&C element to create, update, delete and
retrieve AN instances is provided in the source code \"example requests\"
directory.

Sending NETCONF requests from a SDN M&C element to an AN Instance
-----------------------------------------------------------------

In order for a SDN M&C element to send a request to an AN Instance, the
SDN M&C has to obtain the credentials used by the BAA layer. These
credentials are located in the docker-compose.yml file.

```
docker-compose.yml
version: '2'
services:
baa:
image: baa
container_name: baa
restart: always
ports:
- "8080:8080"
- "5005:5005"
- "9292:9292" <---NBI Netconf server SSH port.
environment:
- JAVA_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005
- BAA_USER=admin NBI Netconf server SSH username.
- BAA_USER_PASSWORD=password <---NBI Netconf server SSH password.
#Possible Values for PMA_SESSION_FACTORY_TYPE are REGULAR,TRANSPARENT, Default value is REGULAR
- PMA_SESSION_FACTORY_TYPE=REGULAR
```

[<--Using OB-BAA](../index.md#using)
