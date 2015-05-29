package ru.intertrust.cm.remoteclient.search;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.SearchService;
import ru.intertrust.cm.remoteclient.ClientBase;

public class DampSearch extends ClientBase {
    public static void main(String[] args) {
        try {
            DampSearch test = new DampSearch();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void execute(String[] args) throws Exception {
        super.execute(args);
        
        SearchService searchService = (SearchService) getService(
                "SearchService", SearchService.Remote.class);
        
        searchService.dumpAll();
        
    }
}
