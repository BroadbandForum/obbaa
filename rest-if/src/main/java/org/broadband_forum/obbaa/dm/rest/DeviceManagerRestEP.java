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

package org.broadband_forum.obbaa.dm.rest;

import java.util.List;
import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;

import org.broadband_forum.obbaa.connectors.sbi.netconf.NewDeviceInfo;
import org.broadband_forum.obbaa.dm.DeviceManager;
import org.broadband_forum.obbaa.store.dm.DeviceInfo;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

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

    @ApiOperation(value = "Start managing a new device", position = 0)
    @PUT
    @RequestMapping(consumes = {"application/json"}, method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK)
    public void createDevice(@ApiParam(value = "details of the device") @RequestBody DeviceInfo deviceInfo) {
        m_deviceManager.createDevice(deviceInfo);
    }

    @ApiOperation(value = "Get details of a managed device", position = 1)
    @RequestMapping(value = "/{deviceName}", method = RequestMethod.GET)
    @ResponseBody
    public DeviceInfo getDevice(@ApiParam(value = "unique name of the device", defaultValue = "AccessNode1")
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
    public Set<DeviceInfo> getAllDevices() {
        return m_deviceManager.getAllDevices();
    }

    @ApiOperation(value = "Update details of a managed device", position = 3)
    @POST
    @RequestMapping(consumes = {"application/json"}, method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void updateDevice(@ApiParam(value = "details of the device") @RequestBody DeviceInfo deviceInfo) {
        m_deviceManager.updateDevice(deviceInfo);
    }

    @ApiOperation(value = "Stop managing a device", position = 4)
    @DELETE
    @RequestMapping(value = "/{deviceName}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteDevice(@PathVariable("deviceName") String deviceName) {
        m_deviceManager.deleteDevice(deviceName);
    }

}
