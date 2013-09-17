package ru.intertrust.cm.core.gui.impl.client.plugins.navigation;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;

public class TableWidgetLoupe extends FlowPanel {

    HorizontalPanel horizont = new HorizontalPanel();

    Image img = new Image("css/images/filter-sts.png");
    TextBox text = new TextBox();
    Label label;

    public TableWidgetLoupe(String nameLabel) {
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
