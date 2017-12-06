package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import ru.intertrust.cm.core.business.api.dto.Case;
import ru.intertrust.cm.core.business.api.dto.DomainObjectVersion;
import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.dao.api.AuditLogServiceDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.impl.utils.DaoUtils;
import ru.intertrust.cm.core.dao.impl.utils.DefaultFields;
import ru.intertrust.cm.core.dao.impl.utils.MultipleVersionRowMapper;
import ru.intertrust.cm.core.dao.impl.utils.SingleVersionRowMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.intertrust.cm.core.dao.api.DomainObjectDao.*;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.*;

public class AuditLogServiceDaoImpl implements AuditLogServiceDao {

    @Autowired
    @Qualifier("masterNamedParameterJdbcTemplate")
    private NamedParameterJdbcOperations masterJdbcTemplate; // Use for data modifying operations

    @Autowired
    @Qualifier("switchableNamedParameterJdbcTemplate")
    private NamedParameterJdbcOperations switchableJdbcTemplate; // User for read operations

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private DomainObjectTypeIdCache domainObjectTypeIdCache;

    /**
     * Получение всех версий доменного объекта
     */
    @Override
    public List<DomainObjectVersion> findAllVersions(Id domainObjectId) {
        if (domainObjectId == null) {
            throw new IllegalArgumentException("Object domainObjectId can not be null");
        }

        // TODO Запретить получение версии для всех кроме администратора, или
        // продумать права на версию

        RdbmsId rdbmsId = (RdbmsId) domainObjectId;
        String typeName = domainObjectTypeIdCache.getName(domainObjectId);

        String query = generateAllVersionsFindQuery(typeName);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("id", rdbmsId.getId());

        return switchableJdbcTemplate.query(query, parameters, new MultipleVersionRowMapper(
                typeName, DefaultFields.DEFAULT_ID_FIELD, configurationExplorer, domainObjectTypeIdCache));
    }

    @Override
    public DomainObjectVersion findVersion(Id versionId) {
        if (versionId == null) {
            throw new IllegalArgumentException("Object domainObjectId can not be null");
        }

        // TODO Запретить получение версии для всех кроме администратора, или
        // продумать права на версию

        RdbmsId rdbmsId = (RdbmsId) versionId;
        String typeName = domainObjectTypeIdCache.getName(versionId);

        String query = generateVersionFindQuery(typeName);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("id", rdbmsId.getId());

        return switchableJdbcTemplate.query(query, parameters, new SingleVersionRowMapper(
                typeName, DefaultFields.DEFAULT_ID_FIELD, configurationExplorer, domainObjectTypeIdCache));
    }

    public String generateDeleteLogQuery(String typeName, String rootType) {

        StringBuilder query = new StringBuilder();
        query.append("delete ");
        query.append(" from ");
        query.append(DaoUtils.wrap(getALTableSqlName(typeName)));
        query.append("where ").append(DaoUtils.wrap(ID_COLUMN)).append(" in (select ").append(DaoUtils.wrap(ID_COLUMN)).append(" from ");
        query.append(DaoUtils.wrap(getALTableSqlName(rootType))).append(" ");
        query.append("where ").append(DaoUtils.wrap("domain_object_id")).append(" = :id)");

        return query.toString();
    }

    @Override
    public void clean(Id domainObjectId) {

        if (domainObjectId == null) {
            throw new IllegalArgumentException("Object domainObjectId can not be null");
        }
        // TODO удаление версии для всех кроме администратора, или
        // продумать права на версию

        String typeName = domainObjectTypeIdCache.getName(domainObjectId);
        deleteLog(domainObjectId, typeName);
    }

    private void deleteLog(Id domainObjectId, String typeName) {
        if (typeName != null) {
            DomainObjectTypeConfig config = configurationExplorer.getConfig(
                    DomainObjectTypeConfig.class, typeName);
            String query = generateDeleteLogQuery(typeName, getRootTypeName(config));
            RdbmsId rdbmsId = (RdbmsId) domainObjectId;
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("id", rdbmsId.getId());

            masterJdbcTemplate.update(query, parameters);

            deleteLog(domainObjectId, config.getExtendsAttribute());
        }

    }

