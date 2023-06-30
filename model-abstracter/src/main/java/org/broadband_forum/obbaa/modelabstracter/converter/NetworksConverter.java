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

package org.broadband_forum.obbaa.modelabstracter.converter;

import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;

import org.broadband_forum.obbaa.aggregator.jaxb.utils.JaxbUtils;
import org.broadband_forum.obbaa.modelabstracter.ConvertRet;
import org.broadband_forum.obbaa.modelabstracter.converter.network.L2VlanTpSubConverter;
import org.broadband_forum.obbaa.modelabstracter.converter.network.L2VnniVuniTpSubConverter;
import org.broadband_forum.obbaa.modelabstracter.converter.network.LinkForwarderSubConverter;
import org.broadband_forum.obbaa.modelabstracter.converter.network.NetworkModelSubConverter;
import org.broadband_forum.obbaa.modelabstracter.converter.network.OltNodeAndTpSubConverter;
import org.broadband_forum.obbaa.modelabstracter.converter.network.OnuNodeAndTpSubConverter;
import org.broadband_forum.obbaa.modelabstracter.jaxb.network.L2AccessAttributes;
import org.broadband_forum.obbaa.modelabstracter.jaxb.network.L2TerminationPointAttributes;
import org.broadband_forum.obbaa.modelabstracter.jaxb.network.Link;
import org.broadband_forum.obbaa.modelabstracter.jaxb.network.Network;
import org.broadband_forum.obbaa.modelabstracter.jaxb.network.Networks;
import org.broadband_forum.obbaa.modelabstracter.jaxb.network.Node;
import org.broadband_forum.obbaa.modelabstracter.jaxb.network.Source;
import org.broadband_forum.obbaa.modelabstracter.jaxb.network.TerminationPoint;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import javax.xml.bind.JAXBException;

/**
 * Networks converter.
 */
@Slf4j
public class NetworksConverter extends AbstractModelConverter {
    private static final String BBF_L2_NS = "xmlns:bbf-l2t=\"urn:bbf:yang:obbaa:l2-topology\"";

    private static final String NODE_TYPE_OLT = "olt";

    private static final String NODE_TYPE_ONU = "onu";

    private static final String TP_TYPE_L2_V_NNI = "l2-v-nni";

    @Override
    public ConvertRet processRequest(String request)
        throws JAXBException, TemplateException, IOException, NetconfMessageBuilderException {
        Networks networks = JaxbUtils.unmarshal(request, Networks.class);

        NetworkModelSubConverter converter = getConvert(request, networks);
        if (Objects.isNull(converter)) {
            log.error("Does not support this request: {}", request);
            throw new NetconfMessageBuilderException("Does not support this request");
        }
        return converter.convert(networks);
    }

    private NetworkModelSubConverter getConvert(String request, Networks networks) {
        NetworkModelSubConverter converter = getOltNodeAndTpConverter(request, networks);
        if (Objects.nonNull(converter)) {
            return converter;
        }
        converter = getOnuNodeAndTpConverter(request, networks);
        if (Objects.nonNull(converter)) {
            return converter;
        }
        converter = getL2VnniVuniTpConverter(request, networks);
        if (Objects.nonNull(converter)) {
            return converter;
        }
        converter = getL2VlanTpConverter(request, networks);
        if (Objects.nonNull(converter)) {
            return converter;
        }
        converter = getLinkForwarderConverter(networks);
        if (Objects.nonNull(converter)) {
            return converter;
        }
        return null;
    }

    private NetworkModelSubConverter getOltNodeAndTpConverter(String oriReq, Networks networks) {
        if (oriReq.contains(BBF_L2_NS)) {
            return null;
        }
        return Optional.ofNullable(networks.getNetworks().get(0))
            .map(Network::getNodes)
            .map(e -> e.get(0))
            .map(Node::getNodeId)
            .filter(nodeId -> nodeId.toLowerCase(Locale.ROOT).startsWith(NODE_TYPE_OLT))
            .map((Function<String, NetworkModelSubConverter>) OltNodeAndTpSubConverter::new)
            .orElse(null);
    }

