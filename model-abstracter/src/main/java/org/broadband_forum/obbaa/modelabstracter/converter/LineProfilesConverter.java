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

import org.apache.commons.collections.MapUtils;
import org.broadband_forum.obbaa.aggregator.jaxb.utils.JaxbUtils;
import org.broadband_forum.obbaa.modelabstracter.ConvertRet;
import org.broadband_forum.obbaa.modelabstracter.jaxb.profile.line.LineProfile;
import org.broadband_forum.obbaa.modelabstracter.jaxb.profile.line.LineProfiles;
import org.broadband_forum.obbaa.modelabstracter.jaxb.profile.line.VirtualPort;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;

import java.util.Locale;
import java.util.Map;

import javax.xml.bind.JAXBException;

/**
 * Line profiles converter.
 */
@Slf4j
public class LineProfilesConverter extends AbstractModelConverter {
    @Override
    public ConvertRet processRequest(String request) throws JAXBException, NetconfMessageBuilderException {
        LineProfiles lineProfiles = JaxbUtils.unmarshal(request, LineProfiles.class);
        LineProfile lineProfile = lineProfiles.getLineProfiles().get(0);
        VirtualPort virtualPort = lineProfile.getVirtualPorts().getVirtualPorts().get(0);
        Map<String, Object> values = Maps.newHashMap();
        values.put("mappingRule", virtualPort.getMatchCriteria().getName());
        values.put("virtualPortName", virtualPort.getName());
        values.put("vlanId", virtualPort.getMatchCriteria().getVlan());
        String lineBandwidthProf = virtualPort.getLineBandwidthRef();
        Map<String, Object> bandwidthValues = TemporaryDataManager.get(lineBandwidthProf);
        if (MapUtils.isEmpty(bandwidthValues)) {
            String tipMsg = String.format(Locale.ROOT, "There is no line profile named %s", lineBandwidthProf);
            log.warn(tipMsg);
            throw new NetconfMessageBuilderException(tipMsg);
        }
        values.putAll(bandwidthValues);
        TemporaryDataManager.put(lineProfile.getName(), values);
        return ConvertRet.builder().build();
    }
}
