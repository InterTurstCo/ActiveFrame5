package ru.intertrust.cm.core.gui.rpc.api;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.crypto.SignedData;
import ru.intertrust.cm.core.config.crypto.SignedResult;
import ru.intertrust.cm.core.gui.model.crypto.DigitalSignatureConfig;

import com.google.gwt.user.client.rpc.RemoteService;

public interface DigitalSignatureService extends RemoteService{
    DigitalSignatureConfig getConfig();
    
    SignedData getSignedData(Id rootId);
    
    void saveSignResult(SignedResult result);
}
