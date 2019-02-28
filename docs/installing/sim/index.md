
<a id="sim" />

# Setting up Simulators


OB-BAA uses the following simulators for simulating control of a device
via the BAA layer NBI:

1.  Device Simulator =\> Simulates a NETCONF device. OB-BAA connects to
    the device via NETCONF and deploys the instructions passed on to it
    by Controller.

2.  Controller Simulator =\> Simulates a SDN M&C. Connects to OB-BAA via
    its Northbound NETCONF protocol adapter.

These simulators are loaded with the YANG modules that are to be tested
or demonstrated.

## Device Simulator

Netopeer2 is used as NETCONF device simulator for OB-BAA project
(<https://github.com/CESNET/Netopeer2>).

### System Requirements

For lab setup and trials that require one device simulator, the device 
simulator can be installed into the same virtual machine as the OB-BAA project\'s baa docker container.
If there are multiple device simulators used in the lab or trial each device
simulator has the requirement of 1 (vCPU), 1 GB RAM and 5 GB HDD.

### Installation and Execution

The docker based netopeer2 distribution is available for easy
installation.

Follow the instructions below for pulling and running the docker image of netopeer2

```
docker run -it --network="baadist_default" --name sysrepo -v "/tmp/fastyangs:/yang" -p 830:830 --rm sysrepo/sysrepo-netopeer2:v0.7.4

    - baadist_default: the sysrepo container will use the network bridge of the baa docker container

    - sysrepo: docker service name

    - /tmp/fastyangs: directory in host machine containing simulated device YANG modules.

    - /yang: mapped mount point for the sysrepo service. Sysrepo will use /yang to refer to the /tmp/fastyangs directory in the host machine.

    - 830: indicates the TCP port where the simulator will be listening
```

Install the modules for the type of device that you want to test by installing the modules and then setting up a subscription for change notifications for the modules in sysrepo.
YANG modules for supported by the device simulator can be installed by executing a shell script or manually one at a time.

#### Installing YANG modules using a shell script
Installing YANG modules using the "simulatorYangInstall.sh" shell script provided in the BAA layer distribution uses the following procedure:
```
1. Copy the script "obbaa/baa-dist/simulatorYangInstall.sh" from the obbaa server to target simulator server if the simulator and obbaa are running in different servers.
2. Modify the shell scripts permissions to allow it to be executed with the following syntax chmod +x simulatorYangInstall.sh
3. Execute the script with following syntax "./simulatorYangInstall.sh [docker_name] [yangModelFilePathInDocker]"
   For example: ./simulatorYangInstall.sh sysrepo /yang
```

#### Installing YANG modules manually
Installing YANG modules using netopeer2 operations to install YANG modules one at a time with the following procedure:
```
# Open a shell in the device simulator's environment and navigate to the examples directory
docker exec -it sysrepo /bin/bash
cd /opt/dev/sysrepo/build/examples

# For each module to be installed execute the following
# /yang/ietf-interfaces.yang: could be any YANG module file. In this case the ietf-interfaces module will be installed into simulator and then subscribed for changes.
# root:root: owner:group of the sysrepo service in the server. Should match the owner in the deployed server.

sysrepoctl --install --yang=/yang/ietf-interfaces.yang --owner=root:root --permissions=666
nohup ./application_example ietf_interfaces &

# List the modules to ensure they were installed
sysrepoctl -l
```

### Setting up a Device Simulator for callhome

The Device simulator can be configured to be as a device that supports
the \"callhome\" connection method for the BAA layer.


**Info:** To determine which devices have completed the callhome procedure can be obtained by sending the following:

```
<rpc message-id="10101" xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
    <get>
        <filter type="subtree">
          <network-manager xmlns="urn:bbf:yang:obbaa:network-manager">
            <new-devices xmlns="urn:bbf:yang:obbaa:network-manager"/>
          </network-manager>
        </filter>
     </get>
</rpc>
```

The procedure to setup the simulator to support a \"callhome\" device is as follows:

**Warning:** The instructions in this section use sample certificates and private keys and are used for testing only. These files are included in the distribution of OB-BAA
and can easily be compromised if used in real deployments. Do not use these certificates and private keys in actual deployments.

-   Create TLS certificates

    The OB-BAA code base includes script to generate sample certificates
    that can be found under obbaa/baa-dist/generateDeviceDuidTLSFiles.sh.
	```
    Command: ./generateDeviceDuidTLSFiles.sh <DUID>
    Example: ./generateDeviceDuidTLSFiles.sh DPU1
	```

<p align="center">
 <img width="600px" height="200px" src="{{site.url}}/installing/sim/generateCertificates.jpg">
</p>

> On running the script the certificates will be generated under a new folder with the provided DUID (e.g., DPU1).

<p align="center">
 <img width="600px" height="200px" src="{{site.url}}/installing/sim/generateCertificates.jpg">
</p>

-   Start the sysrepo netopeer2 docker service as discussed in the
    \"Installation and Execution\" section.

-   Login to the sysrepo docker container

```
  docker exec -it sysrepo bash
```

-   Using the builtin NETCONF client (netopeer2-cli) to configure
    callhome

```
  # netopeer2-cli
```

<p align="center">
 <img width="600px" height="100px" src="{{site.url}}/installing/sim/deviceCerts.jpg">
</p>

-   From netopeer2-cli in the sysrepo docker container connect to
    netopeer-server.

    **Note:** The default ssh username/password of
    netopeer2 is netconf/netconf.

```
  connect \--ssh \--login netconf
```

<p align="center">
 <img width="600px" height="100px" src="{{site.url}}/installing/sim/deviceCerts.jpg">
</p>

-   From netopeer2-cli in the sysrepo docker container, load the server
    private key using user-rpc command. This brings up a vi editor where
    the action request needs to be keyed in.

```
  user-rpc <return>
```

-   Load the private key by typing in an action \"load-private-key\" in
    the format provided below. Once done close using \":wq!\" (vi
    command for save & quit). Successful execution will return \"OK\"

