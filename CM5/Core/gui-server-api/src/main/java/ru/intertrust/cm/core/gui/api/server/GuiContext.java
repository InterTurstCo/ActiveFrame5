package ru.intertrust.cm.core.gui.api.server;

import ru.intertrust.cm.core.UserInfo;

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
}
