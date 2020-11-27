package ru.intertrust.cm.nbrbase.gui.collections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.BooleanValue;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.GenericIdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.SortCriterion;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.BooleanFieldConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.dao.api.component.CollectionDataGenerator;
import ru.intertrust.cm.core.dao.api.component.ServerComponent;

@ServerComponent(name = "process.definition.collection")
public class ProcessDefinitionCollectionGenerator implements CollectionDataGenerator {
    @Autowired
    private ProcessService processService;

    @Autowired
    private CollectionsService collectionsService;

    @Override
    public IdentifiableObjectCollection findCollection(List<? extends Filter> filters, SortOrder sortOrder, int offset, int limit) {
        GenericIdentifiableObjectCollection result = new GenericIdentifiableObjectCollection();

        // Чтоб много раз не получать крайнюю версию сохраняем в рамках одного запроса идентификаторы крайних версий
        Map<String, Id> lastIds = new HashMap<>();

        Boolean isLastFilter = null;
        String where = "";
        List<Value> params = new ArrayList<>();
        int paramCount = 0;
        for (Filter filter : filters) {
            if (filter.getFilter().equals("byFileName")){
                where += " and lower(pd.file_name) like lower({" + paramCount + "})";
            }else if(filter.getFilter().equals("byProcessId")){
                where += " and lower(pd.process_id) like lower({" + paramCount + "})";
            }else if(filter.getFilter().equals("byProcessName")){
                where += " and lower(pd.process_name) like lower({" + paramCount + "})";
            }else if(filter.getFilter().equals("byUpdatedDate")){
                where += " and updated_date between ({" + paramCount + "} and {" + paramCount + "})";
            }else if(filter.getFilter().equals("byLogin")){
                where += " and lower(p.login) like lower({" + paramCount + "})";
            }

            if (filter.getFilter().equals("isLast")){
                isLastFilter = ((BooleanValue)filter.getCriterion(0)).get();
            }else {
                if(filter.getFilter().equals("byUpdatedDate")){
                    params.add(filter.getCriterion(0));
                    params.add(filter.getCriterion(1));
                    paramCount = paramCount + 2;
                }else {
                    params.add(filter.getCriterion(0));
                    paramCount++;
                }
            }
        }

        String sort = "";
        if (sortOrder.size() > 0){
            sort += " order by";
        }

        for (SortCriterion sortCriterion: sortOrder) {
            sort += " " + sortCriterion.getField() + " " +
                    (sortCriterion.getOrder() == SortCriterion.Order.DESCENDING ? "desc" : "");
        }

        String query = "select pd.id, pd.file_name, pd.process_id, pd.process_name, pd.version, pd.category, " +
                "s.name as status, pd.updated_date, p.login " +
                "from process_definition pd " +
                "join status s on s.id = pd.status " +
                "join person p on p.id = pd.updated_by " +
                "where lower(s.name) in ('draft', 'active')" + where + sort;

        IdentifiableObjectCollection collection = collectionsService.findCollectionByQuery(query, params, offset, limit);

        ArrayList<FieldConfig> fieldConfigs = collection.getFieldsConfiguration();
        fieldConfigs.add(new BooleanFieldConfig("last", true, false));
        result.setFieldsConfiguration(fieldConfigs);

        int rowCount = 0;
        for (int i = 0; i < collection.size(); i++) {
            Id lastId = getLastId(lastIds, collection.get(i).getString("process_id"));
            boolean isLast = collection.get(i).getId().equals(lastId);

            if (isLastFilter == null ||
                    (isLastFilter.equals(isLast))) {
                result.setId(rowCount, collection.get(i).getId());
                result.set("file_name", rowCount, collection.get(i).getValue("file_name"));
                result.set("process_id", rowCount, collection.get(i).getValue("process_id"));
                result.set("process_name", rowCount, collection.get(i).getValue("process_name"));
                result.set("version", rowCount, collection.get(i).getValue("version"));
                result.set("category", rowCount, collection.get(i).getValue("category"));
                result.set("status", rowCount, collection.get(i).getValue("status"));
                result.set("last", rowCount, new BooleanValue(isLast));
                result.set("updated_date", rowCount, collection.get(i).getValue("created_date"));
                result.set("login", rowCount, collection.get(i).getValue("login"));

                rowCount++;
            }
        }
        return result;
    }

    private Id getLastId(Map<String, Id> lastIds, String name) {
        Id result = lastIds.get(name);
        if (result == null){
            result = processService.getLastProcessDefinitionId(name);
            if (result != null){
                lastIds.put(name, result);
            }
        }
        return result;
    }

    @Override
    public int findCollectionCount(List<? extends Filter> filterValues) {
        return 0;
    }

}
