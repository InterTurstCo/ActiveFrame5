package ru.intertrust.cm.core.gui.impl.client.form;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;

/**
 * Created with IntelliJ IDEA.
 * User: tbilyi
 * Date: 04.11.13
 * Time: 18:56
 * To change this template use File | Settings | File Templates.
 */
public class BookmarksTabPanel implements IsWidget {
    private HorizontalPanel rootPanel = new HorizontalPanel();
    private VerticalPanel leftPanel = new VerticalPanel();
    private VerticalPanel buttonHidePanel = new VerticalPanel();
    private Button buttonHide = new Button("<-");
    private VerticalPanel contentRightPanel = new VerticalPanel();

    public BookmarksTabPanel() {
        init();
    }

    private void init() {
        rootPanel.add(leftPanel);
        rootPanel.add(buttonHidePanel);
        buttonHidePanel.add(buttonHide);
        rootPanel.add(contentRightPanel);

        buttonHide.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (rootPanel.getWidget(0).isVisible()) {
                    rootPanel.getWidget(0).setVisible(false);
                }
                else
                    rootPanel.getWidget(0).setVisible(true);
            }
        });
    }

    public void add(String title, IsWidget content) {
        final Button label = new Button(title);
        label.setWidth("200px");
        leftPanel.add(label);
        contentRightPanel.add(content);

        label.addClickHandler(new ClickHandler() {
            int index;

            @Override
            public void onClick(ClickEvent event) {
                index = leftPanel.getWidgetIndex(label);
                contentRightPanel.getWidget(index).setVisible(true);
                for (int i = 0; i < contentRightPanel.getWidgetCount(); i++) {
                    if (i == index) {
                        contentRightPanel.getWidget(i).setVisible(true);
                    }
                    else {
                        contentRightPanel.getWidget(i).setVisible(false);
                    }
                }
            }
        });
    }

    @Override
     public Widget asWidget() {
        // TODO Auto-generated method stub
        return rootPanel;
    }

    public void selectedIndex(int index) {
        for (int i = 0; i < contentRightPanel.getWidgetCount(); i++) {
            if (i == index) {
                contentRightPanel.getWidget(i).setVisible(true);
            }
            else {
                contentRightPanel.getWidget(i).setVisible(false);
            }
        }
    }
}
