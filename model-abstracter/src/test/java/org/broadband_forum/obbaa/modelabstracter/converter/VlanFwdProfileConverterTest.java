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

import org.apache.commons.io.IOUtils;
import org.broadband_forum.obbaa.modelabstracter.ConvertRet;
import org.broadband_forum.obbaa.modelabstracter.jaxb.common.ForwardingPort;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class VlanFwdProfileConverterTest {
    private VlanFwdProfileConverter vlanFwdProfileConverter;

    @Before
    public void setUp() {
        vlanFwdProfileConverter = new VlanFwdProfileConverter();
    }

    @Test
    public void processRequest() throws JAXBException, IOException {
        InputStream stream = this.getClass().getResourceAsStream("/vlan-forward-profile-request.xml");
        String request = IOUtils.toString(Objects.requireNonNull(stream));
        ConvertRet ret = vlanFwdProfileConverter.processRequest(request);
        Assert.assertTrue(ret.isConfigEmpty());
        Assert.assertNull(ret.getConfigElements());
        Map<String, Object> data = TemporaryDataManager.get("vlan-fwd-profile1");
        Assert.assertNotNull(data);
        Object ports = data.get("portList");
        Assert.assertTrue(ports instanceof List<?>);
        @SuppressWarnings("unchecked")
        List<ForwardingPort> portList = (List<ForwardingPort>) ports;
        Assert.assertEquals(1, portList.size());
        ForwardingPort fwdPort = portList.get(0);
        Assert.assertEquals("fport1", fwdPort.getName());
        Assert.assertEquals("nni1", fwdPort.getTpId());
        Assert.assertEquals("OLT1", fwdPort.getNodeId());
    }
}