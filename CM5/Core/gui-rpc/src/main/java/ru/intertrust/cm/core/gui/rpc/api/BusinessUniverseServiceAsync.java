package ru.intertrust.cm.core.gui.rpc.api;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import ru.intertrust.cm.core.business.api.dto.AttachmentUploadPercentage;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.model.BusinessUniverseInitialization;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.counters.CollectionCountersRequest;
import ru.intertrust.cm.core.gui.model.counters.CollectionCountersResponse;
import ru.intertrust.cm.core.gui.model.form.FormDisplayData;

/**
 * @author Denis Mitavskiy
 *         Date: 31.07.13
 *         Time: 17:14
 */
public interface BusinessUniverseServiceAsync {
    void getBusinessUniverseInitialization(AsyncCallback<BusinessUniverseInitialization> async);

    void executeCommand(Command command, AsyncCallback<? extends Dto> async);

    void getForm(Id domainObjectId, AsyncCallback<FormDisplayData> async);

    void getAttachmentUploadPercentage(AsyncCallback<AttachmentUploadPercentage> async);

    void getCollectionCounters(CollectionCountersRequest req, AsyncCallback<CollectionCountersResponse> async);


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

        public static <T extends Dto> void executeCommand(Command command, final AsyncCallback<T> async) {
            getInstance().executeCommand(command, new AsyncCallback<T>() {

                @Override
                public void onFailure(Throwable caught) {
                    final String initialToken = History.getToken();
                    if (caught.getMessage() != null && caught.getMessage().contains("")) {
                        String queryString = Window.Location.getQueryString() == null ? "" : Window.Location.getQueryString();
                        final StringBuilder loginPathBuilder = new StringBuilder(GWT.getHostPageBaseURL())
                                .append("Login.html").append(queryString);
                        if (initialToken != null && !initialToken.isEmpty()) {
                            loginPathBuilder.append('#').append(initialToken);
                        }
                        Window.Location.assign(loginPathBuilder.toString());
                    } else {
                        async.onFailure(caught);
                    }
                }

                @Override
                public void onSuccess(T result) {
                    async.onSuccess(result);
                }
            });
        }
    }
}
