
<a id="device_adapter" />

# Device Adapter Framework

The BAA layer provides the capability to manage access nodes of the same
type from different vendor implementations across the BAA layer\'s
Southbound Interface (SBI) where each vendor implementation can provide
an adaptation to the abstracted standard data model for that type of
device (e.g., OLT, DPU) across the BAA layer\'s Northbound Interface
(NBI). To realize this capability, the OB-BAA software architecture\'s
PMA Registry component includes an Adapter Framework where each vendor
implementation is defined as an instance of a Vendor Device Adapter
(e.g., Device Adapter A, Device Adapter B) that is adapted to the
Standard Device Adapter (SDA) for that type of device.

## Adapter Framework

The OB-BAA software architecture\'s Adapter Framework provides the
following capabilities:

-   An interface to hot plug Device Adapters

-   Life cycle management of Device Adapters

-   The ability to forward instructions coming from management and
    control systems through the BAA layer\'s NBI to the Device Adapter
    that corresponds to the targeted device instance

-   The ability to receive notifications from Device Adapters and
    forward to components within the BAA layer and/or management and
    control systems through the BAA layer\'s NBI

-   Facilitate adaptation by providing relevant context (e.g. AN Config
    Store) to Device Adapters

## Device Adapters

The Architecture section\'s overview discussed that the representation
of an AN is exposed through interfaces in the BAA layer. These views of
the AN are defined as:

-   Physical Access Node (pAN):

    A pAN is a technology-specific representation of a given AN type (e.g
    OLT, DPU), based on a standard AN model and is based on a standard
    data model that is a common, vendor-agnostic representation of the
    type of AN. In the BAA layer pAN\'s are instantiated with the AN is
    created.

-   Virtual Access Node (vAN):

    The vAN abstraction of the pAN that can providing a L2-3
    representation that is common across device types (generic AN).  When
    deployed as an Actuator, the L2-3 representation is provided using the
    BAA layer\'s data model access control mechanism (e.g., NACM) that
    limits what parts of the pAN a client can view and/or manipulate.
    **The access control mechanism is not supported in this release of
    OB-BAA.**

-   Vendor Access Node (vendorAN):

    The vendorAN represents the vendor\'s implementation of the AN
    instance (e.g Nokia DPU v1.0, Huawei OLT v3.0). This representation
    exposes the vendor\'s implementation of the access node\'s device
    model to the SB layer. The vendorAN provides variations

The vendorAN and pAN representations are realized in OB-BAA using two
different types of Device Adapters - Standard Device Adapters and Vendor
Device Adapters.

### Standard Device Adapter (SDA)

The Standard Device Adapter provides a standard compliant abstract data
model per device type (e.g., DPU, OLT) that represents the standard pAN
instances to management and control systems. The BAA layer uses the SDAs
to validate the compliance of Vendor Device Adapters.

