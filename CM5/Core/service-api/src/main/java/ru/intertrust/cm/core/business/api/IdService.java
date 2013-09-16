package ru.intertrust.cm.core.business.api;

import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Создание фабричных методов идентификаторов и доменных объектов с идентификатором, чтобы избавиться от new RdbsId().
 * Необходимо для клиентского слоя, который, например из URL вытащит строку Id документа.
 */
public interface IdService {

    /**
     * Создает идентификатор
     *
     * @param stringRep строковое представление индентификатора
     * @return идентификатор {@link ru.intertrust.cm.core.business.api.dto.RdbmsId}
     */
    Id createId(String stringRep);
}