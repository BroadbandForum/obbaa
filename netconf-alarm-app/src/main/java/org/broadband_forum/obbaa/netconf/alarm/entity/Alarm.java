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

package org.broadband_forum.obbaa.netconf.alarm.entity;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity(name = "Alarm")
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {Alarm.SOURCE_OBJECT, Alarm.ALARM_TYPE_ID}),
        indexes = {@Index(name = "sourceObject_index", columnList = "sourceObject"),
                @Index(name = "deviceId_index", columnList = "deviceId")})
public class Alarm {

    public static final String SOURCE_OBJECT = "sourceObject";
    public static final String ALARM_TYPE_ID = "alarmTypeId";
    public static final String ALARM_TYPE_QUALIFIER = "alarmTypeQualifier";
    public static final String DEVICE_ID = "deviceId";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column
    private Long id;

    @Column
    private String sourceObject;

    @Column
    private String deviceId;

    @Column
    private Timestamp raisedTime;

    @Column
    @Enumerated(EnumType.STRING)
    private AlarmSeverity severity;

    @Column(length = 1000)
    private String alarmText;

    @Column
    private boolean yangResource;


    @Column(nullable = false)
    private String alarmTypeId;

    @Column
    private String alarmTypeQualifier;

    public String getAlarmTypeId() {
        return alarmTypeId;
    }

    public void setAlarmTypeId(String alarmTypeId) {
        this.alarmTypeId = alarmTypeId;
    }

    public String getAlarmTypeQualifier() {
        return alarmTypeQualifier;
    }

    public void setAlarmTypeQualifier(String alarmTypeQualifier) {
        this.alarmTypeQualifier = alarmTypeQualifier;
    }

    private String resourceNamespaces;

    private String resourceName;

    public String getResourceNamespaces() {
        return resourceNamespaces;
    }

    public void setResourceNamespaces(String resourceNamespaces) {
        this.resourceNamespaces = resourceNamespaces;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSourceObject() {
        return sourceObject;
    }

    public void setSourceObject(String sourceObject) {
        this.sourceObject = sourceObject;
    }

    public Timestamp getRaisedTime() {
        return raisedTime;
    }

    public void setRaisedTime(Timestamp raisedTime) {
        this.raisedTime = raisedTime;
    }

    public AlarmSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(AlarmSeverity severity) {
        this.severity = severity;
    }

    public String getAlarmText() {
        return alarmText;
    }

    public void setAlarmText(String alarmText) {
        this.alarmText = alarmText;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public boolean isYangResource() {
        return yangResource;
    }

    public void setYangResource(boolean yangResource) {
        this.yangResource = yangResource;
    }

    @Override
    public String toString() {
        return "Alarm [id = " + id + ", sourceObject= " + sourceObject + ", deviceId = " + deviceId + ", alarmTypeId = "
                + alarmTypeId + ", alarmTypeQualifier = " + alarmTypeQualifier + ", raisedTime = " + raisedTime + ", severity= "
                + severity + ", alarmText = " + alarmText + ", yangResource = " + yangResource + "]";
    }
}