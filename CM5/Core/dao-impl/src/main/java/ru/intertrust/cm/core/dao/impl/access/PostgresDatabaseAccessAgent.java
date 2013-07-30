package ru.intertrust.cm.core.dao.impl.access;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.dao.access.AccessType;
import ru.intertrust.cm.core.dao.access.CreateChildAccessType;
import ru.intertrust.cm.core.dao.access.DomainObjectAccessType;
import ru.intertrust.cm.core.dao.impl.PostgreSqlQueryHelper;

/**
 * Реализация агента БД по запросам прав доступа для PostgreSQL.
 * <p>Перед использованием объекта необходимо установить корректный источник данных вызовом метода
 * {@link #setDataSource(DataSource)}. Обычно это делается через соотвествующий атрибут или тег конфигурации
 * объекта в Spring-контексте приложения (beans.xml), где создаётся и сам объект.
 * 
 * @author apirozhkov
 */
public class PostgresDatabaseAccessAgent implements DatabaseAccessAgent {

    private NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * Устанавливает источник данных, который будет использоваться для выполнения запросов.
     * 
     * @param dataSource Источник данных
     */
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public boolean checkDomainObjectAccess(int userId, Id objectId, AccessType type) {
        RdbmsId id = (RdbmsId) objectId;
        String opCode = makeAccessTypeCode(type);                
        String query = getQueryForCheckDomainObjectAccess(id);        
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("user_id", userId);
        parameters.put("object_id", id.getId());
        parameters.put("operation", opCode);        
        Integer result = jdbcTemplate.queryForObject(query, parameters, Integer.class);
        return result > 0;
    }

    private String getQueryForCheckDomainObjectAccess(RdbmsId id) {
        String domainObjectAclTable = getAclTableName(id);
        String query = "select count(*) from " + domainObjectAclTable + " a " +
                "inner join group_member gm on a.group_id = gm.parent " +
                "where gm.person_id = :user_id and a.object_id = :object_id and a.operation = :operation";
        return query;
    }

    @Override
    public Id[] checkMultiDomainObjectAccess(int userId, Id[] objectIds, AccessType type) {
        RdbmsId[] ids = (RdbmsId[]) objectIds;
        String opCode = makeAccessTypeCode(type);
        if (objectIds == null || objectIds.length == 0) {
            return new RdbmsId[0];
        }

        List<Id> idsWithAllowedAccess = new ArrayList<Id>();
        IdSorterByType idSorterByType = new IdSorterByType(ids);

        for (final String domainObjectType : idSorterByType.getDomainObjectTypes()) {
            List<Id> checkedIds = getIdsWithAllowedAccessByType(userId, opCode, idSorterByType, domainObjectType);
            idsWithAllowedAccess.addAll(checkedIds);
        }

        return idsWithAllowedAccess.toArray(new Id[idsWithAllowedAccess.size()]);
    }
    
    /**
     * Возвращает список id доменных объектов одного типа, для которых разрешен доступ.
     * @param userId id пользователя
     * @param opCode код операции доступа
     * @param sorterByType {@see IdSorterByType}
     * @param domainObjectType тип доменного объекта
     * @return
     */
    private List<Id> getIdsWithAllowedAccessByType(int userId, String opCode, IdSorterByType sorterByType,
            final String domainObjectType) {
        String query = getQueryForCheckMultiDomainObjectAccess(domainObjectType);

        List<Long> listIds = convertRdbmsIdsToLongIds(sorterByType.getIdsOfType(domainObjectType));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("user_id", userId);
        parameters.put("object_ids", listIds);
        parameters.put("operation", opCode);

        List<Id> checkedIds = jdbcTemplate.query(query, parameters, new RowMapper<Id>() {

            @Override
            public Id mapRow(ResultSet rs, int rowNum) throws SQLException {
                Long objectId = rs.getLong("object_id");
                RdbmsId id = new RdbmsId(domainObjectType, objectId);
                return id;
            }

        });
        return checkedIds;
    }

    private String getQueryForCheckMultiDomainObjectAccess(String domainObjectType) {
        String domainObjectAclTable = getAclTableNameFor(domainObjectType);

        String query = "select a.object_id object_id from " + domainObjectAclTable + " a " +
                "inner join group_member gm on a.group_id = gm.parent " +
                "where gm.person_id = :user_id and a.object_id in (:object_ids) and a.operation = :operation";
        return query;
    }
    
    private List<Long> convertRdbmsIdsToLongIds(List<RdbmsId> objectIds) {
        List<Long> idList = new ArrayList<Long>();
        for (RdbmsId id : objectIds) {
            if (id != null && id.getClass().equals(RdbmsId.class)) {
                idList.add(id.getId());
            }
        }
        return idList;
    }

