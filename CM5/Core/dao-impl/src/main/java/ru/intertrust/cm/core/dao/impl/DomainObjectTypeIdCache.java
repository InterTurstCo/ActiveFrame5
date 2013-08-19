package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.business.api.dto.DomainObjectTypeId;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Кэш идентификаторов типов доменных объектов
 * @author vmatsukevich
 *         Date: 8/13/13
 *         Time: 3:12 PM
 */
public class DomainObjectTypeIdCache {

    private static DomainObjectTypeIdCache instance = new DomainObjectTypeIdCache();

    private Map<String, Integer> nameToIdMap = new ConcurrentHashMap<>();

    /**
     * Возврящает экземпляр {@link DomainObjectTypeIdCache}
     * @return экземпляр {@link DomainObjectTypeIdCache}
     */
    public static DomainObjectTypeIdCache getInstance() {
        return instance;
    }

    private DomainObjectTypeIdCache() {

    }

    /**
     * Конструирует кэш из списка {@link DomainObjectTypeId}
      * @param domainObjectTypeIds список {@link DomainObjectTypeId}
     */
    public void  build(List<DomainObjectTypeId> domainObjectTypeIds) {
        if (domainObjectTypeIds == null || domainObjectTypeIds.isEmpty()) {
            return;
        }

        nameToIdMap.clear();
        for (DomainObjectTypeId domainObjectTypeId : domainObjectTypeIds) {
            nameToIdMap.put(domainObjectTypeId.getName(), domainObjectTypeId.getId());
        }
    }

    /**
     * Добавляет в кэш идентификатор типа доменного объекта
     * @param domainObjectTypeId {@link DomainObjectTypeId}
     */
    public void add(DomainObjectTypeId domainObjectTypeId) {
        if (domainObjectTypeId == null || domainObjectTypeId.getName() == null || domainObjectTypeId.getId() == null) {
            return;
        }

        nameToIdMap.put(domainObjectTypeId.getName(), domainObjectTypeId.getId());
    }

    /**
     * Возвращает идентификатор типа доменного объекта по его имени
     * @param name имя типа доменного объекта
     * @return идентификатор типа доменного объекта
     */
    public Integer getIdByName(String name) {
        return nameToIdMap.get(name);
    }
}
