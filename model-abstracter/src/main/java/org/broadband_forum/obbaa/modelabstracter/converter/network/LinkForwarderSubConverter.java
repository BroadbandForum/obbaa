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

import org.apache.commons.collections.MapUtils;
import org.broadband_forum.obbaa.modelabstracter.ConvertRet;
import org.broadband_forum.obbaa.modelabstracter.converter.TemporaryDataManager;
import org.broadband_forum.obbaa.modelabstracter.jaxb.network.Link;
import org.broadband_forum.obbaa.modelabstracter.jaxb.network.Networks;
import org.broadband_forum.obbaa.modelabstracter.utils.TemplateUtils;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Convert L2 v-nni and v-uni TPs.
 */
@AllArgsConstructor
public class LinkForwarderSubConverter implements NetworkModelSubConverter {
    private static final String TEMPLATE_NAME_1_1_FORWARDER = "3-2-a-create-1-1-forwarder.ftl";

    private static final String TEMPLATE_NAME_N_1_FORWARDER = "3-3-1-create-n-1-vlan-tp-forwarder.ftl";

    private final String m_deviceName;

    @Override
    public ConvertRet convert(Networks networks) throws NetconfMessageBuilderException, TemplateException, IOException {
        Map<String, Object> values = Maps.newHashMap();
        List<List<Element>> result = Lists.newArrayList();
        Link link = networks.getNetworks().get(0).getLinks().get(0);
        String destTp = link.getDestination().getDestTp();
        Map<String, Object> fwderProfData = TemporaryDataManager.get(destTp);
        if (MapUtils.isEmpty(fwderProfData)) {
            String linkId = link.getLinkId();
            String sourceTp = link.getSource().getSourceTp();
            values.put("fwderName", linkId);
            values.put("srcSubIfName", sourceTp);
            values.put("destSubIfName", destTp);
            String msg = TemplateUtils.processTemplate(values, TEMPLATE_NAME_1_1_FORWARDER);
            Document document = DocumentUtils.stringToDocument(msg);
            result.add(Lists.newArrayList(document.getDocumentElement()));
        } else {
            String sourceTp = link.getSource().getSourceTp();
            values.put("vlanName", destTp);
            values.put("portName", sourceTp);
            values.put("portTpId", sourceTp);
            values.putAll(fwderProfData);
            String msg = TemplateUtils.processTemplate(values, TEMPLATE_NAME_N_1_FORWARDER);
            Document document = DocumentUtils.stringToDocument(msg);
            result.add(Lists.newArrayList(document.getDocumentElement()));
        }
        return ConvertRet.builder().deviceName(m_deviceName).configElements(result).build();
    }
}
