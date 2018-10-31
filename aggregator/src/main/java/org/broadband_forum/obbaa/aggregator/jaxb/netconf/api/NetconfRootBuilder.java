package org.broadband_forum.obbaa.aggregator.jaxb.netconf.api;

/**
 * Template for building Netconf root container.
 */
public interface NetconfRootBuilder {

    /**
     * Build data of the node. If the node is null, we need ignore the node.
     */
    void execute();
}
