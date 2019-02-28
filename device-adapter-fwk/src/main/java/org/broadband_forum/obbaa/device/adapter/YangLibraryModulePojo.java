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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendaylight.yangtools.yang.common.QName;

public class YangLibraryModulePojo {

    private List<QName> m_modules = null;
    private List<QName> m_supportedFeatures = new ArrayList<>();
    private Map<QName, List<QName>> m_supportedDeviations = new HashMap<>();
    private Map<QName, String> m_moduleToVariant = new HashMap<>();
    private String m_variant;

    public YangLibraryModulePojo(List<QName> modules, List<QName> supportedFeatures,
                                 Map<QName, List<QName>> supportedDeviations, String variant) {
        m_modules = modules;
        if (supportedFeatures != null) {
            m_supportedFeatures = supportedFeatures;
        }
        if (supportedDeviations != null) {
            m_supportedDeviations = supportedDeviations;
        }
        m_variant = variant;
    }

    public YangLibraryModulePojo(List<QName> modules, List<QName> supportedFeatures,
                                 Map<QName, List<QName>> supportedDeviations, Map<QName, String> moduleToFamily) {
        this(modules, supportedFeatures, supportedDeviations, (String) null);
        m_moduleToVariant = moduleToFamily;
    }

    public List<QName> getModules() {
        return m_modules;
    }

    public List<QName> getSupportedFeatures() {
        return m_supportedFeatures;
    }

    public Map<QName, List<QName>> getSupportedDeviations() {
        return m_supportedDeviations;
    }

    public Map<QName, String> getModuleToVariantNames() {
        return m_moduleToVariant;
    }

    public String getVariant() {
        return m_variant;
    }
}
