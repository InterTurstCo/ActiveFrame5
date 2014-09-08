package ru.intertrust.cm.core.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.Dto;
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
import ru.intertrust.cm.core.dao.api.DomainObjectFinderService;
import ru.intertrust.cm.core.dao.impl.doel.DoelResolver;
import ru.intertrust.cm.core.model.SearchException;

/**
 * Имплементация сервиса получения списка идентификаторов доменных объектов с помощью класса, DOEL или запроса
 * @author larin
 * 
 */
public class DomainObjectFinderServiceImpl implements DomainObjectFinderService {

    @Autowired
    private CollectionsDao collectionService;

    @Autowired
    private DoelResolver doelResolver;

    @Autowired
    private AccessControlService accessService;

    @Autowired
    private CurrentUserAccessor currentUserAccessor;

    /**
     * Поиск доменных объектов по одному из способов в зависимости от типа параметра findObjectsConfig. Поиск может
     * производится с помощью класса, запроса или DOEL выражения
     */
    @Override
    public List<Id> findObjects(FindObjectsConfig findObjectsConfig, Id contextDomainObjectId, Dto extensionContext) {
        List<Id> result = null;
        AccessToken accessToken = accessService.createCollectionAccessToken(currentUserAccessor.getCurrentUser());
        try {
            if (findObjectsConfig.getFindObjectType() instanceof FindObjectsClassConfig) {
                //Поиск с помощью класса
                FindObjectsClassConfig config = (FindObjectsClassConfig) findObjectsConfig.getFindObjectType();
                Class<DomainObjectFinder> finderClass = (Class<DomainObjectFinder>) Class.forName(config.getData());
                DomainObjectFinder findObjects = finderClass.newInstance();
                findObjects.init(config.getSettings(), extensionContext);
                result = findObjects.findObjects(contextDomainObjectId);
            } else if (findObjectsConfig.getFindObjectType() instanceof FindObjectsQueryConfig) {
                // Поиск с помощью запроса
                FindObjectsQueryConfig config = (FindObjectsQueryConfig) findObjectsConfig.getFindObjectType();
                List<Value> params = new ArrayList<Value>();
                if (contextDomainObjectId != null) {
                    params.add(new ReferenceValue(contextDomainObjectId));
                }
                IdentifiableObjectCollection collection =
                        collectionService.findCollectionByQuery(config.getData(), params, 0, 1000, accessToken);
                result = new ArrayList<Id>();
                for (IdentifiableObject identifiableObject : collection) {
                    result.add(identifiableObject.getId());
                }
            } else if (findObjectsConfig.getFindObjectType() instanceof FindObjectsDoelConfig) {
                //Поиск с помощью DOEL
                FindObjectsDoelConfig config = (FindObjectsDoelConfig) findObjectsConfig.getFindObjectType();
                DoelExpression expression = DoelExpression.parse(config.getData());
                List<Value> values = doelResolver.evaluate(expression, contextDomainObjectId, accessToken);
                result = new ArrayList<Id>();
                for (Value value : values) {
                    if (value instanceof ReferenceValue)
                        result.add(((ReferenceValue) value).get());
                }
            }

            return result;
        } catch (Exception ex) {
            throw new SearchException("Error find objects", ex);
        }
    }

}
