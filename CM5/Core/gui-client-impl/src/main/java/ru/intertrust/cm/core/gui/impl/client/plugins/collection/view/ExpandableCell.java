package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import ru.intertrust.cm.core.gui.impl.client.converter.ValueConverter;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;
import ru.intertrust.cm.core.gui.model.util.WidgetUtil;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 21.06.2015
 *         Time: 19:00
 */
public abstract class ExpandableCell<C> extends AbstractExpandableCell<C> {
    public ExpandableCell(String columnKey, String style, ValueConverter valueConverter) {
        super(columnKey, style, valueConverter);
    }

    protected void drawChildren(SafeHtmlBuilder sb, CollectionRowItem item) {

        sb.append(SafeHtmlUtils.fromTrustedString("<div class=\"hierarchicalFilter\"><input></input><div></div></div>"));
        List<CollectionRowItem> collectionRowItems = item.getCollectionRowItems();
        for (CollectionRowItem collectionRowItem : collectionRowItems) {
            String childText = valueConverter.valueToString(collectionRowItem.getRowValue(columnKey));
            String styleClassNames = collectionRowItem.getCollectionRowItems().isEmpty()
                    ? "expandable"
                    : "notExpandable";
            sb.append(SafeHtmlUtils.fromTrustedString("<div class=\""));
            sb.append(SafeHtmlUtils.fromTrustedString(styleClassNames));
            sb.append(SafeHtmlUtils.fromTrustedString("\""));
            sb.append(SafeHtmlUtils.fromTrustedString(style));
            sb.append(SafeHtmlUtils.fromTrustedString("/><span>"));
            sb.append(SafeHtmlUtils.fromString(childText));
            sb.append(SafeHtmlUtils.fromTrustedString("</span></div>"));
            if (collectionRowItem.isExpanded() && WidgetUtil.isNotEmpty(collectionRowItem.getCollectionRowItems())) {
                drawChildren(sb, collectionRowItem);
            }
        }


    }

}
