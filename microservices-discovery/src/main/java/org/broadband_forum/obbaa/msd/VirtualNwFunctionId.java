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


public class VirtualNwFunctionId implements Serializable {

    private NetworkFunctionType networkFunctionType;
    private String softwareVersion;
    private String name;
    private String vendor;

    public VirtualNwFunctionId(NetworkFunctionType networkFunctionType, String softwareVersion, String name, String vendor) {
        this.networkFunctionType = networkFunctionType;
        this.softwareVersion = softwareVersion;
        this.name = name;
        this.vendor = vendor;
    }

    public VirtualNwFunctionId() {
    }

    public NetworkFunctionType getNetworkFunctionType() {
        return networkFunctionType;
    }

    public void setNetworkFunctionType(NetworkFunctionType networkFunctionType) {
        this.networkFunctionType = networkFunctionType;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VirtualNwFunctionId that = (VirtualNwFunctionId) o;

        if (getNetworkFunctionType() != that.getNetworkFunctionType()) return false;
        if (getSoftwareVersion() != null ? !getSoftwareVersion().equals(that.getSoftwareVersion()) : that.getSoftwareVersion() != null)
            return false;
        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) return false;
        return getVendor() != null ? getVendor().equals(that.getVendor()) : that.getVendor() == null;
    }

    @Override
    public int hashCode() {
        int result = getNetworkFunctionType() != null ? getNetworkFunctionType().hashCode() : 0;
        result = 31 * result + (getSoftwareVersion() != null ? getSoftwareVersion().hashCode() : 0);
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        result = 31 * result + (getVendor() != null ? getVendor().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "VirtualNwFunctionId{" +
                "networkFunctionType=" + networkFunctionType +
                ", softwareVersion='" + softwareVersion + '\'' +
                ", name='" + name + '\'' +
                ", vendor='" + vendor + '\'' +
                '}';
    }
}
