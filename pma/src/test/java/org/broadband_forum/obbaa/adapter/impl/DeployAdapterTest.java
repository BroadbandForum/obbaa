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

package org.broadband_forum.obbaa.adapter.impl;


import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.broadband_forum.obbaa.adapter.handler.DeviceAdapterActionHandlerImpl;
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

public class DeployAdapterTest {

    private NonCodedAdapterService m_nonCodedAdapterService;
    private AdapterManager m_adapterManager;
    private DeviceAdapterActionHandlerImpl m_deviceAdapterActionHandlerImpl;
    public static final String ZIP_ARCHIVE_PATH = "noncodedadapter/adapterExample.zip";
    public static final String STAGED_AREA_ARCHIVE_PATH = "/adapterExample.zip";
    public static final String ZIP_ARCHIVE_PATH_INCORRECT = "noncodedadapter/adapterIncorrect.zip";
    public static final String STAGED_AREA_ARCHIVE_PATH_INCORRECT = "/adapterIncorrect.zip";
    private SubSystem m_subsystem;
    private File m_tempDir;
    private String m_dir;

    @Before
    public void setup() throws Exception {
        m_tempDir = com.google.common.io.Files.createTempDir();
        m_dir = m_tempDir.getAbsolutePath();
        m_adapterManager = mock(AdapterManagerImpl.class);
        m_subsystem = mock(DeviceSubsystem.class);
        m_nonCodedAdapterService = new NonCodedAdapterServiceImpl("noncodedadapter", m_adapterManager);
        m_nonCodedAdapterService.setStagingArea(m_dir);
        m_deviceAdapterActionHandlerImpl = new DeviceAdapterActionHandlerImpl(m_nonCodedAdapterService, m_subsystem);
    }

    @Test
    public void deployAdapterTest() throws Exception {
        copyFilesToStagingArea(m_dir, ZIP_ARCHIVE_PATH,  STAGED_AREA_ARCHIVE_PATH);

        m_deviceAdapterActionHandlerImpl.deployRpc("adapterExample.zip");
        verify(m_adapterManager).deploy(any(DeviceAdapter.class), any(), any());
    }

    @Test
    public void deployAdapterTestWhenAdapterFileNotPresentInCorrectPath() throws Exception {
        copyFilesToStagingArea(m_dir, ZIP_ARCHIVE_PATH_INCORRECT,  STAGED_AREA_ARCHIVE_PATH_INCORRECT);
        try {
            m_deviceAdapterActionHandlerImpl.deployRpc("adapterIncorrect.zip");
        } catch (Exception e) {
            assertEquals("adapter.xml file does not exist in the specified archive file", e.getMessage());
        }
    }

    @Test
    public void undeployAdapter() throws Exception {
        copyFilesToStagingArea(m_dir, ZIP_ARCHIVE_PATH,  STAGED_AREA_ARCHIVE_PATH);
        m_deviceAdapterActionHandlerImpl.undeploy("adapterExample.zip");
        verify(m_adapterManager).undeploy(any(DeviceAdapter.class));
    }

    @Test
    public void zipFiles() throws Exception {
        copyFilesToStagingArea(m_dir, ZIP_ARCHIVE_PATH,  STAGED_AREA_ARCHIVE_PATH);
        m_deviceAdapterActionHandlerImpl.init();
        verify(m_adapterManager).deploy(any(DeviceAdapter.class), any(), any());
    }

    private void copyFilesToStagingArea(String stagingAreaPath, String zipArchivePath, String stagedAreaArchivePath) throws IOException{
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String sourceYangsDirName = cl.getResource(zipArchivePath).getFile();

        Path sourcePath = Paths.get(sourceYangsDirName);
        Path destinationPath = Paths.get(stagingAreaPath+stagedAreaArchivePath);
        //overwrite existing file, if exists
        CopyOption[] options = new CopyOption[]{
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.COPY_ATTRIBUTES
        };
        Files.copy(sourcePath, destinationPath, options);
    }


    @After
    public void teardown() throws Exception{
        deleteIfExists(m_tempDir);
    }

    private void deleteIfExists(File file) {
        if(file != null && file.exists()){
            file.delete();
        }
    }
}
