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
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.MapUtils;
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
import java.util.Locale;
import java.util.Map;

/**
 * Convert ONU Node and TPs.
 */
@Slf4j
@AllArgsConstructor
public class OnuNodeAndTpSubConverter implements NetworkModelSubConverter {
    private static final String TEMPLATE_NAME_ONT_ETH_PORT = "2-2-d-create-ont-eth-port.ftl";

    private static final String TEMPLATE_NAME_BAND_WIDTH_PROFILE = "2-3-0-create-band-width-profile.ftl";

    private static final String TEMPLATE_NAME_T_CONT = "2-3-1-a-line-profile-create-t-cont.ftl";

    private static final String TEMPLATE_NAME_OLT_V_ENET = "2-3-1-b-line-profile-create-olt-v-enet.ftl";

    private static final String TEMPLATE_NAME_ONU_V_ENET = "2-3-1-c-line-profile-create-onu-v-enet.ftl";

    private static final String TEMPLATE_NAME_LINE_PROF_LINK_TABLE = "2-3-1-d-line-profile-create-link-table.ftl";

    private static final String TEMPLATE_NAME_GEM_PORT = "2-3-1-e-line-profile-create-gemport.ftl";

    private static final String TEMPLATE_NAME_GEM_MAPPING = "2-3-1-f-line-profile-create-gem-mapping.ftl";

    private static final String TEMPLATE_NAME_PORT_VLAN = "2-3-2-service-profile-create-port-vlan.ftl";

    private final String m_deviceName;

    @Override
    public ConvertRet convert(Networks networks) throws NetconfMessageBuilderException, TemplateException, IOException {
        Map<String, Object> values = Maps.newHashMap();
        Node node = networks.getNetworks().get(0).getNodes().get(0);
        String nodeId = node.getNodeId();
        values.put("onuId", nodeId);
        values.put("oltId", m_deviceName);
        String uniTpId = node.getTerminationPoints()
            .stream()
            .filter(e -> e.getTpType().equals("uni"))
            .map(TerminationPoint::getTpId)
            .findFirst()
            .orElse(nodeId);
        values.put("uniTpId", uniTpId);
        // get line profile values
        String ntLineProfile = node.getAccessNodeAttributes().getNtLineProfile();
        Map<String, Object> lineProfValues = TemporaryDataManager.get(ntLineProfile);
        if (MapUtils.isEmpty(lineProfValues)) {
            String tipMsg = String.format(Locale.ROOT, "There is no line profile named %s", ntLineProfile);
            log.warn(tipMsg);
            throw new NetconfMessageBuilderException(tipMsg);
        }
        values.putAll(lineProfValues);
        // get service profile values
        String ntServiceProfile = node.getAccessNodeAttributes().getNtServiceProfile();
        Map<String, Object> serviceProfValues = TemporaryDataManager.get(ntServiceProfile);
        if (MapUtils.isEmpty(serviceProfValues)) {
            String tipMsg = String.format(Locale.ROOT, "There is no service profile named %s", ntLineProfile);
            log.warn(tipMsg);
            throw new NetconfMessageBuilderException(tipMsg);
        }
        values.putAll(serviceProfValues);

        List<List<Element>> result = Lists.newArrayList();
        List<String> templateList = Arrays.asList(TEMPLATE_NAME_BAND_WIDTH_PROFILE, TEMPLATE_NAME_ONT_ETH_PORT,
            TEMPLATE_NAME_T_CONT, TEMPLATE_NAME_OLT_V_ENET, TEMPLATE_NAME_ONU_V_ENET,
            TEMPLATE_NAME_LINE_PROF_LINK_TABLE, TEMPLATE_NAME_GEM_PORT, TEMPLATE_NAME_GEM_MAPPING,
            TEMPLATE_NAME_PORT_VLAN);
        for (String template : templateList) {
            String msg = TemplateUtils.processTemplate(values, template);
            Document document = DocumentUtils.stringToDocument(msg);
            result.add(Lists.newArrayList(document.getDocumentElement()));
        }
        return ConvertRet.builder().deviceName(m_deviceName).configElements(result).build();
    }
}
