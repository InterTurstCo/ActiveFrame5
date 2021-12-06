package ru.intertrust.cm.core.gui.impl.client.action;

import com.google.gwt.user.client.Window;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * copy of TableViewerDeleteComponent.java (5/6) -- удаление записи в TableViewer с подтверждением
 */
@ComponentName("table.viewer.confirm.delete.component")
public class TableViewerConfirmDeleteComponent extends DefaultCustomDeleteComponent {

    @Override
    public void delete(Id id, EventBus eventBus) {
        boolean delete = Window.confirm("Действительно удалить?");
        if (delete) super.delete(id, eventBus);
    }

    @Override
    public Component createNew() {
        return new TableViewerConfirmDeleteComponent();
    }
}
