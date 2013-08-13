package ru.intertrust.cm.core.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.DomainObjectTypeId;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdDao;

import java.util.List;

/**
 * Инициализатор {@link DomainObjectTypeIdCache}
 * @author vmatsukevich
 *         Date: 8/13/13
 *         Time: 3:30 PM
 */
public class DomainObjectTypeIdCacheInitializer {

    @Autowired
    private DomainObjectTypeIdDao domainObjectTypeIdDao;

    /**
     * Конструирует {@link DomainObjectTypeIdCacheInitializer}
     */
    public DomainObjectTypeIdCacheInitializer() {
    }

    /**
     * Устанавливает {@link #domainObjectTypeIdDao}
     * @param domainObjectTypeIdDao {@link DomainObjectTypeIdDao}
     */
    public void setDomainObjectTypeIdDao(DomainObjectTypeIdDao domainObjectTypeIdDao) {
        this.domainObjectTypeIdDao = domainObjectTypeIdDao;
    }

    /**
     * Инициализирует {@link DomainObjectTypeIdCache}
    */
    public void init() {
        List<DomainObjectTypeId> domainObjectTypeIds = domainObjectTypeIdDao.readAll();
        DomainObjectTypeIdCache.getInstance().build(domainObjectTypeIds);
    }
}
