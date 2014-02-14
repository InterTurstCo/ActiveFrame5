package ru.intertrust.cm.core.gui.impl.server.widget;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.SearchService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionColumnConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionViewConfig;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created with IntelliJ IDEA.
 * User: lvov
 * Date: 14.01.14
 * Time: 10:20
 * To change this template use File | Settings | File Templates.
 */
@Controller
public class ExportToCsv {

    @Autowired
    CollectionsService collectionsService;

    @Autowired
    SearchService searchService;

    @Autowired
    ConfigurationService configurationService;

    public static final int START_ROW_COUNT = 1000;

    private static final String DEFAULT_ENCODING = "ANSI-1251";

    @ResponseBody
    @RequestMapping(value = "export-to-csv")
    public void getCollection( HttpServletRequest request,
        HttpServletResponse response) throws IOException {

        String collectionName =  request.getParameter("collectionName");
        String simpleSearchQuery =  request.getParameter("simpleSearchQuery");

        String area = request.getParameter("searchArea");
        String sortDirection = request.getParameter("Sortable");
        String columnName = request.getParameter("ColumnName");
        String filterQuery = request.getParameter("filterName");
        String collectionViewName = request.getParameter("collectionView");

        ArrayList<Filter>filters = new ArrayList<Filter>();
        SortOrder sortOrder = new SortOrder();
        ArrayList<Boolean> isPrint = new ArrayList<Boolean>();


        CollectionViewConfig collectionViewConfig = findRequiredCollectionView(collectionViewName);
        List<CollectionColumnConfig> collectionColumnConfigs = collectionViewConfig.getCollectionDisplayConfig().getColumnConfig();

        if (sortDirection !=null){
            createSortOrder(sortDirection, columnName, sortOrder);
        }

        if (filterQuery != null && filterQuery.length() > 0){
           // System.out.println("filter "+filterQuery);
             createFilterList(filterQuery,filters);
        }

        IdentifiableObjectCollection collections;

        if (simpleSearchQuery != null){
            collections = searchService.search(simpleSearchQuery, area, collectionName, 1000);

        }   else {
            collections = collectionsService.findCollection(collectionName, sortOrder, filters, 0, START_ROW_COUNT);
        }

        response.setHeader("Content-Disposition", "attachment; filename=" + collectionName+".csv");

        OutputStream resOut = response.getOutputStream();
        OutputStream buffer = new BufferedOutputStream(resOut);
        OutputStreamWriter writer = new OutputStreamWriter(buffer, Charset.forName(DEFAULT_ENCODING));


       //Создание заголовков таблицы
       isPrint.add(false);
       next:
        for (int i = 1; i <collections.getFieldsConfiguration().size(); i++){
            String tmp = collections.getFieldsConfiguration().get(i).getName();
            for (int viewCollectionField = 1; viewCollectionField <collectionColumnConfigs.size(); viewCollectionField++ ) {
                 if (tmp.equals(collectionColumnConfigs.get(viewCollectionField).getField())){
                    writer.append(collectionColumnConfigs.get(viewCollectionField).getName()+";");
                    isPrint.add(true);
                    continue next;
            }

            }
            isPrint.add(false);
        }

        writer.append(" \n");
        print(writer,  collections,  isPrint);
        int sumRowCount = START_ROW_COUNT;
        while(collections.size() == START_ROW_COUNT){
            collections = collectionsService.findCollection(collectionName, sortOrder, filters, sumRowCount, START_ROW_COUNT);
            print(writer,  collections,  isPrint);
            sumRowCount += collections.size();
        }


        writer.close();
    }

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    private void print(OutputStreamWriter writer, IdentifiableObjectCollection collections, ArrayList<Boolean> isPrint) throws IOException {
        for (int row = 0; row < collections.size(); row++){
            for (int col = 1; col < collections.getFieldsConfiguration().size(); col++){
                if (isPrint.get(col)){
                    Value value = collections.get(col, row);
                    if (value.get() == null) {
                        writer.append(" ");
                    } else if (value.get() instanceof Date) {
                        writer.append(dateFormat.format((Date)value.get()));
                    } else {
                        writer.append(value.get().toString());
                    }
                    writer.append(";");
                }
            }
            writer.append("\n");
        }

        writer.flush();

    }



    private void createSortOrder(String sortDirection, String columnName, SortOrder sortOrder){
                SortCriterion.Order order;
                if (sortDirection.equals("true")){
                    order = SortCriterion.Order.ASCENDING;
                } else {
                    order = SortCriterion.Order.DESCENDING;
                }
                sortOrder.add(new SortCriterion(columnName, order));
    }

    private void createFilterList(String filterQuery, ArrayList filterList){

        String []arr = filterQuery.split(":");
        System.out.println("size");
                for (int i = 0; i <arr.length; i = i+2){
                Filter filter = new Filter();
                Value value;
                filter.setFilter(arr[i]);
                value = new StringValue("%"+arr[i+1]+"%");
                filter.addCriterion(0,value);
                filterList.add(filter);
                }


    }

    private CollectionViewConfig findRequiredCollectionView(String collectionViewName) {
        return configurationService.getConfig(CollectionViewConfig.class, collectionViewName);
    }

}
