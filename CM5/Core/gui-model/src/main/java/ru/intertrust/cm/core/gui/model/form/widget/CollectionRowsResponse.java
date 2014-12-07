package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: lvov
 * Date: 21.11.13
 * Time: 14:19
 * To change this template use File | Settings | File Templates.
 */
public class CollectionRowsResponse implements Dto {
    ArrayList<CollectionRowItem> collectionRows;
    public ArrayList<CollectionRowItem> getCollectionRows() {
        return collectionRows;
    }

    public void setCollectionRows(ArrayList<CollectionRowItem> collectionRows) {
        this.collectionRows = collectionRows;
    }

}