**Warning:** In this release of OB-BAA, the OLT and DPU SDAs defined in [TR-413](http://www.broadband-forum.org/technical/download/TR-413.pdf) are provided by the OB-BAA distribution. However, SDA\'s are not pluggable modules and only the SDA\'s provided by the OB-BAA distribution are supported. In a future release new SDA\'s can be defined and deployed by the BAA layer administrator.

### Vendor Device Adapter (VDA) or Coded Adapter

New instances of a vendorAN requires a corresponding VDA that supports
instances of the vendorAN based on the unique combination of Vendor +
Hardware Variant + Model version (E.g. Nokia DPU 1.0, Huawei OLT 2.0).

The purpose of the VDA is to:

-   Translate commands based on the pAN instance\'s standard data model
    to the vendorAN instance\'s device specific representation

-   Translate notifications based on the vendorAN instance\'s device
    specific model to pAN instance\'s standard representation

-   Select the supported communication protocol adapter for interaction
    with the device

As a VDA is specific to a vendor, the VDAs are developed by Vendors and
are pluggable into OB-BAA using the VDA\'s interface.

The VDA that is plugged into OB-BAA has certain requirements. These are:

-   The VDA implements the VDA interface that is defined [here](#VDAInterface).  
	VDA's that are not based on the NETCONF protocol need to include the following interface so that notifications received and forwarded for a device.
	```
	public interface NonNCNotificationHandler {
	
	    /**
	     * This api must be called by VDA when a notification is received.
	     * @param ip : ip of the device from which notification is received
	     * @param port : port of the device from which notification is received
	     * @param notification : Notification received from the device
	     */
	    void handleNotification(String ip, String port, Notification notification);
	
	}
	```

-   The VDA provides the complete set of YANG modules that comprise its
    YANG module library

-   A normalization library that provides the capability to translate
    between the vendorAN and pAN representations

-   A mediation library that provides the capability required for SBI
    protocol adapter selection and any device instance specific
    communication details

-   A metadata artifact that contains static information used by the
    Adapter Framework to relate the vendorAN instance with a VDA

## The Adapter Framework At Work

The Adapter Framework translates the commands received by the BAA layer
in the standard data model for that type of pAN into the data model
supported by the vendorAN instance. Conversely the Adapter Framework
takes events that come from the vendorAN instance and translates the
event into a representation supported by the pAN device type. The flow
is depicted in the diagram below:

<p align="center">
 <img width="1000px" height="700px" src="{{site.url}}/architecture/device_adapter/adapter_frmk_at_work.png">
</p>

The diagram below provides an example of an \"Edit Configuration\"
command as it processes through the Adapter Framework:

<p align="center">
 <img width="1000px" height="700px" src="{{site.url}}/architecture/device_adapter/adapter_frmk_command.png">
</p>

## Vendor Device Adapter Interface<a name="VDAInterface"></a>

The following interface is implemented by instances of a VDA in order to work within the Adapter Framework.

```
public interface DeviceInterface {
   /**
     * Veto changes if needed based on adapter specific rules.
     * @param device    Device for which the request is dedicated
     * @param request   request which needs to validated based on rules
     * @param oldDataStore the PMA data-store for the device as before the edit-config
     * @param updatedDataStore the current PMA data-store for the device
     */
    void veto(Device device, EditConfigRequest request, Document oldDataStore, Document updatedDataStore)
            throws SubSystemValidationException;
 
   /**
     * Send an edit-config request for a device at the SBI side.
     *
     * @param device  Device for which the request is dedicated
     * @param request the edit-config request
     * @param getConfigResponse the get-config response from PMA data-store for device
     * @return future response
     * @throws ExecutionException throws Execution Exception
     */
    Future<NetConfResponse> align(Device device, EditConfigRequest request, NetConfResponse getConfigResponse)
            throws ExecutionException;
 
    /**
     * Send request to device on first contact scenario at SBI side.
     *
     * @param device Device for which the request is dedicated
     * @param getConfigResponse get-config response from PMA data-store for device
     * @return pair of request and response
     * @throws NetconfMessageBuilderException throws Netconf Message builder exception
     * @throws ExecutionException             throws Execution Exception
     */
    Pair<AbstractNetconfRequest, Future<NetConfResponse>> forceAlign(Device device, NetConfResponse getConfigResponse)
            throws NetconfMessageBuilderException, ExecutionException;
 
    /**
     * Send a get request to device at SBI Side.
     *
     * @param device     Device for which the request is dedicated
     * @param getRequest The filtered get request for device
     * @return future response
     * @throws ExecutionException throws Execution Exception
     */
    Future<NetConfResponse> get(Device device, GetRequest getRequest) throws ExecutionException;
 
    /**
     * Send a get-config request to device at SBI Side.
     *
     * @param device           Device for which the request is dedicated
     * @param getConfigRequest The filtered get-config request for device
     * @return future response
     * @throws ExecutionException throws Execution Exception
     */
    Future<NetConfResponse> getConfig(Device device, GetConfigRequest getConfigRequest) throws ExecutionException;
 
    /**
     * Get the connection state for the device based on the protocol.
     *
     * @param device Device for which the request is dedicated
     * @return Connection State of the device
     */
    ConnectionState getConnectionState(Device device);
 
    /**
     * Normalize the vendor specific notification coming from the device to the supported notification format.
     *
     * @param notification the notification from the device to be normalized
     * @return the normalized notification
     */
    Notification normalizeNotification(Notification notification);
}
```

[<--Architecture](../index.md#architecture)