    private NetworkModelSubConverter getOnuNodeAndTpConverter(String oriReq, Networks networks) {
        if (oriReq.contains(BBF_L2_NS)) {
            return null;
        }
        if (!Optional.ofNullable(networks.getNetworks().get(0))
            .map(Network::getNodes)
            .map(e -> e.get(0))
            .map(Node::getNodeId)
            .filter(nodeId -> nodeId.toLowerCase(Locale.ROOT).startsWith(NODE_TYPE_ONU))
            .isPresent()) {
            return null;
        }

        return Optional.ofNullable(networks.getNetworks().get(0))
            .map(Network::getLinks)
            .map(e -> e.get(0))
            .map(Link::getSource)
            .map(Source::getSourceNode)
            .filter(nodeId -> nodeId.toLowerCase(Locale.ROOT).startsWith(NODE_TYPE_OLT))
            .map((Function<String, NetworkModelSubConverter>) OnuNodeAndTpSubConverter::new)
            .orElse(null);
    }

    private NetworkModelSubConverter getL2VnniVuniTpConverter(String oriReq, Networks networks) {
        // L2 v-nni and v-uni should contain this namespace
        if (!oriReq.contains(BBF_L2_NS)) {
            return null;
        }
        Optional<Node> nodeOptional = Optional.ofNullable(networks.getNetworks().get(0))
            .map(Network::getNodes)
            .flatMap(nodes -> nodes.stream()
                .filter(node -> Optional.ofNullable(node)
                    .map(Node::getTerminationPoints)
                    .map(e -> e.get(0))
                    .map(TerminationPoint::getTpType)
                    .map(e -> e.contains(TP_TYPE_L2_V_NNI))
                    .orElse(false))
                .findFirst());
        if (!nodeOptional.map(Node::getTerminationPoints)
            .map(e -> e.get(0))
            .map(TerminationPoint::getL2TerminationPointAttributes)
            .map(L2TerminationPointAttributes::getL2AccessAttributes)
            .map(e -> e.get(0))
            .map(L2AccessAttributes::getVlanTranslation)
            .isPresent()) {
            return null;
        }
        return nodeOptional.map(Node::getNodeId).map(L2VnniVuniTpSubConverter::new).orElse(null);
    }

    private NetworkModelSubConverter getL2VlanTpConverter(String oriReq, Networks networks) {
        // L2 VLAN should contain this namespace
        if (!oriReq.contains(BBF_L2_NS)) {
            return null;
        }
        Optional<Node> nodeOptional = Optional.ofNullable(networks.getNetworks().get(0))
            .map(Network::getNodes)
            .map(e -> e.get(0));
        if (!nodeOptional.map(Node::getTerminationPoints)
            .map(e -> e.get(0))
            .map(TerminationPoint::getL2TerminationPointAttributes)
            .map(L2TerminationPointAttributes::getForwardingVlans)
            .isPresent()) {
            return null;
        }
        return nodeOptional.map(Node::getNodeId).map(L2VlanTpSubConverter::new).orElse(null);
    }

    private NetworkModelSubConverter getLinkForwarderConverter(Networks networks) {
        Optional<List<Node>> nodes = Optional.ofNullable(networks.getNetworks().get(0)).map(Network::getNodes);
        Optional<Link> link = Optional.ofNullable(networks.getNetworks().get(0))
            .map(Network::getLinks)
            .map(e -> e.get(0));
        if (nodes.isPresent() || !link.isPresent()) {
            return null;
        }
        String sourceNode = link.get().getSource().getSourceNode();
        String destNode = link.get().getDestination().getDestNode();
        String deviceName = sourceNode.toLowerCase(Locale.ROOT).startsWith(NODE_TYPE_OLT) ? sourceNode : destNode;
        return new LinkForwarderSubConverter(deviceName);
    }
}
