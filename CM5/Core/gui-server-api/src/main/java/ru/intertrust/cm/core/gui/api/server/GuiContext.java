package ru.intertrust.cm.core.gui.api.server;

import ru.intertrust.cm.core.UserInfo;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginState;

import java.util.TimeZone;

/**
 * @author Sergey.Okolot
 *         Created on 18.02.14 15:42.
 */
public class GuiContext {

    private static ThreadLocal<GuiContext> instance = new ThreadLocal<GuiContext>(){
        @Override
        protected GuiContext initialValue() {
            return new GuiContext();
        }
    };

    private UserInfo userInfo;

    private FormPluginState formPluginState;

    private GuiContext() {}

    public static GuiContext get() {
        return instance.get();
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(final UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public FormPluginState getFormPluginState() {
        return formPluginState;
    }

    public void setFormPluginState(FormPluginState formPluginState) {
        this.formPluginState = formPluginState;
    }

    public static TimeZone getUserTimeZone() {
        return TimeZone.getTimeZone(getUserTimeZoneId());
    }

    public static String getUserTimeZoneId() {
        return get().getUserInfo().getTimeZoneId();
    }

    public static String getUserLocale() {
        UserInfo userInfo = get().getUserInfo();
        return userInfo == null ? "en" : userInfo.getLocale();
    }
}
