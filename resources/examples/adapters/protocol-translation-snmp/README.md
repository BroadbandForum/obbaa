# protocol-translation-snmp
Translation from OB-BAA NETCONF to SNMP

# Supported devices
This is an adapter to translate NETCONF messages from the OB-BAA framework into SNMP messagesi. As of now this is tested with DZS OLTs but this could be
easily extended as a model to develop an SNMP adapter for any device.

# How to use
Follow the instructions from the BBF website to compile the code.
 
To deploy the adapter use the generated KAR file, sample-OLT-protocolsnmp-1.0.kar.

Below are some examples for NETCONF messages to be used to test the adapter:

```
To deploy the adapter
---------------------
<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="10101">
    <action xmlns="urn:ietf:params:xml:ns:yang:1">
        <deploy-adapter xmlns="urn:bbf:yang:obbaa:device-adapters">
            <deploy>
                <adapter-archive>sample-OLT-protocolsnmp-1.0.kar</adapter-archive>
            </deploy>
        </deploy-adapter>
    </action>
</rpc>


To undeploy the adapter
---------------------
<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="10101">
    <action xmlns="urn:ietf:params:xml:ns:yang:1">
        <undeploy-adapter xmlns="urn:bbf:yang:obbaa:device-adapters">
            <undeploy>
                <adapter-archive>sample-OLT-protocolsnmp-1.0.kar</adapter-archive>
            </undeploy>
        </undeploy-adapter>
    </action>
</rpc>


To create an SNMP V2 device
---------------------------
<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="1">
    <edit-config>
        <target>
            <running />
        </target>
        <config>
            <network-manager xmlns="urn:bbf:yang:obbaa:network-manager">
                <managed-devices>
                    <device xmlns:xc="urn:ietf:params:xml:ns:netconf:base:1.0" xc:operation="create">
                        <name>[DEVICE NAME]</name>
                        <device-management>
                            <type>OLT</type>
                            <interface-version>1.0</interface-version>
                            <vendor>sample</vendor>
                            <model>protocolsnmp</model>
                            <device-connection>
                                <connection-model>snmp</connection-model>
                                <snmp-auth>
                                    <snmp-authentication>
                                        <address>[IP ADDRESS]</address>
                                        <agent-port>[AGENT PORT]</agent-port>
                                        <trap-port>[MANAGER TRAP PORT]</trap-port>
                                        <snmp-version>v2c</snmp-version>
                                        <community-string>[COMMUNITY STRING]</community-string>
                                    </snmp-authentication>
                                </snmp-auth>
                            </device-connection>
                        </device-management>
                    </device>
                </managed-devices>
            </network-manager>
        </config>
    </edit-config>
</rpc>

To create an SNMP V3 device
---------------------------
<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="1">
    <edit-config>
        <target>
            <running />
        </target>
        <config>
            <network-manager xmlns="urn:bbf:yang:obbaa:network-manager">
                <managed-devices>
                    <device xmlns:xc="urn:ietf:params:xml:ns:netconf:base:1.0" xc:operation="create">
                        <name>[DEVICE NAME]</name>
                        <device-management>
                            <type>OLT</type>
                            <interface-version>1.0</interface-version>
                            <vendor>sample</vendor>
                            <model>protocolsnmp</model>
                            <device-connection>
                                <connection-model>snmp</connection-model>
                                <snmp-auth>
                                    <snmp-authentication>
                                        <address>[IP ADDRESS]</address>
                                        <agent-port>[AGENT PORT]</agent-port>
                                        <trap-port>[MANAGER TRAP PORT]</trap-port>
                                        <snmp-version>v3</snmp-version>
                                        <snmpv3-auth>
                                           <user-name>user</user-name>
                                           <security-level>[SECURITY LEVEL, eg:authPriv]</security-level>
                                           <auth-protocol>[AUTHENTICATION PRTOCOL, eg:AuthSHA]</auth-protocol>
                                           <auth-password>[AUTHENTICATION PASSPHRASE, eg:my_authpass]</auth-password>
                                           <priv-protocol>[PRIVACY PRTOCOL, eg:PrivAES]</priv-protocol>
                                           <priv-password>[PRIVASY PASSPHRASE, eg:my_privpass]</priv-password>
                                        </snmpv3-auth>
                                    </snmp-authentication>
                                </snmp-auth>
                            </device-connection>
                        </device-management>
                    </device>
                </managed-devices>
            </network-manager>
        </config>
    </edit-config>
</rpc>


Command to test the SNMP transport class
----------------------------------------
<edit-config xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
  <target>
    <running/>
  </target>
  <config>
    <network-manager xmlns="urn:bbf:yang:obbaa:network-manager">
      <managed-devices>
        <device>
          <name>deviceA</name>
          <root>
            <hardware xmlns="urn:ietf:params:xml:ns:yang:ietf-hardware">
              <component xmlns:xc="urn:ietf:params:xml:ns:netconf:base:1.0" xc:operation="create">
                <name>fan_1</name> <!-- should be of format [a-zA-Z]*_[0-9]+ .digits comes after underscore is mapped to entPhysicalIndex of entity MIB in this sample implementation-->
                <class xmlns:ianahw="urn:ietf:params:xml:ns:yang:iana-hardware">ianahw:fan</class>
                <serial-num>W0929174</serial-num>
                <alias>fanA</alias>
                <asset-id>hwc8972</asset-id>
              </component>
            </hardware>
          </root>
        </device>
      </managed-devices>
    </network-manager>
  </config>
</edit-config>
```
