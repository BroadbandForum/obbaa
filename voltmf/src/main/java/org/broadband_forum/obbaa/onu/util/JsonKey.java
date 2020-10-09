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

package org.broadband_forum.obbaa.onu.util;

import org.json.JSONObject;

/**
 * <p>
 * Utility to convert to JSON objects
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 22/07/2020.
 */
public class JsonKey implements Comparable<JsonKey> {

    private Object m_jsonValue;
    private String m_jsonKey;

    public JsonKey(String jsonKey, Object jsonValue) {
        this.m_jsonValue = jsonValue;
        this.m_jsonKey = jsonKey;
    }

    public Object getJsonValue() {
        return m_jsonValue;
    }

    public String getJsonKey() {
        return m_jsonKey;
    }

    @Override
    public int compareTo(JsonKey other) {
        if (!(m_jsonValue instanceof JSONObject) && !(other.getJsonValue() instanceof JSONObject)) {
            return 0;
        } else if (m_jsonValue instanceof JSONObject && other.getJsonValue() instanceof JSONObject) {
            return 0;
        } else if (m_jsonValue instanceof JSONObject) {
            return 1;
        }
        return -1;
    }

    @Override
    public String toString() {
        return "JsonKey [jsonKey=" + m_jsonValue + "]";
    }
}
