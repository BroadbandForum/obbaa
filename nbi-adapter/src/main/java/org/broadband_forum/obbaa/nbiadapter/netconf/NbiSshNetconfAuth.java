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

package org.broadband_forum.obbaa.nbiadapter.netconf;

import java.io.Serializable;
import java.security.PublicKey;

import org.broadband_forum.obbaa.netconf.api.server.auth.ClientAuthenticationInfo;
import org.broadband_forum.obbaa.netconf.api.server.auth.NetconfServerAuthenticationHandler;


/**
 * OB-BAA NBI SSH Netconf server authentication class
 * Responsible for SSH Netconf Server username and password Authentication function.
 */
public class NbiSshNetconfAuth implements NetconfServerAuthenticationHandler {


    /**
     * User name and password are set by Sprint boot in application-context.xml file.
     */
    private String m_strUserName = "admin";

    private String m_strPassword = "password";


    public void setUserName(String userName) {
        m_strUserName = userName;
    }

    public void setPassword(String password) {
        m_strPassword = password;
    }

    /**
     *  NBI SSH Netconf server authenticate function based on user name and password.
     */
    @Override
    public boolean authenticate(ClientAuthenticationInfo clientAuthInfo) {

        if (m_strUserName.equals(clientAuthInfo.getUsername()) && m_strPassword.equals(clientAuthInfo.getPassword())) {
            LOGGER.info("Authentication is successful");
            return true;
        }

        LOGGER.info("Authentication is failed");
        return false;
    }

    /**
     *  NBI SSH Netconf server authenticate function based on public key.
     */
    @Override
    public boolean authenticate(PublicKey pubKey) {

        //Currently does not support public key authentication
        return false;
    }

    @Override
    public void logout(Serializable sshSessionId) {

    }
}
