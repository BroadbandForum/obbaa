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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;

import org.broadband_forum.obbaa.pm.service.DataHandlerService;
import org.broadband_forum.obbaa.pm.service.IpfixDataHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Dummy IPFIXCollector implementation to test PMDataHandler.
 */
public class IPFIXCollector implements DataHandlerService, ThreadFactory {

    class Data {
        public String m_ip;
        public String[] m_counterNames;
        public long[] m_counterValues;
        public long m_observationDomain;

        public Data() {
        }
    }

    private static final Logger LOG
        = LoggerFactory.getLogger(IPFIXCollector.class);

    private final static int NUM_DEVICES = 10000;
    private final static int NUM_THREADS = 100;
    private final static int NUM_DEV_PER_THREAD = NUM_DEVICES / NUM_THREADS;

    private final Map<String, Data> m_countersPerDpu;

    private final List<IpfixDataHandler> m_messageReceiver;

    private ScheduledExecutorService m_scheduler;
    private final ArrayList<ScheduledFuture<?>> m_timerHandles;
    private final ThreadGroup m_threadGroup;
    private int m_threadNum;

    private int m_records;
    private final Object m_mutex = new Object();

    public IPFIXCollector() {
        m_messageReceiver = new ArrayList<>(1);
        m_timerHandles = new ArrayList<>(NUM_THREADS);
        m_threadGroup = new ThreadGroup("IPFIX-ThreadGroup");
        m_threadNum = 0;

        m_countersPerDpu = new HashMap<>(NUM_DEVICES);
        for (int i = 0; i < NUM_DEVICES; i++) {
            String dev = "dpu" + i;
            IPFIXCollector.Data data = new Data();
            data.m_ip = "192.168.1." + i;
            data.m_counterNames = new String[] {
                "/if:interfaces-state/if:interface/if:statistics/if:in-errors",
                "/if:interfaces-state/if:interface/if:statistics/if:in-discards",
                "/if:interfaces-state/if:interface/if:statistics/if:out-errors",
                "/if:interfaces-state/if:interface/if:statistics/if:out-discards"
            };
            data.m_counterValues = new long[]{0L, 0L, 0L, 0L};
            data.m_observationDomain = 1;
            m_countersPerDpu.put(dev, data);
        }
    }

    public void start() {
        m_records = 0;

        m_scheduler = Executors.newScheduledThreadPool(NUM_THREADS, this);
        for (int i = 0; i < NUM_THREADS; i++) {
            Runnable timerTask = _createTimerTask(i * NUM_DEV_PER_THREAD,
                i * NUM_DEV_PER_THREAD + NUM_DEV_PER_THREAD - 1);
            int timeout = 1;
            m_timerHandles.add(
                m_scheduler.scheduleAtFixedRate(timerTask, 0, timeout, TimeUnit.SECONDS));
        }
    }

    private Runnable _createTimerTask(final int minDevice, final int maxDevice) {
        Runnable timerTask = () -> {
            LOG.debug("Starting handler notification for device {} to {}.",
                minDevice, maxDevice);
            for (int i = minDevice; i <= maxDevice; i++) {
                String dev = "dpu" + i;
                try {
                    Data data;
                    synchronized (m_countersPerDpu) {
                        data = m_countersPerDpu.get(dev);
                    }
                    String msg = _createMsg(dev, data);
                    synchronized (m_messageReceiver) {
                        m_messageReceiver.forEach((handler) -> {
                            handler.handleIpfixData(msg);
                        });
                    }
                }
                catch (Throwable e) {
                    LOG.error("Caught exception: ", e);
                }
            }
            synchronized (m_mutex) {
                m_records += maxDevice - minDevice + 1;
                if (PMDataHandlerTest.ipfixgui != null) {
                    PMDataHandlerTest.ipfixgui.addMessage(m_records);
                }
            }
            LOG.debug("Handler notification done.");
        };
        return timerTask;
    }

