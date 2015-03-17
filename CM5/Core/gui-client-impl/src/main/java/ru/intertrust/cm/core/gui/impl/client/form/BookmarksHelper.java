package ru.intertrust.cm.core.gui.impl.client.form;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.impl.client.event.form.ParentTabSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.form.widget.HyperLinkWithHistorySupport;

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
    private EventBus eventBus;

    @Deprecated //use public BookmarksHelper(EventBus eventBus) instead
    public BookmarksHelper() {
       init();
    }
    public BookmarksHelper(EventBus eventBus){
        this.eventBus = eventBus;
        init();
    }
    private void init(){
        initMarkup();
        setStyle();
        initHandlers();
    }

    private void initMarkup(){
        rootDiv = new AbsolutePanel();
        divLeft = new AbsolutePanel();
        divLeft.setHeight("100%");
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

    }

    @SuppressWarnings("deprecation")
    public void add(String title, IsWidget content) {
        final HyperLinkWithHistorySupport linkLabel = new HyperLinkWithHistorySupport();

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
                        eventBus.fireEvent(new ParentTabSelectedEvent(decoratedDivRight.getWidget(i)));
                    }
                    else {
                        decoratedDivRight.getWidget(i).setVisible(false);
                        decoratedDivLeft.getWidget(i).setStyleName("bookmarks-link-non-active");
                    }
                }
            }
        });
    }
    public void addDivLeftClickHandler(ClickHandler handler){
        divLeftButton.addClickHandler(handler);
    }
    public void addDivRightClickHandler(ClickHandler handler){
        divRightButton.addClickHandler(handler);
    }

    private void initHandlers() {

        divLeftButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {

                decoratedDivLeft.setVisible(false);
                divLeftButton.setVisible(false);
                divRightButton.setVisible(true);
                divRight.setStyleName("bookmarks-no-margin");
                divLeft.setStyleName("left-div-invisible");
                rootDiv.setStyleName("root-div-unshadowed");
            }
        });

        divRightButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                decoratedDivLeft.setVisible(true);
                divLeftButton.setVisible(true);
                divRightButton.setVisible(false);

                divLeft.setStyleName("left-div-visible");
                divRight.setStyleName("right-div");
                rootDiv.setStyleName("root-div");
            }
        });
    }


    private void setStyle() {
        divRightButton.setStyleName("right-button");
        divLeftButton.setStyleName("left-button");
        rootDiv.setStyleName("root-div");
        divLeft.setStyleName("left-div-visible");

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
        HyperLinkWithHistorySupport startVerticalTabLabel = (HyperLinkWithHistorySupport)decoratedDivLeft.getWidget(index);
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