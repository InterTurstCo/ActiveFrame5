package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.impl.client.converter.ValueConverter;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.ControlExpandableCell;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;


/**
 * @author Yaroslav Bondarchuk
 *         Date: 17.06.2015
 *         Time: 8:52
 */
public class ExpandableColumn extends CollectionParameterizedColumn {

    private ValueConverter converter;
    public ExpandableColumn(ControlExpandableCell cell) {
        super(cell);

    }

    @Override
    public String getValue(CollectionRowItem object) {
        return converter.valueToString(object.getRowValue(fieldName));

    }

    public ExpandableColumn(ControlExpandableCell cell, String fieldName, Boolean resizable, ValueConverter converter, EventBus eventBus) {
        super(cell, fieldName, eventBus, resizable);
        this.converter = converter;
    }

}
