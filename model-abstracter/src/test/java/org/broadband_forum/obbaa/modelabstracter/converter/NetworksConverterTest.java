/*
 *   Copyright 2023 Broadband Forum
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
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.broadband_forum.obbaa.modelabstracter.ConvertRet;
import org.broadband_forum.obbaa.modelabstracter.jaxb.common.ForwardingPort;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class NetworksConverterTest {
    private NetworksConverter networksConverter;

    @Before
    public void setUp() throws Exception {
        networksConverter = new NetworksConverter();
        Map<String, Object> values = Maps.newHashMap();
        values.put("vlanTranslationProfileName", "vlan-trans-profile-2");
        values.put("outTagVlanId", "200");
        values.put("pushOutTagVlanId", "300");
        values.put("popTags", "0");
        TemporaryDataManager.put("vlan-trans-profile-2", values);
        Map<String, Object> values2 = Maps.newHashMap();
        values2.put("vlanTranslationProfileName", "vlan-trans-profile-3");
        values2.put("outTagVlanId", "300");
        values2.put("pushOutTagVlanId", "100");
        values2.put("popTags", "0");
        TemporaryDataManager.put("vlan-trans-profile-3", values2);

        Map<String, Object> values3 = new HashMap<>();
        values3.put("mappingRule", "mapping-index-1");
        values3.put("virtualPortName", "v-nni1");
        values3.put("vlanId", "200");
        values3.put("assuredBandwidth", "102400");
        values3.put("fixedBandwidth", "102400");
        values3.put("maximumBandwidth", "204800");
        values3.put("bandwidthProfile", "bandwidth-profile-1");
        TemporaryDataManager.put("line-profile-1", values3);

        values3.put("portName", "eth.1");
        values3.put("portVlanName", "vlan-trans-profile-1");
        values3.put("vlanTranslationProfileName", "vlan-trans-profile-1");
        values3.put("outTagVlanId", "100");
        values3.put("pushOutTagVlanId", "200");
        values3.put("popTags", "0");
        TemporaryDataManager.put("service-profile-1", values3);

        List<ForwardingPort> forwardingPortList = new ArrayList<>();
        ForwardingPort port = new ForwardingPort();
        port.setName("fport1");
        port.setNodeId("OLT1");
        port.setTpId("nni1");
        forwardingPortList.add(port);
        Map<String, Object> values4 = new HashMap<>();
        values4.put("portList", forwardingPortList);
        TemporaryDataManager.put("vlan-fwd-profile1", values4);
    }

    @Test
    public void processRequestForLinkForward() throws TemplateException, JAXBException, NetconfMessageBuilderException,
            IOException {
        InputStream stream = this.getClass().getResourceAsStream("/link-forward-request.xml");
        String request = IOUtils.toString(Objects.requireNonNull(stream));
        ConvertRet convertRet = networksConverter.processRequest(request);
        Assert.assertFalse(convertRet.isConfigEmpty());
    }

    @Test
    public void processRequestForL2VnniVuni() throws TemplateException, JAXBException, NetconfMessageBuilderException,
            IOException {
        InputStream stream = this.getClass().getResourceAsStream("/l2-v-nni-v-uni-request.xml");
        String request = IOUtils.toString(Objects.requireNonNull(stream));
        ConvertRet convertRet = networksConverter.processRequest(request);
        Assert.assertFalse(convertRet.isConfigEmpty());
    }

    @Test
    public void processRequestForOnuNodeTp() throws TemplateException, JAXBException, NetconfMessageBuilderException,
            IOException {
        InputStream stream = this.getClass().getResourceAsStream("/onu-node-tp-request.xml");
        String request = IOUtils.toString(Objects.requireNonNull(stream));
        ConvertRet convertRet = networksConverter.processRequest(request);
        Assert.assertFalse(convertRet.isConfigEmpty());
    }

    @Test
    public void processRequestForL2VlanTp() throws TemplateException, JAXBException, NetconfMessageBuilderException,
            IOException {
        InputStream stream = this.getClass().getResourceAsStream("/l2-vlan-tp-request.xml");
        String request = IOUtils.toString(Objects.requireNonNull(stream));
        ConvertRet convertRet = networksConverter.processRequest(request);
        Assert.assertFalse(convertRet.isConfigEmpty());
    }
}