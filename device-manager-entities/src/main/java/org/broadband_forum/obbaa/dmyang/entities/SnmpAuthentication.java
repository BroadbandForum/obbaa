/*
 * Copyright 2018 Broadband Forum
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

import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.ADDRESS;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.AGENT_PORT;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.COMMUNITY_STRING;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.NS;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.SNMP_AUTHENTICATION;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.SNMP_VERSION;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.TRAP_PORT;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangAttribute;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangChild;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangContainer;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;

@Entity
@YangContainer(name = SNMP_AUTHENTICATION, namespace = NS)
public class SnmpAuthentication {
    @Id
    @YangParentId
    @Column(name = YangParentId.PARENT_ID_FIELD_NAME)
    private String parentId;

    @YangSchemaPath
    @Column(length = 1000)
    private String schemaPath;

    @YangAttribute(name = ADDRESS)
    @Column(name = "address")
    private String address;

    @YangAttribute(name = AGENT_PORT)
    @Column(name = "agent_port")
    private String agentPort;

    @YangAttribute(name = TRAP_PORT)
    @Column(name = "trap_port")
    private String trapPort;

    @YangAttribute(name = SNMP_VERSION)
    @Column(name = "snmp_version")
    private String snmpVersion;

    @YangAttribute(name = COMMUNITY_STRING)
    @Column(name = "community_string")
    private String communityString;

    @YangChild
    @OneToOne(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER, orphanRemoval = true)
    private Snmpv3Auth snmpv3Auth;

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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAgentPort() {
        return agentPort;
    }

    public void setAgentPort(String agentPort) {
        this.agentPort = agentPort;
    }

    public String getTrapPort() {
        return trapPort;
    }

    public void setTrapPort(String trapPort) {
        this.trapPort = trapPort;
    }

    public String getSnmpVersion() {
        return snmpVersion;
    }

    public void setSnmpVersion(String snmpVersion) {
        this.snmpVersion = snmpVersion;
    }

    public String getCommunityString() {
        return communityString;
    }

    public void setCommunityString(String communityString) {
        this.communityString = communityString;
    }

    public Snmpv3Auth getSnmpv3Auth() {
        return snmpv3Auth;
    }

    public void setSnmpv3Auth(Snmpv3Auth snmpv3Auth) {
        this.snmpv3Auth = snmpv3Auth;
    }
}
