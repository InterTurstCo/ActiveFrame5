package ru.intertrust.cm.core.dao.impl.access;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.access.AccessType;
import ru.intertrust.cm.core.dao.access.CreateChildAccessType;
import ru.intertrust.cm.core.dao.access.DomainObjectAccessType;
import ru.intertrust.cm.core.dao.access.ExecuteActionAccessType;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.impl.PostgreSqlQueryHelper;
import ru.intertrust.cm.core.dao.impl.utils.DaoUtils;
import ru.intertrust.cm.core.dao.impl.utils.IdSorterByType;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getSqlName;

/**
 * Реализация агента БД по запросам прав доступа для PostgreSQL.
 * <p>Перед использованием объекта необходимо установить корректный источник данных вызовом метода
 * {@link #setDataSource(DataSource)}. Обычно это делается через соотвествующий атрибут или тег конфигурации
 * объекта в Spring-контексте приложения (beans.xml), где создаётся и сам объект.
 *
 * @author apirozhkov
 */
public class PostgresDatabaseAccessAgent implements DatabaseAccessAgent {

    @Autowired
    private DomainObjectTypeIdCache domainObjetcTypeIdCache;

    @Autowired    
    private ConfigurationExplorer configurationExplorer;
    
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void setDomainObjetcTypeIdCache(DomainObjectTypeIdCache domainObjetcTypeIdCache) {
        this.domainObjetcTypeIdCache = domainObjetcTypeIdCache;
    }

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
        
        StringBuilder query = new StringBuilder();
        
        query.append("select count(*) from ").append(DaoUtils.wrap(domainObjectAclTable)).append(" a ");
        query.append(" inner join ").append(DaoUtils.wrap("group_group")).append(" gg on a.").append(DaoUtils.wrap("group_id"))
                .append(" = gg.").append(DaoUtils.wrap("parent_group_id"));
        query.append(" inner join ").append(DaoUtils.wrap("group_member")).append(" gm on gg.")
                .append(DaoUtils.wrap("child_group_id")).append(" = gm.").append(DaoUtils.wrap("usergroup"));
        query.append(" where gm.").append(DaoUtils.wrap("person_id")).append(" = :user_id and a.")
                .append(DaoUtils.wrap("object_id")).append(" = :object_id and a.")
                .append(DaoUtils.wrap("operation")).append(" = :operation");
        return query.toString();
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

        //check configuration 
        for (final Integer domainObjectTypeId : idSorterByType.getDomainObjectTypeIds()) {
            String domainObjectType = domainObjetcTypeIdCache.getName(domainObjectTypeId);
            if (DomainObjectAccessType.READ.equals(type)
                    && configurationExplorer.isReadPermittedToEverybody(domainObjectType)) {
                List<Id> allIdsOfOneType = idSorterByType.getIdsOfType(domainObjectTypeId);
                idsWithAllowedAccess.addAll(allIdsOfOneType);
            } else {
                idSorterByType.getIdsOfType(domainObjectTypeId);
                List<Id> checkedIds = getIdsWithAllowedAccessByType(userId, opCode, idSorterByType, domainObjectTypeId);
                idsWithAllowedAccess.addAll(checkedIds);
            }
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
            final Integer domainObjectType) {
        String query = getQueryForCheckMultiDomainObjectAccess(domainObjetcTypeIdCache.getName(domainObjectType));

        List<Long> listIds = AccessControlUtility.convertRdbmsIdsToLongIds(sorterByType.getIdsOfType(domainObjectType));

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

        StringBuilder query = new StringBuilder();
        
        query.append("select a.").append(DaoUtils.wrap("object_id")).append(" object_id from ")
                .append(DaoUtils.wrap(domainObjectAclTable)).append(" a ");
        query.append(" inner join ").append(DaoUtils.wrap("group_group")).append(" gg on a.").append(DaoUtils.wrap("group_id"))
                .append(" = gg.").append(DaoUtils.wrap("parent_group_id"));
        query.append(" inner join ").append(DaoUtils.wrap("group_member")).append(" gm on gg.").append(DaoUtils.wrap("child_group_id"))
                .append(" = gm.").append(DaoUtils.wrap("usergroup"));
        query.append(" where gm.").append(DaoUtils.wrap("person_id")).append(" = :user_id and a.").append(DaoUtils.wrap("object_id"))
                .append(" in (:object_ids) and ")
                .append("a.").append(DaoUtils.wrap("operation")).append(" = :operation");
        return query.toString();
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

        StringBuilder query = new StringBuilder();
        
        query.append("select a.").append(DaoUtils.wrap("operation")).append(" operation from ")
                .append(DaoUtils.wrap(domainObjectAclTable)).append(" a ");
        query.append(" inner join ").append(DaoUtils.wrap("group_group")).append(" gg on a.").append(DaoUtils.wrap("group_id"))
                .append(" = gg.").append(DaoUtils.wrap("parent_group_id"));
        query.append(" inner join ").append(DaoUtils.wrap("group_member")).append(" gm on gg.").append(DaoUtils.wrap("child_group_id"))
                .append(" = gm.").append(DaoUtils.wrap("usergroup"));
        query.append(" where gm.").append(DaoUtils.wrap("person_id")).append(" = :user_id and a.").append(DaoUtils.wrap("object_id"))
                .append(" = :object_id and a.")
                .append(DaoUtils.wrap("operation")).append(" in (:operations)");

        return query.toString();
    }

    private String getAclTableName(RdbmsId id) {
        String domainObjectTable = domainObjetcTypeIdCache.getName(id.getTypeId());
        return getAclTableNameFor(domainObjectTable);
    }

    private String getAclTableNameFor(String domainObjectTable) {
        return getSqlName(domainObjectTable + PostgreSqlQueryHelper.ACL_TABLE_SUFFIX);
    }

    public static String makeAccessTypeCode(AccessType type) {
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
        if (ExecuteActionAccessType.class.equals(type.getClass())) {
            ExecuteActionAccessType executeActionType = (ExecuteActionAccessType) type;
            return new StringBuilder("E_").append(executeActionType.getActionName()).toString();
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
        StringBuilder query = new StringBuilder();
        
        query.append("select count(*) from ").append(DaoUtils.wrap("user_group")).append(" ug ");
        query.append("inner join ").append(DaoUtils.wrap("group_group")).append(" gg on ug.").append(DaoUtils.wrap("id"))
                .append(" = gg.").append(DaoUtils.wrap("parent_group_id"));
        query.append("inner join ").append(DaoUtils.wrap("group_member")).append(" gm on gg.").append(DaoUtils.wrap("child_group_id"))
                .append(" = gm.").append(DaoUtils.wrap("usergroup"));
        query.append("where gm.").append(DaoUtils.wrap("person_id")).append(" = :user_id and ug.").append(DaoUtils.wrap("group_name"))
                .append(" = :group_name");
        return query.toString();
    }

}
