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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

import org.broadband_forum.obbaa.ipfix.collector.service.CollectingService;
import org.broadband_forum.obbaa.ipfix.entities.util.IpfixConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class IpfixCollectorServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(IpfixCollectorServer.class);
    private static final int IPFIX_COLLECTOR_PORT = IpfixConstants.FE_PORT;
    private AtomicLong m_clientConnectedCount = new AtomicLong(0);
    private int m_serverPort;
    private CollectingService m_collectingService;
    private Channel m_tcpChannel;
    private ExecutorService m_executorService;
    private ExecutorService m_msgProcessingExecutor;

    public IpfixCollectorServer(CollectingService collectingService,
                                ExecutorService executorService, ExecutorService msgProcessingExecutor) {
        m_collectingService = collectingService;
        this.m_executorService = executorService;
        this.m_msgProcessingExecutor = msgProcessingExecutor;
        this.m_serverPort = IPFIX_COLLECTOR_PORT;
    }

    public void startServer() {
        /*EventLoopGroup bossGroup = new NioEventLoopGroup(10, m_executorService);
        EventLoopGroup workerGroup = new NioEventLoopGroup(IpfixConstants.IPFIX_WORKER_GROUP, m_msgProcessingExecutor);*/
        EventLoopGroup bossGroup = new NioEventLoopGroup(10);
        EventLoopGroup workerGroup = new NioEventLoopGroup(IpfixConstants.IPFIX_WORKER_GROUP);
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new IpfixCollectorClientInitializer(m_collectingService, m_clientConnectedCount));
        try {
            LOGGER.info(String.format("Init tcp server at port: %s", m_serverPort));
            m_tcpChannel = serverBootstrap.bind(m_serverPort).sync().channel();
        } catch (InterruptedException e) {
            LOGGER.error(String.format("Interrupted while initializing TCP server", e));
        }
    }

    public void closeServer() {
        try {
            m_tcpChannel.close().await();
            LOGGER.info("Closing TCP Channel");
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted while shutting down the TCP server ", e);
        }
    }
}
