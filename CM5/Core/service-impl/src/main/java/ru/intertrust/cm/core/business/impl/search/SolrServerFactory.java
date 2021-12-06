package ru.intertrust.cm.core.business.impl.search;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.model.FatalException;

public class SolrServerFactory implements FactoryBean<SolrClient> {

    private static final Logger log = LoggerFactory.getLogger(SolrServerFactory.class);

    @Autowired
    private SolrSearchConfiguration solrSearchConfiguration;

    @Override
    @SuppressWarnings("unchecked")
    public SolrClient getObject() throws Exception {
        try {
            if (!solrSearchConfiguration.isSolrEnable()) {
                return new LocalOffSolrServer();
            }

            if (solrSearchConfiguration.getSolrUrl() != null && !solrSearchConfiguration.getSolrUrl().isEmpty()) {
                HttpSolrClient server = new HttpSolrClient.Builder(solrSearchConfiguration.getSolrUrl()).build();
                if (solrSearchConfiguration.getQueryTimeout() > 0) {
                    server.setSoTimeout(solrSearchConfiguration.getQueryTimeout());
                }
                return server;
            }

            log.info("Embedded solr server was removed from AF5 platform. You should specify external solr params to use indexed search.");
            return new LocalOffSolrServer();

        } catch (Exception e) {
            throw new FatalException("Error initializing search engine", e);
        }
    }

    @Override
    public Class<?> getObjectType() {
        return SolrClient.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
