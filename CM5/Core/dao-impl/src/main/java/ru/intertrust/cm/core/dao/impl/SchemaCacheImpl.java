package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.ColumnInfo;
import ru.intertrust.cm.core.business.api.dto.ForeignKeyInfo;
import ru.intertrust.cm.core.business.api.dto.IndexInfo;
import ru.intertrust.cm.core.business.api.dto.UniqueKeyInfo;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.dao.api.DataStructureDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.SchemaCache;

import java.util.*;

import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.*;

/**
 * Реализация {@link ru.intertrust.cm.core.dao.api.SchemaCache}
 * Created by vmatsukevich on 27.1.15.
 */
public class SchemaCacheImpl implements SchemaCache {

    @Autowired
    private DataStructureDao dataStructureDao;

    private Map<String, Map<String, ColumnInfo>> schemaTables;
    private Map<String, Map<String, ForeignKeyInfo>> foreignKeys;
    private Map<String, Map<String, UniqueKeyInfo>> uniqueKeys;
    private Map<String, Map<String, IndexInfo>> indexes;

    /**
     * {@link ru.intertrust.cm.core.dao.api.SchemaCache#reset()}
     */
    @Override
    public synchronized void reset() {
        schemaTables = dataStructureDao.getSchemaTables();
        foreignKeys = dataStructureDao.getForeignKeys();
        uniqueKeys = dataStructureDao.getUniqueKeys();
        indexes = dataStructureDao.getIndexes();
    }

    /**
     * {@link ru.intertrust.cm.core.dao.api.SchemaCache#isTableExist(String)}
     */
    @Override
    public boolean isTableExist(String tableName) {
        return schemaTables.get(tableName) != null;
    }

    @Override
    public boolean isTableExist(DomainObjectTypeConfig config) {
        return isTableExist(getSqlName(config));
    }

    /**
     * {@link ru.intertrust.cm.core.dao.api.SchemaCache#isColumnExist(ru.intertrust.cm.core.config.DomainObjectTypeConfig, ru.intertrust.cm.core.config.FieldConfig)}
     */
    @Override
    public boolean isColumnExist(DomainObjectTypeConfig config, FieldConfig fieldConfig) {
        return getColumnInfo(config, fieldConfig) != null;
    }

    /**
     * {@link ru.intertrust.cm.core.dao.api.SchemaCache#isReferenceColumnExist(ru.intertrust.cm.core.config.DomainObjectTypeConfig, ru.intertrust.cm.core.config.ReferenceFieldConfig)}
     */
    @Override
    public boolean isReferenceColumnExist(DomainObjectTypeConfig config, ReferenceFieldConfig fieldConfig) {
        return getReferenceColumnInfo(config, fieldConfig) != null;
    }

    /**
     * {@link ru.intertrust.cm.core.dao.api.SchemaCache#isColumnNotNull(ru.intertrust.cm.core.config.DomainObjectTypeConfig, ru.intertrust.cm.core.config.FieldConfig)}
     */
    @Override
    public boolean isColumnNotNull(DomainObjectTypeConfig config, FieldConfig fieldConfig) {
        ColumnInfo columnInfo = getColumnInfo(config, fieldConfig);
        return columnInfo.isNotNull();
    }

    /**
     * {@link ru.intertrust.cm.core.dao.api.SchemaCache#getColumnLength(ru.intertrust.cm.core.config.DomainObjectTypeConfig, ru.intertrust.cm.core.config.FieldConfig)}
     */
    @Override
    public int getColumnLength(DomainObjectTypeConfig config, FieldConfig fieldConfig) {
        ColumnInfo columnInfo = getColumnInfo(config, fieldConfig);
        return columnInfo.getLength();
    }

    /**
     * {@link ru.intertrust.cm.core.dao.api.SchemaCache#getColumnInfo(ru.intertrust.cm.core.config.DomainObjectTypeConfig, ru.intertrust.cm.core.config.FieldConfig)}
     */
    @Override
    public ColumnInfo getColumnInfo(DomainObjectTypeConfig config, FieldConfig fieldConfig) {
        Map<String, ColumnInfo> columns = schemaTables.get(getSqlName(config));
        if (columns == null) {
            return null;
        }

        return columns.get(getSqlName(fieldConfig));
    }

