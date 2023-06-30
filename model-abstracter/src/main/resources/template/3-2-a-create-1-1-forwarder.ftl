<bbf-l2-fwd:forwarding xmlns:bbf-l2-fwd="urn:bbf:yang:bbf-l2-forwarding">
    <bbf-l2-fwd:forwarders>
        <bbf-l2-fwd:forwarder>
            <bbf-l2-fwd:name>${fwderName}</bbf-l2-fwd:name>
            <bbf-l2-fwd:ports>
                <bbf-l2-fwd:port>
                    <bbf-l2-fwd:name>${srcSubIfName}</bbf-l2-fwd:name>
                    <bbf-l2-fwd:sub-interface>${srcSubIfName}</bbf-l2-fwd:sub-interface>
                </bbf-l2-fwd:port>
                <bbf-l2-fwd:port>
                    <bbf-l2-fwd:name>${destSubIfName}</bbf-l2-fwd:name>
                    <bbf-l2-fwd:sub-interface>${destSubIfName}</bbf-l2-fwd:sub-interface>
                </bbf-l2-fwd:port>
            </bbf-l2-fwd:ports>
        </bbf-l2-fwd:forwarder>
    </bbf-l2-fwd:forwarders>
</bbf-l2-fwd:forwarding>