
<a id="micro-discovery" />

# Examples for discovering micro-services

This section describes how to obtain the micro-service list discovered
by OB-BAA in a Kubernetes environment. In this release, only
micro-services running in the same Kubernetes cluster of OB-BAA will be discoverable.

**Pre-requisites:**

-   Running instances of BAA in a kubernetes environment (the Helm
    Charts in *resources/helm-charts* can be used)

**Steps**

In this release, micro-services discoverable by OB-BAA must be deployed with the following environment variables:

|Variable Name Description|Type|Description|
|BBF_OBBAA_VNF_NAME|String up to 64 ASCII printable characters|Name of the VNF/micro-service|
|BBF_OBBAA_VNF_TYPE|Enumeration(VOMCI, VPROXY, ACCESS-VNF)|VNF/micro-service Type. The values of the enumeration correspond to the identities currently available in bbf-network-function-types.yang. The available types may grow in the future. The type "ACCESS-VNF" can be used for functions that do not match any other values of the enumeration.|
|BBF_OBBAA_VNF_VERSION|String up to 64 ASCII printable characters|Version of the VNF/micro-service|
|BBF_OBBAA_VNF_VENDOR|String up to 64 ASCII printable characters|Vendor of the VNF/micro-service|

Below is a possible example for the environment variables section of the Helm Chart for deploying the vOMCI function (values.yaml):

```
  env:
    open:
      BBF_OBBAA_VNF_NAME: "BBF-vomci-instance1"
      BBF_OBBAA_VNF_TYPE: "VOMCI"
      BBF_OBBAA_VNF_VERSION: "5.0.0"
      BBF_OBBAA_VNF_VENDOR: "BBF"
```

Examples for getting the available micro-service types:

GET:

```
<?xml version="1.0" encoding="utf-8"?>
<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="">
  <get>
    <filter type="subtree">
      <baa-network-manager:network-manager xmlns:baa-network-manager="urn:bbf:yang:obbaa:network-manager"
                                           xmlns:bbf-baa-nfstate="urn:bbf:yang:obbaa:network-function-state">
        <bbf-baa-nfstate:network-functions-state>
          <bbf-baa-nfstate:virtual-network-function/>
        </bbf-baa-nfstate:network-functions-state>
      </baa-network-manager:network-manager>
    </filter>
  </get>
</rpc>
```

RESPONSE:

```
<rpc-reply xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
  <data>
    <baa-network-manager:network-manager xmlns:baa-network-manager="urn:bbf:yang:obbaa:network-manager">
      <bbf-baa-nfstate:network-functions-state xmlns:bbf-baa-nfstate="urn:bbf:yang:obbaa:network-function-state">
        <bbf-baa-nfstate:virtual-network-function>
          <!-- Read from information from the descriptor  -->
          <bbf-baa-nfstate:name>BBF-vOMCI</bbf-baa-nfstate:name>
          <bbf-baa-nfstate:vendor>BBF</bbf-baa-nfstate:vendor>
          <bbf-baa-nfstate:software-version>5.0.0</bbf-baa-nfstate:software-version>
          <bbf-baa-nfstate:network-function-type xmlns:bbf-nf-types="urn:bbf:yang:bbf-network-function-types">bbf-nf-types:vomci-function-type</bbf-baa-nfstate:network-function-type>

          <bbf-baa-nfstate:hosting-environment>
            <bbf-baa-nfstate:api-type-description>kubernetes</bbf-baa-nfstate:api-type-description>
            <bbf-baa-nfstate:api-version>1.0</bbf-baa-nfstate:api-version>
          </bbf-baa-nfstate:hosting-environment>

        </bbf-baa-nfstate:virtual-network-function>
      </bbf-baa-nfstate:network-functions-state>
    </baa-network-manager:network-manager>
  </data>
</rpc-reply>
```

Examples for getting the available micro-service instances:

GET:

```
<?xml version="1.0" encoding="utf-8"?>
<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="">
  <get>
    <filter type="subtree">
      <baa-network-manager:network-manager xmlns:baa-network-manager="urn:bbf:yang:obbaa:network-manager"
                xmlns:bbf-baa-nfstate="urn:bbf:yang:obbaa:network-function-state">
        <bbf-baa-nfstate:network-functions-state>
          <bbf-baa-nfstate:virtual-network-function-instance/>
        </bbf-baa-nfstate:network-functions-state>
      </baa-network-manager:network-manager>
    </filter>
  </get>
</rpc>
```

RESPONSE:

```
<rpc-reply xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
  <data>
    <baa-network-manager:network-manager xmlns:baa-network-manager="urn:bbf:yang:obbaa:network-manager">
      <bbf-baa-nfstate:network-functions-state xmlns:bbf-baa-nfstate="urn:bbf:yang:obbaa:network-function-state">
        <bbf-baa-nfstate:virtual-network-function-instance>
          <bbf-baa-nfstate:name>BBF-vomci-instance1</bbf-baa-nfstate:name>
          <bbf-baa-nfstate:virtual-network-function>BBF-vOMCI</bbf-baa-nfstate:virtual-network-function>
          <bbf-baa-nfstate:admin-state>unlocked</bbf-baa-nfstate:admin-state>
          <bbf-baa-nfstate:oper-state>up</bbf-baa-nfstate:oper-state>
        </bbf-baa-nfstate:virtual-network-function-instance>
      </bbf-baa-nfstate:network-functions-state>
    </baa-network-manager:network-manager>
  </data>
</rpc-reply>
```

[<--Using OB-BAA](../index.md#using)
