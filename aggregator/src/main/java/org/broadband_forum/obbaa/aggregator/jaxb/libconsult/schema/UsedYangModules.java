/*
 *   Copyright 2018 Broadband Forum
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.broadband_forum.obbaa.aggregator.jaxb.libconsult.schema;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "in-use-library-modules", namespace = "urn:bbf:yang:obbaa:module-library-check")
public class UsedYangModules {
    private List<YangModule> yangModules;

    @XmlElement(name = "module")
    public List<YangModule> getYangModules() {
        return yangModules;
    }

    public void setYangModules(List<YangModule> yangModules) {
        this.yangModules = yangModules;
    }
}
