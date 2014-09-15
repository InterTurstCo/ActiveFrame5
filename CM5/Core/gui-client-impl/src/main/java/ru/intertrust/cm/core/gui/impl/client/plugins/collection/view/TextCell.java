package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 14/02/14
 *         Time: 12:05 PM
 */
public class TextCell extends AbstractCell<String> {
    protected String style;

    public TextCell(String style) {
        this.style = style;
    }

    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context, String text, SafeHtmlBuilder sb) {
        sb.append(SafeHtmlUtils.fromTrustedString("<div " + style + "/>" + text + "</div>"));
    }

}