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

import static com.google.common.io.Files.createTempDir;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.broadband_forum.obbaa.connectors.sbi.netconf.NetconfConnectionManager;
import org.broadband_forum.obbaa.device.adapter.AdapterManager;
import org.broadband_forum.obbaa.device.adapter.AdapterSpecificConstants;
import org.broadband_forum.obbaa.device.adapter.CommonFileUtil;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapter;
import org.broadband_forum.obbaa.device.adapter.DeviceAdapterId;
import org.broadband_forum.obbaa.device.adapter.DeviceInterface;
import org.broadband_forum.obbaa.device.adapter.NonCodedAdapterService;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

public class NonCodedAdapterServiceImplTest {

    private NonCodedAdapterService m_nonCodedAdapterService;
    private AdapterManager m_adapterManager;
    public static final String ZIP_ARCHIVE_PATH = "noncodedadapter/adapter.zip";
    public static final String STAGED_AREA_ARCHIVE_PATH = "/adapter.zip";
    private File m_tempDir;
    private String m_dir;
    private DeviceAdapterId m_expectedAdapterId;
    @Mock
    private SubSystem m_subsystem;
    @Mock
    private DeviceInterface m_devInterface;
    @Mock
    private NetconfConnectionManager m_connectionManager;
    private String m_expectedDefaultXml;

    @Before
    public void setup() throws Exception {
        m_tempDir = createTempDir();
        m_dir = m_tempDir.getAbsolutePath();
        m_adapterManager = mock(AdapterManagerImpl.class);
        m_nonCodedAdapterService = new NonCodedAdapterServiceImpl(m_adapterManager, m_connectionManager);
        m_expectedAdapterId = new DeviceAdapterId("DPU","1.0", "example", "UT");
        m_expectedDefaultXml = "<data>" + System.lineSeparator() +
                "\t<if:interfaces" + System.lineSeparator() +
                "\t\txmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\"" + System.lineSeparator() +
                "\t\txmlns:bbfift=\"urn:broadband-forum-org:yang:bbf-if-type\">" + System.lineSeparator() +
                "\t\t<if:interface>" + System.lineSeparator() +
                "\t\t\t<if:name>UT-Interface</if:name>" + System.lineSeparator() +
                "\t\t\t<if:type>bbfift:xdsl</if:type>" + System.lineSeparator() +
                "\t\t</if:interface>" + System.lineSeparator() +
                "\t</if:interfaces>" + System.lineSeparator() +
                "</data>";
    }

    @Test
    public void testGetAdapterID() throws Exception {
        DeviceAdapterId adapterId = getDeviceAdapterId();
        assertEquals(m_expectedAdapterId,  adapterId);
    }

    @Test
    public void testDeployNonCodedAdapter() throws Exception {
        DeviceAdapterId adapter = getDeviceAdapterId();
        ArgumentCaptor<DeviceAdapter> devAdapter = ArgumentCaptor.forClass(DeviceAdapter.class);
        String adapterId = adapter.getType() + AdapterSpecificConstants.DASH + adapter.getInterfaceVersion()
                + AdapterSpecificConstants.DASH + adapter.getModel() + AdapterSpecificConstants.DASH + adapter.getVendor();
        CommonFileUtil.unpackToSpecificDirectory(m_dir
                + STAGED_AREA_ARCHIVE_PATH, m_dir + File.separator + adapterId);

        m_nonCodedAdapterService.deployAdapter(adapterId, m_subsystem, this.getClass(), m_dir, m_dir, m_devInterface);
        verify(m_adapterManager).deploy(devAdapter.capture(), eq(m_subsystem), eq(this.getClass()), eq(m_devInterface));
        DeviceAdapter actualAdapter = devAdapter.getValue();
        assertEquals(m_expectedAdapterId, actualAdapter.getDeviceAdapterId());
        assertEquals(3, actualAdapter.getSupportedFeatures().size());
        assertEquals(1, actualAdapter.getSupportedDevations().size());
        assertEquals(5, actualAdapter.getCapabilities().size());
        assertEquals(3, actualAdapter.getRevisions().size());
        assertEquals(m_expectedDefaultXml, IOUtils.toString(new ByteArrayInputStream(actualAdapter.getDefaultXmlBytes())).trim());
    }

    private DeviceAdapterId getDeviceAdapterId() throws Exception {
        copyFilesToStagingArea(m_dir, ZIP_ARCHIVE_PATH, STAGED_AREA_ARCHIVE_PATH);
        return m_nonCodedAdapterService.getNonCodedAdapterId(m_dir + STAGED_AREA_ARCHIVE_PATH,
                AdapterSpecificConstants.ADAPTER_XML_PATH);
    }

    private void copyFilesToStagingArea(String stagingAreaPath, String zipArchivePath, String stagedAreaArchivePath) throws IOException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String sourceYangsDirName = cl.getResource(zipArchivePath).getFile();

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
