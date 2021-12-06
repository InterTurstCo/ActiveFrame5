package ru.intertrust.cm.core.gui.impl.client.util;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.fasterxml.jackson.databind.util.JSONWrappedObject;
import com.google.gwt.json.client.*;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.gui.form.widget.filter.AbstractFilterConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.ParamConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.extra.CollectionExtraFiltersConfig;
import ru.intertrust.cm.core.config.gui.navigation.InitialFiltersConfig;
import ru.intertrust.cm.core.config.gui.navigation.SortCriteriaConfig;
import ru.intertrust.cm.core.config.gui.navigation.SortCriterionConfig;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.SortCollectionState;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;

import java.util.*;

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
            CollectionColumnProperties> fieldPropertiesMap, Map<String, List<String>> filtersMap) {
        Set<String> fieldNames = fieldPropertiesMap.keySet();
        JSONArray propertiesArray = new JSONArray();
        int count = 0;
        for (String fieldName : fieldNames) {
            JSONObject jsonColumnProperties = new JSONObject();
            List<String> filterValues = filtersMap.get(fieldName);
            CollectionColumnProperties properties = fieldPropertiesMap.get(fieldName);
            if (filterValues != null) {
                JSONArray jsonFilterValues = prepareFilterValuesRepresentation(filterValues);
                jsonColumnProperties.put("filterValues", jsonFilterValues);


            }
            String filterName = (String) properties.getProperty(CollectionColumnProperties.SEARCH_FILTER_KEY);
            if (filterName != null) {
                jsonColumnProperties.put("filterName", new JSONString(filterName));
            }
            jsonColumnProperties.put("fieldName", new JSONString(fieldName));
            String datePattern = (String) properties.getProperty(CollectionColumnProperties.DATE_PATTERN);
            jsonColumnProperties.put("datePattern", new JSONString(datePattern));
            String timePattern = (String) properties.getProperty(CollectionColumnProperties.TIME_PATTERN);
            JSONString jsonTimePattern = timePattern == null ? null : new JSONString(timePattern);
            jsonColumnProperties.put("timePattern", jsonTimePattern);
            String fieldType = (String) properties.getProperty(CollectionColumnProperties.TYPE_KEY);
            jsonColumnProperties.put("fieldType", new JSONString(fieldType));
            String timeZoneId = (String) properties.getProperty(CollectionColumnProperties.TIME_ZONE_ID);
            jsonColumnProperties.put("timeZoneId", new JSONString(timeZoneId));
            String columnName = (String) properties.getProperty(CollectionColumnProperties.NAME_KEY);
            jsonColumnProperties.put("columnName", new JSONString(columnName));
            List<String> initialFilterValues = (List<String>) properties.getProperty(CollectionColumnProperties.INITIAL_FILTER_VALUES);
            if (initialFilterValues != null) {
                jsonColumnProperties.put("initialFilterValues", prepareFilterValuesRepresentation(initialFilterValues));
            }

            propertiesArray.set(count, jsonColumnProperties);

            count++;

        }
        requestObj.put("columnProperties", propertiesArray);


    }

    private static JSONArray prepareFilterValuesRepresentation(List<String> filterValues) {
        JSONArray jsonFiltersArr = new JSONArray();
        for (int i = 0; i < filterValues.size(); i++) {
            String filterValue = filterValues.get(i);
            jsonFiltersArr.set(i, new JSONString(filterValue));
        }
        return jsonFiltersArr;

    }

    public static void prepareJsonAttributes(JSONObject requestObj, String collectionName, String simpleSearchQuery,
                                             String searchArea) {

        requestObj.put("collectionName", new JSONString(collectionName));
        requestObj.put("simpleSearchQuery", new JSONString(simpleSearchQuery));
        requestObj.put("simpleSearchArea", new JSONString(searchArea));
    }

    public static void prepareJsonInitialFilters(JSONObject requestObj, InitialFiltersConfig initialFiltersConfig,
                                                 String filterAttributeName) {
        if (initialFiltersConfig == null) {
            return;
        }
        JSONObject jsonInitialFiltersObj = new JSONObject();
        putNotNullString("panelState", initialFiltersConfig.getPanelState(), jsonInitialFiltersObj);
        prepareFiltersConfigs(jsonInitialFiltersObj, initialFiltersConfig.getFilterConfigs());
        requestObj.put(filterAttributeName, jsonInitialFiltersObj);

    }

    public static void prepareJsonSelectedIdsFilter(JSONObject requestObj, List<Id> selectedIds, String filterAttributeName) {
        if (selectedIds == null || selectedIds.isEmpty()) {
            return;
        }
        JSONObject jsonFilterObject = new JSONObject();
        JSONArray jsonIdsArr = new JSONArray();
        int index = 0;
        for (Id id : selectedIds) {
            if (id != null) {
                jsonIdsArr.set(index, new JSONString(id.toStringRepresentation()));
                index++;
            }
        }
        jsonFilterObject.put("selectedIds", jsonIdsArr);
        requestObj.put(filterAttributeName, jsonFilterObject);
    }

    public static void prepareJsonHierarchicalFiltersConfig(JSONObject requestObj, CollectionExtraFiltersConfig extraFiltersConfig,
                                                            String filterAttributeName) {
        if (extraFiltersConfig == null) {
            return;
        }
        JSONObject filtersObj = new JSONObject();
        prepareFiltersConfigs(filtersObj, extraFiltersConfig.getFilterConfigs());
        requestObj.put(filterAttributeName, filtersObj);
    }


    public static void prepareJsonExtendedSearchParams(JSONObject requestObj, SearchQuery searchQuery, String propertyName){
        if (searchQuery == null) {
            return;
        }

        requestObj.put(propertyName, searchQueryToJson(searchQuery));
    }

    private static void prepareFiltersConfigs(JSONObject filtersObj,
                                              List<? extends AbstractFilterConfig<? extends ParamConfig>> filterConfigs) {
        JSONArray jsonFiltersArr = new JSONArray();
        int index = 0;
        for (AbstractFilterConfig<? extends ParamConfig> filterConfig : filterConfigs) {
            prepareJsonFilter(jsonFiltersArr, filterConfig, index);
            index++;
        }
        filtersObj.put("jsonInitialFilters", jsonFiltersArr);
    }

    private static void prepareJsonFilter(JSONArray jsonFiltersArr, AbstractFilterConfig<? extends ParamConfig> filterConfig,
                                          int index) {
        JSONObject jsonInitialFilterObj = new JSONObject();
        String filterName = filterConfig.getName();
        jsonInitialFilterObj.put("name", new JSONString(filterName));
        List<? extends ParamConfig> paramConfigs = filterConfig.getParamConfigs();
        if (paramConfigs != null) {
            int paramIndex = 0;
            JSONArray jsonFilterParamArr = new JSONArray();
            for (ParamConfig paramConfig : paramConfigs) {
                prepareJsonFilterParam(jsonFilterParamArr, paramConfig, paramIndex);
                paramIndex++;
            }
            jsonInitialFilterObj.put("filterParams", jsonFilterParamArr);
        }
        jsonFiltersArr.set(index, jsonInitialFilterObj);

    }

    private static void prepareJsonFilterParam(JSONArray jsonFilterParamArr, ParamConfig paramConfig, int index) {
        JSONObject jsonFilterParamObj = new JSONObject();
        Integer name = paramConfig.getName();
        jsonFilterParamObj.put("name", new JSONNumber(name));
        putNotNullString("value", paramConfig.getValue(), jsonFilterParamObj);
        putNotNullString("type", paramConfig.getType(), jsonFilterParamObj);
        putNotNullString("timeZoneId", paramConfig.getTimeZoneId(), jsonFilterParamObj);
        jsonFilterParamObj.put("setCurrentMoment", JSONBoolean.getInstance(paramConfig.isSetCurrentMoment()));
        jsonFilterParamObj.put("setCurrentUser", JSONBoolean.getInstance(paramConfig.isSetCurrentUser()));
        jsonFilterParamObj.put("setBaseObject", JSONBoolean.getInstance(paramConfig.isSetBaseObject()));
        jsonFilterParamArr.set(index, jsonFilterParamObj);
    }

    private static void putNotNullString(String attributeName, String value, JSONObject jsonObj) {
        if (value != null) {
            jsonObj.put(attributeName, new JSONString(value));
        }
    }


    public static JSONObject searchQueryToJson(SearchQuery searchQuery) {
        JSONObject result = new JSONObject();
        result.put("targetObjectType", new JSONString(searchQuery.getTargetObjectTypes().get(0)));

        JSONArray areasValue = new JSONArray();


        int areaIndex = 0;
        for(String area : searchQuery.getAreas()){
            areasValue.set(areaIndex, new JSONString(area));
            areaIndex++;
        }


        result.put("areas", areasValue);

        JSONArray filtersValue = new JSONArray();

        int fIndex = 0;
        for(SearchFilter filter : searchQuery.getFilters()){
            filtersValue.set(fIndex, searchFilterToJson(filter));
            fIndex ++;
        }
        result.put("filters", filtersValue);

        return result;
    }

    private static JSONObject searchFilterToJson(SearchFilter filter) {
        JSONObject result = new JSONObject();
        result.put("type", new JSONString(filter.getClass().getCanonicalName()));
        result.put("name", new JSONString(filter.getFieldName()));
        JSONArray values = new JSONArray();

        if(filter instanceof BooleanSearchFilter){
            values.set(0, createJsonValueObject("value", JSONBoolean.getInstance(((BooleanSearchFilter) filter).getValue())));
        }else if(filter instanceof EmptyValueFilter){
            values.set(0, createJsonValueObject("value", JSONNull.getInstance()));
        }else if(filter instanceof CombiningFilter){
            values.set(0, createJsonValueObject("value", new JSONNumber(((CombiningFilter) filter).getOperation().ordinal())));
            JSONArray filters = new JSONArray();
            for(int fIndex = 0; fIndex< ((CombiningFilter) filter).getFilters().size(); fIndex++){
                values.set(fIndex + 1, createJsonValueObject("filter", searchFilterToJson(((CombiningFilter) filter).getFilters().get(fIndex))));
            }
        }else if(filter instanceof NegativeFilter){
            values.set(0, createJsonValueObject("value", searchFilterToJson(((NegativeFilter) filter).getBaseFilter())));
        }else if(filter instanceof NumberRangeFilter){
            Number max = ((NumberRangeFilter) filter).getMax();
            Number min = ((NumberRangeFilter) filter).getMin();

            values.set(0, createJsonValueObject("min", min != null ? new JSONNumber(min.intValue()) : JSONNull.getInstance() ));
            values.set(1, createJsonValueObject("max", max != null ? new JSONNumber(max.intValue()) : JSONNull.getInstance() ));

        }else if(filter instanceof OneOfListFilter){
            for(int fIndex = 0; fIndex< ((OneOfListFilter) filter).getValues().size(); fIndex++){
                values.set(fIndex, createJsonValueObject("value", new JSONString(((OneOfListFilter) filter).getValues().get(fIndex).get().toStringRepresentation())));
            }
        }else if(filter instanceof TextSearchFilter ){
            values.set(0, createJsonValueObject("value", new JSONString(((TextSearchFilter) filter).getText())));
        }else if(filter instanceof TimeIntervalFilter){
            Date startTime = ((TimeIntervalFilter) filter).getStartTime();
            Date endTime = ((TimeIntervalFilter) filter).getEndTime();
            values.set(0, createJsonValueObject("start", new JSONNumber(startTime != null ? startTime.getTime() : 0)));
            values.set(1, createJsonValueObject("end",  new JSONNumber(endTime != null ? endTime.getTime() : 0)));
        }
        result.put("values", values);
        return result;
    }

    private static JSONObject createJsonValueObject (String propertyName, JSONValue value){
        JSONObject result = new JSONObject();
        result.put("propertyName", new JSONString(propertyName));
        result.put("propertyValue", value);
        return result;
    }

}
