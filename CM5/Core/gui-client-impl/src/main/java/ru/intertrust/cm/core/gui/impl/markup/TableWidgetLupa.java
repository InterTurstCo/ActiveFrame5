package ru.intertrust.cm.core.gui.impl.markup;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public class TableWidgetLupa extends FlowPanel {

    HorizontalPanel horizont = new HorizontalPanel();

    Image img = new Image("css/images/filter-sts.png");
    TextBox text = new TextBox();
    Label label;

    public TableWidgetLupa(String nameLabel) {
        this.label = new Label(nameLabel);
        text.setVisible(false);
        img.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (text.isVisible() == true) {
                    text.setVisible(false);
                }
                else {
                    text.setVisible(true);
                }

            }
        });

        this.add(horizont);

        horizont.add(img);
        horizont.add(text);
        horizont.add(label);
    }

}
