package ru.intertrust.cm.core.gui.rpc.api;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import ru.intertrust.cm.core.business.api.dto.UserCredentials;

/**
 * @author Denis Mitavskiy
 *         Date: 06.08.13
 *         Time: 17:44
 */
public interface BusinessUniverseAuthenticationServiceAsync {
    void login(UserCredentials userCredentials, AsyncCallback<Void> async);
    void logout(AsyncCallback<Void> async);

    public static class Impl {
        private static final BusinessUniverseAuthenticationServiceAsync instance;

        static {
            instance = GWT.create(BusinessUniverseAuthenticationService.class);
            ServiceDefTarget endpoint = (ServiceDefTarget) instance;
            endpoint.setServiceEntryPoint("remote/BusinessUniverseAuthenticationService");
        }

        public static BusinessUniverseAuthenticationServiceAsync getInstance() {
            return instance;
        }
    }
}
