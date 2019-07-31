
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

For developers and users the easiest way to get started with OB-BAA is to download OB-BAA from the [Broadband Forum's baa docker hub artifactory](https://hub.docker.com/r/broadbandforum/baa).

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

OB-BAA is realized as a micro service under a docker engine.

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

### Running BAA

Once the baa image has been downloaded or built using the source code and the docker daemon is running
(ps -ef|grep dockerd should return a valid process), you can run the baa image using the following:

```
  cd obbaa/baa-dist
  docker-compose -f docker-compose.yml up -d

The following commands displays the BAA application logs:
   docker exec -it baa bash
   cd /baa/baa-dist/data/log
   tail -f karaf.log (the file is moved to ".1" extension after reaching certain size and a new file is created)
```

<a id="source" />
## Building OB-BAA Using the Source Code
This section of the document describes how to obtain access to the source code repository for OB-BAA and then use the source code to build the baa image.

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

### Build OB-BAA

Building OB-BAA to generate the docker image along with its compose file is
done in two steps:

-   Compilation of Code to generate necessary jars

-   Build a docker image out of the jars

#### Compilation:

For compilation the sequence is to first compile the NETCONF stack and
then obbaa.

```
Build NetConf stack
  Change directory to obbaa-netconf-stack
  mvn clean install -DskipTests

Build OBBAA
  Change directory obbaa
  mvn clean install -DskipTests
```

#### Build docker image:

The next step is to build docker image from the generated jars.

```
  cd obbaa/baa-dist
  docker build -t baa .
```

[<--Installing](../index.md#installing)
