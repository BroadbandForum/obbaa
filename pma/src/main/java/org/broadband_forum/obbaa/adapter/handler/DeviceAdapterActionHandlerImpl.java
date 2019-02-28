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

package org.broadband_forum.obbaa.adapter.handler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;
import org.apache.karaf.kar.KarService;
import org.broadband_forum.obbaa.adapter.AdapterDeployer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceAdapterActionHandlerImpl implements AdapterDeployer {

    private static final String KAR_EXTENSION = "kar";
    private String m_stagingArea;
    private final File m_stagingAreaDir;
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceAdapterActionHandlerImpl.class);
    private KarService m_karService;

    public DeviceAdapterActionHandlerImpl(String stagingArea, KarService karService) {
        m_stagingAreaDir = new File(stagingArea);
        m_stagingArea = stagingArea;
        m_karService = karService;
    }

    public void init() throws Exception {
        if (!m_stagingAreaDir.exists()) {
            m_stagingAreaDir.mkdirs();
        } else if (!m_stagingAreaDir.isDirectory()) {
            throw new RuntimeException(m_stagingArea + " is not a directory");
        }
        File dir = new File(m_stagingArea);
        File[] files = dir.listFiles((directory, name) -> name.endsWith(".kar"));
        if (files != null) {
            for (File file : files) {
                deployAdapter(file.getName());
            }
        }
    }

    @Override
    public void deployAdapter(String fileName) throws Exception {
        LOGGER.info("Received request for deploying coded adapter " + fileName);
        try {
            if (fileName != null) {
                String archive = m_stagingArea + File.separator + fileName.trim();
                String fileExt = FilenameUtils.getExtension(fileName);

                if (!KAR_EXTENSION.equals(fileExt)) {
                    throw new IOException("File format '" + fileExt + "' not supported");
                }
                deployKar(archive);
            } else {
                throw new RuntimeException("File name cannot be null");
            }
        } catch (Exception e) {
            LOGGER.error("Error when deploying coded adapter : ", e);
            throw new RuntimeException("Error when deploying coded adapter : " + e.getMessage());
        }
    }

    @Override
    public void undeployAdapter(String fileName) throws Exception {
        LOGGER.info("Received request for un-deploying coded adapter : " + fileName);
        try {
            if (fileName != null) {
                String fileExt = FilenameUtils.getExtension(fileName);
                String fileNameWOExt = FilenameUtils.removeExtension(fileName);

                if (!KAR_EXTENSION.equals(fileExt)) {
                    throw new IOException("File format '" + fileExt + "' not supported");
                }
                undeployKar(fileNameWOExt);
            }
        } catch (Exception e) {
            LOGGER.error("Error when un-deploying coded adapter : ", e);
            throw new RuntimeException("Error when un-deploying coded adapter : " + e);
        }
    }

    private void deployKar(String url) throws Exception {
        LOGGER.info("Installing kar ", url);
        m_karService.install(Paths.get(url).toUri());
    }

    private void undeployKar(String karName) throws Exception {
        LOGGER.info("Undeploying kar ", karName);
        m_karService.uninstall(karName);
    }
}
