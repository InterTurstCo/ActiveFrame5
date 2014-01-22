package ru.intertrust.cm.core.gui.impl.authentication;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;


/**
 * @author Denis Mitavskiy
 *         Date: 25.07.13
 *         Time: 14:49
 */
public class LoginPage implements EntryPoint {
    public void onModuleLoad() {
        final LoginWindow loginWindow = (LoginWindow) ComponentRegistry.instance.get("login.window");
        loginWindow.center();
        loginWindow.show();

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            public void execute() {
                loginWindow.getLoginField().setFocus(true);
            }
        });

        loginWindow.getLoginDialog().getElement().getStyle().setBorderStyle(Style.BorderStyle.SOLID);
        loginWindow.getLoginDialog().getElement().getStyle().setBorderColor("black");
        loginWindow.getLoginDialog().getElement().getStyle().setBorderWidth(1, Style.Unit.PX);
    }
 }


