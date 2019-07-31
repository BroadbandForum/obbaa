package org.broadband_forum.obbaa.netconf.alarm.api;

import org.broadband_forum.obbaa.netconf.stack.logging.ApplicationName;

public enum LogAppNames implements ApplicationName {
    NETCONF_ALARM("netconf.alarm");

    private final String m_name;

    LogAppNames(String name) {
        m_name = name;
    }

    public String getName() {
        return m_name;
    }
}
