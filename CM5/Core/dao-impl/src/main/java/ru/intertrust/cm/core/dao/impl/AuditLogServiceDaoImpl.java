package ru.intertrust.cm.core.dao.impl;

import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getSqlAlias;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getSqlName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import ru.intertrust.cm.core.business.api.dto.DomainObjectVersion;
import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.dao.api.AuditLogServiceDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.impl.utils.DefaultFields;
import ru.intertrust.cm.core.dao.impl.utils.MultipleVersionRowMapper;
import ru.intertrust.cm.core.dao.impl.utils.SingleVersionRowMapper;

public class AuditLogServiceDaoImpl implements AuditLogServiceDao {

    @Autowired
    private NamedParameterJdbcOperations jdbcTemplate;

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

        return jdbcTemplate.query(query, parameters, new MultipleVersionRowMapper(
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

        return jdbcTemplate.query(query, parameters, new SingleVersionRowMapper(
                typeName, DefaultFields.DEFAULT_ID_FIELD, configurationExplorer, domainObjectTypeIdCache));
    }

    public String generateDeleteLogQuery(String typeName, String rootType) {

        StringBuilder query = new StringBuilder();
        query.append("delete ");
        query.append(" from ");
        query.append(getSqlName(typeName));
        query.append("_LOG ");
        query.append("where id in (select id from ");
        query.append(getSqlName(rootType));
        query.append("_LOG ");
        query.append("where domain_object_id = :id)");

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

            jdbcTemplate.update(query, parameters);

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

        String rootAlias = getRootTypeName(config);

        StringBuilder query = new StringBuilder();
        query.append(generateFindQuery(typeName));
        query.append(" where ").append(rootAlias).append(".domain_object_id=:id ");

        return query.toString();
    }

    private String generateFindQuery(String typeName) {
        DomainObjectTypeConfig config = configurationExplorer.getConfig(
                DomainObjectTypeConfig.class, typeName);

        String rootAlias = getRootTypeName(config);

        StringBuilder query = new StringBuilder();
        query.append("select ");
        query.append(rootAlias).append(".id, ");
        query.append(rootAlias).append(".type_id, ");
        query.append(rootAlias).append(".operation, ");
        query.append(rootAlias).append(".updated_date, ");
        query.append(rootAlias).append(".domain_object_id, ");
        query.append(rootAlias).append(".component, ");
        query.append(rootAlias).append(".ip_address, ");
        query.append(rootAlias).append(".info ");
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

            query.append(", ").append(tableAlias).append(".").append(getSqlName(fieldConfig));
            if (fieldConfig.getFieldType().equals(FieldType.REFERENCE)) {
                query.append(", ").append(tableAlias).append(".").append(getSqlName(fieldConfig)).append("_type");
            }
        }

        if (config.getExtendsAttribute() != null) {
            appendColumnsQueryPart(query, config.getExtendsAttribute());
        }
    }

    private void appendVersionTableNameQueryPart(StringBuilder query, String typeName) {
        String aliasName = getSqlName(typeName);
        String tableName = getSqlName(typeName) + "_LOG";
        query.append(tableName).append(" ").append(aliasName);
        appendVersionParentTable(query, typeName);
    }

    private void appendVersionParentTable(StringBuilder query, String typeName) {
        DomainObjectTypeConfig config = configurationExplorer.getConfig(
                DomainObjectTypeConfig.class, typeName);

        if (config.getExtendsAttribute() == null) {
            return;
        }

        String tableAlias = getSqlAlias(typeName);

        String parentTableName = getSqlName(config.getExtendsAttribute()) + "_LOG";
        String parentTableAlias = getSqlAlias(config.getExtendsAttribute());

        query.append(" inner join ").append(parentTableName).append(" ")
                .append(parentTableAlias);
        query.append(" on ").append(tableAlias).append(".").append(DomainObjectDao.ID_COLUMN).append("=");
        query.append(parentTableAlias).append(".").append(DomainObjectDao.ID_COLUMN);

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

        query.append(" inner join ").append(parentTableName).append(" ")
                .append(parentTableAlias);
        query.append(" on ").append(tableAlias).append(".").append(DomainObjectDao.ID_COLUMN).append("=");
        query.append(parentTableAlias).append(".").append(DomainObjectDao.ID_COLUMN);

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

        Long versionId = jdbcTemplate.queryForObject(query, parameters, Long.class);
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
        query.append("select max(id) ");
        query.append("from ");
        query.append(getSqlName(rootType));
        query.append("_log ");
        query.append("where domain_object_id = :id");

        return query.toString();
    }
}
