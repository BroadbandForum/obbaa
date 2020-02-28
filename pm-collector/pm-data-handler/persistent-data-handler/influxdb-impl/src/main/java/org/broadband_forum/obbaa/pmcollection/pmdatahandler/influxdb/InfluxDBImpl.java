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

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.pmcollection.pmdatahandler.DBInterface;
import org.broadband_forum.obbaa.pmcollection.pmdatahandler.TSData;
import org.broadband_forum.obbaa.pmcollection.pmdatahandler.TSData.TSPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.client.write.events.AbstractWriteEvent;
import com.influxdb.client.write.events.BackpressureEvent;
import com.influxdb.client.write.events.EventListener;
import com.influxdb.client.write.events.ListenerRegistration;
import com.influxdb.client.write.events.WriteErrorEvent;
import com.influxdb.client.write.events.WriteRetriableErrorEvent;
import com.influxdb.client.write.events.WriteSuccessEvent;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

/**
 * InfluxDB interface.
 */
public class InfluxDBImpl implements DBInterface, EventListener<AbstractWriteEvent> {
    private static final Logger LOG =
        LoggerFactory.getLogger(InfluxDBImpl.class);

    private InfluxDBClient m_dbClient;
    private WriteApi m_writeApi;
    private String m_organisation;
    private String m_bucketId;
    private ListenerRegistration m_registration;
    private final Object m_busyMutex = new Object();
    private Boolean m_busy = false;

    public InfluxDBImpl() {
        LOG.info("Loading implementation.");
    }

    @Override
    public void executeStore(TSData tsData) {
        long startTime = 0;
        if (LOG.isDebugEnabled()) {
            // record start time
            startTime = System.currentTimeMillis();
        }

        synchronized (m_busyMutex) {
            while (m_busy) {
                try {
                    LOG.error("Waiting");
                    m_busyMutex.wait();
                    LOG.error("Waiting has an end.");
                }
                catch (InterruptedException ex) {
                    LOG.error("Waiting for busy mutex interrupted.", ex);
                    return;
                }
            }
            m_busy = true;
        }

        long numberOfPoints = 0;
        for (TSPoint p : tsData.getTSPoints()) {
            Point point = Point
                .measurement(p.m_measurement)
                .time((long)p.m_timestamp, WritePrecision.S);
            p.m_tags.keySet().forEach((key) -> {
                point.addTag(key, p.m_tags.get(key));
            });
            if (p.m_fields == null) { // we must have at least one field
                continue;
            }
            p.m_fields.keySet().forEach((String key) -> {
                Object val = p.m_fields.get(key);
                if (val instanceof Long) {
                    point.addField(key, (Long)val);
                }
                else if (val instanceof Double) {
                    point.addField(key, (Double)val);
                }
                else if (val instanceof Float) {
                    point.addField(key, (Number)val);
                }
                else if (val instanceof String) {
                    point.addField(key, (String)val);
                }
                else if (val instanceof Boolean) {
                    point.addField(key, (Boolean)val);
                }
                else if (val != null) {
                    LOG.error("Type of field not supported: {}", val.getClass());
                }
            });
            m_writeApi.writePoint(m_bucketId, m_organisation, point);
            numberOfPoints++;
        }

        m_writeApi.flush();

        if (LOG.isDebugEnabled()) {
            // calculate elapsed time
            long elapsedTime = System.currentTimeMillis() - startTime;
            LOG.debug("Added {} points in {} milliseconds.",
                numberOfPoints, elapsedTime);
        }
    }

    private void appendFilter(Map<String, List<String>> filter,
        StringBuilder query) {
        if (filter == null || filter.isEmpty()) {
            return;
        }
        for (String key : filter.keySet()) {
            if (!TSData.TSPoint.TAG_NAMES.contains(key)) {
                LOG.error("Wrong key '{}' in filter expression. Ignored!", key);
                continue;
            }
            List<String> values = filter.get(key);
            for (int i = 0; i < values.size() - 1; i++) {
                query.append(" |> filter(fn: (r) => r.").append(key).append(" == \"").append(values.get(i)).append("\" or r.");
            }
            query.append(key).append(" == \"").append(values.get(values.size() - 1)).append("\")");
        }
    }

