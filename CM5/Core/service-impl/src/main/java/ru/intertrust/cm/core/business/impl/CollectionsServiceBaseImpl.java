package ru.intertrust.cm.core.business.impl;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsServiceDelegate;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.model.RemoteSuitableException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Реализация сервиса коллекций
 * @author vmatsukevich
 *         Date: 7/1/13
 *         Time: 6:39 PM
 */
public class CollectionsServiceBaseImpl implements CollectionsServiceDelegate {

    final static org.slf4j.Logger logger = LoggerFactory.getLogger(CollectionsServiceBaseImpl.class);

    @Autowired
    private CollectionsDao collectionsDao;

    @Autowired
    private AccessControlService accessControlService;

    @Autowired    
    private CurrentUserAccessor currentUserAccessor; 
    
    public void setCurrentUserAccessor(CurrentUserAccessor currentUserAccessor) {
        this.currentUserAccessor = currentUserAccessor;
    }

    public void setAccessControlService(AccessControlService accessControlService) {
        this.accessControlService = accessControlService;
    }
    
    public void setCollectionsDao(CollectionsDao collectionsDao) {
        this.collectionsDao = collectionsDao;
    }

    public IdentifiableObjectCollection findCollection(String collectionName, SortOrder sortOrder,
            List<? extends Filter> filterValues, int offset, int limit) {
        try {
            String user = currentUserAccessor.getCurrentUser();

            AccessToken accessToken = accessControlService.createCollectionAccessToken(user);
            return collectionsDao.findCollection(collectionName, filterValues, sortOrder, offset, limit, accessToken);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    public IdentifiableObjectCollection findCollection(String collectionName, SortOrder sortOrder,
            List<? extends Filter> filters) {
        return findCollection(collectionName, sortOrder, filters, 0, 0);
    }

    public IdentifiableObjectCollection findCollection(String collectionName, SortOrder sortOrder) {
        return findCollection(collectionName, sortOrder, Collections.EMPTY_LIST, 0, 0);
    }

    public IdentifiableObjectCollection findCollection(String collectionName) {
        return findCollection(collectionName, null, Collections.EMPTY_LIST, 0, 0);
    }

    public int findCollectionCount(String collectionName, List<? extends Filter> filterValues) {
        try {
            String user = currentUserAccessor.getCurrentUser();
            AccessToken accessToken = accessControlService.createCollectionAccessToken(user);
            return collectionsDao.findCollectionCount(collectionName, filterValues, accessToken);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    /** {@inheritDoc} */
    public IdentifiableObjectCollection findCollectionByQuery(String query, int offset, int limit) {
        try {
            String user = currentUserAccessor.getCurrentUser();
            AccessToken accessToken = accessControlService.createCollectionAccessToken(user);
            return collectionsDao.findCollectionByQuery(query, offset, limit, accessToken);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    /** {@inheritDoc} */
    public IdentifiableObjectCollection findCollectionByQuery(String query) {
        return findCollectionByQuery(query, 0, 0);
    }

    /** {@inheritDoc} */
    public IdentifiableObjectCollection findCollectionByQuery(String query,
            List<? extends Value> params, int offset, int limit) {
        try {
            String user = currentUserAccessor.getCurrentUser();
            AccessToken accessToken = accessControlService.createCollectionAccessToken(user);
            return collectionsDao.findCollectionByQuery(query, params, offset, limit, accessToken);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    /** {@inheritDoc} */
    public IdentifiableObjectCollection findCollectionByQuery(String query, List<? extends Value> params) {
        return findCollectionByQuery(query, params, 0, 0);
    }

    public boolean isCollectionEmpty(String collectionName, List<? extends Filter> filters) {
        IdentifiableObjectCollection objectCollection = findCollection(collectionName, null, filters, 0, 1);
        return objectCollection.size() == 0;
    }
}
