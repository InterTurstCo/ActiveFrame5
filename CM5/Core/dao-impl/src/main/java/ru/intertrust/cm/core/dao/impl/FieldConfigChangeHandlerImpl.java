package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.ColumnInfo;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.dao.api.DataStructureDao;
import ru.intertrust.cm.core.dao.api.SchemaCache;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Обработчик изменения конфигурации поля типа доменного объекта
 * Created by vmatsukevich on 23.1.15.
 */
public class FieldConfigChangeHandlerImpl implements FieldConfigChangeHandler {

    @Autowired
    private SchemaCache schemaCache;

    @Autowired
    protected DataStructureDao dataStructureDao;

    /**
     * Обрабатывает изменение конфигурации поля типа доменного объекта
     * @param newFieldConfig новая конфигурация поля
     * @param oldFieldConfig старая конфигурация поля
     * @param domainObjectTypeConfig конфигурация типа доменного объекта
     */
    @Override
    public void handle(FieldConfig newFieldConfig, FieldConfig oldFieldConfig,
                       DomainObjectTypeConfig domainObjectTypeConfig, ConfigurationExplorer configurationExplorer) {
        handleBasicAttributes(newFieldConfig, oldFieldConfig, domainObjectTypeConfig);

        if (newFieldConfig instanceof StringFieldConfig && oldFieldConfig instanceof StringFieldConfig) {
            handle((StringFieldConfig) newFieldConfig, (StringFieldConfig) oldFieldConfig, domainObjectTypeConfig);
        } else if (newFieldConfig instanceof PasswordFieldConfig && oldFieldConfig instanceof PasswordFieldConfig) {
            handle((PasswordFieldConfig) newFieldConfig, (PasswordFieldConfig) oldFieldConfig, domainObjectTypeConfig);
        } else if (newFieldConfig instanceof ReferenceFieldConfig && oldFieldConfig instanceof ReferenceFieldConfig) {
            handle((ReferenceFieldConfig) newFieldConfig, (ReferenceFieldConfig) oldFieldConfig, domainObjectTypeConfig, configurationExplorer);
        } else if (newFieldConfig instanceof DecimalFieldConfig && oldFieldConfig instanceof DecimalFieldConfig) {
            handle((DecimalFieldConfig) newFieldConfig, (DecimalFieldConfig) oldFieldConfig, domainObjectTypeConfig);
        }
    }

    private void handle(StringFieldConfig newFieldConfig, StringFieldConfig oldFieldConfig, DomainObjectTypeConfig domainObjectTypeConfig) {
        if (newFieldConfig.getLength() != oldFieldConfig.getLength()) {
            int length = schemaCache.getColumnLength(domainObjectTypeConfig, newFieldConfig);
            if (length != newFieldConfig.getLength()) {
                if (length < newFieldConfig.getLength()) {
                    dataStructureDao.updateColumnType(domainObjectTypeConfig, newFieldConfig);
                } else {
                    throw new ConfigurationException("Configuration loading aborted: cannot decrease length of " +
                            domainObjectTypeConfig.getName() + "." + newFieldConfig.getName());
                }
            }
        }
    }

    private void handle(PasswordFieldConfig newFieldConfig, PasswordFieldConfig oldFieldConfig, DomainObjectTypeConfig domainObjectTypeConfig) {
        if (newFieldConfig.getLength() != oldFieldConfig.getLength()) {
            int length = schemaCache.getColumnLength(domainObjectTypeConfig, newFieldConfig);
            if (length < newFieldConfig.getLength()) {
                dataStructureDao.updateColumnType(domainObjectTypeConfig, newFieldConfig);
            } else {
                throw new ConfigurationException("Configuration loading aborted: cannot decrease length of " +
                        domainObjectTypeConfig.getName() + "." + newFieldConfig.getName());
            }
        }
    }

