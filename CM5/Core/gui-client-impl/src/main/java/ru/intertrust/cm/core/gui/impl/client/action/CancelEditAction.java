package ru.intertrust.cm.core.gui.impl.client.action;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.FormMode;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.IsDomainObjectEditor;
import ru.intertrust.cm.core.gui.model.plugin.IsIdentifiableObjectList;

import java.util.List;

/**
 * @author Sergey.Okolot
 */
@ComponentName("cancel.edit.action")
public class CancelEditAction extends Action {

    @Override
    public void execute() {
        IsDomainObjectEditor editor = (IsDomainObjectEditor) getPlugin();
        Id id = editor.getFormState().getObjects().getRootNode().getDomainObject().getId();
        if (id == null && editor instanceof IsIdentifiableObjectList) {
                final List<Id> ids = ((IsIdentifiableObjectList) editor).getSelectedIds();
                id = ids.isEmpty() ? null : ids.get(0);
        }
        final FormPluginConfig config;
        if (id == null) {
            config = new FormPluginConfig(editor.getRootDomainObject().getTypeName());
        } else {
            config = new FormPluginConfig(id);
        }
        config.setMode(editor.getFormPluginMode());
        config.getMode().updateMode(FormMode.EDITABLE, false);
        editor.replaceForm(config);
    }

    @Override
    public Component createNew() {
        return new CancelEditAction();
    }
}
