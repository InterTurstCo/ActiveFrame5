package ru.intertrust.cm.core.dao.impl;

import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getSqlAlias;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getSqlName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import ru.intertrust.cm.core.business.api.dto.DomainObjectVersion;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.model.FieldConfig;
import ru.intertrust.cm.core.dao.api.AuditLogServiceDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.impl.utils.DefaultFields;
import ru.intertrust.cm.core.dao.impl.utils.MultipleVersionRowMapper;
import ru.intertrust.cm.core.dao.impl.utils.SingleVersionRowMapper;

public class AuditLogServiceDaoImpl implements AuditLogServiceDao {

    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private DomainObjectTypeIdCache domainObjectTypeIdCache;    
    
    /**
     * Устанавливает источник соединений
     * 
     * @param dataSource
     */
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

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
        
        String query = generateAllVersionsFindQuery(typeName);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("id", rdbmsId.getId());
        
        return jdbcTemplate.query(query, parameters, new SingleVersionRowMapper(
                typeName, DefaultFields.DEFAULT_ID_FIELD, configurationExplorer, domainObjectTypeIdCache));
    }

    @Override
    public void clean(Id domainObjectId) {
        // TODO Auto-generated method stub

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
        String tableAlias = getSqlAlias(typeName);

        StringBuilder query = new StringBuilder();
        query.append("select ");
        appendColumnsQueryPart(query, typeName);
        query.append(" from ");
        appendTableNameQueryPart(query, typeName);
        query.append(" where ").append(tableAlias).append(".id=:id ");

        return query.toString();
    }

    protected String generateAllVersionsFindQuery(String typeName) {
        String tableAlias = getSqlAlias(typeName);

        StringBuilder query = new StringBuilder();
        query.append("select ");
        query.append("id, type_id, operation, updated_date, domain_object_id, component, ip_address, info ");
        appendColumnsQueryPart(query, typeName);
        query.append(" from ");
        appendTableNameQueryPart(query, typeName);
        query.append(" where ").append(tableAlias).append(".domain_object_id=:id ");

        return query.toString();
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
        }

        if (config.getExtendsAttribute() != null) {
            appendColumnsQueryPart(query, config.getExtendsAttribute());
        }
    }

    private void appendTableNameQueryPart(StringBuilder query, String typeName) {
        String tableName = getSqlName(typeName);
        query.append(tableName).append(" ").append(getSqlAlias(tableName));
        appendParentTable(query, typeName);
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
}
