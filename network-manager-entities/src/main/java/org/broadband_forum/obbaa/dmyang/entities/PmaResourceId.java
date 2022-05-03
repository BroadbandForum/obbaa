/*
 * Copyright 2022 Broadband Forum
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

package org.broadband_forum.obbaa.dmyang.entities;

import java.util.Objects;

/**
 * <p>
 * Class to pass around resource ids in the PMA hierarchy
 * </p>
 * Created by J.V.Correia (Altice Labs) on 26/01/2022.
 */

public class PmaResourceId {
    private Type m_resourceType;
    private String m_resourceName;

    public enum Type {
        DEVICE,
        NETWORK_FUNCTION
    }

    public PmaResourceId(Type type, String name) {
        m_resourceType = type;
        m_resourceName = name;
    }

    public Type getResourceType() {
        return m_resourceType;
    }

    public String getResourceName() {
        return m_resourceName;
    }

    @Override
    public String toString() {
        return "PmaResourceId{"
                + "m_resourceType=" + m_resourceType
                + ", m_resourceName='" + m_resourceName + '\'' + '}';
    }

    @Override
    public boolean equals(Object resourceId) {
        if (this == resourceId) {
            return true;
        }
        if (resourceId == null || getClass() != resourceId.getClass()) {
            return false;
        }
        PmaResourceId that = (PmaResourceId) resourceId;
        return m_resourceType == that.m_resourceType && Objects.equals(m_resourceName, that.m_resourceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_resourceType, m_resourceName);
    }
}


