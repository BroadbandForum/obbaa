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

package org.broadband_forum.obbaa.pmcollection.pmdatahandler;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.broadband_forum.obbaa.pm.service.DataHandlerService;
import org.broadband_forum.obbaa.pm.service.IpfixDataHandler;
import org.broadband_forum.obbaa.pmcollection.nbi.NBIServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * PMData handler.
 */
public class PMDataHandler implements IpfixDataHandler, ThreadFactory {

    private static final Logger LOG
        = LoggerFactory.getLogger(PMDataHandler.class);

    private final DataHandlerService m_dataHandlerService;

    private final DBInterface m_dbInterface;
    private boolean m_dbOpen;

    private final HashMap<String, TSData> m_tsDataBuffer;
    private final HashMap<String, Long> m_tsDataBufferTime;
    private long m_timeout;
    private int m_maxBufferedPoints;
    private int m_maxBufferedMeasurements;
    private ScheduledExecutorService m_scheduler;
    private ScheduledFuture<?> m_timerHandle;
    private Runnable m_timerTask;

    private final NBIServer m_grpcServer;
    private boolean m_grpcServerRunning;

    private boolean m_running;

    /**
     * Constructor.
     *
     * @param dataHandlerService IPFix message source
     * @param dbInterface db implementation
     */
    public PMDataHandler(DataHandlerService dataHandlerService,
        DBInterface dbInterface) {
        m_dataHandlerService = dataHandlerService;
        m_dbInterface = dbInterface;
        m_dbOpen = false;

        m_tsDataBuffer = new HashMap<>();
        m_tsDataBufferTime = new HashMap<>();

        m_grpcServer = new NBIServer(this);
        m_grpcServerRunning = false;

        m_running = false;
    }

    /**
     * Start PMDataHandler. This method loads the configuration, fills the
     * mapping of metric names to their types, Load to class providing
     * the DB Interface, starts the DB interface, start a timer to purge write
     * jobs after a timeout and start the northbound interface.
     */
    public void start() {
        if (m_dbOpen && m_grpcServerRunning && m_timerHandle != null && m_running) {
            LOG.error("PMDataHandler already running! Ignoring start command.");
            return;
        }

        LOG.debug("PMDataHandler starting.");

        // get config from environment
        m_timeout = getIntVarFromEnv("PMD_TIMEOUT_BUFFERED_POINTS");
        m_maxBufferedPoints = getIntVarFromEnv("PMD_MAX_BUFFERED_POINTS");
        m_maxBufferedMeasurements = getIntVarFromEnv("PMD_MAX_BUFFERED_MEASUREMENTS");

        // open connection to time series DB
        if (!m_dbInterface.openDB()) {
            return;
        }
        m_dbOpen = true;

        // start timer purging DB write jobs
        m_timerTask = () -> {
            purgeBuffer();
        };

        m_scheduler = Executors.newScheduledThreadPool(1, this);
        m_timerHandle
            = m_scheduler.scheduleAtFixedRate(m_timerTask, 0, 10, TimeUnit.SECONDS);

        /*
        // start north bound interface
        if (!m_grpcServer.start()) {
            stop(); // stop already running DB and timer scheduler
            return;
        }
        m_grpcServerRunning = true;
        */

        m_dataHandlerService.registerIpfixDataHandler(this);

        m_running = true;
        LOG.info("PMDataHandler started.");
    }

    /**
     * Stop all PMDataHandler activity.
     *
     * @see PMDataHandler#start
     */
    public void stop() {
        LOG.debug("PMDataHandler stopping.");
        m_running = false;

        m_dataHandlerService.unregisterIpfixDataHandler(this);

        // stop north bound interface
        if (m_grpcServerRunning) {
            m_grpcServer.stop();
            m_grpcServerRunning = false;
        }

        flushBuffer();

        // stop timer purging write jobs
        if (m_timerHandle != null) {
            boolean success = m_timerHandle.cancel(true);
            if (success) { // all dead
                LOG.debug("Nothing to cancel.");
                m_scheduler.shutdownNow();
            }
            else {
                try {
                    m_scheduler.shutdown();
                    success = m_scheduler.awaitTermination(2, TimeUnit.SECONDS);
                    if (!success) {
                        m_scheduler.shutdownNow();
                    }
                }
                catch (InterruptedException ex) {
                    LOG.error("Caught exception: ", ex);
                }
            }
            m_scheduler = null;
            m_timerHandle = null;
        }

        // stop DB interface/connection
        if (m_dbOpen) {
            m_dbInterface.shutdownDB();
            m_dbOpen = false;
        }
        LOG.info("PMDataHandler stopped.");
    }

