package ru.intertrust.cm.core.gui.impl.client.action;

import com.google.gwt.user.client.Window;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * @author Sergey.Okolot
 *         Created on 09.06.2014 13:37.
 */
@ComponentName("refresh.action")
public class RefreshAction extends Action {
    @Override
    public void execute() {
        Window.alert("Will be implements");
    }

    @Override
    public Component createNew() {
        return new RefreshAction();
    }
}
