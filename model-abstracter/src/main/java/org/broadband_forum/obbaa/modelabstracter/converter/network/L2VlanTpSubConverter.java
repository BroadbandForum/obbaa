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

import freemarker.template.TemplateException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.MapUtils;
import org.broadband_forum.obbaa.modelabstracter.ConvertRet;
import org.broadband_forum.obbaa.modelabstracter.converter.TemporaryDataManager;
import org.broadband_forum.obbaa.modelabstracter.jaxb.network.Networks;
import org.broadband_forum.obbaa.modelabstracter.jaxb.network.TerminationPoint;
import org.broadband_forum.obbaa.modelabstracter.utils.TemplateUtils;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Convert L2 VLAN TPs.
 */
@Slf4j
@AllArgsConstructor
public class L2VlanTpSubConverter implements NetworkModelSubConverter {
    private static final String TEMPLATE_NAME_NNI_VLAN_SUB_IF = "3-3-2-create-vlan-sub-interface-for-nni.ftl";

    private final String m_deviceName;

    @Override
    public ConvertRet convert(Networks networks) throws NetconfMessageBuilderException, TemplateException, IOException {
        TerminationPoint terminationPoint = networks.getNetworks()
            .get(0)
            .getNodes()
            .get(0)
            .getTerminationPoints()
            .get(0);
        String forwardingProfile = terminationPoint.getL2TerminationPointAttributes()
            .getForwardingVlans()
            .get(0)
            .getForwardingProfile();
        Map<String, Object> profData = TemporaryDataManager.get(forwardingProfile);
        if (MapUtils.isEmpty(profData)) {
            String tipMsg = String.format(Locale.ROOT, "There is no translation profile named %s", forwardingProfile);
            log.warn(tipMsg);
            throw new NetconfMessageBuilderException(tipMsg);
        }
        TemporaryDataManager.put(terminationPoint.getTpId(), profData);
        List<List<Element>> result = Lists.newArrayList();
        Map<String, Object> values = new HashMap<>(profData);
        values.put("vlanName", terminationPoint.getTpId());
        values.put("fwderVlanId",
            terminationPoint.getL2TerminationPointAttributes().getForwardingVlans().get(0).getVlanId());
        String msg = TemplateUtils.processTemplate(values, TEMPLATE_NAME_NNI_VLAN_SUB_IF);
        Document document = DocumentUtils.stringToDocument(msg);
        result.add(Lists.newArrayList(document.getDocumentElement()));
        return ConvertRet.builder().deviceName(m_deviceName).configElements(result).build();
    }
}