package ru.intertrust.cm.core.business.impl.search;

import java.util.ArrayList;
import java.util.Collections;

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
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.IdsIncludedFilter;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.SearchFilter;
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

    @Autowired
    private ImplementorFactory<SearchFilter, FilterAdapter> searchFilterImplementorFactory;

    @Autowired
    private SearchConfigHelper configHelper;

    @Override
    public IdentifiableObjectCollection search(String query, String areaName, String targetCollectionName,
            int maxResults) {
        SolrQuery solrQuery = new SolrQuery()
                .setQuery(SolrFields.EVERYTHING + ":" + protectQueryString(query))
                .addFilterQuery(SolrFields.AREA + ":" + areaName)
                //.addFilterQuery("cm_type:" + configHelper.getTargetObjectType(targetCollectionName))
                .addField(SolrFields.OBJECT_ID);

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
    public IdentifiableObjectCollection search(SearchQuery query, String targetCollectionName, int maxResults) {
        StringBuilder queryString = new StringBuilder();
        for (SearchFilter filter : query.getFilters()) {
            FilterAdapter adapter = searchFilterImplementorFactory.createImplementorFor(filter.getClass());
            String filterValue = adapter.getFilterValue(filter);
            if (filterValue == null || filterValue.trim().isEmpty()) {
                continue;
            }

            if (queryString.length() > 0) {
                queryString.append(" AND ");
            }
            if (SearchFilter.EVERYWHERE.equalsIgnoreCase(filter.getFieldName())) {
                queryString.append(SolrFields.EVERYTHING);
            } else {
                queryString.append(SolrFields.FIELD_PREFIX).append(filter.getFieldName().toLowerCase());
            }
            queryString.append(":").append(filterValue);
        }
        StringBuilder areas = new StringBuilder();
        for (String areaName : query.getAreas()) {
            areas.append(areas.length() == 0 ? "(" : " OR ")
                 .append(areaName);
        }
        areas.append(")");
        SolrQuery solrQuery = new SolrQuery()
                .setQuery(queryString.toString())
                .addFilterQuery(SolrFields.AREA + ":" + areas)
                //.addFilterQuery("cm_type:" + configHelper.getTargetObjectType(targetCollectionName))
                .addField(SolrFields.FIELD_PREFIX + "*")
                .addField(SolrFields.OBJECT_ID);

        QueryResponse response = null;
        try {
            response = solrServer.query(solrQuery);
            if (log.isDebugEnabled()) {
                log.debug("Response: " + response);
            }
        } catch (Exception e) {
            log.error("Search error", e);
            throw new SearchException("Search error: " + e.getMessage());
        }
        return queryCollection(targetCollectionName, response);
    }

    private IdentifiableObjectCollection queryCollection(String collectionName, QueryResponse response) {
        ArrayList<ReferenceValue> ids = new ArrayList<>();
        SolrDocumentList found = response.getResults();
        if (found.size() == 0) {
            return new GenericIdentifiableObjectCollection();   //*****
        }
        for (SolrDocument doc : found) {
            Id id = idService.createId((String) doc.getFieldValue(SolrFields.OBJECT_ID));
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