    /**
     * {@link ru.intertrust.cm.core.dao.api.SchemaCache#getReferenceColumnInfo(ru.intertrust.cm.core.config.DomainObjectTypeConfig, ru.intertrust.cm.core.config.ReferenceFieldConfig)}
     */
    @Override
    public ColumnInfo getReferenceColumnInfo(DomainObjectTypeConfig config, ReferenceFieldConfig fieldConfig) {
        Map<String, ColumnInfo> columns = schemaTables.get(getSqlName(config));
        if (columns == null) {
            return null;
        }

        return columns.get(getReferenceTypeColumnName(fieldConfig.getName()));
    }

    /**
     * {@link ru.intertrust.cm.core.dao.api.SchemaCache#getForeignKeyName(ru.intertrust.cm.core.config.DomainObjectTypeConfig, ru.intertrust.cm.core.config.ReferenceFieldConfig)}
     */
    @Override
    public String getForeignKeyName(DomainObjectTypeConfig config, ReferenceFieldConfig fieldConfig) {
        Map<String, ForeignKeyInfo> tableForeignKeys = foreignKeys.get(getSqlName(config));

        if (tableForeignKeys == null) {
            return null;
        }

        String foreignTableName = getSqlName(fieldConfig.getType());
        for (ForeignKeyInfo foreignKey : tableForeignKeys.values()) {
            if (foreignTableName.equals(foreignKey.getReferencedTableName())) {
                if (foreignKey.getColumnNames().size() != 2) {
                    continue;
                }

                boolean idReferenceFound = false;
                boolean idTypeReferenceFound = false;

                for (Map.Entry<String, String> columnEntry : foreignKey.getColumnNames().entrySet()) {
                    if (columnEntry.getKey().equals(getSqlName(fieldConfig.getName())) &&
                            columnEntry.getValue().equals(DomainObjectDao.ID_COLUMN)) {
                        idReferenceFound = true;
                    }

                    if (columnEntry.getKey().equals(getReferenceTypeColumnName(getSqlName(fieldConfig.getName()))) &&
                            columnEntry.getValue().equals(DomainObjectDao.TYPE_COLUMN)) {
                        idTypeReferenceFound = true;
                    }
                }

                if (idReferenceFound && idTypeReferenceFound) {
                    return foreignKey.getName();
                }
            }
        }

        return null;
    }

    public String getParentTypeForeignKeyName(DomainObjectTypeConfig config) {
        final String parentName = config.getExtendsAttribute();
        if (parentName == null || parentName.isEmpty()) {
            return null;
        }
        final Map<String, ForeignKeyInfo> fks = foreignKeys.get(getSqlName(config.getName()));
        for (Map.Entry<String, ForeignKeyInfo> entry : fks.entrySet()) {
            final ForeignKeyInfo fkInfo = entry.getValue();
            if (fkInfo.getReferencedTableName().equalsIgnoreCase(parentName)) {
                final Map<String, String> columnNames = fkInfo.getColumnNames();
                if (columnNames != null && columnNames.size() == 1
                        && columnNames.keySet().iterator().next().equalsIgnoreCase("id") && columnNames.values().iterator().next().equalsIgnoreCase("id")) {
                        return entry.getKey();
                }
            }
        }
        return null;
    }

    /**
     * {@link ru.intertrust.cm.core.dao.api.SchemaCache#getUniqueKeys(ru.intertrust.cm.core.config.DomainObjectTypeConfig)}
     */
    @Override
    public Collection<UniqueKeyInfo> getUniqueKeys(DomainObjectTypeConfig config) {
        Map<String, UniqueKeyInfo> domainObjectTypeKeys = uniqueKeys.get(getSqlName(config.getName()));
        if (domainObjectTypeKeys == null) {
            return null;
        }

        return domainObjectTypeKeys.values();
    }

