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
