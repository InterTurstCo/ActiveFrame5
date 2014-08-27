package ru.intertrust.cm.core.gui.impl.client.action;

import ru.intertrust.cm.core.gui.api.client.ActionManager;
import ru.intertrust.cm.core.gui.api.client.ConfirmCallback;
import ru.intertrust.cm.core.gui.impl.client.ApplicationWindow;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;

/**
 * @author Sergey.Okolot
 *         Created on 17.07.2014 17:17.
 */
public class ActionManagerImpl implements ActionManager {

    final PluginPanel workplace;

    public ActionManagerImpl(final PluginPanel workplace) {
        this.workplace = workplace;
    }

    @Override
    public void checkChangesBeforeExecution(ConfirmCallback confirmCallback) {
        final Plugin plugin = workplace.getCurrentPlugin();
        if (plugin != null && plugin.isDirty()) {
            ApplicationWindow.confirm(BusinessUniverseConstants.DATA_IS_NOT_SAVED_CONFIRM_MESSAGE, confirmCallback);
        } else {
            confirmCallback.onAffirmative();
        }
    }
}
