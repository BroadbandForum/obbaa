
<a id="scale_testing" />

# OB-BAA Scale Testing

This section will describe about the system requirements for the OB-BAA scale testing. In this release OB-BAA is capable of managing 10K ONUs (vONU or eONU).

The values presented in this page are the total times that takes to provision the subscribers in OB-BAA. The requests were sent sequentially to OB-BAA.

### System requirements for Scale testing of 10K ONUs

|RAM|64 GB|
|Root partition size|500 GB|
|CPU(s)|16|
|OS|Ubuntu VM R20.04|


### ONU Creation time
**Time taken to create vONU devices**

| OLT Device | CT | ONU Devices | Time taken | Average time Per ONU |
| :---: | :---:| :--- | :--- | :---|
|OLT 1-7| 1-8 |7168|7 hours 49 mins 31 seconds| 4 seconds |

**Info:** Above stated time is taken when the environment variable NC_ENABLE_POST_EDIT_DS_VALIDATION_SUPPORT in docker-compose/helm chart is set to False, if NC_ENABLE_POST_EDIT_DS_VALIDATION_SUPPORT support is enabled, it is expected that BAA will take longer time to populate the DD.

Requests used to create vONU devices can be found at https://github.com/BroadbandForum/obbaa/tree/master/resources/examples/vomci-end-to-end-config

**Time taken to create eONU devices**

| OLT Device | CT | ONU Devices | Time taken |Average time Per ONU |
| :---: | :---: | :--- | :--- | :---|
|OLT 1-10|1-8|10240|6 hours 10 min 12 second|2 seconds |

Create OLT and create OLT infra rpc requests remains same as vONU configurations at https://github.com/BroadbandForum/obbaa/tree/master/resources/examples/vomci-end-to-end-config

To create the eONU under OLT use the below mentioned netconf rpc.

```
<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="9${test.olt.msg.id}${test.olt.id}0">
  <edit-config xmlns:xc="urn:ietf:params:xml:ns:netconf:base:1.0">
    <target>
      <running/>
    </target>
    <config>
      <network-manager xmlns="urn:bbf:yang:obbaa:network-manager">
        <managed-devices>
          <device>
            <name>OLT1</name>
            <root>
              <interfaces xmlns="urn:ietf:params:xml:ns:yang:ietf-interfaces">
                <!-- olt-side vani -->
                <interface>
                  <name>VANI_eONU_ABCD12345678</name>
                  <enabled>true</enabled>
                  <type xmlns:bbf-xponift="urn:bbf:yang:bbf-xpon-if-type">bbf-xponift:v-ani</type>
                  <v-ani xmlns="urn:bbf:yang:bbf-xponvani">
                    <channel-partition>CG_1.CPart_1</channel-partition>
                    <onu-id>1</onu-id>
                    <expected-serial-number>ABCD12345678</expected-serial-number>
                    <preferred-channel-pair>CG_1.CPart_1.CPair_gpon</preferred-channel-pair>
                  </v-ani>
                </interface>
                <!-- olt-side venet -->
                <interface>
                  <name>VENET_eONU_ABCD12345678_1_1</name>
                  <type xmlns:bbf-xponift="urn:bbf:yang:bbf-xpon-if-type">bbf-xponift:olt-v-enet</type>
                  <enabled>true</enabled>
                  <olt-v-enet xmlns="urn:bbf:yang:bbf-xponvani">
                    <lower-layer-interface>VANI_eONU_ABCD12345678</lower-layer-interface>
                  </olt-v-enet>
                </interface>
                <!-- onu-side ani -->
                <interface>
                  <name>ANI_eONU_ABCD12345678</name>
                  <type xmlns:bbf-xponift="urn:bbf:yang:bbf-xpon-if-type">bbf-xponift:ani</type>
                  <enabled>true</enabled>
                  <ani xmlns="urn:bbf:yang:bbf-xponani">
                    <management-gemport-aes-indicator>false</management-gemport-aes-indicator>
                    <upstream-fec>true</upstream-fec>
                    <onu-id>1</onu-id>
                  </ani>
                </interface>
                <!-- onu-side uni interface -->
                <interface>
                  <name>ENET_eONU_ABCD12345678_1_1</name>
                  <type xmlns:ianaift="urn:ietf:params:xml:ns:yang:iana-if-type">ianaift:ethernetCsmacd</type>
                  <enabled>true</enabled>
                </interface>
              </interfaces>
              <!-- ani<->vani linktable -->
              <link-table xmlns="urn:bbf:yang:bbf-link-table">
                <link-table>
                  <from-interface>ANI_eONU_ABCD12345678</from-interface>
                  <to-interface>VANI_eONU_ABCD12345678</to-interface>
                </link-table>
                <link-table>
                  <from-interface>ENET_eONU_ABCD12345678_1_1</from-interface>
                  <to-interface>VENET_eONU_ABCD12345678_1_1</to-interface>
                </link-table>
              </link-table>
            </root>
          </device>
        </managed-devices>
      </network-manager>
    </config>
  </edit-config>
</rpc>

```
**Info:** Above stated time data to populate OB-BAA DB with number of ONU device creation.

[<--Using OB-BAA](../index.md#using)