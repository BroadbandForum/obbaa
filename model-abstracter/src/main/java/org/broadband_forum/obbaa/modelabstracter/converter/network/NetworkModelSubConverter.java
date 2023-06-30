/*
 *   Copyright 2022 Broadband Forum
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

import org.broadband_forum.obbaa.modelabstracter.ConvertRet;
import org.broadband_forum.obbaa.modelabstracter.jaxb.network.Networks;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;

import java.io.IOException;

/**
 * The interface of sub-network converter.
 */
public interface NetworkModelSubConverter {
    /**
     * convert network request.
     *
     * @param networks network model
     * @return convert result
     * @throws NetconfMessageBuilderException NETCONF message build exception
     * @throws TemplateException process template exception
     * @throws IOException read template file exception
     */
    ConvertRet convert(Networks networks) throws NetconfMessageBuilderException, TemplateException, IOException;
}
