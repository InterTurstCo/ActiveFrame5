package ru.intertrust.cm.core.dao.exception;

import ru.intertrust.cm.core.business.api.dto.BusinessObject;

/**
 * Представляет случай когда
 *
 * @author skashanski
 *
 */
public class OptimisticLockException extends DataAccessException {


    private final BusinessObject object;


    public OptimisticLockException(BusinessObject object) {
        super();
        this.object = object;
    }





    public BusinessObject getObject() {
        return object;
    }




}