```
<action xmlns="urn:ietf:params:xml:ns:yang:1">
<keystore xmlns="urn:ietf:params:xml:ns:yang:ietf-keystore">
<private-keys>
<load-private-key>
<name>[name the private key]</name>
<private-key>[Copy the contents of private key (DPU1.key in example) here without the BEGIN and END]</private-key>
</load-private-key>
</private-keys>
</keystore>
</action>
```

> Below is an example of how to load the private key for the DPU server (e.g., DPU1.key):

```
<action xmlns="urn:ietf:params:xml:ns:yang:1">
<keystore xmlns="urn:ietf:params:xml:ns:yang:ietf-keystore">
<private-keys>
<load-private-key>
<name>DPU_server_key</name>
<private-key>MIIEpAIBAAKCAQEAtp/X2lHNVLi6G6a+AiAH8Rg2J/YpidA2C/jerc5uqerM/ihD
nqN+OVI8ErukpEuQ5d7PTzU3CnDiB8zAPDN/+9mR2L4FM9nlklieXeI8LHGjI5/E
P5JLmVA7HpRzL4yGWFS4PyL+e8ly3PGNpAQyOm5tRlPbTKi8PJvTLXTkVGhh+ZYX
fcMGVLoP0riPsYrZkjWVLIrnhz4n5sdniEzUDOg3Q0aQWqKnIc407cpU4zfd3zvV
Q8lMVpZIRDqFT82OMosSovJPUm/qQ/+BUihy2RZ0IkyfwuYB+aqUtl9lIRQG3mfN
rraeCuEJ5jsf+weQ61McTg2x0oB/v+VOMCMAbQIDAQABAoIBAB1RUdy5jyYPtcjk
ntJGhB/fTCpkKUz3gQWxAUaTwk5C5H/UxO49vvDC0+QJ/admfi948nz7xQdHEfJA
m4fOmLg8uF48OyeUzncNBPd7bz+PSqpYZq/x83L8X1FMcaVrgNHDYAS6wHHs6CSy
HpP7HHkm6yIEKkuHNxjfxyof/tvR1EjSka3MHL9L38mGQTATCMLGRgEjHCefSgsE
o5ACs5NmhfkAJDsBDvBp6kLUKk2RwchaDjz58f6HgXkiGEgeUNDTnWU6SXnv6zmw
/9KmXMMBO8gG4upYe250pHKfLRP5Kk+0Sg6efstgCagM9sTpCTuEUpay8PN2/awJ
3mpIi2ECgYEA70z1COAPpwgpOeTIIqWIPSDlN17KL3pSdMLPP9BWMRlqq07k2G7V
V3MFMP4RWllfLfifVhsBuOWWQod1uRTJTAG46qjLy9tVkG7XxUEDdK3MVnYPI/Yr
5WwrttO1IP+lpsRfC41KRciuF2iZPqWwKVV9sTHHmIVZafeejM5EINUCgYEAw15h
S1cqnI7Otew8IXZ7npzkaND2Pi0y53jGbh7Ve0+1FpZVqxgtU+3uWVlWsoV3FO0D
v6Oawa9/YKbOpGrvy/Tng7a1hYctTUMn8OzcTWLXGjYXNda3x/Nr9oDViUeBWP/1
sAeRotcXpUhBrLrigkOsoonzbR3BOg+hPHOJbTkCgYEAl9IgZ+gXpiZUWYmfu/N/
cyDqq+10oxidNbze1iKsfGwes97S68mtitTLh2C5y7OF4lmpZWyu9AqdAUqF0s2w
RIuXMTG0UnquV3srY9cyhRU63eP7CRrGkMDHHzBD61KmWx6dGmwiohGG+gz/pLJk
CGVX4FKvykFYEBXsvnKi4J0CgYAolj/iNf6dPbHF64jmbsXpwrBU8ixl6F/t5JJc
qD0ze/Cj+6FahRBNol1k5IF1XvLJPyALPQLWgA4XVzAQykJ8/ajnHRsC3X1U0sHG
dH6j+Qe0403ZGn1dpb1lHYi/F0LQ6YPyCPCBguvfo245Yy3RYBvKPJx8q7TLyhl2
BI6lSQKBgQDbiJQukEOHJnDkhUKerGIEGfwEVdNpkFgUN/Qcu7Y63Lp3SDeQr0ne
xkAmsUiC/rRt/njelMvopUaXqaSqPM3d7didD0Hx6ZDTs6wCzm06UtdsvLGne1If
nzVIO6JFSbMbrGCnce/YDyfe3RYIZoiTUZot7/rSnWseScmAygX1gA==</private-key>
</load-private-key>
</private-keys>
</keystore>
</action>
```

