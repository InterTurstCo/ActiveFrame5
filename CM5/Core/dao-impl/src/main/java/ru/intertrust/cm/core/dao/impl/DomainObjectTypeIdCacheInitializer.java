package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdDao;

/**
 * @author vmatsukevich
 *         Date: 8/19/13
 *         Time: 3:19 PM
 */
public class DomainObjectTypeIdCacheInitializer {

    @Autowired
    private DomainObjectTypeIdDao domainObjectTypeIdDao;

    public void init() {
        DomainObjectTypeIdCache.getInstance().build(domainObjectTypeIdDao.readAll());
    }
}
