package ru.intertrust.cm.core.dao.impl.utils;

import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.*;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.impl.DataType;
import ru.intertrust.cm.core.model.FatalException;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getSqlName;

/**
 * Базовй класс для отображения {@link java.sql.ResultSet} на доменные объекты и коллекции.
 *
 * @author atsvetkov
 */
public class BasicRowMapper {

    private static final String MASTER_FIELD = "master";

    protected final String domainObjectType;
    protected final String idField;

    protected ConfigurationExplorer configurationExplorer;
    protected DomainObjectTypeIdCache domainObjectTypeIdCache;

    public BasicRowMapper(String domainObjectType, String idField, ConfigurationExplorer configurationExplorer,
                          DomainObjectTypeIdCache domainObjectTypeIdCache) {
        this.domainObjectType = domainObjectType;
        this.idField = idField;
        this.configurationExplorer = configurationExplorer;
        this.domainObjectTypeIdCache = domainObjectTypeIdCache;
    }

    /**
     * Отображает типы полей в базе на {@link ru.intertrust.cm.core.dao.impl.DataType}
     *
     * @param columnTypeName
     * @return
     */
    protected DataType getColumnType(String columnTypeName) {
        DataType result = null;
        if (columnTypeName.equals("int8")) {
            result = DataType.INTEGER;
        } else if (columnTypeName.equals("timestamp")) {
            result = DataType.DATETIME;
        } else if (columnTypeName.equals("varchar") || columnTypeName.equals("unknown")
                || columnTypeName.equals("text")) {
            result = DataType.STRING;
        } else if (columnTypeName.equals("bool")) {
            result = DataType.BOOLEAN;
        } else if (columnTypeName.equals("numeric")) {
            result = DataType.DECIMAL;
        }
        return result;
    }

    /**
     * Отображает типы колонок в конфигурации коллекции на {@link ru.intertrust.cm.core.dao.impl.DataType}.
     *
     * @param columnType типы колонок в конфигурации
     * @return объект {@link ru.intertrust.cm.core.dao.impl.DataType}
     */
    protected DataType getColumnDataType(String columnType) {
        DataType result = null;
        if (columnType.equals("integer")) {
            result = DataType.INTEGER;
        } else if (columnType.equals("datetime")) {
            result = DataType.DATETIME;
        } else if (columnType.equals("string")) {
            result = DataType.STRING;
        } else if (columnType.equals("boolean")) {
            result = DataType.BOOLEAN;
        } else if (columnType.equals("decimal")) {
            result = DataType.DECIMAL;
        }
        return result;
    }

