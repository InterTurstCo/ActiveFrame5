package ru.intertrust.cm.core.gui.impl.client.form;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;

/**
 * Created with IntelliJ IDEA.
 * User: Timofiy Bilyi
 * Date: 22.11.13
 * Time: 14:19
 * To change this template use File | Settings | File Templates.
 */

public class BookmarksHelper implements IsWidget {
    private AbsolutePanel rootDiv;
    private AbsolutePanel divLeft;
    private AbsolutePanel decoratedDivLeft;
    private AbsolutePanel divRight;
    private AbsolutePanel decoratedDivRight;
    private AbsolutePanel verticalTabText;
    private AbsolutePanel insideVerticalTabText;
    private HTML insideVerticalTabLabel;
    private FocusPanel divLeftButton;
    private FocusPanel divRightButton;

    public BookmarksHelper() {

        rootDiv = new AbsolutePanel();

        divLeft = new AbsolutePanel();
        decoratedDivLeft = new AbsolutePanel();

        divRight = new AbsolutePanel();
        decoratedDivRight = new AbsolutePanel();

        divLeftButton = new FocusPanel();

        divRightButton = new FocusPanel();
        divRightButton.setVisible(false);

        verticalTabText = new AbsolutePanel();

        divRightButton.add(verticalTabText);
        insideVerticalTabText = new AbsolutePanel();

        verticalTabText.add(insideVerticalTabText);
        insideVerticalTabLabel = new HTML("");
        insideVerticalTabText.add(insideVerticalTabLabel);

        divLeft.add(decoratedDivLeft);
        divLeft.add(divLeftButton);
        rootDiv.add(divLeft);

        divRight.add(divRightButton);
        divRight.add(decoratedDivRight);

        rootDiv.add(divRight);

        setStyle();
        init();
    }

    @SuppressWarnings("deprecation")
    public void add(String title, IsWidget content) {
        final Hyperlink linkLabel = new Hyperlink();

        linkLabel.setStyleName("bookmarks-link-non-active");
        linkLabel.setText(title);

        decoratedDivLeft.add(linkLabel);

        decoratedDivRight.add(content);

        linkLabel.addClickHandler(new ClickHandler() {
            int index;

            @Override
            public void onClick(ClickEvent event) {
                index = decoratedDivLeft.getWidgetIndex(linkLabel);
                decoratedDivRight.getWidget(index).setVisible(true);
                insideVerticalTabLabel.setHTML("<ins>"+linkLabel.getText()+"</ins>");

                for (int i = 0; i < decoratedDivRight.getWidgetCount(); i++) {
                    if (i == index) {
                        decoratedDivRight.getWidget(i).setVisible(true);
                        decoratedDivLeft.getWidget(i).setStyleName("bookmarks-link-active");
                    }
                    else {
                        decoratedDivRight.getWidget(i).setVisible(false);
                        decoratedDivLeft.getWidget(i).setStyleName("bookmarks-link-non-active");
                    }
                }
            }
        });
    }

   private void init() {
//        divRightButton.addClickHandler(new ClickHandler() {
        divLeftButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {

                decoratedDivLeft.setVisible(false);
                divLeftButton.setVisible(false);
                divRightButton.setVisible(true);
                divRight.addStyleName("bookmarks-no-margin");
            }
        });

        divRightButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                decoratedDivLeft.setVisible(true);
                divLeftButton.setVisible(true);
                divRightButton.setVisible(false);
                divRight.removeStyleName("bookmarks-no-margin");
            }
        });
    }

    private void setStyle() {
        divRightButton.setStyleName("right-button");
        divLeftButton.setStyleName("left-button");
        rootDiv.setStyleName("root-div");
        divLeft.setStyleName("left-div");
        divLeft.getElement().setId("bookmark-left-side");
        decoratedDivLeft.setStyleName("decorated-left-div");
        divRight.setStyleName("right-div");
        decoratedDivRight.setStyleName("decorated-right-div");
        verticalTabText.setStyleName("vertical-tab-text");
        insideVerticalTabText.setStyleName("inside-vertical-tab-text");
    }

    @Override
    public Widget asWidget() {
        return rootDiv;
    }

    public void selectedIndex(int index) {
        Hyperlink startVerticalTabLabel = (Hyperlink)decoratedDivLeft.getWidget(index);
        insideVerticalTabLabel.setHTML("<ins>"+ startVerticalTabLabel.getText() +"</ins>");
        decoratedDivRight.getWidget(index).setVisible(true);

        for (int i = 0; i < decoratedDivRight.getWidgetCount(); i++) {
            if (i == index) {
                decoratedDivRight.getWidget(i).setVisible(true);
                decoratedDivLeft.getWidget(i).setStyleName("bookmarks-link-active");
            }
            else {
                decoratedDivRight.getWidget(i).setVisible(false);
                decoratedDivLeft.getWidget(i).setStyleName("bookmarks-link-non-active");
            }
        }
    }
}