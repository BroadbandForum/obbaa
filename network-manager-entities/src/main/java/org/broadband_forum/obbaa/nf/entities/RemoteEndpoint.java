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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.broadband_forum.obbaa.netconf.stack.api.annotations.AttributeType;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangAttribute;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangAttributeNS;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangChild;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangList;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangListKey;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;

/**
 * <p>
 * Entity class for Remote Endpoint
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 10/05/2021.
 */
@Entity
@IdClass(RemoteEndpointPK.class)
@YangList(name = NetworkFunctionNSConstants.REMOTE_ENDPOINT, namespace = NetworkFunctionNSConstants.NS,
        revision = NetworkFunctionNSConstants.REVISION)
public class RemoteEndpoint implements Comparable<RemoteEndpoint> {
    @Id
    @YangParentId
    @Column(name = YangParentId.PARENT_ID_FIELD_NAME, length = 1000)
    private String parentId;

    @YangSchemaPath
    @Column(length = 1000)
    private String schemaPath;

    @Id
    @YangListKey(name = NetworkFunctionNSConstants.REMOTE_ENDPOINT_FUNCTION_NAME)
    @Column(name = "name")
    private String remoteEndpointName;

    @YangAttribute(name = NetworkFunctionNSConstants.REMOTE_ENDPOINT_TYPE, namespace = NetworkFunctionNSConstants.NS,
            revision = NetworkFunctionNSConstants.REVISION, attributeType = AttributeType.IDENTITY_REF_CONFIG_ATTRIBUTE)
    @Column(name = "type")
    private String type;

    @YangAttributeNS(belongsToAttribute = NetworkFunctionNSConstants.REMOTE_ENDPOINT_TYPE,
            attributeNamespace = NetworkFunctionNSConstants.NS, attributeRevision = NetworkFunctionNSConstants.REVISION)
    @Column(name = "typeNS")
    private String typeNS;

    @YangAttribute(name = NetworkFunctionNSConstants.LOCAL_ENDPOINT_NAME)
    @Column(name = "local_endpoint_name")
    private String localEndpointName;

    @YangChild
    @OneToOne(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER, orphanRemoval = true)
    private KafkaAgent kafkaAgent;

    @YangChild
    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<AccessPoint> accessPoints = new HashSet<>();

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

    public String getRemoteEndpointName() {
        return remoteEndpointName;
    }

    public void setRemoteEndpointName(String remoteEndpointName) {
        this.remoteEndpointName = remoteEndpointName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public KafkaAgent getKafkaAgent() {
        return kafkaAgent;
    }

    public void setKafkaAgent(KafkaAgent kafkaAgent) {
        this.kafkaAgent = kafkaAgent;
    }

    public Set<AccessPoint> getAccessPoints() {
        return accessPoints;
    }

    public void setAccessPoints(Set<AccessPoint> accessPoints) {
        this.accessPoints = accessPoints;
    }

    public String getTypeNS() {
        return typeNS;
    }

    public void setTypeNS(String typeNS) {
        this.typeNS = typeNS;
    }

    public String getLocalEndpointName() {
        return localEndpointName;
    }

    public void setLocalEndpointName(String localEndpointName) {
        this.localEndpointName = localEndpointName;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        RemoteEndpoint remoteEndpoint = (RemoteEndpoint) other;

        if (parentId != null ? !parentId.equals(remoteEndpoint.parentId) : remoteEndpoint.parentId != null) {
            return false;
        }
        if (schemaPath != null ? !schemaPath.equals(remoteEndpoint.schemaPath) : remoteEndpoint.schemaPath != null) {
            return false;
        }
        if (type != null ? !type.equals(remoteEndpoint.type)
                : remoteEndpoint.type != null) {
            return false;
        }
        if (localEndpointName != null ? !localEndpointName.equals(remoteEndpoint.localEndpointName)
                : remoteEndpoint.localEndpointName != null) {
            return false;
        }
        if (kafkaAgent != null ? !kafkaAgent.equals(remoteEndpoint.kafkaAgent)
                : remoteEndpoint.kafkaAgent != null) {
            return false;
        }
        if (accessPoints != null ? !accessPoints.equals(remoteEndpoint.accessPoints)
                : remoteEndpoint.accessPoints != null) {
            return false;
        }
        return remoteEndpointName != null ? remoteEndpointName.equals(remoteEndpoint.remoteEndpointName)
                : remoteEndpoint.remoteEndpointName == null;

    }

    @Override
    public int hashCode() {
        int result = parentId != null ? parentId.hashCode() : 0;
        result = 31 * result + (schemaPath != null ? schemaPath.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (localEndpointName != null ? localEndpointName.hashCode() : 0);
        result = 31 * result + (remoteEndpointName != null ? remoteEndpointName.hashCode() : 0);
        result = 31 * result + (kafkaAgent != null ? kafkaAgent.hashCode() : 0);
        result = 31 * result + (accessPoints != null ? accessPoints.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RemoteEndpoint{");
        sb.append("parentId='").append(parentId).append('\'');
        sb.append(", schemaPath='").append(schemaPath).append('\'');
        sb.append(", networkFunctionType='").append(type).append('\'');
        sb.append(", localEndPointName='").append(localEndpointName).append('\'');
        sb.append(", remoteEndPointName='").append(remoteEndpointName).append('\'');
        sb.append(", kafkaAgent='").append(kafkaAgent).append('\'');
        sb.append(", accessPoint='").append(accessPoints).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int compareTo(RemoteEndpoint other) {
        return remoteEndpointName.compareTo(other.remoteEndpointName);
    }

}
