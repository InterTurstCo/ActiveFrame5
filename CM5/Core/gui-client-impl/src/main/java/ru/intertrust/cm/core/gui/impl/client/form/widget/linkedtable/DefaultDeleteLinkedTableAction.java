package ru.intertrust.cm.core.gui.impl.client.form.widget.linkedtable;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * Created by andrey on 19.12.14.
 */
@ComponentName("default.delete.table.action")
public class DefaultDeleteLinkedTableAction extends LinkedTableAction {

    @Override
    protected void execute(Id id, int rowIndex) {
    }

    @Override
    protected String getServerComponentName() {
        return "default.delete.table.action";
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public Component createNew() {
        return new DefaultDeleteLinkedTableAction();
    }
}
