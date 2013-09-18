package ru.intertrust.cm.core.dao.impl.access;

import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getSqlName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.BindContextConfig;
import ru.intertrust.cm.core.config.model.DoelAware;
import ru.intertrust.cm.core.config.model.DynamicGroupConfig;
import ru.intertrust.cm.core.config.model.GetPersonConfig;
import ru.intertrust.cm.core.config.model.TrackDomainObjectsConfig;
import ru.intertrust.cm.core.config.model.doel.DoelExpression;
import ru.intertrust.cm.core.config.model.doel.DoelExpression.Element;
import ru.intertrust.cm.core.dao.access.DynamicGroupService;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;

import java.util.*;

import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getSqlName;

/**
 * Реализация сервиса по работе с динамическими группами пользователей
 * @author atsvetkov
 */
public class DynamicGroupServiceImpl extends BaseDynamicGroupServiceImpl implements DynamicGroupService {

    final static Logger logger = LoggerFactory.getLogger(DynamicGroupServiceImpl.class);

    private static final String USER_GROUP_DOMAIN_OBJECT = "User_Group";

    private static final String GROUP_MEMBER_DOMAIN_OBJECT = "Group_Member";

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private DomainObjectDao domainObjectDao;

    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
        doelResolver.setConfigurationExplorer(configurationExplorer);
    }

    public void setDomainObjectDao(DomainObjectDao domainObjectDao) {
        this.domainObjectDao = domainObjectDao;
    }

    @Override
    public void notifyDomainObjectChanged(Id objectId, List<String> modifiedFieldNames) {
        String status = getStatusFor(objectId);
        List<DynamicGroupConfig> dynamicGroups =
                getDynamicGroupsForToRecalculateForUpdate(objectId, status, modifiedFieldNames);
        logger.info("Found dynamic groups by id " + objectId + " :" + dynamicGroups);
        for (DynamicGroupConfig dynamicGroupConfig : dynamicGroups) {
            Id contextObjectid = getContextObjectId(dynamicGroupConfig, objectId);
            Id dynamicGroupId = refreshUserGroup(dynamicGroupConfig.getName(), contextObjectid);
            List<Long> groupMembres = getAllGroupMembersFor(dynamicGroupConfig, objectId, contextObjectid);

            refreshGroupMembers(dynamicGroupId, groupMembres);
        }
    }

    /**
     * Выполняет пересчет всех динамических групп, где созданный объект является отслеживаемым (указан в теге <track-domain-objects>).
     */
    @Override
    public void notifyDomainObjectCreated(Id objectId) {
        String status = getStatusFor(objectId);

        List<DynamicGroupConfig> dynamicGroups = getDynamicGroupsForToRecalculate(objectId, status);
        for (DynamicGroupConfig dynamicGroupConfig : dynamicGroups) {
            Id contextObjectid = getContextObjectId(dynamicGroupConfig, objectId);
            Id dynamicGroupId = refreshUserGroup(dynamicGroupConfig.getName(), contextObjectid);
            List<Long> groupMembres = getAllGroupMembersFor(dynamicGroupConfig, objectId, contextObjectid);

            refreshGroupMembers(dynamicGroupId, groupMembres);
        }

    }

    /**
     * Получает список персон динамической группы по дескриптору группы и контекстному объекту.
     * @param dynamicGroupConfig дескриптор динамической группы
     * @param objectId отслеживаемй объект динамической группы. Используется для расчета обратного Doel выражения.
     * @param contextObjectid контекстному объекту динамической группы
     * @return список персон группы
     */
    private List<Long> getAllGroupMembersFor(DynamicGroupConfig dynamicGroupConfig, Id objectId, Id contextObjectid) {
        List<?> result = null;
        List<Long> groupMembers = new ArrayList<Long>();
        TrackDomainObjectsConfig trackDomainObjects = dynamicGroupConfig.getMembers().getTrackDomainObjects();
        if (trackDomainObjects != null && trackDomainObjects.getBindContext() != null) {
            String bindContextDoel = trackDomainObjects.getBindContext().getDoel();
            DoelExpression bindContextExpr = DoelExpression.parse(bindContextDoel);
            DoelExpression reverseBindContextExpr = createReverseExpression(objectId, bindContextExpr);

            String getPersonDoel = null;
            if (trackDomainObjects.getGetPerson() != null && trackDomainObjects.getGetPerson().getDoel() != null) {
                getPersonDoel = trackDomainObjects.getGetPerson().getDoel();
            }

            String getGroupPersonsDoel = createRetrieveGroupPersonsDoel(reverseBindContextExpr, getPersonDoel);
            DoelExpression reverseGetPersonExpr = DoelExpression.parse(getGroupPersonsDoel);
            result = doelResolver.evaluate(reverseGetPersonExpr, contextObjectid);

            groupMembers = convertToListOfLongs(result);
        }
        return groupMembers;
    }

    /**
     * Создает обратное Doel выражение.
     * @param objectId объект, относительно которого вычисляется переданное прямое выражение
     * @param bindContextExpr прямое Doel выражение.
     * @return обратное Doel выражение
     */
    private DoelExpression createReverseExpression(Id objectId, DoelExpression bindContextExpr) {
        String domainObjectTypeName = domainObjectTypeIdCache.getName(((RdbmsId) objectId).getTypeId());
        DoelExpression reverseBindContextExpr =
                doelResolver.createReverseExpression(bindContextExpr, domainObjectTypeName);
        return reverseBindContextExpr;
    }

    private String createRetrieveGroupPersonsDoel(DoelExpression reverseBindContextExpr, String getPersonDoel) {
        String getGroupPersonsDoel = null;
        if (getPersonDoel != null) {
            getGroupPersonsDoel = reverseBindContextExpr.toString() + "." + getPersonDoel;

        } else {
            getGroupPersonsDoel = reverseBindContextExpr.toString() + "." + "id";
        }
        return getGroupPersonsDoel;
    }

    /**
     * Конвертирует результат вычисления Doel выражения в список целочисленных типов.
     * @param result результат вычисления Doel выражения
     * @return список целочисленных типов
     */
    private List<Long> convertToListOfLongs(List<?> result) {
        List<Long> groupMembers = new ArrayList<Long>();

        if (result != null && result.size() > 0) {
            for (Map<String, Object> entry : (List<Map<String, Object>>) result) {
                if (entry != null) {
                    Collection values = entry.values();
                    Long longValue = (Long) values.iterator().next();
                    groupMembers.add(longValue);

                }
            }
        }

        return groupMembers;
    }

    /**
     * Возвращает динамические группы для изменяемого объекта, которые нужно пересчитывать. Поиск динамических группп выполняется по
     * типу и статусу отслеживаемого объекта, а также по измененным полям.
     * @param objectId изменяемый доменный объект
     * @param status статус изменяемого доменног объекта
     * @param modifiedFieldNames списке измененных полей доменного объекта
     * @return список конфигураций динамических групп
     */
    private List<DynamicGroupConfig> getDynamicGroupsForToRecalculateForUpdate(Id objectId, String status, List<String> modifiedFieldNames) {
    private List<DynamicGroupConfig> getDynamicGroupsFor(Id objectId, String status) {
        String objectTypeName = domainObjectTypeIdCache.getName(((RdbmsId) objectId).getTypeId());
        List<DynamicGroupConfig> dynamicGroups =
                configurationExplorer.getDynamicGroupConfigsByTrackDO(objectId, status);

        Set<DynamicGroupConfig> filteredDynamicGroups = new HashSet<DynamicGroupConfig>();

        for (DynamicGroupConfig dynamicGroup : dynamicGroups) {

            if (dynamicGroup.getMembers() != null && dynamicGroup.getMembers().getTrackDomainObjects() != null) {
                TrackDomainObjectsConfig trackDomainObjectsConfig = dynamicGroup.getMembers().getTrackDomainObjects();

                BindContextConfig bindContext = trackDomainObjectsConfig.getBindContext();

                if (containsFieldNameInDoel(modifiedFieldNames, bindContext)) {
                    filteredDynamicGroups.add(dynamicGroup);
                }

                GetPersonConfig getPerson = trackDomainObjectsConfig.getGetPerson();

                if (containsFieldNameInDoel(modifiedFieldNames, getPerson)) {
                    filteredDynamicGroups.add(dynamicGroup);
                }

            }
        }

        return new ArrayList<DynamicGroupConfig>(filteredDynamicGroups);
    }

    /**
     * Возвращает динамические группы для изменяемого объекта, которые нужно пересчитывать. Поиск динамических группп выполняется по
     * типу и статусу отслеживаемого объекта
     * @param objectId изменяемый доменный объект
     * @param status статус изменяемого доменног объекта
     * @return список конфигураций динамических групп
     */
    private List<DynamicGroupConfig> getDynamicGroupsForToRecalculate(Id objectId, String status) {
        List<DynamicGroupConfig> dynamicGroups =
                configurationExplorer.getDynamicGroupConfigsByTrackDO(objectId, status);
                configurationExplorer.getDynamicGroupConfigsByTrackDO(objectTypeName, status);
        return dynamicGroups;
    }

    /**
     * Проверяет, содержит ли Doel выражение в первом элементе название поля, которое было указано в списке переданных
     * измененных полей.
     * @param modifiedFieldNames списке измененных полей.
     * @param doelAware Doel выражение
     * @return true, если содержит, иначе false
     */
    private boolean containsFieldNameInDoel(List<String> modifiedFieldNames, DoelAware doelAware) {
        if (doelAware != null && doelAware.getDoel() != null) {
            String bindContextDoel = doelAware.getDoel();

            DoelExpression expr = DoelExpression.parse(bindContextDoel);

            if (expr.getElements().length > 0) {
                Element firstDoelElement = expr.getElements()[0];
                if (firstDoelElement.getClass().equals(DoelExpression.Field.class)) {
                    String firstDoelElementName = ((DoelExpression.Field) firstDoelElement).getName();
                    if (modifiedFieldNames.contains(firstDoelElementName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Возвращает контекстный объект для динамической группы и отслеживаемого (изменяемого) доменного объекта
     * @param dynamicGroupConfig конфигурация динамической группы
     * @param objectId идентификатор отслеживаемого доменного объекта
     * @return идентификатор контекстного объекта
     */
    private Id getContextObjectId(DynamicGroupConfig dynamicGroupConfig, Id objectId) {
        TrackDomainObjectsConfig trackDomainObjects = dynamicGroupConfig.getMembers().getTrackDomainObjects();
        Id contextObjectid = null;

        if (trackDomainObjects != null && trackDomainObjects.getBindContext() != null) {
            String bindContextDoel = trackDomainObjects.getBindContext().getDoel();
            DoelExpression expr = DoelExpression.parse(bindContextDoel);
            List<?> result = doelResolver.evaluate(expr, objectId);
            String contextObjectType = dynamicGroupConfig.getContext().getDomainObject().getType();
            contextObjectid = convertToId(result, contextObjectType);

        } else {
            contextObjectid = objectId;
        }

        return contextObjectid;
    }

    private Id convertToId(List<?> result, String contextObjectType) {
        Id contextObjectId = null;
        if (result != null && result.size() > 0) {
            Collection values = ((List<Map<String, Object>>) result).get(0).values();
            Long idLongValue = (Long) values.iterator().next();
            contextObjectId = new RdbmsId(domainObjectTypeIdCache.getId(contextObjectType), idLongValue);
        }
        return contextObjectId;
    }

    /**
     * Пересчитывает список персон динамической группы.
     * @param dynamicGroupId идентификатор динамической группы
     * @param personIds список персон
     */
    private void refreshGroupMembers(Id dynamicGroupId, List<Long> personIds) {
        cleanGroupMembers(dynamicGroupId);

        insertGroupMembers(dynamicGroupId, personIds);
    }

    // TODO Optimize performance
    private void insertGroupMembers(Id dynamicGroupId, List<Long> personIds) {
        List<DomainObject> groupMembers = new ArrayList<DomainObject>();
        for (Long personId : personIds) {

            GenericDomainObject groupMemeber = new GenericDomainObject();
            groupMemeber.setTypeName(GROUP_MEMBER_DOMAIN_OBJECT);
            groupMemeber.setLong("person_id", personId);
            groupMemeber.setParent(dynamicGroupId);
            groupMembers.add(groupMemeber);

        }
        List<DomainObject> savedGroupMembers = domainObjectDao.save(groupMembers);
    }

    private void cleanGroupMembers(Id dynamicGroupId) {
        String query = generateDeleteGroupMembersQuery();

        Map<String, Object> parameters = initializeDeleteGroupMembersParameters(dynamicGroupId);
        int count = jdbcTemplate.update(query, parameters);
    }

    private String generateDeleteGroupMembersQuery() {
        String tableName = getSqlName(GROUP_MEMBER_DOMAIN_OBJECT);
        StringBuilder query = new StringBuilder();
        query.append("delete from ");
        query.append(tableName);
        query.append(" where master=:master");

        return query.toString();

    }

    protected Map<String, Object> initializeDeleteGroupMembersParameters(Id groupId) {
        RdbmsId rdbmsId = (RdbmsId) groupId;
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("master", rdbmsId.getId());
        return parameters;
    }

    /**
     * Добавляет группу с данным именем и контекстным объектом, если группы нет в базе данных
     * @param dynamicGroupName имя динамической группы
     * @param contextObjectId контекстный объект динамической группы
     * @return обновленную динамическую группу
     */
    private Id refreshUserGroup(String dynamicGroupName, Id contextObjectId) {
        Id userGroupId = getUserGroupByGroupNameAndObjectId(dynamicGroupName, ((RdbmsId) contextObjectId).getId());

        if (userGroupId == null) {
            GenericDomainObject userGroupDO = new GenericDomainObject();
            userGroupDO.setTypeName(USER_GROUP_DOMAIN_OBJECT);
            userGroupDO.setString("group_name", dynamicGroupName);
            if (contextObjectId != null) {
                userGroupDO.setLong("object_id", ((RdbmsId) contextObjectId).getId());
            }
            DomainObject updatedObject = domainObjectDao.save(userGroupDO);
            userGroupId = updatedObject.getId();
        }
        return userGroupId;
    }

    @Override
    public void cleanDynamicGroupsFor(Id objectId) {
        String status = getStatusFor(objectId);

        List<DynamicGroupConfig> dynamicGroups = getDynamicGroupsForToRecalculate(objectId, status);
        for (DynamicGroupConfig dynamicGroupConfig : dynamicGroups) {
            Id contextObjectId = getContextObjectId(dynamicGroupConfig, objectId);
            Id dynamicGroupId = deleteUserGroupByGroupNameAndObjectId(dynamicGroupConfig.getName(), ((RdbmsId)contextObjectId).getId());
            cleanGroupMembers(dynamicGroupId);
        }

    }

}
