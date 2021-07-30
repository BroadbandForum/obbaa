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
import javax.persistence.IdClass;

import org.broadband_forum.obbaa.netconf.stack.api.annotations.AttributeType;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangAttribute;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangAttributeNS;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangList;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangListKey;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;

/**
 * <p>
 * Entity class for Network Function
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 10/05/2021.
 */
@Entity
@IdClass(NetworkFunctionPK.class)
@YangList(name = NetworkFunctionNSConstants.NETWORK_FUNCTION, namespace = NetworkFunctionNSConstants.NS,
        revision = NetworkFunctionNSConstants.REVISION)
public class NetworkFunction implements Comparable<NetworkFunction> {
    @Id
    @YangParentId
    @Column(name = YangParentId.PARENT_ID_FIELD_NAME)
    private String parentId;

    @YangSchemaPath
    @Column(length = 1000)
    private String schemaPath;

    @Id
    @YangListKey(name = NetworkFunctionNSConstants.NETWORK_FUNCTION_NAME)
    @Column(name = "name")
    private String networkFunctionName;

    @YangAttribute(name = NetworkFunctionNSConstants.NETWORK_FUNCTION_TYPE, namespace = NetworkFunctionNSConstants.NS,
            revision = NetworkFunctionNSConstants.REVISION, attributeType = AttributeType.IDENTITY_REF_CONFIG_ATTRIBUTE)
    @Column(name = "type")
    private String type;

    @YangAttributeNS(belongsToAttribute = NetworkFunctionNSConstants.NETWORK_FUNCTION_TYPE,
            attributeNamespace = NetworkFunctionNSConstants.NS, attributeRevision = NetworkFunctionNSConstants.REVISION)
    @Column(name = "typeNS")
    private String typeNS;

    @YangAttribute(name = NetworkFunctionNSConstants.REMOTE_ENDPOINT_NAME)
    @Column(name = "remote_endpoint_name")
    private String remoteEndpointName;

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

    public String getNetworkFunctionName() {
        return networkFunctionName;
    }

    public void setNetworkFunctionName(String networkFunctionName) {
        this.networkFunctionName = networkFunctionName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRemoteEndpointName() {
        return remoteEndpointName;
    }

    public void setRemoteEndpointName(String remoteEndPointName) {
        this.remoteEndpointName = remoteEndPointName;
    }

    public String getTypeNS() {
        return typeNS;
    }

    public void setTypeNS(String typeNS) {
        this.typeNS = typeNS;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        NetworkFunction networkFunction = (NetworkFunction) other;

        if (parentId != null ? !parentId.equals(networkFunction.parentId) : networkFunction.parentId != null) {
            return false;
        }
        if (schemaPath != null ? !schemaPath.equals(networkFunction.schemaPath) : networkFunction.schemaPath != null) {
            return false;
        }
        if (type != null ? !type.equals(networkFunction.type)
                : networkFunction.type != null) {
            return false;
        }
        if (remoteEndpointName != null ? !remoteEndpointName.equals(networkFunction.remoteEndpointName)
                : networkFunction.remoteEndpointName != null) {
            return false;
        }
        return networkFunctionName != null ? networkFunctionName.equals(networkFunction.networkFunctionName)
                : networkFunction.networkFunctionName == null;

    }

    @Override
    public int hashCode() {
        int result = parentId != null ? parentId.hashCode() : 0;
        result = 31 * result + (schemaPath != null ? schemaPath.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (remoteEndpointName != null ? remoteEndpointName.hashCode() : 0);
        result = 31 * result + (networkFunctionName != null ? networkFunctionName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NetworkFunction{");
        sb.append("parentId='").append(parentId).append('\'');
        sb.append(", schemaPath='").append(schemaPath).append('\'');
        sb.append(", networkFunctionName='").append(networkFunctionName).append('\'');
        sb.append(", networkFunctionType='").append(type).append('\'');
        sb.append(", remoteEndPoint='").append(remoteEndpointName).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int compareTo(NetworkFunction other) {
        return networkFunctionName.compareTo(other.getNetworkFunctionName());
    }

}
