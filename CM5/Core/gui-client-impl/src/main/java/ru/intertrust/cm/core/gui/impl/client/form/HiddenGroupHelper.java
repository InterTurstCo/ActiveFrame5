package ru.intertrust.cm.core.gui.impl.client.form;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Created with IntelliJ IDEA.
 * User: Timofiy Bilyi
 * Date: 22.11.13
 * Time: 17:51
 * To change this template use File | Settings | File Templates.
 */

public class HiddenGroupHelper implements IsWidget {
    private AbsolutePanel rootPanel;

    public HiddenGroupHelper() {
        rootPanel = new AbsolutePanel();
        rootPanel.setStyleName("hidden-group-root-div");
    }

    public void add(String title, IsWidget widget) {
        AbsolutePanel titlePanel = new AbsolutePanel();
        titlePanel.setStyleName("hidden-group-title-panel");
        final AbsolutePanel contentPanel = new AbsolutePanel();

        AbsolutePanel elementPanel = new AbsolutePanel();
        elementPanel.setStyleName("hidden-block-row");

        final Label label = new Label(title);
        label.setStyleName("hidden-group-title-first-level");
        titlePanel.add(label);

        contentPanel.add(widget);
        contentPanel.setStyleName("hidden-group-content-box");

        contentPanel.setVisible(false);
        elementPanel.add(titlePanel);
        elementPanel.add(contentPanel);
        rootPanel.add(elementPanel);

        label.addClickHandler(new ClickHandler() {

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
                label.setStyleName(style);

            }
        });
    }

    @Override
    public Widget asWidget() {
        // TODO Auto-generated method stub
        return rootPanel;
    }

}
