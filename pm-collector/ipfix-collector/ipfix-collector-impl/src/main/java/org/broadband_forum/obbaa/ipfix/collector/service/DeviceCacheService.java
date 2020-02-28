package org.broadband_forum.obbaa.ipfix.collector.service;

public interface DeviceCacheService {

    String getDeviceFamily(String deviceName);

    void deleteDeviceFamilyCache(String var1);

}
