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

@Entity
public class ActualAttachmentPoint {
    @Id
    @Column(name = "attachment_point_id")
    private String attachmentPointId;

    @Column(name = "oltName")
    private String oltName;

    @Column(name = "channelTerminationRef")
    private String channelTerminationRef;

    @Column(name = "onuId")
    private String onuId;

    public String getAttachmentPointId() {
        return attachmentPointId;
    }

    public void setAttachmentPointId(String attachmentPointId) {
        this.attachmentPointId = attachmentPointId;
    }

    public String getOnuId() {
        return onuId;
    }

    public void setOnuId(String onuId) {
        this.onuId = onuId;
    }

    public String getChannelTerminationRef() {
        return channelTerminationRef;
    }

    public void setChannelTerminationRef(String channelTerminationRef) {
        this.channelTerminationRef = channelTerminationRef;
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
        ActualAttachmentPoint that = (ActualAttachmentPoint) other;
        if (attachmentPointId != null ? !attachmentPointId.equals(that.attachmentPointId) : that.attachmentPointId != null) {
            return false;
        }
        if (oltName != null ? !oltName.equals(that.oltName) : that.oltName != null) {
            return false;
        }
        if (channelTerminationRef != null ? !channelTerminationRef.equals(that.channelTerminationRef)
                : that.channelTerminationRef != null) {
            return false;
        }
        return onuId != null ? onuId.equals(that.onuId) : that.onuId == null;

    }

    @Override
    public int hashCode() {
        int result = attachmentPointId != null ? attachmentPointId.hashCode() : 0;
        result = 31 * result + (oltName != null ? oltName.hashCode() : 0);
        result = 31 * result + (channelTerminationRef != null ? channelTerminationRef.hashCode() : 0);
        result = 31 * result + (onuId != null ? onuId.hashCode() : 0);
        return result;
    }
}
