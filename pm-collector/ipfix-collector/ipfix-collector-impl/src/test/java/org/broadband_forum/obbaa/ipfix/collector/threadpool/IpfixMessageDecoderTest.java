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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.broadband_forum.obbaa.ipfix.collector.entities.IpfixMessagesWrapper;
import org.broadband_forum.obbaa.ipfix.collector.util.IPFIXUtilTestHelper;
import org.broadband_forum.obbaa.ipfix.collector.util.IpfixUtilities;
import org.junit.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class IpfixMessageDecoderTest {

    @Test
    public void testDecode1Message() throws Exception {
        byte[] inputByte = IPFIXUtilTestHelper.collectMessage("ONT1", "$6$SW2yXjnVMkUwIUeQ$c.JydO461NKo/VTgTYbvtEyVnKQhUr4NRFaFQD3YI5UfC28WTSewVAaR6UjloBFa.N4lVYrr1at25omRhbPOI.", "admin");
        IpfixMessageDecoder IpfixMessageDecoder = new IpfixMessageDecoder();
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        List<Object> checkedList = new ArrayList<>();
        IpfixMessageDecoder.decode(ctx, Unpooled.wrappedBuffer(inputByte), checkedList);
        assertEquals(1, checkedList.size());
        IpfixMessagesWrapper ipfixMessagesWrapper = (IpfixMessagesWrapper) checkedList.get(0);
        assertArrayEquals(inputByte, ipfixMessagesWrapper.getData());
    }

    @Test
    public void testDecode2Message() throws Exception {
        byte[] inputByte = IPFIXUtilTestHelper.collectMessage("ONT1", "$6$SW2yXjnVMkUwIUeQ$c.JydO461NKo/VTgTYbvtEyVnKQhUr4NRFaFQD3YI5UfC28WTSewVAaR6UjloBFa.N4lVYrr1at25omRhbPOI.", "admin");
        byte[] inputByte2 = IPFIXUtilTestHelper.collectMessage("ONT1", "$6$SW2yXjnVMkUwIUeQ$c.JydO461NKo/VTgTYbvtEyVnKQhUr4NRFaFQD3YI5UfC28WTSewVAaR6UjloBFa.N4lVYrr1at25omRhbPOI.", "admin");
        byte[] testByte = ArrayUtils.addAll(inputByte, inputByte2);
        assertEquals(inputByte.length + inputByte2.length, testByte.length);
        IpfixMessageDecoder IpfixMessageDecoder = new IpfixMessageDecoder();
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        List<Object> checkedList = new ArrayList<>();
        IpfixMessageDecoder.decode(ctx, Unpooled.wrappedBuffer(testByte), checkedList);
        assertEquals(2, checkedList.size());
        IpfixMessagesWrapper ipfixMessagesWrapper = (IpfixMessagesWrapper) checkedList.get(0);
        IpfixMessagesWrapper ipfixMessagesWrapper2 = (IpfixMessagesWrapper) checkedList.get(1);
        assertArrayEquals(inputByte, ipfixMessagesWrapper.getData());
        assertArrayEquals(inputByte2, ipfixMessagesWrapper2.getData());
    }

    @Test
    public void testDecodeNotCompleteMessage() throws Exception {
        byte[] inputByte = IPFIXUtilTestHelper.collectMessage("ONT1", "$6$SW2yXjnVMkUwIUeQ$c.JydO461NKo/VTgTYbvtEyVnKQhUr4NRFaFQD3YI5UfC28WTSewVAaR6UjloBFa.N4lVYrr1at25omRhbPOI.", "admin");
        byte[] inputByteFirstHalf = IpfixUtilities.copyByteArray(inputByte, 0, 300);
        byte[] inputByteSecondHalf = IpfixUtilities.copyByteArray(inputByte, 300, inputByte.length - 300);
        byte[] testByte = ArrayUtils.addAll(inputByte, inputByteFirstHalf);
        ByteBuf byteBuf = Unpooled.buffer(testByte.length + inputByteSecondHalf.length);
        byteBuf.writeBytes(testByte);
        IpfixMessageDecoder IpfixMessageDecoder = new IpfixMessageDecoder();
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        List<Object> checkedList = new ArrayList<>();
        IpfixMessageDecoder.decode(ctx, byteBuf, checkedList);
        assertEquals(1, checkedList.size());
        IpfixMessagesWrapper ipfixMessagesWrapper = (IpfixMessagesWrapper) checkedList.get(0);
        assertArrayEquals(inputByte, ipfixMessagesWrapper.getData());

        // Send remaining data
        byteBuf.writeBytes(inputByteSecondHalf);
        IpfixMessageDecoder.decode(ctx, byteBuf, checkedList);
        assertEquals(2, checkedList.size());
        Object checkedOutput2 = checkedList.get(1);
        IpfixMessagesWrapper ipfixMessagesWrapper2 = (IpfixMessagesWrapper) checkedOutput2;
        assertArrayEquals(inputByte, ipfixMessagesWrapper2.getData());
    }
}
