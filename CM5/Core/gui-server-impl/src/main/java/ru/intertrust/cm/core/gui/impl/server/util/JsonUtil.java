package ru.intertrust.cm.core.gui.impl.server.util;

import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.config.gui.navigation.SortCriteriaConfig;
import ru.intertrust.cm.core.config.gui.navigation.SortCriterionConfig;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;
import ru.intertrust.cm.core.gui.model.csv.JsonColumnProperties;
import ru.intertrust.cm.core.gui.model.csv.JsonSortCriteria;
import ru.intertrust.cm.core.gui.model.csv.JsonSortCriterion;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 09.04.14
 *         Time: 16:15
 */
public class JsonUtil {
    public static List<Filter> prepareFilters(List<JsonColumnProperties> jsonPropertiesList,
                                              Map<String, CollectionColumnProperties> columnPropertiesMap) throws ParseException {
        List<Filter> filters = new ArrayList<>();
        for (JsonColumnProperties jsonProperties : jsonPropertiesList) {
            String filterValue = jsonProperties.getFilterValue();
            if (filterValue != null){
            String fieldName = jsonProperties.getFieldName();
            CollectionColumnProperties columnProperties = columnPropertiesMap.get(fieldName);

            Filter filter = FilterBuilder.prepareSearchFilter(filterValue, columnProperties);
            filters.add(filter);
            }
        }
        return filters;
    }

    private static CollectionColumnProperties convertToColumnProperties(JsonColumnProperties jsonProperties) {
        CollectionColumnProperties properties = new CollectionColumnProperties();
        String filterName = jsonProperties.getFilterName();
        properties.addProperty(CollectionColumnProperties.SEARCH_FILTER_KEY, filterName);
        String fieldName = jsonProperties.getFieldName();
        properties.addProperty(CollectionColumnProperties.FIELD_NAME, fieldName);
        String fieldType = jsonProperties.getFieldType();
        properties.addProperty(CollectionColumnProperties.TYPE_KEY, fieldType);
        String timeZoneId = jsonProperties.getTimeZoneId();
        properties.addProperty(CollectionColumnProperties.TIME_ZONE_ID, timeZoneId);
        String pattern = jsonProperties.getPattern();
        properties.addProperty(CollectionColumnProperties.PATTERN_KEY, pattern);
        String columnName = jsonProperties.getColumnName();
        properties.addProperty(CollectionColumnProperties.NAME_KEY, columnName);
        return properties;

    }

    public static Map<String, CollectionColumnProperties> convertToColumnPropertiesMap(
            List<JsonColumnProperties> jsonPropertiesList){
        Map<String, CollectionColumnProperties> columnPropertiesMap =   new LinkedHashMap<String, CollectionColumnProperties>();
        for (JsonColumnProperties params : jsonPropertiesList) {
            CollectionColumnProperties properties = convertToColumnProperties(params);
            String fieldName = params.getFieldName();
            columnPropertiesMap.put(fieldName, properties);
        }
        return columnPropertiesMap;
    }

    public static SortCriteriaConfig convertToSortCriteriaConfig(JsonSortCriteria sortCriteria) {
        if(sortCriteria == null) {
            return null;
        }
        List<JsonSortCriterion> jsonSortCriterions = sortCriteria.getCriterions();
        List<SortCriterionConfig> sortCriterionConfigs = new ArrayList<>();
        for (JsonSortCriterion sortCriterion : jsonSortCriterions) {
            SortCriterionConfig sortCriterionConfig = new SortCriterionConfig();
            String field = sortCriterion.getField();
            sortCriterionConfig.setField(field);
            String order = sortCriterion.getOrder();
            sortCriterionConfig.setOrderString(order);
            sortCriterionConfigs.add(sortCriterionConfig);
        }
        SortCriteriaConfig sortCriteriaConfig = new SortCriteriaConfig();
        sortCriteriaConfig.setSortCriterionConfigs(sortCriterionConfigs);
        return sortCriteriaConfig;

    }

}
