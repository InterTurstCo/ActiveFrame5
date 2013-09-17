package ru.intertrust.cm.core.dao.access;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Сервис по работе с динамическими группами пользователей. Выполняет пересчет/удаление динамических групп.
 * @author atsvetkov
 */
public interface DynamicGroupService {

    /**
     * Удаляет динамические групппы, в которых переданный доменный объект является контекстным.
     * @param objectId доменный объект
     */
    void cleanDynamicGroupsFor(Id objectId);

    /**
     * Пересчет динамических групп при изменении доменного объекта. Выполняет пересчет дмнамических групп, на состав
     * которых влияет доменный объект. (Доменный объект входит в список отслеживаемых объектов либо входит в Doel
     * выражение в определении отслеживаемых объектов)
     * @param objectId изменяемый доменный объект
     * @param modifiedFieldNames список измененных полей доменного объекта.
     */
    void notifyDomainObjectChanged(Id objectId, List<String> modifiedFieldNames);
    
    /**
     * Пересчет динамических групп при создании отслеживаемого объекта. Выполняет пересчет дмнамических групп, на состав
     * которых влияет отслеживаемый объект. (Доменный объект входит в список отслеживаемых объектов)
     * @param objectId создаваемый доменный объект
     */
    void notifyDomainObjectCreated(Id objectId);
}
