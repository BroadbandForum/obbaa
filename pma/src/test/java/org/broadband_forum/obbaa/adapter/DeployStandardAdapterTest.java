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


import static com.google.common.io.Files.createTempDir;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.FileUtils;
import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapter;
import org.broadband_forum.obbaa.device.adapter.NonCodedAdapterService;
import org.broadband_forum.obbaa.device.adapter.impl.AdapterManagerImpl;
import org.broadband_forum.obbaa.device.adapter.impl.NonCodedAdapterServiceImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.broadband_forum.obbaa.pma.impl.DeviceSubsystem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DeployStandardAdapterTest {

    private NonCodedAdapterService m_nonCodedAdapterService;
    private AdapterManager m_adapterManager;
    private StandardAdaptersDeployer m_adaptersDeployer;
    public static final String ZIP_ARCHIVE_PATH = "noncodedadapter/adapter.zip";
    public static final String ZIP_ARCHIVE_PATH_WITHOUT_YANG_LIB = "noncodedadapter/adapterWithoutYang-library.zip";
    public static final String STAGED_AREA_ARCHIVE_PATH = "/adapter.zip";
    public static final String STAGED_AREA_ARCHIVE_PATH_WITHOUT_YANG_LIB = "/adapterWithoutYang-library.zip";
    public static final String ZIP_ARCHIVE_PATH_INCORRECT = "noncodedadapter/adapterIncorrect.zip";
    public static final String STAGED_AREA_ARCHIVE_PATH_INCORRECT = "/adapterIncorrect.zip";
    private SubSystem m_subsystem;
    private File m_tempDir;
    private String m_dir;
    private NetconfConnectionManager m_connectionManager;

    @Before
    public void setup() throws Exception {
        m_tempDir = createTempDir();
        m_dir = m_tempDir.getAbsolutePath();
        m_adapterManager = mock(AdapterManagerImpl.class);
        m_connectionManager = mock(NetconfConnectionManager.class);
        m_subsystem = mock(DeviceSubsystem.class);
        m_nonCodedAdapterService = new NonCodedAdapterServiceImpl(m_adapterManager, m_connectionManager);
        m_adaptersDeployer = new StandardAdaptersDeployer(m_nonCodedAdapterService, m_subsystem, m_dir, m_dir, null);
    }

    @Test
    public void deployAdapterTest() throws Exception {
        copyFilesToStagingArea(m_dir, ZIP_ARCHIVE_PATH, STAGED_AREA_ARCHIVE_PATH);

        m_adaptersDeployer.deployAdapter("adapter.zip");
        verify(m_adapterManager).deploy(any(DeviceAdapter.class), any(), any(), any());
    }

    @Test(expected = FileNotFoundException.class)
    public void deployAdapterTestFail() throws Exception {
        copyFilesToStagingArea(m_dir, ZIP_ARCHIVE_PATH_WITHOUT_YANG_LIB, STAGED_AREA_ARCHIVE_PATH_WITHOUT_YANG_LIB);
        m_adaptersDeployer.deployAdapter("adapterWithoutYang-library.zip");
    }

    @Test
    public void deployAdapterTestWhenAdapterFileNotPresentInCorrectPath() throws Exception {
        copyFilesToStagingArea(m_dir, ZIP_ARCHIVE_PATH_INCORRECT, STAGED_AREA_ARCHIVE_PATH_INCORRECT);
        try {
            m_adaptersDeployer.deployAdapter("adapterIncorrect.zip");
        } catch (Exception e) {
            assertEquals("device-adapter.xml file does not exist in the specified archive file", e.getMessage());
        }
    }

    @Test
    public void undeployAdapter() throws Exception {
        copyFilesToStagingArea(m_dir, ZIP_ARCHIVE_PATH, STAGED_AREA_ARCHIVE_PATH);
        m_adaptersDeployer.undeployAdapter("adapter.zip");
        verify(m_adapterManager).undeploy(any(DeviceAdapter.class));
    }

    @Test
    public void zipFiles() throws Exception {
        copyFilesToStagingArea(m_dir, ZIP_ARCHIVE_PATH, STAGED_AREA_ARCHIVE_PATH);
        m_adaptersDeployer.init();
        verify(m_adapterManager).deploy(any(DeviceAdapter.class), any(), any(), any());
    }

    private void copyFilesToStagingArea(String stagingAreaPath, String zipArchivePath, String stagedAreaArchivePath) throws IOException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String sourceYangsDirName = cl.getResource(zipArchivePath).getFile();

        if (System.getProperty("os.name").startsWith("Windows")) {
            sourceYangsDirName = sourceYangsDirName.substring(1);
        }

        Path sourcePath = Paths.get(sourceYangsDirName);
        Path destinationPath = Paths.get(stagingAreaPath + stagedAreaArchivePath);
        //overwrite existing file, if exists
        CopyOption[] options = new CopyOption[]{
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.COPY_ATTRIBUTES
        };
        Files.copy(sourcePath, destinationPath, options);
    }

    @After
    public void teardown() throws Exception {
        deleteIfExists(m_tempDir);
    }

    private void deleteIfExists(File file) throws IOException {
        if (file != null && file.exists()) {
            FileUtils.deleteDirectory(file);
        }
    }
}
