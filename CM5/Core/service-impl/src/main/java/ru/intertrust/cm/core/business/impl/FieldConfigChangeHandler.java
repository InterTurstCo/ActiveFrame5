package ru.intertrust.cm.core.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.ColumnInfo;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.dao.api.SchemaCache;

/**
 * Обработчик изменения конфигурации поля типа доменного объекта
 * Created by vmatsukevich on 23.1.15.
 */
public class FieldConfigChangeHandler {

    @Autowired
    private SchemaCache schemaCache;

    /**
     * Обрабатывает изменение конфигурации поля типа доменного объекта
     * @param newFieldConfig новая конфигурация поля
     * @param oldFieldConfig старая конфигурация поля
     * @param domainObjectTypeConfig конфигурация типа доменного объекта
     */
    public void handle(FieldConfig newFieldConfig, FieldConfig oldFieldConfig, DomainObjectTypeConfig domainObjectTypeConfig) {
        handleBasicAttributes(newFieldConfig, oldFieldConfig, domainObjectTypeConfig);

        if (newFieldConfig instanceof StringFieldConfig && oldFieldConfig instanceof StringFieldConfig) {
            handle((StringFieldConfig) newFieldConfig, (StringFieldConfig) oldFieldConfig, domainObjectTypeConfig);
        } else if (newFieldConfig instanceof PasswordFieldConfig && oldFieldConfig instanceof PasswordFieldConfig) {
            handle((PasswordFieldConfig) newFieldConfig, (PasswordFieldConfig) oldFieldConfig, domainObjectTypeConfig);
        } else if (newFieldConfig instanceof ReferenceFieldConfig && oldFieldConfig instanceof ReferenceFieldConfig) {
            handle((ReferenceFieldConfig) newFieldConfig, (ReferenceFieldConfig) oldFieldConfig, domainObjectTypeConfig);
        } else if (newFieldConfig instanceof DecimalFieldConfig && oldFieldConfig instanceof DecimalFieldConfig) {
            handle((DecimalFieldConfig) newFieldConfig, (DecimalFieldConfig) oldFieldConfig, domainObjectTypeConfig);
        } else if (newFieldConfig instanceof DecimalFieldConfig && oldFieldConfig instanceof DecimalFieldConfig) {
            handle((DecimalFieldConfig) newFieldConfig, (DecimalFieldConfig) oldFieldConfig, domainObjectTypeConfig);
        }
    }

    private void handle(StringFieldConfig newFieldConfig, StringFieldConfig oldFieldConfig, DomainObjectTypeConfig domainObjectTypeConfig) {
        if (newFieldConfig.getLength() != oldFieldConfig.getLength()) {
            int length = schemaCache.getColumnLength(domainObjectTypeConfig, newFieldConfig);
            if (length != newFieldConfig.getLength()) {
                throw new ConfigurationException("Configuration loading aborted: unsupported length attribute " +
                        "modification of " + domainObjectTypeConfig.getName() + "." + newFieldConfig.getName());
            }
        }
    }

    private void handle(PasswordFieldConfig newFieldConfig, PasswordFieldConfig oldFieldConfig, DomainObjectTypeConfig domainObjectTypeConfig) {
        if (newFieldConfig.getLength() != oldFieldConfig.getLength()) {
            int length = schemaCache.getColumnLength(domainObjectTypeConfig, newFieldConfig);
            if (length != newFieldConfig.getLength()) {
                throw new ConfigurationException("Configuration loading aborted: unsupported length attribute " +
                        "modification of " + domainObjectTypeConfig.getName() + "." + newFieldConfig.getName());
            }
        }
    }

    private void handle(ReferenceFieldConfig newFieldConfig, ReferenceFieldConfig oldFieldConfig, DomainObjectTypeConfig domainObjectTypeConfig) {
        if (!newFieldConfig.getType().equals(oldFieldConfig.getType())) {
            if (!newFieldConfig.getType().equals(ConfigurationExplorer.REFERENCE_TYPE_ANY) &&
                    !schemaCache.isReferenceFieldForeignKeyExist(domainObjectTypeConfig, newFieldConfig)) {
                throw new ConfigurationException("Configuration loading aborted: unsupported type attribute " +
                        "modification of " + domainObjectTypeConfig.getName() + "." + newFieldConfig.getName());
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
        if (newFieldConfig.isNotNull() != oldFieldConfig.isNotNull()) {
            boolean isNotNull = schemaCache.isColumnNotNull(domainObjectTypeConfig, newFieldConfig);
            if (isNotNull != newFieldConfig.isNotNull()) {
                throw new ConfigurationException("Configuration loading aborted: unsupported not-null attribute " +
                        "modification of " + domainObjectTypeConfig.getName() + "." + newFieldConfig.getName());
            }
        }
    }

}
