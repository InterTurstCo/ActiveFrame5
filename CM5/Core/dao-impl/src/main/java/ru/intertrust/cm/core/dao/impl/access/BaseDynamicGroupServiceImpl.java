package ru.intertrust.cm.core.dao.impl.access;

import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getSqlName;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.FieldModificationImpl;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.api.PersonManagementServiceDao;
import ru.intertrust.cm.core.dao.impl.doel.DoelResolver;
import ru.intertrust.cm.core.dao.impl.utils.DaoUtils;
import ru.intertrust.cm.core.dao.impl.utils.ObjectIdRowMapper;

/**
 * @author atsvetkov
 */
public class BaseDynamicGroupServiceImpl {

    public static final String USER_GROUP_DOMAIN_OBJECT = "user_group";

    public static final String GROUP_MEMBER_DOMAIN_OBJECT = "group_member";

    @Autowired
    protected NamedParameterJdbcOperations jdbcTemplate;

    @Autowired
    protected DoelResolver doelResolver;

    @Autowired
    protected DomainObjectTypeIdCache domainObjectTypeIdCache;

    @Autowired
    protected DomainObjectDao domainObjectDao;

    @Autowired
    protected AccessControlService accessControlService;

    @Autowired
    protected ConfigurationExplorer configurationExplorer;

    @Autowired
    protected PersonManagementServiceDao personManagementService;

    @Autowired
    protected CollectionsDao collectionsService;

    public void setJdbcTemplate(NamedParameterJdbcOperations jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void setDoelResolver(DoelResolver doelResolver) {
        this.doelResolver = doelResolver;
        doelResolver.setDomainObjectTypeIdCache(domainObjectTypeIdCache);
    }

    public void setDomainObjectDao(DomainObjectDao domainObjectDao) {
        this.domainObjectDao = domainObjectDao;
    }

    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    public void setDomainObjectTypeIdCache(DomainObjectTypeIdCache domainObjectTypeIdCache) {
        this.domainObjectTypeIdCache = domainObjectTypeIdCache;
    }


    public void setAccessControlService(AccessControlService accessControlService) {
        this.accessControlService = accessControlService;
    }

    /**
     * Удаляет динамическую группу по названию и контекстному объекту.
     * @param groupName
     *            название динамической группы
     * @param contextObjectId
     *            идентфикатор контекстного объекта
     * @return идентификатор удаленной динамической группы
     */
    protected Id deleteUserGroupByGroupNameAndObjectId(Id userGroupId, String groupName, Long contextObjectId) {
        if (userGroupId != null) {
            String query = generateDeleteUserGroupQuery();

            Map<String, Object> parameters = initializeProcessUserGroupWithContextParameters(groupName, contextObjectId);
            jdbcTemplate.update(query, parameters);
        }

        return userGroupId;
    }

    private String generateDeleteUserGroupQuery() {
        String tableName = getSqlName(GenericDomainObject.USER_GROUP_DOMAIN_OBJECT);
        StringBuilder query = new StringBuilder();
        query.append("delete from ");
        query.append(DaoUtils.wrap(tableName)).append(" ug");
        query.append(" where ug.").append(DaoUtils.wrap("group_name")).append(" = :group_name and ").
                append("ug.").append(DaoUtils.wrap("object_id")).append(" = :object_id");

        return query.toString();
    }

    /**
     * Возвращает идентификатор группы пользователей по имени группы и идентификатору контекстного объекта
     * @param groupName
     *            имя динамической группы
     * @param contextObjectId
     *            идентификатор контекстного объекта
     * @return идентификатор группы пользователей
     */
    protected Id getUserGroupByGroupNameAndObjectId(String groupName, Id contextObjectId) {
        Id result = null;
        DomainObject group = personManagementService.findDynamicGroup(groupName, contextObjectId);
        if (group != null){
            result = group.getId(); 
        }
        return result;
    }

    private Map<String, Object> initializeProcessUserGroupWithContextParameters(String groupName, Long contextObjectId) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("group_name", groupName);
        parameters.put("object_id", contextObjectId);
        return parameters;
    }

    private String generateGetUserGroupWithContextQuery() {
        String tableName = getSqlName(GenericDomainObject.USER_GROUP_DOMAIN_OBJECT);
        StringBuilder query = new StringBuilder();
        query.append("select ug.").append(DaoUtils.wrap("id")).append(" from ");
        query.append(DaoUtils.wrap(tableName)).append(" ug");
        query.append(" where ug.").append(DaoUtils.wrap("group_name")).append(" = :group_name and ug.").
                append(DaoUtils.wrap("object_id")).append(" = :object_id");

        return query.toString();
    }

    public Id getUserGroupByGroupName(String groupName) {
        String query = generateGetUserGroupQuery();

        Map<String, Object> parameters = initializeProcessUserGroupParameters(groupName);
        Integer doTypeId = domainObjectTypeIdCache.getId(GenericDomainObject.USER_GROUP_DOMAIN_OBJECT);
        return jdbcTemplate.query(query, parameters, new ObjectIdRowMapper("id", doTypeId));
    }

