package ru.intertrust.cm.core.dao.impl.utils;

import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.impl.DomainObjectCacheServiceImpl;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.util.SpringApplicationContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.List;

import static ru.intertrust.cm.core.dao.api.DomainObjectDao.CREATED_DATE_COLUMN;
import static ru.intertrust.cm.core.dao.api.DomainObjectDao.UPDATED_DATE_COLUMN;

/**
 * Базовй класс для отображения {@link java.sql.ResultSet} на доменные объекты и
 * коллекции.
 *
 * @author atsvetkov
 */
public class BasicRowMapper extends ValueReader {

    protected static final String TYPE_ID_COLUMN = Case.toLower(DomainObjectDao.TYPE_COLUMN);

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

    protected FieldConfig getFieldConfigByDbTypeName(String columnName, int columnType) {
        FieldConfig result;

        if (columnType == Types.BIGINT || columnType == Types.INTEGER) {
            result =  new LongFieldConfig();
        } else if (columnType == Types.TIMESTAMP) {
            result = new DateTimeFieldConfig();
        } else if (columnType == Types.DATE) {
            result = new TimelessDateFieldConfig();
        } else if (columnType == Types.VARCHAR || columnType == Types.LONGVARCHAR || columnType == Types.LONGNVARCHAR ||
                columnType == Types.CLOB || columnType == Types.NCLOB || columnType == Types.OTHER) {
            result = new StringFieldConfig();
        } else if (columnType == Types.BOOLEAN || columnType == Types.SMALLINT) {
            result = new BooleanFieldConfig();
        } else if (columnType == Types.DECIMAL) {
            result = new DecimalFieldConfig();
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
     * @param columns
     *            колонки, которые извлекаются из {@see ResultSet}
     * @throws SQLException
     */
    protected void fillValueModel(ResultSet rs, FieldValueModel valueModel, List<Column> columns, int columnIndex,
                                  FieldConfig fieldConfig) throws SQLException {
        Column column = columns.get(columnIndex);

        if (idField.equalsIgnoreCase(column.getName())) {
            Id id = readId(rs, column);
            if (id != null) {
                valueModel.setId(id);
            }
            return;
        }

        Value value = readValue(rs, columns, columnIndex, fieldConfig);
        valueModel.setValue(value);

        if (CREATED_DATE_COLUMN.equalsIgnoreCase(column.getName()) && value != null && value.get() != null) {
            valueModel.setCreatedDate(((DateTimeValue) value).get());
        } else if (UPDATED_DATE_COLUMN.equalsIgnoreCase(column.getName()) && value != null && value.get() != null) {
            valueModel.setModifiedDate(((DateTimeValue) value).get());
        }
    }

    @Override
    protected ReferenceValue readReferenceValue(ResultSet rs, List<BasicRowMapper.Column> columns, int columnIndex)
            throws SQLException {
        BasicRowMapper.Column column = columns.get(columnIndex);

        if (!DomainObjectDao.ACCESS_OBJECT_ID.equals(column.getName())) {
            return super.readReferenceValue(rs, columns, columnIndex);
        }

        String accessObjectTypeName = configurationExplorer.getMatrixReferenceTypeName(domainObjectType);
        if (accessObjectTypeName == null) {
            accessObjectTypeName = domainObjectType;
        }
        accessObjectTypeName = ConfigurationExplorerUtils.getTopLevelParentType(configurationExplorer, accessObjectTypeName);

        Integer accessObjectType = domainObjectTypeIdCache.getId(accessObjectTypeName);

        Number longValue = (Number) rs.getObject(column.getIndex());
        if (longValue == null) {
            throw new FatalException("access_object_id is null for " + domainObjectType);
        }

        return new ReferenceValue(new RdbmsId(accessObjectType, longValue.longValue()));
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
            String typeName = domainObjectTypeIdCache.getName(object.getId());
            if (typeName != null) {
                object.setTypeName(typeName);
            }
        }
        if (valueModel.getModifiedDate() != null) {
            object.setModifiedDate(valueModel.getModifiedDate());
        }
        if (valueModel.getCreatedDate() != null) {
            object.setCreatedDate(valueModel.getCreatedDate());
        }
        if (valueModel.getValue() != null) {
            Value filledValue = object.getValue(fieldConfig.getName());
            if (filledValue == null || filledValue.get() == null) {
                object.setValue(fieldConfig.getName(), valueModel.getValue());
            }
        }
    }

    protected ColumnModel buildColumnModel(ResultSet rs) throws SQLException {
        ColumnModel columnModel = new ColumnModel();
        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
            String fieldName = rs.getMetaData().getColumnName(i);
            columnModel.getColumns().add(new Column(i, fieldName));
        }

        return columnModel;
    }

    protected DomainObject buildDomainObject(ResultSet rs, ColumnModel columnModel) throws SQLException {
        GenericDomainObject object = new GenericDomainObject();
        object.setTypeName(domainObjectType);

        for (int i = 0; i < columnModel.getColumns().size(); i ++) {
            FieldValueModel valueModel = new FieldValueModel();
            Column column = columnModel.getColumns().get(i);
            FieldConfig fieldConfig = configurationExplorer.getFieldConfig(object.getTypeName(), column.getName());

            fillValueModel(rs, valueModel, columnModel.getColumns(), i, fieldConfig);
            fillObjectValue(object, valueModel, fieldConfig);
        }

        if (object.getId() == null) {
            throw new FatalException("Id field can not be null for object " + domainObjectType);
        }

        object.resetDirty();

        return object;
    }

    protected DomainObjectCacheServiceImpl getDomainObjectCacheService() {
        if (domainObjectCacheService == null) {
            domainObjectCacheService = SpringApplicationContext.getContext().getBean("domainObjectCacheService",
                    DomainObjectCacheServiceImpl.class);
        }
        return domainObjectCacheService;
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

}