    public List<TSData> query(String measurement, Instant startTime,
            Instant stopTime, Map<String, List<String>> filter) {
        List<TSData> results = new ArrayList<>();
        m_dbInterface.executeQuery(measurement, startTime, stopTime, filter, results);
        return results;
    }

    /**
     * @param ipfixMessageJson ipfix message data in JSOn format
     * @see IpfixDataHandler#handleIpfixData.
     */
    @Override
    public void handleIpfixData(String ipfixMessageJson) {
        if (!m_running) {
            LOG.error("PMDataHandler not running. Ignoring data.");
            return;
        }
        // convert Json message to TSData object
        TSData tsData = convertIPFixData(ipfixMessageJson);
        if (tsData != null) {
            // store data
            storeData(tsData);
        }
    }

    /**
     * Convert IPFIX message in Json format to internal TSData object.
     *
     * @param ipfixMessage Json formatted ipfix data
     * @return the TSData object containing the ipfix message
     */
    private TSData convertIPFixData(String ipfixMessage) {
        LOG.debug("Converting IPFIX message to TSData. Msg: '{}'", ipfixMessage);

        // parse json message
        JsonElement jsonTree;
        try {
            jsonTree = new JsonParser().parse(ipfixMessage);
        }
        catch (JsonSyntaxException e) {
            LOG.error("Failed to parse ipfixMessage. error: {}, ipfixMsg: {}",
                e.getMessage(), ipfixMessage);
            return null;
        }
        if (!jsonTree.isJsonObject()) {
            LOG.error("No JsonObject in ipfixMessage: {}", ipfixMessage);
            return null;
        }
        JsonObject obj = jsonTree.getAsJsonObject();

        String measurement = "";
        long timestamp = 0;
        Map<String, String> tags = new HashMap<>(TSData.TSPoint.TAG_NAMES.size());
        // loop over all header object and copy value to TSData tags
        for (String key : obj.keySet()) {
            if (key.equals("timestamp")) {
                String timestampVal = obj.get("timestamp").getAsString();
                timestamp = Instant.parse(timestampVal).getEpochSecond();
                continue;
            }
            if (key.equals("data")) {
                continue;
            }
            // check for known tags
            if (TSData.TSPoint.TAG_NAMES.contains(key)) {
                if (key.equals("observationDomain")) {
                    tags.put(key, Long.toString(obj.get(key).getAsLong()));
                    continue;
                }
                if (key.equals("templateID")) {
                    // we use the templateID as measurement name
                    measurement = Long.toString(obj.get(key).getAsLong());
                    tags.put(key, measurement);
                    continue;
                }
                tags.put(key, obj.get(key).getAsString());
            }
            else {
                LOG.error("Unsupported tag key '{}'", key);
                return null;
            }
        }
        // if not all tags found, drop message
        if (tags.size() != TSData.TSPoint.TAG_NAMES.size()) {
            LOG.error("Only {} tags found. Expecting {}",
                tags.size(), TSData.TSPoint.TAG_NAMES.size());
            return null;
        }

        TSData.TSPoint point = new TSData.TSPoint();
        point.m_measurement = measurement;
        point.m_timestamp = (int) timestamp;
        point.m_fields = new HashMap<>();
        point.m_tags = tags;
        TSData tsData = new TSData();
        tsData.m_tsPoints.add(point);

        // loop over all object and copy the information to fields.
        JsonArray arr = obj.getAsJsonArray("data");
        for (int i = 0; i < arr.size(); i++) {
            JsonObject objectList = (JsonObject)arr.get(i);
            for (String key : objectList.keySet()) {
                String fieldname = objectList.get("metric").getAsString();
                TSData.MetricType mt
                    = getMetricType(objectList.get("dataType").getAsString());
                String value = objectList.get("value").getAsString();
                if (fieldname != null && mt != null && value != null) {
                    switch (mt) {
                        case T_long:
                            point.m_fields.put(fieldname, Long.parseLong(value));
                            break;
                        case T_double:
                            point.m_fields.put(fieldname, Double.parseDouble(value));
                            break;
                        case T_string:
                        default:
                            point.m_fields.put(fieldname, value);
                            break;
                        case T_boolean:
                            point.m_fields.put(fieldname, Boolean.parseBoolean(value));
                    }
                }
                else {
                    LOG.error("Wrong data format '{}' '{}' '{}'. Data ignored.",
                        fieldname, mt, value);
                    break;
                }
            }
        }
        return tsData;
    }

