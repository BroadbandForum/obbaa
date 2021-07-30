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
 * Entity class for GRPC Connection Backoff
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 10/05/2021.
 */
@Entity
@YangContainer(name = NetworkFunctionNSConstants.GRPC_CONNECTION_BACKOFF, namespace = NetworkFunctionNSConstants.NS,
        revision = NetworkFunctionNSConstants.REVISION)
public class GrpcConnectionBackoff {
    @Id
    @YangParentId
    @Column(name = YangParentId.PARENT_ID_FIELD_NAME, length = 1000)
    private String parentId;

    @YangSchemaPath
    @Column(length = 1000)
    private String schemaPath;

    @YangAttribute(name = NetworkFunctionNSConstants.INITIAL_BACKOFF)
    @Column(name = "initial_backoff")
    private String initialBackoff;

    @YangAttribute(name = NetworkFunctionNSConstants.MIN_CONNECT_TIMEOUT)
    @Column(name = "min_connect_timeout")
    private String minConnectTimeout;

    @YangAttribute(name = NetworkFunctionNSConstants.JITTER)
    @Column(name = "jitter")
    private String jitter;

    @YangAttribute(name = NetworkFunctionNSConstants.MULTIPLIER)
    @Column(name = "multiplier")
    private String multiplier;

    @YangAttribute(name = NetworkFunctionNSConstants.MAX_BACKOFF)
    @Column(name = "max_backoff")
    private String maxBackoff;

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

    public String getInitialBackoff() {
        return initialBackoff;
    }

    public void setInitialBackoff(String initialBackoff) {
        this.initialBackoff = initialBackoff;
    }

    public String getMinConnectTimeout() {
        return minConnectTimeout;
    }

    public void setMinConnectTimeout(String minConnectTimeout) {
        this.minConnectTimeout = minConnectTimeout;
    }

    public String getJitter() {
        return jitter;
    }

    public void setJitter(String jitter) {
        this.jitter = jitter;
    }

    public String getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(String multiplier) {
        this.multiplier = multiplier;
    }

    public String getMaxBackoff() {
        return maxBackoff;
    }

    public void setMaxBackoff(String maxBackoff) {
        this.maxBackoff = maxBackoff;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        GrpcConnectionBackoff that = (GrpcConnectionBackoff) other;

        if (parentId != null ? !parentId.equals(that.parentId) : that.parentId != null) {
            return false;
        }
        if (initialBackoff != null ? !initialBackoff.equals(that.initialBackoff) : that.initialBackoff != null) {
            return false;
        }
        if (jitter != null ? !jitter.equals(that.jitter) : that.jitter != null) {
            return false;
        }
        if (multiplier != null ? !multiplier.equals(that.multiplier) : that.multiplier != null) {
            return false;
        }
        if (maxBackoff != null ? !maxBackoff.equals(that.maxBackoff) : that.maxBackoff != null) {
            return false;
        }
        if (minConnectTimeout != null ? !minConnectTimeout.equals(that.minConnectTimeout) : that.minConnectTimeout != null) {
            return false;
        }
        return schemaPath != null ? schemaPath.equals(that.schemaPath) : that.schemaPath == null;

    }

    @Override
    public int hashCode() {
        int result = parentId != null ? parentId.hashCode() : 0;
        result = 31 * result + (schemaPath != null ? schemaPath.hashCode() : 0);
        result = 31 * result + (maxBackoff != null ? maxBackoff.hashCode() : 0);
        result = 31 * result + (minConnectTimeout != null ? minConnectTimeout.hashCode() : 0);
        result = 31 * result + (initialBackoff != null ? initialBackoff.hashCode() : 0);
        result = 31 * result + (jitter != null ? jitter.hashCode() : 0);
        result = 31 * result + (multiplier != null ? multiplier.hashCode() : 0);
        return result;
    }
}
