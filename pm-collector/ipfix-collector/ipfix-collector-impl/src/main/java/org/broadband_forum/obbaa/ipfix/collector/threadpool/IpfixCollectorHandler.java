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

import static org.broadband_forum.obbaa.ipfix.entities.util.IpfixUtilities.bytesToHex;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.broadband_forum.obbaa.ipfix.collector.entities.IpfixMessagesWrapper;
import org.broadband_forum.obbaa.ipfix.collector.service.CollectingService;
import org.broadband_forum.obbaa.ipfix.entities.exception.CollectingProcessException;
import org.broadband_forum.obbaa.ipfix.entities.exception.NotEnoughBytesException;
import org.broadband_forum.obbaa.ipfix.entities.message.header.IpfixMessageHeader;
import org.broadband_forum.obbaa.ipfix.entities.util.IpfixUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class IpfixCollectorHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(IpfixCollectorHandler.class);
    private CollectingService m_collectingService;
    private Optional<String> m_hostName;
    private Optional<Long> m_obsvDomain;

    public IpfixCollectorHandler(CollectingService collectingService) {
        this.m_collectingService = collectingService;
        m_hostName = Optional.empty();
        m_obsvDomain = Optional.empty();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object in) throws Exception {
        IpfixMessagesWrapper ipfixMessagesWrapper = null;
        SocketAddress remoteAddress = ctx.channel().remoteAddress();
        try {
            ipfixMessagesWrapper = (IpfixMessagesWrapper) in;
            IpfixMessageHeader header = new IpfixMessageHeader(ipfixMessagesWrapper.getData());
            byte[] bodyData = new byte[header.getLength() - IpfixMessageHeader.TOTAL_LENGTH];
            byte[] messageData = IpfixUtilities.concatByteArray(ipfixMessagesWrapper.getData(), bodyData);
            // Remote address need to be decode in messages
            String hostAddress = Objects.nonNull(remoteAddress) ? ((InetSocketAddress) remoteAddress).getAddress().getHostAddress() : "";

            Map.Entry<Optional<Long>, Optional<String>> entry = m_collectingService.collect(messageData, hostAddress, m_hostName);
            if (entry != null) {
                m_obsvDomain = entry.getKey();
                m_hostName = entry.getValue();
            }
            if (!m_hostName.isPresent()) {
                LOGGER.warn(String.format("Couldn't find hostname, message %s will be ignored", bytesToHex(messageData)));
            }
        } catch (CollectingProcessException e) {
            m_collectingService.processDecodeFailure();
            LOGGER.error(String.format("Failed to collect IPFIX data from connection %s",
                    m_hostName.orElse(String.valueOf(remoteAddress))), e);
            ctx.close();
        } catch (NotEnoughBytesException | NegativeArraySizeException | IllegalArgumentException e) {
            m_collectingService.processDecodeFailure();
            LOGGER.error(String.format("Host %s send: Malformed IPFIX message, connection closed. Decoded data: %s",
                    m_hostName.orElse(String.valueOf(remoteAddress)), IpfixUtilities.bytesToHex(ipfixMessagesWrapper.getData())), e);
            ctx.close();
        } catch (Exception e) {
            m_collectingService.processMessageCount();
            LOGGER.error(String.format("Unhandled exception from connection %s. Decoded data: %s",
                    m_hostName.orElse(String.valueOf(remoteAddress)),
                    IpfixUtilities.bytesToHex(ipfixMessagesWrapper.getData())), e);
            ctx.close();
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.debug("Incoming connection, start collecting process");
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // Clear hostname cache
        LOGGER.error(String.format("The connection from %s is closed", m_hostName));
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("Channel has exception. Closing channel", cause);
        ctx.close();
    }

}
