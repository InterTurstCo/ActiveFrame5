package ru.intertrust.cm.core.gui.impl.server.widget;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.SearchService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.gui.navigation.SortCriteriaConfig;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.GuiServerHelper;
import ru.intertrust.cm.core.gui.impl.server.util.JsonUtil;
import ru.intertrust.cm.core.gui.impl.server.util.SortOrderBuilder;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;
import ru.intertrust.cm.core.gui.model.csv.JsonColumnProperties;
import ru.intertrust.cm.core.gui.model.csv.JsonCsvRequest;
import ru.intertrust.cm.core.gui.model.csv.JsonSortCriteria;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 09.04.14
 *         Time: 16:15
 */
@Controller
public class JsonExportToCsv {

    @Autowired
    CollectionsService collectionsService;

    @Autowired
    SearchService searchService;

    @Autowired
    ConfigurationService configurationService;

    private static final String DEFAULT_ENCODING = "ANSI-1251";

    @ResponseBody
    @RequestMapping(value = "json-export-to-csv", method = RequestMethod.POST )
    public void generateCsv(@ModelAttribute("json") String stringCsvRequest,HttpServletResponse response)
            throws IOException, ParseException, ServletException {
        ObjectMapper mapper = new ObjectMapper();
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
        }
        List<JsonColumnProperties> columnParams = csvRequest.getColumnProperties();
        Map<String, CollectionColumnProperties> columnPropertiesMap = JsonUtil.convertToColumnPropertiesMap(columnParams);
        List<Filter> filters = JsonUtil.prepareFilters(columnParams, columnPropertiesMap);
        IdentifiableObjectCollection collections;
        int rowCount = csvRequest.getRowCount();
        if (simpleSearchQuery.isEmpty()){
            collections = collectionsService.findCollection(collectionName, sortOrder, filters, 0, rowCount);

        }   else {
            collections = searchService.search(simpleSearchQuery, area, collectionName, 1000);
        }

        response.setHeader("Content-Disposition", "attachment; filename=" + collectionName+".csv");
        response.setContentType("application/csv");
        response.addHeader("Access-Control-Allow-Origin","");
        OutputStream resOut = response.getOutputStream();
        OutputStream buffer = new BufferedOutputStream(resOut);
        OutputStreamWriter writer = new OutputStreamWriter(buffer, Charset.forName(DEFAULT_ENCODING));

        //Создание заголовков таблицы
        printHeader(writer, columnPropertiesMap);
        writer.append(" \n");
        printBody(writer, collections, columnPropertiesMap);
        writer.close();

    }

    private void  printHeader(OutputStreamWriter writer,  Map<String, CollectionColumnProperties> columnPropertiesMap)
            throws IOException {
             Set<String> fieldNames = columnPropertiesMap.keySet();
        for(String fieldName : fieldNames) {
            CollectionColumnProperties properties = columnPropertiesMap.get(fieldName);
            String columnName = (String) properties.getProperty(CollectionColumnProperties.NAME_KEY);
            writer.append(columnName+";");
        }
    }

    private void printBody(OutputStreamWriter writer, IdentifiableObjectCollection collections,
                           Map<String, CollectionColumnProperties> columnPropertiesMap) throws IOException {
        for (int row = 0; row < collections.size(); row++) {
            IdentifiableObject identifiableObject = collections.get(row);
            Map<String, Value> rowValues = getRowValues(identifiableObject, columnPropertiesMap) ;
            Set<String> fields = rowValues.keySet();
            for (String field : fields) {
                    Value value = rowValues.get(field);

                    if (value.get() == null) {
                        writer.append(" ");
                    } else {
                        writer.append(value.get().toString());
                    }
                    writer.append(";");

            }
            writer.append("\n");
        }

        writer.flush();

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
                value = identifiableObject.getValue(field.toLowerCase());
            }
            if (value != null && value.get() != null) {
                DateFormat dateFormat;
                Calendar calendar;
                String timeZoneId;
                final String pattern =
                        (String) columnPropertiesMap.get(field).getProperty(CollectionColumnProperties.PATTERN_KEY);
                switch (value.getFieldType()) {
                    case DATETIMEWITHTIMEZONE:
                        final DateTimeWithTimeZone dateTimeWithTimeZone = (DateTimeWithTimeZone) value.get();
                        calendar = GuiServerHelper.dateTimeWithTimezoneToCalendar(dateTimeWithTimeZone);
                        dateFormat = new SimpleDateFormat(pattern);
                        dateFormat.setTimeZone(TimeZone.getTimeZone(
                                dateTimeWithTimeZone.getTimeZoneContext().getTimeZoneId()));
                        value = new StringValue(dateFormat.format(calendar.getTime()));
                        break;
                    case DATETIME:
                        timeZoneId = GuiContext.get().getUserInfo().getTimeZoneId();
                        final DateTimeValue dateTimeValue = (DateTimeValue) value;
                        dateFormat = new SimpleDateFormat(pattern);
                        dateFormat.setTimeZone(TimeZone.getTimeZone(timeZoneId));
                        value = new StringValue(dateFormat.format(dateTimeValue.get()));
                        break;
                    case TIMELESSDATE:
                        final TimelessDate timelessDate = (TimelessDate) value.get();
                        timeZoneId = GuiContext.get().getUserInfo().getTimeZoneId();
                        calendar = GuiServerHelper.timelessDateToCalendar(timelessDate, GuiServerHelper.GMT_TIME_ZONE);
                        dateFormat = new SimpleDateFormat(pattern);
                        dateFormat.setTimeZone(TimeZone.getTimeZone(timeZoneId));
                        value = new StringValue(dateFormat.format(calendar.getTime()));
                }
            }

            values.put(field, value);

        }
        return values;

    }
}

