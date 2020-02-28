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

package org.broadband_forum.obbaa.ipfix.collector.entities.logging;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class IpfixDecodedData {

    @Expose
    @SerializedName("metric")
    private final String m_counterName;
    @Expose
    @SerializedName("dataType")
    private final String m_dataType;

    @Expose
    @SerializedName("value")
    private final String m_decodedValue;

    public IpfixDecodedData(String counterName, String dataType, String decodedValue) {
        this.m_counterName = counterName;
        this.m_dataType = dataType;
        this.m_decodedValue = decodedValue;
    }

    public String getCounterName() {
        return m_counterName;
    }

    public String getDataType() {
        return m_dataType;
    }

    public String getDecodedValue() {
        return m_decodedValue;
    }
}
