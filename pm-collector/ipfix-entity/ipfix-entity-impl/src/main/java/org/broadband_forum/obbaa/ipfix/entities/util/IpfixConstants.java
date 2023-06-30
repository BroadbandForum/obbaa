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

package org.broadband_forum.obbaa.ipfix.entities.util;

import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

public class IpfixConstants {

    public static final String SLASH = "/";
    public static final int FE_PORT;
    public static final int IPFIX_WORKER_GROUP;
    public static final long FE_MAX_CONNECTION;
    public static final int FE_DATA_SET_BUFFER_TIME;
    public static final String FE_IE_MAPPING_DIR;
    public static final String IEID_CSV_FILE;
    public static final String HOSTNAME_IE_KEY;
    public static final String BBF = "BBF";
    public static final String STANDARD = "standard";
    private static final String IPFIX_COLLECTOR_PORT = "IPFIX_COLLECTOR_PORT";
    private static final String IPFIX_COLLECTOR_WORKER_GROUP = "IPFIX_COLLECTOR_WORKER_GROUP";
    private static final String IPFIX_HOSTNAME_IE_KEY = "IPFIX_HOSTNAME_IE_KEY";
    private static final String IE_MAPPING_DIR = "IPFIX_IE_MAPPING_DIR";
    private static final String IEID_CSV_FILE_NAME = "IEID_CSV_FILE_NAME";
    private static final String IPFIX_COLLECTOR_MAX_CONNECTION = "IPFIX_COLLECTOR_MAX_CONNECTION";
    private static final String DATA_SET_BUFFER_TIME = "DATA_SET_BUFFER_TIME";
    private static final String IPFIX_COLLECTOR_TEMPLATE_RECORD_CACHE_SIZE = "IPFIX_COLLECTOR_TEMPLATE_RECORD_CACHE_SIZE";
    private static final int DEFAULT_IPFIX_COLLECTOR_TEMPLATE_RECORD_CACHE_SIZE = 1_000_000;
    private static final String IPFIX_COLLECTOR_DATA_SET_CACHE_SIZE = "IPFIX_COLLECTOR_DATA_SET_CACHE_SIZE";
    private static final int DEFAULT_IPFIX_COLLECTOR_DATA_SET_CACHE_SIZE = 10_000;
    private static final String IPFIX_COLLECTOR_TEMPLATE_RECORD_CACHE_EXPIRATION = "IPFIX_COLLECTOR_TEMPLATE_RECORD_CACHE_EXPIRATION";
    private static final int DEFAULT_IPFIX_COLLECTOR_TEMPLATE_RECORD_CACHE_EXPIRATION = 25;
    private static final String DEFAULT_HOSTNAME_IE_KEY = "/sys:system/sys:hostname";
    private static final String DEFAULT_IE_MAPPING_DIR = "/ipfix/ie-mapping";
    private static final String DEFAULT_IEID_CSV_FILE_NAME = "IPFIX_IEId.csv";
    private static final int DEFAULT_DATA_SET_BUFFER_TIME = 30 * 60; // seconds
    private static final int DEFAULT_COLLECTOR_PORT_VALUE = 4494;
    private static final long DEFAULT_MAX_CONNECTION_VALUE = 30000;
    private static final int DEFAULT_IPFIX_COLLECTOR_WORKER_GROUP = 20;
    private static final CharSequence KEY_CONCAT = "_";
    // Socket Timeout
    private static final String IPFIX_COLLECTOR_TCP_CONNECTION_TIMEOUT = "IPFIX_COLLECTOR_TCP_CONNECTION_TIMEOUT";
    private static final int DEFAULT_IPFIX_COLLECTOR_TCP_CONNECTION_TIMEOUT = (int) TimeUnit.HOURS.toMinutes(1); // minutes

    static {
        FE_PORT = getIpfixCollectorPort();
        IPFIX_WORKER_GROUP = getIpfixCollectorWorkerGroup();
        FE_MAX_CONNECTION = getMaxConnection();
        FE_DATA_SET_BUFFER_TIME = getDataSetBufferTime();
        FE_IE_MAPPING_DIR = getIeMappingDir();
        HOSTNAME_IE_KEY = getHostNameIeKey();
        IEID_CSV_FILE = getIEIdCsvFileName();
    }

    private static String getHostNameIeKey() {
        String value = System.getenv(IPFIX_HOSTNAME_IE_KEY);
        if (value == null || value.isEmpty()) {
            return DEFAULT_HOSTNAME_IE_KEY;
        }
        return value;
    }

    private static String getIeMappingDir() {
        String value = System.getenv(IE_MAPPING_DIR);
        if (value == null || value.isEmpty()) {
            return DEFAULT_IE_MAPPING_DIR;
        }
        return value;
    }

    private static String getIEIdCsvFileName() {
        String value = System.getenv(IEID_CSV_FILE_NAME);
        if (value == null || value.isEmpty()) {
            return DEFAULT_IEID_CSV_FILE_NAME;
        }
        return value;
    }

    private static int getDataSetBufferTime() {
        String value = System.getenv(DATA_SET_BUFFER_TIME);
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return DEFAULT_DATA_SET_BUFFER_TIME;
        }
    }

    private static int getIpfixCollectorPort() {
        String value = System.getenv(IPFIX_COLLECTOR_PORT);
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return DEFAULT_COLLECTOR_PORT_VALUE;
        }
    }

    private static long getMaxConnection() {
        String value = System.getenv(IPFIX_COLLECTOR_MAX_CONNECTION);
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return DEFAULT_MAX_CONNECTION_VALUE;
        }
    }

    private static int getIpfixCollectorWorkerGroup() {
        String value = System.getenv(IPFIX_COLLECTOR_WORKER_GROUP);
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return DEFAULT_IPFIX_COLLECTOR_WORKER_GROUP;
        }
    }

    public static int getTemplateRecordCacheSize() {
        String value = System.getenv(IPFIX_COLLECTOR_TEMPLATE_RECORD_CACHE_SIZE);
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return DEFAULT_IPFIX_COLLECTOR_TEMPLATE_RECORD_CACHE_SIZE;
        }
    }

    public static int getTemplateRecordCacheExpiration() {
        String value = System.getenv(IPFIX_COLLECTOR_TEMPLATE_RECORD_CACHE_EXPIRATION);
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return DEFAULT_IPFIX_COLLECTOR_TEMPLATE_RECORD_CACHE_EXPIRATION;
        }
    }

    public static int getDataSetCacheSize() {
        String value = System.getenv(IPFIX_COLLECTOR_DATA_SET_CACHE_SIZE);
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return DEFAULT_IPFIX_COLLECTOR_DATA_SET_CACHE_SIZE;
        }
    }

    public static String buildCacheKey(Object... args) {
        StringJoiner sb = new StringJoiner(KEY_CONCAT);
        for (Object arg : args) {
            sb.add(String.valueOf(arg));
        }
        return sb.toString();
    }

    public static int getIntegerValueFromEnv(String envName, int defaultValue) {
        String value = System.getenv(envName);
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static int getTcpConnectionTimeout() {
        return getIntegerValueFromEnv(IPFIX_COLLECTOR_TCP_CONNECTION_TIMEOUT, DEFAULT_IPFIX_COLLECTOR_TCP_CONNECTION_TIMEOUT);
    }

}
