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
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LineProfilesConverterTest {
    private LineProfilesConverter lineProfilesConverter;

    @Before
    public void setUp() {
        lineProfilesConverter = new LineProfilesConverter();
        Map<String, Object> values = new HashMap<>();
        values.put("assuredBandwidth", "102400");
        values.put("fixedBandwidth", "102400");
        values.put("maximumBandwidth", "204800");
        TemporaryDataManager.put("bandwidth-profile-1", values);
    }

    @Test
    public void processRequest() throws JAXBException, NetconfMessageBuilderException, IOException {
        InputStream stream = this.getClass().getResourceAsStream("/line-profile-request.xml");
        String request = IOUtils.toString(Objects.requireNonNull(stream));
        ConvertRet ret = lineProfilesConverter.processRequest(request);
        Assert.assertTrue(ret.isConfigEmpty());
        Assert.assertNull(ret.getConfigElements());
        Map<String, Object> data = TemporaryDataManager.get("line-profile-1");
        Assert.assertEquals("mapping-index-1", data.get("mappingRule"));
        Assert.assertEquals("v-nni1", data.get("virtualPortName"));
        Assert.assertEquals("200", data.get("vlanId"));
    }

    @Test(expected = UnmarshalException.class)
    public void processRequestWithException() throws JAXBException, NetconfMessageBuilderException {
        String invalidRequest = "";
        lineProfilesConverter.processRequest(invalidRequest);
    }
}