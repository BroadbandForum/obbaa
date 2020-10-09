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
import static org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants.DASH;
import static org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants.DEFAULT_CONFIG_XML_PATH;
import static org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants.DEFAULT_DEVIATIONS_PATH;
import static org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants.DEFAULT_FEATURES_PATH;
import static org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants.DEFAULT_IPFIX_MAPPING_FILE;
import static org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants.DEFAULT_IPFIX_MAPPING_FILE_PATH;
import static org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants.MODEL;
import static org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants.SLASH;
import static org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants.YANG_LIB_FILE;
import static org.broadband_forum.obbaa.device.adapter.CommonFileUtil.getAdapterDeviationsFromFile;
import static org.broadband_forum.obbaa.device.adapter.CommonFileUtil.getAdapterFeaturesFromFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.device.adapter.AdapterBuilder;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.device.adapter.CommonFileUtil;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapter;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapterId;
import org.broadband_forum.obbaa.device.adapter.DeviceInterface;
import org.broadband_forum.obbaa.device.adapter.NonCodedAdapterService;
import org.broadband_forum.obbaa.device.adapter.VomciAdapterDeviceInterface;
import org.broadband_forum.obbaa.device.adapter.util.VariantFileUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.opendaylight.yangtools.yang.common.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NonCodedAdapterServiceImpl implements NonCodedAdapterService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NonCodedAdapterServiceImpl.class);
    private final NetconfConnectionManager m_netconfConnManager;
    private AdapterManager m_manager;

    public NonCodedAdapterServiceImpl(AdapterManager manager, NetconfConnectionManager netconfConnManager) {
        m_manager = manager;
        m_netconfConnManager = netconfConnManager;
    }

    @Override
    public void deployAdapter(String deviceAdapterId, SubSystem subSystem, Class klass, String stagingArea,
                              String ipfixStagingArea, DeviceInterface deviceInterface) throws Exception {
        String destDirname = stagingArea + File.separator + deviceAdapterId;
        String deviceXmlPath = destDirname + ADAPTER_XML_PATH;
        String yangXmlPath = destDirname + File.separator + YANG_LIB_FILE;
        String supportedFeaturesPath = destDirname + DEFAULT_FEATURES_PATH;
        String supportedDeviationPath = destDirname + DEFAULT_DEVIATIONS_PATH;
        String defaultXml = destDirname + DEFAULT_CONFIG_XML_PATH;
        String ipfixIeMappingFile = destDirname + File.separator + DEFAULT_IPFIX_MAPPING_FILE_PATH;
        File deviceFile = new File(deviceXmlPath);
        List<String> modulePaths = new ArrayList<>();
        Map<URL, InputStream> moduleStream = CommonFileUtil.buildModuleStreamMap(destDirname, modulePaths);
        VariantFileUtils.copyYangLibFiles(yangXmlPath, destDirname + SLASH + MODEL);
        try (InputStream deviceXmlInputStream = new FileInputStream(deviceFile);
             InputStream supportedFeaturesInputStream = new FileInputStream(supportedFeaturesPath);
             InputStream supportedDeviationInputStream = new FileInputStream(supportedDeviationPath);
             InputStream defaultXmlStream = new FileInputStream(defaultXml)) {
            Set<QName> features = getAdapterFeaturesFromFile(supportedFeaturesInputStream);
            Map<QName, Set<QName>> deviations = getAdapterDeviationsFromFile(supportedDeviationInputStream);
            byte[] defaultXmlbytes = IOUtils.toByteArray(defaultXmlStream);
            DeviceAdapter adapter = AdapterBuilder.createAdapterBuilder()
                    .setDeviceXml(deviceXmlInputStream)
                    .setModuleStream(moduleStream)
                    .setSupportedFeatures(features)
                    .setSupportedDeviations(deviations)
                    .setDefaultxmlBytes(defaultXmlbytes)
                    .build();
            adapter.init();
            moveIpfixMappingFileToStagingArea(ipfixIeMappingFile, ipfixStagingArea, adapter.getDeviceAdapterId());
            if (adapter.getType().equals("ONU")) {
                VomciAdapterDeviceInterface vomciDeviceInterface = new VomciAdapterDeviceInterface(m_netconfConnManager);
                m_manager.deploy(adapter, subSystem, klass, vomciDeviceInterface);
            } else {
                m_manager.deploy(adapter, subSystem, klass, deviceInterface);
            }
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

    private void moveIpfixMappingFileToStagingArea(String ipfixMappingFileName, String ipfixStagingArea,
                                                   DeviceAdapterId deviceAdapterId) {
        File ipfixMappingFile = new File(ipfixMappingFileName);
        if (ipfixMappingFile.exists()) {
            String deviceAdapter = deviceAdapterId.getVendor() + DASH
                    + deviceAdapterId.getType() + DASH
                    + deviceAdapterId.getModel() + DASH
                    + deviceAdapterId.getInterfaceVersion();
            String destDir = ipfixStagingArea + File.separator + deviceAdapter;
            File ipfixMappingFileDestDir = new File(destDir);
            if (!ipfixMappingFileDestDir.exists()) {
                ipfixMappingFileDestDir.mkdirs();
            } else if (!ipfixMappingFileDestDir.isDirectory()) {
                throw new RuntimeException(ipfixMappingFileDestDir + " is not a directory");
            }
            File ipfixMappingDestFile = new File(ipfixMappingFileDestDir, DEFAULT_IPFIX_MAPPING_FILE);
            try {
                FileUtils.copyFile(ipfixMappingFile, ipfixMappingDestFile);
            } catch (IOException e) {
                LOGGER.error("Error while copying IPFIX IE mapping file for adapter:" + deviceAdapter, e);
            }
        }
    }

}
