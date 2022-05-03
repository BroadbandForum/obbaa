/*
 * Copyright 2020 Broadband Forum
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
import javax.persistence.IdClass;

import org.broadband_forum.obbaa.netconf.stack.api.annotations.AttributeType;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangAttribute;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangAttributeNS;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangList;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;

@Entity
@IdClass(ExpectedAttachmentPointPK.class)
@YangList(name = DeviceManagerNSConstants.EXPECTED_ATTACHMENT_POINT, namespace = DeviceManagerNSConstants.ONU_MANAGEMENT_NS,
        revision = DeviceManagerNSConstants.ONU_MANAGEMENT_REVISION)
public class ExpectedAttachmentPoint implements Comparable<ExpectedAttachmentPoint> {
    @Id
    @YangParentId
    @Column(name = YangParentId.PARENT_ID_FIELD_NAME)
    private String parentId;

    @YangSchemaPath
    @Column(length = 1000)
    private String schemaPath;


    @Id
    @YangAttribute(name = DeviceManagerNSConstants.NAME)
    @Column(name = "name")
    private String name;

    @YangAttribute(name = DeviceManagerNSConstants.OLT_NAME)
    @Column(name = "oltName")
    private String oltName;


    @YangAttribute(name = DeviceManagerNSConstants.CHANNEL_PARTITION_NAME)
    @Column(name = "channelPartitionName")
    private String channelPartitionName;

    @YangAttribute(name = DeviceManagerNSConstants.PLANNED_ONU_MANAGEMENT_MODE_IN_THIS_OLT,
            attributeType = AttributeType.IDENTITY_REF_CONFIG_ATTRIBUTE)
    @Column(name = "plannedOnuManagementModeInThisOlt")
    private String plannedOnuManagementModeInThisOlt;

    @YangAttributeNS(belongsToAttribute = DeviceManagerNSConstants.PLANNED_ONU_MANAGEMENT_MODE_IN_THIS_OLT,
            attributeNamespace = DeviceManagerNSConstants.ONU_MANAGEMENT_NS,
            attributeRevision = DeviceManagerNSConstants.ONU_MANAGEMENT_REVISION)
    @Column(name = "plannedOnuManagementModeInThisOlt_ns")
    private String plannedOnuManagementModeInThisOltNs;

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

    public String getChannelPartitionName() {
        return channelPartitionName;
    }

    public void setChannelPartitionName(String channelPartition) {
        this.channelPartitionName = channelPartition;
    }

    public String getOltName() {
        return oltName;
    }

    public void setOltName(String oltName) {
        this.oltName = oltName;
    }

    public String getPlannedOnuManagementModeInThisOlt() {
        return plannedOnuManagementModeInThisOlt;
    }

    public void setPlannedOnuManagementModeInThisOlt(String plannedOnuManagementModeInThisOlt) {
        this.plannedOnuManagementModeInThisOlt = plannedOnuManagementModeInThisOlt;
    }

    public String getPlannedOnuManagementModeInThisOltNs() {
        return plannedOnuManagementModeInThisOltNs;
    }

    public void setPlannedOnuManagementModeInThisOltNs(String plannedOnuManagementModeInThisOltNs) {
        this.plannedOnuManagementModeInThisOltNs = plannedOnuManagementModeInThisOltNs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        ExpectedAttachmentPoint that = (ExpectedAttachmentPoint) other;

        if (parentId != null ? !parentId.equals(that.parentId) : that.parentId != null) {
            return false;
        }
        if (schemaPath != null ? !schemaPath.equals(that.schemaPath) : that.schemaPath != null) {
            return false;
        }
        if (oltName != null ? !oltName.equals(that.oltName) : that.oltName != null) {
            return false;
        }
        if (plannedOnuManagementModeInThisOlt != null ? !plannedOnuManagementModeInThisOlt.equals(that.plannedOnuManagementModeInThisOlt)
                : that.plannedOnuManagementModeInThisOlt != null) {
            return false;
        }
        return (channelPartitionName != null ? !channelPartitionName.equals(that.channelPartitionName) : that.channelPartitionName != null);

    }

    @Override
    public int hashCode() {
        int result = parentId != null ? parentId.hashCode() : 0;
        result = 31 * result + (schemaPath != null ? schemaPath.hashCode() : 0);
        result = 31 * result + (oltName != null ? oltName.hashCode() : 0);
        result = 31 * result + (channelPartitionName != null ? channelPartitionName.hashCode() : 0);
        result = 31 * result + (plannedOnuManagementModeInThisOlt != null ? plannedOnuManagementModeInThisOlt.hashCode() : 0);
        result = 31 * result + (plannedOnuManagementModeInThisOltNs != null ? plannedOnuManagementModeInThisOltNs.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(ExpectedAttachmentPoint expectedAttachmentPoint) {
        return oltName.compareTo(expectedAttachmentPoint.getOltName());
    }
}