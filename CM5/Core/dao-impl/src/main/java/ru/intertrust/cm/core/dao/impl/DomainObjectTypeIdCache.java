package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.DomainObjectTypeId;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdDao;

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

    @Autowired
    private DomainObjectTypeIdDao domainObjectTypeIdDao;

    private Map<String, Integer> nameToIdMap = new ConcurrentHashMap<>();

    private DomainObjectTypeIdCache() {

    }

    /**
     * Конструирует кэш из списка {@link DomainObjectTypeId}
     */
    public void  build() {
        List<DomainObjectTypeId> domainObjectTypeIds = domainObjectTypeIdDao.readAll();

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
