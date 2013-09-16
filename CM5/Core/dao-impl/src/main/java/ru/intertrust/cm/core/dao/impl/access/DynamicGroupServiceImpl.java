package ru.intertrust.cm.core.dao.impl.access;

import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getSqlName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.DynamicGroupConfig;
import ru.intertrust.cm.core.config.model.TrackDomainObjectsConfig;
import ru.intertrust.cm.core.config.model.doel.DoelExpression;
import ru.intertrust.cm.core.dao.access.DynamicGroupService;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;

/**
 * Реализация сервиса по работе с динамическими группами пользователей
 * @author atsvetkov
 */
public class DynamicGroupServiceImpl extends BaseDynamicGroupServiceImpl implements DynamicGroupService {

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
    public void notifyDomainObjectChanged(Id objectId) {
        String status = getStatusFor(objectId);

        List<DynamicGroupConfig> dynamicGroups = getDynamicGroupsFor(objectId, status);
        for (DynamicGroupConfig dynamicGroupConfig : dynamicGroups) {
            Id contextObjectid = getContextObjectId(dynamicGroupConfig, objectId);
            Id dynamicGroupId = refreshUserGroup(dynamicGroupConfig.getName(), contextObjectid);
            List<Long> groupMembres = getAllGroupMembersFor(dynamicGroupConfig, objectId, contextObjectid);

            refreshGroupMembers(dynamicGroupId, groupMembres);
        }
    }

    @Override
    public void notifyDomainObjectCreated(Id objectId) {

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
        DoelExpression reverseBindContextExpr =
                doelResolver.createReverseExpression(bindContextExpr, ((RdbmsId) objectId).getTypeName());
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
     * Возвращает динамические группы для изменяемого объекта, которые нужно пересчитывать.
     * @param objectId изменяемый доменный объект
     * @param status статус изменяемого доменног объекта
     * @return список конфигураций динамических групп
     */
    private List<DynamicGroupConfig> getDynamicGroupsFor(Id objectId, String status) {
        List<DynamicGroupConfig> dynamicGroups =
                configurationExplorer.getDynamicGroupConfigsByTrackDO(objectId, status);
        return dynamicGroups;
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
            contextObjectId = new RdbmsId(contextObjectType, idLongValue);
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

        List<DynamicGroupConfig> dynamicGroups = getDynamicGroupsFor(objectId, status);
        for (DynamicGroupConfig dynamicGroupConfig : dynamicGroups) {
            Id contextObjectId = getContextObjectId(dynamicGroupConfig, objectId);
            Id dynamicGroupId = deleteUserGroupByGroupNameAndObjectId(dynamicGroupConfig.getName(), ((RdbmsId)contextObjectId).getId());           
            cleanGroupMembers(dynamicGroupId);            
        }

    }

}
