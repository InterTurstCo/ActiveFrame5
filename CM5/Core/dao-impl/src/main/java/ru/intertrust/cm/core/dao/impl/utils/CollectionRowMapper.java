package ru.intertrust.cm.core.dao.impl.utils;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import ru.intertrust.cm.core.business.api.dto.GenericIdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionColumnConfig;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.impl.DataType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Отображает {@link java.sql.ResultSet} на {@link ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection}.
 *
 * @author atsvetkov
 */
@SuppressWarnings("rawtypes")
public class CollectionRowMapper extends BasicRowMapper implements
        ResultSetExtractor<IdentifiableObjectCollection> {

    protected final String collectionName;
    protected final Map<String, FieldConfig> columnToConfigMap;

    public CollectionRowMapper(String collectionName, Map<String, FieldConfig> columnToConfigMap, String idField,
                               ConfigurationExplorer configurationExplorer,
                               DomainObjectTypeIdCache domainObjectTypeIdCache) {
        super(null, idField, configurationExplorer, domainObjectTypeIdCache);
        this.collectionName = collectionName;
        this.columnToConfigMap = columnToConfigMap;
    }

    public CollectionRowMapper(Map<String, FieldConfig> columnToConfigMap, ConfigurationExplorer configurationExplorer,
                               DomainObjectTypeIdCache domainObjectTypeIdCache) {
        super(null, DomainObjectDao.ID_COLUMN, configurationExplorer, domainObjectTypeIdCache);
        this.collectionName = null;
        this.columnToConfigMap = columnToConfigMap;
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

                FieldConfig fieldConfig = columnToConfigMap.get(columnName);
                fillValueModel(rs, valueModel, columnName, fieldConfig);

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