-   From netopeer2-cli in the sysrepo docker container, load
    certificates using edit-config. This command brings up a vi editor.

```
  edit-config --target running --config <return>
```

-   Add the certificates following code snippet format below. Once done
    close using \":wq!\" (vi command for save & quit). Successful
    execution will return \"OK\"

```
<keystore xmlns="urn:ietf:params:xml:ns:yang:ietf-keystore">
<private-keys>
<private-key>
<name>[use the same private key name from above here]</name>
<certificate-chains>
<certificate-chain>
<name>[name the cert name]</name>
<certificate>[Copy the contents of DPU1_certchain.crt here without the BEGIN and END]</certificate>
</certificate-chain>
</certificate-chains>
</private-key>
</private-keys>
<trusted-certificates>
<name>[name the trust list]</name>
<trusted-certificate>
<name>[name the trust ca]</name>
<certificate>[Copy the contents of trustchain.pem here without the BEGIN and END]</certificate>
</trusted-certificate>
</trusted-certificates>
</keystore>
```

> Below is an example of how to load the certificate and trust chains for the DPU server (e.g., DPU1.cert, BAA CA certificate):

```
<keystore xmlns="urn:ietf:params:xml:ns:yang:ietf-keystore">
<private-keys>
<private-key>
<name>DPU_server_key</name>
<certificate-chains>
<certificate-chain>
<name>DPU_server_cert</name>
<certificate>MIIDAzCCAesCCQDXuKt20sxNuzANBgkqhkiG9w0BAQsFADA5MQswCQYDVQQGEwJJ
TjELMAkGA1UECAwCS0ExDDAKBgNVBAcMA0JMUjEPMA0GA1UECgwGT0ItQkFBMCAX
DTE4MDcyNjEwNDgxM1oYDzMzODcwNzA5MTA0ODEzWjBMMQswCQYDVQQGEwJJTjEL
MAkGA1UECAwCS0ExDDAKBgNVBAcMA0JMUjEOMAwGA1UECgwFT0JCQUExEjAQBgNV
BAMMCURVSUQvRFBVMTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALaf
19pRzVS4uhumvgIgB/EYNif2KYnQNgv43q3ObqnqzP4oQ56jfjlSPBK7pKRLkOXe
z081Nwpw4gfMwDwzf/vZkdi+BTPZ5ZJYnl3iPCxxoyOfxD+SS5lQOx6Ucy+MhlhU
uD8i/nvJctzxjaQEMjpubUZT20yovDyb0y105FRoYfmWF33DBlS6D9K4j7GK2ZI1
lSyK54c+J+bHZ4hM1AzoN0NGkFqipyHONO3KVOM33d871UPJTFaWSEQ6hU/NjjKL
EqLyT1Jv6kP/gVIoctkWdCJMn8LmAfmqlLZfZSEUBt5nza62ngrhCeY7H/sHkOtT
HE4NsdKAf7/lTjAjAG0CAwEAATANBgkqhkiG9w0BAQsFAAOCAQEAnzArkupGHm7s
hX2iNZdBZFUEVWbkgJTke0iO17VuntH+lCpCaMfu+MMKhKzILnO3F7ebyLS+r+YG
Fzxd0/KHLNPcFUJTDtout1JyuiRrR5SEb4fgETrLlJBKXq/Rs/7vdbmkcu7hJTb/
S4BV9866/BO8DVvt/RYz55IZ9xYPIcgxtxEQS2nRpc3EgN73iDE7sK6Hf6GEG78C
m90mb9EYETPjB/xcQSr9Ecea3GDkx9YYZVil3YdV9hJXEZHhkN7pzLR0HRhemWDD
kyY+R7//metm9BJzruaSEqguqXVGuYgbSyanlCDF8Lwe8WlEAB4jTfmXcXRzHW9e
S2Tv09/y8Q==</certificate>
</certificate-chain>
</certificate-chains>
</private-key>
</private-keys>
<trusted-certificates>
<name>DPU_trusted_ca_list</name>
<trusted-certificate>
<name>test_ca</name>
<certificate>MIIDRTCCAi2gAwIBAgIJAL6nrHtB9RlEMA0GCSqGSIb3DQEBCwUAMDkxCzAJBgNV
BAYTAklOMQswCQYDVQQIDAJLQTEMMAoGA1UEBwwDQkxSMQ8wDQYDVQQKDAZPQi1C
QUEwHhcNMTgwNzEwMDQyODUwWhcNMjEwNDI5MDQyODUwWjA5MQswCQYDVQQGEwJJ
TjELMAkGA1UECAwCS0ExDDAKBgNVBAcMA0JMUjEPMA0GA1UECgwGT0ItQkFBMIIB
IjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuV1gWjWTXFWCbBCYe8vkt3Wb
KQWazG1CaJdCE+HMem62dUGSNyN29N6OdoaPot3eEzBNbiox+N3aSkX6+5078px4
GkxKDTiQf4Tw119g7JICDCD4YeYzYUat35N3XsLsrL9ua8svfDCn7UNxasI29HsX
60vH7ip9F9Mh+BvA0k8f++6ylXeNJbu5Re7pyMt9y5E6W0WOGOnvOrmj546QhshL
PtMin0+VaYfOu6p/YOOIdfadjv5i3zxeD+MGUO8F0Vv1NLTv+DpxzldK9gMlLaud
+WzZqMyrnFu3qlnYRej6dq171BiNtBc0ebJl8bbHl/8vlfWIE/oOUv8sPvd4QwID
AQABo1AwTjAdBgNVHQ4EFgQUbRVtFVtOGue/yIfclQ9cEOzhpBkwHwYDVR0jBBgw
FoAUbRVtFVtOGue/yIfclQ9cEOzhpBkwDAYDVR0TBAUwAwEB/zANBgkqhkiG9w0B
AQsFAAOCAQEAjJ/qvSXreYE2AGs8tZ121ubk+laD+ADpq+EtvpVpkCcGHKGSSGvW
4r1wDtrMGKLKKzwMIkFIK1Bwka5bIC5Jo7c/oRVt1djVScSkCmL/4z4fGRei5iGy
FK6hTTtDfoxvht+/ILEeOFsDTJSWMHFOfjkdH/DKekrS03wqS9GVawUZboRaoSi3
c110ZSJag2QcyveWbvRbMTvk1jjoPakTnFK7HTiTv+XxheLJL4WOn1Mhj+iksP/v
ZFs7XY38w/1AgA8gGGh9exfD28MpxjnO+xAgkTufuW+KB+82lXf1geoCzGoV7S2R
KBKGYUlmDh7SJaQO0x3fGwFz84IQhgZvxA==</certificate>
</trusted-certificate>
</trusted-certificates>
</keystore>
```

