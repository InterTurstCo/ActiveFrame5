package ru.intertrust.cm.core.dao.impl.access;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.ConfigurationException;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.DynamicGroupConfig;
import ru.intertrust.cm.core.config.model.DynamicGroupTrackDomainObjectsConfig;
import ru.intertrust.cm.core.config.model.TrackDomainObjectsConfig;
import ru.intertrust.cm.core.config.model.doel.DoelExpression;
import ru.intertrust.cm.core.dao.access.DynamicGroupCollector;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;

public class TrackDomainObjectCollector extends BaseDynamicGroupServiceImpl implements DynamicGroupCollector {

    private DynamicGroupConfig config;
    private TrackDomainObjectsConfig settings;

    @Autowired
    protected DomainObjectTypeIdCache domainObjectTypeIdCache;

    @Autowired
    private ConfigurationExplorer configurationExplorer;
    
    public void setDomainObjectTypeIdCache(DomainObjectTypeIdCache domainObjectTypeIdCache) {
        this.domainObjectTypeIdCache = domainObjectTypeIdCache;
    }

    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    @Override
    public List<Id> getPersons(Id domainObjectId, Id contextId) {
        List<Id> groupMembres = getGroupMembers(domainObjectId, contextId, false);
        return groupMembres;
    }

    @Override
    public List<Id> getGroups(Id domainObjectId, Id contextId) {
        List<Id> groupMembres = getGroupMembers(domainObjectId, contextId, true);
        return groupMembres;
    }

    // Получение типов, на которые реагирует текер
    @Override
    public List<String> getTrackTypeNames() {
        List<String> result = new ArrayList<String>();
        // Не указан TrackDomainObjects, следим только за текущим типом
        if (settings == null) {
            result.add(config.getContext().getDomainObject().getType());
        } else {
            result.add(settings.getType());
        }
        return null;
    }

    @Override
    public List<Id> getInvalidContexts(DomainObject domainObject,
            List<FieldModification> modifiedFields) {
        String status = getStatusFor(domainObject.getId());

        List<Id> result = new ArrayList<Id>();
        List<DynamicGroupConfig> dynamicGroups = getDynamicGroupsToRecalculate(domainObject.getId(), status);
        for (DynamicGroupConfig dynamicGroupConfig : dynamicGroups) {
            result = getContextObjectId(dynamicGroupConfig, domainObject.getId());
        }

        return result;
    }

    @Override
    public void init(DynamicGroupConfig config) {
        this.config = config;
        if (config.getMembers() != null) {
            this.settings = config.getMembers().getTrackDomainObjects();
        }
    }

    /**
     * Возвращает динамические группы для изменяемого объекта, которые нужно
     * пересчитывать. Поиск динамических групп выполняется по типу и статусу
     * отслеживаемого объекта
     * 
     * @param objectId
     *            изменяемый доменный объект
     * @param status
     *            статус изменяемого доменного объекта
     * @return список конфигураций динамических групп
     */
    private List<DynamicGroupConfig> getDynamicGroupsToRecalculate(Id objectId,
            String status) {
        List<DynamicGroupConfig> dynamicGroups = configurationExplorer
                .getDynamicGroupConfigsByTrackDO(
                        domainObjectTypeIdCache.getName(objectId), status);
        return dynamicGroups;
    }

    /**
     * Получает список персон динамической группы по дескриптору группы и
     * контекстному объекту.
     * 
     * @param objectId
     *            отслеживаемый объект динамической группы. Используется для
     *            расчета обратного Doel выражения.
     * @param contextObjectid
     *            контекстному объекту динамической группы
     * @param groups
     *            флаг указывающий какой doel использовать. Для получения групп
     *            или для получения пользователей
     * @return список персон группы
     */
    private List<Id> getGroupMembers(Id objectId, Id contextObjectid, boolean groups) {
        List<Id> result = new ArrayList<Id>();
        DynamicGroupTrackDomainObjectsConfig trackDomainObjects = config.getMembers().getTrackDomainObjects();
        if (trackDomainObjects != null
                && trackDomainObjects.getBindContext() != null) {
            String bindContextDoel = trackDomainObjects.getBindContext()
                    .getDoel();
            DoelExpression bindContextExpr = DoelExpression
                    .parse(bindContextDoel);
            DoelExpression reverseBindContextExpr = createReverseExpression(
                    objectId, bindContextExpr);

            // В зависимости от флага получаем или группы или персоны
            String doel = null;
            if (trackDomainObjects.getGetPerson() != null) {
                if (groups) {
                    if (trackDomainObjects.getGetPerson().getDoel() != null) {
                        doel = trackDomainObjects.getGetGroup().getDoel();
                    }
                } else {
                    if (trackDomainObjects.getGetPerson().getDoel() != null) {
                        doel = trackDomainObjects.getGetPerson().getDoel();
                    }
                }
            }

            String getGroupPersonsDoel = createRetrieveGroupPersonsDoel(
                    reverseBindContextExpr, doel);
            DoelExpression reverseGetPersonExpr = DoelExpression
                    .parse(getGroupPersonsDoel);
            List<Value> valueList = doelResolver.evaluate(reverseGetPersonExpr,
                    contextObjectid);
            for (Value value : valueList) {
                result.add((Id) value.get());
            }
        }
        return result;
    }

