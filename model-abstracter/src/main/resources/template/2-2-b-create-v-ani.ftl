<interfaces xmlns="urn:ietf:params:xml:ns:yang:ietf-interfaces" xmlns:bbf-xponvani="urn:bbf:yang:bbf-xponvani">
    <interface>
        <name>v-ani.${oltId}.${onuId}</name>
        <description>v-ani</description>
        <type xmlns:bbf-xpon-if-type="urn:bbf:yang:bbf-xpon-if-type">bbf-xpon-if-type:v-ani</type>
        <enabled>true</enabled>
        <bbf-xponvani:v-ani>
            <bbf-xponvani:onu-id>101</bbf-xponvani:onu-id>
            <bbf-xponvani:channel-partition>0</bbf-xponvani:channel-partition>
            <bbf-xponvani:expected-serial-number>ABCD12345678</bbf-xponvani:expected-serial-number>
        </bbf-xponvani:v-ani>
    </interface>
</interfaces>