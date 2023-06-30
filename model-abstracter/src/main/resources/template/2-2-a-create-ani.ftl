<interfaces xmlns="urn:ietf:params:xml:ns:yang:ietf-interfaces" xmlns:bbf-xponani="urn:bbf:yang:bbf-xponani">
    <interface>
        <name>ani.${oltId}.${onuId}</name>
        <type xmlns:bbf-xpon-if-type="urn:bbf:yang:bbf-xpon-if-type">bbf-xpon-if-type:ani</type>
        <bbf-xponani:ani>
            <bbf-xponani:upstream-fec>true</bbf-xponani:upstream-fec>
            <bbf-xponani:management-gemport-aes-indicator>true</bbf-xponani:management-gemport-aes-indicator>
        </bbf-xponani:ani>
    </interface>
</interfaces>