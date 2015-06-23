package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import ru.intertrust.cm.core.gui.impl.client.converter.ValueConverter;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 08.06.2015
 *         Time: 9:29
 */
public abstract class AbstractExpandableCell<C> extends AbstractCell<C> {
    protected String style;
    protected String columnKey;
    protected ValueConverter valueConverter;

    public AbstractExpandableCell(String columnKey, String style, ValueConverter valueConverter) {
        this.columnKey = columnKey;
        this.style = style;
        this.valueConverter = valueConverter;
    }

    protected abstract void drawChildren(SafeHtmlBuilder sb, CollectionRowItem item);

}
