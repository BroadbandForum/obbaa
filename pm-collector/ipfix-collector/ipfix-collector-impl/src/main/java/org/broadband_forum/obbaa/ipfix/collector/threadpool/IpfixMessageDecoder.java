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

import java.util.List;

import org.broadband_forum.obbaa.ipfix.collector.entities.IpfixMessagesWrapper;
import org.broadband_forum.obbaa.ipfix.entities.exception.UtilityException;
import org.broadband_forum.obbaa.ipfix.entities.message.header.IpfixMessageHeader;
import org.broadband_forum.obbaa.ipfix.entities.util.IpfixUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class IpfixMessageDecoder extends ByteToMessageDecoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(IpfixMessageDecoder.class);
    private static final String NUMBER_OF_MESSAGE_BUFFER_ENV = "NUMBER_OF_MESSAGE_BUFFER";
    private static final long NUMBER_OF_MESSAGE_BUFFER_DEFAULT = 1000;
    private static final long NUMBER_OF_MESSAGE_BUFFER = getNumberOfMessageBuffer();

    private static long getNumberOfMessageBuffer() {
        long noMessageBuffer = NUMBER_OF_MESSAGE_BUFFER_DEFAULT;
        try {
            String noMessageBufferAsString = System.getenv(NUMBER_OF_MESSAGE_BUFFER_ENV);
            if (noMessageBufferAsString != null && !noMessageBufferAsString.isEmpty()) {
                noMessageBuffer = Long.valueOf(noMessageBufferAsString);
            }
        } catch (Exception e) {
            LOGGER.debug("Error while retrieving environment set value for message buffer");
        }
        return noMessageBuffer;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        byte[] array = new byte[msg.readableBytes()];
        msg.getBytes(msg.readerIndex(), array);
        handleCreateIpfixMessages(array, out, msg);
    }

    private void handleCreateIpfixMessages(byte[] checkedByte, List<Object> out, ByteBuf byteBuf) throws UtilityException {
        byte[] remainingByteAfterCheck = checkedByte;
        while (true) {
            if (remainingByteAfterCheck.length < IpfixMessageHeader.TOTAL_LENGTH) {
                // Not enough bytes for header. Continue
                LOGGER.debug(String.format("Checked bytes: %s is smaller than header size. Keep waiting...",
                        remainingByteAfterCheck.length));
                break;
            }
            byte[] messageLength = IpfixUtilities.copyByteArray(remainingByteAfterCheck, IpfixMessageHeader.VERSION_NUBMER_FIELD_LENGTH,
                    IpfixMessageHeader.LENGTH_FIELD_LENGTH);
            int messageLengthAsInt = IpfixUtilities.byteToInteger(messageLength, 1, 2);
            if (remainingByteAfterCheck.length < messageLengthAsInt) {
                // Not enough bytes for data. Continue
                LOGGER.debug(String.format("Checked bytes: %s is smaller than IPFIX message size. Keep waiting...",
                        remainingByteAfterCheck.length, messageLengthAsInt));
                break;
            }
            byte[] completedIpfixMsgAsByte = IpfixUtilities.copyByteArray(remainingByteAfterCheck, 0, messageLengthAsInt);
            LOGGER.debug(String.format("The message length received in HEX: %s, messageLengthAsInt: %s, and completed "
                            + "IPFIX message in HEX: %s", IpfixUtilities.bytesToHex(messageLength),
                    messageLengthAsInt, IpfixUtilities.bytesToHex(completedIpfixMsgAsByte)));
            if (messageLengthAsInt == 0) {
                LOGGER.error("The message length is zero");
                throw new IllegalArgumentException("The message length is zero");
            }
            IpfixMessagesWrapper ipfixMessagesWrapper = new IpfixMessagesWrapper(completedIpfixMsgAsByte);
            out.add(ipfixMessagesWrapper);
            ByteBuf slicedBuf = byteBuf.readSlice(completedIpfixMsgAsByte.length);
            slicedBuf.clear();
            if (out.size() > NUMBER_OF_MESSAGE_BUFFER) {
                LOGGER.warn(String.format("Reach maximum of buffer for IPFIXMessageWrapper. Current size %s. Remaining data size in"
                        + " byteBuffer %s", out.size(), remainingByteAfterCheck.length));
                break;
            }
            remainingByteAfterCheck = IpfixUtilities.copyByteArray(remainingByteAfterCheck, messageLengthAsInt,
                    remainingByteAfterCheck.length - messageLengthAsInt);
            LOGGER.debug(String.format("The wrapper message count: %s, remainingByteAfterCheck length: %s", out.size(),
                    remainingByteAfterCheck.length));
        }
    }
}
