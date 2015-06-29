package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import com.google.gwt.cell.client.AbstractCell;
import com.google.web.bindery.event.shared.EventBus;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 14/02/14
 *         Time: 12:05 PM
 */
public abstract class CollectionParameterizedColumn extends CollectionColumn<String> {

    public CollectionParameterizedColumn(AbstractCell cell) {
        super(cell);
    }

    public CollectionParameterizedColumn(AbstractCell cell, String fieldName, EventBus eventBus, boolean resizable) {
       super(cell, fieldName, eventBus, resizable);

    }

}