-   Enable callhome by performing an edit-config (explained in an earlier
    step) and executing code-snippet sample below:

```
<netconf-server xmlns="urn:ietf:params:xml:ns:yang:ietf-netconf-server">
<call-home>
<netconf-client>
<name>[Name the client]</name>
<tls>
<endpoints>
<endpoint>
<name>[name the end-point]</name>
<address>[obbaa callhome address]</address>
<port>[obbaa callhome port]</port>
</endpoint>
</endpoints>
<certificates>
<certificate>
<name>[use the same server cert name]</name>
</certificate>
</certificates>
<client-auth>
<trusted-ca-certs>[use the same trust list name]</trusted-ca-certs>
<cert-maps>
<cert-to-name>
<id>1</id>
<fingerprint>[finger print of the obbaa certificate]</fingerprint>
<map-type xmlns:x509c2n="urn:ietf:params:xml:ns:yang:ietf-x509-cert-to-name">x509c2n:specified</map-type>
<name>[user name]</name>
</cert-to-name>
</cert-maps>
</client-auth>
</tls>
<connection-type>
<persistent/>
</connection-type>
</netconf-client>
</call-home>
</netconf-server>
```
- Obtain the fingerprint of obbaa certificate by using openssl command

```
openssl x509 -noout -fingerprint -sha256 -inform pem -in /obbaa/baa-dist/src/main/assembly/conf/tls/certchain.crt
```
<p align="center">
 <img width="700px" height="40px" src="{{site.url}}/installing/sim/finger-print.jpg">
