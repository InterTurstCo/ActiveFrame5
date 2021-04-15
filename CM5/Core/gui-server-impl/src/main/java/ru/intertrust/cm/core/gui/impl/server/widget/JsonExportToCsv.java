package ru.intertrust.cm.core.gui.impl.server.widget;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.SearchService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.gui.form.widget.filter.extra.CollectionExtraFiltersConfig;
import ru.intertrust.cm.core.config.gui.navigation.InitialFiltersConfig;
import ru.intertrust.cm.core.config.gui.navigation.SortCriteriaConfig;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.GuiServerHelper;
import ru.intertrust.cm.core.gui.api.server.plugin.FilterBuilder;
import ru.intertrust.cm.core.gui.api.server.plugin.SortOrderHelper;
import ru.intertrust.cm.core.gui.impl.server.util.CollectionPluginHelper;
import ru.intertrust.cm.core.gui.impl.server.util.JsonUtil;
import ru.intertrust.cm.core.gui.impl.server.util.SortOrderBuilder;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;
import ru.intertrust.cm.core.gui.model.csv.*;
import ru.intertrust.cm.core.gui.model.filters.ComplexFiltersParams;
import ru.intertrust.cm.core.gui.model.filters.InitialFiltersParams;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 09.04.14
 *         Time: 16:15
 */
@Controller
public class JsonExportToCsv {

    @Autowired
    private CollectionsService collectionsService;

    @Autowired
    private SearchService searchService;

    @Autowired
    private FilterBuilder filterBuilder;

    @Autowired
    private SortOrderHelper sortOrderHelper;

    private static final String DEFAULT_ENCODING = "ANSI-1251";
    private static final int CHUNK_SIZE = 1000;



    @ResponseBody
    @RequestMapping(value = "json-extended-search-export-to-csv", method = RequestMethod.POST)
    public void extendedSearchGenerateCsv(@ModelAttribute("json") String stringCsvRequest, HttpServletResponse response)
            throws IOException, ParseException, ServletException {

        ObjectMapper mapper = new ObjectMapper();

        JsonExtendedSearchCSVRequest csvRequest = mapper.readValue(stringCsvRequest, JsonExtendedSearchCSVRequest.class);
        String collectionName = csvRequest.getCollectionName();
        String simpleSearchQuery = csvRequest.getSimpleSearchQuery();
        String area = csvRequest.getSimpleSearchArea();
        boolean ascend = csvRequest.isAscend();
        String columnName = csvRequest.getSortedFieldName();
        SortOrder sortOrder = null;
        if (columnName != null) {
            JsonSortCriteria sortCriteria = csvRequest.getSortCriteria();
            SortCriteriaConfig sortCriteriaConfig = JsonUtil.convertToSortCriteriaConfig(sortCriteria);
            sortOrder = SortOrderBuilder.getSortOrder(sortCriteriaConfig, columnName, ascend);
        } else {
            sortOrder = sortOrderHelper.buildSortOrderByIdField(collectionName);
        }
        List<JsonColumnProperties> columnParams = csvRequest.getColumnProperties();
        Map<String, CollectionColumnProperties> columnPropertiesMap = JsonUtil.convertToColumnPropertiesMap(columnParams);
        JsonInitialFilters jsonInitialFilters = csvRequest.getJsonInitialFilters();
        JsonInitialFilters jsonHierarchicalFilters = csvRequest.getJsonHierarchicalFilters();
        List<Filter> filters = prepareFilters(columnPropertiesMap, jsonInitialFilters, jsonHierarchicalFilters);

        response.setHeader("Content-Disposition", "attachment; filename=" + collectionName + ".csv");
        response.setContentType("application/csv");
        response.addHeader("Access-Control-Allow-Origin", "");
        OutputStream resOut = response.getOutputStream();
        OutputStream buffer = new BufferedOutputStream(resOut);
        OutputStreamWriter writer = new OutputStreamWriter(buffer, Charset.forName(DEFAULT_ENCODING));

        //Создание заголовков таблицы
        printHeader(writer, columnPropertiesMap);
        writer.append(" \n");
        IdentifiableObjectCollection collection =  searchService.search(JsonUtil.parseSearchQuesyFromJson(csvRequest.getJsonSearchQuery()),
                    collectionName , filters,  CHUNK_SIZE);
        final SortOrder sortOrderProperty = sortOrder;
        printBody(writer, collection, columnPropertiesMap, new Comparator<Map<String, Value>>() {
            @Override
            public int compare(Map<String, Value> o1, Map<String, Value> o2) {
                Value v1 = o1.get(sortOrderProperty.get(0).getField());
                Value v2 = o2.get(sortOrderProperty.get(0).getField());
                return  Value.getComparator(sortOrderProperty.get(0).getOrder().equals(SortCriterion.Order.ASCENDING), true).compare(v1, v2);
            }
        });
    }

