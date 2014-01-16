package ru.intertrust.cm.core.gui.impl.authentication;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;


/**
 * @author Denis Mitavskiy
 *         Date: 25.07.13
 *         Time: 14:49
 */
public class LoginPage implements EntryPoint {
    public void onModuleLoad() {
        final LoginWindow loginWindow = (LoginWindow) ComponentRegistry.instance.get("login.window");
        loginWindow.setPopupPosition(Window.getClientWidth() / 2, Window.getClientHeight() / 2);
        //loginWindow.center();
        loginWindow.show();

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            public void execute() {
                loginWindow.getLoginField().setFocus(true);
            }
        });
        RootLayoutPanel.get().add(loginWindow);

        int halfLoginWindowWidth = 200;
        int halfLoginWindowHeight = 200;
        loginWindow.getElement().getStyle().setPaddingLeft((Window.getClientWidth() / 2) - halfLoginWindowWidth, Style.Unit.PX);
        loginWindow.getElement().getStyle().setPaddingTop((Window.getClientHeight() / 2) - halfLoginWindowHeight, Style.Unit.PX);
    }
 }


