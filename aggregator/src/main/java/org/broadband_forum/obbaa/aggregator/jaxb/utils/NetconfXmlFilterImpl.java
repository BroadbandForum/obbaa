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

package org.broadband_forum.obbaa.aggregator.jaxb.utils;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * XML Filter implementation for Netconf message. It's used to remove the prefix of nsx.
 */
public class NetconfXmlFilterImpl extends XMLFilterImpl {

    @Override
    public void startElement(String uri, String localName, String qname, Attributes atts) throws SAXException {
        super.startElement(uri, localName, localName, atts);
    }

    @Override
    public void endElement(String uri, String localName, String qname) throws SAXException {
        super.endElement(uri, localName, localName);
    }

    @Override
    public void startPrefixMapping(String prefix, String url) throws SAXException {
        super.startPrefixMapping("", url);
    }
}
