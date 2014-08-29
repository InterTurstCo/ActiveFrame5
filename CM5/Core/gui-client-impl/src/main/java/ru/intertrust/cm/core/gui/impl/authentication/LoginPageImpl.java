package ru.intertrust.cm.core.gui.impl.authentication;

import com.google.gwt.core.client.Scheduler;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;


/**
 * @author Denis Mitavskiy
 *         Date: 25.07.13
 *         Time: 14:49
 */
public class LoginPageImpl implements LoginPage {

    public void onModuleLoad() {
        final LoginWindow loginWindow = ComponentRegistry.instance.get("login.window");
        loginWindow.center();
        loginWindow.show();
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            public void execute() {
                loginWindow.getLoginField().setFocus(true);
            }
        });
    }
 }


