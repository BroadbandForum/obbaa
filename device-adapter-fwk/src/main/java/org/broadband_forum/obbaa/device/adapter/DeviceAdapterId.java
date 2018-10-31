/*
 * Copyright 2018 Broadband Forum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadband_forum.obbaa.device.adapter;

import java.io.Serializable;

public class DeviceAdapterId implements Serializable {

    private String m_type;
    private String m_interfaceVersion;
    private String m_model;
    private String m_vendor;

    public DeviceAdapterId(String type, String interfaceVersion, String model, String vendor) {
        m_type = type;
        m_interfaceVersion = interfaceVersion;
        m_model = model;
        m_vendor = vendor;
    }

    public String getType() {
        return m_type;
    }

    public void setType(String type) {
        m_type = type;
    }

    public String getInterfaceVersion() {
        return m_interfaceVersion;
    }

    public void setInterfaceVersion(String interfaceVersion) {
        m_interfaceVersion = interfaceVersion;
    }

    public String getModel() {
        return m_model;
    }

    public void setModel(String model) {
        m_model = model;
    }

    public String getVendor() {
        return m_vendor;
    }

    public void setVendor(String vendor) {
        m_vendor = vendor;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        final DeviceAdapterId that = (DeviceAdapterId) object;

        if (m_type != null ? !m_type.equals(that.m_type) : that.m_type != null) {
            return false;
        }
        if (m_interfaceVersion != null ? !m_interfaceVersion.equals(that.m_interfaceVersion) : that.m_interfaceVersion != null) {
            return false;
        }
        if (m_model != null ? !m_model.equals(that.m_model) : that.m_model != null) {
            return false;
        }
        return m_vendor != null ? m_vendor.equals(that.m_vendor) : that.m_vendor == null;

    }

    @Override
    public int hashCode() {
        int result = m_type != null ? m_type.hashCode() : 0;
        result = 31 * result + (m_interfaceVersion != null ? m_interfaceVersion.hashCode() : 0);
        result = 31 * result + (m_model != null ? m_model.hashCode() : 0);
        result = 31 * result + (m_vendor != null ? m_vendor.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DeviceAdapterId{"
                + "m_type='" + m_type
                + ", m_interfaceVersion='" + m_interfaceVersion
                + ", m_model='" + m_model
                + ", m_vendor='" + m_vendor
                + '}';
    }

}
