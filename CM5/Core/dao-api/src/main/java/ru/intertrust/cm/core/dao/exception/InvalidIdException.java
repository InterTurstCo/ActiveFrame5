package ru.intertrust.cm.core.dao.exception;

import ru.intertrust.cm.core.business.api.dto.Id;

/**
 *
 *Представляет случай когда идентификатор доменного объекта не корректный
 * @author skashanski
 *
 */
public class InvalidIdException extends DaoException {

    private final Id id;

    public InvalidIdException(Id id) {
        super();
        this.id = id;
    }

}
