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
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants;
import org.broadband_forum.obbaa.device.adapter.CommonFileUtil;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapterId;
import org.broadband_forum.obbaa.device.adapter.NonCodedAdapterService;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.broadband_forum.obbaa.pma.impl.DeviceXmlStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceAdapterActionHandlerImpl implements DeviceAdapterActionHandler {

    private String m_stagingArea;
    private NonCodedAdapterService m_nonCodedAdapterService;
    private SubSystem m_subSystem;
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceAdapterActionHandlerImpl.class);

    public DeviceAdapterActionHandlerImpl(NonCodedAdapterService nonCodedAdapterService, SubSystem subSystem) {
        m_nonCodedAdapterService = nonCodedAdapterService;
        m_stagingArea = nonCodedAdapterService.getStagingArea();
        m_subSystem = subSystem;
    }

    public void init() throws Exception {
        File dir = new File(m_stagingArea);
        File [] files = dir.listFiles((directory, name) -> {
            return name.endsWith(".zip");
        });
        for (File file : files) {
            deployRpc(file.getName());
        }
    }

    @Override
    public void deployRpc(String archiveName) throws Exception {
        LOGGER.info("Received request for deploying adapter zip : " + archiveName);
        if (archiveName != null) {
            String archive = archiveName.trim();
            deployAdapter(archive, m_subSystem);
        }
    }

    @Override
    public void undeploy(String archiveName) throws Exception {
        LOGGER.info("Received request for undeploying adapter zip : " + archiveName);
        if (archiveName != null) {
            String archive = archiveName.trim();
            undeployAdapter(archive);
        }
    }

    private void undeployAdapter(String archive) throws Exception {
        DeviceAdapterId adapter = getNonCodedPlugId(archive);
        String adapterId = adapter.getType() + AdapterSpecificConstants.DASH + adapter.getInterfaceVersion()
                + AdapterSpecificConstants.DASH + adapter.getModel() + AdapterSpecificConstants.DASH + adapter.getVendor();
        String destDir = m_stagingArea + File.separator + adapterId;
        String plugArchivePath = m_stagingArea + File.separator + archive;
        m_nonCodedAdapterService.unDeployPlug(adapter.getType(), adapter.getInterfaceVersion(), adapter.getModel(),
                adapter.getVendor());
        deleteAdapterFiles(destDir, plugArchivePath);

    }

    private void deleteAdapterFiles(String destDir, String archive) throws IOException {
        Path archieveFile = Paths.get(archive);
        Files.deleteIfExists(archieveFile);
        File unpackedDir = new File(destDir);
        if (unpackedDir.exists()) {
            FileUtils.deleteDirectory(unpackedDir);
        }

    }

    private void deployAdapter(String archive, SubSystem subSystem) throws Exception {
        DeviceAdapterId adapter = getNonCodedPlugId(archive);
        String adapterId = adapter.getType() + AdapterSpecificConstants.DASH + adapter.getInterfaceVersion()
                + AdapterSpecificConstants.DASH + adapter.getModel() + AdapterSpecificConstants.DASH + adapter.getVendor();
        String destDir = m_stagingArea + File.separator + adapterId;
        String plugArchivePath = m_stagingArea + File.separator + archive;
        operationsOnArchive(destDir, plugArchivePath);
        m_nonCodedAdapterService.deployPlug(adapterId, subSystem, DeviceXmlStore.class);
    }

    private DeviceAdapterId getNonCodedPlugId(String archive) throws Exception {
        return m_nonCodedAdapterService.getNonCodedPlugId(
                m_nonCodedAdapterService.getStagingArea() + File.separator + archive, AdapterSpecificConstants.ADAPTER_XML_PATH);
    }

    private void operationsOnArchive(String destDir, String plugArchivePath) throws Exception {
        validateExistenceOfPlugArchive(plugArchivePath);
        CommonFileUtil.unpackToSpecificDirectory(plugArchivePath, destDir);

    }

    protected void validateExistenceOfPlugArchive(String plugArchiveFileName) throws Exception {
        Path path = Paths.get(plugArchiveFileName);
        boolean exists = Files.exists(path, new LinkOption[] { LinkOption.NOFOLLOW_LINKS });
        if (!exists) {
            LOGGER.error(AdapterSpecificConstants.ADAPTER_DEFINITION_ARCHIVE_NOT_FOUND_ERROR);
            throw new Exception(AdapterSpecificConstants.ADAPTER_DEFINITION_ARCHIVE_NOT_FOUND_ERROR);
        }
    }
}
