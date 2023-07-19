<a id="model_abstracter" />

Model abstracter
====================


# Enable/Disable Model Abstracter with env-variable MODEL_ABSTRACTER_STATUS
The environment variable MODEL_ABSTRACTER_STATUS is used in two scenarios.

## Docker compose file

We could use docker-compose command to deploy OB-BAA. And add MODEL_ABSTRACTER_STATUS variable in node of services.baa.environment as follows. It's only a part of docker-compose.yml file to demonstrate the usage.

~~~
version: '3.5'
networks:
    baadist_default:
        driver: bridge
        name: baadist_default
services:
    baa:
        image: baa
        build: ./
        container_name: baa
        restart: always
        ports:
            - "8080:8080"
            - "5005:5005"
            - "9292:9292"
            - "4335:4335"
            - "162:162/udp"
        environment:
            #- EXTRA_JAVA_OPTS=-Xms128M -Xmx512M -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005
            - BAA_USER=admin
            - BAA_USER_PASSWORD=password
            #Possible Values for PMA_SESSION_FACTORY_TYPE are REGULAR,TRANSPARENT, Default value is REGULAR
            - PMA_SESSION_FACTORY_TYPE=REGULAR
            - MAXIMUM_ALLOWED_ADAPTER_VERSIONS=3
            - VOLTMF_NAME=vOLTMF
            # Enable Model Abstracter or Disable Model Abstracter, Default value is Disable
            - MODEL_ABSTRACTER_STATUS=Disable
            # Below tag shall be set as false if the BAA is going to be tested for Scalability/Performance
            - NC_ENABLE_POST_EDIT_DS_VALIDATION_SUPPORT=True
~~~

## Helm charts file
The helm chart file is used in Kubenetes, and need to add the variable in the deployment.yml of the baa chart. This variable should be added in the node of env as follows. It's also a part of the deployment.yml file.
~~~
spec:
  strategy:
    type: Recreate
  replicas: 1
  selector:
    matchLabels:
      app: baa
  template:
    metadata:
      labels:
        app: baa
    spec:
      volumes:
        - name: baa-store
          persistentVolumeClaim:
            claimName: baa-pvclaim
      hostname: baa
      containers:
        - name: baa
          image: broadbandforum/baa:latest
          imagePullPolicy: Always
          ports:
            - name: http
              containerPort: 8080
              protocol: TCP
            - name: debug
              containerPort: 5005
              protocol: TCP
            - name: ncnbissh
              containerPort: 9292
              protocol: TCP
            - name: callhometls
              containerPort: 4335
              protocol: TCP
            - name: snmp
              containerPort: 162
              protocol: UDP
          stdin: true
          tty: true
          env:
            - name: BAA_USER
              value: admin
            - name: BAA_USER_PASSWORD
              value: password
            - name: PMA_SESSION_FACTORY_TYPE
              value: REGULAR
            - name: VOLTMF_NAME
              value: vOLTMF
            # Enable Model Abstracter or Disable Model Abstracter, Default value is Disable
            - name: MODEL_ABSTRACTER_STATUS
              value: Disable
~~~

# L2 service configuration

The following describes how to config L2 service using the new YANG model.
Note: the steps related to environment preparation which contains the pOLT and onu simulator are omitted below.


1. Add OLT and ONU using 'vomci-end-to-end-config'.
2. Add ani and v-ani interfaces.
3. Create a link table between ani and v-ani.
4. Prepare profiles, e.g., line profile, service profile, and so on.
5. Create OLT node and TPs.
6. Create ONT node and TPs and LINK.
7. Create L2-V-UNI and L2-V-NNI TPs.
8. Create a link-only 1:1 scenario, this step is optional if you decide to create an N:1 scenario.
9. Create an N:1 VLAN forwarding profile.
10. Create N:1 L2 vlan tp.
11. Create a link in the N:1 scenario.

The XML samples mentioned above are as follows:
- add-interfaces-onu

