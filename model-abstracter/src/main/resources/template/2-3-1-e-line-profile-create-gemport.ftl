<bbf-xpongemtcont:xpongemtcont xmlns:bbf-xpongemtcont="urn:bbf:yang:bbf-xpongemtcont">
    <bbf-xpongemtcont:gemports>
        <bbf-xpongemtcont:gemport>
            <bbf-xpongemtcont:name>gemport.${oltId}.${onuId}.${virtualPortName}</bbf-xpongemtcont:name>
            <bbf-xpongemtcont:interface>olt-v-enet.${oltId}.${onuId}.${virtualPortName}</bbf-xpongemtcont:interface>
            <bbf-xpongemtcont:traffic-class>0</bbf-xpongemtcont:traffic-class>
            <bbf-xpongemtcont:tcont-ref>tcont.${oltId}.${onuId}</bbf-xpongemtcont:tcont-ref>
        </bbf-xpongemtcont:gemport>
    </bbf-xpongemtcont:gemports>
</bbf-xpongemtcont:xpongemtcont>