/*
 *   Copyright 2018 Broadband Forum
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package org.broadband_forum.obbaa.libconsult.utils;

import static org.broadband_forum.obbaa.libconsult.utils.LibraryConsultUtils.getDevReferredAdapterId;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.broadband_forum.obbaa.device.adapter.DeviceAdapterId;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.opendaylight.yangtools.yang.common.Revision;


public class YangReferenceInfo {

    private String m_moduleName;
    private String m_revisionDate;

    //all the adapters based on which the devices was created
    private Map<DeviceAdapterId, Set<Device>> m_adapterMap2RelDevices = new ConcurrentHashMap<>();

    public YangReferenceInfo(String moduleName, Revision revision) {
        this.m_moduleName = moduleName;
        this.m_revisionDate = revision.toString();
    }

    public YangReferenceInfo(String moduleName, String revisionDate) {
        this.m_moduleName = moduleName;
        this.m_revisionDate = revisionDate;
    }

    public String getModuleName() {
        return m_moduleName;
    }

    public String getRevisionDate() {
        return m_revisionDate;
    }

    public Map<DeviceAdapterId, Set<Device>> getReferredDevices() {
        return m_adapterMap2RelDevices;
    }

    public void updateReferredInfo(Device device) {
        DeviceAdapterId deviceAdapterId = getDevReferredAdapterId(device);
        Set<Device> devices = m_adapterMap2RelDevices.computeIfAbsent(deviceAdapterId, s -> new HashSet<>());
        devices.add(device);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        final YangReferenceInfo that = (YangReferenceInfo) object;

        if (m_moduleName != null ? !m_moduleName.equals(that.m_moduleName) : that.m_moduleName != null) {
            return false;
        }
        if (m_revisionDate != null ? !m_revisionDate.equals(that.m_revisionDate) : that.m_revisionDate != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = m_moduleName != null ? m_moduleName.hashCode() : 0;
        result = 31 * result + (m_revisionDate != null ? m_revisionDate.hashCode() : 0);
        return result;
    }

}
