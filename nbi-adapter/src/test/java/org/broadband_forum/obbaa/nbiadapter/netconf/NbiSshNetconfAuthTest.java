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

import org.broadband_forum.obbaa.netconf.api.server.auth.ClientAuthenticationInfo;
import org.junit.Before;
import org.junit.Test;
import sun.security.provider.DSAPublicKey;

import static org.junit.Assert.*;

public class NbiSshNetconfAuthTest {

    private NbiSshNetconfAuth sshAuth;
    private ClientAuthenticationInfo clientInfoOk;
    private ClientAuthenticationInfo clientInfoError;

    @Before
    public void setUp() throws Exception {

        sshAuth = new NbiSshNetconfAuth();
        sshAuth.setUserName("NetconfAdmin");
        sshAuth.setPassword("NetconfPassword");

        clientInfoOk = new ClientAuthenticationInfo(0, "NetconfAdmin", "NetconfPassword",
                null, null);
        clientInfoError = new ClientAuthenticationInfo(0, "Amdin", "Admin",
                null, null);
    }

    @Test
    public void authenticate() throws Exception{

        assertTrue(sshAuth.authenticate(clientInfoOk));
        assertFalse(sshAuth.authenticate(clientInfoError));
        assertFalse(sshAuth.authenticate(new DSAPublicKey()));
    }
}