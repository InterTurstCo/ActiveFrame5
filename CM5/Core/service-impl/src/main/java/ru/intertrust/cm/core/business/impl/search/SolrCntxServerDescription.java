package ru.intertrust.cm.core.business.impl.search;

import org.springframework.core.env.Environment;

public class SolrCntxServerDescription {
    private static final String URL = ".url";
    private static final String TIMEOUT = ".timeout";
    private static final String DATADIR = ".search.solr.data";
    private static final String HOMEDIR = ".search.solr.home";

    private final String key;
    private String url;
    private String homeDir;
    private String dataDir;
    private int timeOut;

    public SolrCntxServerDescription(String key, Environment env) {
        this.key = key;
        init(env);
    }

    public String getKey() {
        return key;
    }

    public String getUrl() {
        return url;
    }

    public String getHomeDir() {
        return homeDir;
    }

    public String getDataDir() {
        return dataDir;
    }

    public int getTimeOut() {
        return timeOut;
    }

    public boolean isValid() {
        return !(url.isEmpty() && (dataDir.isEmpty() || homeDir.isEmpty()));
    }

    public boolean isEmbedded() {
        return url.isEmpty();
    }

    private void init(Environment env) {
        this.url = env.getProperty(key + URL, "").trim();
        this.homeDir = env.getProperty(key + HOMEDIR, "").trim();
        this.dataDir = env.getProperty(key + DATADIR, "").trim();
        String sTimeOut = env.getProperty(key + TIMEOUT, "");
        try {
            this.timeOut = Integer.parseInt(sTimeOut);
        } catch (NumberFormatException e) {
            this.timeOut = 180000;
        }
    }
}
