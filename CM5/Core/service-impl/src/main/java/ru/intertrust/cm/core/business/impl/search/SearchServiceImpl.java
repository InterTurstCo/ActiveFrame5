package ru.intertrust.cm.core.business.impl.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.IdService;
import ru.intertrust.cm.core.business.api.SearchService;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.GenericIdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdBasedFilter;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.IdsIncludedFilter;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.model.SearchException;

@Stateless(name = "SearchService")
@Local(SearchService.class)
@Remote(SearchService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class SearchServiceImpl implements SearchService, SearchService.Remote {

    protected Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private SolrServer solrServer;

    @Autowired
    private CollectionsService collectionsService;

    @Autowired
    private IdService idService;

    @Override
    public IdentifiableObjectCollection search(String query, String areaName, String targetCollectionName,
            int maxResults) {
        SolrQuery testQuery = new SolrQuery()
                .setQuery("*:*")
                .addField("cm_id")
                .addField("cm_text")
                .addField("cm_f_*");
        try {
            QueryResponse testResponse = solrServer.query(testQuery);
            System.out.println(testResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }

        SolrQuery solrQuery = new SolrQuery();
        //StringBuilder queryBuilder = new Str
        solrQuery.setQuery("cm_text:" + protectQueryString(query));
        solrQuery.addField("cm_id");
        QueryResponse response = null;
        try {
            response = solrServer.query(solrQuery);
        } catch (Exception e) {
            log.error("Search error", e);
            throw new SearchException("Search error: " + e.getMessage());
        }
        return queryCollection(targetCollectionName, response);
    }

    @Override
    public IdentifiableObjectCollection search(SearchQuery query, int maxResults) {
        // TODO Auto-generated method stub
        return null;
    }

    private IdentifiableObjectCollection queryCollection(String collectionName, QueryResponse response) {
        ArrayList<ReferenceValue> ids = new ArrayList<>();
        SolrDocumentList found = response.getResults();
        if (found.size() == 0) {
            return new GenericIdentifiableObjectCollection();   //*****
        }
        for (SolrDocument doc : found) {
            //List<String> values = (List<String>) doc.getFieldValue("cm_id");
            //Id id = idService.createId(values.get(0));
            Id id = idService.createId((String) doc.getFieldValue("cm_id"));
            ids.add(new ReferenceValue(id));
        }
        Filter idFilter = new IdsIncludedFilter(ids);
        return collectionsService.findCollection(collectionName, new SortOrder(), Collections.singletonList(idFilter));
    }

    private String protectQueryString(String query) {
        //TODO Экранировать символы, нарушающие структуру запроса
        return query;
    }
}
