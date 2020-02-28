package org.broadband_forum.obbaa.pm.service.impl;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.broadband_forum.obbaa.pm.service.DataHandlerService;
import org.broadband_forum.obbaa.pm.service.IpfixDataHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataHandlerServiceImpl implements DataHandlerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataHandlerServiceImpl.class);
    List<IpfixDataHandler> m_dataHandlers;

    @Override
    public void registerIpfixDataHandler(IpfixDataHandler dataHandler) {
        if (m_dataHandlers == null) {
            m_dataHandlers = new CopyOnWriteArrayList<>();
        }
        LOGGER.info("Registering data handler: " + dataHandler);
        m_dataHandlers.add(dataHandler);
    }

    @Override
    public void unregisterIpfixDataHandler(IpfixDataHandler dataHandler) {
        if (m_dataHandlers != null) {
            LOGGER.info("Unregistering data handler: " + dataHandler);
            m_dataHandlers.remove(dataHandler);
        }
    }

    @Override
    public List<IpfixDataHandler> getDataHandlers() {
        return m_dataHandlers;
    }
}
