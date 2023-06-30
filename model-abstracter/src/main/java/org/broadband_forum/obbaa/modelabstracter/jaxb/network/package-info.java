/*
 *   Copyright 2022 Broadband Forum
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

@XmlSchema(namespace = "urn:ietf:params:xml:ns:yang:ietf-network",
    location = "urn:ietf:params:xml:ns:yang:ietf-network", elementFormDefault = XmlNsForm.QUALIFIED, xmlns = {
    @XmlNs(prefix = "nt", namespaceURI = "urn:ietf:params:xml:ns:yang:ietf-network-topology"),
    @XmlNs(prefix = "bbf-an-nw-topology", namespaceURI = "urn:bbf:yang:obbaa:an-network-topology")
})
package org.broadband_forum.obbaa.modelabstracter.jaxb.network;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;