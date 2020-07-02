package ru.intertrust.cm.core.business.impl.search;

public class SolrCntxServerDescription {
    private final String key;
    private final String url;
    private final int timeOut;

    public SolrCntxServerDescription(String key, String url, int timeOut) {
        this.key = key;
        this.url = url;
        this.timeOut = timeOut;
    }

    public String getKey() {
        return key;
    }

    public String getUrl() {
        return url;
    }

    public int getTimeOut() {
        return timeOut;
    }

}
