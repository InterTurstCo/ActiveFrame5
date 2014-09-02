package ru.intertrust.cm.core.gui.api.server.action;

import ru.intertrust.cm.core.gui.model.action.ActionVisibilityContext;

/**
 * @author Sergey.Okolot
 *         Created on 01.09.2014 17:06.
 */
public class DefaultActionVisibilityChecker implements ActionVisibilityChecker {

    @Override
    public boolean isVisible(ActionVisibilityContext context) {
        return true;
    }
}
