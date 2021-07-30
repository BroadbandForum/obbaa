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
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.broadband_forum.obbaa.device.listener.ProcessTrapCallBack;
import org.broadband_forum.obbaa.device.listener.RegisterTrapCallback;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.dmyang.entities.SnmpAuthentication;
import org.broadband_forum.obbaa.pma.NonNCNotificationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.UserTarget;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.MessageProcessingModel;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.AuthSHA;
import org.snmp4j.security.PrivAES128;
import org.snmp4j.security.PrivAES192;
import org.snmp4j.security.PrivAES256;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TreeEvent;
import org.snmp4j.util.TreeUtils;

public final class SnmpTransport {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnmpTransport.class);
    private Target m_target = null;
    private Snmp m_snmp = null;
    SnmpAuthentication m_auth = null;
    private String m_snmpversion = null;

    public SnmpTransport(Device device, NonNCNotificationHandler nonNCNotificationHan, RegisterTrapCallback trapHandle) {
        try {
            if (m_target == null) {
                m_auth = device.getDeviceManagement().getDeviceConnection().getSnmpAuth().getSnmpAuthentication();
                m_snmpversion = m_auth.getSnmpVersion();
                m_target = createTarget();
                m_snmp = new Snmp(new DefaultUdpTransportMapping());


                if (m_snmpversion.equals("v3")) {
                    configureV3();
                }

                m_snmp.listen();

                ProcessTrapCallBack snmpCallBackUpdater = new SnmpTrapHandler(m_auth, nonNCNotificationHan);
                trapHandle.registerCallBackForTrapUpdates(snmpCallBackUpdater, m_auth.getAddress(), m_auth.getTrapPort());
            }
        } catch (IOException e) {
            LOGGER.error("Eror when creating snmp target", e);
            throw new RuntimeException("Error when creating snmp target", e);
        }
    }

    private Target createTarget() {
        try {
            final InetAddress inetAddress = InetAddress.getByName(m_auth.getAddress());
            final Address address = new UdpAddress(inetAddress, Integer.parseInt(m_auth.getAgentPort()));
            if (m_snmpversion.equals("v3")) {
                UserTarget userTarget = new UserTarget();
                int securityLevel = (SecurityLevel.valueOf(m_auth.getSnmpv3Auth().getSecurityLevel())).getSnmpValue();
                userTarget.setSecurityLevel(securityLevel);
                userTarget.setSecurityName(new OctetString(m_auth.getSnmpv3Auth().getUsername()));
                userTarget.setVersion(SnmpConstants.version3);
                userTarget.setAddress(address);
                userTarget.setTimeout(1500);
                return userTarget;
            } else {
                CommunityTarget commTarget = new CommunityTarget();
                if (m_snmpversion.equals("v2c")) {
                    commTarget.setVersion(SnmpConstants.version2c);
                } else if (m_snmpversion.equals("v1")) {
                    commTarget.setVersion(SnmpConstants.version1);
                }
                commTarget.setCommunity(new OctetString(m_auth.getCommunityString()));
                commTarget.setAddress(address);
                commTarget.setTimeout(1500);
                return commTarget;
            }
        } catch (IOException e) {
            LOGGER.error("Eror when creating snmp target", e);
            throw new RuntimeException("Error when creating snmp target", e);
        }
    }

    private void configureV3() {
        OID privProtocol = null;
        OID authProtocol = null;
        OctetString privPassword = null;
        OctetString authPassword = null;
        USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
        OctetString securityName = new OctetString(m_auth.getSnmpv3Auth().getUsername());

        int securityLevel = (SecurityLevel.valueOf(m_auth.getSnmpv3Auth().getSecurityLevel())).getSnmpValue();

        switch (securityLevel) {
            case SecurityLevel.AUTH_PRIV:
                authProtocol = (m_auth.getSnmpv3Auth().getAuthProtocol().equals("AuthMD5")) ? AuthMD5.ID : AuthSHA.ID;
                authPassword =  new OctetString(m_auth.getSnmpv3Auth().getAuthPassword());
                privProtocol = getPrivProtocol(m_auth.getSnmpv3Auth().getPrivProtocol());
                privPassword =  new OctetString(m_auth.getSnmpv3Auth().getPrivPassword());
                break;
            case SecurityLevel.AUTH_NOPRIV:
                authProtocol = (m_auth.getSnmpv3Auth().getAuthProtocol().equals("AuthMD5")) ? AuthMD5.ID : AuthSHA.ID;
                authPassword =  new OctetString(m_auth.getSnmpv3Auth().getAuthPassword());
                break;
            case SecurityLevel.NOAUTH_NOPRIV:
                break;
            default:
                break;
        }

        usm.addUser(securityName, new UsmUser(securityName, authProtocol,
              authPassword, privProtocol, privPassword));
        MessageProcessingModel oldModel = m_snmp.getMessageDispatcher().getMessageProcessingModel(MessageProcessingModel.MPv3);

        if (oldModel != null) {
            m_snmp.getMessageDispatcher().removeMessageProcessingModel(oldModel);
        }
        m_snmp.getMessageDispatcher().addMessageProcessingModel(new MPv3(usm));

        return;
    }

    private OID getPrivProtocol(String protocolString) {
        OID privProtocol = null;
        if (protocolString.equals("PrivDES")) {
            privProtocol = PrivDES.ID;
        } else if ((protocolString.equals("PrivAES128"))
                    || (protocolString.equals("PrivAES"))) {
            privProtocol = PrivAES128.ID;
        } else if (protocolString.equals("PrivAES192")) {
            privProtocol = PrivAES192.ID;
        } else if (protocolString.equals("PrivAES256")) {
            privProtocol = PrivAES256.ID;
        }
        return privProtocol;
    }

    private PDU getPDU() {
        if (m_snmpversion.equals("v3")) {
            return new ScopedPDU();
        } else {
            return new PDU();
        }
    }

    public String snmpSet(VariableBinding varbind) throws IOException {
        LOGGER.debug("ProtTranslateSnmp:  inside snmp SET");
        String result = null;
        try {
            PDU pdu = getPDU();
            pdu.add(varbind);
            pdu.setType(PDU.SET);
            pdu.setMaxRepetitions(50);
            pdu.setRequestID(new Integer32(1));
            ResponseEvent event = m_snmp.send(pdu, m_target);
            if (event.getResponse() == null) {
                // request timed out
                LOGGER.warn("ProtTranslateSnmp:  SNMP request timedout");
            }
            else {
                LOGGER.debug("ProtTranslateSnmp:  Received response from: " + event.getPeerAddress());
                result = event.getResponse().toString();
                if (PDU.noError == event.getResponse().getErrorStatus()) {
                    LOGGER.debug("ProtTranslateSnmp:  snmpSet succedded");
                    LOGGER.debug(result);
                } else {
                    LOGGER.warn("ProtTranslateSnmp: snmpSet got a error reply ");
                    LOGGER.warn(result);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Error while performing snmp operation", e);
            throw new RuntimeException(e);
        }
        return result;
    }

    public String snmpSetGroup(List<VariableBinding> varbinds) throws IOException {
        LOGGER.debug("ProtTranslateSnmp:  inside snmp SET Group ");
        String result = null;
        try {
            if (null == varbinds) {
                LOGGER.debug("Nothing to send - varbind array is null\n");
                return "Nothing to do";
            }
            if (varbinds.isEmpty()) {
                LOGGER.debug("Nothing to send - varbind array is Empty\n");
                return "Nothing to send";
            }
            for (VariableBinding var :varbinds) {
                if (null == var) {
                    LOGGER.error("Variable binding value before group set - received a NULL  - unexpected!!");
                } else {
                    LOGGER.debug("Variable binding value before group set" + var.toString() + " " + var.toValueString());
                }
            }
            PDU pdu = getPDU();
            pdu.addAll(varbinds);
            pdu.setType(PDU.SET);
            pdu.setMaxRepetitions(50);
            pdu.setRequestID(new Integer32(1));
            ResponseEvent event = m_snmp.send(pdu, m_target);
            if (event.getResponse() == null) {
                // request timed out
                LOGGER.info("ProtTranslateSnmp:  SNMP request timedout");
            }
            else {
                LOGGER.debug("ProtTranslateSnmp:  Received response from: " + event.getPeerAddress());
                result = event.getResponse().toString();
                if (PDU.noError == event.getResponse().getErrorStatus()) {
                    LOGGER.debug("ProtTranslateSnmp:  snmpSetGroup succedded");
                    LOGGER.debug(result);
                } else {
                    LOGGER.error("ProtTranslateSnmp: snmpSetGroup got a error reply ");
                    LOGGER.error(result);
                }
            }
        } catch (IOException e) {
            LOGGER.error("error while performing snmp operation", e);
            throw new RuntimeException(e);
        }
        return result;
    }

    public String snmpGet(VariableBinding varbind) throws IOException {
        String result = null;
        try {
            PDU pdu = getPDU();
            pdu.add(varbind);
            pdu.setType(PDU.GET);
            pdu.setMaxRepetitions(50);
            ResponseEvent event = m_snmp.send(pdu, m_target);
            if (event.getResponse() == null) {
                // request timed out
                LOGGER.warn("ProtTranslateSnmp:  SNMP request timedout");
            }
            else {
                LOGGER.debug("ProtTranslateSnmp:  Received response from: " + event.getPeerAddress());
                result = event.getResponse().toString();
                if (PDU.noError == event.getResponse().getErrorStatus()) {
                    LOGGER.debug("ProtTranslateSnmp: snmpGet succedded");
                    LOGGER.debug(result);
                } else {
                    LOGGER.warn("ProtTranslateSnmp: snmpGet got a error reply ");
                    LOGGER.warn(result);
                }
            }
        } catch (IOException e) {
            LOGGER.error("error while performing snmp operation", e);
            throw new RuntimeException(e);
        }
        return result;
    }

    public String snmpGetNext(VariableBinding varbind) throws IOException {
        String result = null;
        try {
            PDU pdu = getPDU();
            pdu.add(varbind);
            pdu.setType(PDU.GETNEXT);
            pdu.setMaxRepetitions(50);
            ResponseEvent event = m_snmp.send(pdu, m_target);
            if (event.getResponse() == null) {
                // request timed out
                LOGGER.warn("ProtTranslateSnmp:  SNMP request timedout");
            }
            else {
                LOGGER.debug("ProtTranslateSnmp:  Received response from: " + event.getPeerAddress());
                result = event.getResponse().toString();
                if (PDU.noError == event.getResponse().getErrorStatus()) {
                    LOGGER.debug("ProtTranslateSnmp: snmpGetNext succedded");
                    LOGGER.debug(result);
                } else {
                    LOGGER.warn("ProtTranslateSnmp: snmpGetNext got a error reply ");
                    LOGGER.warn(result);
                }
            }
        } catch (IOException e) {
            LOGGER.error("error while performing snmp operation", e);
            throw new RuntimeException(e);
        }
        return result;
    }

    public String snmpWalk(String tableOid) throws IOException {
        String rvalue = null;
        Map<String, String> result = new TreeMap<>();
        TreeUtils treeUtils = new TreeUtils(m_snmp, new DefaultPDUFactory());
        List<TreeEvent> events = treeUtils.getSubtree(m_target, new OID(tableOid));
        if (events == null || events.size() == 0) {
            LOGGER.error("Error: Unable to read table...");
            return rvalue;
        }

        for (TreeEvent event : events) {
            if (event == null) {
                continue;
            }
            if (event.isError()) {
                LOGGER.error("Error: table OID [" + tableOid + "] " + event.getErrorMessage());
                continue;
            }

            VariableBinding[] varBindings = event.getVariableBindings();
            if (varBindings == null || varBindings.length == 0) {
                continue;
            }
            for (VariableBinding varBinding : varBindings) {
                if (varBinding == null) {
                    continue;
                }

                result.put("." + varBinding.getOid().toString(), varBinding.getVariable().toString());
            }

        }
        for (Map.Entry<String, String> entry : result.entrySet()) {
            LOGGER.info(entry.getKey() + ": " + entry.getValue());
        }
        //ToDo: map should be returned and netconf response should be formed from the
        //map.
        return "OK";
    }
}
