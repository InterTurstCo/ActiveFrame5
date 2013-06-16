package ru.intertrust.cm.core.dao.exception;

import ru.intertrust.cm.core.business.api.dto.DomainObject;

/**
 * Представляет случай когда объект был модифицирован после чтения другим
 * пользователем
 *
 *
 * @author skashanski
 *
 */
public class OptimisticLockException extends DataAccessException {

    private final DomainObject object;

    public OptimisticLockException(DomainObject object) {
        super();
        this.object = object;
    }

    public DomainObject getObject() {
        return object;
    }

}
