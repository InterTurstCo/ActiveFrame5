package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import ru.intertrust.cm.core.gui.impl.client.converter.ValueConverter;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;
import ru.intertrust.cm.core.gui.model.util.WidgetUtil;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 14/02/14
 *         Time: 12:05 PM
 */
public class TextCell extends ExpandableCell<String> {
    protected String style;

    public TextCell(String columnKey, String style, ValueConverter valueConverter) {
        super(columnKey, style,valueConverter);
    }


    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context, String text, SafeHtmlBuilder sb) {
        sb.append(SafeHtmlUtils.fromTrustedString("<div class=\"collectionCellWrapper\" >"));
        sb.append(SafeHtmlUtils.fromTrustedString("<div " + style + "/>"));
        sb.append(SafeHtmlUtils.fromString(text));
        sb.append(SafeHtmlUtils.fromTrustedString("</div>"));
        CollectionRowItem item = (CollectionRowItem)context.getKey();
        if(item.isExpanded() && WidgetUtil.isNotEmpty(item.getCollectionRowItems())){
            drawChildren(sb, item);
        }
        sb.append(SafeHtmlUtils.fromTrustedString("</div>"));
    }

}