package org.broadband_forum.obbaa.pm.service;

import java.util.List;

public interface DataHandlerService {

    void registerIpfixDataHandler(IpfixDataHandler dataHandler);

    void unregisterIpfixDataHandler(IpfixDataHandler dataHandler);

    void registerOnuPmDataHandler(OnuPMDataHandler onuPMDataHandler);

    void unregisterOnuPmDataHandler(OnuPMDataHandler onuPMDataHandler);

    List<IpfixDataHandler> getIpfixDataHandlers();

    List<OnuPMDataHandler> getOnuPmDataHandlers();

}
