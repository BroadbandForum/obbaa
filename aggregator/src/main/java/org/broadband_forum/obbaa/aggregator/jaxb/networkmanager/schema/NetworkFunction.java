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

package org.broadband_forum.obbaa.aggregator.jaxb.networkmanager.schema;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>
 * Class for representing the xml schema for network functions
 * </p>
 * Created by J.V.Correia (Altice Labs) on 26/01/2022.
 */

@XmlRootElement(name = "network-function")
public class NetworkFunction {
    private String networkFunctionName;
    private Root root;

    @XmlElement(name = "name")
    public String getNetworkFunctionName() {
        return networkFunctionName;
    }

    public void setNetworkFunctionName(String networkFunctionName) {
        this.networkFunctionName = networkFunctionName;
    }

    @XmlElement
    public Root getRoot() {
        return root;
    }

    public void setRoot(Root root) {
        this.root = root;
    }
}
