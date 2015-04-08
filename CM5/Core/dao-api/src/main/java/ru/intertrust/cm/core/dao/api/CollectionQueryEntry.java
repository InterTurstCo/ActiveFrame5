package ru.intertrust.cm.core.dao.api;

import java.util.Map;

import ru.intertrust.cm.core.config.FieldConfig;

/**
 * Кешируемая информация о коллекции. Включает SQL запрос, конфигурацию колонок из запроса (необходима для отображения
 * результата запроса на {@link IdentifiableObjectCollection}).
 * @author atsvetkov
 */
public class CollectionQueryEntry {

    private String query;
    Map<String, FieldConfig> columnToConfigMap;

    public String getQuery() {
        return query;
    }

    public Map<String, FieldConfig> getColumnToConfigMap() {
        return columnToConfigMap;
    }

    public CollectionQueryEntry(String query, Map<String, FieldConfig> columnToConfigMap) {
        this.query = query;
        this.columnToConfigMap = columnToConfigMap;
    }
}
