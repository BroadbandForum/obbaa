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

package org.broadband_forum.obbaa.aggregator.jaxb.yanglibrary.api;

import org.broadband_forum.obbaa.aggregator.jaxb.netconf.api.NetconfProtocol;
import org.broadband_forum.obbaa.aggregator.jaxb.yanglibrary.schema.library.YangLibrary;
import org.broadband_forum.obbaa.aggregator.jaxb.yanglibrary.schema.state.ModulesState;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class YangLibraryMessageTest {
    private static final String TEST_STRING = "135797531";

    @Test
    public void getYangLibrary() throws Exception {
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1520261367256\">\n" +
                "  <get>\n" +
                "    <source>\n" +
                "      <running />\n" +
                "    </source>\n" +
                "    <filter type=\"subtree\">\n" +
                "      <yang-library xmlns=\"urn:ietf:params:xml:ns:yang:ietf-yang-library\">\n" +
                "     </yang-library>\n" +
                "    </filter>\n" +
                "  </get>\n" +
                "</rpc>\n";

        YangLibraryMessage yangLibraryMessage = new YangLibraryMessage(request);
        assertTrue(yangLibraryMessage.getYangLibrary() != null);
        assertTrue(yangLibraryMessage.getModulesState() == null);
    }

    @Test
    public void getModulesState() throws Exception {
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1520261367256\">\n" +
                "  <get>\n" +
                "    <source>\n" +
                "      <running />\n" +
                "    </source>\n" +
                "    <filter type=\"subtree\">\n" +
                "      <modules-state xmlns=\"urn:ietf:params:xml:ns:yang:ietf-yang-library\">\n" +
                "     </modules-state>\n" +
                "    </filter>\n" +
                "  </get>\n" +
                "</rpc>\n";

        YangLibraryMessage yangLibraryMessage = new YangLibraryMessage(request);
        assertTrue(yangLibraryMessage.getModulesState() != null);
    }

    @Test
    public void buildRpcReplyResponse() throws Exception {
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1520261367256\">\n" +
                "  <get>\n" +
                "    <source>\n" +
                "      <running />\n" +
                "    </source>\n" +
                "    <filter type=\"subtree\">\n" +
                "      <yang-library xmlns=\"urn:ietf:params:xml:ns:yang:ietf-yang-library\">\n" +
                "     </yang-library>\n" +
                "      <modules-state xmlns=\"urn:ietf:params:xml:ns:yang:ietf-yang-library\">\n" +
                "     </modules-state>\n" +
                "    </filter>\n" +
                "  </get>\n" +
                "</rpc>\n";

        YangLibraryMessage yangLibraryMessage = new YangLibraryMessage(request);

        YangLibrary yangLibrary = yangLibraryMessage.getYangLibrary();
        yangLibrary.setCheckSum(TEST_STRING);

        ModulesState modulesState = yangLibraryMessage.getModulesState();
        modulesState.setModuleSetId(TEST_STRING);

        String response = yangLibraryMessage.buildRpcReplyResponse();
        assertTrue(response.contains(TEST_STRING));
        assertTrue(response.contains(NetconfProtocol.VERSION_1_0));
    }

}
