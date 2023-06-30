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

package org.broadband_forum.obbaa.modelabstracter.converter;

import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;

import org.broadband_forum.obbaa.modelabstracter.ConvertRet;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;

import java.io.IOException;

import javax.xml.bind.JAXBException;

/**
 * Abstract converter.
 */
@Slf4j
public abstract class AbstractModelConverter implements ModelConverter {
    @Override
    public ConvertRet convert(String request) throws NetconfMessageBuilderException {
        try {
            return processRequest(request);
        } catch (JAXBException | TemplateException | IOException e) {
            log.error("failed to convert request", e);
            throw new NetconfMessageBuilderException(e);
        }
    }

    /**
     * Process NETCONF request message.
     *
     * @param request NETCONF request
     * @return config elements
     */
    public abstract ConvertRet processRequest(String request)
        throws JAXBException, TemplateException, IOException, NetconfMessageBuilderException;
}
