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

package org.broadband_forum.obbaa.modelabstracter.converter.network;

import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.broadband_forum.obbaa.aggregator.jaxb.utils.JaxbUtils;
import org.broadband_forum.obbaa.modelabstracter.ConvertRet;
import org.broadband_forum.obbaa.modelabstracter.converter.TemporaryDataManager;
import org.broadband_forum.obbaa.modelabstracter.jaxb.network.Networks;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class OnuNodeAndTpSubConverterTest {
    private static final String DEVICE_NAME = "test_olt";

    private OnuNodeAndTpSubConverter onuNodeAndTpSubConverter;

    @Before
    public void setUp() {
        onuNodeAndTpSubConverter = new OnuNodeAndTpSubConverter(DEVICE_NAME);
        Map<String, Object> values = new HashMap<>();
        values.put("mappingRule", "mapping-index-1");
        values.put("virtualPortName", "v-nni1");
        values.put("vlanId", "200");
        values.put("assuredBandwidth", "102400");
        values.put("fixedBandwidth", "102400");
        values.put("maximumBandwidth", "204800");
        values.put("bandwidthProfile", "bandwidth-profile-1");
        TemporaryDataManager.put("line-profile-1", values);

        values.put("portName", "eth.1");
        values.put("portVlanName", "vlan-trans-profile-1");
        values.put("vlanTranslationProfileName", "vlan-trans-profile-1");
        values.put("outTagVlanId", "100");
        values.put("pushOutTagVlanId", "200");
        values.put("popTags", "0");
        TemporaryDataManager.put("service-profile-1", values);
    }

    @Test
    public void processRequest() throws NetconfMessageBuilderException, JAXBException, TemplateException, IOException {
        InputStream stream = this.getClass().getResourceAsStream("/onu-node-tp-request.xml");
        String request = IOUtils.toString(Objects.requireNonNull(stream));
        Networks networks = JaxbUtils.unmarshal(request, Networks.class);
        ConvertRet ret = onuNodeAndTpSubConverter.convert(networks);
        Assert.assertFalse(ret.isConfigEmpty());
        Assert.assertNotNull(ret.getConfigElements());
        Assert.assertEquals(DEVICE_NAME, ret.getDeviceName());
        Assert.assertEquals(9, ret.getConfigElements().size());
    }
}