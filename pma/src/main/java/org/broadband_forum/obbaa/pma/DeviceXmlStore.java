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

package org.broadband_forum.obbaa.pma;


import static org.broadband_forum.obbaa.pma.DeviceXmlStore.BAA_PMA_NS;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangContainer;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangXmlSubtree;

@YangContainer(name = "device-store", namespace = BAA_PMA_NS)
public class DeviceXmlStore {
    public static final String BAA_PMA_NS = "http://pma.baa.com";
    private final String m_filePath;
    @YangXmlSubtree
    String deviceXml;
    @YangSchemaPath
    String schemaPath;
    @YangParentId
    String parentId;

    public DeviceXmlStore(String filePath) {
        m_filePath = filePath;

        try {
            File file = new File(m_filePath);
            if (!file.exists()) {
                FileUtils.touch(file);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not touch file with path " + m_filePath, e);
        }
    }


    public String getDeviceXml() {
        return readFile();
    }

    private synchronized String readFile() {
        try {
            return FileUtils.readFileToString(new File(m_filePath), Charset.defaultCharset());
        } catch (IOException e) {
            throw new RuntimeException("Could read file with path " + m_filePath, e);
        }
    }

    public synchronized void setDeviceXml(String deviceXml) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(m_filePath))) {
            bw.write(deviceXml);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getSchemaPath() {
        return schemaPath;
    }

    public void setSchemaPath(String schemaPath) {
        this.schemaPath = schemaPath;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
}
