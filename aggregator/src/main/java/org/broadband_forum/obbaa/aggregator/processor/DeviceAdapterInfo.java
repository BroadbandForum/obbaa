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

package org.broadband_forum.obbaa.aggregator.processor;

public class DeviceAdapterInfo {
    private String m_type;
    private String m_softwareVersion;
    private String m_model;
    private String m_vendor;

    public DeviceAdapterInfo() {
    }

    public DeviceAdapterInfo(String type, String softwareVersion, String model, String vendor) {
        m_type = type;
        m_softwareVersion = softwareVersion;
        m_model = model;
        m_vendor = vendor;
    }

    public String getType() {
        return m_type;
    }

    public void setType(String type) {
        m_type = type;
    }

    public String getSoftwareVersion() {
        return m_softwareVersion;
    }

    public void setSoftwareVersion(String softwareVersion) {
        m_softwareVersion = softwareVersion;
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
}
