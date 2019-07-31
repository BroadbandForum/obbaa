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


import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opendaylight.yangtools.yang.common.QName;


public class AdapterBuilder {

    private List<String> m_caps;
    private InputStream m_deviceXml;
    private Map<URL, InputStream> m_moduleStream;
    private DeviceAdapterId m_deviceAdapterId;
    private Set<QName> m_supportedFeatures;
    private Map<QName, Set<QName>> m_supportedDeviations;
    private byte[] m_defaultXmlBytes;

    public AdapterBuilder() {
    }

    public static AdapterBuilder createAdapterBuilder() {
        return new AdapterBuilder();
    }

    public AdapterBuilder setDeviceAdapterId(DeviceAdapterId deviceAdapterId) {
        this.m_deviceAdapterId = deviceAdapterId;
        return this;
    }

    public AdapterBuilder setCaps(List<String> caps) {
        this.m_caps = caps;
        return this;
    }

    public AdapterBuilder setDeviceXml(InputStream deviceXml) {
        this.m_deviceXml = deviceXml;
        return this;
    }

    public AdapterBuilder setModuleStream(Map<URL, InputStream> moduleStream) {
        this.m_moduleStream = moduleStream;
        return this;
    }

    public AdapterBuilder setSupportedFeatures(Set<QName> supportedFeatures) {
        this.m_supportedFeatures = supportedFeatures;
        return this;
    }

    public DeviceAdapter build() {
        if (m_deviceAdapterId == null) {
            m_deviceAdapterId = new DeviceAdapterId();
        }
        return new DeviceAdapter(m_deviceAdapterId, m_caps, m_deviceXml, m_supportedFeatures, m_supportedDeviations,
                m_moduleStream, m_defaultXmlBytes);
    }

    public AdapterBuilder setSupportedDeviations(Map<QName, Set<QName>> supportedDevations) {
        this.m_supportedDeviations = supportedDevations;
        return this;
    }

    public AdapterBuilder setDefaultxmlBytes(byte[] defaultXmlBytes) {
        this.m_defaultXmlBytes = defaultXmlBytes;
        return this;
    }
}
