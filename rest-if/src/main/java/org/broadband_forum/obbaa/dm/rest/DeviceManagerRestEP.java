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

package org.broadband_forum.obbaa.nm.devicemanager.rest;

import java.util.List;

import javax.ws.rs.GET;

import org.broadband_forum.obbaa.connectors.sbi.netconf.NewDeviceInfo;
import org.broadband_forum.obbaa.nm.devicemanager.DeviceManager;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Created by kbhatk on 3/10/17.
 */
@Api(tags = {"Device Administration"}, description = "Create/Read/Delete/Update details of a device")
@RequestMapping("/baa/dm")
public class DeviceManagerRestEP {

    private DeviceManager m_deviceManager;

    public DeviceManagerRestEP(DeviceManager deviceManager) {
        this.m_deviceManager = deviceManager;
    }

    @ApiOperation(value = "Get details of a managed device", position = 1)
    @RequestMapping(value = "/{deviceName}", method = RequestMethod.GET)
    @ResponseBody
    public Device getDevice(@ApiParam(value = "unique name of the device", defaultValue = "AccessNode1")
                            @PathVariable("deviceName") String deviceName) {
        return m_deviceManager.getDevice(deviceName);
    }

    @ApiOperation(value = "Get details of all un-managed netconf call-home devices", position = 5)
    @GET
    @RequestMapping(value = "/newdevices", method = RequestMethod.GET)
    @ResponseBody
    public List<NewDeviceInfo> getNewDevices() {
        return m_deviceManager.getNewDevices();
    }

    @ApiOperation(value = "Get details of all managed devices", position = 2)
    @GET
    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    public List<Device> getAllDevices() {
        return m_deviceManager.getAllDevices();
    }

}