    /**
     * Инициализирует параметр c id доменного объекта
     *
     * @param id
     *            идентификатор доменного объекта
     * @return карту объектов содержащую имя параметра и его значение
     */
    protected Map<String, Object> initializeIdParameter(Id id) {
        RdbmsId rdbmsId = (RdbmsId) id;
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("id", rdbmsId.getId());
        return parameters;
    }

    protected String generateVersionFindQuery(String typeName) {
        DomainObjectTypeConfig config = configurationExplorer.getConfig(
                DomainObjectTypeConfig.class, typeName);

        String rootAlias = getRootTypeName(config);

        StringBuilder query = new StringBuilder();
        query.append(generateFindQuery(typeName));
        query.append(" where ").append(rootAlias).append(".id=:id ");

        return query.toString();
    }

    protected String generateAllVersionsFindQuery(String typeName) {
        DomainObjectTypeConfig config = configurationExplorer.getConfig(
                DomainObjectTypeConfig.class, typeName);

        String rootAlias = Case.toLower(getRootTypeName(config));

        StringBuilder query = new StringBuilder();
        query.append(generateFindQuery(typeName));
        query.append(" where ").append(DaoUtils.wrap(rootAlias)).append(".").append(DaoUtils.wrap("domain_object_id")).append("=:id ");

        return query.toString();
    }

    private String generateFindQuery(String typeName) {
        DomainObjectTypeConfig config = configurationExplorer.getConfig(
                DomainObjectTypeConfig.class, typeName);

        String rootAlias = Case.toLower(getRootTypeName(config));

        StringBuilder query = new StringBuilder();
        query.append("select ");
        query.append(DaoUtils.wrap(rootAlias)).append(".").append(DaoUtils.wrap(ID_COLUMN)).append(", ");
        query.append(DaoUtils.wrap(rootAlias)).append(".").append(DaoUtils.wrap(TYPE_COLUMN)).append(", ");
        query.append(DaoUtils.wrap(rootAlias)).append(".").append(DaoUtils.wrap(OPERATION_COLUMN)).append(", ");
        query.append(DaoUtils.wrap(rootAlias)).append(".").append(DaoUtils.wrap(UPDATED_DATE_COLUMN)).append(", ");
        query.append(DaoUtils.wrap(rootAlias)).append(".").append(DaoUtils.wrap(UPDATED_BY)).append(", ");
        query.append(DaoUtils.wrap(rootAlias)).append(".").append(DaoUtils.wrap(UPDATED_BY_TYPE_COLUMN)).append(", ");        
        query.append(DaoUtils.wrap(rootAlias)).append(".").append(DaoUtils.wrap(DOMAIN_OBJECT_ID_COLUMN)).append(", ");
        query.append(DaoUtils.wrap(rootAlias)).append(".").append(DaoUtils.wrap(COMPONENT_COLUMN)).append(", ");
        query.append(DaoUtils.wrap(rootAlias)).append(".").append(DaoUtils.wrap(IP_ADDRESS_COLUMN)).append(", ");
        query.append(DaoUtils.wrap(rootAlias)).append(".").append(DaoUtils.wrap(INFO_COLUMN)).append(" ");
        appendColumnsQueryPart(query, typeName);
        query.append(" from ");
        appendVersionTableNameQueryPart(query, typeName);

        return query.toString();
    }

    private String getRootTypeName(DomainObjectTypeConfig config) {
        String result = config.getName();
        if (config.getExtendsAttribute() != null) {
            DomainObjectTypeConfig parentConfig = configurationExplorer.getConfig(
                    DomainObjectTypeConfig.class, config.getExtendsAttribute());
            result = getRootTypeName(parentConfig);
        }
        return result;
    }

