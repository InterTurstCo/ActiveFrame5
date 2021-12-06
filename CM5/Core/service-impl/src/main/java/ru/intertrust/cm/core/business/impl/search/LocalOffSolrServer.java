package ru.intertrust.cm.core.business.impl.search;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.util.NamedList;

import java.io.IOException;

/**
 * Created by Vitaliy.Orlov on 03.07.2019.
 */
public class LocalOffSolrServer extends SolrClient {
    @Override
    public NamedList<Object> request(SolrRequest request, String collection) throws SolrServerException, IOException {
        return new NamedList<>();
    }

    @Override
    public void close() throws IOException {
    }
}
