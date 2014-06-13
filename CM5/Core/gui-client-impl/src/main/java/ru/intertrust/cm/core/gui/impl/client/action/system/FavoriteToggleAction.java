package ru.intertrust.cm.core.gui.impl.client.action.system;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.action.ToggleAction;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ToggleActionContext;

/**
 * @author Sergey.Okolot
 */
@ComponentName("favorite.toggle.action")
public class FavoriteToggleAction extends ToggleAction {

    @Override
    public void execute() {
        ToggleActionContext actionContext = getInitialContext();
        actionContext.setPushed(!actionContext.isPushed());
    }

    @Override
    public Component createNew() {
        return new FavoriteToggleAction();
    }
}
