package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Label;
import ru.intertrust.cm.core.gui.impl.client.converter.ValueConverter;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.LabelCell;
import ru.intertrust.cm.core.gui.model.plugin.CollectionRowItem;

/**
 * @author Denis Mitavskiy
 *         Date: 21.01.14
 *         Time: 22:59
 */
public class CollectionColumn extends Column<CollectionRowItem, Label> {
    private String fieldName;
    private ValueConverter converter;

    public CollectionColumn(LabelCell cell) {
        super(cell);
    }

    @Override
    public Label getValue(CollectionRowItem object) {
       String text =  converter.valueToString(object.getRowValue(fieldName));
       Label label = new Label(text);
        return label;
    }

    public CollectionColumn(String fieldName, ValueConverter converter, LabelCell cell) {
        super(cell);
        this.fieldName = fieldName;
        this.converter = converter;
    }

    public String getFieldName() {
        return fieldName;
    }


}
