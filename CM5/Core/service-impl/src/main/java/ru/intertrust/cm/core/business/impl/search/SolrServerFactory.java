package ru.intertrust.cm.core.business.impl.search;

import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.util.NamedList;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.tools.DynamicLoadClassFactory;
import ru.intertrust.cm.core.util.SpringApplicationContext;

import java.io.IOException;

public class SolrServerFactory implements FactoryBean<SolrServer> {

    @Autowired
    private SolrSearchConfiguration solrSearchConfiguration;

    private static final String SOLR_FACTORY_CLASS =
            "ru.intertrust.cm.core.business.impl.solr.EmbeddedSolrServerFactory";

    @Override
    @SuppressWarnings("unchecked")
    public SolrServer getObject() throws Exception {
        try {
            if(solrSearchConfiguration.isSolrEnable()){
                if (solrSearchConfiguration.getSolrUrl() != null && !solrSearchConfiguration.getSolrUrl().isEmpty()) {
                    HttpSolrServer server = new HttpSolrServer(solrSearchConfiguration.getSolrUrl());
                    if (solrSearchConfiguration.getQueryTimeout() > 0) {
                        server.setSoTimeout(solrSearchConfiguration.getQueryTimeout());
                    }
                    return server;
                }

                DynamicLoadClassFactory<SolrServer> factory =
                        (DynamicLoadClassFactory<SolrServer>) Class.forName(SOLR_FACTORY_CLASS).newInstance();
                AutowireCapableBeanFactory beanFactory =
                        SpringApplicationContext.getContext().getAutowireCapableBeanFactory();
                beanFactory.autowireBean(factory);
                beanFactory.initializeBean(factory, "solrEmbeddedServerFactory");
                return factory.createInstance();
            }else{
               return new LocalOffSolrServer();
            }
        } catch (Exception e) {
            throw new FatalException("Error initializing search engine", e);
        }
    }

    @Override
    public Class<?> getObjectType() {
        return SolrServer.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
