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
    private HorizontalPanel leftPanelWithHideButtonContainer = new HorizontalPanel();
    private HorizontalPanel rightPanelWithHideButtonContainer = new HorizontalPanel();
    private FocusPanel leftInnerPanel = new FocusPanel();
    private VerticalPanel buttonHidePanel = new VerticalPanel();
    private FocusPanel buttonHide;
    private FocusPanel buttonHideOff;
    private VerticalPanel contentRightPanel = new VerticalPanel();
    private FocusPanel contentInnerPanel = new FocusPanel();

    public BookmarksTabPanel() {
        init();


        rootPanel.setStyleName("panel-left-exist");

        buttonHide.setStyleName("button-hide");
        buttonHideOff.setStyleName("buttonHideOff");
        leftInnerPanel.addStyleName("bookmark-leftpanel");

        leftInnerPanel.addStyleName("bookmark-leftpanel-active");

        contentInnerPanel.addStyleName("bookmark-content");
        leftPanelWithHideButtonContainer.setStyleName("bookmark-panel-left");
        rightPanelWithHideButtonContainer.setStyleName("bookmark-panel-right");


    }

    private void init() {

        buttonHide = new FocusPanel();
        buttonHideOff = new FocusPanel();
        buttonHideOff.setVisible(false);
        leftPanelWithHideButtonContainer.add(leftPanel);
        leftPanelWithHideButtonContainer.add(buttonHidePanel);
        leftInnerPanel.add(leftPanelWithHideButtonContainer);



        rootPanel.add(leftInnerPanel);

        buttonHidePanel.add(buttonHide);

        contentInnerPanel.add(contentRightPanel);

        rightPanelWithHideButtonContainer.add(buttonHideOff);
        rightPanelWithHideButtonContainer.add(contentInnerPanel);

        rootPanel.add(rightPanelWithHideButtonContainer);

        buttonHide.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                    leftPanel.setVisible(false);
                    buttonHide.setStyleName("button-hide-selected");
                    leftInnerPanel.removeStyleName("bookmark-leftpanel-active");
                    leftInnerPanel.addStyleName("bookmark-leftpanel-non-active");
                    buttonHideOff.setVisible(true);
                    buttonHide.setVisible(false);
            }
        });

        buttonHideOff.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                leftPanel.setVisible(true);
                buttonHide.setStyleName("button-hide");
                leftInnerPanel.removeStyleName("bookmark-leftpanel-non-active");
                leftInnerPanel.addStyleName("bookmark-leftpanel-active");
                buttonHideOff.setVisible(false);
                buttonHide.setVisible(true);
            }
        });
    }

    public void add(String title, IsWidget content) {
        final Hyperlink linkLabel = new Hyperlink();
        linkLabel.setStyleName("bookmarks-link-non-active");
        linkLabel.setText(title);
        leftPanel.add(linkLabel);

        contentRightPanel.add(content);


        linkLabel.addClickHandler(new ClickHandler() {
            int index;

            @Override
            public void onClick(ClickEvent event) {
                index = leftPanel.getWidgetIndex(linkLabel);
                contentRightPanel.getWidget(index).setVisible(true);

                for (int i = 0; i < contentRightPanel.getWidgetCount(); i++) {
                    if (i == index) {
                        contentRightPanel.getWidget(i).setVisible(true);
                        leftPanel.getWidget(i).setStyleName("bookmarks-link-active");
                    }
                    else {
                        contentRightPanel.getWidget(i).setVisible(false);
                        leftPanel.getWidget(i).setStyleName("bookmarks-link-non-active");
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
