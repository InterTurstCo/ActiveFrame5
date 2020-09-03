package ru.intertrust.cm.core.business.impl.search;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import ru.intertrust.cm.core.business.impl.solr.EmbeddedSolrCntxServerFactory;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.util.SpringApplicationContext;

import java.util.Map;

public class SolrServerMapFactory implements FactoryBean<SolrServerWrapperMap> {

    @Autowired
    private SolrSearchConfiguration solrSearchConfiguration;

    @Autowired
    private SolrServer regSolrServer;

    @Override
    @SuppressWarnings("unchecked")
    public SolrServerWrapperMap getObject() throws Exception {
        SolrServerWrapperMap solrServerMap = new SolrServerWrapperMap();
        AutowireCapableBeanFactory beanFactory =
                SpringApplicationContext.getContext().getAutowireCapableBeanFactory();
        SolrServerWrapper ssw = beanFactory.getBean(SolrServerWrapper.class,(String) null, regSolrServer, true);
        solrServerMap.addSolrServerWrapper(ssw);

        if (solrSearchConfiguration.isSolrEnable()) {
            try {
                Map<String, SolrCntxServerDescription> solrMap = solrSearchConfiguration.getSolrCntxServerDescriptionMap();
                if (!solrMap.isEmpty()) {
                    for (SolrCntxServerDescription descr : solrMap.values()) {
                        if (descr != null && descr.isValid()) {
                            if (descr.isEmbedded()) {
                                SolrServer server = EmbeddedSolrCntxServerFactory.getSolrServer(descr.getHomeDir(), descr.getDataDir());
                                ssw = server != null ? beanFactory.getBean(SolrServerWrapper.class, descr.getKey(), server, false) : null;
                            } else {
                                HttpSolrServer server = new HttpSolrServer(descr.getUrl());
                                if (descr.getTimeOut() > 0) {
                                    server.setSoTimeout(descr.getTimeOut());
                                }
                                ssw = beanFactory.getBean(SolrServerWrapper.class, descr.getKey(), server, false);
                            }
                            if (ssw != null) {
                                solrServerMap.addSolrServerWrapper(ssw);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                throw new FatalException("Error initializing search context engine", e);
            }
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
