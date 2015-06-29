package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import com.google.gwt.cell.client.AbstractCell;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.impl.client.converter.ValueConverter;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 14/02/14
 *         Time: 12:05 PM
 */
public class TextCollectionColumn extends CollectionParameterizedColumn {

    private ValueConverter converter;

    public TextCollectionColumn(AbstractCell cell) {
        super(cell);
    }

    @Override
    public String getValue(CollectionRowItem object) {
        return converter.valueToString(object.getRowValue(fieldName));

    }

    public TextCollectionColumn(AbstractCell cell, String fieldName, EventBus eventBus, boolean resizable, ValueConverter converter) {
        super(cell, fieldName, eventBus, resizable);
        this.converter = converter;
    }


}

