package ru.intertrust.cm.core.dao.impl.utils;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.CollectionColumnConfig;
import ru.intertrust.cm.core.dao.impl.DataType;
import ru.intertrust.cm.core.dao.impl.SqlQueryModifier;
import ru.intertrust.cm.core.model.FatalException;

/**
 * Отображает {@link java.sql.ResultSet} на {@link ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection}.
 *
 * @author atsvetkov
 */
@SuppressWarnings("rawtypes")
public class CollectionRowMapper extends BasicRowMapper implements
        ResultSetExtractor<IdentifiableObjectCollection> {

    protected final String collectionName;
    
    public CollectionRowMapper(String collectionName, String idField, ConfigurationExplorer configurationExplorer) {
        super(null, idField, configurationExplorer);
        this.collectionName = collectionName;
    }

    @Override
    public IdentifiableObjectCollection extractData(ResultSet rs) throws SQLException, DataAccessException {
        IdentifiableObjectCollection collection = new GenericIdentifiableObjectCollection();

        ColumnModel columnModel = new ColumnModel();
        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
            String fieldName = rs.getMetaData().getColumnName(i);
            columnModel.getColumnNames().add(fieldName);            
        }

        collection.setFields(columnModel.getColumnNames());

        int row = 0;
        while (rs.next()) {
            
            int index = 0;
            int columnIndex = 0;
            FieldValueModel valueModel = new FieldValueModel();
            
            for (String columnName : columnModel.getColumnNames()) {                               
                fillValueModel(rs, valueModel, columnName);
                
                if (valueModel.getId() != null) {
                    collection.setId(row, valueModel.getId());
                    columnIndex = index == 0 ? 0 : index -1;
                }
                if (valueModel.getValue() != null) {
                    collection.set(columnIndex, row, valueModel.getValue());
                }
                index++;
            }
            row++;
        }
                       
        return collection;
    }

    protected void fillValueModel(ResultSet rs, FieldValueModel valueModel, String columnName) throws SQLException {
        CollectionColumnConfig columnConfig =
                configurationExplorer.getCollectionColumnConfig(collectionName, columnName);

        DataType fieldType = null;
        if (columnConfig != null) {
            fieldType = getColumnDataType(columnConfig.getType());
        }
        Value value = null;
        Id id = null;

        if (idField.equalsIgnoreCase(columnName)) {
            Long longValue = rs.getLong(columnName);
            String idType = rs.getString(SqlQueryModifier.DOMAIN_OBJECT_TYPE_ALIAS);
            
            if (!rs.wasNull()) {
                id = new RdbmsId(idType, longValue);
            } else {
                throw new FatalException("Id field can not be null for object " + domainObjectType);
            }
        } else if (DataType.INTEGER.equals(fieldType)) {
            value = new DecimalValue();
            Long longValue = rs.getLong(columnName);
            if (!rs.wasNull()) {
                value = new LongValue(longValue);
            } else {
                value = new LongValue();
            }

        } else if (DataType.DATETIME.equals(fieldType)) {
            Timestamp timestamp = rs.getTimestamp(columnName);
            if (!rs.wasNull()) {
                Date date = new Date(timestamp.getTime());
                value = new TimestampValue(date);
            } else {
                value = new TimestampValue();
            }

        } else if (DataType.STRING.equals(fieldType)) {
            String fieldValue = rs.getString(columnName);
            if (!rs.wasNull()) {
                value = new StringValue(fieldValue);
            } else {
                value = new StringValue();
            }

        } else if (DataType.BOOLEAN.equals(fieldType)) {
            Boolean fieldValue = rs.getBoolean(columnName);
            if (!rs.wasNull()) {
                value = new BooleanValue(fieldValue);
            } else {
                value = new BooleanValue();
            }

        } else if (DataType.DECIMAL.equals(fieldType)) {
            BigDecimal fieldValue = rs.getBigDecimal(columnName);
            if (!rs.wasNull()) {
                value = new DecimalValue(fieldValue);
            } else {
                value = new DecimalValue();
            }
        }

        if (id != null) {
            valueModel.setId(id);
        }
        valueModel.setValue(value);
    }

}
