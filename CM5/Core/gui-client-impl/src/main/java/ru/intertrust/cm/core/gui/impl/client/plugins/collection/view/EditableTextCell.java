package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 14/02/14
 *         Time: 12:05 PM
 */
public class EditableTextCell extends AbstractTextCell {

    public EditableTextCell(String style, String field) {
        super(style, field);
    }

    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context, String text, SafeHtmlBuilder sb) {
        sb.append(SafeHtmlUtils.fromTrustedString("<div "));
        CollectionRowItem item = (CollectionRowItem) context.getKey();
        addClassName(item, sb);
        sb.append(SafeHtmlUtils.fromTrustedString(style));
        sb.append(SafeHtmlUtils.fromTrustedString(">"));
        sb.append(SafeHtmlUtils.fromString(text));
        sb.append(SafeHtmlUtils.fromTrustedString("</div>"));
    }

}