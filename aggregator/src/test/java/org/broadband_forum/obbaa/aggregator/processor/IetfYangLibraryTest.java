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

package org.broadband_forum.obbaa.aggregator.processor;

import org.broadband_forum.obbaa.aggregator.api.Aggregator;
import org.broadband_forum.obbaa.aggregator.impl.AggregatorImpl;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class IetfYangLibraryTest {
    private Aggregator aggregator;
    private IetfYangLibrary ietfYangLibrary;

    @Before
    public void setUp() throws Exception {
        aggregator = new AggregatorImpl();
        ietfYangLibrary = new IetfYangLibrary(aggregator);
        ietfYangLibrary.init();
    }

    @Test
    public void processRequest() throws Exception {
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"152026367257\">\n" +
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

        String response = ietfYangLibrary.processRequest(request);
        assertTrue(response.contains("yang-library"));
    }

    @Test
    public void processRequestModulesState() throws Exception {
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"152026367257\">\n" +
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

        String response = ietfYangLibrary.processRequest(request);
        assertTrue(response.contains("modules-state"));
    }

    @Test
    public void processRequestModulesStateFilter() throws Exception {
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"152026367257\">\n" +
                        "  <get>\n" +
                        "    <source>\n" +
                        "      <running />\n" +
                        "    </source>\n" +
                        "    <filter type=\"subtree\">\n" +
                        "      <modules-state xmlns=\"urn:ietf:params:xml:ns:yang:ietf-yang-library\">\n" +
                        "     <module-set-id>all</module-set-id>\n" +
                        "     </modules-state>\n" +
                        "    </filter>\n" +
                        "  </get>\n" +
                        "</rpc>\n";

        String response = ietfYangLibrary.processRequest(request);
        assertTrue(response.contains("modules-state"));
    }

    @Test
    public void processRequestFilter() throws Exception {
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"152026367257\">\n" +
                        "  <get>\n" +
                        "    <source>\n" +
                        "      <running />\n" +
                        "    </source>\n" +
                        "    <filter type=\"subtree\">\n" +
                        "      <yang-library xmlns=\"urn:ietf:params:xml:ns:yang:ietf-yang-library\">\n" +
                        "            <modules>\n" +
                        "                <module>\n" +
                        "                    <id>urn:ietf:params:xml:ns:yang:ietf-yang-library?module=ietf-yang-library&amp;revision=2017-10-30</id>\n" +
                        "                </module>\n" +
                        "            </modules>\n" +
                        "            <module-sets>\n" +
                        "                <module-set>\n" +
                        "                    <id>System-global</id>\n" +
                        "                </module-set>\n" +
                        "            </module-sets>\n" +
                        "     </yang-library>\n" +
                        "      <modules-state xmlns=\"urn:ietf:params:xml:ns:yang:ietf-yang-library\">\n" +
                        "     </modules-state>\n" +
                        "    </filter>\n" +
                        "  </get>\n" +
                        "</rpc>\n";

        String response = ietfYangLibrary.processRequest(request);
        assertTrue(response.contains("yang-library"));
    }
}
