package ru.intertrust.cm.remoteclient.search;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.SearchService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.remoteclient.ClientBase;
import ru.intertrust.cm.remoteclient.crud.test.TestCrudService;

import java.util.Collections;

public class TestSearchService extends ClientBase {

    public static void main(String[] args) {
        try {
            TestSearchService test = new TestSearchService();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void execute(String[] args) throws Exception {
        super.execute(args);

        CrudService crudService = (CrudService) getService(
                "CrudServiceImpl", CrudService.Remote.class);

        SearchService searchService = (SearchService) getService(
                "SearchService", SearchService.Remote.class);

        for (int i=0; i<10; i++) {
            DomainObject person = crudService.createDomainObject("person");
            person.setString("login", "person-" + System.currentTimeMillis());
            person.setString("firstname", "name-" + System.currentTimeMillis());
            crudService.save(person);
        }

        SearchQuery query = new SearchQuery();
        query.setTargetObjectType("person");
        query.addArea("persons");
        query.addFilter(new TextSearchFilter("login", "person*"));
        IdentifiableObjectCollection collection = searchService.searchAndQuery(query, "select id from person", 10);
        assertTrue("Search 1", collection.size() > 0);

        collection = searchService.searchAndQuery(query,
                "select id from person where firstname like {0}",
                Collections.singletonList(new StringValue("name%")),
                10);
        assertTrue("Search 2", collection.size() > 0);
    }
}
