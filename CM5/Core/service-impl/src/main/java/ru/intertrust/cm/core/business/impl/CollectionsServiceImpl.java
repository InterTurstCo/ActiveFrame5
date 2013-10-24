package ru.intertrust.cm.core.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CollectionsDao;

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

    public void setAccessControlService(AccessControlService accessControlService) {
        this.accessControlService = accessControlService;
    }

    @Override
    public IdentifiableObjectCollection findCollection(String collectionName, SortOrder sortOrder, List<Filter> filterValues,
                                                       int offset, int limit) {
        // TODO get userId from EJB Context
        String user = "admin";
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
        // TODO get userId from EJB Context
        String user = "admin";

        AccessToken accessToken = accessControlService.createCollectionAccessToken(user);
        return collectionsDao.findCollectionCount(collectionName, filterValues, accessToken);
    }

    /** {@inheritDoc} */
    @Override
    public IdentifiableObjectCollection findCollectionByQuery(String query, int offset, int limit) {
        // TODO get userId from EJB Context
        String user = "admin";
        AccessToken accessToken = accessControlService.createCollectionAccessToken(user);
        return collectionsDao.findCollectionByQuery(query, offset, limit, accessToken);
    }

    /** {@inheritDoc} */
    @Override
    public IdentifiableObjectCollection findCollectionByQuery(String query) {
        return findCollectionByQuery(query, 0, 0);
    }

}
