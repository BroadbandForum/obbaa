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

package org.broadband_forum.obbaa.pma.rest;

import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.POST;

import org.broadband_forum.obbaa.pma.PmaRegistry;
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
 * Created by kbhatk on 9/10/17.
 */
@Api(tags = {"Persistence Management Agent"}, description = "Perform netonf operations on a single PMA instance")
@RequestMapping("/baa/pma")
public class PmaRestEP {
    private final PmaRegistry m_pmaRegistry;

    public PmaRestEP(PmaRegistry pmaRegistry) {
        m_pmaRegistry = pmaRegistry;
    }

    @ApiOperation(value = "Execute a netconf operation on a PMA")
    @POST
    @RequestMapping(value = "/{deviceName}/executeNC", produces = "application/xml", consumes = "application/xml",
            method = RequestMethod.POST)
    @ResponseBody
    public String executeNC(@ApiParam("Unique name of the device") @PathVariable("deviceName") String deviceName,
                            @ApiParam("Netconf request to be executed") @RequestBody String netconfRequest) throws IllegalArgumentException,
            ExecutionException {
        return m_pmaRegistry.executeNC(deviceName, netconfRequest);
    }

    @ApiOperation(value = "Perform full alignment of the PMA configurations with the device")
    @POST
    @RequestMapping(value = "/{deviceName}/forceAlign", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void forceAlign(@ApiParam("Unique name of the device") @PathVariable("deviceName") String deviceName) throws
            IllegalArgumentException,
            ExecutionException {
        m_pmaRegistry.forceAlign(deviceName);
    }

    @ApiOperation(value = "Scan the YANG deployment directory for new YANG modules and load them")
    @POST
    @RequestMapping(value = "/reloadmodel", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public List<String> reloadDeviceModel() {
        return m_pmaRegistry.reloadDeviceModel();
    }

}
