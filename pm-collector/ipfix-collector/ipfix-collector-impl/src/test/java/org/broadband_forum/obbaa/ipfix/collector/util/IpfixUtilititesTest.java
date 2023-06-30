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

package org.broadband_forum.obbaa.ipfix.collector.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.broadband_forum.obbaa.ipfix.entities.exception.DataTypeLengthMismatchException;
import org.broadband_forum.obbaa.ipfix.entities.exception.MissingAbstractDataTypeException;
import org.broadband_forum.obbaa.ipfix.entities.exception.UtilityException;
import org.broadband_forum.obbaa.ipfix.entities.util.IpfixUtilities;
import org.junit.Test;

public class IpfixUtilititesTest {

    private static byte[] floatToBytes(float value, int length) {
        byte[] bytes = new byte[length];
        ByteBuffer.wrap(bytes).putFloat(value);
        return bytes;
    }

    @Test
    public void testOneByteToInteger() throws UtilityException {
        byte[] data = IpfixUtilities.hexStringToByteArray("7F");
        int actual = IpfixUtilities.oneByteToInteger(data[0], 1);
        assertEquals(actual, 127);
    }

    @Test
    public void testBytesToInteger() throws UtilityException {
        //Test decode unsigned16
        byte[] data = IpfixUtilities.hexStringToByteArray("7B7B");
        int actual = IpfixUtilities.byteToInteger(data, 1, 2);
        assertEquals(actual, 31611);

        //Test decode signed16
        data = IpfixUtilities.hexStringToByteArray("FFF6");
        actual = IpfixUtilities.byteToInteger(data, 0, 2);
        assertEquals(-10, actual);
    }

    @Test
    public void testHexStringToByteArray() {
        byte[] expected = new byte[]{(byte) 0xe0, 0x4f, (byte) 0xd0, 0x20, (byte) 0xea, 0x3a, 0x69, 0x10, (byte) 0xa2,
                (byte) 0xd8, 0x08, 0x00, 0x2b, 0x30, 0x30, (byte) 0x9d};
        byte[] actual = IpfixUtilities.hexStringToByteArray("e04fd020ea3a6910a2d808002b30309d");
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testGetBit() {
        byte[] data = IpfixUtilities.hexStringToByteArray("2008");
        assertEquals(0, IpfixUtilities.getBit(data, 1));
        assertEquals(1, IpfixUtilities.getBit(data, 2));
    }

    @Test
    public void testByteArrayToString() {
        byte[] data = IpfixUtilities.hexStringToByteArray("44534c31");
        assertEquals("DSL1", IpfixUtilities.byteArrayToString(data));
    }

    @Test
    public void testBytesToLong() throws UtilityException {
        byte[] data = IpfixUtilities.hexStringToByteArray("10101010");
        long actual = IpfixUtilities.bytesToLong(data, 1, 4);
        assertEquals(269488144, actual);
    }

    @Test
    public void testBytesToFloat() throws UtilityException {
        byte[] data = IpfixUtilities.hexStringToByteArray("40A3333300000000");
        float actual = IpfixUtilities.bytesToFloat(data, 8);
        assertEquals(5.1f, actual, 0);
    }

    @Test
    public void testBytesToIPv4Address() throws UtilityException, UnknownHostException {
        String ipv4 = "192.168.0.1";
        InetAddress address = InetAddress.getByName(ipv4);
        byte[] data = address.getAddress();
        String actual = IpfixUtilities.bytesToIPv4OrIPv6Address(data, 4);
        assertEquals(ipv4, actual);
    }

    @Test
    public void testBytesToIPv6Address() throws UnknownHostException, UtilityException {
        String expect = "2001:dbb:ac10:fe01:0:0:0:0";
        byte[] data = IpfixUtilities.hexStringToByteArray("20010DBBAC10FE010000000000000000");
        String actual = IpfixUtilities.bytesToIPv4OrIPv6Address(data, 16);
        assertEquals(expect, actual);
    }

    @Test
    public void testByteToMacAddress() throws UnknownHostException, SocketException, UtilityException {
        String expect = "FA:16:3E:94:66:78";
        byte[] data = IpfixUtilities.hexStringToByteArray("FA163E946678");
        String actual = IpfixUtilities.bytesToMacAddress(data, 6);
        assertEquals(expect, actual);
    }

    @Test
    public void testBytesToEnum() throws UtilityException {
        String expect = "enum1";
        byte[] data = IpfixUtilities.hexStringToByteArray("00000001");
        String actual = IpfixUtilities.bytesToEnum(data, 4);
        assertEquals(expect, actual);
    }

    @Test
    public void testBytesToUnion() throws UtilityException, MissingAbstractDataTypeException, DataTypeLengthMismatchException, UnknownHostException {
        //Test decode union - enumeration
        String expect = "enum1";
        byte[] data = IpfixUtilities.hexStringToByteArray("05FE00000001");
        String actual = IpfixUtilities.bytesToUnion(data);
        assertEquals(expect, actual);

        //Test decode union - signed16
        expect = "-1";
        data = IpfixUtilities.hexStringToByteArray("0306FFFF");
        actual = IpfixUtilities.bytesToUnion(data);
        assertEquals(expect, actual);

        //Test decode union - unsigned32
        expect = "1";
        data = IpfixUtilities.hexStringToByteArray("050300000001");
        actual = IpfixUtilities.bytesToUnion(data);
        assertEquals(expect, actual);

        //Test decode union - string
        expect = "DSL1";
        data = IpfixUtilities.hexStringToByteArray("050D44534C31");
        actual = IpfixUtilities.bytesToUnion(data);
        assertEquals(expect, actual);

        //Test decode union - IPv6
        expect = "2001:dbb:ac10:fe01:0:0:0:0";
        data = IpfixUtilities.hexStringToByteArray("111320010DBBAC10FE010000000000000000");
        actual = IpfixUtilities.bytesToUnion(data);
        assertEquals(expect, actual);

        //Test decode union - octet array
        expect = "20010DBBAC10FE010000000000000000";
        data = IpfixUtilities.hexStringToByteArray("110020010DBBAC10FE010000000000000000");
        actual = IpfixUtilities.bytesToUnion(data);
        assertEquals(expect, actual);

        //Test decode union - union - union - enum
        expect = "enum1";
        data = IpfixUtilities.hexStringToByteArray("0BFF09FF07FF05FE00000001");
        actual = IpfixUtilities.bytesToUnion(data);
        assertEquals(expect, actual);
    }

}
