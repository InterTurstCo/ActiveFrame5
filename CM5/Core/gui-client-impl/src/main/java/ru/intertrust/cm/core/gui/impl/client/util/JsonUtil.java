package ru.intertrust.cm.core.gui.impl.client.util;

import com.google.gwt.json.client.*;
import ru.intertrust.cm.core.config.gui.navigation.SortCriteriaConfig;
import ru.intertrust.cm.core.config.gui.navigation.SortCriterionConfig;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.SortCollectionState;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 09.04.14
 *         Time: 16:15
 */
public class JsonUtil {

    public static void prepareJsonSortCriteria(JSONObject requestObj, LinkedHashMap<String,
            CollectionColumnProperties> fieldPropertiesMap, SortCollectionState sortCollectionState) {
        if (sortCollectionState != null && sortCollectionState.isSortable()) {
            String fieldName = sortCollectionState.getField();
            requestObj.put("sortedFieldName", new JSONString(fieldName));
            boolean ascend = sortCollectionState.isAscend();
            requestObj.put("ascend", JSONBoolean.getInstance(ascend));
            CollectionColumnProperties properties = fieldPropertiesMap.get(fieldName);
            SortCriteriaConfig sortCriteriaConfig = ascend ? properties.getAscSortCriteriaConfig()
                    : properties.getDescSortCriteriaConfig();
            JSONObject sortCriteria = null;
            if (sortCriteriaConfig != null) {
                List<SortCriterionConfig> sortCriterionConfigs = sortCriteriaConfig.getSortCriterionConfigs();
                JSONArray criterions = new JSONArray();
                int count = 0;
                for (SortCriterionConfig sortCriterionConfig : sortCriterionConfigs) {
                    JSONObject sortCriterion = new JSONObject();
                    String field = sortCriterionConfig.getField();
                    sortCriterion.put("field", new JSONString(field));
                    String order = sortCriterionConfig.getOrderString();
                    sortCriterion.put("order", new JSONString(order));
                    criterions.set(count, sortCriterion);
                    count++;

                }
                sortCriteria = new JSONObject();
                sortCriteria.put("criterions", criterions);
            }
            requestObj.put("sortCriteria", sortCriteria);
        }

    }

    public static void prepareJsonColumnProperties(JSONObject requestObj, LinkedHashMap<String,
            CollectionColumnProperties> fieldPropertiesMap, Map<String, String> filtersMap) {
        Set<String> fieldNames = fieldPropertiesMap.keySet();
        JSONArray propertiesArray = new JSONArray();
        int count = 0;
        for (String fieldName : fieldNames) {
            JSONObject jsonColumnProperties = new JSONObject();
            String filterValue = filtersMap.get(fieldName);
            CollectionColumnProperties properties = fieldPropertiesMap.get(fieldName);
            if (filterValue != null) {
                jsonColumnProperties.put("filterValue", new JSONString(filterValue));
                String filterName = (String) properties.getProperty(CollectionColumnProperties.SEARCH_FILTER_KEY);
                jsonColumnProperties.put("filterName", new JSONString(filterName));
            }
            jsonColumnProperties.put("fieldName", new JSONString(fieldName));
            String pattern = (String) properties.getProperty(CollectionColumnProperties.PATTERN_KEY);
            jsonColumnProperties.put("pattern", new JSONString(pattern));
            String fieldType = (String) properties.getProperty(CollectionColumnProperties.TYPE_KEY);
            jsonColumnProperties.put("fieldType", new JSONString(fieldType));
            String timeZoneId = (String) properties.getProperty(CollectionColumnProperties.TIME_ZONE_ID);
            jsonColumnProperties.put("timeZoneId", new JSONString(timeZoneId));
            String columnName = (String) properties.getProperty(CollectionColumnProperties.NAME_KEY);
            jsonColumnProperties.put("columnName", new JSONString(columnName));
            propertiesArray.set(count, jsonColumnProperties);
            count++;

        }
        requestObj.put("columnProperties", propertiesArray);


    }

    public static void prepareJsonAttributes(JSONObject requestObj, String collectionName,
                                              String simpleSearchQuery, String searchArea, int rowCount) {
        requestObj.put("collectionName", new JSONString(collectionName));
        requestObj.put("simpleSearchQuery", new JSONString(simpleSearchQuery));
        requestObj.put("simpleSearchArea", new JSONString(searchArea));
        requestObj.put("rowCount", new JSONNumber(rowCount));
    }
}
