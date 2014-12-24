package ru.intertrust.cm.core.gui.impl.client.form.widget.linkedtable;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTMLTable;

import javax.naming.Context;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 14.09.2014
 *         Time: 11:01
 */
public class StyledButtonCell extends AbstractCell<ColumnContext> {
    private String buttonClass;

    public StyledButtonCell(String buttonClass) {
        super("click");
        this.buttonClass = buttonClass;
    }


    @Override
    public void render(Context context, ColumnContext data, SafeHtmlBuilder sb) {
        sb.appendHtmlConstant("<button type=\"button\" tabindex=\"-1\" class = \"" + buttonClass + "\">");
        if (data != null) {
            SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();
            safeHtmlBuilder.appendHtmlConstant(data.renderRow());
            sb.append(safeHtmlBuilder.toSafeHtml());
        }
        sb.appendHtmlConstant("</button>");
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, ColumnContext value, NativeEvent event, ValueUpdater<ColumnContext> valueUpdater) {
        valueUpdater.update(value);
        super.onBrowserEvent(context, parent, value, event, valueUpdater);
    }
}