    /**
     * Создает обратное Doel выражение.
     * 
     * @param objectId
     *            объект, относительно которого вычисляется переданное прямое
     *            выражение
     * @param bindContextExpr
     *            прямое Doel выражение.
     * @return обратное Doel выражение
     */
    private DoelExpression createReverseExpression(Id objectId,
            DoelExpression bindContextExpr) {
        String domainObjectTypeName = domainObjectTypeIdCache
                .getName(((RdbmsId) objectId).getTypeId());
        DoelExpression reverseBindContextExpr = doelResolver
                .createReverseExpression(bindContextExpr, domainObjectTypeName);
        return reverseBindContextExpr;
    }

    private String createRetrieveGroupPersonsDoel(
            DoelExpression reverseBindContextExpr, String getPersonDoel) {
        String getGroupPersonsDoel = null;
        if (getPersonDoel != null) {
            getGroupPersonsDoel = reverseBindContextExpr.toString() + "."
                    + getPersonDoel;

        } else {
            getGroupPersonsDoel = reverseBindContextExpr.toString() + "."
                    + "id";
        }
        return getGroupPersonsDoel;
    }

    /**
     * Возвращает контекстный объект для динамической группы и отслеживаемого
     * (изменяемого) доменного объекта
     * 
     * @param dynamicGroupConfig
     *            конфигурация динамической группы
     * @param objectId
     *            идентификатор отслеживаемого доменного объекта
     * @return идентификатор контекстного объекта
     */
    private List<Id> getContextObjectId(DynamicGroupConfig dynamicGroupConfig,
            Id objectId) {

        List<Id> result = new ArrayList<>();
        TrackDomainObjectsConfig trackDomainObjects = dynamicGroupConfig
                .getMembers().getTrackDomainObjects();

        if (trackDomainObjects != null
                && trackDomainObjects.getBindContext() != null) {
            String bindContextDoel = trackDomainObjects.getBindContext()
                    .getDoel();
            DoelExpression expr = DoelExpression.parse(bindContextDoel);
            List<Value> valueList = doelResolver.evaluate(expr, objectId);
            for (Value value : valueList) {
                result.add((Id) value.get());
            }

        } else {
            result.add(objectId);
        }

        return result;
    }

    /**
     * Добавляет группу с данным именем и контекстным объектом, если группы нет
     * в базе данных
     * 
     * @param dynamicGroupName
     *            имя динамической группы
     * @param contextObjectId
     *            контекстный объект динамической группы
     * @return обновленную динамическую группу
     */
    private Id refreshUserGroup(String dynamicGroupName, Id contextObjectId) {
        Id userGroupId = getUserGroupByGroupNameAndObjectId(dynamicGroupName,
                ((RdbmsId) contextObjectId).getId());

        if (userGroupId == null) {
            userGroupId = createUserGroup(dynamicGroupName, contextObjectId);
        }
        return userGroupId;
    }

    /**
     * Конвертирует результат вычисления doel выражения поиска контекстного
     * объекта в идентификатор контекстного объекта.
     * 
     * @param result
     *            вычисления doel выражения поиска контекстного объекта
     * @return идентификатор контекстного объекта
     */
    private Id convertToId(List<Value> result) {
        Id contextObjectId = null;
        if (result != null && result.size() > 0) {
            Value value = ((List<Value>) result).get(0);

            if (value.getClass().equals(ReferenceValue.class)) {
                contextObjectId = ((ReferenceValue) value).get();
            } else {
                throw new ConfigurationException(
                        "Doel expression in bind context should result in ReferenceValue");
            }
        }
        return contextObjectId;
    }
}
