package ru.intertrust.cm.core.business.impl.search;

import javax.annotation.PreDestroy;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.request.AbstractUpdateRequest;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@Interceptors(SpringBeanAutowiringInterceptor.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class SolrIndexingBean {

    private static final long WORKTIME_LIMIT = 290000L;

    protected Logger log = LoggerFactory.getLogger(getClass());

    private volatile boolean active = false;

    @Autowired
    private SolrUpdateRequestQueue requestQueue;

    @Autowired
    private SolrServer solrServer;

    @Schedule(hour = "*", minute = "*", second = "*/20", persistent = false)
    public void processTimer() {
        if (active) {
            return;
        }
        active = true;
        processUpdateQueue();
        active = false;
    }

    private void processUpdateQueue() {
        long breakTime = System.currentTimeMillis() + WORKTIME_LIMIT;

        if (log.isTraceEnabled()) {
            log.trace("Solr request queue processing started");
        }
        int processed = 0;

        while(requestQueue.hasRequests() && System.currentTimeMillis() < breakTime) {
            AbstractUpdateRequest request = requestQueue.fetchRequest();
            try {
                solrServer.request(request);
                if (request instanceof UpdateRequest && ((UpdateRequest) request).getDocuments() != null) {
                    processed += ((UpdateRequest) request).getDocuments().size();
                } else {
                    processed++;
                }
            } catch (Exception e) {
                log.error("Error processing Solr request: " + request, e);
            }
        }
        if (processed > 0) {
            try {
                solrServer.commit();
                if (log.isInfoEnabled()) {
                    log.info(Integer.toString(processed) + " document(s) indexed by Solr");
                }
            } catch (Exception e) {
                log.error("Error committing Solr update", e);
            }
        }
        if (log.isTraceEnabled()) {
            log.trace("Solr request queue processing finished");
        }
    }

    @PreDestroy
    public void shutdown() {
        solrServer.shutdown();
    }
}
