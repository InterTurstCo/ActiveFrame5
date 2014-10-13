package ru.intertrust.cm.core.dao.impl.utils;

import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.impl.DataType;
import ru.intertrust.cm.core.dao.impl.DomainObjectCacheServiceImpl;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.util.SpringApplicationContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static ru.intertrust.cm.core.dao.api.DomainObjectDao.CREATED_DATE_COLUMN;
import static ru.intertrust.cm.core.dao.api.DomainObjectDao.UPDATED_DATE_COLUMN;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getReferenceTypeColumnName;

/**
 * Базовй класс для отображения {@link java.sql.ResultSet} на доменные объекты и
 * коллекции.
 *
 * @author atsvetkov
 */
public class BasicRowMapper extends ValueReader {

    protected static final String TYPE_ID_COLUMN = DomainObjectDao.TYPE_COLUMN.toLowerCase();

    protected final String domainObjectType;
    protected final String idField;

    protected ConfigurationExplorer configurationExplorer;

    protected DomainObjectTypeIdCache domainObjectTypeIdCache;

    private DomainObjectCacheServiceImpl domainObjectCacheService;

    public BasicRowMapper(String domainObjectType, String idField, ConfigurationExplorer configurationExplorer,
            DomainObjectTypeIdCache domainObjectTypeIdCache) {
        this.domainObjectType = domainObjectType;
        this.idField = idField;
        this.configurationExplorer = configurationExplorer;
        this.domainObjectTypeIdCache = domainObjectTypeIdCache;
    }

