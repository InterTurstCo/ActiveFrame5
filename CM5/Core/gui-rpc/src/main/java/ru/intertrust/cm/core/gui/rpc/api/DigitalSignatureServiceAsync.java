package ru.intertrust.cm.core.gui.rpc.api;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.crypto.SignedData;
import ru.intertrust.cm.core.config.crypto.SignedResult;
import ru.intertrust.cm.core.gui.model.crypto.DigitalSignatureConfig;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public interface DigitalSignatureServiceAsync {
    void getConfig(AsyncCallback<DigitalSignatureConfig> callback);

    void getSignedData(Id rootId, AsyncCallback<SignedData> callback);

    void saveSignResult(SignedResult result, AsyncCallback<Void> callback);
    
    public static class Impl {
        private static final DigitalSignatureServiceAsync instance;

        static {
            instance = GWT.create(DigitalSignatureService.class);
            ServiceDefTarget endpoint = (ServiceDefTarget) instance;
            endpoint.setServiceEntryPoint("remote/DigitalSignatureService");
        }

        public static DigitalSignatureServiceAsync getInstance() {
            return instance;
        }
    }
}
