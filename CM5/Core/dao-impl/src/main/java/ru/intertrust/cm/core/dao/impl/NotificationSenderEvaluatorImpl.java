package ru.intertrust.cm.core.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.FindObjectsClassConfig;
import ru.intertrust.cm.core.config.FindObjectsConfig;
import ru.intertrust.cm.core.config.FindObjectsDoelConfig;
import ru.intertrust.cm.core.config.FindObjectsQueryConfig;
import ru.intertrust.cm.core.config.doel.DoelExpression;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.DomainObjectFinder;
import ru.intertrust.cm.core.dao.api.NotificationSenderEvaluator;
import ru.intertrust.cm.core.dao.impl.doel.DoelResolver;
import ru.intertrust.cm.core.model.SearchException;

/**
 * 
 * @author atsvetkov
 *
 */
public class NotificationSenderEvaluatorImpl implements NotificationSenderEvaluator {

    @Autowired
    private CollectionsDao collectionService;

    @Autowired
    private DoelResolver doelResolver;

    @Autowired
    private AccessControlService accessService;

    @Autowired
    private CurrentUserAccessor currentUserAccessor;

    @Override
    public Id findSender(FindObjectsConfig findObjectsConfig, Id contextDomainObjectId) {

        AccessToken accessToken = accessService.createCollectionAccessToken(currentUserAccessor.getCurrentUser());
        try {
            if (findObjectsConfig.getFindObjectType() instanceof FindObjectsClassConfig) {
                FindObjectsClassConfig config = (FindObjectsClassConfig) findObjectsConfig.getFindObjectType();
                List<Id> result = findPersonsByClass(config, contextDomainObjectId);
                if (result != null && result.size() > 0) {
                    return result.get(0);
                }
            } else if (findObjectsConfig.getFindObjectType() instanceof FindObjectsQueryConfig) {
                FindObjectsQueryConfig config = (FindObjectsQueryConfig) findObjectsConfig.getFindObjectType();
                IdentifiableObjectCollection collection = findPersonsByQuery(config, contextDomainObjectId, accessToken);

                if (collection != null && collection.size() > 0) {
                    return collection.getId(0);
                }
            } else if (findObjectsConfig.getFindObjectType() instanceof FindObjectsDoelConfig) {
                FindObjectsDoelConfig config = (FindObjectsDoelConfig) findObjectsConfig.getFindObjectType();
                List<Value> values = findPersonsByDoel(config, contextDomainObjectId, accessToken);
                for (Value value : values) {
                    if (value instanceof ReferenceValue)
                        return (((ReferenceValue) value).get());
                }
            }
            return null;
        } catch (Exception ex) {
            throw new SearchException("Error find objects", ex);
        }
    }

    private List<Id> findPersonsByClass(FindObjectsClassConfig config, Id contextDomainObjectId) throws ClassNotFoundException, InstantiationException,
            IllegalAccessException {
        Class<DomainObjectFinder> finderClass = (Class<DomainObjectFinder>) Class.forName(config.getData());
        DomainObjectFinder findObjects = finderClass.newInstance();
        findObjects.init(config.getSettings());
        List<Id> result = findObjects.findObjects(contextDomainObjectId);
        return result;
    }

    private List<Value> findPersonsByDoel(FindObjectsDoelConfig config, Id contextDomainObjectId, AccessToken accessToken) {
        DoelExpression expression = DoelExpression.parse(config.getData());
        List<Value> values = doelResolver.evaluate(expression, contextDomainObjectId, accessToken);
        return values;
    }

    private IdentifiableObjectCollection findPersonsByQuery(FindObjectsQueryConfig config, Id contextDomainObjectId, AccessToken accessToken) {
        List<Value> params = new ArrayList<Value>();
        if (contextDomainObjectId != null) {
            params.add(new ReferenceValue(contextDomainObjectId));
        }
        IdentifiableObjectCollection collection =
                collectionService.findCollectionByQuery(config.getData(), params, 0, 1000, accessToken);
        return collection;
    }

}
