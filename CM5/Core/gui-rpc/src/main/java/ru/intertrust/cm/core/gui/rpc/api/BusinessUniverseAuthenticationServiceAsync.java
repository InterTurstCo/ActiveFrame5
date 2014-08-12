package ru.intertrust.cm.core.gui.rpc.api;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

import ru.intertrust.cm.core.business.api.dto.UserCredentials;
import ru.intertrust.cm.core.gui.model.LoginWindowInitialization;
import ru.intertrust.cm.core.model.AuthenticationException;

/**
 * @author Denis Mitavskiy
 *         Date: 06.08.13
 *         Time: 17:44
 */
public interface BusinessUniverseAuthenticationServiceAsync {
    void login(UserCredentials userCredentials, AsyncCallback<Void> async) throws AuthenticationException;
    void logout(AsyncCallback<Void> async);
    void getLoginWindowInitialization(AsyncCallback<LoginWindowInitialization> callback);

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
