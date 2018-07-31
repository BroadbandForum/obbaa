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

package org.broadband_forum.obbaa.aggregator.jaxb.netconf.schema.rpc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class ConfigBase {
    private Target target;
    private Config config;
    private String defaultOperation;
    private String testOption;
    private String errorOption;

    @XmlElement(name = "target")
    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    @XmlElement(name = "config")
    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    @XmlElement(name = "default-operation")
    public String getDefaultOperation() {
        return defaultOperation;
    }

    public void setDefaultOperation(String defaultOperation) {
        this.defaultOperation = defaultOperation;
    }

    @XmlElement(name = "test-option")
    public String getTestOption() {
        return testOption;
    }

    public void setTestOption(String testOption) {
        this.testOption = testOption;
    }

    @XmlElement(name = "error-option")
    public String getErrorOption() {
        return errorOption;
    }

    public void setErrorOption(String errorOption) {
        this.errorOption = errorOption;
    }
}
