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

    public DomainObjectTypeIdCacheImpl() {
    }

    public void setDomainObjectTypeIdDao(DomainObjectTypeIdDao domainObjectTypeIdDao) {
        this.domainObjectTypeIdDao = domainObjectTypeIdDao;
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
            nameToIdMap.put(domainObjectTypeId.getName().toLowerCase(), domainObjectTypeId.getId());
        }

        idToNameMap.clear();
        for (DomainObjectTypeId domainObjectTypeId : domainObjectTypeIds) {
            idToNameMap.put(domainObjectTypeId.getId(), domainObjectTypeId.getName().toLowerCase());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getId(String name) {
        if (name == null) {
            return null;
        }
        return nameToIdMap.get(name.toLowerCase());
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
        String name = idToNameMap.get(((RdbmsId) id).getTypeId());

        if (name != null) {
            name = name.toLowerCase();
        }

        return name;
    }

}
