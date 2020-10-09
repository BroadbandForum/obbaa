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
 *
 * This file contains MetaData class which stores OID meta data.
 *
 * Created by Balaji Venkatachalapathy (DZSI) on 01/10/2020.
 */

package org.broadband_forum.obbaa.protocol.snmp;

public final class MetaData implements Cloneable {

    public String m_oid;

    public OIDType m_oidType;

    public String m_leaf;

    public String m_oidValue;

    public MetaData(String oid, OIDType oidType, String leaf) {
        m_oid = oid;
        m_oidType = oidType;
        m_leaf = leaf;
    }

    public MetaData(MetaData mdata) {
        this.m_oid = mdata.m_oid;
        this.m_oidType = mdata.m_oidType;
        this.m_leaf = mdata.m_leaf;
        this.m_oidValue = mdata.m_oidValue;
    }

    public Object clone() throws CloneNotSupportedException {
        return (new MetaData(this));
    }
}
