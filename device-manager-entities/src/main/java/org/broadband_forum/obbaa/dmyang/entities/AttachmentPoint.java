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

import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.CHANNEL_PARTITION;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.EXPECTED_ATTACHMENT_POINT;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.OLT_NAME;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.ONU_ID;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.ONU_MANAGEMENT_NS;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.ONU_MANAGEMENT_REVISION;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangAttribute;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangContainer;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;

@Entity
@YangContainer(name = EXPECTED_ATTACHMENT_POINT, namespace = ONU_MANAGEMENT_NS, revision = ONU_MANAGEMENT_REVISION)
public class AttachmentPoint {
    @Id
    @YangParentId
    @Column(name = YangParentId.PARENT_ID_FIELD_NAME)
    private String parentId;

    @YangSchemaPath
    @Column(length = 1000)
    private String schemaPath;

    @YangAttribute(name = OLT_NAME)
    @Column(name = "oltName")
    private String oltName;

    @YangAttribute(name = CHANNEL_PARTITION)
    @Column(name = "channelPartition")
    private String channelPartition;

    @YangAttribute(name = ONU_ID)
    @Column(name = "onuId")
    private String onuId;

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

    public String getOnuId() {
        return onuId;
    }

    public void setOnuId(String onuId) {
        this.onuId = onuId;
    }

    public String getChannelPartition() {
        return channelPartition;
    }

    public void setChannelPartition(String channelPartition) {
        this.channelPartition = channelPartition;
    }

    public String getOltName() {
        return oltName;
    }

    public void setOltName(String oltName) {
        this.oltName = oltName;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        AttachmentPoint that = (AttachmentPoint) other;

        if (parentId != null ? !parentId.equals(that.parentId) : that.parentId != null) {
            return false;
        }
        if (schemaPath != null ? !schemaPath.equals(that.schemaPath) : that.schemaPath != null) {
            return false;
        }
        if (oltName != null ? !oltName.equals(that.oltName) : that.oltName != null) {
            return false;
        }
        if (channelPartition != null ? !channelPartition.equals(that.channelPartition) : that.channelPartition != null) {
            return false;
        }
        return onuId != null ? onuId.equals(that.onuId) : that.onuId == null;

    }

    @Override
    public int hashCode() {
        int result = parentId != null ? parentId.hashCode() : 0;
        result = 31 * result + (schemaPath != null ? schemaPath.hashCode() : 0);
        result = 31 * result + (oltName != null ? oltName.hashCode() : 0);
        result = 31 * result + (channelPartition != null ? channelPartition.hashCode() : 0);
        result = 31 * result + (onuId != null ? onuId.hashCode() : 0);
        return result;
    }
}
