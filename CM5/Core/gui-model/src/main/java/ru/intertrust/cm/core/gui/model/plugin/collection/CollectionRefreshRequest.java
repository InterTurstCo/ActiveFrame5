package ru.intertrust.cm.core.gui.model.plugin.collection;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.model.filters.ComplexFiltersParams;

/**
 * @author Sergey.Okolot
 *         Created on 07.11.2014 13:35.
 */
public class CollectionRefreshRequest implements Dto {

    private CollectionRowsRequest collectionRowsRequest;
    private Id idToFindIfAbsent;
    private Dto filtersParams;

    public CollectionRefreshRequest() {
    }

    public CollectionRefreshRequest(CollectionRowsRequest collectionRowsRequest, Id idToFindIfAbsent) {
        this.collectionRowsRequest = collectionRowsRequest;
        this.idToFindIfAbsent = idToFindIfAbsent;
    }

    public CollectionRefreshRequest(CollectionRowsRequest collectionRowsRequest, Id idToFindIfAbsent, Dto filterParams) {
        this.collectionRowsRequest = collectionRowsRequest;
        this.idToFindIfAbsent = idToFindIfAbsent;
        this.filtersParams = filterParams;
    }

    public CollectionRowsRequest getCollectionRowsRequest() {
        return collectionRowsRequest;
    }

    public Id getIdToFindIfAbsent() {
        return idToFindIfAbsent;
    }

    public Dto getFiltersParams() {
        if (filtersParams == null)
            return new ComplexFiltersParams();
        else
            return filtersParams;
    }

    public void setFiltersParams(ComplexFiltersParams filtersParams) {
        this.filtersParams = filtersParams;
    }
}
