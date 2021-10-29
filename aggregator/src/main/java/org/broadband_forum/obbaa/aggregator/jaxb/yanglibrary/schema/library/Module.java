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

package org.broadband_forum.obbaa.aggregator.jaxb.yanglibrary.schema.library;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "module", namespace = "urn:ietf:params:xml:ns:yang:ietf-yang-library")
public class Module {
    private String id;
    private String name;
    private String revision;
    private String schema;
    private String namespace;
    private List<String> feature;
    private Deviation deviation;
    private String conformanceType;
    private SubModule subModule;

    @XmlElement(name = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlElement(name = "name", namespace = "urn:ietf:params:xml:ns:yang:ietf-yang-library")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(name = "revision", namespace = "urn:ietf:params:xml:ns:yang:ietf-yang-library")
    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    @XmlElement(name = "schema", namespace = "urn:ietf:params:xml:ns:yang:ietf-yang-library")
    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    @XmlElement(name = "namespace", namespace = "urn:ietf:params:xml:ns:yang:ietf-yang-library")
    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @XmlElement(name = "feature", namespace = "urn:ietf:params:xml:ns:yang:ietf-yang-library")
    public List<String> getFeature() {
        return feature;
    }

    public void setFeature(List<String> feature) {
        this.feature = feature;
    }

    @XmlElement(name = "deviation", namespace = "urn:ietf:params:xml:ns:yang:ietf-yang-library")
    public Deviation getDeviation() {
        return deviation;
    }

    public void setDeviation(Deviation deviation) {
        this.deviation = deviation;
    }

    @XmlElement(name = "conformance-type", namespace = "urn:ietf:params:xml:ns:yang:ietf-yang-library")
    public String getConformanceType() {
        return conformanceType;
    }

    public void setConformanceType(String conformanceType) {
        this.conformanceType = conformanceType;
    }

    @XmlElement(name = "submodule", namespace = "urn:ietf:params:xml:ns:yang:ietf-yang-library")
    public SubModule getSubModule() {
        return subModule;
    }

    public void setSubModule(SubModule subModule) {
        this.subModule = subModule;
    }
}
