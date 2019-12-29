package ru.intertrust.cm.core.gui.rpc.api;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.UrlBuilder;
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

        /**
         * Выполняет команду без блокировки экрана для дальнейших действий пользователя
         *
         * @param command команда
         * @param async   обратный вызов по результату запроса
         * @param <T>     возвращаемый тип
         */
        public static <T extends Dto> void executeCommand(Command command, final AsyncCallback<T> async) {
            executeCommand(command, async, false, false, false);
        }

        /**
         * Выполняет команду
         *
         * @param command    команда
         * @param async      обратный вызов по результату запроса
         * @param lockScreen блокировать ли экран при выполнении запроса, если true, то незаметная блокировка срабатывает сразу, а через 1 секунду визуализируется
         * @param <T>        возвращаемый тип
         */
        public static <T extends Dto> void executeCommand(Command command, final AsyncCallback<T> async, final boolean lockScreen) {
            executeCommand(command, async, lockScreen, true, false);
        }

        /**
         * Выполняет команду
         *
         * @param command    команда
         * @param async      обратный вызов по результату запроса
         * @param lockScreen блокировать ли экран при выполнении запроса
         * @param indicate   нужно ли показывать индикатор выполнения запроса через 1 секунду (игнорируется, если экран не блокировался (lockScreen == false) )
         */
        public static <T extends Dto> void executeCommand(Command command, final AsyncCallback<T> async, final boolean lockScreen, final boolean indicate) {
            executeCommand(command, async, lockScreen, indicate, false);
        }

        /**
         * Выполняет команду
         *
         * @param command                  команда
         * @param async                    обратный вызов по результату запроса
         * @param lockScreen               блокировать ли экран при выполнении запроса
         * @param indicate                 нужно ли показывать индикатор выполнения запроса (игнорируется, если экран не блокировался (lockScreen == false) )
         * @param showIndicatorImmediately нужно ли немедленно показывать индикатор выполнения задачи,
         *                                 либо через секунду, для того, чтобы сделать выполнение экшена незаметным для пользователя в случае, когда он отрабатывает быстро (т.е. в течении секунды)
         *                                 (игнорируется, если экран не блокировался (lockScreen == false) )
         * @param <T>                      возвращаемый тип
         */
        public static <T extends Dto> void executeCommand(Command command, final AsyncCallback<T> async, final boolean lockScreen, final boolean indicate, final boolean showIndicatorImmediately) {
            if (lockScreen) {
                Application.getInstance().lockScreen(indicate, showIndicatorImmediately);
            }
            getInstance().executeCommand(command, new AsyncCallback<T>() {

                @Override
                public void onFailure(Throwable caught) {
                    if (lockScreen) {
                        Application.getInstance().unlockScreen();
                    }
                    final String initialToken = History.getToken();
                    if (caught.getMessage() != null && caught.getMessage().contains("LoginPage")) {
                        UrlBuilder urlBuilder = Window.Location.createUrlBuilder();
                        String contextPath = Window.Location.getPath().split("/")[1];
                        String appPath = Window.Location.getPath().substring(contextPath.length() + 2);
                        urlBuilder.setPath(contextPath + "/Login.html");
                        urlBuilder.setParameter("targetPage", appPath);

                        Window.Location.assign(urlBuilder.buildString());
                    } else {
                        async.onFailure(caught);
                    }
                }

                @Override
                public void onSuccess(T result) {
                    if (lockScreen) {
                        Application.getInstance().unlockScreen();
                    }
                    async.onSuccess(result);
                }
            });
        }
    }

}