    @ResponseBody
    @RequestMapping(value = "json-export-to-csv", method = RequestMethod.POST)
    public void generateCsv(@ModelAttribute("json") String stringCsvRequest,  HttpServletRequest request, HttpServletResponse response)
            throws IOException, ParseException, ServletException {

        ObjectMapper mapper = new ObjectMapper();
        if(stringCsvRequest.equals(StringUtils.EMPTY) && request.getAttribute("Json Payload")!=null){
            stringCsvRequest = java.net.URLDecoder.decode(request.getAttribute("Json Payload")
                .toString()
                .split("=")[1], "UTF-8");
        }

        JsonCsvRequest csvRequest = mapper.readValue(stringCsvRequest, JsonCsvRequest.class);
        String collectionName = csvRequest.getCollectionName();
        String simpleSearchQuery = csvRequest.getSimpleSearchQuery();
        String area = csvRequest.getSimpleSearchArea();
        boolean ascend = csvRequest.isAscend();
        String columnName = csvRequest.getSortedFieldName();
        SortOrder sortOrder = null;
        if (columnName != null) {
            JsonSortCriteria sortCriteria = csvRequest.getSortCriteria();
            SortCriteriaConfig sortCriteriaConfig = JsonUtil.convertToSortCriteriaConfig(sortCriteria);
            sortOrder = SortOrderBuilder.getSortOrder(sortCriteriaConfig, columnName, ascend);
        } else {
            sortOrder = sortOrderHelper.buildSortOrderByIdField(collectionName);
        }
        List<JsonColumnProperties> columnParams = csvRequest.getColumnProperties();
        Map<String, CollectionColumnProperties> columnPropertiesMap = JsonUtil.convertToColumnPropertiesMap(columnParams);
        JsonInitialFilters jsonInitialFilters = csvRequest.getJsonInitialFilters();
        JsonInitialFilters jsonHierarchicalFilters = csvRequest.getJsonHierarchicalFilters();
        JsonSelectedIdsFilter jsonSelectedIdsFilter = csvRequest.getJsonSelectedIdsFilter();

        List<Filter> filters = prepareFilters(columnPropertiesMap, jsonInitialFilters, jsonHierarchicalFilters);

        response.setHeader("Content-Disposition", "attachment; filename=" + collectionName + ".csv");
        response.setContentType("application/csv");
        response.addHeader("Access-Control-Allow-Origin", "");
        OutputStream resOut = response.getOutputStream();
        OutputStream buffer = new BufferedOutputStream(resOut);
        OutputStreamWriter writer = new OutputStreamWriter(buffer, Charset.forName(DEFAULT_ENCODING));

        //Создание заголовков таблицы
        printHeader(writer, columnPropertiesMap);
        writer.append(" \n");
        if (simpleSearchQuery.isEmpty()) {
            // Добавление фильтра по id, если есть
            List<ReferenceValue> ids = jsonSelectedIdsFilter != null ? jsonSelectedIdsFilter.getFilterIds() : null;
            IdsIncludedFilter idFilter = ids != null && !ids.isEmpty() ? new IdsIncludedFilter(ids) : null;
            if (idFilter != null) {
                filters.add(idFilter);
            }
            printBodyBatch(writer, collectionName, sortOrder, filters, columnPropertiesMap);
        } else {
            IdentifiableObjectCollection collection = searchService.search(simpleSearchQuery, area, collectionName, CHUNK_SIZE);
            printBody(writer, collection, columnPropertiesMap, null);
        }

        writer.close();

    }

    private void printHeader(OutputStreamWriter writer, Map<String, CollectionColumnProperties> columnPropertiesMap)
            throws IOException {
        Set<String> fieldNames = columnPropertiesMap.keySet();
        for (String fieldName : fieldNames) {
            CollectionColumnProperties properties = columnPropertiesMap.get(fieldName);
            String columnName = (String) properties.getProperty(CollectionColumnProperties.NAME_KEY);
            writer.append(columnName + ";");
        }
    }

    private void printBodyBatch(OutputStreamWriter writer, String collectionName, SortOrder sortOrder, List<Filter> filters,
                                Map<String, CollectionColumnProperties> columnPropertiesMap) throws IOException {
        int offset = 0;
        IdentifiableObjectCollection collection = collectionsService.findCollection(collectionName, sortOrder, filters,
                offset, CHUNK_SIZE);
        while (collection.size() != 0) {
            printBody(writer, collection, columnPropertiesMap, null);
            offset += CHUNK_SIZE;
            collection = collectionsService.findCollection(collectionName, sortOrder, filters,
                    offset, CHUNK_SIZE);
        }

    }

    private void printBody(OutputStreamWriter writer, IdentifiableObjectCollection collection,
                           Map<String, CollectionColumnProperties> columnPropertiesMap, Comparator rowComparator) throws IOException {

        List<Map<String, Value>> rows = new ArrayList<>();
        for (int row = 0; row < collection.size(); row++) {
            IdentifiableObject identifiableObject = collection.get(row);
            rows.add( getRowValues(identifiableObject, columnPropertiesMap));
        }

        if(rowComparator != null){
            Collections.sort(rows, rowComparator);
        }

        printBody(writer, rows);
    }



