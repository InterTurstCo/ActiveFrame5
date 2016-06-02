package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.business.api.dto.DomainObjectTypeId;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;

import java.util.List;

/**
 * Dao для DOMAIN_OBJECT_TYPE_ID
 * @author vmatsukevich
 *         Date: 8/13/13
 *         Time: 2:42 PM
 */
public interface DomainObjectTypeIdDao {

    String DOMAIN_OBJECT_TYPE_ID_TABLE = "domain_object_type_id";
    String ID_COLUMN = "id";
    String NAME_COLUMN = "name";

    /**
     * Возвращает идентификаторы всех типов доменных объектов
     * @return идентификаторы всех типов доменных объектов
     */
    public List<DomainObjectTypeId> readAll();

    /**
     * Создает и сохраняет идентификатор типа доменного объекта
     * @param domainObjectTypeConfig
     * @return
     */
    public Integer insert(DomainObjectTypeConfig domainObjectTypeConfig);

    /**
     * Удаляет идентификатор типа доменного объекта
     * @param domainObjectTypeConfig
     * @return
     */
    public Integer delete(DomainObjectTypeConfig config);


    /**
     * Возвращает идентификатор типа доменного объекта по имени
     * @param configName
     * @return
     */
    public Integer findIdByName(String configName);
}
