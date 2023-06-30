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

package org.broadband_forum.obbaa.modelabstracter.converter.network;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import freemarker.template.TemplateException;
import lombok.AllArgsConstructor;

import org.broadband_forum.obbaa.modelabstracter.ConvertRet;
import org.broadband_forum.obbaa.modelabstracter.jaxb.network.Networks;
import org.broadband_forum.obbaa.modelabstracter.jaxb.network.Node;
import org.broadband_forum.obbaa.modelabstracter.jaxb.network.TerminationPoint;
import org.broadband_forum.obbaa.modelabstracter.utils.TemplateUtils;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Convert OLT Node and TPs.
 */
@AllArgsConstructor
public class OltNodeAndTpSubConverter implements NetworkModelSubConverter {
    private static final String TEMPLATE_NAME_OLT_NODE_TP = "2-1-create-network-access-node-and-tps-in-npc.ftl";

    private static final String TP_TYPE_NNI = "nni";

    private static final String TP_TYPE_UNI = "uni";

    private final String m_deviceName;

    @Override
    public ConvertRet convert(Networks networks) throws NetconfMessageBuilderException, TemplateException, IOException {
        Map<String, Object> values = Maps.newHashMap();
        Node node = networks.getNetworks().get(0).getNodes().get(0);
        List<TerminationPoint> terminationPoints = node.getTerminationPoints();
        terminationPoints.forEach(tp -> {
            if (TP_TYPE_NNI.equals(tp.getTpType())) {
                values.put("nniName", tp.getTpId());
            } else if (TP_TYPE_UNI.equals(tp.getTpType())) {
                values.put("uniName", tp.getTpId());
            }
        });
        List<List<Element>> result = Lists.newArrayList();
        String msg = TemplateUtils.processTemplate(values, TEMPLATE_NAME_OLT_NODE_TP);
        Document document = DocumentUtils.stringToDocument(msg);
        result.add(Lists.newArrayList(document.getDocumentElement()));
        return ConvertRet.builder().deviceName(m_deviceName).configElements(result).build();
    }
}
