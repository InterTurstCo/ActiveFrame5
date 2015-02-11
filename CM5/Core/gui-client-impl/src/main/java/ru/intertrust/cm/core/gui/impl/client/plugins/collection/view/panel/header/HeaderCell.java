package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.header;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.header.widget.HeaderWidget;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 22.06.2014
 *         Time: 21:29
 */
public class HeaderCell extends AbstractCell<HeaderWidget> {

    public HeaderCell() {
        super("mousemove", "keydown", "keyup", "change", "blur");

    }

    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context,
                       final HeaderWidget value, SafeHtmlBuilder sb) {
        value.init();
        sb.append(SafeHtmlUtils.fromTrustedString(value.getHtml()));

    }

}


