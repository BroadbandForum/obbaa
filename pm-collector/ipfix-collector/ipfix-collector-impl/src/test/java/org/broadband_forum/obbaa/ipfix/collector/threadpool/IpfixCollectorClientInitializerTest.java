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


import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicLong;

import org.broadband_forum.obbaa.ipfix.collector.service.CollectingService;
import org.junit.Before;
import org.junit.Test;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.SocketChannelConfig;

public class IpfixCollectorClientInitializerTest {

    private static final int MAX_CONNECTION = 10;
    private static final int INCOMING_CONNECTION = 20;
    private CollectingService m_collectingService;
    private AtomicLong m_clientConnectedCount;

    @Before
    public void setup() {
        m_clientConnectedCount = new AtomicLong();
        m_collectingService = mock(CollectingService.class);
    }

    @Test
    public void testInitChannel() throws Exception {
        for (int i = 0; i < INCOMING_CONNECTION; i++) {
            IpfixCollectorClientInitializer client = createClient();

            SocketChannel chanel = createChanel();
            client.initChannel(chanel);
        }
        assertEquals(MAX_CONNECTION, m_clientConnectedCount.get());
    }

    @Test
    public void testInitChannelWithTimeoutHandler() throws Exception {
        IpfixCollectorTimeoutHandler timeoutHandler = mock(IpfixCollectorTimeoutHandler.class);

        IpfixCollectorClientInitializer client = new IpfixCollectorClientInitializer(m_collectingService, m_clientConnectedCount) {
            @Override
            protected IpfixCollectorTimeoutHandler createTimeoutHandler() {
                return timeoutHandler;
            }
        };

        ChannelHandlerContext context = mock(ChannelHandlerContext.class);
        doAnswer(invocationOnMock -> {
            ChannelHandlerContext channelHandlerContext = (ChannelHandlerContext) invocationOnMock.getArguments()[0];
            channelHandlerContext.close();
            return null;
        }).when(timeoutHandler).readTimedOut(any());
        SocketChannel channel = createChannelWithMockTimeoutHandler(context);
        client.initChannel(channel);

        verify(context).close();
    }

    private IpfixCollectorClientInitializer createClient() {
        return new IpfixCollectorClientInitializer(m_collectingService, m_clientConnectedCount) {

            @Override
            protected long getIpfixCollectorMaxConnection() {
                return MAX_CONNECTION;
            }
        };
    }

    private SocketChannel createChanel() throws InterruptedException {
        SocketChannel socketChannel = mock(SocketChannel.class);

        SocketChannelConfig config = mock(SocketChannelConfig.class);
        when(socketChannel.config()).thenReturn(config);

        ChannelFuture closeFuture = createCloseFuture();
        when(socketChannel.closeFuture()).thenReturn(closeFuture);
        when(socketChannel.close()).thenAnswer((invocation -> {
            closeFuture.sync();
            return null;
        }));

        ChannelPipeline channelPipeline = mock(ChannelPipeline.class);
        when(channelPipeline.addLast(any())).thenReturn(channelPipeline);

        when(socketChannel.pipeline()).thenReturn(channelPipeline);

        return socketChannel;
    }

    private ChannelFuture createCloseFuture() throws InterruptedException {
        final ChannelFutureListener[] listeners = new ChannelFutureListener[1];
        ChannelFuture closeFuture = mock(ChannelFuture.class);

        Channel channel = mock(Channel.class);
        SocketAddress remoteAddress = mock(SocketAddress.class);
        when(channel.remoteAddress()).thenReturn(remoteAddress);
        when(closeFuture.channel()).thenReturn(channel);
        when(closeFuture.addListener(any())).then(invocationOnMock -> {

            listeners[0] = (ChannelFutureListener) invocationOnMock.getArguments()[0];
            return null;
        });

        when(closeFuture.sync()).then(invocation -> {
            listeners[0].operationComplete(closeFuture);
            return null;
        });

        return closeFuture;
    }

    private SocketChannel createChannelWithMockTimeoutHandler(ChannelHandlerContext context) throws InterruptedException {
        SocketChannel socketChannel = mock(SocketChannel.class);

        SocketChannelConfig config = mock(SocketChannelConfig.class);
        when(socketChannel.config()).thenReturn(config);

        ChannelFuture closeFuture = createCloseFuture();
        when(socketChannel.closeFuture()).thenReturn(closeFuture);
        when(socketChannel.close()).thenAnswer((invocation -> {
            closeFuture.sync();
            return null;
        }));

        ChannelPipeline channelPipeline = mock(ChannelPipeline.class);

        when(channelPipeline.addLast(any())).then(invocationOnMock -> {
            Object obj = invocationOnMock.getArguments()[0];
            if (obj instanceof IpfixCollectorTimeoutHandler) {
                IpfixCollectorTimeoutHandler ipfixCollectorTimeoutHandler = (IpfixCollectorTimeoutHandler) obj;
                ipfixCollectorTimeoutHandler.readTimedOut(context);
            }
            return channelPipeline;
        });

        when(socketChannel.pipeline()).thenReturn(channelPipeline);

        return socketChannel;
    }
}
