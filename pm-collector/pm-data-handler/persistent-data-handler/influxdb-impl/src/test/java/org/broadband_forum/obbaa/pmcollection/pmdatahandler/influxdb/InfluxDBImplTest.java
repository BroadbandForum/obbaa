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

package org.broadband_forum.obbaa.pmcollection.pmdatahandler.influxdb;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.pmcollection.pmdatahandler.TSData;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class InfluxDBImplTest {
    private static final int MEASUREMENTS = 100;

    private static InfluxDBImpl instance;
    private static Instant startTime;
    private static Instant stopTime;

    public InfluxDBImplTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        System.out.println("openDB");
        instance = new InfluxDBImpl();
        boolean expResult = true;
        boolean result = instance.openDB();
        assertEquals(expResult, result);
    }

    @AfterClass
    public static void tearDownClass() {
        System.out.println("shutdownDB");
        instance.shutdownDB();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    private TSData fillTSData() {
        int timeIncrement = 1;

        startTime = Instant.now().minusSeconds(MEASUREMENTS * timeIncrement);
        long timestamp = startTime.getEpochSecond();

        long val = 0;
        TSData tsData = new TSData();
        tsData.m_tsPoints.ensureCapacity(MEASUREMENTS);
        for (int i = 0; i < MEASUREMENTS; i++) {
            TSData.TSPoint p = new TSData.TSPoint();
            timestamp += timeIncrement;
            val = val + Math.round(10.0 * Math.random());
            p.m_measurement = "unittest";
            p.m_timestamp = (int)timestamp;

            HashMap<String, String> tags =
                new HashMap<>(TSData.TSPoint.TAG_NAMES.size());
            tags.put("deviceAdapter", "sample-DPU-modeltls-1.0");
            tags.put("hostName", "dpu1");
            tags.put("sourceIP", "192.168.1.1");
            tags.put("templateID", "1");
            tags.put("observationDomain", "2");
            p.m_tags = tags;

            p.m_fields = new HashMap<>(2);
            p.m_fields.put(
                "/if:interfaces-state/if:interface/if:statistics/if:in-errors",
                val);
            p.m_fields.put(
                "/if:interfaces-state/if:interface/if:statistics/if:out-errors",
                2 * val);
            p.m_fields.put("/if:interfaces-state/if:interface/if:name", "DSL1");
            tsData.m_tsPoints.add(p);
        }
        stopTime = startTime.plusSeconds(MEASUREMENTS * timeIncrement);
        return tsData;
    }

    public static void showResult(List<TSData> results) {
        // now all of the results are in so we just iterate over each set of
        // results and do any processing necessary.
        results.forEach((TSData dataSets) -> {
            for (final TSData.TSPoint data : dataSets.getTSPoints()) {
                System.out.println(
                    new Date((long)data.m_timestamp * 1000) + " " +
                    data.getMeasurementName());
                Map<String, String> tags = data.getTags();
                if (tags != null && tags.size() > 0) {
                    System.out.println("Tags:");
                    tags.entrySet().forEach((pair) -> {
                        System.out.println(
                            " " + pair.getKey() + "=" + pair.getValue());
                    });
                    System.out.print("\n");
                }
                Map<String, Object> fields = data.getFields();
                if (fields != null && fields.size() > 0) {
                    System.out.println("Fields:");
                    fields.entrySet().forEach((pair) -> {
                        System.out.println(
                            " " + pair.getKey() + "=" + pair.getValue());
                    });
                    System.out.print("\n");
                }
            }
        });
    }

    /**
     * Test of executeStore method, of class InfluxDBImpl.
     */
    @Test
    public void test1ExecuteStore() {
        System.out.println("executeStore");
        TSData tsData = fillTSData();
        instance.executeStore(tsData);
    }

    /**
     * Test of executeQuery method, of class InfluxDBImpl.
     */
    @Test
    public void test2ExecuteQuery() {
        System.out.println("executeQuery");
        String measurement = "unittest";
        Map<String, List<String>> filter = null;
        List<TSData> results = new ArrayList<>();
        instance.executeQuery(measurement, startTime, stopTime, filter, results);
        assertEquals(3, results.size()); //two fields: if:in-errors and if:name
        assertEquals(MEASUREMENTS, results.get(0).m_tsPoints.size());
        assertEquals(MEASUREMENTS, results.get(1).m_tsPoints.size());
        showResult(results);
    }
}
