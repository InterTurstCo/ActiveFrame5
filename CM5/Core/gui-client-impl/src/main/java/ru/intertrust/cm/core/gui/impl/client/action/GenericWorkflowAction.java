package ru.intertrust.cm.core.gui.impl.client.action;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * @author Denis Mitavskiy
 *         Date: 20.09.13
 *         Time: 21:17
 */
@ComponentName("generic.workflow.action")
public class GenericWorkflowAction extends SimpleServerAction {
    @Override
    public Component createNew() {
        return new GenericWorkflowAction();
    }
}