</p>
This is a SHA256 fingerprint and we need to specify it by prefixing 04 in the <fingerprint> leaf.

> Below is an example of how to enable the callhome feature for the DPU:

```
<netconf-server xmlns="urn:ietf:params:xml:ns:yang:ietf-netconf-server">
<call-home>
<netconf-client>
<name>test_tls_ch_client</name>
<tls>
<endpoints>
<endpoint>
<name>test_tls_ch_endpt</name>
<address>30.1.1.125</address>
<port>4335</port>
</endpoint>
</endpoints>
<certificates>
<certificate>
<name>DPU_server_cert</name>
</certificate>
</certificates>
<client-auth>
<trusted-ca-certs>DPU_trusted_ca_list</trusted-ca-certs>
<cert-maps>
<cert-to-name>
<id>1</id>
<fingerprint>04:4D:7D:F0:3A:C7:07:B1:66:E7:A0:62:A6:88:7A:A9:BF:0A:53:CC:13:BD:4A:9B:1B:9D:97:3C:FF:5A:C3:8E:A7</fingerprint>
<map-type xmlns:x509c2n="urn:ietf:params:xml:ns:yang:ietf-x509-cert-to-name">x509c2n:specified</map-type>
<name>netconf</name>
</cert-to-name>
</cert-maps>
</client-auth>
</tls>
<connection-type>
<persistent/>
</connection-type>
</netconf-client>
</call-home>
</netconf-server>
```

Once these steps are complete - The device will then begin its
callhome procedure and will appear in the BAA layer as a new device.

To check if the device has successfully completed the callhome procedure:

```
<rpc message-id="10101" xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
    <get>
        <filter type="subtree">
            <network-manager xmlns="urn:bbf:yang:obbaa:network-manager">
                <new-devices xmlns="urn:bbf:yang:obbaa:network-manager"/>
            </network-manager>
        </filter>
    </get>
</rpc>
```

