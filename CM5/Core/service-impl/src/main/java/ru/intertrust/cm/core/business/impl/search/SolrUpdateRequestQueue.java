package ru.intertrust.cm.core.business.impl.search;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.solr.client.solrj.request.AbstractUpdateRequest;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.common.SolrInputDocument;

public class SolrUpdateRequestQueue {

    private Queue<AbstractUpdateRequest> requestQueue = new ConcurrentLinkedQueue<>();

    public void addRequest(AbstractUpdateRequest request) {
        requestQueue.add(request);
    }

    public void addDocuments(Collection<SolrInputDocument> documents) {
        UpdateRequest request = new UpdateRequest();
        request.add(documents);
        addRequest(request);
    }

    public boolean hasRequests() {
        return !requestQueue.isEmpty();
    }

    public AbstractUpdateRequest fetchRequest() {
        return requestQueue.remove();
    }
}
