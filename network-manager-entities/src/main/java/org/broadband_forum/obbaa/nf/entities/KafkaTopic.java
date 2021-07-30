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

import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangAttribute;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangList;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangListKey;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;

/**
 * <p>
 * Entity class for Kafka Topic
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 10/05/2021.
 */
@Entity
@IdClass(KafkaTopicPK.class)
@YangList(name = NetworkFunctionNSConstants.KAFKA_TOPIC, namespace = NetworkFunctionNSConstants.NS,
        revision = NetworkFunctionNSConstants.REVISION)
public class KafkaTopic implements Comparable<KafkaTopic> {
    @Id
    @YangParentId
    @Column(name = YangParentId.PARENT_ID_FIELD_NAME, length = 1000)
    private String parentId;

    @YangSchemaPath
    @Column(length = 1000)
    private String schemaPath;

    @Id
    @YangListKey(name = NetworkFunctionNSConstants.TOPIC_NAME)
    @Column(name = "name")
    private String topicName;

    @YangAttribute(name = NetworkFunctionNSConstants.PURPOSE)
    @Column(name = "purpose")
    private String purpose;

    @YangAttribute(name = NetworkFunctionNSConstants.PARTITION)
    @Column(name = "partition")
    private String partition;

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

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getPartition() {
        return partition;
    }

    public void setPartition(String partition) {
        this.partition = partition;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        KafkaTopic kafkaTopic = (KafkaTopic) other;

        if (parentId != null ? !parentId.equals(kafkaTopic.parentId) : kafkaTopic.parentId != null) {
            return false;
        }
        if (schemaPath != null ? !schemaPath.equals(kafkaTopic.schemaPath) : kafkaTopic.schemaPath != null) {
            return false;
        }
        if (topicName != null ? !topicName.equals(kafkaTopic.topicName)
                : kafkaTopic.topicName != null) {
            return false;
        }
        if (purpose != null ? !purpose.equals(kafkaTopic.purpose)
                : kafkaTopic.purpose != null) {
            return false;
        }
        return partition != null ? partition.equals(kafkaTopic.partition)
                : kafkaTopic.partition == null;

    }

    @Override
    public int hashCode() {
        int result = parentId != null ? parentId.hashCode() : 0;
        result = 31 * result + (schemaPath != null ? schemaPath.hashCode() : 0);
        result = 31 * result + (topicName != null ? topicName.hashCode() : 0);
        result = 31 * result + (purpose != null ? purpose.hashCode() : 0);
        result = 31 * result + (partition != null ? partition.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("KafkaTopic{");
        sb.append("parentId='").append(parentId).append('\'');
        sb.append(", schemaPath='").append(schemaPath).append('\'');
        sb.append(", topicName='").append(topicName).append('\'');
        sb.append(", purpose='").append(purpose).append('\'');
        sb.append(", partition='").append(partition).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int compareTo(KafkaTopic other) {
        return topicName.compareTo(other.topicName);
    }
}
