package ru.intertrust.cm.core.model;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.model.SystemException;

import javax.ejb.ApplicationException;

/**
 * Представляет случай когда не существует объектов с данным  идентификатором
 * @author skashanski
 *
 */
@ApplicationException(rollback = false)
public class ObjectNotFoundException extends SystemException {

    private final Id id;

    public ObjectNotFoundException(Id id) {
        super();
        this.id = id;
    }

}
