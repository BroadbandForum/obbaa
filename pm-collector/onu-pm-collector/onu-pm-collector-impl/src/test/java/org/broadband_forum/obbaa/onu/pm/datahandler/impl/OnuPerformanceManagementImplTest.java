package org.broadband_forum.obbaa.onu.pm.datahandler.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.broadband_forum.obbaa.onu.pm.message.gpb.message.Body;
import org.broadband_forum.obbaa.onu.pm.message.gpb.message.GetDataResp;
import org.broadband_forum.obbaa.onu.pm.message.gpb.message.Header;
import org.broadband_forum.obbaa.onu.pm.message.gpb.message.Msg;
import org.broadband_forum.obbaa.onu.pm.message.gpb.message.Response;
import org.broadband_forum.obbaa.pm.service.DataHandlerService;
import org.broadband_forum.obbaa.pm.service.OnuPMDataHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.protobuf.ByteString;

@RunWith(PowerMockRunner.class)
@PrepareForTest(OnuPerformanceManagementImpl.class)
public class OnuPerformanceManagementImplTest {

    OnuPerformanceManagementImpl onuPerformanceManagement;
    @Mock
    DataHandlerService m_dataHandlerService;

    private Msg m_onuPmData;
    @Mock
    private List<OnuPMDataHandler> m_onuPMDataHandlerList;

    @Before
    public void setup() {
        onuPerformanceManagement = new OnuPerformanceManagementImpl(m_dataHandlerService);
        m_dataHandlerService = PowerMockito.mock(DataHandlerService.class);
    }

    @Test
    public void testProcessOnuPmData() {
        m_onuPmData = prepareGpbmsg("");
        when(m_dataHandlerService.getOnuPmDataHandlers()).thenReturn(m_onuPMDataHandlerList);
        onuPerformanceManagement.processOnuPmData(m_onuPmData);
        assertEquals(m_onuPmData.getHeader().getMsgId(), "0");
        assertEquals(m_onuPmData.getHeader().getObjectType(), Header.OBJECT_TYPE.ONU);
    }

    private Msg prepareGpbmsg(String getResponse) {
        Msg msg = Msg.newBuilder()
                .setHeader(Header.newBuilder()
                        .setMsgId("0")
                        .setSenderName("vomci-vendor-1")
                        .setRecipientName("onu-pm-collector")
                        .setObjectName("ont1")
                        .setObjectType(Header.OBJECT_TYPE.ONU)
                        .build())
                .setBody(Body.newBuilder()
                        .setResponse(Response.newBuilder()
                                .setGetResp(GetDataResp.newBuilder()
                                        .setData(ByteString.copyFromUtf8(getResponse))
                                )
                                .build()))
                .build();
        return msg;
    }
}
