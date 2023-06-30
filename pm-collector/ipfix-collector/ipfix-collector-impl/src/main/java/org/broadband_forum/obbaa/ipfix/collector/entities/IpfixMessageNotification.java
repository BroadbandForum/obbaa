package org.broadband_forum.obbaa.ipfix.collector.entities;

import java.util.Set;

import org.broadband_forum.obbaa.ipfix.entities.message.logging.IpfixDecodedData;

import com.google.gson.annotations.SerializedName;

public class IpfixMessageNotification {

    @SerializedName("sourceIP")
    private String m_sourceIP;

    @SerializedName("hostName")
    private String m_hostName;

    @SerializedName("deviceAdapter")
    private String m_deviceAdapter;

    @SerializedName("templateID")
    private int m_templateID;

    @SerializedName("observationDomain")
    private long m_observationDomain;

    @SerializedName("timestamp")
    private String m_timestamp;

    @SerializedName("data")
    private Set<IpfixDecodedData> m_data;

    public String getSourceIP() {
        return m_sourceIP;
    }

    public void setSourceIP(String sourceIP) {
        this.m_sourceIP = sourceIP;
    }

    public String getHostName() {
        return m_hostName;
    }

    public void setHostName(String hostName) {
        this.m_hostName = hostName;
    }

    public int getTemplateID() {
        return m_templateID;
    }

    public void setTemplateID(int templateID) {
        this.m_templateID = templateID;
    }

    public long getObservationDomain() {
        return m_observationDomain;
    }

    public void setObservationDomain(long observationDomain) {
        this.m_observationDomain = observationDomain;
    }

    public String getDeviceAdapter() {
        return m_deviceAdapter;
    }

    public void setDeviceAdapter(String deviceAdapter) {
        this.m_deviceAdapter = deviceAdapter;
    }

    public String getTimestamp() {
        return m_timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.m_timestamp = timestamp;
    }

    public Set<IpfixDecodedData> getData() {
        return m_data;
    }

    public void setData(Set<IpfixDecodedData> data) {
        this.m_data = data;
    }
}