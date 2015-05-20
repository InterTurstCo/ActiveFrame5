package ru.intertrust.cm.core.model;

import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Представляет случай когда не существует объектов с данным  идентификатором
 * @author skashanski
 *
 */
public class ObjectNotFoundException extends SystemException {

    private final Id id;

    public ObjectNotFoundException(Id id) {
        super(id== null ? "null" : id.toStringRepresentation());
        this.id = id;
    }

}