    private void handle(ReferenceFieldConfig newFieldConfig, ReferenceFieldConfig oldFieldConfig,
                        DomainObjectTypeConfig domainObjectTypeConfig, ConfigurationExplorer configurationExplorer) {
        if (!newFieldConfig.getType().equalsIgnoreCase(oldFieldConfig.getType())) {
            if (newFieldConfig.getType().equals(ConfigurationExplorer.REFERENCE_TYPE_ANY)) {
                if (oldFieldConfig.getType() != null) {
                    String foreignKeyName = schemaCache.getForeignKeyName(domainObjectTypeConfig, oldFieldConfig);
                    if (foreignKeyName != null) {
                        dataStructureDao.dropConstraint(domainObjectTypeConfig, foreignKeyName);
                    }
                }
            } else {
                String newForeignKeyName = schemaCache.getForeignKeyName(domainObjectTypeConfig, newFieldConfig);
                if (newForeignKeyName == null) {
                    if (oldFieldConfig.getType() == null) {
                        dataStructureDao.createForeignKeyAndUniqueConstraints(domainObjectTypeConfig,
                                Collections.singletonList(newFieldConfig), new ArrayList<UniqueKeyConfig>());
                    } else if (configurationExplorer.isAssignable(oldFieldConfig.getType(), newFieldConfig.getType())) {
                        String foreignKeyName = schemaCache.getForeignKeyName(domainObjectTypeConfig, oldFieldConfig);
                        if (foreignKeyName != null) {
                            dataStructureDao.dropConstraint(domainObjectTypeConfig, foreignKeyName);
                        }

                        dataStructureDao.createForeignKeyAndUniqueConstraints(domainObjectTypeConfig,
                                Collections.singletonList(newFieldConfig), new ArrayList<UniqueKeyConfig>());
                    } else {
                        throw new ConfigurationException("Configuration loading aborted: cannot change reference type " +
                                "from '" + oldFieldConfig.getType() + "' to '" + newFieldConfig.getType() +
                                "' in " + domainObjectTypeConfig.getName() + "." + newFieldConfig.getName());
                    }
                }
            }
        }
    }

    private void handle(DecimalFieldConfig newFieldConfig, DecimalFieldConfig oldFieldConfig, DomainObjectTypeConfig domainObjectTypeConfig) {
        ColumnInfo columnInfo = schemaCache.getColumnInfo(domainObjectTypeConfig, newFieldConfig);

        if (!newFieldConfig.getScale().equals(oldFieldConfig.getScale()) &&
                columnInfo.getScale() != newFieldConfig.getScale()) {
            throw new ConfigurationException("Configuration loading aborted: unsupported scale attribute " +
                    "modification of " + domainObjectTypeConfig.getName() + "." + newFieldConfig.getName());
        }

        if (!newFieldConfig.getPrecision().equals(oldFieldConfig.getPrecision()) &&
                columnInfo.getPrecision() != newFieldConfig.getPrecision()) {
            throw new ConfigurationException("Configuration loading aborted: unsupported precision attribute " +
                    "modification of " + domainObjectTypeConfig.getName() + "." + newFieldConfig.getName());
        }
    }

    private void handleBasicAttributes(FieldConfig newFieldConfig, FieldConfig oldFieldConfig, DomainObjectTypeConfig domainObjectTypeConfig) {
        ColumnInfo columnInfo = schemaCache.getColumnInfo(domainObjectTypeConfig, newFieldConfig);

        if (!newFieldConfig.getClass().equals(oldFieldConfig.getClass()) &&
                !dataStructureDao.getSqlType(newFieldConfig).startsWith(columnInfo.getDataType())) {
            throw new ConfigurationException("Configuration loading aborted: cannot change field type of " +
                    domainObjectTypeConfig.getName() + " from " +
                    oldFieldConfig.getClass().getName() + " to " + newFieldConfig.getClass().getName());
        }

        if (newFieldConfig.isNotNull() != oldFieldConfig.isNotNull()) {
            if (columnInfo.isNotNull() != newFieldConfig.isNotNull()) {
                if (!newFieldConfig.isNotNull()) {
                    dataStructureDao.setColumnNotNull(domainObjectTypeConfig, newFieldConfig, false);
                } else {
                    throw new ConfigurationException("Configuration loading aborted: cannot set not-null on " +
                            domainObjectTypeConfig.getName() + "." + newFieldConfig.getName());
                }
            }
        }
    }

}