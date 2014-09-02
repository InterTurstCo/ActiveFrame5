package ru.intertrust.cm.core.gui.api.server.action;

import ru.intertrust.cm.core.gui.model.action.ActionVisibilityContext;

/**
 * @author Sergey.Okolot
 *         Created on 01.09.2014 17:03.
 */
public interface ActionVisibilityChecker {

    boolean isVisible(ActionVisibilityContext context);
}
