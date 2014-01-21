package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import ru.intertrust.cm.core.gui.impl.client.converter.ValueConverter;
import ru.intertrust.cm.core.gui.model.plugin.CollectionRowItem;

/**
 * @author Denis Mitavskiy
 *         Date: 21.01.14
 *         Time: 22:59
 */
public class CollectionColumn extends com.google.gwt.user.cellview.client.TextColumn<CollectionRowItem> {
    private String fieldName;
    private ValueConverter converter;

    public CollectionColumn(String fieldName, ValueConverter converter) {
        super();
        this.fieldName = fieldName;
        this.converter = converter;
    }

    public String getFieldName() {
        return fieldName;
    }

    @Override
    public String getValue(CollectionRowItem object) {
        return converter.valueToString(object.getRowValue(fieldName));
    }
}
