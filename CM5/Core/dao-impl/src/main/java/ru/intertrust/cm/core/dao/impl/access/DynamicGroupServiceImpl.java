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
    public void refreshDynamicGroupsFor(Id objectId) {
        RdbmsId id = (RdbmsId) objectId;
        List<DynamicGroupConfig> dynamicGroups =
                configurationExplorer.getDynamicGroupConfigsByContextType(id.getTypeName());

        for (DynamicGroupConfig dynamicGroupConfig : dynamicGroups) {
            Id dynamicGroupId = refreshUserGroup(dynamicGroupConfig.getName(), id);
            List<Id> personIds = getGroupMembersFor(dynamicGroupConfig, objectId);
            refreshGroupMembers(dynamicGroupId, personIds);
        }
    }

    @Override
    public void notiyDomainObjectChanged(Id objectId) {
        String status = getStatusFor(objectId);
        RdbmsId id = (RdbmsId) objectId;

        List<DynamicGroupConfig> dynamicGroups = getDynamicGroupsFor(objectId, status);
        for (DynamicGroupConfig dynamicGroupConfig : dynamicGroups) {
            Id contextObjectid = getContextObjectId(dynamicGroupConfig, objectId);

            Id dynamicGroupId = refreshUserGroup(dynamicGroupConfig.getName(), contextObjectid);

            List<Id> personIds = getAllGroupMembersFor(dynamicGroupConfig, contextObjectid);
            refreshGroupMembers(dynamicGroupId, personIds);
        }

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
     * @param objectId идентификатор отслеживаемый доменный объект
     * @return идентификатор контекстного объекта
     */    
    private Id getContextObjectId(DynamicGroupConfig dynamicGroupConfig, Id objectId) {
        TrackDomainObjectsConfig trackDomainObjects = dynamicGroupConfig.getMembers().getTrackDomainObjects();
        Id contextObjectid = null;

        if (trackDomainObjects != null && trackDomainObjects.getBindContext() != null) {
            String bindContextDoel = dynamicGroupConfig.getMembers().getTrackDomainObjects().getBindContext().getDoel();
            DoelExpression expr = DoelExpression.parse(bindContextDoel);
            List<?> result = doelResolver.evaluate(expr, objectId);
            String doelResultType = getDoelReturnValueType(bindContextDoel);

            contextObjectid = convertToId(result, doelResultType);

        } else {
            contextObjectid = objectId;
        }

        return contextObjectid;
    }

    private Id convertToId(List<?> result, String doelResultType) {
        Id contextObjectId = null;
        if (result != null && result.size() > 0) {
            Collection values = ((List<Map<String, Object>>) result).get(0).values();
            Long idLongValue = (Long) values.iterator().next();
            contextObjectId = new RdbmsId(doelResultType, idLongValue);
        }
        return contextObjectId;
    }

    private String getDoelReturnValueType(String bindContextDoel) {
        //TODO
        return "Person";
    }

    /**
     * Получает список персон для конкретного экземпляра динамической группы (определяется названием группы и контекстным объектом)
     * @param dynamicGroupConfig конфигурация динамической группы
     * @param contextObjectId идентификатор контекстного объекта
     * @return список идентификаторов персон группы
     */
    private List<Id> getAllGroupMembersFor(DynamicGroupConfig dynamicGroupConfig, Id contextObjectId) {

        List<Id> personIds = new ArrayList<Id>();
        if (dynamicGroupConfig.getMembers() != null && dynamicGroupConfig.getMembers().getTrackDomainObjects() != null) {

            final String contextObjectType = dynamicGroupConfig.getContext().getDomainObject().getType();
            TrackDomainObjectsConfig trackDomainObjectsConfig = dynamicGroupConfig.getMembers().getTrackDomainObjects();
            String query = null;
            final String getPersonField = getGetPersonField(trackDomainObjectsConfig);
            String trackDomainObjectType = trackDomainObjectsConfig.getType();
            
            if (trackDomainObjectsConfig.getBindContext() == null) {
                query = "Select p.id from " + trackDomainObjectType
                        + " p where p.id = :contextObjectId";

            } else {
                String bindContectField = trackDomainObjectsConfig.getBindContext().getDoel();

                query = "Select o." + getPersonField + " from " + trackDomainObjectType + " o where o." +
                        bindContectField + " = " + ":contextObjectId";

            }
            Map<String, Object> parameters = initializeGetGroupMembersParameters(contextObjectId);
            personIds =
                    jdbcTemplate.query(query, parameters, new ListObjectIdRowMapper(getPersonField, contextObjectType));
            System.out.println("Found personds for dynamic group : " + dynamicGroupConfig.getName()
                    + " and context id: " + contextObjectId + " : " + personIds);
        }

        return personIds;
    }
    
    /**
     * Получает список персон динамической группы
     * @param dynamicGroupConfig конфигурация динамической группы
     * @param contextObjectId идентификатор контекстного объекта
     * @return список идентификаторов персон группы
     */
    private List<Id> getGroupMembersFor(DynamicGroupConfig dynamicGroupConfig, Id contextObjectId) {

        List<Id> personIds = new ArrayList<Id>();
        if (dynamicGroupConfig.getMembers() != null && dynamicGroupConfig.getMembers().getTrackDomainObjects() != null) {

            final String contextObjectType = dynamicGroupConfig.getContext().getDomainObject().getType();
            TrackDomainObjectsConfig trackDomainObjectsConfig = dynamicGroupConfig.getMembers().getTrackDomainObjects();
            String query = null;
            final String getPersonField = getGetPersonField(trackDomainObjectsConfig);
            if (trackDomainObjectsConfig.getBindContext() != null) {
                String trackDomainObjectType = trackDomainObjectsConfig.getType();

                String bindContectField = trackDomainObjectsConfig.getBindContext().getDoel();
                query = "Select d." + getPersonField + " from " + contextObjectType + " p inner join "
                        + trackDomainObjectType + " d on d." + bindContectField + " = p.id where p.id = " +
                        ":contextObjectId";
            } else {
                query = "Select p." + getPersonField + " from " + contextObjectType
                        + " p where p.id = :contextObjectId";

            }

            Map<String, Object> parameters = initializeGetGroupMembersParameters(contextObjectId);
            personIds =
                    jdbcTemplate.query(query, parameters, new ListObjectIdRowMapper(getPersonField, contextObjectType));
            System.out.println("Found personds for dynamic group : " + dynamicGroupConfig.getName()
                    + " and context id: " + contextObjectId + " : " + personIds);
        }

        return personIds;
    }
    
    private Map<String, Object> initializeGetGroupMembersParameters(Id contextObjectId) {
        RdbmsId rdbmsId = (RdbmsId) contextObjectId;
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("contextObjectId", rdbmsId.getId());
        return parameters;
    }

    private String getGetPersonField(TrackDomainObjectsConfig trackDomainObjectsConfig) {
        String getPersonField = null;
        if (trackDomainObjectsConfig.getGetPerson() != null) {
            getPersonField = trackDomainObjectsConfig.getGetPerson().getDoel();
        } else {
            // если тег get-person не заполнен, то сам отслеживаемый объект совпадает с контекстным
            getPersonField = "id";
        }
        return getPersonField;
    }

    /**
     * Пересчитывает список персон динамической группы.
     * @param dynamicGroupId идентификатор динамической группы
     * @param personIds список персон
     */
    private void refreshGroupMembers(Id dynamicGroupId, List<Id> personIds) {

        cleanGroupMembers(dynamicGroupId);

        insertGroupMembers(dynamicGroupId, personIds);
    }

    // TODO Optimize performance
    private void insertGroupMembers(Id dynamicGroupId, List<Id> personIds) {
        List<DomainObject> groupMembers = new ArrayList<DomainObject>();
        for (Id personId : personIds) {

            GenericDomainObject groupMemeber = new GenericDomainObject();
            groupMemeber.setTypeName(GROUP_MEMBER_DOMAIN_OBJECT);
            groupMemeber.setLong("person_id", ((RdbmsId) personId).getId());
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
    public void cleanDynamicGroupsFor(Id id) {
        // TODO Auto-generated method stub
    }

}
