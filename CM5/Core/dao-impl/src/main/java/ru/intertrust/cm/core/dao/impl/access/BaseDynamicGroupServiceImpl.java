package ru.intertrust.cm.core.dao.impl.access;

import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getSqlName;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.dao.impl.doel.DoelResolver;

/**
 * @author atsvetkov
 */
public class BaseDynamicGroupServiceImpl {

    public static final String USER_GROUP_DOMAIN_OBJECT = "User_Group";

    public static final String GROUP_MEMBER_DOMAIN_OBJECT = "Group_Member";

    protected NamedParameterJdbcTemplate jdbcTemplate;

    protected DoelResolver doelResolver = new DoelResolver();

    /**
     * Устанавливает источник соединений
     * @param dataSource
     */
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        doelResolver.setDataSource(dataSource);

    }

    /**
     * Возвращает идентификатор группы пользователей по имени группы и идентификатору контекстного объекта
     * @param groupName имя динамической группы
     * @param contextObjectId идентификатор контекстного объекта
     * @return идентификатор группы пользователей
     */
    protected Id getUserGroupByGroupNameAndObjectId(String groupName, Long contextObjectId) {
        String query = generateGetUserGroupQuery();

        Map<String, Object> parameters = initializeGetUserGroupParameters(groupName, contextObjectId);
        return jdbcTemplate.query(query, parameters, new ObjectIdRowMapper("id", USER_GROUP_DOMAIN_OBJECT));
    }

    private Map<String, Object> initializeGetUserGroupParameters(String groupName, Long contextObjectId) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("group_name", groupName);
        parameters.put("object_id", contextObjectId);
        return parameters;
    }

    private String generateGetUserGroupQuery() {
        String tableName = getSqlName(USER_GROUP_DOMAIN_OBJECT);
        StringBuilder query = new StringBuilder();
        query.append("select ug.id from ");
        query.append(tableName).append(" ug");
        query.append(" where ug.group_name = :group_name and ug.object_id = :object_id");

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
        String tableName = getSqlName(id.getTypeName());
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

        private String domainObjectType;

        public ListObjectIdRowMapper(String idField, String domainObjectType) {
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

        private String domainObjectType;

        public ObjectIdRowMapper(String idField, String domainObjectType) {
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
