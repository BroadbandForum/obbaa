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
 * This file contains SnmpVarBindBuilder which will perform
 * SNMP VarBind building functionalites.
 *
 * Created by Sujeesh Ramakrishnan (DZSI) on 01/10/2020.
 */

package org.broadband_forum.obbaa.protocol.snmp;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.snmp4j.smi.Counter32;
import org.snmp4j.smi.Gauge32;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UnsignedInteger32;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;

public final class SnmpVarBindBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(SnmpVarBindBuilder.class);

    public SnmpVarBindBuilder() {
    }

/*
    Function to build the array of Varbinds
*/
    public List<VariableBinding> buildVarbindArray(List<MetaData> oids) {
        List<VariableBinding> varBinds = new ArrayList<VariableBinding>();
        for (MetaData iter:oids) {
            VariableBinding vbind = formVarbind(iter);
            if (null != vbind) {
                varBinds.add(vbind);
            } else {
                varBinds.clear();
            }
        }
        return varBinds;
    }

/*
    Function to build single varbind entity
*/
    public VariableBinding formVarbind(MetaData mdata) {
        VariableBinding vbind = null;
        if (mdata.m_oidValue != null) {
            vbind = new VariableBinding(new OID(mdata.m_oid), castOidType(mdata.m_oidType, mdata.m_oidValue));
        }
        return vbind;
    }

/*
    Function to cast the OID value based on the oid type
*/
    public Variable castOidType(OIDType type, String value) {
        Variable oidValue = null;
        switch (type) {
            case INTEGER32:
                oidValue = new Integer32(Integer.parseInt(value));
                break;
            case UNSIGNED32:
                oidValue = new UnsignedInteger32(Integer.parseInt(value));
                break;
            case COUNTER32:
                oidValue = new Counter32(Long.parseLong(value));
                break;
            case GAUGE32:
                oidValue = new Gauge32(Long.parseLong(value));
                break;
            case OCTETSTRING:
                oidValue = new OctetString(value);
                break;
            case BITSTRING:
                int val = Integer.parseInt(value);
                int arrayIndex = val / 8;
                byte[] buf = new byte[] { 0x00, 0x00, 0x00, 0x00 };
                buf[arrayIndex] |= 1 << (8 - (val % 8));
                OctetString oct = new OctetString();
                oct.append(buf);
                oidValue = new OctetString(oct);
                break;
            default:
                break;
        }
        return oidValue;
    }
}