    @Override
    public void executeQuery(String measurement, Instant startTime, Instant stopTime,
        Map<String, List<String>> filter, List<TSData> results) {
        long startQueryTime = 0;
        if (LOG.isDebugEnabled()) {
            // record start time
            startQueryTime = System.currentTimeMillis();
        }

        // build query filter
        StringBuilder query = new StringBuilder()
            .append("from(bucket: \"").append(m_bucketId).append("\") ")
            .append("|> range(start: ").append(startTime).append(", stop: ").append(stopTime).append(") ")
            .append("|> filter(fn: (r) => r._measurement == \"").append(measurement).append("\")");
        appendFilter(filter, query);
        LOG.debug("filter: {}", query.toString());

        // excecute query
        List<FluxTable> tables =
            m_dbClient.getQueryApi().query(query.toString(), m_organisation);
        LOG.debug("result: {} tables", tables.size());

        // parse result
        for (FluxTable table : tables) {
            TSData tsData = new TSData();
            for (FluxRecord fluxRecord : table.getRecords()) {
                TSPoint point = new TSPoint();
                point.m_measurement =
                    (String)fluxRecord.getValueByKey("_measurement");
                Instant time = fluxRecord.getTime();
                if (time != null) {
                    point.m_timestamp = (int)time.getEpochSecond();
                }
                else {
                    point.m_timestamp = 0;
                    LOG.error("Timestamp from DB is null");
                }
                Object val = fluxRecord.getValueByKey("_value");
                point.m_fields = new HashMap<>(1);
                if (val instanceof Long) {
                    point.m_fields.put((String)fluxRecord.getValueByKey("_field"),
                        (Long)val);
                }
                else if (val instanceof Double) {
                    point.m_fields.put((String)fluxRecord.getValueByKey("_field"),
                        (Double)val);
                }
                else if (val instanceof String) {
                    point.m_fields.put((String)fluxRecord.getValueByKey("_field"),
                        (String)val);
                }
                else if (val instanceof Boolean) {
                    point.m_fields.put((String)fluxRecord.getValueByKey("_field"),
                        (Boolean)val);
                }
                else if (val != null) {
                    LOG.error("Unsupported value type {}", val.getClass());
                }

                point.m_tags.put("deviceAdapter",
                    (String)fluxRecord.getValueByKey("deviceAdapter"));
                point.m_tags.put("hostName",
                    (String)fluxRecord.getValueByKey("hostName"));
                point.m_tags.put("sourceIP",
                    (String)fluxRecord.getValueByKey("sourceIP"));
                point.m_tags.put("templateID",
                    (String)fluxRecord.getValueByKey("templateID"));
                point.m_tags.put("observationDomain",
                    (String)fluxRecord.getValueByKey("observationDomain"));
                tsData.m_tsPoints.add(point);
            }
            results.add(tsData);
        }

        if (LOG.isDebugEnabled()) {
            // Calculate elapsed time.
            long elapsedTime = System.currentTimeMillis() - startQueryTime;
            LOG.debug("Query returned in {} milliseconds.", elapsedTime);
        }
    }

    @Override
    public boolean openDB() {
        LOG.info("Opening InfluxDB connection.");

        m_busy = false;

        // check/get data from environment
        m_organisation = System.getenv("INFLUXDB_ORGANISATION");
        if (m_organisation == null || m_organisation.isEmpty()) {
            LOG.error("environment variable 'INFLUXDB_ORGANISATION' is null or empty.");
            return false;
        }
        m_bucketId = System.getenv("INFLUXDB_BUCKETID");
        if (m_bucketId == null || m_bucketId.isEmpty()) {
            LOG.error("environment variable 'INFLUXDB_BUCKETID' is null or empty.");
            return false;
        }
        String apiURL = System.getenv("INFLUXDB_API_URL");
        if (apiURL == null || apiURL.isEmpty()) {
            LOG.error("environment variable 'INFLUXDB_API_URL' is null or empty.");
            return false;
        }
        String token = System.getenv("INFLUXDB_TOKEN");
        if (token == null || token.isEmpty()) {
            LOG.error("environment variable 'INFLUXDB_TOKEN' is null or empty.");
            return false;
        }

        // create client and writeApi object to access DB
        m_dbClient = InfluxDBClientFactory.create(apiURL, token.toCharArray());
        m_writeApi = m_dbClient.getWriteApi();

        // register for writeApi results
        m_registration = m_writeApi.listenEvents(AbstractWriteEvent.class, this);

        LOG.info("InfluxDB client initialized.");
        return true;
    }

    @Override
    public void shutdownDB() {
        synchronized (m_busyMutex) {
            while (m_busy) {
                try {
                    LOG.debug("Waiting");
                    m_busyMutex.wait();
                    LOG.debug("Waiting has an end.");
                }
                catch (InterruptedException ex) {
                    LOG.error("Waiting for busy mutex interrupted.", ex);
                    return;
                }
            }
            m_busy = true;
        }

        m_registration.dispose();
        m_writeApi.close();
        m_dbClient.close();
        LOG.info("InfluxDB connection closed.");
    }

    @Override
    public void onEvent(AbstractWriteEvent value) {
        if (value instanceof WriteSuccessEvent) {
            //value.logEvent();
        }
        else if (value instanceof WriteErrorEvent) {
            value.logEvent();
        }
        else if (value instanceof WriteRetriableErrorEvent) {
            value.logEvent();
        }
        else if (value instanceof BackpressureEvent) {
            value.logEvent();
        }
        synchronized (m_busyMutex) {
            m_busy = false;
            m_busyMutex.notifyAll();
        }
    }
}
