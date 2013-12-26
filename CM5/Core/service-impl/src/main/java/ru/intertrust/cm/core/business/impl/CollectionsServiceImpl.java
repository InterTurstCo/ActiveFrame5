package ru.intertrust.cm.core.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import java.util.Collections;
import java.util.List;

/**
 * @author vmatsukevich
 *         Date: 7/1/13
 *         Time: 6:39 PM
 */
@Stateless
@Local(CollectionsService.class)
@Remote(CollectionsService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class CollectionsServiceImpl implements CollectionsService {

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

    @Override
    public IdentifiableObjectCollection findCollection(String collectionName, SortOrder sortOrder, List<Filter> filterValues,
                                                       int offset, int limit) {
        String user = currentUserAccessor.getCurrentUser();

        AccessToken accessToken = accessControlService.createCollectionAccessToken(user);
        return collectionsDao.findCollection(collectionName, filterValues, sortOrder, offset, limit, accessToken);
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
        String user = currentUserAccessor.getCurrentUser();
        AccessToken accessToken = accessControlService.createCollectionAccessToken(user);
        return collectionsDao.findCollectionCount(collectionName, filterValues, accessToken);
    }

    /** {@inheritDoc} */
    @Override
    public IdentifiableObjectCollection findCollectionByQuery(String query, int offset, int limit) {
        String user = currentUserAccessor.getCurrentUser();
        AccessToken accessToken = accessControlService.createCollectionAccessToken(user);
        return collectionsDao.findCollectionByQuery(query, offset, limit, accessToken);
    }

    /** {@inheritDoc} */
    @Override
    public IdentifiableObjectCollection findCollectionByQuery(String query) {
        return findCollectionByQuery(query, 0, 0);
    }

    /** {@inheritDoc} */
    @Override
    public IdentifiableObjectCollection findCollectionByQuery(String query, List<Value> params, int offset, int limit) {
        String user = currentUserAccessor.getCurrentUser();
        AccessToken accessToken = accessControlService.createCollectionAccessToken(user);
        return collectionsDao.findCollectionByQuery(query, params, offset, limit, accessToken);
    }

    /** {@inheritDoc} */
    @Override
    public IdentifiableObjectCollection findCollectionByQuery(String query, List<Value> params) {
        return findCollectionByQuery(query, params, 0, 0);
    }

}
