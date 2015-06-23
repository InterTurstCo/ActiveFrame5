package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import ru.intertrust.cm.core.gui.impl.client.converter.ValueConverter;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;
import ru.intertrust.cm.core.gui.model.util.WidgetUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 15.06.2015
 *         Time: 9:36
 */
public class ControlExpandableCell extends AbstractExpandableCell<String> {

    public ControlExpandableCell(String columnKey, String style, ValueConverter valueConverter) {
        super(columnKey, style,valueConverter);
    }

    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context, String text, SafeHtmlBuilder sb) {
        renderSingleRowWithChildren(context, text, sb);
    }

    private void renderSingleRowWithChildren(com.google.gwt.cell.client.Cell.Context context, String text, SafeHtmlBuilder sb) {
        sb.append(SafeHtmlUtils.fromTrustedString("<div class=\"collectionCellWrapper\" >"));
        CollectionRowItem item = (CollectionRowItem) context.getKey();
        boolean expanded = item.isExpanded();
        String id = item.getId().toStringRepresentation();
        if (expanded) {
            sb.append(SafeHtmlUtils.fromTrustedString("<div class=\"hierarchicalColumn\" "));
                    sb.append(SafeHtmlUtils.fromTrustedString(style));
            sb.append(SafeHtmlUtils.fromTrustedString("/><span ><span class=\"collapseSign"));
            sb.append(SafeHtmlUtils.fromTrustedString(id));
            sb.append(SafeHtmlUtils.fromTrustedString("\">-</span>"));
            sb.append(SafeHtmlUtils.fromString(text));
            sb.append(SafeHtmlUtils.fromTrustedString("<span></div>"));
            drawChildren(sb, item);
        } else {
            sb.append(SafeHtmlUtils.fromTrustedString("<div class=\"hierarchicalColumn\" "));
            sb.append(SafeHtmlUtils.fromTrustedString(style));
            sb.append(SafeHtmlUtils.fromTrustedString("/><span ><span class=\"expandSign"));
            sb.append(SafeHtmlUtils.fromTrustedString(id));
            sb.append(SafeHtmlUtils.fromTrustedString("\">+</span>"));
            sb.append(SafeHtmlUtils.fromString(text));
            sb.append(SafeHtmlUtils.fromTrustedString("<span></div>"));
        }

        sb.append(SafeHtmlUtils.fromTrustedString("</div>"));
    }

    @Override
    public Set<String> getConsumedEvents() {
        HashSet<String> events = new HashSet<String>();
        events.add("click");
        return events;
    }

    protected void drawChildren(SafeHtmlBuilder sb, CollectionRowItem item) {
        sb.append(SafeHtmlUtils.fromTrustedString("<div class=\"hierarchicalFilter\"><input></input><div></div></div>"));
        List<CollectionRowItem> collectionRowItems = item.getCollectionRowItems();
        for (CollectionRowItem collectionRowItem : collectionRowItems) {
            String childText = valueConverter.valueToString(collectionRowItem.getRowValue(columnKey));
            String styleClassName = collectionRowItem.isExpandable()
                    ? "notExpandable"
                    : "expandable";
            sb.append(SafeHtmlUtils.fromTrustedString("<div class=\""));
            sb.append(SafeHtmlUtils.fromTrustedString(styleClassName));
            sb.append(SafeHtmlUtils.fromTrustedString("\""));
            boolean expanded = collectionRowItem.isExpanded();
            String id = collectionRowItem.getId().toStringRepresentation();
            String styleClassNameWithId = expanded ? "collapseSign" + id : "expandSign" + id ;

            sb.append(SafeHtmlUtils.fromTrustedString("/><span><span class=\""));
            sb.append(SafeHtmlUtils.fromTrustedString(styleClassNameWithId));
            sb.append(SafeHtmlUtils.fromTrustedString("\">"));
            String sign = collectionRowItem.isExpandable() ? "" : (collectionRowItem.isExpanded() ? "-" : "+");
            sb.append(SafeHtmlUtils.fromTrustedString(sign));
            sb.append(SafeHtmlUtils.fromTrustedString("</span>"));
            sb.append(SafeHtmlUtils.fromString(childText));
            sb.append(SafeHtmlUtils.fromTrustedString("</span></div>"));
            if (collectionRowItem.isExpanded() && WidgetUtil.isNotEmpty(collectionRowItem.getCollectionRowItems())) {
                drawChildren(sb, collectionRowItem);
            }
        }
        sb.append(SafeHtmlUtils.fromTrustedString("<button class=\"moreItems\">More</button>"));

    }

}
