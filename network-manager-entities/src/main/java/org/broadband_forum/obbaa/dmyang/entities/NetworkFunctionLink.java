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

package org.broadband_forum.obbaa.dmyang.entities;

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

@Entity
@IdClass(NetworkFunctionLinkPK.class)
@YangList(name = DeviceManagerNSConstants.NETWORK_FUNCTION_LINK, namespace = DeviceManagerNSConstants.ONU_MANAGEMENT_NS,
        revision = DeviceManagerNSConstants.ONU_MANAGEMENT_REVISION)
public class NetworkFunctionLink implements Comparable<NetworkFunctionLink> {
    @Id
    @YangParentId
    @Column(name = YangParentId.PARENT_ID_FIELD_NAME)
    String parentId;

    @YangSchemaPath
    @Column(length = 1000)
    private String schemaPath;

    @Id
    @YangListKey(name = DeviceManagerNSConstants.NETWORK_FUNCTION_LINK_NAME)
    @Column(name = "name")
    private String name;

    @YangChild
    @OneToOne(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER, orphanRemoval = true)
    private TerminationPointA terminationPointA;

    @YangChild
    @OneToOne(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER, orphanRemoval = true)
    private TerminationPointB terminationPointB;

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public int compareTo(NetworkFunctionLink networkFunctionLink) {
        return name.compareTo(networkFunctionLink.getName());
    }

    public TerminationPointA getTerminationPointA() {
        return terminationPointA;
    }

    public void setTerminationPointA(TerminationPointA terminationPointA) {
        this.terminationPointA = terminationPointA;
    }

    public TerminationPointB getTerminationPointB() {
        return terminationPointB;
    }

    public void setTerminationPointB(TerminationPointB terminationPointB) {
        this.terminationPointB = terminationPointB;
    }

    public String getSchemaPath() {
        return schemaPath;
    }

    public void setSchemaPath(String schemaPath) {
        this.schemaPath = schemaPath;
    }
}
