package ru.intertrust.cm.core.gui.rpc.api;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Denis Mitavskiy
 *         Date: 31.07.13
 *         Time: 17:14
 */
public interface BusinessUniverseServiceAsync {
    void getBusinessUniverseInitialization(AsyncCallback<? extends Dto> async);

    public static class Impl {
        private static final BusinessUniverseServiceAsync instance;

        static {
            instance = GWT.create(BusinessUniverseService.class);
            ServiceDefTarget endpoint = (ServiceDefTarget) instance;
            endpoint.setServiceEntryPoint("remote/BusinessUniverseService");
        }

        public static BusinessUniverseServiceAsync getInstance() {
            return instance;
        }
    }

}
