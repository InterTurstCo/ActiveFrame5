package ru.intertrust.cm.core.gui.impl.client.action;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.IsDomainObjectEditor;

/**
 * @author Sergey.Okolot
 */
@ComponentName("edit.action")
public class EditAction extends Action {

    @Override
    public void execute() {
        final IsDomainObjectEditor editor = (IsDomainObjectEditor) getPlugin();
        final Id id = editor.getFormState().getObjects().getRootNode().getDomainObject().getId();
        final FormPluginConfig config;
        if (id == null) {
            config = new FormPluginConfig(editor.getRootDomainObject().getTypeName());
        } else {
            config = new FormPluginConfig(id);
        }
        config.setEditable(true);
        editor.replaceForm(config);
    }

    @Override
    public Component createNew() {
        return new EditAction();
    }
}
