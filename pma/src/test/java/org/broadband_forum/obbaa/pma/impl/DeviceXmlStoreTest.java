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

package org.broadband_forum.obbaa.pma.impl;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.broadband_forum.obbaa.pma.DeviceXmlStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DeviceXmlStoreTest {
    private static final String DEVICE_DATA1 = "<device-data xmlns=\"http://namespace\"><data/></device-data>";
    private static final String DEVICE_DATA2 = "<device-data xmlns=\"http://namespace\"><data2/></device-data>";;
    DeviceXmlStore m_store;
    private File m_tempFile;

    @Before
    public void setUp() throws IOException {
        m_tempFile = File.createTempFile("temp-file-name", ".xml");
        m_store = new DeviceXmlStore(m_tempFile.getAbsolutePath());
    }

    @After
    public void tearDown(){
        if(m_tempFile !=null && m_tempFile.exists()){
            m_tempFile.delete();
        }
    }

    @Test
    public void testStoreReadsFromFileAndWritesToFile() throws IOException {
        assertEquals("", m_store.getDeviceXml());

        m_store.setDeviceXml(DEVICE_DATA1);
        assertEquals(DEVICE_DATA1, m_store.getDeviceXml());
        assertEquals(DEVICE_DATA1, FileUtils.readFileToString(m_tempFile, Charset.defaultCharset()));

        m_store.setDeviceXml(DEVICE_DATA2);
        assertEquals(DEVICE_DATA2, m_store.getDeviceXml());
        assertEquals(DEVICE_DATA2, FileUtils.readFileToString(m_tempFile, Charset.defaultCharset()));
    }

}
