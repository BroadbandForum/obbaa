
<a id="vomci_onu" />

# Examples for managing ONUs via vOMCI

## vOMCI Demonstration
This section describes how to perform an end-to-end demonstration of ONU
management through vOMCI using OLT and ONU simulators.

The examples referred in this section can be found in
*resources/examples/vomci-end-to-end-config*.

**Pre-requisites:**

-   Running instances of BAA core (contains the vOLTMF), vOMCI, vOMCI
    Proxy

    -   The the docker-compose file present in *baa-dist* should be
        used, since it contains initialization parameters that match the
        examples below.

-   A pOLT simulator instance and an ONU simulator instance running

    -   Use the following docker-compose file:
        *resources/examples/vomci-end-to-end-config/docker-compose-simulators.yml*

**Steps**

* Create OLT in BAA - *1-create-olt.xml*

*    Hostnames are not yet supported, so the IP address in the
        request must match the IP address of the *polt-simulator* (Use
        the *docker inspect* command)
        <p align="left">
         <img width="450px" height="115px" src="{{site.url}}/using/vomci_onu/polt_sim_ip.png">
        </p>
* Configure infrastructure in OLT - *2-create-infra-std-2.1.xml*

* Configure GRPC settings in OLT - *3-grpc-settings-olt.xml*

* Check that the OLT is connected and aligned -
    *4-get-alignment-status-olt.xml*

  At this point the following log should appear in the vOMCI Proxy logs:

  ```
  control-vm:~$ docker logs obbaa-vproxy 
  (...)
  INFO:omcc.grpc.grpc_server:vOMCI bbf-vproxy received hello from olt-grpc-2 at ipv4:172.18.0.3:53756
  ```

* Configure vOLTMF connectivity to Network Functions -
    *5-network-functions.xml*

*  Create ONU - *6-create-onu-use-vomci.xml*

* Configure ONU in BAA - *7-configure-onu-std-1.0.xml*

* Trigger ONU state change in OLT simulator.
    Attach to the polt-simulator using the following command

    ```
    docker attach polt-simulator
    ```

    Run the following commands in the polt-simulator terminal. This will connect to the ONU simulator and trigger the ONU state change
    notification. The ONU simulator IP address must be retrieved with the *docker inspect* command.

    ```
    /po/rx_mode mode=onu_sim onu_sim_ip=172.18.0.2 onu_sim_port=50000
    /po/onu channel_term=CT_1 onu_id=1 serial_vendor_id=ABCD serial_vendor_specific=12345678 management_state=relying-on-vomci loid=test flags=present+in_o5+expected
    ```

* Check the ONU alignment status using *8-get-alignment-status-onu.xml*. At this point the configurations should have been applied to the ONU and the ONU should appear as connected and aligned.

## Retrieving state data

This section describes how to get state data from an ONU.

**Pre-requisites:**

-   The ONU is managed through vOMCI and it is reported as connected and aligned in OB-BAA

**Steps**

* Use the example request to get the ONU\'s state data
    *7-get-state-data.xml*

* In this release, the vOMCI function only supports getting the
    software version of the ONU. An example reply would be:

    ```
    <rpc-reply message-id="110" xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
        <data>
            <hw-state:hardware xmlns:hw-state="urn:ietf:params:xml:ns:yang:ietf-hardware-state">
                <hw-state:component>
                    <hw-state:name>ont1</hw-state:name>
                    <hw-state:hardware-rev>HWREV_1</hw-state:hardware-rev>
                    <hw-state:mfg-name>manufacturer1</hw-state:mfg-name>
                    <hw-state:model-name>EqptModelName_1</hw-state:model-name>
                    <hw-state:class xmlns:ianahw="urn:ietf:params:xml:ns:yang:iana-hardware">ianahw:chassis</hw-state:class>
                    <hw-state:serial-num>ABCD12345678</hw-state:serial-num>
                </hw-state:component>
            </hw-state:hardware>
            <if:interfaces-state xmlns:if="urn:ietf:params:xml:ns:yang:ietf-interfaces">
                <if:interface>
                    <if:name>ont1_uni1</if:name>
                    <if:oper-status>up</if:oper-status>
                </if:interface>
            </if:interfaces-state>
            <hw:hardware xmlns:hw="urn:ietf:params:xml:ns:yang:ietf-hardware">
                <hw:component>
                    <hw:name>ont1</hw:name>
                    <bbf-swm:software xmlns:bbf-swm="urn:bbf:yang:bbf-software-management">
                        <bbf-swm:software>
                            <bbf-swm:name>model1-software</bbf-swm:name>
                            <bbf-swm:revisions>
                                <bbf-swm:maximum-number-of-revisions>2</bbf-swm:maximum-number-of-revisions>
                                <bbf-swm:revision>
                                    <bbf-swm:id>1</bbf-swm:id>
                                    <bbf-swm:last-changed>2022-01-09T13:53:36+00:00</bbf-swm:last-changed>
                                    <bbf-swm:is-valid>true</bbf-swm:is-valid>
                                    <bbf-swm:alias>model1-software-rev1</bbf-swm:alias>
                                    <bbf-swm:is-active>true</bbf-swm:is-active>
                                    <bbf-swm:is-committed>true</bbf-swm:is-committed>
                                    <bbf-swm:state>in-use</bbf-swm:state>
                                    <bbf-swm:product-code>pcode</bbf-swm:product-code>
                                    <bbf-swm:version>1.0.0</bbf-swm:version>
                                    <bbf-swm:hash>123456789</bbf-swm:hash>
                                </bbf-swm:revision>
                                <bbf-swm:revision>
                                    <bbf-swm:id>2</bbf-swm:id>
                                    <bbf-swm:last-changed>2022-01-09T13:55:36+00:00</bbf-swm:last-changed>
                                    <bbf-swm:is-valid>true</bbf-swm:is-valid>
                                    <bbf-swm:alias>model1-software-rev2</bbf-swm:alias>
                                    <bbf-swm:is-active>false</bbf-swm:is-active>
                                    <bbf-swm:is-committed>false</bbf-swm:is-committed>
                                    <bbf-swm:state>available</bbf-swm:state>
                                    <bbf-swm:product-code>pcode</bbf-swm:product-code>
                                    <bbf-swm:version>2.0.0</bbf-swm:version>
                                    <bbf-swm:hash>134323233</bbf-swm:hash>
                                </bbf-swm:revision>
                            </bbf-swm:revisions>
                        </bbf-swm:software>
                    </bbf-swm:software>
                </hw:component>
            </hw:hardware>
        </data>
    ```
[<--Using OB-BAA](../index.md#using)
