
<a id="ipfixpmc" />

Performance Monitoring (PM) Collector for IPFIX Messages
===============================

In the BAA layer, the PM collector is responsible for
collecting performance monitoring data (e.g., IPFIX messages) from
managed pANs, decode and provide formatted data to data handlers for
further processing and storage of data. The IPFIX PM Data Collector
(IPFIX Collector) is based on IETF\'s [RFC
7011](http://specification%20of%20the%20ip%20flow%20information%20e/) IPFIX
protocol specification and mechanisms as IPFIX is the method defined by
the BBF\'s TR-413 specification.

## Pre-requisites for supporting an IPFIX stream


-   Vendor adapters should include IPFIX\_IEId.csv mapping file.

-   Access node (pAN) which exports IPFIX stream should be managed by
    BAA.

-   The hostname of the pAN must be the same as the deviceName that is
    managed in BAA.

## Launching IPFIX Collector

IPFIX collector is bundled as docker image and runs as a separate docker
container similar to BAA core service. IPFIX collector docker
configurations can be brought up along with BAA container by using
baa\_setup.yml docker-compose file.

The IPFIX collector listens for the IPFIX stream on default port 4494
that is configurable via the docker-compose file. The docker-compose
file will also include the mount point from where the IPFIX\_IEId.csv
file is located. By default this is /baa/stores/ipfix. Details of BAA
such as BAA IP, BAA port, username and password should also be provided
in the yaml file.

### BAA docker-compose file (baa_setup.yml) with PM Collection

```
###########################################################################
# Copyright 2018-2020 Broadband Forum
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
###########################################################################
version: '3.5'
networks:
    baadist_default:
        driver: bridge
        name: baadist_default
services:
    baa:
        image: baa
        container_name: baa
        restart: always
        ports:
            - "8080:8080"
            - "5005:5005"
            - "9292:9292"
            - "4335:4335"
            - "162:162/udp"
        environment:
            - BAA_USER=admin
            - BAA_USER_PASSWORD=password
            #Possible Values for PMA_SESSION_FACTORY_TYPE are REGULAR,TRANSPARENT, Default value is REGULAR
            - PMA_SESSION_FACTORY_TYPE=REGULAR
            - MAXIMUM_ALLOWED_ADAPTER_VERSIONS=3
        volumes:
            - /baa/stores:/baa/stores
        networks:
            - baadist_default

    ipfix-collector:
        image: ipfix-collector
        container_name: ipfix-collector
        restart: always
        ports:
            - "8005:5005"
            - "4494:4494"
            - "5051:5051"
        environment:
            - IPFIX_COLLECTOR_PORT=4494
            - IPFIX_IE_MAPPING_DIR=/ipfix/ie-mapping/
            - IPFIX_COLLECTOR_MAX_CONNECTION=10000
            - BAA_HOST=baa
            - BAA_SSH_PORT=9292
            - BAA_USERNAME=admin
            - BAA_PASSWORD=password
            - DEBUG=true
            - INFLUXDB_ORGANISATION=broadband_forum
            - INFLUXDB_BUCKETID=pm-collection
            - INFLUXDB_API_URL=http://obbaa-influxdb:9999
            - INFLUXDB_TOKEN=_6Mb0Td0U5pbKecnJZ0ajSSw3uGJZggVpLmr9WDdAbXsTDImNZI3pO3zj5OgJtoiGXV6-1HGD5E8xi_4GwFw-g==
            - PMD_MAX_BUFFERED_POINTS=5000
            - PMD_MAX_BUFFERED_MEASUREMENTS=100
            - PMD_TIMEOUT_BUFFERED_POINTS=60
            - PMD_NBI_PORT=5051
        volumes:
            - /baa/stores/ipfix:/ipfix/ie-mapping/
        depends_on:
            - baa
        networks:
            - baadist_default

    influxdb:
        image: broadbandforum/influxdb:2.0.0-beta.2-3
        container_name: obbaa-influxdb
        command: --bolt-path /var/opt/influxdb/influxd.bolt --engine-path /var/opt/influxdb/engine --reporting-disabled
        restart: on-failure
        ports:
            - "0.0.0.0:9999:9999"
        environment:
            - DEBUG=true
            - INFLUX_USER=influxdb
            - INFLUX_PW=influxdb
            - INFLUX_ORG=broadband_forum
            - INFLUX_BUCKET=pm-collection
            - INFLUX_RETENTION=720
            - INFLUX_PORT=9999
            - INFLUX_ROOT=/var/opt/influxdb
        volumes:
            - /baa/stores/influxdb:/var/opt/influxdb
        depends_on:
            - baa
        networks:
            - baadist_default

```

### Launch BAA docker container

```
docker-compose -f baa_setup.yml up -d
```

### Enabling PM Export on an AN
IPFIX Exporting process should be enabled on pAN by BAA with the destination IP and port of the IPFIX collector mentioned.

```
<rpc	xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="1527307907169">
  <edit-config>
    <target>
      <running/>
    </target>
    <config>
      <network-manager				xmlns="urn:bbf:yang:obbaa:network-manager">
        <managed-devices>
          <device>
            <name>deviceA</name>
            <root>
              <ipfix								xmlns="urn:ietf:params:xml:ns:yang:ietf-ipfix-psamp">
                <observationPoint>
                  <name>OP at eth0 (ingress)</name>
                  <observationDomainId>123</observationDomainId>
                  <ifName>eth0</ifName>
                  <direction>ingress</direction>
                  <selectionProcess>Count-based packet selection</selectionProcess>
                </observationPoint>
                <observationPoint>
                  <name>OP at eth1</name>
                  <observationDomainId>456</observationDomainId>
                  <ifName>eth1</ifName>
                  <selectionProcess>All packet selection</selectionProcess>
                </observationPoint>
                <selectionProcess>
                  <name>Count-based packet selection</name>
                  <selector>
                    <name>Count-based sampler</name>
                    <sampCountBased>
                      <packetInterval>1</packetInterval>
                      <packetSpace>99</packetSpace>
                    </sampCountBased>
                  </selector>
                  <cache>Flow cache</cache>
                </selectionProcess>
                <selectionProcess>
                  <name>All packet selection</name>
                  <selector>
                    <name>Select all</name>
                    <selectAll/>
                  </selector>
                  <cache>Flow cache</cache>
                </selectionProcess>
                <cache>
                  <name>Flow cache</name>
                  <permanentCache>
                    <maxFlows>4096</maxFlows>
                    <exportInterval>900</exportInterval>
                    <cacheLayout>
                      <cacheField>
                        <name>Field 1</name>
                        <ieName>sourceIPv4Address</ieName>
                        <isFlowKey/>
                      </cacheField>
                      <cacheField>
                        <name>Field 2</name>
                        <ieName>destinationIPv4Address</ieName>
                        <isFlowKey/>
                      </cacheField>
                      <cacheField>
                        <name>Field 3</name>
                        <ieName>protocolIdentifier</ieName>
                        <isFlowKey/>
                      </cacheField>
                      <cacheField>
                        <name>Field 4</name>
                        <ieName>sourceTransportPort</ieName>
                        <isFlowKey/>
                      </cacheField>
                      <cacheField>
                        <name>Field 5</name>
                        <ieName>destinationTransportPort</ieName>
                        <isFlowKey/>
                      </cacheField>
                      <cacheField>
                        <name>Field 6</name>
                        <ieName>flowStartMilliseconds</ieName>
                      </cacheField>
                      <cacheField>
                        <name>Field 7</name>
                        <ieName>flowEndSeconds</ieName>
                      </cacheField>
                      <cacheField>
                        <name>Field 8</name>
                        <ieName>octetDeltaCount</ieName>
                      </cacheField>
                      <cacheField>
                        <name>Field 9</name>
                        <ieName>packetDeltaCount</ieName>
                      </cacheField>
                    </cacheLayout>
                  </permanentCache>
                  <exportingProcess>TCP export</exportingProcess>
                </cache>
                <exportingProcess>
                  <name>TCP export</name>
                  <destination>
                    <name>TCP destination</name>
                    <tcpExporter>
                      <sourceIPAddress>192.0.0.1</sourceIPAddress>
                      <destinationPort>4739</destinationPort>
                      <destinationIPAddress>192.0.2.2</destinationIPAddress>
                      <ipfixVersion>9</ipfixVersion>
                    </tcpExporter>
                  </destination>
                  <options>
                    <name>Options 1</name>
                    <optionsType>selectionSequence</optionsType>
                    <optionsTimeout>0</optionsTimeout>
                  </options>
                  <options>
                    <name>Options 2</name>
                    <optionsType>exportingReliability</optionsType>
                    <optionsTimeout>60000</optionsTimeout>
                  </options>
                </exportingProcess>
              </ipfix>
            </root>
          </device>
        </managed-devices>
      </network-manager>
    </config>
  </edit-config>
</rpc>
```

## Sample Vendor Adapter with PM Model
Vendor adapters should include the PM model or information elements mapping file (IPFIX_IEId.csv). The name of the file should be IPFIX_IEId.csv.

While creating the adapter, the IPFIX_IEId.csv file must be placed in the directory structure as shown below:

<p align="center">
 <img width="400px" height="400px" src="{{site.url}}/using/ipfixpm/pmie_location.png">
</p>

While deploying the adapter, BAA will place IPFIX_IEId.csv file in the common mount point under a folder by name \<vendor\>-\<type\>-\<model\>-\<interfaceVersion\>. If the IPFIX_IEId.csv file is missing when IPFIX collector is decoding messages, then the collector will log an error message that the mapping file for the IPFIX messages from the device is missing.


## PM Data Handler
The IPFIX collector decodes IPFIX messages into the requisite IEs and their associated values. This is then forwarded to Data Handler(s) (IpfixDataHandler) that subscribes to IPFIX Collector. Data handlers can subscribe to IPFIX collector using the interface DataHandlerService. DataHandler should invoke registerIpfixDataHandler and unregisterIpfixDataHandler of the DataHandlerService from their init and destroy methods respectively. DataHandlerServiceImpl implements DataHandlerService which maintains the list of Data handlers.

### Data Handler Service Interface
```
public interface DataHandlerService {

    void registerIpfixDataHandler(IpfixDataHandler dataHandler);

    void unregisterIpfixDataHandler(IpfixDataHandler dataHandler);
}
```

### IPFIX Data Handler Interface
```

public interface IpfixDataHandler{

    void handleIpfixData(String ipfixMessageJson);

}
```

## Developing a PM Data Handler
In this release, OB-BAA provides a [IPFIX PM Data Handler](./index.md#ipfixpmdm) and an example PM Data Handler that registers to IPFIX collector and logs the IPFIX messages received. This example is located in /obbaa/pm-collector/pm-data-handler/pm-data-handler-example. It follows the below structure:

<p align="center">
 <img width="400px" height="400px" src="{{site.url}}/using/ipfixpm/pm_data_handler_struct.png">
</p>

New PM Data handlers can be built as a separate karaf bundle. This module should be placed within PM Data Handler package of PM Collector.

In this pm-data-handler-example you can see that descriptor.xml imports the service reference to DataHandlerService and passes it to its constructor.

### Example Descriptor for PM Data Handler
```
<?xml version="1.0" encoding="UTF-8"?>
<blueprint xmlns="http://www.osgi.org/xmlns/blueprint/v1.0.0" xmlns:jpa="http://aries.apache.org/xmlns/jpa/v2.0.0"
           xmlns:tx="http://aries.apache.org/xmlns/transactions/v1.2.0"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xsi:schemaLocation="http://www.osgi.org/xmlns/blueprint/v1.0.0 https://osgi.org/xmlns/blueprint/v1.0.0/blueprint.xsd"
           default-activation="eager">
    <jpa:enable/>
    <tx:enable-annotations/>
    <reference id="dataHandlerService" interface="org.broadband_forum.obbaa.pm.service.DataHandlerService" availability="mandatory"/>
    <!-- Example data handler -->
    <bean id="dataHandler" class="org.broadband_forum.obbaa.datahandler.IpfixDataHandlerExample"
          init-method="init" destroy-method="destroy">
        <argument ref="dataHandlerService"/>
    </bean>
</blueprint>
```

IpfixDataHandlerExample class of pm-data-handler-example implements IpfixDataHandler interface and implements its method handleIpfixData() where the further processing of the IPFIX messages is performed. init() of this class subscribes to IPFIX collector by calling DataHandlerService#registerIpfixDataHandler() and unsubscribes it by calling DataHandlerService#unregisterIpfixDataHandler().

### IPFIX Data Handler Example Implementation
```
public class IpfixDataHandlerExample implements IpfixDataHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(IpfixDataHandlerExample.class);
    private DataHandlerService m_dataHandlerService;

    public IpfixDataHandlerExample(DataHandlerService dataHandlerService) {
        m_dataHandlerService = dataHandlerService;
    }

    public void init() {
        m_dataHandlerService.registerIpfixDataHandler(this);
    }

    public void destroy() {
        m_dataHandlerService.unregisterIpfixDataHandler(this);
    }

    @Override
    public void handleIpfixData(String ipfixMessageJson) {
        LOGGER.info("Decoded data : " + ipfixMessageJson);
    }
}
```

Karaf bundle of the new PM Data handler can be created by using the maven-bundle-plugin in the pom.xml.
This bundle can be deployed in IPFIX collector by adding this in the features.xml of ipfix-collector-feature.

### IPFIX Collector Feature file
```
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<!--
  ~ Copyright 2018-2020 Broadband Forum
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->
 
<features xmlns="http://karaf.apache.org/xmlns/features/v1.4.0" name="ipfix-collector-features">
 
    <repository>mvn:org.opendaylight.yangtools/features-yangtools/${opendaylight-yangtools.version}/xml/features</repository>
    <repository>mvn:org.apache.karaf.features/enterprise/${apache.karaf.version}/xml/features</repository>
    <repository>mvn:org.ops4j.pax.jdbc/pax-jdbc-features/${pax-jdbc.version}/xml/features</repository>
 
 
    <feature name="ipfix-feature" version="${project.version}" description="IPFIX Collector feature">
        <feature version="${project.version}">third-party-libs-ipfix</feature>
        <feature version="${project.version}">pm-data-handler-feature</feature>
        <bundle>mvn:org.broadband-forum.obbaa.netconf/netconf-api/${project.version}</bundle>
        <bundle>mvn:org.broadband-forum.obbaa.netconf/stack-logging-api/${project.version}</bundle>
        <bundle>mvn:org.broadband-forum.obbaa.netconf/netconf-client/${project.version}</bundle>
        <bundle>mvn:org.broadband-forum.obbaa/pm-service/${project.version}</bundle>
        <bundle>mvn:org.broadband-forum.obbaa.pm-collector.pm-data-handler/pm-data-handler-example/${project.version}</bundle>
        <bundle>mvn:org.broadband-forum.obbaa/ipfix-collector-impl/${project.version}</bundle>
    </feature>
 
    <feature name="third-party-libs-ipfix" version="${project.version}" description="Third party dependencies for IPFIX">
        <feature version="${opendaylight-yangtools.version}">odl-yangtools-parser</feature>
        <feature version="${transaction.version}">transaction</feature>
        <feature version="${transaction-api.version}">transaction-api</feature>
        <feature version="${pax-jdbc.version}">pax-jdbc-config</feature>
        <feature version="${pax-jdbc.version}">pax-jdbc-h2</feature>
        <feature version="${pax-jdbc.version}">pax-jdbc-pool-dbcp2</feature>
        <feature version="${pax-jdbc.version}">pax-jdbc-mariadb</feature>
        <feature version="${pax-jdbc.version}">pax-jdbc-hsqldb</feature>
        <feature version="${jpa.version}">jpa</feature>
        <feature version="${hibernate.version}" prerequisite="true">hibernate</feature>
        <feature version="${pax-http-whiteboard.version}">pax-http-whiteboard</feature>
        <feature>jndi</feature>
        <feature>jdbc</feature>
 
        <bundle>mvn:org.apache.commons/commons-pool2/${commons-pool2.version}</bundle>
        <bundle>mvn:com.google.guava/guava/${guava.version}</bundle>
        <bundle><![CDATA[wrap:mvn:com.google.errorprone/error_prone_annotations/${errorprone.version}$Bundle-SymbolicName=errorprone-annotations&Bundle-Version=${errorprone.version}]]></bundle>
        <bundle><![CDATA[wrap:mvn:com.google.j2objc/j2objc-annotations/${j2objc-annotations.version}$Bundle-SymbolicName=j2objc-annotations&Bundle-Version=${j2objc-annotations.version}]]></bundle>
        <bundle><![CDATA[wrap:mvn:org.codehaus.mojo/animal-sniffer-annotations/${animal-sniffer-annotation.version}$Bundle-SymbolicName=animalsniffer-annotations&Bundle-Version=${animal-sniffer-annotation.version}]]></bundle>
        <bundle>mvn:io.netty/netty-handler/${netty.version}</bundle>
        <bundle>mvn:io.netty/netty-common/${netty.version}</bundle>
        <bundle>mvn:io.netty/netty-codec/${netty.version}</bundle>
        <bundle>mvn:io.netty/netty-transport/${netty.version}</bundle>
        <bundle>mvn:io.netty/netty-resolver/${netty.version}</bundle>
        <bundle>mvn:io.netty/netty-buffer/${netty.version}</bundle>
        <bundle>mvn:org.bouncycastle/bcprov-jdk16/${bouncy-castle.version}</bundle>
        <bundle>mvn:joda-time/joda-time/${joda-time.version}</bundle>
        <bundle>mvn:org.apache.sshd/sshd-core/${mina.version}</bundle>
        <bundle>mvn:commons-io/commons-io/${commons-io.version}</bundle>
        <bundle>mvn:commons-lang/commons-lang/${commons.lang.version}</bundle>
        <bundle>mvn:commons-collections/commons-collections/${commons-collection.version}</bundle>
        <bundle>mvn:commons-beanutils/commons-beanutils/${commons-beanutils.version}</bundle>
        <bundle>mvn:org.apache.commons/commons-lang3/${commons-lang3.version}</bundle>
        <bundle>mvn:javax.servlet/javax.servlet-api/${javax.servlet-api.version}</bundle>
        <bundle><![CDATA[wrap:mvn:org.jdom/jdom/1.1.3$Bundle-SymbolicName=jdom&Bundle-Version=${jdom.version}]]></bundle>
        <bundle>mvn:commons-jxpath/commons-jxpath/${commons-jxpath.version}</bundle>
        <bundle>mvn:javax.ws.rs/javax.ws.rs-api/${javax.ws.rs-api.version}</bundle>
        <bundle>mvn:org.apache.servicemix.bundles/org.apache.servicemix.bundles.aopalliance/${apache.servicemix.bundles.aopalliance.version}</bundle>
        <bundle start-level="20" dependency="true">mvn:org.apache.servicemix.bundles/org.apache.servicemix.bundles.xmlbeans/${org.apache.servicemix.bundles.xmlbeans.version}</bundle>
        <bundle>mvn:org.apache.servicemix.bundles/org.apache.servicemix.bundles.xmlresolver/${apache.xmlresolver.version}</bundle>
        <bundle>mvn:org.apache.servicemix.bundles/org.apache.servicemix.bundles.jaxp-ri/${jaxp.ri.version}</bundle>
        <bundle>mvn:org.apache.servicemix.bundles/org.apache.servicemix.bundles.xmlbeans/${sm-osgi.xbean.version}</bundle>
        <bundle>mvn:com.google.code.gson/gson/${gson.version}</bundle>
        <bundle>wrap:mvn:net.sourceforge.javacsv/javacsv/${javacsv.version}</bundle>
        <bundle><![CDATA[wrap:mvn:org.apache.commons/commons-collections4/${commons-collections4.version}$Bundle-SymbolicName=commons-collections4&Bundle-Version=${commons-collections4.version}]]></bundle>
        <bundle>mvn:net.sf.ehcache/ehcache/${ehcache.version}</bundle>
    </feature>
 
    <feature name="pm-data-handler-feature" version="${project.version}" description="PM Data Handler">
        <bundle>wrap:mvn:org.broadband-forum.obbaa.pm-collector.pm-data-handler.persistent-data-handler/db-interface/${project.version}</bundle>
        <bundle>mvn:org.broadband-forum.obbaa.pm-collector.pm-data-handler.persistent-data-handler/influxdb-impl/${project.version}</bundle>
        <bundle>mvn:org.broadband-forum.obbaa.pm-collector.pm-data-handler.persistent-data-handler/pm-data-handler-impl/${project.version}</bundle>
        <feature version="${project.version}">third-party-libs-pm-data-handler-feature</feature>
    </feature>
 
    <feature name="netty-feature" version="${project.version}" description="Netty dependencies">
        <bundle>mvn:io.netty/netty-buffer/${netty.version}</bundle>
        <bundle>mvn:io.netty/netty-codec/${netty.version}</bundle>
        <bundle>mvn:io.netty/netty-codec-http/${netty.version}</bundle>
        <bundle>mvn:io.netty/netty-codec-http2/${netty.version}</bundle>
        <bundle>mvn:io.netty/netty-common/${netty.version}</bundle>
        <bundle>mvn:io.netty/netty-handler/${netty.version}</bundle>
        <bundle>mvn:io.netty/netty-resolver/${netty.version}</bundle>
        <bundle>mvn:io.netty/netty-transport/${netty.version}</bundle>
    </feature>
 
    <feature name="grpc-feature" version="${project.version}" description="gRPC dependencies">
        <feature version="${project.version}">netty-feature</feature>
 
        <bundle>wrap:mvn:com.google.auth/google-auth-library-credentials/${google-auth.version}$Bundle-SymbolicName=com.google.auth.google-auth-library-credentials&Bundle-Version=${google-auth.version}</bundle>
        <bundle>wrap:mvn:com.google.auth/google-auth-library-oauth2-http/${google-auth.version}$Bundle-SymbolicName=com.google.auth.google-auth-library-oauth2-http&Bundle-Version=${google-auth.version}</bundle>
 
        <bundle>wrap:mvn:io.grpc/grpc-api/${grpc.version}$Bundle-SymbolicName=io.grpc.grpc-api&Bundle-Version=${grpc.version}&Export-Package=io.grpc;version=${grpc.version}&SPI-Consumer=*</bundle>
        <bundle>wrap:mvn:io.grpc/grpc-auth/${grpc.version}$Bundle-SymbolicName=io.grpc.grpc-auth&Bundle-Version=${grpc.version}&Export-Package=io.grpc.auth;version=${grpc.version}&Import-Package=io.grpc;version=${grpc.version}</bundle>
        <bundle>wrap:mvn:io.grpc/grpc-context/${grpc.version}$Bundle-SymbolicName=io.grpc.grpc-context&Bundle-Version=${grpc.version}&Export-Package=io.grpc;version=${grpc.version}&Import-Package=io.grpc;version=${grpc.version}</bundle>
        <bundle>wrap:mvn:io.grpc/grpc-core/${grpc.version}$Bundle-SymbolicName=io.grpc.grpc-core&Bundle-Version=${grpc.version}&Export-Package=io.grpc.inprocess,io.grpc.util,io.grpc.internal;version=${grpc.version}&Import-Package=io.grpc;version=${grpc.version}&SPI-Provider=*</bundle>
        <bundle>wrap:mvn:io.grpc/grpc-netty/${grpc.version}$Bundle-SymbolicName=io.grpc.grpc-netty&Bundle-Version=${grpc.version}&Export-Package=io.grpc.netty;version=${grpc.version}&Import-Package=io.grpc,io.grpc.internal;version=${grpc.version}</bundle>
        <bundle>wrap:mvn:io.grpc/grpc-okhttp/${grpc.version}$Bundle-SymbolicName=io.grpc.grpc-okhttp&Bundle-Version=${grpc.version}&Export-Package=io.grpc.okhttp,io.grpc.okhttp.internal;version=${grpc.version}&Import-Package=io.grpc,io.grpc.internal,io.grpc.okhttp.internal;version=${grpc.version}&SPI-Provider=*</bundle>
        <bundle>wrap:mvn:io.grpc/grpc-protobuf/${grpc.version}$Bundle-SymbolicName=io.grpc.grpc-protobuf&Bundle-Version=${grpc.version}&Export-Package=io.grpc.protobuf;version=${grpc.version},io.grpc.protobuf.services;version=${grpc.version},io.grpc.services;version=${grpc.version}&Import-Package=io.grpc;version=${grpc.version},io.grpc.stub;version=${grpc.version}</bundle>
        <bundle>wrap:mvn:io.grpc/grpc-stub/${grpc.version}$Bundle-SymbolicName=io.grpc.grpc-stub&Bundle-Version=${grpc.version}&Export-Package=io.grpc.stub.*;version=${grpc.version};version=${grpc.version},io.grpc.stub.annotations;version=${grpc.version}&Import-Package=io.grpc;version=${grpc.version}</bundle>
    </feature>
 
    <!--
    <feature name="grpc-simple-feature" version="${project.version}" description="gRPC dependencies">
        <feature version="${project.version}">netty-feature</feature>
 
        <bundle>wrap:mvn:com.google.auth/google-auth-library-credentials/${google-auth.version}$Bundle-SymbolicName=com.google.auth.google-auth-library-credentials&Bundle-Version=${google-auth.version}</bundle>
        <bundle>wrap:mvn:com.google.auth/google-auth-library-oauth2-http/${google-auth.version}$Bundle-SymbolicName=com.google.auth.google-auth-library-oauth2-http&Bundle-Version=${google-auth.version}</bundle>
 
        <bundle>wrap:mvn:io.grpc/grpc-api/${grpc.version}</bundle>
        <bundle>wrap:mvn:io.grpc/grpc-auth/${grpc.version}</bundle>
        <bundle>wrap:mvn:io.grpc/grpc-context/${grpc.version}</bundle>
        <bundle>wrap:mvn:io.grpc/grpc-core/${grpc.version}</bundle>
        <bundle>wrap:mvn:io.grpc/grpc-netty/${grpc.version}</bundle>
        <bundle>wrap:mvn:io.grpc/grpc-okhttp/${grpc.version}</bundle>
        <bundle>wrap:mvn:io.grpc/grpc-protobuf/${grpc.version}</bundle>
        <bundle>wrap:mvn:io.grpc/grpc-stub/${grpc.version}</bundle>
    </feature>
    -->
 
    <feature name="third-party-libs-pm-data-handler-feature" version="${project.version}" description="Third party dependencies for PM Data Handler">
        <feature version="${project.version}">netty-feature</feature>
        <feature version="${project.version}">grpc-feature</feature>
 
        <bundle>mvn:org.json/json/${json.version}</bundle>
        <bundle>mvn:com.google.code.gson/gson/${gson.version}</bundle>
        <bundle>mvn:com.google.protobuf/protobuf-java/${protobuf.version}</bundle>
 
        <bundle>wrap:mvn:com.influxdb/influxdb-client-core/${influxdb-client.version}</bundle>
        <bundle>wrap:mvn:com.influxdb/influxdb-client-java/${influxdb-client.version}</bundle>
        <bundle>wrap:mvn:com.squareup.okhttp3/okhttp/3.13.1</bundle>
        <bundle>wrap:mvn:com.squareup.okhttp3/logging-interceptor/3.13.1</bundle>
        <bundle>wrap:mvn:com.squareup.okio/okio/1.17.3</bundle>
        <bundle>wrap:mvn:com.squareup.retrofit2/retrofit/2.5.0</bundle>
        <bundle>wrap:mvn:com.squareup.retrofit2/converter-scalars/2.5.0</bundle>
        <bundle>wrap:mvn:com.squareup.retrofit2/converter-gson/2.5.0</bundle>
        <bundle>wrap:mvn:io.gsonfire/gson-fire/1.8.0</bundle>
        <bundle>wrap:mvn:io.reactivex.rxjava2/rxjava/2.2.6</bundle>
        <bundle>wrap:mvn:org.reactivestreams/reactive-streams/1.0.2</bundle>
    </feature>
</features>
```

<a id="ipfixpmdm" />

IPFIX PM Data Handler
===============================
The IPFIX PM data handler is part of the PM Collection Framework.
The data handler receives IPFIX messages in an internal format from
the IPFIX collector and processes them in such a way that they can be
efficiently stored into a time series database.

The database engine used is InfluxDB V2 in its current version
2.0.0-beta.2. InfluxDB provides a web interface at http://localhost:9999
with several APIs to query the stored data and to administer the database.

These APIs are documented at
<https://v2.docs.influxdata.com/v2.0/query-data/execute-queries/#influxdb-api>,
InfluxDB\'s full documentation can be found at
<https://v2.docs.influxdata.com/v2.0/> as well.

## DBInterface
To decouple the PM-Data-Handler from the DB used to persist the data,
the DBInterface is used. The DBInterface provides methods to intialize /
deinitialize the DB connection and to execute store and query commands.

DBInterface

```
/*
 * Copyright 2020 Broadband Forum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadband_forum.obbaa.pmcollection.pmdatahandler;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Database interface.
 */
public interface DBInterface {
    boolean openDB();

    void executeStore(TSData tsData);

    void executeQuery(String metric, Instant startTime, Instant stopTime,
        Map<String, List<String>> filter, List<TSData> results);

    void shutdownDB();
}

```

## InfluxDB Setup
As with any other database engine, InfluxDB needs an initial setup that
creates an administrative user, an access token used by client to access
a database and a first database.

The InfluxDB structure is organized in three layers: the top layer
consists of \"organizations\". Below an organization so called buckets
are created. Buckets are what databases are in relational database
world. The default setup creates the organization \"broadband\_forum\"
and the bucket \"pm-collection\".

To access a bucket a token must be created. During setup the token is
created and stored in /root/.influxdbv2/credentials directory located in
the influxdbv2 instances virtual environment. The admin user and its
password are both set to influxdb .

For use in a production environment you should create your own
organization, bucket and token. The password should also be changed to a
secure password. You could do this by using the web gui
(http://localhost:9999).

### Configuration

The PM-Data-Handler has the following configuration options:

| Option | Description | Default |
| :--- | :--- | :---: |
|INFLUXDB_ORGANISATION|InfluxDB organization|broadband_forum|
|INFLUXDB_BUCKETID|Bucket name|pm-collection|
|INFLUXDB_API_URL|URL used to communicate to InfluxDB|http://localhost:9999|
|INFLUXDB_TOKEN|The access token generated during setup|-|
|PMD_MAX_BUFFERED_POINTS|The maximum number of points to buffer|5000|
|PMD_MAX_BUFFERED_MEASUREMENTS|The maximum number of measurements to buffer|100|
|PMD_TIMEOUT_BUFFERED_POINTS|The timeout after points are written to the DB in seconds|60|

The configuration is done by setting the environment variables in the
./baa-dist/docker-compose.yml. Developers can change default values for
these environment variables in the two pom.xml
./pm-collector/pm-data-handler/pm-data-handler-impl/pom.xml and
pm-collector/pm-data-handler/influxdb-impl/pom.xml, if necessary.


### Retrieving Data

The simplest way to view the stored data is to use the InfluxDB Web GUI.
The Web GUIs Data Explorer allows an easy interactive selection of the
bucket and allows to define a filter. Data can be filtered by the
following parameters:

| Tag | Description |
| :--- | :--- |
|\_measurement|The \_measurement could be compared to a DB table in the relational DB world. The _measurement is created by the pm data handler when storing data. The value used to describe the measurement is the template ID from the IPFIX Message. The templateID defines the list of IPFIX Elements measured.|
|deviceAdapter|The device type|
|hostName|The hostname of the device from the YANG model|
|observationDomain|The IPFIX observation domain|
|sourceIP|The source IP address of the IPFIX message|
|templateID|The IPFIX template ID defining the measurement|


Data could also be stored and queried using influx the CLI interface to
an InfluxDB. Here is a simple example storing and retrieving the data from
\"now\" - 10 seconds to \"now\" for dpu0:

```
#!/bin/bash
INFLUX_ORG="broadband_forum"
INFLUX_BUCKET="pm-collection"
stopTime=`date +%s`
startTime=`expr ${stopTime} - 10`
influx write -o ${INFLUX_ORG} -b ${INFLUX_BUCKET} -p s 'measurement,hostName=dpu0,templateId=267 if:inerrors=7i,if:outerrors=564i '${startTime}
influx query -o ${INFLUX_ORG} 'from(bucket: "'${INFLUX_BUCKET}'") |> range(start: '${startTime}', stop: '${stopTime}') |> filter(fn: (r) => r._measurement == "measurement")|> filter(fn: (r) => r.hostName == "dpu0")'
```

[<--Using OB-BAA](../index.md#using)
