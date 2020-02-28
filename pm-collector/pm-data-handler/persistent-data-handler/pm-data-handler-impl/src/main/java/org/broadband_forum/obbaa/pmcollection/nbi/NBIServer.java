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

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.pmcollection.pmdatahandler.PMDataHandler;
import org.broadband_forum.obbaa.pmcollection.pmdatahandler.TSData;
import org.broadband_forum.obbaa.pmcollection.pmdatahandler.TSData.TSPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

/**
 * NBI server supporting queries and query streams.
 */
public class NBIServer {

    private static final Logger LOG
        = LoggerFactory.getLogger(NBIServer.class);

    private Server m_server;
    private final PMDataHandler m_pmDataHandler;

    public NBIServer(PMDataHandler pmDataHandler) {
        m_pmDataHandler = pmDataHandler;
    }

    public boolean start() {
        /* get port number from config */
        int port;
        String envVar = System.getenv("PMD_NBI_PORT");
        if (envVar == null || envVar.isEmpty()) {
            LOG.error("Environment variable PMD_NBI_PORT not set.");
            return false;
        }
        try {
            port = Integer.parseInt(envVar);
        }
        catch (NumberFormatException e) {
            LOG.error("Value '{}' of PMD_MAX_BUFFERED_POINTS is not an integer number", envVar);
            return false;
        }

        // build server
        m_server = ServerBuilder.forPort(port)
            .addService(new PMDataRequestImpl(m_pmDataHandler))
            .addService(new PMDataStreamImpl(m_pmDataHandler))
            .build();

        // start server
        try {
            m_server.start();
        }
        catch (IOException ex) {
            LOG.error("Caught exception: {}", ex.getMessage());
            return false;
        }
        LOG.info("NBIServer started. Listening on port {}", port);

        return true;
    }

    public void stop() {
        if (m_server != null) {
            m_server.shutdown();
            try {
                m_server.awaitTermination();
            }
            catch (InterruptedException ex) {
                LOG.debug("Caught exception: ", ex);
            }
            m_server = null;
            LOG.info("NBIServer stopped.");
        }
    }

    private static Map<String, List<String>> convertFilter(Filter filter) {
        Map<Integer, FilterValues> map = filter.getFilterExpressionsMap();
        Map<String, List<String>> internalFilter = new HashMap<>();
        for (Integer key : map.keySet()) {
            FilterValues filterValues = map.get(key);
            List<String> valu =
                new ArrayList<>(filterValues.getFilterItemValuesCount());
            for (String filterValue : filterValues.getFilterItemValuesList()) {
                valu.add(filterValue);
            }
            internalFilter.put(TSPoint.TAG_NAMES.get(key), valu);
        }
        return internalFilter;
    }

    static class PMDataRequestImpl extends PMDataRequestGrpc.PMDataRequestImplBase {

        private final PMDataHandler m_pmDataHandler;

        PMDataRequestImpl(PMDataHandler pmDataHandler) {
            m_pmDataHandler = pmDataHandler;
        }

        @Override
        public void query(QueryRequest req,
                StreamObserver<QueryReply> responseObserver) {
            if (m_pmDataHandler == null) { // stand alone nbi test mode
                LOG.info("measurement: {}", req.getMeasurement());
                LOG.info("filter: \"{}\"", req.getFilter());

                TimeSeriesPoint ts = TimeSeriesPoint.newBuilder()
                    .setMeasurement(req.getMeasurement())
                    .setTimestamp((int) (System.currentTimeMillis() / 1000))
                    .putTags("sourceIP", "192.168.1.1")
                    .putTags("hostName", "dpu1")
                    .putTags("deviceAdapter", "sample-DPU-modeltls-1.0")
                    .putTags("templateID", "1")
                    .putTags("observationDomain", "2")
                    .putFields(
                        "/if:interfaces-state/if:interface/if:statistics/if:in-errors",
                        ValueUnion.newBuilder().setLvalue(0).build())
                    .putFields(
                        "/if:interfaces-state/if:interface/if:statistics/if:in-discards",
                        ValueUnion.newBuilder().setLvalue(1).build())
                    .putFields(
                        "/if:interfaces-state/if:interface/if:statistics/if:out-errors",
                        ValueUnion.newBuilder().setLvalue(2).build())
                    .putFields(
                        "/if:interfaces-state/if:interface/if:statistics/if:out-discards",
                        ValueUnion.newBuilder().setLvalue(3).build())
                    .putFields(
                        "/if:interfaces-state/if:interface/if:name",
                        ValueUnion.newBuilder().setSvalue("DSL1").build())
                    .build();
                QueryReply reply = QueryReply.newBuilder().addTsPoints(ts).build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
                LOG.info("query done.");
                return;
            }

            LOG.debug("measurement: {}", req.getMeasurement());
            LOG.debug("filter: \"{}\"", req.getFilter());

            Instant startTime = Instant.ofEpochSecond(req.getStartTime());
            Instant stopTime = Instant.ofEpochSecond(req.getStopTime());
            List<TSData> tsDataList = m_pmDataHandler.query(req.getMeasurement(),
                    startTime, stopTime, convertFilter(req.getFilter()));

            QueryReply.Builder builder = QueryReply.newBuilder();
            for (TSData tsData : tsDataList) {
                for (TSPoint point : tsData.m_tsPoints) {
                    TimeSeriesPoint.Builder tsBuilder = TimeSeriesPoint.newBuilder()
                        .setMeasurement(point.getMeasurementName())
                        .setTimestamp(point.getTimestamp());
                    for (String tag : point.getTags().keySet()) {
                        tsBuilder.putTags(tag, point.getTags().get(tag));
                    }
                    for (String field : point.getFields().keySet()) {
                        Object value = point.getFields().get(field);
                        if (value instanceof Long) {
                            tsBuilder
                                .putFields(field, ValueUnion.newBuilder()
                                .setLvalue((Long) value)
                                .build());
                        } else if (value instanceof Double) {
                            tsBuilder
                                .putFields(field, ValueUnion.newBuilder()
                                .setDvalue((Double) value)
                                .build());
                        } else if (value instanceof String) {
                            tsBuilder
                                .putFields(field, ValueUnion.newBuilder()
                                .setSvalue((String) value)
                                .build());
                        } else if (value instanceof Boolean) {
                            tsBuilder
                                .putFields(field, ValueUnion.newBuilder()
                                .setBvalue((Boolean) value)
                                .build());
                        }
                    }
                    builder.addTsPoints(tsBuilder.build());
                }
            }

            QueryReply reply = builder.build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
            LOG.info("query done.");
        }
    }

