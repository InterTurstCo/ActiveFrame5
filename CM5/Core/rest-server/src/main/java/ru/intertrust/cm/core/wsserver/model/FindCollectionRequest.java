package ru.intertrust.cm.core.wsserver.model;

import java.util.List;

public class FindCollectionRequest {
    private String query;
    private List<ValueData> params;
    private int limit;
    private int offset;

    public FindCollectionRequest() {
    }

    public FindCollectionRequest(String query, List<ValueData> params, int limit, int offset) {
        this.query = query;
        this.params = params;
        this.limit = limit;
        this.offset = offset;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<ValueData> getParams() {
        return params;
    }

    public void setParams(List<ValueData> params) {
        this.params = params;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
}
