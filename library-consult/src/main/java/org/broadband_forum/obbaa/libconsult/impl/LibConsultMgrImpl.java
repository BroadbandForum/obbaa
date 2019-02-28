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

package org.broadband_forum.obbaa.libconsult.impl;

import org.broadband_forum.obbaa.libconsult.LibConsultMgr;
import org.broadband_forum.obbaa.libconsult.LibInfoChecker;
import org.broadband_forum.obbaa.libconsult.LibInfoInvestigator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class LibConsultMgrImpl implements LibConsultMgr {

    private LibInfoChecker m_libInfoChecker;
    private LibInfoInvestigator m_libInfoInvestigator;

    public LibConsultMgrImpl(LibInfoChecker libInfoChecker, LibInfoInvestigator libInfoInvestigator) {
        this.m_libInfoChecker = libInfoChecker;
        this.m_libInfoInvestigator = libInfoInvestigator;
    }

    @Override
    public Element getSpecificUsedYangModules(String moduleName, String revisionDate, Document responseDoc) {
        return m_libInfoChecker.getSpecificUsedYangModules(moduleName, revisionDate, responseDoc);
    }

    @Override
    public Element getAllUsedYangModules(Document responseDoc) {
        return m_libInfoChecker.getAllUsedYangModules(responseDoc);
    }

    @Override
    public Element getUsedYangModulesPerDevice(Document responseDoc, String devName) {
        return m_libInfoChecker.getUsedYangModulesPerDevice(responseDoc, devName);
    }
}