    @Override
    public AccessType[] checkDomainObjectMultiAccess(int userId, Id objectId, AccessType[] types) {
        RdbmsId id = (RdbmsId) objectId;
        String[] opCodes = makeAccessTypeCodes(types);

        String query = getQueryForCheckDomainObjectMultiAccess(id);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("user_id", userId);
        parameters.put("object_id", id.getId());
        parameters.put("operations", Arrays.asList(opCodes));

        return jdbcTemplate.query(query, parameters, new RowMapper<AccessType>() {
            @Override
            public AccessType mapRow(ResultSet rs, int rowNum) throws SQLException {
                String code = rs.getString("operation");
                return decodeAccessType(code);
            }
        }).toArray(new AccessType[0]);
    }

    private String getQueryForCheckDomainObjectMultiAccess(RdbmsId id) {
        String domainObjectAclTable = getAclTableName(id);
        String query = "select a.operation operation from " + domainObjectAclTable + " a " +
                "inner join group_member gm on a.group_id = gm.parent " +
                "where gm.person_id = :user_id and a.object_id = :object_id and a.operation in (:operations)";
        return query;
    }

    private String getAclTableName(RdbmsId id) {
        String domainObjectTable = id.getTypeName();
        return getAclTableNameFor(domainObjectTable);
    }

    private String getAclTableNameFor(String domainObjectTable) {
        String domainObjectAclTable = domainObjectTable + PostgreSqlQueryHelper.ACL_TABLE_SUFFIX;
        return domainObjectAclTable;
    }

    private static String makeAccessTypeCode(AccessType type) {
        // Разрешения на чтение хранятся в отдельной таблице, поэтому код "R" не используется
        /*if (DomainObjectAccessType.READ.equals(type)) {
            return "R";
        }*/
        if (DomainObjectAccessType.WRITE.equals(type)) {
            return "W";
        }
        if (DomainObjectAccessType.DELETE.equals(type)) {
            return "D";
        }
        if (CreateChildAccessType.class.equals(type.getClass())) {
            CreateChildAccessType ccType = (CreateChildAccessType) type;
            return new StringBuilder("C_").append(ccType.getChildType()).toString();
        }
        throw new IllegalArgumentException("Unknown access type: " + type);
    }

    private static String[] makeAccessTypeCodes(AccessType[] types) {
        String[] codes = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            codes[i] = makeAccessTypeCode(types[i]);
        }
        return codes;
    }

    private static AccessType decodeAccessType(String code) {
        String[] codeParts = code.split("_", 2);
        if ("W".equals(codeParts[0])) {
            return DomainObjectAccessType.WRITE;
        }
        if ("D".equals(codeParts[0])) {
            return DomainObjectAccessType.DELETE;
        }
        if ("C".equals(codeParts[0])) {
            return new CreateChildAccessType(codeParts[1]);
        }
        throw new IllegalArgumentException("Unknown access type: " + code);
    }

    @Override
    public boolean checkUserGroup(int userId, String groupName) {
        String query = getQueryForCheckUserGroup();

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("user_id", userId);
        parameters.put("group_name", groupName);
        Integer result = jdbcTemplate.queryForObject(query, parameters, Integer.class);
        return result > 0;
    }

    private String getQueryForCheckUserGroup() {
        String query = "select count(*) from user_group ug " +
                "inner join group_member gm on ug.id = gm.parent " +
                "where gm.person_id = :user_id and ug.group_name = :group_name";
        return query;
    }
    
    /**
     * Группирует идентификаторы доменных объектов по типу. Это нужно для оптимизации проверки прав доступа к списку ДО.
     * @author atsvetkov
     */
    private class IdSorterByType {

        private Set<String> domainObjectTypes = new HashSet<String>();

        private Map<String, List<RdbmsId>> groupedByTypeObjectIds = new HashMap<String, List<RdbmsId>>();

        public IdSorterByType(RdbmsId[] ids) {
            collectDomainObjectTypes(ids);

            groupIdsByType(ids);
        }

        private void groupIdsByType(RdbmsId[] objectIds) {
            for (String domainObjectType : domainObjectTypes) {
                List<RdbmsId> singleTypeIds = null;
                for (RdbmsId id : objectIds) {
                    if (domainObjectType.equals(id.getTypeName())) {
                        if (groupedByTypeObjectIds.get(domainObjectType) != null) {
                            singleTypeIds = groupedByTypeObjectIds.get(domainObjectType);
                        } else {
                            singleTypeIds = new ArrayList<RdbmsId>();
                            groupedByTypeObjectIds.put(domainObjectType, singleTypeIds);

                        }
                        singleTypeIds.add(id);
                    }
                }
            }
        }

        private void collectDomainObjectTypes(RdbmsId[] objectIds) {
            for (RdbmsId id : objectIds) {
                String typeName = id.getTypeName();
                domainObjectTypes.add(typeName);
            }
        }

        /**
         * Возвращает идентификаторы ДО заданного типа
         * @param domainObjectType тип ДО
         * @return список идентификаторов заданного типа
         */
        public List<RdbmsId> getIdsOfType(String domainObjectType) {
            return groupedByTypeObjectIds.get(domainObjectType);
        }

        /**
         * Возвращает список типов доменных объектов.
         * @return список типов ДО
         */
        public Set<String> getDomainObjectTypes() {
            return domainObjectTypes;
        }
    }

}