~~~
<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="3">
    <edit-config xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
        <target>
            <running />
        </target>
        <config>
            <network-manager xmlns="urn:bbf:yang:obbaa:network-manager">
                <managed-devices>
                    <device>
                        <name>OLT1</name>
                        <root>
                            <if:interfaces xmlns:if="urn:ietf:params:xml:ns:yang:ietf-interfaces">
                                <if:interface>
                                    <if:name>v-ani.OLT1.onu1</if:name>
                                    <if:type xmlns:bbf-xponift="urn:bbf:yang:bbf-xpon-if-type">bbf-xponift:v-ani</if:type>
                                    <bbf-xponvani:v-ani xmlns:bbf-xponvani="urn:bbf:yang:bbf-xponvani">
                                        <bbf-xponvani:expected-serial-number>ABCD12345678</bbf-xponvani:expected-serial-number>
                                    </bbf-xponvani:v-ani>
                                </if:interface>
                                <if:interface>
                                    <if:name>ani.OLT1.onu1</if:name>
                                    <if:type xmlns:bbf-xponift="urn:bbf:yang:bbf-xpon-if-type">bbf-xponift:ani</if:type>
                                    <bbf-xponani:ani xmlns:bbf-xponani="urn:bbf:yang:bbf-xponani">
                                    </bbf-xponani:ani>
                                </if:interface>
                            </if:interfaces>
                        </root>
                    </device>
                </managed-devices>
            </network-manager>
        </config>
    </edit-config>
</rpc>
~~~

- create-link-table
~~~
<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="4">
    <edit-config xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
        <target>
            <running/>
        </target>
        <config>
            <network-manager xmlns="urn:bbf:yang:obbaa:network-manager">
                <managed-devices>
                    <device>
                        <name>OLT1</name>
                        <root>
                            <bbf-lt:link-table xmlns:bbf-lt="urn:bbf:yang:bbf-link-table">
                                <bbf-lt:link-table>
                                    <bbf-lt:from-interface>ani.OLT1.onu1</bbf-lt:from-interface>
                                    <bbf-lt:to-interface>v-ani.OLT1.onu1</bbf-lt:to-interface>
                                </bbf-lt:link-table>
                            </bbf-lt:link-table>
                        </root>
                    </device>
                </managed-devices>
            </network-manager>
        </config>
    </edit-config>
</rpc>
~~~

-create-line-bandwidth-profile
~~~
<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="5">
    <edit-config>
        <target>
            <running/>
        </target>
        <config>
            <line-bandwidth-profiles xmlns="urn:bbf:yang:obbaa:nt-line-profile">
                <line-bandwidth-profile>
                    <name>bandwidth-profile-1</name>
                    <fixed-bandwidth>102400</fixed-bandwidth>
                    <assured-bandwidth>102400</assured-bandwidth>
                    <maximum-bandwidth>204800</maximum-bandwidth>
                </line-bandwidth-profile>
            </line-bandwidth-profiles>
        </config>
    </edit-config>
</rpc>
~~~

- create-line-profile
~~~
<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="6">
    <edit-config>
        <target>
            <running/>
        </target>
        <config>
            <line-profiles xmlns="urn:bbf:yang:obbaa:nt-line-profile">
                <line-profile>
                    <name>line-profile-1</name>
                    <virtual-ports>
                        <virtual-port>
                            <name>v-nni1</name>
                            <line-bandwidth-ref>bandwidth-profile-1</line-bandwidth-ref>
                            <match-criteria>
                                <name>mapping-index-1</name>
                                <vlan>200</vlan>
                            </match-criteria>
                        </virtual-port>
                    </virtual-ports>
                </line-profile>
            </line-profiles>
        </config>
    </edit-config>
</rpc>
~~~

- create-vlan-translation-profile
~~~
<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="7">
    <edit-config>
        <target>
            <running/>
        </target>
        <config>
            <vlan-translation-profiles xmlns="urn:bbf:yang:obbaa:l2-access-common">
                <vlan-translation-profile>
                    <name>vlan-trans-profile-1</name>
                    <match-criteria>
                        <outer-tag>
                            <vlan-id>100</vlan-id>
                            <pbit>1</pbit>
                        </outer-tag>
                    </match-criteria>
                    <ingress-rewrite>
                        <pop-tags>0</pop-tags>
                        <push-outer-tag>
                            <vlan-id>200</vlan-id>
                            <pbit>0</pbit>
                        </push-outer-tag>
                    </ingress-rewrite>
                </vlan-translation-profile>
                <vlan-translation-profile>
                    <name>vlan-trans-profile-2</name>
                    <match-criteria>
                        <outer-tag>
                            <vlan-id>200</vlan-id>
                            <pbit>1</pbit>
                        </outer-tag>
                    </match-criteria>
                    <ingress-rewrite>
                        <pop-tags>0</pop-tags>
                        <push-outer-tag>
                            <vlan-id>300</vlan-id>
                            <pbit>0</pbit>
                        </push-outer-tag>
                    </ingress-rewrite>
                </vlan-translation-profile>
                <vlan-translation-profile>
                    <name>vlan-trans-profile-3</name>
                    <match-criteria>
                        <outer-tag>
                            <vlan-id>300</vlan-id>
                            <pbit>1</pbit>
                        </outer-tag>
                    </match-criteria>
                    <ingress-rewrite>
                        <pop-tags>0</pop-tags>
                        <push-outer-tag>
                            <vlan-id>100</vlan-id>
                            <pbit>0</pbit>
                        </push-outer-tag>
                    </ingress-rewrite>
                </vlan-translation-profile>
            </vlan-translation-profiles>
        </config>
    </edit-config>
