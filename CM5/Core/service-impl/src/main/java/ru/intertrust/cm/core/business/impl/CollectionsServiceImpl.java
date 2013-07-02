package ru.intertrust.cm.core.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.api.CollectionsDao;

import java.util.List;

/**
 * @author vmatsukevich
 *         Date: 7/1/13
 *         Time: 6:39 PM
 */
public class CollectionsServiceImpl implements CollectionsService {

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private CollectionsDao collectionsDao;

    public void setCollectionsDao(CollectionsDao collectionsDao) {
        this.collectionsDao = collectionsDao;
    }

    @Override
    public IdentifiableObjectCollection findCollection(String collectionName, List<Filter> filterValues,
                                                       SortOrder sortOrder, int offset, int limit) {
        return collectionsDao.findCollection(collectionName, filterValues, sortOrder, offset, limit);
    }

    @Override
    public int findCollectionCount(String collectionName, List<Filter> filterValues) {
        return collectionsDao.findCollectionCount(collectionName, filterValues);
    }
}