    public void stop() {
        if (m_scheduler == null) {
            return;
        }

        LOG.debug("Stopping all idle timer.");
        boolean success = true;
        for (ScheduledFuture<?> timerHandle : m_timerHandles) {
            success = success && timerHandle.cancel(false);
        }
        m_timerHandles.clear();

        LOG.debug("Stopping all running timer.");
        m_scheduler.shutdown();
        try {
            success = m_scheduler.awaitTermination(1, TimeUnit.MINUTES);
            if (!success) {
                m_scheduler.shutdownNow();
            }
        }
        catch (InterruptedException ex) {
            LOG.error("Caught exception: ", ex);
        }
        LOG.info("records: {}", m_records);
    }

    private String _createMsg(String host, Data data) {
        /*
        {
          "sourceIP":"172.17.0.1",
          "hostName":"lsdpu1",
          "deviceAdapter":"sample-DPU-modeltls-1.0",
          "templateID":275,
          "observationDomain":4335,
          "timestamp":"2019-12-02T07:30:06Z",
          "data": [
            {
                "metric":"/if:interfaces-state/if:interface/if:statistics/if:in-errors",
                "dataType":"unsigned32",
                "value":"15"
            },
            {
                "metric":"/if:interfaces-state/if:interface/if:statistics/if:out-discards",
                "dataType":"unsigned32",
                "value":"150"
            },
            {
                "metric":"/if:interfaces-state/if:interface/if:statistics/if:out-errors",
                "dataType":"unsigned32",
                "value":"1"
            },
            {
                "metric":"/if:interfaces-state/if:interface/if:statistics/if:in-discards",
                "dataType":"unsigned32",
                "value":"1500"
            },
            {
                "metric":"/if:interfaces-state/if:interface/if:name",
                "dataType":"string",
                "value":"DSL1"
            }
          ]
        }
        */

        JsonObject json = new JsonObject();
        json.addProperty("sourceIP", data.m_ip);
        json.addProperty("hostName", host);
        json.addProperty("deviceAdapter", "sample-DPU-modeltls-1.0");
        json.addProperty("templateID", 267);
        json.addProperty("observationDomain", data.m_observationDomain);
        json.addProperty("timestamp",
            ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT));

        JsonArray ipfixElements = new JsonArray();
        JsonObject ipfixElement;
        for (int i = 0; i < data.m_counterNames.length; i++) {
            ipfixElement = new JsonObject();
            data.m_counterValues[i]
                = data.m_counterValues[i] + Math.round(10.0 * Math.random());
            ipfixElement.addProperty("metric", data.m_counterNames[i]);
            ipfixElement.addProperty("dataType", "unsigned32");
            ipfixElement.addProperty("value", 
                Long.toString(data.m_counterValues[i]));
            ipfixElements.add(ipfixElement);
        }
        ipfixElement = new JsonObject();
        ipfixElement.addProperty("metric",
            "/if:interfaces-state/if:interface/if:name");
        ipfixElement.addProperty("dataType", "string");
        ipfixElement.addProperty("value", "DSL1");
        ipfixElements.add(ipfixElement);

        json.add("data", ipfixElements);

        return json.toString();
    }

    @Override
    public void registerIpfixDataHandler(IpfixDataHandler dataHandler) {
        synchronized (m_messageReceiver) {
            m_messageReceiver.add(dataHandler);
        }
    }

    @Override
    public void unregisterIpfixDataHandler(IpfixDataHandler dataHandler) {
        synchronized (m_messageReceiver) {
            m_messageReceiver.remove(dataHandler);
        }
    }

    @Override
    public Thread newThread(Runnable r) {
        String threadName = String.format("IPFIX-Worker-%02d", m_threadNum++);
        return new Thread(m_threadGroup, r, threadName);
    }

    @Override
    public List<IpfixDataHandler> getDataHandlers() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

