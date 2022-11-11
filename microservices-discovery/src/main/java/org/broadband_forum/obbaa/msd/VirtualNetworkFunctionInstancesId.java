/*
 * Copyright 2022 Broadband Forum
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

package org.broadband_forum.obbaa.msd;

import java.io.Serializable;

public class VirtualNetworkFunctionInstancesId implements Serializable {
    private String virtualNetworkFunctionInstanceName;
    private String virtualNetworkFunction;
    private String adminState;
    private OperState operState;

    public VirtualNetworkFunctionInstancesId(String virtualNetworkFunctionInstanceName, String virtualNetworkFunction, String adminState, OperState operState) {
        this.virtualNetworkFunctionInstanceName = virtualNetworkFunctionInstanceName;
        this.virtualNetworkFunction = virtualNetworkFunction;
        this.adminState = adminState;
        this.operState = operState;
    }

    public VirtualNetworkFunctionInstancesId() {
    }

    public String getVirtualNetworkFunctionInstanceName() {
        return virtualNetworkFunctionInstanceName;
    }

    public void setVirtualNetworkFunctionInstanceName(String virtualNetworkFunctionInstanceName) {
        this.virtualNetworkFunctionInstanceName = virtualNetworkFunctionInstanceName;
    }

    public String getVirtualNetworkFunction() {
        return virtualNetworkFunction;
    }

    public void setVirtualNetworkFunction(String virtualNetworkFunction) {
        this.virtualNetworkFunction = virtualNetworkFunction;
    }

    public String getAdminState() {
        return adminState;
    }

    public void setAdminState(String adminState) {
        this.adminState = adminState;
    }

    public OperState getOperState() {
        return operState;
    }

    public void setOperState(OperState operState) {
        this.operState = operState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VirtualNetworkFunctionInstancesId that = (VirtualNetworkFunctionInstancesId) o;

        if (getVirtualNetworkFunctionInstanceName() != null ? !getVirtualNetworkFunctionInstanceName().equals(that.getVirtualNetworkFunctionInstanceName()) : that.getVirtualNetworkFunctionInstanceName() != null)
            return false;
        if (getVirtualNetworkFunction() != null ? !getVirtualNetworkFunction().equals(that.getVirtualNetworkFunction()) : that.getVirtualNetworkFunction() != null)
            return false;
        if (getAdminState() != null ? !getAdminState().equals(that.getAdminState()) : that.getAdminState() != null)
            return false;
        return getOperState() == that.getOperState();
    }

    @Override
    public int hashCode() {
        int result = getVirtualNetworkFunctionInstanceName() != null ? getVirtualNetworkFunctionInstanceName().hashCode() : 0;
        result = 31 * result + (getVirtualNetworkFunction() != null ? getVirtualNetworkFunction().hashCode() : 0);
        result = 31 * result + (getAdminState() != null ? getAdminState().hashCode() : 0);
        result = 31 * result + (getOperState() != null ? getOperState().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "VirtualNetworkFunctionInstancesId{" +
                "virtualNetworkFunctionInstanceName='" + virtualNetworkFunctionInstanceName + '\'' +
                ", virtualNetworkFunction='" + virtualNetworkFunction + '\'' +
                ", adminState='" + adminState + '\'' +
                ", operState=" + operState +
                '}';
    }
}
