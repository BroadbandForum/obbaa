/*
 * Copyright 2018 Broadband Forum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadband_forum.obbaa.aggregator.jaxb.pma.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.broadband_forum.obbaa.aggregator.api.DispatchException;
import org.junit.Test;

public class UndeployAdapterRpcTest {

    @Test
    public void getUndeployAdapterTest() throws DispatchException {
        String undeployAction = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"10101\">\n" +
                " <action xmlns=\"urn:ietf:params:xml:ns:yang:1\">\n" +
                "  <undeploy-adapter xmlns=\"urn:bbf:yang:obbaa:device-adapters\">\n" +
                "   <undeploy>\n" +
                "    <adapter-archive>adapterExample.zip</adapter-archive>\n" +
                "   </undeploy>\n" +
                "  </undeploy-adapter>\n" +
                " </action>\n" +
                "</rpc>";

        UndeployAdapterRpc undeployAdapterRpc = UndeployAdapterRpc.getInstance(undeployAction);
        assertNotNull(undeployAdapterRpc.getUndeployAdapter().getUndeploy());
        assertEquals("adapterExample.zip", undeployAdapterRpc.getUndeployAdapter().getUndeploy().getAdapterArchive());
    }

    @Test
    public void getundeployAdapterInvalidFormatTest() throws DispatchException {
        String undeployAction = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"10101\">\n" +
                " <action xmlns=\"urn:ietf:params:xml:ns:yang:1\">\n" +
                "  <undeploy-adapter xmlns=\"urn:bbf:yang:obbaa:device-adapters\">\n" +
                "  </undeploy-adapter>\n" +
                " </action>\n" +
                "</rpc>";

        UndeployAdapterRpc undeployAdapterRpc = UndeployAdapterRpc.getInstance(undeployAction);
        assertNull(undeployAdapterRpc.getUndeployAdapter().getUndeploy());
    }
}
