<bbf-l2-fwd:forwarding xmlns:bbf-l2-fwd="urn:bbf:yang:bbf-l2-forwarding">
    <bbf-l2-fwd:forwarders>
        <bbf-l2-fwd:forwarder>
            <bbf-l2-fwd:name>${vlanName}</bbf-l2-fwd:name>
            <bbf-l2-fwd:ports>
                <bbf-l2-fwd:port>
                    <bbf-l2-fwd:name>${portName}</bbf-l2-fwd:name>
                    <bbf-l2-fwd:sub-interface>${portTpId}</bbf-l2-fwd:sub-interface>
                </bbf-l2-fwd:port>
                <#list portList as port>
                    <bbf-l2-fwd:port>
                        <bbf-l2-fwd:name>${port.name}</bbf-l2-fwd:name>
                        <bbf-l2-fwd:sub-interface>vlan-sub-if.${vlanName}.${port.tpId}</bbf-l2-fwd:sub-interface>
                    </bbf-l2-fwd:port>
                </#list>
            </bbf-l2-fwd:ports>
        </bbf-l2-fwd:forwarder>
    </bbf-l2-fwd:forwarders>
</bbf-l2-fwd:forwarding>