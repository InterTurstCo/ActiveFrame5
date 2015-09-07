package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 27.06.2015
 *         Time: 19:53
 */
public abstract class AbstractTextCell extends AbstractCell<String> {
    protected String style;
    protected String field;

    public AbstractTextCell(String style, String field) {
        this.style = style;
        this.field = field;
    }

    protected void addClassName(CollectionRowItem item, SafeHtmlBuilder sb){
        if(item.getParentId() != null){
            sb.append(SafeHtmlUtils.fromTrustedString(" class=\"childRow\" "));
        }
    }

}
