package ru.intertrust.cm.core.business.impl.search;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
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
    private ImplementorFactory<SearchFilter, FilterAdapter<? extends SearchFilter>> searchFilterImplementorFactory;

    @Autowired
    private SearchConfigHelper configHelper;

    @Override
    public IdentifiableObjectCollection search(String query, String areaName, String targetCollectionName,
            int maxResults) {
        StringBuilder queryString = new StringBuilder();
        for (String field : listCommonSolrFields()) {
            queryString.append(queryString.length() == 0 ? "" : " OR ")
                    .append(field)
                    .append(":")
                    .append(protectQueryString(query));
        }
        SolrQuery solrQuery = new SolrQuery()
                .setQuery(queryString.toString())
                .addFilterQuery(SolrFields.AREA + ":\"" + areaName + "\"")
                //.addFilterQuery(SolrFields.TARGET_TYPE + ":" + configHelper.getTargetObjectType(targetCollectionName))
                .addField(SolrFields.MAIN_OBJECT_ID);
        if (maxResults > 0) {
            solrQuery.setRows(maxResults);
        }

        int fetchLimit = maxResults;
        while (true) {
            QueryResponse response = null;
            try {
                response = solrServer.query(solrQuery);
            } catch (Exception e) {
                log.error("Search error", e);
                throw new SearchException("Search error: " + e.getMessage());
            }
            if (fetchLimit <= 0) {
                fetchLimit = response.getResults().size();
            }
            IdentifiableObjectCollection result =
                    queryCollection(targetCollectionName, response.getResults(), maxResults);
            if (response.getResults().size() == fetchLimit && result.size() < maxResults) {
                // Увеличиваем размер выборки в Solr
                int factor = 10;
                if (result.size() > 0) {
                    // Пытаемся оценить процент отсева 
                    factor = 1 + response.getResults().size() / result.size();
                }
                fetchLimit *= factor;
                solrQuery.setRows(fetchLimit);
                continue;
            }
            return result;
        }
        //return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public IdentifiableObjectCollection search(SearchQuery query, String targetCollectionName, int maxResults) {
        StringBuilder queryString = new StringBuilder();
        for (SearchFilter filter : query.getFilters()) {
            @SuppressWarnings("rawtypes")
            FilterAdapter adapter = searchFilterImplementorFactory.createImplementorFor(filter.getClass());
            String filterValue = adapter.getFilterString(filter, query);
            if (filterValue == null || filterValue.isEmpty()) {
                continue;
            }

            if (queryString.length() > 0) {
                queryString.append(" AND ");
            }
            queryString.append(filterValue);
        }
        StringBuilder areas = new StringBuilder();
        for (String areaName : query.getAreas()) {
            areas.append(areas.length() == 0 ? "(" : " OR ")
                 .append("\"")
                 .append(areaName)
                 .append("\"");
        }
        areas.append(")");
        SolrQuery solrQuery = new SolrQuery()
                .setQuery(queryString.toString())
                .addFilterQuery(SolrFields.AREA + ":" + areas)
                .addFilterQuery(SolrFields.TARGET_TYPE + ":\"" + query.getTargetObjectType() + "\"")
                //.addField(SolrFields.FIELD_PREFIX + "*")
                .addField(SolrFields.MAIN_OBJECT_ID);
        if (maxResults > 0) {
            solrQuery.setRows(maxResults);
        }

        int fetchLimit = maxResults;
        while(true) {
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
            if (fetchLimit <= 0) {
                fetchLimit = response.getResults().size();
            }
            IdentifiableObjectCollection result =
                    queryCollection(targetCollectionName, response.getResults(), maxResults);
            if (response.getResults().size() == fetchLimit && result.size() < maxResults) {
                // Увеличиваем размер выборки в Solr
                int factor = 10;
                if (result.size() > 0) {
                    // Пытаемся оценить процент отсева 
                    factor = 1 + response.getResults().size() / result.size();
                }
                fetchLimit *= factor;
                solrQuery.setRows(fetchLimit);
                continue;
            }
            return result;
        }
    }

    private List<String> listCommonSolrFields() {
        List<String> langs = configHelper.getSupportedLanguages();
        if (langs.size() == 0) {
            return Arrays.asList(SolrFields.EVERYTHING, SolrFields.CONTENT);
        }
        ArrayList<String> fields = new ArrayList<>(2 * langs.size());
        for (String langId : langs) {
            StringBuilder everything = new StringBuilder()
                    .append(SolrFields.EVERYTHING)
                    .append(langId.isEmpty() ? "" : "_")
                    .append(langId);
            fields.add(everything.toString());
            StringBuilder content = new StringBuilder()
                    .append(SolrFields.CONTENT)
                    .append(langId.isEmpty() ? "" : "_")
                    .append(langId);
            fields.add(content.toString());
        }
        return fields;
    }

    private IdentifiableObjectCollection queryCollection(String collectionName, SolrDocumentList found,
            int maxResults) {
        ArrayList<ReferenceValue> ids = new ArrayList<>();
        for (SolrDocument doc : found) {
            Id id = idService.createId((String) doc.getFieldValue(SolrFields.MAIN_OBJECT_ID));
            ids.add(new ReferenceValue(id));
        }
        Filter idFilter = new IdsIncludedFilter(ids);
        return collectionsService.findCollection(collectionName, new SortOrder(), Collections.singletonList(idFilter),
                0, maxResults);
    }

    private String protectQueryString(String query) {
        //TODO Экранировать символы, нарушающие структуру запроса
        return "(" + query.replaceAll("[()\"]", "\\$0") + ")";
    }

    @Override
    public void dumpAll() {
        PrintStream out = null;
        try {
            out = new PrintStream("search-index-dump.txt", "cp1251");
            SolrQuery testQuery = new SolrQuery()
                    .setQuery("*:*")
                    .addField("*")
                    .setRows(1000000);
            QueryResponse all = solrServer.query(testQuery);
            out.println("Total " + all.getResults().getNumFound() + " document(s)");
            int i = 0;
            for (SolrDocument doc : all.getResults()) {
                out.println("==> Document #" + ++i);
                for (String field : doc.getFieldNames()) {
                    out.println("\t" + field + " = " + doc.getFieldValues(field));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
}
