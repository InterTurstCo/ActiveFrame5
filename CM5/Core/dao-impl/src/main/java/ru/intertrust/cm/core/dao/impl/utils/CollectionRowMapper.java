package ru.intertrust.cm.core.dao.impl.utils;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.gui.collection.view.CollectionColumnConfig;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.impl.DataType;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Отображает {@link java.sql.ResultSet} на {@link ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection}.
 *
 * @author atsvetkov
 */
@SuppressWarnings("rawtypes")
public class CollectionRowMapper extends BasicRowMapper implements
        ResultSetExtractor<IdentifiableObjectCollection> {

    protected final String collectionName;

    public CollectionRowMapper(String collectionName, String idField, ConfigurationExplorer configurationExplorer,
                               DomainObjectTypeIdCache domainObjectTypeIdCache) {
        super(null, idField, configurationExplorer, domainObjectTypeIdCache);
        this.collectionName = collectionName;
    }

    @Override
    public IdentifiableObjectCollection extractData(ResultSet rs) throws SQLException, DataAccessException {
        GenericIdentifiableObjectCollection collection = new GenericIdentifiableObjectCollection();

        ColumnModel columnModel = new ColumnModel();
        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
            String fieldName = rs.getMetaData().getColumnName(i);
            columnModel.getColumnNames().add(fieldName);
        }

        List<String> fieldNamesToInsert = collectColumnNamesToInsert(columnModel);

        collection.setFields(fieldNamesToInsert);

        int row = 0;
        while (rs.next()) {

            int index = 0;
            int columnIndex = 0;
            FieldValueModel valueModel = new FieldValueModel();

            for (String columnName : columnModel.getColumnNames()) {
                fillValueModel(rs, columnModel, valueModel, columnName);

                if (valueModel.getId() != null) {
                    collection.setId(row, valueModel.getId());
                    columnIndex = index == 0 ? 0 : index - 1;
                }
                if (valueModel.getValue() != null) {
                    collection.set(columnIndex, row, valueModel.getValue());
                }
                index++;
            }

            collection.resetDirty(row);
            row++;
        }

        return collection;
    }

    /**
     * Возвращает список названий колонок, которые будут добавлены в коллекцию. SQL запрос коллекции может содержать
     * произвольные поля, но добавляются в коллекцию только поля, которые указаны в конфигурации представления
     * коллекции. Причем, список не содержит колонку-идентификатор.
     * @param columnModel модель колонок содержит список всех колонок из запроса.
     * @return список колонок, которые будут добавлены в коллекцию.
     */
    private List<String> collectColumnNamesToInsert(ColumnModel columnModel) {
        List<String> fieldNamesToInsert = new ArrayList<String>();
        for (String columnName : columnModel.getColumnNames()) {
            CollectionColumnConfig columnConfig =
                    configurationExplorer.getCollectionColumnConfig(collectionName, columnName);
            if(idField.equals(columnName)) {
                continue;
            }
            if (columnConfig != null) {
                fieldNamesToInsert.add(columnName);
            }
        }
        return fieldNamesToInsert;
    }

    protected void fillValueModel(ResultSet rs, ColumnModel columnModel, FieldValueModel valueModel,
                                  String columnName) throws SQLException {
        CollectionColumnConfig columnConfig =
                configurationExplorer.getCollectionColumnConfig(collectionName, columnName);

        DataType fieldType = null;
        if (columnConfig != null) {
            fieldType = getColumnDataType(columnConfig.getType());
        }
        Value value = null;
        Id id = null;

        if (idField.equalsIgnoreCase(columnName)) {
            id = readId(rs, columnName);
        } else if (DataType.INTEGER.equals(fieldType)) {
            String typeColumnName = columnName + REFERENCE_TYPE_POSTFIX;
            if (columnModel.getColumnNames().contains(typeColumnName)) {
                // Это id поле
                value = readReferenceValue(rs, columnName, typeColumnName);
            } else {
                // Это просто целочисленное поле
                Long longValue = rs.getLong(columnName);
                if (!rs.wasNull()) {
                    value = new LongValue(longValue);
                } else {
                    value = new LongValue();
                }
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
