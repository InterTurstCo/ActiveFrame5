package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.ForeignKeyInfo;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.business.api.dto.ColumnInfo;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.dao.api.DataStructureDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.SchemaCache;

import java.util.Map;

import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getSqlName;

/**
 * Реализация {@link ru.intertrust.cm.core.dao.api.SchemaCache}
 * Created by vmatsukevich on 27.1.15.
 */
public class SchemaCacheImpl implements SchemaCache {

    @Autowired
    private DataStructureDao dataStructureDao;

    private Map<String, Map<String, ColumnInfo>> schemaTables;
    private Map<String, Map<String, ForeignKeyInfo>> foreignKeys;

    /**
     * {@link ru.intertrust.cm.core.dao.api.SchemaCache#reset()}
     */
    @Override
    public synchronized void reset() {
        schemaTables = dataStructureDao.getSchemaTables();
        foreignKeys = dataStructureDao.getForeignKeys();
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
     * {@link ru.intertrust.cm.core.dao.api.SchemaCache#isReferenceFieldForeignKeyExist(ru.intertrust.cm.core.config.DomainObjectTypeConfig, ru.intertrust.cm.core.config.ReferenceFieldConfig)}
     */
    @Override
    public boolean isReferenceFieldForeignKeyExist(DomainObjectTypeConfig config, ReferenceFieldConfig fieldConfig) {
        Map<String, ForeignKeyInfo> tableForeignKeys = foreignKeys.get(getSqlName(config));

        if (tableForeignKeys == null) {
            return false;
        }

        String foreignTableName = getSqlName(fieldConfig.getType());
        for (ForeignKeyInfo foreignKey : tableForeignKeys.values()) {
            if (foreignTableName.equals(foreignKey.getReferencedTableName())) {
                for (Map.Entry<String, String> columnEntry : foreignKey.getColumnNames().entrySet()) {
                    if (columnEntry.getKey().equals(getSqlName(fieldConfig.getName())) &&
                            columnEntry.getValue().equals(DomainObjectDao.ID_COLUMN)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
