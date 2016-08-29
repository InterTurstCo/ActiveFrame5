package ru.intertrust.cm.core.gui.model.csv;

import ru.intertrust.cm.core.business.api.dto.SearchQuery;

import java.util.List;

/**
 * Created by Vitaliy Orlov on 23.08.2016.
 */
public class JsonExtendedSearchCSVRequest extends JsonCsvRequest {
    private JsonSearchQuery jsonSearchQuery;

    public JsonSearchQuery getJsonSearchQuery() {
        return jsonSearchQuery;
    }

    public void setJsonSearchQuery(JsonSearchQuery jsonSearchQuery) {
        this.jsonSearchQuery = jsonSearchQuery;
    }
}
