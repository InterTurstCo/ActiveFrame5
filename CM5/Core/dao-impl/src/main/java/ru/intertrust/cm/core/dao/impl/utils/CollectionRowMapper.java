package ru.intertrust.cm.core.dao.impl.utils;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.gui.collection.view.CollectionColumnConfig;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.impl.DataType;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getServiceColumnName;

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

    public CollectionRowMapper(ConfigurationExplorer configurationExplorer,
                               DomainObjectTypeIdCache domainObjectTypeIdCache) {
        super(null, DomainObjectDao.ID_COLUMN, configurationExplorer, domainObjectTypeIdCache);
        this.collectionName = null;
    }

    @Override
    public IdentifiableObjectCollection extractData(ResultSet rs) throws SQLException, DataAccessException {
        GenericIdentifiableObjectCollection collection = new GenericIdentifiableObjectCollection();

        ColumnModel columnModel = new ColumnModel();
        Map<String, DataType> columnTypeMap = new HashMap<>();
        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
            String fieldName = rs.getMetaData().getColumnName(i);
            columnModel.getColumnNames().add(fieldName);
            columnTypeMap.put(fieldName, getColumnDataTypeByDbTypeName(rs.getMetaData().getColumnTypeName(i)));
        }

        List<String> fieldNamesToInsert = collectColumnNamesToInsert(columnModel);

        collection.setFields(fieldNamesToInsert);

        int row = 0;
        while (rs.next()) {

            int index = 0;

            for (String columnName : columnModel.getColumnNames()) {
                FieldValueModel valueModel = new FieldValueModel();

                fillValueModel(rs, columnModel, columnTypeMap, valueModel, columnName);

                if (valueModel.getId() != null) {
                    collection.setId(row, valueModel.getId());
                } else if (valueModel.getValue() != null && collectionConfigExists(columnName)) {
                    collection.set(index, row, valueModel.getValue());
                    // инкремент индекса только при заполнении полей коллекции (id каждой записи заполняется отдельно)
                    index++;
                }
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
            if(idField.equals(columnName)) {
                continue;
            }
            if (collectionConfigExists(columnName)) {
                fieldNamesToInsert.add(columnName);
            }
        }
        return fieldNamesToInsert;
    }

    protected void fillValueModel(ResultSet rs, ColumnModel columnModel, Map<String, DataType> columnTypeMap,
                                  FieldValueModel valueModel, String columnName) throws SQLException {
        DataType fieldType = getType(columnName, columnTypeMap);
        Value value = null;
        Id id = null;

        if (idField.equalsIgnoreCase(columnName)) {
            id = readId(rs, columnName);
        } else if (DataType.INTEGER.equals(fieldType)) {
            String typeColumnName = getServiceColumnName(columnName, REFERENCE_TYPE_POSTFIX).toLowerCase();
            if (columnModel.getColumnNames().contains(typeColumnName)) {
                // Это id поле
                value = readReferenceValue(rs, columnName, typeColumnName);
            } else {
                // Это просто целочисленное поле
                value = readLongValue(rs, columnName);
            }

        } else if (DataType.DATETIME.equals(fieldType)) {
            String timezoneIdColumnName = getServiceColumnName(columnName, TIME_ZONE_ID_POSTFIX).toLowerCase();
            if (columnModel.getColumnNames().contains(timezoneIdColumnName)) {
                // Это поле с таймзоной
                value = readDateTimeWithTimeZoneValue(rs, columnName, timezoneIdColumnName);
            } else {
                // Это просто поле с датой
                value = readTimestampValue(rs, valueModel, columnName);
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

    protected DataType getType(String columnName, Map<String, DataType> columnTypeMap) {
        if (collectionName != null) {
            CollectionColumnConfig columnConfig =
                configurationExplorer.getCollectionColumnConfig(collectionName, columnName);
            if (columnConfig == null) {
                return null;
            } else {
                return getColumnDataType(columnConfig.getType());
            }
        } else {
            return columnTypeMap.get(columnName);
        }
    }

    protected boolean collectionConfigExists(String columnName) {
        if (collectionName != null) {
            CollectionColumnConfig columnConfig =
                    configurationExplorer.getCollectionColumnConfig(collectionName, columnName);
            return columnConfig != null;
        } else {
            return true; // Для коллекций, получаемых по запросу без конфигурации возвращаем все колонки
        }
    }

}
