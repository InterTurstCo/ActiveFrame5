package ru.intertrust.cm.core.gui.rpc.api;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import ru.intertrust.cm.core.business.api.dto.AttachmentUploadPercentage;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.History;
import ru.intertrust.cm.core.gui.model.BusinessUniverseInitialization;
import ru.intertrust.cm.core.gui.model.Client;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.counters.CollectionCountersRequest;
import ru.intertrust.cm.core.gui.model.counters.CollectionCountersResponse;

/**
 * @author Denis Mitavskiy
 *         Date: 31.07.13
 *         Time: 17:14
 */
public interface BusinessUniverseServiceAsync {
    void getBusinessUniverseInitialization(Client clientInfo, AsyncCallback<BusinessUniverseInitialization> async);

    void executeCommand(Command command, AsyncCallback<? extends Dto> async);

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
            executeCommand(command, async, false, false);
        }

        /**
         * Выполняет команду
         * @param command команда
         * @param async обратный вызов по результату запроса
         * @param indicate показывать индикатор выполения серверного запроса
         * @param lockScreenImmediately немедленно блокировать экран. Игнорируется, если индикатор выполнения не показывается (indicate == false)
         * @param <T> возвращаемый тип
         */
        public static <T extends Dto> void executeCommand(Command command, final AsyncCallback<T> async, final boolean indicate, boolean lockScreenImmediately) {
            if (indicate) {
                Application.getInstance().showLoadingIndicator(lockScreenImmediately);
            }
            getInstance().executeCommand(command, new AsyncCallback<T>() {

                @Override
                public void onFailure(Throwable caught) {
                    if (indicate) {
                        Application.getInstance().hideLoadingIndicator();
                    }
                    final String initialToken = History.getToken();
                    if (caught.getMessage() != null && caught.getMessage().contains("LoginPage")) {
                        String queryString = Window.Location.getQueryString() == null ? "" : Window.Location.getQueryString();
                        final StringBuilder loginPathBuilder = new StringBuilder(GWT.getHostPageBaseURL())
                                .append(Window.Location.getPath().substring(Window.Location.getPath().lastIndexOf("/")+1))
                                .append(queryString);
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
                    if (indicate) {
                        Application.getInstance().hideLoadingIndicator();
                    }
                    async.onSuccess(result);
                }
            });
        }
    }
}
