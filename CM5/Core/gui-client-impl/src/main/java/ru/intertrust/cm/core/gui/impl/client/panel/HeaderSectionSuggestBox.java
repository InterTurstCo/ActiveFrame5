package ru.intertrust.cm.core.gui.impl.client.panel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;

/**
 * Created with IntelliJ IDEA.
 * User: Timofiy Bilyi
 * Date: 28.11.13
 * Time: 15:20
 * To change this template use File | Settings | File Templates.
 */
public class HeaderSectionSuggestBox implements IsWidget{
    private AbsolutePanel rootSuggestDiv;
    private FocusPanel firstImage;
    private FocusPanel secondImage;
    private AbsolutePanel decoratedListSuggestBox;

    public HeaderSectionSuggestBox(){
        rootSuggestDiv = new AbsolutePanel();
        rootSuggestDiv.setStyleName("root-suggest-div");

        decoratedListSuggestBox = new AbsolutePanel();
        decoratedListSuggestBox.setStyleName("decorated-list-suggest-box");

        firstImage = new FocusPanel();
        firstImage.setStyleName("header-first-action-button");
        firstImage.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if(firstImage.getStyleName().equals("header-first-action-button")){
                    firstImage.addStyleName("header-first-action-button-mute");
                }
                else{
                    firstImage.removeStyleName("header-first-action-button-mute");
                }

            }
        });

        secondImage = new FocusPanel();
        secondImage.setStyleName("header-second-action-button");

        AbsolutePanel sectionSuggestBox = new AbsolutePanel();
        sectionSuggestBox.setStyleName("section-suggest-box");

        AbsolutePanel decoratedSuggestBox = new AbsolutePanel();
        decoratedSuggestBox.setStyleName("decorated-suggest-box");

        TextBox suggestBox = new TextBox();
        suggestBox.setWidth("100%");
        suggestBox.setStyleName("upper-suggestbox");

        sectionSuggestBox.add(decoratedSuggestBox);
        sectionSuggestBox.add(secondImage);
        decoratedSuggestBox.add(suggestBox);

        decoratedListSuggestBox.add(firstImage);
        decoratedListSuggestBox.add(sectionSuggestBox);

        rootSuggestDiv.add(decoratedListSuggestBox);


    }

    @Override
    public Widget asWidget() {
        return rootSuggestDiv;
    }
}
