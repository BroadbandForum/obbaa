
<a id="polt_sim" />

# pOLT Simulator

Simulates the physical OLT for interactions between the OLT and the BAA layer.

The pOLT simulator is a framework comprises the pOLT simulator and an
ONU simulator which are two separate components at the time of writing.

The pOLT simulator framework only contains basic ONU simulation
capabilities that is only necessary to complement the pOLT interactions with the BAA layer (e.g., detect/undetect message handling). As such, the pOLT simulator framework doesn\'t contain a ONU simulator.

## Deployment

The pOLT simulator code can be found at the [OBBAA/obbaa-polt-simulator](https://github.com/BroadbandForum/obbaa-polt-simulator) repository in Github.
The code contains a Dockerfile to make sure the code is compiled correctly on any (Linux x64) platform.
It also features a docker-compose file to make the process much more convenient.
The build process requires these tools that can be installed as follows:

### Docker:

```
curl -fsSL https://get.docker.com/ -o get-docker.sh
chmod +x get-docker.sh && ./get-docker.sh
sudo usermod -aG docker `whoami`
```

**You might want to logout and login again to make sure your user is
part of the docker group:**
*groups*

**Docker version should be listed**:
*docker version*

### Docker compose:

```
wget https://github.com/docker/compose/releases/download/1.26.0/docker-compose-Linux-x86_64
-o /usr/local/bin/docker-compose && sudo chmod +x
/usr/local/bin/docker-compose
```

**Docker compose version should be listed:**

```
docker-compose -version
```

### Clone obbaa-polt-simulator repo in your local:

```
git clone https://github.com/BroadbandForum/obbaa-onu-simulator.git
```

### If not done already: generate vOMCI key and certificate:

```
openssl genrsa -out certificates/vomci_privatekey.pem
openssl req -new -x509 -sha256 -key certificates/vomci_privatekey.pem
-out certificates/vomci.cer -days 3650
```

You will then be asked to enter a few parameters, if you want to see the ones used previously, just have a look into \'certificates/request.conf\' and adapt \'polt-simulator\' to \'vomci1\'.

### Running pOLT simulator with ONU simulator (TR451 flow):

*  Update the tr451.cli file with below changes (Remove existing
    content of certificates/cli\_scripts/tr451.cli and paste below said contents into the file.

```
#/po/au priv_key=/certificates/vomci_privatekey.pem local_cert=/certificates/vomci.cer peer_cert=/certificates/polt.cer
/po/set client_server=client enable=yes
#/po/end client_server=client name=polt-simulator port=8433 host=polt-simulator
/log/n name=POLT log_level_print=DEBUG log_level_save=DEBUG
```
* Update line \#14 of docker-compose.yml to **command:
\[\"-dummy\_tr385\",\"-f\",\"/certificates/cli\_scripts/tr451.cli\"\]**

After the compile and run step) the pOLT simulator is started successfully, ONU simulator must be attached to pOLT simulator for running TR451 flow.

```
docker attach polt-simulator_compose

/po/rx_mode mode=onu_sim onu_sim_ip=<ip-of-onu-simulator-container> onu_sim_port=<port-used-in-onu-simulator>

example : /po/rx_mode mode=onu_sim onu_sim_ip=192.168.96.3 onu_sim_port=50000

```
Before attaching ONU simulator to pOLT simulator, ensure that ONU simulator is running on the same docker network on which polt simulor is running.


### Compile and run
----------------

Before the simulator is run, the vOMCI\'s certificate has to be copied to \'certificates/vomci.cer\'. If you are planning to use a TR451 flow instead, make sure you generated a private key and a certificate. Once this is done, the code can be compiled and run via this command from the repository\'s root:

```
Command to be used if you are on branch \<= R4.0.0:
  docker-compose up \--build
Otherwise:
  docker-compose build --build-arg OBBAA_OLT_ADAPTER_VERSION=2.0
```


**Note:** By default NETOPEER 2 version is set as 1.x, if you want to build polt-simulator with NETOPEER2 with version 2.x, we must update the file \~/obbaa-polt-simulator/third_party/libnetconf2/CMakeLists.txt (line #6 must be updated with the below line

```
bcm_make_normal_option(NETOPEER2_VERSION_2X BOOL "Use libyang,sysrepo,libnetconf2,netopeer2 v2.x packages" y)
```

This starts a docker compose container with a running netopeer2 server
and automatically starts an instance of the pOLT simulator.
If you don\'t want to build the code locally, **you can use the Docker hub image** like this:

```
docker-compose pull
docker-compose up
```

## Examples:
Examples of POLT simulator commands for standard-OLT-adapter 2.1 can be found [here](https://github.com/BroadbandForum/obbaa-polt-simulator/blob/master/RELEASE_NOTES).

### Run polt simulator using images from DockerHub:

***Pre-requisites:***

* polt-simulator code is checked out locally
* vOMCI key and certificates needs to be generated already.

**Steps to be followed:**

* Run command to start the polt simulator with the given image:
```
docker-compose up -d  \--\>
```

If you already built or pulled the image, \'docker-compose up\' is
enough to start the container. To enable the gRPC server function and
load some dummy interfaces, invoke these commands:

**Start netopeer2-cli:**

```
docker-compose exec polt-simulator netopeer2-cli
```

**Connect to the netconf server and start the gRPC (TR451) server:**

```
connect -p 10830*
yes
root
subscribe
edit-config --target running --defop merge --test test-then-set
--config=/certificates/1-set-tr451-server.yc
edit-config --target running --defop merge --test test-then-set
--config=/certificates/2-add-interfaces-olt.yc
```

The gRPC server is now started and ready for connections from vOMCI
function. The pOLT simulator\'s generated certificate can be found at
\'certificates/polt.cer\'.

The simulator\'s NETCONF port is now exposed at port 10830 at the host
machine. To create a new ONU, issue these commands:

**Attach to the running container\'s stdin and stdout (keep open, CTRL+C
stops the container):

```
docker attach polt-simulator_compose
```

You can now use the NETCONF server\'s command line interface.

**Create a new ONU device (not needed if provided via NETCONF):**\

```
/polt/onu_add channel_term=channeltermination.1 onu_id=1
serial_vendor_id=BRCM serial_vendor_specific=00000001
```

All subscribed NETCONF clients now receive an ONU detect notification.
Give this address and port to your OBBAA instance if you want to check
notification handling. More detailed description of all the commands can be found inside the project\'s
[README](https://github.com/BroadbandForum/obbaa-polt-simulator/blob/master/README.md)
file.
To trigger an ONU undetect notification, simply remove the ONU via:

```
/po/onu_del channel_term=channeltermination.1 onu_id=1
serial_vendor_id=BRCM serial_vendor_specific=00000001
```

Both /polt/onu\_add and /polt/onu\_del CLI commands support an optional \"flags\" parameter which can be a combination of \"expected\", \"present\", \"in\_o5\" and \"activation\_failed\". 

-   if \"flags\" parameter is NOT specified

    -   /polt/onu\_add command will trigger generation of
        \'onu-present-and-on-expected-channel-termination\'

    -   /polt/onu\_del command will trigger
        \'onu-not-present-with-v-ani\'

```
/polt/onu_add channel_term=channeltermination.1 onu_id=1
    serial_vendor_id=BRCM serial_vendor_specific=00000001
    flags=present+in_o5-  'onu-present-and-unexpected'
```

### Simulate vOMCI instance

If you want to simulate a vOMCI instance connected via gRPC as well, you can do this from a second terminal. Make sure there is a private key and a matching certificate available at
\'certificates/vomci\_privatekey.pem\' and \'certificates/vomci.cer\'.

**Run TR-451 simulator:**

```
docker-compose exec polt-simulator /usr/local/start_tr451_polt.sh -d
-f /certificates/cli_scripts/tr451.cli
```

**Inject vOMCI packets:**

```
/po/inject channel_term=channeltermination.1
onu=1
data=ffffffff44556677889900112233445566778899001122334455667788990011223344556677889900112233
```

The injected packet should now trigger a reaction in the simulator
terminal.

### Use the simulator with BAA

The easiest way to do that is starting the whole setup i.e. vomci, baa, kafka, vproxy, polt-simulator via one merged compose file so all the containers are in the same network. In the example below, the simulator code was placed in the subfolder polt-sim. You might want to adapt the line *- \"./polt-sim/certificates:/certificates\"* to your folder name. Creating the certificates is not needed.

docker-compose.yml file:

```
version: '3.5'
networks:
    baadist_default:
        driver: bridge
        name: baadist_default
services:
    baa:
        image: broadbandforum/baa
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
        depends_on:
          - kafka
          - zookeeper

    ipfix-collector:
        image: broadbandforum/ipfix-collector
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

    zookeeper:
      image: confluentinc/cp-zookeeper:5.5.0
      hostname: zookeeper
      container_name: zookeeper
      environment:
        ZOOKEEPER_CLIENT_PORT: 2181
        ZOOKEEPER_TICK_TIME: 2000
      networks:
        - baadist_default

    kafka:
      image: confluentinc/cp-kafka:5.5.0
      hostname: kafka
      container_name: kafka
      depends_on:
        - zookeeper
      environment:
        KAFKA_BROKER_ID: 1
        KAFKA_ZOOKEEPER_CONNECT: 'zookeeper:2181'
        KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
        KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092,PLAINTEXT_HOST://localhost:29092
        KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
        KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0
      networks:
        - baadist_default

    vomci:
      image: broadbandforum/obbaa-vomci
      hostname: obbaa-vomci
      container_name: obbaa-vomci
      ports:
        - 8801:8801
      environment:
        GRPC_SERVER_NAME: vOMCIProxy
        LOCAL_GRPC_SERVER_PORT: 58433
        KAFKA_BOOTSTRAP_SERVER: "kafka:9092"
        KAFKA_CONSUMER_TOPICS: "OBBAA_ONU_REQUEST OBBAA_ONU_NOTIFICATION"
        KAFKA_RESPONSE_TOPIC: 'OBBAA_ONU_RESPONSE'
      networks:
        - baadist_default
      depends_on:
        - zookeeper
        - kafka

    polt-simulator:
        image: broadbandforum/obbaa-polt-simulator:latest
        stdin_open: true
        tty: true
        container_name: polt-simulator_compose
        command: -dummy_tr385 -f /certificates/cli_scripts/read_certs_start_server.cli
        environment:
          - PASSWD=root
        ports:
          - "10830:10830"
        volumes:
          - "./polt-sim/certificates:/certificates"
        networks:
          - baadist_default

    vproxy:
      image: broadbandforum/obbaa-vproxy
      hostname: obbaa-vproxy
      container_name: obbaa-vproxy
      ports:
        - 8433:8433
      environment:
        LOCAL_GRPC_SERVER_PORT: 8433
        REMOTE_GRPC_SERVER_PORT: 58433
        REMOTE_GRPC_SERVER_ADDR: obbaa-vomci
      networks:
        - baadist_default
      depends_on:
        - vomci

    control_relay:
      image: broadbandforum/obbaa-control-relay
      container_name: obbaa-control-relay
      restart: always
      ports:
        - "50052:50052"
        - "50055:50055"
      environment:
        - CONTROL_RELAY_HELLO_NAME=control_relay_service
        - PLUGIN_PORT=50052
        - SDN_MC_SERVER_PORT=
        - SDN_MC_SERVER_LIST=
        - CONTROL_RELAY_PORT=50055
        - OBBAA_ADDRESS=baa
        - OBBAA_PORT=9292
        - SSH_USER=admin
        - SSH_PASSWORD=password
        - SHARED_FOLDER=./plugin-repo
        - PRIVATE_FOLDER=./plugin-enabled
      volumes:
        - /baa/control-relay/plugins:/control_relay/plugin-repo
      depends_on:
        - baa
```

The vProxy provides the gRPC server in this constellation, so the
default commands for the simulator have to be changed. To do this, edit the file: certificates/cli\_scripts/read\_certs\_start\_server.cli and
change the content to:\
*/po/set client\_server=client enable=yes*\
*/po/end client\_server=client name=vomci1 port=8433 host=vproxy*

Now start the ensemble and tail the vomci logs via:

```
docker-compose up -d polt-simulator baa kafka vomci vproxy &&
docker-compose logs -f vomci
```

Login to the simulator via:

```
docker-compose exec polt-simulator netopeer2-cli
connect -p 10830
yes
root
subscribe
```

And configure the interfaces:

```
edit-config --target running --defop merge --test test-then-set
--config=/certificates/2-add-interfaces-olt.yc
```

Now create the OLT in the BAA, enter the IP address of your **host
machine** (do NOT use the loopback address as this will point to BAA\'s internal address, not the host). To do so, you need to connect to BAA with a NETCONF browser like MGSOFT or Atom.

To create the OLT use the [1-create-olt.xml](https://github.com/BroadbandForum/obbaa/tree/master/resources/examples/vomci-end-to-end-config/1-create-olt.xml) example command.
Check for successful creation of the device connection via:

```
docker-compose exec baa tail -f /baa/baa-dist/data/log/karaf.log
```

Once this device is created, send the infra config command to BAA to create the connection through the OLT via the [2-create-infra-2.1.xml](https://github.com/BroadbandForum/obbaa/tree/master/resources/examples/vomci-end-to-end-config/2-create-infra-std-2.1.xml) example command.

After a few seconds, this config should be applied. The ONU can now be added to BAA via the [1-create-onu.xml](https://github.com/BroadbandForum/obbaa/tree/master/resources/examples/onu-config/1-create-onu.xml) example command.


At last, subscribe to the kafka notification topic that belongs to the
vOMCI instance:

```
docker exec -ti kafka kafka-console-consumer --bootstrap-server
kafka:9092 --topic OBBAA_ONU_NOTIFICATION

and watch for the notification when you \"connect\" the virtual ONU.
This is done by attaching to the simulator:

```
docker attach polt-simulator_compose
```

and then issue the ONU creation command from there:

```
po/onu channel_term=channeltermination.1 onu_id=1
serial_vendor_id=BRCM serial_vendor_specific=00000001
```

The notification should now be sent through the Kafka topic.

[<--Simulators](../index.md#sim)
