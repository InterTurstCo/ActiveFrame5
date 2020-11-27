package ru.intertrust.cm.core.business.impl.search;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SolrServerWrapperMap implements Serializable {
    private final Map<String, SolrServerWrapper> solrServerMap = new HashMap<>();

    public SolrServerWrapper getSolrServerWrapper(String key) {
        return solrServerMap.get(key != null ? key.trim() : null);
    }

    public void addSolrServerWrapper(SolrServerWrapper solrServerWrapper) {
        solrServerMap.put(solrServerWrapper.getKey(), solrServerWrapper);
    }

    public Map<String, SolrServerWrapper> getMap() {
        return Collections.unmodifiableMap(solrServerMap);
    }

    public SolrServerWrapper getRegularSolrServerWrapper() {
        return solrServerMap.get(SolrServerWrapper.REGULAR);
    }

    public boolean isCntxSolrServer(String key) {
        return solrServerMap.containsKey(key != null ? key.trim() : null);
    }
}
