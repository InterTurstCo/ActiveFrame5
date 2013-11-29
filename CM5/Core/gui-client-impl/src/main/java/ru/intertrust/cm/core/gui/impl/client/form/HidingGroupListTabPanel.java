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
        final FocusPanel btn = new FocusPanel();
        btn.setStyleName("hidden-group");

        final HorizontalPanel contentPanel = new HorizontalPanel();

        VerticalPanel elementPanel = new VerticalPanel();

        titlePanel.add(btn);
        Label label = new Label(title);
        label.setStyleName("hidden-group-title-first-level");
        titlePanel.add(label);

        contentPanel.add(widget);
        contentPanel.setStyleName("hidden-group-content-box");


        contentPanel.setVisible(false);
        elementPanel.add(titlePanel);
        elementPanel.add(contentPanel);
        rootPanel.add(elementPanel);

        btn.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                String path;
                String style;
                if (!contentPanel.isVisible()) {
                    contentPanel.setVisible(true);
                    style = "hidden-group-select";
                }
                else {
                    contentPanel.setVisible(false);
                    style = "hidden-group";

                }
                btn.setStyleName(style);

            }
        });
    }

    @Override
    public Widget asWidget() {
        // TODO Auto-generated method stub
        return rootPanel;
    }
}