</rpc>
~~~

- create-service-profile

~~~
<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="8">
    <edit-config>
        <target>
            <running/>
        </target>
        <config>
            <service-profiles xmlns="urn:bbf:yang:obbaa:nt-service-profile">
                <service-profile>
                    <name>service-profile-1</name>
                    <ports>
                        <port>
                            <name>eth.1</name>
                            <port-vlans>
                                <port-vlan>
                                    <name>vlan-trans-profile-1</name>
                                </port-vlan>
                            </port-vlans>
                        </port>
                    </ports>
                </service-profile>
            </service-profiles>
        </config>
    </edit-config>
</rpc>
~~~

- create-OLT-node-and-TPs
~~~
<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="9">
    <edit-config>
        <target>
            <running/>
        </target>
        <config>
            <networks xmlns="urn:ietf:params:xml:ns:yang:ietf-network" xmlns:nt="urn:ietf:params:xml:ns:yang:ietf-network-topology" xmlns:bbf-an-nw-topology="urn:bbf:yang:obbaa:an-network-topology">
                <network>
                    <network-id>network-fan1</network-id>
                    <supporting-network>
                        <network-ref>network-fan1</network-ref>
                    </supporting-network>
                    <node>
                        <node-id>OLT1</node-id>
                        <supporting-node>
                            <network-ref>network-fan1</network-ref>
                            <node-ref>p-OLT1</node-ref>
                        </supporting-node>
                        <nt:termination-point>
                            <nt:tp-id>uni1</nt:tp-id>
                            <nt:supporting-termination-point>
                                <nt:network-ref>network-fan1</nt:network-ref>
                                <nt:node-ref>OLT1</nt:node-ref>
                                <nt:tp-ref>pon-tp1</nt:tp-ref>
                            </nt:supporting-termination-point>
                            <bbf-an-nw-topology:tp-type>uni</bbf-an-nw-topology:tp-type>
                        </nt:termination-point>
                        <nt:termination-point>
                            <nt:tp-id>nni1</nt:tp-id>
                            <nt:supporting-termination-point>
                                <nt:network-ref>network-fan1</nt:network-ref>
                                <nt:node-ref>OLT1</nt:node-ref>
                                <nt:tp-ref>eth-tp1</nt:tp-ref>
                            </nt:supporting-termination-point>
                            <bbf-an-nw-topology:tp-type>nni</bbf-an-nw-topology:tp-type>
                        </nt:termination-point>
                    </node>
                </network>
            </networks>
        </config>
    </edit-config>
</rpc>
~~~

- create-ONU-node-and-TPs-and-LINK
~~~
<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="10">
    <edit-config>
        <target>
            <running/>
        </target>
        <config>
            <networks xmlns="urn:ietf:params:xml:ns:yang:ietf-network" xmlns:nt="urn:ietf:params:xml:ns:yang:ietf-network-topology" xmlns:bbf-an-nw-topology="urn:bbf:yang:obbaa:an-network-topology">
                <network>
                    <network-id>network-fan1</network-id>
                    <node>
                        <node-id>onu1</node-id>
                        <bbf-an-nw-topology:access-node-attributes>
                            <bbf-an-nw-topology:nt-line-profile>line-profile-1</bbf-an-nw-topology:nt-line-profile>
                            <bbf-an-nw-topology:nt-service-profile>service-profile-1</bbf-an-nw-topology:nt-service-profile>
                        </bbf-an-nw-topology:access-node-attributes>
                        <supporting-node>
                            <network-ref>network-fan1</network-ref>
                            <node-ref>p-onu1</node-ref>
                        </supporting-node>
                        <nt:termination-point>
                            <nt:tp-id>uni1</nt:tp-id>
                            <nt:supporting-termination-point>
                                <nt:network-ref>network-fan1</nt:network-ref>
                                <nt:node-ref>onu1</nt:node-ref>
                                <nt:tp-ref>lan-port-tp1</nt:tp-ref>
                            </nt:supporting-termination-point>
                            <bbf-an-nw-topology:tp-type>uni</bbf-an-nw-topology:tp-type>
                        </nt:termination-point>
                        <nt:termination-point>
                            <nt:tp-id>nni1</nt:tp-id>
                            <nt:supporting-termination-point>
                                <nt:network-ref>network-fan1</nt:network-ref>
                                <nt:node-ref>onu1</nt:node-ref>
                                <nt:tp-ref>wan-port-tp1</nt:tp-ref>
                            </nt:supporting-termination-point>
                            <bbf-an-nw-topology:tp-type>nni</bbf-an-nw-topology:tp-type>
                        </nt:termination-point>
                    </node>
                    <nt:link>
                        <nt:link-id>link.OLT1.onu1</nt:link-id>
                        <nt:source>
                            <nt:source-node>OLT1</nt:source-node>
                            <nt:source-tp>uni1</nt:source-tp>
                        </nt:source>
                        <nt:destination>
                            <nt:dest-node>onu1</nt:dest-node>
                            <nt:dest-tp>nni1</nt:dest-tp>
                        </nt:destination>
                        <bbf-an-nw-topology:access-link-attributes>
                            <bbf-an-nw-topology:link-direction>bidirectional</bbf-an-nw-topology:link-direction>
                        </bbf-an-nw-topology:access-link-attributes>
                    </nt:link>
                </network>
            </networks>
        </config>
    </edit-config>
