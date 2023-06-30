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

import org.broadband_forum.obbaa.aggregator.jaxb.utils.JaxbUtils;
import org.broadband_forum.obbaa.modelabstracter.ConvertRet;
import org.broadband_forum.obbaa.modelabstracter.jaxb.profile.line.LineBandwidthProfiles;

import java.util.Map;

import javax.xml.bind.JAXBException;

/**
 * Line bandwidth profiles converter.
 */
public class LineBandwidthProfilesConverter extends AbstractModelConverter {
    @Override
    public ConvertRet processRequest(String request) throws JAXBException {
        LineBandwidthProfiles lineBandwidthProfiles = JaxbUtils.unmarshal(request, LineBandwidthProfiles.class);
        Map<String, Object> values = Maps.newHashMap();
        lineBandwidthProfiles.getLineBandwidthProfiles().forEach(profile -> {
            values.put("bandwidthProfile", profile.getName());
            values.put("fixedBandwidth", profile.getFixedBandwidth());
            values.put("assuredBandwidth", profile.getAssuredBandwidth());
            values.put("maximumBandwidth", profile.getMaximumBandwidth());
            TemporaryDataManager.put(profile.getName(), values);
        });
        return ConvertRet.builder().build();
    }
}
