package ru.intertrust.cm.core.business.impl.search;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.apache.solr.client.solrj.SolrServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.intertrust.cm.core.business.api.SearchService;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;

@Stateless(name = "SearchService")
@Local(SearchService.class)
@Remote(SearchService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class SearchServiceImpl implements SearchService, SearchService.Remote {

    @Autowired
    private SolrServer solrServer;

    @Override
    public IdentifiableObjectCollection search(String query, String areaName, String objectType, int maxResults) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IdentifiableObjectCollection search(SearchQuery query, int maxResults) {
        // TODO Auto-generated method stub
        return null;
    }

}
