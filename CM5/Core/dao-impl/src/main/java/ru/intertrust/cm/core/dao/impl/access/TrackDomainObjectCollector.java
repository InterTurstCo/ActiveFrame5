package ru.intertrust.cm.core.dao.impl.access;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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
import ru.intertrust.cm.core.config.model.CollectorSettings;
import ru.intertrust.cm.core.config.model.DynamicGroupConfig;
import ru.intertrust.cm.core.config.model.DynamicGroupTrackDomainObjectsConfig;
import ru.intertrust.cm.core.config.model.doel.DoelExpression;
import ru.intertrust.cm.core.dao.access.DynamicGroupCollector;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;

public class TrackDomainObjectCollector extends BaseDynamicGroupServiceImpl implements DynamicGroupCollector {

    private DynamicGroupConfig config;
    private DynamicGroupTrackDomainObjectsConfig trackDomainObjects;

    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    @Override
    public List<Id> getPersons(Id contextId) {
        List<Id> groupMembres = getGroupMembers(contextId, false);
        return groupMembres;
    }

    @Override
    public List<Id> getGroups(Id contextId) {
        List<Id> groupMembres = getGroupMembers(contextId, true);
        return groupMembres;
    }

    // Получение типов, на которые реагирует текер
    @Override
    public List<String> getTrackTypeNames() {
        List<String> result = new ArrayList<String>();
        // Не указан TrackDomainObjects или не указан тип, следим только за текущим типом
        if (trackDomainObjects == null || trackDomainObjects.getType() == null) {
            result.add(config.getContext().getDomainObject().getType());
        } else {
            result.add(trackDomainObjects.getType());
        }
        return result;
    }

    @Override
    public List<Id> getInvalidContexts(DomainObject domainObject,
            List<FieldModification> modifiedFields) {

        List<Id> result = new ArrayList<Id>();

        if (trackDomainObjects.getBindContext() != null && trackDomainObjects.getBindContext().getDoel() != null) {
            DoelExpression expression = DoelExpression.parse(trackDomainObjects.getBindContext().getDoel());
            List<Value> invalidContexts = doelResolver.evaluate(expression, domainObject.getId());
            //Проверяем статус
            if (trackDomainObjects.getStatus() != null) {
                String status = getStatusFor(domainObject.getId());
                if (trackDomainObjects.getStatus().equals(status)) {
                    result = getIdList(invalidContexts);
                }
            } else {
                result = getIdList(invalidContexts);
            }
        } else {
            result.add(domainObject.getId());
        }

        return result;
    }

    @Override
    public void init(DynamicGroupConfig config, CollectorSettings setings) {
        this.config = config;
        if (config.getMembers() != null) {
            this.trackDomainObjects = (DynamicGroupTrackDomainObjectsConfig) setings;
        }
    }

    /**
     * Возвращает динамические группы для изменяемого объекта, которые нужно пересчитывать. Поиск динамических групп выполняется по типу и статусу
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
     * Получает список персон динамической группы по дескриптору группы и контекстному объекту.
     * 
     * @param contextObjectid
     *            контекстному объекту динамической группы
     * @param groups
     *            флаг указывающий какой doel использовать. Для получения групп или для получения пользователей
     * @return список персон группы
     */
    private List<Id> getGroupMembers(Id contextObjectid, boolean groups) {
        List<Id> result = new ArrayList<Id>();

        // В зависимости от флага получаем или группы или персоны
        String doel = null;
        if (groups) {
            if (trackDomainObjects.getGetGroup() != null) {
                doel = trackDomainObjects.getGetGroup().getDoel();
            }
        } else {
            if (trackDomainObjects.getGetPerson() != null) {
                doel = trackDomainObjects.getGetPerson().getDoel();
            }
        }

        if (doel != null) {
            DoelExpression getMemberExpr = DoelExpression
                    .parse(doel);
            List<Value> valueList = doelResolver.evaluate(getMemberExpr,
                    contextObjectid);
            result.addAll(getIdList(valueList));

            if (groups) {
                result = getDynGroups(result, trackDomainObjects.getGetGroup().getGroupName());
            }
        }
        return result;
    }

    private List<Id> getDynGroups(List<Id> groupOwners, String groupName) {
        List<Id> result = new ArrayList<Id>();

        if (groupName != null && groupName.length() > 0) {
            for (Id groupOwner : groupOwners) {
                DomainObject dynGroup = personManagementService.findDynamicGroup(groupName, groupOwner);
                if (dynGroup != null) {
                    result.add(dynGroup.getId());
                }
            }
        } else {
            result = groupOwners;
        }
        return result;
    }

    /**
     * Создает обратное Doel выражение.
     * 
     * @param objectId
     *            объект, относительно которого вычисляется переданное прямое выражение
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
     * Возвращает контекстный объект для динамической группы и отслеживаемого (изменяемого) доменного объекта
     * 
     * @param dynamicGroupConfig
     *            конфигурация динамической группы
     * @param objectId
     *            идентификатор отслеживаемого доменного объекта
     * @return идентификатор контекстного объекта
     */
    private List<Id> getContextObjectId(DynamicGroupConfig dynamicGroupConfig, Id objectId) {

        List<Id> result = new ArrayList<>();

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
     * Добавляет группу с данным именем и контекстным объектом, если группы нет в базе данных
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
     * Конвертирует результат вычисления doel выражения поиска контекстного объекта в идентификатор контекстного объекта.
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
