package ru.intertrust.cm.core.gui.impl.client.form.widget.linkedtable;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 14.09.2014
 *         Time: 11:01
 */
public class StyledButtonCell extends ButtonCell {
    private String buttonClass;

    public StyledButtonCell(String buttonClass) {
        this.buttonClass = buttonClass;
    }

    @Override
    public void render(Context context, SafeHtml data, SafeHtmlBuilder sb) {
        sb.appendHtmlConstant("<button type=\"button\" tabindex=\"-1\" class = \"" + buttonClass + "\">");
        if (data != null) {
            sb.append(data);
        }
        sb.appendHtmlConstant("</button>");
    }
}
