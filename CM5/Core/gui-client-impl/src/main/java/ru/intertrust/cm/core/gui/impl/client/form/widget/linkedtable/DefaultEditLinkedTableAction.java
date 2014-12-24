package ru.intertrust.cm.core.gui.impl.client.form.widget.linkedtable;

import com.google.gwt.user.client.Window;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * Created by andrey on 19.12.14.
 */
@ComponentName("default.edit.table.action")
public class DefaultEditLinkedTableAction extends LinkedTableAction {

    @Override
    void execute(Id id, int rowIndex) {
    }

    @Override
    String getServerComponentName() {
        return "default.edit.table.action";
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
