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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangAttribute;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangContainer;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;

/**
 * <p>
 * Entity class for GRPC Server
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 10/05/2021.
 */
@Entity
@YangContainer(name = NetworkFunctionNSConstants.GRPC_SERVER_PARAMETERS, namespace = NetworkFunctionNSConstants.NS,
        revision = NetworkFunctionNSConstants.REVISION)
public class GrpcServerParameters {
    @Id
    @YangParentId
    @Column(name = YangParentId.PARENT_ID_FIELD_NAME, length = 1000)
    private String parentId;

    @YangSchemaPath
    @Column(length = 1000)
    private String schemaPath;

    @YangAttribute(name = NetworkFunctionNSConstants.LOCAL_ENDPOINT_NAME)
    @Column(name = "local_endpoint_name")
    private String localEndpointName;

    @YangAttribute(name = NetworkFunctionNSConstants.LOCAL_ADDRESS)
    @Column(name = "local_address")
    private String localAddress;

    @YangAttribute(name = NetworkFunctionNSConstants.LOCAL_PORT)
    @Column(name = "local_port")
    private Long localPort;

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

    public String getLocalEndpointName() {
        return localEndpointName;
    }

    public void setLocalEndpointName(String localEndpointName) {
        this.localEndpointName = localEndpointName;
    }

    public String getLocalAddress() {
        return localAddress;
    }

    public void setLocalAddress(String localAddress) {
        this.localAddress = localAddress;
    }

    public Long getLocalPort() {
        return localPort;
    }

    public void setLocalPort(Long localPort) {
        this.localPort = localPort;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        GrpcServerParameters that = (GrpcServerParameters) other;

        if (parentId != null ? !parentId.equals(that.parentId) : that.parentId != null) {
            return false;
        }
        if (localEndpointName != null ? !localEndpointName.equals(that.localEndpointName) : that.localEndpointName != null) {
            return false;
        }
        if (localAddress != null ? !localAddress.equals(that.localAddress) : that.localAddress != null) {
            return false;
        }
        if (localPort != null ? !localPort.equals(that.localPort) : that.localPort != null) {
            return false;
        }
        return schemaPath != null ? schemaPath.equals(that.schemaPath) : that.schemaPath == null;

    }

    @Override
    public int hashCode() {
        int result = parentId != null ? parentId.hashCode() : 0;
        result = 31 * result + (schemaPath != null ? schemaPath.hashCode() : 0);
        result = 31 * result + (localEndpointName != null ? localEndpointName.hashCode() : 0);
        result = 31 * result + (localAddress != null ? localAddress.hashCode() : 0);
        result = 31 * result + (localPort != null ? localPort.hashCode() : 0);
        return result;
    }

}
