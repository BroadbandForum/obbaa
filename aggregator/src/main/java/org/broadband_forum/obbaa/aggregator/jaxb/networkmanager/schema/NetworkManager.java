package org.broadband_forum.obbaa.aggregator.jaxb.networkmanager.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "network-manager", namespace = "urn:bbf:yang:obbaa:network-manager")
public class NetworkManager {
    private ManagedDevices m_managedDevices;
    private ManagedNetworkFunctions m_managedNetworkFunctions;

    @XmlElement(name = "managed-devices")
    public ManagedDevices getManagedDevices() {
        return m_managedDevices;
    }

    public void setManagedDevices(ManagedDevices managedDevices) {
        m_managedDevices = managedDevices;
    }

    @XmlElement(name = "network-functions")
    public ManagedNetworkFunctions getManagedNetworkFunctions() {
        return m_managedNetworkFunctions;
    }

    public void setManagedNetworkFunctions(ManagedNetworkFunctions managedNetworkFunctions) {
        m_managedNetworkFunctions = managedNetworkFunctions;
    }
}
