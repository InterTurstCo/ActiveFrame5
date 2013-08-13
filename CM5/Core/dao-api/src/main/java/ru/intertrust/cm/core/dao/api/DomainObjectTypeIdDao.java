package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.business.api.dto.DomainObjectTypeId;

import java.util.List;

/**
 * Dao для DOMAIN_OBJECT_TYPE_ID
 * @author vmatsukevich
 *         Date: 8/13/13
 *         Time: 2:42 PM
 */
public interface DomainObjectTypeIdDao {

    String DOMAIN_OBJECT_TYPE_ID_TABLE = "DOMAIN_OBJECT_TYPE_ID";
    String ID_COLUMN = "ID";
    String NAME_COLUMN = "NAME";

    /**
     * Возвращает идентификаторы всех типов доменных объектов
     * @return идентификаторы всех типов доменных объектов
     */
    public List<DomainObjectTypeId> readAll();

    /**
     * Создает и сохраняет идентификатор типа доменного объекта
     * @param domainObjectTypeName
     * @return
     */
    public Long insert(String domainObjectTypeName);
}
