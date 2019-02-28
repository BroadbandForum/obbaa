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

package org.broadband_forum.obbaa.protocol.tls;

import java.io.File;

public class SftpTargetInfo {

    public String m_serverAddress;

    public int m_portNumber = 22;

    public String m_userName;

    public String m_password;

    public File m_localFile;

    public String m_remoteDirectory;

    public SftpTargetInfo(String hostName, String userName, String password, String remoteDirectory, File file, int port) {
        m_serverAddress = hostName;
        m_userName = userName;
        m_password = password;
        m_remoteDirectory = remoteDirectory;
        m_localFile = file;
        m_portNumber = port;
    }

    public String getServerAddress() {
        return m_serverAddress;
    }

    public String getRemoteDirectory() {
        return m_remoteDirectory;
    }

    public File getLocalFile() {
        return m_localFile;
    }

    public String getPassword() {
        return m_password;
    }

    public String getUserName() {
        return m_userName;
    }

    public int getPortNumber() {
        return m_portNumber;
    }

}
