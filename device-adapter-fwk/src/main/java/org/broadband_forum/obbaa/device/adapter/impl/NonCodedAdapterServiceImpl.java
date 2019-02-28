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

package org.broadband_forum.obbaa.device.adapter.impl;

import static org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants.ADAPTER_XML_PATH;
import static org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants.DEFAULT_DEVIATIONS_PATH;
import static org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants.DEFAULT_FEATURES_PATH;
import static org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants.MODEL;
import static org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants.SLASH;
import static org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants.YANG_LIB_FILE;
import static org.broadband_forum.obbaa.device.adapter.CommonFileUtil.getAdapterDeviationsFromFile;
import static org.broadband_forum.obbaa.device.adapter.CommonFileUtil.getAdapterFeaturesFromFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.device.adapter.AdapterBuilder;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.device.adapter.CommonFileUtil;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapter;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapterId;
import org.broadband_forum.obbaa.device.adapter.DeviceInterface;
import org.broadband_forum.obbaa.device.adapter.NonCodedAdapterService;
import org.broadband_forum.obbaa.device.adapter.util.VariantFileUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.opendaylight.yangtools.yang.common.QName;

public class NonCodedAdapterServiceImpl implements NonCodedAdapterService {

    private AdapterManager m_manager;

    public NonCodedAdapterServiceImpl(AdapterManager manager) {
        m_manager = manager;
    }

    @Override
    public void deployAdapter(String deviceAdapterId, SubSystem subSystem, Class klass, String stagingArea,
                              DeviceInterface configAlign) throws Exception {
        String destDirname = stagingArea + File.separator + deviceAdapterId;
        String deviceXmlPath = destDirname + ADAPTER_XML_PATH;
        String yangXmlPath = destDirname + File.separator + YANG_LIB_FILE;
        String supportedFeaturesPath = destDirname + DEFAULT_FEATURES_PATH;
        String supportedDeviationPath = destDirname + DEFAULT_DEVIATIONS_PATH;
        File deviceFile = new File(deviceXmlPath);
        List<String> modulePaths = new ArrayList<>();
        Map<URL, InputStream> moduleStream = CommonFileUtil.buildModuleStreamMap(destDirname, modulePaths);
        VariantFileUtils.copyYangLibFiles(yangXmlPath, destDirname + SLASH + MODEL);
        try (InputStream deviceXmlInputStream = new FileInputStream(deviceFile);
             InputStream supportedFeaturesInputStream = new FileInputStream(supportedFeaturesPath);
             InputStream supportedDeviationInputStream = new FileInputStream(supportedDeviationPath)) {
            Set<QName> features = getAdapterFeaturesFromFile(supportedFeaturesInputStream);
            Map<QName, Set<QName>> deviations = getAdapterDeviationsFromFile(supportedDeviationInputStream);
            DeviceAdapter adapter = AdapterBuilder.createAdapterBuilder()
                    .setDeviceXml(deviceXmlInputStream)
                    .setModuleStream(moduleStream)
                    .setSupportedFeatures(features)
                    .setSupportedDeviations(deviations)
                    .build();
            adapter.init();
            m_manager.deploy(adapter, subSystem, klass, configAlign);
        }
    }

    @Override
    public void unDeployAdapter(String type, String interfaceVersion, String model, String vendor) throws Exception {
        m_manager.undeploy(m_manager.getDeviceAdapter(new DeviceAdapterId(type, interfaceVersion, model, vendor)));

    }

    @Override
    public DeviceAdapterId getNonCodedAdapterId(String adapterArchiveFileName, String deviceXmlpath) throws Exception {
        return CommonFileUtil.parseAdapterXMLFile(adapterArchiveFileName, deviceXmlpath);
    }

}
