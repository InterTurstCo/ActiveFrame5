package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Label;

/**
 * Created by User on 11.02.14.
 */
public class LabelCell extends AbstractCell<Label>{
    private String style;
    public LabelCell(String style) {
       this.style = style;
    }
    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context, Label label, SafeHtmlBuilder sb) {
        sb.append(SafeHtmlUtils.fromTrustedString("<div " + style +"/>" + label.getText() + "</div>"));
    }

}