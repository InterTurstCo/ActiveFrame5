package ru.intertrust.cm.core.gui.impl.client.form.widget.linkedtable;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.LinkedDomainObjectsTableState;

/**
 * Created by andrey on 19.12.14.
 */
@ComponentName("default.edit.table.action")
public class DefaultEditLinkedTableAction extends LinkedTableAction {
    private LinkedDomainObjectsTableState state;
    @Override
    protected void execute(Id id, int rowIndex) {

    }

    public void setState(LinkedDomainObjectsTableState state) {
        this.state = state;
    }

    @Override
    protected String getServerComponentName() {
        return null;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public Component createNew() {
        return new DefaultEditLinkedTableAction();
    }
}
