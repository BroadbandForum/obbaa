
<a id="env" />

# Setting up an OB-BAA Environment

Depending on what the user will want to do with the OB-BAA project this section provides the appropriate information for developers and users of the OB-BAA project.

Developers should be able to understand:

-   The pre-requisites needed for a development environment

-   How to obtain and synchronize the code base

-   How to build the code base in order to generate a deployable image

Users should be able to understand:

-   The pre-requisites needed for a development environment

-   How to deploy and bring-up the OB-BAA executables

Before deploying OB-BAA the environment has to be prepared, you can find out how to install and run OB-BAA in a bare metal environment or a virtualized machine (VM) [here](./#platform).

For developers and users the easiest way to get started with OB-BAA is to download the OB-BAA micro-services from the [Broadband Forum's baa docker hub artifactory](https://hub.docker.com/r/broadbandforum/baa). Instructions for using OB-BAA micro-services can be found [here](./#artifactory)

If developers require additional access to the source code, the source code can be downloaded and OB-BAA can be built using the instructions found [here](./#source).

<a id="platform" />
## Platform Requirements and Running OB-BAA

This section of the document provides the platform requirements needed to run OB-BAA regardless if the developer requires source code access or simply downloads the BAA image from the docker artifactory.

### Platform Requirements
#### Server Requirements

OB-BAA can be deployed in either a Bare Metal or a VM.

##### Bare Metal based

No specific requirements on Bare Metal Server type.

Minimum resource requirement of server would be: 4 cores, 16 GB RAM and
80 GB HDD

##### VM based

No specific requirement on Hyper-visor. It could be a Virtual Box, VMWare or any others.

###### Production

Minimum resource requirement of VM would be: 4 vCPUs, 16 GB RAM and 80
GB HDD

###### Lab Trail

Minimum resource requirement of VM would be: 1 vCPUs, 4 GB RAM and 40 GB
HDD

#### Operating System (OS) Requirements

While there isn\'t a strict requirement on the OS needed for the
project, Ubuntu is the OS in which the core development team uses.
For the Guest OS in a VM: 64bit Ubuntu 16.04.04 is the recommended
and either server or desktop versions are supported.

#### Network Requirements

Build and deployment of OB-BAA pulls several artifacts (including source
code) from public network. So access to Internet is mandatory.

**Info:** This document assumes that the server is directly connected to Internet with no network proxy. If proxies are part of your network then appropriate proxy settings need to be made for artifact access, docker & maven (instructions available in Internet).

**Warning:** Shell commands used in this page are in bash syntax. If you are using other shells, specific adaption in command would be required.

For run-time of the BAA layer, the only connectivity requirements
between OB-BAA components is between the BAA layer and the simulator
which is covered between in the Simulator setup section.

#### Docker

OB-BAA services are realized as a micro-services under a docker engine and requires docker and docker-compose applications to be installed.

```
Install docker:
  apt-get install -y curl
  curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
  sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
  sudo apt-get update
  apt-cache policy docker-ce
  sudo apt-get install -y docker-ce
  sudo usermod -aG docker ${USER}
Install Docker Compose:
  sudo apt-get install docker-compose
```

### Running OB-BAA

Once the baa image has been downloaded or built using the source code and the docker daemon is running
(ps -ef|grep dockerd should return a valid process), you can run the baa image using the following:

```
  cd obbaa/baa-dist
  docker-compose -f docker-compose.yml up -d

Note: As the docker-compose.yml file now have ipfix-collector, influxDB, zookeeper and kafka containers added, it will also bring up the mentioned ipfix-collector, influxDB, zookeeper and kafka microservices along with BAA.

The following commands displays the BAA application logs:
   docker exec -it baa bash
   cd /baa/baa-dist/data/log
   tail -f karaf.log (the file is moved to ".1" extension after reaching certain size and a new file is created)
```


<a id="artifactory" />
## Deploying OB-BAA micro-services from the Broadband Forum\'s public docker artifactory
This section of the document provides information of how to deploy (pull and run) an OB-BAA distribution from the Broadband Forum\'s public docker registry.

Obtain the OB-BAA source code:
Instructions for obtaining the OB-BAA source code can be found [here](./#source).

Running OB-BAA micro-services from the public docker registry:

```
cd obbaa/resources
Pull images from public docker registry using command "docker-compose -f ob-baa_setup.yml pull"
Start the docker containers using command "docker-compose -f ob-baa_setup.yml up -d"

Note: As the ob-baa_setup.yml file now have ipfix-collector, influxDB, zookeeper,
      kafka, and vomci and vproxy containers added, the ob-baa_setup.yml file will 
      also bring up the mentioned ipfix collector, influxDB, zookeeper, kafka, 
      vomci and vproxy microservices along with baa.

```

<a id="source" />
## Building OB-BAA Using the Source Code
This section of the document describes how to obtain access to the source code repository for OB-BAA and then use the source code to build the baa image.

**Warning:** When building OB-BAA from the Source Code, the build of the baa image is required prior to building any submodules (e.g., Vendor Device Adapters).

### Platform Requirements
#### Git

Git is the versioning and source code control used for OB-BAA. Git
version 2.7.4 or above should be installed in the system.

```
  sudo apt-get install git
```

#### Open JDK

Open JDK is used by the OB-BAA build process. Install OpenJDK 1.8 or
above

```
  sudo add-apt-repository ppa:openjdk-r/ppa
  sudo apt-get update
  sudo apt-get install openjdk-8-jdk

```

#### Maven

Maven is the build tool used for OB-BAA build

To install maven use the following command in a apt based system

```
  sudo apt-get install maven
```

#### Repositories

The OB-BAA code is maintained in Github within the [Broadband Forum\'s
repositories](http://www.github.com/BroadbandForum).

The code base is split across two repositories:

-   obbaa-netconf-stack - NETCONF specific client and server
    implementations

-   obbaa - OB-BAA main repository

##### Clone OB-BAA NETCONF stack repository

```
  git clone https://github.com/BroadbandForum/obbaa-netconf-stack.git
```

##### Clone OB-BAA obbaa repository

```
  git clone https://github.com/BroadbandForum/obbaa.git
```

##### Clone OB-BAA obbaa-vomci repository

```
  git clone https://github.com/BroadbandForum/obbaa-vomci.git
```

##### Clone OB0BAA Control Relay service repository

```
  git clone https://github.com/BroadbandForum/obbaa-fc-relay.git
```

### Build OB-BAA

Building OB-BAA to generate the docker image along with its compose file is
done in two steps:

-   Compilation of Code to generate necessary jars

-   Build a docker image out of the jars

#### Compilation:

For compilation the sequence is to first compile the NETCONF stack and
then obbaa.

```
Build NETCONF stack
  Change directory to obbaa-netconf-stack
  mvn clean install -DskipTests

Build OBBAA
  Change directory to obbaa
  mvn clean install -DskipTests
  
Build OBBAA with Unit Test (UT)
  Pre-requisite: Start InfluxDB docker container using the command:
     docker-compose -f ~obbaa/pm-collector/pm-data-handler/persistent-data-handler/influxdb-impl/bamboo-docker/obbaa-influxdb.yml up -d
  If InfluxDB is not running there will be UT failures in pm-collector modules. 
  This step is required only if you are going to run UT in your local environment.
    Change directory obbaa
    mvn clean install

```

#### Build docker image:

The next step is to build docker images from the generated jars.

```
Build OBBAA Docker Image
	cd <obbaa>/baa-dist
	docker build -t baa .

Build IPFIX-Collector Docker Image
   cd <obbaa>/pm-collector/ipfix-collector/ipfix-collector-dist
	docker build -t ipfix-collector .

Build vOMCI and vProxy Docker Images
   cd <obbaa-vomci>
   make docker-build

Note: The docker-build make command builds the docker images for
      obbaa-vomci and obbaa-vproxy.

Build Control Relay Service Docker Image
   cd <obbaa-fc-relay>/control-relay
   Follow the instructions in the [BBF public github obbaa-fc-relay repository](https://github.com/BroadbandForum/obbaa-fc-relay) readme
   or use the following command:
     docker build . -f single-command-build.dockerfile -t obbaa-control-relay

   Contents of the file single-command-build.dockerfile:
		FROM golang:1.14.4 AS builder
		
		RUN apt update && apt-get install protobuf-compiler unzip -y
		RUN go get -u github.com/golang/protobuf/protoc-gen-go
		WORKDIR /opt/control-relay
		COPY . .
		
		RUN protoc --proto_path=proto --go_out=plugins=grpc:pb --go_opt=paths=source_relative ./proto/control_relay_packet_filter_service.v1.proto ./proto/control_relay_service.proto
		RUN go build -buildmode=plugin -o bin/plugin-standard/BBF-OLT-standard-1.0.so plugins/standard/BBF-OLT-standard-1.0.go
		RUN go build -o bin/control-relay
		RUN chmod +x create-bundle.sh && ./create-bundle.sh
		
		FROM ubuntu:18.04
		
		RUN apt-get update && \
		    apt-get upgrade -y
		
		WORKDIR /control_relay
		
		COPY --from=builder /opt/control-relay/dist/control-relay.tgz /control_relay
		RUN tar xvfz control-relay.tgz
		RUN rm control-relay.tgz
		
		CMD ["./control-relay"]    

```

[<--Installing](../index.md#installing)
