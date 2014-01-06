package ru.intertrust.cm.core.gui.model.action;

import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * User: IPetrov
 * Date: 09.12.13
 * Time: 15:47
 */
public class DeleteActionData extends ActionData {
    /**
     * @defaultUID
     */
    private static final long serialVersionUID = 1L;


    // идентификатор удаленного объекта
    private Id id;

    public DeleteActionData() {
    }

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return id == null ? 17 : id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof DeleteActionData)) {
            return false;
        }
        DeleteActionData other = (DeleteActionData) obj;
        return id == null ? other.id == null : id.equals(other.id);
    }
}


