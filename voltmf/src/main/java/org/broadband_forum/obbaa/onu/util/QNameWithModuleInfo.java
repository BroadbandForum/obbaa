/*
 * Copyright 2020 Broadband Forum
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

package org.broadband_forum.obbaa.onu.util;

import java.net.URI;

import org.opendaylight.yangtools.yang.common.QName;

public class QNameWithModuleInfo {

    private final QName m_qname;
    private final String m_modulePrefix;
    private final String m_moduleName;
    private final String m_revision;

    public QNameWithModuleInfo(QName qname, String modulePrefix, String moduleName, String revision) {
        m_qname = qname;
        m_modulePrefix = modulePrefix;
        m_moduleName = moduleName;
        m_revision = revision;
    }

    public QName getQName() {
        return m_qname;
    }

    public static QNameWithModuleInfo create(String namespace, String revision, String localName, String prefix, String moduleName) {
        QName qname;
        if (revision == null) {
            qname = QName.create(namespace, localName);
        }
        else {
            qname = QName.create(namespace, revision, localName);
        }
        return new QNameWithModuleInfo(qname, prefix, moduleName, revision);
    }

    public String getModulePrefix() {
        return m_modulePrefix;
    }

    public URI getNamespace() {
        return m_qname.getNamespace();
    }

    public String getLocalName() {
        return m_qname.getLocalName();
    }

    public String getQualifiedName() {
        return m_modulePrefix + ":" + m_qname.getLocalName();
    }

    public String getModuleName() {
        return m_moduleName;
    }

    public String getRevision() {
        return m_revision;
    }

    @Override
    public int hashCode() {
        // m_modulePrefix does not have to determine identity
        return m_qname.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof QNameWithModuleInfo) {
            // m_modulePrefix does not have to determine identity
            return m_qname.equals(((QNameWithModuleInfo) obj).getQName());
        } else {
            return super.equals(obj);
        }
    }

    @Override
    public String toString() {
        return m_qname.toString() + "[prefix=" + m_modulePrefix + "]";
    }

}

