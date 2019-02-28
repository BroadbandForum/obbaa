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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UserInfo;


public final class SftpFileTransferUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(SftpFileTransferUtil.class);

    private SftpFileTransferUtil() {
    }

    public static void transfer(SftpTargetInfo sftpTargetInfo, Session sftpSession) throws IOException, SftpException, JSchException {
        sftpFileTransfer(sftpTargetInfo, sftpSession);
    }

    private static void sftpFileTransfer(SftpTargetInfo sftpTargetInfo, Session sftpSession) throws FileNotFoundException,
            JSchException, SftpException {
        FileInputStream fileInputStream = null;
        ChannelSftp sftpChannel = null;
        try {
            File localFile = sftpTargetInfo.getLocalFile();
            fileInputStream = new FileInputStream(localFile);

            if (sftpSession == null) {
                sftpSession = createSftpSession(sftpTargetInfo.getServerAddress(), sftpTargetInfo.getUserName(),
                        sftpTargetInfo.getPassword(), sftpTargetInfo.getPortNumber());
            }
            sftpChannel = (ChannelSftp) sftpSession.openChannel("sftp");
            String targetFolder = sftpTargetInfo.getRemoteDirectory();

            LOGGER.debug("Connecting to SFTP channel " + sftpTargetInfo.getServerAddress());
            sftpChannel.connect();

            sftpChannel.cd(targetFolder);
            sftpChannel.put(fileInputStream, localFile.getName());
            LOGGER.debug("Successfully transfered " + localFile + " to location " + targetFolder);
        } finally {
            if (fileInputStream != null) {
                IOUtils.closeQuietly(fileInputStream);
            }
            if (sftpChannel != null) {
                sftpChannel.disconnect();
            }
            if (sftpSession != null) {
                sftpSession.disconnect();
            }
        }
    }

    public static Session createSftpSession(String targetIp, String user, String password, int portNumber)
            throws JSchException {
        LOGGER.debug("Creating SFTP session to" + targetIp);
        Session session = getSession(targetIp, user, portNumber);
        UserInfo ui = new SftpUserInfo(password);
        session.setUserInfo(ui);

        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        config.put("PreferredAuthentications", "publickey,keyboard-interactive,password");
        session.setConfig(config);
        session.connect(30000);
        return session;
    }


    private static final class SftpUserInfo implements UserInfo {
        private final String m_password;

        private SftpUserInfo(String password) {
            m_password = password;
        }

        public String getPassphrase() {
            return null;
        }

        public String getPassword() {
            return m_password;
        }

        public boolean promptPassphrase(String arg0) {
            return false;
        }

        public boolean promptPassword(String arg0) {
            return true;
        }

        public boolean promptYesNo(String arg0) {
            return false;
        }

        public void showMessage(String arg0) {
        }
    }

    private static Session getSession(String targetIp, String user, int portNumber) throws JSchException {

        return new JSch().getSession(user, targetIp, portNumber);
    }
}