</rpc>
~~~

- create-1-1-l2-v-uni-and-nni-tp E
~~~
<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="11">
    <edit-config>
        <target>
            <running/>
        </target>
        <config>
            <networks xmlns="urn:ietf:params:xml:ns:yang:ietf-network" xmlns:bbf-l2t="urn:bbf:yang:obbaa:l2-topology" xmlns:l2t="urn:ietf:params:xml:ns:yang:ietf-l2-topology" xmlns:bbf-an-nw-topology="urn:bbf:yang:obbaa:an-network-topology" xmlns:nt="urn:ietf:params:xml:ns:yang:ietf-network-topology">
                <network>
                    <network-id>network-fan1</network-id>
                    <node>
                        <node-id>onu1</node-id>
                        <nt:termination-point>
                            <nt:tp-id>l2-v-uni1</nt:tp-id>
                            <nt:supporting-termination-point>
                                <nt:network-ref>network-fan1</nt:network-ref>
                                <nt:node-ref>onu1</nt:node-ref>
                                <nt:tp-ref>v-nni1</nt:tp-ref>
                            </nt:supporting-termination-point>
                            <bbf-an-nw-topology:tp-type xmlns:bbf-tp-types="urn:bbf:yang:obbaa:tp-types">bbf-tp-types:l2-v-uni</bbf-an-nw-topology:tp-type>
                            <l2t:l2-termination-point-attributes>
                                <bbf-l2t:l2-access-attributes>
                                    <bbf-l2t:vlan-translation>
                                        <bbf-l2t:translation-profile>vlan-trans-profile-2</bbf-l2t:translation-profile>
                                    </bbf-l2t:vlan-translation>
                                </bbf-l2t:l2-access-attributes>
                            </l2t:l2-termination-point-attributes>
                        </nt:termination-point>
                    </node>
                    <node>
                        <node-id>OLT1</node-id>
                        <nt:termination-point>
                            <nt:tp-id>l2-v-nni1</nt:tp-id>
                            <nt:supporting-termination-point>
                                <nt:network-ref>network-fan1</nt:network-ref>
                                <nt:node-ref>OLT1</nt:node-ref>
                                <nt:tp-ref>nni1</nt:tp-ref>
                            </nt:supporting-termination-point>
                            <bbf-an-nw-topology:tp-type xmlns:bbf-tp-types="urn:bbf:yang:obbaa:tp-types">bbf-tp-types:l2-v-nni</bbf-an-nw-topology:tp-type>
                            <l2t:l2-termination-point-attributes>
                                <bbf-l2t:l2-access-attributes>
                                    <bbf-l2t:vlan-translation>
                                        <bbf-l2t:translation-profile>vlan-trans-profile-3</bbf-l2t:translation-profile>
                                    </bbf-l2t:vlan-translation>
                                </bbf-l2t:l2-access-attributes>
                            </l2t:l2-termination-point-attributes>
                        </nt:termination-point>
                    </node>
                </network>
            </networks>
        </config>
    </edit-config>
</rpc>
~~~

