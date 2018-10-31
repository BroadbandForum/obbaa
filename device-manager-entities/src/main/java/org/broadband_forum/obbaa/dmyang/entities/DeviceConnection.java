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


import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.CONNECTION_MODEL;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.DEVICE_CONNECTION;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.DUID;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.NS;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangAttribute;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangChild;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangContainer;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;

@Entity
@YangContainer(name = DEVICE_CONNECTION, namespace = NS)
public class DeviceConnection {
    @Id
    @YangParentId
    @Column(name = YangParentId.PARENT_ID_FIELD_NAME)
    private String parentId;

    @YangSchemaPath
    @Column(length = 1000)
    private String schemaPath;

    @YangAttribute(name = CONNECTION_MODEL)
    @Column(name = "connection_model")
    private String connectionModel;

    @YangChild
    @OneToOne(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER, orphanRemoval = true)
    private PasswordAuth passwordAuth;

    @YangAttribute(name = DUID)
    @Column(name = "duid")
    private String duid;


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

    public String getConnectionModel() {
        return connectionModel;
    }

    public void setConnectionModel(String connectionModel) {
        this.connectionModel = connectionModel;
    }

    public PasswordAuth getPasswordAuth() {
        return passwordAuth;
    }

    public void setPasswordAuth(PasswordAuth passwordAuth) {
        this.passwordAuth = passwordAuth;
    }

    public String getDuid() {
        return duid;
    }

    public void setDuid(String duid) {
        this.duid = duid;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        DeviceConnection that = (DeviceConnection) other;

        if (parentId != null ? !parentId.equals(that.parentId) : that.parentId != null) {
            return false;
        }
        if (schemaPath != null ? !schemaPath.equals(that.schemaPath) : that.schemaPath != null) {
            return false;
        }
        if (connectionModel != null ? !connectionModel.equals(that.connectionModel) : that.connectionModel != null) {
            return false;
        }
        if (passwordAuth != null ? !passwordAuth.equals(that.passwordAuth) : that.passwordAuth != null) {
            return false;
        }
        return duid != null ? duid.equals(that.duid) : that.duid == null;

    }

    @Override
    public int hashCode() {
        int result = parentId != null ? parentId.hashCode() : 0;
        result = 31 * result + (schemaPath != null ? schemaPath.hashCode() : 0);
        result = 31 * result + (connectionModel != null ? connectionModel.hashCode() : 0);
        result = 31 * result + (passwordAuth != null ? passwordAuth.hashCode() : 0);
        result = 31 * result + (duid != null ? duid.hashCode() : 0);
        return result;
    }
}
