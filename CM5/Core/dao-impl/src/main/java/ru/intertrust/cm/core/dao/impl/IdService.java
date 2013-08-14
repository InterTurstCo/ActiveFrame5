package ru.intertrust.cm.core.dao.impl;

import org.springframework.stereotype.Service;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;

/**
 * Создание фабричных методов идентификаторов и доменных объектов с идентификатором, чтобы избавиться от new RdbsId().
 * Необходимо для клиентского слоя, который, например из URL вытащит строку Id документа.
 */
@Service
public class IdService {

    /**
     * Создает идентификатор
     *
     * @param stringRep строковое представление индентификатора
     * @return идентификатор {@link ru.intertrust.cm.core.business.api.dto.RdbmsId}
     */
    public Id createId(String stringRep) {
        return new RdbmsId(stringRep);
    }
}