Example response:
```
<rpc-reply xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="10101">
  <data>
    <network-manager:network-manager xmlns:network-manager="urn:bbf:yang:obbaa:network-manager">
      <new-devices xmlns="urn:bbf:yang:obbaa:network-manager">
        <new-device>
          <duid>DPU1</duid>
          <device-capability>urn:ietf:params:xml:ns:yang:ietf-netconf-acm?module=ietf-netconf-acm&amp;revision=2012-02-22</device-capability>
          <device-capability>urn:ietf:params:xml:ns:yang:ietf-netconf-with-defaults?module=ietf-netconf-with-defaults&amp;revision=2011-06-01</device-capability>
          <device-capability>urn:ietf:params:netconf:capability:with-defaults:1.0?basic-mode=explicit&amp;also-supported=report-all,report-all-tagged,trim,explicit</device-capability>
          <device-capability>urn:ietf:params:xml:ns:yang:iana-crypt-hash?module=iana-crypt-hash&amp;revision=2014-08-06</device-capability>
          <device-capability>urn:ietf:params:xml:ns:yang:1?module=yang&amp;revision=2017-02-20</device-capability>
          <device-capability>urn:ietf:params:xml:ns:yang:ietf-interfaces?module=ietf-interfaces&amp;revision=2014-05-08</device-capability>
          <device-capability>urn:ietf:params:xml:ns:yang:iana-if-type?module=iana-if-type&amp;revision=2014-05-08</device-capability>
          <device-capability>urn:ietf:params:xml:ns:yang:ietf-yang-library?revision=2018-01-17&amp;module-set-id=25</device-capability>
          <device-capability>urn:ietf:params:xml:ns:yang:ietf-netconf-monitoring?module=ietf-netconf-monitoring&amp;revision=2010-10-04</device-capability>
          <device-capability>http://example.net/turing-machine?module=turing-machine&amp;revision=2013-12-27</device-capability>
          <device-capability>urn:ietf:params:netconf:capability:rollback-on-error:1.0</device-capability>
          <device-capability>urn:ietf:params:netconf:capability:xpath:1.0</device-capability>
          <device-capability>urn:ietf:params:netconf:capability:startup:1.0</device-capability>
          <device-capability>urn:ietf:params:xml:ns:yang:ietf-x509-cert-to-name?module=ietf-x509-cert-to-name&amp;revision=2014-12-10</device-capability>
          <device-capability>urn:ietf:params:xml:ns:yang:ietf-yang-metadata?module=ietf-yang-metadata&amp;revision=2016-08-05</device-capability>
          <device-capability>urn:ietf:params:netconf:capability:writable-running:1.0</device-capability>
          <device-capability>urn:ietf:params:xml:ns:yang:ietf-ip?module=ietf-ip&amp;revision=2014-06-16</device-capability>
          <device-capability>urn:ietf:params:netconf:capability:interleave:1.0</device-capability>
          <device-capability>urn:ietf:params:xml:ns:yang:ietf-netconf-notifications?module=ietf-netconf-notifications&amp;revision=2012-02-06</device-capability>
          <device-capability>urn:ietf:params:xml:ns:netconf:base:1.0?module=ietf-netconf&amp;revision=2011-06-01&amp;features=writable-running,candidate,rollback-on-error,validate,startup,xpath</device-capability>
          <device-capability>urn:ietf:params:xml:ns:netconf:notification:1.0?module=notifications&amp;revision=2008-07-14</device-capability>
          <device-capability>urn:ietf:params:xml:ns:netmod:notification?module=nc-notifications&amp;revision=2008-07-14</device-capability>
          <device-capability>urn:ietf:params:netconf:base:1.1</device-capability>
          <device-capability>urn:ietf:params:netconf:base:1.0</device-capability>
          <device-capability>urn:ietf:params:xml:ns:yang:ietf-yang-types?module=ietf-yang-types&amp;revision=2013-07-15</device-capability>
          <device-capability>urn:ietf:params:netconf:capability:notification:1.0</device-capability>
          <device-capability>urn:ietf:params:netconf:capability:validate:1.1</device-capability>
          <device-capability>urn:ietf:params:xml:ns:yang:ietf-system?module=ietf-system&amp;revision=2014-08-06&amp;features=,authentication,local-users</device-capability>
          <device-capability>urn:ietf:params:netconf:capability:candidate:1.0</device-capability>
          <device-capability>urn:ietf:params:xml:ns:yang:ietf-inet-types?module=ietf-inet-types&amp;revision=2013-07-15</device-capability>
        </new-device>
      </new-devices>
    </network-manager:network-manager>
  </data>
</rpc-reply>
```

## Controller Simulator

