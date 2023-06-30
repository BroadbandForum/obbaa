<interfaces xmlns="urn:ietf:params:xml:ns:yang:ietf-interfaces" xmlns:bbf-subif-tag="urn:bbf:yang:bbf-sub-interface-tagging"
            xmlns:bbf-subif="urn:bbf:yang:bbf-sub-interfaces">
    <interface>
        <name>${l2vNniName}</name>
        <type xmlns:bbf-if-type="urn:bbf:yang:bbf-if-type">bbf-if-type:vlan-sub-interface</type>
        <bbf-subif:subif-lower-layer>
            <bbf-subif:interface>ethernetCsmacd.${l2vNniTpRef}</bbf-subif:interface>
        </bbf-subif:subif-lower-layer>
        <bbf-subif:inline-frame-processing>
            <bbf-subif:ingress-rule>
                <bbf-subif:rule>
                    <bbf-subif:name>rule1</bbf-subif:name>
                    <bbf-subif:priority>1</bbf-subif:priority>
                    <bbf-subif:flexible-match>
                        <bbf-subif-tag:match-criteria>
                            <bbf-subif-tag:tag>
                                <bbf-subif-tag:index>0</bbf-subif-tag:index>
                                <bbf-subif-tag:dot1q-tag>
                                    <bbf-subif-tag:tag-type xmlns:bbf-dot1q-types="urn:bbf:yang:bbf-dot1q-types">bbf-dot1q-types:c-vlan</bbf-subif-tag:tag-type>
                                    <bbf-subif-tag:vlan-id>${l2vNni_outTagVlanId}</bbf-subif-tag:vlan-id>
                                    <bbf-subif-tag:pbit>any</bbf-subif-tag:pbit>
                                </bbf-subif-tag:dot1q-tag>
                            </bbf-subif-tag:tag>
                        </bbf-subif-tag:match-criteria>
                    </bbf-subif:flexible-match>
                    <bbf-subif:ingress-rewrite>
                        <bbf-subif-tag:pop-tags>${l2vNni_popTags}</bbf-subif-tag:pop-tags>
                    </bbf-subif:ingress-rewrite>
                </bbf-subif:rule>
            </bbf-subif:ingress-rule>
        </bbf-subif:inline-frame-processing>
    </interface>
</interfaces>