package ru.intertrust.cm.core.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.dao.api.CollectionsDao;

import java.util.Collections;
import java.util.List;

/**
 * @author vmatsukevich
 *         Date: 7/1/13
 *         Time: 6:39 PM
 */
public class CollectionsServiceImpl implements CollectionsService {

    @Autowired
    private CollectionsDao collectionsDao;

    @Override
    public IdentifiableObjectCollection findCollection(String collectionName, SortOrder sortOrder, List<Filter> filterValues,
                                                       int offset, int limit) {
        return collectionsDao.findCollection(collectionName, filterValues, sortOrder, offset, limit);
    }

    @Override
    public IdentifiableObjectCollection findCollection(String collectionName, SortOrder sortOrder, List<Filter> filters) {
        return findCollection(collectionName, sortOrder, filters, 0, 0);
    }

    @Override
    public IdentifiableObjectCollection findCollection(String collectionName, SortOrder sortOrder) {
        return findCollection(collectionName, sortOrder, Collections.EMPTY_LIST, 0, 0);
    }

    @Override
    public IdentifiableObjectCollection findCollection(String collectionName) {
        return findCollection(collectionName, null, Collections.EMPTY_LIST, 0, 0);
    }

    @Override
    public int findCollectionCount(String collectionName, List<Filter> filterValues) {
        return collectionsDao.findCollectionCount(collectionName, filterValues);
    }

}
