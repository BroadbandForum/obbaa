
<a id="env" />

# Setting up an OB-BAA Environment

Depending on what the user will want to do with the OB-BAA project this section provides the appropriate information for developers and users of the OB-BAA project.

Developers should be able to understand:

-   The pre-requisites needed for a deployment environment

-   How to obtain and synchronize the code base

-   How to build the code base in order to generate a deployable image

Users should be able to understand:

-   The pre-requisites needed for a development environment

-   How to deploy and bring-up the OB-BAA executables

Before deploying OB-BAA the environment has to be prepared, you can find out how to setup an environment on a bare metal environment or a virtualized machine (VM) [here](./#platform).

For developers and users the easiest way to get started with OB-BAA is to download the OB-BAA micro-services from the [Broadband Forum's baa docker hub artifactory](https://hub.docker.com/r/broadbandforum/baa). Instructions for using OB-BAA micro-services can be found [here](./#artifactory).

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
For the Guest OS in a VM: 64bit Ubuntu 18.04 is the recommended
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

OB-BAA is realized as micro-services under the docker engine and requires docker to be installed.

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

### Kubernetes

Kubernetes is a portable, extensible, open-source platform for managing containerized workloads and services that facilitates both declarative configuration and automation.
Kubernetes services, support, and tools are widely available and more details about K8s can be found [here](https://kubernetes.io/).

Install kubernetes:
<https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/install-kubeadm/>

```
kubectl version

Client Version: version.Info{Major:"1", Minor:"18", GitVersion:"v1.18.13", GitCommit:"4c00c3c459261e8ff3381c1070ddf798f0131956", GitTreeState:"clean", BuildDate:"2020-12-09T11:18:24Z", GoVersion:"go1.13.15", Compiler:"gc", Platform:"linux/amd64"}
Server Version: version.Info{Major:"1", Minor:"18", GitVersion:"v1.18.13", GitCommit:"4c00c3c459261e8ff3381c1070ddf798f0131956", GitTreeState:"clean", BuildDate:"2020-12-09T11:09:27Z", GoVersion:"go1.13.15", Compiler:"gc", Platform:"linux/amd64"}
```

#### Minikube

**Minikube** is a tool that lets you run Kubernetes locally.
**Minikube** runs a single-node Kubernetes cluster on your personal
computer (including Windows, macOS and Linux PCs) so that you can try
out Kubernetes, or use for daily development work. More details about
minikube can be found [here](https://kubernetes.io/docs/tutorials/hello-minikube/)

Install minikube: <https://minikube.sigs.k8s.io/docs/start/>

```
minikube version
minikube version: v1.14.2
commit: 2c82918e2347188e21c4e44c8056fc80408bce10
```

Start Minikube:

```
sudo minikube start --vm-driver=none
sudo chown -R $USER:$USER ~/.kube ~/.minikube
helm init
```

#### Helm

Helm helps you manage Kubernetes applications and help define, install, and upgrade even the most complex Kubernetes application. Charts are easy to create, version, share, and publish.

Install helm: <https://helm.sh/docs/intro/install/>

```
helm version
version.BuildInfo{Version:"v3.2.4", GitCommit:"0ad800ef43d3b826f31a5ad8dfbb4fe05d143688", GitTreeState:"clean", GoVersion:"go1.13.12"}

```

Note: The OB-BAA installation is verified with above said K8s, Minikube and Helm versions.

<a id="artifactory" />
## Using OB-BAA micro-services from the Broadband Forum\'s public docker artifactory
This section of the document provides information of how to deploy (pull and run) an OB-BAA distribution from the Broadband Forum\'s public docker registry.

Obtain the OB-BAA source code:
Instructions for obtaining the OB-BAA source code can be found [here](./#source).

Running OB-BAA micro-services from the public docker registry:
In this release, OB-BAA can be deployed in two ways:
  - Using a Docker Compose file
  - Using Helm Charts (K8s installation)

### Using Docker Compose

```
cd obbaa/resources
Pull images from public docker registry using command "docker-compose -f ob-baa_setup.yml pull"
Start the docker containers using command "docker-compose -f ob-baa_setup.yml up -d"

Note: As the ob-baa_setup.yml file now have ipfix-collector, influxDB, zookeeper,
      kafka, and vomci and vproxy containers added, the ob-baa_setup.yml file will
      also bring up the mentioned ipfix collector, influxDB, zookeeper, kafka,
      vomci and vproxy microservices along with baa.

```

### Using Helm Charts
OB-BAA Helm Chart Hierarchy
<p align="left">
 <img width="400px" height="400px" src="{{site.url}}/installing/env/helm_hierarchy.png">
</p>

```
cd obbaa/resources/helm-charts/obbaa-helm-charts
create persistent-volume & volume claims using command : "kubectl create -f obbaa-pv-pvc.yaml"
Start the helm charts using command: "helm install obbaa ./obbaa" (helm install <name-of-the-installation> ./parentChartDirectory)

Note: As the we have run the helm install command from the parent chart, it will also bring up the mentioned control-relay, ipfix-collector, influxDB, zookeeper, kafka, vomci and vproxy microservices along with baa
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

##### Clone OB-BAA Control Relay service repository

```
  git clone https://github.com/BroadbandForum/obbaa-fc-relay.git
```

##### Clone OB-BAA YANG Modules repository

```
  git clone https://github.com/BroadbandForum/obbaa-yang-modules.git
```
**Note: ** The obbaa-yang-modules repository must be checked out to the same directory where obbaa repository is checked out. This step is applicable from R5.0.0 onwards.


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

Build Yang Repo (This step is applicable from R5.0.0 onwards)
   Change directory to obbaa-yang-modules
   make

Build OBBAA
	Change directory to obbaa
  mvn clean -P copy-standard-adapters,copy-core-yang-modules,copy-aggregator-yang-modules(to clean up the already loaded yang modules and std adapters. This step is applicable from R5.0.0)

	mvn clean install -DskipTests -P copy-standard-adapters,copy-core-yang-modules,copy-aggregator-yang-modules,copy-nf-standard-adapters (Run this command to copy the standard-adapters zip files and aggregator yang modules into obbaa repo, and compile obbaa repo this step is applicable from R5.0.0)

	mvn clean install (Use this command to build obbaa release < R5.0.0)

Build OBBAA with Unit Test (UT)
  Pre-requisite: Start InfluxDB docker container using the command:
     docker-compose -f ~obbaa/pm-collector/pm-data-handler/persistent-data-handler/influxdb-impl/bamboo-docker/obbaa-influxdb.yml up -d

  If InfluxDB is not running there will be UT failures in pm-collector modules.

  This step is required only if you are going to run UT in your local environment.
    Change directory obbaa
    mvn clean install -DskipTests -P copy-standard-adapters,copy-core-yang-modules,copy-aggregator-yang-modules,copy-nf-standard-adapters (This step is applicable from R5.0.0)

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
