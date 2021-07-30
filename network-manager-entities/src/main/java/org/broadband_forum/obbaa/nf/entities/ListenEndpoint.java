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
import javax.persistence.IdClass;
import javax.persistence.OneToOne;

import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangChild;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangList;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangListKey;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;

/**
 * <p>
 * Entity class for Listen Endpoint
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 10/05/2021.
 */
@Entity
@IdClass(ListenEndpointPK.class)
@YangList(name = NetworkFunctionNSConstants.LISTEN_ENDPOINT, namespace = NetworkFunctionNSConstants.NS,
        revision = NetworkFunctionNSConstants.REVISION)
public class ListenEndpoint implements Comparable<ListenEndpoint> {
    @Id
    @YangParentId
    @Column(name = YangParentId.PARENT_ID_FIELD_NAME)
    private String parentId;

    @YangSchemaPath
    @Column(length = 1000)
    private String schemaPath;

    @Id
    @YangListKey(name = NetworkFunctionNSConstants.LISTEN_ENDPOINT_NAME)
    @Column(name = "name")
    private String listenEndpointName;

    @YangChild
    @OneToOne(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER, orphanRemoval = true)
    private GrpcServer grpcServer;

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

    public String getListenEndpointName() {
        return listenEndpointName;
    }

    public void setListenEndpointName(String listenEndpointName) {
        this.listenEndpointName = listenEndpointName;
    }

    public GrpcServer getGrpcServer() {
        return grpcServer;
    }

    public void setGrpcServer(GrpcServer grpcServer) {
        this.grpcServer = grpcServer;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        ListenEndpoint that = (ListenEndpoint) other;

        if (parentId != null ? !parentId.equals(that.parentId) : that.parentId != null) {
            return false;
        }
        if (listenEndpointName != null ? !listenEndpointName.equals(that.listenEndpointName) : that.listenEndpointName != null) {
            return false;
        }
        if (grpcServer != null ? !grpcServer.equals(that.grpcServer) : that.grpcServer != null) {
            return false;
        }
        return schemaPath != null ? schemaPath.equals(that.schemaPath) : that.schemaPath == null;

    }

    @Override
    public int hashCode() {
        int result = parentId != null ? parentId.hashCode() : 0;
        result = 31 * result + (schemaPath != null ? schemaPath.hashCode() : 0);
        result = 31 * result + (listenEndpointName != null ? listenEndpointName.hashCode() : 0);
        result = 31 * result + (grpcServer != null ? grpcServer.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(ListenEndpoint other) {
        return listenEndpointName.compareTo(other.getListenEndpointName());
    }

}