    /**
     * Заполняет модель {@see FieldValueModel} из объекта {@see ResultSet}.
     *
     * @param rs         {@see ResultSet}
     * @param valueModel модель {@see FieldValueModel}
     * @param columnName имя колонки, которая извлекается из {@see ResultSet}
     * @throws SQLException
     */
    protected void fillValueModel(ResultSet rs, FieldValueModel valueModel, String columnName) throws SQLException {

        FieldConfig fieldConfig = configurationExplorer.getFieldConfig(domainObjectType, columnName);

        Value value = null;
        Id id = null;
        Id parentId = null;

        DomainObjectTypeConfig objectTypeConfig =
                configurationExplorer.getConfig(DomainObjectTypeConfig.class, domainObjectType);
        String parentDomainObjectType = null;
        if (objectTypeConfig != null && objectTypeConfig.getParentConfig() != null) {
            parentDomainObjectType = objectTypeConfig.getParentConfig().getName();
        }

        if (idField.equalsIgnoreCase(columnName)) {
            Long longValue = rs.getLong(columnName);
            if (!rs.wasNull()) {
                id = new RdbmsId(domainObjectTypeIdCache.getId(domainObjectType), longValue);
            } else {
                throw new FatalException("Id field can not be null for object " + domainObjectType);
            }
        } else if (MASTER_FIELD.equalsIgnoreCase(columnName)) {
            if (parentDomainObjectType == null) {
                throw new FatalException("Parent is not configured for domain object but exists in DB: "
                        + domainObjectType);
            }
            Long longValue = rs.getLong(columnName);
            if (!rs.wasNull()) {
                parentId = new RdbmsId(domainObjectTypeIdCache.getId(parentDomainObjectType), longValue);
            } else {
                parentId = null;
            }

        } else if (fieldConfig != null && StringFieldConfig.class.equals(fieldConfig.getClass())) {
            String fieldValue = rs.getString(columnName);
            if (!rs.wasNull()) {
                value = new StringValue(fieldValue);
            } else {
                value = new StringValue();
            }
        } else if (fieldConfig != null && LongFieldConfig.class.equals(fieldConfig.getClass())) {
            Long longValue = rs.getLong(columnName);
            if (!rs.wasNull()) {
                value = new LongValue(longValue);
            } else {
                value = new LongValue();
            }
        } else if (fieldConfig != null && DecimalFieldConfig.class.equals(fieldConfig.getClass())) {
            BigDecimal fieldValue = rs.getBigDecimal(columnName);
            if (!rs.wasNull()) {
                value = new DecimalValue(fieldValue);
            } else {
                value = new DecimalValue();
            }
        } else if (fieldConfig != null && ReferenceFieldConfig.class.equals(fieldConfig.getClass())) {
            if (!rs.wasNull()) {
                String referenceType = findTypeByColumnName((ReferenceFieldConfig) fieldConfig, columnName);
                Long longValue = rs.getLong(columnName);

                value = new ReferenceValue(new RdbmsId(domainObjectTypeIdCache.getId(referenceType), longValue));
            } else {
                value = new ReferenceValue();
            }
        } else if (fieldConfig != null && DateTimeFieldConfig.class.equals(fieldConfig.getClass())) {
            Timestamp timestamp = rs.getTimestamp(columnName);
            if (!rs.wasNull()) {
                Date date = new Date(timestamp.getTime());
                value = new TimestampValue(date);
            } else {
                value = new TimestampValue();
            }
        }

        if (id != null) {
            valueModel.setId(id);
        }
        if (parentId != null) {
            valueModel.setParentId(parentId);
        }

        valueModel.setValue(value);
    }

    /**
     * Заполняет поля доменного объекта (id, parent или атрибут) из модели {@see FieldValueModel}.
     *
     * @param object     исходный доменного объекта
     * @param valueModel модель {@see FieldValueModel}
     * @param columnName имя поля, нужно если заполняется обычное поле
     */
    protected void fillObjectValue(GenericDomainObject object, FieldValueModel valueModel, String columnName) {
        if (valueModel.getId() != null) {
            object.setId(valueModel.getId());
        }
        if (valueModel.getParentId() != null) {
            object.setParent(valueModel.getParentId());
        }
        if (valueModel.getValue() != null) {
            object.setValue(columnName, valueModel.getValue());
        }
    }

    private String findTypeByColumnName(ReferenceFieldConfig fieldConfig, String columnName) {
        for (ReferenceFieldTypeConfig typeConfig : fieldConfig.getTypes()) {
            if (columnName.equalsIgnoreCase(getSqlName(fieldConfig, typeConfig))) {
                return typeConfig.getName();
            }
        }

        throw new FatalException("Domain Object Type cannot be found for column '" + columnName + "'");
    }

    //protected void fillValueModelWithSystemFields(SystemField systemFields,)

    /**
     * Обертывает заполненное поле (атрибут), поле parent или поле id в доменном объекте.
     *
     * @author atsvetkov
     */
    protected class FieldValueModel {
        private Id id = null;
        private Id parentId = null;
        private Value value = null;

        public Id getId() {
            return id;
        }

        public void setId(Id id) {
            this.id = id;
        }

        public Value getValue() {
            return value;
        }

        public void setValue(Value value) {
            this.value = value;
        }

        public Id getParentId() {
            return parentId;
        }

        public void setParentId(Id parentId) {
            this.parentId = parentId;
        }
    }

    /**
     * Модель для хранкения названия колонолк и названия колонки-первичного ключа для доменного объекта.
     *
     * @author atsvetkov
     */
    protected class ColumnModel {

        private String idField;
        private List<String> columnNames;

        public List<String> getColumnNames() {
            if (columnNames == null) {
                columnNames = new ArrayList<String>();
            }
            return columnNames;
        }

        public String getIdField() {
            return idField;
        }

        public void setIdField(String idField) {
            this.idField = idField;
        }
    }
}
