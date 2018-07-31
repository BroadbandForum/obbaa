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

package org.broadband_forum.obbaa.connectors.sbi.netconf.impl;

import org.broadband_forum.obbaa.netconf.api.x509certificates.DynamicX509KeyManagerImpl;
import org.broadband_forum.obbaa.netconf.api.x509certificates.DynamicX509TrustManagerImpl;
import org.broadband_forum.obbaa.netconf.api.x509certificates.KeyManagerInitException;
import org.broadband_forum.obbaa.netconf.api.x509certificates.TrustManagerInitException;

public final class DynamicX509Factory {

    private DynamicX509Factory(){}

    public static DynamicX509TrustManagerImpl createDynamicTrustManager(String trustChainPathVar) throws
            TrustManagerInitException {
        return new DynamicX509TrustManagerImpl(SystemPropertyUtils.getInstance().getFromEnvOrSysProperty(
                trustChainPathVar, "/baa/baa-dist/conf/tls/rootCA.crt"));
    }

    public static DynamicX509KeyManagerImpl createDynamicKeyManager(String pkCertPathVar, String pkPathVar, String
            pkPassVar) throws KeyManagerInitException {
        String tlsPrivateKeyPass = SystemPropertyUtils.getInstance().getFromEnvOrSysProperty(pkPassVar, "");
        return new DynamicX509KeyManagerImpl(SystemPropertyUtils.getInstance().getFromEnvOrSysProperty(pkCertPathVar,
                "/baa/baa-dist/conf/tls/certchain.crt"), SystemPropertyUtils.getInstance().getFromEnvOrSysProperty(pkPathVar,
                "/baa/baa-dist/conf/tls/privatekey.pem"),
                tlsPrivateKeyPass);
    }
}
