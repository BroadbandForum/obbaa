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

import java.util.concurrent.atomic.AtomicLong;

import org.broadband_forum.obbaa.ipfix.collector.service.CollectingService;
import org.broadband_forum.obbaa.ipfix.entities.util.IpfixConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class IpfixCollectorClientInitializer extends ChannelInitializer<SocketChannel> {
    private static final long IPFIX_COLLECTOR_MAX_CONNECTION = IpfixConstants.FE_MAX_CONNECTION;
    private static final Logger LOGGER = LoggerFactory.getLogger(IpfixCollectorClientInitializer.class);
    private CollectingService m_collectingService;
    private AtomicLong m_clientConnectedCount;

    public IpfixCollectorClientInitializer(CollectingService collectingService, AtomicLong clientConnectedCount) {
        this.m_collectingService = collectingService;
        this.m_clientConnectedCount = clientConnectedCount;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        LOGGER.debug("Initializing channel");
        socketChannel.config().setKeepAlive(false);
        socketChannel.closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                m_clientConnectedCount.decrementAndGet();
                LOGGER.debug(String.format("Channel {} is closed", channelFuture.channel()));
            }
        });
        if (m_clientConnectedCount.get() >= getIpfixCollectorMaxConnection()) {
            LOGGER.debug(String.format("Current tcp connection is %s", m_clientConnectedCount.get()));
            LOGGER.info(String.format("Max tcp connection is reached: %s", IPFIX_COLLECTOR_MAX_CONNECTION));
            socketChannel.close();
            return;
        }
        m_clientConnectedCount.incrementAndGet();
        LOGGER.debug(String.format("Current number of tcp connection is %s", m_clientConnectedCount.get()));
        IpfixCollectorTimeoutHandler timeoutHandler = createTimeoutHandler();
        socketChannel.pipeline()
                .addLast(new IpfixMessageDecoder())
                .addLast(timeoutHandler)
                .addLast(new IpfixCollectorHandler(m_collectingService));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error(String.format("Channel {} has exception. Closing channel", ctx.channel().remoteAddress()), cause);
        ctx.close();
    }

    // For UT
    protected long getIpfixCollectorMaxConnection() {
        return IPFIX_COLLECTOR_MAX_CONNECTION;
    }

    // For UT
    protected IpfixCollectorTimeoutHandler createTimeoutHandler() {
        return new IpfixCollectorTimeoutHandler();
    }
}
