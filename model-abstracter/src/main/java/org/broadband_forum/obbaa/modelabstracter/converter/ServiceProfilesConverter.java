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
import org.broadband_forum.obbaa.modelabstracter.jaxb.profile.service.Port;
import org.broadband_forum.obbaa.modelabstracter.jaxb.profile.service.ServiceProfile;
import org.broadband_forum.obbaa.modelabstracter.jaxb.profile.service.ServiceProfiles;

import java.util.Map;

import javax.xml.bind.JAXBException;

/**
 * Service profiles converter.
 */
@Slf4j
public class ServiceProfilesConverter extends AbstractModelConverter {
    @Override
    public ConvertRet processRequest(String request) throws JAXBException {
        ServiceProfiles serviceProfiles = JaxbUtils.unmarshal(request, ServiceProfiles.class);

        Map<String, Object> values = Maps.newHashMap();
        ServiceProfile serviceProfile = serviceProfiles.getServiceProfiles().get(0);
        Port port = serviceProfile.getPorts().getPorts().get(0);
        values.put("portName", port.getName());
        values.put("portType", port.getPortType());
        String portVlanName = port.getPortVlans().getPortVlans().get(0).getName();
        values.put("portVlanName", portVlanName);
        values.putAll(TemporaryDataManager.get(portVlanName));
        TemporaryDataManager.put(serviceProfile.getName(), values);

        return ConvertRet.builder().build();
    }
}