    static class PMDataStreamImpl extends PMDataStreamGrpc.PMDataStreamImplBase {

        private final PMDataHandler m_pmDataHandler;

        PMDataStreamImpl(PMDataHandler pmDataHandler) {
            m_pmDataHandler = pmDataHandler;
        }

        @Override
        public void queryStream(QueryRequest req,
                StreamObserver<QueryReply> responseObserver) {
            if (m_pmDataHandler == null) { // stand alone nbi test mode
                LOG.info("measurement: {}", req.getMeasurement());
                LOG.info("filter: \"{}\"", req.getFilter());

                TimeSeriesPoint ts = TimeSeriesPoint.newBuilder()
                    .setMeasurement("1")
                    .setTimestamp((int) (System.currentTimeMillis() / 1000))
                    .putTags("sourceIP", "192.168.1.1")
                    .putTags("hostName", "dpu1")
                    .putTags("deviceAdapter", "sample-DPU-modeltls-1.0")
                    .putTags("templateID", "1")
                    .putTags("observationDomain", "2")
                    .putFields(
                        "/if:interfaces-state/if:interface/if:statistics/if:in-errors",
                        ValueUnion.newBuilder().setLvalue(0).build())
                    .putFields(
                        "/if:interfaces-state/if:interface/if:statistics/if:in-discards",
                        ValueUnion.newBuilder().setLvalue(1).build())
                    .putFields(
                        "/if:interfaces-state/if:interface/if:statistics/if:out-errors",
                        ValueUnion.newBuilder().setLvalue(2).build())
                    .putFields(
                        "/if:interfaces-state/if:interface/if:statistics/if:out-discards",
                        ValueUnion.newBuilder().setLvalue(3).build())
                    .putFields(
                        "/if:interfaces-state/if:interface/if:name",
                        ValueUnion.newBuilder().setSvalue("DSL1").build())
                    .build();
                QueryReply reply = QueryReply.newBuilder().addTsPoints(ts).build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
                LOG.info("query done.");
                return;
            }

            LOG.debug("measurement: {}", req.getMeasurement());
            LOG.debug("filter: \"{}\"", req.getFilter());

            Instant startTime = Instant.ofEpochSecond(req.getStartTime());
            Instant stopTime = Instant.ofEpochSecond(req.getStopTime());
            List<TSData> tsDataList = m_pmDataHandler.query(req.getMeasurement(),
                startTime, stopTime, convertFilter(req.getFilter()));

            QueryReply.Builder builder = QueryReply.newBuilder();
            for (TSData tsData : tsDataList) {
                for (TSPoint point : tsData.m_tsPoints) {
                    TimeSeriesPoint.Builder tsBuilder = TimeSeriesPoint.newBuilder()
                        .setMeasurement(point.getMeasurementName())
                        .setTimestamp(point.getTimestamp());
                    for (String tag : point.getTags().keySet()) {
                        tsBuilder.putTags(tag, point.getTags().get(tag));
                    }
                    for (String field : point.getFields().keySet()) {
                        Object value = point.getFields().get(field);
                        if (value instanceof Long) {
                            tsBuilder
                                .putFields(field, ValueUnion.newBuilder()
                                .setLvalue((Long) value)
                                .build());
                        } else if (value instanceof Double) {
                            tsBuilder
                                .putFields(field, ValueUnion.newBuilder()
                                .setDvalue((Double) value)
                                .build());
                        } else if (value instanceof String) {
                            tsBuilder
                                .putFields(field, ValueUnion.newBuilder()
                                .setSvalue((String) value)
                                .build());
                        } else if (value instanceof Boolean) {
                            tsBuilder
                                .putFields(field, ValueUnion.newBuilder()
                                .setBvalue((Boolean) value)
                                .build());
                        }
                    }
                    builder.addTsPoints(tsBuilder.build());
                }
            }

            QueryReply reply = builder.build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }
}
