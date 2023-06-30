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

package org.broadband_forum.obbaa.ipfix.collector.threadpool;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.broadband_forum.obbaa.ipfix.entities.util.IpfixConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.ReadTimeoutHandler;

public class IpfixCollectorTimeoutHandler extends ReadTimeoutHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(IpfixCollectorTimeoutHandler.class);

    private static final int TCP_CONNECTION_TIMEOUT = IpfixConstants.getTcpConnectionTimeout();
    private long m_start;

    public IpfixCollectorTimeoutHandler() {
        this(TCP_CONNECTION_TIMEOUT, TimeUnit.MINUTES);
    }

    public IpfixCollectorTimeoutHandler(long timeout, TimeUnit unit) {
        super(timeout, unit);
        m_start = System.currentTimeMillis();
    }

    @Override
    protected void readTimedOut(ChannelHandlerContext ctx) throws Exception {
        super.readTimedOut(ctx);
        track(ctx);
    }

    private void track(ChannelHandlerContext ctx) {
        String address = getAddress(ctx);
        if (StringUtils.isNotEmpty(address)) {
            long readTimeOutAt = System.currentTimeMillis();
            LOGGER.info(null, "Connection from address [{}] is timeout after {} (minutes)", address, TCP_CONNECTION_TIMEOUT);
            LOGGER.debug(null, "Total life time of connection from [{}] is {} (ms) before timeout", address, readTimeOutAt - m_start);
        }
    }

    private String getAddress(ChannelHandlerContext ctx) {
        try {
            return ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
        } catch (Exception e) {
            LOGGER.warn(String.format("Error while getting address from connection"), e);
            return null;
        }
    }
}
