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

package org.broadband_forum.obbaa.adapter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants;
import org.broadband_forum.obbaa.device.adapter.CommonFileUtil;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapterId;
import org.broadband_forum.obbaa.device.adapter.DeviceInterface;
import org.broadband_forum.obbaa.device.adapter.NonCodedAdapterService;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.broadband_forum.obbaa.pma.DeviceXmlStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StandardAdaptersDeployer implements AdapterDeployer {

    private String m_stagingArea;
    private final File m_stagingAreaDir;
    private NonCodedAdapterService m_nonCodedAdapterService;
    private SubSystem m_subSystem;
    private DeviceInterface m_deviceInterface;
    private static final Logger LOGGER = LoggerFactory.getLogger(StandardAdaptersDeployer.class);

    public StandardAdaptersDeployer(NonCodedAdapterService nonCodedAdapterService, SubSystem subSystem,
                                    String stagingArea, DeviceInterface deviceInterface) {
        m_nonCodedAdapterService = nonCodedAdapterService;
        m_stagingAreaDir = new File(stagingArea);
        m_stagingArea = stagingArea;
        m_subSystem = subSystem;
        m_deviceInterface = deviceInterface;
    }

    public void init() throws Exception {
        if (!m_stagingAreaDir.exists()) {
            m_stagingAreaDir.mkdirs();
        } else if (!m_stagingAreaDir.isDirectory()) {
            throw new RuntimeException(m_stagingArea + " is not a directory");
        }
        File dir = new File(m_stagingArea);
        File[] files = dir.listFiles((directory, name) -> name.endsWith(".zip"));
        if (files != null) {
            for (File file : files) {
                deployAdapter(file.getName());
            }
        }
    }

    @Override
    public void deployAdapter(String archiveName) throws Exception {
        LOGGER.info("Received request for deploying adapter zip : " + archiveName);
        if (archiveName != null) {
            String archive = archiveName.trim();
            deployAdapter(archive, m_subSystem, m_stagingArea, m_deviceInterface);
        }
    }

    private void deployAdapter(String archive, SubSystem subSystem, String stagingArea, DeviceInterface devInterface) throws Exception {
        DeviceAdapterId adapter = getNonCodedAdapterId(archive, stagingArea);
        String adapterId = adapter.getType() + AdapterSpecificConstants.DASH + adapter.getInterfaceVersion()
                + AdapterSpecificConstants.DASH + adapter.getModel() + AdapterSpecificConstants.DASH + adapter.getVendor();
        String destDir = stagingArea + File.separator + adapterId;
        String adpaterArchivePath = stagingArea + File.separator + archive;
        operationsOnArchive(destDir, adpaterArchivePath);
        m_nonCodedAdapterService.deployAdapter(adapterId, subSystem, DeviceXmlStore.class, stagingArea, devInterface);
    }

    @Override
    public void undeployAdapter(String archiveName) throws Exception {
        LOGGER.info("Received request for undeploying adapter zip : " + archiveName);
        if (archiveName != null) {
            String archive = archiveName.trim();
            undeployAdapter(archive, m_stagingArea);
        }
    }

    private void undeployAdapter(String archive, String stagingArea) throws Exception {
        DeviceAdapterId adapter = getNonCodedAdapterId(archive, stagingArea);
        String adapterId = adapter.getType() + AdapterSpecificConstants.DASH + adapter.getInterfaceVersion()
                + AdapterSpecificConstants.DASH + adapter.getModel() + AdapterSpecificConstants.DASH + adapter.getVendor();
        String destDir = stagingArea + File.separator + adapterId;
        String adapterArchivePath = stagingArea + File.separator + archive;
        m_nonCodedAdapterService.unDeployAdapter(adapter.getType(), adapter.getInterfaceVersion(), adapter.getModel(),
                adapter.getVendor());
        deleteAdapterFiles(destDir, adapterArchivePath);

    }

    private void deleteAdapterFiles(String destDir, String archive) throws IOException {
        Path archieveFile = Paths.get(archive);
        Files.deleteIfExists(archieveFile);
        File unpackedDir = new File(destDir);
        if (unpackedDir.exists()) {
            FileUtils.deleteDirectory(unpackedDir);
        }

    }

    private DeviceAdapterId getNonCodedAdapterId(String archive, String stagingArea) throws Exception {
        return m_nonCodedAdapterService.getNonCodedAdapterId(
                stagingArea + File.separator + archive, AdapterSpecificConstants.ADAPTER_XML_PATH);
    }

    private void operationsOnArchive(String destDir, String adapterArchivePath) throws Exception {
        validateExistenceOfAdapterArchive(adapterArchivePath);
        CommonFileUtil.unpackToSpecificDirectory(adapterArchivePath, destDir);

    }

    private void validateExistenceOfAdapterArchive(String adapterArchiveFileName) throws Exception {
        Path path = Paths.get(adapterArchiveFileName);
        boolean exists = Files.exists(path, new LinkOption[]{LinkOption.NOFOLLOW_LINKS});
        if (!exists) {
            LOGGER.error(AdapterSpecificConstants.ADAPTER_DEFINITION_ARCHIVE_NOT_FOUND_ERROR);
            throw new Exception(AdapterSpecificConstants.ADAPTER_DEFINITION_ARCHIVE_NOT_FOUND_ERROR);
        }
    }
}
