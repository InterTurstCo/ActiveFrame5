package ru.intertrust.cm.core.dao.api;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.FindObjectsConfig;

/**
 * Сервис получения списка идентификаторов доменных объектов по запросу или с помощью DOEL или с помощью класса
 * @author larin
 * 
 */
public interface DomainObjectFinderService {

    /**
     * Метод получения списка идентификаторов доменных объектов по одному из параметров: запроса, класса или DOEL
     * выражения
     * @param getObjectsConfig
     *            Параметр в котором содержится или имя класса или запрос или doel выражение
     * @param contextDomainObjectId
     *            идентификатор доменного объекта, относительно которого производятся вычисления. Идентификатори
     *            передается в запрос в параметре номер 1 ({1}), а в класс как параметр метода получения списка
     *            объектов.
     * @return
     */
    List<Id> findObjects(FindObjectsConfig getObjectsConfig, Id contextDomainObjectId);
}
