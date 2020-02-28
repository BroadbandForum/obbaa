package org.broadband_forum.obbaa.device.listener;

import java.net.InetAddress;

import org.broadband_forum.obbaa.device.listener.ProcessTrapCallBack;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.UdpAddress;

public class TrapListenerTest {

    @Mock
    private ProcessTrapCallBack m_trapCallback;

    private TrapListener m_trapListener;

    @Mock
    private CommandResponderEvent event;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        m_trapListener = new TrapListener();
    }

    @Test
    public void testRegisterCallBackForTrapUpdates() throws Exception {
        m_trapListener.registerCallBackForTrapUpdates(m_trapCallback, "0.0.0.0", "162");
        assertEquals(1, m_trapListener.getNoOfCallbacks());
        m_trapListener.registerCallBackForTrapUpdates(m_trapCallback, "127.0.0.1", "222");
        assertEquals(2, m_trapListener.getNoOfCallbacks());
        m_trapListener.unRegisterCallBackForTrapUpdates(m_trapCallback, "0.0.0.0", "162");
        assertEquals(1, m_trapListener.getNoOfCallbacks());
        m_trapListener.unRegisterCallBackForTrapUpdates(m_trapCallback, "127.0.0.1", "222");
        assertEquals(0, m_trapListener.getNoOfCallbacks());
    }

    @Test
    public void testProcessPdu() throws Exception {
        m_trapListener.registerCallBackForTrapUpdates(m_trapCallback, "0.0.0.0", "162");
        InetAddress inetAddress = InetAddress.getByName("0.0.0.0");
        Address address = new UdpAddress(inetAddress, 162);
        when( event.getPeerAddress()).thenReturn(address);
        m_trapListener.processPdu(event);
        verify(m_trapCallback).processTrap(event);
    }

    @After
    public void teardown() {
    }
}
