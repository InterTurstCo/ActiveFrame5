package ru.intertrust.cm.core.gui.model.plugin.collection;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * @author Sergey.Okolot
 *         Created on 07.11.2014 13:35.
 */
public class CollectionRefreshRequest implements Dto {

    private CollectionRowsRequest collectionRowsRequest;
    private Id id;

    public CollectionRefreshRequest() {
    }

    public CollectionRefreshRequest(CollectionRowsRequest collectionRowsRequest, Id id) {
        this.collectionRowsRequest = collectionRowsRequest;
        this.id = id;
    }

    public CollectionRowsRequest getCollectionRowsRequest() {
        return collectionRowsRequest;
    }

    public Id getId() {
        return id;
    }
}
