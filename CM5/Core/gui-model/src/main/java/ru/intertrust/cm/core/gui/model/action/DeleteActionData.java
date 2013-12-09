package ru.intertrust.cm.core.gui.model.action;

import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * User: IPetrov
 * Date: 09.12.13
 * Time: 15:47
 * заглушка для действия удаления объекта
 */
public class DeleteActionData extends ActionData {
    // идентификатор удаленного объекта
    protected Id id;

    public DeleteActionData() {
    }

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }
}


