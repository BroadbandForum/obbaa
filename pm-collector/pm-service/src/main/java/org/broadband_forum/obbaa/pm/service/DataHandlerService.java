package org.broadband_forum.obbaa.pm.service;

import java.util.List;

public interface DataHandlerService {

    void registerIpfixDataHandler(IpfixDataHandler dataHandler);

    void unregisterIpfixDataHandler(IpfixDataHandler dataHandler);

    List<IpfixDataHandler> getDataHandlers();

}
