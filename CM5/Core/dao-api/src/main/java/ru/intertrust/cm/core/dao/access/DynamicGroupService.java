package ru.intertrust.cm.core.dao.access;

import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Сервис по работе с динамическими группами пользователей. Выполняет пересчет динамических групп.
 * @author atsvetkov
 */
public interface DynamicGroupService {

    /**
     * Пересчитывает динамические группы для доменного объекта.
     * @param id - ДО для которого происходит персчет
     */
    void refreshDynamicGroupsFor(Id id);

    /**
     * Удаляет динамические групппы и их дочерние объекты (персоны динамических групп).
     * @param id
     */
    void cleanDynamicGroupsFor(Id id);

    /**
     * Пересчет динамических групп при изменении отслеживаемого объекта. Выполняет пересчет дмнамических групп для
     * отслеживаемого объекта и для связанных объектов.
     * @param objectId отслеживаемый доменный объект
     */
    void notiyDomainObjectChanged(Id objectId);
}
