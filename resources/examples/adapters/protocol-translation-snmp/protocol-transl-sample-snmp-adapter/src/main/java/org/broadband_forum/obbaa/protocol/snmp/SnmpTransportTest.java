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

package org.broadband_forum.obbaa.protocol.snmp;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;

public final class SnmpTransportTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnmpTransportTest.class);

    private static final OID SYS_LOCATION = new OID("1.3.6.1.2.1.1.6.0");

    public SnmpTransportTest() {
    }


    public void testSnmp(SnmpTransport snmp, String sysLocation) {
        try {
            String response = null;
            LOGGER.info("SnmpTransportTest:testSnmp - ##### Testing snmpSet ####");
            VariableBinding setVarbind = new VariableBinding(SYS_LOCATION, new OctetString(sysLocation));
            response = snmp.snmpSet(setVarbind);
            if (response == null) {
                LOGGER.error("SnmpTransportTest:testSnmp - snmpSet Request timed out!!");
            } else {
                LOGGER.info("SnmpTransportTest:testSnmp - snmpSet Received response:: " + response);
            }

            LOGGER.info("SnmpTransportTest:testSnmp - ##### Testing snmpGet ####");
            VariableBinding getVarbind = new VariableBinding(SYS_LOCATION);
            response = snmp.snmpGet(getVarbind);
            if (response == null) {
                LOGGER.error("SnmpTransportTest:testSnmp - snmpGet Request timed out!!");
            } else {
                LOGGER.info("SnmpTransportTest:testSnmp - snmpGet Received response:: " + response);
            }
        } catch (IOException e) {
            LOGGER.error("Eror during SnmpTarget test", e);
            throw new RuntimeException("Error during SnmpTarget test", e);
        }
    }
}
