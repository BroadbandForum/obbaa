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
import org.broadband_forum.obbaa.modelabstracter.jaxb.common.ForwardingPort;
import org.broadband_forum.obbaa.modelabstracter.jaxb.common.VlanForwardingProfile;
import org.broadband_forum.obbaa.modelabstracter.jaxb.common.VlanForwardingProfiles;

import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

/**
 * Vlan forwarding profile converter.
 */
public class VlanFwdProfileConverter extends AbstractModelConverter {
    @Override
    public ConvertRet processRequest(String request) throws JAXBException {
        VlanForwardingProfiles vlanForwardingProfiles = JaxbUtils.unmarshal(request, VlanForwardingProfiles.class);
        VlanForwardingProfile vlanForwardingProfile = vlanForwardingProfiles.getVlanForwardingProfiles().get(0);
        String vlanForwardingProfileName = vlanForwardingProfile.getName();
        List<ForwardingPort> forwardingPortList = vlanForwardingProfile.getForwardingPorts().getForwardingPortList();
        Map<String, Object> values = Maps.newHashMap();
        values.put("portList", forwardingPortList);
        TemporaryDataManager.put(vlanForwardingProfileName, values);
        return ConvertRet.builder().build();
    }
}
