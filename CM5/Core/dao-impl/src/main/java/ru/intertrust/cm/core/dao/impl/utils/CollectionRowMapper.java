package ru.intertrust.cm.core.dao.impl.utils;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import ru.intertrust.cm.core.business.api.dto.GenericIdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.util.ObjectCloner;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.impl.ResultSetExtractionLogger;
import ru.intertrust.cm.core.model.FatalException;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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

        ColumnModel columnModel = buildColumnModel(rs);
        ResultSetMetaData metaData = rs.getMetaData();
        Map<String, FieldConfig> columnTypeMap = new HashMap<>((int) (metaData.getColumnCount() / 0.75f));

        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String fieldName = metaData.getColumnName(i);
            columnTypeMap.put(fieldName, getFieldConfigByDbTypeName(fieldName, metaData.getColumnType(i)));
        }

        addMissedFieldConfigs(columnModel, columnTypeMap);
        List<FieldConfig> collectionFieldConfigs = collectFieldConfigs(columnModel);
        collection.setFieldsConfiguration(collectionFieldConfigs);
        
        int row = 0;
        String firstReferenceName = null;
        final long start = System.currentTimeMillis();
        while (rs.next()) {
            ResultSetExtractionLogger.log("CollectionRowMapper.extractData", start, row + 1);
            int index = 0;

            for (int i = 0; i < columnModel.getColumns().size(); i ++) {
                Column column = columnModel.getColumns().get(i);
                FieldValueModel valueModel = new FieldValueModel();

                FieldConfig fieldConfig = columnToConfigMap.get(column.getName());
                fillValueModel(rs, valueModel, columnModel.getColumns(),i, fieldConfig);

                if (valueModel.getId() != null) {
                    collection.setId(row, valueModel.getId());
                    collection.set(index, row, new ReferenceValue(valueModel.getId()));
                    index++;
                } else if (valueModel.getValue() != null) {
                    collection.set(index, row, valueModel.getValue());
                    index++;
                }
                // если поле в коллекцию не добавляется, то индекс не инкрементируется
            }

            if (index == 0) {
                continue;
            }

            // Для случая извлечения коллекции по запросу при отсутствующем Id, заполняем Id первым ссылочным полем
            if (collectionName == null && collection.getId(row) == null) {
                if (firstReferenceName == null) {
                    for (FieldConfig fieldConfig : collection.getFieldsConfiguration()) {
                        if (fieldConfig instanceof ReferenceFieldConfig) {
                            firstReferenceName = ((ReferenceFieldConfig) fieldConfig).getName();
                            break;
                        }
                    }
                }
                collection.setId(row, collection.get(row).getReference(firstReferenceName));
            } else if (collection.getId(row) == null) {
                throw new FatalException("Id field can not be null in collection " + collectionName);
            }

            collection.resetDirty(row);
            row++;
        }

        return collection;
    }

    private List<FieldConfig> collectFieldConfigs(ColumnModel columnModel) {
        List<String> fieldNamesToDisplay = collectColumnNamesToDisplay(columnModel);
        List<FieldConfig> collectionFieldConfigs = new ArrayList<>(fieldNamesToDisplay.size());

        ObjectCloner cloner = ObjectCloner.getInstance();

        for (String columnName : fieldNamesToDisplay) {
            FieldConfig columnFieldConfig = columnToConfigMap.get(columnName);
            if (columnFieldConfig != null) {
                FieldConfig collectionColumnFieldConfig = cloner.cloneObject(columnFieldConfig, FieldConfig.class);
                collectionColumnFieldConfig.setName(columnName);

                collectionFieldConfigs.add(collectionColumnFieldConfig);
            }
        }
        return collectionFieldConfigs;
    }

    private void addMissedFieldConfigs(ColumnModel columnModel, Map<String, FieldConfig> columnTypeMap) {
        for (Column column : columnModel.getColumns()) {
            String columnName = column.getName();
            FieldConfig columnFieldConfig = columnToConfigMap.get(columnName);
            if (columnFieldConfig != null) {
                continue;
            }

            if (columnName.endsWith(DomainObjectDao.REFERENCE_TYPE_POSTFIX)) {
                String testFieldName = columnName.substring(0,
                        columnName.length() - DomainObjectDao.REFERENCE_TYPE_POSTFIX.length());
                if (columnToConfigMap.get(testFieldName) == null) {
                    columnToConfigMap.put(columnName, columnTypeMap.get(columnName));
                }
            } else if (columnName.endsWith(DomainObjectDao.TIME_ID_ZONE_POSTFIX)) {
                String testFieldName = columnName.substring(0,
                        columnName.length() - DomainObjectDao.TIME_ID_ZONE_POSTFIX.length());
                if (columnToConfigMap.get(testFieldName) == null) {
                    columnToConfigMap.put(columnName, columnTypeMap.get(columnName));
                }
            } else {
                columnToConfigMap.put(columnName, columnTypeMap.get(columnName));
            }
        }
    }

    /**
     * Возвращает список названий колонок, которые будут заполняться в коллекции. SQL запрос коллекции может содержать
     * произвольные поля, но добавляются в коллекцию только поля, которые указаны в конфигурации представления
     * коллекции (collections-view.xml). Причем, список не содержит колонку идентификатор типа (id_type).
     * Колонка идентификатор содержится в списке всегда.
     * @param columnModel модель колонок содержит список всех колонок из запроса.
     * @return список колонок, которые будут добавлены в коллекцию.
     */
    private List<String> collectColumnNamesToDisplay(ColumnModel columnModel) {
        final List<Column> columns = columnModel.getColumns();
        final List<String> fieldNamesToInsert = new ArrayList<>(columns.size());
        for (Column column : columns) {
            if(!TYPE_ID_COLUMN.equals(column.getName())) {
                fieldNamesToInsert.add(column.getName());
            }
        }
        return fieldNamesToInsert;
    }

}
