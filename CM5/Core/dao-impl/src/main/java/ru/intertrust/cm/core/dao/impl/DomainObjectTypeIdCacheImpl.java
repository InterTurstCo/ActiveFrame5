package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.Case;
import ru.intertrust.cm.core.business.api.dto.DomainObjectTypeId;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
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

    private Map<CaseTolerantKey, Integer> nameToIdMap = new ConcurrentHashMap<>();
    private Map<Integer, String> idToNameMap = new ConcurrentHashMap<>();

    public DomainObjectTypeIdCacheImpl() {
    }

    public void setDomainObjectTypeIdDao(DomainObjectTypeIdDao domainObjectTypeIdDao) {
        this.domainObjectTypeIdDao = domainObjectTypeIdDao;
    }

    /**
     * Конструирует кэш из списка {@link DomainObjectTypeId}
     */
    @Override
    public void build() {
        List<DomainObjectTypeId> domainObjectTypeIds = domainObjectTypeIdDao.readAll();

        if (domainObjectTypeIds == null || domainObjectTypeIds.isEmpty()) {
            return;
        }

        nameToIdMap.clear();
        for (DomainObjectTypeId domainObjectTypeId : domainObjectTypeIds) {
            nameToIdMap.put(new CaseTolerantKey(domainObjectTypeId.getName()), domainObjectTypeId.getId());
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
        if (name == null) {
            return null;
        }
        return nameToIdMap.get(new CaseTolerantKey(name));
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

    private class CaseTolerantKey {
        private String key;

        private CaseTolerantKey(String key) {
            if (key != null) {
                this.key = Case.toLower(key);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CaseTolerantKey key1 = (CaseTolerantKey) o;

            if (key != null ? !key.equals(key1.key) : key1.key != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return key != null ? key.hashCode() : 0;
        }
    }

}
