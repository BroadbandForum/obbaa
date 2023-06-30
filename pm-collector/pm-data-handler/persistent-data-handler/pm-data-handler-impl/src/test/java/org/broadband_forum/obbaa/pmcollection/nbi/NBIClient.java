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

package org.broadband_forum.obbaa.pmcollection.nbi;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

/**
 * A simple client retrieving PM/IPFIX data.
 */
public class NBIClient {

    private static final Logger LOG
            = LoggerFactory.getLogger(NBIClient.class);

    private final ManagedChannel m_channel;
    private final PMDataRequestGrpc.PMDataRequestBlockingStub m_blockingStub;
    private final PMDataStreamGrpc.PMDataStreamBlockingStub m_streamBlockingStub;

    /**
     * Construct client connecting to HelloWorld server at {@code host:port}.
     *
     * @param host
     * @param port
     */
    public NBIClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                // Channels are secure by default (via SSL/TLS). For now we
                // disable TLS to avoid needing certificates.
                .usePlaintext()
                .build());
    }

    /**
     * Construct client for accessing PMCollector server using the existing
     * channel.
     *
     * @param channel
     */
    NBIClient(ManagedChannel channel) {
        m_channel = channel;
        m_blockingStub = PMDataRequestGrpc.newBlockingStub(channel);
        m_streamBlockingStub = PMDataStreamGrpc.newBlockingStub(channel);
    }

    /**
     * main.
     *
     * @param args
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        // Access service running on local machine port 5051
        NBIClient client = new NBIClient("localhost", 5051);
        try {
            String measurement = "267";
            if (args.length > 0) {
                measurement = args[0]; // Use the arg as the measurement name to query
            }
            Instant startTime = Instant.now().minus(10, ChronoUnit.MINUTES);
            Instant stopTime = Instant.now();
            client.query(measurement, startTime, stopTime, null);
        } finally {
            client.shutdown();
        }
    }

    public void shutdown() throws InterruptedException {
        m_channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    private Filter _createFilter(Map<Integer, List<String>> filter) {
        if (filter == null || filter.isEmpty()) {
            return null;
        }
        Filter.Builder b = Filter.newBuilder();
        for (Integer key : filter.keySet()) {
            if (key < 0 || key > 4) {
                LOG.error("Wrong key '{}' in filter expression. Ignored!", key);
                continue;
            }
            FilterValues.Builder fb = FilterValues.newBuilder();
            for (String val : filter.get(key)) {
                fb.addFilterItemValues(val);
            }

            b.putFilterExpressions(key, fb.build());
        }
        return b.build();
    }

    /**
     * Request PM Data.
     *
     * @param measurement measurement name
     * @param startTime
     * @param stopTime
     * @param filter
     */
    public void query(String measurement, Instant startTime, Instant stopTime,
                      Map<Integer, List<String>> filter) {
        int records = 0;
        int queryReplies = 0;
        LOG.info("Will try to query measurement {}...", measurement);
        QueryRequest.Builder b = QueryRequest.newBuilder()
                .setMeasurement(measurement)
                .setStartTime((int) startTime.getEpochSecond())
                .setStopTime((int) stopTime.getEpochSecond());
        if (filter != null && !filter.isEmpty()) {
            b.setFilter(_createFilter(filter));
        }
        QueryRequest request = b.build();
        Iterator<QueryReply> response;
        try {
            // call server
            response = m_blockingStub.query(request);

            // show result
            while (response.hasNext()) {
                QueryReply reply = response.next();
                for (TimeSeriesPoint ts : reply.getTsPointsList()) {
                    LOG.info("measurement: {} timestamp: {}",
                            ts.getMeasurement(), ts.getTimestamp());
                    ts.getTagsMap().keySet().forEach((key) -> {
                        LOG.info("tag key: {}, tag value: {}",
                                key, ts.getTagsMap().get(key));
                    });
                    ts.getFieldsMap().keySet().forEach((key) -> {
                        ValueUnion v = ts.getFieldsMap().get(key);
                        switch (v.getValueCase()) {
                            case LVALUE:
                                LOG.info("field key: {}, field value: {}",
                                        key, ts.getFieldsMap().get(key).getLvalue());
                                break;
                            case DVALUE:
                                LOG.info("field key: {}, field value: {}",
                                        key, ts.getFieldsMap().get(key).getDvalue());
                                break;
                            case SVALUE:
                                LOG.info("field key: {}, field value: {}",
                                        key, ts.getFieldsMap().get(key).getSvalue());
                                break;
                            case BVALUE:
                                LOG.info("field key: {}, field value: {}",
                                        key, ts.getFieldsMap().get(key).getBvalue());
                                break;
                        }
                    });
                    records++;
                }
                queryReplies++;
            }
        } catch (StatusRuntimeException e) {
            LOG.warn("RPC failed: {}", e.getStatus());
            return;
        }
        LOG.info("got {} queryReplies and {} records.", queryReplies, records);
    }

    /**
     * Request PM Data.
     *
     * @param measurement measurement name
     * @param startTime
     * @param stopTime
     * @param filter
     */
    public void queryStream(String measurement, Instant startTime,
                            Instant stopTime, Map<Integer, List<String>> filter) {
        int records = 0;
        int queryReplies = 0;
        LOG.info("Will try to query measurement {}...", measurement);

        QueryRequest.Builder b = QueryRequest.newBuilder()
                .setMeasurement(measurement)
                .setStartTime((int) startTime.getEpochSecond())
                .setStopTime((int) stopTime.getEpochSecond());
        if (filter != null && !filter.isEmpty()) {
            b.setFilter(_createFilter(filter));
        }
        QueryRequest request = b.build();

        Iterator<QueryReply> response;
        try {
            // call server
            response = m_streamBlockingStub.queryStream(request);

            // show result
            while (response.hasNext()) {
                QueryReply reply = response.next();
                for (TimeSeriesPoint ts : reply.getTsPointsList()) {
                    LOG.info("measurement: {} timestamp: {}",
                            ts.getMeasurement(), ts.getTimestamp());
                    ts.getTagsMap().keySet().forEach((key) -> {
                        LOG.info("tag key: {}, tag value: {}",
                                key, ts.getTagsMap().get(key));
                    });
                    ts.getFieldsMap().keySet().forEach((key) -> {
                        ValueUnion v = ts.getFieldsMap().get(key);
                        switch (v.getValueCase()) {
                            case LVALUE:
                                LOG.info("field key: {}, field value: {}",
                                        key, ts.getFieldsMap().get(key).getLvalue());
                                break;
                            case DVALUE:
                                LOG.info("field key: {}, field value: {}",
                                        key, ts.getFieldsMap().get(key).getDvalue());
                                break;
                            case SVALUE:
                                LOG.info("field key: {}, field value: {}",
                                        key, ts.getFieldsMap().get(key).getSvalue());
                                break;
                            case BVALUE:
                                LOG.info("field key: {}, field value: {}",
                                        key, ts.getFieldsMap().get(key).getBvalue());
                                break;
                        }
                    });
                    records++;
                }
                queryReplies++;
            }
        } catch (StatusRuntimeException e) {
            LOG.warn("RPC failed: {}", e.getStatus());
            return;
        }
        LOG.info("got {} queryReplies and {} records.", queryReplies, records);
    }
}
