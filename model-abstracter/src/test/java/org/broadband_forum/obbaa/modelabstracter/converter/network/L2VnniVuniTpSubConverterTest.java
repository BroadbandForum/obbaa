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

import com.google.common.collect.Maps;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.broadband_forum.obbaa.aggregator.jaxb.utils.JaxbUtils;
import org.broadband_forum.obbaa.modelabstracter.ConvertRet;
import org.broadband_forum.obbaa.modelabstracter.converter.TemporaryDataManager;
import org.broadband_forum.obbaa.modelabstracter.jaxb.network.Networks;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

public class L2VnniVuniTpSubConverterTest {
    private static final String DEVICE_NAME = "test_olt";

    private L2VnniVuniTpSubConverter l2VnniVuniTpSubConverter;

    @Before
    public void setUp() {
        l2VnniVuniTpSubConverter = new L2VnniVuniTpSubConverter(DEVICE_NAME);
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
    }

    @Test
    public void convert() throws JAXBException, TemplateException, NetconfMessageBuilderException, IOException {
        InputStream stream = this.getClass().getResourceAsStream("/l2-v-nni-v-uni-request.xml");
        String request = IOUtils.toString(Objects.requireNonNull(stream));
        Networks networks = JaxbUtils.unmarshal(request, Networks.class);
        ConvertRet ret = l2VnniVuniTpSubConverter.convert(networks);
        Assert.assertEquals(ret.getDeviceName(), DEVICE_NAME);
        Assert.assertFalse(ret.isConfigEmpty());
        Assert.assertEquals(2, ret.getConfigElements().size());
        stream = this.getClass().getResourceAsStream("/l2-v-nni-v-uni-valid-1.xml");
        String validRet1 = IOUtils.toString(Objects.requireNonNull(stream));
        stream = this.getClass().getResourceAsStream("/l2-v-nni-v-uni-valid-2.xml");
        String validRet2 = IOUtils.toString(Objects.requireNonNull(stream));
        Assert.assertEquals(validRet1, DocumentUtils.documentToPrettyString(ret.getConfigElements().get(0).get(0)));
        Assert.assertEquals(validRet2, DocumentUtils.documentToPrettyString(ret.getConfigElements().get(1).get(0)));
    }
}