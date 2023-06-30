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
import org.broadband_forum.obbaa.modelabstracter.jaxb.network.Networks;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class OltNodeAndTpSubConverterTest {
    private static final String DEVICE_NAME = "test_olt";

    private OltNodeAndTpSubConverter oltNodeAndTpSubConverter;

    @Before
    public void setUp() {
        oltNodeAndTpSubConverter = new OltNodeAndTpSubConverter(DEVICE_NAME);
    }

    @Test
    public void processRequest() throws NetconfMessageBuilderException, JAXBException, TemplateException, IOException {
        InputStream stream = this.getClass().getResourceAsStream("/olt-node-tp-request.xml");
        String request = IOUtils.toString(Objects.requireNonNull(stream));
        Networks networks = JaxbUtils.unmarshal(request, Networks.class);
        ConvertRet ret = oltNodeAndTpSubConverter.convert(networks);
        Assert.assertFalse(ret.isConfigEmpty());
        Assert.assertNotNull(ret.getConfigElements());
        Assert.assertEquals(DEVICE_NAME, ret.getDeviceName());
        Element elements = ret.getConfigElements().get(0).get(0);
        stream = this.getClass().getResourceAsStream("/olt-node-tp-valid.xml");
        String validRet = IOUtils.toString(Objects.requireNonNull(stream));
        Assert.assertEquals(validRet, DocumentUtils.documentToPrettyString(elements));
    }
}