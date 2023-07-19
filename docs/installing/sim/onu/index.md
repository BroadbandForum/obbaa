
<a id="onu_sim" />

# ONU Simulator

The ONU simulator can be  used for simulating ONU functionality related to processing and responding to OMCI messages, as well as to test the scale of the BAA layer.

The ONU simulator, at the time of writing, is designed to interact with the pOLT simulator through using UDP.
Each instance runs in it\'s
container being able to simulate one or more ONU in a single channel
termination.

## Deployment

The ONU simulator code can be found at the [OBBAA/obbaa-onu-simulator](https://github.com/BroadbandForum/obbaa-onu-simulator) repository in Github.

**Clone obba-polt-simulator repo in your local:**

```
git clone https://github.com/BroadbandForum/obbaa-onu-simulator.git
```

### Compile and run

To generate the image from the code


*make docker-build*

Start the container exposing, for example, the port 50000:

```
docker container run -it \--name obbaa-onu-simulator \--rm -p
50000:50000/udp broadbandforum/obbaa-onu-simulator:latest bash
```

At the onu simulator container, launch the simulation application.\
Example for simulating one ONU with ID 1, in a channel termination with name \"CT\_1\": *onusim.py -p 50000 -n CT\_1 -i 1*

There are 3 log levels (0=errors+warnings, 1=info, 2=debug); default:
0). To set the log level use the option -l.\
Examples:

```
onusym.py -l 1

onusim.py -p 50000 -n CT\_1 -i 1 -l 2
```

To simulate several ONUs in the same channel termination it\'s possible to indicate a range of ONUs using option -i to the initial value and -I
to the final value.\
Example:

```
onusim.py -p 50000 -n CT\_1 -i 1 -I 50
```

**Connect with pOLT simulator**

After the ONU simulator is in place, set the connection in the pOLT
simulator.\
If the pOLT simulator is not yet running, launch it.

In the pOLT container, set the rx\_mode to use the ONU simulator:

```
/po/rx\_mode mode=onu\_sim onu\_sim\_ip=\<onu simulator container IP\>
onu\_sim\_port=\<onusym listening port\>
```

From this point on, the ONU simulator shall be able to receive, process and reply to all the OMCI packets sent.

[<--Simulators](../index.md#sim)
