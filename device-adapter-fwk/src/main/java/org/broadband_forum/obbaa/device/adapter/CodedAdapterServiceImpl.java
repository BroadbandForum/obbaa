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

import static org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants.ADAPTER_XML_PATH;
import static org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants.DEFAULT_DEVIATIONS_PATH;
import static org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants.DEFAULT_FEATURES_PATH;
import static org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants.YANG_PATH;
import static org.broadband_forum.obbaa.device.adapter.CommonFileUtil.getAdapterDeviationsFromFile;
import static org.broadband_forum.obbaa.device.adapter.CommonFileUtil.getAdapterFeaturesFromFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.opendaylight.yangtools.yang.common.QName;
import org.osgi.framework.Bundle;

public class CodedAdapterServiceImpl implements CodedAdapterService {
    private AdapterManager m_adapterManager;
    private Bundle m_bundle;
    private Class m_klass;
    private SubSystem m_subsystem;
    private DeviceInterface m_deviceInterface;

    public CodedAdapterServiceImpl(AdapterManager adapterManager, Bundle bundle, Class klass, SubSystem subSystem,
                                   DeviceInterface deviceInterface) {
        m_adapterManager = adapterManager;
        m_bundle = bundle;
        m_klass = klass;
        m_subsystem = subSystem;
        m_deviceInterface = deviceInterface;
    }

    @Override
    public void deployAdapter() throws Exception {
        Map<URL, InputStream> moduleStream = buildModuleStreamMap(YANG_PATH);
        try (InputStream deviceXmlInputStream = m_bundle.getResource(ADAPTER_XML_PATH).openStream();
        InputStream supportedFeaturesInputStream = m_bundle.getResource(DEFAULT_FEATURES_PATH).openStream();
        InputStream supportedDeviationInputStream = m_bundle.getResource(DEFAULT_DEVIATIONS_PATH).openStream()) {
            Set<QName> features = getAdapterFeaturesFromFile(supportedFeaturesInputStream);
            Map<QName, Set<QName>> deviations = getAdapterDeviationsFromFile(supportedDeviationInputStream);
            DeviceAdapter adapter = AdapterBuilder.createAdapterBuilder()
                    .setDeviceXml(deviceXmlInputStream)
                    .setModuleStream(moduleStream)
                    .setSupportedFeatures(features)
                    .setSupportedDeviations(deviations)
                    .build();
            adapter.init();
            m_adapterManager.deploy(adapter, m_subsystem, m_klass, m_deviceInterface);
        }
    }

    @Override
    public void unDeployAdapter() throws Exception {
        DeviceAdapterId adapterId = CommonFileUtil.parseAdapterXMLFile(ADAPTER_XML_PATH, m_bundle);
        m_adapterManager.undeploy(m_adapterManager.getDeviceAdapter(adapterId));
    }

    protected Map<URL, InputStream> buildModuleStreamMap(String yangPath) throws IOException {
        Map<URL, InputStream> moduleStream = new HashMap<URL, InputStream>();
        List<String> yangModulePath = new ArrayList<>();
        if (yangPath != null) {
            yangModulePath = new ArrayList<>();
            Enumeration<URL> entries = m_bundle.findEntries(yangPath, "*.yang", true);
            if (entries != null) {
                while (entries.hasMoreElements()) {
                    URL entry = entries.nextElement();
                    moduleStream.put(entry, entry.openStream());
                    yangModulePath.add(entry.getPath());
                }
            }
            Collections.sort(yangModulePath);
        } else {
            for (String path : yangModulePath) {
                moduleStream.put(getClass().getResource(path), getClass().getResourceAsStream(path));
            }
        }
        return moduleStream;
    }
}

