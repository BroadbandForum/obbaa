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

package org.broadband_forum.obbaa.ipfix.collector.entities.ie;

public class InformationElement {
    private int m_elementId;
    private String m_name;
    private InformationElementType m_dataType;
    private String m_dataTypeSemantics;
    private String m_status;
    private String m_description;
    private String m_units;
    private String m_range;
    private String m_references;
    private String m_requester;
    private String m_revision;
    private String m_date;

    public int getElementId() {
        return m_elementId;
    }

    public void setElementId(int elementId) {
        m_elementId = elementId;
    }

    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    public InformationElementType getDataType() {
        return m_dataType;
    }

    public void setDataType(InformationElementType dataType) {
        m_dataType = dataType;
    }

    public String getDataTypeSemantics() {
        return m_dataTypeSemantics;
    }

    public void setDataTypeSemantics(String dataTypeSemantics) {
        m_dataTypeSemantics = dataTypeSemantics;
    }

    public String getStatus() {
        return m_status;
    }

    public void setStatus(String status) {
        m_status = status;
    }

    public String getDescription() {
        return m_description;
    }

    public void setDescription(String description) {
        m_description = description;
    }

    public String getUnits() {
        return m_units;
    }

    public void setUnits(String units) {
        m_units = units;
    }

    public String getRange() {
        return m_range;
    }

    public void setRange(String range) {
        m_range = range;
    }

    public String getReferences() {
        return m_references;
    }

    public void setReferences(String references) {
        m_references = references;
    }

    public String getRequester() {
        return m_requester;
    }

    public void setRequester(String requester) {
        m_requester = requester;
    }

    public String getRevision() {
        return m_revision;
    }

    public void setRevision(String revision) {
        m_revision = revision;
    }

    public String getDate() {
        return m_date;
    }

    public void setDate(String date) {
        m_date = date;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_dataType == null) ? 0 : m_dataType.hashCode());
        result = prime * result + ((m_dataTypeSemantics == null) ? 0 : m_dataTypeSemantics.hashCode());
        result = prime * result + ((m_date == null) ? 0 : m_date.hashCode());
        result = prime * result + ((m_description == null) ? 0 : m_description.hashCode());
        result = prime * result + m_elementId;
        result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
        result = prime * result + ((m_range == null) ? 0 : m_range.hashCode());
        result = prime * result + ((m_references == null) ? 0 : m_references.hashCode());
        result = prime * result + ((m_requester == null) ? 0 : m_requester.hashCode());
        result = prime * result + ((m_revision == null) ? 0 : m_revision.hashCode());
        result = prime * result + ((m_status == null) ? 0 : m_status.hashCode());
        result = prime * result + ((m_units == null) ? 0 : m_units.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        InformationElement other = (InformationElement) obj;
        if (m_dataType != other.m_dataType) {
            return false;
        }
        if (m_dataTypeSemantics == null) {
            if (other.m_dataTypeSemantics != null) {
                return false;
            }
        } else if (!m_dataTypeSemantics.equals(other.m_dataTypeSemantics)) {
            return false;
        }
        if (m_date == null) {
            if (other.m_date != null) {
                return false;
            }
        } else if (!m_date.equals(other.m_date)) {
            return false;
        }
        if (m_description == null) {
            if (other.m_description != null) {
                return false;
            }
        } else if (!m_description.equals(other.m_description)) {
            return false;
        }
        if (m_elementId != other.m_elementId) {
            return false;
        }
        if (m_name == null) {
            if (other.m_name != null) {
                return false;
            }
        } else if (!m_name.equals(other.m_name)) {
            return false;
        }
        if (m_range == null) {
            if (other.m_range != null) {
                return false;
            }
        } else if (!m_range.equals(other.m_range)) {
            return false;
        }
        if (m_references == null) {
            if (other.m_references != null) {
                return false;
            }
        } else if (!m_references.equals(other.m_references)) {
            return false;
        }
        if (m_requester == null) {
            if (other.m_requester != null) {
                return false;
            }
        } else if (!m_requester.equals(other.m_requester)) {
            return false;
        }
        if (m_revision == null) {
            if (other.m_revision != null) {
                return false;
            }
        } else if (!m_revision.equals(other.m_revision)) {
            return false;
        }
        if (m_status == null) {
            if (other.m_status != null) {
                return false;
            }
        } else if (!m_status.equals(other.m_status)) {
            if (m_units == null) {
                if (other.m_units != null) {
                    return false;
                }
            } else if (!m_units.equals(other.m_units)) {
                return false;
            }
        }
        return true;
    }

}