    /**
     * Data is stored in a buffer until buffer size is larger than the
     * configured value or the first buffer entry is older than configured
     * timeout.
     *
     * @param data the set of points to be stored
     */
    private void storeData(TSData data) {
        String key = buildKey(data);
        TSData tsData;
        synchronized (m_tsDataBuffer) {
            tsData = m_tsDataBuffer.get(key);
            if (tsData == null) { // if we have no entry for that key, create one.
                LOG.debug("Create new entry for key '{}'", key);
                m_tsDataBuffer.put(key, data);
                m_tsDataBufferTime.put(key, Instant.now().getEpochSecond() + m_timeout);
                tsData = data;
            }
            else { // if we have an entry for that key, add data to existing one.
                tsData.m_tsPoints.addAll(data.m_tsPoints);
            }
            // if number of points of measurement exeeds the limit, remove from buffer
            if (tsData.m_tsPoints.size() >= m_maxBufferedPoints) {
                LOG.debug("Max number of buffered points for key '{}' exceeded", key);
                m_tsDataBuffer.remove(key);
                m_tsDataBufferTime.remove(key);
            } // if number of measurements exeeds the limit, remove from buffer
            else if (m_tsDataBuffer.size() >= m_maxBufferedMeasurements) {
                LOG.debug("Max number of buffered measurements for key '{}' exceeded", key);
                m_tsDataBuffer.remove(key);
                m_tsDataBufferTime.remove(key);
            }
            else { // dont write to db, continue to fill buffer
                LOG.debug("Data stored in buffer.");
                tsData = null;
            }
        }
        if (tsData != null) { // write tsData to DB
            LOG.debug("Writing points for key '{}' to DB.", key);
            m_dbInterface.executeStore(tsData);
        }
    }

    /**
     * Create a key for tsData buffer. The measurement name is used as the key.
     * More complex keys could be build if needed.
     *
     * @param tsData the points to build the key for
     * @return key
     */
    private String buildKey(TSData tsData) {
        return tsData.getTSPoints().get(0).m_measurement;
    }

    /**
     * Flush any pending data to the DB. When the PMDataHandler is stopped,
     * this method flushes any pending data.
     */
    private void flushBuffer() {
        synchronized (m_tsDataBuffer) {
            for (String key : m_tsDataBuffer.keySet()) {
                m_dbInterface.executeStore(m_tsDataBuffer.get(key));
            }
            m_tsDataBuffer.clear();
            m_tsDataBufferTime.clear();
        }
    }

    /**
     * Write TSData to the DB that is older than timeout.
     */
    private void purgeBuffer() {
        LOG.debug("Purging timed out db write jobs.");
        long now = Instant.now().getEpochSecond();

        try {
            List<TSData> pointsTimedout = new ArrayList<>();
            synchronized (m_tsDataBuffer) {
                for (Iterator<String> it = m_tsDataBufferTime.keySet().iterator();
                        it.hasNext();) {
                    String key = it.next();
                    long timeout = m_tsDataBufferTime.get(key);
                    if (timeout < now) {
                        LOG.debug("Purging data for key '{}', time: {}", key, timeout);
                        it.remove();
                        TSData tsData = m_tsDataBuffer.remove(key);
                        if (tsData != null) {
                            pointsTimedout.add(tsData);
                        }
                        else {
                            LOG.error("No data for key '{}'", key);
                        }
                    }
                }
            }
            if (!pointsTimedout.isEmpty()) {
                for (TSData tsData: pointsTimedout) {
                    m_dbInterface.executeStore(tsData);
                }
            }
        }
        catch (Throwable t) {
            LOG.error("Caught exception: ", t);
        }
    }

    /**
     * Get type of an IPFix element.
     *
     * @param key key for metric type
     * @return metric type of key
     */
    private synchronized TSData.MetricType getMetricType(String key) {
        TSData.MetricType mt = TSData.IPFIXTYPETODBTYPE.get(key);
        if (mt == null) {
            mt = TSData.MetricType.T_string;
        }
        return mt;
    }

    /**
     * Create a new new thread instance. This is called by
     * ScheduledExecutorService and used to give threads a name.
     *
     * @see ThreadFactory#newThread(java.lang.Runnable)
     *
     * @param runnable threads runnable
     * @return new thread
     */
    @Override
    public Thread newThread(Runnable runnable) {
        return new Thread(runnable, "PMDH-Timer-1");
    }

    private Integer getIntVarFromEnv(String envName) {
        String envVar = System.getenv(envName);
        if (envVar == null || envVar.isEmpty()) {
            throw new RuntimeException("Environment variable " + envName + " not set.");
        }
        Integer intVal;
        try {
            intVal = Integer.parseInt(envVar);
        }
        catch (NumberFormatException e) {
            LOG.error("Value '{}' of " + envName + " is not an integer number", envVar);
            throw e;
        }
        return intVal;
    }
}
