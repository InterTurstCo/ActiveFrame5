package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.DomainObjectTypeId;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
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
public class DomainObjectTypeIdCacheImpl implements DomainObjectTypeIdCache {

    @Autowired
    private DomainObjectTypeIdDao domainObjectTypeIdDao;

    private Map<String, Integer> nameToIdMap = new ConcurrentHashMap<>();
    private Map<Integer, String> idToNameMap = new ConcurrentHashMap<>();

    private DomainObjectTypeIdCacheImpl() {

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

        idToNameMap.clear();
        for (DomainObjectTypeId domainObjectTypeId : domainObjectTypeIds) {
            idToNameMap.put(domainObjectTypeId.getId(), domainObjectTypeId.getName());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getId(String name) {
        return nameToIdMap.get(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName(Integer id) {
        return idToNameMap.get(id);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getName(Id id) {
        return idToNameMap.get(((RdbmsId) id).getTypeId());
    }

    public void setDomainObjectTypeIdDao(DomainObjectTypeIdDao domainObjectTypeIdDao) {
        this.domainObjectTypeIdDao = domainObjectTypeIdDao;
    }

}
