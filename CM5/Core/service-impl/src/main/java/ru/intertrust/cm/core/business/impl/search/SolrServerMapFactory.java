package ru.intertrust.cm.core.business.impl.search;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.util.SpringApplicationContext;

import java.util.Map;

public class SolrServerMapFactory implements FactoryBean<SolrServerWrapperMap> {

    @Autowired
    private SolrSearchConfiguration solrSearchConfiguration;

    @Autowired
    private SolrClient regSolrServer;

    @Override
    @SuppressWarnings("unchecked")
    public SolrServerWrapperMap getObject() throws Exception {
        SolrServerWrapperMap solrServerMap = new SolrServerWrapperMap();
        AutowireCapableBeanFactory beanFactory =
                SpringApplicationContext.getContext().getAutowireCapableBeanFactory();
        SolrServerWrapper ssw = beanFactory.getBean(SolrServerWrapper.class,(String) null, regSolrServer, true);
        solrServerMap.addSolrServerWrapper(ssw);

        if (!solrSearchConfiguration.isSolrEnable()) {
            return solrServerMap;
        }

        try {
            Map<String, SolrCntxServerDescription> solrMap = solrSearchConfiguration.getSolrCntxServerDescriptionMap();
            if (solrMap.isEmpty()) {
                return solrServerMap;
            }
            for (SolrCntxServerDescription descr : solrMap.values()) {
                if (descr == null || !descr.isValid()) {
                    continue;
                }
                HttpSolrClient server = new HttpSolrClient.Builder(descr.getUrl()).build();
                if (descr.getTimeOut() > 0) {
                    server.setSoTimeout(descr.getTimeOut());
                }
                ssw = beanFactory.getBean(SolrServerWrapper.class, descr.getKey(), server, false);
                if (ssw != null) {
                    solrServerMap.addSolrServerWrapper(ssw);
                }
            }
        } catch (Exception e) {
            throw new FatalException("Error initializing search context engine", e);
        }
        return solrServerMap;
    }

    @Override
    public Class<?> getObjectType() {
        return SolrServerWrapperMap.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
