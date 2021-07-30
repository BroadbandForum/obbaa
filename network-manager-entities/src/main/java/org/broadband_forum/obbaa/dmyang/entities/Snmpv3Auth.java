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

package org.broadband_forum.obbaa.dmyang.entities;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangAttribute;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangContainer;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;

@Entity
@YangContainer(name = DeviceManagerNSConstants.SNMPV3_AUTH, namespace = DeviceManagerNSConstants.NS)
public class Snmpv3Auth {
    @Id
    @YangParentId
    @Column(name = YangParentId.PARENT_ID_FIELD_NAME)
    private String parentId;

    @YangSchemaPath
    @Column(length = 1000)
    private String schemaPath;

    @YangAttribute(name = DeviceManagerNSConstants.USER_NAME)
    @Column(name = "user_name")
    private String username;

    @YangAttribute(name = DeviceManagerNSConstants.SECURITY_LEVEL)
    @Column(name = "security_level")
    private String securityLevel;

    @YangAttribute(name = DeviceManagerNSConstants.AUTH_PROTOCOL)
    @Column(name = "auth_protocol")
    private String authProtocol;

    @YangAttribute(name = DeviceManagerNSConstants.AUTH_PASSWORD)
    @Column(name = "auth_password")
    private String authPassword;

    @YangAttribute(name = DeviceManagerNSConstants.PRIV_PROTOCOL)
    @Column(name = "priv_protocol")
    private String privProtocol;

    @YangAttribute(name = DeviceManagerNSConstants.PRIV_PASSWORD)
    @Column(name = "priv_password")
    private String privPassword;

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getSchemaPath() {
        return schemaPath;
    }

    public void setSchemaPath(String schemaPath) {
        this.schemaPath = schemaPath;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSecurityLevel() {
        return securityLevel;
    }

    public void setSecurityLevel(String securityLevel) {
        this.securityLevel = securityLevel;
    }

    public String getAuthProtocol() {
        return authProtocol;
    }

    public void setAuthProtocol(String authProtocol) {
        this.authProtocol = authProtocol;
    }

    public String getAuthPassword() {
        return authPassword;
    }

    public void setAuthPassword(String authPassword) {
        this.authPassword = authPassword;
    }

    public String getPrivProtocol() {
        return privProtocol;
    }

    public void setPrivProtocol(String privProtocol) {
        this.privProtocol = privProtocol;
    }

    public String getPrivPassword() {
        return privPassword;
    }

    public void setPrivPassword(String privPassword) {
        this.privPassword = privPassword;
    }
}
