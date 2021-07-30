/*
 * Copyright 2021 Broadband Forum
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


package org.broadband_forum.obbaa.nf.entities;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangChild;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangContainer;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentSchemaPath;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * <p>
 * Entity class for Network Functions Settings
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 10/05/2021.
 */
@Entity
@YangContainer(name = NetworkFunctionNSConstants.NETWORK_FUNCTIONS_SETTINGS, namespace = NetworkFunctionNSConstants.NS,
        revision = NetworkFunctionNSConstants.REVISION)
public class NetworkFunctionsSettings {
    @Id
    @YangParentId
    @Column(name = YangParentId.PARENT_ID_FIELD_NAME)
    private String parentId;

    @YangSchemaPath
    @Column(length = 1000)
    private String schemaPath;

    @YangChild
    @OneToOne(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER, orphanRemoval = true)
    private NFClient nfClient;

    @YangChild
    @OneToOne(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER, orphanRemoval = true)
    private NFServer nfServer;

    @YangParentSchemaPath
    public static final SchemaPath getParentSchemaPath() {
        return NetworkFunctionNSConstants.NETWORK_MANAGER_SP;
    }

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

    public NFClient getNfClient() {
        return nfClient;
    }

    public void setNfClient(NFClient nfClient) {
        this.nfClient = nfClient;
    }

    public NFServer getNfServer() {
        return nfServer;
    }

    public void setNfServer(NFServer nfServer) {
        this.nfServer = nfServer;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        NetworkFunctionsSettings that = (NetworkFunctionsSettings) other;

        if (parentId != null ? !parentId.equals(that.parentId) : that.parentId != null) {
            return false;
        }
        if (nfClient != null ? !nfClient.equals(that.nfClient) : that.nfClient != null) {
            return false;
        }
        if (nfServer != null ? !nfServer.equals(that.nfServer) : that.nfServer != null) {
            return false;
        }
        return schemaPath != null ? schemaPath.equals(that.schemaPath) : that.schemaPath == null;

    }

    @Override
    public int hashCode() {
        int result = parentId != null ? parentId.hashCode() : 0;
        result = 31 * result + (schemaPath != null ? schemaPath.hashCode() : 0);
        result = 31 * result + (nfClient != null ? nfClient.hashCode() : 0);
        result = 31 * result + (nfServer != null ? nfServer.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NetworkFunctionsSettings{");
        sb.append("parentId='").append(parentId).append('\'');
        sb.append(", schemaPath='").append(schemaPath).append('\'');
        sb.append(", nfClient='").append(nfClient).append('\'');
        sb.append(", nfServer='").append(nfServer).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
