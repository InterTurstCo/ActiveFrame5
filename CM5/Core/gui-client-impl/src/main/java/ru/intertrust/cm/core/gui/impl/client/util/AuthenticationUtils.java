package ru.intertrust.cm.core.gui.impl.client.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import ru.intertrust.cm.core.config.localization.LocalizationKeys;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.impl.client.ApplicationWindow;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseAuthenticationServiceAsync;

/**
 * Набор утилит связанных с аутентификацией.<br>
 * <br>
 * <p>
 * Created by Myskin Sergey on 21.07.2020.
 */
public class AuthenticationUtils {

    private AuthenticationUtils() {
    }

    /**
     * Производит разлогинивание с редиректом на страницу логина.
     */
    public static void logout() {
        AsyncCallback<Void> callback = new AsyncCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (!Window.Location.getPath().contains("BusinessUniverse.html")) {

                    Window.Location.assign(GWT.getHostPageBaseURL() +
                            Window.Location.getPath().substring(Window.Location.getPath().lastIndexOf("/") + 1) +
                            Window.Location.getQueryString());
                    Window.Location.reload();
                } else {

                    Window.Location.assign(GWT.getHostPageBaseURL() +
                            BusinessUniverseConstants.LOGIN_PAGE +
                            Window.Location.getQueryString());
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                ApplicationWindow.errorAlert(LocalizeUtil.get(LocalizationKeys.LOGOUT_ERROR_MESSAGE_KEY,
                        BusinessUniverseConstants.LOGOUT_ERROR_MESSAGE));
            }
        };
        BusinessUniverseAuthenticationServiceAsync.Impl.getInstance().logout(callback);
    }

}