    /**
     * {@link ru.intertrust.cm.core.dao.api.SchemaCache#getUniqueKeyName(ru.intertrust.cm.core.config.DomainObjectTypeConfig, ru.intertrust.cm.core.config.UniqueKeyConfig)}
     */
    @Override
    public String getUniqueKeyName(DomainObjectTypeConfig config, UniqueKeyConfig keyConfig) {
        Map<String, UniqueKeyInfo> domainObjectTypeKeys = uniqueKeys.get(getSqlName(config.getName()));
        if (domainObjectTypeKeys == null || keyConfig.getUniqueKeyFieldConfigs() == null) {
            return null;
        }

        List<String> uniqueKeyFields = getUniqueKeyFields(config, keyConfig);

        for (UniqueKeyInfo uniqueKeyInfo : domainObjectTypeKeys.values()) {
            if (uniqueKeyInfo.getColumnNames() == null || uniqueKeyInfo.getColumnNames().size() != uniqueKeyFields.size()) {
                continue;
            }

            boolean fieldsMatch = true;
            for (String fieldName : uniqueKeyFields) {
                if (!uniqueKeyInfo.getColumnNames().contains(fieldName)) {
                    fieldsMatch = false;
                    break;
                }
            }

            if (fieldsMatch) {
                return uniqueKeyInfo.getName();
            }
        }

        return null;
    }

    /**
     * {@link ru.intertrust.cm.core.dao.api.SchemaCache#getIndexName(ru.intertrust.cm.core.config.DomainObjectTypeConfig, ru.intertrust.cm.core.config.IndexConfig)}
     */
    @Override
    public String getIndexName(DomainObjectTypeConfig config, IndexConfig indexConfig) {
        Map<String, IndexInfo> domainObjectTypeIndexes = indexes.get(getSqlName(config.getName()));
        if (domainObjectTypeIndexes == null) {
            return null;
        }

        // todo: wrong algorithm ?
        // loop through all indexes and if we don't find our column in an expression index, it's an error
        for (IndexInfo indexInfo : domainObjectTypeIndexes.values()) {
            if (indexInfo.getColumnNames() == null || indexConfig.getIndexFieldConfigs() == null ||
                    indexInfo.getColumnNames().size() != getIndexFieldsNumber(indexConfig)) {
                continue;
            }

            boolean fieldsMatch = true;
            for (BaseIndexExpressionConfig baseIndexExpressionConfig : indexConfig.getIndexFieldConfigs()) {
                if (!(baseIndexExpressionConfig instanceof IndexFieldConfig)) {
                    continue;
                }

                IndexFieldConfig fieldConfig = (IndexFieldConfig) baseIndexExpressionConfig;
                // todo index info column names are empty
                if (!indexInfo.getColumnNames().contains(getSqlName(fieldConfig.getName()))) {
                    fieldsMatch = false;
                    break;
                }
            }

            if (fieldsMatch) {
                return indexInfo.getName();
            }
        }

        return null;
    }

    /**
     * {@link ru.intertrust.cm.core.dao.api.SchemaCache#getIndices(ru.intertrust.cm.core.config.DomainObjectTypeConfig)}
     */
    @Override
    public Collection<IndexInfo> getIndices(DomainObjectTypeConfig config) {
        Map<String, IndexInfo> domainObjectTypeIndexes = indexes.get(getSqlName(config.getName()));
        return domainObjectTypeIndexes == null ? new ArrayList<IndexInfo>() : domainObjectTypeIndexes.values();
    }

    /**
     * {@link ru.intertrust.cm.core.dao.api.SchemaCache#getIndices(ru.intertrust.cm.core.config.DomainObjectTypeConfig)}
     */
    @Override
    public Set<String> getIndexNames(DomainObjectTypeConfig config) {
        Map<String, IndexInfo> domainObjectTypeIndexes = indexes.get(getSqlName(config.getName()));
        return domainObjectTypeIndexes == null ? new HashSet<String>() : domainObjectTypeIndexes.keySet();
    }

    private int getIndexFieldsNumber(IndexConfig indexConfig) {
        int fieldsNumber = 0;

        for (BaseIndexExpressionConfig baseIndexExpressionConfig : indexConfig.getIndexFieldConfigs()) {
            if (!(baseIndexExpressionConfig instanceof IndexFieldConfig)) {
                continue;
            }

            fieldsNumber++;
        }

        return fieldsNumber;
    }
}