    private Map<String, Object> initializeProcessUserGroupParameters(String groupName) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("group_name", groupName);
        return parameters;
    }

    private String generateGetUserGroupQuery() {
        String tableName = getSqlName(GenericDomainObject.USER_GROUP_DOMAIN_OBJECT);
        StringBuilder query = new StringBuilder();
        query.append("select ug.").append(DaoUtils.wrap("id")).append(" from ");
        query.append(DaoUtils.wrap(tableName)).append(" ug");
        query.append(" where ug.").append(DaoUtils.wrap("group_name")).append(" = :group_name");
        return query.toString();
    }

    /**
     * Возвращает статус доменного объекта
     * @param objectId
     *            идентификатор доменного объекта
     * @return статус доменного объекта
     */
    protected String getStatusFor(Id objectId) {
        String status = null;
        String query = generateGetStatusForQuery(objectId);
        Map<String, Object> parameters = initializeGetStatusParameters(objectId);
        status = jdbcTemplate.query(query, parameters, new ResultSetExtractor<String>() {
            @Override
            public String extractData(ResultSet rs) throws SQLException, DataAccessException {
                String status = null;
                while (rs.next()) {
                    status = rs.getString(1);
                }
                return status;
            }
        });

        return status;
    }

    /**
     * Получение имени типа документа
     * @param objectId
     * @return
     */
    protected String getTypeName(Id objectId) {
        return domainObjectTypeIdCache.getName(objectId);
    }

    private String generateGetStatusForQuery(Id objectId) {
        RdbmsId id = (RdbmsId) objectId;

        //Получение типа верхнего уровня
        DomainObjectTypeConfig typeConfig =
                configurationExplorer.getConfig(DomainObjectTypeConfig.class,
                        domainObjectTypeIdCache.getName(id.getTypeId()));
        while (typeConfig.getExtendsAttribute() != null) {
            typeConfig =
                    configurationExplorer.getConfig(DomainObjectTypeConfig.class, typeConfig.getExtendsAttribute());
        }

        String tableName = getSqlName(typeConfig.getName());
        StringBuilder query = new StringBuilder();
        query.append("select s.").append(DaoUtils.wrap("name")).append(" from ").append(DaoUtils.wrap(tableName)).
                append(" o inner join ").append(DaoUtils.wrap(GenericDomainObject.STATUS_DO)).append(" s on ").
                append("s.").append(DaoUtils.wrap("id")).append(" = o.").append(DaoUtils.wrap(GenericDomainObject.STATUS_FIELD_NAME));
        query.append(" where o.").append(DaoUtils.wrap("id")).append(" = :object_id");

        return query.toString();
    }

    private Map<String, Object> initializeGetStatusParameters(Id objectId) {
        RdbmsId rdbmsId = (RdbmsId) objectId;
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("object_id", rdbmsId.getId());
        return parameters;
    }

    /**
     * Отображает {@link java.sql.ResultSet} на список идентификаторов доменных объектов {@link Id}
     * @author atsvetkov
     */
    protected class ListObjectIdRowMapper implements ResultSetExtractor<List<Id>> {

        private String idField;

        private Integer domainObjectType;

        public ListObjectIdRowMapper(String idField, Integer domainObjectType) {
            this.idField = idField;
            this.domainObjectType = domainObjectType;
        }

        @Override
        public List<Id> extractData(ResultSet rs) throws SQLException, DataAccessException {
            List<Id> personIds = new ArrayList<>();

            while (rs.next()) {
                Long longValue = rs.getLong(idField);

                Id id = new RdbmsId(domainObjectType, longValue);
                personIds.add(id);

            }
            return personIds;
        }
    }

    protected Id createUserGroup(String dynamicGroupName, Id contextObjectId) {
        Id userGroupId;
        GenericDomainObject userGroupDO = new GenericDomainObject();
        userGroupDO.setTypeName(GenericDomainObject.USER_GROUP_DOMAIN_OBJECT);
        userGroupDO.setString("group_name", dynamicGroupName);
        if (contextObjectId != null) {
            userGroupDO.setReference("object_id", contextObjectId);
        }
        AccessToken accessToken = accessControlService.createSystemAccessToken("BaseDynamicGroupService");
        DomainObject updatedObject = domainObjectDao.save(userGroupDO, accessToken);
        userGroupId = updatedObject.getId();
        return userGroupId;
    }

    protected List<FieldModification> getNewObjectModificationList(
            DomainObject domainObject) {
        List<FieldModification> result = new ArrayList<FieldModification>();

        for (String fieldName : domainObject.getFields()) {
            result.add(new FieldModificationImpl(fieldName, null, domainObject
                    .getValue(fieldName)));
        }

        return result;
    }

    protected List<FieldModification> getDeletedModificationList(
            DomainObject domainObject) {
        List<FieldModification> result = new ArrayList<FieldModification>();

        for (String fieldName : domainObject.getFields()) {
            result.add(new FieldModificationImpl(fieldName, domainObject
                    .getValue(fieldName), null));
        }

        return result;
    }

    /**
     * Преобразование списка Value в список Id
     * @param valueList
     * @return
     */
    protected List<Id> getIdList(List<Value> valueList) {
        List<Id> result = new ArrayList<Id>();
        for (Value value : valueList) {
            if (value.get() != null) {
                result.add((Id) value.get());
            }
        }
        return result;
    }

    protected List<Id> getIdListFromDomainObjectList(List<DomainObject> domainObjectList) {
        List<Id> result = new ArrayList<Id>();
        if (domainObjectList != null) {
            for (DomainObject value : domainObjectList) {
                result.add(value.getId());
            }
        }
        return result;
    }

    /**
     * Добавление элементов коллекции без дублирования
     * @param targetCollection
     * @param sourceCollection
     */
    protected void addAllWithoutDuplicate(List targetCollection, List sourceCollection) {
        if (sourceCollection != null) {
            for (Object id : sourceCollection) {
                if (!targetCollection.contains(id)) {
                    targetCollection.add(id);
                }
            }
        }
    }


}
