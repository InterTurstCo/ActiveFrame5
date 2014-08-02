package ru.intertrust.cm.core.gui.impl.authentication;

import com.google.gwt.core.client.Scheduler;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;

/**
 * Created by
 * Bondarchuk Yaroslav
 * 02.08.2014
 * 18:41
 */

public class DebugLoginPageImpl implements LoginPage {
    public void onModuleLoad() {
        final LoginWindow loginWindow = (LoginWindow) ComponentRegistry.instance.get("login.window");
        loginWindow.addClearUserSettingsButton();
        loginWindow.center();
        loginWindow.show();
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            public void execute() {
                loginWindow.getLoginField().setFocus(true);
            }
        });
    }
}
