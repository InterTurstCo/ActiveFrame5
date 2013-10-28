package ru.intertrust.cm.core.dao.access;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.model.DynamicGroupConfig;

/**
 * Интерфейс класса, выполняющего пересчет динамической группы
 * @author larin
 * 
 */
public interface DynamicGroupCollector {

    /**
     * Вычисляет персоны, входящие в состав динамической группы. Вызывается при
     * расчете состава динамической группы, в случае если данная динамическая
     * группа определена для пересчета алгоритмом определения зависимых
     * контекстных групп. Возвращает список идентификаторов пользователей,
     * входящих в группу
     * @param contextId
     *            идентификатор доменного объекта контекста динамической группы
     * @param domainObjectId
     *            идентификатор доменного объекта который отслеживался
     * @return
     */
    List<Id> getPersons(Id domainObjectId, Id contextId);

    /**
     * Вычисляет группы, входящие в состав динамической группы. Вызывается при
     * расчете состава динамической группы, в случае если данная динамическая
     * группа определена для пересчета алгоритмом определения зависимых
     * контекстных групп. Возвращает список идентификаторов групп, входящих в
     * группу.
     * @param contextId
     *            идентификатор доменного объекта контекста динамической группы
     * @param domainObjectId
     *            идентификатор доменного объекта который отслеживался
     * @return
     */
    List<Id> getGroups(Id domainObjectId, Id contextId);

    /**
     * Возвращает список типов, изменение которых влечет за собой изменение
     * состава динамической группы.
     * @return
     */
    List<String> getTrackTypeNames();

    /**
     * Возвращает список идентификаторов доменных групп, состав которых
     * необходимо пересчитать.
     * @return
     */
    List<Id> getInvalidContexts(DomainObject domainObject, List<FieldModification> modifiedFields);

    /**
     * Инициализация коллектора данными из конфигурационного xml
     */
    void init(DynamicGroupConfig config);
}
