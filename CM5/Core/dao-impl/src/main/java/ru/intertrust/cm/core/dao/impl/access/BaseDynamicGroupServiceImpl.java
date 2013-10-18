package ru.intertrust.cm.core.dao.impl.access;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.impl.doel.DoelResolver;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getSqlName;

/**
 * @author atsvetkov
 */
public class BaseDynamicGroupServiceImpl {

    public static final String USER_GROUP_DOMAIN_OBJECT = "User_Group";

    public static final String GROUP_MEMBER_DOMAIN_OBJECT = "Group_Member";

    protected NamedParameterJdbcTemplate jdbcTemplate;

    protected DoelResolver doelResolver = new DoelResolver();

    @Autowired
    protected DomainObjectTypeIdCache domainObjectTypeIdCache;

    public void setDomainObjectTypeIdCache(DomainObjectTypeIdCache domainObjectTypeIdCache) {
        this.domainObjectTypeIdCache = domainObjectTypeIdCache;
        doelResolver.setDomainObjectTypeIdCache(domainObjectTypeIdCache);
    }

    /**
     * Устанавливает источник соединений
     * @param dataSource
     */
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        doelResolver.setDataSource(dataSource);

    }

    /**
     * Удаляет динамическую группу по названию и контекстному объекту.
     * @param groupName название динамической группы
     * @param contextObjectId идентфикатор контекстного объекта
     * @return идентификатор удаленной динамической группы
     */
    protected Id deleteUserGroupByGroupNameAndObjectId(String groupName, Long contextObjectId) {
        Id userGroupId = getUserGroupByGroupNameAndObjectId(groupName, contextObjectId);

        if (userGroupId != null) {
            String query = generateDeleteUserGroupQuery();

            Map<String, Object> parameters = initializeProcessUserGroupWithContextParameters(groupName, contextObjectId);
            jdbcTemplate.update(query, parameters);
        }

        return userGroupId;
    }

    private String generateDeleteUserGroupQuery() {
        String tableName = getSqlName(USER_GROUP_DOMAIN_OBJECT);
        StringBuilder query = new StringBuilder();
        query.append("Delete from ");
        query.append(tableName).append(" ug");
        query.append(" where ug.group_name = :group_name and ug.object_id = :object_id");

        return query.toString();
    }

    /**
     * Возвращает идентификатор группы пользователей по имени группы и идентификатору контекстного объекта
     * @param groupName имя динамической группы
     * @param contextObjectId идентификатор контекстного объекта
     * @return идентификатор группы пользователей
     */
    protected Id getUserGroupByGroupNameAndObjectId(String groupName, Long contextObjectId) {
        String query = generateGetUserGroupWithContextQuery();

        Map<String, Object> parameters = initializeProcessUserGroupWithContextParameters(groupName, contextObjectId);
        Integer doTypeId = domainObjectTypeIdCache.getId(USER_GROUP_DOMAIN_OBJECT);
        return jdbcTemplate.query(query, parameters, new ObjectIdRowMapper("id", doTypeId));
    }

    private Map<String, Object> initializeProcessUserGroupWithContextParameters(String groupName, Long contextObjectId) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("group_name", groupName);
        parameters.put("object_id", contextObjectId);
        return parameters;
    }

    private String generateGetUserGroupWithContextQuery() {
        String tableName = getSqlName(USER_GROUP_DOMAIN_OBJECT);
        StringBuilder query = new StringBuilder();
        query.append("select ug.id from ");
        query.append(tableName).append(" ug");
        query.append(" where ug.group_name = :group_name and ug.object_id = :object_id");

        return query.toString();
    }

    protected Id getUserGroupByGroupName(String groupName) {
        String query = generateGetUserGroupQuery();

        Map<String, Object> parameters = initializeProcessUserGroupParameters(groupName);
        Integer doTypeId = domainObjectTypeIdCache.getId(USER_GROUP_DOMAIN_OBJECT);
        return jdbcTemplate.query(query, parameters, new ObjectIdRowMapper("id", doTypeId));
    }

    private Map<String, Object> initializeProcessUserGroupParameters(String groupName) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("group_name", groupName);
        return parameters;
    }

    private String generateGetUserGroupQuery() {
        String tableName = getSqlName(USER_GROUP_DOMAIN_OBJECT);
        StringBuilder query = new StringBuilder();
        query.append("select ug.id from ");
        query.append(tableName).append(" ug");
        query.append(" where ug.group_name = :group_name");
        return query.toString();
    }

    /**
     * Возвращает статус доменного объекта
     * @param objectId идентификатор доменного объекта
     * @return статус доменного объекта
     */
    protected String getStatusFor(Id objectId) {
        String query = generateGetStatusForQuery(objectId);

        Map<String, Object> parameters = initializeGetStatusParameters(objectId);
        return jdbcTemplate.queryForObject(query, parameters, String.class);
    }

    private String generateGetStatusForQuery(Id objectId) {
        RdbmsId id = (RdbmsId) objectId;
        String tableName = getSqlName(domainObjectTypeIdCache.getName(id.getTypeId()));
        StringBuilder query = new StringBuilder();
        query.append("select o.status from ");
        query.append(tableName).append(" o");
        query.append(" where o.id = :object_id");

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

    /**
     * Отображает {@link java.sql.ResultSet} на идентификатор доменного объекта {@link Id}
     * @author atsvetkov
     */
    protected class ObjectIdRowMapper implements ResultSetExtractor<Id> {

        private String idField;

        private Integer domainObjectType;

        public ObjectIdRowMapper(String idField, Integer domainObjectType) {
            this.idField = idField;
            this.domainObjectType = domainObjectType;
        }

        @Override
        public Id extractData(ResultSet rs) throws SQLException, DataAccessException {
            Id id = null;
            while (rs.next()) {
                Long longValue = rs.getLong(idField);

                id = new RdbmsId(domainObjectType, longValue);

            }
            return id;
        }
    }

}