- optional-create-link-only-1-1
~~~
<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="12">
    <edit-config>
        <target>
            <running/>
        </target>
        <config>
            <networks xmlns="urn:ietf:params:xml:ns:yang:ietf-network" xmlns:bbf-an-nw-topology="urn:bbf:yang:obbaa:an-network-topology" xmlns:nt="urn:ietf:params:xml:ns:yang:ietf-network-topology">
                <network>
                    <network-id>network-fan1</network-id>
                    <nt:link>
                        <nt:link-id>l2-v-uni1-l2-v-nni1</nt:link-id>
                        <nt:source>
                            <nt:source-node>onu1</nt:source-node>
                            <nt:source-tp>l2-v-uni1</nt:source-tp>
                        </nt:source>
                        <nt:destination>
                            <nt:dest-node>OLT1</nt:dest-node>
                            <nt:dest-tp>l2-v-nni1</nt:dest-tp>
                        </nt:destination>
                        <bbf-an-nw-topology:access-link-attributes>
                            <bbf-an-nw-topology:link-direction>bidirectional</bbf-an-nw-topology:link-direction>
                        </bbf-an-nw-topology:access-link-attributes>
                    </nt:link>
                </network>
            </networks>
        </config>
    </edit-config>
</rpc>
~~~

From here, it's the N:1 scenario, and this profile is only used in N:1 scenario.
- create-N-1-vlan-forwarding-profile
~~~
<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="13">
    <edit-config>
        <target>
            <running/>
        </target>
        <config>
            <vlan-forwarding-profiles xmlns="urn:bbf:yang:obbaa:l2-access-common">
                <vlan-forwarding-profile>
                    <name>vlan-fwd-profile1</name>
                    <forwarding-ports>
                        <port>
                            <name>fport1</name>
                            <node-id>OLT1</node-id>
                            <tp-id>nni1</tp-id>
                        </port>
                    </forwarding-ports>
                </vlan-forwarding-profile>
            </vlan-forwarding-profiles>
        </config>
    </edit-config>
</rpc>
~~~

- create-N-1-l2-vlan-tp
~~~
<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="14">
    <edit-config>
        <target>
            <running/>
        </target>
        <config>
            <networks xmlns="urn:ietf:params:xml:ns:yang:ietf-network" xmlns:nt="urn:ietf:params:xml:ns:yang:ietf-network-topology" xmlns:bbf-l2t="urn:bbf:yang:obbaa:l2-topology" xmlns:l2t="urn:ietf:params:xml:ns:yang:ietf-l2-topology">
                <network>
                    <network-id>network-fan1</network-id>
                    <node>
                        <node-id>OLT1</node-id>
                        <nt:termination-point>
                            <nt:tp-id>l2-vlan300</nt:tp-id>
                            <bbf-l2t:tp-type>l2-vlan</bbf-l2t:tp-type>
                            <l2t:l2-termination-point-attributes>
                                <bbf-l2t:forwarding-vlan>
                                    <bbf-l2t:vlan-id>300</bbf-l2t:vlan-id>
                                    <bbf-l2t:forwarding-profile>vlan-fwd-profile1</bbf-l2t:forwarding-profile>
                                </bbf-l2t:forwarding-vlan>
                            </l2t:l2-termination-point-attributes>
                        </nt:termination-point>
                    </node>
                </network>
            </networks>
        </config>
    </edit-config>
</rpc>
~~~

- optional-create-link-only-N-1 
~~~
<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="15">
    <edit-config>
        <target>
            <running/>
        </target>
        <config>
            <networks xmlns="urn:ietf:params:xml:ns:yang:ietf-network" xmlns:nt="urn:ietf:params:xml:ns:yang:ietf-network-topology" xmlns:bbf-an-nw-topology="urn:bbf:yang:obbaa:an-network-topology">
                <network>
                    <network-id>network-fan1</network-id>
                    <nt:link>
                        <nt:link-id>l2-v-uni2-l2-vlan300</nt:link-id>
                        <nt:source>
                            <nt:source-node>onu1</nt:source-node>
                            <nt:source-tp>l2-v-uni1</nt:source-tp>
                        </nt:source>
                        <nt:destination>
                            <nt:dest-node>OLT1</nt:dest-node>
                            <nt:dest-tp>l2-vlan300</nt:dest-tp>
                        </nt:destination>
                        <bbf-an-nw-topology:access-link-attributes>
                            <bbf-an-nw-topology:link-direction>bidirectional</bbf-an-nw-topology:link-direction>
                        </bbf-an-nw-topology:access-link-attributes>
                    </nt:link>
                </network>
            </networks>
        </config>
    </edit-config>
</rpc>

~~~
