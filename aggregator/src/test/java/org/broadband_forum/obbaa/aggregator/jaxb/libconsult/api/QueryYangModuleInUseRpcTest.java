/*
 *   Copyright 2018 Broadband Forum
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

package org.broadband_forum.obbaa.aggregator.jaxb.libconsult.api;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 2018/11/29 16:57
 *
 */
public class QueryYangModuleInUseRpcTest {
    @Test
    public void getInstance() throws Exception {
        String queryUsedYangModules = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1542612595090\">\n"
                + "  <get>\n"
                + "    <filter type=\"subtree\">\n"
                + "      <in-use-library-modules xmlns=\"urn:bbf:yang:obbaa:module-library-check\" />\n"
                + "    </filter>\n"
                + "  </get>\n"
                + "</rpc>";
        QueryYangModuleInUseRpc queryYangModuleInUseRpc = QueryYangModuleInUseRpc.getInstance(queryUsedYangModules);
        assertNotNull(queryYangModuleInUseRpc.getUsedYangModules());
    }
    @Test
    public void getInstanceWithInvalidXml() throws Exception {
        String queryUsedYangModules = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1542612595090\">\n"
                + "  <get>\n"
                + "    <filter type=\"subtree\">\n"
                + "      <used-adapters xmlns=\"urn:bbf:yang:obbaa:module-library-check\" />\n"
                + "    </filter>\n"
                + "  </get>\n"
                + "</rpc>";
        QueryYangModuleInUseRpc queryYangModuleInUseRpc = QueryYangModuleInUseRpc.getInstance(queryUsedYangModules);
        assertNull(queryYangModuleInUseRpc.getUsedYangModules());
    }

    @Test
    public void getUsedYangModulesWithFilter() throws Exception {
        String queryUsedYangModules = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1543477791934\">\n"
                + "  <get-config>\n"
                + "    <source>\n"
                + "      <running />\n"
                + "    </source>\n"
                + "    <filter type=\"subtree\">\n"
                + "      <in-use-library-modules xmlns=\"urn:bbf:yang:obbaa:module-library-check\">\n"
                + "        <module>\n"
                + "           <name>bbf-fiber</name>\n"
                + "           <revision>2016-09-08</revision>\n"
                + "        </module>\n"
                + "      </in-use-library-modules>\n"
                + "    </filter>\n"
                + "  </get-config>\n"
                + "</rpc>";
        QueryYangModuleInUseRpc queryYangModuleInUseRpc = QueryYangModuleInUseRpc.getInstance(queryUsedYangModules);
        assertNotNull(queryYangModuleInUseRpc.getUsedYangModules().getYangModules().get(0));
    }

}