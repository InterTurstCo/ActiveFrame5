package ru.intertrust.cm.core.gui.impl.client.action;

import com.google.gwt.user.client.Window;

import ru.intertrust.cm.core.gui.api.client.ActionManager;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
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
    public boolean isExecuteIfWorkplaceDirty() {
        boolean isExecute = true;
        final Plugin plugin = workplace.getCurrentPlugin();
        if (plugin instanceof IsDomainObjectEditor) {
            final IsDomainObjectEditor editor = (IsDomainObjectEditor) plugin;
            if (editor.isDirty()) {
                isExecute = Window.confirm("Изменения данных не сохранены.\nПродолжить выполнение команды ?");
            }
        }
        return isExecute;
    }
}
