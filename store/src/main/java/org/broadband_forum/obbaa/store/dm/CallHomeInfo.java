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

package org.broadband_forum.obbaa.store.dm;

import java.io.Serializable;
import java.util.Objects;

public class CallHomeInfo implements Serializable {
    private String m_duid;

    public CallHomeInfo() {
    }

    public CallHomeInfo(String duid) {
        m_duid = duid;
    }

    public String getDuid() {
        return m_duid;
    }

    public void setDuid(String duid) {
        m_duid = duid;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        CallHomeInfo that = (CallHomeInfo) other;
        return Objects.equals(m_duid, that.m_duid);
    }

    @Override
    public int hashCode() {

        return Objects.hash(m_duid);
    }

    @Override
    public String toString() {
        return "CallHomeInfo{"
                + "m_duid='" + m_duid + '\''
                + '}';
    }
}