Use a NETCONF browser (<http://www.mg-soft.com/download.html>) or the
Atom editor with NETCONF plug (<https://atom.io/packages/atom-netconf>)
to simulate a SDN Controller over the OB-BAA NBI.

Using the NetConf server settings option, configure the Atom editor to
point to the OB-BAA NETCONF server using the settings defined in the
\"Using OBBAA\" section. The following fields need to appropriately
updated in the setting:

1.  Hostname : IP Address of OB-BAA server. By default BAA & sysrepo
    listens in to all the network interfaces of the host server.

2.  UserName : admin

3.  Password : password

4.  TCP Port : 9292

Click connect for the editor to connect to OB-BAA NETCONF server. As
shown below the handshake Hello message should be visible. A snapshot of
the Atom editor and the initial exchange with OB-BAA server is shown
below.

<p align="center">
 <img width="600px" height="400px" src="{{site.url}}/installing/sim/atom_simulator.png">
</p>

Once the connection is established, additional NETCONF commands can be
sent with the responses received in the Atom editor.

## YANG modules

### OB-BAA Network Manager YANG module

OB-BAA exposes a Network Manager YANG module which represents the
aggregated BAA model that is available in the code base at
resources/models/yang/aggregator-yang/bbf-obbaa-network-manager.yang

### Sample Device YANG module

The OB-BAA code base provides example YANG module bundles that can be
used for simulating different types of devices (e.g., DPU, OLT). These
examples are available in the resources/examples/device-yang directory
of the OB-BAA source code distribution.

## Simulate a new Device

Once the Controller (NBI) and Device (SBI) simulators are setup, next
step is to first create a device in BAA layer and execute commands on
the device.

The following sections contains information to execute commands from the
Controller simulator to the BAA layer to configure and maintain a
Device.

To try the commands with a Controller simulator using Atom, copy the
request command of each use-case into a dedicated xml file and execute
it from Atom NETCONF editor (illustration below).

<p align="center">
 <img width="200px" height="400px" src="{{site.url}}/installing/sim/atom_obbaa.png">
</p>

### Create Device

#### Create In Direct Connection mode

When creating a device in direct connection mode the IP address and port
of the device is needed as well as the SSH username and password.

For simulators that reside in a docker container using these
instructions, the management port of the simulated device is obtained
via the command used to run the netopeer2 docker image. The IP address
of the simulated device can be obtained using the following docker
command:

```
docker network inspect baadist_default
```
**Warning:** The SSH credential for a simulated device uses the default
sysrepo/netopeer2 configuration of \"netconf/netconf\". These credentials
should be modified if used in environments than local lab testing.

Request:

```
<?xml version="1.0" encoding="UTF-8"?>
<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="1527307907656">
    <edit-config>
        <target>
            <running />
        </target>
        <config>
            <network-manager xmlns="urn:bbf:yang:obbaa:network-manager">
                <managed-devices>
                    <device xmlns:xc="urn:ietf:params:xml:ns:netconf:base:1.0" xc:operation="create">
                        <name>deviceA</name>
                        <device-management>
                            <type>DPU</type>
                            <interface-version>1.0.0</interface-version>
                            <vendor>Nokia</vendor>
                            <model>4LT</model>
                            <device-connection>
                                <connection-model>direct</connection-model>
                                <password-auth>
                                    <authentication>
                                        <address>192.168.169.1</address>
                                        <!-- ip address of the device/simulator. Replace with the right ip -->
                                        <management-port>830</management-port>
                                        <!-- port number of device/simulator NC server. -->
                                        <user-name>netconf</user-name>
                                        <!-- username of device NC server -->
                                        <password>netconf</password>
                                        <!-- password of device NC server -->
                                    </authentication>
                                </password-auth>
                            </device-connection>
                        </device-management>
                    </device>
                </managed-devices>
            </network-manager>
        </config>
    </edit-config>
</rpc>
```

Response:
```
<rpc-reply xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="1527307907656">
  <ok/>
</rpc-reply>
```

#### Create In Call-home mode

Request:
```
<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="1527307907656">
    <edit-config>
        <target>
            <running />
        </target>
        <config>
            <managed-devices xmlns="urn:bbf:yang:obbaa:network-manager">
                <device xmlns:xc="urn:ietf:params:xml:ns:netconf:base:1.0" xc:operation="create">
                    <name>Bangalore-Mayata-E2</name>
                    <device-management>
                        <type>DPU</type>
                        <interface-version>1.0.0</interface-version>
                        <vendor>Nokia</vendor>
                        <device-connection>
                            <connection-model>call-home</connection-model>
                            <duid>DPU1</duid>
                        </device-connection>
                    </device-management>
                </device>
            </managed-devices>
        </config>
    </edit-config>
</rpc>
```

Response:
```
<rpc-reply xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="1527307907656">
  <ok/>
</rpc-reply>
```

#### Retrieve Device Information

Request:
```
<?xml version="1.0" encoding="UTF-8"?>
<rpc message-id="10101" xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
    <get>
        <filter type="subtree">
            <network-manager xmlns="urn:bbf:yang:obbaa:network-manager">
                <managed-devices>
                    <device>
                        <name>deviceA</name>
                    </device>
                </managed-devices>
            </network-manager>
        </filter>
    </get>
</rpc>
```

Response:

```
<rpc-reply xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="10101">
  <data>
    <network-manager:network-manager xmlns:network-manager="urn:bbf:yang:obbaa:network-manager">
      <network-manager:managed-devices>
        <network-manager:device>
          <network-manager:name>deviceA</network-manager:name>
          <network-manager:device-management>
            <device-state xmlns="urn:bbf:yang:obbaa:network-manager">
              <configuration-alignment-state>Never Aligned</configuration-alignment-state>
              <connection-state>
                <connected>false</connected>
                <connection-creation-time>1970-01-01T00:00:00+00:00</connection-creation-time>
              </connection-state>
            </device-state>
            <network-manager:interface-version>1.0.0</network-manager:interface-version>
            <network-manager:model>4LT</network-manager:model>
            <network-manager:push-pma-configuration-to-device>true</network-manager:push-pma-configuration-to-device>
            <network-manager:type>DPU</network-manager:type>
            <network-manager:vendor>Nokia</network-manager:vendor>
            <network-manager:device-connection>
              <network-manager:connection-model>direct</network-manager:connection-model>
              <network-manager:password-auth>
                <network-manager:authentication>
                  <network-manager:address>192.168.169.1</network-manager:address>
                  <network-manager:management-port>830</network-manager:management-port>
                  <network-manager:password>netconf</network-manager:password>
                  <network-manager:user-name>netconf</network-manager:user-name>
                </network-manager:authentication>
              </network-manager:password-auth>
            </network-manager:device-connection>
          </network-manager:device-management>
        </network-manager:device>
      </network-manager:managed-devices>
    </network-manager:network-manager>
  </data>
</rpc-reply>
```

### Update Device Configuration

When updating a device configuration it is important that any YANG
modules referenced have been loaded into the BAA layer (see the
instructions for Deploying YANG modules into the BAA layer) as well as
the device. In the case of the simulated device using the docker
container this would have happened when setup the simulator using the
instructions on this page.

Request:

```
<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="1527307907169">
    <edit-config>
        <target>
            <running/>
        </target>
        <config>
            <network-manager xmlns="urn:bbf:yang:obbaa:network-manager">
                <managed-devices>
                    <device>
                        <name>deviceA</name>
                        <root>
                            <if:interfaces xmlns:if="urn:ietf:params:xml:ns:yang:ietf-interfaces">
                                <if:interface xmlns:xc="urn:ietf:params:xml:ns:netconf:base:1.0" xc:operation="create">
                                    <if:name>interfaceB</if:name>
                                    <if:type xmlns:ianaift="urn:ietf:params:xml:ns:yang:iana-if-type">ianaift:ethernetCsmacd</if:type>
                                </if:interface>
                            </if:interfaces>
                        </root>
                    </device>
                </managed-devices>
            </network-manager>
        </config>
    </edit-config>
</rpc>
```

Response:

```
<rpc-reply message-id="1527307907664" xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
<ok/>
</rpc-reply>
```

### Retrieve Device Configuration

Request:

```
<?xml version="1.0" encoding="UTF-8"?>
<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="1527307907656">
    <get-config xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
        <source>
            <running/>
        </source>
        <filter type="subtree">
            <network-manager xmlns="urn:bbf:yang:obbaa:network-manager">
                <managed-devices>
                    <device>
                        <name>deviceA</name>
                        <root>
                            <if:interfaces xmlns:if="urn:ietf:params:xml:ns:yang:ietf-interfaces"/>
                        </root>
                    </device>
                </managed-devices>
            </network-manager>
        </filter>
    </get-config>
</rpc>
```

Response:

```
<data xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"> 
	<managed-devices xmlns="urn:bbf:yang:obbaa:network-manager">
		<device>     
			<device-name>deviceA</device-name>    
			<root>       
				<if:interfaces xmlns:if="urn:ietf:params:xml:ns:yang:ietf-interfaces">        
					<if:interface>
			            <if:name>interfaceA</if:name>
			            <if:enabled>true</if:enabled>
			            <if:type xmlns:ianaift="urn:ietf:params:xml:ns:yang:iana-if-type">ianaift:ethernetCsmacd</if:type>         
					</if:interface>       
				</if:interfaces>    
			</root>  
		</device>
	</managed-devices>
</data>
```

### Delete Device

Request:

```
<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="1527307907464">
    <edit-config>
        <target>
            <running />
        </target>
        <config>
            <network-manager xmlns="urn:bbf:yang:obbaa:network-manager">
                <managed-devices>
                    <device xmlns:xc="urn:ietf:params:xml:ns:netconf:base:1.0" xc:operation="delete">
                        <name>deviceA</name>
                    </device>
                </managed-devices>
            </network-manager>
        </config>
    </edit-config>
</rpc>
```

Response:

```
<rpc-reply xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="1527307907464">
  <ok/>
</rpc-reply>
```

[<--Installing](../index.md#installing)
