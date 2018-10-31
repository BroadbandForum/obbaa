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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants;
import org.broadband_forum.obbaa.device.adapter.CommonFileUtil;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapter;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapterId;
import org.broadband_forum.obbaa.device.adapter.NonCodedAdapterService;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;

public class NonCodedAdapterServiceImpl implements NonCodedAdapterService {

    private final File m_stagingAreaDir;
    private String m_stagingArea;
    private AdapterManager m_manager;

    public NonCodedAdapterServiceImpl(String stagingArea, AdapterManager manager) {
        m_stagingArea = stagingArea;
        m_manager = manager;
        m_stagingAreaDir = new File(m_stagingArea);
    }

    public void init() {
        if (!m_stagingAreaDir.exists()) {
            m_stagingAreaDir.mkdirs();
        } else if (!m_stagingAreaDir.isDirectory()) {
            throw new RuntimeException(m_stagingArea + " is not a directory");
        }
    }

    @Override
    public void deployPlug(String devicePlugId, SubSystem subSystem, Class klass) throws Exception {
        String destDirname = m_stagingArea + File.separator + devicePlugId;
        String deviceXmlPath = destDirname + AdapterSpecificConstants.ADAPTER_XML_PATH;
        File deviceFile = new File(deviceXmlPath);
        InputStream inputStream = new FileInputStream(deviceFile);
        List<String> modulePaths = new ArrayList<>();
        Map<URL, InputStream> moduleStream = CommonFileUtil.buildModuleStreamMap(destDirname, modulePaths);
        DeviceAdapter adapter = new DeviceAdapter(null, null, null, null, null, inputStream, moduleStream);
        adapter.init();
        adapter.setModuleStream(moduleStream);
        m_manager.deploy(adapter, subSystem, klass);
    }

    @Override
    public void unDeployPlug(String type, String interfaceVersion, String model, String vendor) throws Exception {
        m_manager.undeploy(m_manager.getDeviceAdapter(new DeviceAdapterId(type, interfaceVersion, model, vendor)));

    }

    @Override
    public DeviceAdapterId getNonCodedPlugId(String plugArchiveFileName, String deviceXmlpath) throws Exception {
        return CommonFileUtil.parseAdapterDeviceXMLFile(plugArchiveFileName, deviceXmlpath);
    }

    @Override
    public String getStagingArea() {
        return m_stagingArea;
    }

    @Override
    public void setStagingArea(String stagingArea) {
        m_stagingArea = stagingArea;
    }
}
