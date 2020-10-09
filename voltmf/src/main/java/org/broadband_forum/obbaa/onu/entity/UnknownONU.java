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

package org.broadband_forum.obbaa.onu.entity;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * <p>
 * Entity to persist unknown ONUs in DB
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 22/07/2020.
 */
@Entity(name = "UnknownONU")
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {UnknownONU.SERIAL_NUMBER}))
public class UnknownONU {

    public static final String SERIAL_NUMBER = "serialNumber";

    @Id
    @Column
    private String serialNumber;

    @Column
    private String registrationId;

    @Column
    private String onuState;

    @Column
    private String vAniRef;

    @Column
    private String oltDeviceName;

    @Column
    private String channelTermRef;

    @Column
    private String onuID;

    @Column
    private Timestamp onuStateLastChange;

    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getOnuState() {
        return onuState;
    }

    public void setOnuState(String onuState) {
        this.onuState = onuState;
    }

    public String getOltDeviceName() {
        return oltDeviceName;
    }

    public void setOltDeviceName(String oltDeviceName) {
        this.oltDeviceName = oltDeviceName;
    }

    public String getVAniRef() {
        return vAniRef;
    }

    public void setVAniRef(String vaniRef) {
        this.vAniRef = vaniRef;
    }

    public String getChannelTermRef() {
        return channelTermRef;
    }

    public void setChannelTermRef(String channelTermRef) {
        this.channelTermRef = channelTermRef;
    }

    public Timestamp getOnuStateLastChange() {
        return onuStateLastChange;
    }

    public void setOnuStateLastChange(Timestamp onuStateLastChange) {
        this.onuStateLastChange = onuStateLastChange;
    }

    public String getOnuId() {
        return onuID;
    }

    public void setOnuID(String onuID) {
        this.onuID = onuID;
    }

    @Override
    public String toString() {
        return "UnknownONU [serialNumber = " + serialNumber + ", registrationId = " + registrationId + ", vAniRef = "
                + vAniRef + ", channel Termination Ref = " + channelTermRef + ", ONU state last change = "
                + onuStateLastChange + ", ONU ID = "
                + onuID + ", ONU state = " + onuState + "]";
    }

}
