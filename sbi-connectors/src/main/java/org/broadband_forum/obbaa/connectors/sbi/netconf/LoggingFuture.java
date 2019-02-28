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

package org.broadband_forum.obbaa.connectors.sbi.netconf;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.dmyang.entities.Device;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.jetbrains.annotations.NotNull;

public class LoggingFuture implements Future<NetConfResponse> {
    private static final Logger LOGGER = Logger.getLogger(LoggingFuture.class);
    private final Future<NetConfResponse> m_innerFuture;
    private final Device m_device;

    public LoggingFuture(Device device, Future<NetConfResponse> future) {
        m_device = device;
        m_innerFuture = future;
    }

    @Override
    public boolean cancel(boolean bool) {
        return m_innerFuture.cancel(bool);
    }

    @Override
    public boolean isCancelled() {
        return m_innerFuture.isCancelled();
    }

    @Override
    public boolean isDone() {
        return m_innerFuture.isDone();
    }

    @Override
    public NetConfResponse get() throws InterruptedException, ExecutionException {
        NetConfResponse netConfResponse = m_innerFuture.get();
        LOGGER.info(String.format("Got response for device %s, response %s", m_device, netConfResponse
                .responseToString()));
        return netConfResponse;
    }

    @Override
    public NetConfResponse get(long value, @NotNull TimeUnit timeUnit) throws InterruptedException,
            ExecutionException,
            TimeoutException {
        NetConfResponse netConfResponse = m_innerFuture.get(value, timeUnit);
        LOGGER.info(String.format("Got response for device %s, response %s", m_device, netConfResponse
                .responseToString()));
        return netConfResponse;
    }
}

