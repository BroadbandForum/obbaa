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
import org.broadband_forum.obbaa.modelabstracter.jaxb.common.ForwardingPort;
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
import java.util.*;

public class L2VlanTpSubConverterTest {
    private static final String DEVICE_NAME = "test_olt";

    private L2VlanTpSubConverter l2VlanTpSubConverter;

    @Before
    public void setUp() {
        l2VlanTpSubConverter = new L2VlanTpSubConverter(DEVICE_NAME);
        List<ForwardingPort> forwardingPortList = new ArrayList<>();
        ForwardingPort port = new ForwardingPort();
        port.setName("fport1");
        port.setNodeId("OLT1");
        port.setTpId("nni1");
        forwardingPortList.add(port);
        Map<String, Object> values = new HashMap<>();
        values.put("portList", forwardingPortList);
        TemporaryDataManager.put("vlan-fwd-profile1", values);
    }

    @Test
    public void convert() throws JAXBException, TemplateException, NetconfMessageBuilderException, IOException {
        InputStream stream = this.getClass().getResourceAsStream("/l2-vlan-tp-request.xml");
        String request = IOUtils.toString(Objects.requireNonNull(stream));
        Networks networks = JaxbUtils.unmarshal(request, Networks.class);
        ConvertRet ret = l2VlanTpSubConverter.convert(networks);
        Assert.assertEquals(ret.getDeviceName(), DEVICE_NAME);
        Assert.assertFalse(ret.isConfigEmpty());
        List<Element> elements = ret.getConfigElements().get(0);
        String respMsg = DocumentUtils.documentToPrettyString(elements.get(0));
        stream = this.getClass().getResourceAsStream("/l2-vlan-tp-valid.xml");
        String validMsg = IOUtils.toString(Objects.requireNonNull(stream));
        Assert.assertEquals(validMsg, respMsg);
    }
}