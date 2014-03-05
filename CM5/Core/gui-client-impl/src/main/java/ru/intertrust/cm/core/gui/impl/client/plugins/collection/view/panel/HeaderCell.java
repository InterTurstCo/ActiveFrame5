package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * Created by User on 27.02.14.
 */
public class HeaderCell extends AbstractCell<HeaderWidget> {

    public HeaderCell( ) {
        super("mousemove","keydown","keyup","change","blur");

    }

    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context,
                       final    HeaderWidget value, SafeHtmlBuilder sb) {

        value.init();
        sb.append(new SafeHtml() {
            @Override
            public String asString() {
                return value.getHtml();
            }
        });


    }

}


