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

package org.broadband_forum.obbaa.netconf.alarm.api;

import java.util.List;

import org.broadband_forum.obbaa.netconf.alarm.util.AlarmConstants;
import org.broadband_forum.obbaa.netconf.alarm.util.AlarmsDocumentTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfNotification;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Element;

public class DefaultAlarmStateChangeNotification extends NetconfNotification {

    public static final QName TYPE = QName.create(AlarmConstants.ALARM_NAMESPACE, "alarm-notification");
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(DefaultAlarmStateChangeNotification.class,
            LogAppNames.NETCONF_ALARM);
    private List<AlarmNotification> m_alarms;

    private SchemaRegistry m_schemaRegistry;
    private ModelNodeDataStoreManager m_dsm;

    public DefaultAlarmStateChangeNotification(SchemaRegistry schemaRegistry, ModelNodeDataStoreManager dsm) {
        m_schemaRegistry = schemaRegistry;
        m_dsm = dsm;
    }

    public List<AlarmNotification> getAlarmNotification() {
        return m_alarms;
    }

    public void setAlarmNotification(List<AlarmNotification> alarms) {
        this.m_alarms = alarms;
    }

    @Override
    public Element getNotificationElement() {
        Element alarmNotificationElement = super.getNotificationElement();
        if (alarmNotificationElement == null) {
            alarmNotificationElement = getAlarmNotificationElement();
            setNotificationElement(alarmNotificationElement);
        }
        return alarmNotificationElement;
    }

    private Element getAlarmNotificationElement() {
        try {
            AlarmsDocumentTransformer transformer = new AlarmsDocumentTransformer(m_schemaRegistry, m_dsm);
            return transformer.getAlarmNotificationElement(m_alarms);
        } catch (NetconfMessageBuilderException e) {
            LOGGER.error(null, "Error while getting alarm notification element. ", e);
        }
        return null;
    }

    @Override
    public String toString() {
        return "DefaultAlarmStateChangeNotification [alarms=" + m_alarms + "]";
    }

    @Override
    public QName getType() {
        return TYPE;
    }

    // Only for UT
    public ModelNodeDataStoreManager getModelNodeDSM() {
        return m_dsm;
    }
}
