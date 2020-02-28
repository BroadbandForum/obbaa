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

package org.broadband_forum.obbaa.ipfix.collector.pmdatahandler;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;
import org.broadband_forum.obbaa.pmcollection.nbi.NBIClient;
import org.broadband_forum.obbaa.pmcollection.nbi.NBIServer;
import org.broadband_forum.obbaa.pmcollection.pmdatahandler.PMDataHandler;
import org.broadband_forum.obbaa.pmcollection.pmdatahandler.influxdb.InfluxDBImpl;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PM Data Handler tests.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PMDataHandlerTest {
    private final static Logger LOG =
        LoggerFactory.getLogger(PMDataHandlerTest.class);
    private static IPFIXCollector ipfixCollector;
    private static InfluxDBImpl db;
    public static PMDataHandler pmDataHandler;
    public static IPFIXGUI ipfixgui;
    public static boolean withGUI = false;
    public static final Object CONDITION = new Object();

    public PMDataHandlerTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        ipfixCollector = new IPFIXCollector();
        db = new InfluxDBImpl();
        pmDataHandler = new PMDataHandler(ipfixCollector, db);
        if (PMDataHandlerTest.withGUI) {
            SwingUtilities.invokeLater(() -> {
                ipfixgui = new IPFIXGUI();
            });
            try {
              synchronized (CONDITION) {
                CONDITION.wait();
              }
            } catch (InterruptedException ex) {
            }
            withGUI = false;
            ipfixgui = null;
        }
    }

    public static boolean start() {
        pmDataHandler.start();
        ipfixCollector.start();
        return true;
    }

    public static void stop() {
        ipfixCollector.stop();
        pmDataHandler.stop();
    }

    public static void destroy() {
        synchronized (CONDITION) {
            CONDITION.notify();
        }
    }

    @AfterClass
    public static void tearDownClass() {
        if (ipfixgui != null) {
            ipfixgui.dispose();
        }
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of start method of class PMDataHandler.
     */
    @Test
    public void test1Start() {
        LOG.info("Test start");
        boolean expResult = true;
        boolean result = start();
        assertEquals(expResult, result);
    }

    @Test
    public void test3Query() {
    /*
        LOG.info("Test NBI query");
        // Access service running on the local machine on port 5051
        NBIClient client = new NBIClient("localhost", 5051);
        try {
            String measurement = "267";
            Instant startTime = Instant.now().minus(10, ChronoUnit.MINUTES);
            Instant stopTime = Instant.now();
            Map<Integer, List<String>> filter = new HashMap<>();
            List<String> devices = new ArrayList<>();
            devices.add("dpu0");
            devices.add("dpu1");
            // get all data for dpu0 and dpu1
            filter.put(1, devices); // F_HOSTNAME
            client.query(measurement, startTime, stopTime, filter);
        }
        finally {
            try {
                client.shutdown();
            }
            catch (InterruptedException e) {
                System.err.println("Caught exception: " + e);
            }
        }
    */
    }

    @Test
    public void test4QueryStream() {
    /*
        LOG.info("Test NBI query stream");
        NBIClient client = new NBIClient("localhost", 5051);
        try {
            // Access a service running on the local machine on port 5051
            String measurement = "267";
            Instant startTime = Instant.now().minus(10, ChronoUnit.MINUTES);
            Instant stopTime = Instant.now();
            Map<Integer, List<String>> filter = new HashMap<>();
            List<String> devices = new ArrayList<>();
            devices.add("dpu0");
            devices.add("dpu1");
            // get all data for dpu0 and dpu1
            filter.put(1, devices); // F_HOSTNAME
            client.queryStream(measurement, startTime, stopTime, filter);
        }
        finally {
            try {
                client.shutdown();
            }
            catch (InterruptedException e) {
                System.err.println("Caught exception: " + e);
            }
        }
    */
    }

    /**
     * Test of stop method, of class PMDataHandler.
     */
    @Test
    public void test5Stop() {
        LOG.info("Test stop PM Data handler");
        stop();
    }
}
