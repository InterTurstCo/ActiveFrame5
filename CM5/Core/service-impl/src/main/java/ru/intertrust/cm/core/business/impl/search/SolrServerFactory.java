package ru.intertrust.cm.core.business.impl.search;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.tools.DynamicLoadClassFactory;
import ru.intertrust.cm.core.util.SpringApplicationContext;

public class SolrServerFactory implements FactoryBean<SolrServer> {

    @Value("${search.solr.url}")
    private String solrUrl;

    private static final String SOLR_FACTORY_CLASS =
            "ru.intertrust.cm.core.business.impl.solr.EmbeddedSolrServerFactory";

    @Override
    @SuppressWarnings("unchecked")
    public SolrServer getObject() throws Exception {
        try {
            if (solrUrl != null && !solrUrl.isEmpty()) {
                return new HttpSolrServer(solrUrl);
            }

            DynamicLoadClassFactory<SolrServer> factory =
                    (DynamicLoadClassFactory<SolrServer>) Class.forName(SOLR_FACTORY_CLASS).newInstance();
            AutowireCapableBeanFactory beanFactory =
                    SpringApplicationContext.getContext().getAutowireCapableBeanFactory();
            beanFactory.autowireBean(factory);
            beanFactory.initializeBean(factory, "solrEmbeddedServerFactory");
            return factory.createInstance();
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
