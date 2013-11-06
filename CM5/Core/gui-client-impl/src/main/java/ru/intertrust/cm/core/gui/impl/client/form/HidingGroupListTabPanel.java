package ru.intertrust.cm.core.gui.impl.client.form;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;

/**
 * Created with IntelliJ IDEA.
 * User: tbilyi
 * Date: 05.11.13
 * Time: 10:47
 * To change this template use File | Settings | File Templates.
 */
public class HidingGroupListTabPanel implements IsWidget{

        private VerticalPanel rootPanel = new VerticalPanel();

   public void add(String title, IsWidget widget) {
        final HorizontalPanel titlePanel = new HorizontalPanel();
        final Image btn = new Image("images/right.png");
        final HorizontalPanel contentPanel = new HorizontalPanel();
        VerticalPanel elementPanel = new VerticalPanel();

        titlePanel.add(btn);
        titlePanel.add(new Label(title));
        contentPanel.add(widget);
        contentPanel.setVisible(false);
        elementPanel.add(titlePanel);
        elementPanel.add(contentPanel);
        rootPanel.add(elementPanel);

        btn.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                String path;
                if (!contentPanel.isVisible()) {
                    contentPanel.setVisible(true);
                    path = ("images/down.png");

                }
                else {
                    contentPanel.setVisible(false);
                    path = ("images/right.png");
                }
                btn.setUrl(path);
            }
        });
    }

    @Override
    public Widget asWidget() {
        // TODO Auto-generated method stub
        return rootPanel;
    }
}
