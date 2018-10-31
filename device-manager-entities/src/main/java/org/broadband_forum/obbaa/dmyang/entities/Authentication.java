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

import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.ADDRESS;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.AUTHENTICATION;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.MANAGEMENT_PORT;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.NS;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.PASSWORD;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.USER_NAME;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangAttribute;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangContainer;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;

@Entity
@YangContainer(name = AUTHENTICATION, namespace = NS)
public class Authentication {
    @Id
    @YangParentId
    @Column(name = YangParentId.PARENT_ID_FIELD_NAME)
    private String parentId;

    @YangSchemaPath
    @Column(length = 1000)
    private String schemaPath;

    @YangAttribute(name = ADDRESS)
    @Column(name = "address")
    private String address;

    @YangAttribute(name = MANAGEMENT_PORT)
    @Column(name = "management_port")
    private String managementPort;

    @YangAttribute(name = USER_NAME)
    @Column(name = "user_name")
    private String username;

    @YangAttribute(name = PASSWORD)
    @Column(name = "password")
    private String password;

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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getManagementPort() {
        return managementPort;
    }

    public void setManagementPort(String managementPort) {
        this.managementPort = managementPort;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
