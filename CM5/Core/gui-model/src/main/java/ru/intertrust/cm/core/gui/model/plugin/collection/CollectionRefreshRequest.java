package ru.intertrust.cm.core.gui.model.plugin.collection;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * @author Sergey.Okolot
 *         Created on 07.11.2014 13:35.
 */
public class CollectionRefreshRequest implements Dto {

    private CollectionRowsRequest collectionRowsRequest;
    private Id idToFindIfAbsent;

    public CollectionRefreshRequest() {
    }

    public CollectionRefreshRequest(CollectionRowsRequest collectionRowsRequest, Id idToFindIfAbsent) {
        this.collectionRowsRequest = collectionRowsRequest;
        this.idToFindIfAbsent = idToFindIfAbsent;
    }

    public CollectionRowsRequest getCollectionRowsRequest() {
        return collectionRowsRequest;
    }

    public Id getIdToFindIfAbsent() {
        return idToFindIfAbsent;
    }
}