    private void printBody(OutputStreamWriter writer, List<Map<String, Value>> rows) throws IOException{
        for(Map<String, Value> rowValues : rows){
            Set<String> fields = rowValues.keySet();
            for (String field : fields) {
                Value value = rowValues.get(field);
                writer.append("\"");
                if (value == null || value.get() == null) {
                    writer.append(" ");
                } else {
                    writer.append(value.get().toString().replaceAll("\"", "\"\""));
                }
                writer.append("\"").append(";");
            }
            writer.append("\n");
        }
        writer.flush();
    }

    public List<Filter> prepareFilters( Map<String, CollectionColumnProperties> columnPropertiesMap,
                                        JsonInitialFilters jsonInitialFilters,
                                        JsonInitialFilters jsonHierarchicalFilters) throws ParseException {
        List<Filter> filters = new ArrayList<>();
        List<String> excludedFilterFields = new ArrayList<>();

        InitialFiltersConfig initialFiltersConfig = JsonUtil.convertToInitialFiltersConfig(jsonInitialFilters);
        CollectionExtraFiltersConfig hierarchicalFiltersConfig = JsonUtil.
                convertToCollectionExtraFiltersConfig(jsonHierarchicalFilters);
        Map<String, CollectionColumnProperties> filterNameColumnPropertiesMap = CollectionPluginHelper
                .getFilterNameColumnPropertiesMap(columnPropertiesMap, initialFiltersConfig);
        InitialFiltersParams simpleFiltersParams = new InitialFiltersParams(excludedFilterFields, filterNameColumnPropertiesMap);
        filterBuilder.prepareInitialFilters(initialFiltersConfig, simpleFiltersParams, filters);
        ComplexFiltersParams hierarchyFiltersParams = new ComplexFiltersParams();
        filterBuilder.prepareExtraFilters(hierarchicalFiltersConfig, hierarchyFiltersParams, filters);
        return filters;
    }

    private Map<String, Value> getRowValues(IdentifiableObject identifiableObject,
                                            Map<String, CollectionColumnProperties> columnPropertiesMap) {

        LinkedHashMap<String, Value> values = new LinkedHashMap<>();
        Set<String> fields = columnPropertiesMap.keySet();
        for (String field : fields) {
            Value value;
            if ("id".equalsIgnoreCase(field)) {
                value = new StringValue(identifiableObject.getId().toStringRepresentation());
            } else {
                value = identifiableObject.getValue(Case.toLower(field));
            }
            if (value != null && value.get() != null) {
                DateFormat dateFormat;
                Calendar calendar;
                String timeZoneId;
                String datePattern =
                        (String) columnPropertiesMap.get(field).getProperty(CollectionColumnProperties.DATE_PATTERN);
                String timePattern = (String) columnPropertiesMap.get(field).getProperty(CollectionColumnProperties.TIME_PATTERN);
                String dateTimePattern = GuiServerHelper.prepareDatePattern(datePattern, timePattern);
                switch (value.getFieldType()) {
                    case DATETIMEWITHTIMEZONE:
                        final DateTimeWithTimeZone dateTimeWithTimeZone = (DateTimeWithTimeZone) value.get();
                        calendar = GuiServerHelper.dateTimeWithTimezoneToCalendar(dateTimeWithTimeZone);
                        dateFormat = new SimpleDateFormat(dateTimePattern);
                        dateFormat.setTimeZone(TimeZone.getTimeZone(
                                dateTimeWithTimeZone.getTimeZoneContext().getTimeZoneId()));
                        value = new StringValue(dateFormat.format(calendar.getTime()));
                        break;
                    case DATETIME:
                        timeZoneId = GuiContext.get().getUserInfo().getTimeZoneId();
                        final DateTimeValue dateTimeValue = (DateTimeValue) value;
                        dateFormat = new SimpleDateFormat(dateTimePattern);
                        dateFormat.setTimeZone(TimeZone.getTimeZone(timeZoneId));
                        value = new StringValue(dateFormat.format(dateTimeValue.get()));
                        break;
                    case TIMELESSDATE:
                        final TimelessDate timelessDate = (TimelessDate) value.get();
                        timeZoneId = GuiContext.get().getUserInfo().getTimeZoneId();
                        calendar = GuiServerHelper.timelessDateToCalendar(timelessDate, GuiServerHelper.GMT_TIME_ZONE);
                        dateFormat = new SimpleDateFormat(datePattern);
                        dateFormat.setTimeZone(TimeZone.getTimeZone(timeZoneId));
                        value = new StringValue(dateFormat.format(calendar.getTime()));
                }
            }

            values.put(field, value);

        }
        return values;

    }
}

