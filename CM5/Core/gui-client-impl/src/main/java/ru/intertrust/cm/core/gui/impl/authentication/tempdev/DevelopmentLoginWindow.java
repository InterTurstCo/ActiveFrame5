package ru.intertrust.cm.core.gui.impl.authentication.tempdev;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.authentication.LoginWindow;
import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * @author Denis Mitavskiy
 *         Date: 19.09.13
 *         Time: 19:32
 */
@ComponentName("login.window")
public class DevelopmentLoginWindow extends LoginWindow {
    @Override
    public void center() {
        super.center();
        loginField.setText("admin");
        passwordField.setText("admin");
        login();
    }

    @Override
    public Component createNew() {
        return new DevelopmentLoginWindow();
    }
}
