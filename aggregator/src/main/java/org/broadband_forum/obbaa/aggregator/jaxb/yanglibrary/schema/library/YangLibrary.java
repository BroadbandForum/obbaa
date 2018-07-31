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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "yang-library", namespace = "urn:ietf:params:xml:ns:yang:ietf-yang-library")
public class YangLibrary {
    private Modules modules;
    private ModuleSets moduleSets;
    private DataStores dataStores;
    private String checkSum;

    @XmlElement(name = "modules")
    public Modules getModules() {
        return modules;
    }

    public void setModules(Modules modules) {
        this.modules = modules;
    }

    @XmlElement(name = "module-sets")
    public ModuleSets getModuleSets() {
        return moduleSets;
    }

    public void setModuleSets(ModuleSets moduleSets) {
        this.moduleSets = moduleSets;
    }

    @XmlElement(name = "datastores")
    public DataStores getDataStores() {
        return dataStores;
    }

    public void setDataStores(DataStores dataStores) {
        this.dataStores = dataStores;
    }

    @XmlElement(name = "checksum")
    public String getCheckSum() {
        return checkSum;
    }

    public void setCheckSum(String checkSum) {
        this.checkSum = checkSum;
    }
}
