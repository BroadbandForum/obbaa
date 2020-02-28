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

package org.broadband_forum.obbaa.device.listener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.Snmp;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;

public  class TrapListener extends Thread implements CommandResponder, RegisterTrapCallback {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrapListener.class);
    private MultiThreadedMessageDispatcher m_tdispatcher;
    private Snmp m_tsnmp = null;
    private Address m_tlistenAddress;
    private ThreadPool m_tthreadPool = null;
    private static List<String> devices = new ArrayList<String>();
    private static final String TRAP_LISTENER_ADDRESS = "0.0.0.0";
    private static final String TRAP_LISTENER_PORT = "162";

    private int m_no = 0;
    private long m_start = -1;

    private static Map<String, ProcessTrapCallBack> m_callbacks = new HashMap<>();

    @Override
    public void registerCallBackForTrapUpdates(ProcessTrapCallBack trapCallBack, String ipAddr, String port) {
        LOGGER.info("Trap callback registered for IP " + ipAddr + " port " + port);
        m_callbacks.put(ipAddr, trapCallBack);
    }

    @Override
    public void unRegisterCallBackForTrapUpdates(ProcessTrapCallBack trapCallBack, String ipAddr, String port) {
        LOGGER.info("Trap callback Unregistered for IP " + ipAddr + " port " + port);
        m_callbacks.remove(ipAddr);
    }

    private void init() throws IOException {
        m_tthreadPool = ThreadPool.create("Trap", 4);
        m_tdispatcher = new MultiThreadedMessageDispatcher(m_tthreadPool,
                new MessageDispatcherImpl());

        m_tlistenAddress = GenericAddress.parse("udp:" + TRAP_LISTENER_ADDRESS + "/"
        + TRAP_LISTENER_PORT);
        DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping(
                (UdpAddress) m_tlistenAddress, true);
        m_tsnmp = new Snmp(m_tdispatcher, transport);
        m_tsnmp.getMessageDispatcher().addMessageProcessingModel(new MPv1());
        m_tsnmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());
        m_tsnmp.getMessageDispatcher().addMessageProcessingModel(new MPv3());
        USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(
                    MPv3.createLocalEngineID()), 0);
        SecurityModels.getInstance().addSecurityModel(usm);
        m_tsnmp.listen();
        LOGGER.info("Listening for traps on "
                + transport.getListenAddress().toString());
    }

    public void startup() {
        setDaemon(true);
        start(); //This will trigger the run() in a new thread.
    }

    public void run() {
        try {
            init();
            m_tsnmp.addCommandResponder(this);
        } catch (IOException ex) {
            LOGGER.error("error during trap listener creation", ex);
            throw new RuntimeException(ex);
        }
    }

    public void shutdown() throws IOException {
        LOGGER.info("Shutting down the TrapListener");

        if (m_tsnmp != null) {
            m_tsnmp.close();
        }

        if (m_tthreadPool != null) {
            m_tthreadPool.stop();
        }
    }

    protected int getNoOfCallbacks() {
        return m_callbacks.size();
    }

    @Override
    public void processPdu(CommandResponderEvent event) {
        String senderAddress = event.getPeerAddress().toString();
        String sender = senderAddress.substring(0, senderAddress.indexOf("/"));
        if (m_callbacks.containsKey(sender)) {
            m_callbacks.get(sender).processTrap(event);
        }
    }
}
