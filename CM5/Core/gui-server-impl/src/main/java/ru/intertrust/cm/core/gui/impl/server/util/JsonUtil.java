package ru.intertrust.cm.core.gui.impl.server.util;

import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.gui.form.widget.filter.ExtraParamConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.InitialParamConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.ParamConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.extra.CollectionExtraFiltersConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.extra.ExtraFilterConfig;
import ru.intertrust.cm.core.config.gui.navigation.InitialFilterConfig;
import ru.intertrust.cm.core.config.gui.navigation.InitialFiltersConfig;
import ru.intertrust.cm.core.config.gui.navigation.SortCriteriaConfig;
import ru.intertrust.cm.core.config.gui.navigation.SortCriterionConfig;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;
import ru.intertrust.cm.core.gui.model.csv.*;

import java.util.*;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 09.04.14
 *         Time: 16:15
 */
public class JsonUtil {

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
        String datePattern = jsonProperties.getDatePattern();
        properties.addProperty(CollectionColumnProperties.DATE_PATTERN, datePattern);
        String timePattern = jsonProperties.getTimePattern();
        properties.addProperty(CollectionColumnProperties.TIME_PATTERN, timePattern);
        String columnName = jsonProperties.getColumnName();
        properties.addProperty(CollectionColumnProperties.NAME_KEY, columnName);
        List<String> initialFilterValues = jsonProperties.getInitialFilterValues();
        properties.addProperty(CollectionColumnProperties.INITIAL_FILTER_VALUES, initialFilterValues);
        return properties;

    }

    public static Map<String, CollectionColumnProperties> convertToColumnPropertiesMap(
            List<JsonColumnProperties> jsonPropertiesList) {
        Map<String, CollectionColumnProperties> columnPropertiesMap = new LinkedHashMap<String, CollectionColumnProperties>();
        for (JsonColumnProperties params : jsonPropertiesList) {
            CollectionColumnProperties properties = convertToColumnProperties(params);
            String fieldName = params.getFieldName();
            columnPropertiesMap.put(fieldName, properties);
        }
        return columnPropertiesMap;
    }

    public static SortCriteriaConfig convertToSortCriteriaConfig(JsonSortCriteria sortCriteria) {
        if (sortCriteria == null) {
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

    public static InitialFiltersConfig convertToInitialFiltersConfig(JsonInitialFilters jsonInitialFilters) {
        if (jsonInitialFilters == null) {
            return null;
        }
        InitialFiltersConfig initialFiltersConfig = new InitialFiltersConfig();
        String panelState = jsonInitialFilters.getPanelState();
        initialFiltersConfig.setPanelState(panelState);
        List<JsonInitialFilter> initialFilters = jsonInitialFilters.getJsonInitialFilters();
        List<InitialFilterConfig> initialFilterConfigs = new ArrayList<InitialFilterConfig>();
        for (JsonInitialFilter initialFilter : initialFilters) {
            InitialFilterConfig initialFilterConfig = convertToInitialFilterConfig(initialFilter);
            initialFilterConfigs.add(initialFilterConfig);
        }
        initialFiltersConfig.setFilterConfigs(initialFilterConfigs);
        return initialFiltersConfig;

    }

    private static InitialFilterConfig convertToInitialFilterConfig(JsonInitialFilter initialFilter) {
        InitialFilterConfig initialFilterConfig = new InitialFilterConfig();
        String filterName = initialFilter.getName();
        initialFilterConfig.setName(filterName);
        List<JsonFilterParam> jsonFilterParams = initialFilter.getFilterParams();
        if (jsonFilterParams != null && !jsonFilterParams.isEmpty()) {
            List<InitialParamConfig> paramConfigs = new ArrayList<>();
            for (JsonFilterParam jsonFilterParam : jsonFilterParams) {
                InitialParamConfig paramConfig = new InitialParamConfig();
                fillParamConfig(paramConfig, jsonFilterParam);
                paramConfigs.add(paramConfig);
            }
            initialFilterConfig.setParamConfigs(paramConfigs);
        }
        return initialFilterConfig;
    }

    public static CollectionExtraFiltersConfig convertToCollectionExtraFiltersConfig(JsonInitialFilters jsonFilters) {
        if (jsonFilters == null) {
            return null;
        }
        CollectionExtraFiltersConfig initialFiltersConfig = new CollectionExtraFiltersConfig();

        List<JsonInitialFilter> initialFilters = jsonFilters.getJsonInitialFilters();
        List<ExtraFilterConfig> extraFilterConfigs = new ArrayList<>();
        for (JsonInitialFilter initialFilter : initialFilters) {
            ExtraFilterConfig extraFilterConfig = convertToExtraFilterConfig(initialFilter);
            extraFilterConfigs.add(extraFilterConfig);
        }
        initialFiltersConfig.setFilterConfigs(extraFilterConfigs);
        return initialFiltersConfig;

    }

    private static ExtraFilterConfig convertToExtraFilterConfig(JsonInitialFilter initialFilter) {
        ExtraFilterConfig extraFilterConfig = new ExtraFilterConfig();
        String filterName = initialFilter.getName();
        extraFilterConfig.setName(filterName);
        List<JsonFilterParam> jsonFilterParams = initialFilter.getFilterParams();
        if (jsonFilterParams != null && !jsonFilterParams.isEmpty()) {
            List<ExtraParamConfig> paramConfigs = new ArrayList<>();
            for (JsonFilterParam jsonFilterParam : jsonFilterParams) {
                ExtraParamConfig paramConfig = new ExtraParamConfig();
                fillParamConfig(paramConfig, jsonFilterParam);
                paramConfigs.add(paramConfig);
            }
            extraFilterConfig.setParamConfigs(paramConfigs);
        }
        return extraFilterConfig;
    }

    private static void fillParamConfig(ParamConfig paramConfig, JsonFilterParam jsonFilterParam) {
        paramConfig.setName(jsonFilterParam.getName());
        paramConfig.setValue(jsonFilterParam.getValue());
        paramConfig.setType(jsonFilterParam.getType());
        paramConfig.setSetBaseObject(jsonFilterParam.isSetBaseObject());
        paramConfig.setSetCurrentMoment(jsonFilterParam.isSetCurrentMoment());
        paramConfig.setSetCurrentUser(jsonFilterParam.isSetCurrentUser());
    }


    public static SearchQuery parseSearchQuesyFromJson(JsonSearchQuery jsonData){
        SearchQuery result = new SearchQuery();
        result.setTargetObjectType(jsonData.getTargetObjectType());
        result.addAreas(jsonData.getAreas());
        for(JsonSearchQueryFilter searchFilter : jsonData.getFilters()){
            result.addFilter(parseSearchFilter(searchFilter));
        }
        return result;
    }

    private static SearchFilter parseSearchFilter(JsonSearchQueryFilter filterItem) {
        SearchFilter result = null;
        String typeValue = filterItem.getType();
        String filterName = filterItem.getName();

        if(BooleanSearchFilter.class.getCanonicalName().equals(typeValue)){
            result = new BooleanSearchFilter(filterName,  filterItem.getBooleanValue("value"));
        }else if(EmptyValueFilter.class.getCanonicalName().equals(typeValue)){
            result = new EmptyValueFilter(filterName);
        }else if(CombiningFilter.class.getCanonicalName().equals(typeValue)){
            CombiningFilter.Op optValue = CombiningFilter.Op.values()[filterItem.getDoubleValue("value").intValue()];
            List<SearchFilter> filters = new ArrayList<>();

            List<JsonSearchQueryFilter> filterValues = filterItem.getListValue("filter", JsonSearchQueryFilter.class);
            for(JsonSearchQueryFilter fItem : filterValues){
                filters.add(parseSearchFilter(fItem));
            }
            result = new CombiningFilter(optValue, filters);
        }else if(NegativeFilter.class.getCanonicalName().equals(typeValue)){
           result = new NegativeFilter(parseSearchFilter(filterItem.getValue("value", JsonSearchQueryFilter.class)));
        }else if(NumberRangeFilter.class.getCanonicalName().equals(typeValue)){
            Integer min = filterItem.getLongValue("min").intValue();
            Integer max = filterItem.getLongValue("max").intValue();

            result = new NumberRangeFilter(filterName, min, max);
        }else if(OneOfListFilter.class.getCanonicalName().equals(typeValue)){
            List<ReferenceValue> referenceValues = new ArrayList<>();
            for(String reference : filterItem.getStringListValue("value")){
                referenceValues.add(new ReferenceValue(new RdbmsId(reference)));
            }
            result = new OneOfListFilter(filterName, referenceValues);
        }else if (TextSearchFilter.class.getCanonicalName().equals(typeValue)){
            result = new TextSearchFilter(filterName, filterItem.getStringValue("value"));
        }else if(TimeIntervalFilter.class.getCanonicalName().equals(typeValue)){
            Long start = filterItem.getLongValue("start");
            Long end =  filterItem.getLongValue("end");
            result = new TimeIntervalFilter(filterName, start != null && start >0 ? new Date(start) : null , end != null && end > 0 ? new Date(end) : null);
        }
        return result;
    }






}
