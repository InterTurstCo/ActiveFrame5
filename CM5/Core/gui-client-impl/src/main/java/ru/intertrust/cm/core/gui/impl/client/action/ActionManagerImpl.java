package ru.intertrust.cm.core.gui.impl.client.action;

import ru.intertrust.cm.core.gui.api.client.ActionManager;
import ru.intertrust.cm.core.gui.api.client.ConfirmCallback;
import ru.intertrust.cm.core.gui.impl.client.ApplicationWindow;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.model.plugin.IsDomainObjectEditor;

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
    public void executeIfUserAgree(ConfirmCallback confirmCallback) {
        ApplicationWindow.confirm(BusinessUniverseConstants.DATA_IS_NOT_SAVED_CONFIRM_MESSAGE, confirmCallback);
    }

    public boolean isEditorDirty() {
        boolean result = false;
        final Plugin plugin = workplace.getCurrentPlugin();
        if (plugin instanceof IsDomainObjectEditor) {
            final IsDomainObjectEditor editor = (IsDomainObjectEditor) plugin;
            if (editor.isDirty()) {
                result = true;
            }
        }
        return result;

    }
}
