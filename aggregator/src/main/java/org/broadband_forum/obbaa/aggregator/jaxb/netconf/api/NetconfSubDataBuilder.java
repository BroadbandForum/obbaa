package org.broadband_forum.obbaa.aggregator.jaxb.netconf.api;

/**
 * Template for building Netconf sub data.
 */
public interface NetconfSubDataBuilder {

    /**
     * Build data of a node. If the node is empty, we need create a instance for it.
     * @param object Object of JAXB data class
     */
    void execute(Object object);
}
