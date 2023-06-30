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

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

public class LineBandwidthProfilesConverterTest {
    private LineBandwidthProfilesConverter lineBandwidthProfilesConverter;

    @Before
    public void setUp() {
        lineBandwidthProfilesConverter = new LineBandwidthProfilesConverter();
    }

    @Test
    public void processRequest() throws NetconfMessageBuilderException, IOException {
        InputStream stream = this.getClass().getResourceAsStream("/line-bandwidth-profile-request.xml");
        String request = IOUtils.toString(Objects.requireNonNull(stream));
        ConvertRet ret = lineBandwidthProfilesConverter.convert(request);
        Assert.assertTrue(ret.isConfigEmpty());
        Assert.assertNull(ret.getConfigElements());
        Map<String, Object> data = TemporaryDataManager.get("bandwidth-profile-1");
        Assert.assertEquals("bandwidth-profile-1", data.get("bandwidthProfile"));
        Assert.assertEquals("102400", data.get("assuredBandwidth"));
        Assert.assertEquals("204800", data.get("maximumBandwidth"));
        Assert.assertEquals("102400", data.get("fixedBandwidth"));
    }
}