    protected DataType getColumnDataTypeByDbTypeName(String columnTypeName) {
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
        } else if (columnTypeName.equals("bigint")) {
            result = DataType.LONG;
        }
        return result;
    }

    protected FieldConfig getFieldConfigByDbTypeName(String columnName, String columnTypeName) {
        FieldConfig result;

        if (columnTypeName.startsWith("int")) {
            result =  new LongFieldConfig();
        } else if (columnTypeName.equals("timestamp")) {
            result = new DateTimeFieldConfig();
        } else if (columnTypeName.equals("varchar") || columnTypeName.equals("unknown")
                || columnTypeName.equals("text")) {
            result = new StringFieldConfig();
        } else if (columnTypeName.equals("bool")) {
            result = new BooleanFieldConfig();
        } else if (columnTypeName.equals("numeric")) {
            result = new DecimalFieldConfig();
        } else if (columnTypeName.equals("bigint")) {
            result = new LongFieldConfig();
        } else {
            result = new StringFieldConfig();
        }

        result.setName(columnName);
        return result;
    }

    /**
     * Заполняет модель {@see FieldValueModel} из объекта {@see ResultSet}.
     *
     * @param rs
     *            {@see ResultSet}
     * @param valueModel
     *            модель {@see FieldValueModel}
     * @param columnName
     *            имя колонки, которая извлекается из {@see ResultSet}
     * @throws SQLException
     */
    protected void fillValueModel(ResultSet rs, FieldValueModel valueModel, String columnName, FieldConfig fieldConfig) throws SQLException {
        if (idField.equalsIgnoreCase(columnName)) {
            Id id = readId(rs, columnName);
            if (id != null) {
                valueModel.setId(id);
            }
            return;
        }

        Value value = readValue(rs, columnName, fieldConfig);
        valueModel.setValue(value);

        if (CREATED_DATE_COLUMN.equalsIgnoreCase(columnName) && value != null && value.get() != null) {
            valueModel.setCreatedDate(((DateTimeValue) value).get());
        } else if (UPDATED_DATE_COLUMN.equalsIgnoreCase(columnName) && value != null && value.get() != null) {
            valueModel.setModifiedDate(((DateTimeValue) value).get());
        }
    }

    /**
     * Заполняет поля доменного объекта (id, parent или атрибут) из модели
     * {@see FieldValueModel}.
     *
     * @param object
     *            исходный доменного объекта
     * @param valueModel
     *            модель {@see FieldValueModel}
     * @param fieldConfig
     *            имя поля, нужно если заполняется обычное поле
     */
    protected void fillObjectValue(GenericDomainObject object, FieldValueModel valueModel, FieldConfig fieldConfig) {
        if (valueModel.getId() != null) {
            object.setId(valueModel.getId());
        }
        if (valueModel.getModifiedDate() != null) {
            object.setModifiedDate(valueModel.getModifiedDate());
        }
        if (valueModel.getCreatedDate() != null) {
            object.setCreatedDate(valueModel.getCreatedDate());
        }
        if (valueModel.getValue() != null) {
            object.setValue(fieldConfig.getName(), valueModel.getValue());
        }
    }

    protected DomainObject buildDomainObject(ResultSet rs, ColumnModel columnModel) throws SQLException {
        GenericDomainObject object = new GenericDomainObject();
        object.setTypeName(domainObjectType);

        for (String columnName : columnModel.getColumnNames()) {
            FieldValueModel valueModel = new FieldValueModel();
            FieldConfig fieldConfig = configurationExplorer.getFieldConfig(domainObjectType, columnName);
            fillValueModel(rs, valueModel, columnName, fieldConfig);
            fillObjectValue(object, valueModel, fieldConfig);
        }

        if (object.getId() != null) {
            String typeName = domainObjectTypeIdCache.getName(object.getId());
            if (typeName != null) {
                object.setTypeName(typeName);
            }
        }

        // TODO добавлено Лариным. М. после выноса системных арибутов в
        // родительский класс надо будет убрать эти 3 строчки
        object.setCreatedDate(object.getTimestamp("created_date"));
        object.setModifiedDate(object.getTimestamp("updated_date"));
        object.setStatus(object.getReference("status"));
        object.resetDirty();

        return object;
    }

    protected DomainObjectVersion buildDomainObjectVersion(ResultSet rs) throws SQLException {
        GenericDomainObjectVersion object = new GenericDomainObjectVersion();

        int typeId = domainObjectTypeIdCache.getId(domainObjectType);

        // Установка полей версии
        object.setId(new RdbmsId(typeId, rs.getLong(DomainObjectDao.ID_COLUMN)));
        object.setDomainObjectId(new RdbmsId(typeId, rs.getLong(DomainObjectDao.DOMAIN_OBJECT_ID_COLUMN)));
        object.setModifiedDate(rs.getTimestamp(UPDATED_DATE_COLUMN));

        ReferenceValue updatedByRef = readReferenceValue(rs, DomainObjectDao.UPDATED_BY, null);
        object.setModifier(updatedByRef.get());

        object.setVersionInfo(rs.getString(DomainObjectDao.INFO_COLUMN));
        object.setIpAddress(rs.getString(DomainObjectDao.IP_ADDRESS_COLUMN));
        object.setComponent(rs.getString(DomainObjectDao.COMPONENT_COLUMN));
        object.setOperation(getOperation(rs.getInt(DomainObjectDao.OPERATION_COLUMN)));

        setDomainObjectFields(object, rs, domainObjectType);

        object.resetDirty();

        return object;
    }

    private DomainObjectVersion.AuditLogOperation getOperation(int operation){
        DomainObjectVersion.AuditLogOperation result = null;
        if (operation == 1){
            result = DomainObjectVersion.AuditLogOperation.CREATE;
        }else if(operation == 2){
            result = DomainObjectVersion.AuditLogOperation.UPDATE;
        }else{
            result = DomainObjectVersion.AuditLogOperation.DELETE;
        }
        return result;
    }

    private void setDomainObjectFields(GenericDomainObjectVersion object, ResultSet rs, String type) throws SQLException {
        if (type != null) {
            DomainObjectTypeConfig doConfig = configurationExplorer.getConfig(DomainObjectTypeConfig.class, type);
            // Установка полей доменного объекта
            for (FieldConfig fieldConfig : doConfig.getDomainObjectFieldsConfig().getFieldConfigs()) {
                FieldValueModel valueModel = new FieldValueModel();
                fillValueModel(rs, valueModel, fieldConfig.getName(), fieldConfig);
                object.setValue(fieldConfig.getName(), valueModel.getValue());
            }
            setDomainObjectFields(object, rs, doConfig.getExtendsAttribute());
        }
    }

    protected DomainObjectCacheServiceImpl getDomainObjectCacheService() {
        if (domainObjectCacheService == null) {
            domainObjectCacheService = SpringApplicationContext.getContext().getBean("domainObjectCacheService",
                    DomainObjectCacheServiceImpl.class);
        }
        return domainObjectCacheService;
    }

    protected RdbmsId readId(ResultSet rs, String columnName) throws SQLException {
        Long longValue = rs.getLong(columnName);
        if (rs.wasNull()) {
            throw new FatalException("Id field can not be null for object " + domainObjectType);
        }

        Integer idType = null;
        if (columnName.equals(DomainObjectDao.ID_COLUMN.toLowerCase())){
            idType = rs.getInt(TYPE_ID_COLUMN);
        } else {
            idType = rs.getInt(getReferenceTypeColumnName(columnName).toLowerCase());
        }
        if (rs.wasNull()) {
            throw new FatalException("Id type field can not be null for object " + domainObjectType);
        }

        return new RdbmsId(idType, longValue);
    }

    // protected void fillValueModelWithSystemFields(SystemField systemFields,)

    /**
     * Обертывает заполненное поле (атрибут), поле parent или поле id в доменном
     * объекте.
     *
     * @author atsvetkov
     */
    protected class FieldValueModel {
        private Id id = null;
        private Value value = null;
        private Date createdDate = null;
        private Date modifiedDate = null;

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

        public Date getCreatedDate() {
            return createdDate;
        }

        public void setCreatedDate(Date createdDate) {
            this.createdDate = createdDate;
        }

        public Date getModifiedDate() {
            return modifiedDate;
        }

        public void setModifiedDate(Date modifiedDate) {
            this.modifiedDate = modifiedDate;
        }
    }

    /**
     * Модель для хранкения названия колонолк и названия колонки-первичного
     * ключа для доменного объекта.
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