    private void appendColumnsQueryPart(StringBuilder query, String typeName) {
        DomainObjectTypeConfig config = configurationExplorer.getConfig(
                DomainObjectTypeConfig.class, typeName);

        String tableAlias = getSqlAlias(config.getName());

        for (FieldConfig fieldConfig : config.getFieldConfigs()) {
            if ("ID".equals(fieldConfig.getName())) {
                continue;
            }

            query.append(", ").append(tableAlias).append(".").append(DaoUtils.wrap(getSqlName(fieldConfig)));
            if (fieldConfig.getFieldType().equals(FieldType.REFERENCE)) {
                query.append(", ").append(tableAlias).append(".").append(DaoUtils.wrap(getSqlName(fieldConfig) + "_type"));
            }
        }

        if (config.getExtendsAttribute() != null) {
            appendColumnsQueryPart(query, config.getExtendsAttribute());
        }
    }

    private void appendVersionTableNameQueryPart(StringBuilder query, String typeName) {
        String aliasName = getSqlName(typeName);
        String tableName = getALTableSqlName(typeName);
        query.append(DaoUtils.wrap(tableName)).append(" ").append(aliasName);
        appendVersionParentTable(query, typeName);
    }

    private void appendVersionParentTable(StringBuilder query, String typeName) {
        DomainObjectTypeConfig config = configurationExplorer.getConfig(
                DomainObjectTypeConfig.class, typeName);

        if (config.getExtendsAttribute() == null) {
            return;
        }

        String tableAlias = getSqlAlias(typeName);

        String parentTableName = getALTableSqlName(config.getExtendsAttribute());
        String parentTableAlias = getSqlAlias(config.getExtendsAttribute());

        query.append(" inner join ").append(DaoUtils.wrap(parentTableName)).append(" ").append(parentTableAlias);
        query.append(" on ").append(tableAlias).append(".").append(DaoUtils.wrap(ID_COLUMN)).append("=");
        query.append(parentTableAlias).append(".").append(DaoUtils.wrap(ID_COLUMN));

        appendParentTable(query, config.getExtendsAttribute());
    }

    private void appendParentTable(StringBuilder query, String typeName) {
        DomainObjectTypeConfig config = configurationExplorer.getConfig(
                DomainObjectTypeConfig.class, typeName);

        if (config.getExtendsAttribute() == null) {
            return;
        }

        String tableAlias = getSqlAlias(typeName);

        String parentTableName = getSqlName(config.getExtendsAttribute());
        String parentTableAlias = getSqlAlias(config.getExtendsAttribute());

        query.append(" inner join ").append(DaoUtils.wrap(parentTableName)).append(" ")
                .append(parentTableAlias);
        query.append(" on ").append(tableAlias).append(".").append(DaoUtils.wrap(ID_COLUMN)).append("=");
        query.append(parentTableAlias).append(".").append(DaoUtils.wrap(ID_COLUMN));

        appendParentTable(query, config.getExtendsAttribute());
    }

    /**
     * Получение крайней версии доменного объекта
     */
    @Override
    public DomainObjectVersion findLastVersion(Id domainObjectId) {
        if (domainObjectId == null) {
            throw new IllegalArgumentException("Object domainObjectId can not be null");
        }

        // TODO Запретить получение версии для всех кроме администратора, или
        // продумать права на версию

        RdbmsId rdbmsId = (RdbmsId) domainObjectId;
        String typeName = domainObjectTypeIdCache.getName(domainObjectId);

        String query = generateFindLastVersionQuery(typeName);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("id", rdbmsId.getId());

        Long versionId = switchableJdbcTemplate.queryForObject(query, parameters, Long.class);
        return findVersion(new RdbmsId(rdbmsId.getTypeId(), versionId));
    }

    /**
     * Вормирование запроса на получение идентификатора крайней версии доменного объекта
     * @param typeName
     * @return
     */
    private String generateFindLastVersionQuery(String typeName) {
        DomainObjectTypeConfig config = configurationExplorer.getConfig(
                DomainObjectTypeConfig.class, typeName);

        String rootType = getRootTypeName(config);

        StringBuilder query = new StringBuilder();
        query.append("select max(").append(DaoUtils.wrap(ID_COLUMN)).append(") ");
        query.append("from ");
        query.append(DaoUtils.wrap(getALTableSqlName(rootType))).append(" ");
        query.append("where ").append(DaoUtils.wrap("domain_object_id")).append(" = :id");

        return query.toString();
    }
}
