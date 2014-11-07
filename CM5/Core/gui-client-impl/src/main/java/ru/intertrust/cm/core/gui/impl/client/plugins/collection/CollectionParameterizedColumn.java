package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import com.google.gwt.cell.client.AbstractCell;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 14/02/14
 *         Time: 12:05 PM
 */
public abstract class CollectionParameterizedColumn extends CollectionColumn<CollectionRowItem, String> {

    public CollectionParameterizedColumn(AbstractCell cell) {
        super(cell);
    }

    public CollectionParameterizedColumn(AbstractCell cell, String fieldName, boolean resizable) {
       super(cell, fieldName, resizable);

    }

}
