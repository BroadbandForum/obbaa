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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class holding the data of a time series measurement.
 */
public class TSData {
    /**
     * Type of measurement data.
     */
    public enum MetricType {
        T_long("unsigned64"), T_double("float64"),
        T_string("string"), T_boolean("boolean");

        public String m_typeName;

        MetricType(String typeName) {
            this.m_typeName = typeName;
        }
    }

    public static class TSPoint {
        public static final ArrayList<String> TAG_NAMES = new ArrayList<>(5);

        static { // should be in lexicographic order
            TAG_NAMES.add("deviceAdapter");
            TAG_NAMES.add("hostName");
            TAG_NAMES.add("observationDomain");
            TAG_NAMES.add("sourceIP");
            TAG_NAMES.add("templateID");
        }

        public String m_measurement;
        public int m_timestamp;
        public Map<String, String> m_tags;
        public Map<String, Object> m_fields;

        public TSPoint() {
            m_tags = new HashMap<>(TAG_NAMES.size());
        }

        public String getMeasurementName() {
            return m_measurement;
        }

        public int getTimestamp() {
            return m_timestamp;
        }

        public Map<String, String> getTags() {
            return m_tags;
        }

        public Map<String, Object> getFields() {
            return m_fields;
        }
    }

    public static final Map<String, MetricType> IPFIXTYPETODBTYPE;

    /* from
     * https://www.iana.org/assignments/ipfix/ipfix.xhtml#ipfix-information-elements
     */
    static {
        IPFIXTYPETODBTYPE = new HashMap<>();
        IPFIXTYPETODBTYPE.put("octetArray", MetricType.T_string);
        IPFIXTYPETODBTYPE.put("unsigned8", MetricType.T_long);
        IPFIXTYPETODBTYPE.put("unsigned16", MetricType.T_long);
        IPFIXTYPETODBTYPE.put("unsigned32", MetricType.T_long);
        IPFIXTYPETODBTYPE.put("unsigned64", MetricType.T_long);
        IPFIXTYPETODBTYPE.put("signed8", MetricType.T_long);
        IPFIXTYPETODBTYPE.put("signed16", MetricType.T_long);
        IPFIXTYPETODBTYPE.put("signed32", MetricType.T_long);
        IPFIXTYPETODBTYPE.put("signed64", MetricType.T_long);
        IPFIXTYPETODBTYPE.put("float32", MetricType.T_double);
        IPFIXTYPETODBTYPE.put("float64", MetricType.T_double);
        IPFIXTYPETODBTYPE.put("boolean", MetricType.T_boolean);
        IPFIXTYPETODBTYPE.put("macAddress", MetricType.T_string);
        IPFIXTYPETODBTYPE.put("string", MetricType.T_string);
        IPFIXTYPETODBTYPE.put("dateTimeSeconds", MetricType.T_long);
        IPFIXTYPETODBTYPE.put("dateTimeMilliseconds", MetricType.T_long);
        IPFIXTYPETODBTYPE.put("dateTimeMicroseconds", MetricType.T_long);
        IPFIXTYPETODBTYPE.put("dateTimeNanoseconds", MetricType.T_long);
        IPFIXTYPETODBTYPE.put("ipv4Address", MetricType.T_string);
        IPFIXTYPETODBTYPE.put("ipv6Address", MetricType.T_string);
        IPFIXTYPETODBTYPE.put("basicList", MetricType.T_string);
        IPFIXTYPETODBTYPE.put("subTemplateList", MetricType.T_string);
        IPFIXTYPETODBTYPE.put("subTemplateMultiList", MetricType.T_string);
    }

    public final ArrayList<TSPoint> m_tsPoints;

    public TSData() {
        m_tsPoints = new ArrayList<>();
    }

    public List<TSPoint> getTSPoints() {
        return m_tsPoints;
    }
}
