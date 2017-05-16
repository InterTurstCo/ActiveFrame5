package ru.intertrust.cm.core.gui.impl.server.configextension;

import ru.intertrust.cm.core.gui.api.server.action.ActionVisibilityChecker;
import ru.intertrust.cm.core.gui.api.server.action.ActionVisibilityContext;
import ru.intertrust.cm.core.gui.model.ComponentName;


/**
 * Created by Ravil on 16.05.2017.
 */
@ComponentName("extension.delete.visibility.checker")
public class DeleteActionVisibilityChecker implements ActionVisibilityChecker {


    @Override
    public boolean isVisible(ActionVisibilityContext context) {
       return true;
    }
}
