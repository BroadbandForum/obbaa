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
import org.broadband_forum.obbaa.modelabstracter.converter.TemporaryDataManager;
import org.broadband_forum.obbaa.modelabstracter.jaxb.network.Networks;
import org.broadband_forum.obbaa.modelabstracter.jaxb.network.Node;
import org.broadband_forum.obbaa.modelabstracter.jaxb.network.TerminationPoint;
import org.broadband_forum.obbaa.modelabstracter.utils.TemplateUtils;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Convert L2 v-nni and v-uni TPs.
 */
@AllArgsConstructor
public class L2VnniVuniTpSubConverter implements NetworkModelSubConverter {
    private static final String TEMPLATE_NAME_L2_V_UNI = "3-1-a-create-vlan-sub-interface_v_uni.ftl";

    private static final String TEMPLATE_NAME_L2_V_NNI = "3-1-b-create-vlan-sub-interface_v_nni.ftl";

    private static final String TP_TYPE_L2_V_UNI = "l2-v-uni";

    private static final String TP_TYPE_L2_V_NNI = "l2-v-nni";

    private static final String PREFIX_L2_V_UNI = "l2vUni_";

    private static final String PREFIX_L2_V_NNI = "l2vNni_";

    private final String m_deviceName;

    @Override
    public ConvertRet convert(Networks networks) throws NetconfMessageBuilderException, TemplateException, IOException {
        Map<String, Object> values = Maps.newHashMap();
        List<Node> nodes = networks.getNetworks().get(0).getNodes();
        for (Node node : nodes) {
            TerminationPoint tp = node.getTerminationPoints().get(0);
            String tpType = tp.getTpType();
            if (tpType.endsWith(TP_TYPE_L2_V_UNI)) {
                values.put("onuId", node.getNodeId());
                values.put("l2vUniName", tp.getTpId());
                values.put("l2vUniTpRef", tp.getSupportingTerminationPoint().getTpRef());
                String translationProfile = tp.getL2TerminationPointAttributes()
                    .getL2AccessAttributes()
                    .get(0)
                    .getVlanTranslation()
                    .getTranslationProfile();
                Map<String, Object> transProfMap = TemporaryDataManager.get(translationProfile);
                transProfMap.forEach((key, val) -> values.put(PREFIX_L2_V_UNI + key, val));
            } else if (tpType.endsWith(TP_TYPE_L2_V_NNI)) {
                values.put("oltId", node.getNodeId());
                values.put("l2vNniName", tp.getTpId());
                values.put("l2vNniTpRef", tp.getSupportingTerminationPoint().getTpRef());
                String translationProfile = tp.getL2TerminationPointAttributes()
                    .getL2AccessAttributes()
                    .get(0)
                    .getVlanTranslation()
                    .getTranslationProfile();
                Map<String, Object> transProfMap = TemporaryDataManager.get(translationProfile);
                transProfMap.forEach((key, val) -> values.put(PREFIX_L2_V_NNI + key, val));
            }
        }
        List<List<Element>> result = Lists.newArrayList();
        List<String> templateList = Arrays.asList(TEMPLATE_NAME_L2_V_UNI, TEMPLATE_NAME_L2_V_NNI);
        for (String template : templateList) {
            String msg = TemplateUtils.processTemplate(values, template);
            Document document = DocumentUtils.stringToDocument(msg);
            result.add(Lists.newArrayList(document.getDocumentElement()));
        }
        return ConvertRet.builder().deviceName(m_deviceName).configElements(result).build();
    }
}
