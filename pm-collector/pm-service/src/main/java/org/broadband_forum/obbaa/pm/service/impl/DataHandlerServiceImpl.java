package org.broadband_forum.obbaa.pm.service.impl;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.broadband_forum.obbaa.pm.service.DataHandlerService;
import org.broadband_forum.obbaa.pm.service.IpfixDataHandler;
import org.broadband_forum.obbaa.pm.service.OnuPMDataHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataHandlerServiceImpl implements DataHandlerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataHandlerServiceImpl.class);
    List<IpfixDataHandler> m_ipfixDataHandlers;
    List<OnuPMDataHandler> m_onuPmDataHandlers;

    @Override
    public void registerIpfixDataHandler(IpfixDataHandler dataHandler) {
        if (m_ipfixDataHandlers == null) {
            m_ipfixDataHandlers = new CopyOnWriteArrayList<>();
        }
        LOGGER.info("Registering data handler: " + dataHandler);
        m_ipfixDataHandlers.add(dataHandler);
    }

    @Override
    public void unregisterIpfixDataHandler(IpfixDataHandler dataHandler) {
        if (m_ipfixDataHandlers != null) {
            LOGGER.info("Unregistering data handler: " + dataHandler);
            m_ipfixDataHandlers.remove(dataHandler);
        }
    }

    @Override
    public void registerOnuPmDataHandler(OnuPMDataHandler dataHandler) {
        if (m_onuPmDataHandlers == null) {
            m_onuPmDataHandlers = new CopyOnWriteArrayList<>();
        }
        LOGGER.info("Registering data handler: " + dataHandler);
        m_onuPmDataHandlers.add(dataHandler);
    }

    @Override
    public void unregisterOnuPmDataHandler(OnuPMDataHandler dataHandler) {
        if (m_ipfixDataHandlers != null) {
            LOGGER.info("Unregistering data handler: " + dataHandler);
            m_onuPmDataHandlers.remove(dataHandler);
        }
    }

    @Override
    public List<IpfixDataHandler> getIpfixDataHandlers() {
        return m_ipfixDataHandlers;
    }

    @Override
    public List<OnuPMDataHandler> getOnuPmDataHandlers() {
        return m_onuPmDataHandlers;
    }

}
