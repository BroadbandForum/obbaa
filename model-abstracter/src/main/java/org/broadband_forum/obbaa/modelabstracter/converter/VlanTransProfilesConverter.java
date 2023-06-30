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

import com.google.common.collect.Maps;

import lombok.extern.slf4j.Slf4j;

import org.broadband_forum.obbaa.aggregator.jaxb.utils.JaxbUtils;
import org.broadband_forum.obbaa.modelabstracter.ConvertRet;
import org.broadband_forum.obbaa.modelabstracter.jaxb.common.VlanTranslationProfiles;

import java.util.Map;

import javax.xml.bind.JAXBException;

/**
 * Vlan translation profiles converter.
 */
@Slf4j
public class VlanTransProfilesConverter extends AbstractModelConverter {
    @Override
    public ConvertRet processRequest(String request) throws JAXBException {
        VlanTranslationProfiles translationProfiles = JaxbUtils.unmarshal(request, VlanTranslationProfiles.class);
        translationProfiles.getVlanTranslationProfiles().forEach(profile -> {
            String vlanTranslationProfileName = profile.getName();
            String outTagVlanId = profile.getMatchCriteria().getOuterTag().getVlanId();
            String pushOutTagVlanId = profile.getIngressRewrite().getPushOuterTag().getVlanId();
            String popTags = profile.getIngressRewrite().getPopTags();
            Map<String, Object> values = Maps.newHashMap();
            values.put("vlanTranslationProfileName", vlanTranslationProfileName);
            values.put("outTagVlanId", outTagVlanId);
            values.put("pushOutTagVlanId", pushOutTagVlanId);
            values.put("popTags", popTags);
            TemporaryDataManager.put(vlanTranslationProfileName, values);
        });
        return ConvertRet.builder().build();
    }
}
