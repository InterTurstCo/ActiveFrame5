package ru.intertrust.cm.core.gui.impl.authentication;

import com.google.gwt.core.client.EntryPoint;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;

/**
 * @author Denis Mitavskiy
 *         Date: 25.07.13
 *         Time: 14:49
 */
public class LoginPage implements EntryPoint {
    @Override
    public void onModuleLoad() {
        LoginWindow loginWindow = (LoginWindow) ComponentRegistry.instance.get("login.window");
        loginWindow.center();

        //loginWindow.show();
    }
}
