package ru.intertrust.cm.core.gui.model.form;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Node of an hierarchy which may contain a list of Domain Objects
 *
 * @author Denis Mitavskiy
 *         Date: 18.10.13
 *         Time: 19:15
 */
public abstract class ObjectsNode implements Dto {
    private String type;

    public ObjectsNode() {
    }

    protected ObjectsNode(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public abstract boolean isEmpty();
}
