package ru.intertrust.cm.core.gui.impl.server.action;

import ru.intertrust.cm.core.gui.api.server.action.ActionVisibilityChecker;
import ru.intertrust.cm.core.gui.api.server.action.ActionVisibilityContext;
import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * Возвращает TRUE если доменный объект новый.
 *
 * @author Sergey.Okolot
 *         Created on 19.09.2014 12:49.
 */
@ComponentName("custom.visibility.checker")
public class CustomVisibilityChecker implements ActionVisibilityChecker {

    @Override
    public boolean isVisible(ActionVisibilityContext context) {
        if (context.getDomainObject() != null) {
            return context.getDomainObject().isNew();
        }
        return true;
    }
}
